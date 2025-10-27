// File: src/main/java/com/ptudn12/main/controller/AddCustomerDialogController.java
package com.ptudn12.main.controller;

import com.ptudn12.main.dao.KhachHangDAO;
import com.ptudn12.main.entity.KhachHang;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddCustomerDialogController {

	private final KhachHangDAO khachHangDAO = new KhachHangDAO();

	// THÊM 2 PHẦN NÀY
	private boolean saveClicked = false;

	public boolean isSaveClicked() {
		return saveClicked;
	}
	// KẾT THÚC PHẦN THÊM MỚI

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

	// THAY THẾ TOÀN BỘ PHƯƠNG THỨC handleSave() BẰNG PHƯƠNG THỨC NÀY
	@FXML
	private void handleSave() {
		// 1. Lấy dữ liệu từ form
		String name = nameField.getText().trim();
		String idValue = idField.getText().trim();
		String phone = phoneField.getText().trim();
		boolean isForeigner = isForeignerCheckBox.isSelected();

		// 2. KIỂM TRA RÀNG BUỘC DỮ LIỆU ĐẦU VÀO
		if (name.isEmpty()) {
			showAlert(Alert.AlertType.WARNING, "Lỗi Nhập Liệu", "Họ và tên không được để trống!");
			return;
		}
		if (phone.isEmpty()) {
			showAlert(Alert.AlertType.WARNING, "Lỗi Nhập Liệu", "Số điện thoại không được để trống!");
			return;
		}
		// Kiểm tra định dạng SĐT Việt Nam (bắt đầu bằng 0, theo sau là 9 chữ số)
		if (!phone.matches("^0\\d{9}$")) {
			showAlert(Alert.AlertType.WARNING, "Lỗi Nhập Liệu",
					"Số điện thoại không hợp lệ. Phải bắt đầu bằng số 0 và có 10 chữ số.");
			return;
		}
		if (idValue.isEmpty()) {
			String fieldName = isForeigner ? "Số Hộ chiếu" : "Số CCCD";
			showAlert(Alert.AlertType.WARNING, "Lỗi Nhập Liệu", fieldName + " không được để trống!");
			return;
		}
		// Nếu là khách trong nước, kiểm tra định dạng CCCD
		if (!isForeigner) {
			if (!idValue.matches("\\d{9}|\\d{12}")) {
				showAlert(Alert.AlertType.WARNING, "Lỗi Nhập Liệu", "Số CCCD phải là 9 hoặc 12 chữ số.");
				return;
			}
		}

		// 3. KIỂM TRA TRÙNG LẶP TRONG DATABASE (giữ nguyên logic cũ)
		String cccd = isForeigner ? null : idValue;
		String hoChieu = isForeigner ? idValue : null;
		Integer currentCustomerId = null;
		if (editingCustomer != null) {
			currentCustomerId = Integer.parseInt(editingCustomer.getMaKhachHang().substring(2));
		}
		if (khachHangDAO.kiemTraTrungLap(cccd, hoChieu, currentCustomerId)) {
			showAlert(Alert.AlertType.ERROR, "Lỗi Trùng Lặp",
					"Số CCCD hoặc Hộ chiếu này đã tồn tại. Vui lòng kiểm tra lại!");
			return;
		}

		// 4. THỰC HIỆN LƯU DỮ LIỆU
		boolean success;
		if (editingCustomer != null) {
			// Chế độ Sửa
			editingCustomer.setTenKhachHang(name);
			editingCustomer.setSoDienThoai(phone);
			if (isForeigner) {
				editingCustomer.setHoChieu(idValue);
				editingCustomer.setSoCCCD(null);
			} else {
				editingCustomer.setSoCCCD(idValue);
				editingCustomer.setHoChieu(null);
			}
			success = khachHangDAO.capNhatKhachHang(editingCustomer);
		} else {
			// Chế độ Thêm
			KhachHang newCustomer = new KhachHang("TEMP", name, idValue, isForeigner, phone, 0);
			success = khachHangDAO.themKhachHang(newCustomer);
		}

		// 5. THÔNG BÁO KẾT QUẢ
		if (success) {
			showAlert(Alert.AlertType.INFORMATION, "Thành công",
					editingCustomer != null ? "Đã cập nhật thông tin khách hàng!" : "Đã thêm khách hàng mới!");
			saveClicked = true; // Đánh dấu là đã lưu thành công
			closeDialog();
		} else {
			showAlert(Alert.AlertType.ERROR, "Thất bại", "Đã có lỗi xảy ra trong quá trình lưu vào cơ sở dữ liệu.");
		}
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