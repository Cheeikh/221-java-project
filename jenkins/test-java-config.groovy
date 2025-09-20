// Test Jenkins - Configuration Java
// Ce script teste la configuration JDK dans Jenkins

pipeline {
    agent any
    
    tools {
        jdk 'JDK-11'  // Utilise la configuration JDK que vous avez créée
    }
    
    environment {
        JAVA_HOME = tool('JDK-11')
        PATH = "${JAVA_HOME}/bin:/opt/homebrew/bin:/usr/local/bin:${env.PATH}"
    }
    
    stages {
        stage('Test 1: Vérification des variables') {
            steps {
                echo '🔍 Test 1: Vérification des variables d\'environnement'
                sh '''
                    echo "=== Variables d'environnement ==="
                    echo "JAVA_HOME: $JAVA_HOME"
                    echo "PATH: $PATH"
                    echo "WORKSPACE: $WORKSPACE"
                    echo "BUILD_NUMBER: $BUILD_NUMBER"
                '''
            }
        }
        
        stage('Test 2: Vérification Java') {
            steps {
                echo '☕ Test 2: Vérification de l\'installation Java'
                sh '''
                    echo "=== Test Java ==="
                    echo "Commande java trouvée:"
                    which java
                    echo ""
                    echo "Version Java:"
                    java -version
                    echo ""
                    echo "Version Java Compiler:"
                    javac -version
                    echo ""
                    echo "JAVA_HOME contenu:"
                    ls -la $JAVA_HOME/
                '''
            }
        }
        
        stage('Test 3: Test de compilation') {
            steps {
                echo '🔨 Test 3: Test de compilation Java'
                sh '''
                    echo "=== Test de compilation ==="
                    # Créer un fichier Java simple
                    cat > TestJava.java << 'EOF'
public class TestJava {
    public static void main(String[] args) {
        System.out.println("Hello from Java " + System.getProperty("java.version"));
        System.out.println("Java Home: " + System.getProperty("java.home"));
    }
}
EOF
                    
                    echo "Compilation du fichier TestJava.java:"
                    javac TestJava.java
                    
                    if [ -f TestJava.class ]; then
                        echo "✅ Compilation réussie!"
                        echo "Exécution du programme:"
                        java TestJava
                    else
                        echo "❌ Échec de la compilation!"
                        exit 1
                    fi
                '''
            }
        }
        
        stage('Test 4: Test Maven') {
            steps {
                echo '📦 Test 4: Test de Maven avec Java'
                sh '''
                    echo "=== Test Maven ==="
                    echo "PATH actuel: $PATH"
                    echo ""
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
                        else
                            echo "❌ Homebrew non trouvé, installation manuelle requise"
                            exit 1
                        fi
                    fi
                    echo ""
                    echo "Version Maven:"
                    mvn -version
                    echo ""
                    echo "Test de compilation Maven:"
                    # Aller dans le répertoire du projet pour le test Maven
                    if [ -f "pom.xml" ]; then
                        mvn clean compile -q
                        echo "✅ Maven fonctionne correctement avec le projet!"
                    else
                        echo "ℹ️ Pas de projet Maven dans ce répertoire, test de version uniquement"
                        echo "✅ Maven est installé et accessible!"
                    fi
                '''
            }
        }
        
        stage('Test 5: Test Docker') {
            steps {
                echo '🐳 Test 5: Test de Docker'
                sh '''
                    echo "=== Test Docker ==="
                    echo "Version Docker:"
                    docker --version
                    echo ""
                    echo "Info Docker:"
                    docker info --format "{{.ServerVersion}}"
                    echo ""
                    echo "Test de construction d'image:"
                    docker build -t test-java:latest .
                    echo "✅ Docker fonctionne correctement!"
                '''
            }
        }
    }
    
    post {
        always {
            echo '🧹 Nettoyage des fichiers de test'
            sh '''
                rm -f TestJava.java TestJava.class
                docker rmi test-java:latest 2>/dev/null || true
            '''
        }
        
        success {
            echo '✅ Tous les tests sont passés avec succès!'
            echo '🎉 Votre configuration Jenkins est prête!'
        }
        
        failure {
            echo '❌ Certains tests ont échoué!'
            echo '🔧 Vérifiez la configuration et les logs.'
        }
    }
}
