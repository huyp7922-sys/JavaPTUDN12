// File: src/main/java/com/ptudn12/main/controller/CustomerManagementController.java
package com.ptudn12.main.controller;

import java.io.IOException;
import java.util.List;

import com.ptudn12.main.dao.KhachHangDAO;
import com.ptudn12.main.dao.VeTauDAO;
import com.ptudn12.main.entity.KhachHang;
import com.ptudn12.main.entity.VeDaMua;

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

	private final KhachHangDAO khachHangDAO = new KhachHangDAO();

	private final VeTauDAO veTauDAO = new VeTauDAO();

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
		loadDataFromDatabase();
	}

	private void loadDataFromDatabase() {
		customerData.clear();

		// Gọi DAO để lấy danh sách khách hàng từ DB
		List<KhachHang> danhSach = khachHangDAO.layTatCaKhachHang();

		// Thêm danh sách lấy được vào ObservableList để hiển thị
		customerData.addAll(danhSach);

		// Dòng này không bắt buộc nếu bạn đã set một lần trong initialize()
		// nhưng để đây để đảm bảo bảng luôn được cập nhật.
		customerTable.setItems(customerData);
	}

	@FXML
	private void handleAddCustomer() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/add-customer-dialog.fxml"));
			Scene scene = new Scene(loader.load());

			// Lấy controller của dialog
			AddCustomerDialogController controller = loader.getController();

			Stage dialogStage = new Stage();
			dialogStage.setTitle("Thêm Khách Hàng Mới");
			dialogStage.initModality(Modality.APPLICATION_MODAL);
			dialogStage.setScene(scene);

			// Hiển thị và chờ cho đến khi dialog được đóng
			dialogStage.showAndWait();

			// CHỈ làm mới dữ liệu nếu người dùng đã bấm LƯU thành công
			if (controller.isSaveClicked()) {
				handleRefresh();
			}

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

			// Lấy controller của dialog và truyền dữ liệu khách hàng qua
			AddCustomerDialogController controller = loader.getController();
			controller.loadCustomerForEdit(selected);

			Stage dialogStage = new Stage();
			dialogStage.setTitle("Sửa Thông Tin Khách Hàng");
			dialogStage.initModality(Modality.APPLICATION_MODAL);
			dialogStage.setScene(scene);

			// Hiển thị và chờ
			dialogStage.showAndWait();

			// CHỈ làm mới bảng nếu người dùng đã bấm LƯU thành công
			if (controller.isSaveClicked()) {
				customerTable.refresh(); // .refresh() hiệu quả hơn là load lại toàn bộ
			}

		} catch (IOException e) {
			e.printStackTrace();
			showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở form sửa thông tin khách hàng!");
		}
	}

	@FXML
	private void handleRefresh() {
		loadDataFromDatabase();
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
			int maKhachHang = Integer.parseInt(selected.getMaKhachHang().substring(2));

			// Gọi DAO để lấy danh sách lịch sử vé
			List<VeDaMua> historyList = veTauDAO.layLichSuMuaVeTheoKhachHang(maKhachHang);

			// =================================================================
			// KIỂM TRA NẾU KHÁCH HÀNG KHÔNG CÓ VÉ NÀO
			if (historyList.isEmpty()) {
				showAlert(Alert.AlertType.INFORMATION, "Thông báo",
						"Khách hàng \"" + selected.getTenKhachHang() + "\" chưa mua vé nào.");
				return; // Dừng lại, không mở cửa sổ lịch sử
			}
			// =================================================================

			// Nếu có vé, tiếp tục mở cửa sổ như bình thường
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/customer-history-view.fxml"));
			Scene scene = new Scene(loader.load());

			CustomerHistoryController controller = loader.getController();
			controller.initData(selected, historyList);

			Stage dialogStage = new Stage();
			dialogStage.setTitle("Lịch Sử Mua Vé của " + selected.getTenKhachHang());
			dialogStage.initModality(Modality.APPLICATION_MODAL);
			dialogStage.setScene(scene);
			dialogStage.showAndWait();

		} catch (NumberFormatException e) {
			e.printStackTrace();
			showAlert(Alert.AlertType.ERROR, "Lỗi Dữ Liệu", "Mã khách hàng không hợp lệ: " + selected.getMaKhachHang());
		} catch (IOException e) {
			e.printStackTrace();
			showAlert(Alert.AlertType.ERROR, "Lỗi Giao Diện", "Không thể mở cửa sổ xem lịch sử mua vé!");
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