FROM openjdk:23-jdk-slim

WORKDIR /app

COPY target/dungeon-game-1.0-SNAPSHOT.jar .
RUN chmod +x dungeon-game-1.0-SNAPSHOT.jar

EXPOSE 8080

CMD ["java", "-jar", "dungeon-game-1.0-SNAPSHOT.jar"]