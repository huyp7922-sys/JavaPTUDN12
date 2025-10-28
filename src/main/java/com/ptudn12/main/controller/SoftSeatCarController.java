package com.ptudn12.main.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import com.ptudn12.main.entity.Cho;
import com.ptudn12.main.enums.LoaiCho;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.HBox;

/**
 * Controller chuyên trách vẽ sơ đồ toa ngồi mềm. Có thể hoạt động ở 2 chế độ:
 * VIEW_ONLY và INTERACTIVE.
 */
public class SoftSeatCarController {

	// Inject các HBox từ FXML (không đổi)
	@FXML
	private HBox topLeftRow1, topLeftRow2, topRightRow1, topRightRow2;
	@FXML
	private HBox bottomLeftRow1, bottomLeftRow2, bottomRightRow1, bottomRightRow2;

	private List<Cho> danhSachCho;

	// --- PHƯƠNG THỨC CÔNG KHAI DÀNH CHO CONTROLLER CHA ---

	/**
	 * YÊU CẦU 1: Khởi tạo toa ở chế độ CHỈ XEM (từ Quản lý tàu). Các ghế sẽ ở trạng
	 * thái mặc định (trống) và không thể tương tác.
	 * 
	 * @param seatCount Tổng số ghế của loại toa này.
	 */
	public void initViewOnlyMode(int seatCount, LoaiCho loaiCho) {
		List<Boolean> defaultStates = new ArrayList<>(Collections.nCopies(seatCount, false));
		populateLayoutInternal(defaultStates, loaiCho, null);
	}

	/**
	 * YÊU CẦU 2: Khởi tạo toa ở chế độ TƯƠNG TÁC (từ Bán vé). Trạng thái ghế được
	 * lấy từ DB và có thể tương tác.
	 * 
	 * @param seatStates   Danh sách trạng thái ghế (true=đã bán) lấy từ DAO.
	 * @param clickHandler Hành động sẽ được thực thi khi một ghế được click.
	 */
	public void initInteractiveMode(List<Boolean> seatStates, LoaiCho loaiCho, Consumer<Cho> clickHandler) {
		populateLayoutInternal(seatStates, loaiCho, clickHandler);
	}

	// --- CÁC PHƯƠNG THỨC NỘI BỘ ---

	/**
	 * Hàm nội bộ, chịu trách nhiệm vẽ toàn bộ layout dựa trên dữ liệu đầu vào.
	 */
	private void populateLayoutInternal(List<Boolean> trangThaiCho, LoaiCho loaiCho,
			Consumer<Cho> onBerthClickHandler) {
		// TẠO ĐỐI TƯỢNG CHO CHÍNH XÁC
		this.danhSachCho = new ArrayList<>();
		for (int i = 0; i < trangThaiCho.size(); i++) {
			// SỬA LỖI QUAN TRỌNG: Dùng constructor có tham số
			Cho cho = new Cho(loaiCho);
			cho.setSoThuTu(i + 1);
			this.danhSachCho.add(cho);
		}

		clearAllRows();

		// Vòng lặp vẽ ghế (logic không đổi)
		for (int col = 0; col < 8; col++) {
			// ... (logic tính toán seat_num không đổi)
			int seat1_num, seat2_num, seat3_num, seat4_num;
			if (col % 2 == 0) {
				seat1_num = col * 4 + 1;
				seat2_num = col * 4 + 2;
				seat3_num = col * 4 + 3;
				seat4_num = col * 4 + 4;
			} else {
				seat1_num = col * 4 + 4;
				seat2_num = col * 4 + 3;
				seat3_num = col * 4 + 2;
				seat4_num = col * 4 + 1;
			}

			// Thêm ghế vào các hàng, truyền cả trạng thái và hành động click
			topLeftRow1.getChildren().add(createBerthNode(seat1_num, BerthNodeController.Orientation.LEFT, trangThaiCho,
					onBerthClickHandler));
			topLeftRow2.getChildren().add(createBerthNode(seat2_num, BerthNodeController.Orientation.LEFT, trangThaiCho,
					onBerthClickHandler));
			topRightRow1.getChildren().add(createBerthNode(seat1_num + 32, BerthNodeController.Orientation.RIGHT,
					trangThaiCho, onBerthClickHandler));
			topRightRow2.getChildren().add(createBerthNode(seat2_num + 32, BerthNodeController.Orientation.RIGHT,
					trangThaiCho, onBerthClickHandler));
			bottomLeftRow1.getChildren().add(createBerthNode(seat3_num, BerthNodeController.Orientation.LEFT,
					trangThaiCho, onBerthClickHandler));
			bottomLeftRow2.getChildren().add(createBerthNode(seat4_num, BerthNodeController.Orientation.LEFT,
					trangThaiCho, onBerthClickHandler));
			bottomRightRow1.getChildren().add(createBerthNode(seat3_num + 32, BerthNodeController.Orientation.RIGHT,
					trangThaiCho, onBerthClickHandler));
			bottomRightRow2.getChildren().add(createBerthNode(seat4_num + 32, BerthNodeController.Orientation.RIGHT,
					trangThaiCho, onBerthClickHandler));
		}
	}

	/**
	 * Dùng FXMLLoader để tạo một node ghế từ FXML và cấu hình nó.
	 */
	private Node createBerthNode(int seatNumber, BerthNodeController.Orientation orientation,
			List<Boolean> trangThaiCho, Consumer<Cho> onBerthClickHandler) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/berth-node.fxml"));
			Node berthNode = loader.load();
			BerthNodeController controller = loader.getController();

			int dataIndex = seatNumber - 1;
			Cho cho = danhSachCho.get(dataIndex);
			boolean isAvailable = !trangThaiCho.get(dataIndex);

			controller.setData(cho, isAvailable, BerthNodeController.Type.SEAT, orientation);

			// QUYẾT ĐỊNH TƯƠNG TÁC
			if (onBerthClickHandler != null) {
				controller.setOnBerthClickHandler(onBerthClickHandler);
				controller.setInteractive(true);
			} else {
				controller.setInteractive(false); // Không có hành động -> vô hiệu hóa tương tác
			}

			return berthNode;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private void clearAllRows() {
		topLeftRow1.getChildren().clear();
		topLeftRow2.getChildren().clear();
		topRightRow1.getChildren().clear();
		topRightRow2.getChildren().clear();
		bottomLeftRow1.getChildren().clear();
		bottomLeftRow2.getChildren().clear();
		bottomRightRow1.getChildren().clear();
		bottomRightRow2.getChildren().clear();
	}
}