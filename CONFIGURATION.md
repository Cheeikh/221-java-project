# ⚙️ Guide de Configuration - Pipeline CI/CD

Ce guide unifié contient toute la configuration nécessaire pour le pipeline CI/CD avec Jenkins, Docker Hub et Render.

## 📋 Table des matières

- [Vue d'ensemble](#vue-densemble)
- [Configuration Jenkins](#configuration-jenkins)
- [Configuration Docker Hub](#configuration-docker-hub)
- [Configuration Render](#configuration-render)
- [Tests et validation](#tests-et-validation)
- [Déploiement](#déploiement)
- [Dépannage](#dépannage)

## 🎯 Vue d'ensemble

### Architecture du pipeline
```
GitHub Push → Jenkins → Docker Build → Docker Hub → Render API → Deploy
```

### Services utilisés
- **Jenkins** : Intégration continue et déploiement
- **Docker Hub** : Stockage des images Docker
- **Render** : Hébergement de l'application (sans webhooks)

## 🔧 Configuration Jenkins

### 1. Installation et configuration de base

#### Prérequis système
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-11-jdk wget curl git

# CentOS/RHEL
sudo yum install java-11-openjdk wget curl git
```

#### Installation Jenkins
```bash
# Ajouter la clé GPG
wget -q -O - https://pkg.jenkins.io/debian/jenkins.io.key | sudo apt-key add -

# Ajouter le repository
echo deb https://pkg.jenkins.io/debian binary/ | sudo tee /etc/apt/sources.list.d/jenkins.list

# Installer Jenkins
sudo apt update
sudo apt install jenkins

# Démarrer Jenkins
sudo systemctl start jenkins
sudo systemctl enable jenkins
```

### 2. Configuration JDK 11

1. **Allez dans** : Manage Jenkins > Global Tool Configuration
2. **Configurez JDK** :
   - **Nom** : `JDK-11`
   - **Install automatically** : ✅ (coché)
   - **Installer** : "Extract *.zip/*.tar.gz"
   - **Download URL** : `https://download.java.net/java/GA/jdk11/9/GPL/openjdk-11.0.2_linux-x64_bin.tar.gz`
   - **Subdirectory** : `jdk-11.0.2`

### 3. Configuration des credentials

#### Docker Hub Credentials
- **ID** : `docker-hub-credentials`
- **Type** : Username with password
- **Username** : `Cheeikh`
- **Password** : Votre token Docker Hub

#### GitHub Credentials
- **ID** : `github-credentials`
- **Type** : SSH Username with private key
- **Username** : `git`
- **Private Key** : Votre clé privée SSH

#### Render API Key
- **ID** : `render-api-key`
- **Type** : Secret text
- **Secret** : Votre clé API Render

### 4. Configuration du job pipeline

1. **Créez** un nouveau job Pipeline
2. **Nom** : `spring-boot-demo-pipeline`
3. **Configuration** :
   - **Pipeline script from SCM**
   - **SCM** : Git
   - **Repository URL** : `https://github.com/Cheeikh/221-java-project.git`
   - **Credentials** : `github-credentials`
   - **Script Path** : `Jenkinsfile`

### 5. Plugins requis

| Plugin | Version | Description |
|--------|---------|-------------|
| Pipeline | 2.45+ | Support des pipelines |
| Docker Pipeline | 1.28+ | Intégration Docker |
| Git | 4.8.3+ | Support Git |
| GitHub | 1.34.0+ | Intégration GitHub |
| Credentials Binding | 2.6+ | Gestion des credentials |
| AnsiColor | 1.0.0+ | Couleurs dans les logs |
| Timestamper | 1.18+ | Horodatage des logs |
| Build Timeout | 1.24+ | Timeout des builds |

## 🐳 Configuration Docker Hub

### 1. Création du repository

1. **Connectez-vous** à [Docker Hub](https://hub.docker.com)
2. **Cliquez** sur "Create Repository"
3. **Configuration** :
   - **Nom** : `spring-boot-demo`
   - **Visibilité** : Public ou Private
   - **Description** : Spring Boot Demo Application

### 2. Configuration des tags

Les images sont automatiquement taguées :
- `latest` : Dernière version stable
- `BUILD_NUMBER` : Numéro de build
- `GIT_COMMIT_SHORT` : Hash court du commit

## 🌐 Configuration Render

### 1. Création du service

1. **Connectez-vous** à [Render](https://render.com)
2. **Cliquez** sur "New" > "Web Service"
3. **Configuration** :
   - **Name** : `spring-boot-demo`
   - **Environment** : Docker
   - **Dockerfile Path** : `./Dockerfile`
   - **Docker Context** : `.`
   - **Plan** : Starter (gratuit)
   - **Region** : Oregon
   - **Branch** : main

### 2. Variables d'environnement

Configurez ces variables dans Render :

| Variable | Valeur | Description |
|----------|--------|-------------|
| `SPRING_PROFILES_ACTIVE` | `production` | Profil Spring Boot |
| `JAVA_OPTS` | `-Xmx512m -Xms256m` | Options JVM |
| `PORT` | `10000` | Port de l'application |
| `RENDER` | `true` | Indique que l'app tourne sur Render |

### 3. Configuration du déploiement

**Note** : Les webhooks Render ne sont pas gratuits. Le déploiement se fait via l'API Render depuis Jenkins.

Le pipeline Jenkins :
1. Construit l'image Docker
2. La pousse vers Docker Hub
3. Déclenche le déploiement sur Render via l'API

## 🧪 Tests et validation

### 1. Test de configuration Java

Créez un job Pipeline avec ce script :

```groovy
pipeline {
    agent any
    tools { jdk 'JDK-11' }
    environment {
        JAVA_HOME = tool('JDK-11')
        PATH = "${JAVA_HOME}/bin:${env.PATH}"
    }
    stages {
        stage('Test Java') {
            steps {
                sh '''
                    echo "JAVA_HOME: $JAVA_HOME"
                    java -version
                    javac -version
                '''
            }
        }
    }
}
```

### 2. Test du pipeline complet

Utilisez le contenu de `jenkins/test-pipeline.groovy` pour tester l'ensemble du pipeline.

### 3. Test des credentials

Utilisez le contenu de `jenkins/test-credentials.groovy` pour tester les credentials.

## 🚀 Déploiement

### 1. Déploiement automatique

Le déploiement se fait automatiquement via Jenkins :
1. Push du code sur GitHub
2. Déclenchement du pipeline Jenkins
3. Construction et push de l'image Docker
4. Déploiement sur Render via l'API

### 2. Déploiement manuel

```bash
# Configuration des variables
export RENDER_SERVICE_ID="srv-d378mo9r0fns739b1rd0"
export RENDER_API_KEY="your-api-key"
export DOCKER_USERNAME="Cheeikh"
export DOCKER_PASSWORD="your-docker-password"

# Déploiement
./scripts/deploy-to-render.sh
```

### 3. Vérification du déploiement

```bash
# Vérifier le statut du service
curl -H "Authorization: Bearer $RENDER_API_KEY" \
  https://api.render.com/v1/services/$RENDER_SERVICE_ID

# Vérifier l'application
curl https://your-app.onrender.com/actuator/health
```

## 🔧 Dépannage

### Problèmes Jenkins

#### Java non trouvé
```bash
# Vérifier la configuration JDK
# Manage Jenkins > Global Tool Configuration > JDK
```

#### Docker non accessible
```bash
# Vérifier Docker
sudo systemctl status docker
sudo usermod -aG docker jenkins
sudo systemctl restart jenkins
```

#### Credentials manquants
```bash
# Vérifier les credentials
# Manage Jenkins > Manage Credentials
```

### Problèmes Docker

#### Build échoue
```bash
# Vérifier le Dockerfile
docker build --no-cache -t test-image .

# Vérifier les logs
docker logs <container_id>
```

#### Push échoue
```bash
# Vérifier l'authentification
docker login

# Vérifier les permissions
docker push Cheeikh/spring-boot-demo:latest
```

### Problèmes Render

#### Déploiement échoue
```bash
# Vérifier les logs Render
# Dashboard Render > Service > Logs

# Vérifier l'API
curl -H "Authorization: Bearer $RENDER_API_KEY" \
  https://api.render.com/v1/services/$RENDER_SERVICE_ID
```

#### Application ne démarre pas
```bash
# Vérifier les variables d'environnement
# Dashboard Render > Service > Environment

# Vérifier les logs
# Dashboard Render > Service > Logs
```

### Commandes de diagnostic

```bash
# Vérifier Jenkins
sudo systemctl status jenkins
tail -f /var/log/jenkins/jenkins.log

# Vérifier Docker
docker --version
docker info

# Vérifier Java
java -version
javac -version

# Vérifier Maven
mvn -version
```

## 📊 Monitoring

### Logs

- **Jenkins** : Interface web Jenkins > Job > Build > Console Output
- **Docker** : `docker logs <container_id>`
- **Render** : Dashboard Render > Service > Logs

### Métriques

- **Render** : Dashboard Render > Service > Metrics
- **Jenkins** : Dashboard Jenkins > Job > Build History

### Health Checks

- **Endpoint** : `/actuator/health`
- **Fréquence** : Toutes les 30 secondes
- **Timeout** : 10 secondes

## 🎯 Prochaines étapes

1. **Configurez** Jenkins avec JDK 11
2. **Ajoutez** les credentials requis
3. **Créez** le service Render
4. **Testez** avec les scripts fournis
5. **Lancez** le pipeline complet

## 📚 Ressources

- [Documentation Jenkins](https://www.jenkins.io/doc/)
- [Documentation Docker](https://docs.docker.com/)
- [Documentation Render](https://render.com/docs)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)

---

**Configuration unifiée - Tout en un ! 🚀**
