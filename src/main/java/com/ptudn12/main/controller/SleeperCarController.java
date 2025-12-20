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
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class SleeperCarController {

	@FXML
	private VBox tierLabelsContainer;
	@FXML
	private HBox compartmentLabelsContainer;
	@FXML
	private HBox bedsLayoutContainer;

	private List<Cho> danhSachCho;

	// --- PHƯƠNG THỨC CÔNG KHAI ---

	public void initViewOnlyMode(int numberOfTiers, LoaiCho loaiCho) {
		int seatCount = 7 * (numberOfTiers * 2); // 7 khoang * (số tầng * 2 giường/tầng)
		List<Boolean> defaultStates = new ArrayList<>(Collections.nCopies(seatCount, false));
		populateLayoutInternal(numberOfTiers, defaultStates, loaiCho, null);
	}

	public void initInteractiveMode(int numberOfTiers, List<Boolean> seatStates, LoaiCho loaiCho,
			Consumer<Cho> clickHandler) {
		populateLayoutInternal(numberOfTiers, seatStates, loaiCho, clickHandler);
	}

	// --- LOGIC NỘI BỘ ---

	private void populateLayoutInternal(int numberOfTiers, List<Boolean> trangThaiCho, LoaiCho loaiCho,
			Consumer<Cho> onBerthClickHandler) {
		// 1. Tạo dữ liệu đối tượng "Cho"
		this.danhSachCho = new ArrayList<>();
		for (int i = 0; i < trangThaiCho.size(); i++) {
			Cho cho = new Cho(loaiCho);
			cho.setSoThuTu(i + 1);
			this.danhSachCho.add(cho);
		}

		// 2. Dọn dẹp và vẽ lại giao diện
		clearLayout();
		createTierLabels(numberOfTiers);
		createCompartmentLabels();
		createBedsLayout(numberOfTiers, trangThaiCho, onBerthClickHandler);
	}

	private void createBedsLayout(int numberOfTiers, List<Boolean> trangThaiCho, Consumer<Cho> onBerthClickHandler) {
		int numberOfCompartments = 7;
		int bedsPerCompartment = numberOfTiers * 2;

		// Thêm vách ngăn đầu tiên
		bedsLayoutContainer.getChildren().add(createDivider());

		for (int khoang = 0; khoang < numberOfCompartments; khoang++) {
			GridPane compartmentPane = new GridPane();
			compartmentPane.getStyleClass().add("compartment-pane");

			for (int hang = 0; hang < numberOfTiers; hang++) {
				int baseNum = khoang * bedsPerCompartment;
				int tierOffset = (numberOfTiers - 1 - hang) * 2;
				int seatNumLeft = baseNum + tierOffset + 1;
				int seatNumRight = baseNum + tierOffset + 2;

				Node leftBed = createBerthNode(seatNumLeft, trangThaiCho, onBerthClickHandler);
				Node rightBed = createBerthNode(seatNumRight, trangThaiCho, onBerthClickHandler);

				compartmentPane.add(leftBed, 0, hang);
				compartmentPane.add(rightBed, 1, hang);
			}
			bedsLayoutContainer.getChildren().add(compartmentPane);
			bedsLayoutContainer.getChildren().add(createDivider()); // Thêm vách ngăn sau mỗi khoang
		}
	}

	private Node createBerthNode(int seatNumber, List<Boolean> trangThaiCho, Consumer<Cho> onBerthClickHandler) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/berth-node.fxml"));
			Node berthNode = loader.load();
			BerthNodeController controller = loader.getController();

			int dataIndex = seatNumber - 1;
			Cho cho = danhSachCho.get(dataIndex);
			boolean isAvailable = !trangThaiCho.get(dataIndex);

			// Cấu hình là giường, hướng BOTTOM
			controller.setData(cho, isAvailable, BerthNodeController.Type.BED, BerthNodeController.Orientation.BOTTOM);

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

	private void createTierLabels(int numberOfTiers) {
		for (int i = numberOfTiers; i >= 1; i--) {
			Label tierLabel = new Label("T" + i);
			tierLabel.getStyleClass().add("tier-label");
			tierLabelsContainer.getChildren().add(tierLabel);
		}
	}

	private void createCompartmentLabels() {
		for (int i = 1; i <= 7; i++) {
			Label lbl = new Label("Khoang " + i);
			lbl.getStyleClass().add("compartment-label");
			HBox.setHgrow(lbl, Priority.ALWAYS);
			lbl.setMaxWidth(Double.MAX_VALUE);
			compartmentLabelsContainer.getChildren().add(lbl);
		}
	}

	private VBox createDivider() {
		VBox divider = new VBox();
		divider.getStyleClass().add("compartment-divider");
		return divider;
	}

	private void clearLayout() {
		tierLabelsContainer.getChildren().clear();
		compartmentLabelsContainer.getChildren().clear();
		bedsLayoutContainer.getChildren().clear();
	}
}