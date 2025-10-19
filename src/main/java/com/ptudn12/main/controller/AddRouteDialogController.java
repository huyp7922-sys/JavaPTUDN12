package com.ptudn12.main.controller;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

/**
 *
 * @author Huy
 */
public class AddRouteDialogController {

    @FXML private ComboBox<String> startStationCombo;
    @FXML private ComboBox<String> endStationCombo;
    @FXML private TextField durationField;

    @FXML
    public void initialize() {
        var stations = FXCollections.observableArrayList(
            "Hà Nội", "Sài Gòn", "Đà Nẵng", "Nha Trang", "Huế", "Vinh", "Quảng Ngãi", "Hải Phòng"
        );
        startStationCombo.setItems(stations);
        endStationCombo.setItems(stations);
        
        startStationCombo.setValue("Hà Nội");
        endStationCombo.setValue("Quảng Ngãi");
    }

    @FXML
    private void handleSave() {
        String start = startStationCombo.getValue();
        String end = endStationCombo.getValue();
        String duration = durationField.getText();

        if (start == null || end == null || duration.isEmpty()) {
            showAlert("Vui lòng điền đầy đủ thông tin!");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thành công");
        alert.setHeaderText(null);
        alert.setContentText("Đã thêm tuyến mới: " + start + " → " + end);
        alert.showAndWait();

        closeDialog();
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) startStationCombo.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Cảnh báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}