package com.design.controller;

import com.design.Context;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;


public class AddStock {

    @FXML private TextField symbol;
    @FXML private TextField amount;
    @FXML private TextField totalRate;
    @FXML private TextField companyName;
    @FXML private Label exp;

    private String username;

    public void setUsername(String username) { this.username = username; }

    @FXML
    private void addNewStock() {
        try {
            String amountString = amount.getText();
            int amount = Integer.parseInt(amountString);
            int rate = Integer.parseInt(totalRate.getText()) / amount;
            Context.rse.addUserStock(companyName.getText(), symbol.getText(), rate, amount, username);
            exp.setVisible(false);
        } catch (NumberFormatException ignore) {
            exp.setVisible(true);
            exp.setText("There is no number with a digit in it. please enter a number");
        } catch (Exception ex) {
            exp.setVisible(true);
            exp.setText(ex.getMessage());
        }
    }
}
