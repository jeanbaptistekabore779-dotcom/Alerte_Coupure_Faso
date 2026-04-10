🇧🇫 Alerte Coupure Faso
Alerte Coupure Faso est une application mobile Android développée avec Kotlin et Firebase. Elle permet aux citoyens burkinabè de signaler et de suivre en temps réel les interruptions de fourniture d'électricité et d'eau.

## L'Équipe
Ce projet a été réalisé en binôme par :

Jean Baptiste Kaboré : Lead Developer (Android & Firebase Architecture).

Alima Kagambega: Backend & Intégration Qualité.

## Fonctionnalités
📱 Pour les Citoyens
Tableau de bord temps réel : Visualisation des coupures en cours à Ouagadougou et environ.

Signalement rapide : Formulaire pour déclarer une coupure (Ville, Quartier, Type de panne).

Statistiques : Analyse des probabilités de rétablissement.

Profil personnalisé : Gestion des informations personnelles et historique.

## Pour l'Administrateur
Gestion des utilisateurs : Possibilité de bloquer ou supprimer des comptes.

Modération : Validation, mise à jour du statut (En cours / Résolu) ou suppression des alertes.

Suivi global : Statistiques sur le nombre total de signalements et d'utilisateurs.

## Stack Technique
## Langage : Kotlin

Interface : XML (Material Design), ViewBinding, Fragments.

Backend : Firebase Auth (Authentification), Cloud Firestore (Base de données NoSQL).

Images : Glide (Chargement fluide des photos de profil).

Navigation : Navigation Drawer (Menu latéral) & TabLayout.

## Architecture du Projet
Le projet suit une architecture modulaire pour séparer la logique de l'interface :

ui/ : Activités et Fragments (Accueil, Admin, Login).

models/ : Classes de données (User, Alerte).

adapters/ : Gestionnaires pour les listes dynamiques (RecyclerView).

## Installation et Test
Cloner le projet :

## Bash
git clone https://github.com/votre-username/alerte-coupure-faso.git
Configuration Firebase :

Créer un projet sur la console Firebase.

Ajouter le fichier google-services.json dans le dossier app/.

Activer Authentication (Email/Password) et Firestore.

Lancement :

Ouvrir dans Android Studio.

Faire un Sync Project with Gradle Files.

Lancer sur un émulateur ou un appareil physique.

## Contexte Académique
Projet réalisé dans le cadre de la Licence 3 Informatique (L3S6) à l'Université Joseph KI-ZERBO (UJKZ).