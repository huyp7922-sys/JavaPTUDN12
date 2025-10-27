/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ptudn12.main.controller;

/**
 *
 * @author fo3cp
 */

// import com.ptudn12.main.model.VeTamThoi;
 import com.ptudn12.main.enums.LoaiVe;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;

public class HanhKhachRowController {
    // Cột 1
    @FXML private TextField txtHoTen;
    @FXML private ComboBox<LoaiVe> comboDoiTuong;
    @FXML private TextField txtSoGiayTo;
    
    // Cột 2 (MỚI)
    @FXML private Label lblTenTau;
    @FXML private Label lblThoiGianDi;
    
    // Cột 3 (Cập nhật)
    @FXML private Label lblThongTinCho;
    @FXML private Label lblLoaiToa; // (MỚI)
    
    // Cột 4-7
    @FXML private Label lblGiaVe;
    @FXML private Label lblGiamGia;
    @FXML private Label lblBaoHiem;
    @FXML private Label lblThanhTien;

    private VeTamThoi ve;
    private Step3Controller step3Controller;
    private DecimalFormat moneyFormatter = new DecimalFormat("#,##0 'VND'");
    
    // Thêm helper để định dạng thời gian
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    private double giaVeGoc;
    private final double PHI_BAO_HIEM = 2000; // Lấy từ logic của bạn

    @FXML
    public void initialize() {
        // Tải danh sách LoaiVe vào ComboBox
        comboDoiTuong.setItems(FXCollections.observableArrayList(LoaiVe.values()));
        comboDoiTuong.setValue(LoaiVe.VE_BINH_THUONG); // Mặc định là vé thường

        // Thêm listener để tự động cập nhật giá khi đổi đối tượng
        comboDoiTuong.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updatePrice();
            }
        });
    }

    // Hàm này được gọi từ Step3Controller để nạp dữ liệu
    public void setData(VeTamThoi ve, Step3Controller step3Controller) {
        this.ve = ve;
        this.step3Controller = step3Controller;
        this.giaVeGoc = ve.getGiaVe() - PHI_BAO_HIEM;

        // --- Populate UI (Đã cập nhật) ---
        
        // Cột 2: Thông tin chuyến tàu
        lblTenTau.setText("Tàu " + ve.getLichTrinh().getTau().getMacTau());
        lblThoiGianDi.setText(ve.getLichTrinh().getNgayGioKhoiHanh().format(timeFormatter));

        // Cột 3: Thông tin chỗ ngồi
        String chieu = ve.isChieuDi() ? " (Đi)" : " (Về)";
        lblThongTinCho.setText("Toa " + ve.getChiTietToa().getToa().getMaToa() + 
                              " - Ghế " + ve.getChiTietToa().getSoThuTu() + chieu);
        lblLoaiToa.setText(ve.getChiTietToa().getToa().getLoaiToa().getDescription()); // Dữ liệu mới

        // Cột 4-7
        lblGiaVe.setText(moneyFormatter.format(ve.getGiaVe()));
        lblBaoHiem.setText(moneyFormatter.format(PHI_BAO_HIEM));
        
        updatePrice(); // Cập nhật giảm giá & thành tiền
    }

    // Cập nhật lại cột Giảm giá và Thành tiền
    private void updatePrice() {
        LoaiVe doiTuong = comboDoiTuong.getValue();
        if (doiTuong == null) return;

        double tienGiam = giaVeGoc * doiTuong.getHeSoGiamGia();
        double thanhTien = ve.getGiaVe() - tienGiam;

        lblGiamGia.setText("- " + moneyFormatter.format(tienGiam));
        lblThanhTien.setText(moneyFormatter.format(thanhTien));

        // Báo cho Step3Controller cập nhật tổng tiền
        if (step3Controller != null) {
            step3Controller.updateTongThanhTien();
        }
    }

    // Hàm để Step3Controller lấy thành tiền
    public double getThanhTien() {
        LoaiVe doiTuong = comboDoiTuong.getValue();
        if (doiTuong == null) return ve.getGiaVe();
        
        double tienGiam = giaVeGoc * doiTuong.getHeSoGiamGia();
        return ve.getGiaVe() - tienGiam;
    }

    // --- Các hàm phục vụ cho Autofill ---
    public TextField getTxtHoTen() { return txtHoTen; }
    public TextField getTxtSoGiayTo() { return txtSoGiayTo; }
    
    // --- Các hàm lấy dữ liệu cuối cùng (cho Step 4) ---
    public String getHoTen() { return txtHoTen.getText(); }
    public String getSoGiayTo() { return txtSoGiayTo.getText(); }
    public LoaiVe getDoiTuong() { return comboDoiTuong.getValue(); }
}
