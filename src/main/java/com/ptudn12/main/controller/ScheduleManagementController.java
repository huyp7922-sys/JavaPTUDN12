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
import javafx.geometry.Pos;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.layout.StackPane;

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
    
    @FXML private Label pageLabel;
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    
    @FXML private StackPane loadingOverlay;
    @FXML private ProgressIndicator loadingSpinner;
    @FXML private Label loadingLabel;

    private ObservableList<LichTrinh> scheduleData = FXCollections.observableArrayList();
    private ObservableList<LichTrinh> allScheduleData = FXCollections.observableArrayList();
    private ObservableList<LichTrinh> filteredData = FXCollections.observableArrayList();
    private LichTrinhDAO lichTrinhDAO = new LichTrinhDAO();
    
    // Cache thông tin giống redis gần gần giống =))
    private Map<String, String> seatInfoCache = new HashMap<>();
    private Map<String, Double> seatRatioCache = new HashMap<>();
    
    private static final int ITEMS_PER_PAGE = 20;
    private int currentPage = 0;
    private int totalPages = 0;

    @FXML
    public void initialize() {
        // lấy cột trong table
        idColumn.setCellValueFactory(new PropertyValueFactory<>("maLichTrinh"));
        trainColumn.setCellValueFactory(new PropertyValueFactory<>("maTauDisplay"));
        routeColumn.setCellValueFactory(new PropertyValueFactory<>("tuyenDuongDisplay"));
        departureColumn.setCellValueFactory(new PropertyValueFactory<>("ngayGioKhoiHanhFormatted"));
        arrivalColumn.setCellValueFactory(new PropertyValueFactory<>("ngayGioDenFormatted"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("giaCoBanFormatted"));
        
        seatsColumn.setCellFactory(column -> new TableCell<LichTrinh, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    LichTrinh lichTrinh = getTableRow().getItem();
                    if (lichTrinh != null && lichTrinh.getMaLichTrinh() != null) {
                        String maLichTrinh = lichTrinh.getMaLichTrinh();
                        
                        // Kiểm tra cache trước
                        if (seatInfoCache.containsKey(maLichTrinh) && seatRatioCache.containsKey(maLichTrinh)) {
                            String thongTinCho = seatInfoCache.get(maLichTrinh);
                            double tyLe = seatRatioCache.get(maLichTrinh);
                            displaySeatInfo(thongTinCho, tyLe);
                        } else {
                            // Hiển thị loading
                            Label loadingLabel = new Label("Đang tải...");
                            loadingLabel.setStyle("-fx-text-fill: #999999; -fx-font-style: italic;");
                            setGraphic(loadingLabel);
                            setAlignment(Pos.CENTER);
                            
                            // Load async để không block UI
                            Task<Void> loadTask = new Task<Void>() {
                                private String thongTinCho;
                                private double tyLe;
                                
                                @Override
                                protected Void call() throws Exception {
                                    thongTinCho = lichTrinhDAO.layThongTinChoNgoiFormat(maLichTrinh);
                                    tyLe = lichTrinhDAO.layTyLeChoNgoiDaBan(maLichTrinh);
                                    
                                    // Cache kết quả
                                    seatInfoCache.put(maLichTrinh, thongTinCho);
                                    seatRatioCache.put(maLichTrinh, tyLe);
                                    return null;
                                }
                                
                                @Override
                                protected void succeeded() {
                                    Platform.runLater(() -> {
                                        if (getTableRow() != null && getTableRow().getItem() != null && 
                                            getTableRow().getItem().getMaLichTrinh().equals(maLichTrinh)) {
                                            displaySeatInfo(thongTinCho, tyLe);
                                        }
                                    });
                                }
                                
                                @Override
                                protected void failed() {
                                    Platform.runLater(() -> {
                                        Label errorLabel = new Label("N/A");
                                        errorLabel.setStyle("-fx-text-fill: #999999;");
                                        setGraphic(errorLabel);
                                        setAlignment(Pos.CENTER);
                                    });
                                }
                            };
                            
                            new Thread(loadTask).start();
                        }
                    }
                }
            }
            
            private void displaySeatInfo(String thongTinCho, double tyLe) {
                Label label = new Label(thongTinCho);
                label.getStyleClass().add("seats-label");
                
                if (tyLe >= 90) {
                    label.setStyle("-fx-background-color: #ffcccc; -fx-text-fill: #cc0000; " +
                                 "-fx-padding: 3 8 3 8; -fx-background-radius: 3; -fx-font-weight: bold;");
                } else if (tyLe >= 70) {
                    label.setStyle("-fx-background-color: #fff3cd; -fx-text-fill: #856404; " +
                                 "-fx-padding: 3 8 3 8; -fx-background-radius: 3;");
                } else if (tyLe >= 50) {
                    label.setStyle("-fx-background-color: #d1ecf1; -fx-text-fill: #0c5460; " +
                                 "-fx-padding: 3 8 3 8; -fx-background-radius: 3;");
                } else {
                    label.setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724; " +
                                 "-fx-padding: 3 8 3 8; -fx-background-radius: 3;");
                }
                
                setGraphic(label);
                setAlignment(Pos.CENTER);
            }
        });
        
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
                        case "ChuaKhoiHanh":
                            label.getStyleClass().add("status-waiting");
                            break;
                    }
                    setGraphic(label);
                }
            }
        });

        loadDataFromDatabase();
        setupFilters();
        setupFilterListeners();
    }   
    @FXML
    private void handleGenerateSchedules() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/generate-schedules-dialog.fxml"));
            Scene scene = new Scene(loader.load());
            
            GenerateSchedulesDialogController controller = loader.getController();
            controller.setParentController(this);
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Tự Động Gen Lịch Trình");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở form gen lịch trình:\n" + e.getMessage());
        }
    }

    /**
     * Load dữ liệu từ database
     */
    private void loadDataFromDatabase() {
        showLoading("Đang tải danh sách lịch trình...");
        
        Task<List<LichTrinh>> loadTask = new Task<List<LichTrinh>>() {
            @Override
            protected List<LichTrinh> call() throws Exception {
                return lichTrinhDAO.layTatCaLichTrinh();
            }
            
            @Override
            protected void succeeded() {
                try {
                    List<LichTrinh> danhSach = getValue();
                    allScheduleData.clear();
                    allScheduleData.addAll(danhSach);
                    
                    filteredData.clear();
                    filteredData.addAll(danhSach);
                    
                    currentPage = 0;
                    updatePagination();
                    
                } finally {
                    hideLoading();
                }
            }
            
            @Override
            protected void failed() {
                hideLoading();
                Throwable e = getException();
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải dữ liệu từ database:\n" + e.getMessage());
            }
        };
        
        new Thread(loadTask).start();
    }
    
    private void showLoading(String message) {
        Platform.runLater(() -> {
            if (loadingLabel != null) {
                loadingLabel.setText(message);
            }
            if (loadingOverlay != null) {
                loadingOverlay.setVisible(true);
            }
        });
    }
    
    private void hideLoading() {
        Platform.runLater(() -> {
            if (loadingOverlay != null) {
                loadingOverlay.setVisible(false);
            }
        });
    }
    
    private void updatePagination() {
        totalPages = (int) Math.ceil((double) filteredData.size() / ITEMS_PER_PAGE);
        if (totalPages == 0) totalPages = 1;
        
        int fromIndex = currentPage * ITEMS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, filteredData.size());
        
        scheduleData.clear();
        if (fromIndex < filteredData.size()) {
            scheduleData.addAll(filteredData.subList(fromIndex, toIndex));
        }
        scheduleTable.setItems(scheduleData);
        
        if (pageLabel != null) {
            pageLabel.setText("Trang " + (currentPage + 1) + "/" + totalPages + 
                            " (Tổng: " + filteredData.size() + " lịch trình)");
        }
        
        if (prevButton != null) prevButton.setDisable(currentPage == 0);
        if (nextButton != null) nextButton.setDisable(currentPage >= totalPages - 1);
    }
    
    @FXML
    private void handlePrevPage() {
        if (currentPage > 0) {
            currentPage--;
            updatePagination();
        }
    }
    
    @FXML
    private void handleNextPage() {
        if (currentPage < totalPages - 1) {
            currentPage++;
            updatePagination();
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
        showLoading("Đang lọc dữ liệu...");
        
        Task<List<LichTrinh>> filterTask = new Task<List<LichTrinh>>() {
            @Override
            protected List<LichTrinh> call() throws Exception {
                String startStation = startStationCombo.getValue();
                String endStation = endStationCombo.getValue();
                String train = trainCombo.getValue();
                String status = statusCombo.getValue();
                
                return allScheduleData.stream()
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
            }
            
            @Override
            protected void succeeded() {
                try {
                    List<LichTrinh> filtered = getValue();
                    filteredData.clear();
                    filteredData.addAll(filtered);
                    
                    currentPage = 0;
                    updatePagination();
                } finally {
                    hideLoading();
                }
            }
            
            @Override
            protected void failed() {
                hideLoading();
            }
        };
        
        new Thread(filterTask).start();
    
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
        // Xóa cache để reload dữ liệu mới
        seatInfoCache.clear();
        seatRatioCache.clear();
        
        loadDataFromDatabase();
        
        // Reset filters về mặc định
        startStationCombo.setValue("Tất cả điểm đi");
        endStationCombo.setValue("Tất cả điểm đến");
        trainCombo.setValue("Tất cả mã tàu");
        statusCombo.setValue("Tất cả trạng thái");
        
        // Cập nhật lại danh sách filter
        setupFilters();

    }
    
    
    @FXML
    private void handleViewSeatsDetail() {
        LichTrinh selected = scheduleTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn lịch trình để xem chi tiết!");
            return;
        }
        
        try {
            // Lấy thông tin chi tiết
            int[] thongTin = lichTrinhDAO.layThongTinChoNgoiTau(selected.getMaLichTrinh());
            List<Map<String, Object>> danhSachDaBan = lichTrinhDAO.layDanhSachChoDaBan(selected.getMaLichTrinh());
            
            StringBuilder message = new StringBuilder();
            message.append("Lịch trình: ").append(selected.getMaLichTrinh()).append("\n");
            message.append("Tàu: ").append(selected.getTau().getMacTau()).append("\n\n");
            message.append("Tổng số chỗ: ").append(thongTin[0]).append("\n");
            message.append("Đã bán: ").append(thongTin[1]).append("\n");
            message.append("Còn trống: ").append(thongTin[2]).append("\n");
            message.append("Tỷ lệ: ").append(String.format("%.2f%%", lichTrinhDAO.layTyLeChoNgoiDaBan(selected.getMaLichTrinh()))).append("\n\n");
            
            if (!danhSachDaBan.isEmpty()) {
                message.append("Chi tiết chỗ đã bán:\n");
                for (Map<String, Object> cho : danhSachDaBan) {
                    message.append("- Toa: ").append(cho.get("tenToa"))
                           .append(", Vị trí: ").append(cho.get("viTriCho"))
                           .append(", Loại: ").append(cho.get("loaiCho"))
                           .append(", Vé: ").append(cho.get("maVe"))
                           .append("\n");
                }
            }
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Chi tiết chỗ ngồi");
            alert.setHeaderText("Thông tin chỗ ngồi");
            alert.setContentText(message.toString());
            alert.getDialogPane().setPrefWidth(600);
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể lấy thông tin chi tiết:\n" + e.getMessage());
        }

    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}