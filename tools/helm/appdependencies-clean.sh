#!/bin/bash

kubectl delete -f appdependencies/bankservice/kubernetes-secrets/secrets.yaml -n apps

helm delete --namespace apps bankservice
helm delete --namespace apps fraudservice
