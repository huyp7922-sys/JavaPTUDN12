/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ptudn12.main.controller;

/**
 *
 * @author fo3cp
 */

//import com.ptudn12.main.Controller.VeTamThoi;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Step3Controller {
    @FXML private ScrollPane scrollPaneHanhKhach;
    @FXML private VBox containerHanhKhach;
    @FXML private Label lblTongThanhTien;

    // Thông tin người mua
    @FXML private TextField txtNguoiMuaHoTen;
    @FXML private TextField txtNguoiMuaSoGiayTo;
    @FXML private TextField txtNguoiMuaEmail;
    @FXML private TextField txtNguoiMuaSDT;
    
    private BanVeController mainController;
    private DecimalFormat moneyFormatter = new DecimalFormat("#,##0 'VNĐ'");
    
    // Danh sách các controller của từng hàng
    private List<HanhKhachRowController> rowControllers = new ArrayList<>();
    private List<VeTamThoi> allTickets = new ArrayList<>();
    
    // Listeners cho auto-fill
    private ChangeListener<String> autoFillHoTenListener;
    private ChangeListener<String> autoFillGiayToListener;
    private boolean isAutoFillActive = true; 

    public void setMainController(BanVeController mainController) {
        this.mainController = mainController;
    }
    
    @FXML
    public void initialize() {
        // Thêm listeners để TẮT auto-fill nếu người dùng tự nhập
        txtNguoiMuaHoTen.textProperty().addListener((obs, oldVal, newVal) -> {
            if (isAutoFillActive && !newVal.equals(oldVal)) isAutoFillActive = false;
        });
        txtNguoiMuaSoGiayTo.textProperty().addListener((obs, oldVal, newVal) -> {
            if (isAutoFillActive && !newVal.equals(oldVal)) isAutoFillActive = false;
        });
    }
    
    public void initData() {
        // 1. Dọn dẹp
        containerHanhKhach.getChildren().clear();
        rowControllers.clear();
        allTickets.clear();

        // 2. Lấy dữ liệu giỏ hàng từ Step 2
        List<VeTamThoi> gioHangDi = (List<VeTamThoi>) mainController.getUserData("gioHang_Di");
        List<VeTamThoi> gioHangVe = (List<VeTamThoi>) mainController.getUserData("gioHang_Ve");

        if (gioHangDi != null) allTickets.addAll(gioHangDi);
        if (gioHangVe != null) allTickets.addAll(gioHangVe);

        if (allTickets.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Lỗi: Không có vé nào trong giỏ hàng.");
            return;
        }

        // 3. Tạo các hàng hành khách
        boolean isFirstRow = true;
        for (VeTamThoi ve : allTickets) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/hanhkhach-row.fxml")); // (Đảm bảo đường dẫn đúng)
                Node rowNode = loader.load();
                HanhKhachRowController rowController = loader.getController();
                
                // Nạp dữ liệu vào hàng
                rowController.setData(ve, this);
                
                // Thêm hàng vào UI
                containerHanhKhach.getChildren().add(rowNode);
                rowControllers.add(rowController);

                // 4. Thiết lập Auto-fill cho HÀNG ĐẦU TIÊN
                if (isFirstRow) {
                    setupAutoFill(rowController);
                    isFirstRow = false;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        // 5. Cập nhật tổng tiền lần đầu
        updateTongThanhTien();
    }

    // Thiết lập tính năng auto-fill
    private void setupAutoFill(HanhKhachRowController firstRowController) {
        isAutoFillActive = true; 
        
        // Xóa listener cũ (nếu có)
        if (autoFillHoTenListener != null) {
            firstRowController.getTxtHoTen().textProperty().removeListener(autoFillHoTenListener);
        }
        if (autoFillGiayToListener != null) {
            firstRowController.getTxtSoGiayTo().textProperty().removeListener(autoFillGiayToListener);
        }

        // Tạo listener mới
        autoFillHoTenListener = (obs, oldVal, newVal) -> {
            if (isAutoFillActive) txtNguoiMuaHoTen.setText(newVal);
        };
        autoFillGiayToListener = (obs, oldVal, newVal) -> {
            if (isAutoFillActive) txtNguoiMuaSoGiayTo.setText(newVal);
        };

        // Gán listener
        firstRowController.getTxtHoTen().textProperty().addListener(autoFillHoTenListener);
        firstRowController.getTxtSoGiayTo().textProperty().addListener(autoFillGiayToListener);
    }


    // Hàm này được gọi bởi HanhKhachRowController
    public void updateTongThanhTien() {
        double tong = 0;
        for (HanhKhachRowController row : rowControllers) {
            tong += row.getThanhTien();
        }
        lblTongThanhTien.setText(moneyFormatter.format(tong));
    }

    @FXML
    private void handleTiepTheo() {
        // 1. Validation
        for (HanhKhachRowController row : rowControllers) {
            if (row.getHoTen().isEmpty() || row.getSoGiayTo().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Vui lòng nhập đầy đủ thông tin (Họ tên, Số giấy tờ) cho tất cả hành khách.");
                return;
            }
        }
        
        if (txtNguoiMuaHoTen.getText().isEmpty() || txtNguoiMuaSoGiayTo.getText().isEmpty() ||
            txtNguoiMuaEmail.getText().isEmpty() || txtNguoiMuaSDT.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Vui lòng nhập đầy đủ thông tin người mua vé.");
            return;
        }

        // 2. Thu thập dữ liệu
        List<Map<String, Object>> danhSachHanhKhach = new ArrayList<>();
        for (int i = 0; i < allTickets.size(); i++) {
            HanhKhachRowController row = rowControllers.get(i);
            VeTamThoi ve = allTickets.get(i);
            
            Map<String, Object> hanhKhach = new HashMap<>();
            hanhKhach.put("veTamThoi", ve); 
            hanhKhach.put("hoTen", row.getHoTen());
            hanhKhach.put("soGiayTo", row.getSoGiayTo());
            hanhKhach.put("doiTuong", row.getDoiTuong()); // SỬA: Giờ đây là LoaiVe
            hanhKhach.put("thanhTien", row.getThanhTien());
            danhSachHanhKhach.add(hanhKhach);
        }
        
        Map<String, String> nguoiMuaVe = new HashMap<>();
        nguoiMuaVe.put("hoTen", txtNguoiMuaHoTen.getText());
        nguoiMuaVe.put("soGiayTo", txtNguoiMuaSoGiayTo.getText());
        nguoiMuaVe.put("email", txtNguoiMuaEmail.getText());
        nguoiMuaVe.put("sdt", txtNguoiMuaSDT.getText());

        // 3. Gửi dữ liệu qua MainController
        mainController.setUserData("danhSachHanhKhachDaNhap", danhSachHanhKhach);
        mainController.setUserData("thongTinNguoiMua", nguoiMuaVe);
        mainController.setUserData("tongThanhTien", lblTongThanhTien.getText());

        // 4. Chuyển bước
        mainController.loadContent("step-4.fxml");
    }
    
    @FXML 
    private void handleQuayLai() { 
        mainController.loadContent("step-2.fxml"); 
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setTitle("Thông báo");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
