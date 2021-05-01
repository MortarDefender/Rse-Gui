package com.design.controller;

import com.design.Context;
import com.ui.AlertBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import objects.StockDTO;
import objects.TransactionDTO;
import objects.UserDTO;

import java.net.URL;

public class UserController {

    @FXML private TableView<StockDTO> stockTable;
    @FXML private TableView<TransactionDTO> transactionTable;

    private UserDTO user;

    public void setUser(UserDTO user) {
        this.user = user;
        this.initialize();
    }

    @FXML
    public void initialize() {
        ObservableList<StockDTO> list = FXCollections.observableArrayList(Context.rse.getStocks(user.getUsername()));
        String[] columns = {"symbol", "quantity", "rate"};

        if (list.size() == 0)
            stockTable.setPlaceholder(new Label("No rows to display"));
        else
            stockTable.setItems(list);

        stockTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableView.TableViewSelectionModel<StockDTO> selectionModel = stockTable.getSelectionModel();
        selectionModel.setSelectionMode(SelectionMode.SINGLE);
        selectionModel.select(0);
        stockTable.setStyle("-fx-text-alignment: center;-fx-font-size: 15px;");
        for (String column : columns) {
            TableColumn<StockDTO, String> col = new TableColumn<>(column);
            col.prefWidthProperty().bind(stockTable.widthProperty().multiply(1.0 / columns.length));
            col.setStyle( "-fx-alignment: CENTER;-fx-font-size: 13px;");
            col.setCellValueFactory(new PropertyValueFactory<>(column));
            // col.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getSymbol()));  // -> Using Interface
            stockTable.getColumns().add(col);
        }

        String[] transColumns = {"time", "symbol", "actionType", "sum", "accountBefore", "accountAfter"};
        ObservableList<TransactionDTO> transList = FXCollections.observableArrayList(user.getTransactions());

        if (transList.size() == 0)
            transactionTable.setPlaceholder(new Label("No rows to display"));
        else
            transactionTable.setItems(transList);

        transactionTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        transactionTable.setStyle("-fx-text-alignment: center;-fx-font-size: 15px;");
        for (String column : transColumns) {
            TableColumn<TransactionDTO, String> col = new TableColumn<>(column);
            col.prefWidthProperty().bind(transactionTable.widthProperty().multiply(1.0 / transColumns.length));
            col.setStyle( "-fx-alignment: CENTER;-fx-font-size: 13px;");
            col.setCellValueFactory(new PropertyValueFactory<>(column));
            transactionTable.getColumns().add(col);
        }
    }

    @FXML
    private void addStock() {
        try {
            /*addUserStock(user.getUsername(), rse);
            for (UserDTO ue : all_lists.keySet()) {  // set all lists to the changed value
                all_lists.get(ue).clear();
                all_lists.get(ue).addAll(Context.rse.getStocks(ue.getUsername()));
                // Stage 3 only
                all_trans.get(ue).clear();
                all_trans.get(ue).addAll(Context.rse.getTransactions(ue.getUsername()));
                transTables.get(ue.getUsername()).setItems(all_trans.get(ue));
                totals.get(ue).setText("Total Stock Value: " + ue.getRevolution());
            }
            admin_list.clear();
            admin_list.addAll(rse.getStocks());*/
        } catch (NullPointerException ignore) { }
        catch (Exception ex) {
            AlertBox.displayMultiple("Alert", "Error in file: " + ex.getMessage());
        }
    }

    @FXML
    private void addCharge() {
        try {
            int amount = Integer.parseInt(AlertBox.getAmount( "Account Charge", "Enter the amount to charge your account: ", true));
            System.out.println("self charge: " + amount);
            Context.rse.addAccountCharge(user.getUsername(), amount);
            /*for (UserDTO ue : all_lists.keySet()) {  // set all lists to the changed value
                all_trans.get(ue).clear();
                all_trans.get(ue).addAll(Context.rse.getTransactions(ue.getUsername()));
                transTables.get(ue.getUsername()).setItems(all_trans.get(ue));
                totals.get(ue).setText("Total Stock Value: " + ue.getRevolution());
            }*/
        } catch (NullPointerException | NumberFormatException err) {
            System.out.println(err.getMessage());
        }
        catch (Exception ex) {
            AlertBox.displayMultiple("Alert", "Error in file: " + ex.getMessage());
        }
    }
}
