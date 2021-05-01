package com.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.util.Callback;
import objects.*;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Menu {
    private final Stage window;
    private Scene prev = null;
    private RSE rse;
    private String loadChoice, saveChoice;
    private boolean status = true;

    public Menu(RSE r, Stage stage, Scene scene) {
        this.rse = r;
        this.window = stage;
        this.prev = scene;
    }

    public Menu(RSE r) {
        this.rse = r;
        this.window = new Stage();
    }

    public void startMenu() {
        Map<UserDTO, ObservableList<StockDTO>> all_lists = new HashMap<>();
        Map<String, TableView<TransactionDTO>> transTables = new HashMap<>();     // Stage 3 only
        Map<UserDTO, ObservableList<TransactionDTO>> all_trans = new HashMap<>(); // Stage 3 only
        Map<UserDTO, Label> totals = new HashMap<>();                             // Stage 3 only
        window.setTitle("Main Menu");
        window.setMinWidth(1000);
        window.setMinHeight(600);

        window.setOnCloseRequest(e -> {
            e.consume();
            if (this.status) {
                this.status = false;
                window.setScene(prev);
            }
            else
                window.close();
        });

        ObservableList<StockDTO> admin_list = FXCollections.observableArrayList(rse.getStocks());

        Tab menu = new Tab("Menu");
        menu.setClosable(false);
        VBox layout = new VBox(50);
        layout.setAlignment(Pos.CENTER);

        Label label = new Label("Menu: ");
        label.setAlignment(Pos.TOP_RIGHT);
        label.setStyle("-fx-text-fill: white;-fx-underline: true; -fx-font-weight: bold;-fx-font-size: 35px;");

        Button xmlLoad = new Button("Load Xml File");
        xmlLoad.setPrefSize(120, 30);
        xmlLoad.setStyle("-fx-font-size: 15px;");
        HBox innerLayout = new HBox(80);
        innerLayout.setAlignment(Pos.CENTER);

        Label exp_title = new Label("exp");
        exp_title.setStyle("-fx-text-fill: white;-fx-font-size: 20px;");

        Button load = new Button("load");
        load.setPrefSize(100, 30);
        load.setStyle("-fx-font-size: 15px;");


        Button save = new Button("Save");
        save.setPrefSize(100, 30);
        save.setStyle("-fx-font-size: 15px;");
        save.setOnAction(e -> {
            saveChoice = AlertBox.getAmount( "Save", "Enter the name to save to: ", false);
            try {
                save(saveChoice);
                AlertBox.displayMultiple("Alert", "the file has been saved successfully at " + saveChoice);
                exp_title.setText("");
            } catch (NullPointerException ignore) { }
            catch (Exception ex) {
                exp_title.setText("Error in file: " + ex.getMessage());
                if (!layout.getChildren().contains(exp_title))
                    layout.getChildren().add(exp_title);
            }
        });

        innerLayout.getChildren().addAll(load, xmlLoad, save);
        layout.getChildren().addAll(label, innerLayout);

        xmlLoad.setOnAction(e -> {
            try {
                Main.loadXmlFile(rse, window);
                exp_title.setText("");
                this.startMenu();
            }  catch (NullPointerException ignore) {}
            catch (Exception er) {
                exp_title.setText("Error in file: " + er.getMessage());
                if (!layout.getChildren().contains(exp_title)) {
                    layout.getChildren().add(exp_title);
                }
            }
        });

        menu.setContent(layout);
        TabPane tabPane = new TabPane(menu);
        ObservableValue<Number> binding = new SimpleDoubleProperty(1000 / (rse.getUsers().size() + 2.5));  // window.getWidth() / (rse.getUsers().size() + 2.5)
        tabPane.tabMinWidthProperty().bind(binding);

        // tabPane.setTabMinWidth(50); //
        Scene scene = new Scene(tabPane, window.getWidth(), window.getHeight());
        List<UserDTO> users = rse.getUsers();

        for (UserDTO user : users) {
            Tab u = new Tab(capital(user.getUsername()));
            u.setClosable(false);
            VBox user_layout = userPage(user, scene, all_lists, all_trans, admin_list, transTables, totals);
            u.setContent(user_layout);
            tabPane.getTabs().add(u);
        }

        Tab admin = new Tab("Admin");
        admin.setClosable(false);

        VBox admin_layout = adminTab(scene, admin_list);
        admin.setContent(admin_layout);

        load.setOnAction(e -> {
            loadChoice = AlertBox.getAmount( "Load", "Enter the name of the file to load: ", false);
            try {
                load(loadChoice);
                exp_title.setText("");
                for (UserDTO ue : all_lists.keySet()) {  // set all lists to the changed value
                    all_lists.get(ue).clear();
                    all_lists.get(ue).addAll(rse.getStocks(ue.getUsername()));
                    // Stage 3 only
                    all_trans.get(ue).clear();
                    all_trans.get(ue).addAll(rse.getTransactions(ue.getUsername()));
                    transTables.get(ue.getUsername()).setItems(all_trans.get(ue));
                    totals.get(ue).setText("Total Stock Value: " + ue.getRevolution());
                }
                admin_list.clear();
                admin_list.addAll(rse.getStocks());
                AlertBox.displayMultiple("Alert", "the file has been loaded successfully from " + loadChoice);
            } catch (NullPointerException ignore) { }
            catch (Exception ex) {
                exp_title.setText("Error in file: " + ex.getMessage());
                if (!layout.getChildren().contains(exp_title))
                    layout.getChildren().add(exp_title);
            }
        });


        tabPane.getTabs().add(admin);
        scene.getStylesheets().add(Menu.class.getResource("css/alert.css").toExternalForm());
        window.setScene(scene);
        window.showAndWait();
    }

    public VBox adminTab(Scene scene, ObservableList<StockDTO> admin_list) {
        String[] choice = {""};
        VBox admin_layout = new VBox(10);
        admin_layout.setAlignment(Pos.CENTER);
        admin_layout.setPadding(new Insets(10, 10, 10, 10));

        Label admin_title = new Label("Admin Page: ");
        admin_title.setStyle("-fx-text-fill: white;-fx-underline: true;-fx-font-weight: bold; -fx-font-size: 20px;-fx-text-alignment: center;");
        admin_title.setAlignment(Pos.TOP_LEFT);

        Label admin_stock_title = new Label("Stock List: ");
        admin_stock_title.setStyle("-fx-text-fill: white;-fx-underline: true;-fx-font-size: 15px;-fx-font-weight: bold;-fx-text-alignment: center;");
        AnchorPane stockLine = new AnchorPane();
        stockLine.getChildren().addAll(admin_stock_title);

        String[] columns = {"companyName", "symbol", "quantity", "rate", "totalDeals", "revolution"}; // get in table view
        TableView<StockDTO> all_stocks = new TableView<>();

        if (admin_list.size() == 0)
            all_stocks.setPlaceholder(new Label("No rows to display"));
        else
            all_stocks.setItems(admin_list);

        all_stocks.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableView.TableViewSelectionModel<StockDTO> selectionModel = all_stocks.getSelectionModel();
        selectionModel.setSelectionMode(SelectionMode.SINGLE);
        selectionModel.select(0);
        choice[0] = selectionModel.getSelectedItems().get(0).getSymbol();
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
                choice[0] = change.getList().get(0).getSymbol();
                System.out.println("Selection changed: " + change.getList());
            }
        });

        Button submit = new Button("Investigate stock");
        submit.setStyle("-fx-font-size: 15px;");
        submit.setOnAction(e -> adminPage(choice[0], window, scene) );

        admin_layout.getChildren().addAll(admin_title, stockLine, all_stocks, submit);
        return admin_layout;
    }

    public VBox userPage(UserDTO user, Scene scene, Map<UserDTO, ObservableList<StockDTO>> all_lists, Map<UserDTO, ObservableList<TransactionDTO>> all_trans, ObservableList<StockDTO> admin_list,
                         Map<String, TableView<TransactionDTO>> transTables, Map<UserDTO, Label> totals) {
        VBox user_layout = new VBox(10);
        user_layout.setAlignment(Pos.CENTER);
        user_layout.setPadding(new Insets(10, 10, 10, 10));

        Label title = new Label(capital(user.getUsername()) + ": ");
        title.setStyle("-fx-text-fill: white;-fx-underline: true; -fx-font-weight: bold;-fx-font-size: 20px;");

        Label stock_title = new Label("Stocks: ");
        stock_title.setStyle("-fx-text-fill: white;-fx-underline: true;-fx-font-weight: bold;  -fx-font-size: 15px;-fx-text-alignment: center;");
        Button addStock = new Button();  // add stock
        ImageView im = new ImageView(new Image("com/ui/css/addBtn2.png"));
        im.setFitHeight(20);
        im.setFitWidth(20);
        addStock.setGraphic(im);
        addStock.setStyle("-fx-background-color: transparent;");
        addStock.setOnAction(e -> {
            try {
                addUserStock(user.getUsername(), rse);
                for (UserDTO ue : all_lists.keySet()) {  // set all lists to the changed value
                    all_lists.get(ue).clear();
                    all_lists.get(ue).addAll(rse.getStocks(ue.getUsername()));
                    // Stage 3 only
                    all_trans.get(ue).clear();
                    all_trans.get(ue).addAll(rse.getTransactions(ue.getUsername()));
                    transTables.get(ue.getUsername()).setItems(all_trans.get(ue));
                    totals.get(ue).setText("Total Stock Value: " + ue.getRevolution());
                }
                admin_list.clear();
                admin_list.addAll(rse.getStocks());
            } catch (NullPointerException ignore) { }
            catch (Exception ex) {
                AlertBox.displayMultiple("Alert", "Error in file: " + ex.getMessage());
            }
        });

        AnchorPane stockLine = new AnchorPane();
        AnchorPane.setRightAnchor(addStock, 0d);
        stockLine.getChildren().addAll(stock_title, addStock);

        ObservableList<StockDTO> list = FXCollections.observableArrayList(rse.getStocks(user.getUsername()));
        all_lists.put(user, list);
        String[] columns = {"symbol", "quantity", "rate"};
        TableView<StockDTO> user_stocks = new TableView<>();

        if (list.size() == 0)
            user_stocks.setPlaceholder(new Label("No rows to display"));
        else
            user_stocks.setItems(list);

        user_stocks.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableView.TableViewSelectionModel<StockDTO> selectionModel = user_stocks.getSelectionModel();
        selectionModel.setSelectionMode(SelectionMode.SINGLE);
        selectionModel.select(0);
        user_stocks.setStyle("-fx-text-alignment: center;-fx-font-size: 15px;");
        for (String column : columns) {
            TableColumn<StockDTO, String> col = new TableColumn<>(column);
            col.prefWidthProperty().bind(user_stocks.widthProperty().multiply(1.0 / columns.length));
            col.setStyle( "-fx-alignment: CENTER;-fx-font-size: 13px;");
            col.setCellValueFactory(new PropertyValueFactory<>(column));
            // col.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getSymbol()));  // -> Using Interface
            user_stocks.getColumns().add(col);
        }

        // getTransactions  // Stage 3 only
        Label transactionLabel = new Label("Transactions: ");
        transactionLabel.setStyle("-fx-text-fill: white; -fx-font-size: 15px;-fx-font-weight: bold; -fx-underline: true;");
        Button addTrans = new Button();  // add trans
        ImageView im2 = new ImageView(new Image("com/ui/css/addBtn2.png"));
        im2.setFitHeight(20);
        im2.setFitWidth(20);
        addTrans.setGraphic(im2);
        addTrans.setStyle("-fx-background-color: transparent;");
        addTrans.setOnAction(e -> {
            try {
                int amount = Integer.parseInt(AlertBox.getAmount( "Account Charge", "Enter the amount to charge your account: ", true));
                System.out.println("self charge: " + amount);
                rse.addAccountCharge(user.getUsername(), amount);
                for (UserDTO ue : all_lists.keySet()) {  // set all lists to the changed value
                    all_trans.get(ue).clear();
                    all_trans.get(ue).addAll(rse.getTransactions(ue.getUsername()));
                    transTables.get(ue.getUsername()).setItems(all_trans.get(ue));
                    totals.get(ue).setText("Total Stock Value: " + ue.getRevolution());
                }
            } catch (NullPointerException | NumberFormatException err) {
                System.out.println(err.getMessage());
            }
            catch (Exception ex) {
                AlertBox.displayMultiple("Alert", "Error in file: " + ex.getMessage());
            }
        });

        AnchorPane transLine = new AnchorPane();
        AnchorPane.setRightAnchor(addTrans, 0d);
        transLine.getChildren().addAll(transactionLabel, addTrans);

        String[] transColumns = {"time", "symbol", "actionType", "sum", "accountBefore", "accountAfter"};
        TableView<TransactionDTO> userTrans = new TableView<>();
        ObservableList<TransactionDTO> transList = FXCollections.observableArrayList(user.getTransactions());
        all_trans.put(user, transList);
        transTables.put(user.getUsername(), userTrans);

        if (transList.size() == 0)
            userTrans.setPlaceholder(new Label("No rows to display"));
        else
            userTrans.setItems(transList);

        userTrans.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        userTrans.setStyle("-fx-text-alignment: center;-fx-font-size: 15px;");
        for (String column : transColumns) {
            TableColumn<TransactionDTO, String> col = new TableColumn<>(column);
            col.prefWidthProperty().bind(userTrans.widthProperty().multiply(1.0 / transColumns.length));
            col.setStyle( "-fx-alignment: CENTER;-fx-font-size: 13px;");
            col.setCellValueFactory(new PropertyValueFactory<>(column));
            userTrans.getColumns().add(col);
        }

        // inner layout
        HBox inner = new HBox(100);
        inner.setAlignment(Pos.CENTER);
        Label total_title = new Label("Total Stock Value: " + user.getRevolution());
        total_title.setStyle("-fx-text-fill: white;-fx-font-weight: bold;-fx-font-size: 15px;");
        totals.put(user, total_title);
        // Label type_title = new Label(String.valueOf(user.getType()));  // stage 3
        Button trade = new Button("Trade");
        trade.setStyle("-fx-font-size: 15px;");
        trade.setOnAction(e -> {
            tradeOneWindow(user.getUsername(),scene, all_lists, admin_list, all_trans);
            for (UserDTO ur: all_trans.keySet()) {  // Stage 3 only
                transTables.get(ur.getUsername()).setItems(all_trans.get(ur));
                totals.get(ur).setText("Total Stock Value: " + ur.getRevolution());
            }
        });
        Button loadBtn = new Button("Load Xml");
        loadBtn.setOnAction(e -> {
            try {
                Main.loadXmlFile(rse, window, user.getUsername());
                list.clear();
                list.addAll(rse.getStocks(user.getUsername()));
                user_stocks.setItems(list);
            } catch (NullPointerException ignore) {
            } catch (Exception ex) {
                AlertBox.displayMultiple("Alert", ex.getMessage());
            }
        });
        inner.getChildren().addAll(total_title, trade, loadBtn);
        user_layout.getChildren().addAll(title, stockLine, user_stocks, transLine, userTrans, inner);
        return user_layout;
    }

    @Deprecated
    public void Trade(String username) {
        String[] trade = {"", "", "", "", ""};
        int index = 0;
        String choice = "";
        UserDTO user = rse.getUser(username);
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setMinWidth(350);
        window.setMinHeight(350);

        do {
            if (index == 0)
                choice = AlertBox.askQuestion(window, "Trade", "Action", "Sell", "Buy", "Back");
            else if (index == 1) {
                ObservableList<StockDTO> list;
                if (trade[0].equals("Sell"))
                    list = FXCollections.observableArrayList(rse.getStocks(username));  // getStocks
                else
                    list = FXCollections.observableArrayList(rse.getStocks());
                choice = AlertBox.showStockTable(window, "Trade", "Stocks: ", list, "companyName", "symbol", "rate", "quantity");
            }
            else if (index == 2) {
                choice = AlertBox.askQuestion(window, "Trade", "Command", "LMT", "MKT", "FOK", "IOC", "Back");
                if (choice.equals("MKT"))
                    trade[4] =  String.valueOf(rse.getRate(trade[1]));
            }
            else if (index == 3)
                choice = AlertBox.getAmount(window, "Trade", "Amount", true);
            else if (index == 4)
                choice = AlertBox.getAmount(window, "Trade", "Rate", true);
            else if (index == 5) {
                choice = AlertBox.display(window, "Trade", "Trade action: " + trade[0] + "\nStock Symbol: " + trade[1] + "\nCommand: " + trade[2] +
                        "\nStock Amount: " + trade[3] + ((!trade[4].equals("0")) ? "\nStock rate: " + trade[4] : ""), "Back", "Submit");
            }
            if (choice == null)
                window.close();
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
        if (choice.equals("Back"))
            return;
        List<DealDTO> out = new ArrayList<>();
        try {
            switch (trade[2]) {
                case "MKT":
                    out = rse.MKT(trade[1], trade[0].equals("Buy"), Integer.parseInt(trade[3]), username);
                    break;
                case "LMT":
                    out = rse.LMT(trade[1], trade[0].equals("Buy"), Integer.parseInt(trade[3]), Integer.parseInt(trade[4]), username);
                    break;
                case "FOK":
                    out = rse.FOK(trade[1], trade[0].equals("Buy"), Integer.parseInt(trade[3]), Integer.parseInt(trade[4]), username);
                    break;
                case "IOC":
                    out = rse.IOC(trade[1], trade[0].equals("Buy"), Integer.parseInt(trade[3]), Integer.parseInt(trade[4]), username);
                    break;
                default:
                    System.out.println("There is no option " + trade[2] + ". error in Trade function");
                    break;
            }
        } catch (Exception e) {
            AlertBox.displayMultiple("Alert", e.getMessage());
            return;  // Trade(username, prev);
        }
        if (!out.isEmpty())
            AlertBox.displayTable("Results", out);
        else
            AlertBox.displayMultiple("Alert", "There was no deal made");
    }

    public void tradeOneWindow(String username, final Scene prevScene, Map<UserDTO, ObservableList<StockDTO>> all_lists, ObservableList<StockDTO> admin_list, Map<UserDTO, ObservableList<TransactionDTO>> all_trans) {
        String[] tradeChoice = {"", "", "", "", ""};

        UserDTO user = rse.getUser(username);
        GridPane layout = new GridPane();
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(10, 10, 10, 10));
        layout.setVgap(10);
        layout.setHgap(10);

        Label title = new Label("Trade: ");
        Label action = new Label("Action: ");
        Label stocks = new Label("Stocks: ");
        Label command = new Label("Command: ");
        Label amount = new Label("Amount: ");
        Label rate = new Label("Rate: ");
        rate.setVisible(true);

        title.setStyle("-fx-text-fill: white; -fx-font-weight: bold;-fx-underline: true;-fx-font-size: 23px;");
        action.setStyle("-fx-text-fill: white;-fx-font-size: 17px;-fx-underline: true;");
        stocks.setStyle("-fx-text-fill: white;-fx-font-size: 17px;-fx-underline: true;");
        command.setStyle("-fx-text-fill: white;-fx-font-size: 17px;-fx-underline: true;");
        amount.setStyle("-fx-text-fill: white;-fx-font-size: 17px;-fx-underline: true;");
        rate.setStyle("-fx-text-fill: white;-fx-font-size: 17px;-fx-underline: true;");

        GridPane.setConstraints(title, 1, 0);
        GridPane.setConstraints(action, 0, 1);
        GridPane.setConstraints(stocks, 0, 2);
        GridPane.setConstraints(command, 0, 3);
        GridPane.setConstraints(amount, 0, 4);
        GridPane.setConstraints(rate, 0, 5);

        ChoiceBox<String> actionChoice = new ChoiceBox<>();
        actionChoice.setStyle("-fx-font-size: 15px;");
        actionChoice.getItems().addAll("Buy", "Sell");
        actionChoice.setValue("Buy");
        tradeChoice[0] = "Buy";

        TextField rateField = new TextField();

        ChoiceBox<String> commandChoice = new ChoiceBox<>();
        commandChoice.setStyle("-fx-font-size: 15px;");
        commandChoice.getItems().addAll("Limit", "Market", "Fill Or Kill", "Immediate Or Cancel");  // "LMT", "MKT", "FOK", "IOC"
        commandChoice.setValue("Limit");  // LMT
        tradeChoice[2] = commandChoice.getValue();
        commandChoice.setOnAction(e -> {
            tradeChoice[2] = commandChoice.getValue();
            rateField.setVisible(!commandChoice.getValue().equals("Market"));  // MKT
            rate.setVisible(!commandChoice.getValue().equals("Market"));          // MKT
        });

        List<StockDTO> userStocksList = rse.getStocks(user.getUsername());
        ObservableList<StockDTO> list;
        if (tradeChoice[0].equals("Sell"))
            list = FXCollections.observableArrayList(userStocksList);  // userStocksList
        else
            list = FXCollections.observableArrayList(rse.getStocks());
        String[] columns = {"companyName", "symbol", "rate", "quantity"};
        TableView<StockDTO> tableStocks = new TableView<>();
        tableStocks.setStyle("-fx-font-size: 15px;");
        if (list.size() == 0)
            tableStocks.setPlaceholder(new Label("No rows to display"));
        else
            tableStocks.setItems(list);

        tableStocks.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableView.TableViewSelectionModel<StockDTO> selectionModel = tableStocks.getSelectionModel();
        selectionModel.setSelectionMode(SelectionMode.SINGLE);
        selectionModel.select(0);

        actionChoice.setOnAction(e -> {
            tradeChoice[0] = actionChoice.getValue();
            ObservableList<StockDTO> l;
            if (tradeChoice[0].equals("Buy"))
                l = FXCollections.observableArrayList(rse.getStocks());
            else
                l = FXCollections.observableArrayList(rse.getStocks(user.getUsername()));
            try {
                list.clear();
                list.addAll(l);
                selectionModel.select(0);
            }
            catch (Exception ignore) {}
        });

        tradeChoice[1] = selectionModel.getSelectedItems().get(0).getSymbol();
        tableStocks.setStyle("-fx-text-alignment: center;");
        for (String column : columns) {
            TableColumn<StockDTO, String> col = new TableColumn<>(column);
            col.setMinWidth(200);
            // col.prefWidthProperty().bind(tableStocks.widthProperty().multiply(2.0 / columns.length));
            col.setStyle( "-fx-alignment: CENTER;-fx-font-size: 13px;");
            col.setCellValueFactory(new PropertyValueFactory<>(column));
            tableStocks.getColumns().add(col);
        }

        ObservableList<StockDTO> selectedItems = selectionModel.getSelectedItems();
        selectedItems.addListener((ListChangeListener<StockDTO>) change -> {
            if (change != null && change.getList().size() != 0) {
                tradeChoice[1] = change.getList().get(0).getSymbol();
                System.out.println("Selection changed: " + change.getList());
            }
        });

        TextField amountField = new TextField();
        amountField.setOnAction(e -> tradeChoice[3] = amountField.getText());

        rateField.setVisible(true);
        rateField.setOnAction(e -> tradeChoice[4] = rateField.getText());

        Button back = new Button(); // "Back"
        ImageView im = new ImageView(new Image("com/ui/css/back2.png"));
        im.setFitHeight(15);
        im.setFitWidth(15);
        back.setGraphic(im);
        back.setStyle("-fx-background-color: white;");
        back.setOnAction(e -> {
            if (prevScene == null)
                window.close();
            else
                window.setScene(prevScene);
            for (UserDTO ue : all_lists.keySet()) {  // set all lists to the changed value
                all_lists.get(ue).clear();
                all_lists.get(ue).addAll(rse.getStocks(ue.getUsername())); // rse.getUsersMap().get(ue.getUsername()).getUserStock()
            }
            for (UserDTO ue : all_trans.keySet()) {   // Stage 3 only
                all_trans.get(ue).clear();
                all_trans.get(ue).addAll(rse.getTransactions(ue.getUsername()));  // rse.getUsersMap().get(ue.getUsername()).getTransactions()
            }
            admin_list.clear();
            admin_list.addAll(rse.getStocks());
        });

        Button submit = new Button("Submit");
        submit.setStyle("-fx-font-size: 15px;");
        submit.setOnAction(e -> {
            List<DealDTO> out = new ArrayList<>();
            try {
                tradeChoice[3] = amountField.getText();
                tradeChoice[4] = rateField.getText();
                for (String i: tradeChoice)
                    System.out.print(i + " - ");
                switch (tradeChoice[2]) {
                    case "Market":  // MKT
                        out = rse.MKT(tradeChoice[1], tradeChoice[0].equals("Buy"), Integer.parseInt(tradeChoice[3]), username);
                        break;
                    case "Limit":  // LMT
                        out = rse.LMT(tradeChoice[1], tradeChoice[0].equals("Buy"), Integer.parseInt(tradeChoice[3]), Integer.parseInt(tradeChoice[4]), username);
                        break;
                    case "Fill Or Kill":    // FOK
                        out = rse.FOK(tradeChoice[1], tradeChoice[0].equals("Buy"), Integer.parseInt(tradeChoice[3]), Integer.parseInt(tradeChoice[4]), username);
                        break;
                    case "Immediate Or Cancel":    // IOC
                        out = rse.IOC(tradeChoice[1], tradeChoice[0].equals("Buy"), Integer.parseInt(tradeChoice[3]), Integer.parseInt(tradeChoice[4]), username);
                        break;
                    default:
                        System.out.println("There is no option " + tradeChoice[2] + ". error in Trade function");
                        break;
                }
            } catch (NumberFormatException ignore) {
                AlertBox.displayMultiple("Alert", "a letter has been entered into amount or rate. enter a number instead");
            }
            catch (Exception exp) {
                AlertBox.displayMultiple("Alert", exp.getMessage());
                return;
            }
            if (!out.isEmpty())
                AlertBox.displayTable("Results", out);
            else
                AlertBox.displayMultiple("Alert", "There was no deal made");
            userStocksList.clear();
            userStocksList.addAll(rse.getStocks(user.getUsername()));
            for (UserDTO ue : all_lists.keySet()) {  // set all lists to the changed value
                all_lists.get(ue).clear();
                all_lists.get(ue).addAll(rse.getStocks(ue.getUsername()));
            }
            for (UserDTO ue : all_trans.keySet()) {   // Stage 3 only
                all_trans.get(ue).clear();
                all_trans.get(ue).addAll(rse.getTransactions(ue.getUsername()));
            }
            admin_list.clear();
            admin_list.addAll(rse.getStocks());
            ObservableList<StockDTO> l;
            if (tradeChoice[0].equals("Buy"))
                l = FXCollections.observableArrayList(rse.getStocks());
            else
                l = FXCollections.observableArrayList(rse.getStocks(user.getUsername()));
            list.clear();
            list.addAll(l);
            amountField.setText("");
            rateField.setText("");
        });

        HBox inner = new HBox(30);
        inner.setAlignment(Pos.CENTER);
        inner.getChildren().addAll(submit);

        GridPane.setConstraints(actionChoice, 1, 1);
        GridPane.setConstraints(tableStocks, 1, 2);
        GridPane.setConstraints(commandChoice, 1, 3);
        GridPane.setConstraints(amountField, 1, 4);
        GridPane.setConstraints(rateField, 1, 5);
        GridPane.setConstraints(inner, 1, 6);
        GridPane.setConstraints(back, 0, 0);

        layout.getChildren().addAll(title, action, stocks, command, amount, rate, actionChoice, tableStocks, commandChoice, amountField, rateField, inner, back); // submit, back

        Scene scene = new Scene(layout, window.getWidth(), window.getHeight());
        window.setScene(scene);
        scene.getStylesheets().add(Menu.class.getResource("css/trade.css").toExternalForm());
    }

    public void adminPage(String stockName, Stage stage, Scene prev) {
        String[] listNames = {"Buy", "Sell", "Approved"};
        String[] columns = {"action", "symbol", "time", "amount", "rate", "revolution", "publisherName"};

        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(10, 10, 30, 10));

        Label title = new Label(stockName);
        title.setStyle("-fx-text-fill: white;-fx-font-size: 20px;-fx-font-weight: bold;-fx-underline: true;");
        layout.getChildren().add(title);

        for (String listName : listNames) {
            Label listTitle = new Label(listName + " list: ");
            listTitle.setStyle("-fx-underline: true; -fx-font-size: 15px;-fx-text-alignment: right; -fx-text-fill: white;-fx-font-weight: bold;");
            listTitle.setAlignment(Pos.TOP_LEFT);
            AnchorPane listLine = new AnchorPane();
            listLine.getChildren().addAll(listTitle);
            ObservableList<DealDTO> list = FXCollections.observableArrayList(rse.getAdminList(stockName, listName));
            TableView<DealDTO> dealsTable = new TableView<>();
            if (list.size() == 0)
                dealsTable.setPlaceholder(new Label("No rows to display"));
            else
                dealsTable.setItems(list);

            dealsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            dealsTable.setStyle("-fx-text-alignment: center;-fx-font-size: 15px;");
            for (String column : columns) {
                TableColumn<DealDTO, String> col = new TableColumn<>(column);
                col.prefWidthProperty().bind(dealsTable.widthProperty().multiply(1.0 / columns.length));
                col.setStyle( "-fx-alignment: CENTER;-fx-font-size: 13px;");
                col.setCellValueFactory(new PropertyValueFactory<>(column));
                dealsTable.getColumns().add(col);
            }
            layout.getChildren().addAll(listLine, dealsTable);
        }

        Scene scene = new Scene(layout, window.getWidth(), window.getHeight());
        HBox inner = new HBox(30);
        inner.setAlignment(Pos.CENTER);

        Button graphBtn = new Button("Show Graph");
        graphBtn.setStyle("-fx-font-weight: bold;-fx-font-size: 15px;");
        graphBtn.setOnAction(e -> Menu.graph(stockName, stage, scene, rse));

        Button back = new Button("back");
        back.setStyle("-fx-font-size: 15px;");
        back.setOnAction(e -> stage.setScene(prev));

        inner.getChildren().addAll(graphBtn, back);

        layout.getChildren().add(inner);
        scene.getStylesheets().add(Menu.class.getResource("css/admin.css").toExternalForm());
        stage.setScene(scene);
    }

    public static void graph(String stockName, Stage window, Scene prev, RSE rse) {
        Map<String, Integer> tableGraph = new TreeMap<>(rse.graph(stockName));
        int lowestPrice = 0;
        int maxPrice = 100;
        if (tableGraph.size() != 0) {
            maxPrice = tableGraph.values().stream().max(Integer::compareTo).get() + 10 * 4;
            lowestPrice = tableGraph.values().stream().min(Integer::compareTo).get();
            if (lowestPrice - 10 * 4 > 0)
                lowestPrice -= 10 * 4;
        }
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Time");
        NumberAxis yAxis = new NumberAxis(lowestPrice, maxPrice, 10);
        yAxis.setLabel("Price");

        xAxis.setTickLabelFill(Color.WHITE);
        xAxis.setTickLabelFont(Font.font(15));
        yAxis.setTickLabelFill(Color.WHITE);
        yAxis.setTickLabelFont(Font.font(15));

        //  Creating the Area chart
        AreaChart areaChart = new AreaChart(xAxis, yAxis);
        areaChart.setMinSize(window.getWidth() - 200, window.getHeight() - 200);  //1000, 600
        areaChart.setTitle("Stock Price X Time ");

        XYChart.Series series = new XYChart.Series();
        series.setName(stockName);

        if (tableGraph.size() == 0) {
            String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
            for (int i = 0; i < 7; i++)
                series.getData().add(new XYChart.Data(days[i], ThreadLocalRandom.current().nextInt(100)));
        } else {
            for(String key : tableGraph.keySet())
                series.getData().add(new XYChart.Data(key, tableGraph.get(key)));
        }

        areaChart.getData().addAll(series);

        Group root = new Group(areaChart);

        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);
        Button back = new Button("Back");
        back.setOnAction(e -> window.setScene(prev));
        layout.getChildren().addAll(root, back);

        Scene scene = new Scene(layout, window.getWidth(), window.getHeight());
        window.setTitle(stockName + " graph:");
        scene.getStylesheets().add(Menu.class.getResource("css/graph.css").toExternalForm());
        window.setScene(scene);
    }

    private static void addUserStock(String username, RSE rse) {  // Stage 3 only
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setMinWidth(350);
        window.setMinHeight(350);
        window.getIcons().add(new Image("com/ui/css/icon.jpg"));

        GridPane layout = new GridPane();
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(10, 10, 10, 10));
        Label title = new Label("Add Stock: ");
        title.setStyle("-fx-font-size: 20px;");

        Label companyNameTitle = new Label("Company Name: ");
        TextField companyName = new TextField();

        Label symbolTitle = new Label("Stock Symbol: ");
        TextField stockSymbol= new TextField();

        Label amountTitle = new Label("Stock shares amount: ");
        TextField stockAmount = new TextField();

        Label stockRateTitle = new Label("Stock Total rate: ");
        TextField stockRate = new TextField();

        Label exp = new Label("");
        Button submit = new Button("create");
        submit.setOnAction(e -> {
            try {
                int amount = Integer.parseInt(stockAmount.getText());
                int rate = Integer.parseInt(stockRate.getText()) / amount;
                rse.addUserStock(companyName.getText(), stockSymbol.getText(), rate, amount, username);
                exp.setText("");
                window.close();
            } catch (NumberFormatException ignore) {
                exp.setText("There is no number with a digit in it. please enter a number");
            }
            catch (Exception ex) {
                exp.setText(ex.getMessage());
            }
        });

        layout.setVgap(20);
        layout.setHgap(10);
        GridPane.setConstraints(title, 1, 1);
        GridPane.setConstraints(companyNameTitle, 0, 3);
        GridPane.setConstraints(companyName, 1, 3);
        GridPane.setConstraints(symbolTitle, 0, 4);
        GridPane.setConstraints(stockSymbol, 1, 4);
        GridPane.setConstraints(amountTitle, 0, 5);
        GridPane.setConstraints(stockAmount, 1, 5);
        GridPane.setConstraints(stockRateTitle, 0, 6);
        GridPane.setConstraints(stockRate, 1, 6);
        GridPane.setConstraints(submit, 1, 7);
        GridPane.setConstraints(exp, 0, 9, 2, 1);
        layout.getChildren().addAll(title, companyNameTitle, companyName, symbolTitle, stockSymbol, amountTitle, stockAmount, stockRateTitle, stockRate, submit, exp);

        Scene scene = new Scene(layout, window.getWidth(), window.getHeight());
        scene.getStylesheets().add(Menu.class.getResource("css/alert2.css").toExternalForm());
        window.setScene(scene);
        window.showAndWait();
    }

    private void save(String fileName) throws IOException {
        /* save the current system to a binary file */
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName))) {
            out.writeObject(rse);
            out.flush();
        }
    }

    private void load(String fileName) throws IOException, ClassNotFoundException {
        /* load the system from a file */
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName))) {
            this.rse = (RSE) in.readObject();
        }
    }

    private static String capital(String name) { return ("" + name.charAt(0)).toUpperCase() + name.substring(1); }
}
