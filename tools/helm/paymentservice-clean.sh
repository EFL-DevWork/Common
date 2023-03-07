#!/bin/bash

kubectl delete -f paymentservice/kubernetes-secrets/secrets.yaml -n apps

helm delete --namespace apps paymentservice