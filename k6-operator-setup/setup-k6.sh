#!/usr/bin/env bash
set -euo pipefail

# =========================
# Default configuration (override via env if needed)
# =========================
CLUSTER_NAME="${CLUSTER_NAME:-k6-distributed-test}"
NAMESPACE="${NAMESPACE:-k6-tests}"
PARALLELISM="${PARALLELISM:-3}"
CONFIGMAP_NAME="${CONFIGMAP_NAME:-k6-script}"
TESTRUN_NAME="${TESTRUN_NAME:-k6-demo}"

# Resources per k6 runner pod
REQ_CPU="${REQ_CPU:-500m}"
REQ_MEM="${REQ_MEM:-256Mi}"
LIM_CPU="${LIM_CPU:-1000m}"
LIM_MEM="${LIM_MEM:-512Mi}"

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
MANIFESTS_DIR="${MANIFESTS_DIR:-${ROOT_DIR}/k6-operator-setup/manifests}"
TEMPLATE="${MANIFESTS_DIR}/testrun.tmpl.yaml"

# =========================
# Pre-checks
# =========================
need() {
  command -v "$1" >/dev/null 2>&1 || { echo "ERROR: '$1' not found in PATH"; exit 1; }
}
need kubectl
need kind
need curl
need envsubst

if [[ ! -f "${ROOT_DIR}/stress/test.js" ]]; then
  echo "ERROR: k6 script not found at '${ROOT_DIR}/stress/test.js'."
  echo "       Make sure your test is at that path."
  exit 1
fi

if [[ ! -f "$TEMPLATE" ]]; then
  echo "ERROR: template not found at '$TEMPLATE'"
  exit 1
fi

# =========================
# 1) Create Kind cluster (if not exists)
# =========================
if ! kind get clusters 2>/dev/null | grep -qx "$CLUSTER_NAME"; then
  echo ">> Creating Kind cluster: $CLUSTER_NAME"
  kind create cluster --name "$CLUSTER_NAME"
else
  echo ">> Kind cluster '$CLUSTER_NAME' already exists"
fi

# =========================
# 2) Install k6-operator
# =========================
echo ">> Installing/updating k6-operator..."
curl -fsSL https://raw.githubusercontent.com/grafana/k6-operator/main/bundle.yaml | kubectl apply -f -
echo ">> Waiting for k6-operator to be ready..."
kubectl -n k6-operator-system rollout status deploy/k6-operator-controller-manager

# =========================
# 3) Namespace for tests

# =========================
if ! kubectl get ns "$NAMESPACE" >/dev/null 2>&1; then
  echo ">> Creating namespace: $NAMESPACE"
  kubectl create ns "$NAMESPACE"
else
  echo ">> Namespace '$NAMESPACE' already exists"
fi
# =========================
# 3.1) Wire external 'app' (docker-compose) into this namespace
#      We create a Service+Endpoints named 'app' -> ${APP_HOST_IP}:${APP_PORT}
# =========================
APP_HOST_IP="${APP_HOST_IP:-172.19.0.1}"
APP_PORT="${APP_PORT:-8080}"
EXTERNAL_APP_TMPL="${MANIFESTS_DIR}/external-app.tmpl.yaml"

# =========================
# 3.2) Wire external 'influxdb' (docker-compose) into this namespace
#      Service+Endpoints named 'influxdb' -> ${INFLUX_HOST_IP}:${INFLUX_PORT}
# =========================
INFLUX_HOST_IP="${INFLUX_HOST_IP:-172.19.0.1}"
INFLUX_PORT="${INFLUX_PORT:-8086}"
EXTERNAL_INFLUX_TMPL="${MANIFESTS_DIR}/external-influx.tmpl.yaml"

if [[ -f "$EXTERNAL_INFLUX_TMPL" ]]; then
  echo ">> Creating Service+Endpoints 'influxdb' -> ${INFLUX_HOST_IP}:${INFLUX_PORT} in ns ${NAMESPACE}"
  env NAMESPACE="$NAMESPACE" TESTRUN_NAME="$TESTRUN_NAME" PARALLELISM="$PARALLELISM" CONFIGMAP_NAME="$CONFIGMAP_NAME" REQ_CPU="$REQ_CPU" REQ_MEM="$REQ_MEM" LIM_CPU="$LIM_CPU" LIM_MEM="$LIM_MEM" INFLUX_PORT="$INFLUX_PORT" envsubst < "$TEMPLATE" | kubectl apply -f -
else
  echo "WARN: external-influx.tmpl.yaml not found at '$EXTERNAL_INFLUX_TMPL' — skipping external influx wiring"
fi

if [[ -f "$EXTERNAL_APP_TMPL" ]]; then
  echo ">> Creating Service+Endpoints 'app' -> ${APP_HOST_IP}:${APP_PORT} in ns ${NAMESPACE}"
  env NAMESPACE="$NAMESPACE" APP_HOST_IP="$APP_HOST_IP" APP_PORT="$APP_PORT" envsubst < "$EXTERNAL_APP_TMPL" | kubectl apply -f -
else
  echo "WARN: external-app.tmpl.yaml not found at '$EXTERNAL_APP_TMPL' — skipping external service wiring"
fi

# echo ">> Preflight: DNS+HTTP check to influxdb"
# kubectl -n "$NAMESPACE" run -it --rm k6-preflight-influx --image=curlimages/curl --restart=Never -- \
#   sh -lc 'set -e; getent hosts influxdb; curl -sSI http://influxdb:8086/ping | head -n1' || {
#     echo "ERROR: influxdb not reachable from namespace '$NAMESPACE'.";
#     echo "       Check INFLUX_HOST_IP (current: $INFLUX_HOST_IP) and that your docker-compose InfluxDB is UP.";
#     exit 1;
# }

# echo ">> Preflight: DNS+HTTP check to app"
# kubectl -n "$NAMESPACE" run -it --rm k6-preflight-app --image=curlimages/curl --restart=Never -- \
#   sh -lc 'set -e; getent hosts app; curl -sSI http://app:8080 | head -n1' || {
#     echo "ERROR: app not reachable from namespace '$NAMESPACE'.";
#     echo "       Check APP_HOST_IP (current: $APP_HOST_IP) and that your app is UP.";
#     exit 1;
# }
# =========================
# 4) ConfigMap with k6 script
# =========================
echo ">> Creating/updating ConfigMap '${CONFIGMAP_NAME}' from 'stress/test.js'"
kubectl -n "$NAMESPACE" create configmap "$CONFIGMAP_NAME"       --from-file "test.js=${ROOT_DIR}/stress/test.js"       --dry-run=client -o yaml | kubectl apply -f -

# =========================
# 5) Render and apply TestRun (3 instances by default)
# =========================
echo ">> Applying TestRun '${TESTRUN_NAME}' with parallelism=${PARALLELISM}"
NAMESPACE="$NAMESPACE" TESTRUN_NAME="$TESTRUN_NAME" PARALLELISM="$PARALLELISM"     CONFIGMAP_NAME="$CONFIGMAP_NAME" REQ_CPU="$REQ_CPU" REQ_MEM="$REQ_MEM" LIM_CPU="$LIM_CPU" LIM_MEM="$LIM_MEM"     envsubst < "$TEMPLATE" | kubectl apply -f -

echo
echo "=============================================="
echo "✅ TestRun created: ${TESTRUN_NAME} (ns: ${NAMESPACE})"
echo "   Parallelism : ${PARALLELISM}"
echo "   Output      : (none - check k6 runner pod logs)"
echo "----------------------------------------------"
echo "Watch pods:"
echo "  kubectl -n ${NAMESPACE} get pods -w"
echo
echo "See logs (once a job/pod starts):"
echo "  kubectl -n ${NAMESPACE} get jobs"
echo "  kubectl -n ${NAMESPACE} logs job/${TESTRUN_NAME}-0"
echo "=============================================="