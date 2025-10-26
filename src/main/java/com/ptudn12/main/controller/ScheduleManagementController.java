package com.ptudn12.main.controller;

import com.ptudn12.main.dao.LichTrinhDAO;
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
import java.util.List;
import java.util.stream.Collectors;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

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
    private ObservableList<LichTrinh> allScheduleData = FXCollections.observableArrayList();
    private LichTrinhDAO lichTrinhDAO = new LichTrinhDAO();

    @FXML
    public void initialize() {
        // Setup table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("maLichTrinh"));
        trainColumn.setCellValueFactory(new PropertyValueFactory<>("maTauDisplay"));
        routeColumn.setCellValueFactory(new PropertyValueFactory<>("tuyenDuongDisplay"));
        departureColumn.setCellValueFactory(new PropertyValueFactory<>("ngayGioKhoiHanhFormatted"));
        arrivalColumn.setCellValueFactory(new PropertyValueFactory<>("ngayGioDenFormatted"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("giaCoBanFormatted"));
        seatsColumn.setCellValueFactory(new PropertyValueFactory<>("soGheDisplay"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("trangThaiDisplay"));

        // Custom cell factory for status column
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
                        case "TamHoan":
                            label.getStyleClass().add("status-paused");
                            break;
                        case "ChuaKhoiHanh":   // trạng thái mới
                            label.getStyleClass().add("status-waiting");
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
            List<LichTrinh> danhSach = lichTrinhDAO.layTatCaLichTrinh();
            allScheduleData.clear();
            allScheduleData.addAll(danhSach);
            
            scheduleData.clear();
            scheduleData.addAll(danhSach);
            scheduleTable.setItems(scheduleData);
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải dữ liệu từ database:\n" + e.getMessage());
        }
    }

    private void setupFilters() {
        if (startStationCombo == null || endStationCombo == null || trainCombo == null || statusCombo == null) {
            return;
        }
        
        ObservableList<String> startStations = FXCollections.observableArrayList("Tất cả điểm đi");
        if (allScheduleData != null) {
            allScheduleData.stream()
                .filter(lt -> lt != null && lt.getTuyenDuong() != null && lt.getTuyenDuong().getTenDiemDi() != null)
                .map(lt -> lt.getTuyenDuong().getTenDiemDi())
                .distinct()
                .sorted()
                .forEach(startStations::add);
        }
        startStationCombo.setItems(startStations);
        startStationCombo.setValue("Tất cả điểm đi");
        
        ObservableList<String> endStations = FXCollections.observableArrayList("Tất cả điểm đến");
        if (allScheduleData != null) {
            allScheduleData.stream()
                .filter(lt -> lt != null && lt.getTuyenDuong() != null && lt.getTuyenDuong().getTenDiemDen() != null)
                .map(lt -> lt.getTuyenDuong().getTenDiemDen())
                .distinct()
                .sorted()
                .forEach(endStations::add);
        }
        endStationCombo.setItems(endStations);
        endStationCombo.setValue("Tất cả điểm đến");

        ObservableList<String> trains = FXCollections.observableArrayList("Tất cả mã tàu");
        if (allScheduleData != null) {
            allScheduleData.stream()
                .filter(lt -> lt != null && lt.getTau() != null && lt.getTau().getMacTau() != null)
                .map(lt -> lt.getTau().getMacTau())
                .distinct()
                .sorted()
                .forEach(trains::add);
        }
        trainCombo.setItems(trains);
        trainCombo.setValue("Tất cả mã tàu");

        ObservableList<String> statuses = FXCollections.observableArrayList("Tất cả trạng thái");
        if (allScheduleData != null) {
            allScheduleData.stream()
                .filter(lt -> lt != null && lt.getTrangThai() != null && lt.getTrangThai().getTenTrangThai() != null)
                .map(lt -> lt.getTrangThai().getTenTrangThai())
                .distinct()
                .sorted()
                .forEach(statuses::add);
        }
        statusCombo.setItems(statuses);
        statusCombo.setValue("Tất cả trạng thái");
    }
    
    private void setupFilterListeners() {
        startStationCombo.setOnAction(e -> applyFilters());
        endStationCombo.setOnAction(e -> applyFilters());
        trainCombo.setOnAction(e -> applyFilters());
        statusCombo.setOnAction(e -> applyFilters());
    }
    
    /**
     * Áp dụng bộ lọc
     */
    private void applyFilters() {
        String startStation = startStationCombo.getValue();
        String endStation = endStationCombo.getValue();
        String train = trainCombo.getValue();
        String status = statusCombo.getValue();
        
        List<LichTrinh> filtered = allScheduleData.stream()
            .filter(lichTrinh -> {
                // Filter điểm đi
                boolean matchStart = startStation == null || "Tất cả điểm đi".equals(startStation) || 
                    (lichTrinh.getTuyenDuong() != null && lichTrinh.getTuyenDuong().getTenDiemDi() != null && 
                     lichTrinh.getTuyenDuong().getTenDiemDi().equals(startStation));
                
                // Filter điểm đến
                boolean matchEnd = endStation == null || "Tất cả điểm đến".equals(endStation) || 
                    (lichTrinh.getTuyenDuong() != null && lichTrinh.getTuyenDuong().getTenDiemDen() != null && 
                     lichTrinh.getTuyenDuong().getTenDiemDen().equals(endStation));
                
                // Filter mã tàu
                boolean matchTrain = train == null || "Tất cả mã tàu".equals(train) || 
                    (lichTrinh.getTau() != null && lichTrinh.getTau().getMacTau() != null && 
                     lichTrinh.getTau().getMacTau().equals(train));
                
                // Filter trạng thái
                boolean matchStatus = status == null || "Tất cả trạng thái".equals(status) || 
                    (lichTrinh.getTrangThai() != null && lichTrinh.getTrangThai().getTenTrangThai() != null && 
                     lichTrinh.getTrangThai().getTenTrangThai().equals(status));
                
                return matchStart && matchEnd && matchTrain && matchStatus;
            })
            .collect(Collectors.toList());
        
        scheduleData.clear();
        scheduleData.addAll(filtered);
    }

    @FXML
    private void handleAddSchedule() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/add-schedule-dialog.fxml"));
            Scene scene = new Scene(loader.load());
            
            AddScheduleDialogController controller = loader.getController();
            controller.setParentController(this);
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Thêm Lịch Trình Mới");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở form thêm lịch trình:\n" + e.getMessage());
        }
    }

    @FXML
    private void handleEditSchedule() {
        LichTrinh selected = scheduleTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn lịch trình cần sửa!");
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/add-schedule-dialog.fxml"));
            Scene scene = new Scene(loader.load());
            
            AddScheduleDialogController controller = loader.getController();
            controller.setParentController(this);
            controller.setEditMode(selected);
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Sửa Lịch Trình");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở form sửa lịch trình:\n" + e.getMessage());
        }
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
            try {
                boolean success = lichTrinhDAO.xoaLichTrinh(selected.getMaLichTrinh());
                
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xóa/tạm ngưng lịch trình!");
                    handleRefresh();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Thất bại", "Không thể xóa lịch trình!");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Lỗi khi xóa lịch trình:\n" + e.getMessage());
            }
        }
    }

    @FXML
    public void handleRefresh() {
        loadDataFromDatabase();
        
        // Reset filters về mặc định
        startStationCombo.setValue("Tất cả điểm đi");
        endStationCombo.setValue("Tất cả điểm đến");
        trainCombo.setValue("Tất cả mã tàu");
        statusCombo.setValue("Tất cả trạng thái");
        
        // Cập nhật lại danh sách filter
        setupFilters();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}