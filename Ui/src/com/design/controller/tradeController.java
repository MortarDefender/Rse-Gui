package com.design.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import objects.StockDTO;

public class tradeController {

    @FXML ChoiceBox<String> actionChoice;
    @FXML TableView<StockDTO> stockTable;
    @FXML ChoiceBox<String> commandChoice;
    @FXML TextField amountField;
    @FXML TextField rateField;
    @FXML Label rateLabel;

    @FXML
    private void createDeal() {

    }

}
