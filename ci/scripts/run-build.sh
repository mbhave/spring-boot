#!/bin/bash

cp -r maven-repo-cache/m2 ~/.m2
cd git-repo
mvn install -DskipTests

echo Copy cache over
cp -r ~/.m2/. m2
ls m2


