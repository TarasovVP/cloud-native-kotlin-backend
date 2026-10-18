FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

RUN useradd --system --uid 10001 appuser

COPY --chown=appuser:appuser server/build/libs/cloud-native-kotlin-backend.jar app.jar

ENV PORT=8080
EXPOSE 8080

USER appuser

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
