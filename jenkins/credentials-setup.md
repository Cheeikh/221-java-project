# Configuration des Credentials Jenkins

## Credentials requis

### 1. Docker Hub Credentials
- **ID**: `dockerhub-credentials`
- **Type**: Username with password
- **Username**: Cheeikh
- **Password**: Votre token d'accès Docker Hub

### 2. GitHub Credentials
- **ID**: `github-credentials`
- **Type**: SSH Username with private key
- **Username**: `git`
- **Private Key**: Votre clé privée SSH pour GitHub

### 3. Render API Key
- **ID**: `render-api-key`
- **Type**: Secret text
- **Secret**: Votre clé API Render

## Configuration des Credentials

### Via l'interface Jenkins :
1. Allez dans **Manage Jenkins** > **Manage Credentials**
2. Sélectionnez le domaine global
3. Cliquez sur **Add Credentials**
4. Configurez chaque credential selon les spécifications ci-dessus

### Via l'API Jenkins :
```bash
# Docker Hub Credentials
curl -X POST \
  -u admin:password \
  -H "Content-Type: application/xml" \
  -d @dockerhub-credentials.xml \
  http://localhost:8080/credentials/store/system/domain/_/createCredentials

# GitHub Credentials
curl -X POST \
  -u admin:password \
  -H "Content-Type: application/xml" \
  -d @github-credentials.xml \
  http://localhost:8080/credentials/store/system/domain/_/createCredentials

# Render API Key
curl -X POST \
  -u admin:password \
  -H "Content-Type: application/xml" \
  -d @render-api-key.xml \
  http://localhost:8080/credentials/store/system/domain/_/createCredentials
```

## Fichiers de configuration XML

### dockerhub-credentials.xml
```xml
<com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl>
  <scope>GLOBAL</scope>
  <id>dockerhub-credentials</id>
  <description>Docker Hub credentials</description>
  <username>Cheeikh</username>
  <password>your-dockerhub-token</password>
</com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl>
```

### github-credentials.xml
```xml
<com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey>
  <scope>GLOBAL</scope>
  <id>github-credentials</id>
  <description>GitHub SSH credentials</description>
  <username>git</username>
  <privateKeySource class="com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey$DirectEntryPrivateKeySource">
    <privateKey>-----BEGIN OPENSSH PRIVATE KEY-----
your-private-key-here
-----END OPENSSH PRIVATE KEY-----</privateKey>
  </privateKeySource>
</com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey>
```

### render-api-key.xml
```xml
<org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl>
  <scope>GLOBAL</scope>
  <id>render-api-key</id>
  <description>Render API Key</description>
  <secret>your-render-api-key</secret>
</org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl>
```

## Plugins Jenkins requis

Assurez-vous d'avoir installé les plugins suivants :
- Pipeline
- Docker Pipeline
- Git
- GitHub
- Credentials Binding
- AnsiColor
- Timestamper
- Build Timeout
- Checkstyle
- SpotBugs
- HTML Publisher
- Test Results Analyzer
