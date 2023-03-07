kubectl apply -f postgresclient/configmap.yml -n infra
kubectl apply -f postgresclient/deployment.yml -n infra
kubectl apply -f postgresclient/secrets.yml -n infra

