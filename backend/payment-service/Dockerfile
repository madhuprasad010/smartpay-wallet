FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S smartpay && adduser -S smartpay -G smartpay
USER smartpay
COPY payment-service/target/payment-service-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8085
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8085/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=docker"]
