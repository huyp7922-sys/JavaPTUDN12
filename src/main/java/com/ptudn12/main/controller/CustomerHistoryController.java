// File: src/main/java/com/ptudn12/main/controller/CustomerHistoryController.java
package com.ptudn12.main.controller;

import java.text.NumberFormat;
import java.util.Locale;

import com.ptudn12.main.entity.KhachHang;
import com.ptudn12.main.entity.VeDaMua;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class CustomerHistoryController {

	// Customer Info TextFields (non-editable)
	@FXML
	private TextField maKhField;
	@FXML
	private TextField hoTenField;
	@FXML
	private TextField cccdField;
	@FXML
	private TextField hoChieuField;
	@FXML
	private TextField sdtField;
	@FXML
	private TextField diemTichField;

	// Table and Columns
	@FXML
	private TableView<VeDaMua> historyTable;
	@FXML
	private TableColumn<VeDaMua, Integer> sttColumn;
	@FXML
	private TableColumn<VeDaMua, String> ngayMuaColumn;
	@FXML
	private TableColumn<VeDaMua, String> maVeColumn;
	@FXML
	private TableColumn<VeDaMua, String> macTauColumn;
	@FXML
	private TableColumn<VeDaMua, String> hanhTrinhColumn;
	@FXML
	private TableColumn<VeDaMua, String> thoiGianColumn;
	@FXML
	private TableColumn<VeDaMua, String> toaColumn;
	@FXML
	private TableColumn<VeDaMua, Integer> soChoColumn;
	@FXML
	private TableColumn<VeDaMua, String> giaVeColumn;

	// Summary Fields
	@FXML
	private Label recordCountLabel;
	@FXML
	private TextField totalAmountField;

	private ObservableList<VeDaMua> purchaseHistoryData = FXCollections.observableArrayList();

	@FXML
	public void initialize() {
		// Liên kết các cột trong bảng với các thuộc tính của class VeDaMua
		sttColumn.setCellValueFactory(new PropertyValueFactory<>("stt"));
		ngayMuaColumn.setCellValueFactory(new PropertyValueFactory<>("ngayMuaVe"));
		maVeColumn.setCellValueFactory(new PropertyValueFactory<>("maVe"));
		macTauColumn.setCellValueFactory(new PropertyValueFactory<>("macTau"));
		hanhTrinhColumn.setCellValueFactory(new PropertyValueFactory<>("hanhTrinh"));
		thoiGianColumn.setCellValueFactory(new PropertyValueFactory<>("thoiGianDiDen"));
		toaColumn.setCellValueFactory(new PropertyValueFactory<>("toa"));
		soChoColumn.setCellValueFactory(new PropertyValueFactory<>("soCho"));
		giaVeColumn.setCellValueFactory(new PropertyValueFactory<>("giaVe"));
	}

	/**
	 * Phương thức này được controller chính gọi để truyền dữ liệu khách hàng được
	 * chọn.
	 * 
	 * @param customer Khách hàng cần hiển thị lịch sử.
	 */
	public void loadCustomerData(KhachHang customer) {
		// Điền thông tin khách hàng vào các trường text field
		maKhField.setText(customer.getMaKhachHang());
		hoTenField.setText(customer.getTenKhachHang());
		cccdField.setText(
				customer.getSoCCCD() != null && !customer.getSoCCCD().isEmpty() ? customer.getSoCCCD() : "N/A");
		hoChieuField.setText(
				customer.getHoChieu() != null && !customer.getHoChieu().isEmpty() ? customer.getHoChieu() : "N/A");
		sdtField.setText(customer.getSoDienThoai());
		diemTichField.setText(String.valueOf(customer.getDiemTich()));

		// Tải dữ liệu ảo cho lịch sử mua vé
		loadMockHistory();
	}

	/**
	 * Tạo dữ liệu ảo cho bảng. Sau này sẽ được thay thế bằng lệnh gọi DAO.
	 */
	private void loadMockHistory() {
		purchaseHistoryData.clear();
		purchaseHistoryData.addAll(
				new VeDaMua(1, "25/09/2025", "123456789", "SE1", "Sài Gòn - Hà Nội", "18:00 - 08:30", "3 - Ngồi mềm",
						12, "1,200,000đ"),
				new VeDaMua(2, "15/08/2025", "987654321", "SE3", "Đà Nẵng - Nha Trang", "20:00 - 06:30",
						"7 - Giường khoang 6", 5, "800,000đ"),
				new VeDaMua(3, "02/07/2025", "456789123", "SE5", "Hải Phòng - Vinh", "07:00 - 12:20", "2 - Ngồi cứng",
						30, "600,000đ"),
				new VeDaMua(4, "10/06/2025", "321654987", "SE7", "Huế - Quảng Ngãi", "09:00 - 13:30", "1 - Giường VIP",
						2, "1,500,000đ"));
		historyTable.setItems(purchaseHistoryData);

		// Tính toán và hiển thị thông tin tổng kết
		updateSummary();
	}

	private void updateSummary() {
		// 1. Cập nhật tổng số vé
		int count = purchaseHistoryData.size();
		recordCountLabel.setText("Có " + count + " vé đã mua");

		// 2. Tính và cập nhật tổng tiền
		long totalAmount = 0;
		for (VeDaMua ve : purchaseHistoryData) {
			try {
				// Xóa các ký tự không phải là số (dấu phẩy và chữ 'đ') rồi chuyển thành số
				String cleanPrice = ve.getGiaVe().replaceAll("[^\\d]", "");
				totalAmount += Long.parseLong(cleanPrice);
			} catch (NumberFormatException e) {
				System.err.println("Không thể phân tích giá: " + ve.getGiaVe());
			}
		}

		// Định dạng tổng tiền theo kiểu tiền tệ Việt Nam
		Locale vietnameseLocale = Locale.forLanguageTag("vi-VN");
		NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(vietnameseLocale);
		totalAmountField.setText(currencyFormatter.format(totalAmount));
	}

	@FXML
	private void handleClose() {
		Stage stage = (Stage) maKhField.getScene().getWindow();
		stage.close();
	}
}