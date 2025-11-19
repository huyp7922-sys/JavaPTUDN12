package com.ptudn12.main.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.ptudn12.main.utils.SessionManager;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class DashboardController {

	@FXML
	private StackPane contentPane;
	@FXML
	private Label dateTimeLabel;
	@FXML
	private Label lblUsername;
	@FXML
	private Label lblRole;
	@FXML
	private Button btnHome;
	@FXML
	private Button btnInvoice;
	@FXML
	private Button btnSchedule;
	@FXML
	private Button btnRoute;
	@FXML
	private Button btnTrain;
	@FXML
	private Button btnEmployee;
	@FXML
	private Button btnStatistics;

	@FXML
	private Button btnLogout;

	private String currentUser;

	@FXML
	public void initialize() {
		// Update clock every second
		Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			dateTimeLabel.setText(LocalDateTime.now().format(formatter));
		}), new KeyFrame(Duration.seconds(1)));
		clock.setCycleCount(Timeline.INDEFINITE);
		clock.play();

		// ===== THÊM DÒNG NÀY =====
		updateUserInfo();
		// =========================

		// Load dashboard statistics by default
		showHome();
	}

	public void setUsername(String username) {
		this.currentUser = username;
		// ===== THÊM DÒNG NÀY =====
		updateUserInfo();
		// =========================
	}

	/**
	 * Cập nhật thông tin user trên giao diện
	 */
	private void updateUserInfo() {
		SessionManager session = SessionManager.getInstance();

		if (session.isLoggedIn()) {
			// Hiển thị tên và vai trò
			if (lblUsername != null) {
				lblUsername.setText(session.getDisplayName());
			}
			if (lblRole != null) {
				lblRole.setText("Vai trò: " + session.getRole());
			}
		}
	}

	@FXML
	private void showHome() {
		resetMenuButtons();
		btnHome.getStyleClass().add("menu-item-active");
		loadView("dashboard_statistics.fxml");
	}

	@FXML
	private void showInvoice() {
		resetMenuButtons();
		btnInvoice.getStyleClass().add("menu-item-active");
		loadView("invoice-management.fxml");
	}

	@FXML
	private void showSchedule() {
		resetMenuButtons();
		btnSchedule.getStyleClass().add("menu-item-active");
		loadView("schedule-management.fxml");
	}

	@FXML
	private void showRoute() {
		resetMenuButtons();
		btnRoute.getStyleClass().add("menu-item-active");
		loadView("route-management.fxml");
	}

	@FXML
	private void showTrain() {
		resetMenuButtons();
		btnTrain.getStyleClass().add("menu-item-active");
		loadView("train-management.fxml");
	}

	@FXML
	private void showEmployee() {
		resetMenuButtons();
		btnEmployee.getStyleClass().add("menu-item-active");
		loadView("employee-management.fxml");
	}

	@FXML
	private void showStatistics() {
		resetMenuButtons();
		btnStatistics.getStyleClass().add("menu-item-active");
		loadView("statistics-management.fxml");
	}

	@FXML
	private void showDashboard() {
		resetMenuButtons();
		btnStatistics.getStyleClass().add("menu-item-active");
		loadView("DBoard.fxml");
	}

	@FXML
	private void handleAbout() {
		try {
			java.awt.Desktop.getDesktop().browse(new java.net.URI("https://yourcompany.com/about"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@FXML
	private void handleHelp() {
		try {
			java.awt.Desktop.getDesktop().browse(new java.net.URI("https://yourcompany.com/help"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@FXML
	private void handleLogout() {
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("Xác nhận đăng xuất");
		alert.setHeaderText(null);
		alert.setContentText("Bạn có chắc chắn muốn đăng xuất?");

		if (alert.showAndWait().get() == ButtonType.OK) {
			SessionManager.getInstance().logout();

			try {
				Parent root = FXMLLoader.load(getClass().getResource("/views/login.fxml"));
				Stage stage = (Stage) contentPane.getScene().getWindow();
				stage.setScene(new Scene(root));
				stage.setTitle("Đăng Nhập HệỐng");
			} catch (IOException e) {
				e.printStackTrace();
				showError("Lỗi khi đăng xuất: " + e.getMessage());
			}
		}
	}

	private void loadView(String fxmlFile) {
		try {
			// Check if file exists
			if (getClass().getResource("/views/" + fxmlFile) == null) {
				showPlaceholder("Chức năng chưa được tạo",
						"Trang " + fxmlFile.replace("-management.fxml", "").replace("dashboard-", "").replace("-", " ")
								+ " đang được phát triển.\nVui lòng quay lại sau!");
				return;
			}

			FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/" + fxmlFile));
			Node view = loader.load();
			contentPane.getChildren().clear();
			contentPane.getChildren().add(view);

		} catch (IOException e) {
			e.printStackTrace();
			showPlaceholder("Lỗi khi tải giao diện",
					"Không thể tải file: " + fxmlFile + "\n\n" + "Chi tiết lỗi: " + e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			showPlaceholder("Lỗi không xác định",
					"Đã xảy ra lỗi khi tải giao diện.\n\n" + "Chi tiết lỗi: " + e.getMessage());
		}
	}

	private void showPlaceholder(String title, String message) {
		VBox placeholder = new VBox(25);
		placeholder.setStyle("-fx-alignment: center; -fx-padding: 50;");

		// Icon
		Label icon = new Label("⚠️");
		icon.setStyle("-fx-font-size: 80px;");

		// Title
		Label titleLabel = new Label(title);
		titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #e67e22;");

		// Message
		Label messageLabel = new Label(message);
		messageLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d; -fx-text-alignment: center;");
		messageLabel.setWrapText(true);
		messageLabel.setMaxWidth(600);

		// Back button
		Button backButton = new Button("← Về Trang Chủ");
		backButton.setStyle("-fx-background-color: #3498db; " + "-fx-text-fill: white; " + "-fx-font-size: 14px; "
				+ "-fx-padding: 10 30; " + "-fx-background-radius: 5; " + "-fx-cursor: hand;");
		backButton.setOnAction(e -> showHome());

		placeholder.getChildren().addAll(icon, titleLabel, messageLabel, backButton);

		contentPane.getChildren().clear();
		contentPane.getChildren().add(placeholder);
	}

	private void resetMenuButtons() {
		btnHome.getStyleClass().remove("menu-item-active");
		btnInvoice.getStyleClass().remove("menu-item-active");
		btnSchedule.getStyleClass().remove("menu-item-active");
		btnRoute.getStyleClass().remove("menu-item-active");
		btnTrain.getStyleClass().remove("menu-item-active");
		btnEmployee.getStyleClass().remove("menu-item-active");
		btnStatistics.getStyleClass().remove("menu-item-active");
	}

	private void showError(String message) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle("Lỗi");
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}
}
