package com.design.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import objects.RSE;
import com.design.Context;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StartController {

    @FXML private Label exp_title;
    @FXML private Button loadXmlButton;

    private Stage window;

    public Stage getWindow() { return window; }

    public void setStage(Stage stage) { this.window = stage; }

    @FXML
    private void loadXml() {
        // window = (Stage) vbox.getScene().getWindow();
        Platform.runLater(() -> {  // new Thread
            try {
                loadXmlFile(Context.rse, window);
                exp_title.setVisible(false);
                // Login l = new Login(rse, primaryStage, scene);
                // l.startLogin();
                // Menu m = new Menu(Context.rse, primaryStage, scene);
                // m.startMenu();
                System.out.println("move to main menu");
            }
            catch (NullPointerException | IllegalStateException ignore) { }
            catch (Exception exp) {
                System.out.println(exp.getMessage());
                exp_title.setVisible(true);
                exp_title.setText("Error in file: " + exp.getMessage());
                exp_title.setStyle("-fx-text-fill: red; -fx-font-size: 20px;");
                System.out.println(exp.getMessage());
            }
        });
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
}
