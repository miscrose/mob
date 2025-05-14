# Gestion Pharmacie

Application de gestion de pharmacie avec Spring Boot (Backend) et React Native (Frontend).

## Prérequis

- Git installé
- Node.js et npm installés
- Java JDK 17 ou plus
- MySQL installé
- Expo Go (SDK 52) installé sur votre appareil mobile
  > Note: SDK 52 est recommandé car les notifications ne fonctionnent pas correctement avec SDK 53

## Installation avec Docker (Recommandé)

1. Cloner le repository :
```bash
git clone https://github.com/miscrose/mob
```

2. Démarrer l'application :
```bash
docker-compose up --build
```

3. Accéder à l'application :
- Frontend (Web) : http://localhost:8081
- Backend API : http://localhost:8080

## Installation pour le Développement Mobile

1. Cloner le repository :
```bash
git clone https://github.com/miscrose/mob
```

2. Configuration du Backend :
   - Ouvrir `gestion-pharmacie/src/main/resources/application.properties`
   - Configurer la connexion MySQL selon votre environnement :
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/pharmacy_db?createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true&useSSL=false
   spring.datasource.username=votre_username
   spring.datasource.password=votre_password
   ```

3. Configuration du Frontend :
   - Ouvrir `PharmacyApp/constants/config.ts`
   - Remplacer l'URL de l'API par votre IP locale :
   ```typescript
   export const API_URL = 'http://VOTRE_IP_LOCALE:8080';
   ```
   Pour trouver votre IP :
   - Windows : `ipconfig` dans le terminal
   - Linux/Mac : `ifconfig` ou `ip addr` dans le terminal


4. Démarrer MySQL :



5. Démarrer le Backend :
```bash
cd gestion-pharmacie
./mvnw spring-boot:run
```

6. Démarrer le Frontend :
```bash
cd PharmacyApp
npm install
npx expo start
```

7. Accéder à l'application :
- Frontend : 
  - Web : http://localhost:8081
  - Mobile : Scannez le QR code avec l'application Expo Go
- Backend : http://localhost:8080

## Structure du projet

- `PharmacyApp/` : Frontend React Native
- `gestion-pharmacie/` : Backend Spring Boot

## Commandes Docker Utiles

### Démarrer les conteneurs
```bash
docker-compose up
```

### Arrêter les conteneurs
```bash
docker-compose down
```

### Voir les logs
```bash
docker-compose logs -f
```

### Reconstruire les images
```bash
docker-compose build
```

