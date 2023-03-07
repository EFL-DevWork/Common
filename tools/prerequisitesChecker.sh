#!/bin/bash
# shellcheck disable=SC2006
JAVA_VERSION=`java -version 2>&1 |awk 'NR==1{print $1}'`
NOT_FOUND="prerequisitesChecker.sh:"
if [ "$JAVA_VERSION" == "$NOT_FOUND" ]
then
    echo "install java"
else
    echo "java is already installed"
fi
DOCKER_VERSION=`docker --version 2>&1 |awk 'NR==1{print $1}'`
if [ "$DOCKER_VERSION" == "$NOT_FOUND" ]
then
    echo "install docker"
else
    echo "docker is already installed"
fi
POSTGRES_VERSION=`psql --version 2>&1 |awk 'NR==1{ print $1}'`
if [ "$POSTGRES_VERSION" == "$NOT_FOUND" ]
then
    echo "install postgres"
else
    echo "postgres is already installed"
fi