package com.ptudn12.main.controller;

import java.io.IOException;

import com.ptudn12.main.dao.TauDAO;
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

	private TauDAO tauDAO;

	@FXML
	public void initialize() {
		this.tauDAO = new TauDAO();
		carriageTypeComboBox.setItems(FXCollections.observableArrayList(LoaiToa.values()));
		carriageTypeComboBox.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
			if (newValue != null) {
				updateCarriageView(newValue);
			}
		});
		carriageTypeComboBox.setValue(LoaiToa.NGOI_MEM);
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
					"Không tìm thấy Loại Chỗ tương ứng cho Loại Toa: " + type.getDescription());
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
			case NGOI_MEM:
				FXMLLoader contentLoader1 = new FXMLLoader(getClass().getResource("/views/soft-seat-car-layout.fxml"));
				carLayoutContent = contentLoader1.load();
				SoftSeatCarController contentController1 = contentLoader1.getController();
				contentController1.initViewOnlyMode(type.getSoChoMacDinh(type), correspondingLoaiCho);
				break;

			// --- CÁC LOẠI TOA KHÁC SẼ ĐƯỢC THÊM VÀO ĐÂY ---

			case GIUONG_NAM_KHOANG_4:
			case GIUONG_NAM_KHOANG_6:
			case GIUONG_NAM_VIP:
				FXMLLoader sleeperLoader = new FXMLLoader(getClass().getResource("/views/sleeper-car-layout.fxml"));
				carLayoutContent = sleeperLoader.load();
				SleeperCarController sleeperController = sleeperLoader.getController();

				// Xác định số tầng dựa trên LoaiToa
				int tiers = (type == LoaiToa.GIUONG_NAM_KHOANG_6) ? 3 : (type == LoaiToa.GIUONG_NAM_KHOANG_4) ? 2 : 1;
				sleeperController.initViewOnlyMode(tiers, correspondingLoaiCho);
				break;
			case NGOI_CUNG:
				FXMLLoader contentLoader2 = new FXMLLoader(getClass().getResource("/views/hard-seat-car-layout.fxml"));
				carLayoutContent = contentLoader2.load();
				HardSeatCarController contentController2 = contentLoader2.getController();
				contentController2.initViewOnlyMode(type.getSoChoMacDinh(type), correspondingLoaiCho);
				break;
			}
			String title = String.format("%s", type.getDescription());
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
		case NGOI_MEM:
			return LoaiCho.GHE_NGOI_MEM;
		case NGOI_CUNG:
			return LoaiCho.GHE_CUNG;
		case GIUONG_NAM_KHOANG_4:
			return LoaiCho.GIUONG_4;
		case GIUONG_NAM_KHOANG_6:
			return LoaiCho.GIUONG_66;
		case GIUONG_NAM_VIP:
			return LoaiCho.GIUONG_VIP;
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
		if (selectedType == null) {
			showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng chọn một loại toa!");
			return;
		}

		// TODO: Cần có giao diện để người dùng nhập tên toa.
		// Tạm thời, chúng ta sẽ tạo một tên toa ngẫu nhiên để demo.
		String tenToaMoi = generateNewCarriageName(selectedType);

		// Gọi phương thức DAO để thêm toa mới
		boolean success = tauDAO.themToaMoi(tenToaMoi, selectedType);

		if (success) {
			showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã thêm thành công toa mới: " + tenToaMoi);
			closeStage();
		} else {
			showAlert(Alert.AlertType.ERROR, "Thất bại",
					"Không thể thêm toa mới vào cơ sở dữ liệu. Vui lòng kiểm tra console để biết chi tiết.");
		}
	}

	@FXML
	private void handleClose() {
		closeStage();
	}

	/**
	 * Hàm tạm thời để tạo tên toa mới dựa trên loại toa. Trong thực tế, bạn nên có
	 * một ô TextField để người dùng nhập.
	 */
	private String generateNewCarriageName(LoaiToa type) {
		String prefix = "";
		switch (type) {
		case NGOI_CUNG:
			prefix = "NC";
			break;
		case NGOI_MEM:
			prefix = "NM";
			break;
		case GIUONG_NAM_KHOANG_6:
			prefix = "GN6";
			break;
		case GIUONG_NAM_KHOANG_4:
			prefix = "GN4";
			break;
		case GIUONG_NAM_VIP:
			prefix = "VIP";
			break;
		}
		// Thêm một số ngẫu nhiên để đảm bảo tên là duy nhất
		return prefix + String.format("%02d", (int) (Math.random() * 90 + 10));
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