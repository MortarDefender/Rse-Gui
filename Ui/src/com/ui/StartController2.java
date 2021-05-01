package com.ui;

import com.rse.Engine;
import objects.RSE;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class StartController2 {
    @FXML
    private Button signInButton;
    @FXML private Text actiontarget;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    private RSE rse;
    private Scene next_scene;
    private Stage primaryStage;
    private SimpleBooleanProperty isFileSelected;
    private SimpleStringProperty selectedFileProperty;


    public StartController2() {
        this.rse = new Engine();
    }

    @FXML
    public void initialize() {
        System.out.println("Inside runnable init 2");
        selectedFileProperty = new SimpleStringProperty();
        isFileSelected = new SimpleBooleanProperty(false);
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void setNextScene(Scene nxt) {
        this.next_scene = nxt;
    }

    public RSE getRSE() { return this.rse; }

    @FXML
    public void openFileButtonAction() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select xml file");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("xml files", "*.xml"));
        File selectedFile = fileChooser.showOpenDialog(primaryStage);
        if (selectedFile == null)
            return;

        String absolutePath = selectedFile.getAbsolutePath();
        selectedFileProperty.set(absolutePath);
        isFileSelected.set(true);
        // objects.RSE rse = new objects.RSE();
        try {
            rse.loadXml(selectedFile.toString());
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }

        // System.out.println(rse.allStocks());
        // System.out.println(rse.allUsers());


        //
        /*String tradeChoice = AlertBox.askQuestion("Trade", "Action", "Sell", "BUY");
        String actionChoice = AlertBox.askQuestion("Trade", "Command", "LMT", "MKT", "FOK", "IOC");
        int amount = Integer.parseInt(AlertBox.getAmount("Trade", "Amount"));
        int rate = 0;

        if (!actionChoice.equals("MKT"))
            rate = Integer.parseInt(AlertBox.getAmount("Trade", "Rate"));

        System.out.println("trade choice is: " + tradeChoice);
        System.out.println("command choice is: " + actionChoice);
        System.out.println("stock amount is: " + amount);
        System.out.println("stock rate is: " + rate);*/

        // AlertBox.display("Trade1", "Trade action: " + tradeChoice + "\nCommand: " + actionChoice + "\nStock Amount: " + amount + ((rate != 0) ? "\nStock rate: " + rate : ""));

        Menu m = new Menu(this.rse);
        m.startMenu();

        /*String[] trade = {"", "", "", "", ""}; // new String[5];
        int index = 0;
        String choice = "";
        do {
            if (index == 0) {
                choice = AlertBox.askQuestion("Trade", "Action", "Sell", "BUY", "Back");
            }
            else if (index == 1) {
                ObservableList<Stock> list = FXCollections.observableArrayList(rse.getStocks());
                choice = AlertBox.showStockTable("Trade", "Stocks: ", list, "companyName", "symbol", "rate", "totalDeals", "revolution", "quantity");
            }
            else if (index == 2) {
                choice = AlertBox.askQuestion("Trade", "Command", "LMT", "MKT", "FOK", "IOC", "Back");
                if (choice.equals("MKT"))
                    trade[4] =  rse.getRate(trade[1]);
            }
            else if (index == 3) {
                choice = AlertBox.getAmount("Trade", "Amount");
            }
            else if (index == 4) {
                choice = AlertBox.getAmount("Trade", "Rate");
            }
            else if (index == 5) {
                choice = AlertBox.display("Trade1", "Trade action: " + trade[0] + "\nStock Symbol: " + trade[1] + "\nCommand: " + trade[2] +
                        "\nStock Amount: " + trade[3] + ((!trade[4].equals("0")) ? "\nStock rate: " + trade[4] : ""), "Back", "Submit");
            }
            if (choice.equals("Back"))
                index--;
            else {
                if (index != 5)
                    trade[index] = choice;
                if (index == 3)
                    if (trade[2].equals("MKT"))
                        index++;
                index++;
            }
        } while (index >= 0 && index <= 5);
        System.out.println("the trade has been approved");*/

        // AlertBox.displayMultiple("Stocks", rse.allStocks().split("\n\n"));
        // AlertBox.displayMultiple("Users", rse.allUsers().split("\n\n"));

        // AlertBox.display("Stocks", rse.allStocks());
        // AlertBox.display("Users", rse.allUsers());
    }

    private void openNextScene() {
        this.primaryStage.setScene(next_scene);
    }
}