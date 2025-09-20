#!/bin/bash

# Script de déploiement sur Render (sans webhooks)
# Ce script peut être exécuté manuellement ou via Jenkins

set -e

# Configuration
DOCKER_USERNAME="${DOCKER_USERNAME:-Cheeikh}"
DOCKER_IMAGE="spring-boot-demo"
DOCKER_TAG="${DOCKER_TAG:-latest}"
RENDER_SERVICE_ID="${RENDER_SERVICE_ID}"
RENDER_API_KEY="${RENDER_API_KEY}"

# Couleurs pour les logs
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Fonction de logging
log() {
    echo -e "${BLUE}[$(date +'%Y-%m-%d %H:%M:%S')]${NC} $1"
}

error() {
    echo -e "${RED}[ERROR]${NC} $1"
    exit 1
}

success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# Vérification des prérequis
check_prerequisites() {
    log "Vérification des prérequis..."
    
    if [ -z "$RENDER_SERVICE_ID" ]; then
        error "RENDER_SERVICE_ID n'est pas défini"
    fi
    
    if [ -z "$RENDER_API_KEY" ]; then
        error "RENDER_API_KEY n'est pas défini"
    fi
    
    if ! command -v docker &> /dev/null; then
        error "Docker n'est pas installé"
    fi
    
    if ! command -v curl &> /dev/null; then
        error "curl n'est pas installé"
    fi
    
    success "Prérequis validés"
}

# Login Docker Hub
docker_login() {
    log "Connexion à Docker Hub..."
    
    if [ -z "$DOCKER_PASSWORD" ]; then
        error "DOCKER_PASSWORD n'est pas défini"
    fi
    
    echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin
    
    if [ $? -eq 0 ]; then
        success "Connexion Docker Hub réussie"
    else
        error "Échec de la connexion Docker Hub"
    fi
}

# Construction de l'image Docker
build_docker_image() {
    log "Construction de l'image Docker..."
    
    docker build -t "${DOCKER_USERNAME}/${DOCKER_IMAGE}:${DOCKER_TAG}" .
    
    if [ $? -eq 0 ]; then
        success "Image Docker construite: ${DOCKER_USERNAME}/${DOCKER_IMAGE}:${DOCKER_TAG}"
    else
        error "Échec de la construction de l'image Docker"
    fi
}

# Push vers Docker Hub
push_docker_image() {
    log "Push de l'image vers Docker Hub..."
    
    docker push "${DOCKER_USERNAME}/${DOCKER_IMAGE}:${DOCKER_TAG}"
    
    if [ $? -eq 0 ]; then
        success "Image poussée vers Docker Hub"
    else
        error "Échec du push vers Docker Hub"
    fi
}

# Déploiement sur Render via API
deploy_to_render_api() {
    log "Déploiement sur Render via API..."
    
    local response=$(curl -s -w "\n%{http_code}" \
        -X POST \
        -H "Authorization: Bearer ${RENDER_API_KEY}" \
        -H "Content-Type: application/json" \
        -d "{\"image\": \"${DOCKER_USERNAME}/${DOCKER_IMAGE}:${DOCKER_TAG}\"}" \
        "https://api.render.com/v1/services/${RENDER_SERVICE_ID}/deploys")
    
    local http_code=$(echo "$response" | tail -n1)
    local body=$(echo "$response" | head -n -1)
    
    if [ "$http_code" -eq 200 ] || [ "$http_code" -eq 201 ]; then
        success "Déploiement sur Render réussi (HTTP $http_code)"
        echo "Response: $body"
    else
        error "Échec du déploiement sur Render (HTTP $http_code)"
        echo "Response: $body"
    fi
}

# Déploiement sur Render via CLI
deploy_to_render_cli() {
    log "Déploiement sur Render via CLI..."
    
    # Installation de Render CLI si nécessaire
    if ! command -v render &> /dev/null; then
        log "Installation de Render CLI..."
        curl -fsSL https://cli.render.com/install.sh | sh
        export PATH="$HOME/.local/bin:$PATH"
    fi
    
    # Configuration du CLI
    export RENDER_API_KEY="${RENDER_API_KEY}"
    
    # Déploiement
    render service deploy "${RENDER_SERVICE_ID}" --image "${DOCKER_USERNAME}/${DOCKER_IMAGE}:${DOCKER_TAG}"
    
    if [ $? -eq 0 ]; then
        success "Déploiement via CLI réussi"
    else
        error "Échec du déploiement via CLI"
    fi
}

# Vérification du déploiement
verify_deployment() {
    log "Vérification du déploiement..."
    
    # Attendre que le service soit prêt
    local max_attempts=30
    local attempt=1
    
    while [ $attempt -le $max_attempts ]; do
        log "Tentative $attempt/$max_attempts - Vérification du service..."
        
        local service_info=$(curl -s \
            -H "Authorization: Bearer ${RENDER_API_KEY}" \
            "https://api.render.com/v1/services/${RENDER_SERVICE_ID}")
        
        local status=$(echo "$service_info" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)
        
        if [ "$status" = "live" ]; then
            success "Service déployé et en ligne"
            break
        elif [ "$status" = "build_failed" ] || [ "$status" = "deploy_failed" ]; then
            error "Échec du déploiement (status: $status)"
        else
            log "Status actuel: $status - Attente..."
            sleep 10
            ((attempt++))
        fi
    done
    
    if [ $attempt -gt $max_attempts ]; then
        warning "Timeout lors de la vérification du déploiement"
    fi
}

# Nettoyage
cleanup() {
    log "Nettoyage des images Docker locales..."
    
    # Supprimer l'image locale
    docker rmi "${DOCKER_USERNAME}/${DOCKER_IMAGE}:${DOCKER_TAG}" 2>/dev/null || true
    
    # Nettoyer les images inutilisées
    docker image prune -f
    
    success "Nettoyage terminé"
}

# Fonction principale
main() {
    log "=== Déploiement sur Render ==="
    log "Image: ${DOCKER_USERNAME}/${DOCKER_IMAGE}:${DOCKER_TAG}"
    log "Service Render: ${RENDER_SERVICE_ID}"
    
    check_prerequisites
    docker_login
    build_docker_image
    push_docker_image
    
    # Essayer d'abord l'API, puis le CLI en fallback
    if deploy_to_render_api; then
        verify_deployment
    else
        warning "Déploiement via API échoué, tentative avec CLI..."
        deploy_to_render_cli
        verify_deployment
    fi
    
    cleanup
    
    success "=== Déploiement terminé avec succès ==="
}

# Gestion des arguments
case "${1:-}" in
    --api-only)
        check_prerequisites
        docker_login
        build_docker_image
        push_docker_image
        deploy_to_render_api
        verify_deployment
        cleanup
        ;;
    --cli-only)
        check_prerequisites
        docker_login
        build_docker_image
        push_docker_image
        deploy_to_render_cli
        verify_deployment
        cleanup
        ;;
    --help)
        echo "Usage: $0 [--api-only|--cli-only|--help]"
        echo ""
        echo "Options:"
        echo "  --api-only    Utiliser uniquement l'API Render"
        echo "  --cli-only    Utiliser uniquement le CLI Render"
        echo "  --help        Afficher cette aide"
        echo ""
        echo "Variables d'environnement requises:"
        echo "  RENDER_SERVICE_ID    ID du service Render"
        echo "  RENDER_API_KEY       Clé API Render"
        echo "  DOCKER_USERNAME      Nom d'utilisateur Docker Hub"
        echo "  DOCKER_PASSWORD      Mot de passe Docker Hub"
        echo "  DOCKER_TAG           Tag de l'image (défaut: latest)"
        ;;
    *)
        main
        ;;
esac
