// Test Jenkins Final - Configuration complète
// Ce script teste la configuration complète de Jenkins

pipeline {
    agent any
    
    tools {
        jdk 'JDK-11'  // Utilise la configuration JDK-11
    }
    
    environment {
        JAVA_HOME = tool('JDK-11')
        PATH = "${JAVA_HOME}/bin:/opt/homebrew/bin:/usr/local/bin:${env.PATH}"
    }
    
    stages {
        stage('Test 1: Vérification de l\'environnement') {
            steps {
                echo '🔍 Test 1: Vérification de l\'environnement'
                sh '''
                    echo "=== Informations système ==="
                    echo "OS: $(uname -s)"
                    echo "Architecture: $(uname -m)"
                    echo "JAVA_HOME: $JAVA_HOME"
                    echo "PATH: $PATH"
                    echo "WORKSPACE: $WORKSPACE"
                    echo "BUILD_NUMBER: $BUILD_NUMBER"
                '''
            }
        }
        
        stage('Test 2: Vérification Java 11') {
            steps {
                echo '☕ Test 2: Vérification Java 11'
                sh '''
                    echo "=== Test Java 11 ==="
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
        
        stage('Test 3: Test de compilation Java 11') {
            steps {
                echo '🔨 Test 3: Test de compilation Java 11'
                sh '''
                    echo "=== Test de compilation Java 11 ==="
                    # Créer un fichier Java simple
                    cat > TestJava11.java << 'EOF'
public class TestJava11 {
    public static void main(String[] args) {
        System.out.println("Hello from Java 11!");
        System.out.println("Java Version: " + System.getProperty("java.version"));
        System.out.println("Java Home: " + System.getProperty("java.home"));
        System.out.println("OS: " + System.getProperty("os.name"));
        System.out.println("Architecture: " + System.getProperty("os.arch"));
        System.out.println("Java Vendor: " + System.getProperty("java.vendor"));
    }
}
EOF
                    
                    echo "Compilation du fichier TestJava11.java:"
                    javac TestJava11.java
                    
                    if [ -f TestJava11.class ]; then
                        echo "✅ Compilation réussie!"
                        echo "Exécution du programme:"
                        java TestJava11
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
                    echo "Version Maven:"
                    mvn -version
                    echo ""
                    echo "Test de Maven (sans projet):"
                    echo "ℹ️ Maven est installé et accessible!"
                    echo "✅ Maven fonctionne correctement!"
                '''
            }
        }
        
        stage('Test 5: Test Maven avec projet') {
            steps {
                echo '📦 Test 5: Test Maven avec projet'
                sh '''
                    echo "=== Test Maven avec projet ==="
                    # Aller dans le répertoire du projet principal
                    if [ -f "pom.xml" ]; then
                        echo "Projet Maven trouvé, test de compilation:"
                        mvn clean compile -q
                        echo "✅ Maven fonctionne correctement avec le projet!"
                    else
                        echo "ℹ️ Pas de projet Maven dans ce répertoire"
                        echo "✅ Maven est installé et accessible!"
                    fi
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
        
        stage('Test 7: Test Docker avec projet') {
            when {
                expression { 
                    sh(script: 'command -v docker >/dev/null 2>&1', returnStatus: true) == 0 
                }
            }
            steps {
                echo '🐳 Test 7: Test Docker avec projet'
                sh '''
                    echo "=== Test Docker avec projet ==="
                    if [ -f "Dockerfile" ]; then
                        echo "Dockerfile trouvé, test de construction:"
                        docker build -t test-java11:latest .
                        echo "✅ Docker fonctionne correctement avec le projet!"
                    else
                        echo "ℹ️ Pas de Dockerfile dans ce répertoire"
                        echo "✅ Docker est installé et accessible!"
                    fi
                '''
            }
        }
    }
    
    post {
        always {
            echo '🧹 Nettoyage des fichiers de test'
            sh '''
                rm -f TestJava11.java TestJava11.class
                # Nettoyer les images Docker de test si Docker est disponible
                if command -v docker >/dev/null 2>&1; then
                    docker rmi test-java11:latest 2>/dev/null || true
                fi
            '''
        }
        
        success {
            echo '✅ Tous les tests sont passés avec succès!'
            echo '🎉 Votre configuration Jenkins est parfaite!'
            echo '🚀 Vous pouvez maintenant utiliser le pipeline principal'
        }
        
        failure {
            echo '❌ Certains tests ont échoué!'
            echo '🔧 Vérifiez la configuration et les logs.'
        }
    }
}
