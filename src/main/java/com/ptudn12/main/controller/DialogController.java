package com.ptudn12.main.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

public class DialogController {

    @FXML
    public void closeDialog(ActionEvent event) {
        Button btn = (Button) event.getSource();
        Stage stage = (Stage) btn.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void handleButtonHover(javafx.scene.input.MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 40; -fx-background-radius: 5; -fx-cursor: hand;");
    }

    @FXML
    public void handleButtonExit(javafx.scene.input.MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 40; -fx-background-radius: 5; -fx-cursor: hand;");
    }
}
