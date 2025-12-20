package com.ptudn12.main.controller;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class CarriageFrameController {

	@FXML
	private Label titleLabel;
	@FXML
	private StackPane contentArea;

	/**
	 * Đặt tiêu đề cho khung toa.
	 */
	public void setTitle(String title) {
		titleLabel.setText(title);
	}

	/**
	 * Đặt nội dung (sơ đồ toa) vào vùng trung tâm của khung.
	 */
	public void setContent(Node contentNode) {
		contentArea.getChildren().clear();
		if (contentNode != null) {
			contentArea.getChildren().add(contentNode);
			StackPane.setAlignment(contentNode, Pos.CENTER); // Căn giữa sơ đồ
		}
	}
}