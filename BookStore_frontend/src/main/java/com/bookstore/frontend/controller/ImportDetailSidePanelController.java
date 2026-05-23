package com.bookstore.frontend.controller;
import com.bookstore.frontend.model.ImportDetailModel;
import com.bookstore.frontend.model.ImportModel;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class ImportDetailSidePanelController {

    @FXML private VBox sidePanelRoot;
    @FXML private Label lblImportId;
    @FXML private Label lblStaff;
    @FXML private Label lblDate;
    @FXML private Label lblTotalCost;

    @FXML private TableView<ImportDetailModel> tvDetails;
    @FXML private TableColumn<ImportDetailModel, String> colTitle;
    @FXML private TableColumn<ImportDetailModel, Integer> colQty;
    @FXML private TableColumn<ImportDetailModel, Double> colPrice;
    @FXML private TableColumn<ImportDetailModel, Double> colTotal;

    private boolean isShowing = false;

    @FXML
    public void initialize() {
        // Ánh xạ dữ liệu cột
        colTitle.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("importPrice"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("lineTotal"));

        // Format cột tiền tệ
        colPrice.setCellFactory(tc -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) setText(null);
                else setText(String.format("$%.2f", price));
            }
        });

        colTotal.setCellFactory(tc -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(Double total, boolean empty) {
                super.updateItem(total, empty);
                if (empty || total == null) setText(null);
                else {
                    setText(String.format("$%.2f", total));
                    setStyle("-fx-text-fill: #FFC107; -fx-font-weight: bold; -fx-alignment: center-right;");
                }
            }
        });
    }

    // Nhận dữ liệu từ API và trượt Panel ra
    public void setImportDataAndShow(ImportModel data) {
        lblImportId.setText("#IMP-" + data.getId());
        lblStaff.setText(data.getStaffUsername());
        lblDate.setText(data.getImportDate());
        lblTotalCost.setText(String.format("$%.2f", data.getTotalCost()));

        tvDetails.setItems(data.getDetails());

        if (!isShowing) {
            slidePanel(0);
            isShowing = true;
        }
    }

    @FXML
    public void handleClose() {
        if (isShowing) {
            slidePanel(sidePanelRoot.getPrefWidth());
            isShowing = false;
        }
    }

    private void slidePanel(double targetX) {
        TranslateTransition transition = new TranslateTransition(Duration.millis(300), sidePanelRoot);
        transition.setToX(targetX);
        transition.play();
    }
}