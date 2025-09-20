#!/bin/bash

# Script d'installation Docker pour Jenkins
# Ce script installe Docker sur le nœud Jenkins

set -e

echo "🐳 Installation de Docker pour Jenkins..."

# Vérifier si Docker est déjà installé
if command -v docker &> /dev/null; then
    echo "✅ Docker est déjà installé"
    docker --version
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
    
    # Installer Docker Desktop via Homebrew
    echo "📦 Installation de Docker Desktop..."
    brew install --cask docker
    
    # Démarrer Docker Desktop
    echo "🚀 Démarrage de Docker Desktop..."
    open -a Docker
    
    # Attendre que Docker soit prêt
    echo "⏳ Attente que Docker soit prêt..."
    timeout=60
    while [ $timeout -gt 0 ]; do
        if docker info &> /dev/null; then
            echo "✅ Docker est prêt!"
            break
        fi
        sleep 2
        ((timeout-=2))
    done
    
    if [ $timeout -le 0 ]; then
        echo "❌ Timeout: Docker n'a pas démarré dans les temps"
        echo "Veuillez démarrer Docker Desktop manuellement"
        exit 1
    fi

elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
    echo "🐧 Détection Linux"
    
    # Mettre à jour les paquets
    sudo apt-get update
    
    # Installer les dépendances
    sudo apt-get install -y \
        apt-transport-https \
        ca-certificates \
        curl \
        gnupg \
        lsb-release
    
    # Ajouter la clé GPG Docker
    curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg
    
    # Ajouter le repository Docker
    echo \
        "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu \
        $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
    
    # Installer Docker
    sudo apt-get update
    sudo apt-get install -y docker-ce docker-ce-cli containerd.io
    
    # Démarrer Docker
    sudo systemctl start docker
    sudo systemctl enable docker
    
    # Ajouter l'utilisateur jenkins au groupe docker
    sudo usermod -aG docker jenkins
    
    echo "✅ Docker installé sur Linux"

else
    echo "❌ OS non supporté: $OSTYPE"
    echo "Veuillez installer Docker manuellement"
    exit 1
fi

# Vérifier l'installation
echo "🔍 Vérification de l'installation..."
docker --version
docker info

echo "✅ Installation Docker terminée!"
echo "💡 Redémarrez Jenkins après l'installation"
