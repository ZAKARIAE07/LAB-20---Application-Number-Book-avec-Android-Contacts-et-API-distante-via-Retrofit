# Number Book - Application Android de Gestion de Contacts

## Description
Number Book est une application Android permettant de lire les contacts locaux du téléphone, de les afficher dans une interface mobile, et de les synchroniser avec une base de données MySQL distante via une API PHP RESTful utilisant Retrofit.

## Fonctionnalités
- **Lecture des contacts système** : Accès au `ContentResolver` d'Android pour récupérer les noms et numéros.
- **Gestion des permissions** : Demande dynamique de la permission `READ_CONTACTS`.
- **Synchronisation distante** : Envoi des contacts vers un serveur backend.
- **Recherche distante** : Recherche de contacts stockés en base par nom ou numéro.
- **Interface fluide** : Utilisation d'un `RecyclerView` pour l'affichage des données.

---

## Structure du Projet Backend (PHP/MySQL)

Le serveur doit être organisé comme suit dans votre dossier `htdocs` ou `www` :

```text
numberbook-api/
├── config/
│   └── Database.php      # Connexion PDO à MySQL
├── model/
│   └── Contact.php       # Modèle objet Contact
├── service/
│   └── ContactService.php # Logique métier (Insert, GetAll, Search)
└── api/
    ├── insertContact.php
    ├── getAllContacts.php
    └── searchContact.php
```

### Installation de la Base de Données

1. Créez la base de données :
   ```sql
   CREATE DATABASE IF NOT EXISTS numberbook CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   USE numberbook;
   ```

2. Créez la table `contact` :
   ```sql
   CREATE TABLE contact (
       id INT AUTO_INCREMENT PRIMARY KEY,
       name VARCHAR(150) NOT NULL,
       phone VARCHAR(50) NOT NULL,
       source VARCHAR(50) DEFAULT 'mobile',
       created_at DATETIME DEFAULT CURRENT_TIMESTAMP
   );
   ```

---

## Configuration Android

### Prérequis
- **Retrofit 2** : Pour les appels API.
- **Gson** : Pour la conversion JSON.
- **RecyclerView** : Pour la liste.

### Permissions
L'application requiert les permissions suivantes dans `AndroidManifest.xml` :
```xml
<uses-permission android:name="android.permission.READ_CONTACTS" />
<uses-permission android:name="android.permission.INTERNET" />
```

### Connexion au Serveur Local
Dans `RetrofitClient.java`, l'URL de base est configurée sur `http://10.0.2.2/`. 
- **10.0.2.2** est l'adresse utilisée par l'émulateur Android pour pointer vers le `localhost` de l'ordinateur hôte.
- Si vous utilisez un appareil physique, remplacez-la par l'adresse IP de votre machine (ex: `192.168.1.x`).

---

## Utilisation
1. Lancez votre serveur local (XAMPP/WAMP).
2. Ouvrez l'application Number Book.
3. Cliquez sur **"Charger les contacts"** pour importer les données du téléphone.
4. Cliquez sur **"Synchroniser vers le serveur"** pour sauvegarder les contacts en ligne.
5. Utilisez le champ de recherche et le bouton **"Rechercher"** pour interroger la base distante.

<img width="181" height="388" alt="1" src="https://github.com/user-attachments/assets/15b9d0c9-929b-4cb0-9c01-0065c184a1f6" />

