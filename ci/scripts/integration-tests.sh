#!/bin/bash

cp -r maven-repo-cache/m2 ~/.m2
cd git-repo/spring-boot-integration-tests
mvn install