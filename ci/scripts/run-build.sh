#!/bin/bash

cp -r maven-repo-cache/m2 ~/.m2
pushd git-repo
mvn install -DskipTests
popd

echo Copy cache over
cp -r ~/.m2/. m2
ls m2


