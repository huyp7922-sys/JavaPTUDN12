package com.ptudn12.main.controller;

import com.ptudn12.main.entity.Ga;
import com.ptudn12.main.entity.TuyenDuong;
import com.ptudn12.main.enums.TrangThai;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class RouteManagementController {

    @FXML private TableView<TuyenDuong> routeTable;
    @FXML private TableColumn<TuyenDuong, String> idColumn;
    @FXML private TableColumn<TuyenDuong, String> startStationColumn;
    @FXML private TableColumn<TuyenDuong, String> endStationColumn;
    @FXML private TableColumn<TuyenDuong, Integer> distanceColumn;
    @FXML private TableColumn<TuyenDuong, String> durationColumn;
    @FXML private TableColumn<TuyenDuong, String> priceColumn;
    @FXML private TableColumn<TuyenDuong, String> statusColumn;

    @FXML private ComboBox<String> startStationCombo;
    @FXML private ComboBox<String> endStationCombo;
    @FXML private ComboBox<String> statusCombo;

    private ObservableList<TuyenDuong> routeData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("maTuyen"));
        startStationColumn.setCellValueFactory(new PropertyValueFactory<>("tenDiemDi"));
        endStationColumn.setCellValueFactory(new PropertyValueFactory<>("tenDiemDen"));
        distanceColumn.setCellValueFactory(new PropertyValueFactory<>("soKm"));
        durationColumn.setCellValueFactory(new PropertyValueFactory<>("thoiGianDuKienFormatted"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("giaCoBanFormatted"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("trangThaiDisplay"));

        statusColumn.setCellFactory(column -> new TableCell<TuyenDuong, String>() {
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
                        case "TamNgung":
                            label.getStyleClass().add("status-paused");
                            break;
                        case "Nhap":
                            label.getStyleClass().add("status-inactive");
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
        routeData.clear();
        
        Ga gaHaNoi = new Ga("Ga Hà Nội", 0);
        gaHaNoi.setMaGa(1);
        
        Ga gaSaiGon = new Ga("Ga Sài Gòn", 1726);
        gaSaiGon.setMaGa(25);
        
        Ga gaDaNang = new Ga("Ga Đà Nẵng", 791);
        gaDaNang.setMaGa(16);
        
        Ga gaNhaTrang = new Ga("Ga Nha Trang", 1315);
        gaNhaTrang.setMaGa(21);
        
        Ga gaHaiPhong = new Ga("Ga Hải Phòng", 102);
        gaHaiPhong.setMaGa(5);
        
        Ga gaVinh = new Ga("Ga Vinh", 319);
        gaVinh.setMaGa(11);
        
        Ga gaHue = new Ga("Ga Huế", 688);
        gaHue.setMaGa(15);
        
        Ga gaQuangNgai = new Ga("Ga Quảng Ngãi", 915);
        gaQuangNgai.setMaGa(18);
        
        Ga gaBacNinh = new Ga("Ga Bắc Ninh", 28);
        gaBacNinh.setMaGa(2);
        
        Ga gaTamKy = new Ga("Ga Tam Kỳ", 849);
        gaTamKy.setMaGa(17);

        TuyenDuong td1 = new TuyenDuong(gaHaNoi, gaSaiGon, 29);
        td1.setMaTuyen("TD001");
        td1.setTrangThai(TrangThai.SanSang);
        
        TuyenDuong td2 = new TuyenDuong(gaDaNang, gaNhaTrang, 9);
        td2.setMaTuyen("TD002");
        td2.setTrangThai(TrangThai.Nhap);
        
        TuyenDuong td3 = new TuyenDuong(gaHaiPhong, gaVinh, 4);
        td3.setMaTuyen("TD003");
        td3.setTrangThai(TrangThai.TamNgung);
        
        TuyenDuong td4 = new TuyenDuong(gaHue, gaQuangNgai, 4);
        td4.setMaTuyen("TD004");
        td4.setTrangThai(TrangThai.SanSang);
        
        TuyenDuong td5 = new TuyenDuong(gaSaiGon, gaDaNang, 16);
        td5.setMaTuyen("TD005");
        td5.setTrangThai(TrangThai.SanSang);
        
        TuyenDuong td6 = new TuyenDuong(gaNhaTrang, gaHaNoi, 22);
        td6.setMaTuyen("TD006");
        td6.setTrangThai(TrangThai.Nhap);
        
        TuyenDuong td7 = new TuyenDuong(gaHaNoi, gaDaNang, 13);
        td7.setMaTuyen("TD007");
        td7.setTrangThai(TrangThai.SanSang);
        
        TuyenDuong td8 = new TuyenDuong(gaVinh, gaHue, 6);
        td8.setMaTuyen("TD008");
        td8.setTrangThai(TrangThai.SanSang);
        
        TuyenDuong td9 = new TuyenDuong(gaDaNang, gaSaiGon, 16);
        td9.setMaTuyen("TD009");
        td9.setTrangThai(TrangThai.SanSang);
        
        TuyenDuong td10 = new TuyenDuong(gaQuangNgai, gaNhaTrang, 7);
        td10.setMaTuyen("TD010");
        td10.setTrangThai(TrangThai.Nhap);
        
        TuyenDuong td11 = new TuyenDuong(gaHaNoi, gaVinh, 5);
        td11.setMaTuyen("TD011");
        td11.setTrangThai(TrangThai.SanSang);
        
        TuyenDuong td12 = new TuyenDuong(gaHue, gaDaNang, 2);
        td12.setMaTuyen("TD012");
        td12.setTrangThai(TrangThai.SanSang);
        
        TuyenDuong td13 = new TuyenDuong(gaNhaTrang, gaSaiGon, 7);
        td13.setMaTuyen("TD013");
        td13.setTrangThai(TrangThai.SanSang);
        
        TuyenDuong td14 = new TuyenDuong(gaHaiPhong, gaHaNoi, 2);
        td14.setMaTuyen("TD014");
        td14.setTrangThai(TrangThai.TamNgung);
        
        TuyenDuong td15 = new TuyenDuong(gaSaiGon, gaNhaTrang, 7);
        td15.setMaTuyen("TD015");
        td15.setTrangThai(TrangThai.SanSang);
        
        TuyenDuong td16 = new TuyenDuong(gaVinh, gaHaNoi, 5);
        td16.setMaTuyen("TD016");
        td16.setTrangThai(TrangThai.SanSang);
        
        TuyenDuong td17 = new TuyenDuong(gaDaNang, gaHue, 2);
        td17.setMaTuyen("TD017");
        td17.setTrangThai(TrangThai.Nhap);
        
        TuyenDuong td18 = new TuyenDuong(gaQuangNgai, gaHue, 4);
        td18.setMaTuyen("TD018");
        td18.setTrangThai(TrangThai.SanSang);
        
        TuyenDuong td19 = new TuyenDuong(gaSaiGon, gaHaNoi, 29);
        td19.setMaTuyen("TD019");
        td19.setTrangThai(TrangThai.SanSang);
        
        TuyenDuong td20 = new TuyenDuong(gaNhaTrang, gaDaNang, 9);
        td20.setMaTuyen("TD020");
        td20.setTrangThai(TrangThai.SanSang);

        routeData.addAll(td1, td2, td3, td4, td5, td6, td7, td8, td9, td10,
                        td11, td12, td13, td14, td15, td16, td17, td18, td19, td20);

        routeTable.setItems(routeData);
    }

    private void setupFilters() {
        ObservableList<String> stations = FXCollections.observableArrayList(
            "Tất cả điểm đi", "Ga Hà Nội", "Ga Sài Gòn", "Ga Đà Nẵng", 
            "Ga Nha Trang", "Ga Huế", "Ga Vinh", "Ga Hải Phòng", "Ga Quảng Ngãi"
        );
        startStationCombo.setItems(stations);
        endStationCombo.setItems(stations);

        ObservableList<String> statuses = FXCollections.observableArrayList(
            "Tất cả trạng thái", "SanSang", "Nhap", "TamNgung"
        );
        statusCombo.setItems(statuses);
    }

    @FXML
    private void handleAddRoute() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/add-route-dialog.fxml"));
            Scene scene = new Scene(loader.load());
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Thêm Tuyến Mới");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
            
            handleRefresh();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở form thêm tuyến đường!");
        }
    }

    @FXML
    private void handleEditRoute() {
        TuyenDuong selected = routeTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn tuyến đường cần sửa!");
            return;
        }
        showAlert(Alert.AlertType.INFORMATION, "Thông báo", 
                 "Chức năng sửa tuyến đường: " + selected.getTenDiemDi() + " → " + selected.getTenDiemDen());
    }

    @FXML
    private void handleDeleteRoute() {
        TuyenDuong selected = routeTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn tuyến đường cần xóa!");
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText("Bạn có chắc muốn xóa tuyến đường này?");
        confirm.setContentText(selected.getMaTuyen() + ": " + 
                              selected.getTenDiemDi() + " → " + selected.getTenDiemDen());
        
        if (confirm.showAndWait().get() == ButtonType.OK) {
            routeData.remove(selected);
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xóa tuyến đường!");
        }
    }

    @FXML
    private void handleRefresh() {
        loadMockData();
        showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Đã làm mới dữ liệu!");
    }

    @FXML
    private void handleDevelopRoute() {
        showAlert(Alert.AlertType.INFORMATION, "Thông báo", 
                 "Chức năng phát triển tuyến đường đang được phát triển!");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}