
FROM gradle:8.8-jdk17 AS builder
WORKDIR /workspace
COPY . /workspace
RUN gradle clean bootJar -x test

FROM eclipse-temurin:17-jre
ENV TZ=Asia/Seoul \
    JAVA_OPTS="-XX:MaxRAMPercentage=75 -Duser.timezone=Asia/Seoul" \
    SPRING_PROFILES_ACTIVE=prod
WORKDIR /app
COPY --from=builder /workspace/build/libs/*.jar /app/app.jar
EXPOSE 8080
USER 1000
ENTRYPOINT ["sh", "-lc", "java $JAVA_OPTS -jar /app/app.jar"]