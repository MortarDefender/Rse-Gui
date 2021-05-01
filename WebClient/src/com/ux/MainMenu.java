package com.ux;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import objects.*;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;


public class MainMenu {
    private boolean flag;
    private final Gson gson;
    private final Scene prev;
    private final Stage window;
    private final String username;
    private final boolean userType;
    private final List<Timer> timerMap;
    private final ScheduledExecutorService schedule;

    public MainMenu(Stage window, Scene prev, String username, boolean userType) {
        this.flag = true;
        this.prev = prev;
        this.window = window;
        this.gson = new Gson();
        this.username = username;
        this.userType = userType;
        this.timerMap = new ArrayList<>();
        this.schedule = Executors.newSingleThreadScheduledExecutor();
    }

    public void startPage() {
        window.setOnCloseRequest( e -> {
            e.consume();
            for (Timer t: this.timerMap)
                t.cancel();
            schedule.shutdownNow();
            MainPage.logout(this.username);
            if (flag) {
                flag = false;
                window.setScene(prev);
            }
            else
                window.close();
        });

        if (this.userType)
            userPage();
        else
            adminPage(prev);
    }

    private void userPage() {
        Timer t = new Timer();
        timerMap.add(t);
        window.setTitle("Main Menu");
        window.setMinWidth(1000);
        window.setMinHeight(600);

        Label total_title = new Label("Total Stock Value: " + getUserRevolution());

        VBox user_layout = new VBox(10);
        user_layout.setAlignment(Pos.CENTER);
        user_layout.setPadding(new Insets(10, 10, 10, 10));

        Scene scene = new Scene(user_layout, window.getWidth(), window.getHeight());
        Label title = new Label(capital(this.username) + ": ");
        title.setStyle("-fx-text-fill: white;-fx-underline: true; -fx-font-weight: bold;-fx-font-size: 20px;");

        Label stock_title = new Label("Stocks: ");
        stock_title.setStyle("-fx-text-fill: white;-fx-underline: true;-fx-font-weight: bold;  -fx-font-size: 15px;-fx-text-alignment: center;");
        Button addStock = new Button();  // add stock
        ImageView im = new ImageView(new Image("com/ux/css/addBtn2.png"));
        im.setFitHeight(20);
        im.setFitWidth(20);
        addStock.setGraphic(im);
        addStock.setStyle("-fx-background-color: transparent;");
        addStock.setOnAction(e -> {
            try {
                addUserStock();
                total_title.setText("Total Stock Value: " + getUserRevolution());
            } catch (NullPointerException ignore) { }
            catch (Exception ex) {
                Alert.displayMultiple("Alert", "Error in file: " + ex.getMessage());
            }
        });

        AnchorPane stockLine = new AnchorPane();
        AnchorPane.setRightAnchor(addStock, 0d);
        stockLine.getChildren().addAll(stock_title, addStock);

        String[] columns = {"symbol", "quantity", "rate"};
        TableView<StockDTO> user_stocks = new TableView<>();
        user_stocks.setPlaceholder(new Label("No rows to display"));

        TableView<TransactionDTO> userTrans = new TableView<>();
        userTrans.setPlaceholder(new Label("No rows to display"));

        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                refreshStock(user_stocks);
                refreshTrade(userTrans);
            }
        },0, MainPage.refreshTime);

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
            user_stocks.getColumns().add(col);
        }

        Label transactionLabel = new Label("Transactions: ");
        transactionLabel.setStyle("-fx-text-fill: white; -fx-font-size: 15px;-fx-font-weight: bold; -fx-underline: true;");
        Button addTrans = new Button();  // add trans
        ImageView im2 = new ImageView(new Image("com/ux/css/addBtn2.png"));
        im2.setFitHeight(20);
        im2.setFitWidth(20);
        addTrans.setGraphic(im2);
        addTrans.setStyle("-fx-background-color: transparent;");
        addTrans.setOnAction(e -> {
            try {
                int amount = Integer.parseInt(Alert.getAmount( "Account Charge", "Enter the amount to charge your account: ", true));
                System.out.println("self charge: " + amount);
                Map<String, String> charge = new HashMap<>();
                charge.put("username", this.username);  // "session"
                charge.put("amount", String.valueOf(amount));
                MainPage.post("/addCharge", charge);

                String[] req1 = {"info", "symbol", "username"};
                String[] res1 = {"false", "--", this.username};
                Type transListType1 = new TypeToken<List<TransactionDTO>>(){}.getType();
                List<TransactionDTO> userTransactionsTemp = gson.fromJson(MainPage.post("/userUtil", req1, res1), transListType1);
                ObservableList<TransactionDTO> transListTemp = FXCollections.observableArrayList(userTransactionsTemp);
                userTrans.setItems(transListTemp);
               total_title.setText("Total Stock Value: " + getUserRevolution());
            } catch (NullPointerException | NumberFormatException err) {
                System.out.println(err.getMessage());
            }
            catch (Exception ex) {
                Alert.displayMultiple("Alert", "Error in file: " + ex.getMessage());
            }
        });

        AnchorPane transLine = new AnchorPane();
        AnchorPane.setRightAnchor(addTrans, 0d);
        transLine.getChildren().addAll(transactionLabel, addTrans);

        String[] transColumns = {"time", "symbol", "actionType", "sum", "accountBefore", "accountAfter"};

        userTrans.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        userTrans.setStyle("-fx-text-alignment: center;-fx-font-size: 15px;");
        for (String column : transColumns) {
            TableColumn<TransactionDTO, String> col = new TableColumn<>(column);
            col.prefWidthProperty().bind(userTrans.widthProperty().multiply(1.0 / transColumns.length));
            col.setStyle( "-fx-alignment: CENTER;-fx-font-size: 13px;");
            col.setCellValueFactory(new PropertyValueFactory<>(column));
            userTrans.getColumns().add(col);
        }

        HBox inner = new HBox(100);
        inner.setAlignment(Pos.CENTER);

        total_title.setStyle("-fx-text-fill: white;-fx-font-weight: bold;-fx-font-size: 15px;");
        // Label type_title = new Label(String.valueOf(user.getType()));  // stage 3
        Button trade = new Button("Trade");
        trade.setStyle("-fx-font-size: 15px;");
        trade.setOnAction(e -> {
            t.cancel();
            trade();
            total_title.setText("Total Stock Value: " + getUserRevolution());
        });
        Button loadBtn = new Button("Load Xml");
        loadBtn.setOnAction(e -> {
            try {  // load an xml
                ;
                // loadXmlFile(rse, window, user.getUsername());
            } catch (NullPointerException ignore) {
            } catch (Exception ex) {
                Alert.displayMultiple("Alert", ex.getMessage());
            }
        });
        inner.getChildren().addAll(total_title, trade, loadBtn);

        Button back = new Button(); // "Back"
        ImageView ime = new ImageView(new Image("com/ux/css/back2.png"));
        ime.setFitHeight(15);
        ime.setFitWidth(15);
        back.setGraphic(ime);
        back.setStyle("-fx-background-color: white;");
        back.setOnAction(e -> {
            t.cancel();
            this.flag = false;
            window.setScene(prev);
        });

        AnchorPane backLine = new AnchorPane();
        AnchorPane.setLeftAnchor(back, 0d);
        backLine.getChildren().addAll(back);

        user_layout.getChildren().addAll(backLine, title, stockLine, user_stocks, transLine, userTrans, inner);
        scene.getStylesheets().add(MainMenu.class.getResource("css/alert.css").toExternalForm());
        window.setScene(scene);
    }

    private int getUserRevolution() {
        String[] cols = {"info", "symbol", "username"};
        String[] ans = {"true", "totalRevolution", this.username};
        return gson.fromJson(MainPage.post("/userUtil", cols, ans), int.class);
    }

    private void refreshStock(TableView<StockDTO> userStocks) {
        String[] cols = {"all", "info", "symbol"};
        String[] ans = {"true", "true", this.username};
        Type listType = new TypeToken<List<StockDTO>>(){}.getType();
        List<StockDTO> stocks = gson.fromJson(MainPage.post("/getStocks", cols, ans), listType);
        ObservableList<StockDTO> list = FXCollections.observableArrayList(stocks);
        if (list.size() != 0)
            userStocks.setItems(list);
    }

    private void refreshTrade(TableView<TransactionDTO> userTrans) {
        String[] req = {"info", "symbol", "username"};
        String[] res = {"false", "--", this.username};
        Type listType = new TypeToken<List<TransactionDTO>>(){}.getType();
        List<TransactionDTO> userTransactions = gson.fromJson(MainPage.post("/userUtil", req, res), listType);

        ObservableList<TransactionDTO> transList = FXCollections.observableArrayList(userTransactions);
        if (transList.size() != 0)
            userTrans.setItems(transList);
    }

    private void trade() {
        Timer t = new Timer();
        timerMap.add(t);
        String[] tradeChoice = {"", "", "", "", ""};

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
            rate.setVisible(!commandChoice.getValue().equals("Market"));       // MKT
        });

        String[] keys = {"all", "info", "symbol"};
        String[] values = {"true", "true", this.username};

        Type listType = new TypeToken<List<StockDTO>>(){}.getType();
        List<StockDTO> userStocksList = gson.fromJson(MainPage.post("/getStocks", keys, values), listType);
        values[1] = "false";
        List<StockDTO> allStocksList = gson.fromJson(MainPage.post("/getStocks", keys, values), listType);

        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                values[1] = "true";
                Type listType = new TypeToken<List<StockDTO>>(){}.getType();
                userStocksList.clear();
                userStocksList.addAll(gson.fromJson(MainPage.post("/getStocks", keys, values), listType));
                values[1] = "false";
                allStocksList.clear();
                allStocksList.addAll(gson.fromJson(MainPage.post("/getStocks", keys, values), listType));
            }
        },0, MainPage.refreshTime);

        ObservableList<StockDTO> list;
        if (tradeChoice[0].equals("Sell"))
            list = FXCollections.observableArrayList(userStocksList);
        else
            list = FXCollections.observableArrayList(allStocksList);
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
                l = FXCollections.observableArrayList(allStocksList);
            else
                l = FXCollections.observableArrayList(userStocksList);
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
            col.setStyle( "-fx-alignment: CENTER;-fx-font-size: 13px;");
            col.setCellValueFactory(new PropertyValueFactory<>(column));
            tableStocks.getColumns().add(col);
        }

        ObservableList<StockDTO> selectedItems = selectionModel.getSelectedItems();
        selectedItems.addListener((ListChangeListener<StockDTO>) change -> {
            if (change != null && change.getList().size() != 0) {
                tradeChoice[1] = change.getList().get(0).getSymbol();
                System.out.println("Selection changed: " + change.getList().get(0).getSymbol());
            }
        });

        TextField amountField = new TextField();
        amountField.setOnAction(e -> tradeChoice[3] = amountField.getText());

        rateField.setVisible(true);
        rateField.setOnAction(e -> tradeChoice[4] = rateField.getText());

        Button back = new Button(); // "Back"
        ImageView im = new ImageView(new Image("com/ux/css/back2.png"));
        im.setFitHeight(15);
        im.setFitWidth(15);
        back.setGraphic(im);
        back.setStyle("-fx-background-color: white;");
        back.setOnAction(e -> {
            t.cancel();
            userPage();
        });


        Type commandListType = new TypeToken<List<DealDTO>>(){}.getType();

        Button submit = new Button("Submit");
        submit.setStyle("-fx-font-size: 15px;");
        submit.setOnAction(e -> {
            String[] commandCol = {"username", "action", "symbol", "amount", "rate", "command"};
            String[] commandAns = {this.username, String.valueOf(tradeChoice[0].equals("Buy")), tradeChoice[1], tradeChoice[3], tradeChoice[4], ""};
            List<DealDTO> out = new ArrayList<>();
            try {
                tradeChoice[3] = amountField.getText();
                tradeChoice[4] = rateField.getText();

                commandAns[3] = amountField.getText();
                commandAns[4] = rateField.getText();
                for (String i: tradeChoice)
                    System.out.print(i + " - ");
                switch (tradeChoice[2]) {
                    case "Market":  // MKT
                        commandAns[5] = "mkt";
                        break;
                    case "Limit":  // LMT
                        commandAns[5] = "lmt";
                        break;
                    case "Fill Or Kill":    // FOK
                        commandAns[5] = "fok";
                        break;
                    case "Immediate Or Cancel":    // IOC
                        commandAns[5] = "ioc";
                        break;
                    default:
                        System.out.println("There is no option " + tradeChoice[2] + ". error in Trade function");
                        break;
                }
                out = gson.fromJson(MainPage.post("/addTrade", commandCol, commandAns), commandListType);
            } catch (NumberFormatException ignore) {
                Alert.displayMultiple("Alert", "a letter has been entered into amount or rate. enter a number instead");
            }
            catch (Exception exp) {
                Alert.displayMultiple("Alert", exp.getMessage());
                return;
            }
            if (!out.isEmpty())
                Alert.displayTable("Results", out);
            else
                Alert.displayMultiple("Alert", "There was no deal made");

            values[1] = "true";
            userStocksList.clear();
            userStocksList.addAll(gson.fromJson(MainPage.post("/getStocks", keys, values), listType));
            values[1] = "false";
            allStocksList.clear();
            allStocksList.addAll(gson.fromJson(MainPage.post("/getStocks", keys, values), listType));

            ObservableList<StockDTO> l;
            if (tradeChoice[0].equals("Buy"))
                l = FXCollections.observableArrayList(allStocksList);
            else
                l = FXCollections.observableArrayList(userStocksList);
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
        scene.getStylesheets().add(MainMenu.class.getResource("css/trade.css").toExternalForm());
    }

    private void adminPage(Scene scene) {
        Timer t = new Timer();
        timerMap.add(t);
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

        String[] columns = {"companyName", "symbol", "quantity", "rate", "totalDeals", "revolution"};
        TableView<StockDTO> all_stocks = new TableView<>();

        all_stocks.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableView.TableViewSelectionModel<StockDTO> selectionModel = all_stocks.getSelectionModel();
        selectionModel.setSelectionMode(SelectionMode.SINGLE);
        selectionModel.select(0);

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

        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                adminHelper(all_stocks, selectionModel, choice);
            }
        },0, MainPage.refreshTime);

        Button submit = new Button("Investigate stock");
        submit.setStyle("-fx-font-size: 15px;");
        submit.setOnAction(e -> {
            t.cancel();
            adminListPage(choice[0]);
        } );

        admin_layout.getChildren().addAll(admin_title, stockLine, all_stocks, submit);
        Scene currScene = new Scene(admin_layout, window.getWidth(), window.getHeight());
        currScene.getStylesheets().add(MainMenu.class.getResource("css/admin.css").toExternalForm());
        window.setScene(currScene);
    }

    private void adminHelper(TableView<StockDTO> all_stocks, TableView.TableViewSelectionModel<StockDTO> selectionModel, String[] choice) {
        String[] keys = {"all", "info", "symbol"};
        String[] values = {"true", "false", this.username};

        Type listType = new TypeToken<List<StockDTO>>(){}.getType();
        List<StockDTO> allStocksList = gson.fromJson(MainPage.post("/getStocks", keys, values), listType);
        ObservableList<StockDTO> admin_list = FXCollections.observableArrayList(allStocksList);

        if (admin_list.size() == 0)
            all_stocks.setPlaceholder(new Label("No rows to display"));
        else
            all_stocks.setItems(admin_list);
        if (selectionModel.getSelectedItems().size() != 0)
            choice[0] = selectionModel.getSelectedItems().get(0).getSymbol();
    }

    private void adminListPage(String stockName) {
        Timer t = new Timer();
        timerMap.add(t);
        String[] listNames = {"Buy", "Sell", "Approved"};
        String[] columns = {"action", "symbol", "time", "amount", "rate", "revolution", "publisherName"};

        Map<String, TableView<DealDTO>>  allAdminList = new HashMap<>();
        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(10, 10, 30, 10));

        Label title = new Label(stockName);
        title.setStyle("-fx-text-fill: white;-fx-font-size: 20px;-fx-font-weight: bold;-fx-underline: true;");
        layout.getChildren().add(title);

        Map<String, String> getAdminStocks = new HashMap<>();
        getAdminStocks.put("listKind", "all");
        getAdminStocks.put("symbol", stockName);
        getAdminStocks.put("username", this.username);

        Type listType = new TypeToken<Map<String, List<DealDTO>>>(){}.getType();
        Map<String, List<DealDTO>> adminLists = gson.fromJson(MainPage.post("/adminApi", getAdminStocks), listType);

        for (String listName : listNames) {
            Label listTitle = new Label(listName + " list: ");
            listTitle.setStyle("-fx-underline: true; -fx-font-size: 15px;-fx-text-alignment: right; -fx-text-fill: white;-fx-font-weight: bold;");
            listTitle.setAlignment(Pos.TOP_LEFT);
            AnchorPane listLine = new AnchorPane();
            listLine.getChildren().addAll(listTitle);

            ObservableList<DealDTO> list = FXCollections.observableArrayList(adminLists.get(listName));
            TableView<DealDTO> dealsTable = new TableView<>();
            allAdminList.put(listName, dealsTable);
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

        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                adminListHelper(stockName, allAdminList);
            }
        },0, MainPage.refreshTime);

        Scene scene = new Scene(layout, window.getWidth(), window.getHeight());
        HBox inner = new HBox(30);
        inner.setAlignment(Pos.CENTER);

        Button graphBtn = new Button("Show Graph");
        graphBtn.setStyle("-fx-font-weight: bold;-fx-font-size: 15px;");
        graphBtn.setOnAction(e -> {
            t.cancel();
            graphPage(stockName, scene);
        });

        Button back = new Button("back");
        back.setStyle("-fx-font-size: 15px;");
        back.setOnAction(e -> adminPage(scene));

        inner.getChildren().addAll(graphBtn, back);

        layout.getChildren().add(inner);
        scene.getStylesheets().add(MainMenu.class.getResource("css/admin.css").toExternalForm());
        window.setScene(scene);
    }

    private void adminListHelper(String stockName, Map<String, TableView<DealDTO>> allAdminList) {
        String[] req = {"listKind", "symbol", "username"};
        String[] res = {"all", stockName, this.username};

        Type listType = new TypeToken<Map<String, List<DealDTO>>>(){}.getType();
        Map<String, List<DealDTO>> adminLists = gson.fromJson(MainPage.post("/adminApi", req, res), listType);
        for(String key : allAdminList.keySet())
            allAdminList.get(key).setItems(FXCollections.observableArrayList(adminLists.get(key)));
    }

    private void graphPage(String stockName, Scene prev) {
        Map<String, Number> tableGraph = new TreeMap<>(getGraph(stockName, null));
        int lowestPrice = 0;
        int maxPrice = 100;
        if (tableGraph.size() != 0) {
            maxPrice = (int) tableGraph.values().stream().max((x, y) -> (x.intValue() < y.intValue()) ? -1 : ((x.equals(y)) ? 0 : 1)).get() + 10 * 4;  // getAsInt
            lowestPrice = (int) tableGraph.values().stream().min((x, y) -> (x.intValue() < y.intValue()) ? -1 : ((x.equals(y)) ? 0 : 1)).get();
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
        AreaChart<String, Number> areaChart = new AreaChart<>(xAxis, yAxis);
        areaChart.setMinSize(window.getWidth() - 200, window.getHeight() - 200);  //1000, 600
        areaChart.setTitle("Stock Price X Time ");
        areaChart.setAnimated(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(stockName);

        if (tableGraph.size() == 0) {
            String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
            for (int i = 0; i < 7; i++)
                series.getData().add(new XYChart.Data<>(days[i], ThreadLocalRandom.current().nextInt(100)));
        } else {
            for(String key : tableGraph.keySet())
                series.getData().add(new XYChart.Data<>(key, tableGraph.get(key)));
        }

        areaChart.getData().add(series);

        Group root = new Group(areaChart);

        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);
        Button back = new Button("Back");
        back.setOnAction(e -> {
            this.schedule.shutdownNow();
            window.setScene(prev);
        });
        layout.getChildren().addAll(root, back);

        Scene scene = new Scene(layout, window.getWidth(), window.getHeight());
        window.setTitle(stockName + " graph:");
        scene.getStylesheets().add(MainPage.class.getResource("css/graph.css").toExternalForm());
        window.setScene(scene);

        this.schedule.scheduleAtFixedRate(() -> Platform.runLater(() -> graphHelper(stockName, tableGraph, series)), 0, 2, TimeUnit.SECONDS);
    }

    private void graphHelper(String stockName, Map<String, Number> tableGraph, XYChart.Series<String, Number> series) {
        Map<String, Number> update = new TreeMap<>(getGraph(stockName, tableGraph));
        if (update.size() != 0) {
            tableGraph.putAll(update);

            for (String key : tableGraph.keySet())
                series.getData().add(new XYChart.Data<>(key, tableGraph.get(key)));
        }
    }

    private Map<String, Number> getGraph(String stockName, Map<String, Number> tableGraph) {
        String[] keys = {"api", "symbol", "username"};
        String[] values = {"true", stockName, username};

        Type graphMapType = new TypeToken<Map<String, Integer>>(){}.getType();
        Map<String, Number> graphMap = gson.fromJson(MainPage.post("/graphUtil", keys, values), graphMapType);
        if (tableGraph == null)
            return graphMap;

        Map<String, Number> updateMap = new TreeMap<>();

        for(String key : graphMap.keySet()) {
            if (!tableGraph.containsKey(key))
                updateMap.put(key, graphMap.get(key));
        }
        return updateMap;
    }

    private void addUserStock() {
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setMinWidth(350);
        window.setMinHeight(350);
        window.getIcons().add(new Image("com/ux/css/icon.jpg"));

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
                int totalRate = Integer.parseInt(stockRate.getText());

                String[] keys = {"companyName", "symbol", "quantity", "totalValue", "username"};
                String[] values = {companyName.getText(), stockSymbol.getText(), String.valueOf(amount), String.valueOf(totalRate), this.username};

                Type graphMapType = new TypeToken<Map<String, String>>(){}.getType();
                Map<String, String> msg = gson.fromJson(MainPage.post("/addStock", keys, values), graphMapType);
                if (msg.containsKey("error"))
                    exp.setText("There is no number with a digit in it. please enter a number");
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
        scene.getStylesheets().add(MainMenu.class.getResource("css/alert2.css").toExternalForm());
        window.setScene(scene);
        window.showAndWait();
    }

    private static String capital(String name) { return ("" + name.charAt(0)).toUpperCase() + name.substring(1); }
}
