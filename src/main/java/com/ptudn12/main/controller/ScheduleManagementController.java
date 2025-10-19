package com.ptudn12.main.controller;

import com.ptudn12.main.entity.Ga;
import com.ptudn12.main.entity.LichTrinh;
import com.ptudn12.main.entity.Tau;
import com.ptudn12.main.entity.TuyenDuong;
import com.ptudn12.main.enums.TrangThai;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDateTime;

public class ScheduleManagementController {

    @FXML private TableView<LichTrinh> scheduleTable;
    @FXML private TableColumn<LichTrinh, String> idColumn;
    @FXML private TableColumn<LichTrinh, String> trainColumn;
    @FXML private TableColumn<LichTrinh, String> routeColumn;
    @FXML private TableColumn<LichTrinh, String> departureColumn;
    @FXML private TableColumn<LichTrinh, String> arrivalColumn;
    @FXML private TableColumn<LichTrinh, String> priceColumn;
    @FXML private TableColumn<LichTrinh, String> seatsColumn;
    @FXML private TableColumn<LichTrinh, String> statusColumn;

    @FXML private ComboBox<String> startStationCombo;
    @FXML private ComboBox<String> endStationCombo;
    @FXML private ComboBox<String> trainCombo;
    @FXML private ComboBox<String> statusCombo;

    private ObservableList<LichTrinh> scheduleData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("maLichTrinh"));
        trainColumn.setCellValueFactory(new PropertyValueFactory<>("maTauDisplay"));
        routeColumn.setCellValueFactory(new PropertyValueFactory<>("tuyenDuongDisplay"));
        departureColumn.setCellValueFactory(new PropertyValueFactory<>("ngayGioKhoiHanhFormatted"));
        arrivalColumn.setCellValueFactory(new PropertyValueFactory<>("ngayGioDenFormatted"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("giaCoBanFormatted"));
        seatsColumn.setCellValueFactory(new PropertyValueFactory<>("soGheDisplay"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("trangThaiDisplay"));

        statusColumn.setCellFactory(column -> new TableCell<LichTrinh, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label label = new Label(status);
                    label.getStyleClass().add("status-label");
                    switch (status) {
                        case "SanSang":
                            label.getStyleClass().add("status-ready");
                            break;
                        case "Nhap":
                            label.getStyleClass().add("status-inactive");
                            break;
                        case "DangChay":
                            label.getStyleClass().add("status-ready");
                            break;
                        case "TamNgung":
                            label.getStyleClass().add("status-paused");
                            break;
                    }
                    setGraphic(label);
                }
            }
        });

        loadMockData();

        setupFilters();
    }

    private void loadMockData() {
        scheduleData.clear();

        Ga gaHaNoi = new Ga("Ga Hà Nội", 0);
        Ga gaSaiGon = new Ga("Ga Sài Gòn", 1726);
        Ga gaDaNang = new Ga("Ga Đà Nẵng", 791);
        Ga gaNhaTrang = new Ga("Ga Nha Trang", 1315);
        Ga gaHaiPhong = new Ga("Ga Hải Phòng", 102);
        Ga gaVinh = new Ga("Ga Vinh", 319);

        TuyenDuong tuyenSGHN = new TuyenDuong(gaSaiGon, gaHaNoi, 29);
        tuyenSGHN.setMaTuyen("SG-HN");
        tuyenSGHN.setTrangThai(TrangThai.SanSang);

        TuyenDuong tuyenDNNT = new TuyenDuong(gaDaNang, gaNhaTrang, 9);
        tuyenDNNT.setMaTuyen("DN-NT");
        tuyenDNNT.setTrangThai(TrangThai.SanSang);

        TuyenDuong tuyenHPV = new TuyenDuong(gaHaiPhong, gaVinh, 4);
        tuyenHPV.setMaTuyen("HP-V");
        tuyenHPV.setTrangThai(TrangThai.SanSang);

        Tau tauSE1 = new Tau("SE1", 100);
        Tau tauSE3 = new Tau("SE3", 100);
        Tau tauSE5 = new Tau("SE5", 100);

        LichTrinh lt1 = new LichTrinh(tuyenSGHN, tauSE1, LocalDateTime.of(2025, 9, 28, 12, 30));
        lt1.setMaLichTrinh("SaiGon_HaNoi_27092025_1");
        lt1.setSoGheTrong(0);
        lt1.setTrangThai(TrangThai.SanSang);

        LichTrinh lt2 = new LichTrinh(tuyenDNNT, tauSE3, LocalDateTime.of(2025, 9, 28, 19, 30));
        lt2.setMaLichTrinh("DaNang_NhaTrang_27092025_1");
        lt2.setSoGheTrong(100);
        lt2.setTrangThai(TrangThai.Nhap);

        LichTrinh lt3 = new LichTrinh(tuyenHPV, tauSE5, LocalDateTime.of(2025, 10, 19, 6, 45));
        lt3.setMaLichTrinh("HaiPhong_Vinh_27092025_1");
        lt3.setSoGheTrong(20);
        lt3.setTrangThai(TrangThai.DangChay);

        LichTrinh lt4 = new LichTrinh(tuyenDNNT, tauSE1, LocalDateTime.of(2025, 9, 28, 19, 30));
        lt4.setMaLichTrinh("DaNang_NhaTrang_27092025_2");
        lt4.setSoGheTrong(100);
        lt4.setTrangThai(TrangThai.TamNgung);

        scheduleData.addAll(lt1, lt2, lt3, lt4);
        scheduleTable.setItems(scheduleData);
    }

    private void setupFilters() {
        ObservableList<String> stations = FXCollections.observableArrayList(
            "Tất cả điểm đi", "Ga Hà Nội", "Ga Sài Gòn", "Ga Đà Nẵng", 
            "Ga Nha Trang", "Ga Hải Phòng", "Ga Vinh"
        );
        startStationCombo.setItems(stations);
        endStationCombo.setItems(stations);

        ObservableList<String> trains = FXCollections.observableArrayList(
            "Tất cả mã tàu", "SE1", "SE3", "SE5"
        );
        trainCombo.setItems(trains);

        ObservableList<String> statuses = FXCollections.observableArrayList(
            "Tất cả trạng thái", "SanSang", "Nhap", "DangChay", "TamNgung"
        );
        statusCombo.setItems(statuses);
    }

    @FXML
    private void handleAddSchedule() {
        showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Chức năng thêm lịch trình đang được phát triển!");
    }

    @FXML
    private void handleEditSchedule() {
        LichTrinh selected = scheduleTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn lịch trình cần sửa!");
            return;
        }
        showAlert(Alert.AlertType.INFORMATION, "Thông báo", 
                 "Chức năng sửa lịch trình: " + selected.getMaLichTrinh());
    }

    @FXML
    private void handleDeleteSchedule() {
        LichTrinh selected = scheduleTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn lịch trình cần xóa!");
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText("Bạn có chắc muốn xóa lịch trình này?");
        confirm.setContentText(selected.getMaLichTrinh());
        
        if (confirm.showAndWait().get() == ButtonType.OK) {
            scheduleData.remove(selected);
        }
    }

    @FXML
    private void handleRefresh() {
        loadMockData();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}