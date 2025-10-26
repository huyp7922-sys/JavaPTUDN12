// File: src/main/java/com/ptudn12/main/controller/CustomerManagementController.java
package com.ptudn12.main.controller;

import java.io.IOException;

import com.ptudn12.main.entity.KhachHang;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class CustomerManagementController {

	// Khai báo các thành phần FXML cho bảng Khách hàng
	@FXML
	private TableView<KhachHang> customerTable;
	@FXML
	private TableColumn<KhachHang, String> idColumn;
	@FXML
	private TableColumn<KhachHang, String> nameColumn;
	@FXML
	private TableColumn<KhachHang, String> cccdColumn;
	@FXML
	private TableColumn<KhachHang, String> passportColumn;
	@FXML
	private TableColumn<KhachHang, String> phoneColumn;
	@FXML
	private TableColumn<KhachHang, Integer> pointsColumn;

	// Danh sách dữ liệu khách hàng
	private ObservableList<KhachHang> customerData = FXCollections.observableArrayList();

	@FXML
	public void initialize() {
		// Liên kết các cột với thuộc tính của đối tượng KhachHang
		idColumn.setCellValueFactory(new PropertyValueFactory<>("maKhachHang"));
		nameColumn.setCellValueFactory(new PropertyValueFactory<>("tenKhachHang"));
		cccdColumn.setCellValueFactory(new PropertyValueFactory<>("soCCCD"));
		passportColumn.setCellValueFactory(new PropertyValueFactory<>("hoChieu"));
		phoneColumn.setCellValueFactory(new PropertyValueFactory<>("soDienThoai"));
		pointsColumn.setCellValueFactory(new PropertyValueFactory<>("diemTich"));

		// Tải dữ liệu mẫu
		loadMockData();
	}

	private void loadMockData() {
		customerData.clear();
		customerData.addAll(new KhachHang("KH000000001", "Nguyễn Văn An", "012345678912", false, "0905123456", 150),
				new KhachHang("KH000000002", "John Doe", "C1234567", true, "+1-202-555-0182", 2000),
				new KhachHang("KH000000003", "Lê Thị Cẩm", "098765432109", false, "0989999888", 50),
				new KhachHang("KH000000004", "Phạm Văn Dũng", "011223344556", false, "0977123789", 0),
				new KhachHang("KH000000005", "Emily Smith", "B9876543", true, "+44 20 7946 0958", 320));
		customerTable.setItems(customerData);
	}

	@FXML
	private void handleAddCustomer() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/add-customer-dialog.fxml"));
			Scene scene = new Scene(loader.load());

			Stage dialogStage = new Stage();
			dialogStage.setTitle("Thêm Khách Hàng Mới");
			dialogStage.initModality(Modality.APPLICATION_MODAL);
			dialogStage.setScene(scene);
			dialogStage.showAndWait();

			handleRefresh();
		} catch (IOException e) {
			e.printStackTrace();
			showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở form thêm khách hàng!");
		}
	}

	@FXML
	private void handleEditCustomer() {
		KhachHang selected = customerTable.getSelectionModel().getSelectedItem();
		if (selected == null) {
			showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn khách hàng cần sửa!");
			return;
		}

		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/add-customer-dialog.fxml"));
			Scene scene = new Scene(loader.load());

			AddCustomerDialogController controller = loader.getController();
			controller.loadCustomerForEdit(selected);

			Stage dialogStage = new Stage();
			dialogStage.setTitle("Sửa Thông Tin Khách Hàng");
			dialogStage.initModality(Modality.APPLICATION_MODAL);
			dialogStage.setScene(scene);
			dialogStage.showAndWait();

			customerTable.refresh();
		} catch (IOException e) {
			e.printStackTrace();
			showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở form sửa thông tin khách hàng!");
		}
	}

	@FXML
	private void handleRefresh() {
		loadMockData();
		customerTable.refresh();
		showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Đã tải lại danh sách khách hàng!");
	}

	@FXML
	private void handleViewHistory() {
		KhachHang selected = customerTable.getSelectionModel().getSelectedItem();
		if (selected == null) {
			showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn một khách hàng để xem lịch sử!");
			return;
		}

		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/customer-history-view.fxml"));
			Scene scene = new Scene(loader.load());

			CustomerHistoryController controller = loader.getController();
			controller.loadCustomerData(selected);

			Stage dialogStage = new Stage();
			dialogStage.setTitle("Lịch Sử Mua Vé của " + selected.getTenKhachHang());
			dialogStage.initModality(Modality.APPLICATION_MODAL);
			dialogStage.setScene(scene);
			dialogStage.showAndWait();
		} catch (IOException e) {
			e.printStackTrace();
			showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở cửa sổ xem lịch sử mua vé!");
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