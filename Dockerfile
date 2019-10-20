# Dockerfile to run JaCaMo applications
# based on JaCaMo SNAPSHOT release

# to build:
#    docker build  -t jomifred/jacamo-runrest .

FROM alpine

ENV JAVA_HOME /usr/lib/jvm/java-1.8-openjdk
#ENV JACAMO_HOME=/jacamo/build
ENV PATH $PATH:$JAVA_HOME/bin #:$JACAMO_HOME/scripts

RUN apk add --update --no-cache git gradle openjdk8-jre bash fontconfig ttf-dejavu graphviz

# download and run jacamo-web (just to update local maven rep)
RUN git clone https://github.com/jacamo-lang/jacamo-web.git && \
    cd jacamo-web && \
    gradle build

EXPOSE 3271
EXPOSE 3272
EXPOSE 3273
EXPOSE 8080

WORKDIR /app

#ENTRYPOINT [ "/jacamo/build/scripts/jacamo" ]
CMD ["bash"]
