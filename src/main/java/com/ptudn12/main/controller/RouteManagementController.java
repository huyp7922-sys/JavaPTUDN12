package com.ptudn12.main.controller;

import com.ptudn12.main.dao.TuyenDuongDAO;
import com.ptudn12.main.entity.Ga;
import com.ptudn12.main.entity.TuyenDuong;
import com.ptudn12.main.enums.TrangThai;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import com.ptudn12.main.dao.TuyenDuongDAO;
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
    private TuyenDuongDAO tuyenDuongDAO = new TuyenDuongDAO();

    @FXML
    public void initialize() {
        // Setup table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("maTuyen"));
        startStationColumn.setCellValueFactory(new PropertyValueFactory<>("tenDiemDi"));
        endStationColumn.setCellValueFactory(new PropertyValueFactory<>("tenDiemDen"));
        distanceColumn.setCellValueFactory(new PropertyValueFactory<>("soKm"));
        durationColumn.setCellValueFactory(new PropertyValueFactory<>("thoiGianDuKienFormatted"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("giaCoBanFormatted"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("trangThaiDisplay"));

        // Custom cell factory for status column
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

        // Load data from database
        loadDataFromDatabase();

        // Setup filters
        setupFilters();
        
        // Setup filter listeners
        setupFilterListeners();
    }

    /**
     * Load dữ liệu từ database
     */
    private void loadDataFromDatabase() {
        try {
            List<TuyenDuong> danhSach = tuyenDuongDAO.layTatCaTuyenDuong();
            routeData.clear();
            routeData.addAll(danhSach);
            routeTable.setItems(routeData);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải dữ liệu từ database:\n" + e.getMessage());
        }
    }

    private void setupFilters() {
        ObservableList<String> stations = FXCollections.observableArrayList(
            "Tất cả điểm đi", "Ga Hà Nội", "Ga Sài Gòn", "Ga Đà Nẵng", 
            "Ga Nha Trang", "Ga Huế", "Ga Vinh", "Ga Hải Phòng", "Ga Quảng Ngãi"
        );
        startStationCombo.setItems(stations);
        startStationCombo.setValue("Tất cả điểm đi");
        
        endStationCombo.setItems(stations);
        endStationCombo.setValue("Tất cả điểm đi");

        ObservableList<String> statuses = FXCollections.observableArrayList(
            "Tất cả trạng thái", "SanSang", "Nhap", "TamNgung"
        );
        statusCombo.setItems(statuses);
        statusCombo.setValue("Tất cả trạng thái");
    }

    /**
     * Setup filter listeners
     */
    private void setupFilterListeners() {
        startStationCombo.setOnAction(e -> applyFilters());
        endStationCombo.setOnAction(e -> applyFilters());
        statusCombo.setOnAction(e -> applyFilters());
    }

    /**
     * Áp dụng bộ lọc
     */
    private void applyFilters() {
        String startStation = startStationCombo.getValue();
        String endStation = endStationCombo.getValue();
        String status = statusCombo.getValue();
        
        // TODO: Implement filter logic with DAO
        // For now, just reload all data
        loadDataFromDatabase();
    }

    @FXML
    private void handleAddRoute() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/add-route-dialog.fxml"));
            Scene scene = new Scene(loader.load());
            
            AddRouteDialogController controller = loader.getController();
            controller.setParentController(this);
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Thêm Tuyến Mới");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở form thêm tuyến đường:\n" + e.getMessage());
        }
    }

    @FXML
    private void handleEditRoute() {
        TuyenDuong selected = routeTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn tuyến đường cần sửa!");
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/add-route-dialog.fxml"));
            Scene scene = new Scene(loader.load());
            
            AddRouteDialogController controller = loader.getController();
            controller.setParentController(this);
            controller.setEditMode(selected);
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Sửa Tuyến Đường");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở form sửa tuyến đường:\n" + e.getMessage());
        }
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
            try {
                boolean success = tuyenDuongDAO.xoaTuyenDuong(Integer.parseInt(selected.getMaTuyen()));
                
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xóa/tạm ngưng tuyến đường!");
                    handleRefresh();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Thất bại", "Không thể xóa tuyến đường!");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Lỗi khi xóa tuyến đường:\n" + e.getMessage());
            }
        }
    }

    @FXML
     void handleRefresh() {
        loadDataFromDatabase();
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