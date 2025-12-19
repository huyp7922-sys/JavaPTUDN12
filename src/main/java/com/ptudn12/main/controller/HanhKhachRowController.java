package com.ptudn12.main.controller;

import com.ptudn12.main.dao.KhachHangDAO;
import com.ptudn12.main.entity.KhachHang;
import com.ptudn12.main.enums.LoaiVe;
import com.ptudn12.main.controller.VeTamThoi;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class HanhKhachRowController {
    @FXML private TextField txtHoTen;
    @FXML private ComboBox<LoaiVe> comboDoiTuong;
    @FXML private TextField txtSoGiayTo;
    @FXML private Label lblTenTauDi;
    @FXML private Label lblThoiGianDi;
    @FXML private Label lblTenTauVe;
    @FXML private Label lblThoiGianVe;
    @FXML private Label lblThongTinChoDi;
    @FXML private Label lblLoaiToaDi;
    @FXML private Label lblThongTinChoVe;
    @FXML private Label lblLoaiToaVe;
    @FXML private Label lblGiaVe;
    @FXML private Label lblGiamGia;
    @FXML private Label lblBaoHiem;
    @FXML private Label lblThanhTien;
    
    @FXML private VBox columnHanhKhach;
    @FXML private VBox columnChuyenTau;
    @FXML private VBox columnChoNgoi;
    @FXML private VBox columnGiaVe;
    @FXML private VBox columnGiamGia;
    @FXML private VBox columnBaoHiem;
    @FXML private VBox columnThanhTien;
    
    @FXML private Button btnChonNgaySinh;
    @FXML private HBox boxMaVeNguoiLon;
    @FXML private TextField txtMaVeNguoiLon;
    
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();
    private ChangeListener<String> dataChangeListener;
    
    // Getters VBox
    public VBox getColumnHanhKhach() { return columnHanhKhach; }
    public VBox getColumnChuyenTau() { return columnChuyenTau; }
    public VBox getColumnChoNgoi() { return columnChoNgoi; }
    public VBox getColumnGiaVe() { return columnGiaVe; }
    public VBox getColumnGiamGia() { return columnGiamGia; }
    public VBox getColumnBaoHiem() { return columnBaoHiem; }
    public VBox getColumnThanhTien() { return columnThanhTien; }

    private VeTamThoi veDi;
    private VeTamThoi veVe;
    private Step3Controller step3Controller;
    private DecimalFormat moneyFormatter = new DecimalFormat("#,##0 'VNĐ'");
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    private double giaVeGocDi;
    private double giaVeGocVe;
    private final double PHI_BAO_HIEM = 2000;
    
    private LocalDate ngaySinh = null;
    private boolean isFreeTicket = false;
    private boolean requiresAdultTicket = false;
    
    @FXML
    public void initialize() {
        // --- SAFE CHECK: Kiểm tra xem FXML inject có thành công không ---
        if (txtHoTen == null) {
            System.err.println("CRITICAL ERROR: txtHoTen is NULL inside Initialize!");
            // Không return để code chạy tiếp, nhưng log sẽ báo lỗi
        }

        comboDoiTuong.setItems(FXCollections.observableArrayList(LoaiVe.values()));
        comboDoiTuong.setValue(LoaiVe.VE_BINH_THUONG);

        comboDoiTuong.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) handleDoiTuongChange(newVal);
        });
        
        txtSoGiayTo.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleTimKiemHanhKhach(); 
                notifyDataChange(); 
            }
        });
        
        txtHoTen.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                notifyDataChange();
            }
        });
        
        if (columnHanhKhach != null) {
            columnHanhKhach.setMinWidth(320); // Đặt con số này khớp với prefWidth trong FXML
            columnHanhKhach.setPrefWidth(320);
        }
        
        hideExtraControls();
    }
    
    private void handleTimKiemHanhKhach() {
        if (txtSoGiayTo == null) return;
        String cccd = txtSoGiayTo.getText().trim();
        if (cccd.isEmpty()) return;

        KhachHang kh = khachHangDAO.timKhachHangTheoGiayTo(cccd);
        
        if (kh != null) {
            if (txtHoTen != null) txtHoTen.setText(kh.getTenKhachHang());
            comboDoiTuong.setValue(LoaiVe.VE_BINH_THUONG); 
            if (txtHoTen != null) txtHoTen.requestFocus();
        } else {
            // Logic khi không tìm thấy (nếu cần)
        }
    }
    
    public void setOnDataChange(ChangeListener<String> listener) {
        this.dataChangeListener = listener;
    }

    private void notifyDataChange() {
        if (dataChangeListener != null) {
            dataChangeListener.changed(null, null, "updated");
        }
    }
    
    
    private void hideExtraControls() {
        btnChonNgaySinh.setVisible(false);
        btnChonNgaySinh.setManaged(false);
        boxMaVeNguoiLon.setVisible(false);
        boxMaVeNguoiLon.setManaged(false);
        requiresAdultTicket = false;
    }

    private void handleDoiTuongChange(LoaiVe selectedLoaiVe) {
        hideExtraControls();
        isFreeTicket = false;
        enableInputs();
        boolean ageRequired = (selectedLoaiVe == LoaiVe.VE_TRE_EM || selectedLoaiVe == LoaiVe.VE_NGUOI_LON_TUOI);
        if (ageRequired) {
            btnChonNgaySinh.setVisible(true);
            btnChonNgaySinh.setManaged(true);
            if (ngaySinh != null) validateAgeAndApplyPolicy();
            else updatePriceInternal(false);
        } else {
            if (!ageRequired) ngaySinh = null;
            updatePriceInternal(true);
        }
    }

    @FXML private void handleChonNgaySinh() { showDatePickerDialog(); }

    private void showDatePickerDialog() {
        Dialog<LocalDate> dialog = new Dialog<>();
        dialog.setTitle("Chọn ngày sinh");
        dialog.setHeaderText("Vui lòng chọn ngày sinh của hành khách.");
        DatePicker datePicker = new DatePicker();
        datePicker.setValue(ngaySinh != null ? ngaySinh : LocalDate.now().minusYears(10));
        VBox content = new VBox(10, datePicker);
        content.setPadding(new Insets(20));
        dialog.getDialogPane().setContent(content);
        ButtonType okButtonType = ButtonType.OK;
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) return datePicker.getValue();
            return null;
        });
        Optional<LocalDate> result = dialog.showAndWait();
        result.ifPresent(date -> {
            ngaySinh = date;
            validateAgeAndApplyPolicy();
        });
    }

    private void validateAgeAndApplyPolicy() {
        LoaiVe currentLoaiVe = comboDoiTuong.getValue();
        if (isFreeTicket) return;
        if (ngaySinh == null || currentLoaiVe == null || (currentLoaiVe != LoaiVe.VE_TRE_EM && currentLoaiVe != LoaiVe.VE_NGUOI_LON_TUOI)) {
             updatePriceInternal(true);
             return;
        }
        LocalDate today = LocalDate.now();
        int age = Period.between(ngaySinh, today).getYears();
        boolean ageIsValidForDiscount = false;
        hideExtraControls();
        btnChonNgaySinh.setVisible(true); btnChonNgaySinh.setManaged(true);
        boolean resetComboBox = false;
        if (currentLoaiVe == LoaiVe.VE_TRE_EM) {
            if (age < 6) {
                handleFreeTicket();
                return;
            } else if (age >= 6 && age < 10) {
                ageIsValidForDiscount = true;
                requiresAdultTicket = true;
                boxMaVeNguoiLon.setVisible(true); boxMaVeNguoiLon.setManaged(true);
            } else {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng kiểm tra độ tuổi!");
                resetComboBox = true;
            }
        } else if (currentLoaiVe == LoaiVe.VE_NGUOI_LON_TUOI) {
            if (age >= 60) ageIsValidForDiscount = true;
            else {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng kiểm tra độ tuổi!");
                resetComboBox = true;
            }
        }
        updatePriceInternal(ageIsValidForDiscount);
        if (resetComboBox) comboDoiTuong.setValue(LoaiVe.VE_BINH_THUONG);
    }

    private void handleFreeTicket() {
        isFreeTicket = true;
        requiresAdultTicket = false;
        showAlert(Alert.AlertType.INFORMATION, "Thông báo miễn vé", "Trẻ em dưới 6 tuổi được miễn vé.");
        lblGiaVe.setText("Miễn phí");
        lblGiamGia.setText("-");
        lblBaoHiem.setText("-");
        lblThanhTien.setText("Miễn phí");
        lblThanhTien.setStyle("-fx-font-weight: bold; -fx-text-fill: #2ecc71;");
        comboDoiTuong.setDisable(true);
        btnChonNgaySinh.setDisable(true);
        boxMaVeNguoiLon.setVisible(false); boxMaVeNguoiLon.setManaged(false);
        if (step3Controller != null) {
            step3Controller.updateTongThanhTien();
            if (veDi != null) step3Controller.requestCancelTicket(veDi.getMaCho(), true);
            if (veVe != null) step3Controller.requestCancelTicket(veVe.getMaCho(), false);
        }
    }

    private void enableInputs() {
         comboDoiTuong.setDisable(false);
         btnChonNgaySinh.setDisable(false);
         lblThanhTien.setStyle("-fx-font-weight: bold; -fx-text-fill: #c0392b;");
    }

    public void setData(VeTamThoi veDi, VeTamThoi veVe, Step3Controller step3Controller) {
        this.veDi = veDi;
        this.veVe = veVe;
        this.step3Controller = step3Controller;
        this.giaVeGocDi = (veDi != null) ? veDi.getGiaVe() - PHI_BAO_HIEM : 0;
        this.giaVeGocVe = (veVe != null) ? veVe.getGiaVe() - PHI_BAO_HIEM : 0;

        if (veDi != null) {
            lblTenTauDi.setText("Tàu " + veDi.getLichTrinh().getTau().getMacTau() + " (Đi)");
            lblThoiGianDi.setText(veDi.getLichTrinh().getNgayGioKhoiHanh().format(timeFormatter));
            lblThongTinChoDi.setText("Toa " + veDi.getChiTietToa().getToa().getMaToa() + " - Ghế " + veDi.getChiTietToa().getSoThuTu());
            lblLoaiToaDi.setText(veDi.getChiTietToa().getToa().getLoaiToa().getDescription());
        } else lblTenTauDi.setText("Lỗi vé đi");

        if (veVe != null) {
            lblTenTauVe.setText("Tàu " + veVe.getLichTrinh().getTau().getMacTau() + " (Về)");
            lblThoiGianVe.setText(veVe.getLichTrinh().getNgayGioKhoiHanh().format(timeFormatter));
            lblThongTinChoVe.setText("Toa " + veVe.getChiTietToa().getToa().getMaToa() + " - Ghế " + veVe.getChiTietToa().getSoThuTu());
            lblLoaiToaVe.setText(veVe.getChiTietToa().getToa().getLoaiToa().getDescription());
            lblTenTauVe.setVisible(true); lblTenTauVe.setManaged(true);
            lblThoiGianVe.setVisible(true); lblThoiGianVe.setManaged(true);
            lblThongTinChoVe.setVisible(true); lblThongTinChoVe.setManaged(true);
            lblLoaiToaVe.setVisible(true); lblLoaiToaVe.setManaged(true);
        } else {
            lblTenTauVe.setVisible(false); lblTenTauVe.setManaged(false);
            lblThoiGianVe.setVisible(false); lblThoiGianVe.setManaged(false);
            lblThongTinChoVe.setVisible(false); lblThongTinChoVe.setManaged(false);
            lblLoaiToaVe.setVisible(false); lblLoaiToaVe.setManaged(false);
        }

        double tongGiaVe = (veDi != null ? veDi.getGiaVe() : 0) + (veVe != null ? veVe.getGiaVe() : 0);
        double tongBaoHiem = (veDi != null ? PHI_BAO_HIEM : 0) + (veVe != null ? PHI_BAO_HIEM : 0);
        lblGiaVe.setText(moneyFormatter.format(tongGiaVe));
        lblBaoHiem.setText(moneyFormatter.format(tongBaoHiem));
        updatePrice();
    }

    private void updatePrice() {
        boolean ageIsValid = !(comboDoiTuong.getValue() == LoaiVe.VE_TRE_EM || comboDoiTuong.getValue() == LoaiVe.VE_NGUOI_LON_TUOI) || ngaySinh == null;
        if (!ageIsValid) updatePriceInternal(false);
        else validateAgeAndApplyPolicy();
    }

    private void updatePriceInternal(boolean ageIsValidForDiscount) {
        if (isFreeTicket) return;
        LoaiVe doiTuong = comboDoiTuong.getValue();
        if (doiTuong == null) return;
        double heSoGiamApDung = 0.0;
        if (doiTuong == LoaiVe.VE_BINH_THUONG || doiTuong == LoaiVe.VE_HSSV) heSoGiamApDung = doiTuong.getHeSoGiamGia();
        else if (ageIsValidForDiscount) heSoGiamApDung = doiTuong.getHeSoGiamGia();

        double giamGiaDi = giaVeGocDi * heSoGiamApDung;
        double giamGiaVe = giaVeGocVe * heSoGiamApDung;
        double tongGiamGia = giamGiaDi + giamGiaVe;
        double thanhTienDi = (veDi != null ? veDi.getGiaVe() : 0) - giamGiaDi;
        double thanhTienVe = (veVe != null ? veVe.getGiaVe() : 0) - giamGiaVe;
        double tongThanhTien = thanhTienDi + thanhTienVe;

        lblGiamGia.setText("- " + moneyFormatter.format(tongGiamGia));
        lblThanhTien.setText(moneyFormatter.format(tongThanhTien));
        if (step3Controller != null) step3Controller.updateTongThanhTien();
    }

    public double getThanhTien() {
        if (isFreeTicket) return 0.0;
        LoaiVe doiTuong = comboDoiTuong.getValue();
        double tongGiaVe = (veDi != null ? veDi.getGiaVe() : 0) + (veVe != null ? veVe.getGiaVe() : 0);
        if (doiTuong == null) return tongGiaVe;
        double heSoGiamApDung = 0.0;
        boolean ageRequired = (doiTuong == LoaiVe.VE_TRE_EM || doiTuong == LoaiVe.VE_NGUOI_LON_TUOI);
        if (!ageRequired) heSoGiamApDung = doiTuong.getHeSoGiamGia();
        else if (ngaySinh != null) {
             int age = Period.between(ngaySinh, LocalDate.now()).getYears();
             if ((doiTuong == LoaiVe.VE_TRE_EM && age >= 6 && age < 10) || (doiTuong == LoaiVe.VE_NGUOI_LON_TUOI && age >= 60)) {
                  heSoGiamApDung = doiTuong.getHeSoGiamGia();
             }
        }
        double giamGiaDi = giaVeGocDi * heSoGiamApDung;
        double giamGiaVe = giaVeGocVe * heSoGiamApDung;
        return tongGiaVe - (giamGiaDi + giamGiaVe);
    }

    // --- GETTERS AN TOÀN (Sửa lỗi NullPointerException) ---
    public String getHoTen() { 
        return txtHoTen != null ? txtHoTen.getText() : ""; 
    }
    public String getSoGiayTo() { 
        return txtSoGiayTo != null ? txtSoGiayTo.getText() : ""; 
    }
    public TextField getTxtHoTen() { return txtHoTen; }
    public TextField getTxtSoGiayTo() { return txtSoGiayTo; }
    public LoaiVe getDoiTuong() { return comboDoiTuong != null ? comboDoiTuong.getValue() : LoaiVe.VE_BINH_THUONG; }
    public LocalDate getNgaySinh() { return ngaySinh; }
    public String getMaVeNguoiLon() { return txtMaVeNguoiLon != null ? txtMaVeNguoiLon.getText() : ""; }
    public boolean isFreeTicket() { return isFreeTicket; }
    public boolean isRequiresAdultTicket() { return requiresAdultTicket; }
    
    public VeTamThoi getVeDi() { return veDi; }
    public VeTamThoi getVeVe() { return veVe; }
    
    public ComboBox<LoaiVe> getComboDoiTuong() {
        return this.comboDoiTuong;
    }
    
    public void setNgaySinh(LocalDate date) {
        this.ngaySinh = date;
        
        // Nếu đã có loại vé (được set từ trước) và có ngày sinh, hãy chạy lại validation
        // để hiển thị đúng các nút chức năng (ví dụ: hiện ô nhập mã vé người lớn nếu là trẻ em 6-10t)
        if (this.comboDoiTuong.getValue() != null && date != null) {
            LoaiVe loaiVe = this.comboDoiTuong.getValue();
            if (loaiVe == LoaiVe.VE_TRE_EM || loaiVe == LoaiVe.VE_NGUOI_LON_TUOI) {
                btnChonNgaySinh.setVisible(true);
                btnChonNgaySinh.setManaged(true);
            }
            
            validateAgeAndApplyPolicy();
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