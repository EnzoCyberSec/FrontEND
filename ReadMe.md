# 🥡 Wok & Roll - Borne de Commande

![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-21.0.6-2D7904?style=for-the-badge&logo=java&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.9+-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)

**Wok & Roll** est une application de borne de commande interactive développée en **JavaFX**. Elle offre une expérience utilisateur fluide et moderne pour la prise de commande en restauration rapide.

---

## ✨ Fonctionnalités

### 🖥️ Interface & Navigation
* **Design "Kiosk" :** Interface claire et tactile avec des boutons larges et des ombres portées pour une meilleure lisibilité.
* **Barre Latérale Intuitive :** Navigation rapide entre les catégories (Menus, Entrées, Plats, Desserts, Snacks, Boissons).
* **Mise en page Adaptative :** Utilisation de conteneurs flexibles (`StackPane`, `TilePane`) pour une présentation soignée des produits.

### 🥢 Gestion des Commandes

* **Panier Temps Réel :** Ajout, modification des quantités et calcul automatique du total.
* **Fiches Produits :** Popup modale pour visualiser les détails d'un article avant l'ajout.

### 💳 Parcours Client
* **Tunnel de Paiement :** Récapitulatif clair du panier ➔ Choix du moyen de paiement.
* **Confirmation :** Génération d'un numéro de commande unique et simulation du temps d'attente.

---

## 🛠️ Stack Technique

* **Langage :** Java 21
* **Framework UI :** JavaFX 21.0.6
* **Gestionnaire de dépendances :** Maven
* **Architecture :** MVC (Model-View-Controller)

---

## 🚀 Installation et Lancement

1.  **Cloner le dépôt :**
    ```bash
    git clone [https://github.com/enzocybersec/frontend.git](https://github.com/enzocybersec/frontend.git)
    cd frontend
    ```

2.  **Compiler le projet :**
    ```bash
    mvn clean install
    ```

3.  **Lancer l'application :**
    ```bash
    mvn javafx:run
    ```

---

## 👥 Auteurs

* **enzocybersec**
* **LJT16**
* **CrevetteBroadside**

---

*Projet en cours de développement.*