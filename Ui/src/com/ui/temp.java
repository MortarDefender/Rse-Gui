package com.ui;

import com.rse.*;
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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

/*public class temp {
    private final Stage window;
    private Scene prev = null;
    private RSE rse;
    private String loadChoice, saveChoice;
    private boolean status = true;
    private ArrayList<Stock> userStocksList;
    private VBox admin_layout;

    public temp(RSE r, Stage stage, Scene scene) {
        this.rse = r;
        this.window = stage;
        this.prev = scene;
    }

    public temp(RSE r) {
        this.rse = r;
        this.window = new Stage();
    }

    public void startMenu() {
        Map<User, ObservableList<Stock>> all_lists = new HashMap<>();
        Map<String, TableView<Transaction>> transTables = new HashMap<>();  // Stage 3 only
        Map<User, ObservableList<Transaction>> all_trans = new HashMap<>(); // Stage 3 only
        Map<User, Label> totals = new HashMap<>();                          // Stage 3 only
        window.setTitle("Main Menu");
        window.setMinWidth(1000);
        window.setMinHeight(600);  // 800

        window.setOnCloseRequest(e -> {
            e.consume();
            if (this.status) {
                this.status = false;
                window.setScene(prev);
            }
            else
                window.close();
        });

        ObservableList<Stock> admin_list = FXCollections.observableArrayList(rse.getStocks());

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

        // VBox layout = menuPage(window, rse, all_lists, admin_list, all_trans, transTables, totals);
        menu.setContent(layout);
        TabPane tabPane = new TabPane(menu);
        Scene scene = new Scene(tabPane);

        ArrayList<User> users = rse.getUsers();

        for (User user : users) {
            Tab u = new Tab(capital(user.getUsername()));
            u.setClosable(false);
            VBox user_layout = userPage(user, scene, all_lists, all_trans, admin_list, transTables, totals);

            /*VBox user_layout = new VBox(10);
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
                    for (User ue : all_lists.keySet()) {  // set all lists to the changed value
                        all_lists.get(ue).clear();
                        all_lists.get(ue).addAll(rse.getUsersMap().get(ue.getUsername()).getUserStock());
                        // Stage 3 only
                        all_trans.get(ue).clear();
                        all_trans.get(ue).addAll(rse.getUsersMap().get(ue.getUsername()).getTransactions());
                        transTables.get(ue.getUsername()).setItems(all_trans.get(ue));
                        totals.get(ue).setText("Total Stock Value: " + ue.getTotalRevolution());
                    }
                    admin_list.clear();
                    admin_list.addAll(rse.getStocks());
                    exp_title.setText("");
                } catch (NullPointerException ignore) { }
                catch (Exception ex) {
                    exp_title.setText("Error in file: " + ex.getMessage());
                    if (!layout.getChildren().contains(exp_title))
                        layout.getChildren().add(exp_title);
                }
            });

            AnchorPane stockLine = new AnchorPane();
            AnchorPane.setRightAnchor(addStock, 0d);
            stockLine.getChildren().addAll(stock_title, addStock);

            // stockLine.getChildren().addAll(ap); // stock_title, addStock

            ObservableList<Stock> list = FXCollections.observableArrayList(user.getUserStock());
            all_lists.put(user, list);
            String[] columns = {"symbol", "quantity", "rate"};
            TableView<Stock> user_stocks = new TableView<>();

            if (list.size() == 0)
                user_stocks.setPlaceholder(new Label("No rows to display"));
            else
                user_stocks.setItems(list);

            TableView.TableViewSelectionModel<Stock> selectionModel = user_stocks.getSelectionModel();
            selectionModel.setSelectionMode(SelectionMode.SINGLE);
            selectionModel.select(0);
            user_stocks.setStyle("-fx-text-alignment: center;-fx-font-size: 15px;");
            for (String column : columns) {
                TableColumn<Stock, String> col = new TableColumn<>(column);
                // col.setMinWidth(200);
                col.prefWidthProperty().bind(user_stocks.widthProperty().multiply(1.0 / columns.length));
                col.setStyle( "-fx-alignment: CENTER;-fx-font-size: 13px;");
                col.setCellValueFactory(new PropertyValueFactory<>(column));
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
                    rse.accountCharge(user.getUsername(), amount);
                    for (User ue : all_lists.keySet()) {  // set all lists to the changed value
                        all_trans.get(ue).clear();
                        all_trans.get(ue).addAll(rse.getUsersMap().get(ue.getUsername()).getTransactions());
                        transTables.get(ue.getUsername()).setItems(all_trans.get(ue));
                        totals.get(ue).setText("Total Stock Value: " + ue.getTotalRevolution());
                    }
                    exp_title.setText("");
                } catch (NullPointerException | NumberFormatException ignore) { }
                catch (Exception ex) {
                    exp_title.setText("Error in text: " + ex);
                    if (!layout.getChildren().contains(exp_title))
                        layout.getChildren().add(exp_title);
                }
            });

            AnchorPane transLine = new AnchorPane();
            AnchorPane.setRightAnchor(addTrans, 0d);
            transLine.getChildren().addAll(transactionLabel, addTrans);

            String[] transColumns = {"symbol", "actionType", "transactionFee", "accountBalance", "time"};
            TableView<Transaction> userTrans = new TableView<>();
            ObservableList<Transaction> transList = FXCollections.observableArrayList(rse.getUsersMap().get(user.getUsername()).getTransactions());
            all_trans.put(user, transList);
            transTables.put(user.getUsername(), userTrans);

            if (transList.size() == 0)
                userTrans.setPlaceholder(new Label("No rows to display"));
            else
                userTrans.setItems(transList);

            userTrans.setStyle("-fx-text-alignment: center;-fx-font-size: 15px;");
            for (String column : transColumns) {
                TableColumn<Transaction, String> col = new TableColumn<>(column);
                col.prefWidthProperty().bind(userTrans.widthProperty().multiply(1.0 / transColumns.length));
                col.setStyle( "-fx-alignment: CENTER;-fx-font-size: 13px;");
                col.setCellValueFactory(new PropertyValueFactory<>(column));
                userTrans.getColumns().add(col);
            }


            // inner layout
            HBox inner = new HBox(100);
            inner.setAlignment(Pos.CENTER);
            Label total_title = new Label("Total Stock Value: " + user.getTotalRevolution());
            total_title.setStyle("-fx-text-fill: white;-fx-font-weight: bold;-fx-font-size: 15px;");
            totals.put(user, total_title);
            // Label type_title = new Label(String.valueOf(user.getType()));  // stage 3
            Button trade = new Button("Trade");
            trade.setStyle("-fx-font-size: 15px;");
            trade.setOnAction(e -> {
                tradeOneWindow(window, user.getUsername(), rse, scene, all_lists, admin_list, all_trans);
                for (User ur: all_trans.keySet()) {  // Stage 3 only
                    transTables.get(ur.getUsername()).setItems(all_trans.get(ur));
                    totals.get(ur).setText("Total Stock Value: " + ur.getTotalRevolution());
                }
            });
            Button loadBtn = new Button("Load Xml");
            loadBtn.setOnAction(e -> {
                try {
                    Main.loadXmlFile(rse, window, user.getUsername());
                } catch (NullPointerException ignore) {
                } catch (Exception ex) {
                    AlertBox.displayMultiple("Alert", ex.getMessage());
                }
            });
            inner.getChildren().addAll(total_title, trade, loadBtn);
            user_layout.getChildren().addAll(title, stockLine, user_stocks, transLine, userTrans, inner);  // stock_title

            u.setContent(user_layout);
            tabPane.getTabs().add(u);
        }

        Tab admin = new Tab("Admin");
        admin.setClosable(false);

        /*VBox admin_layout = new VBox(10);
        admin_layout.setAlignment(Pos.CENTER);
        admin_layout.setPadding(new Insets(10, 10, 10, 10));

        Label admin_title = new Label("Admin Page: ");
        Label admin_stock_title = new Label("Stock List: ");
        admin_stock_title.setStyle("-fx-text-fill: white;-fx-underline: true;-fx-font-size: 15px;-fx-text-alignment: center;");
        admin_title.setStyle("-fx-text-fill: white;-fx-underline: true;-fx-font-weight: bold; -fx-font-size: 20px;-fx-text-alignment: center;");
        admin_title.setAlignment(Pos.TOP_LEFT);

        String[] columns = {"companyName", "symbol", "rate", "totalDeals", "revolution", "quantity"};
        TableView<Stock> all_stocks = new TableView<>();

        if (admin_list.size() == 0)
            all_stocks.setPlaceholder(new Label("No rows to display"));
        else
            all_stocks.setItems(admin_list);

        TableView.TableViewSelectionModel<Stock> selectionModel = all_stocks.getSelectionModel();
        selectionModel.setSelectionMode(SelectionMode.SINGLE);
        selectionModel.select(0);
        choice = selectionModel.getSelectedItems().get(0).getSymbol();
        all_stocks.setStyle("-fx-text-alignment: center;-fx-font-size: 15px;");
        for (String column : columns) {
            TableColumn<Stock, String> col = new TableColumn<>(column);
            // col.setMinWidth(200);
            col.prefWidthProperty().bind(all_stocks.widthProperty().multiply(1.0 / columns.length));
            col.setStyle( "-fx-alignment: CENTER;-fx-font-size: 13px;");
            col.setCellValueFactory(new PropertyValueFactory<>(column));
            all_stocks.getColumns().add(col);
        }

        ObservableList<Stock> selectedItems = selectionModel.getSelectedItems();
        selectedItems.addListener((ListChangeListener<Stock>) change -> {
            if (change != null && change.getList().size() != 0) {
                choice = change.getList().get(0).getSymbol();
                System.out.println("Selection changed: " + change.getList());
            }
        });

        // Scene scene = new Scene(tabPane);

        Button submit = new Button("Investigate stock");
        submit.setStyle("-fx-font-size: 15px;");
        submit.setOnAction(e -> adminPage(choice, window, scene, rse) );

        admin_layout.getChildren().addAll(admin_title, admin_stock_title, all_stocks, submit);

        // admin_layout = adminTab(window, scene, rse, admin_list);
        /*admin_layout = adminTab(scene, admin_list);
        admin.setContent(admin_layout);

        load.setOnAction(e -> {
            loadChoice = AlertBox.getAmount( "Load", "Enter the name of the file to load: ", false);
            try {
                load(loadChoice);
                exp_title.setText("");
                for (User ue : all_lists.keySet()) {  // set all lists to the changed value
                    all_lists.get(ue).clear();
                    all_lists.get(ue).addAll(rse.getUsersMap().get(ue.getUsername()).getUserStock());
                    // Stage 3 only
                    all_trans.get(ue).clear();
                    all_trans.get(ue).addAll(rse.getUsersMap().get(ue.getUsername()).getTransactions());
                    transTables.get(ue.getUsername()).setItems(all_trans.get(ue));
                    totals.get(ue).setText("Total Stock Value: " + ue.getTotalRevolution());
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
        scene.getStylesheets().add(temp.class.getResource("css/alert.css").toExternalForm());
        window.setScene(scene);
        window.showAndWait();
    }

    // public static VBox adminTab(Stage window, Scene scene, RSE rse, ObservableList<Stock> admin_list) {
    public VBox adminTab(Scene scene, ObservableList<Stock> admin_list) {
        String[] choice = {""};
        VBox admin_layout = new VBox(10);
        admin_layout.setAlignment(Pos.CENTER);
        admin_layout.setPadding(new Insets(10, 10, 10, 10));

        Label admin_title = new Label("Admin Page: ");
        Label admin_stock_title = new Label("Stock List: ");
        admin_stock_title.setStyle("-fx-text-fill: white;-fx-underline: true;-fx-font-size: 15px;-fx-text-alignment: center;");
        admin_title.setStyle("-fx-text-fill: white;-fx-underline: true;-fx-font-weight: bold; -fx-font-size: 20px;-fx-text-alignment: center;");
        admin_title.setAlignment(Pos.TOP_LEFT);

        String[] columns = {"companyName", "symbol", "rate", "totalDeals", "revolution", "quantity"};
        TableView<Stock> all_stocks = new TableView<>();

        if (admin_list.size() == 0)
            all_stocks.setPlaceholder(new Label("No rows to display"));
        else
            all_stocks.setItems(admin_list);

        all_stocks.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableView.TableViewSelectionModel<Stock> selectionModel = all_stocks.getSelectionModel();
        selectionModel.setSelectionMode(SelectionMode.SINGLE);
        selectionModel.select(0);
        choice[0] = selectionModel.getSelectedItems().get(0).getSymbol();
        all_stocks.setStyle("-fx-text-alignment: center;-fx-font-size: 15px;");
        for (String column : columns) {
            TableColumn<Stock, String> col = new TableColumn<>(column);
            // col.setMinWidth(200);
            col.prefWidthProperty().bind(all_stocks.widthProperty().multiply(1.0 / columns.length));
            col.setStyle( "-fx-alignment: CENTER;-fx-font-size: 13px;");
            col.setCellValueFactory(new PropertyValueFactory<>(column));
            all_stocks.getColumns().add(col);
        }

        ObservableList<Stock> selectedItems = selectionModel.getSelectedItems();
        selectedItems.addListener((ListChangeListener<Stock>) change -> {
            if (change != null && change.getList().size() != 0) {
                choice[0] = change.getList().get(0).getSymbol();
                System.out.println("Selection changed: " + change.getList());
            }
        });

        // Scene scene = new Scene(tabPane);

        Button submit = new Button("Investigate stock");
        submit.setStyle("-fx-font-size: 15px;");
        submit.setOnAction(e -> adminPage(choice[0], window, scene) );

        admin_layout.getChildren().addAll(admin_title, admin_stock_title, all_stocks, submit);
        return admin_layout;
    }

    // public static VBox userPage(Stage window, RSE rse, User user, Scene scene, Map<User, ObservableList<Stock>> all_lists, Map<User, ObservableList<Transaction>> all_trans, ObservableList<Stock> admin_list, Map<String, TableView<Transaction>> transTables, Map<User, Label> totals) {
    public VBox userPage(User user, Scene scene, Map<User, ObservableList<Stock>> all_lists, Map<User, ObservableList<Transaction>> all_trans, ObservableList<Stock> admin_list, Map<String, TableView<Transaction>> transTables, Map<User, Label> totals) {
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
                for (User ue : all_lists.keySet()) {  // set all lists to the changed value
                    all_lists.get(ue).clear();
                    all_lists.get(ue).addAll(rse.getUsersMap().get(ue.getUsername()).getUserStock());
                    // Stage 3 only
                    all_trans.get(ue).clear();
                    all_trans.get(ue).addAll(rse.getUsersMap().get(ue.getUsername()).getTransactions());
                    transTables.get(ue.getUsername()).setItems(all_trans.get(ue));
                    totals.get(ue).setText("Total Stock Value: " + ue.getTotalRevolution());
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

        ObservableList<Stock> list = FXCollections.observableArrayList(user.getUserStock());
        all_lists.put(user, list);
        String[] columns = {"symbol", "quantity", "rate"};
        TableView<Stock> user_stocks = new TableView<>();

        if (list.size() == 0)
            user_stocks.setPlaceholder(new Label("No rows to display"));
        else
            user_stocks.setItems(list);

        user_stocks.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableView.TableViewSelectionModel<Stock> selectionModel = user_stocks.getSelectionModel();
        selectionModel.setSelectionMode(SelectionMode.SINGLE);
        selectionModel.select(0);
        user_stocks.setStyle("-fx-text-alignment: center;-fx-font-size: 15px;");
        for (String column : columns) {
            TableColumn<Stock, String> col = new TableColumn<>(column);
            col.prefWidthProperty().bind(user_stocks.widthProperty().multiply(1.0 / columns.length));
            col.setStyle( "-fx-alignment: CENTER;-fx-font-size: 13px;");
            col.setCellValueFactory(new PropertyValueFactory<>(column));
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
                rse.accountCharge(user.getUsername(), amount);
                for (User ue : all_lists.keySet()) {  // set all lists to the changed value
                    all_trans.get(ue).clear();
                    all_trans.get(ue).addAll(rse.getUsersMap().get(ue.getUsername()).getTransactions());
                    transTables.get(ue.getUsername()).setItems(all_trans.get(ue));
                    totals.get(ue).setText("Total Stock Value: " + ue.getTotalRevolution());
                }
            } catch (NullPointerException | NumberFormatException ignore) { }
            catch (Exception ex) {
                AlertBox.displayMultiple("Alert", "Error in file: " + ex.getMessage());
            }
        });

        AnchorPane transLine = new AnchorPane();
        AnchorPane.setRightAnchor(addTrans, 0d);
        transLine.getChildren().addAll(transactionLabel, addTrans);

        String[] transColumns = {"symbol", "actionType", "transactionFee", "accountBalance", "time"};
        TableView<Transaction> userTrans = new TableView<>();
        ObservableList<Transaction> transList = FXCollections.observableArrayList(rse.getUsersMap().get(user.getUsername()).getTransactions());
        all_trans.put(user, transList);
        transTables.put(user.getUsername(), userTrans);

        if (transList.size() == 0)
            userTrans.setPlaceholder(new Label("No rows to display"));
        else
            userTrans.setItems(transList);

        userTrans.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        userTrans.setStyle("-fx-text-alignment: center;-fx-font-size: 15px;");
        for (String column : transColumns) {
            TableColumn<Transaction, String> col = new TableColumn<>(column);
            col.prefWidthProperty().bind(userTrans.widthProperty().multiply(1.0 / transColumns.length));
            col.setStyle( "-fx-alignment: CENTER;-fx-font-size: 13px;");
            col.setCellValueFactory(new PropertyValueFactory<>(column));
            userTrans.getColumns().add(col);
        }

        // inner layout
        HBox inner = new HBox(100);
        inner.setAlignment(Pos.CENTER);
        Label total_title = new Label("Total Stock Value: " + user.getTotalRevolution());
        total_title.setStyle("-fx-text-fill: white;-fx-font-weight: bold;-fx-font-size: 15px;");
        totals.put(user, total_title);
        // Label type_title = new Label(String.valueOf(user.getType()));  // stage 3
        Button trade = new Button("Trade");
        trade.setStyle("-fx-font-size: 15px;");
        trade.setOnAction(e -> {
            tradeOneWindow(user.getUsername(),scene, all_lists, admin_list, all_trans);
            for (User ur: all_trans.keySet()) {  // Stage 3 only
                transTables.get(ur.getUsername()).setItems(all_trans.get(ur));
                totals.get(ur).setText("Total Stock Value: " + ur.getTotalRevolution());
            }
        });
        Button loadBtn = new Button("Load Xml");
        loadBtn.setOnAction(e -> {
            try {
                Main.loadXmlFile(rse, window, user.getUsername());
            } catch (NullPointerException ignore) {
            } catch (Exception ex) {
                AlertBox.displayMultiple("Alert", ex.getMessage());
            }
        });
        inner.getChildren().addAll(total_title, trade, loadBtn);
        user_layout.getChildren().addAll(title, stockLine, user_stocks, transLine, userTrans, inner);
        return user_layout;
    }

    public void Trade(String username) {
        String[] trade = {"", "", "", "", ""};
        int index = 0;
        String choice = "";
        User user = rse.getUser(username);
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setMinWidth(350);
        window.setMinHeight(350);

        do {
            if (index == 0)
                choice = AlertBox.askQuestion(window, "Trade", "Action", "Sell", "Buy", "Back");
            else if (index == 1) {
                ObservableList<Stock> list;
                if (trade[0].equals("Sell"))
                    list = FXCollections.observableArrayList(user.getUserStock());  // getStocks
                else
                    list = FXCollections.observableArrayList(rse.getStocks());
                choice = AlertBox.showStockTable(window, "Trade", "Stocks: ", list, "companyName", "symbol", "rate", "quantity");
            }
            else if (index == 2) {
                choice = AlertBox.askQuestion(window, "Trade", "Command", "LMT", "MKT", "FOK", "IOC", "Back");
                if (choice.equals("MKT"))
                    trade[4] =  rse.getRate(trade[1]);
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
        String out = "";
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
        AlertBox.displayMultiple("Result", out);
    }

    // public static void tradeOneWindow(Stage window, String username, RSE rse, final Scene prevScene, Map<User, ObservableList<Stock>> all_lists, ObservableList<Stock> admin_list, Map<User, ObservableList<Transaction>> all_trans) {
    public void tradeOneWindow(String username, final Scene prevScene, Map<User, ObservableList<Stock>> all_lists, ObservableList<Stock> admin_list, Map<User, ObservableList<Transaction>> all_trans) {
        String[] tradeChoice = {"", "", "", "", ""};

        User user = rse.getUser(username);
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

        ArrayList<Stock> userStocksList = user.getUserStock();
        ObservableList<Stock> list;
        if (tradeChoice[0].equals("Sell"))
            list = FXCollections.observableArrayList(userStocksList);  // userStocksList
        else
            list = FXCollections.observableArrayList(rse.getStocks());
        String[] columns = {"companyName", "symbol", "rate", "quantity"};
        TableView<Stock> tableStocks = new TableView<>();
        tableStocks.setStyle("-fx-font-size: 15px;");
        if (list.size() == 0)
            tableStocks.setPlaceholder(new Label("No rows to display"));
        else
            tableStocks.setItems(list);

        tableStocks.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableView.TableViewSelectionModel<Stock> selectionModel = tableStocks.getSelectionModel();
        selectionModel.setSelectionMode(SelectionMode.SINGLE);
        selectionModel.select(0);

        actionChoice.setOnAction(e -> {
            tradeChoice[0] = actionChoice.getValue();
            ObservableList<Stock> l;
            if (tradeChoice[0].equals("Buy"))
                l = FXCollections.observableArrayList(rse.getStocks());
            else
                l = FXCollections.observableArrayList(user.getUserStock());
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
            TableColumn<Stock, String> col = new TableColumn<>(column);
            col.setMinWidth(200);
            // col.prefWidthProperty().bind(tableStocks.widthProperty().multiply(2.0 / columns.length));
            col.setStyle( "-fx-alignment: CENTER;-fx-font-size: 13px;");
            col.setCellValueFactory(new PropertyValueFactory<>(column));
            tableStocks.getColumns().add(col);
        }

        ObservableList<Stock> selectedItems = selectionModel.getSelectedItems();
        selectedItems.addListener((ListChangeListener<Stock>) change -> {
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
        back.setStyle("-fx-background-color: white;");  // transparent
        // back.setStyle("-fx-font-size: 15px;");
        back.setOnAction(e -> {
            if (prevScene == null)
                window.close();
            else
                window.setScene(prevScene);
            for (User ue : all_lists.keySet()) {  // set all lists to the changed value
                all_lists.get(ue).clear();
                all_lists.get(ue).addAll(rse.getUsersMap().get(ue.getUsername()).getUserStock());
            }
            for (User ue : all_trans.keySet()) {   // Stage 3 only
                all_trans.get(ue).clear();
                all_trans.get(ue).addAll(rse.getUsersMap().get(ue.getUsername()).getTransactions());
            }
            admin_list.clear();
            admin_list.addAll(rse.getStocks());
        });

        Button submit = new Button("Submit");
        submit.setStyle("-fx-font-size: 15px;");
        submit.setOnAction(e -> {
            String out = "";
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
            AlertBox.displayMultiple("Result", out);
            userStocksList.clear();
            userStocksList.addAll(user.getUserStock());
            for (User ue : all_lists.keySet()) {  // set all lists to the changed value
                all_lists.get(ue).clear();
                all_lists.get(ue).addAll(rse.getUsersMap().get(ue.getUsername()).getUserStock());
            }
            for (User ue : all_trans.keySet()) {   // Stage 3 only
                all_trans.get(ue).clear();
                all_trans.get(ue).addAll(rse.getUsersMap().get(ue.getUsername()).getTransactions());
            }
            admin_list.clear();
            admin_list.addAll(rse.getStocks());
            ObservableList<Stock> l;
            if (tradeChoice[0].equals("Buy"))
                l = FXCollections.observableArrayList(rse.getStocks());
            else
                l = FXCollections.observableArrayList(user.getUserStock());
            list.clear();
            list.addAll(l);
            amountField.setText("");
            rateField.setText("");
        });

        HBox inner = new HBox(30);
        inner.setAlignment(Pos.CENTER);
        inner.getChildren().addAll(submit);  // , back

        GridPane.setConstraints(actionChoice, 1, 1);
        GridPane.setConstraints(tableStocks, 1, 2);
        GridPane.setConstraints(commandChoice, 1, 3);
        GridPane.setConstraints(amountField, 1, 4);
        GridPane.setConstraints(rateField, 1, 5);
        GridPane.setConstraints(inner, 1, 6);
        GridPane.setConstraints(back, 0, 0);

        layout.getChildren().addAll(title, action, stocks, command, amount, rate, actionChoice, tableStocks, commandChoice, amountField, rateField, inner, back); // submit, back

        Scene scene = new Scene(layout); // 800, 600
        window.setScene(scene);
        scene.getStylesheets().add(temp.class.getResource("css/trade.css").toExternalForm());
    }

    // public static void adminPage(String stockName, Stage stage, Scene prev, RSE rse) {
    public void adminPage(String stockName, Stage stage, Scene prev) {
        String[] columns = {"action", "symbol", "time", "amount", "revolution", "rate", "publisherName"};

        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(10, 10, 10, 10));

        Label title = new Label(stockName);
        title.setStyle("-fx-text-fill: white;-fx-font-size: 20px;");

        Label Buy_title = new Label("Buy list: ");
        Buy_title.setStyle("-fx-underline: true; -fx-font-size: 15px;-fx-text-alignment: right; -fx-text-fill: white;");
        Buy_title.setAlignment(Pos.TOP_LEFT);
        ObservableList<Deal> buy_list = FXCollections.observableArrayList(rse.getList(stockName, "Buy"));
        TableView<Deal> buy_deals = new TableView<>();
        if (buy_list.size() == 0)
            buy_deals.setPlaceholder(new Label("No rows to display"));
        else
            buy_deals.setItems(buy_list);

        buy_deals.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        buy_deals.setStyle("-fx-text-alignment: center;-fx-font-size: 15px;");
        for (String column : columns) {
            TableColumn<Deal, String> col = new TableColumn<>(column);
            // col.setMinWidth(120);
            col.prefWidthProperty().bind(buy_deals.widthProperty().multiply(1.0 / columns.length));
            col.setStyle( "-fx-alignment: CENTER;-fx-font-size: 13px;");
            col.setCellValueFactory(new PropertyValueFactory<>(column));
            buy_deals.getColumns().add(col);
        }

        Label Sell_title = new Label("Sell List: ");
        Sell_title.setStyle("-fx-underline: true; -fx-font-size: 15px;-fx-text-alignment: right; -fx-text-fill: white;");
        Sell_title.setAlignment(Pos.TOP_LEFT);
        ObservableList<Deal> sell_list = FXCollections.observableArrayList(rse.getList(stockName, "Sell"));
        TableView<Deal> sell_deals = new TableView<>();
        if (sell_list.size() == 0)
            sell_deals.setPlaceholder(new Label("No rows to display"));
        else
            sell_deals.setItems(sell_list);

        sell_deals.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        sell_deals.setStyle("-fx-text-alignment: center;-fx-font-size: 15px;");
        for (String column : columns) {
            TableColumn<Deal, String> col = new TableColumn<>(column);
            // col.setMinWidth(120);
            col.prefWidthProperty().bind(sell_deals.widthProperty().multiply(1.0 / columns.length));
            col.setStyle( "-fx-alignment: CENTER;-fx-font-size: 13px;");
            col.setCellValueFactory(new PropertyValueFactory<>(column));
            sell_deals.getColumns().add(col);
        }

        Label Approved_title = new Label("Approved List: ");
        Approved_title.setStyle("-fx-underline: true; -fx-font-size: 15px;-fx-text-alignment: right; -fx-text-fill: white;");
        Approved_title.setAlignment(Pos.TOP_LEFT);
        ObservableList<Deal> approved_list = FXCollections.observableArrayList(rse.getList(stockName, "Approved"));
        TableView<Deal> approved_deals = new TableView<>();
        if (approved_list.size() == 0)
            approved_deals.setPlaceholder(new Label("No rows to display"));
        else
            approved_deals.setItems(approved_list);

        approved_deals.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        approved_deals.setStyle("-fx-text-alignment: center;-fx-font-size: 15px;");
        for (String column : columns) {
            TableColumn<Deal, String> col = new TableColumn<>(column);
            // col.setMinWidth(120);
            col.prefWidthProperty().bind(approved_deals.widthProperty().multiply(1.0 / columns.length));
            col.setStyle( "-fx-alignment: CENTER;-fx-font-size: 13px;");
            col.setCellValueFactory(new PropertyValueFactory<>(column));
            approved_deals.getColumns().add(col);
        }

        Scene scene = new Scene(layout);
        HBox inner = new HBox(30);
        inner.setAlignment(Pos.CENTER);

        Button graphBtn = new Button("Show Graph");
        graphBtn.setStyle("-fx-font-weight: bold;-fx-font-size: 15px;");
        graphBtn.setOnAction(e -> temp.graph(stockName, stage, scene, rse));

        Button back = new Button("back");
        /*back.setPrefSize(10, 10);
        ImageView im = new ImageView(new Image("com/ui/backArrow.png"));
        im.setFitHeight(20);
        im.setFitWidth(30);
        back.setGraphic(im);
        back.setStyle("-fx-background-color: transparent;");
        back.setStyle("-fx-font-size: 15px;");
        back.setOnAction(e -> stage.setScene(prev));

        inner.getChildren().addAll(graphBtn, back);

        layout.getChildren().addAll(title, Buy_title, buy_deals, Sell_title, sell_deals, Approved_title, approved_deals, inner);
        scene.getStylesheets().add(temp.class.getResource("css/admin.css").toExternalForm());
        stage.setScene(scene);
        // stage.showAndWait();
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

        xAxis.setTickLabelFill(Color.RED);
        yAxis.setTickLabelFill(Color.WHITE);


        //  Creating the Area chart
        AreaChart areaChart = new AreaChart(xAxis, yAxis);
        areaChart.setMinSize(window.getWidth() - 100, window.getHeight() - 100);
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

        Scene scene = new Scene(layout);  // 800, 600
        window.setTitle(stockName + " graph:");
        scene.getStylesheets().add(temp.class.getResource("css/graph.css").toExternalForm());
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
        //HBox layoutCompany = new HBox(20);
        Label companyNameTitle = new Label("Company Name: ");
        TextField companyName = new TextField();
        //layoutCompany.getChildren().addAll(companyNameTitle, companyName);

        //HBox layoutSymbol = new HBox(20);
        Label symbolTitle = new Label("Stock Symbol: ");
        TextField stockSymbol= new TextField();
        //layoutSymbol.getChildren().addAll(symbolTitle, stockSymbol);

        //HBox  layoutAmount = new HBox(20);
        Label amountTitle = new Label("Stock shares amount: ");
        TextField stockAmount = new TextField();
        //layoutAmount.getChildren().addAll(amountTitle, stockAmount);

        //HBox layoutRate = new HBox(20);
        Label stockRateTitle = new Label("Stock Total rate: ");
        TextField stockRate = new TextField();
        //layoutRate.getChildren().addAll(stockRateTitle, stockRate);

        Label exp = new Label("");
        Button submit = new Button("create");
        submit.setOnAction(e -> {
            try {
                int amount = Integer.parseInt(stockAmount.getText());
                int rate = Integer.parseInt(stockRate.getText()) / amount;
                rse.userCreatedStock(username, companyName.getText(), stockSymbol.getText(), amount, rate);
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

        Scene scene = new Scene(layout);
        scene.getStylesheets().add(temp.class.getResource("css/alert2.css").toExternalForm());
        window.setScene(scene);
        window.showAndWait();
    }

    private void save(String fileName) throws IOException {
        /* save the current system to a binary file
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName))) {
            out.writeObject(rse);
            out.flush();
        }
    }

    private void load(String fileName) throws IOException, ClassNotFoundException {
        /* load the system from a file
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName))) {
            this.rse = (RSE) in.readObject();
        }
    }

    private static String capital(String name) { return ("" + name.charAt(0)).toUpperCase() + name.substring(1); }
}
*/