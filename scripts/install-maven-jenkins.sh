#!/bin/bash

# Script d'installation Maven pour Jenkins
# Ce script installe Maven sur le nœud Jenkins

set -e

echo "📦 Installation de Maven pour Jenkins..."

# Vérifier si Maven est déjà installé
if command -v mvn &> /dev/null; then
    echo "✅ Maven est déjà installé"
    mvn -version
    exit 0
fi

# Détecter l'OS
if [[ "$OSTYPE" == "darwin"* ]]; then
    echo "🍎 Détection macOS"
    
    # Vérifier si Homebrew est installé
    if ! command -v brew &> /dev/null; then
        echo "📦 Installation de Homebrew..."
        /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
    fi
    
    # Installer Maven via Homebrew
    echo "📦 Installation de Maven via Homebrew..."
    brew install maven
    
    # Vérifier l'installation
    echo "🔍 Vérification de l'installation..."
    mvn -version
    
    # Afficher le chemin Maven
    MAVEN_HOME=$(brew --prefix maven)
    echo "MAVEN_HOME: $MAVEN_HOME"
    echo "Maven binary: $(which mvn)"
    
    echo "✅ Maven installé sur macOS"

elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
    echo "🐧 Détection Linux"
    
    # Mettre à jour les paquets
    sudo apt-get update
    
    # Installer Maven
    sudo apt-get install -y maven
    
    # Vérifier l'installation
    echo "🔍 Vérification de l'installation..."
    mvn -version
    
    # Afficher le chemin Maven
    MAVEN_HOME=$(mvn -version | grep "Maven home" | cut -d: -f2 | xargs)
    echo "MAVEN_HOME: $MAVEN_HOME"
    echo "Maven binary: $(which mvn)"
    
    echo "✅ Maven installé sur Linux"

else
    echo "❌ OS non supporté: $OSTYPE"
    echo "Veuillez installer Maven manuellement"
    exit 1
fi

# Vérifier l'installation
echo "🔍 Vérification finale..."
mvn -version

# Créer un fichier de configuration pour Jenkins
cat > /tmp/jenkins-maven-config.properties << EOF
# Configuration Maven pour Jenkins
# Ajoutez ces propriétés dans Jenkins > Manage Jenkins > Global Tool Configuration > Maven

# Nom: Maven-3.9
# MAVEN_HOME: $MAVEN_HOME
# Installer automatiquement: false
EOF

echo "📄 Configuration sauvegardée dans /tmp/jenkins-maven-config.properties"
echo ""
echo "🔧 Instructions pour Jenkins:"
echo "1. Allez dans Jenkins > Manage Jenkins > Global Tool Configuration"
echo "2. Dans la section Maven, ajoutez:"
echo "   - Nom: Maven-3.9"
echo "   - MAVEN_HOME: $MAVEN_HOME"
echo "   - Installer automatiquement: false"
echo "3. Sauvegardez la configuration"

echo "✅ Installation Maven terminée!"
