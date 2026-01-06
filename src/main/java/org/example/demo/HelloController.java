package org.example.demo;

// ActionEvent n'est pas utilisé ici (tu n'en as pas besoin car goToMenu() n'a pas de paramètre)
// import javafx.event.ActionEvent;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloController {

    // -------------------------
    // 1) Champs liés au FXML
    // -------------------------
    // Ces variables seront "injectées" automatiquement par JavaFX
    // si dans hello-view.fxml tu as des éléments avec fx:id="tabText" / fx:id="welcomeText"
    @FXML
    public Label tabText;

    @FXML
    private Label welcomeText;

    // IMPORTANT : root est le BorderPane principal de hello-view.fxml
    // Il DOIT exister dans le FXML comme ceci :
    // <BorderPane fx:id="root" ... >
    //
    // Sinon root sera null et root.getScene() plantera / ne fonctionnera pas.
    @FXML
    private BorderPane root;


    // -------------------------
    // 2) Méthodes appelées depuis le FXML (boutons)
    // -------------------------
    // Dans hello-view.fxml tu as typiquement :
    // <Button ... onAction="#onConnexion" />
    // => JavaFX appelle cette méthode quand on clique
    @FXML
    public void onConnexion() {
        System.out.println("Connexion");
    }

    @FXML
    public void onFR() {
        System.out.println("FR");
    }

    @FXML
    public void onEN() {
        System.out.println("EN");
    }

    @FXML
    public void onES() {
        System.out.println("ES");
    }


    // -------------------------
    // 3) Navigation : Accueil -> Menu
    // -------------------------
    // Cette méthode est appelée par le bouton :
    // <Button ... onAction="#goToMenu" />
    //
    // Objectif : remplacer l'écran actuel (hello-view.fxml) par menu.fxml
    @FXML
    private void goToMenu() throws IOException {

        // 1) Préparer le chargeur FXML
        // getClass().getResource("menu.fxml") cherche menu.fxml
        // dans le dossier resources : /org/example/demo/menu.fxml
        //
        // Si le fichier n'est pas au bon endroit / mauvais nom -> NullPointer ou LoadException
        FXMLLoader loader = new FXMLLoader(getClass().getResource("menu.fxml"));

        // 2) Charger le FXML (crée l'arbre graphique du menu)
        // loader.load() retourne un Parent (racine UI)
        // new Scene(...) crée une nouvelle scène entièrement
        //
        // Conséquence : on "repart" sur une nouvelle Scene, donc les styles
        // attachés à l'ancienne Scene ne sont pas automatiquement copiés.
        Scene scene = new Scene(loader.load());

        // 3) Recharger le CSS global sur cette nouvelle scène
        // Même si tu avais mis style.css dans HelloApplication,
        // ça s'appliquait à l'ancienne Scene (accueil).
        // Ici on crée une nouvelle Scene => il faut ré-ajouter la feuille de style.
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        scene.getStylesheets().add(getClass().getResource("menu.css").toExternalForm());

        // 4) Récupérer la fenêtre (Stage) actuelle
        // root -> c'est un Node déjà affiché à l'écran
        // root.getScene() -> la Scene actuelle (accueil)
        // getWindow() -> la Window (c'est un Stage en JavaFX)
        Stage stage = (Stage) root.getScene().getWindow();

        // 5) Remplacer la Scene de la fenêtre
        // A partir de maintenant, ce n'est plus hello-view.fxml qui est affiché,
        // mais menu.fxml (avec son controller MenuController).
        stage.setScene(scene);

        // 6) Rester en plein écran (utile si tu es en mode borne)
        stage.setFullScreen(true);
    }
}
