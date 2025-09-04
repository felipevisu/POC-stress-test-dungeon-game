# k6-operator local (Kind) — Quick Setup

This package creates a Kind cluster (if it does not exist), installs the k6-operator,
publishes your script `stress/test.js` as a ConfigMap, and applies a TestRun
with 3 instances (pods) of k6 running in parallel.

## Structure
```
k6-operator-setup/
├─ setup-k6.sh
└─ manifests/
   └─ testrun.tmpl.yaml
```

> Your test script should be located at: `./stress/test.js` (project root).

## Requirements
- kubectl
- kind
- curl
- envsubst (from gettext package)

## Usage
```bash
cd k6-operator-setup/
chmod +x setup-k6.sh
./setup-k6.sh
```

Optional environment variables:
```bash
TARGET_URL="http://host.docker.internal:8080/health"     INFLUX_URL="http://host.docker.internal:8086/k6"     PARALLELISM=3     NAMESPACE="k6-tests"     ./setup-k6.sh
```

Monitor status/logs:
```bash
kubectl -n k6-tests get pods -w
kubectl -n k6-tests get jobs
kubectl -n k6-tests logs job/k6-demo-0
```