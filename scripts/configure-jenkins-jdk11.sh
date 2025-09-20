#!/bin/bash

# Script de configuration JDK-11 pour Jenkins
# Ce script configure automatiquement JDK-11 dans Jenkins

set -e

echo "☕ Configuration JDK-11 pour Jenkins..."

# Vérifier que OpenJDK 11 est installé
if [ ! -d "/opt/homebrew/opt/openjdk@11/libexec/openjdk.jdk/Contents/Home" ]; then
    echo "❌ OpenJDK 11 non trouvé. Installation..."
    brew install openjdk@11
fi

# Chemin JDK-11
JDK11_HOME="/opt/homebrew/opt/openjdk@11/libexec/openjdk.jdk/Contents/Home"

echo "✅ OpenJDK 11 trouvé: $JDK11_HOME"

# Vérifier la version
echo "Version JDK-11:"
"$JDK11_HOME/bin/java" -version

echo ""
echo "🔧 Configuration Jenkins:"
echo "1. Allez dans Jenkins > Manage Jenkins > Global Tool Configuration"
echo "2. Dans la section JDK, ajoutez:"
echo "   - Nom: JDK-11"
echo "   - JAVA_HOME: $JDK11_HOME"
echo "   - Installer automatiquement: false"
echo "3. Cliquez sur Save"

echo ""
echo "📄 Configuration sauvegardée dans /tmp/jenkins-jdk11-config.txt"
cat > /tmp/jenkins-jdk11-config.txt << EOF
Configuration JDK-11 pour Jenkins
================================

Nom: JDK-11
JAVA_HOME: $JDK11_HOME
Installer automatiquement: false

Instructions:
1. Allez dans Jenkins > Manage Jenkins > Global Tool Configuration
2. Dans la section JDK, cliquez sur "Add JDK"
3. Entrez les informations ci-dessus
4. Cliquez sur "Save"
EOF

echo "✅ Configuration JDK-11 terminée!"
echo "💡 Redémarrez Jenkins après la configuration"
