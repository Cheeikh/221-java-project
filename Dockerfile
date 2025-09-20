# Dockerfile multi-stage optimisé pour application Spring Boot
# Stage 1: Build de l'application
FROM maven:3.8.6-openjdk-11-slim AS build

# Définir le répertoire de travail
WORKDIR /app

# Variables d'environnement Maven pour optimiser le build
ENV MAVEN_OPTS="-Xmx1024m -XX:+UseParallelGC"

# Copier les fichiers de configuration Maven
COPY pom.xml .

# Télécharger les dépendances (utilise le cache Docker si pom.xml n'a pas changé)
RUN mvn dependency:go-offline -B --fail-fast

# Copier le code source
COPY src ./src

# Construire l'application avec optimisations
RUN mvn clean package -DskipTests -Dmaven.test.skip=true --fail-fast

# Stage 2: Image de production optimisée
FROM openjdk:11-jre-slim

# Variables d'environnement Java optimisées
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseParallelGC -XX:+UseContainerSupport"
ENV SPRING_PROFILES_ACTIVE=production

# Créer un utilisateur non-root pour la sécurité
RUN groupadd -r spring && useradd -r -g spring spring

# Installer les outils nécessaires en une seule couche
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/* && \
    apt-get clean

# Définir le répertoire de travail
WORKDIR /app

# Copier le JAR depuis le stage de build et changer la propriété en une seule étape
COPY --from=build /app/target/*.jar app.jar
RUN chown spring:spring app.jar

# Passer à l'utilisateur non-root
USER spring

# Exposer le port
EXPOSE 8080

# Health check optimisé
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Commande de démarrage optimisée
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
