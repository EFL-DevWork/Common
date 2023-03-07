#!/bin/bash

kubectl apply -f paymentservice/kubernetes-secrets/secrets.yaml -n apps

helm upgrade --install --namespace apps paymentservice paymentservice --values paymentservice/values.yaml