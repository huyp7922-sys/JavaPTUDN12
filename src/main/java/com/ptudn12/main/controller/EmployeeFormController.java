package com.ptudn12.main.controller;

import com.ptudn12.main.dao.NhanVienDAO;
import com.ptudn12.main.dao.TaiKhoanDAO;
import com.ptudn12.main.entity.NhanVien;
import com.ptudn12.main.entity.TaiKhoan;
import com.ptudn12.main.utils.PasswordGenerate;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.Period;

public class EmployeeFormController {
     
    @FXML private TextField maNVField;
    @FXML private TextField hoTenField;
    @FXML private TextField cccdField;
    @FXML private DatePicker ngaySinhPicker;
    @FXML private ComboBox<String> gioiTinhCombo;
    @FXML private TextField sdtField;
    @FXML private TextField emailField;
    @FXML private ComboBox<String> chucVuCombo;
    @FXML private ComboBox<String> trangThaiCombo;
    @FXML private CheckBox capTaiKhoanCheck;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    
    private NhanVienDAO nhanVienDAO;
    private TaiKhoanDAO taiKhoanDAO;
    private EmployeeManagementController parentController;
    private FormMode mode;
    private NhanVien currentEmployee;
    
    public enum FormMode {
        ADD, EDIT
    }
    
    @FXML
    public void initialize() {
        nhanVienDAO = new NhanVienDAO();
        taiKhoanDAO = new TaiKhoanDAO();
        
        setupComboBoxes();
        setupValidation();
    }
    
    /**
     * Thiết lập các ComboBox
     */
    private void setupComboBoxes() {
        gioiTinhCombo.getItems().addAll("Nam", "Nữ");
        gioiTinhCombo.setValue("Nam");
        
        chucVuCombo.getItems().addAll("Nhân viên", "Quản lý");
        chucVuCombo.setValue("Nhân viên");
        
        trangThaiCombo.getItems().addAll("đang làm", "tạm nghỉ", "đã nghỉ");
        trangThaiCombo.setValue("đang làm");
    }
    
    /**
     * Thiết lập validation cho các trường
     */
    private void setupValidation() {
        // Chỉ cho phép nhập số cho CCCD
        cccdField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                cccdField.setText(newValue.replaceAll("[^\\d]", ""));
            }
            if (newValue.length() > 12) {
                cccdField.setText(newValue.substring(0, 12));
            }
        });
        
        // Chỉ cho phép nhập số cho SĐT
        sdtField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                sdtField.setText(newValue.replaceAll("[^\\d]", ""));
            }
            if (newValue.length() > 10) {
                sdtField.setText(newValue.substring(0, 10));
            }
        });
    }
    
    /**
     * Đặt chế độ form (Thêm hoặc Sửa)
     */
    public void setMode(FormMode mode) {
        this.mode = mode;
        
        if (mode == FormMode.ADD) {
            maNVField.setText(nhanVienDAO.generateMaNhanVien());
            maNVField.setEditable(false);
            capTaiKhoanCheck.setSelected(true);
            capTaiKhoanCheck.setVisible(true);
        } else {
            capTaiKhoanCheck.setVisible(false);
        }
    }
    
    /**
     * Đặt controller cha
     */
    public void setParentController(EmployeeManagementController controller) {
        this.parentController = controller;
    }
    
    /**
     * Load dữ liệu nhân viên để sửa
     */
    public void loadEmployeeData(NhanVien nv) {
        this.currentEmployee = nv;
        
        maNVField.setText(nv.getMaNhanVien());
        maNVField.setEditable(false);
        hoTenField.setText(nv.getTenNhanVien());
        cccdField.setText(nv.getSoCCCD());
        ngaySinhPicker.setValue(nv.getNgaySinh());
        gioiTinhCombo.setValue(nv.getGioiTinhText());
        sdtField.setText(nv.getSoDienThoai());
        emailField.setText(nv.getEmail());
        chucVuCombo.setValue(nv.getChucVuText());
        trangThaiCombo.setValue(nv.getTinhTrangCV());
    }
    
    /**
     * Lưu thông tin nhân viên
     */
    @FXML
    private void save() {
        if (!validateInput()) {
            return;
        }
        
        NhanVien nv = new NhanVien();
        nv.setMaNhanVien(maNVField.getText().trim());
        nv.setTenNhanVien(hoTenField.getText().trim());
        nv.setSoCCCD(cccdField.getText().trim());
        nv.setNgaySinh(ngaySinhPicker.getValue());
        nv.setGioiTinh(gioiTinhCombo.getValue().equals("Nam"));
        nv.setSoDienThoai(sdtField.getText().trim());
        nv.setEmail(emailField.getText().trim());
        nv.setChucVu(chucVuCombo.getValue().equals("Quản lý"));
        nv.setTinhTrangCV(trangThaiCombo.getValue());
        
        boolean success = false;
        
        if (mode == FormMode.ADD) {
            success = nhanVienDAO.insert(nv);
            
            if (success && capTaiKhoanCheck.isSelected()) {
                // Tạo tài khoản cho nhân viên
                String matKhau = PasswordGenerate.generatePassword(
                    nv.getSoCCCD(), 
                    nv.getChucVu()
                );
                
                TaiKhoan tk = new TaiKhoan();
                tk.setNhanVien(nv);  // truyền đối tượng NhanVien
                tk.setMatKhau(matKhau);
                tk.setTrangThaiTK("danghoatdong");
                
                if (taiKhoanDAO.insert(tk)) {
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", 
                        "Thêm nhân viên thành công!\n" +
                        "Tài khoản: " + nv.getMaNhanVien() + "\n" +
                        "Mật khẩu: " + matKhau);
                } else {
                    showAlert(Alert.AlertType.WARNING, "Cảnh báo", 
                        "Thêm nhân viên thành công nhưng không thể tạo tài khoản!");
                }
            } else if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Thêm nhân viên thành công!");
            }
        } else {
            success = nhanVienDAO.update(nv);
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Cập nhật thông tin nhân viên thành công!");
            }
        }
        
        if (success) {
            if (parentController != null) {
                parentController.refreshTable();
            }
            close();
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể lưu thông tin nhân viên!");
        }
    }
    
    /**
     * Validate dữ liệu đầu vào
     */
    private boolean validateInput() {
        // Kiểm tra họ tên
        if (hoTenField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập họ tên!");
            hoTenField.requestFocus();
            return false;
        }
        
        // Kiểm tra CCCD
        String cccd = cccdField.getText().trim();
        if (cccd.isEmpty() || (cccd.length() != 12)) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "CCCD phải có 12 số!");
            cccdField.requestFocus();
            return false;
        }
        
        // Kiểm tra CCCD trùng (nếu thêm mới hoặc CCCD khác với CCCD cũ)
        if (mode == FormMode.ADD || 
            (currentEmployee != null && !cccd.equals(currentEmployee.getSoCCCD()))) {
            if (nhanVienDAO.isCCCDExists(cccd)) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "CCCD đã tồn tại!");
                cccdField.requestFocus();
                return false;
            }
        }
        
        // Kiểm tra ngày sinh
        if (ngaySinhPicker.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn ngày sinh!");
            ngaySinhPicker.requestFocus();
            return false;
        }
        
        // Kiểm tra tuổi >= 18
        int age = Period.between(ngaySinhPicker.getValue(), LocalDate.now()).getYears();
        if (age < 18) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Nhân viên phải đủ 18 tuổi!");
            ngaySinhPicker.requestFocus();
            return false;
        }
        
        // Kiểm tra số điện thoại
        String sdt = sdtField.getText().trim();
        if (sdt.isEmpty() || sdt.length() != 10) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Số điện thoại phải có 10 số!");
            sdtField.requestFocus();
            return false;
        }
        
        if (!sdt.matches("^(03|05|07|08|09)\\d{8}$")) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", 
                "Số điện thoại phải bắt đầu bằng 03, 05, 07, 08 hoặc 09!");
            sdtField.requestFocus();
            return false;
        }
        
        // Kiểm tra SĐT trùng
        if (mode == FormMode.ADD || 
            (currentEmployee != null && !sdt.equals(currentEmployee.getSoDienThoai()))) {
            if (nhanVienDAO.isSDTExists(sdt)) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Số điện thoại đã tồn tại!");
                sdtField.requestFocus();
                return false;
            }
        }
        
        // Kiểm tra email
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập email!");
            emailField.requestFocus();
            return false;
        }
        
        if (!email.matches("^[A-Za-z0-9+_.-]+@(gmail\\.com|outlook\\.com)$")) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", 
                "Email phải có định dạng @gmail.com hoặc @outlook.com!");
            emailField.requestFocus();
            return false;
        }
        
        // Kiểm tra email trùng (nếu thêm mới hoặc email khác với email cũ)
        if (mode == FormMode.ADD || 
            (currentEmployee != null && !email.equals(currentEmployee.getEmail()))) {
            if (nhanVienDAO.isEmailExists(email)) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Email đã tồn tại!");
                emailField.requestFocus();
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Đóng form
     */
    @FXML
    private void cancel() {
        close();
    }
    
    /**
     * Đóng cửa sổ
     */
    private void close() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
    
    /**
     * Hiển thị thông báo
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}