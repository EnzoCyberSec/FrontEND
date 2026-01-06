package org.example.demo;

// Classe de base JavaFX : toute application JavaFX DOIT étendre Application
import javafx.application.Application;

// FXMLLoader sert à charger un fichier FXML
import javafx.fxml.FXMLLoader;

// Scene = contenu graphique affiché dans la fenêtre
import javafx.scene.Scene;

// Stage = la fenêtre (équivalent de la "borne")
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {

    // ---------------------------------------------------
    // Méthode appelée AUTOMATIQUEMENT par JavaFX au lancement
    // ---------------------------------------------------
    @Override
    public void start(Stage stage) throws IOException {

        // 1) Création du chargeur FXML
        // Ici tu dis : "charge le fichier hello-view.fxml"
        //
        // getResource("hello-view.fxml") signifie :
        // -> cherche dans /resources/org/example/demo/hello-view.fxml
        FXMLLoader fxmlLoader =
                new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));

        // 2) Chargement du FXML
        // fxmlLoader.load() :
        // - lit le fichier hello-view.fxml
        // - crée tous les objets graphiques (BorderPane, Button, ImageView, etc.)
        // - instancie HelloController
        // - relie les fx:id et les onAction
        //
        // new Scene(...) encapsule tout ça dans une scène JavaFX
        Scene scene = new Scene(fxmlLoader.load());

        // 3) On met cette scène dans la fenêtre (Stage)
        // => À partir de maintenant, hello-view.fxml est affiché
        stage.setScene(scene);

        // 4) Mode plein écran (logique pour une borne)
        stage.setFullScreen(true);

        // 5) Chargement du CSS GLOBAL
        // Ce CSS s'applique à TOUT ce qui est dans cette Scene
        // (hello-view.fxml ET tout ce qui restera dans cette scène)
        //
        // Attention :
        // - si plus tard tu changes de Scene (menu.fxml),
        //   ce CSS devra être rechargé dans la nouvelle Scene
        scene.getStylesheets().add(
                getClass().getResource("style.css").toExternalForm()
        );

        // 6) Titre de la fenêtre (peu visible en plein écran, mais propre)
        stage.setTitle("Wok & Roll");

        // 7) Affichage réel de la fenêtre
        // Sans ce show(), rien ne s'affiche
        stage.show();
    }
}
