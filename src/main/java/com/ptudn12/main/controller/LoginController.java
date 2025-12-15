package com.ptudn12.main.controller;

import com.ptudn12.main.dao.NhanVienDAO;
import com.ptudn12.main.dao.TaiKhoanDAO;
import com.ptudn12.main.entity.NhanVien;
import com.ptudn12.main.entity.TaiKhoan;
import com.ptudn12.main.utils.SessionManager;
import com.ptudn12.main.utils.EmailService;

import java.io.IOException;
import java.util.Optional;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonBar;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Label;
import javafx.geometry.Insets;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    
    private NhanVienDAO nhanVienDAO = new NhanVienDAO();
    private TaiKhoanDAO taiKhoanDAO = new TaiKhoanDAO();

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        // Kiểm tra tài khoản từ database
        if (authenticateDatabase(username, password)) {
            return; // Đã xử lý trong hàm
        }
    }

    /**
     * Xác thực tài khoản từ database
     */
    private boolean authenticateDatabase(String username, String password) {
        try {
            // Tìm tài khoản
            TaiKhoan taiKhoan = taiKhoanDAO.findById(username);
            
            if (taiKhoan == null) {
                System.out.println("Không tìm thấy tài khoản: " + username);
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Tên đăng nhập hoặc mật khẩu không đúng");
                return false;
            }
            
            // Kiểm tra mật khẩu
            if (!taiKhoan.getMatKhau().equals(password)) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Tên đăng nhập hoặc mật khẩu không đúng");
                return false;
            }

            // Nếu là mật khẩu tạm (ví dụ vừa reset), bắt buộc phải đổi
            if (taiKhoan.isMatKhauTam()) { // Đảm bảo Entity TaiKhoan đã có hàm này
                boolean doiThanhCong = showForceChangePasswordDialog(taiKhoan);
                
                if (!doiThanhCong) {
                    // Nếu người dùng tắt hộp thoại hoặc đổi thất bại thì không cho đăng nhập
                    showAlert(Alert.AlertType.WARNING, "Yêu cầu", "Bạn bắt buộc phải đổi mật khẩu để tiếp tục sử dụng hệ thống!");
                    return false;
                }
                // Nếu đổi thành công, biến taiKhoan lúc này đã có mật khẩu mới và matKhauTam = false (do tham chiếu)
                // Code sẽ chạy tiếp xuống dưới để login bình thường
            }
            // Lấy thông tin nhân viên
            NhanVien nhanVien = taiKhoan.getNhanVien();
            
            if (nhanVien == null) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không tìm thấy thông tin nhân viên!");
                return false;
            }
            
            String trangThaiTK = taiKhoan.getTrangThaiTK().trim().toLowerCase();
            String tinhTrangCV = nhanVien.getTinhTrangCV().trim().toLowerCase();
            
            // Tài khoản bị ngừng hoạt động (KHÔNG CHO ĐĂNG NHẬP)
            if (trangThaiTK.equals("ngunghan")) {
                showAlert(Alert.AlertType.ERROR, "Tài khoản đã ngừng hoạt động", 
                    "Tài khoản của bạn đã bị ngừng hoạt động!\n" +
                    "Vui lòng liên hệ quản trị viên để được hỗ trợ.");
                return false;
            }
            
            // Nhân viên đã nghỉ việc (KHÔNG CHO ĐĂNG NHẬP)
            if (tinhTrangCV.equals("đã nghỉ")) {
                showAlert(Alert.AlertType.ERROR, "Nhân viên đã nghỉ việc", 
                    "Bạn đã nghỉ việc và không thể đăng nhập vào hệ thống!\n" +
                    "Vui lòng liên hệ phòng nhân sự để biết thêm chi tiết.");
                return false;
            }
            
            // Tài khoản tạm ngưng HOẶC Nhân viên tạm nghỉ (CHO ĐĂNG NHẬP VỚI XÁC NHẬN)
            if (trangThaiTK.equals("tamngung") || tinhTrangCV.equals("tạm nghỉ")) {
                boolean shouldActivate = showConfirmationDialog(
                    "Tài khoản/Nhân viên đang tạm ngưng",
                    "Thông tin trạng thái:",
                    "Bạn có muốn mở lại tài khoản và tiếp tục làm việc không?"
                );
                
                if (shouldActivate) {
                    // Cập nhật trạng thái về bình thường
                    boolean updateSuccess = activateAccountAndEmployee(taiKhoan, nhanVien);
                    
                    if (!updateSuccess) {
                        showAlert(Alert.AlertType.ERROR, "Lỗi", 
                            "Không thể cập nhật trạng thái tài khoản!\n" +
                            "Vui lòng liên hệ quản trị viên.");
                        return false;
                    }
                    
                    // Reload lại thông tin sau khi cập nhật
                    taiKhoan = taiKhoanDAO.findById(username);
                    nhanVien = taiKhoan.getNhanVien();
                    
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", 
                        "Đã mở lại tài khoản và kích hoạt trạng thái làm việc!");
                    
                } else {
                    // Người dùng chọn Hủy - không đăng nhập
                    return false;
                }
            }
            
            // Kiểm tra tài khoản đang hoạt động bình thường
            if (!trangThaiTK.equals("danghoatdong")) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", 
                    "Tài khoản không hoạt động!\n" +
                    "Trạng thái hiện tại: " + taiKhoan.getTrangThaiTK());
                return false;
            }
            
            // Kiểm tra nhân viên đang làm việc
            if (!tinhTrangCV.equals("đang làm")) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", 
                    "Nhân viên không thể làm việc!\n" +
                    "Trạng thái: " + nhanVien.getTinhTrangCV());
                return false;
            }
            
            // Lưu session
            SessionManager.getInstance().login(username, nhanVien, taiKhoan);
            
            // Lấy quyền từ SessionManager
            boolean isAdmin = SessionManager.getInstance().isAdmin();
            
            // Load dashboard tương ứng
            loadDashboard(nhanVien.getTenNhanVien(), isAdmin);
            
            // Hiển thị thông báo thành công
            showAlert(Alert.AlertType.INFORMATION, "Thành công", 
                "Đăng nhập thành công!\n" +
                "Chào mừng: " + nhanVien.getTenNhanVien() + "\n" +
                "Vai trò: " + SessionManager.getInstance().getRole());
            
            return true; 
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Lỗi khi đăng nhập: " + e.getMessage());
            return false;
        }
    }

    /**
     * Kích hoạt lại tài khoản và nhân viên
     */
    private boolean activateAccountAndEmployee(TaiKhoan taiKhoan, NhanVien nhanVien) {
        try {
            boolean success = true;
            
            // Cập nhật trạng thái tài khoản về DangHoatDong
            if (taiKhoan.getTrangThaiTK().trim().equalsIgnoreCase("tamngung")) {
                taiKhoan.setTrangThaiTK("DangHoatDong");
                success = success && taiKhoanDAO.update(taiKhoan);
            }
            
            // Cập nhật tình trạng nhân viên về Đang làm
            if (nhanVien.getTinhTrangCV().trim().equalsIgnoreCase("tạm nghỉ")) {
                nhanVien.setTinhTrangCV("Đang làm");
                success = success && nhanVienDAO.update(nhanVien);
            }
            
            return success;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Hiển thị dialog xác nhận với nút Xác nhận và Hủy
     */
    private boolean showConfirmationDialog(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        
        // Custom button text
        ButtonType btnXacNhan = new ButtonType("Xác nhận mở tài khoản");
        ButtonType btnHuy = new ButtonType("Hủy");
        
        alert.getButtonTypes().setAll(btnXacNhan, btnHuy);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == btnXacNhan;
    }

    /**
     * Load giao diện Dashboard theo quyền
     */
    private void loadDashboard(String displayName, boolean isAdmin) {
        try {
            FXMLLoader loader;
            String title;
            String role = isAdmin ? "Quản Lý" : "Nhân Viên";
            
            if (isAdmin) {
                loader = new FXMLLoader(getClass().getResource("/views/dashboard.fxml"));
                title = "Hệ Thống Quản Lý Bán Vé Tàu - " + role + " (" + displayName + ")";
            } else {
                loader = new FXMLLoader(getClass().getResource("/views/ban-ve.fxml"));
                title = "Hệ Thống Quản Lý Bán Vé Tàu - " + role + " (" + displayName + ")";
            }
            
            Parent root = loader.load();
            
            // Set username cho controller tương ứng
            if (isAdmin) {
                DashboardController controller = loader.getController();
                controller.setUsername(displayName);
            } else {
                BanVeController controller = loader.getController();
                controller.setUsername(displayName);
            }

            // Chuyển scene
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.setMaximized(true);
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải giao diện: " + e.getMessage());
        }
    }

    @FXML
    private void handleForgotPassword() {
        // Tạo dialog nhập mã nhân viên
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Quên mật khẩu");
        dialog.setHeaderText("Khôi phục mật khẩu");
        dialog.setContentText("Nhập mã nhân viên của bạn:");
        
        Optional<String> result = dialog.showAndWait();
        
        if (result.isPresent()) {
            String maNhanVien = result.get().trim();
            
            if (maNhanVien.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập mã nhân viên!");
                return;
            }
            
            // Show loading trước
            Alert loadingAlert = new Alert(Alert.AlertType.INFORMATION);
            loadingAlert.setTitle("Đang xử lý");
            loadingAlert.setHeaderText(null);
            loadingAlert.setContentText("Đang gửi email khôi phục mật khẩu...");
            loadingAlert.show();
            
            // Xử lý trong background thread
            Task<Boolean> task = new Task<>() {
                @Override
                protected Boolean call() throws Exception {
                    return processPasswordReset(maNhanVien);
                }
            };
            
            task.setOnSucceeded(e -> {
                loadingAlert.close();
                if (task.getValue()) {
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", 
                        "Mật khẩu TẠM THỜI đã được gửi về email của bạn!\n" +
                        "Bạn sẽ phải đổi mật khẩu ngay trong lần đăng nhập tới.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", 
                        "Không thể khôi phục mật khẩu!\n" +
                        "Vui lòng kiểm tra lại mã nhân viên hoặc liên hệ quản trị viên.");
                }
            });
            
            task.setOnFailed(e -> {
                loadingAlert.close();
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Có lỗi xảy ra: " + task.getException().getMessage());
            });
            
            new Thread(task).start();
        }
    }

    /**
     * Xử lý reset mật khẩu
     */
    private boolean processPasswordReset(String maNhanVien) {
        try {
            // Tìm tài khoản
            TaiKhoan taiKhoan = taiKhoanDAO.findById(maNhanVien);
            
            if (taiKhoan == null) {
                System.err.println("Không tìm thấy tài khoản: " + maNhanVien);
                return false;
            }
            
            // Chỉ cho phép reset nếu tài khoản đang hoạt động hoặc tạm ngưng
            String trangThai = taiKhoan.getTrangThaiTK().trim().toLowerCase();
            if (!trangThai.equals("danghoatdong") && !trangThai.equals("tamngung")) {
                System.err.println("Tài khoản không thể reset mật khẩu. Trạng thái: " + trangThai);
                return false;
            }
            
            // Lấy thông tin nhân viên
            NhanVien nhanVien = taiKhoan.getNhanVien();
            if (nhanVien == null || nhanVien.getEmail() == null || nhanVien.getEmail().isEmpty()) {
                System.err.println("Không tìm thấy email nhân viên");
                return false;
            }
            
            // Tạo mật khẩu mới
            String newPassword = EmailService.generateRandomPassword();
            
            // Gửi email
            boolean emailSent = EmailService.sendPasswordResetEmail(
                nhanVien.getEmail(), 
                newPassword, 
                nhanVien.getTenNhanVien()
            );
            
            if (!emailSent) {
                System.err.println("Gửi email thất bại");
                return false;
            }
            
            // === [MỚI] CẬP NHẬT TRẠNG THÁI MẬT KHẨU TẠM ===
            taiKhoan.setMatKhau(newPassword);
            taiKhoan.setMatKhauTam(true); // Đánh dấu đây là mật khẩu tạm
            
            boolean updated = taiKhoanDAO.update(taiKhoan);
            // =============================================
            
            if (updated) {
                System.out.println("Đã cập nhật mật khẩu mới (TẠM) cho: " + maNhanVien);
                return true;
            } else {
                System.err.println("Cập nhật mật khẩu thất bại");
            }
            
            return false;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 
     * Trả về true nếu đổi thành công
     */
    private boolean showForceChangePasswordDialog(TaiKhoan taiKhoan) {
        // Tạo Dialog
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Yêu cầu đổi mật khẩu");
        dialog.setHeaderText("Bạn đang sử dụng mật khẩu tạm thời.\nVui lòng đổi mật khẩu mới để tiếp tục.");

        // Tạo nút xác nhận
        ButtonType loginButtonType = new ButtonType("Đổi & Đăng nhập", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        // Tạo giao diện form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        PasswordField newPass = new PasswordField();
        newPass.setPromptText("Mật khẩu mới");
        PasswordField confirmPass = new PasswordField();
        confirmPass.setPromptText("Nhập lại mật khẩu mới");

        grid.add(new Label("Mật khẩu mới:"), 0, 0);
        grid.add(newPass, 1, 0);
        grid.add(new Label("Xác nhận:"), 0, 1);
        grid.add(confirmPass, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // Validate trước khi đóng dialog
        final javafx.scene.control.Button btnOk = (javafx.scene.control.Button) dialog.getDialogPane().lookupButton(loginButtonType);
        btnOk.addEventFilter(ActionEvent.ACTION, event -> {
            String p1 = newPass.getText();
            String p2 = confirmPass.getText();
            
            if (p1.isEmpty() || p2.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập đầy đủ thông tin!");
                event.consume(); // Chặn đóng dialog
                return;
            }
            
            if (!p1.equals(p2)) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Mật khẩu xác nhận không khớp!");
                event.consume(); // Chặn đóng dialog
                return;
            }
            
            if (p1.equals(taiKhoan.getMatKhau())) {
                 showAlert(Alert.AlertType.ERROR, "Lỗi", "Mật khẩu mới không được trùng với mật khẩu tạm!");
                 event.consume();
                 return;
            }
        });

        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return newPass.getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();

        if (result.isPresent()) {
            String newPasswordToSave = result.get();
            try {
                taiKhoan.setMatKhau(newPasswordToSave);
                taiKhoan.setMatKhauTam(false);
                
                boolean success = taiKhoanDAO.update(taiKhoan);
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đổi mật khẩu thành công!");
                    return true;
                } else {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể cập nhật mật khẩu vào CSDL.");
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        return false;
    }

    /**
     * Hiển thị alert dialog
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}