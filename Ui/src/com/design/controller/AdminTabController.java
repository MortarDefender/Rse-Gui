package com.design.controller;

import com.design.Context;
import com.rse.Stock;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import objects.StockDTO;


public class AdminTabController {

    @FXML private TableView<StockDTO> allStocksTable;
    @FXML private Button submit;

    private final String[] choice = {""};

    @FXML
    public void initialize() {
        String[] columns = {"companyName", "symbol", "quantity", "rate", "totalDeals", "revolution"};
        ObservableList<StockDTO> admin_list = FXCollections.observableArrayList(Context.rse.getStocks());
        if (admin_list.size() == 0)
            allStocksTable.setPlaceholder(new Label("No rows to display"));
        else
            allStocksTable.setItems(admin_list);

        allStocksTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableView.TableViewSelectionModel<StockDTO> selectionModel = allStocksTable.getSelectionModel();
        selectionModel.setSelectionMode(SelectionMode.SINGLE);
        selectionModel.select(0);
        choice[0] = selectionModel.getSelectedItems().get(0).getSymbol();
        allStocksTable.setStyle("-fx-text-alignment: center;-fx-font-size: 15px;");
        for (String column : columns) {
            TableColumn<StockDTO, String> col = new TableColumn<>(column);
            col.prefWidthProperty().bind(allStocksTable.widthProperty().multiply(1.0 / columns.length));
            col.setStyle( "-fx-alignment: CENTER;-fx-font-size: 13px;");
            col.setCellValueFactory(new PropertyValueFactory<>(column));
            allStocksTable.getColumns().add(col);
        }

        ObservableList<StockDTO> selectedItems = selectionModel.getSelectedItems();
        selectedItems.addListener((ListChangeListener<StockDTO>) change -> {
            if (change != null && change.getList().size() != 0) {
                choice[0] = change.getList().get(0).getSymbol();
                System.out.println("Selection changed: " + change.getList());
            }
        });
    }

    @FXML
    private void selectRow(MouseEvent click) {
        StockDTO selectedItem = allStocksTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            choice[0] = selectedItem.getSymbol();
            System.out.println("Selection changed: " + selectedItem);
        }
        // go to Admin Page with the stock selected
    }

    @FXML
    private void adminPage() {
        System.out.println("admin page " + choice[0]);
    }

}
