# Dockerfile para produção - Radar LGPD API

# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copia arquivos de configuração do Maven
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# Download de dependências (cache layer)
RUN ./mvnw dependency:go-offline -B

# Copia código fonte
COPY src src

# Build da aplicação (skip tests para build mais rápido)
RUN ./mvnw clean package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Cria usuário não-root para segurança
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copia o JAR buildado do stage anterior
COPY --from=build /app/target/*.jar app.jar

# Expõe a porta (será sobrescrita pela variável PORT do Render)
EXPOSE 8080

# Healthcheck (usa PORT se definida, senão 8080)
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/health || exit 1

# Comando de inicialização
# Render passa PORT via variável de ambiente, Spring Boot a detecta automaticamente
# ou podemos usar shell form para expandir variáveis
CMD java \
  -Djava.security.egd=file:/dev/./urandom \
  -Dspring.profiles.active=prod \
  -jar \
  app.jar
