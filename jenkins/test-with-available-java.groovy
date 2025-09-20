// Test Jenkins avec Java disponible
// Ce script utilise la version Java disponible par défaut

pipeline {
    agent any
    
    environment {
        // Utiliser le Java disponible par défaut
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
        
        stage('Test 2: Vérification Java disponible') {
            steps {
                echo '☕ Test 2: Vérification Java disponible'
                sh '''
                    echo "=== Test Java ==="
                    echo "Commande java trouvée:"
                    which java
                    echo ""
                    echo "Version Java:"
                    java -version
                    echo ""
                    echo "JAVA_HOME: $JAVA_HOME"
                    echo ""
                    echo "Recherche des versions Java disponibles:"
                    echo "OpenJDK 11:"
                    ls -la /opt/homebrew/opt/openjdk@11/libexec/openjdk.jdk/Contents/Home/ 2>/dev/null || echo "OpenJDK 11 non trouvé"
                    echo ""
                    echo "OpenJDK 21:"
                    ls -la /opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home/ 2>/dev/null || echo "OpenJDK 21 non trouvé"
                '''
            }
        }
        
        stage('Test 3: Test de compilation Java') {
            steps {
                echo '🔨 Test 3: Test de compilation Java'
                sh '''
                    echo "=== Test de compilation ==="
                    # Créer un fichier Java simple
                    cat > TestAvailableJava.java << 'EOF'
public class TestAvailableJava {
    public static void main(String[] args) {
        System.out.println("Hello from Jenkins Java Test!");
        System.out.println("Java Version: " + System.getProperty("java.version"));
        System.out.println("Java Home: " + System.getProperty("java.home"));
        System.out.println("OS: " + System.getProperty("os.name"));
        System.out.println("Architecture: " + System.getProperty("os.arch"));
        System.out.println("Java Vendor: " + System.getProperty("java.vendor"));
    }
}
EOF
                    
                    echo "Compilation du fichier TestAvailableJava.java:"
                    javac TestAvailableJava.java
                    
                    if [ -f TestAvailableJava.class ]; then
                        echo "✅ Compilation réussie!"
                        echo "Exécution du programme:"
                        java TestAvailableJava
                    else
                        echo "❌ Échec de la compilation!"
                        exit 1
                    fi
                '''
            }
        }
        
        stage('Test 4: Vérification Maven') {
            steps {
                echo '📦 Test 4: Vérification Maven'
                sh '''
                    echo "=== Test Maven ==="
                    echo "Recherche de Maven:"
                    which mvn || echo "Maven non trouvé dans PATH"
                    echo ""
                    echo "Recherche dans /opt/homebrew/bin:"
                    ls -la /opt/homebrew/bin/mvn* 2>/dev/null || echo "Maven non trouvé dans /opt/homebrew/bin"
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
        
        stage('Test 5: Test Maven (si disponible)') {
            when {
                expression { 
                    sh(script: 'command -v mvn >/dev/null 2>&1', returnStatus: true) == 0 
                }
            }
            steps {
                echo '📦 Test 5: Test Maven'
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
        
        stage('Test 6: Vérification Docker') {
            steps {
                echo '🐳 Test 6: Vérification Docker'
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
    }
    
    post {
        always {
            echo '🧹 Nettoyage des fichiers de test'
            sh '''
                rm -f TestAvailableJava.java TestAvailableJava.class
                # Nettoyer les images Docker de test si Docker est disponible
                if command -v docker >/dev/null 2>&1; then
                    docker rmi test-available-java:latest 2>/dev/null || true
                fi
            '''
        }
        
        success {
            echo '✅ Tous les tests sont passés avec succès!'
            echo '🎉 Votre environnement Jenkins est prêt!'
            echo '💡 Vous pouvez maintenant utiliser le pipeline principal avec JDK-11 configuré'
        }
        
        failure {
            echo '❌ Certains tests ont échoué!'
            echo '🔧 Vérifiez la configuration et les logs.'
        }
    }
}
