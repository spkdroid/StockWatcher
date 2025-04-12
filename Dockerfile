FROM openjdk:21-jdk-slim

ENV GRADLE_USER_HOME=/home/app/.gradle

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    libxext6 libxrender1 libxtst6 libxi6 libfreetype6 libxrandr2 libgtk-3-0 ca-certificates curl unzip && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY . .

RUN chmod +x ./gradlew

RUN ./gradlew assemble

CMD ["./gradlew", "run"]
