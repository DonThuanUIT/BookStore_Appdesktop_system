package com.bookstore.frontend.controller;

import com.bookstore.frontend.interactor.CartInteractor;
import com.bookstore.frontend.model.dto.CartItemDTO;
import com.bookstore.frontend.model.CartModel;
import com.bookstore.frontend.util.CartStore;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.io.File;

public class CartController extends BaseController {
    @FXML private VBox cartItemsContainer;
    @FXML private Label lblTotalPrice;

    // Các UI mới cho phần gửi ảnh minh chứng
    private Label lblFileName;
    private File selectedProofFile;

    private Stage paymentStage;
    private double xOffset = 0;
    private double yOffset = 0;
    private final CartModel model;
    private final CartInteractor interactor;

    public CartController() {
        this.model = CartStore.getInstance().getModel();
        this.interactor = new CartInteractor(this.model);
    }

    @FXML
    public void initialize() {
        if (lblTotalPrice != null) {
            updateTotalPriceLabel(model.totalPriceProperty().get());
            model.totalPriceProperty().addListener((obs, oldVal, newVal) -> updateTotalPriceLabel(newVal.doubleValue()));
        }
        if (cartItemsContainer != null) {
            model.getItems().addListener((javafx.collections.ListChangeListener<CartItemDTO>) c -> {
                while (c.next()) { }
                renderCart();
            });
        }
    }

    private void updateTotalPriceLabel(double value) {
        lblTotalPrice.setText(String.format("$%.2f", value));
    }

    @Override public void onNavigate(Object data) { interactor.loadCartItems(); renderCart(); }

    private void renderCart() {
        cartItemsContainer.getChildren().clear();
        for (CartItemDTO item : model.getItems()) {
            cartItemsContainer.getChildren().add(createCartItemRow(item));
        }
    }

    private HBox createCartItemRow(CartItemDTO item) {
        HBox row = new HBox(24);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 14; -fx-padding: 16 20; -fx-border-color: #ececec; -fx-border-radius: 14;");

        ImageView cover = new ImageView(new Image(item.getBook().getImageUrl(), true));
        cover.setFitWidth(80); cover.setPreserveRatio(true);

        VBox info = new VBox(new Label(item.getBook().getTitle()), new Label("$" + item.getBook().getPrice()));
        HBox.setHgrow(info, Priority.ALWAYS);

        Spinner<Integer> spin = new Spinner<>(1, 100, item.getQuantity());
        spin.valueProperty().addListener((obs, ov, nv) -> item.setQuantity(nv));

        Button del = new Button("Xóa");
        del.setOnAction(e -> CartStore.getInstance().removeItem(item));

        row.getChildren().addAll(cover, info, spin, del);
        return row;
    }

    @FXML
    private void handleCheckout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/bookstore/frontend/view/PaymentMethodView.fxml"));
            VBox root = loader.load();
            paymentStage = new Stage(StageStyle.TRANSPARENT);
            paymentStage.initModality(Modality.APPLICATION_MODAL);

            HBox rowBank = (HBox) root.lookup("#methodBank");
            VBox detBank = (VBox) root.lookup("#detailsBank");
            ImageView checkBank = (ImageView) root.lookup("#checkBank");

            // Tìm các node mới để gán sự kiện (đảm bảo file FXML đã có fx:id hoặc ID tương ứng)
            lblFileName = (Label) root.lookup("#lblFileName");
            Button btnSelectProof = (Button) root.lookup("#btnSelectProof");
            Button btnConfirm = (Button) root.lookup("#btnConfirmPayment");

            rowBank.setOnMouseClicked(e -> {
                detBank.setVisible(true);
                detBank.setManaged(true);
                checkBank.setVisible(true);
                paymentStage.sizeToScene();
            });

            if (btnSelectProof != null) {
                btnSelectProof.setOnAction(e -> handleSelectProofImage());
            }

            if (btnConfirm != null) {
                btnConfirm.setOnAction(e -> handleManualConfirm());
            }

            root.setOnMousePressed(e -> { xOffset = e.getSceneX(); yOffset = e.getSceneY(); });
            root.setOnMouseDragged(e -> { paymentStage.setX(e.getScreenX() - xOffset); paymentStage.setY(e.getScreenY() - yOffset); });

            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            paymentStage.setScene(scene);
            paymentStage.showAndWait();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // Hàm chọn ảnh minh chứng từ máy tính
    private void handleSelectProofImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn ảnh minh chứng chuyển khoản");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        selectedProofFile = fileChooser.showOpenDialog(paymentStage);
        if (selectedProofFile != null && lblFileName != null) {
            lblFileName.setText(selectedProofFile.getName());
            lblFileName.setStyle("-fx-text-fill: #00ff00;"); // Chuyển xanh để báo đã chọn thành công
        }
    }

    @FXML
    private void handleManualConfirm() {
        if (selectedProofFile == null) {
            Alert warning = new Alert(Alert.AlertType.WARNING, "Vui lòng chọn ảnh minh chứng!");
            warning.showAndWait();
            return;
        }

        String orderId = "DH24521882" + (System.currentTimeMillis() % 1000);
        double totalAmount = model.totalPriceProperty().get();

        // 1. Gọi interactor để lưu đơn hàng vào database
        boolean isSaved = interactor.saveOrder(orderId, totalAmount, "PENDING", selectedProofFile.getAbsolutePath());

        if (isSaved) {
            Platform.runLater(() -> {
                Alert a = new Alert(Alert.AlertType.INFORMATION, "Đã nhận được minh chứng. Cảm ơn Trung!");
                a.showAndWait();

                // 2. Gọi hàm clear trực tiếp từ model bạn đã khởi tạo trong constructor
                model.clearCart();

                if (paymentStage != null) paymentStage.close();
            });
        }
    }

    @FXML
    private void handleZoomQR() {
        Stage zoom = new Stage(StageStyle.TRANSPARENT);
        ImageView bigQR = new ImageView(new Image(getClass().getResourceAsStream("/com/bookstore/frontend/image/QR personal.jpg")));
        bigQR.setFitWidth(450); bigQR.setPreserveRatio(true);
        bigQR.setOnMouseClicked(e -> zoom.close());

        StackPane sp = new StackPane(bigQR);
        sp.setStyle("-fx-background-color: rgba(0,0,0,0.8); -fx-padding: 20; -fx-background-radius: 15;");
        zoom.setScene(new Scene(sp, Color.TRANSPARENT));
        zoom.show();
    }

    @FXML private void handleClose() { if (paymentStage != null) paymentStage.close(); }
}