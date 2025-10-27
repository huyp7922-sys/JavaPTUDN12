package com.ptudn12.main.controller;

import java.io.IOException;

import com.ptudn12.main.enums.LoaiCho;
import com.ptudn12.main.enums.LoaiToa;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Controller chính, quản lý toàn bộ cửa sổ dialog "Đăng Kí Toa Mới".
 */
public class AddCarriageDialogController {

	// --- 1. Các thành phần giao diện chính của Dialog ---
	@FXML
	private ComboBox<LoaiToa> carriageTypeComboBox;
	@FXML
	private StackPane carriageLayoutContainer; // Vùng trống để "nhét" sơ đồ toa vào
	@FXML
	private Button addButton;
	@FXML
	private Button closeButton;

	@FXML
	public void initialize() {
		carriageTypeComboBox.setItems(FXCollections.observableArrayList(LoaiToa.values()));
		carriageTypeComboBox.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
			if (newValue != null) {
				updateCarriageView(newValue);
			}
		});
		carriageTypeComboBox.setValue(LoaiToa.NgoiMem);
	}

	/**
	 * Chịu trách nhiệm tải và hiển thị sơ đồ toa con vào vùng
	 * carriageLayoutContainer. Đây là nơi "thuê chuyên gia" làm việc.
	 * 
	 * @param type Loại toa được chọn từ ComboBox.
	 */
	private void updateCarriageView(LoaiToa type) {
		Node carriageFrameNode = null;
		LoaiCho correspondingLoaiCho = mapLoaiToaToLoaiCho(type);

		if (correspondingLoaiCho == null) {
			showAlert(Alert.AlertType.ERROR, "Lỗi Cấu Hình",
					"Không tìm thấy Loại Chỗ tương ứng cho Loại Toa: " + type.getTenLoaiToa());
			carriageLayoutContainer.getChildren().clear();
			return;
		}

		try {
			// --- BƯỚC A: TẢI KHUNG TOA TRƯỚC ---
			FXMLLoader frameLoader = new FXMLLoader(getClass().getResource("/views/carriage-frame.fxml"));
			carriageFrameNode = frameLoader.load(); // carriageFrameNode là VBox chứa khung
			CarriageFrameController frameController = frameLoader.getController();

			Node carLayoutContent = null;
			switch (type) {
			case NgoiMem:
				FXMLLoader contentLoader = new FXMLLoader(getClass().getResource("/views/soft-seat-car-layout.fxml"));
				carLayoutContent = contentLoader.load();
				SoftSeatCarController contentController = contentLoader.getController();
				contentController.initViewOnlyMode(type.getSoChoMacDinh(), correspondingLoaiCho);
				break;

			// --- CÁC LOẠI TOA KHÁC SẼ ĐƯỢC THÊM VÀO ĐÂY ---

			case Giuong4:
			case Giuong6:
			case GiuongVIP:
				FXMLLoader sleeperLoader = new FXMLLoader(getClass().getResource("/views/sleeper-car-layout.fxml"));
				carLayoutContent = sleeperLoader.load();
				SleeperCarController sleeperController = sleeperLoader.getController();

				// Xác định số tầng dựa trên LoaiToa
				int tiers = (type == LoaiToa.Giuong6) ? 3 : (type == LoaiToa.Giuong4) ? 2 : 1;
				sleeperController.initViewOnlyMode(tiers, correspondingLoaiCho);
				break;
			case NgoiCung:
				// Tạm thời xóa layout cũ và thông báo chưa có
				carLayoutContent = null;
				System.out.println("Chưa triển khai giao diện cho: " + type.getTenLoaiToa());
				break;
			}
			String title = String.format("%s", type.getTenLoaiToa());
			frameController.setTitle(title);
			frameController.setContent(carLayoutContent); // Cắm nội dung vào khung
		} catch (IOException e) {
			e.printStackTrace();
			carriageFrameNode = null; // Nếu có lỗi thì reset
		}

		// Cập nhật lại giao diện chính
		if (carriageFrameNode != null) {
			// BỎ style nét đứt đi
			carriageLayoutContainer.getStyleClass().remove("carriage-layout-container");
			carriageLayoutContainer.getChildren().setAll(carriageFrameNode);
		} else {
			carriageLayoutContainer.getChildren().clear();
		}
	}

	/**
	 * THÊM MỚI: Phương thức ánh xạ từ LoaiToa sang LoaiCho. Đây là trung tâm của
	 * logic nghiệp vụ.
	 */
	private LoaiCho mapLoaiToaToLoaiCho(LoaiToa loaiToa) {
		switch (loaiToa) {
		case NgoiMem:
			return LoaiCho.GheNgoiMem;
		case NgoiCung:
			return LoaiCho.GheCung;
		case Giuong4:
			return LoaiCho.Giuong4;
		case Giuong6:
			return LoaiCho.Giuong6;
		case GiuongVIP:
			return LoaiCho.GiuongVIP;
		default:
			// Trả về null hoặc throw exception nếu không có mapping
			return null;
		}
	}

	/**
	 * Đây là TRUNG TÂM XỬ LÝ SỰ KIỆN. Hàm này được thực thi khi một "Chỗ" bất kỳ
	 * được click, nhờ vào "kênh giao tiếp" Consumer.
	 * 
	 * @param cho Đối tượng Chỗ được click, do controller con gửi lên.
	 */
	// --- 4. Xử lý các nút hành động của Dialog ---
	@FXML
	private void handleAdd() {
		LoaiToa selectedType = carriageTypeComboBox.getValue();
		// TODO: Thêm logic lưu toa mới vào database
		System.out.println("Lưu toa mới loại: " + selectedType.getTenLoaiToa());
		showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã đăng ký toa " + selectedType.getTenLoaiToa());
		closeStage();
	}

	@FXML
	private void handleClose() {
		closeStage();
	}

	// --- 5. Các hàm tiện ích ---
	private void closeStage() {
		Stage stage = (Stage) closeButton.getScene().getWindow();
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