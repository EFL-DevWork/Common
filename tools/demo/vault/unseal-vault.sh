function get_data(){
export VAULT_ADDR="http://localhost:8200"
data=$(curl \
    --request POST \
    --data '{"secret_shares": 1, "secret_threshold": 1}' \
    ${VAULT_ADDR}/v1/sys/init | jq -r '.keys_base64[0], .root_token')

    UNSEAL_KEY="$(echo "$data" | sed -n 1p)"
    VAULT_TOKEN="$(echo "$data" | sed -n 2p)"

    if [[ ${UNSEAL_KEY} = null ||  ${VAULT_TOKEN} = null ]];
    then
        echo "Vault is already initialized!"
        exit
    fi
    echo "Unseal key : ${UNSEAL_KEY}"
    echo "Root Token : ${VAULT_TOKEN}"
}
function unseal_vault() {
    curl \
    --request POST \
    --data "{\"key\": \"${UNSEAL_KEY}\"}" \
    ${VAULT_ADDR}/v1/sys/unseal | jq

}

function enable_secret() {
    curl \
        --header "X-Vault-Token: ${1}" \
        --request POST \
        --data '{ "type":"kv-v2" }' \
        ${VAULT_ADDR}/v1/sys/mounts/secret
}

function create_policy() {
    curl --header "X-Vault-Token: ${1}" \
       --request PUT \
       --data '{"policy": "path \"secret/*\" { capabilities = [\"read\", \"list\"] }, path \"secret/\" { capabilities = [\"read\", \"list\"] }"}'\
       ${VAULT_ADDR}/v1/sys/policy/readPolicy
}

function enable_appRole() {
    curl \
        --header "X-Vault-Token: ${1}" \
        --request POST \
        --data '{"type": "approle"}' \
        ${VAULT_ADDR}/v1/sys/auth/approle
}

function create_role() {
    curl \
        --header "X-Vault-Token: ${1}" \
        --request POST \
        --data '{"secret_id_ttl": "525600m","secret_id_num_uses": "0","policies": "default,readpolicy"}' \
        ${VAULT_ADDR}/v1/auth/approle/role/adminrole
}

function get_roleid() {
    role_id=$(curl \
    --header "X-Vault-Token: ${1}" \
    ${VAULT_ADDR}/v1/auth/approle/role/adminrole/role-id | jq -r '.data["role_id"]')
    echo "Role Id : ${role_id}"
}

function get_secretid() {
    secret_id=$(curl \
        --header "X-Vault-Token: ${1}" \
        --request POST \
        ${VAULT_ADDR}/v1/auth/approle/role/adminrole/secret-id | jq -r '.data["secret_id"]')
    echo "Secret Id : ${secret_id}"
}

configure() {
    sleep 2m
    get_data
    unseal_vault
    enable_secret ${VAULT_TOKEN}
    create_policy ${VAULT_TOKEN}
    enable_appRole ${VAULT_TOKEN}
    create_role ${VAULT_TOKEN}
    get_roleid ${VAULT_TOKEN}
    get_secretid ${VAULT_TOKEN}
}

configure
