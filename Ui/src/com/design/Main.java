package com.design;

import java.net.URL;

import com.design.controller.StartController;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import javafx.application.Application;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Welcome");
        FXMLLoader fxmlLoader = new FXMLLoader();
        URL mainUrl = getClass().getResource("fxml/startMenu.fxml");
        // URL mainUrl = getClass().getResource("design/fxml/startMenu.fxml");
        fxmlLoader.setLocation(mainUrl);
        Parent root = fxmlLoader.load(mainUrl.openStream());
        Scene scene = new Scene(root, 1000, 600);

        StartController start = fxmlLoader.getController();
        start.setStage(primaryStage);

        primaryStage.getIcons().add(new Image("com/ui/css/icon.jpg"));
        // primaryStage.setMinWidth(800);
        // primaryStage.setMinHeight(500);

        // scene.getStylesheets().add(getClass().getResource("css/ui.css").toExternalForm());
        // primaryStage.setOnCloseRequest(e -> primaryStage.close());

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
