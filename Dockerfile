# Dockerfile to run JaCaMo applications
# based on JaCaMo

# to build:
#    docker build  -t jomifred/jacamo-web .

FROM openjdk:8-jdk-alpine

#ENV JAVA_HOME /usr/lib/jvm/java-1.8-openjdk
#ENV JACAMO_HOME=/jacamo/build
ENV PATH $PATH:$JAVA_HOME/bin #:$JACAMO_HOME/scripts

RUN apk add --update --no-cache git bash fontconfig ttf-dejavu graphviz nodejs npm

# download and run jacamo-web (just to update local maven rep)
RUN git clone https://github.com/jacamo-lang/jacamo-web.git && \
    cd jacamo-web && \
#    ./gradlew run # To deploy on heroku, use build instead
    ./gradlew build # To lauch jacamo-web automatically, use run instead


EXPOSE 3271
EXPOSE 3272
EXPOSE 3273
EXPOSE 8080

WORKDIR /app

#ENTRYPOINT [ "/jacamo/build/scripts/jacamo" ]
CMD ["bash"]
