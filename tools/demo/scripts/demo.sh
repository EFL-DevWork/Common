#! /bin/bash

demo(){

    #### VARIABLES #####
    GREEN='\033[0;32m'
    NC='\033[0m' # No Color
    # selected options

    # export variables
    echo "Enter Database password you want to use for demo: "; read -r DATABASE_PASSWORD; export DATABASE_PASSWORD
    echo "Enter Key for encryption of sensitive data(length = 16 or 32): "; read -r KEY; export KEY
    wait

    #### FUNCTIONS #####
    echo "${GREEN}Copying sample.env to .env...${NC}"
    make copy-env
    echo "${GREEN}Starting postgres container...${NC}"
    make docker-infra-postgres
    echo "${GREEN}Starting demo downstream services containers...${NC}"
    make docker-app-deps
    echo "${GREEN}Creating database for demo service...${NC}"
    sleep 3
    make init-db
    echo "${GREEN}Starting main demo service...${NC}"
    make docker-app
}

