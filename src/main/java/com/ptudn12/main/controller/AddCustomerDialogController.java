// File: src/main/java/com/ptudn12/main/controller/AddCustomerDialogController.java
package com.ptudn12.main.controller;

import com.ptudn12.main.entity.KhachHang;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddCustomerDialogController {

	// Khai báo các thành phần FXML
	@FXML
	private Label titleLabel;
	@FXML
	private TextField nameField;
	@FXML
	private TextField idField;
	@FXML
	private TextField phoneField;
	@FXML
	private CheckBox isForeignerCheckBox;
	@FXML
	private Label idLabel;

	// Biến để giữ khách hàng đang được sửa, null nếu là chế độ "Thêm"
	private KhachHang editingCustomer;

	@FXML
	public void initialize() {
		// Thêm listener để theo dõi trạng thái của checkbox
		isForeignerCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
			updateIdFieldUI(newValue);
		});

		// Cài đặt trạng thái ban đầu
		updateIdFieldUI(false);
	}

	/**
	 * Phương thức này được gọi từ CustomerManagementController để truyền dữ liệu
	 * sang
	 * 
	 * @param customer Khách hàng cần sửa
	 */
	public void loadCustomerForEdit(KhachHang customer) {
		this.editingCustomer = customer;

		// Cập nhật giao diện cho chế độ sửa
		titleLabel.setText("Sửa Thông Tin Khách Hàng");
		nameField.setText(customer.getTenKhachHang());
		phoneField.setText(customer.getSoDienThoai());

		// Xác định là người nước ngoài hay không dựa trên dữ liệu
		boolean isForeigner = customer.getHoChieu() != null && !customer.getHoChieu().isEmpty();
		isForeignerCheckBox.setSelected(isForeigner);

		// Cập nhật label và điền CCCD/Hộ chiếu
		updateIdFieldUI(isForeigner);
		idField.setText(isForeigner ? customer.getHoChieu() : customer.getSoCCCD());
	}

	/**
	 * Cập nhật Label và PromptText của trường idField dựa vào trạng thái checkbox.
	 * 
	 * @param isForeigner true nếu là người nước ngoài, false nếu là người trong
	 *                    nước.
	 */
	private void updateIdFieldUI(boolean isForeigner) {
		if (isForeigner) {
			idLabel.setText("Số Hộ chiếu:");
			idField.setPromptText("Nhập số hộ chiếu");
		} else {
			idLabel.setText("Số CCCD:");
			idField.setPromptText("Nhập số CCCD");
		}
	}

	@FXML
	private void handleSave() {
		String name = nameField.getText().trim();
		String id = idField.getText().trim();
		String phone = phoneField.getText().trim();
		boolean isForeigner = isForeignerCheckBox.isSelected();

		if (name.isEmpty() || id.isEmpty() || phone.isEmpty()) {
			showAlert(Alert.AlertType.WARNING, "Lỗi", "Vui lòng điền đầy đủ thông tin!");
			return;
		}

		if (editingCustomer != null) {
			// --- Chế độ SỬA ---
			editingCustomer.setTenKhachHang(name);
			editingCustomer.setSoDienThoai(phone);
			if (isForeigner) {
				editingCustomer.setHoChieu(id);
				editingCustomer.setSoCCCD(null); // Xóa trường còn lại để đảm bảo tính nhất quán
			} else {
				editingCustomer.setSoCCCD(id);
				editingCustomer.setHoChieu(null); // Xóa trường còn lại
			}
			showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã cập nhật thông tin khách hàng!");
		} else {
			// --- Chế độ THÊM ---
			// Ở đây bạn sẽ gọi DAO để tạo khách hàng mới trong database
			// Ví dụ: KhachHang newCustomer = new KhachHang("...", name, id, isForeigner,
			// phone, 0);
			// khachHangDAO.create(newCustomer);
			showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã thêm khách hàng mới!");
		}
		closeDialog();
	}

	@FXML
	private void handleCancel() {
		closeDialog();
	}

	private void closeDialog() {
		Stage stage = (Stage) nameField.getScene().getWindow();
		stage.close();
	}

	private void showAlert(Alert.AlertType type, String title, String message) {
		Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}
}