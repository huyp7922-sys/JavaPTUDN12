    package com.ptudn12.main.controller;

    import com.ptudn12.main.dao.NhanVienDAO;
    import com.ptudn12.main.dao.TaiKhoanDAO;
    import com.ptudn12.main.entity.NhanVien;
    import com.ptudn12.main.entity.TaiKhoan;
    import com.ptudn12.main.util.SessionManager;
    import java.io.IOException;
    import javafx.event.ActionEvent;
    import javafx.fxml.FXML;
    import javafx.fxml.FXMLLoader;
    import javafx.scene.Parent;
    import javafx.scene.Scene;
    import javafx.scene.control.Alert;
    import javafx.scene.control.PasswordField;
    import javafx.scene.control.TextField;
    import javafx.stage.Stage;
    import com.ptudn12.main.utils.EmailService;  
    import java.util.Optional;  
    import javafx.scene.control.TextInputDialog;
    import javafx.concurrent.Task;

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
            
            // Sai tên đăng nhập hoặc mật khẩu
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Tên đăng nhập hoặc mật khẩu không đúng!");
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
                return false;
            }
            
            // Kiểm tra mật khẩu
            if (!taiKhoan.getMatKhau().equals(password)) {
                System.out.println("Sai mật khẩu");
                return false;
            }

            
            String trangThai = taiKhoan.getTrangThaiTK().trim().toLowerCase();
            if (!trangThai.equals("danghoatdong")) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", 
                    "Tài khoản không hoạt động!\nTrạng thái hiện tại: " + taiKhoan.getTrangThaiTK());
                return false;
            }
            
            NhanVien nhanVien = taiKhoan.getNhanVien();
            
            if (nhanVien == null) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không tìm thấy thông tin nhân viên!");
                return false;
            }
            
            String tinhTrang = nhanVien.getTinhTrangCV().trim().toLowerCase();
            if (!tinhTrang.equals("đang làm") && !tinhTrang.equals("danglam")) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", 
                    "Nhân viên không còn làm việc!\nTrạng thái: " + nhanVien.getTinhTrangCV());
                return false;
            }
            
            // Lưu session
            SessionManager.getInstance().login(username, nhanVien, taiKhoan);
            
            boolean isAdmin = nhanVien.isQuanLy();  
            
            
            loadDashboard(nhanVien.getTenNhanVien(), isAdmin);
            
            return true; 
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Lỗi khi đăng nhập: " + e.getMessage());
            return false;
        }
    }

        /**
         * Load giao diện Dashboard theo quyền
         */
        private void loadDashboard(String displayName, boolean isAdmin) {
            try {
                FXMLLoader loader;
                String title;
                
                if (isAdmin) {
                    loader = new FXMLLoader(getClass().getResource("/views/dashboard.fxml"));
                    title = "Hệ Thống Quản Lý Bán Vé Tàu - Quản Lý (" + displayName + ")";
                } else {
                    loader = new FXMLLoader(getClass().getResource("/views/ban-ve.fxml"));
                    title = "Hệ Thống Quản Lý Bán Vé Tàu - Nhân Viên (" + displayName + ")";
                }
                
                Parent root = loader.load();
                if (isAdmin) {
                    DashboardController controller = loader.getController();
                    controller.setUsername(displayName);
                } else {
                    BanVeController controller = loader.getController();
                    controller.setUsername(displayName);
                }

                Stage stage = (Stage) usernameField.getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle(title);
                
                // Force maximize using Platform.runLater to ensure it happens after scene is rendered
                javafx.application.Platform.runLater(() -> {
                    stage.setMaximized(true);
                });
                stage.show();
                
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải giao diện!");
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
                
                // Xử lý trong background thread
                Task<Boolean> task = new Task<>() {
                    @Override
                    protected Boolean call() throws Exception {
                        return processPasswordReset(maNhanVien);
                    }
                };
                
                task.setOnSucceeded(e -> {
                    if (task.getValue()) {
                        showAlert(Alert.AlertType.INFORMATION, "Thành công", 
                            "Mật khẩu mới đã được gửi về email của bạn!\n" +
                            "Vui lòng kiểm tra email (kể cả thư mục Spam).");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Lỗi", 
                            "Không thể khôi phục mật khẩu!\n" +
                            "Vui lòng kiểm tra lại mã nhân viên hoặc liên hệ quản trị viên.");
                    }
                });
                
                task.setOnFailed(e -> {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Có lỗi xảy ra: " + task.getException().getMessage());
                });
                
                // Show loading
                Alert loadingAlert = new Alert(Alert.AlertType.INFORMATION);
                loadingAlert.setTitle("Đang xử lý");
                loadingAlert.setHeaderText(null);
                loadingAlert.setContentText("Đang gửi email khôi phục mật khẩu...");
                loadingAlert.show();
                
                task.setOnSucceeded(e -> loadingAlert.close());
                task.setOnFailed(e -> loadingAlert.close());
                
                new Thread(task).start();
            }
        }

        private boolean processPasswordReset(String maNhanVien) {
            try {
                // Tìm tài khoản
                TaiKhoan taiKhoan = taiKhoanDAO.findById(maNhanVien);
                
                if (taiKhoan == null) {
                    System.err.println("Không tìm thấy tài khoản: " + maNhanVien);
                    return false;
                }
                
                // Kiểm tra trạng thái
                if (!taiKhoan.getTrangThaiTK().equalsIgnoreCase("danghoatdong")) {
                    System.err.println("Tài khoản không hoạt động");
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
                    return false;
                }
                
                // Cập nhật mật khẩu trong database
                taiKhoan.setMatKhau(newPassword);
                boolean updated = taiKhoanDAO.update(taiKhoan);
                
                if (updated) {
                    System.out.println("Đã cập nhật mật khẩu mới cho: " + maNhanVien);
                    return true;
                }
                
                return false;
                
            } catch (Exception e) {
                e.printStackTrace();
                return false;
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