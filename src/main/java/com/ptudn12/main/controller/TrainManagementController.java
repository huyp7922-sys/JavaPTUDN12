package com.ptudn12.main.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.ptudn12.main.dao.TauDAO; // Import DAO mới
import com.ptudn12.main.entity.Tau;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class TrainManagementController {

	// --- FXML Components ---
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
	@FXML
	private Button configureTrainButton;
	@FXML
	private Label totalTrainsLabel;
	@FXML
	private TextField searchTextField;
	@FXML
	private Button searchButton;
	@FXML
	private Button showAllButton;

	// --- Data Models ---
	private ObservableList<Tau> trainData = FXCollections.observableArrayList();
	// Map lưu trữ danh sách toa của từng tàu: Key là mã tàu (VD: "SE1"), Value là
	// danh sách Toa
	private List<Tau> masterTrainData = new ArrayList<>();

	// --- DAO ---
	private TauDAO tauDAO;

	@FXML
	public void initialize() {
		this.tauDAO = new TauDAO();

		trainCodeColumn.setCellValueFactory(new PropertyValueFactory<>("macTau"));
		statusColumn.setCellValueFactory(new PropertyValueFactory<>("trangThai"));
		carriageCountColumn.setCellValueFactory(new PropertyValueFactory<>("soToa"));
		totalSeatsColumn.setCellValueFactory(new PropertyValueFactory<>("tongChoNgoi"));
		structureColumn.setCellValueFactory(new PropertyValueFactory<>("cauTrucTau"));


		setupStatusColumnCellFactory();
		setupSelectionListener();

		loadDataFromDatabase();

		configureTrainButton.setDisable(true);
	}

	/**
	 * Tải dữ liệu thật từ cơ sở dữ liệu thông qua TauDAO.
	 */
	private void loadDataFromDatabase() {
		try {
			masterTrainData.clear();
			List<Tau> allTrains = tauDAO.layTatCaTau();
			if (allTrains != null) {
				masterTrainData.addAll(allTrains);
			}

			// Hiển thị tất cả dữ liệu lên bảng
			trainData.setAll(masterTrainData);

			trainTable.setItems(trainData);
			updateTrainCountLabel();
		} catch (Exception e) {
			e.printStackTrace();
			showAlert(Alert.AlertType.ERROR, "Lỗi Tải Dữ Liệu",
					"Không thể tải danh sách tàu từ cơ sở dữ liệu.\nChi tiết: " + e.getMessage());
		}
	}


	@FXML
	private void handleSearch() {
		String searchText = searchTextField.getText().trim().toLowerCase();
		if (searchText.isEmpty()) {
			// Nếu ô tìm kiếm trống, hiển thị tất cả
			trainData.setAll(masterTrainData);
			return;
		}

		// Lọc danh sách gốc
		List<Tau> filteredList = masterTrainData.stream()
				.filter(tau -> tau.getMacTau().toLowerCase().contains(searchText)).collect(Collectors.toList());

		// Cập nhật bảng với kết quả đã lọc
		trainData.setAll(filteredList);
	}

	@FXML
	private void handleShowAll() {
		searchTextField.clear(); // Xóa nội dung ô tìm kiếm
		trainData.setAll(masterTrainData); // Hiển thị lại tất cả dữ liệu
	}

	/**
	 * Xử lý sự kiện nút "Làm mới".
	 */
	@FXML
	private void handleRefresh() {
		// Bước 1: Hiển thị thông báo "Đang tải" ngay lập tức
		Label loadingLabel = new Label("Đang cập nhật danh sách tàu...");
		loadingLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: grey;");
		trainTable.setPlaceholder(loadingLabel);
		trainData.clear(); // Xóa dữ liệu cũ để placeholder hiện ra

		// Bước 2: Tạo một Task để chạy tác vụ nặng (query DB) trên luồng nền
		Task<List<Tau>> loadDataTask = new Task<>() {
			@Override
			protected List<Tau> call() throws Exception {
				// Đây là nơi chạy trên luồng nền, không làm lag UI
				return tauDAO.layTatCaTau();
			}
		};

		// Bước 3: Xử lý khi Task thành công (đã lấy được dữ liệu)
		loadDataTask.setOnSucceeded(event -> {
			// Lấy kết quả từ luồng nền
			List<Tau> result = loadDataTask.getValue();

			// Cập nhật UI trên luồng chính JavaFX
			trainData.setAll(result);
			updateTrainCountLabel();

			// Set lại placeholder cho trường hợp không có dữ liệu
			Label emptyLabel = new Label("Không có dữ liệu tàu.");
			trainTable.setPlaceholder(emptyLabel);
		});

		// Bước 4: Xử lý khi Task thất bại
		loadDataTask.setOnFailed(event -> {
			loadDataTask.getException().printStackTrace();
			showAlert(Alert.AlertType.ERROR, "Lỗi Tải Dữ Liệu", "Không thể tải dữ liệu. Vui lòng xem console.");
		});

		// Bước 5: Khởi động Task trên một luồng mới
		new Thread(loadDataTask).start();
	}

	// ... (setupSelectionListener và setupStatusColumnCellFactory GIỮ NGUYÊN)

	private void setupSelectionListener() {
		trainTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
			if (newSelection != null) {
				// Tàu phải ở trạng thái khác "DangChay" mới cho phép cấu hình (ví dụ: SanSang,
				// TamNgung, ChuaKhoiHanh...)
				// Ở đây giả sử chỉ "DangChay" là bị khóa.
				boolean isRunning = "DangChay".equalsIgnoreCase(newSelection.getTrangThai());
				configureTrainButton.setDisable(isRunning);
			} else {
				configureTrainButton.setDisable(true);
			}
		});
	}

	private void setupStatusColumnCellFactory() {
		statusColumn.setCellFactory(column -> new TableCell<Tau, String>() {
			@Override
			protected void updateItem(String status, boolean empty) {
				super.updateItem(status, empty);
				getStyleClass().removeAll("status-label", "status-ready", "status-inactive", "status-paused",
						"status-running");

				if (empty || status == null) {
					setText(null);
					setGraphic(null);
				} else {
					String displayText = status; // Mặc định
					String styleClass = "status-inactive"; // Mặc định

					switch (status.toLowerCase()) {
					case "dangchay":
					case "đang chạy":
						displayText = "Đang chạy";
						styleClass = "status-running";
						break;
					case "dungchay":
					case "dừng chạy":
						displayText = "Dừng chạy";
						styleClass = "status-inactive";
						break;
					case "sansang":
					case "sẵn sàng":
						displayText = "Sẵn sàng";
						styleClass = "status-ready";
						break;
					case "tamngung":
					case "tạm ngưng":
						displayText = "Tạm ngưng";
						styleClass = "status-paused";
						break;
					case "chuakhoihanh":
					case "chưa khởi hành":
						displayText = "Chưa khởi hành";
						styleClass = "status-inactive";
						break;
					}

					Label label = new Label(displayText);
					label.getStyleClass().addAll("status-label", styleClass);

					setGraphic(label);
					setText(null);
				}
			}
		});
	}

	private void updateTrainCountLabel() {
		totalTrainsLabel.setText("Có tất cả " + trainData.size() + " tàu");
	}


	@FXML
	private void handleRegisterCarriage() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/add-carriage-dialog.fxml"));
			Scene scene = new Scene(loader.load());

			URL cssUrl = getClass().getResource("/views/train-management.css");
			if (cssUrl != null) {
				scene.getStylesheets().add(cssUrl.toExternalForm());
			}

			Stage dialogStage = new Stage();
			dialogStage.setTitle("Đăng Kí Toa Mới");
			dialogStage.initModality(Modality.APPLICATION_MODAL);
			dialogStage.setScene(scene);
			dialogStage.setResizable(false);
			dialogStage.showAndWait();

			// Sau khi đóng dialog đăng kí toa, làm mới lại dữ liệu
			// để nếu có tàu nào được cập nhật toa mới (tính năng tương lai) thì sẽ hiện ra
			// ngay
			handleRefresh();

		} catch (IOException e) {
			e.printStackTrace();
			showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở form đăng kí toa mới!");
		}
	}

//	@FXML
//	private void handleCreateTrain() {
//		showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Chức năng 'Lập tàu mới' đang được phát triển.");
//	}
	@FXML
	private void handleCreateTrain() {
		openModifyTrainDialog(null, ModifyTrainDialogController.Mode.CREATE);
	}

//	@FXML
//	private void handleConfigureTrain() {
//		Tau selected = trainTable.getSelectionModel().getSelectedItem();
//		if (selected != null) {
//			showAlert(Alert.AlertType.INFORMATION, "Thông báo",
//					"Chức năng 'Cấu hình tàu' cho tàu " + selected.getMacTau() + " đang được phát triển.");
//		}
//	}
	@FXML
	private void handleConfigureTrain() {
		Tau selected = trainTable.getSelectionModel().getSelectedItem();
		if (selected == null) {
			showAlert(Alert.AlertType.WARNING, "Chưa chọn tàu", "Vui lòng chọn một tàu để cấu hình.");
			return;
		}
		openModifyTrainDialog(selected, ModifyTrainDialogController.Mode.CONFIGURE);
	}

	/**
	 * Phương thức chung để mở dialog Cấu hình/Tạo mới tàu.
	 */
	private void openModifyTrainDialog(Tau tau, ModifyTrainDialogController.Mode mode) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/modify-train-dialog.fxml"));
			Scene scene = new Scene(loader.load());

			// Lấy controller và truyền dữ liệu vào
			ModifyTrainDialogController controller = loader.getController();
			controller.initData(tau, mode);

			Stage dialogStage = new Stage();
			dialogStage.setTitle(mode == ModifyTrainDialogController.Mode.CREATE ? "Lập Tàu Mới" : "Cấu Hình Tàu");
			dialogStage.initModality(Modality.APPLICATION_MODAL);
			dialogStage.setScene(scene);

			// Áp dụng CSS
			URL cssUrl = getClass().getResource("/views/train-management.css");
			if (cssUrl != null) {
				scene.getStylesheets().add(cssUrl.toExternalForm());
			}

			dialogStage.showAndWait();

			// Sau khi dialog đóng, làm mới lại bảng chính
			if (controller.isSaveChanges()) {
				handleRefresh();
			}

		} catch (IOException e) {
			e.printStackTrace();
			showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở cửa sổ Cấu hình tàu.");
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