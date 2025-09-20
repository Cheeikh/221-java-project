# 🐳 Guide Docker

Ce guide détaille l'utilisation de Docker pour l'application Spring Boot Demo.

## 📋 Table des matières

- [Vue d'ensemble](#vue-densemble)
- [Dockerfile](#dockerfile)
- [Docker Compose](#docker-compose)
- [Images Docker](#images-docker)
- [Volumes et réseaux](#volumes-et-réseaux)
- [Commandes utiles](#commandes-utiles)
- [Optimisation](#optimisation)
- [Sécurité](#sécurité)
- [Dépannage](#dépannage)

## 🎯 Vue d'ensemble

L'application utilise Docker pour :
- **Containerisation** de l'application Spring Boot
- **Développement local** avec Docker Compose
- **Déploiement** sur Render
- **CI/CD** avec Jenkins

## 📄 Dockerfile

### Structure du Dockerfile

```dockerfile
# Stage 1: Build de l'application
FROM maven:3.8.6-openjdk-11-slim AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Image de production
FROM openjdk:11-jre-slim
RUN groupadd -r spring && useradd -r -g spring spring
RUN apt-get update && apt-get install -y --no-install-recommends curl
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
RUN chown spring:spring app.jar
USER spring
EXPOSE 8080
ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV SPRING_PROFILES_ACTIVE=production
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

### Explication des étapes

#### Stage 1 - Build
- **Base** : `maven:3.8.6-openjdk-11-slim`
- **Objectif** : Compiler l'application Maven
- **Optimisation** : Cache des dépendances avec `dependency:go-offline`

#### Stage 2 - Production
- **Base** : `openjdk:11-jre-slim`
- **Sécurité** : Utilisateur non-root
- **Health Check** : Vérification de santé automatique
- **Optimisation** : Image minimale avec JRE uniquement

## 🐙 Docker Compose

### Configuration complète

```yaml
version: '3.8'

services:
  app:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: spring-boot-demo
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=development
      - JAVA_OPTS=-Xmx512m -Xms256m
    volumes:
      - ./logs:/app/logs
    networks:
      - app-network
    depends_on:
      - redis
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s

  redis:
    image: redis:7-alpine
    container_name: redis-cache
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data
    networks:
      - app-network
    restart: unless-stopped
    command: redis-server --appendonly yes

  nginx:
    image: nginx:alpine
    container_name: nginx-proxy
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf:ro
      - ./nginx/ssl:/etc/nginx/ssl:ro
    networks:
      - app-network
    depends_on:
      - app
    restart: unless-stopped

  prometheus:
    image: prom/prometheus:latest
    container_name: prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./monitoring/prometheus.yml:/etc/prometheus/prometheus.yml:ro
      - prometheus-data:/prometheus
    networks:
      - app-network
    restart: unless-stopped

  grafana:
    image: grafana/grafana:latest
    container_name: grafana
    ports:
      - "3000:3000"
    volumes:
      - grafana-data:/var/lib/grafana
      - ./monitoring/grafana/dashboards:/etc/grafana/provisioning/dashboards:ro
      - ./monitoring/grafana/datasources:/etc/grafana/provisioning/datasources:ro
    networks:
      - app-network
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin123
    restart: unless-stopped

volumes:
  redis-data:
    driver: local
  prometheus-data:
    driver: local
  grafana-data:
    driver: local

networks:
  app-network:
    driver: bridge
```

### Services inclus

1. **app** - Application Spring Boot principale
2. **redis** - Cache Redis
3. **nginx** - Reverse proxy
4. **prometheus** - Monitoring et métriques
5. **grafana** - Visualisation des métriques

## 🏷️ Images Docker

### Construction des images

```bash
# Login Docker Hub
echo $DOCKER_PASSWORD | docker login -u $DOCKER_USERNAME --password-stdin

# Image de base
docker build -t $DOCKER_USERNAME/spring-boot-demo .

# Image avec tag spécifique
docker build -t $DOCKER_USERNAME/spring-boot-demo:v1.0.0 .

# Image pour production
docker build -t $DOCKER_USERNAME/spring-boot-demo:prod --target production .
```

### Tags et versions

```bash
# Tagging multiple
docker tag spring-boot-demo:latest $DOCKER_USERNAME/spring-boot-demo:latest
docker tag spring-boot-demo:latest $DOCKER_USERNAME/spring-boot-demo:v1.0.0
docker tag spring-boot-demo:latest $DOCKER_USERNAME/spring-boot-demo:dev

# Push vers Docker Hub
docker push $DOCKER_USERNAME/spring-boot-demo:latest
docker push $DOCKER_USERNAME/spring-boot-demo:v1.0.0
```

### Gestion des images

```bash
# Lister les images
docker images

# Supprimer une image
docker rmi spring-boot-demo:latest

# Nettoyer les images inutilisées
docker image prune -a

# Inspecter une image
docker inspect spring-boot-demo:latest
```

## 💾 Volumes et réseaux

### Volumes

```bash
# Créer un volume
docker volume create app-data

# Lister les volumes
docker volume ls

# Inspecter un volume
docker volume inspect app-data

# Supprimer un volume
docker volume rm app-data
```

### Réseaux

```bash
# Créer un réseau
docker network create app-network

# Lister les réseaux
docker network ls

# Inspecter un réseau
docker network inspect app-network

# Connecter un conteneur à un réseau
docker network connect app-network container-name
```

## 🛠️ Commandes utiles

### Gestion des conteneurs

```bash
# Démarrer un conteneur
docker run -d -p 8080:8080 --name spring-app spring-boot-demo

# Arrêter un conteneur
docker stop spring-app

# Redémarrer un conteneur
docker restart spring-app

# Supprimer un conteneur
docker rm spring-app

# Voir les logs
docker logs -f spring-app

# Exécuter une commande dans le conteneur
docker exec -it spring-app bash
```

### Docker Compose

```bash
# Démarrer tous les services
docker-compose up -d

# Démarrer un service spécifique
docker-compose up -d app

# Voir les logs
docker-compose logs -f app

# Redémarrer un service
docker-compose restart app

# Arrêter tous les services
docker-compose down

# Reconstruire les images
docker-compose build --no-cache

# Voir le statut
docker-compose ps
```

### Debugging

```bash
# Entrer dans un conteneur
docker exec -it spring-app sh

# Voir les processus
docker exec spring-app ps aux

# Voir l'utilisation des ressources
docker stats spring-app

# Voir les informations système
docker system df
docker system info
```

## ⚡ Optimisation

### Multi-stage builds

```dockerfile
# Optimisation avec multi-stage
FROM maven:3.8.6-openjdk-11-slim AS build
# ... étapes de build ...

FROM openjdk:11-jre-slim AS production
# ... configuration de production ...
```

### Cache des couches

```dockerfile
# Optimiser l'ordre des instructions
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package
```

### Images minimales

```dockerfile
# Utiliser des images Alpine
FROM openjdk:11-jre-alpine

# Supprimer les caches
RUN apk add --no-cache curl && \
    rm -rf /var/cache/apk/*
```

### Variables d'environnement

```dockerfile
# Optimiser les variables d'environnement
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC"
ENV SPRING_PROFILES_ACTIVE=production
```

## 🔒 Sécurité

### Utilisateur non-root

```dockerfile
# Créer un utilisateur non-root
RUN groupadd -r spring && useradd -r -g spring spring
USER spring
```

### Scan de sécurité

```bash
# Installer Trivy
curl -sfL https://raw.githubusercontent.com/aquasecurity/trivy/main/contrib/install.sh | sh

# Scanner une image
trivy image spring-boot-demo:latest

# Scanner avec rapport
trivy image --format json --output report.json spring-boot-demo:latest
```

### Bonnes pratiques

1. **Utiliser des images officielles**
2. **Mettre à jour régulièrement les images**
3. **Utiliser des utilisateurs non-root**
4. **Scanner les vulnérabilités**
5. **Limiter les privilèges**

## 🔧 Dépannage

### Problèmes courants

#### 1. Build échoue
```bash
# Vérifier le Dockerfile
docker build --no-cache -t test-image .

# Vérifier les logs
docker build --progress=plain -t test-image .
```

#### 2. Conteneur ne démarre pas
```bash
# Vérifier les logs
docker logs container-name

# Vérifier la configuration
docker inspect container-name

# Tester manuellement
docker run -it spring-boot-demo sh
```

#### 3. Problème de réseau
```bash
# Vérifier les réseaux
docker network ls
docker network inspect app-network

# Tester la connectivité
docker exec container-name ping other-container
```

#### 4. Problème de volumes
```bash
# Vérifier les volumes
docker volume ls
docker volume inspect volume-name

# Vérifier les permissions
docker exec container-name ls -la /app
```

### Commandes de diagnostic

```bash
# Informations système
docker system info
docker system df

# Nettoyage
docker system prune -a
docker volume prune
docker network prune

# Monitoring
docker stats
docker top container-name
```

## 📚 Ressources supplémentaires

- [Documentation Docker](https://docs.docker.com/)
- [Docker Compose](https://docs.docker.com/compose/)
- [Best Practices](https://docs.docker.com/develop/dev-best-practices/)
- [Security](https://docs.docker.com/engine/security/)
- [Multi-stage builds](https://docs.docker.com/develop/dev-best-practices/dockerfile_best-practices/#use-multi-stage-builds)

---

**Votre environnement Docker est configuré et optimisé ! 🐳**
