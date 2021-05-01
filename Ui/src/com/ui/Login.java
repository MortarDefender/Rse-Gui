package com.ui;

import objects.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.*;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

// com.oracle.httpclient  || https://www.baeldung.com/java-http-request  || https://www.twilio.com/blog/5-ways-to-make-http-requests-in-java
public class Login {
    private final Stage window;
    private final Scene prev;
    private final RSE rse;
    private String choice;
    private final Menu m;

    public Login(RSE r, Stage stage, Scene prev) {  // Stage 3 only
        this.rse = r;
        this.prev = prev;
        this.window = stage;
        this.m = new Menu(rse, window, prev);
    }

    public void startLogin() {
        window.setTitle("Login");
        window.setMinWidth(1000);
        window.setMinHeight(600);

        GridPane layout = new GridPane();
        layout.setAlignment(Pos.CENTER);
        layout.setHgap(40);
        layout.setVgap(30);

        Scene scene = new Scene(layout);

        Label title = new Label("LOGIN: ");
        title.setStyle("-fx-font-size: 20px;-fx-font-weight: bold;-fx-text-fill: white;");
        Label usernameTitle = new Label("Username: ");
        usernameTitle.setStyle("-fx-text-fill: white;");
        TextField usernameField = new TextField();
        Label typeTitle = new Label("User Type: ");
        typeTitle.setStyle("-fx-text-fill: white;");
        ChoiceBox<String> typeBox = new ChoiceBox<>();
        typeBox.getItems().addAll("Stock Broker", "Admin");
        typeBox.setStyle("-fx-font-size: 15px;-fx-text-fill: black;");
        typeBox.setValue("Stock Broker");
        typeBox.setPrefWidth(150);
        Button submit = new Button("Enter the stock exchange");
        submit.setStyle("-fx-font-size: 15px;-fx-font-weight: bold;");

        submit.setOnAction(e -> {
            String username = usernameField.getText();
            String type = typeBox.getValue();
            if (!rse.checkUser(username))  // if(rse.getUser(username).getType() == type.equals("Stock Broker"))
                rse.addUser(username, type.equals("Stock Broker"));
            if (type.equals("Stock Broker"))
                userMenu(username, scene);
            else
                adminMenu(scene);
        });

        GridPane.setConstraints(title, 1, 1);
        GridPane.setConstraints(usernameTitle, 0, 2);
        GridPane.setConstraints(usernameField, 1, 2);
        GridPane.setConstraints(typeTitle, 0, 3);
        GridPane.setConstraints(typeBox, 1, 3);
        GridPane.setConstraints(submit, 1, 4);
        layout.getChildren().addAll(title, usernameTitle, usernameField, typeTitle, typeBox, submit);

        scene.getStylesheets().add(Login.class.getResource("css/alert3.css").toExternalForm());
        window.setScene(scene);
        window.showAndWait();
    }

    public void userMenu(String username, Scene prev) {
        Map<UserDTO, ObservableList<StockDTO>> all_lists = new HashMap<>();
        Map<UserDTO, ObservableList<TransactionDTO>> all_trans = new HashMap<>();
        ObservableList<StockDTO> admin_list = FXCollections.observableArrayList(rse.getStocks());
        Map<String, TableView<TransactionDTO>> transTables = new HashMap<>();
        Map<UserDTO, Label> totals = new HashMap<>();
        VBox layout = new VBox(10);
        Button back = new Button("Back");
        back.setOnAction(e -> window.setScene(prev));
        Scene scene = new Scene(layout);
        layout = this.m.userPage(rse.getUser(username), scene, all_lists, all_trans, admin_list, transTables, totals);
        layout.getChildren().add(back);
        scene = new Scene(layout);
        scene.getStylesheets().add(Login.class.getResource("css/alert.css").toExternalForm());
        window.setScene(scene);
    }

    public void adminMenu(Scene prev) {
        VBox admin_layout = new VBox(10);
        admin_layout.setAlignment(Pos.CENTER);
        admin_layout.setPadding(new Insets(10, 10, 10, 10));
        Scene scene = new Scene(admin_layout);

        Label admin_title = new Label("Admin Page: ");
        Label admin_stock_title = new Label("Stock List: ");
        admin_stock_title.setStyle("-fx-text-fill: white;-fx-underline: true;-fx-font-size: 15px;-fx-text-alignment: center;");
        admin_title.setStyle("-fx-text-fill: white;-fx-underline: true;-fx-font-weight: bold; -fx-font-size: 20px;-fx-text-alignment: center;");
        admin_title.setAlignment(Pos.TOP_LEFT);

        String[] columns = {"companyName", "symbol", "rate", "totalDeals", "revolution", "quantity"};
        TableView<StockDTO> all_stocks = new TableView<>();
        ObservableList<StockDTO> admin_list = FXCollections.observableArrayList(rse.getStocks());
        if (admin_list.size() == 0)
            all_stocks.setPlaceholder(new Label("No rows to display"));
        else
            all_stocks.setItems(admin_list);

        TableView.TableViewSelectionModel<StockDTO> selectionModel = all_stocks.getSelectionModel();
        selectionModel.setSelectionMode(SelectionMode.SINGLE);
        selectionModel.select(0);
        choice = selectionModel.getSelectedItems().get(0).getSymbol();
        all_stocks.setStyle("-fx-text-alignment: center;-fx-font-size: 15px;");
        for (String column : columns) {
            TableColumn<StockDTO, String> col = new TableColumn<>(column);
            col.prefWidthProperty().bind(all_stocks.widthProperty().multiply(1.0 / columns.length));
            col.setStyle( "-fx-alignment: CENTER;-fx-font-size: 13px;");
            col.setCellValueFactory(new PropertyValueFactory<>(column));
            all_stocks.getColumns().add(col);
        }

        ObservableList<StockDTO> selectedItems = selectionModel.getSelectedItems();
        selectedItems.addListener((ListChangeListener<StockDTO>) change -> {
            if (change != null && change.getList().size() != 0) {
                choice = change.getList().get(0).getSymbol();
                System.out.println("Selection changed: " + change.getList());
            }
        });

        HBox inner = new HBox(100);
        inner.setAlignment(Pos.CENTER);
        Button submit = new Button("Investigate stock");
        submit.setStyle("-fx-font-size: 15px;");
        submit.setOnAction(e -> this.m.adminPage(choice, window, scene) );

        Button back = new Button("Back");
        back.setOnAction(e -> window.setScene(prev));
        back.setPrefSize(70, 30);

        inner.getChildren().addAll(submit, back);

        admin_layout.getChildren().addAll(admin_title, admin_stock_title, all_stocks, inner);
        scene.getStylesheets().add(Login.class.getResource("css/admin.css").toExternalForm());
        window.setScene(scene);
    }
}
