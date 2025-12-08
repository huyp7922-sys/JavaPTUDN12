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
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

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
    
    // Khai báo @FXML cho các VBox cột
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
    
    // Getters cho các VBox cột
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
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private double giaVeGocDi;
    private double giaVeGocVe;
    private final double PHI_BAO_HIEM = 2000;
    
    // BIẾN LƯU TRẠNG THÁI
    private LocalDate ngaySinh = null;
    private boolean isFreeTicket = false; // Cờ đánh dấu vé miễn phí
    private boolean requiresAdultTicket = false; // Cờ yêu cầu mã vé NL
    
    
    
    @FXML
    public void initialize() {
        comboDoiTuong.setItems(FXCollections.observableArrayList(LoaiVe.values()));
        comboDoiTuong.setValue(LoaiVe.VE_BINH_THUONG);

        // Listener giữ nguyên
        comboDoiTuong.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                handleDoiTuongChange(newVal);
            }
        });

        hideExtraControls();
    }
    
    private void hideExtraControls() {
        btnChonNgaySinh.setVisible(false);
        btnChonNgaySinh.setManaged(false);
        boxMaVeNguoiLon.setVisible(false);
        boxMaVeNguoiLon.setManaged(false);
        requiresAdultTicket = false; // Reset
    }
    
    // Hàm xử lý khi ComboBox Đối tượng thay đổi
    private void handleDoiTuongChange(LoaiVe selectedLoaiVe) {
        hideExtraControls();
        isFreeTicket = false;
        enableInputs();

        boolean ageRequired = (selectedLoaiVe == LoaiVe.VE_TRE_EM || selectedLoaiVe == LoaiVe.VE_NGUOI_LON_TUOI);

        if (ageRequired) {
            btnChonNgaySinh.setVisible(true);
            btnChonNgaySinh.setManaged(true);
            if (ngaySinh != null) {
                // Đã có ngày sinh -> Kiểm tra lại tuổi ngay
                validateAgeAndApplyPolicy();
            } else {
                // Chưa có ngày sinh -> Cập nhật giá tạm thời (không giảm theo tuổi)
                updatePriceInternal(false);
            }
        } else {
            // Không cần tuổi
            ngaySinh = null;
            // Cập nhật giá trực tiếp, tuổi luôn hợp lệ
            updatePriceInternal(true);
        }
    }

    // Hàm xử lý khi nhấn nút "Chọn ngày sinh"
    @FXML
    private void handleChonNgaySinh() {
        showDatePickerDialog();
    }

    // Hàm hiển thị Dialog chọn ngày sinh
    private void showDatePickerDialog() {
        Dialog<LocalDate> dialog = new Dialog<>();
        dialog.setTitle("Chọn ngày sinh");
        dialog.setHeaderText("Vui lòng chọn ngày sinh của hành khách.");

        DatePicker datePicker = new DatePicker();
        datePicker.setValue(ngaySinh != null ? ngaySinh : LocalDate.now().minusYears(10)); // Giá trị mặc định

        VBox content = new VBox(10, datePicker);
        content.setPadding(new Insets(20));
        dialog.getDialogPane().setContent(content);

        ButtonType okButtonType = ButtonType.OK;
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                return datePicker.getValue();
            }
            return null;
        });

        Optional<LocalDate> result = dialog.showAndWait();

        result.ifPresent(date -> {
            // Lưu ngày sinh
            ngaySinh = date;
            // Kiểm tra tuổi và áp dụng chính sách
            validateAgeAndApplyPolicy();
        });
    }

    // Hàm kiểm tra tuổi và áp dụng chính sách (miễn phí, yêu cầu mã vé NL, giảm giá)
    private void validateAgeAndApplyPolicy() {
        LoaiVe currentLoaiVe = comboDoiTuong.getValue();
        // THÊM: Nếu vé đã miễn phí thì không làm gì cả
        if (isFreeTicket) return;
        
        // Nếu không cần tuổi hoặc chưa chọn ngày sinh -> coi như tuổi hợp lệ (hoặc không áp dụng giảm giá tuổi)
        if (ngaySinh == null || currentLoaiVe == null || (currentLoaiVe != LoaiVe.VE_TRE_EM && currentLoaiVe != LoaiVe.VE_NGUOI_LON_TUOI)) {
             updatePriceInternal(true); // Cập nhật giá bình thường (hoặc HSSV)
             return;
        }


        LocalDate today = LocalDate.now();
        int age = Period.between(ngaySinh, today).getYears();
        boolean ageIsValidForDiscount = false; // Mặc định là không hợp lệ

        // Chỉ ẩn/hiện control liên quan đến tuổi
        hideExtraControls(); // Ẩn ô nhập mã vé trước
        btnChonNgaySinh.setVisible(true); btnChonNgaySinh.setManaged(true); // Luôn hiện nút

        boolean resetComboBox = false; // Cờ để biết có cần reset không

        if (currentLoaiVe == LoaiVe.VE_TRE_EM) {
            if (age < 6) {
                handleFreeTicket();
                return; // Miễn phí, kết thúc
            } else if (age >= 6 && age < 10) {
                ageIsValidForDiscount = true;
                requiresAdultTicket = true;
                boxMaVeNguoiLon.setVisible(true); boxMaVeNguoiLon.setManaged(true);
            } else { // >= 10 tuổi
                showAlert(Alert.AlertType.WARNING, "Tuổi không phù hợp", "Vui lòng kiểm tra lại");
                resetComboBox = true; // Đánh dấu cần reset
            }
        } else if (currentLoaiVe == LoaiVe.VE_NGUOI_LON_TUOI) {
            if (age >= 60) {
                ageIsValidForDiscount = true;
            } else { // < 60 tuổi
                showAlert(Alert.AlertType.WARNING, "Tuổi không phù hợp", "Vui lòng kiểm tra lại");
                resetComboBox = true; // Đánh dấu cần reset
            }
        }
        // Các loại khác không cần xử lý ở đây

        // Cập nhật giá dựa trên kết quả kiểm tra
        updatePriceInternal(ageIsValidForDiscount);

        // Reset ComboBox SAU KHI đã cập nhật giá xong (nếu cần)
        // Việc này sẽ trigger handleDoiTuongChange nhưng không gây lặp vô tận
        if (resetComboBox) {
            comboDoiTuong.setValue(LoaiVe.VE_BINH_THUONG);
        }
    }

    // Hàm xử lý khi vé được miễn phí
    private void handleFreeTicket() {
        isFreeTicket = true;
        requiresAdultTicket = false; // Không cần mã vé NL nữa

        // Hiển thị thông báo
        showAlert(Alert.AlertType.INFORMATION, "Thông báo miễn vé",
                  "Trẻ em dưới 6 tuổi được miễn vé và sử dụng chung chỗ của người lớn đi kèm.");

        // Cập nhật UI
        lblGiaVe.setText("Miễn phí");
        lblGiamGia.setText("-");
        lblBaoHiem.setText("-");
        lblThanhTien.setText("Miễn phí");
        lblThanhTien.setStyle("-fx-font-weight: bold; -fx-text-fill: #2ecc71;"); // Màu xanh lá

        // Vô hiệu hóa input (tùy chọn)
        // txtHoTen.setDisable(true);
        // txtSoGiayTo.setDisable(true);
        comboDoiTuong.setDisable(true);
        btnChonNgaySinh.setDisable(true);

        // Ẩn ô nhập mã vé NL (nếu đang hiện)
        boxMaVeNguoiLon.setVisible(false); boxMaVeNguoiLon.setManaged(false);

        // Báo cho Step3Controller cập nhật tổng tiền
        if (step3Controller != null) {
            step3Controller.updateTongThanhTien();
        }

        // --- LOGIC HỦY VÉ NGẦM ---
        System.out.println("HanhKhachRowController: Yêu cầu hủy vé ngầm cho hành khách " + getHoTen());
        if (step3Controller != null) {
            // Hủy vé chiều đi (nếu có)
            if (veDi != null) {
                step3Controller.requestCancelTicket(veDi.getMaCho(), true);
            }
            // Hủy vé chiều về (nếu có)
            if (veVe != null) {
                step3Controller.requestCancelTicket(veVe.getMaCho(), false);
            }
        } else {
            System.err.println("Lỗi: step3Controller bị null, không thể yêu cầu hủy vé ngầm.");
        }
        
    }

    // Hàm bật lại các input
    private void enableInputs() {
         // txtHoTen.setDisable(false);
         // txtSoGiayTo.setDisable(false);
         comboDoiTuong.setDisable(false);
         btnChonNgaySinh.setDisable(false);
         lblThanhTien.setStyle("-fx-font-weight: bold; -fx-text-fill: #c0392b;"); // Reset style thành tiền
    }

    // Hàm này được gọi từ Step3Controller để nạp dữ liệu
    public void setData(VeTamThoi veDi, VeTamThoi veVe, Step3Controller step3Controller) {
        this.veDi = veDi;
        this.veVe = veVe;
        this.step3Controller = step3Controller;

        this.giaVeGocDi = (veDi != null) ? veDi.getGiaVe() - PHI_BAO_HIEM : 0;
        this.giaVeGocVe = (veVe != null) ? veVe.getGiaVe() - PHI_BAO_HIEM : 0;

        // --- Populate UI ---

        // Cột 2 & 3 (Chiều đi - Luôn dùng veDi)
        if (veDi != null) {
            lblTenTauDi.setText("Tàu " + veDi.getLichTrinh().getTau().getMacTau() + " (Đi)");
            lblThoiGianDi.setText(veDi.getLichTrinh().getNgayGioKhoiHanh().format(timeFormatter));
            lblThongTinChoDi.setText("Toa " + veDi.getChiTietToa().getToa().getMaToa() +
                                   " - Ghế " + veDi.getChiTietToa().getSoThuTu());
            lblLoaiToaDi.setText(veDi.getChiTietToa().getToa().getLoaiToa().getDescription());
        } else {
             lblTenTauDi.setText("Lỗi vé đi"); // Xử lý lỗi
        }

        // Cột 2 & 3 (Chiều về - Chỉ hiển thị và dùng veVe nếu có)
        if (veVe != null) {
            lblTenTauVe.setText("Tàu " + veVe.getLichTrinh().getTau().getMacTau() + " (Về)");
            lblThoiGianVe.setText(veVe.getLichTrinh().getNgayGioKhoiHanh().format(timeFormatter));
            lblThongTinChoVe.setText("Toa " + veVe.getChiTietToa().getToa().getMaToa() +
                                     " - Ghế " + veVe.getChiTietToa().getSoThuTu());
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

        // Cột 4-7 (Tổng)
        // Tính tổng giá vé (đã bao gồm bảo hiểm)
        double tongGiaVe = (veDi != null ? veDi.getGiaVe() : 0) + (veVe != null ? veVe.getGiaVe() : 0);
        // Tính tổng bảo hiểm
        double tongBaoHiem = (veDi != null ? PHI_BAO_HIEM : 0) + (veVe != null ? PHI_BAO_HIEM : 0);
        lblGiaVe.setText(moneyFormatter.format(tongGiaVe));
        lblBaoHiem.setText(moneyFormatter.format(tongBaoHiem));

        updatePrice(); // Cập nhật giảm giá & thành tiền dựa trên tổng giá gốc
    }

    // Hàm này được gọi khi đối tượng thay đổi hoặc ngày sinh được chọn
    private void updatePrice() {
        // Mặc định là tuổi không hợp lệ nếu cần kiểm tra
        boolean ageIsValid = !(comboDoiTuong.getValue() == LoaiVe.VE_TRE_EM || comboDoiTuong.getValue() == LoaiVe.VE_NGUOI_LON_TUOI) || ngaySinh == null;
        if (!ageIsValid) {
             // Nếu cần ngày sinh mà chưa có, hoặc tuổi không đúng -> không giảm giá theo tuổi
             updatePriceInternal(false);
        } else {
             // Nếu không cần ngày sinh hoặc ngày sinh đã có và hợp lệ (sẽ được kiểm tra lại trong validateAge)
             validateAgeAndApplyPolicy(); // Gọi hàm kiểm tra tuổi
        }
    }


    // Hàm nội bộ để tính và cập nhật giá, nhận cờ tuổi hợp lệ
    private void updatePriceInternal(boolean ageIsValidForDiscount) {
        if (isFreeTicket) return; // Không làm gì nếu vé đã miễn phí

        LoaiVe doiTuong = comboDoiTuong.getValue();
        if (doiTuong == null) return;

        double heSoGiamApDung = 0.0; // Hệ số giảm giá thực tế sẽ áp dụng

        // Xác định hệ số giảm giá dựa trên loại vé VÀ tuổi hợp lệ (nếu cần)
        if (doiTuong == LoaiVe.VE_BINH_THUONG) {
            heSoGiamApDung = doiTuong.getHeSoGiamGia(); // Luôn là 0
        } else if (doiTuong == LoaiVe.VE_HSSV) {
            heSoGiamApDung = doiTuong.getHeSoGiamGia(); // Luôn áp dụng
        } else if (ageIsValidForDiscount) { // Chỉ áp dụng giảm giá nếu tuổi hợp lệ
             heSoGiamApDung = doiTuong.getHeSoGiamGia();
        }
        // Nếu ageIsValidForDiscount = false, heSoGiamApDung sẽ giữ là 0.0

        // Tính tổng giảm giá và tổng thành tiền
        double giamGiaDi = giaVeGocDi * heSoGiamApDung;
        double giamGiaVe = giaVeGocVe * heSoGiamApDung;
        double tongGiamGia = giamGiaDi + giamGiaVe;

        double thanhTienDi = (veDi != null ? veDi.getGiaVe() : 0) - giamGiaDi;
        double thanhTienVe = (veVe != null ? veVe.getGiaVe() : 0) - giamGiaVe;
        double tongThanhTien = thanhTienDi + thanhTienVe;

        lblGiamGia.setText("- " + moneyFormatter.format(tongGiamGia));
        lblThanhTien.setText(moneyFormatter.format(tongThanhTien));

        if (step3Controller != null) {
            step3Controller.updateTongThanhTien();
        }
    }

    // SỬA ĐỔI: Lấy thành tiền dựa trên trạng thái isFreeTicket
    public double getThanhTien() {
        if (isFreeTicket) {
            return 0.0;
        }

        LoaiVe doiTuong = comboDoiTuong.getValue();
        double tongGiaVe = (veDi != null ? veDi.getGiaVe() : 0) + (veVe != null ? veVe.getGiaVe() : 0);

        if (doiTuong == null) {
            return tongGiaVe;
        }

        // Cần kiểm tra lại tuổi hợp lệ để tính đúng thành tiền gửi đi
        double heSoGiamApDung = 0.0;
        boolean ageRequired = (doiTuong == LoaiVe.VE_TRE_EM || doiTuong == LoaiVe.VE_NGUOI_LON_TUOI);

        if (!ageRequired) { // Bình thường hoặc HSSV
             heSoGiamApDung = doiTuong.getHeSoGiamGia();
        } else if (ngaySinh != null) { // Cần tuổi và đã có ngày sinh
             int age = Period.between(ngaySinh, LocalDate.now()).getYears();
             if ((doiTuong == LoaiVe.VE_TRE_EM && age >= 6 && age < 10) ||
                 (doiTuong == LoaiVe.VE_NGUOI_LON_TUOI && age >= 60)) {
                  heSoGiamApDung = doiTuong.getHeSoGiamGia();
             }
        }
        // Nếu tuổi không hợp lệ, heSoGiamApDung vẫn là 0.0

        double giamGiaDi = giaVeGocDi * heSoGiamApDung;
        double giamGiaVe = giaVeGocVe * heSoGiamApDung;
        double tongGiamGia = giamGiaDi + giamGiaVe;

        return tongGiaVe - tongGiamGia;
    }

    public VeTamThoi getVeDi() {
        return veDi;
    }

    public VeTamThoi getVeVe() {
        return veVe;
    }
    
    // --- Getters ---
    public TextField getTxtHoTen() { return txtHoTen; }
    public TextField getTxtSoGiayTo() { return txtSoGiayTo; }
    public String getHoTen() { return txtHoTen.getText(); }
    public String getSoGiayTo() { return txtSoGiayTo.getText(); }
    public LoaiVe getDoiTuong() { return comboDoiTuong.getValue(); }

    public LocalDate getNgaySinh() { return ngaySinh; }
    public String getMaVeNguoiLon() { return txtMaVeNguoiLon.getText(); }
    public boolean isFreeTicket() { return isFreeTicket; }
    public boolean isRequiresAdultTicket() { return requiresAdultTicket; }

    // Hàm tiện ích hiển thị Alert (có thể chuyển sang lớp Utils)
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
