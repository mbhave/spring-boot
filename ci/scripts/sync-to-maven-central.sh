#!/bin/bash

BUILD_INFO=$( cat artifactory-repo/build-info.json )

java -jar /spring-boot-release-scripts.jar syncToCentral "RELEASE" $BUILD_INFO > /dev/null || { exit 1; }

echo "Sync complete"
echo $version > version/version
