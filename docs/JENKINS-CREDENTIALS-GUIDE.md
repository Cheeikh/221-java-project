# Guide de Configuration des Credentials Jenkins

## Problèmes identifiés

D'après les logs du pipeline, voici les problèmes de configuration des credentials :

### 1. ❌ Credentials GitHub - Type incorrect
**Erreur :** `Credentials 'github-credentials' is of type 'Nom d'utilisateur et mot de passe' where 'com.cloudbees.jenkins.plugins.sshcredentials.SSHUserPrivateKey' was expected`

**Solution :** Configurer des credentials SSH pour GitHub

### 2. ❌ Docker non installé
**Erreur :** `docker: command not found`

**Solution :** Installer Docker sur le nœud Jenkins

### 3. ❌ JDK-11 non configuré
**Erreur :** `Installer "Run Shell Command" cannot be used to install "JDK-11"`

**Solution :** Configurer JDK-11 dans Jenkins

### 4. ✅ Credentials Render - Fonctionnent
**Status :** L'API Render est accessible et fonctionne correctement

## Configuration des Credentials

### 1. Credentials GitHub (SSH)

1. **Générer une clé SSH pour GitHub :**
   ```bash
   # Générer une nouvelle clé SSH
   ssh-keygen -t ed25519 -C "your-email@example.com" -f ~/.ssh/github_jenkins
   
   # Ajouter la clé publique à GitHub
   cat ~/.ssh/github_jenkins.pub
   ```

2. **Ajouter la clé publique à GitHub :**
   - Allez dans GitHub > Settings > SSH and GPG keys
   - Cliquez sur "New SSH key"
   - Collez le contenu de `~/.ssh/github_jenkins.pub`

3. **Configurer dans Jenkins :**
   - Allez dans Jenkins > Manage Jenkins > Manage Credentials
   - Sélectionnez le domaine global
   - Cliquez sur "Add Credentials"
   - Type : "SSH Username with private key"
   - ID : `github-credentials`
   - Username : `git` (ou votre nom d'utilisateur GitHub)
   - Private Key : Sélectionnez "Enter directly" et collez le contenu de `~/.ssh/github_jenkins`

### 2. Credentials Docker Hub

1. **Configurer dans Jenkins :**
   - Allez dans Jenkins > Manage Jenkins > Manage Credentials
   - Sélectionnez le domaine global
   - Cliquez sur "Add Credentials"
   - Type : "Username with password"
   - ID : `dockerhub-credentials`
   - Username : Votre nom d'utilisateur Docker Hub
   - Password : Votre token d'accès Docker Hub

### 3. Credentials Render (déjà configurés ✅)

- ID : `render-api-key`
- Type : Secret text
- Valeur : Votre clé API Render

## Configuration des Outils

### 1. JDK-11

1. **Installer JDK-11 sur le système :**
   ```bash
   # Exécuter le script de configuration
   ./scripts/configure-jdk-jenkins.sh
   ```

2. **Configurer dans Jenkins :**
   - Allez dans Jenkins > Manage Jenkins > Global Tool Configuration
   - Dans la section JDK, cliquez sur "Add JDK"
   - Nom : `JDK-11`
   - JAVA_HOME : `/opt/homebrew/opt/openjdk@11/libexec/openjdk.jdk/Contents/Home` (macOS)
   - Installer automatiquement : false

### 2. Docker

1. **Installer Docker :**
   ```bash
   # Exécuter le script d'installation
   ./scripts/install-docker-jenkins.sh
   ```

2. **Vérifier l'installation :**
   ```bash
   docker --version
   docker info
   ```

## Test de la Configuration

Après avoir configuré tous les credentials et outils :

1. **Exécuter le script de test :**
   ```bash
   # Dans Jenkins, exécuter le pipeline test-credentials.groovy
   ```

2. **Vérifier les résultats :**
   - ✅ Docker Hub : Login et push réussis
   - ✅ GitHub : Connexion SSH réussie
   - ✅ Render : API accessible
   - ✅ Intégration : Test complet réussi

## Dépannage

### Problème : Docker non trouvé
```bash
# Vérifier que Docker est dans le PATH
echo $PATH
which docker

# Redémarrer Jenkins après installation Docker
sudo systemctl restart jenkins  # Linux
# ou redémarrer Jenkins manuellement sur macOS
```

### Problème : JDK non trouvé
```bash
# Vérifier JAVA_HOME
echo $JAVA_HOME
java -version

# Redémarrer Jenkins après configuration JDK
```

### Problème : Credentials GitHub
```bash
# Tester la connexion SSH manuellement
ssh -T -i ~/.ssh/github_jenkins git@github.com
```

## Scripts Utiles

- `scripts/install-docker-jenkins.sh` : Installation Docker
- `scripts/configure-jdk-jenkins.sh` : Configuration JDK-11
- `jenkins/test-credentials.groovy` : Test des credentials

## Notes Importantes

1. **Redémarrage requis :** Redémarrez Jenkins après avoir installé Docker et configuré JDK
2. **Permissions :** Assurez-vous que l'utilisateur Jenkins a les permissions nécessaires
3. **Sécurité :** Ne commitez jamais les clés privées dans le repository
4. **Tests :** Exécutez toujours les tests de credentials avant de déployer en production
