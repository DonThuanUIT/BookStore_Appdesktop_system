package com.bookstore.frontend.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;

public class LoadingController {
    
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Label lblLoadingText;
    
    @FXML
    public void initialize() {
        progressIndicator.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
    }
    
    public void setLoadingText(String text) {
        if (lblLoadingText != null) {
            lblLoadingText.setText(text);
        }
    }
}
