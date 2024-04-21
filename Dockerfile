FROM  openjdk:11-jre

WORKDIR /app
EXPOSE 8080

COPY /build/libs/app*.jar /app/app.jar
COPY ./startup.sh /app/startup.sh

CMD ["/bin/sh","/app/startup.sh"]
