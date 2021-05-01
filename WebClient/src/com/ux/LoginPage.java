package com.ux;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import java.lang.reflect.Type;
import java.util.Map;
import okhttp3.*;

public class LoginPage {
    private final Stage window;
    public LoginPage(Stage stage) {  // Stage 3 only
        this.window = stage;
    }

    public void startLogin() {
        window.setTitle("Login");
        window.setMinWidth(1000);
        window.setMinHeight(600);

        GridPane layout = new GridPane();
        layout.setAlignment(Pos.CENTER);
        layout.setHgap(40);
        layout.setVgap(30);

        Scene scene = new Scene(layout, 1000, 600);

        Label title = new Label("LOGIN: ");
        title.setStyle("-fx-font-size: 20px;-fx-font-weight: bold;-fx-text-fill: white;");
        Label usernameTitle = new Label("Username: ");
        usernameTitle.setStyle("-fx-font-size: 20px; -fx-text-fill: white;");
        TextField usernameField = new TextField();
        usernameField.setStyle("-fx-font-size: 17px; -fx-max-width: 150px");
        Label typeTitle = new Label("User Type: ");
        typeTitle.setStyle("-fx-font-size: 17px; -fx-text-fill: white;");
        ChoiceBox<String> typeBox = new ChoiceBox<>();
        typeBox.getItems().addAll("Stock Broker", "Admin");
        typeBox.setStyle("-fx-font-size: 17px;-fx-text-fill: black;");
        typeBox.setValue("Stock Broker");
        typeBox.setPrefWidth(150);
        Button submit = new Button("Login"); // Enter the stock exchange
        submit.setStyle("-fx-font-size: 17px;-fx-font-weight: bold;");

        submit.setOnAction(e -> {
            String username = usernameField.getText();
            String type = typeBox.getValue();

            // send post request to server for log in
            Gson gson = new Gson();
            RequestBody formBody = new FormBody.Builder()
                    .add("username",username)
                    .add("userType", type)
                    .build();
            String res = MainPage.post("/loginApi", formBody);
            if (res != null) {
                Type mapType = new TypeToken<Map<String, String>>(){}.getType();
                Map<String, String> response = gson.fromJson(res, mapType);
                if (response.containsKey("error"))
                    Alert.displayMultiple("Error In Login", response.get("error"));
                else {
                    MainPage.apiKey = response.get("key");
                    MainMenu m = new MainMenu(window, scene, username, !type.equals("Admin"));
                    m.startPage();
                }
            }
        });

        GridPane.setConstraints(title, 1, 1);
        GridPane.setConstraints(usernameTitle, 0, 2);
        GridPane.setConstraints(usernameField, 1, 2);
        GridPane.setConstraints(typeTitle, 0, 3);
        GridPane.setConstraints(typeBox, 1, 3);
        GridPane.setConstraints(submit, 1, 4);
        layout.getChildren().addAll(title, usernameTitle, usernameField, typeTitle, typeBox, submit);

        scene.getStylesheets().add(LoginPage.class.getResource("css/alert3.css").toExternalForm());
        window.setScene(scene);
        window.show();
    }
}
