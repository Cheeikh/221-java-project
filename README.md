# 🚀 Spring Boot Demo - Pipeline CI/CD

Ce projet implémente une application Spring Boot avec un pipeline CI/CD complet utilisant Jenkins, Docker Hub et Render.

## 📋 Table des matières

- [Vue d'ensemble](#vue-densemble)
- [Architecture](#architecture)
- [Prérequis](#prérequis)
- [Installation](#installation)
- [Configuration](#configuration)
- [Utilisation](#utilisation)
- [Pipeline CI/CD](#pipeline-cicd)
- [Déploiement](#déploiement)
- [Monitoring](#monitoring)
- [Dépannage](#dépannage)
- [Contributions](#contributions)

## 🎯 Vue d'ensemble

Cette application Spring Boot démontre l'implémentation d'un pipeline CI/CD moderne avec :
- **Jenkins** pour l'intégration continue
- **Docker Hub** pour le stockage des images
- **Render** pour le déploiement en production
- **Docker** pour la containerisation
- **Maven** pour la gestion des dépendances

## 🏗️ Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   GitHub        │───▶│   Jenkins       │───▶│   Docker Hub    │
│   (Code)        │    │   (CI/CD)       │    │   (Images)      │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │                        │
                                ▼                        ▼
                       ┌─────────────────┐    ┌─────────────────┐
                       │   Tests         │    │   Render        │
                       │   (Unit/Int)    │    │   (Production)  │
                       └─────────────────┘    └─────────────────┘
```

## 📦 Prérequis

- Java 11+
- Maven 3.6+
- Docker 20.10+
- Docker Compose 2.0+
- Jenkins 2.400+
- Compte Docker Hub
- Compte Render

## 🛠️ Installation

### 1. Cloner le repository

```bash
git clone https://github.com/Cheeikh/221-java-project.git
cd 221-java-project
```

### 2. Configuration locale

```bash
# Construire l'application
mvn clean package

# Construire l'image Docker
docker build -t spring-boot-demo .

# Démarrer avec Docker Compose
docker-compose up -d
```

### 3. Vérification

L'application sera disponible sur `http://localhost:8080`

## ⚙️ Configuration

### Variables d'environnement

| Variable | Description | Valeur par défaut |
|----------|-------------|-------------------|
| `SPRING_PROFILES_ACTIVE` | Profil Spring Boot | `development` |
| `JAVA_OPTS` | Options JVM | `-Xmx512m -Xms256m` |
| `PORT` | Port de l'application | `8080` |

### Configuration Jenkins

1. Installez les plugins requis (voir [jenkins/credentials-setup.md](jenkins/credentials-setup.md))
2. Configurez les credentials
3. Créez le job pipeline

### Configuration Render

1. Créez un nouveau service web
2. Configurez les variables d'environnement
3. Activez les webhooks

## 🚀 Utilisation

### Développement local

```bash
# Démarrer l'application
mvn spring-boot:run

# Exécuter les tests
mvn test

# Construire l'image Docker
docker build -t spring-boot-demo .
```

### Docker Compose

```bash
# Démarrer tous les services
docker-compose up -d

# Voir les logs
docker-compose logs -f app

# Arrêter les services
docker-compose down
```

## 🔄 Pipeline CI/CD

### Étapes du pipeline

1. **Checkout** - Récupération du code source
2. **Build** - Compilation de l'application
3. **Test** - Exécution des tests unitaires et d'intégration
4. **Package** - Création du JAR
5. **Docker Build** - Construction de l'image Docker
6. **Docker Push** - Push vers Docker Hub
7. **Security Scan** - Scan de sécurité avec Trivy
8. **Deploy** - Déploiement sur Render

### Déclencheurs

- Push sur la branche `main` ou `develop`
- Webhook GitHub
- Planification (toutes les 5 minutes)

### Branches supportées

- `main` - Production
- `develop` - Développement

## 🌐 Déploiement

### Déploiement automatique

Le déploiement se fait automatiquement via Jenkins :
1. Push du code sur GitHub
2. Déclenchement du pipeline Jenkins
3. Construction et push de l'image Docker
4. Déploiement sur Render

### Déploiement manuel

```bash
# Login Docker Hub
echo $DOCKER_PASSWORD | docker login -u $DOCKER_USERNAME --password-stdin

# Construire et pousser l'image
docker build -t $DOCKER_USERNAME/spring-boot-demo:latest .
docker push $DOCKER_USERNAME/spring-boot-demo:latest

# Déployer sur Render
curl -X POST \
  -H "Authorization: Bearer $RENDER_API_KEY" \
  -H "Content-Type: application/json" \
  -d "{\"image\": \"$DOCKER_USERNAME/spring-boot-demo:latest\"}" \
  https://api.render.com/v1/services/$SERVICE_ID/deploys
```

## 📊 Monitoring

### Health Checks

- **Endpoint**: `/actuator/health`
- **Fréquence**: Toutes les 30 secondes
- **Timeout**: 10 secondes

### Logs

- **Jenkins**: Interface web Jenkins
- **Docker**: `docker logs <container_id>`
- **Render**: Dashboard Render

### Métriques

- **Prometheus**: `http://localhost:9090`
- **Grafana**: `http://localhost:3000` (admin/admin123)

## 🔧 Dépannage

### Problèmes courants

#### 1. Échec de build Maven
```bash
# Nettoyer le cache Maven
mvn clean

# Vérifier les dépendances
mvn dependency:tree
```

#### 2. Problème Docker
```bash
# Vérifier les images
docker images

# Nettoyer les images inutilisées
docker system prune -a
```

#### 3. Problème Jenkins
- Vérifier les logs dans `/var/log/jenkins/`
- Redémarrer Jenkins : `sudo systemctl restart jenkins`

#### 4. Problème Render
- Vérifier les logs dans le dashboard Render
- Vérifier les variables d'environnement

### Commandes utiles

```bash
# Vérifier le statut des conteneurs
docker-compose ps

# Redémarrer un service
docker-compose restart app

# Voir les logs en temps réel
docker-compose logs -f

# Exécuter une commande dans le conteneur
docker-compose exec app bash
```

## 🤝 Contributions

1. Fork le projet
2. Créez une branche feature (`git checkout -b feature/AmazingFeature`)
3. Committez vos changements (`git commit -m 'Add some AmazingFeature'`)
4. Push vers la branche (`git push origin feature/AmazingFeature`)
5. Ouvrez une Pull Request

## 📄 Licence

Ce projet est sous licence MIT. Voir le fichier [LICENSE](LICENSE) pour plus de détails.

## 📞 Support

Pour toute question ou problème :
- Créez une issue sur GitHub
- Contactez l'équipe de développement

---

**Développé avec ❤️ par l'équipe de développement**
