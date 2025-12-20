package com.ptudn12.main.controller;

import com.ptudn12.main.dao.NhanVienDAO;
import com.ptudn12.main.dao.TaiKhoanDAO;
import com.ptudn12.main.entity.NhanVien;
import com.ptudn12.main.entity.TaiKhoan;
import com.ptudn12.main.utils.EmailService;
import com.ptudn12.main.utils.PasswordUtils;
import com.ptudn12.main.utils.SessionManager;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();
    private final TaiKhoanDAO taiKhoanDAO = new TaiKhoanDAO();

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = safeTrim(usernameField.getText());
        String password = safeTrim(passwordField.getText());

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        authenticateDatabase(username, password);
    }

    private boolean authenticateDatabase(String username, String password) {
        try {
            TaiKhoan taiKhoan = taiKhoanDAO.findById(username);
            if (taiKhoan == null) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Tên đăng nhập hoặc mật khẩu không đúng");
                return false;
            }

            if (!verifyAndMaybeMigratePassword(taiKhoan, username, password)) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Tên đăng nhập hoặc mật khẩu không đúng");
                return false;
            }

            if (taiKhoan.isMatKhauTam()) {
                if (!showForceChangePasswordDialog(taiKhoan)) {
                    showAlert(Alert.AlertType.WARNING, "Yêu cầu", "Bạn bắt buộc phải đổi mật khẩu để tiếp tục sử dụng hệ thống!");
                    return false;
                }
                taiKhoan = taiKhoanDAO.findById(username);
                if (taiKhoan == null) {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải lại thông tin tài khoản sau khi đổi mật khẩu.");
                    return false;
                }
            }

            NhanVien nhanVien = taiKhoan.getNhanVien();
            if (nhanVien == null) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không tìm thấy thông tin nhân viên!");
                return false;
            }

            String trangThaiTK = safeLower(taiKhoan.getTrangThaiTK());
            String tinhTrangCV = safeLower(nhanVien.getTinhTrangCV());

            if (trangThaiTK.equals("ngunghan")) {
                showAlert(Alert.AlertType.ERROR, "Tài khoản đã ngừng hoạt động",
                        "Tài khoản của bạn đã bị ngừng hoạt động!\nVui lòng liên hệ quản trị viên để được hỗ trợ.");
                return false;
            }

            if (tinhTrangCV.equals("đã nghỉ")) {
                showAlert(Alert.AlertType.ERROR, "Nhân viên đã nghỉ việc",
                        "Bạn đã nghỉ việc và không thể đăng nhập vào hệ thống!\nVui lòng liên hệ phòng nhân sự để biết thêm chi tiết.");
                return false;
            }

            if (trangThaiTK.equals("tamngung") || tinhTrangCV.equals("tạm nghỉ")) {
                boolean shouldActivate = showConfirmationDialog(
                        "Tài khoản/Nhân viên đang tạm ngưng",
                        "Thông tin trạng thái:",
                        "Bạn có muốn mở lại tài khoản và tiếp tục làm việc không?"
                );

                if (!shouldActivate) return false;

                if (!activateAccountAndEmployee(taiKhoan, nhanVien)) {
                    showAlert(Alert.AlertType.ERROR, "Lỗi",
                            "Không thể cập nhật trạng thái tài khoản!\nVui lòng liên hệ quản trị viên.");
                    return false;
                }

                taiKhoan = taiKhoanDAO.findById(username);
                if (taiKhoan == null || taiKhoan.getNhanVien() == null) {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải lại thông tin sau khi cập nhật trạng thái.");
                    return false;
                }
                nhanVien = taiKhoan.getNhanVien();

                trangThaiTK = safeLower(taiKhoan.getTrangThaiTK());
                tinhTrangCV = safeLower(nhanVien.getTinhTrangCV());

                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã mở lại tài khoản và kích hoạt trạng thái làm việc!");
            }

            if (!trangThaiTK.equals("danghoatdong")) {
                showAlert(Alert.AlertType.ERROR, "Lỗi",
                        "Tài khoản không hoạt động!\nTrạng thái hiện tại: " + taiKhoan.getTrangThaiTK());
                return false;
            }

            if (!tinhTrangCV.equals("đang làm")) {
                showAlert(Alert.AlertType.ERROR, "Lỗi",
                        "Nhân viên không thể làm việc!\nTrạng thái: " + nhanVien.getTinhTrangCV());
                return false;
            }

            SessionManager.getInstance().login(username, nhanVien, taiKhoan);
            boolean isAdmin = SessionManager.getInstance().isAdmin();

            loadDashboard(nhanVien.getTenNhanVien(), isAdmin);

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

    private boolean verifyAndMaybeMigratePassword(TaiKhoan taiKhoan, String username, String password) {
        String stored = taiKhoan.getMatKhau();
        if (stored == null) return false;

        if (PasswordUtils.looksLikeBCrypt(stored)) {
            return PasswordUtils.verify(password, stored);
        }

        if (!stored.equals(password)) return false;

        try {
            taiKhoan.setMatKhau(PasswordUtils.hash(password));
            taiKhoanDAO.update(taiKhoan);
            TaiKhoan reloaded = taiKhoanDAO.findById(username);
            if (reloaded != null) {
                taiKhoan.setMatKhau(reloaded.getMatKhau());
                taiKhoan.setMatKhauTam(reloaded.isMatKhauTam());
                taiKhoan.setTrangThaiTK(reloaded.getTrangThaiTK());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return true;
    }

    private boolean activateAccountAndEmployee(TaiKhoan taiKhoan, NhanVien nhanVien) {
        try {
            boolean success = true;

            if ("tamngung".equalsIgnoreCase(safeTrim(taiKhoan.getTrangThaiTK()))) {
                taiKhoan.setTrangThaiTK("DangHoatDong");
                success = success && taiKhoanDAO.update(taiKhoan);
            }

            if ("tạm nghỉ".equalsIgnoreCase(safeTrim(nhanVien.getTinhTrangCV()))) {
                nhanVien.setTinhTrangCV("Đang làm");
                success = success && nhanVienDAO.update(nhanVien);
            }

            return success;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean showConfirmationDialog(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        ButtonType btnXacNhan = new ButtonType("Xác nhận mở tài khoản");
        ButtonType btnHuy = new ButtonType("Hủy");
        alert.getButtonTypes().setAll(btnXacNhan, btnHuy);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == btnXacNhan;
    }

    private void loadDashboard(String displayName, boolean isAdmin) {
        try {
            FXMLLoader loader;
            String role = isAdmin ? "Quản Lý" : "Nhân Viên";
            String title;

            if (isAdmin) {
                loader = new FXMLLoader(getClass().getResource("/views/dashboard.fxml"));
                title = "Hệ Thống Quản Lý Bán Vé Tàu - " + role + " (" + displayName + ")";
            } else {
                loader = new FXMLLoader(getClass().getResource("/views/ban-ve.fxml"));
                title = "Hệ Thống Quản Lý Bán Vé Tàu - " + role + " (" + displayName + ")";
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
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.setResizable(true);
            stage.setMaximized(true);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải giao diện: " + e.getMessage());
        }
    }

    @FXML
    private void handleForgotPassword() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Quên mật khẩu");
        dialog.setHeaderText("Khôi phục mật khẩu");
        dialog.setContentText("Nhập mã nhân viên của bạn:");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) return;

        String maNhanVien = safeTrim(result.get());
        if (maNhanVien.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập mã nhân viên!");
            return;
        }

        Alert loadingAlert = new Alert(Alert.AlertType.INFORMATION);
        loadingAlert.setTitle("Đang xử lý");
        loadingAlert.setHeaderText(null);
        loadingAlert.setContentText("Đang gửi email khôi phục mật khẩu...");
        loadingAlert.show();

        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() {
                return processPasswordReset(maNhanVien);
            }
        };

        task.setOnSucceeded(e -> {
            loadingAlert.close();
            if (Boolean.TRUE.equals(task.getValue())) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công",
                        "Mật khẩu TẠM THỜI đã được gửi về email của bạn!\nBạn sẽ phải đổi mật khẩu ngay trong lần đăng nhập tới.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi",
                        "Không thể khôi phục mật khẩu!\nVui lòng kiểm tra lại mã nhân viên hoặc liên hệ quản trị viên.");
            }
        });

        task.setOnFailed(e -> {
            loadingAlert.close();
            Throwable ex = task.getException();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Có lỗi xảy ra: " + (ex == null ? "Unknown" : ex.getMessage()));
        });

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    private boolean processPasswordReset(String maNhanVien) {
        try {
            TaiKhoan taiKhoan = taiKhoanDAO.findById(maNhanVien);
            if (taiKhoan == null) return false;

            String trangThai = safeLower(taiKhoan.getTrangThaiTK());
            if (!trangThai.equals("danghoatdong") && !trangThai.equals("tamngung")) return false;

            NhanVien nhanVien = taiKhoan.getNhanVien();
            if (nhanVien == null) return false;

            String email = safeTrim(nhanVien.getEmail());
            if (email.isEmpty()) return false;

            String newPassword = EmailService.generateRandomPassword();

            boolean emailSent = EmailService.sendPasswordResetEmail(
                    email,
                    newPassword,
                    nhanVien.getTenNhanVien()
            );

            if (!emailSent) return false;

            taiKhoan.setMatKhau(PasswordUtils.hash(newPassword));
            taiKhoan.setMatKhauTam(true);

            boolean updated = taiKhoanDAO.update(taiKhoan);
            if (updated) {
                return true;
            }
            return false;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean showForceChangePasswordDialog(TaiKhoan taiKhoan) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Yêu cầu đổi mật khẩu");
        dialog.setHeaderText("Bạn đang sử dụng mật khẩu tạm thời.\nVui lòng đổi mật khẩu mới để tiếp tục.");

        ButtonType okType = new ButtonType("Đổi & Đăng nhập", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okType, ButtonType.CANCEL);

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

        Button btnOk = (Button) dialog.getDialogPane().lookupButton(okType);
        btnOk.addEventFilter(ActionEvent.ACTION, event -> {
            String p1 = safeTrim(newPass.getText());
            String p2 = safeTrim(confirmPass.getText());

            if (p1.isEmpty() || p2.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập đầy đủ thông tin!");
                event.consume();
                return;
            }

            if (!p1.equals(p2)) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Mật khẩu xác nhận không khớp!");
                event.consume();
                return;
            }

            String stored = taiKhoan.getMatKhau();
            boolean sameAsTemp = PasswordUtils.looksLikeBCrypt(stored)
                    ? PasswordUtils.verify(p1, stored)
                    : (stored != null && p1.equals(stored));

            if (sameAsTemp) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Mật khẩu mới không được trùng với mật khẩu tạm!");
                event.consume();
            }
        });

        dialog.setResultConverter(btn -> btn == okType ? safeTrim(newPass.getText()) : null);

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) return false;

        String newPasswordToSave = safeTrim(result.get());
        if (newPasswordToSave.isEmpty()) return false;

        try {
            taiKhoan.setMatKhau(PasswordUtils.hash(newPasswordToSave));
            taiKhoan.setMatKhauTam(false);

            boolean success = taiKhoanDAO.update(taiKhoan);
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đổi mật khẩu thành công!");
                return true;
            }

            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể cập nhật mật khẩu vào CSDL.");
            return false;

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Có lỗi xảy ra khi đổi mật khẩu: " + e.getMessage());
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

    private String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }

    private String safeLower(String s) {
        return safeTrim(s).toLowerCase();
    }
}