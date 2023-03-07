-include .env 

CMD = up -d
DEMO_PATH = tools/demo
DOCKER_CMD = docker exec -it postgres

clean:
	./gradlew clean

init-db:
ifeq ($(DB_MODE),local)
	$(eval DOCKER_CMD := )
endif
	${DOCKER_CMD} psql -U postgres -c "create user ${DB_USER}  password '${DB_PASSWORD}';";\
	${DOCKER_CMD} psql -U postgres -c "create database ${DB_NAME} owner=${DB_USER};";\

build: clean
	./gradlew build

boot-run:
	./gradlew bootrun

run-checkstyle:
	./gradlew checkstyleMain checkstyleTest

tests:
	./gradlew test

copy-env:
	cp -n $(DEMO_PATH)/env/sample.env .env || true

vault-unseal:
	. ./$(DEMO_PATH)/vault/unseal-vault.sh

vault-add-secrets:
	. ./$(DEMO_PATH)/vault/add-secrets.sh

seed-data:
	sh $(DEMO_PATH)/seed-data/seed.sh

dependencyCheck:
	./gradlew dependencyCheckAnalyze

spotbugs:
	./gradlew spotbugsMain spotbugsTest

ci: clean run-checkstyle spotbugs build

docker-build:
	NETWORK_NAME=$(NETWORK_NAME) docker-compose -f $(DEMO_PATH)/docker-compose.app-payment.yml build

docker-app: docker-build
	NETWORK_NAME=$(NETWORK_NAME) docker-compose -f $(DEMO_PATH)/docker-compose.app-payment.yml $(CMD)

docker-app-deps:
	NETWORK_NAME=$(NETWORK_NAME) docker-compose -f $(DEMO_PATH)/docker-compose.app-dependencies.yml $(CMD)

docker-infra-elk:
	NETWORK_NAME=$(NETWORK_NAME) docker-compose -f $(DEMO_PATH)/docker-compose.infra-elk.yml $(CMD)

docker-infra-postgres:
	NETWORK_NAME=$(NETWORK_NAME) docker-compose -f $(DEMO_PATH)/docker-compose.infra-postgres.yml $(CMD)

docker-infra-config:
	CONFIG_SERVER_USERNAME=$(CONFIG_SERVER_USERNAME) CONFIG_SERVER_PASSWORD=$(CONFIG_SERVER_PASSWORD) NETWORK_NAME=$(NETWORK_NAME) docker-compose -f $(DEMO_PATH)/docker-compose.infra-central-config.yml $(CMD)

docker-infra-vault:
	NETWORK_NAME=$(NETWORK_NAME) docker-compose -f $(DEMO_PATH)/docker-compose.infra-vault.yml $(CMD)

docker-infra-metrics:
	NETWORK_NAME=$(NETWORK_NAME) docker-compose -f $(DEMO_PATH)/docker-compose.infra-metrics.yml $(CMD)

docker-infra-tracing:
	NETWORK_NAME=$(NETWORK_NAME) docker-compose -f $(DEMO_PATH)/docker-compose.infra-tracing.yml $(CMD)

docker-infra-keycloak:
	KEYCLOAK_USER=$(KEYCLOAK_USER) KEYCLOAK_PASSWORD=$(KEYCLOAK_PASSWORD) NETWORK_NAME=$(NETWORK_NAME) docker-compose -f $(DEMO_PATH)/docker-compose.infra-keyclock.yml $(CMD)

deploy-local: copy-env init-db build boot-run

demo:
	@source $(DEMO_PATH)/scripts/demo.sh && demo
