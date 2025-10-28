package com.ptudn12.main.controller;

import com.ptudn12.main.dao.NhanVienDAO;
import com.ptudn12.main.dao.TaiKhoanDAO;
import com.ptudn12.main.entity.NhanVien;
import com.ptudn12.main.entity.TaiKhoan;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.regex.Pattern;

public class ModelEmployeeManagementController {

    @FXML private TextField txtMaNV, txtHoTen, txtCCCD, txtChucVu, txtSdt, txtEmail, txtTenDangNhap;
    @FXML private PasswordField txtMatKhau;
    @FXML private DatePicker dpNgaySinh;
    @FXML private ComboBox<String> cbGioiTinh, cbTrangThai, cbVaiTro;
    @FXML private Button btnLuu, btnHuy;

    private boolean isUpdate = false;
    private NhanVienDAO nhanVienDAO = new NhanVienDAO();
    private TaiKhoanDAO taiKhoanDAO = new TaiKhoanDAO();

    public void initialize() {
        cbGioiTinh.getItems().addAll("Nam", "Nữ");
        cbTrangThai.getItems().addAll("Đang làm việc", "Tạm nghỉ", "Đã nghỉ");
        cbVaiTro.getItems().addAll("Nhân viên", "Quản lý");

        if (!isUpdate) {
            txtMaNV.setText(nhanVienDAO.generateMaNhanVien());
        }
    }

    public void setEmployeeData(NhanVien nv, TaiKhoan tk) {
        isUpdate = true;

        txtMaNV.setText(nv.getMaNhanVien());
        txtHoTen.setText(nv.getTenNhanVien());
        txtCCCD.setText(nv.getSoCCCD());
        dpNgaySinh.setValue(nv.getNgaySinh());
        cbGioiTinh.setValue(nv.getGioiTinhText());
        txtChucVu.setText(nv.getChucVuText());
        txtSdt.setText(nv.getSoDienThoai());
        txtEmail.setText(nv.getEmail());
        cbTrangThai.setValue(nv.getTinhTrangCV());

        // SỬA DÒNG NÀY (dòng 49):
        txtTenDangNhap.setText(tk.getNhanVien().getMaNhanVien());  // ← ĐỔI tk.getMaNhanVien() → tk.getNhanVien().getMaNhanVien()
        txtMatKhau.setText(tk.getMatKhau());
        cbVaiTro.setValue(nv.getChucVuText());

        txtMaNV.setDisable(true);
        txtCCCD.setDisable(true);
    }

    @FXML
    private void saveEmployee() {
        if (!validate()) return;

        boolean gioiTinh = cbGioiTinh.getValue().equals("Nam");
        boolean chucVu = cbVaiTro.getValue().equals("Quản lý");

        NhanVien nv = new NhanVien(
                txtMaNV.getText(),
                txtHoTen.getText(),
                txtCCCD.getText(),
                dpNgaySinh.getValue(),
                gioiTinh,
                chucVu,
                txtSdt.getText(),
                txtEmail.getText(),
                cbTrangThai.getValue()
        );

        TaiKhoan tk = new TaiKhoan(
                nv,                    
                txtMatKhau.getText(),
                cbTrangThai.getValue()
        );

        boolean success;
        if (isUpdate) {
            success = nhanVienDAO.update(nv)
                    && taiKhoanDAO.update(tk);
        } else {
            success = nhanVienDAO.insert(nv)
                    && taiKhoanDAO.insert(tk);
        }

        if (!success) {
            showAlert("Lưu dữ liệu thất bại");
            return;
        }

        closeWindow();
        NhanVienTableController.refreshTable();
    }

    private boolean validate() {
        if (txtHoTen.getText().isEmpty()) return showError("Họ tên không được trống");
        if (!Pattern.matches("^\\d{9,12}$", txtCCCD.getText())) return showError("CCCD từ 9 đến 12 số");
        if (txtSdt.getText().isEmpty()) return showError("SĐT không được trống");
        if (dpNgaySinh.getValue() == null
                || dpNgaySinh.getValue().isAfter(LocalDate.now()))
            return showError("Ngày sinh không hợp lệ");
        if (!txtEmail.getText().contains("@")) return showError("Email không hợp lệ");
        if (txtTenDangNhap.getText().isEmpty()) return showError("Tên đăng nhập không được trống");
        if (txtMatKhau.getText().isEmpty()) return showError("Mật khẩu không được trống");

        if (!isUpdate) {
            if (nhanVienDAO.isCCCDExists(txtCCCD.getText()))
                return showError("CCCD đã tồn tại");
            if (nhanVienDAO.isEmailExists(txtEmail.getText()))
                return showError("Email đã tồn tại");
            if (nhanVienDAO.isSDTExists(txtSdt.getText()))
                return showError("SĐT đã tồn tại");
            if (taiKhoanDAO.exists(txtTenDangNhap.getText()))
                return showError("Tên đăng nhập đã tồn tại");
        }
        return true;
    }

    private boolean showError(String msg) {
        showAlert(msg);
        return false;
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    @FXML
    private void cancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) btnLuu.getScene().getWindow();
        stage.close();
    }
}
