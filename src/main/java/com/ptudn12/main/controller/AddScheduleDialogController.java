package com.ptudn12.main.controller;

import com.ptudn12.main.dao.LichTrinhDAO;
import com.ptudn12.main.dao.TauDAO;
import com.ptudn12.main.dao.TuyenDuongDAO;
import com.ptudn12.main.entity.LichTrinh;
import com.ptudn12.main.entity.Tau;
import com.ptudn12.main.entity.TuyenDuong;
import com.ptudn12.main.enums.TrangThai;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class AddScheduleDialogController {

    @FXML private ComboBox<String> routeCombo;
    @FXML private ComboBox<String> trainCombo;
    @FXML private DatePicker departureDatePicker;
    @FXML private ComboBox<String> hourCombo;
    @FXML private ComboBox<String> minuteCombo;
    @FXML private ComboBox<String> statusCombo;

    private ScheduleManagementController parentController;
    private LichTrinhDAO lichTrinhDAO = new LichTrinhDAO();
    private TuyenDuongDAO tuyenDuongDAO = new TuyenDuongDAO();
    private TauDAO tauDAO = new TauDAO();
    
    private Map<String, TuyenDuong> tuyenMap = new HashMap<>();
    private Map<String, Tau> tauMap = new HashMap<>();
    
    private boolean isEditMode = false;
    private LichTrinh editingSchedule;

    @FXML
    public void initialize() {
        // Load routes
        loadRoutes();
        
        // Load trains
        loadTrains();
        
        // Setup time combos
        ObservableList<String> hours = FXCollections.observableArrayList();
        for (int i = 0; i < 24; i++) {
            hours.add(String.format("%02d", i));
        }
        hourCombo.setItems(hours);
        hourCombo.setValue("06");
        
        ObservableList<String> minutes = FXCollections.observableArrayList("00", "15", "30", "45");
        minuteCombo.setItems(minutes);
        minuteCombo.setValue("00");
        
        var statuses = FXCollections.observableArrayList(
            "Nhap", "SanSang", "ChuaKhoiHanh", "DangChay", "TamHoan", "DaKetThuc", "TamNgung"
        );
        statusCombo.setItems(statuses);
        statusCombo.setValue("Nhap"); // Mặc định là Nháp
        
        // Set default date
        departureDatePicker.setValue(LocalDate.now().plusDays(1));
    }

    private void loadRoutes() {
        try {
            List<TuyenDuong> danhSach = tuyenDuongDAO.layTatCaTuyenDuong();
            ObservableList<String> routeNames = FXCollections.observableArrayList();

            for (TuyenDuong tuyen : danhSach) {
                String routeName = tuyen.getTenDiemDi() + " → " + tuyen.getTenDiemDen();
                routeNames.add(routeName);
                tuyenMap.put(routeName, tuyen);
            }

            routeCombo.setItems(routeNames);
            if (!routeNames.isEmpty()) routeCombo.setValue(routeNames.get(0));

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải danh sách tuyến:\n" + e.getMessage());
        }
    }

    private void loadTrains() {
        try {
            List<Tau> danhSach = tauDAO.layTatCaTau();
            ObservableList<String> trainNames = FXCollections.observableArrayList();
            
            for (Tau tau : danhSach) {
                trainNames.add(tau.getMacTau());
                tauMap.put(tau.getMacTau(), tau);
            }
            
            trainCombo.setItems(trainNames);
            if (!trainNames.isEmpty()) trainCombo.setValue(trainNames.get(0));
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải danh sách tàu:\n" + e.getMessage());
        }
    }

    public void setParentController(ScheduleManagementController parentController) {
        this.parentController = parentController;
    }

    /**
     * Cấu hình cho chế độ Thêm mới:
     * - Khóa trạng thái ở mức "Nhap"
     * - Không cho người dùng chọn trạng thái khác lúc tạo
     */
    public void setAddMode() {
        if (statusCombo != null) {
            statusCombo.setValue("Nhap"); 
            statusCombo.setDisable(true); // Disable để người dùng không đổi được
        }
    }

    public void setEditMode(LichTrinh lichTrinh) {
        this.isEditMode = true;
        this.editingSchedule = lichTrinh;
        
        // Fill data
        String routeName = lichTrinh.getTuyenDuong().getTenDiemDi() + " → " + 
                           lichTrinh.getTuyenDuong().getTenDiemDen();
        routeCombo.setValue(routeName);
        trainCombo.setValue(lichTrinh.getTau().getMacTau());
        departureDatePicker.setValue(lichTrinh.getNgayGioKhoiHanh().toLocalDate());
        hourCombo.setValue(String.format("%02d", lichTrinh.getNgayGioKhoiHanh().getHour()));
        minuteCombo.setValue(String.format("%02d", lichTrinh.getNgayGioKhoiHanh().getMinute()));
        
        // Khi sửa, ta cho phép đổi trạng thái (ví dụ từ Nháp -> Sẵn Sàng)
        // nên không disable statusCombo ở đây.
        statusCombo.setValue(lichTrinh.getTrangThai().getTenTrangThai());
    }

    @FXML
    private void handleSave() {
        String route = routeCombo.getValue();
        String train = trainCombo.getValue();
        LocalDate date = departureDatePicker.getValue();
        String hour = hourCombo.getValue();
        String minute = minuteCombo.getValue();
        String status = statusCombo.getValue();

        if (route == null || train == null || date == null || 
            hour == null || minute == null || status == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng điền đầy đủ thông tin!");
            return;
        }

        try {
            LocalTime time = LocalTime.of(Integer.parseInt(hour), Integer.parseInt(minute));
            LocalDateTime ngayGioKhoiHanh = LocalDateTime.of(date, time);

            if (ngayGioKhoiHanh.isBefore(LocalDateTime.now().plusDays(1))) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", 
                          "Ngày giờ khởi hành phải sau ngày hiện tại ít nhất 1 ngày!");
                return;
            }

            TuyenDuong tuyenDuong = tuyenMap.get(route);
            Tau tau = tauMap.get(train);

            if (tuyenDuong == null || tau == null) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không tìm thấy thông tin tuyến hoặc tàu!");
                return;
            }

            LichTrinh lichTrinh = new LichTrinh(tuyenDuong, tau, ngayGioKhoiHanh);
            // Lưu ý: TrangThai.valueOf cần chuỗi chính xác (case-sensitive) khớp với Enum
            lichTrinh.setTrangThai(TrangThai.valueOf(status));

            boolean success;
            if (isEditMode) {
                lichTrinh.setMaLichTrinh(editingSchedule.getMaLichTrinh());
                success = lichTrinhDAO.capNhatLichTrinh(lichTrinh);
            } else {
                success = lichTrinhDAO.themLichTrinh(lichTrinh);
            }

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", 
                          (isEditMode ? "Đã cập nhật" : "Đã thêm") + " lịch trình thành công!");
                
                if (parentController != null) {
                    parentController.handleRefresh();
                }
                closeDialog();
            } else {
                showAlert(Alert.AlertType.ERROR, "Thất bại", 
                          "Không thể " + (isEditMode ? "cập nhật" : "thêm") + " lịch trình!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Lỗi khi lưu lịch trình:\n" + e.getMessage());
        }
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