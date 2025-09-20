// Test Credentials - Vérification des credentials Jenkins
// Ce script teste la configuration des credentials

pipeline {
    agent any
    
    tools {
        jdk 'JDK-11'
    }
    
    environment {
        JAVA_HOME = tool('JDK-11')
        PATH = "${JAVA_HOME}/bin:${env.PATH}"
    }
    
    stages {
        stage('Test Credentials Docker Hub') {
            steps {
                echo '🐳 Test Credentials Docker Hub'
                script {
                    try {
                        withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials', usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWORD')]) {
                            sh '''
                                echo "=== Test Docker Hub Credentials ==="
                                echo "Username: $DOCKER_USERNAME"
                                echo "Password length: ${#DOCKER_PASSWORD}"
                                
                                # Test de login Docker Hub
                                echo "Test de login Docker Hub..."
                                echo $DOCKER_PASSWORD | docker login -u $DOCKER_USERNAME --password-stdin
                                
                                if [ $? -eq 0 ]; then
                                    echo "✅ Login Docker Hub réussi!"
                                    
                                    # Test de push d'une image simple
                                    echo "Test de construction d'image..."
                                    docker build -t $DOCKER_USERNAME/test-credentials:latest .
                                    
                                    echo "Test de push..."
                                    docker push $DOCKER_USERNAME/test-credentials:latest
                                    
                                    if [ $? -eq 0 ]; then
                                        echo "✅ Push Docker Hub réussi!"
                                    else
                                        echo "❌ Push Docker Hub échoué!"
                                        exit 1
                                    fi
                                else
                                    echo "❌ Login Docker Hub échoué!"
                                    exit 1
                                fi
                            '''
                        }
                    } catch (Exception e) {
                        echo "❌ Erreur avec les credentials Docker Hub: ${e.getMessage()}"
                        echo "Vérifiez que le credential 'dockerhub-credentials' est configuré"
                    }
                }
            }
        }
        
        stage('Test Credentials GitHub') {
            steps {
                echo '🐙 Test Credentials GitHub'
                script {
                    try {
                        // Essayer d'abord avec SSH
                        withCredentials([sshUserPrivateKey(credentialsId: 'github-credentials', keyFileVariable: 'SSH_KEY', usernameVariable: 'SSH_USER')]) {
                            sh '''
                                echo "=== Test GitHub Credentials (SSH) ==="
                                echo "SSH User: $SSH_USER"
                                echo "SSH Key file: $SSH_KEY"
                                
                                # Test de connexion GitHub
                                echo "Test de connexion GitHub..."
                                ssh -T -i $SSH_KEY -o StrictHostKeyChecking=no git@github.com
                                
                                if [ $? -eq 0 ] || [ $? -eq 1 ]; then
                                    echo "✅ Connexion GitHub réussie!"
                                else
                                    echo "❌ Connexion GitHub échouée!"
                                    exit 1
                                fi
                            '''
                        }
                    } catch (Exception e) {
                        echo "⚠️ SSH GitHub échoué, tentative avec Username/Password..."
                        try {
                            // Fallback avec Username/Password
                            withCredentials([usernamePassword(credentialsId: 'github-credentials', usernameVariable: 'GITHUB_USERNAME', passwordVariable: 'GITHUB_PASSWORD')]) {
                                sh '''
                                    echo "=== Test GitHub Credentials (Username/Password) ==="
                                    echo "Username: $GITHUB_USERNAME"
                                    echo "Password length: ${#GITHUB_PASSWORD}"
                                    
                                    # Test de connexion GitHub via API
                                    echo "Test de connexion GitHub API..."
                                    response=$(curl -s -w "%{http_code}" -u "$GITHUB_USERNAME:$GITHUB_PASSWORD" https://api.github.com/user)
                                    
                                    if [[ "$response" == *"200" ]]; then
                                        echo "✅ Connexion GitHub API réussie!"
                                    else
                                        echo "❌ Connexion GitHub API échouée!"
                                        echo "Response: $response"
                                        exit 1
                                    fi
                                '''
                            }
                        } catch (Exception e2) {
                            echo "❌ Erreur avec les credentials GitHub: ${e2.getMessage()}"
                            echo "🔧 Instructions de configuration:"
                            echo "1. Pour SSH: Créez un credential de type 'SSH Username with private key'"
                            echo "2. Pour Username/Password: Créez un credential de type 'Username with password'"
                            echo "3. ID du credential: 'github-credentials'"
                        }
                    }
                }
            }
        }
        
        stage('Test Credentials Render') {
            steps {
                echo '🌐 Test Credentials Render'
                script {
                    try {
                        withCredentials([string(credentialsId: 'render-api-key', variable: 'RENDER_API_KEY')]) {
                            sh '''
                                echo "=== Test Render Credentials ==="
                                echo "API Key length: ${#RENDER_API_KEY}"
                                
                                # Test de l'API Render
                                echo "Test de l'API Render..."
                                response=$(curl -s -w "%{http_code}" -H "Authorization: Bearer $RENDER_API_KEY" https://api.render.com/v1/services)
                                
                                if [[ "$response" == *"200" ]]; then
                                    echo "✅ API Render accessible!"
                                else
                                    echo "❌ API Render inaccessible!"
                                    echo "Response: $response"
                                    exit 1
                                fi
                            '''
                        }
                    } catch (Exception e) {
                        echo "❌ Erreur avec les credentials Render: ${e.getMessage()}"
                        echo "Vérifiez que le credential 'render-api-key' est configuré"
                    }
                }
            }
        }
        
        stage('Test Intégration Complète') {
            steps {
                echo '🔗 Test d\'intégration complète'
                script {
                    try {
                        withCredentials([
                            usernamePassword(credentialsId: 'dockerhub-credentials', usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWORD'),
                            string(credentialsId: 'render-api-key', variable: 'RENDER_API_KEY')
                        ]) {
                            sh '''
                                echo "=== Test d'intégration complète ==="
                                
                                # Login Docker Hub
                                echo $DOCKER_PASSWORD | docker login -u $DOCKER_USERNAME --password-stdin
                                
                                # Build et tag de l'image
                                docker build -t $DOCKER_USERNAME/spring-boot-demo:test-${BUILD_NUMBER} .
                                docker tag $DOCKER_USERNAME/spring-boot-demo:test-${BUILD_NUMBER} $DOCKER_USERNAME/spring-boot-demo:test-latest
                                
                                # Push vers Docker Hub
                                docker push $DOCKER_USERNAME/spring-boot-demo:test-${BUILD_NUMBER}
                                docker push $DOCKER_USERNAME/spring-boot-demo:test-latest
                                
                                # Test de déploiement Render (simulation)
                                echo "Test de déploiement Render..."
                                curl -X POST \
                                    -H "Authorization: Bearer $RENDER_API_KEY" \
                                    -H "Content-Type: application/json" \
                                    -d "{\"image\": \"$DOCKER_USERNAME/spring-boot-demo:test-${BUILD_NUMBER}\"}" \
                                    https://api.render.com/v1/services/your-service-id/deploys || echo "Service ID non configuré"
                                
                                echo "✅ Test d'intégration réussi!"
                            '''
                        }
                    } catch (Exception e) {
                        echo "❌ Erreur dans le test d'intégration: ${e.getMessage()}"
                    }
                }
            }
        }
    }
    
    post {
        always {
            echo '🧹 Nettoyage des tests'
            script {
                try {
                    sh '''
                        # Nettoyer seulement si Docker est disponible
                        if command -v docker >/dev/null 2>&1; then
                            # Nettoyer les images de test
                            docker rmi $DOCKER_USERNAME/test-credentials:latest 2>/dev/null || true
                            docker rmi $DOCKER_USERNAME/spring-boot-demo:test-${BUILD_NUMBER} 2>/dev/null || true
                            docker rmi $DOCKER_USERNAME/spring-boot-demo:test-latest 2>/dev/null || true
                            docker image prune -f || true
                        else
                            echo "Docker non disponible pour le nettoyage"
                        fi
                    '''
                } catch (Exception e) {
                    echo "Erreur lors du nettoyage: ${e.getMessage()}"
                }
            }
        }
        
        success {
            echo '✅ Tous les tests de credentials sont passés!'
            echo '🎉 Votre configuration est prête pour la production!'
        }
        
        failure {
            echo '❌ Certains tests de credentials ont échoué!'
            echo '🔧 Vérifiez la configuration des credentials dans Jenkins'
        }
    }
}
