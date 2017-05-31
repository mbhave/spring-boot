#!/bin/bash
set -e

destination=$1
if [ -z "$destination" ]; then
  echo "usage: $0 <path/to/destination>" >&2
  exit 1
fi

export MAVEN_OPTS=-Xmx1024m
mvn -Dmaven.repo.local=${destination} -Pfast -DskipTests install dependency:go-offline
mvn -Dmaven.repo.local=${destination} -f spring-boot-samples/pom.xml dependency:go-offline
2>/dev/null 1>&2 rm -fr $destination/org/springframework/boot
2>/dev/null 1>&2 find $destination -type d -name "*SNAPSHOT" -exec rm -fr {} \;
