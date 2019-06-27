SET "JAVA_HOME=C:\opt\jdk-8"
SET PATH=%PATH%;C:\Program Files\Git\usr\bin
cd git-repo

tail
./mvnw clean install >> build.log