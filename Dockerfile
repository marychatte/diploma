FROM ubuntu:latest
CMD ["/bin/bash"]
RUN apt-get -yq update
RUN apt-get -yq install openjdk-17-jdk make git lsof unzip gcc
ENV JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
RUN git clone https://github.com/wg/wrk.git /usr/wrk
WORKDIR /usr/wrk
RUN make
RUN cp wrk /usr/local/bin
COPY build/libs/diploma-1.0-SNAPSHOT.jar /build/libs/diploma-1.0-SNAPSHOT.jar
COPY wrk /wrk
COPY results /results
WORKDIR /
