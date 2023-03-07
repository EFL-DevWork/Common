FROM openjdk:17-alpine as builder
RUN apk update && apk add --no-cache make
WORKDIR /service
# COPY build.gradle settings.gradle gradlew gradlew.bat /service/
# COPY gradle /service/gradle
# # hack to cache dependencies
# # https://stackoverflow.com/questions/25873971/docker-cache-gradle-dependencies
# RUN ./gradlew build 2>/dev/null || true
COPY . .
RUN make build

FROM eclipse-temurin:17-jre-alpine
COPY --from=builder /service/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]