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
                script {
                    // Vérifier et installer Maven si nécessaire
                    sh '''
                        # Vérifier si Maven est installé
                        if ! command -v mvn >/dev/null 2>&1; then
                            echo "📦 Installation de Maven..."
                            
                            # Créer le répertoire Maven
                            MAVEN_HOME="/opt/maven"
                            sudo mkdir -p $MAVEN_HOME
                            
                            # Télécharger et installer Maven
                            cd /tmp
                            curl -O https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz
                            sudo tar -xzf apache-maven-3.9.6-bin.tar.gz -C $MAVEN_HOME --strip-components=1
                            
                            # Ajouter Maven au PATH
                            echo 'export PATH=$MAVEN_HOME/bin:$PATH' | sudo tee -a /etc/profile
                            export PATH=$MAVEN_HOME/bin:$PATH
                            
                            # Vérifier l'installation
                            mvn --version
                        else
                            echo "✅ Maven déjà installé"
                            mvn --version
                        fi
                        
                        # Créer un settings.xml temporaire pour utiliser les repositories publics
                        echo "🔧 Configuration des repositories Maven publics..."
                        cat > ~/.m2/settings.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 
          http://maven.apache.org/xsd/settings-1.0.0.xsd">
    <mirrors>
        <mirror>
            <id>central</id>
            <name>Maven Central</name>
            <url>https://repo1.maven.org/maven2</url>
            <mirrorOf>*</mirrorOf>
        </mirror>
    </mirrors>
    <profiles>
        <profile>
            <id>public-repos</id>
            <repositories>
                <repository>
                    <id>central</id>
                    <name>Maven Central</name>
                    <url>https://repo1.maven.org/maven2</url>
                </repository>
                <repository>
                    <id>spring-milestones</id>
                    <name>Spring Milestones</name>
                    <url>https://repo.spring.io/milestone</url>
                </repository>
                <repository>
                    <id>spring-snapshots</id>
                    <name>Spring Snapshots</name>
                    <url>https://repo.spring.io/snapshot</url>
                </repository>
            </repositories>
            <pluginRepositories>
                <pluginRepository>
                    <id>central</id>
                    <name>Maven Central</name>
                    <url>https://repo1.maven.org/maven2</url>
                </pluginRepository>
            </pluginRepositories>
        </profile>
    </profiles>
    <activeProfiles>
        <activeProfile>public-repos</activeProfile>
    </activeProfiles>
</settings>
EOF
                        echo "✅ Settings.xml configuré avec les repositories publics"
                    '''
                    
                    // Construire l'application avec repositories publics
                    sh '''
                        export PATH="/opt/maven/bin:$PATH"
                        mvn clean compile -DskipTests
                    '''
                }
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
                sh '''
                    export PATH="/opt/maven/bin:$PATH"
                    mvn package -DskipTests
                '''
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
        
        
        stage('Security Scan') {
            steps {
                echo '🔒 Scan de sécurité de l\'image Docker...'
                script {
                    withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials', usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWORD')]) {
                        sh '''
                            # Installation de Trivy si nécessaire
                            if ! command -v trivy &> /dev/null; then
                                curl -sfL https://raw.githubusercontent.com/aquasecurity/trivy/main/contrib/install.sh | sh -s -- -b /usr/local/bin
                            fi
                            
                            # Scan de l'image
                            trivy image --exit-code 1 --severity HIGH,CRITICAL ${DOCKER_USERNAME}/${JOB_NAME}:${BUILD_NUMBER}
                        '''
                    }
                }
            }
        }
        
        stage('Deploy to Render') {
            when {
                anyOf {
                    branch 'main'
                }
            }
            steps {
                echo '🚀 Déploiement sur Render...'
                script {
                    withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials', usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWORD')]) {
                        sh '''
                            # Installation de Render CLI si nécessaire
                            if ! command -v render &> /dev/null; then
                                echo "Installation de Render CLI..."
                                curl -fsSL https://cli.render.com/install.sh | sh
                            fi
                            
                            # Configuration du CLI
                            export RENDER_API_KEY="${RENDER_API_KEY}"
                            
                            # Déploiement via CLI
                            render service deploy ${RENDER_SERVICE_ID} --image ${DOCKER_USERNAME}/${JOB_NAME}:${BUILD_NUMBER}
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
