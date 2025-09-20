# 🧪 Tests Jenkins - Guide de validation

Ce guide explique comment exécuter les tests pour valider votre configuration Jenkins.

## 📋 Tests disponibles

### 1. Test de configuration Java (`test-java-config.groovy`)

**Objectif** : Vérifier que JDK 11 est correctement configuré et fonctionnel.

**Ce qui est testé** :
- ✅ Variables d'environnement (JAVA_HOME, PATH)
- ✅ Installation Java (java -version, javac -version)
- ✅ Compilation d'un programme Java simple
- ✅ Intégration Maven
- ✅ Fonctionnement Docker

**Comment exécuter** :
1. Créez un nouveau job Pipeline dans Jenkins
2. Copiez le contenu de `test-java-config.groovy`
3. Exécutez le job

### 2. Test du pipeline complet (`test-pipeline.groovy`)

**Objectif** : Tester l'ensemble du pipeline CI/CD avec la nouvelle configuration.

**Ce qui est testé** :
- ✅ Checkout Git
- ✅ Build Maven
- ✅ Tests unitaires
- ✅ Package JAR
- ✅ Construction Docker
- ✅ Scan de sécurité (Trivy)

**Comment exécuter** :
1. Créez un nouveau job Pipeline dans Jenkins
2. Copiez le contenu de `test-pipeline.groovy`
3. Exécutez le job

### 3. Test des credentials (`test-credentials.groovy`)

**Objectif** : Vérifier que tous les credentials sont correctement configurés.

**Ce qui est testé** :
- ✅ Credentials Docker Hub (login, push)
- ✅ Credentials GitHub (connexion SSH)
- ✅ Credentials Render (API access)
- ✅ Test d'intégration complète

**Prérequis** :
- Credentials configurés dans Jenkins :
  - `dockerhub-credentials`
  - `github-credentials`
  - `render-api-key`

## 🚀 Exécution des tests

### Étape 1 : Test de base (Java)

```bash
# Créer un job Pipeline
# Nom : "Test-Java-Config"
# Type : Pipeline
# Script : Copier le contenu de test-java-config.groovy
```

### Étape 2 : Test du pipeline

```bash
# Créer un job Pipeline
# Nom : "Test-Pipeline-Complete"
# Type : Pipeline
# Script : Copier le contenu de test-pipeline.groovy
```

### Étape 3 : Test des credentials

```bash
# Créer un job Pipeline
# Nom : "Test-Credentials"
# Type : Pipeline
# Script : Copier le contenu de test-credentials.groovy
```

## 📊 Interprétation des résultats

### ✅ Succès
- Tous les tests passent
- Configuration prête pour la production
- Pipeline fonctionnel

### ❌ Échec
- Vérifiez les logs détaillés
- Corrigez la configuration
- Relancez les tests

## 🔧 Dépannage

### Problème : Java non trouvé
```bash
# Vérifier la configuration JDK dans Jenkins
# Manage Jenkins > Global Tool Configuration > JDK
# Vérifier que "JDK-11" est configuré
```

### Problème : Docker non accessible
```bash
# Vérifier que Docker est installé et démarré
sudo systemctl status docker
sudo usermod -aG docker jenkins
sudo systemctl restart jenkins
```

### Problème : Credentials manquants
```bash
# Vérifier les credentials dans Jenkins
# Manage Jenkins > Manage Credentials
# Ajouter les credentials manquants
```

### Problème : Maven non trouvé
```bash
# Vérifier la configuration Maven dans Jenkins
# Manage Jenkins > Global Tool Configuration > Maven
# Installer Maven si nécessaire
```

## 📝 Logs utiles

### Logs Jenkins
```bash
# Logs principaux
tail -f /var/log/jenkins/jenkins.log

# Logs d'un job spécifique
# Interface Jenkins > Job > Build > Console Output
```

### Logs Docker
```bash
# Logs Docker
sudo journalctl -u docker.service

# Vérifier les images
docker images
```

### Logs Maven
```bash
# Logs Maven (dans le workspace)
tail -f target/maven-build.log
```

## 🎯 Prochaines étapes

Une fois tous les tests passés :

1. **Configurer les credentials** manquants
2. **Lancer le pipeline principal** (`Jenkinsfile`)
3. **Configurer les webhooks** GitHub et Docker Hub
4. **Tester le déploiement** sur Render

## 📚 Ressources

- [Documentation Jenkins](https://www.jenkins.io/doc/)
- [Guide Docker](docs/DOCKER-GUIDE.md)
- [Guide Render](docs/RENDER-GUIDE.md)
- [Configuration CI/CD](docs/CI-CD-SETUP.md)

---

**Bonne chance avec vos tests ! 🚀**
