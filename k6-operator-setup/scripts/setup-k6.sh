#!/usr/bin/env bash
set -euo pipefail

# =========================
# Configurações padrão (pode sobrescrever via env)
# =========================
CLUSTER_NAME="${CLUSTER_NAME:-k6-distributed-tes}"
NAMESPACE="${NAMESPACE:-k6-tests}"
PARALLELISM="${PARALLELISM:-3}"
CONFIGMAP_NAME="${CONFIGMAP_NAME:-k6-script}"
TESTRUN_NAME="${TESTRUN_NAME:-k6-demo}"

# TARGET_URL: endpoint da sua aplicação (HTTP)
TARGET_URL="${TARGET_URL:-http://host.docker.internal:8080/actuator/health}"

# (Opcional) output do k6 para InfluxDB v1. Ex.: http://host.docker.internal:8086/k6
INFLUX_URL="${INFLUX_URL:-}"

# Recursos por pod k6 runner
REQ_CPU="${REQ_CPU:-500m}"
REQ_MEM="${REQ_MEM:-256Mi}"
LIM_CPU="${LIM_CPU:-1000m}"
LIM_MEM="${LIM_MEM:-512Mi}"

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
MANIFESTS_DIR="${MANIFESTS_DIR:-${ROOT_DIR}/manifests}"
TEMPLATE="${MANIFESTS_DIR}/testrun.tmpl.yaml"

# =========================
# Pré-checagens
# =========================
need() {
  command -v "$1" >/dev/null 2>&1 || { echo "ERRO: '$1' não encontrado no PATH"; exit 1; }
}
need kubectl
need kind
need curl
need envsubst

if [[ ! -f "${ROOT_DIR}/stress/test.js" ]]; then
  echo "ERRO: script k6 não encontrado em '${ROOT_DIR}/stress/test.js'."
  echo "      Ajuste a estrutura do projeto ou exporte SCRIPT_PATH e altere o comando de ConfigMap manualmente."
  exit 1
fi

if [[ ! -f "$TEMPLATE" ]]; then
  echo "ERRO: template não encontrado em '$TEMPLATE'"
  exit 1
fi

# =========================
# 1) Criar cluster Kind (se não existir)
# =========================
if ! kind get clusters 2>/dev/null | grep -qx "$CLUSTER_NAME"; then
  echo ">> Criando cluster Kind: $CLUSTER_NAME"
  kind create cluster --name "$CLUSTER_NAME"
else
  echo ">> Cluster Kind '$CLUSTER_NAME' já existe"
fi

# =========================
# 2) Instalar k6-operator
# =========================
echo ">> Instalando/atualizando k6-operator..."
curl -fsSL https://raw.githubusercontent.com/grafana/k6-operator/main/bundle.yaml | kubectl apply -f -
echo ">> Aguardando k6-operator ficar pronto..."
kubectl -n k6-operator-system rollout status deploy/k6-operator-controller-manager

# =========================
# 3) Namespace para os testes
# =========================
if ! kubectl get ns "$NAMESPACE" >/dev/null 2>&1; then
  echo ">> Criando namespace: $NAMESPACE"
  kubectl create ns "$NAMESPACE"
else
  echo ">> Namespace '$NAMESPACE' já existe"
fi

# =========================
# 4) ConfigMap com o script do k6
# =========================
echo ">> Criando/atualizando ConfigMap '${CONFIGMAP_NAME}' a partir de 'stress/test.js'"
kubectl -n "$NAMESPACE" create configmap "$CONFIGMAP_NAME"       --from-file "test.js=${ROOT_DIR}/stress/test.js"       --dry-run=client -o yaml | kubectl apply -f -

# =========================
# 5) Renderizar e aplicar TestRun (3 instâncias por padrão)
# =========================
TMP_RENDER="$(mktemp)"
cp "$TEMPLATE" "$TMP_RENDER"

if [[ -n "$INFLUX_URL" ]]; then
  INFLUX_BLOCK=$'  arguments: >\n    --out influxdb='"${INFLUX_URL}"
  # substitui marcador por bloco
  sed -i "s|#__INFLUX_ARGS__|${INFLUX_BLOCK//|/\|}|" "$TMP_RENDER"
else
  # remove marcador
  sed -i "s|#__INFLUX_ARGS__||" "$TMP_RENDER"
fi

echo ">> Aplicando TestRun '${TESTRUN_NAME}' com parallelism=${PARALLELISM}"
NAMESPACE="$NAMESPACE" TESTRUN_NAME="$TESTRUN_NAME" PARALLELISM="$PARALLELISM" \ 
CONFIGMAP_NAME="$CONFIGMAP_NAME" TARGET_URL="$TARGET_URL" \ 
REQ_CPU="$REQ_CPU" REQ_MEM="$REQ_MEM" LIM_CPU="$LIM_CPU" LIM_MEM="$LIM_MEM" \ 
envsubst < "$TMP_RENDER" | kubectl apply -f -

echo
echo "=============================================="
echo "✅ TestRun criado: ${TESTRUN_NAME} (ns: ${NAMESPACE})"
echo "   Paralelismo: ${PARALLELISM}"
echo "   TARGET_URL : ${TARGET_URL}"
if [[ -n "$INFLUX_URL" ]]; then
  echo "   Output     : ${INFLUX_URL}"
else
  echo "   Output     : (nenhum - logs apenas)"
fi
echo "----------------------------------------------"
echo "Acompanhe os pods:"
echo "  kubectl -n ${NAMESPACE} get pods -w"
echo
echo "Veja logs (quando um job/pod iniciar):"
echo "  kubectl -n ${NAMESPACE} get jobs"
echo "  kubectl -n ${NAMESPACE} logs job/${TESTRUN_NAME}-0"
echo "=============================================="
