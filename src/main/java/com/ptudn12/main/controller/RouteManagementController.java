package com.ptudn12.main.controller;

import com.ptudn12.main.dao.TuyenDuongDAO;
import com.ptudn12.main.entity.TuyenDuong;
import com.ptudn12.main.enums.TrangThai;
import java.util.List;
import java.util.stream.Collectors;
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
    private ObservableList<TuyenDuong> allRouteData = FXCollections.observableArrayList(); // ✅ Lưu toàn bộ data
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
            allRouteData.clear();
            allRouteData.addAll(danhSach);
            
            routeData.clear();
            routeData.addAll(danhSach);
            routeTable.setItems(routeData);
            
            System.out.println("Đã tải " + danhSach.size() + " tuyến đường");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải dữ liệu từ database:\n" + e.getMessage());
        }
    }

    private void setupFilters() {
        // ✅ Lấy danh sách ga từ data thực tế
        ObservableList<String> stations = FXCollections.observableArrayList("Tất cả điểm đi");
        
        // Lấy unique ga từ dữ liệu
        allRouteData.stream()
            .map(TuyenDuong::getTenDiemDi)
            .distinct()
            .sorted()
            .forEach(stations::add);
        
        startStationCombo.setItems(stations);
        startStationCombo.setValue("Tất cả điểm đi");
        
        ObservableList<String> endStations = FXCollections.observableArrayList("Tất cả điểm đến");
        allRouteData.stream()
            .map(TuyenDuong::getTenDiemDen)
            .distinct()
            .sorted()
            .forEach(endStations::add);
        
        endStationCombo.setItems(endStations);
        endStationCombo.setValue("Tất cả điểm đến");

        ObservableList<String> statuses = FXCollections.observableArrayList(
            "Tất cả trạng thái", "SanSang", "TamNgung"
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
        
        List<TuyenDuong> filtered = allRouteData.stream()
            .filter(tuyen -> {
                // Filter điểm đi
                boolean matchStart = startStation.equals("Tất cả điểm đi") || 
                                    tuyen.getTenDiemDi().equals(startStation);
                
                // Filter điểm đến
                boolean matchEnd = endStation.equals("Tất cả điểm đến") || 
                                  tuyen.getTenDiemDen().equals(endStation);
                
                // Filter trạng thái
                boolean matchStatus = status.equals("Tất cả trạng thái") || 
                                     tuyen.getTrangThai().getTenTrangThai().equals(status);
                
                return matchStart && matchEnd && matchStatus;
            })
            .collect(Collectors.toList());
        
        routeData.clear();
        routeData.addAll(filtered);
        
        System.out.println("🔍 Lọc: " + filtered.size() + "/" + allRouteData.size() + " tuyến");
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

        // Kiểm tra trạng thái Nhap - không cho sửa
        if (selected.getTrangThai() == TrangThai.Nhap) {
            showAlert(Alert.AlertType.WARNING, "Không thể sửa", 
                     "Tuyến đường đang ở trạng thái Nháp!\n\n" +
                     "Vui lòng chọn 'Phát triển tuyến đường' để kích hoạt tuyến này trước khi chỉnh sửa.");
            return;
        }

        // Nếu không phải Nhap, cho phép sửa
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
        
        // Thông báo rõ ràng hơn
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText("Xóa tuyến đường");
        
        String contentText;
        if (selected.getTrangThai() == TrangThai.Nhap) {
            contentText = "Bạn có chắc muốn xóa tuyến:\n\n" + 
                         selected.getTenDiemDi() + " → " + selected.getTenDiemDen() + "\n\n" +
                         "⚠️ Tuyến ở trạng thái Nháp sẽ bị XÓA HOÀN TOÀN khỏi hệ thống!";
        } else {
            contentText = "Bạn có chắc muốn xóa tuyến:\n\n" + 
                         selected.getTenDiemDi() + " → " + selected.getTenDiemDen() + "\n\n" +
                         "⚠️ Tuyến đang hoạt động sẽ chuyển sang trạng thái TẠM NGƯNG\n" +
                         "(không xóa hoàn toàn).";
        }
        
        confirm.setContentText(contentText);
        
        if (confirm.showAndWait().get() == ButtonType.OK) {
            try {
                boolean success = tuyenDuongDAO.xoaTuyenDuong(Integer.parseInt(selected.getMaTuyen()));
                
                if (success) {
                    String message = selected.getTrangThai() == TrangThai.Nhap 
                        ? "Đã xóa tuyến đường thành công!" 
                        : "Đã chuyển tuyến sang trạng thái Tạm Ngưng!";
                    
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", message);
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
    public void handleRefresh() {
        loadDataFromDatabase();
        
        // Reset filters về mặc định
        startStationCombo.setValue("Tất cả điểm đi");
        endStationCombo.setValue("Tất cả điểm đến");
        statusCombo.setValue("Tất cả trạng thái");
        
        // Cập nhật lại danh sách filter
        setupFilters();
    }

    @FXML
    private void handleDevelopRoute() {
        TuyenDuong selected = routeTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn tuyến đường cần phát triển!");
            return;
        }

        // Chỉ cho phép phát triển tuyến ở trạng thái Nhap
        if (selected.getTrangThai() != TrangThai.Nhap) {
            showAlert(Alert.AlertType.WARNING, "Không thể phát triển", 
                     "Chỉ có thể phát triển tuyến đường ở trạng thái Nháp!\n\n" +
                     "Tuyến đang chọn có trạng thái: " + selected.getTrangThai().getTenTrangThai());
            return;
        }

        // Xác nhận phát triển
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác nhận phát triển");
        confirmAlert.setHeaderText("Phát triển tuyến đường");
        confirmAlert.setContentText(
            "Bạn có chắc muốn phát triển tuyến:\n\n" + 
            "   " + selected.getTenDiemDi() + " → " + selected.getTenDiemDen() + "\n" +
            "   Khoảng cách: " + selected.getSoKm() + " km\n" +
            "   Giá: " + selected.getGiaCoBanFormatted() + "\n\n" +
            "⚠️ Lưu ý: Sau khi phát triển, tuyến sẽ chuyển sang trạng thái Sẵn Sàng\n" +
            "và KHÔNG THỂ QUAY LẠI trạng thái Nháp!"
        );

        ButtonType btnPhatTrien = new ButtonType("Phát triển", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnHuy = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmAlert.getButtonTypes().setAll(btnPhatTrien, btnHuy);

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == btnPhatTrien) {
                selected.setTrangThai(TrangThai.SanSang);
                boolean success = tuyenDuongDAO.capNhatTuyenDuong(selected);

                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", 
                             "Đã phát triển tuyến đường thành công!\n\n" +
                             selected.getTenDiemDi() + " → " + selected.getTenDiemDen() + 
                             "\n\nTrạng thái: Sẵn Sàng ✅");
                    handleRefresh();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", 
                             "Không thể phát triển tuyến đường!\nVui lòng thử lại.");
                }
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}