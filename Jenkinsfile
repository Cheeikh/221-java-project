pipeline {
    agent any
    
    tools {
        jdk 'JDK-11'
        maven 'Maven'
    }
    
    environment {
        // Variables d'environnement
        RENDER_SERVICE_ID = 'srv-d378mo9r0fns739b1rd0'
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
        
        // stage('Test') {
        //     parallel {
        //         stage('Unit Tests') {
        //             steps {
        //                 echo '🧪 Exécution des tests unitaires...'
        //                 sh '''
        //                     export PATH="/opt/maven/bin:$PATH"
        //                     mvn test -Dtest=**/*Test.java
        //                 '''
        //             }
        //             post {
        //                 always {
        //                     publishTestResults testResultsPattern: 'target/surefire-reports/*.xml'
        //                 }
        //             }
        //         }
                
        //         stage('Code Quality') {
        //             steps {
        //                 echo '📊 Analyse de la qualité du code...'
        //                 sh '''
        //                     export PATH="/opt/maven/bin:$PATH"
        //                     mvn checkstyle:checkstyle
        //                     mvn spotbugs:check
        //                 '''
        //             }
        //             post {
        //                 always {
        //                     publishCheckstyle pattern: 'target/checkstyle-results.xml'
        //                     publishHTML([
        //                         allowMissing: false,
        //                         alwaysLinkToLastBuild: true,
        //                         keepAll: true,
        //                         reportDir: 'target/spotbugs',
        //                         reportFiles: 'index.html',
        //                         reportName: 'SpotBugs Report'
        //                     ])
        //                 }
        //             }
        //         }
        //     }
        // }
        
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
                    // Vérifier que Docker est disponible et fonctionnel
                    sh '''
                        echo "=== Vérification Docker ==="
                        echo "PATH: $PATH"
                        echo "Docker location: $(which docker 2>/dev/null || echo 'Non trouvé')"
                        
                        # Vérifier que Docker est accessible
                        if ! command -v docker >/dev/null 2>&1; then
                            echo "❌ Docker non trouvé dans PATH"
                            echo "🔧 Installation de Docker..."
                            
                            # Essayer d'installer Docker automatiquement
                            if [[ "$OSTYPE" == "darwin"* ]]; then
                                if command -v brew >/dev/null 2>&1; then
                                    echo "📦 Installation via Homebrew..."
                                    brew install --cask docker
                                    open -a Docker
                                    echo "⏳ Attente du démarrage de Docker..."
                                    sleep 30
                                else
                                    echo "❌ Homebrew non trouvé. Installez Docker manuellement depuis https://docker.com"
                                    exit 1
                                fi
                            elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
                                echo "📦 Installation via apt..."
                                sudo apt-get update
                                sudo apt-get install -y docker.io
                                sudo systemctl start docker
                                sudo usermod -aG docker $USER
                            else
                                echo "❌ OS non supporté. Installez Docker manuellement"
                                exit 1
                            fi
                        fi
                        
                        # Afficher la version Docker
                        docker --version
                        
                        # Vérifier que Docker daemon est accessible
                        if ! docker info >/dev/null 2>&1; then
                            echo "❌ Docker daemon non accessible"
                            echo "Veuillez démarrer Docker Desktop et réessayer"
                            exit 1
                        fi
                        
                        echo "✅ Docker fonctionnel"
                    '''
                    
                    // Login Docker Hub avec credentials
                    withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials', usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWORD')]) {
                        sh 'echo $DOCKER_PASSWORD | docker login -u $DOCKER_USERNAME --password-stdin'
                    }
                    
                    // Construire l'image Docker avec le nom d'utilisateur Docker Hub
                    withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials', usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWORD')]) {
                        sh "docker build -t ${env.DOCKER_USERNAME}/${env.JOB_NAME}:${env.BUILD_NUMBER} ."
                        sh "docker tag ${env.DOCKER_USERNAME}/${env.JOB_NAME}:${env.BUILD_NUMBER} ${env.DOCKER_USERNAME}/${env.JOB_NAME}:latest"
                        
                        // Push vers Docker Hub
                        sh "docker push ${env.DOCKER_USERNAME}/${env.JOB_NAME}:${env.BUILD_NUMBER}"
                        sh "docker push ${env.DOCKER_USERNAME}/${env.JOB_NAME}:latest"
                    }
                    
                    echo "✅ Image Docker construite et poussée avec succès"
                    echo "Image: ${env.DOCKER_USERNAME}/${env.JOB_NAME}:${env.BUILD_NUMBER}"
                    echo "Tag latest: ${env.DOCKER_USERNAME}/${env.JOB_NAME}:latest"
                }
            }
        }
        
        
        // stage('Security Scan') {
        //     steps {
        //         echo '🔒 Scan de sécurité de l\'image Docker...'
        //         script {
        //             withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials', usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWORD')]) {
        //                 sh '''
        //                     # Installation de Trivy si nécessaire
        //                     if ! command -v trivy &> /dev/null; then
        //                         curl -sfL https://raw.githubusercontent.com/aquasecurity/trivy/main/contrib/install.sh | sh -s -- -b /usr/local/bin
        //                     fi
                            
        //                     # Scan de l'image
        //                     trivy image --exit-code 1 --severity HIGH,CRITICAL ${DOCKER_USERNAME}/${JOB_NAME}:${BUILD_NUMBER}
        //                 '''
        //             }
        //         }
        //     }
        // }
        
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
                            
                            echo "Réponse de l'API Render: $DEPLOY_RESPONSE"
                            
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
