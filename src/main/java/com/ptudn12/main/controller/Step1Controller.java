/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ptudn12.main.controller;

/**
 *
 * @author fo3cp
 */

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class Step1Controller {
    @FXML private ComboBox<String> comboGaDi;
    @FXML private ComboBox<String> comboGaDen;
    @FXML private DatePicker dateNgayDi;
    @FXML private DatePicker dateNgayVe;
    @FXML private RadioButton radioMotChieu;
    @FXML private RadioButton radioKhuHoi;
    @FXML private ToggleGroup chieuToggleGroup;
    @FXML private Button btnTimKiem;
    @FXML private VBox boxDanhSachChuyenTau;
    @FXML private VBox chuyenTauListContainer;
    @FXML private Button btnTiepTheo;

    // SỬA: Đổi loại Controller chính
    private BanVeController mainController;

    // SỬA: Đổi loại Controller chính
    public void setMainController(BanVeController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        // --- MOCK: NẠP DỮ LIỆU GA ĐI/GA ĐẾN ---
        comboGaDi.getItems().addAll("Ga Sài Gòn", "Ga Đà Nẵng", "Ga Hà Nội");
        comboGaDen.getItems().addAll("Ga Sài Gòn", "Ga Đà Nẵng", "Ga Hà Nội");

        // Set ngày mặc định
        dateNgayDi.setValue(LocalDate.now().plusDays(1)); // 24/12/2025
        dateNgayVe.setValue(LocalDate.now().plusDays(7)); // 30/12/2025
        
        // Mặc định ẩn ô ngày về
        dateNgayVe.setDisable(true);
        radioMotChieu.selectedProperty().addListener((obs, oldVal, newVal) -> {
            dateNgayVe.setDisable(newVal);
        });
    }

    @FXML
    private void handleTimKiem() {
        boxDanhSachChuyenTau.setVisible(true);
        boxDanhSachChuyenTau.setManaged(true);
        chuyenTauListContainer.getChildren().clear();
        btnTiepTheo.setDisable(true); // Vô hiệu hóa nút Tiếp theo

        // 3. --- MOCK: TẠO DATA CHUYẾN TÀU ---
        List<ChuyenTauMock> danhSach = generateSampleTrains();
        
        ToggleGroup trainToggleGroup = new ToggleGroup();
        for (ChuyenTauMock tau : danhSach) {
            HBox trainView = createTrainItemView(tau, trainToggleGroup);
            chuyenTauListContainer.getChildren().add(trainView);
        }
        
        trainToggleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle != null) {
                btnTiepTheo.setDisable(false); // Kích hoạt nút Tiếp theo
                mainController.setUserData("selectedTrain", newToggle.getUserData());
            } else {
                btnTiepTheo.setDisable(true);
            }
        });
    }

    // --- MOCK: HÀM TẠO DỮ LIỆU MẪU (Giống hình) ---
    private List<ChuyenTauMock> generateSampleTrains() {
        List<ChuyenTauMock> list = new ArrayList<>();
        list.add(new ChuyenTauMock("SE1 - Thống nhất", "24/12/2025 6:00", "24/12/2025 14:00", 34, 173));
        list.add(new ChuyenTauMock("SE3 - Thống nhất", "24/12/2025 8:00", "30/12/2025 16:00", 1, 202));
        list.add(new ChuyenTauMock("SE22 - Thống nhất", "24/12/2025 6:00", "24/12/2025 12:00", 130, 25));
        list.add(new ChuyenTauMock("SE4 - Thống nhất", "24/12/2025 6:00", "24/12/2025 15:00", 0, 64));
        return list;
    }
    
    // Hàm tạo giao diện cho 1 item chuyến tàu
    private HBox createTrainItemView(ChuyenTauMock tau, ToggleGroup group) {
        HBox hbox = new HBox();
        hbox.getStyleClass().add("train-list-item");

        RadioButton radio = new RadioButton();
        radio.setToggleGroup(group);
        radio.setUserData(tau); // Lưu data vào radio

        VBox info = new VBox(5);
        info.getChildren().add(new Label(tau.tenTau));
        info.getChildren().add(new Label("Thời gian đi: " + tau.tgDi));
        info.getChildren().add(new Label("Thời gian đến: " + tau.tgDen));
        
        VBox seats = new VBox(5);
        seats.getChildren().add(new Label("Số lượng chỗ đặt: " + tau.soLuongDat));
        seats.getChildren().add(new Label("Số lượng chỗ trống: " + tau.soLuongTrong));
        
        hbox.getChildren().addAll(radio, info, seats);
        HBox.setMargin(radio, new javafx.geometry.Insets(0, 20, 0, 0));
        return hbox;
    }

    @FXML
    private void handleTiepTheo() {
        mainController.setUserData("isKhuHoi", radioKhuHoi.isSelected());
        
        // SỬA: Tải file step-2.fxml
        mainController.loadContent("step-2.fxml");
    }

    @FXML
    private void handleQuayLai() {
        // Gọi hàm logout của controller chính
        mainController.handleLogout();
    }
    
    // --- MOCK: Class dữ liệu mẫu ---
    public static class ChuyenTauMock {
        String tenTau, tgDi, tgDen;
        int soLuongDat, soLuongTrong;
        public ChuyenTauMock(String ten, String tgDi, String tgDen, int dat, int trong) {
            this.tenTau = ten; this.tgDi = tgDi; this.tgDen = tgDen;
            this.soLuongDat = dat; this.soLuongTrong = trong;
        }
        @Override public String toString() { return tenTau; }
    }
}
