package com.ptudn12.main.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.ptudn12.main.entity.Tau;
import com.ptudn12.main.entity.Toa;
import com.ptudn12.main.enums.LoaiToa;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class TrainManagementController {

	// Khai báo các thành phần FXML cho bảng Tàu
	@FXML
	private TableView<Tau> trainTable;
	@FXML
	private TableColumn<Tau, String> trainCodeColumn;
	@FXML
	private TableColumn<Tau, Integer> carriageCountColumn;
	@FXML
	private TableColumn<Tau, Integer> totalSeatsColumn;
	@FXML
	private TableColumn<Tau, String> structureColumn;
	@FXML
	private TableColumn<Tau, String> statusColumn;

	// Nút và nhãn mới
	@FXML
	private Button configureTrainButton;
	@FXML
	private Label totalTrainsLabel;

	// Danh sách dữ liệu tàu
	private ObservableList<Tau> trainData = FXCollections.observableArrayList();

	// Cấu trúc để lưu trữ danh sách toa cho mỗi tàu (dùng cho mock data)
	private Map<String, List<Toa>> trainComposition = new HashMap<>();

	@FXML
	public void initialize() {
		// Liên kết các cột đơn giản với thuộc tính của đối tượng Tau
		trainCodeColumn.setCellValueFactory(new PropertyValueFactory<>("macTau"));
		statusColumn.setCellValueFactory(new PropertyValueFactory<>("trangThai"));

		// Thiết lập các cột có dữ liệu cần tính toán
		setupCalculatedColumns();

		// Thiết lập cell factory để tô màu cho cột Trạng Thái
		setupStatusColumnCellFactory();

		// Thiết lập listener để điều khiển nút "Cấu hình tàu"
		setupSelectionListener();

		// Tải dữ liệu mẫu
		loadMockData();

		// Ban đầu vô hiệu hóa nút Cấu hình tàu
		configureTrainButton.setDisable(true);
	}

	/**
	 * Listener cho việc chọn hàng trong bảng
	 */
	private void setupSelectionListener() {
		trainTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
			if (newSelection != null) {
				// Chỉ cho phép cấu hình khi tàu có trạng thái "Dừng chạy"
				boolean isStopped = "Dừng chạy".equals(newSelection.getTrangThai());
				configureTrainButton.setDisable(!isStopped);
			} else {
				// Không có hàng nào được chọn, vô hiệu hóa nút
				configureTrainButton.setDisable(true);
			}
		});
	}

	/**
	 * Tùy chỉnh hiển thị màu sắc cho cột Trạng Thái
	 */
	private void setupStatusColumnCellFactory() {
		statusColumn.setCellFactory(column -> new TableCell<Tau, String>() {
			@Override
			protected void updateItem(String status, boolean empty) {
				super.updateItem(status, empty);
				if (empty || status == null) {
					setText(null);
					setGraphic(null);
					getStyleClass().removeAll("status-ready", "status-inactive");
				} else {
					Label label = new Label(status);
					label.getStyleClass().add("status-label");

					if ("Đang chạy".equals(status)) {
						label.getStyleClass().add("status-ready");
					} else if ("Dừng chạy".equals(status)) {
						label.getStyleClass().add("status-inactive");
					}

					setGraphic(label);
					setText(null); // Chỉ hiển thị label, không hiển thị text
				}
			}
		});
	}

	private void setupCalculatedColumns() {
		// Cột Số Toa
		carriageCountColumn.setCellValueFactory(cellData -> {
			Tau train = cellData.getValue();
			List<Toa> carriages = trainComposition.get(train.getMacTau());
			int count = (carriages != null) ? carriages.size() : 0;
			return new SimpleIntegerProperty(count).asObject();
		});

		// Cột Tổng Chỗ Ngồi
		totalSeatsColumn.setCellValueFactory(cellData -> {
			Tau train = cellData.getValue();
			List<Toa> carriages = trainComposition.get(train.getMacTau());
			int totalSeats = 0;
			if (carriages != null) {
				totalSeats = carriages.stream().mapToInt(toa -> toa.getLoaiToa().getSoChoMacDinh()).sum();
			}
			return new SimpleIntegerProperty(totalSeats).asObject();
		});

		// Cột Cấu trúc tàu
		structureColumn.setCellValueFactory(cellData -> {
			Tau train = cellData.getValue();
			List<Toa> carriages = trainComposition.get(train.getMacTau());
			if (carriages == null || carriages.isEmpty()) {
				return new SimpleStringProperty("Chưa có cấu hình");
			}
			Map<LoaiToa, Long> counts = carriages.stream()
					.collect(Collectors.groupingBy(Toa::getLoaiToa, Collectors.counting()));
			String structure = counts.entrySet().stream()
					.map(entry -> entry.getValue() + "x " + getShortCarriageTypeName(entry.getKey()))
					.collect(Collectors.joining(", "));
			return new SimpleStringProperty(structure);
		});
	}

	private String getShortCarriageTypeName(LoaiToa loaiToa) {
		switch (loaiToa) {
		case Giuong4:
			return "Giường 4";
		case Giuong6:
			return "Giường 6";
		case GiuongVIP:
			return "Giường VIP";
		case NgoiMem:
			return "Ngồi mềm";
		case NgoiCung:
			return "Ngồi cứng";
		default:
			return "";
		}
	}

	private void loadMockData() {
		trainData.clear();
		trainComposition.clear();

		// Tạo dữ liệu mẫu...
		Tau se1 = new Tau("SE1");
		se1.setTrangThai("Đang chạy");
		List<Toa> se1Carriages = new ArrayList<>();
		for (int i = 0; i < 5; i++)
			se1Carriages.add(new Toa(LoaiToa.Giuong4));
		for (int i = 0; i < 5; i++)
			se1Carriages.add(new Toa(LoaiToa.Giuong6));
		for (int i = 0; i < 2; i++)
			se1Carriages.add(new Toa(LoaiToa.NgoiMem));
		trainComposition.put("SE1", se1Carriages);

		Tau se2 = new Tau("SE2");
		se2.setTrangThai("Đang chạy");
		trainComposition.put("SE2", new ArrayList<>(se1Carriages));

		Tau se3 = new Tau("SE3");
		se3.setTrangThai("Dừng chạy");
		List<Toa> se3Carriages = new ArrayList<>();
		for (int i = 0; i < 7; i++)
			se3Carriages.add(new Toa(LoaiToa.Giuong6));
		for (int i = 0; i < 2; i++)
			se3Carriages.add(new Toa(LoaiToa.NgoiMem));
		se3Carriages.add(new Toa(LoaiToa.NgoiCung));
		trainComposition.put("SE3", se3Carriages);

		Tau sqn1 = new Tau("SQN1");
		sqn1.setTrangThai("Đang chạy");
		List<Toa> sqn1Carriages = new ArrayList<>();
		sqn1Carriages.add(new Toa(LoaiToa.GiuongVIP));
		for (int i = 0; i < 3; i++)
			sqn1Carriages.add(new Toa(LoaiToa.Giuong4));
		for (int i = 0; i < 4; i++)
			sqn1Carriages.add(new Toa(LoaiToa.Giuong6));
		for (int i = 0; i < 3; i++)
			sqn1Carriages.add(new Toa(LoaiToa.NgoiMem));
		trainComposition.put("SQN1", sqn1Carriages);

		Tau se21 = new Tau("SE21");
		se21.setTrangThai("Đang chạy");
		List<Toa> se21Carriages = new ArrayList<>();
		for (int i = 0; i < 2; i++)
			se21Carriages.add(new Toa(LoaiToa.GiuongVIP));
		for (int i = 0; i < 4; i++)
			se21Carriages.add(new Toa(LoaiToa.Giuong4));
		for (int i = 0; i < 4; i++)
			se21Carriages.add(new Toa(LoaiToa.Giuong6));
		for (int i = 0; i < 2; i++)
			se21Carriages.add(new Toa(LoaiToa.NgoiMem));
		trainComposition.put("SE21", se21Carriages);

		Tau tn1 = new Tau("TN1");
		tn1.setTrangThai("Dừng chạy");
		List<Toa> tn1Carriages = new ArrayList<>();
		for (int i = 0; i < 6; i++)
			tn1Carriages.add(new Toa(LoaiToa.Giuong4));
		for (int i = 0; i < 5; i++)
			tn1Carriages.add(new Toa(LoaiToa.Giuong6));
		for (int i = 0; i < 2; i++)
			tn1Carriages.add(new Toa(LoaiToa.NgoiMem));
		for (int i = 0; i < 2; i++)
			tn1Carriages.add(new Toa(LoaiToa.NgoiCung));
		trainComposition.put("TN1", tn1Carriages);

		trainData.addAll(Arrays.asList(se1, se2, se3, sqn1, se21, tn1));
		trainTable.setItems(trainData);

		// Cập nhật nhãn đếm
		updateTrainCountLabel();
	}

	/**
	 * Cập nhật nhãn hiển thị tổng số tàu
	 */
	private void updateTrainCountLabel() {
		int count = trainData.size();
		totalTrainsLabel.setText("Có tất cả " + count + " tàu");
	}

	@FXML
	private void handleRefresh() {
		System.out.println("[TrainManagementController] Nút 'Làm mới' được nhấn. Đang tải lại dữ liệu...");
		loadMockData();
		trainTable.refresh();
		showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Đã làm mới danh sách tàu.");
	}

	@FXML
	private void handleCreateTrain() {
		showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Chức năng 'Lập tàu mới' chưa được triển khai.");
	}

	@FXML
	private void handleRegisterCarriage() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/add-carriage-dialog.fxml"));
			Scene scene = new Scene(loader.load());

			scene.getStylesheets().add(getClass().getResource("/views/train-management.css").toExternalForm());

			Stage dialogStage = new Stage();
			dialogStage.setTitle("Đăng Kí Toa Mới");
			dialogStage.initModality(Modality.APPLICATION_MODAL);
			dialogStage.setScene(scene); // Set scene đã có CSS
			dialogStage.setResizable(false);

			dialogStage.showAndWait();

			loadMockData();
			trainTable.refresh();

		} catch (IOException e) {
			e.printStackTrace();
			showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở form đăng kí toa mới!");
		}
	}

	@FXML
	private void handleConfigureTrain() {
		Tau selected = trainTable.getSelectionModel().getSelectedItem();
		// Kiểm tra này gần như là dư thừa vì nút đã bị disable, nhưng để an toàn
		if (selected == null || !"Dừng chạy".equals(selected.getTrangThai())) {
			showAlert(Alert.AlertType.WARNING, "Cảnh báo",
					"Vui lòng chọn một tàu ở trạng thái 'Dừng chạy' để cấu hình!");
			return;
		}
		showAlert(Alert.AlertType.INFORMATION, "Thông báo",
				"Chức năng 'Cấu hình tàu' cho tàu " + selected.getMacTau() + " chưa được triển khai.");
	}

	private void showAlert(Alert.AlertType type, String title, String message) {
		Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}
}