# 🇧🇫 Alerte Coupure Faso

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-blue.svg)](https://kotlinlang.org)
[![Firebase](https://img.shields.io/badge/Firebase-Realtime-orange.svg)](https://firebase.google.com)
[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)

**Alerte Coupure Faso** est une plateforme mobile collaborative de signalement des délestages en temps réel au Burkina Faso. Ce projet utilise le *crowdsourcing* pour offrir une visibilité claire sur l'état du réseau électrique national.

> 🎓 *Projet développé dans le cadre de la Licence 3 Informatique (L3S6) à l'Université Joseph KI-ZERBO (UJKZ).*

---

## Contexte du projet
Dans un contexte marqué par de fortes chaleurs et des délestages fréquents, il est souvent difficile de savoir si une coupure est localisée ou généralisée. Cette application permet aux citoyens de signaler l'état du réseau et à la **Sonabel** de communiquer officiellement de manière ciblée.

---

## Fonctionnalités

### Pour les Citoyens
* **Tableau de bord dynamique :** Visualisation des coupures en cours/résolues avec statistiques.
* **Signalement intelligent (GPS) :** Détection automatique du quartier via l'API Geocoder d'Android.
* **Recherche avancée :** Filtrage instantané par quartier ou ville.
* **Communiqués Officiels :** Consultation et partage (WhatsApp/SMS) des notes de la Sonabel.
* **Mode Hors-ligne :** Mise en cache des données via **Room Database**.

###  Pour l'Administrateur (Mode Sonabel)
* **Rôle automatique :** L'authentification via un email `@sonabel.bf` débloque l'accès Admin.
* **Modération :** Mise à jour du statut des alertes (EN COURS, MAINTENANCE, RÉSOLU).
* **Gestion Utilisateurs :** Modération des comptes signalés.
* **Publication :** Envoi de notifications push via Firebase Cloud Messaging.

---

## Stack Technique & Architecture
Le projet suit les principes de l'**Architecture Clean (proche de MVVM)** pour une maintenance simplifiée.

* **Langage :** Kotlin
* **Interface :** Material Design 3, ViewBinding, Navigation Components.
* **Backend :** * *Firebase Auth :* Gestion des sessions.
    * *Firestore :* Base de données NoSQL temps réel.
    * *Cloud Messaging :* Notifications d'alertes.
* **Local Data :** Room Database (Persistence).

---

## L'Équipe
* **Jean Baptiste Kaboré** : Lead Developer (Architecture Android, Firebase, UI/UX).
* **Alima Kagambega** : Backend & Assurance Qualité (Optimisation, Tests, Documentation).

---

## Installation
1.  **Cloner le dépôt :**
    ```bash
    git clone [https://github.com/jeanbaptistekabore779-dotcom/Alerte_Coupure_Faso.git](https://github.com/jeanbaptistekabore779-dotcom/Alerte_Coupure_Faso.git)
    ```
2.  **Configuration Firebase :**
    * Ajoutez votre fichier `google-services.json` dans le dossier `app/`.
    * Activez *Authentication* et *Firestore* sur la console Firebase.
3.  **Lancement :**
    * Ouvrez sur Android Studio et synchronisez Gradle.

---

## Licence & Droits
Ce projet est réalisé à des fins académiques.
© 2026 - **Jean Baptiste Kaboré & Alima Kagambega**. Tous droits réservés.