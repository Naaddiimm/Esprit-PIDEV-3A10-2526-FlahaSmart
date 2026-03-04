<p align="center">
  <img src="https://img.shields.io/badge/Java-17+-orange?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java"/>
  <img src="https://img.shields.io/badge/JavaFX-21-blue?style=for-the-badge&logo=java&logoColor=white" alt="JavaFX"/>
  <img src="https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white" alt="MySQL"/>
  <img src="https://img.shields.io/badge/Maven-3.6+-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white" alt="Maven"/>
  <img src="https://img.shields.io/badge/License-Academic-green?style=for-the-badge" alt="License"/>
</p>

<h1 align="center">🌾 FlahaSmart</h1>

<p align="center">
  <strong>Plateforme intelligente de gestion agricole avec IA et APIs intégrées</strong>
</p>

<p align="center">
  Application JavaFX moderne combinant gestion d'opérations agricoles, système d'irrigation intelligent, 
  détection de maladies des plantes, rotation optimisée des cultures et chatbot IA propulsé par Google Gemini.
</p>

---

## 📋 Table des matières

- [Présentation](#-présentation)
- [Fonctionnalités](#-fonctionnalités)
- [Architecture](#-architecture)
- [Technologies](#-technologies)
- [Installation](#-installation)
- [Configuration](#-configuration)
- [Utilisation](#-utilisation)
- [APIs Intégrées](#-apis-intégrées)
- [Structure du projet](#-structure-du-projet)
- [Auteur](#-auteur)

---

## 🎯 Présentation

**AgriManager Pro** est une solution complète de gestion agricole intelligente qui combine :

| Module | Description |
|--------|-------------|
| 🚜 **Gestion CRUD** | Équipements et opérations agricoles |
| 🌡️ **Météo temps réel** | API WeatherStack avec conseils dynamiques |
| 💧 **Irrigation intelligente** | Calcul automatique des besoins en eau |
| 🔄 **Rotation des cultures** | Planification optimisée pluriannuelle |
| 🔬 **Détection maladies** | Analyse d'images via API PlantNet |
| 🤖 **AgriBot IA** | Chatbot propulsé par Google Gemini 2.5 Flash |

### ✨ Points forts

```
✅ Interface moderne avec design glassmorphism et dégradés verts
✅ 6 APIs externes intégrées (WeatherStack, Gemini, PlantNet...)
✅ Système d'irrigation basé sur 7 critères météo et agronomiques
✅ Conseils agricoles dynamiques générés en temps réel
✅ Architecture MVC professionnelle avec pattern DAO
✅ Multithreading pour opérations asynchrones fluides
```

---

## 🌟 Fonctionnalités

### 1. 🚜 Gestion des Équipements

| Fonction | Description |
|----------|-------------|
| Ajouter | Créer un équipement (nom, type) |
| Modifier | Mettre à jour les informations |
| Supprimer | Suppression avec confirmation |
| Lister | Affichage en tableau moderne |
| Rechercher | Recherche par nom (Stream API) |
| Trier | Tri alphabétique dynamique |

**Statuts** : `Libre` | `Réservé`

---

### 2. 🔧 Gestion des Opérations

| Fonction | Description |
|----------|-------------|
| Créer | Nouvelle opération avec équipement associé |
| Modifier | Mise à jour complète |
| Terminer | Changement de statut en un clic |
| Supprimer | Avec confirmation |
| Filtrer | Par statut (en cours/terminé) |

**Relation** : Utilisation du modèle `Equipement` (jointure Java correcte)

---

### 3. 📊 Dashboard Intelligent

#### Cards Statistiques Opérations
- **Total** des opérations avec compteur animé
- **En cours** avec pourcentage et navigation directe
- **Terminées** avec indicateur de progression

#### Cards Statistiques Équipements
- Répartition par type avec barres de progression
- Compteurs : Total | Libres | Réservés

#### Navigation intelligente
Clic sur "Opérations en cours" → Redirection avec highlight automatique

---

### 4. 🌤️ Météo & Conseils Agricoles Dynamiques

#### Widget Météo (API WeatherStack)
```
🌡️ Température actuelle et ressentie
💧 Humidité (%)
💨 Vitesse du vent (km/h)
☁️ Description météo
🕐 Heure locale
```

#### Conseils Agricoles Intelligents

Analyse en temps réel des données météo pour générer des conseils professionnels :

| Critère | Analyse |
|---------|---------|
| **Température** | Alertes canicule/gel, conditions optimales |
| **Humidité** | Risques maladies fongiques, arrosage |
| **Vent** | Conditions de pulvérisation, protection |
| **Conditions** | Pluie, nuages, ensoleillement |
| **Ressenti** | Stress thermique, refroidissement éolien |

**Code couleur des conseils** :
- 🔴 **Critique** : Actions immédiates requises
- 🟡 **Attention** : Précautions nécessaires
- 🟢 **Optimal** : Conditions idéales
- 🔵 **Information** : Conseils généraux

---

### 5. 💧 Système Intelligent d'Irrigation

Module avancé calculant automatiquement les besoins en eau selon :

| Critère | Poids |
|---------|-------|
| 🌡️ Température actuelle | Évapotranspiration |
| 💧 Humidité du sol | Rétention hydrique |
| 🌧️ Précipitations prévues | Report irrigation |
| 🌱 Type de culture | Besoins spécifiques |
| 📅 Dernière irrigation | Fréquence optimale |
| 💦 Quantité précédente | Ajustement progressif |
| 🌍 Type de sol | Capacité de rétention |

#### Fonctionnalités
- Sélection du type de culture (Tomates, Blé, Maïs, Vignes...)
- Configuration des paramètres d'irrigation
- Calcul automatique de la priorité (Critique/Haute/Moyenne/Faible)
- Recommandation de durée et horaire optimal

---

### 6. 🔄 Système de Rotation des Cultures

Planification intelligente des rotations pluriannuelles :

| Paramètre | Configuration |
|-----------|---------------|
| Parcelle | Nom, surface (ha) |
| Type de sol | Argileux, Limoneux, Sableux... |
| Historique | Dernières cultures |
| Nutriments | Azote (N), Phosphore (P), Potassium (K) |
| pH du sol | Échelle 4.0 - 9.0 |
| Jachère | Années depuis repos |

#### Résultats
- **Plan de rotation** sur 1 à 10 ans
- **Visualisation** chronologique avec flèches
- **Résumé parcelle** avec indicateurs fertilité

---

### 7. 🔬 Détection de Maladies des Plantes

Intégration de l'API PlantNet pour l'analyse d'images :

| Fonction | Description |
|----------|-------------|
| Upload | Téléchargement d'image de plante |
| Analyse | Identification via IA |
| Résultats | Maladies détectées avec probabilités |
| Conseils | Recommandations de traitement |

---

### 8. 🤖 AgriBot - Chatbot IA (Gemini 2.5 Flash)

Chatbot intelligent propulsé par **Google Gemini** pour répondre aux questions agricoles :

#### Capacités
- Conseils de culture personnalisés
- Diagnostic de problèmes
- Recommandations saisonnières
- Bonnes pratiques agricoles
- Réponses en temps réel

#### Interface
- Design moderne de chat
- Historique des conversations
- Indicateur de chargement asynchrone

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                      PRÉSENTATION                           │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────────────┐   │
│  │    FXML     │ │    CSS      │ │    Controllers      │   │
│  │   (Views)   │ │  (Styles)   │ │   (JavaFX FXML)     │   │
│  └─────────────┘ └─────────────┘ └─────────────────────┘   │
├─────────────────────────────────────────────────────────────┤
│                      MÉTIER                                 │
│  ┌─────────────────────────────────────────────────────┐   │
│  │                    Services                          │   │
│  │  • EquipementService    • IrrigationService         │   │
│  │  • OperationService     • RotationCultureService    │   │
│  │  • WeatherService       • PlantDiseaseService       │   │
│  │  • AgriChatbotService                               │   │
│  └─────────────────────────────────────────────────────┘   │
├─────────────────────────────────────────────────────────────┤
│                      DONNÉES                                │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────────────┐   │
│  │   Models    │ │  MyDataBase │ │    APIs Externes    │   │
│  │  (POJOs)    │ │  (Singleton)│ │  (HTTP Clients)     │   │
│  └─────────────┘ └─────────────┘ └─────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

### Design Patterns utilisés

| Pattern | Utilisation |
|---------|-------------|
| **MVC** | Séparation Views/Controllers/Models |
| **DAO** | Accès données avec PreparedStatement |
| **Singleton** | Connexion base de données |
| **Service Layer** | Logique métier isolée |
| **Observer** | Binding JavaFX properties |

---

## 💻 Technologies

### Core
| Technologie | Version | Usage |
|-------------|---------|-------|
| Java | 17+ | Langage principal |
| JavaFX | 21 | Interface graphique |
| Maven | 3.6+ | Gestion dépendances |
| MySQL | 8.0+ | Base de données |

### APIs & Libraries
| API/Library | Usage |
|-------------|-------|
| WeatherStack | Météo temps réel |
| Google Gemini 2.5 Flash | Chatbot IA |
| PlantNet | Détection maladies plantes |
| Gson | Parsing JSON |
| JDBC MySQL Connector | Connexion BD |

### Concepts Java avancés
- **Stream API** : Recherche et tri dynamiques
- **Lambda expressions** : Code fonctionnel
- **Multithreading** : Task<T> pour appels asynchrones
- **Regex** : Validation et pattern matching
- **Generics** : Collections typées

---

## 📦 Installation

### Prérequis

```bash
# Vérifier Java
java -version   # Doit être 17+

# Vérifier Maven
mvn -version    # Doit être 3.6+

# MySQL Server doit être installé et actif
```

### Installation

```bash
# 1. Cloner le repository
git clone https://github.com/votre-username/AgriManager-Pro.git
cd AgriManager-Pro

# 2. Installer les dépendances
mvn clean install

# 3. Compiler
mvn compile

# 4. Lancer l'application
mvn javafx:run
```

---

## ⚙️ Configuration

### Base de données

1. Créer la base de données :
```sql
CREATE DATABASE agrimanager;
USE agrimanager;
```

2. Éditer `src/main/java/utilies/MyDataBase.java` :
```java
private static final String URL = "jdbc:mysql://localhost:3306/agrimanager";
private static final String USER = "votre_user";
private static final String PASSWORD = "votre_password";
```

### APIs (fichiers de configuration)

| API | Variable | Où configurer |
|-----|----------|---------------|
| WeatherStack | `API_KEY` | `WeatherService.java` |
| Google Gemini | `API_KEY` | `AgriChatbotService.java` |
| PlantNet | `API_KEY` | `PlantDiseaseService.java` |

---

## 🎮 Utilisation

### Navigation principale

```
📊 Dashboard           → Tableau de bord avec statistiques
├── 🚜 Équipements     
│   ├── Liste          → Afficher tous les équipements
│   └── Ajouter        → Créer un équipement
├── 🔧 Opérations      
│   ├── Liste          → Afficher toutes les opérations
│   └── Ajouter        → Créer une opération
├── 💧 Irrigation      → Système intelligent d'irrigation
├── 🔄 Rotation        → Planification rotation cultures
├── 🔬 Analyse Maladie → Détection maladies des plantes
├── 🌤️ Météo          → Widget météo avec conseils
└── 🤖 AgriBot        → Chatbot IA agricole
```

### Workflow typique

1. **Configurer les équipements** → Ajouter tracteurs, outils, systèmes d'irrigation
2. **Planifier les opérations** → Associer équipements aux tâches
3. **Consulter le dashboard** → Vue d'ensemble des activités
4. **Vérifier la météo** → Conseils agricoles adaptés
5. **Planifier l'irrigation** → Calcul automatique des besoins
6. **Optimiser les rotations** → Plan pluriannuel des cultures
7. **Consulter AgriBot** → Questions et conseils IA

---

## 🔌 APIs Intégrées

### WeatherStack API
```
Endpoint: http://api.weatherstack.com/current
Données: Température, humidité, vent, conditions
Limite: 100 requêtes/mois (gratuit)
```

### Google Gemini API
```
Modèle: Gemini 2.5 Flash
Usage: Chatbot agricole intelligent
Endpoint: https://generativelanguage.googleapis.com/v1beta/entities/gemini-2.5-flash
```

### PlantNet API
```
Usage: Identification de plantes et maladies
Endpoint: https://my-api.plantnet.org/v2/identify
```

---

## 📁 Structure du projet

```
AgriManager-Pro/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── controllers/
│   │   │   │   ├── DashboardController.java
│   │   │   │   ├── MainLayoutController.java
│   │   │   │   ├── WeatherWidgetController.java
│   │   │   │   ├── ChatbotController.java
│   │   │   │   ├── IrrigationController.java
│   │   │   │   ├── RotationCultureController.java
│   │   │   │   ├── PlantDiseaseController.java
│   │   │   │   ├── equipement/
│   │   │   │   └── operation/
│   │   │   ├── entities/
│   │   │   │   ├── Equipement.java
│   │   │   │   ├── Operation.java
│   │   │   │   ├── Weather.java
│   │   │   │   ├── ChatMessage.java
│   │   │   │   ├── IrrigationPlan.java
│   │   │   │   ├── Parcelle.java
│   │   │   │   └── RecommandationCulture.java
│   │   │   ├── services/
│   │   │   │   ├── EquipementService.java
│   │   │   │   ├── OperationService.java
│   │   │   │   ├── WeatherService.java
│   │   │   │   ├── AgriChatbotService.java
│   │   │   │   ├── IrrigationService.java
│   │   │   │   ├── RotationCultureService.java
│   │   │   │   └── PlantDiseaseService.java
│   │   │   ├── utilies/
│   │   │   │   └── MyDataBase.java
│   │   │   └── testes/
│   │   │       └── MainApp.java
│   │   └── resources/
│   │       ├── views/
│   │       └── styles/
│   └── test/
├── pom.xml
└── README.md
```

---

## 🔒 Sécurité & Bonnes pratiques

| Pratique | Implémentation |
|----------|----------------|
| SQL Injection | PreparedStatement partout |
| Validation | Contrôles de saisie côté client |
| Async | Appels API sur threads séparés |
| Error Handling | Try-catch avec logs |
| Resources | Fermeture automatique (try-with-resources) |

---

## 📈 Améliorations futures

- [ ] 📱 Version mobile (JavaFX Mobile / Flutter)
- [ ] ☁️ Synchronisation cloud
- [ ] 📊 Export PDF des rapports
- [ ] 📅 Calendrier visuel des opérations
- [ ] 🔔 Notifications push
- [ ] 👥 Multi-utilisateurs avec authentification
- [ ] 📈 Graphiques statistiques avancés (Charts)
- [ ] 🌐 Internationalisation (i18n)

---

## 👨‍💻 Auteur

**[Votre Nom]**

| Contact | Lien |
|---------|------|
| 📧 Email | votre.email@example.com |
| 🔗 GitHub | [@votre-username](https://github.com/votre-username) |
| 💼 LinkedIn | [Votre Profil](https://linkedin.com/in/votre-profil) |

---

## 📄 Licence

Ce projet est réalisé dans un cadre académique.

```
MIT License - Libre d'utilisation avec attribution
```

---
---

<p align="center">
  <strong>Développé avec ❤️ et ☕</strong><br>
  <em>Mars 2026</em>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Version-2.0-success?style=flat-square" alt="Version"/>
  <img src="https://img.shields.io/badge/Status-Stable-brightgreen?style=flat-square" alt="Status"/>
  <img src="https://img.shields.io/badge/Build-Passing-success?style=flat-square" alt="Build"/>
</p>
