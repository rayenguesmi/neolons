#!/bin/bash

# Script de démarrage pour le système de notification Neolons
# Auteur: Système de notification automatique
# Version: 1.0

echo "=========================================="
echo "  Système de Notification Neolons v1.0"
echo "=========================================="
echo ""

# Couleurs pour les messages
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Fonction pour afficher les messages colorés
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Vérifier si le script est exécuté depuis le bon répertoire
if [ ! -f "pom.xml" ]; then
    print_error "Ce script doit être exécuté depuis le répertoire backend/"
    print_info "Utilisation: cd backend && ./start.sh"
    exit 1
fi

print_info "Vérification des prérequis..."

# Vérifier Java
if ! command -v java &> /dev/null; then
    print_error "Java n'est pas installé ou n'est pas dans le PATH"
    print_info "Veuillez installer Java 17 ou supérieur"
    print_info "Ubuntu/Debian: sudo apt install openjdk-17-jdk"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    print_error "Java 17 ou supérieur est requis. Version détectée: $JAVA_VERSION"
    exit 1
fi
print_success "Java $JAVA_VERSION détecté"

# Vérifier Maven
if ! command -v mvn &> /dev/null; then
    if [ ! -f "./mvnw" ]; then
        print_error "Maven n'est pas installé et mvnw n'est pas disponible"
        print_info "Veuillez installer Maven: sudo apt install maven"
        exit 1
    else
        print_info "Utilisation du wrapper Maven (mvnw)"
        MVN_CMD="./mvnw"
    fi
else
    print_success "Maven détecté"
    MVN_CMD="mvn"
fi

# Vérifier MongoDB
if ! pgrep mongod > /dev/null; then
    print_warning "MongoDB ne semble pas être démarré"
    print_info "Tentative de démarrage de MongoDB..."
    
    if command -v systemctl &> /dev/null; then
        sudo systemctl start mongod
        if [ $? -eq 0 ]; then
            print_success "MongoDB démarré avec succès"
        else
            print_warning "Impossible de démarrer MongoDB automatiquement"
            print_info "Veuillez démarrer MongoDB manuellement: sudo systemctl start mongod"
        fi
    else
        print_warning "systemctl non disponible, veuillez démarrer MongoDB manuellement"
    fi
else
    print_success "MongoDB est en cours d'exécution"
fi

# Charger les variables d'environnement
if [ -f ".env" ]; then
    print_info "Chargement des variables d'environnement depuis .env"
    export $(cat .env | grep -v '^#' | grep -v '^$' | xargs)
    print_success "Variables d'environnement chargées"
else
    print_warning "Fichier .env non trouvé"
    print_info "Création d'un fichier .env d'exemple..."
    
    cat > .env << EOF
# Configuration Email (OBLIGATOIRE - À MODIFIER)
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# Configuration Optionnelle
SUPPORT_EMAIL=support@neolons.com
NOTIFICATION_ENABLED=true
REMINDER_DAYS=7,3,1
COMPANY_NAME=Neolons
EOF
    
    print_warning "Fichier .env créé avec des valeurs d'exemple"
    print_error "IMPORTANT: Modifiez le fichier .env avec vos vraies informations email avant de continuer"
    print_info "Éditez le fichier: nano .env"
    
    read -p "Appuyez sur Entrée pour continuer une fois le fichier .env configuré..."
    
    if [ -f ".env" ]; then
        export $(cat .env | grep -v '^#' | grep -v '^$' | xargs)
    fi
fi

# Vérifier la configuration email
if [ "$MAIL_USERNAME" = "your-email@gmail.com" ] || [ -z "$MAIL_USERNAME" ]; then
    print_error "Configuration email non valide"
    print_info "Veuillez modifier le fichier .env avec vos vraies informations email"
    exit 1
fi

print_success "Configuration email: $MAIL_USERNAME"

# Vérifier si le JAR existe
JAR_FILE="target/App-0.0.1-SNAPSHOT.jar"
if [ ! -f "$JAR_FILE" ]; then
    print_info "JAR non trouvé, compilation du projet..."
    
    print_info "Nettoyage et compilation..."
    $MVN_CMD clean compile
    
    if [ $? -ne 0 ]; then
        print_error "Échec de la compilation"
        exit 1
    fi
    
    print_info "Création du package JAR..."
    $MVN_CMD package -DskipTests
    
    if [ $? -ne 0 ]; then
        print_error "Échec de la création du package"
        exit 1
    fi
    
    print_success "Compilation terminée avec succès"
else
    print_success "JAR trouvé: $JAR_FILE"
fi

# Vérifier que le port 8090 est libre
if netstat -tlnp 2>/dev/null | grep -q ":8090 "; then
    print_warning "Le port 8090 est déjà utilisé"
    print_info "Arrêt de l'application existante ou changement de port requis"
    
    read -p "Voulez-vous continuer quand même? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

print_info "Démarrage de l'application Neolons..."
print_info "L'application sera accessible sur: http://localhost:8090"
print_info "API de notification: http://localhost:8090/api/license-notifications/"
print_info ""
print_info "Pour arrêter l'application, utilisez Ctrl+C"
print_info ""

# Démarrer l'application
java -jar "$JAR_FILE"

# Si on arrive ici, l'application s'est arrêtée
print_info "Application arrêtée"

