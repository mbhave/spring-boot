#!/bin/bash
set -e

source $(dirname $0)/common.sh
repository=$(pwd)/distribution-repository

pushd git-repo > /dev/null
./mvnw -f spring-boot-project/pom.xml clean deploy -U -Dfull -DaltDeploymentRepository=distribution::default::file://${repository}
popd > /dev/null
