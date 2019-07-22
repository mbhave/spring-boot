SET "JAVA_HOME=C:\opt\jdk-8"
SET PATH=%PATH%;C:\Program Files\Git\usr\bin
cd git-repo

echo ".\mvnw clean install" > build.log
.\mvnw clean install -DskipTests >> build.log 2>&1 && exit 1
cd spring-boot-tests/spring-boot-smoke-tests/spring-boot-smoke-test-jetty-jsp
..\..\..\mvnw install