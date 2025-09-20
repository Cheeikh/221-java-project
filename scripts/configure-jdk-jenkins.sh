#!/bin/bash

# Script de configuration JDK-11 pour Jenkins
# Ce script configure JDK-11 sur le nœud Jenkins

set -e

echo "☕ Configuration JDK-11 pour Jenkins..."

# Détecter l'OS
if [[ "$OSTYPE" == "darwin"* ]]; then
    echo "🍎 Détection macOS"
    
    # Vérifier si Homebrew est installé
    if ! command -v brew &> /dev/null; then
        echo "📦 Installation de Homebrew..."
        /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
    fi
    
    # Installer OpenJDK 11
    echo "📦 Installation d'OpenJDK 11..."
    brew install openjdk@11
    
    # Créer un lien symbolique
    sudo ln -sfn /opt/homebrew/opt/openjdk@11/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-11.jdk
    
    # Définir JAVA_HOME
    export JAVA_HOME="/opt/homebrew/opt/openjdk@11/libexec/openjdk.jdk/Contents/Home"
    
    echo "✅ OpenJDK 11 installé sur macOS"
    echo "JAVA_HOME: $JAVA_HOME"

elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
    echo "🐧 Détection Linux"
    
    # Mettre à jour les paquets
    sudo apt-get update
    
    # Installer OpenJDK 11
    sudo apt-get install -y openjdk-11-jdk
    
    # Définir JAVA_HOME
    export JAVA_HOME="/usr/lib/jvm/java-11-openjdk-amd64"
    
    echo "✅ OpenJDK 11 installé sur Linux"
    echo "JAVA_HOME: $JAVA_HOME"

else
    echo "❌ OS non supporté: $OSTYPE"
    echo "Veuillez installer JDK-11 manuellement"
    exit 1
fi

# Vérifier l'installation
echo "🔍 Vérification de l'installation..."
java -version
javac -version

# Afficher JAVA_HOME
echo "JAVA_HOME: $JAVA_HOME"

# Créer un fichier de configuration pour Jenkins
cat > /tmp/jenkins-jdk-config.properties << EOF
# Configuration JDK-11 pour Jenkins
# Ajoutez ces propriétés dans Jenkins > Manage Jenkins > Global Tool Configuration > JDK

# Nom: JDK-11
# JAVA_HOME: $JAVA_HOME
# Installer automatiquement: false
EOF

echo "📄 Configuration sauvegardée dans /tmp/jenkins-jdk-config.properties"
echo ""
echo "🔧 Instructions pour Jenkins:"
echo "1. Allez dans Jenkins > Manage Jenkins > Global Tool Configuration"
echo "2. Dans la section JDK, ajoutez:"
echo "   - Nom: JDK-11"
echo "   - JAVA_HOME: $JAVA_HOME"
echo "   - Installer automatiquement: false"
echo "3. Sauvegardez la configuration"

echo "✅ Configuration JDK-11 terminée!"
