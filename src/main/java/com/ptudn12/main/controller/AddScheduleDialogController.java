package com.ptudn12.main.controller;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import java.time.LocalDate;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

/**
 *
 * @author Huy
 */
public class AddScheduleDialogController {

    @FXML private ComboBox<String> routeCombo;
    @FXML private ComboBox<String> trainCombo;
    @FXML private DatePicker departureDatePicker;
    @FXML private ComboBox<String> hourCombo;
    @FXML private ComboBox<String> minuteCombo;
    @FXML private TextField priceField;

    @FXML
    public void initialize() {
        ObservableList<String> routes = FXCollections.observableArrayList(
            "Hà Nội → Sài Gòn", "Sài Gòn → Hà Nội", "Hà Nội → Đà Nẵng",
            "Đà Nẵng → Nha Trang", "Hà Nội → Huế", "Sài Gòn → Đà Nẵng"
        );
        routeCombo.setItems(routes);
        
        ObservableList<String> trains = FXCollections.observableArrayList(
            "SE1", "SE2", "SE3", "SE4", "SE5", "SE6", "SE7", "SE8", "SE9", "SE10"
        );
        trainCombo.setItems(trains);
        
        ObservableList<String> hours = FXCollections.observableArrayList();
        for (int i = 0; i < 24; i++) {
            hours.add(String.format("%02d", i));
        }
        hourCombo.setItems(hours);
        
        ObservableList<String> minutes = FXCollections.observableArrayList("00", "15", "30", "45");
        minuteCombo.setItems(minutes);
        
        departureDatePicker.setValue(LocalDate.now().plusDays(1));
    }

    @FXML
    private void handleSave() {
        String route = routeCombo.getValue();
        String train = trainCombo.getValue();
        LocalDate date = departureDatePicker.getValue();
        String hour = hourCombo.getValue();
        String minute = minuteCombo.getValue();
        String price = priceField.getText();

        // Validation
        if (route == null || train == null || date == null || 
            hour == null || minute == null || price.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng điền đầy đủ thông tin!");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thành công");
        alert.setHeaderText(null);
        alert.setContentText(String.format(
            "Đã thêm lịch trình mới:\n\n" +
            "Tuyến: %s\n" +
            "Tàu: %s\n" +
            "Ngày: %s\n" +
            "Giờ: %s:%s\n" +
            "Giá: %s VNĐ",
            route, train, date, hour, minute, price
        ));
        alert.showAndWait();

        closeDialog();
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) routeCombo.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}