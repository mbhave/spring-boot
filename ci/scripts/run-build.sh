#!/bin/sh

cd java-cache-test
docker build -t springio/java-cache-resource .

#cp -r maven-repo-cache/m2 ~/.m2
#cd git-repo
#mvn install -DskipTests
#
#cp -r ~/.m2 m2

