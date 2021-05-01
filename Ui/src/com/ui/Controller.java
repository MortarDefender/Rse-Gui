package com.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

public class Controller {
    @FXML
    private Button signInButton;
    @FXML private Text actiontarget;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    @FXML
    public void initialize() {
        System.out.println("Inside runnable init 2");
    }

    @FXML protected void handleSubmitButtonAction(ActionEvent event) {
        final String username = usernameField.getText();
        final String password = passwordField.getText();
        String result = getLoginResult(username, password);
        actiontarget.setText(result);
    }

    public String getLoginResult(String username, String password) {
        if (username.equals("admin") && password.equals("123456"))
            return "admin";
        return "not admin";
    }
}