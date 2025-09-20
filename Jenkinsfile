pipeline {
    agent any
    
    tools {
        jdk 'JDK-11'
        maven 'Maven'
    }
    
    environment {
        // Variables d'environnement
        RENDER_SERVICE_ID = credentials('render-service-id')
        RENDER_API_KEY = credentials('render-api-key')
        MAVEN_OPTS = '-Xmx1024m'
        PATH = "/usr/local/bin:${env.PATH}"
        DOCKER_BUILDKIT = "1"
        JAVA_HOME = tool('JDK-11')
    }
    
    options {
        // Options du pipeline
        timeout(time: 30, unit: 'MINUTES')
        retry(2)
        timestamps()
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo '🔄 Récupération du code source...'
                checkout scm
                script {
                    env.GIT_COMMIT_SHORT = sh(
                        script: 'git rev-parse --short HEAD',
                        returnStdout: true
                    ).trim()
                }
            }
        }
        
        stage('Build') {
            steps {
                echo '🔨 Construction de l\'application Maven...'
                sh 'mvn clean compile -DskipTests'
            }
        }
        
        stage('Package') {
            steps {
                echo '📦 Création du package JAR...'
                sh 'mvn package -DskipTests'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }
        
        stage('Docker Build') {
            steps {
                script {
                    // Login Docker Hub avec credentials
                    withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials', usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWORD')]) {
                        sh 'echo $DOCKER_PASSWORD | docker login -u $DOCKER_USERNAME --password-stdin'
                    }
                    
                    // Construire l'image Docker avec optimisations
                    withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials', usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWORD')]) {
                        sh '''                            
                            # Nettoyer les images Docker inutiles avant le build
                            docker image prune -f || true
                         
                            docker build \
                                -t ${DOCKER_USERNAME}/${JOB_NAME}:${BUILD_NUMBER} \
                                -t ${DOCKER_USERNAME}/${JOB_NAME}:latest \
                                .
                            docker push ${DOCKER_USERNAME}/${JOB_NAME}:${BUILD_NUMBER} &
                            docker push ${DOCKER_USERNAME}/${JOB_NAME}:latest &
                            wait
                        '''
                    }
                }
            }
        }
        
        stage('Deploy to Render') {
            steps {
                echo '🚀 Déploiement sur Render...'
                script {
                    withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials', usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWORD')]) {
                        sh '''
                            echo "=== Déploiement sur Render via API ==="
                            echo "Service ID: ${RENDER_SERVICE_ID}"
                            echo "Image Docker: ${DOCKER_USERNAME}/${JOB_NAME}:${BUILD_NUMBER}"
                            
                            # Déploiement via API REST de Render
                            DEPLOY_RESPONSE=$(curl -s -X POST \
                                -H "Authorization: Bearer ${RENDER_API_KEY}" \
                                -H "Content-Type: application/json" \
                                -d '{
                                    "serviceId": "'${RENDER_SERVICE_ID}'",
                                    "image": "'${DOCKER_USERNAME}/${JOB_NAME}:${BUILD_NUMBER}'",
                                    "autoDeploy": true
                                }' \
                                "https://api.render.com/v1/services/${RENDER_SERVICE_ID}/deploys")
                            
                            
                            # Vérifier si le déploiement a été initié avec succès
                            if echo "$DEPLOY_RESPONSE" | grep -q '"id"'; then
                                echo "✅ Déploiement initié avec succès sur Render!"
                                echo "📋 Détails du déploiement:"
                                echo "$DEPLOY_RESPONSE" | jq '.' 2>/dev/null || echo "$DEPLOY_RESPONSE"
                            else
                                echo "⚠️ Problème lors du déploiement:"
                                echo "$DEPLOY_RESPONSE"
                                echo "🔄 Tentative alternative avec image latest..."
                                
                                # Tentative avec l'image latest
                                DEPLOY_RESPONSE_LATEST=$(curl -s -X POST \
                                    -H "Authorization: Bearer ${RENDER_API_KEY}" \
                                    -H "Content-Type: application/json" \
                                    -d '{
                                        "serviceId": "'${RENDER_SERVICE_ID}'",
                                        "image": "'${DOCKER_USERNAME}/${JOB_NAME}:latest'",
                                        "autoDeploy": true
                                    }' \
                                    "https://api.render.com/v1/services/${RENDER_SERVICE_ID}/deploys")
                                
                                if echo "$DEPLOY_RESPONSE_LATEST" | grep -q '"id"'; then
                                    echo "✅ Déploiement avec image latest réussi!"
                                else
                                    echo "❌ Échec du déploiement avec l'image latest aussi"
                                    echo "Réponse: $DEPLOY_RESPONSE_LATEST"
                                fi
                            fi
                        '''
                    }
                }
            }
        }
    }
    
    post {
        always {
            echo '🧹 Nettoyage des images Docker locales...'
            script {
                try {
                    sh '''
                        # Nettoyer seulement si Docker est disponible
                        if command -v docker >/dev/null 2>&1; then
                            docker image prune -f || true
                            docker system prune -f || true
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
            echo '✅ Pipeline exécuté avec succès!'
            script {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials', usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWORD')]) {
                    def message = """
                    🎉 **Déploiement réussi!**
                    
                    **Détails:**
                    - Build: #${env.BUILD_NUMBER}
                    - Commit: ${env.GIT_COMMIT_SHORT}
                    - Image: ${env.DOCKER_USERNAME}/${env.JOB_NAME}:${env.BUILD_NUMBER}
                    - Branche: ${env.BRANCH_NAME}
                    
                    **Liens:**
                    - [Docker Hub](https://hub.docker.com/r/${env.DOCKER_USERNAME}/${env.JOB_NAME})
                    - [Jenkins Build](http://localhost:8080/job/${env.JOB_NAME}/${env.BUILD_NUMBER}/)
                    """
                    echo message
                }
            }
        }
        
        failure {
            echo '❌ Pipeline échoué!'
            script {
                def message = """
                💥 **Déploiement échoué!**
                
                **Détails:**
                - Build: #${env.BUILD_NUMBER}
                - Commit: ${env.GIT_COMMIT_SHORT}
                - Branche: ${env.BRANCH_NAME}
                
                **Vérifiez les logs pour plus de détails.**
                """
                echo message
            }
        }
        
        unstable {
            echo '⚠️ Pipeline instable!'
        }
    }
}
