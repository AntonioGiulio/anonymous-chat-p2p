FROM alpine/git
WORKDIR /app
RUN git clone https://github.com/AntonioGiulio/anonymous-chat-p2p.git

FROM maven:3.5-jdk-8-alpine
WORKDIR /app
COPY --from=0 /app/anonymous-chat-p2p/p2p_anonymous_chat /app
RUN mvn package

FROM openjdk:8-jre-alpine
WORKDIR /app
ENV MASTERIP=127.0.0.1
ENV ID=0

COPY --from=1 /app/target/p2p_anonymous_chat-0.0.1-SNAPSHOT-final.jar /app
CMD /usr/bin/java -jar p2p_anonymous_chat-0.0.1-SNAPSHOT-final.jar -m $MASTERIP -id $ID