#!/bin/bash

# Script de diagnostic Jenkins
# Ce script vérifie la configuration complète de Jenkins

set -e

echo "🔍 Diagnostic Jenkins - Vérification de la configuration"
echo "=================================================="

# Couleurs pour l'affichage
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Fonction pour afficher les résultats
print_status() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✅ $2${NC}"
    else
        echo -e "${RED}❌ $2${NC}"
    fi
}

print_warning() {
    echo -e "${YELLOW}⚠️ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ️ $1${NC}"
}

echo ""
echo "1. Vérification de l'environnement système"
echo "----------------------------------------"

# Vérifier l'OS
print_info "Système d'exploitation: $(uname -s)"
print_info "Architecture: $(uname -m)"

# Vérifier les outils de base
echo ""
echo "2. Vérification des outils de base"
echo "--------------------------------"

# Java
if command -v java >/dev/null 2>&1; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1)
    print_status 0 "Java installé: $JAVA_VERSION"
    
    # Vérifier JAVA_HOME
    if [ -n "$JAVA_HOME" ]; then
        print_status 0 "JAVA_HOME configuré: $JAVA_HOME"
    else
        print_warning "JAVA_HOME non configuré"
    fi
else
    print_status 1 "Java non installé"
fi

# Maven
if command -v mvn >/dev/null 2>&1; then
    MAVEN_VERSION=$(mvn -version 2>&1 | head -n 1)
    print_status 0 "Maven installé: $MAVEN_VERSION"
else
    print_status 1 "Maven non installé"
fi

# Docker
if command -v docker >/dev/null 2>&1; then
    DOCKER_VERSION=$(docker --version)
    print_status 0 "Docker installé: $DOCKER_VERSION"
    
    # Vérifier que Docker daemon fonctionne
    if docker info >/dev/null 2>&1; then
        print_status 0 "Docker daemon accessible"
    else
        print_status 1 "Docker daemon non accessible"
        print_warning "Démarrez Docker Desktop et réessayez"
    fi
else
    print_status 1 "Docker non installé"
    print_warning "Exécutez: ./scripts/install-docker-jenkins.sh"
fi

# Git
if command -v git >/dev/null 2>&1; then
    GIT_VERSION=$(git --version)
    print_status 0 "Git installé: $GIT_VERSION"
else
    print_status 1 "Git non installé"
fi

# Curl
if command -v curl >/dev/null 2>&1; then
    print_status 0 "Curl installé"
else
    print_status 1 "Curl non installé"
fi

echo ""
echo "3. Vérification de la configuration Jenkins"
echo "------------------------------------------"

# Vérifier si Jenkins est en cours d'exécution
if pgrep -f jenkins >/dev/null 2>&1; then
    print_status 0 "Jenkins en cours d'exécution"
else
    print_warning "Jenkins ne semble pas être en cours d'exécution"
fi

# Vérifier les ports Jenkins
if netstat -an 2>/dev/null | grep -q ":8080.*LISTEN"; then
    print_status 0 "Port 8080 (Jenkins) ouvert"
else
    print_warning "Port 8080 (Jenkins) non accessible"
fi

echo ""
echo "4. Vérification des credentials"
echo "------------------------------"

# Vérifier les fichiers de credentials (si accessibles)
if [ -d "$HOME/.jenkins" ]; then
    print_info "Répertoire Jenkins trouvé: $HOME/.jenkins"
else
    print_warning "Répertoire Jenkins non trouvé"
fi

echo ""
echo "5. Vérification de la connectivité"
echo "--------------------------------"

# Test de connectivité GitHub
if curl -s --connect-timeout 5 https://github.com >/dev/null; then
    print_status 0 "GitHub accessible"
else
    print_status 1 "GitHub non accessible"
fi

# Test de connectivité Docker Hub
if curl -s --connect-timeout 5 https://hub.docker.com >/dev/null; then
    print_status 0 "Docker Hub accessible"
else
    print_status 1 "Docker Hub non accessible"
fi

# Test de connectivité Render
if curl -s --connect-timeout 5 https://api.render.com >/dev/null; then
    print_status 0 "Render API accessible"
else
    print_status 1 "Render API non accessible"
fi

echo ""
echo "6. Vérification des permissions"
echo "-----------------------------"

# Vérifier les permissions Docker
if groups | grep -q docker; then
    print_status 0 "Utilisateur dans le groupe docker"
else
    print_warning "Utilisateur pas dans le groupe docker"
    print_info "Exécutez: sudo usermod -aG docker $USER"
fi

# Vérifier les permissions d'écriture
if [ -w "$HOME" ]; then
    print_status 0 "Permissions d'écriture sur $HOME"
else
    print_status 1 "Pas de permissions d'écriture sur $HOME"
fi

echo ""
echo "7. Recommandations"
echo "-----------------"

# Recommandations basées sur les vérifications
if ! command -v docker >/dev/null 2>&1; then
    echo "🔧 Installez Docker: ./scripts/install-docker-jenkins.sh"
fi

if ! command -v java >/dev/null 2>&1; then
    echo "🔧 Installez Java: ./scripts/configure-jdk-jenkins.sh"
fi

if ! docker info >/dev/null 2>&1; then
    echo "🔧 Démarrez Docker Desktop"
fi

if ! groups | grep -q docker; then
    echo "🔧 Ajoutez l'utilisateur au groupe docker: sudo usermod -aG docker $USER"
fi

echo ""
echo "8. Prochaines étapes"
echo "------------------"
echo "1. Configurez les credentials dans Jenkins:"
echo "   - dockerhub-credentials (Username/Password)"
echo "   - github-credentials (SSH ou Username/Password)"
echo "   - render-api-key (Secret text)"
echo ""
echo "2. Configurez JDK-11 dans Jenkins:"
echo "   - Manage Jenkins > Global Tool Configuration > JDK"
echo "   - Nom: JDK-11"
echo "   - JAVA_HOME: $(echo $JAVA_HOME)"
echo ""
echo "3. Testez la configuration:"
echo "   - Exécutez le pipeline test-credentials.groovy"
echo "   - Vérifiez que tous les tests passent"

echo ""
echo "=================================================="
echo "🏁 Diagnostic terminé!"
