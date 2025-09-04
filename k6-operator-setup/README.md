# k6-operator local (Kind) — Setup rápido

Este pacote cria um cluster Kind (se não existir), instala o k6-operator,
publica seu script `stress/test.js` via ConfigMap e aplica um TestRun
com 3 instâncias (pods) de k6 em paralelo.

## Estrutura
```
k6-operator-setup/
├─ scripts/
│  └─ setup-k6.sh
└─ manifests/
   └─ testrun.tmpl.yaml
```

> Seu script de teste deve estar em: `./stress/test.js` (raiz do projeto).

## Pré-requisitos
- kubectl
- kind
- curl
- envsubst (pacote gettext)

## Uso
```bash
cd k6-operator-setup/scripts
chmod +x setup-k6.sh
./setup-k6.sh
```

Variáveis opcionais:
```bash
TARGET_URL="http://host.docker.internal:8080/health"     INFLUX_URL="http://host.docker.internal:8086/k6"     PARALLELISM=3     NAMESPACE="k6-tests"     ./setup-k6.sh
```

Ver status/logs:
```bash
kubectl -n k6-tests get pods -w
kubectl -n k6-tests get jobs
kubectl -n k6-tests logs job/k6-demo-0
```
