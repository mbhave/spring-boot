FROM ubuntu:bionic-20190807

ADD setup.sh /setup.sh
ADD get-jdk-url.sh /get-jdk-url.sh
RUN ./setup.sh java8

ENV JAVA_HOME /opt/openjdk
ENV PATH $JAVA_HOME/bin:$PATH
ADD docker-lib.sh /docker-lib.sh

ADD build-release-scripts.sh /build-release-scripts.sh
ADD releasescripts /release-scripts
RUN ./build-release-scripts.sh
ADD spring-boot-release-scripts.jar /spring-boot-release-scripts.jar
ENTRYPOINT [ "switch", "shell=/bin/bash", "--", "codep", "/bin/docker daemon" ]
