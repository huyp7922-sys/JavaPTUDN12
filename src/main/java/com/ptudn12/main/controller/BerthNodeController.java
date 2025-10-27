package com.ptudn12.main.controller;

import java.util.function.Consumer;

import com.ptudn12.main.entity.Cho;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;

public class BerthNodeController {

	// Enum để xác định loại và hướng
	public enum Type {
		SEAT, BED
	}

	public enum Orientation {
		LEFT, RIGHT, BOTTOM
	}

	@FXML
	private BorderPane berthNodePane;
	@FXML
	private Label numberLabel;
	@FXML
	private Region backLeft;
	@FXML
	private Region backRight;
	@FXML
	private Region backBottom;

	private Cho cho;
	private Consumer<Cho> onBerthClickHandler;

	/**
	 * Phương thức chính để cấu hình giao diện cho một "Chỗ".
	 */
	public void setData(Cho cho, boolean isAvailable, Type type, Orientation orientation) {
		this.cho = cho;
		numberLabel.setText(String.valueOf(cho.getSoThuTu()));

		// 1. Cấu hình loại (ghế/giường) bằng cách thêm/bớt style class
		if (type == Type.SEAT) {
			berthNodePane.getStyleClass().add("seat-type");
		} else {
			berthNodePane.getStyleClass().add("bed-type");
		}

		// 2. Cấu hình hướng (ẩn các thành không cần thiết)
		// Dùng switch để code rõ ràng hơn
		switch (orientation) {
		case LEFT:
			hide(backRight);
			hide(backBottom);
			break;
		case RIGHT:
			hide(backLeft);
			hide(backBottom);
			break;
		case BOTTOM:
			hide(backLeft);
			hide(backRight);
			break;
		}

		// 3. Cấu hình trạng thái (còn trống/đã bán)
		berthNodePane.getStyleClass().add(isAvailable ? "available" : "sold");
	}

	// Thiết lập kênh giao tiếp
	public void setOnBerthClickHandler(Consumer<Cho> handler) {
		this.onBerthClickHandler = handler;
	}

	// Khi được click, bắn tín hiệu qua kênh giao tiếp
	@FXML
	void onBerthClicked(MouseEvent event) {
		if (onBerthClickHandler != null) {
			onBerthClickHandler.accept(this.cho);
		}
	}

	public void setInteractive(boolean interactive) {
		// setMouseTransparent(true) làm cho node "trong suốt" với sự kiện chuột.
		// Mọi click sẽ đi xuyên qua nó.
		berthNodePane.setMouseTransparent(!interactive);
	}

	// Hàm tiện ích để ẩn một Region hoàn toàn
	private void hide(Region region) {
		region.setVisible(false);
		region.setManaged(false);
	}
}