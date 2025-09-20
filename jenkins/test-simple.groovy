// Test Jenkins Simple - Sans configuration préalable
// Ce script teste l'environnement Jenkins sans dépendances

pipeline {
    agent any
    
    environment {
        // Utiliser les outils disponibles par défaut
        PATH = "/opt/homebrew/bin:/usr/local/bin:/usr/bin:/bin:${env.PATH}"
    }
    
    stages {
        stage('Test 1: Vérification de l\'environnement') {
            steps {
                echo '🔍 Test 1: Vérification de l\'environnement'
                sh '''
                    echo "=== Informations système ==="
                    echo "OS: $(uname -s)"
                    echo "Architecture: $(uname -m)"
                    echo "PATH: $PATH"
                    echo "WORKSPACE: $WORKSPACE"
                    echo "BUILD_NUMBER: $BUILD_NUMBER"
                '''
            }
        }
        
        stage('Test 2: Vérification Java') {
            steps {
                echo '☕ Test 2: Vérification Java'
                sh '''
                    echo "=== Test Java ==="
                    echo "Commande java trouvée:"
                    which java || echo "Java non trouvé"
                    echo ""
                    echo "Version Java:"
                    java -version || echo "Java non accessible"
                    echo ""
                    echo "JAVA_HOME: $JAVA_HOME"
                '''
            }
        }
        
        stage('Test 3: Vérification Maven') {
            steps {
                echo '📦 Test 3: Vérification Maven'
                sh '''
                    echo "=== Test Maven ==="
                    echo "Recherche de Maven:"
                    which mvn || echo "Maven non trouvé dans PATH"
                    echo ""
                    echo "Recherche dans /opt/homebrew/bin:"
                    ls -la /opt/homebrew/bin/mvn* 2>/dev/null || echo "Maven non trouvé dans /opt/homebrew/bin"
                    echo ""
                    echo "Recherche dans /usr/local/bin:"
                    ls -la /usr/local/bin/mvn* 2>/dev/null || echo "Maven non trouvé dans /usr/local/bin"
                    echo ""
                    echo "Test d'installation Maven si nécessaire:"
                    if ! command -v mvn >/dev/null 2>&1; then
                        echo "Installation de Maven via Homebrew..."
                        if command -v brew >/dev/null 2>&1; then
                            brew install maven
                            export PATH="/opt/homebrew/bin:$PATH"
                            echo "Maven installé, nouvelle version:"
                            mvn -version
                        else
                            echo "❌ Homebrew non trouvé, installation manuelle requise"
                        fi
                    else
                        echo "Maven trouvé, version:"
                        mvn -version
                    fi
                '''
            }
        }
        
        stage('Test 4: Vérification Docker') {
            steps {
                echo '🐳 Test 4: Vérification Docker'
                sh '''
                    echo "=== Test Docker ==="
                    echo "Recherche de Docker:"
                    which docker || echo "Docker non trouvé"
                    echo ""
                    echo "Version Docker:"
                    docker --version || echo "Docker non accessible"
                    echo ""
                    echo "Info Docker:"
                    docker info --format "{{.ServerVersion}}" 2>/dev/null || echo "Docker daemon non accessible"
                '''
            }
        }
        
        stage('Test 5: Test de compilation Java') {
            steps {
                echo '🔨 Test 5: Test de compilation Java'
                sh '''
                    echo "=== Test de compilation ==="
                    # Créer un fichier Java simple
                    cat > TestSimple.java << 'EOF'
public class TestSimple {
    public static void main(String[] args) {
        System.out.println("Hello from Jenkins Java Test!");
        System.out.println("Java Version: " + System.getProperty("java.version"));
        System.out.println("Java Home: " + System.getProperty("java.home"));
        System.out.println("OS: " + System.getProperty("os.name"));
        System.out.println("Architecture: " + System.getProperty("os.arch"));
    }
}
EOF
                    
                    echo "Compilation du fichier TestSimple.java:"
                    javac TestSimple.java
                    
                    if [ -f TestSimple.class ]; then
                        echo "✅ Compilation réussie!"
                        echo "Exécution du programme:"
                        java TestSimple
                    else
                        echo "❌ Échec de la compilation!"
                        exit 1
                    fi
                '''
            }
        }
        
        stage('Test 6: Test Maven (si disponible)') {
            when {
                expression { 
                    sh(script: 'command -v mvn >/dev/null 2>&1', returnStatus: true) == 0 
                }
            }
            steps {
                echo '📦 Test 6: Test Maven'
                sh '''
                    echo "=== Test Maven ==="
                    echo "Version Maven:"
                    mvn -version
                    echo ""
                    echo "Test de compilation Maven:"
                    mvn clean compile -q
                    echo "✅ Maven fonctionne correctement!"
                '''
            }
        }
    }
    
    post {
        always {
            echo '🧹 Nettoyage des fichiers de test'
            sh '''
                rm -f TestSimple.java TestSimple.class
                # Nettoyer les images Docker de test si Docker est disponible
                if command -v docker >/dev/null 2>&1; then
                    docker rmi test-simple:latest 2>/dev/null || true
                fi
            '''
        }
        
        success {
            echo '✅ Tous les tests sont passés avec succès!'
            echo '🎉 Votre environnement Jenkins est prêt!'
        }
        
        failure {
            echo '❌ Certains tests ont échoué!'
            echo '🔧 Vérifiez la configuration et les logs.'
        }
    }
}
