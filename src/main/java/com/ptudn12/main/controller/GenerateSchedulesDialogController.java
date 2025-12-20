package com.ptudn12.main.controller;

import com.ptudn12.main.dao.*;
import com.ptudn12.main.entity.*;
import com.ptudn12.main.enums.TrangThai;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class GenerateSchedulesDialogController {

    @FXML private ComboBox<TuyenDuong> routeCombo;
    @FXML private ComboBox<Tau> trainCombo;
    @FXML private TextField departureTimeField;
    @FXML private Label routeInfoLabel; 
    @FXML private RadioButton radio7Days;
    @FXML private RadioButton radio30Days;
    @FXML private CheckBox roundTripCheckbox;

    private TuyenDuongDAO tuyenDuongDAO = new TuyenDuongDAO();
    private TauDAO tauDAO = new TauDAO();
    private LichTrinhDAO lichTrinhDAO = new LichTrinhDAO();
    private ScheduleManagementController parentController;

    @FXML
    public void initialize() {
        // Load danh sách tuyến
        List<TuyenDuong> danhSachTuyen = tuyenDuongDAO.getAllTuyenDuong();
        routeCombo.setItems(FXCollections.observableArrayList(danhSachTuyen));
        routeCombo.setConverter(new javafx.util.StringConverter<TuyenDuong>() {
            @Override
            public String toString(TuyenDuong tuyen) {
                if (tuyen == null) return "";
                return tuyen.getDiemDi().getViTriGa() + " → " + tuyen.getDiemDen().getViTriGa();
            }

            @Override
            public TuyenDuong fromString(String string) {
                return null;
            }
        });

        routeCombo.setOnAction(e -> {
            TuyenDuong tuyen = routeCombo.getValue();
            if (tuyen != null) {
                int khoangCach = Math.abs(tuyen.getDiemDen().getMocKm() - tuyen.getDiemDi().getMocKm());
                routeInfoLabel.setText(String.format(
                    "Khoảng cách: %d km | Giá cơ bản: %,.0f VNĐ | Thời gian: %d giờ",
                    khoangCach,
                    tuyen.getGiaCoBan(),
                    tuyen.getThoiGianDuKien()
                ));
            }
        });

        // Load danh sách tàu
        List<Tau> danhSachTau = tauDAO.getAllTau();
        trainCombo.setItems(FXCollections.observableArrayList(danhSachTau));
        trainCombo.setConverter(new javafx.util.StringConverter<Tau>() {
            @Override
            public String toString(Tau tau) {
                if (tau == null) return "";
                return tau.getMacTau();
            }

            @Override
            public Tau fromString(String string) {
                return null;
            }
        });

        // Set giá trị mặc định
        departureTimeField.setText("08:00");
    }

    public void setParentController(ScheduleManagementController parent) {
        this.parentController = parent;
    }

    @FXML
    private void handleGenerate() {
        // Validate
        if (routeCombo.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn tuyến đường!");
            return;
        }
        if (trainCombo.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn tàu!");
            return;
        }
        if (departureTimeField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập giờ khởi hành!");
            return;
        }

        // Parse dữ liệu
        TuyenDuong tuyen = routeCombo.getValue();
        Tau tau = trainCombo.getValue();
        String departureTimeStr = departureTimeField.getText().trim();

        try {
            LocalTime.parse(departureTimeStr, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Giờ khởi hành không hợp lệ! (VD: 08:30)");
            return;
        }

        float basePrice = tuyen.getGiaCoBan();

        int days = radio7Days.isSelected() ? 7 : 30;
        boolean isRoundTrip = roundTripCheckbox.isSelected();

        // Confirm
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận");
        confirm.setHeaderText("Xác nhận tạo lịch trình");
        confirm.setContentText(
            "Tuyến: " + tuyen.getDiemDi().getViTriGa() + " → " + tuyen.getDiemDen().getViTriGa() + "\n" +
            "Tàu: " + tau.getMacTau() + "\n" +
            "Giờ khởi hành: " + departureTimeStr + "\n" +
            "Giá cơ bản: " + String.format("%,.0f VNĐ", basePrice) + "\n" +
            "Số ngày: " + days + "\n" +
            "Khứ hồi: " + (isRoundTrip ? "Có" : "Không") + "\n\n" +
            "Tổng lịch trình sẽ tạo: " + (isRoundTrip ? days * 2 : days)
        );

        if (confirm.showAndWait().get() != ButtonType.OK) {
            return;
        }

        // Tạo lịch trình trong background
        Task<Integer> task = new Task<Integer>() {
            @Override
            protected Integer call() throws Exception {
                return generateSchedules(tuyen, tau, departureTimeStr, basePrice, days, isRoundTrip);
            }

            @Override
            protected void succeeded() {
                int count = getValue();
                showAlert(Alert.AlertType.INFORMATION, "Thành công", 
                    "Đã tạo thành công " + count + " lịch trình!");
                
                if (parentController != null) {
                    parentController.handleRefresh();
                }
                
                closeDialog();
            }

            @Override
            protected void failed() {
                Throwable e = getException();
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Lỗi khi tạo lịch trình:\n" + e.getMessage());
            }
        };

        new Thread(task).start();
    }

    private int generateSchedules(TuyenDuong tuyen, Tau tau, String departureTimeStr, 
                              float basePrice, int days, boolean isRoundTrip) {
        int count = 0;
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalTime departureTime = LocalTime.parse(departureTimeStr, DateTimeFormatter.ofPattern("HH:mm"));

        int khoangCach = Math.abs(tuyen.getDiemDen().getMocKm() - tuyen.getDiemDi().getMocKm());

        for (int i = 0; i < days; i++) {
            LocalDate currentDate = startDate.plusDays(i);
            
            LichTrinh lichTrinhDi = new LichTrinh();
            lichTrinhDi.setMaLichTrinh(generateMaLichTrinh(currentDate, tuyen, "DI", i));
            lichTrinhDi.setTau(tau);
            lichTrinhDi.setTuyenDuong(tuyen);
            lichTrinhDi.setNgayGioKhoiHanh(LocalDateTime.of(currentDate, departureTime));
            
            long travelHours = (long) (khoangCach / 60.0);
            lichTrinhDi.setNgayGioDen(lichTrinhDi.getNgayGioKhoiHanh().plusHours(travelHours));
            
            lichTrinhDi.setGiaCoBan(basePrice);
            lichTrinhDi.setTrangThai(TrangThai.ChuaKhoiHanh); // ✅ ĐÚNG
            
            try {
                if (lichTrinhDAO.themLichTrinh(lichTrinhDi)) {
                    count++;
                }
            } catch (Exception e) {
                System.err.println("❌ Lỗi tạo lịch trình đi ngày " + currentDate + ": " + e.getMessage());
            }

            if (isRoundTrip) {
                TuyenDuong tuyenNguoc = tuyenDuongDAO.findTuyenNguoc(tuyen.getMaTuyen());
                
                if (tuyenNguoc == null) {
                    continue;
                }
                
                LichTrinh lichTrinhVe = new LichTrinh();
                lichTrinhVe.setMaLichTrinh(generateMaLichTrinh(currentDate, tuyenNguoc, "VE", i));
                lichTrinhVe.setTau(tau);
                lichTrinhVe.setTuyenDuong(tuyenNguoc);
                
                lichTrinhVe.setNgayGioKhoiHanh(lichTrinhDi.getNgayGioDen().plusHours(4));
                lichTrinhVe.setNgayGioDen(lichTrinhVe.getNgayGioKhoiHanh().plusHours(travelHours));
                lichTrinhVe.setGiaCoBan(tuyenNguoc.getGiaCoBan());
                lichTrinhVe.setTrangThai(TrangThai.ChuaKhoiHanh); 
                
                try {
                    if (lichTrinhDAO.themLichTrinh(lichTrinhVe)) {
                        count++;
                    }
                } catch (Exception e) {
                    System.err.println("Lỗi tạo lịch trình về ngày " + currentDate + ": " + e.getMessage());
                }
            }
        }

        return count;
    }
    private String generateMaLichTrinh(LocalDate date, TuyenDuong tuyen, String loai, int index) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String dateStr = date.format(formatter);
        String gaXuatPhat = tuyen.getDiemDi().getViTriGa().substring(0, Math.min(3, tuyen.getDiemDi().getViTriGa().length()));
        return "LT" + dateStr + "_" + gaXuatPhat + "_" + loai + "_" + index;
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
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}