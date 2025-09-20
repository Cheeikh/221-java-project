# Configuration des Webhooks Render

## Webhook Docker Hub vers Render

### 1. Configuration du Webhook dans Docker Hub

1. Connectez-vous à votre compte Docker Hub
2. Allez dans votre repository `Cheeikh/spring-boot-demo`
3. Cliquez sur **Webhooks** dans le menu
4. Cliquez sur **Create Webhook**
5. Configurez le webhook :
   - **Webhook Name**: `Render Deploy`
   - **Webhook URL**: `https://api.render.com/v1/services/YOUR_SERVICE_ID/deploys`
   - **Content Type**: `application/json`
   - **Secret**: Votre clé API Render
   - **Events**: Sélectionnez `Push` et `Tag`

### 2. Configuration du Webhook dans Render

1. Allez dans votre dashboard Render
2. Sélectionnez votre service
3. Allez dans **Settings** > **Webhooks**
4. Ajoutez un nouveau webhook :
   - **Name**: `Docker Hub Integration`
   - **URL**: `https://api.render.com/v1/services/YOUR_SERVICE_ID/deploys`
   - **Events**: `push`, `tag`

### 3. Script de déploiement automatique

Créez un script `deploy.sh` pour automatiser le déploiement :

```bash
#!/bin/bash

# Configuration
RENDER_SERVICE_ID="your-service-id"
RENDER_API_KEY="your-api-key"
DOCKER_IMAGE="Cheeikh/spring-boot-demo"
DOCKER_TAG="latest"

# Fonction pour déployer sur Render
deploy_to_render() {
    echo "🚀 Déploiement de l'image ${DOCKER_IMAGE}:${DOCKER_TAG} sur Render..."
    
    curl -X POST \
        -H "Authorization: Bearer ${RENDER_API_KEY}" \
        -H "Content-Type: application/json" \
        -d "{\"image\": \"${DOCKER_IMAGE}:${DOCKER_TAG}\"}" \
        https://api.render.com/v1/services/${RENDER_SERVICE_ID}/deploys
    
    if [ $? -eq 0 ]; then
        echo "✅ Déploiement réussi!"
    else
        echo "❌ Échec du déploiement!"
        exit 1
    fi
}

# Exécution du déploiement
deploy_to_render
```

### 4. Variables d'environnement Render

Configurez les variables d'environnement suivantes dans Render :

| Variable | Valeur | Description |
|----------|--------|-------------|
| `SPRING_PROFILES_ACTIVE` | `production` | Profil Spring Boot |
| `JAVA_OPTS` | `-Xmx512m -Xms256m` | Options JVM |
| `PORT` | `10000` | Port de l'application |
| `DATABASE_URL` | `postgresql://...` | URL de la base de données |
| `REDIS_URL` | `redis://...` | URL Redis |

### 5. Monitoring et Logs

#### Logs de déploiement
- Accédez aux logs via le dashboard Render
- Utilisez l'API Render pour récupérer les logs :
```bash
curl -H "Authorization: Bearer ${RENDER_API_KEY}" \
  https://api.render.com/v1/services/${RENDER_SERVICE_ID}/logs
```

#### Health Checks
- L'application expose un endpoint `/actuator/health`
- Render vérifie automatiquement la santé de l'application
- Configuration dans `render.yaml` :
```yaml
healthCheckPath: /actuator/health
```

### 6. Rollback automatique

En cas d'échec du déploiement, Render peut automatiquement revenir à la version précédente :

```yaml
# Dans render.yaml
autoDeploy: true
rollbackPolicy:
  enabled: true
  maxRetries: 3
  retryDelay: 300s
```

### 7. Notifications

Configurez les notifications pour être informé des déploiements :

1. Allez dans **Settings** > **Notifications**
2. Ajoutez votre email ou webhook Slack
3. Sélectionnez les événements à notifier :
   - Déploiement réussi
   - Déploiement échoué
   - Rollback
