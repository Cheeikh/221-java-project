# 🔧 Guide Jenkins

Ce guide détaille la configuration et l'utilisation de Jenkins pour le pipeline CI/CD.

## 📋 Table des matières

- [Installation](#installation)
- [Configuration](#configuration)
- [Pipeline](#pipeline)
- [Plugins](#plugins)
- [Credentials](#credentials)
- [Jobs](#jobs)
- [Monitoring](#monitoring)
- [Sécurité](#sécurité)
- [Dépannage](#dépannage)

## 🚀 Installation

### Prérequis système

```bash
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-11-jdk wget curl git

# CentOS/RHEL
sudo yum install java-11-openjdk wget curl git

# Vérifier Java
java -version
```

### Installation Jenkins

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

# Vérifier le statut
sudo systemctl status jenkins
```

### Configuration initiale

1. **Accéder à Jenkins**
   - URL : `http://localhost:8080`
   - Récupérer le mot de passe initial :
   ```bash
   sudo cat /var/lib/jenkins/secrets/initialAdminPassword
   ```

2. **Installation des plugins**
   - Sélectionner "Install suggested plugins"
   - Attendre la fin de l'installation

3. **Création de l'utilisateur admin**
   - Nom d'utilisateur : `admin`
   - Mot de passe : `admin123` (à changer)
   - Email : `admin@yourcompany.com`

## ⚙️ Configuration

### Configuration système

1. **Allez dans** : Manage Jenkins > Configure System
2. **Configurez** :
   - **Jenkins URL** : `http://your-server:8080`
   - **System Message** : Message d'accueil
   - **# of executors** : `2` (selon votre serveur)

### Configuration globale des outils

1. **Allez dans** : Manage Jenkins > Global Tool Configuration
2. **Configurez** :
   - **JDK** : Java 11
   - **Maven** : Maven 3.8.6
   - **Git** : Git (système)

### Configuration des plugins

#### Pipeline
- **Nom** : Pipeline
- **Version** : 2.45+
- **Description** : Support des pipelines

#### Docker Pipeline
- **Nom** : Docker Pipeline
- **Version** : 1.28+
- **Description** : Intégration Docker

#### Git
- **Nom** : Git
- **Version** : 4.8.3+
- **Description** : Support Git

#### GitHub
- **Nom** : GitHub
- **Version** : 1.34.0+
- **Description** : Intégration GitHub

## 🔄 Pipeline

### Structure du pipeline

```groovy
pipeline {
    agent any
    
    environment {
        RENDER_SERVICE_ID = 'your-render-service-id'
        RENDER_API_KEY = credentials('render-api-key')
        MAVEN_OPTS = '-Xmx1024m'
        PATH = "/usr/local/bin:${env.PATH}"
        DOCKER_BUILDKIT = "1"
    }
    
    options {
        timeout(time: 30, unit: 'MINUTES')
        retry(2)
        timestamps()
        ansiColor('xterm')
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Build') {
            steps {
                sh 'mvn clean compile'
            }
        }
        
        stage('Test') {
            steps {
                sh 'mvn test'
            }
        }
        
        stage('Docker Build') {
            steps {
                script {
                    // Vérification Docker
                    sh '''
                        if ! command -v docker >/dev/null 2>&1; then
                            echo "❌ Docker non trouvé"
                            exit 1
                        fi
                        docker --version
                    '''
                    
                    // Login et build avec credentials
                    withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials', usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWORD')]) {
                        sh 'echo $DOCKER_PASSWORD | docker login -u $DOCKER_USERNAME --password-stdin'
                        sh "docker build -t ${env.DOCKER_USERNAME}/${env.JOB_NAME}:${env.BUILD_NUMBER} ."
                        sh "docker tag ${env.DOCKER_USERNAME}/${env.JOB_NAME}:${env.BUILD_NUMBER} ${env.DOCKER_USERNAME}/${env.JOB_NAME}:latest"
                        sh "docker push ${env.DOCKER_USERNAME}/${env.JOB_NAME}:${env.BUILD_NUMBER}"
                        sh "docker push ${env.DOCKER_USERNAME}/${env.JOB_NAME}:latest"
                    }
                }
            }
        }
    }
    
    post {
        always {
            echo 'Pipeline terminé'
        }
        success {
            echo 'Pipeline réussi'
        }
        failure {
            echo 'Pipeline échoué'
        }
    }
}
```

### Étapes du pipeline

1. **Checkout** - Récupération du code source
2. **Build** - Compilation de l'application
3. **Test** - Exécution des tests (unitaires et qualité)
4. **Package** - Création du JAR
5. **Docker Build** - Construction, tag et push de l'image Docker
6. **Security Scan** - Scan de sécurité avec Trivy
7. **Deploy** - Déploiement sur Render

### Déclencheurs

```groovy
triggers {
    // Polling SCM
    pollSCM('H/5 * * * *')
    
    // Webhook GitHub
    githubPush()
    
    // Planification
    cron('H 2 * * *')
}
```

## 🔌 Plugins

### Plugins essentiels

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
| Checkstyle | 4.0.0+ | Analyse de code |
| SpotBugs | 4.0.0+ | Détection de bugs |
| HTML Publisher | 1.30+ | Publication HTML |
| Test Results Analyzer | 1.0.0+ | Analyse des tests |

### Installation des plugins

1. **Via l'interface** :
   - Manage Jenkins > Manage Plugins
   - Available > Rechercher le plugin
   - Installer et redémarrer

2. **Via la ligne de commande** :
   ```bash
   # Installer un plugin
   jenkins-plugin-cli --plugins pipeline docker-workflow git github
   
   # Redémarrer Jenkins
   sudo systemctl restart jenkins
   ```

## 🔐 Credentials

### Types de credentials

1. **Username with password**
   - Docker Hub
   - Base de données

2. **SSH Username with private key**
   - GitHub
   - Serveurs distants

3. **Secret text**
   - API Keys
   - Tokens

### Configuration des credentials

1. **Allez dans** : Manage Jenkins > Manage Credentials
2. **Sélectionnez** : Global > Add Credentials
3. **Configurez** selon le type

### Credentials pour ce projet

#### Docker Hub
- **ID** : `dockerhub-credentials`
- **Type** : Username with password
- **Username** : Votre nom d'utilisateur Docker Hub (ex: Cheeikh)
- **Password** : Votre token Docker Hub

#### GitHub
- **ID** : `github-credentials`
- **Type** : SSH Username with private key
- **Username** : `git`
- **Private Key** : Votre clé privée SSH

#### Render API
- **ID** : `render-api-key`
- **Type** : Secret text
- **Secret** : Votre clé API Render

## 📋 Jobs

### Création d'un job pipeline

1. **Nouveau Item** > **Pipeline**
2. **Nom** : `spring-boot-demo-pipeline`
3. **Configuration** :
   - **Pipeline script from SCM**
   - **SCM** : Git
   - **Repository URL** : `https://github.com/cheikhmbacke/221-java-project.git`
   - **Credentials** : `github-credentials`
   - **Script Path** : `Jenkinsfile`

### Configuration du job

```xml
<?xml version='1.1' encoding='UTF-8'?>
<flow-definition plugin="workflow-job@2.45">
  <actions>
    <org.jenkinsci.plugins.pipeline.modeldefinition.actions.DeclarativeJobAction plugin="pipeline-model-definition@1.9.3"/>
  </actions>
  <description>Pipeline CI/CD pour Spring Boot Demo</description>
  <keepDependencies>false</keepDependencies>
  <properties>
    <jenkins.model.BuildDiscarderProperty>
      <strategy class="hudson.tasks.LogRotator">
        <daysToKeep>30</daysToKeep>
        <numToKeep>50</numToKeep>
        <artifactDaysToKeep>7</artifactDaysToKeep>
        <artifactNumToKeep>10</artifactNumToKeep>
      </strategy>
    </jenkins.model.BuildDiscarderProperty>
  </properties>
  <definition class="org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition" plugin="workflow-cps@2.92">
    <scm class="hudson.plugins.git.GitSCM" plugin="git@4.8.3">
      <configVersion>2</configVersion>
      <userRemoteConfigs>
        <hudson.plugins.git.UserRemoteConfig>
          <url>https://github.com/cheikhmbacke/221-java-project.git</url>
          <credentialsId>github-credentials</credentialsId>
        </hudson.plugins.git.UserRemoteConfig>
      </userRemoteConfigs>
      <branches>
        <hudson.plugins.git.BranchSpec>
          <name>*/main</name>
        </hudson.plugins.git.BranchSpec>
      </branches>
      <doGenerateSubmoduleConfigurations>false</doGenerateSubmoduleConfigurations>
      <submoduleCfg class="list"/>
    </scm>
    <scriptPath>Jenkinsfile</scriptPath>
    <lightweight>false</lightweight>
  </definition>
  <triggers/>
  <disabled>false</disabled>
</flow-definition>
```

## 📊 Monitoring

### Logs

```bash
# Logs Jenkins
sudo tail -f /var/log/jenkins/jenkins.log

# Logs d'un build spécifique
# Interface web > Job > Build > Console Output
```

### Métriques

1. **Dashboard Jenkins**
   - Vue d'ensemble des jobs
   - Statut des builds
   - Tendances

2. **Blue Ocean**
   - Interface moderne
   - Visualisation des pipelines
   - Intégration GitHub

### Alertes

```groovy
post {
    failure {
        // Envoyer une notification
        emailext (
            subject: "Build Failed: ${env.JOB_NAME} - ${env.BUILD_NUMBER}",
            body: "Build failed. Check console output.",
            to: "admin@yourcompany.com"
        )
    }
}
```

## 🔒 Sécurité

### Configuration de sécurité

1. **Allez dans** : Manage Jenkins > Configure Global Security
2. **Configurez** :
   - **Security Realm** : Jenkins' own user database
   - **Authorization** : Matrix-based security
   - **CSRF Protection** : Activé

### Utilisateurs et rôles

```groovy
// Configuration des rôles
role('developer') {
    permissions(
        'hudson.model.Item.Build',
        'hudson.model.Item.Read',
        'hudson.model.Item.Workspace'
    )
}

role('admin') {
    permissions(
        'hudson.model.Item.Build',
        'hudson.model.Item.Configure',
        'hudson.model.Item.Delete',
        'hudson.model.Item.Read',
        'hudson.model.Item.Workspace'
    )
}
```

### Bonnes pratiques

1. **Utiliser HTTPS**
2. **Mettre à jour régulièrement**
3. **Configurer les credentials**
4. **Limiter les accès**
5. **Scanner les vulnérabilités**

## 🔧 Dépannage

### Problèmes courants

#### 1. Jenkins ne démarre pas
```bash
# Vérifier les logs
sudo journalctl -u jenkins

# Vérifier les permissions
sudo chown -R jenkins:jenkins /var/lib/jenkins

# Redémarrer
sudo systemctl restart jenkins
```

#### 2. Build échoue
```bash
# Vérifier les logs du build
# Interface web > Job > Build > Console Output

# Vérifier les credentials
# Manage Jenkins > Manage Credentials

# Vérifier les plugins
# Manage Jenkins > Manage Plugins
```

#### 3. Problème de mémoire
```bash
# Modifier la configuration JVM
sudo nano /etc/default/jenkins

# Ajouter
JAVA_ARGS="-Xmx2048m -Xms1024m"

# Redémarrer
sudo systemctl restart jenkins
```

#### 4. Problème de permissions
```bash
# Corriger les permissions
sudo chown -R jenkins:jenkins /var/lib/jenkins
sudo chmod -R 755 /var/lib/jenkins

# Vérifier les permissions Docker
sudo usermod -aG docker jenkins
```

### Commandes de diagnostic

```bash
# Statut Jenkins
sudo systemctl status jenkins

# Logs en temps réel
sudo tail -f /var/log/jenkins/jenkins.log

# Vérifier les processus
ps aux | grep jenkins

# Vérifier les ports
netstat -tlnp | grep 8080

# Vérifier l'espace disque
df -h
```

### Sauvegarde et restauration

```bash
# Sauvegarde
sudo tar -czf jenkins-backup.tar.gz /var/lib/jenkins

# Restauration
sudo systemctl stop jenkins
sudo rm -rf /var/lib/jenkins
sudo tar -xzf jenkins-backup.tar.gz -C /
sudo chown -R jenkins:jenkins /var/lib/jenkins
sudo systemctl start jenkins
```

## 📚 Ressources supplémentaires

- [Documentation Jenkins](https://www.jenkins.io/doc/)
- [Pipeline Syntax](https://www.jenkins.io/doc/book/pipeline/syntax/)
- [Plugin Development](https://www.jenkins.io/doc/developer/)
- [Best Practices](https://www.jenkins.io/doc/book/pipeline/pipeline_best_practices/)
- [Security](https://www.jenkins.io/doc/book/security/)

---

**Votre environnement Jenkins est configuré et prêt ! 🔧**
