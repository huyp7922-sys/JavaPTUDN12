package com.ptudn12.main.controller;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import com.ptudn12.main.dao.GaDAO;
import com.ptudn12.main.dao.TuyenDuongDAO;
import com.ptudn12.main.entity.Ga;
import com.ptudn12.main.entity.TuyenDuong;
import com.ptudn12.main.enums.TrangThai;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

/**
 *
 * @author Huy
 */


public class AddRouteDialogController {

    @FXML private ComboBox<String> startStationCombo;
    @FXML private ComboBox<String> endStationCombo;
    @FXML private TextField durationField;
    @FXML private ComboBox<String> statusCombo;

    private RouteManagementController parentController;
    private TuyenDuongDAO tuyenDuongDAO = new TuyenDuongDAO();
    private GaDAO gaDAO = new GaDAO();
    private Map<String, Ga> gaMap = new HashMap<>();
    
    private boolean isEditMode = false;
    private TuyenDuong editingRoute;

    @FXML
    public void initialize() {
        // Load danh sách ga từ database
        loadStations();
        
        // Setup status combo
        var statuses = FXCollections.observableArrayList("SanSang", "TamNgung");
        statusCombo.setItems(statuses);
        statusCombo.setValue("Nhap");
    }

    /**
     * Load danh sách ga từ database
     */
   private void loadStations() {
    try {
        List<Ga> danhSachGa = gaDAO.layTatCaGa();
        
        // Chỉ định rõ kiểu String
        ObservableList<String> stationNames = FXCollections.observableArrayList();
        
        for (Ga ga : danhSachGa) {
            String tenGa = ga.getViTriGa();
            stationNames.add(tenGa);
            gaMap.put(tenGa, ga);
        }
        
        startStationCombo.setItems(stationNames);
        endStationCombo.setItems(stationNames);
        
        // Không set giá trị mặc định - để user tự chọn
        // Tránh trường hợp start == end
        
    } catch (Exception e) {
        e.printStackTrace();
        showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải danh sách ga:\n" + e.getMessage());
    }
}
    /**
     * Set parent controller để refresh sau khi thêm/sửa
     */
    public void setParentController(RouteManagementController parentController) {
        this.parentController = parentController;
    }

    /**
     * Set edit mode với tuyến đường cần sửa
     */
    public void setEditMode(TuyenDuong tuyen) {
        this.isEditMode = true;
        this.editingRoute = tuyen;
        
        // Fill data vào form
        startStationCombo.setValue(tuyen.getDiemDi().getViTriGa());
        endStationCombo.setValue(tuyen.getDiemDen().getViTriGa());
        durationField.setText(String.valueOf(tuyen.getThoiGianDuKien()));
        statusCombo.setValue(tuyen.getTrangThai().getTenTrangThai());
    }

    @FXML
    private void handleSave() {
        String start = startStationCombo.getValue();
        String end = endStationCombo.getValue();
        String duration = durationField.getText();
        String status = statusCombo.getValue();

        // Validation
        if (start == null || end == null || duration.isEmpty() || status == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng điền đầy đủ thông tin!");
            return;
        }

        if (start.equals(end)) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Điểm đi và điểm đến không được trùng nhau!");
            return;
        }

        try {
            int thoiGian = Integer.parseInt(duration);
            if (thoiGian < 1) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Thời gian dự kiến phải >= 1 giờ!");
                return;
            }

            // Lấy ga từ map
            Ga gaDi = gaMap.get(start);
            Ga gaDen = gaMap.get(end);

            if (gaDi == null || gaDen == null) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không tìm thấy thông tin ga!");
                return;
            }

            // Tạo tuyến đường
            TuyenDuong tuyen = new TuyenDuong(gaDi, gaDen, thoiGian);
            tuyen.setTrangThai(TrangThai.valueOf(status));

            boolean success;
            if (isEditMode) {
                // Cập nhật
                tuyen.setMaTuyen(editingRoute.getMaTuyen());
                success = tuyenDuongDAO.capNhatTuyenDuong(tuyen);
            } else {
                // Thêm mới
                success = tuyenDuongDAO.themTuyenDuong(tuyen);
            }

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", 
                         (isEditMode ? "Đã cập nhật" : "Đã thêm") + " tuyến đường: " + start + " → " + end);
                
                // Refresh parent controller
                if (parentController != null) {
                    parentController.handleRefresh();
                }
                
                closeDialog();
            } else {
                showAlert(Alert.AlertType.ERROR, "Thất bại", 
                         "Không thể " + (isEditMode ? "cập nhật" : "thêm") + " tuyến đường!");
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Thời gian dự kiến phải là số nguyên!");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Lỗi khi lưu tuyến đường:\n" + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) startStationCombo.getScene().getWindow();
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