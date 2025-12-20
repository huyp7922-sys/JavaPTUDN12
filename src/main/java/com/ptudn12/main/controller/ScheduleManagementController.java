package com.ptudn12.main.controller;

import com.ptudn12.main.dao.LichTrinhDAO;
import com.ptudn12.main.entity.LichTrinh;
import com.ptudn12.main.enums.TrangThai;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    private final ObservableList<LichTrinh> scheduleData = FXCollections.observableArrayList();
    private final ObservableList<LichTrinh> allScheduleData = FXCollections.observableArrayList();
    private final ObservableList<LichTrinh> filteredData = FXCollections.observableArrayList();

    private final LichTrinhDAO lichTrinhDAO = new LichTrinhDAO();

    private final Map<String, String> seatInfoCache = new HashMap<>();
    private final Map<String, Double> seatRatioCache = new HashMap<>();

    private static final int ITEMS_PER_PAGE = 20;

    private static final String ALL_START = "Tất cả điểm đi";
    private static final String ALL_END = "Tất cả điểm đến";
    private static final String ALL_TRAIN = "Tất cả mã tàu";
    private static final String ALL_STATUS = "Tất cả trạng thái";

    private int currentPage = 0;
    private int totalPages = 0;

    @FXML
    public void initialize() {
        setupTableColumns();
        setupFilterListeners();
        loadDataFromDatabase();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("maLichTrinh"));
        trainColumn.setCellValueFactory(new PropertyValueFactory<>("maTauDisplay"));
        routeColumn.setCellValueFactory(new PropertyValueFactory<>("tuyenDuongDisplay"));
        departureColumn.setCellValueFactory(new PropertyValueFactory<>("ngayGioKhoiHanhFormatted"));
        arrivalColumn.setCellValueFactory(new PropertyValueFactory<>("ngayGioDenFormatted"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("giaCoBanFormatted"));

        setupSeatsColumn();
        setupStatusColumn();
    }

    private void setupSeatsColumn() {
        seatsColumn.setCellFactory(column -> new TableCell<LichTrinh, String>() {

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    clearCell();
                    return;
                }

                LichTrinh lichTrinh = getTableRow().getItem();
                if (lichTrinh == null || lichTrinh.getMaLichTrinh() == null) {
                    clearCell();
                    return;
                }

                String maLichTrinh = lichTrinh.getMaLichTrinh();

                if (seatInfoCache.containsKey(maLichTrinh) && seatRatioCache.containsKey(maLichTrinh)) {
                    displaySeatInfo(seatInfoCache.get(maLichTrinh), seatRatioCache.get(maLichTrinh));
                    return;
                }

                Label cellLoading = new Label("Đang tải...");
                cellLoading.setStyle("-fx-text-fill: #999999; -fx-font-style: italic;");
                setGraphic(cellLoading);
                setAlignment(Pos.CENTER);

                Task<Void> loadTask = new Task<>() {
                    private String thongTinCho;
                    private double tyLe;

                    @Override
                    protected Void call() throws Exception {
                        thongTinCho = lichTrinhDAO.layThongTinChoNgoiFormat(maLichTrinh);
                        tyLe = lichTrinhDAO.layTyLeChoNgoiDaBan(maLichTrinh);
                        seatInfoCache.put(maLichTrinh, thongTinCho);
                        seatRatioCache.put(maLichTrinh, tyLe);
                        return null;
                    }

                    @Override
                    protected void succeeded() {
                        Platform.runLater(() -> {
                            if (getTableRow() != null && getTableRow().getItem() != null
                                    && getTableRow().getItem().getMaLichTrinh().equals(maLichTrinh)) {
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

                runDaemon(loadTask);
            }

            private void displaySeatInfo(String thongTinCho, double tyLe) {
                Label label = new Label(thongTinCho);
                label.getStyleClass().add("seats-label");

                if (tyLe >= 90) {
                    label.setStyle("-fx-background-color: #ffcccc; -fx-text-fill: #cc0000; -fx-padding: 3 8 3 8; -fx-background-radius: 3; -fx-font-weight: bold;");
                } else if (tyLe >= 70) {
                    label.setStyle("-fx-background-color: #fff3cd; -fx-text-fill: #856404; -fx-padding: 3 8 3 8; -fx-background-radius: 3;");
                } else if (tyLe >= 50) {
                    label.setStyle("-fx-background-color: #d1ecf1; -fx-text-fill: #0c5460; -fx-padding: 3 8 3 8; -fx-background-radius: 3;");
                } else {
                    label.setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724; -fx-padding: 3 8 3 8; -fx-background-radius: 3;");
                }

                setGraphic(label);
                setAlignment(Pos.CENTER);
            }

            private void clearCell() {
                setText(null);
                setGraphic(null);
                setStyle("");
            }
        });
    }

    private void setupStatusColumn() {
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("trangThaiDisplay"));
        statusColumn.setCellFactory(column -> new TableCell<LichTrinh, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);

                if (empty || status == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                Label label = new Label(status);
                label.getStyleClass().add("status-label");

                switch (status) {
                    case "SanSang":
                    case "DangChay":
                        label.getStyleClass().add("status-ready");
                        break;
                    case "Nhap":
                        label.getStyleClass().add("status-inactive");
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
        });
    }

    private void loadDataFromDatabase() {
        showLoading("Đang tải danh sách lịch trình...");

        Task<List<LichTrinh>> loadTask = new Task<>() {
            @Override
            protected List<LichTrinh> call() throws Exception {
                List<LichTrinh> lichTrinhList = lichTrinhDAO.layTatCaLichTrinh();
                LocalDateTime now = LocalDateTime.now();

                for (LichTrinh lichTrinh : lichTrinhList) {
                    if (lichTrinh.getNgayGioKhoiHanh() == null || lichTrinh.getNgayGioDen() == null) continue;

                    if (lichTrinh.getNgayGioKhoiHanh().isBefore(now) && lichTrinh.getNgayGioDen().isAfter(now)) {
                        lichTrinh.setTrangThai(TrangThai.DangChay);
                    } else if (lichTrinh.getNgayGioDen().isBefore(now)) {
                        lichTrinh.setTrangThai(TrangThai.DaKetThuc);
                    } else if (lichTrinh.getNgayGioKhoiHanh().isAfter(now)) {
                        if (lichTrinh.getTrangThai() == TrangThai.Nhap) {
                        } else {
                            lichTrinh.setTrangThai(TrangThai.SanSang);
                        }
                    }
                }

                return lichTrinhList;
            }

            @Override
            protected void succeeded() {
                try {
                    List<LichTrinh> danhSach = getValue();
                    allScheduleData.setAll(danhSach);
                    filteredData.setAll(danhSach);
                    setupFilters();
                    currentPage = 0;
                    updatePagination();
                } finally {
                    hideLoading();
                }
            }

            @Override
            protected void failed() {
                hideLoading();
                getException().printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải dữ liệu:\n" + getException().getMessage());
            }
        };

        runDaemon(loadTask);
    }

    private void setupFilters() {
        if (startStationCombo == null || endStationCombo == null || trainCombo == null || statusCombo == null) return;

        String currentStart = startStationCombo.getValue();
        String currentEnd = endStationCombo.getValue();
        String currentTrain = trainCombo.getValue();
        String currentStatus = statusCombo.getValue();

        ObservableList<String> startStations = FXCollections.observableArrayList(ALL_START);
        allScheduleData.stream()
                .filter(lt -> lt.getTuyenDuong() != null)
                .map(lt -> lt.getTuyenDuong().getTenDiemDi())
                .distinct().sorted()
                .forEach(startStations::add);
        startStationCombo.setItems(startStations);
        startStationCombo.setValue(startStations.contains(currentStart) ? currentStart : ALL_START);

        ObservableList<String> endStations = FXCollections.observableArrayList(ALL_END);
        allScheduleData.stream()
                .filter(lt -> lt.getTuyenDuong() != null)
                .map(lt -> lt.getTuyenDuong().getTenDiemDen())
                .distinct().sorted()
                .forEach(endStations::add);
        endStationCombo.setItems(endStations);
        endStationCombo.setValue(endStations.contains(currentEnd) ? currentEnd : ALL_END);

        ObservableList<String> trains = FXCollections.observableArrayList(ALL_TRAIN);
        allScheduleData.stream()
                .filter(lt -> lt.getTau() != null)
                .map(lt -> lt.getTau().getMacTau())
                .distinct().sorted()
                .forEach(trains::add);
        trainCombo.setItems(trains);
        trainCombo.setValue(trains.contains(currentTrain) ? currentTrain : ALL_TRAIN);

        ObservableList<String> statuses = FXCollections.observableArrayList(ALL_STATUS);
        allScheduleData.stream()
                .filter(lt -> lt.getTrangThai() != null)
                .map(lt -> lt.getTrangThai().getTenTrangThai())
                .distinct().sorted()
                .forEach(statuses::add);
        statusCombo.setItems(statuses);
        statusCombo.setValue(statuses.contains(currentStatus) ? currentStatus : ALL_STATUS);
    }

    private void setupFilterListeners() {
        if (startStationCombo != null) startStationCombo.setOnAction(e -> applyFilters());
        if (endStationCombo != null) endStationCombo.setOnAction(e -> applyFilters());
        if (trainCombo != null) trainCombo.setOnAction(e -> applyFilters());
        if (statusCombo != null) statusCombo.setOnAction(e -> applyFilters());
    }

    private void applyFilters() {
        showLoading("Đang lọc dữ liệu...");

        Task<List<LichTrinh>> filterTask = new Task<>() {
            @Override
            protected List<LichTrinh> call() {
                String start = startStationCombo.getValue();
                String end = endStationCombo.getValue();
                String train = trainCombo.getValue();
                String status = statusCombo.getValue();

                return allScheduleData.stream().filter(lt -> {
                    boolean mStart = start == null || ALL_START.equals(start) ||
                            (lt.getTuyenDuong() != null && lt.getTuyenDuong().getTenDiemDi().equals(start));
                    boolean mEnd = end == null || ALL_END.equals(end) ||
                            (lt.getTuyenDuong() != null && lt.getTuyenDuong().getTenDiemDen().equals(end));
                    boolean mTrain = train == null || ALL_TRAIN.equals(train) ||
                            (lt.getTau() != null && lt.getTau().getMacTau().equals(train));
                    boolean mStatus = status == null || ALL_STATUS.equals(status) ||
                            (lt.getTrangThai() != null && lt.getTrangThai().getTenTrangThai().equals(status));
                    return mStart && mEnd && mTrain && mStatus;
                }).collect(Collectors.toList());
            }

            @Override
            protected void succeeded() {
                filteredData.setAll(getValue());
                currentPage = 0;
                updatePagination();
                hideLoading();
            }

            @Override
            protected void failed() {
                hideLoading();
                getException().printStackTrace();
            }
        };

        runDaemon(filterTask);
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
            pageLabel.setText("Trang " + (currentPage + 1) + "/" + totalPages + " (Tổng: " + filteredData.size() + " lịch trình)");
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

    @FXML
    private void handleAddSchedule() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/add-schedule-dialog.fxml"));
            Scene scene = new Scene(loader.load());

            AddScheduleDialogController controller = loader.getController();
            controller.setParentController(this);
            controller.setAddMode();

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

        if (selected.getTrangThai() != TrangThai.Nhap) {
            showAlert(Alert.AlertType.WARNING, "Không thể sửa",
                    "Chỉ được phép chỉnh sửa các lịch trình ở trạng thái 'Nháp'!\n\n" +
                            "Trạng thái hiện tại: " + selected.getTrangThai().getTenTrangThai());
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/add-schedule-dialog.fxml"));
            Scene scene = new Scene(loader.load());

            AddScheduleDialogController controller = loader.getController();
            controller.setParentController(this);
            controller.setEditMode(selected);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Sửa Lịch Trình (Nháp)");
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

        if (selected.getTrangThai() != TrangThai.Nhap) {
            try {
                int[] stats = lichTrinhDAO.layThongTinChoNgoiTau(selected.getMaLichTrinh());
                int tongSoGhe = stats[0];
                int soGheConLai = stats[2];
                int soGheDaChiEm = tongSoGhe - soGheConLai;

                if (soGheDaChiEm > 0) {
                    showAlert(Alert.AlertType.ERROR, "Không thể xóa",
                            "Lịch trình này ĐÃ CÓ " + soGheDaChiEm + " GHẾ ĐƯỢC ĐẶT/BÁN!\n" +
                                    "(Tổng: " + tongSoGhe + " - Còn lại: " + soGheConLai + ")\n\n" +
                                    "Để đảm bảo dữ liệu, bạn không thể xóa lịch trình đã phát sinh giao dịch.\n" +
                                    "Vui lòng hủy vé/chỗ trước hoặc chọn phương án 'Tạm Ngưng'.");
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Lỗi Database", "Không thể kiểm tra thông tin chỗ ngồi!");
                return;
            }
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText("Bạn có chắc muốn xóa lịch trình này?");

        String content = "Mã: " + selected.getMaLichTrinh() + "\n" +
                "Tàu: " + selected.getTau().getMacTau() + "\n" +
                "Tuyến: " + selected.getTuyenDuong().getTenDiemDi() + " - " + selected.getTuyenDuong().getTenDiemDen();

        if (selected.getTrangThai() != TrangThai.Nhap) {
            content += "\n\nLưu ý: Lịch trình sẽ chuyển sang trạng thái TẠM NGƯNG.";
        } else {
            content += "\n\nLưu ý: Lịch trình Nháp sẽ bị xóa hoàn toàn khỏi hệ thống.";
        }

        confirm.setContentText(content);

        if (confirm.showAndWait().get() == ButtonType.OK) {
            try {
                boolean success = lichTrinhDAO.xoaLichTrinh(selected.getMaLichTrinh());
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Thành công",
                            selected.getTrangThai() == TrangThai.Nhap
                                    ? "Đã xóa lịch trình vĩnh viễn!"
                                    : "Đã chuyển lịch trình sang Tạm Ngưng!");
                    handleRefresh();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Thất bại", "Không thể xóa/tạm ngưng lịch trình!");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Lỗi khi xóa lịch trình:\n" + e.getMessage());
            }
        }
    }

    @FXML
    private void handleGenerateSchedules() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/generate-schedules-dialog.fxml"));
            Scene scene = new Scene(loader.load());

            GenerateSchedulesDialogController controller = loader.getController();
            controller.setParentController(this);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Tự Động Tạo Lịch Trình");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở form gen lịch trình:\n" + e.getMessage());
        }
    }

    @FXML
    public void handleRefresh() {
        seatInfoCache.clear();
        seatRatioCache.clear();
        loadDataFromDatabase();
    }

    @FXML
    private void handleViewSeatsDetail() {
        LichTrinh selected = scheduleTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn lịch trình để xem chi tiết!");
            return;
        }

        try {
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
            alert.setTitle("Chi tiết lịch trình");
            alert.setHeaderText("Thông tin");
            alert.setContentText(message.toString());
            alert.getDialogPane().setPrefWidth(600);
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể lấy thông tin chi tiết:\n" + e.getMessage());
        }
    }

    private void showLoading(String message) {
        Platform.runLater(() -> {
            if (loadingLabel != null) loadingLabel.setText(message);
            if (loadingOverlay != null) loadingOverlay.setVisible(true);
        });
    }

    private void hideLoading() {
        Platform.runLater(() -> {
            if (loadingOverlay != null) loadingOverlay.setVisible(false);
        });
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void runDaemon(Task<?> task) {
        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }
}
