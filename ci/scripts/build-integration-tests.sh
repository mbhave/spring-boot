#!/bin/bash
set -e

pwd
ls -la
source $(dirname $0)/common.sh
repository=/distribution-repository

pushd /git-repo > /dev/null
run_maven -f spring-boot-tests/spring-boot-integration-tests/pom.xml clean install -U -Dfull -Drepository=file://${repository}
popd > /dev/null
