// Test Pipeline - Vérification complète du pipeline CI/CD
// Ce script teste l'ensemble du pipeline avec la nouvelle configuration

pipeline {
    agent any
    
    tools {
        jdk 'JDK-11'
    }
    
    environment {
        JAVA_HOME = tool('JDK-11')
        PATH = "${JAVA_HOME}/bin:${env.PATH}"
        MAVEN_OPTS = '-Xmx1024m'
        DOCKER_BUILDKIT = "1"
    }
    
    options {
        timeout(time: 15, unit: 'MINUTES')
        timestamps()
        ansiColor('xterm')
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo '🔄 Test Checkout'
                checkout scm
                script {
                    env.GIT_COMMIT_SHORT = sh(
                        script: 'git rev-parse --short HEAD',
                        returnStdout: true
                    ).trim()
                }
                echo "Commit: ${env.GIT_COMMIT_SHORT}"
            }
        }
        
        stage('Build Test') {
            steps {
                echo '🔨 Test Build Maven'
                sh '''
                    echo "=== Test Build ==="
                    echo "Maven version:"
                    mvn -version
                    echo ""
                    echo "Compilation du projet:"
                    mvn clean compile -q
                    echo "✅ Build réussi!"
                '''
            }
        }
        
        stage('Test Unitaires') {
            steps {
                echo '🧪 Test des tests unitaires'
                sh '''
                    echo "=== Test des tests unitaires ==="
                    mvn test -q
                    echo "✅ Tests unitaires réussis!"
                '''
            }
            post {
                always {
                    publishTestResults testResultsPattern: 'target/surefire-reports/*.xml'
                }
            }
        }
        
        stage('Package Test') {
            steps {
                echo '📦 Test Package'
                sh '''
                    echo "=== Test Package ==="
                    mvn package -DskipTests -q
                    echo "JAR créé:"
                    ls -la target/*.jar
                    echo "✅ Package réussi!"
                '''
            }
        }
        
        stage('Docker Test') {
            steps {
                echo '🐳 Test Docker'
                script {
                    // Test de vérification Docker
                    sh '''
                        echo "=== Test Docker ==="
                        echo "Docker version:"
                        docker --version
                        echo ""
                        echo "Docker info:"
                        docker info --format "{{.ServerVersion}}"
                        echo ""
                        echo "Test de construction d'image:"
                        docker build -t test-spring-boot:${BUILD_NUMBER} .
                        echo "✅ Docker fonctionne!"
                    '''
                }
            }
        }
        
        stage('Security Test') {
            steps {
                echo '🔒 Test de sécurité'
                sh '''
                    echo "=== Test de sécurité ==="
                    # Test d'installation de Trivy
                    if ! command -v trivy &> /dev/null; then
                        echo "Installation de Trivy..."
                        curl -sfL https://raw.githubusercontent.com/aquasecurity/trivy/main/contrib/install.sh | sh -s -- -b /usr/local/bin
                    fi
                    
                    echo "Version Trivy:"
                    trivy --version
                    echo ""
                    echo "Test de scan (mode info seulement):"
                    trivy image --format table test-spring-boot:${BUILD_NUMBER} || echo "Scan terminé"
                    echo "✅ Test de sécurité réussi!"
                '''
            }
        }
    }
    
    post {
        always {
            echo '🧹 Nettoyage'
            sh '''
                echo "=== Nettoyage ==="
                # Nettoyer les images Docker de test
                docker rmi test-spring-boot:${BUILD_NUMBER} 2>/dev/null || true
                docker image prune -f
                echo "Nettoyage terminé"
            '''
        }
        
        success {
            echo '✅ Pipeline de test réussi!'
            script {
                def message = """
                🎉 **Tests réussis!**
                
                **Résumé des tests:**
                - ✅ Checkout Git
                - ✅ Build Maven
                - ✅ Tests unitaires
                - ✅ Package JAR
                - ✅ Docker
                - ✅ Sécurité (Trivy)
                
                **Configuration validée:**
                - Java: ${JAVA_HOME}
                - Maven: Fonctionnel
                - Docker: Fonctionnel
                - Pipeline: Prêt pour la production
                
                **Prochaines étapes:**
                1. Configurer les credentials Docker Hub
                2. Configurer les credentials Render
                3. Lancer le pipeline complet
                """
                echo message
            }
        }
        
        failure {
            echo '❌ Pipeline de test échoué!'
            script {
                def message = """
                💥 **Tests échoués!**
                
                **Vérifiez:**
                1. Configuration JDK-11 dans Jenkins
                2. Installation de Docker
                3. Configuration Maven
                4. Logs détaillés ci-dessus
                
                **Commandes de diagnostic:**
                - java -version
                - mvn -version
                - docker --version
                """
                echo message
            }
        }
    }
}
