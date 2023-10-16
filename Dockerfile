# Dockerfile to run jacamo-web application
#
# to build:
#   $ docker build -t jacamo-web:0.5 .
# to run a jacamo-web container:
#   $ docker run -it --name jacamo-web jacamo-web:0.5
#   Browse http://127.0.0.1:8080


FROM openjdk:8-jdk-alpine

ENV PATH $PATH:$JAVA_HOME/bin

# Install necessary linux packages for jacamo-web
RUN apk add --update --no-cache git bash fontconfig ttf-dejavu graphviz nodejs npm

# Clone jacamo-web on internal /app directory
WORKDIR /app
RUN git clone https://github.com/jacamo-lang/jacamo-web.git

# Expose default jacamo ports (for debugging)
EXPOSE 3271
EXPOSE 3272
EXPOSE 3273
# Expose jacamo-web http port (for jacamo-web interface and rest endpoints)
EXPOSE 8080
# Expose jacamo-web websockets port
EXPOSE 8026

# Commands to be executed when the container runs
WORKDIR /app/jacamo-web
ENTRYPOINT ["/app/jacamo-web/gradlew", "run"]
