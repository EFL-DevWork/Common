#!/bin/bash

curl \
--header "X-Vault-Token: ${VAULT_TOKEN}" \
--data '{"data": {"DB_PASSWORD": "'"${DATABASE_PASSWORD}"'","CRYPTO_KEY" : "'"${KEY}"'"}}' \
http://localhost:8200/v1/secret/data/paymentservice