// File: com/ptudn12/main/controller/HardSeatCarController.java
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
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Controller chuyên trách vẽ sơ đồ toa ngồi cứng, theo mô hình của
 * SoftSeatCarController.
 */
public class HardSeatCarController {

	private static final double CLUSTER_GAP = 10; // Khoảng trống giữa các cụm ghế

	@FXML
	private HBox topSeatBlock;
	@FXML
	private HBox bottomSeatBlock;

	private List<Cho> danhSachCho;

	// --- PHƯƠNG THỨC CÔNG KHAI ---

	public void initViewOnlyMode(int seatCount, LoaiCho loaiCho) {
		List<Boolean> defaultStates = new ArrayList<>(Collections.nCopies(seatCount, false));
		populateLayoutInternal(defaultStates, loaiCho, null);
	}

	public void initInteractiveMode(List<Boolean> seatStates, Consumer<Cho> clickHandler) {
		populateLayoutInternal(seatStates, LoaiCho.GheCung, clickHandler);
	}

	// --- CÁC PHƯƠNG THỨC NỘI BỘ ---

	private void populateLayoutInternal(List<Boolean> trangThaiCho, LoaiCho loaiCho,
			Consumer<Cho> onBerthClickHandler) {
		this.danhSachCho = new ArrayList<>();
		for (int i = 0; i < trangThaiCho.size(); i++) {
			Cho cho = new Cho(loaiCho);
			cho.setSoThuTu(i + 1);
			this.danhSachCho.add(cho);
		}

		clearAllRows();

		int totalColumns = trangThaiCho.size() / 4; // 18 cột cho 72 ghế

		for (int col = 0; col < totalColumns; col++) {
			VBox columnBoxTop = new VBox(0);
			VBox columnBoxBottom = new VBox(0);

			// Xác định số ghế
			int seatNumTop1 = col * 4 + 1;
			int seatNumTop2 = col * 4 + 2;
			int seatNumBottom1 = col * 4 + 3;
			int seatNumBottom2 = col * 4 + 4;

			// Xác định hướng ghế
			BerthNodeController.Orientation orientation = (col % 2 == 0) ? BerthNodeController.Orientation.LEFT
					: BerthNodeController.Orientation.RIGHT;

			// Tạo và thêm ghế vào cột
			columnBoxTop.getChildren().addAll(
					createBerthNode(seatNumTop1, orientation, trangThaiCho, onBerthClickHandler),
					createBerthNode(seatNumTop2, orientation, trangThaiCho, onBerthClickHandler));
			columnBoxBottom.getChildren().addAll(
					createBerthNode(seatNumBottom1, orientation, trangThaiCho, onBerthClickHandler),
					createBerthNode(seatNumBottom2, orientation, trangThaiCho, onBerthClickHandler));

			// Thêm cột vào dãy
			topSeatBlock.getChildren().add(columnBoxTop);
			bottomSeatBlock.getChildren().add(columnBoxBottom);

			// Thêm khoảng trống giữa các cụm (một cụm là 2 cột)
			if (col % 2 != 1 && col < totalColumns - 1) {
				Region spacerTop = new Region();
				spacerTop.setMinWidth(CLUSTER_GAP);
				topSeatBlock.getChildren().add(spacerTop);

				Region spacerBottom = new Region();
				spacerBottom.setMinWidth(CLUSTER_GAP);
				bottomSeatBlock.getChildren().add(spacerBottom);
			}
		}
	}

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

			if (onBerthClickHandler != null) {
				controller.setOnBerthClickHandler(onBerthClickHandler);
				controller.setInteractive(true);
			} else {
				controller.setInteractive(false);
			}

			return berthNode;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private void clearAllRows() {
		topSeatBlock.getChildren().clear();
		bottomSeatBlock.getChildren().clear();
	}
}