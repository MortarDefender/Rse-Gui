package com.ui;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import objects.DealDTO;
import objects.StockDTO;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;

public class AlertBox {
    static String btnChoice;

    private static Stage getWindow() {
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setMinWidth(350);
        window.setMinHeight(350);
        window.getIcons().add(new Image("com/ui/css/icon.jpg"));
        return window;
    }

    public static void displayMultiple(String title, String ... messages) {
        displayMultiple(getWindow(), title, messages);
    }
    public static void displayMultiple(Stage window, String title, String ... messages) {
        window.setTitle(title);

        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);

        HBox innerLayout = new HBox(30);
        innerLayout.setAlignment(Pos.CENTER);

        for (String item: messages) {
            Label label = new Label();
            label.setText(item);
            label.setStyle("-fx-text-fill: white;-fx-font-size: 15px");
            innerLayout.getChildren().add(label);
        }
        layout.getChildren().add(innerLayout);
        Button btn = new Button("Close");
        btn.setOnAction(e -> window.close());
        layout.getChildren().add(btn);

        Scene scene = new Scene(layout);
        scene.getStylesheets().add(AlertBox.class.getResource("css/alert.css").toExternalForm());
        window.setScene(scene);
        window.showAndWait();
    }

    public static String display(String title, String message, String backBtn, String submitBtn) {
        return display(getWindow(), title, message, backBtn, submitBtn);
    }
    public static String display(Stage window, String title, String message, String backBtn, String submitBtn) {
        window.setTitle(title);

        Label label = new Label();
        label.setText(message);
        label.setStyle("-fx-text-fill: white;");
        Button back = new Button(backBtn);
        back.setOnAction(e -> {
            btnChoice = back.getText();
            window.close();
        });

        Button submit = new Button(submitBtn);
        submit.setOnAction(e -> {
            btnChoice = submit.getText();
            window.close();
        });

        VBox layout = new VBox(10);
        HBox innerLayout = new HBox(30);
        innerLayout.setAlignment(Pos.CENTER);
        innerLayout.getChildren().addAll(back, submit);

        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(label, innerLayout);
        Scene scene = new Scene(layout);
        scene.getStylesheets().add(AlertBox.class.getResource("css/alert.css").toExternalForm()); //"alert.css");
        window.setScene(scene);
        window.showAndWait();

        return btnChoice;
    }

    public static String askQuestion(String pageTitle, String title, String ... names) {
        return askQuestion(getWindow(), pageTitle, title, names);
    }
    public static String askQuestion(Stage window, String pageTitle, String title, String ... names) {
        int index = 0;
        window.setTitle(pageTitle);

        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);

        Label label = new Label();
        label.setText(title);
        label.setStyle("-fx-text-fill: white;");
        layout.getChildren().add(label);

        HBox innerLayout = new HBox(30);
        innerLayout.setAlignment(Pos.CENTER);
        for (String item: names) {
            if (index != 0 && index % 2 == 0) {
                layout.getChildren().add(innerLayout);
                innerLayout = new HBox(30);
                innerLayout.setAlignment(Pos.CENTER);
            }
            Button btn = new Button(item);
            btn.setOnAction(e -> {
                btnChoice = btn.getText();
                System.out.println("choice in: " + btnChoice);
                window.close();
            });
            innerLayout.getChildren().add(btn);
            index++;
        }
        layout.getChildren().add(innerLayout);

        Scene scene = new Scene(layout);

        scene.getStylesheets().add(AlertBox.class.getResource("css/alert.css").toExternalForm()); //"alert.css");
        window.setScene(scene);
        window.showAndWait();

        System.out.println("choice: " + btnChoice);
        return btnChoice;
    }

    public static String getAmount(String pageTitle, String title, boolean type) {
        return getAmount(getWindow(), pageTitle, title, type);
    }
    public static String getAmount(Stage window, String pageTitle, String title, boolean type) {
        window.setTitle(pageTitle);

        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);

        Label label1 = new Label(title);
        label1.setStyle("-fx-text-fill: white;-fx-font-size: 20px");
        TextField textField = new TextField();
        textField.setStyle("-fx-max-width: 250px");

        layout.getChildren().addAll(label1, textField);

        Label text = new Label("There is no number with a digit in it. please enter a number");
        text.setStyle("-fx-text-fill: white;-fx-font-size: 20px;");
        textField.setOnAction(e -> {
            try {
                if (type) {
                    int amount = Integer.parseInt(textField.getText());
                }
                btnChoice = textField.getText();
                window.close();
            } catch (NumberFormatException ignore) {
                System.out.println(layout.getChildren());
                if (!layout.getChildren().contains(text))
                    layout.getChildren().add(text);
            }
        });


        Button back = new Button("Back");
        back.setOnAction(e -> {
            btnChoice = back.getText();
            window.close();
        });

        Button submit = new Button("Submit");
        submit.setOnAction(e -> {
            try {
                if (type) {
                    int amount = Integer.parseInt(textField.getText());
                }
                btnChoice = textField.getText();
                window.close();
            } catch (NumberFormatException ignore) {
                System.out.println(layout.getChildren());
                if (!layout.getChildren().contains(text))
                    layout.getChildren().add(text);
            }
        });

        HBox innerLayout = new HBox(30);
        innerLayout.setAlignment(Pos.CENTER);
        innerLayout.getChildren().addAll(back, submit);

        layout.getChildren().add(innerLayout);

        Scene scene = new Scene(layout);
        scene.getStylesheets().add(AlertBox.class.getResource("css/alert.css").toExternalForm()); //"alert.css");
        window.setScene(scene);
        window.showAndWait();

        return btnChoice;
    }

    public static String showStockTable(String pageTitle, String title, ObservableList<StockDTO> list, String ... columns) {
        return AlertBox.showStockTable(getWindow(), pageTitle, title, list, columns);
    }
    public static String showStockTable(Stage window, String pageTitle, String title, ObservableList<StockDTO> list, String ... columns) {
        window.setTitle(pageTitle);

        Label label = new Label();
        label.setStyle("-fx-text-fill: white;");
        label.setText(title);

        TableView<StockDTO> table = new TableView<>();
        if (list.size() == 0)
            table.setPlaceholder(new Label("No rows to display"));
        else
            table.setItems(list);

        TableView.TableViewSelectionModel<StockDTO> selectionModel = table.getSelectionModel();
        selectionModel.setSelectionMode(SelectionMode.SINGLE);
        selectionModel.select(0);
        btnChoice = selectionModel.getSelectedItems().get(0).getSymbol();
        for (String column : columns) {
            TableColumn<StockDTO, String> col = new TableColumn<>(column);
            // col.setMinWidth(200);
            col.prefWidthProperty().bind(table.widthProperty().multiply(1.0 / columns.length));
            col.setCellValueFactory(new PropertyValueFactory<>(column));
            table.getColumns().add(col);
        }

        ObservableList<StockDTO> selectedItems = selectionModel.getSelectedItems();

        selectedItems.addListener((ListChangeListener<StockDTO>) change -> {
            btnChoice = change.getList().get(0).getSymbol();
            System.out.println("Selection changed: " + change.getList());
        });

        Button back = new Button("Back");
        back.setOnAction(e -> {
            btnChoice = back.getText();
            window.close();
        });

        Button submit = new Button("Submit");
        submit.setOnAction(e -> window.close() );

        HBox innerLayout = new HBox(30);
        innerLayout.setAlignment(Pos.CENTER);
        innerLayout.getChildren().addAll(back, submit);

        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(label, table, innerLayout);

        Scene scene = new Scene(layout);
        scene.getStylesheets().add(AlertBox.class.getResource("css/alert.css").toExternalForm()); //"alert.css");
        window.setScene(scene);
        window.showAndWait();

        return btnChoice;
    }

    public static void displayTable(String title, List<DealDTO> deals) {
        Stage window = getWindow();
        window.setTitle("Trade Results:");
        window.setMinWidth(1000);
        window.setMinHeight(600);

        Label label = new Label(title + ":");
        label.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-underline: true; -fx-font-weight: bold;");

        ObservableList<DealDTO> list = FXCollections.observableArrayList(deals);
        TableView<DealDTO> table = new TableView<>();
        if (list.size() == 0)
            table.setPlaceholder(new Label("No rows to display"));
        else
            table.setItems(list);

        String[] columns = {"time", "action", "symbol", "rate", "amount", "revolution", "publisherName", "status"};
        for (String column : columns) {
            TableColumn<DealDTO, String> col = new TableColumn<>(column);
            col.prefWidthProperty().bind(table.widthProperty().multiply(1.0 / columns.length));
            col.setStyle( "-fx-alignment: CENTER;-fx-font-size: 13px;");
            col.setCellValueFactory(new PropertyValueFactory<>(column));
            table.getColumns().add(col);
        }

        Button back = new Button("OK");
        back.setOnAction(e -> window.close());

        VBox layout = new VBox(10);
        label.setPadding(new Insets(10, 10, 10, 10));
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(label, table, back);

        Scene scene = new Scene(layout);
        scene.getStylesheets().add(AlertBox.class.getResource("css/alert.css").toExternalForm());
        window.setScene(scene);
        window.showAndWait();
    }
}
