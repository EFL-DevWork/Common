#!/bin/bash

kubectl apply -f appdependencies/bankservice/kubernetes-secrets/secrets.yaml -n apps
helm upgrade --install --namespace apps bankservice appdependencies/bankservice --values appdependencies/bankservice/values.yaml
helm upgrade --install --namespace apps fraudservice appdependencies/fraudservice --values appdependencies/fraudservice/values.yaml