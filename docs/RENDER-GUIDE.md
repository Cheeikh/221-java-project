# 🌐 Guide Render

Ce guide détaille la configuration et l'utilisation de Render pour le déploiement de l'application Spring Boot.

## 📋 Table des matières

- [Vue d'ensemble](#vue-densemble)
- [Configuration du service](#configuration-du-service)
- [Variables d'environnement](#variables-denvironnement)
- [Déploiement](#déploiement)
- [Monitoring](#monitoring)
- [Sécurité](#sécurité)
- [Optimisation](#optimisation)
- [Dépannage](#dépannage)

## 🎯 Vue d'ensemble

Render est une plateforme cloud qui simplifie le déploiement d'applications. Pour ce projet, nous utilisons :
- **Web Service** pour l'application Spring Boot
- **PostgreSQL** pour la base de données (optionnel)
- **Redis** pour le cache (optionnel)

## ⚙️ Configuration du service

### 1. Création du service

1. **Connectez-vous** à [Render](https://render.com)
2. **Cliquez** sur "New" > "Web Service"
3. **Connectez** votre repository GitHub
4. **Sélectionnez** le repository `221-java-project`

### 2. Configuration de base

```yaml
# render.yaml
services:
  - type: web
    name: spring-boot-demo
    env: docker
    dockerfilePath: ./Dockerfile
    dockerContext: .
    plan: starter
    region: oregon
    branch: main
    buildCommand: echo "Build handled by Docker"
    startCommand: java -jar app.jar
    envVars:
      - key: SPRING_PROFILES_ACTIVE
        value: production
      - key: JAVA_OPTS
        value: -Xmx512m -Xms256m
      - key: PORT
        value: 10000
    healthCheckPath: /actuator/health
    autoDeploy: true
    pullRequestPreviewsEnabled: true
```

### 3. Configuration avancée

```yaml
# Configuration complète
services:
  - type: web
    name: spring-boot-demo
    env: docker
    dockerfilePath: ./Dockerfile
    dockerContext: .
    plan: standard
    region: oregon
    branch: main
    buildCommand: echo "Build handled by Docker"
    startCommand: java -jar app.jar
    envVars:
      - key: SPRING_PROFILES_ACTIVE
        value: production
      - key: JAVA_OPTS
        value: -Xmx1024m -Xms512m
      - key: PORT
        value: 10000
      - key: DATABASE_URL
        fromDatabase:
          name: postgres-db
          property: connectionString
      - key: REDIS_URL
        fromService:
          type: redis
          name: redis-cache
          property: connectionString
    healthCheckPath: /actuator/health
    autoDeploy: true
    pullRequestPreviewsEnabled: true
    disk:
      name: app-data
      mountPath: /app/data
      sizeGB: 1
```

## 🔧 Variables d'environnement

### Variables système

| Variable | Description | Valeur par défaut |
|----------|-------------|-------------------|
| `PORT` | Port de l'application | `10000` |
| `RENDER` | Indique que l'app tourne sur Render | `true` |
| `RENDER_EXTERNAL_URL` | URL externe du service | Auto-générée |

### Variables d'application

| Variable | Description | Valeur recommandée |
|----------|-------------|-------------------|
| `SPRING_PROFILES_ACTIVE` | Profil Spring Boot | `production` |
| `JAVA_OPTS` | Options JVM | `-Xmx512m -Xms256m` |
| `DATABASE_URL` | URL de la base de données | `postgresql://...` |
| `REDIS_URL` | URL Redis | `redis://...` |
| `LOG_LEVEL` | Niveau de log | `INFO` |

### Configuration des variables

1. **Via l'interface Render** :
   - Allez dans votre service
   - **Environment** > **Add Environment Variable**
   - Ajoutez chaque variable

2. **Via render.yaml** :
   ```yaml
   envVars:
     - key: SPRING_PROFILES_ACTIVE
       value: production
     - key: JAVA_OPTS
       value: -Xmx512m -Xms256m
   ```

## 🚀 Déploiement

### Déploiement automatique

Le déploiement se fait automatiquement :
1. **Push** sur la branche `main`
2. **Webhook** Docker Hub déclenche le déploiement
3. **Render** pull la nouvelle image
4. **Déploiement** de l'application

### Déploiement manuel

```bash
# Login Docker Hub
echo $DOCKER_PASSWORD | docker login -u $DOCKER_USERNAME --password-stdin

# Build et push de l'image
docker build -t $DOCKER_USERNAME/spring-boot-demo:latest .
docker push $DOCKER_USERNAME/spring-boot-demo:latest

# Via l'API Render
curl -X POST \
  -H "Authorization: Bearer $RENDER_API_KEY" \
  -H "Content-Type: application/json" \
  -d "{\"image\": \"$DOCKER_USERNAME/spring-boot-demo:latest\"}" \
  https://api.render.com/v1/services/$SERVICE_ID/deploys

# Via l'interface Render
# Allez dans votre service > Deploys > Manual Deploy
```

### Configuration du déploiement automatique

**Note** : Les webhooks Render ne sont pas gratuits. Nous utilisons une approche alternative via Jenkins.

#### Méthode 1 : Déploiement via API Render (Recommandée)

Le déploiement se fait automatiquement via Jenkins qui :
1. Construit l'image Docker
2. La pousse vers Docker Hub
3. Déclenche le déploiement sur Render via l'API

#### Méthode 2 : Déploiement manuel

Utilisez le script `scripts/deploy-to-render.sh` :

```bash
# Configuration des variables d'environnement
export RENDER_SERVICE_ID="your-service-id"
export RENDER_API_KEY="your-api-key"
export DOCKER_USERNAME="Cheeikh"
export DOCKER_PASSWORD="your-docker-password"
export DOCKER_TAG="latest"

# Exécution du déploiement
./scripts/deploy-to-render.sh
```

## 📊 Monitoring

### Logs

1. **Via l'interface Render** :
   - Allez dans votre service
   - **Logs** pour voir les logs en temps réel

2. **Via l'API** :
   ```bash
   curl -H "Authorization: Bearer $RENDER_API_KEY" \
     https://api.render.com/v1/services/$SERVICE_ID/logs
   ```

### Métriques

Render fournit des métriques automatiques :
- **CPU Usage**
- **Memory Usage**
- **Response Time**
- **Request Count**
- **Error Rate**

### Health Checks

```yaml
# Configuration du health check
healthCheckPath: /actuator/health
```

L'application doit exposer un endpoint de santé :
```java
@RestController
public class HealthController {
    
    @GetMapping("/actuator/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("timestamp", Instant.now().toString());
        return ResponseEntity.ok(status);
    }
}
```

## 🔒 Sécurité

### HTTPS

Render fournit automatiquement :
- **Certificat SSL** Let's Encrypt
- **HTTPS** forcé
- **HSTS** headers

### Variables sensibles

```yaml
# Utilisation de secrets
envVars:
  - key: DATABASE_PASSWORD
    fromDatabase:
      name: postgres-db
      property: password
  - key: API_KEY
    fromSecret: api-key-secret
```

### Headers de sécurité

```java
@Configuration
public class SecurityConfig {
    
    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("https://yourdomain.com");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }
}
```

## ⚡ Optimisation

### Performance

1. **JVM Options** :
   ```yaml
   envVars:
     - key: JAVA_OPTS
       value: -Xmx1024m -Xms512m -XX:+UseG1GC
   ```

2. **Caching** :
   ```java
   @Configuration
   @EnableCaching
   public class CacheConfig {
       
       @Bean
       public CacheManager cacheManager() {
           return new ConcurrentMapCacheManager("users", "products");
       }
   }
   ```

3. **Database Connection Pool** :
   ```yaml
   # application.yml
   spring:
     datasource:
       hikari:
         maximum-pool-size: 10
         minimum-idle: 5
         connection-timeout: 20000
   ```

### Coûts

1. **Plan Starter** : Gratuit (limité)
2. **Plan Standard** : $7/mois
3. **Plan Pro** : $25/mois

### Optimisation des coûts

```yaml
# Configuration optimisée
services:
  - type: web
    name: spring-boot-demo
    plan: starter  # Commencer avec le plan gratuit
    envVars:
      - key: JAVA_OPTS
        value: -Xmx256m -Xms128m  # Limiter la mémoire
```

## 🔧 Dépannage

### Problèmes courants

#### 1. Application ne démarre pas
```bash
# Vérifier les logs
# Interface Render > Service > Logs

# Vérifier les variables d'environnement
# Interface Render > Service > Environment

# Vérifier la configuration Docker
docker run -it $DOCKER_USERNAME/spring-boot-demo:latest sh
```

#### 2. Problème de base de données
```bash
# Vérifier la connexion
# Interface Render > Database > Connect

# Vérifier les variables d'environnement
echo $DATABASE_URL
```

#### 3. Problème de mémoire
```yaml
# Augmenter la mémoire
envVars:
  - key: JAVA_OPTS
    value: -Xmx1024m -Xms512m

# Ou passer au plan supérieur
plan: standard
```

#### 4. Problème de réseau
```bash
# Vérifier la connectivité
curl -I https://your-app.onrender.com

# Vérifier les DNS
nslookup your-app.onrender.com

# Vérifier l'image Docker
docker pull $DOCKER_USERNAME/spring-boot-demo:latest
```

### Commandes de diagnostic

```bash
# Vérifier le statut du service
curl -H "Authorization: Bearer $RENDER_API_KEY" \
  https://api.render.com/v1/services/$SERVICE_ID

# Vérifier les déploiements
curl -H "Authorization: Bearer $RENDER_API_KEY" \
  https://api.render.com/v1/services/$SERVICE_ID/deploys

# Vérifier les logs
curl -H "Authorization: Bearer $RENDER_API_KEY" \
  https://api.render.com/v1/services/$SERVICE_ID/logs
```

### Rollback

```bash
# Rollback vers une version précédente
curl -X POST \
  -H "Authorization: Bearer $RENDER_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{"deployId": "previous-deploy-id"}' \
  https://api.render.com/v1/services/$SERVICE_ID/deploys
```

## 📚 Ressources supplémentaires

- [Documentation Render](https://render.com/docs)
- [API Reference](https://render.com/docs/api)
- [Docker Guide](https://render.com/docs/docker)
- [Environment Variables](https://render.com/docs/environment-variables)
- [Monitoring](https://render.com/docs/monitoring)

---

**Votre application est déployée sur Render ! 🌐**
