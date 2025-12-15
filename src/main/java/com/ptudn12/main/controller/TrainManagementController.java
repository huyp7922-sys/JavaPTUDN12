package com.ptudn12.main.controller;

import java.io.IOException;
import java.net.URL;
import java.util.List;

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

	// --- Data Models ---
	private ObservableList<Tau> trainData = FXCollections.observableArrayList();
	// Map lưu trữ danh sách toa của từng tàu: Key là mã tàu (VD: "SE1"), Value là
	// danh sách Toa

	// --- DAO ---
	private TauDAO tauDAO;

	@FXML
	public void initialize() {
		this.tauDAO = new TauDAO();

		// ✅ LIÊN KẾT TRỰC TIẾP CÁC CỘT VỚI CÁC TRƯỜNG MỚI CỦA ENTITY
		trainCodeColumn.setCellValueFactory(new PropertyValueFactory<>("macTau"));
		statusColumn.setCellValueFactory(new PropertyValueFactory<>("trangThai"));
		carriageCountColumn.setCellValueFactory(new PropertyValueFactory<>("soToa"));
		totalSeatsColumn.setCellValueFactory(new PropertyValueFactory<>("tongChoNgoi"));
		structureColumn.setCellValueFactory(new PropertyValueFactory<>("cauTrucTau"));

		// ❌ KHÔNG CẦN GỌI setupCalculatedColumns() NỮA

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
			trainData.clear();
			List<Tau> allTrains = tauDAO.layTatCaTau(); // Chỉ cần gọi 1 hàm duy nhất
			if (allTrains != null) {
				trainData.addAll(allTrains);
			}
			trainTable.setItems(trainData);
			updateTrainCountLabel();
		} catch (Exception e) {
			e.printStackTrace();
			showAlert(Alert.AlertType.ERROR, "Lỗi Tải Dữ Liệu",
					"Không thể tải danh sách tàu từ cơ sở dữ liệu.\nChi tiết: " + e.getMessage());
		}
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
				// Reset style cũ
				getStyleClass().removeAll("status-label", "status-ready", "status-inactive", "status-paused",
						"status-running");

				if (empty || status == null) {
					setText(null);
					setGraphic(null);
				} else {
					Label label = new Label(status);
					label.getStyleClass().add("status-label");

					// Ánh xạ trạng thái từ DB sang CSS class
					// Bạn có thể điều chỉnh các case này tùy theo giá trị thực tế trong DB của bạn
					switch (status) {
					case "SanSang":
						label.getStyleClass().add("status-ready");
						break;
					case "DangChay":
						label.getStyleClass().add("status-running");
						break; // Cần thêm class này vào CSS nếu muốn màu riêng
					case "TamNgung":
						label.getStyleClass().add("status-paused");
						break; // Cần thêm class này vào CSS nếu muốn màu riêng
					case "ChuaKhoiHanh":
						label.getStyleClass().add("status-inactive");
						break;
					default:
						label.getStyleClass().add("status-inactive");
						break;
					}

					setGraphic(label);
					setText(null);
				}
			}
		});
	}

	private void updateTrainCountLabel() {
		totalTrainsLabel.setText("Có tất cả " + trainData.size() + " tàu");
	}

	// --- ACTION HANDLERS ---

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