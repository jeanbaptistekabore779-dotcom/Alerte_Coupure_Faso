        🇧🇫 Alerte Coupure Faso
    
Application mobile collaborative de signalement des délestages en temps réel au Burkina Faso.

KotlinFirebaseAndroid

    📖 Contexte du projet
Dans un contexte marqué par des périodes de forte chaleur et des délestages fréquents, il est souvent difficile pour les populations de savoir si une coupure d'électricité est localisée ou généralisée. Alerte Coupure Faso est une application de crowdsourcing qui permet aux citoyens de signaler l'état du réseau électrique et aux autorités de communiquer officiellement.

Ce projet a été développé dans le cadre de la Licence 3 Informatique (L3S6) à l'Université Joseph KI-ZERBO (UJKZ).

    👥 L'Équipe
Ce projet a été réalisé en binôme par :

Jean Baptiste Kaboré : Lead Developer (Architecture Android, Firebase, Interface).
Alima Kagambega : Backend & Intégration Qualité (Optimisation, Tests, Documentation).
    ✨ Fonctionnalités
    📱 Pour les Citoyens (Utilisateurs)
Tableau de bord dynamique : Visualisation des coupures en cours et résolues avec statistiques en temps réel.
Signalement intelligent (GPS) : Signalement rapide via une BottomSheet avec détection automatique du quartier grâce à l'API de Géolocalisation Android.
Recherche avancée : Filtrage instantané des alertes par quartier.
Communiqués Officiels : Consultation des annonces de la Sonabel avec partage direct vers WhatsApp (formatage Markdown intégré).
Profil personnalisé : Gestion des informations et historique local (Room).
    🛡️ Pour l'Administrateur (Sonabel)
Rôle automatique : L'authentification avec un email @sonabel.bf bascule automatiquement l'utilisateur en mode Administrateur.
Modération des alertes : Changement de statut (EN COURS / RÉSOLU / MAINTENANCE) ou suppression en un clic.
Gestion des utilisateurs : Possibilité de bloquer ou supprimer des comptes citoyens.
Publication officielle : Envoi de communiqués officiels priorités aux utilisateurs.
🛠️ Stack Technique & Architecture
Le projet suit une architecture modulaire stricte (proche de MVVM) pour séparer la logique métier de l'interface :

Langage : Kotlin 100%
UI & Frontend : XML (Material Design 3), ViewBinding, Fragments, RecyclerView, SwipeRefreshLayout.
Backend & BDD :
Firebase Authentication (Login/Register)
Cloud Firestore (Base de données NoSQL temps réel)
Firebase Storage (Stockage photos de profil)
Firebase Messaging (Notifications push)
Local (Hors-ligne) : Room Database (Synchronisation locale pour l'historique).
Outils : Glide (Images), Geocoder (Localisation GPS), Coroutine/Flow.
Structure des dossiers :

com.jbk.alerte.coupure.faso/├── ui/            # Activités, Fragments et Adapters├── models/        # Data classes (User, Alerte, Communique)├── data/          # Room (DAO, Database) & Repository└── services/      # Notifications Firebase
    🚀 Installation et Test
Prérequis
Android Studio (Dernière version stable)
Un compte Firebase
Étapes d'installation
Cloner le dépôt
bash

git clone https://github.com/jeanbaptistekabore779-dotcom/Alerte_Coupure_Faso.git
Configuration Firebase
Créer un projet sur la Console Firebase.
Activer Authentication (Email/Mot de passe).
Créer une base de données Firestore.
Télécharger le fichier google-services.json et le placer dans le dossier app/.
Lancement
Ouvrir le projet dans Android Studio.
Attendre le Sync Project with Gradle Files.
Lancer l'application sur un émulateur (API 24+) ou un appareil physique.
    📜 Licence
Ce projet est réalisé à des fins académiques dans le cadre de l'UJKZ.
© 2026 - Jean Baptiste Kaboré & Alima Kagambega.