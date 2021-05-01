package com.ui;

import objects.RSE;
import com.rse.Engine;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.InputMismatchException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Welcome");
        /*FXMLLoader fxmlLoader2 = new FXMLLoader();
        URL mainUrl = getClass().getResource("menu.fxml");  // start || ui
        fxmlLoader2.setLocation(mainUrl);
        Parent root2 = fxmlLoader2.load(mainUrl.openStream());
        Scene scene2 = new Scene(root2, 300, 300);

        StartController crt = new StartController();
        crt.setPrimaryStage(primaryStage);
        crt.setNextScene(scene2);
        objects.RSE rse = crt.getRSE();

        FXMLLoader fxmlLoader = new FXMLLoader();
        URL startUrl = getClass().getResource("start.fxml");  // start || ui
        fxmlLoader.setLocation(startUrl);
        Parent root = fxmlLoader.load(startUrl.openStream());
        crt = fxmlLoader.getController();
        Scene scene = new Scene(root, 300, 300);*/

        primaryStage.getIcons().add(new Image("com/ui/css/icon.jpg"));
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(500);

        RSE rse = new Engine();
        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);
        Label title = new Label("Welcome");
        Label exp_title = new Label("exp");
        Button loadXml = new Button("Load An Xml File");
        loadXml.setStyle("-fx-font-size: 17px;");
        layout.getChildren().addAll(title, loadXml);
        Scene scene = new Scene(layout,800, 500);
        loadXml.setOnAction(e -> {
            try {
                loadXmlFile(rse, primaryStage);
                exp_title.setText("");
                // Login l = new Login(rse, primaryStage, scene);
                // l.startLogin();
                Menu m = new Menu(rse, primaryStage, scene);
                m.startMenu();
            }
            catch (NullPointerException | IllegalStateException ignore) {}
            catch (Exception exp) {
                exp_title.setText("Error in file: " + exp.getMessage());
                exp_title.setStyle(" -fx-text-fill: white;-fx-font-size: 20px;");
                if (!layout.getChildren().contains(exp_title))
                    layout.getChildren().add(exp_title);
                System.out.println(exp.getMessage());
            }
        });

        scene.getStylesheets().add(getClass().getResource("css/ui.css").toExternalForm());

        primaryStage.setOnCloseRequest(e -> primaryStage.close());

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private static String xmlExplorer(Stage primaryStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select xml file");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("xml files", "*.xml"));
        File selectedFile = fileChooser.showOpenDialog(primaryStage);
        if (selectedFile == null)
            throw new NullPointerException("null");
        return selectedFile.toString();
    }

    public static void loadXmlFile(RSE rse, Stage primaryStage) throws Exception {
        String selected = xmlExplorer(primaryStage);
        rse.loadXml(selected);
    }

    public static void loadXmlFile(RSE rse, Stage primaryStage, String username) throws Exception {
        String selected = xmlExplorer(primaryStage);
        rse.loadXml(selected, username);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
