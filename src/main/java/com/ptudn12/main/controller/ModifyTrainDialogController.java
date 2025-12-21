package com.ptudn12.main.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.ptudn12.main.dao.TauDAO;
import com.ptudn12.main.entity.Tau;
import com.ptudn12.main.entity.Toa;
import com.ptudn12.main.enums.LoaiToa;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ModifyTrainDialogController {

	// Enum để xác định chế độ hoạt động
	public enum Mode {
		CREATE, CONFIGURE
	}

	private Mode currentMode;

	private static class CarriageWrapper {
		Toa toa;
		int sequenceNumber; // Số thứ tự được gán ban đầu

		public CarriageWrapper(Toa toa, int sequenceNumber) {
			this.toa = toa;
			this.sequenceNumber = sequenceNumber;
		}
	}

	// --- Injected FXML Components ---
	@FXML
	private HBox trainTrackContainer;
	@FXML
	private TextField trainIdTextField;
	@FXML
	private Button navLeftButton, navRightButton, removeCarriageButton, exitButton;
	@FXML
	private StackPane carriageDetailContainer;
	@FXML
	private Label defaultCenterLabel;
	@FXML
	private HBox legendContainer;
	@FXML
	private Button saveButton;

	// --- Data Model ---
	private List<CarriageWrapper> currentCarriages; // Toa trên tàu
	private List<Toa> availableCarriages; // Toa còn trống
	private int selectedCarriageIndex = -1;
	private boolean hasUnsavedChanges = false;

	private TauDAO tauDAO;
	private boolean saveChanges = false;

	/**
	 * ✅ THÊM MỚI: Phương thức để controller cha kiểm tra trạng thái
	 */
	public boolean isSaveChanges() {
		return this.saveChanges;
	}

	@FXML
	public void initialize() {
		// Ẩn các nút chức năng chưa được cài đặt
		removeCarriageButton.setVisible(false);
		navLeftButton.setVisible(false);
		navRightButton.setVisible(false);

		this.tauDAO = new TauDAO();
		this.availableCarriages = new ArrayList<>();
	}

	/**
	 * Phương thức chính được gọi từ bên ngoài để khởi tạo dialog.
	 * 
	 * @param tau  Đối tượng tàu cần cấu hình, hoặc null nếu là tạo mới.
	 * @param mode Chế độ hoạt động (CREATE hoặc CONFIGURE).
	 */
	public void initData(Tau tau, Mode mode) {
		this.currentMode = mode;
		this.currentCarriages = new ArrayList<>();

		if (mode == Mode.CREATE) {
			trainIdTextField.setDisable(false);
			trainIdTextField.setText("");
			// Bắt đầu với tàu trống, tải tất cả toa chưa sử dụng
			this.availableCarriages = tauDAO.layTatCaToaChuaSuDung();
		} else if (mode == Mode.CONFIGURE && tau != null) {
			trainIdTextField.setDisable(true);
			trainIdTextField.setText(tau.getMacTau());

			// 1. Tải cấu trúc toa THẬT của tàu đang sửa
			Map<Integer, Toa> cauTrucThat = tauDAO.layCauTrucToaCuaTau(tau.getMacTau());

			// 2. Chuyển đổi Map thành List<CarriageWrapper>
			for (Map.Entry<Integer, Toa> entry : cauTrucThat.entrySet()) {
				currentCarriages.add(new CarriageWrapper(entry.getValue(), entry.getKey()));
			}
			// Sắp xếp lại danh sách theo đúng số thứ tự (mặc dù map đã sort, nhưng list thì
			// chưa)
			currentCarriages.sort(Comparator.comparingInt(cw -> cw.sequenceNumber));

			Collections.reverse(currentCarriages);

			// 3. Tải danh sách toa trống (toa chưa gán cho BẤT KỲ tàu nào)
			this.availableCarriages = tauDAO.layTatCaToaChuaSuDung();
		}

		redrawTrainTrack();
		createLegend();
	}

	/**
	 * Vẽ lại toàn bộ thanh track tàu ở trên cùng.
	 */
//	private void redrawTrainTrack() {
//		trainTrackContainer.setAlignment(Pos.BOTTOM_LEFT);
//		trainTrackContainer.getChildren().clear();
//		trainTrackContainer.getChildren().add(createAddButton(-1));
//
//		for (int i = 0; i < currentCarriages.size(); i++) {
//			CarriageWrapper wrapper = currentCarriages.get(i);
//			int carriageNumberToDisplay;
//
//			// LOGIC QUAN TRỌNG:
//			// - Lập tàu mới: Tính lại số thứ tự từ 1 đến N
//			// - Cấu hình tàu: Giữ nguyên số thứ tự ban đầu của toa
//			if (currentMode == Mode.CREATE) {
//				carriageNumberToDisplay = currentCarriages.size() - i;
//			} else { // Mode.CONFIGURE
//				carriageNumberToDisplay = wrapper.sequenceNumber;
//			}
//
//			Node carriageNode = createCarriageNode(wrapper.toa, carriageNumberToDisplay, i);
//			trainTrackContainer.getChildren().add(carriageNode);
//			trainTrackContainer.getChildren().add(createAddButton(i));
//		}
//
//		trainTrackContainer.getChildren().add(createEngineNode());
//	}
	private void redrawTrainTrack() {
		trainTrackContainer.setAlignment(Pos.BOTTOM_LEFT);
		trainTrackContainer.getChildren().clear();

		// --- LOGIC TÍNH TOÁN LẠI SỐ THỨ TỰ (ÁP DỤNG CHO CẢ 2 CHẾ ĐỘ) ---
		for (int i = 0; i < currentCarriages.size(); i++) {
			// Toa ngoài cùng bên trái (index 0) sẽ có số thứ tự lớn nhất.
			// Toa trong cùng bên phải (index cuối) sẽ có số thứ tự là 1.
			currentCarriages.get(i).sequenceNumber = currentCarriages.size() - i;
		}

		trainTrackContainer.getChildren().add(createAddButton(-1));
		for (int i = 0; i < currentCarriages.size(); i++) {
			CarriageWrapper wrapper = currentCarriages.get(i);
			int carriageNumberToDisplay = wrapper.sequenceNumber;

			Node carriageNode = createCarriageNode(wrapper.toa, carriageNumberToDisplay, i);
			trainTrackContainer.getChildren().add(carriageNode);
			trainTrackContainer.getChildren().add(createAddButton(i));
		}

		trainTrackContainer.getChildren().add(createEngineNode());
	}

	/**
	 * Hiển thị chi tiết cấu trúc của một toa được chọn.
	 * 
	 * @param index Vị trí của toa trong danh sách `currentCarriages`.
	 */
	private void displayCarriageDetails(int index) {
		if (index < 0 || index >= currentCarriages.size())
			return;

		this.selectedCarriageIndex = index;
		Toa selectedToa = currentCarriages.get(index).toa;

		defaultCenterLabel.setVisible(false);
		removeCarriageButton.setVisible(true);
		navLeftButton.setVisible(true);
		navRightButton.setVisible(true);

		try {
			// Bước A: Tải khung toa trước (không đổi)
			URL frameUrl = getClass().getResource("/views/carriage-frame.fxml");
			FXMLLoader frameLoader = new FXMLLoader(frameUrl);
			Node carriageFrameNode = frameLoader.load();
			CarriageFrameController frameController = frameLoader.getController();

			// Bước B: Tải NỘI DUNG SƠ ĐỒ TOA dựa trên loại toa
			Node carLayoutContent = null;
			LoaiToa type = selectedToa.getLoaiToa();

			// Dùng switch để quyết định tải FXML nào
			switch (type) {
			case NGOI_MEM:
				URL softSeatUrl = getClass().getResource("/views/soft-seat-car-layout.fxml");
				FXMLLoader softSeatLoader = new FXMLLoader(softSeatUrl);
				carLayoutContent = softSeatLoader.load();
				SoftSeatCarController softSeatController = softSeatLoader.getController();
				softSeatController.initViewOnlyMode(type.getSoChoMacDinh(type), null); // Tạm thời null loaiCho
				break;
			case NGOI_CUNG: // TÁCH THÀNH CASE RIÊNG
				URL hardSeatUrl = getClass().getResource("/views/hard-seat-car-layout.fxml");
				FXMLLoader hardSeatLoader = new FXMLLoader(hardSeatUrl);
				carLayoutContent = hardSeatLoader.load();
				HardSeatCarController hardSeatController = hardSeatLoader.getController();
				hardSeatController.initViewOnlyMode(type.getSoChoMacDinh(type), null);
				break;

			case GIUONG_NAM_KHOANG_6:
			case GIUONG_NAM_KHOANG_4:
			case GIUONG_NAM_VIP:
				URL sleeperUrl = getClass().getResource("/views/sleeper-car-layout.fxml");
				FXMLLoader sleeperLoader = new FXMLLoader(sleeperUrl);
				carLayoutContent = sleeperLoader.load();
				SleeperCarController sleeperController = sleeperLoader.getController();
				int tiers = (type == LoaiToa.GIUONG_NAM_KHOANG_6) ? 3 : (type == LoaiToa.GIUONG_NAM_KHOANG_4) ? 2 : 1;
				sleeperController.initViewOnlyMode(tiers, null); // Tạm thời null loaiCho
				break;
			}

			// Bước C: Lắp ráp (không đổi)
			int carriageNumber = currentCarriages.size() - index;
			frameController.setTitle("Toa số " + carriageNumber + ": " + selectedToa.getLoaiToa().getDescription());
			frameController.setContent(carLayoutContent);

			carriageDetailContainer.getChildren().setAll(carriageFrameNode, removeCarriageButton);
			StackPane.setAlignment(removeCarriageButton, Pos.TOP_RIGHT);

			updateNavigationButtonsState();

		} catch (IOException e) {
			e.printStackTrace();
			defaultCenterLabel.setText("Lỗi tải giao diện toa.");
			defaultCenterLabel.setVisible(true);
		}
	}

	/**
	 * ✅ THÊM MỚI: Hàm xử lý sự kiện cho nút "Bỏ toa".
	 */
	@FXML
	private void handleRemoveCarriage() {
		if (selectedCarriageIndex < 0 || selectedCarriageIndex >= currentCarriages.size())
			return;

		// 1. Xóa toa khỏi đoàn tàu
		CarriageWrapper removedWrapper = currentCarriages.remove(selectedCarriageIndex);

		// 2. TRẢ LẠI toa đó vào danh sách toa trống (visual only)
		availableCarriages.add(removedWrapper.toa);

		hasUnsavedChanges = true;
		redrawTrainTrack();
		resetDetailView();
	}

	/**
	 * ✅ THÊM MỚI: Hàm tiện ích để trả vùng chi tiết về mặc định.
	 */
	private void resetDetailView() {
		// Xóa nội dung hiện tại (khung toa)
		carriageDetailContainer.getChildren().clear();

		// Hiện lại nhãn "Hãy chọn 1 toa"
		defaultCenterLabel.setVisible(true);
		carriageDetailContainer.getChildren().add(defaultCenterLabel);

		// Ẩn nút "Bỏ toa"
		removeCarriageButton.setVisible(false);
		// Nút này nằm trong FXML, chúng ta cần thêm lại nó vào StackPane nhưng để ẩn
		carriageDetailContainer.getChildren().add(removeCarriageButton);

		// Vô hiệu hóa các nút điều hướng
		navLeftButton.setDisable(true);
		navRightButton.setDisable(true);

		// Reset index đang chọn
		selectedCarriageIndex = -1;
	}

	/**
	 * ✅ THÊM MỚI: Xử lý sự kiện click nút điều hướng sang trái.
	 */
	@FXML
	private void handleNavLeft() {
		// Chỉ di chuyển nếu index hiện tại lớn hơn 0
		if (selectedCarriageIndex > 0) {
			displayCarriageDetails(selectedCarriageIndex - 1);
		}
	}

	/**
	 * ✅ THÊM MỚI: Xử lý sự kiện click nút điều hướng sang phải.
	 */
	@FXML
	private void handleNavRight() {
		// Chỉ di chuyển nếu index hiện tại nhỏ hơn vị trí cuối cùng
		if (selectedCarriageIndex < currentCarriages.size() - 1) {
			displayCarriageDetails(selectedCarriageIndex + 1);
		}
	}

	/**
	 * ✅ THÊM MỚI: Cập nhật trạng thái (enabled/disabled) của 2 nút mũi tên. Hàm này
	 * được gọi mỗi khi một toa mới được hiển thị.
	 */
	private void updateNavigationButtonsState() {
		// Nút trái bị vô hiệu hóa nếu đang ở toa đầu tiên (index = 0)
		navLeftButton.setDisable(selectedCarriageIndex <= 0);

		// Nút phải bị vô hiệu hóa nếu đang ở toa cuối cùng
		navRightButton.setDisable(selectedCarriageIndex >= currentCarriages.size() - 1);
	}

	// --- Các hàm tạo giao diện phụ ---

	private Node createCarriageNode(Toa toa, int carriageNumber, final int index) {
		VBox container = new VBox(5);
		container.setAlignment(Pos.CENTER);

		Label numberLabel = new Label(String.valueOf(carriageNumber));
		Pane carriageRect = new Pane();
		carriageRect.setPrefSize(80, 40);
		carriageRect.setStyle("-fx-background-color: " + getColorForToa(toa.getLoaiToa())
				+ "; -fx-background-radius: 5; -fx-border-color: black; -fx-border-radius: 5;");

		container.getChildren().addAll(numberLabel, carriageRect);

		// Thêm sự kiện click
		container.setOnMouseClicked(event -> {
			displayCarriageDetails(index);
		});

		return container;
	}

	private Node createAddButton(final int index) {
		Button addButton = new Button("+");
		addButton.getStyleClass().add("add-carriage-button");
		addButton.setOnAction(event -> {
			// ✅ THAY ĐỔI LOGIC Ở ĐÂY
			openAddCarriageDialog(index);
		});
		return addButton;
	}

	/**
	 * ✅ THÊM MỚI: Mở dialog chọn toa và xử lý kết quả.
	 * 
	 * @param insertIndex Vị trí sẽ chèn toa mới vào.
	 */
	private void openAddCarriageDialog(int insertIndex) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/add-carriage-selection-dialog.fxml"));
			Scene scene = new Scene(loader.load());

			// Áp dụng CSS
			URL cssUrl = getClass().getResource("/views/train-management.css");
			if (cssUrl != null)
				scene.getStylesheets().add(cssUrl.toExternalForm());

			AddCarriageSelectionDialogController controller = loader.getController();

			controller.initData(this.availableCarriages);

			Stage dialogStage = new Stage();
			dialogStage.setTitle("Chọn Toa Để Thêm Vào Tàu");
			dialogStage.initModality(Modality.APPLICATION_MODAL);
			dialogStage.setScene(scene);
			dialogStage.showAndWait();

			// Lấy kết quả sau khi dialog đóng
			Toa selectedToa = controller.getSelectedToa();
			if (selectedToa != null) {
				// 1. Thêm toa vào đoàn tàu
				currentCarriages.add(insertIndex + 1, new CarriageWrapper(selectedToa, 0));

				// 2. XÓA toa đó khỏi danh sách toa trống (visual only)
				availableCarriages.remove(selectedToa);

				hasUnsavedChanges = true;
				redrawTrainTrack();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private Node createEngineNode() {
		VBox container = new VBox(5);
		container.setAlignment(Pos.CENTER);
		Label label = new Label("Đầu tàu");
		Pane engineRect = new Pane();
		engineRect.setPrefSize(100, 40);
		engineRect.setStyle(
				"-fx-background-color: #555555; -fx-background-radius: 5; -fx-border-color: black; -fx-border-radius: 5;");
		container.getChildren().addAll(label, engineRect);
		return container;
	}

	private void createLegend() {
		legendContainer.getChildren().clear();
		for (LoaiToa type : LoaiToa.values()) {
			HBox legendItem = new HBox(5);
			legendItem.setAlignment(Pos.CENTER_LEFT);
			Rectangle colorRect = new Rectangle(20, 20, Color.web(getColorForToa(type)));
			colorRect.setStroke(Color.BLACK);
			Label nameLabel = new Label(type.getDescription());
			legendItem.getChildren().addAll(colorRect, nameLabel);
			legendContainer.getChildren().add(legendItem);
		}
	}

	private String getColorForToa(LoaiToa type) {
		if (type == LoaiToa.NGOI_MEM) {
			return "#0f0";
		}
		if (type == LoaiToa.NGOI_CUNG) {
			return "#0a0";
		}
		if (type == LoaiToa.GIUONG_NAM_VIP) {
			return "yellow";
		}
		if (type == LoaiToa.GIUONG_NAM_KHOANG_4) {
			return "#00f";
		}
		if (type == LoaiToa.GIUONG_NAM_KHOANG_6) {
			return "#00a";
		}
		return "grey";
	}

	// --- Dữ liệu giả ---
	private void createMockCarriages() {
		currentCarriages.add(new CarriageWrapper(new Toa(LoaiToa.NGOI_MEM), 5));
		currentCarriages.add(new CarriageWrapper(new Toa(LoaiToa.GIUONG_NAM_KHOANG_4), 4));
		currentCarriages.add(new CarriageWrapper(new Toa(LoaiToa.GIUONG_NAM_KHOANG_4), 3));
		currentCarriages.add(new CarriageWrapper(new Toa(LoaiToa.GIUONG_NAM_KHOANG_6), 2));
		currentCarriages.add(new CarriageWrapper(new Toa(LoaiToa.NGOI_CUNG), 1));
	}

	@FXML
	private void handleExit() {
		if (hasUnsavedChanges) {
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Xác nhận thoát");
			alert.setHeaderText("Bạn có thay đổi chưa được lưu.");
			alert.setContentText("Bạn có muốn lưu lại trước khi thoát không?");

			ButtonType btnSave = new ButtonType("Lưu");
			ButtonType btnDontSave = new ButtonType("Không Lưu");
			ButtonType btnCancel = new ButtonType("Hủy");

			alert.getButtonTypes().setAll(btnSave, btnDontSave, btnCancel);

			Optional<ButtonType> result = alert.showAndWait();

			if (result.isPresent()) {
				if (result.get() == btnSave) {
					// Nếu người dùng chọn Lưu, gọi hàm handleSave()
					// Nếu lưu thành công (hàm trả về true), thì mới đóng cửa sổ.
					if (handleSave()) {
						closeStage();
					}
					// Nếu lưu thất bại (validation fail), không làm gì cả, ở lại màn hình.
				} else if (result.get() == btnDontSave) {
					// Nếu chọn Không Lưu, đóng cửa sổ ngay
					closeStage();
				}
				// Nếu chọn Hủy hoặc đóng dialog, không làm gì cả.
			}
		} else {
			// Nếu không có thay đổi, đóng cửa sổ ngay
			closeStage();
		}
	}

	/**
	 * ✅ THÊM MỚI: Xử lý sự kiện cho nút "Lưu".
	 * 
	 * @return true nếu lưu thành công (hoặc không cần lưu), false nếu validation
	 *         thất bại.
	 */
	@FXML
	private boolean handleSave() {

		// --- BƯỚC 1: VALIDATION ---
		// Kiểm tra mác tàu không được trống
		if (trainIdTextField.getText() == null || trainIdTextField.getText().trim().isEmpty()) {
			showAlert(AlertType.ERROR, "Lỗi Lưu", "Mác tàu không được để trống.");
			return false;
		}

		// Kiểm tra số lượng toa
		int carriageCount = currentCarriages.size();
		if (carriageCount < 3 || carriageCount > 16) {
			showAlert(AlertType.ERROR, "Lỗi Lưu",
					"Số lượng toa phải từ 3 đến 16.\nHiện tại: " + carriageCount + " toa.");
			return false;
		}

		// TODO: Kiểm tra mác tàu trùng lặp trong DB (nếu ở chế độ CREATE)

		// --- BƯỚC 2: THỰC HIỆN LƯU ---
		// TODO: Viết logic gọi DAO để lưu
		String macTau = trainIdTextField.getText().trim();
		boolean isSuccess = false;
		if (currentMode == Mode.CREATE) {
			// ✅ THÊM MỚI: Kiểm tra mác tàu trùng lặp trước khi lưu
			if (tauDAO.kiemTraMacTauTonTai(macTau)) {
				showAlert(AlertType.ERROR, "Lỗi Lưu",
						"Mác tàu '" + macTau + "' đã tồn tại. Vui lòng chọn mác tàu khác.");
				return false;
			}

			List<Toa> toasToAdd = currentCarriages.stream().map(w -> w.toa).collect(Collectors.toList());
			Tau newTau = new Tau(macTau);
			isSuccess = tauDAO.lapTauMoi(newTau, toasToAdd);
		} else if (currentMode == Mode.CONFIGURE) {
			Map<Integer, Toa> cauTrucMoi = new LinkedHashMap<>();
			for (CarriageWrapper wrapper : currentCarriages) {
				cauTrucMoi.put(wrapper.sequenceNumber, wrapper.toa);
			}

			// 2. Gọi DAO để thực hiện transaction cập nhật
			isSuccess = tauDAO.capNhatCauTrucTau(macTau, cauTrucMoi);
		}

		if (isSuccess) {
			hasUnsavedChanges = false;
			this.saveChanges = true;

			if (currentMode == Mode.CREATE) {
				// Hiển thị dialog xác nhận buộc click
				Alert confirmationAlert = new Alert(AlertType.INFORMATION);
				confirmationAlert.setTitle("Thành Công");
				confirmationAlert.setHeaderText("Đã lập tàu '" + trainIdTextField.getText().trim() + "' thành công.");
				confirmationAlert.setContentText("Danh sách tàu sẽ được cập nhật.");
				confirmationAlert.showAndWait(); // Chờ người dùng nhấn OK

				// Đóng cửa sổ sau khi người dùng xác nhận
				closeStage();
			} else {
				// Với chế độ Cấu hình, chỉ cần báo thành công là đủ
				showAlert(AlertType.INFORMATION, "Thành Công", "Đã lưu cấu trúc tàu thành công!");
			}

			return true;
		} else {
			showAlert(AlertType.ERROR, "Lỗi", "Lưu thất bại. Vui lòng xem console.");
			return false;
		}
	}

	private void closeStage() {
		Stage stage = (Stage) exitButton.getScene().getWindow();
		stage.close();
	}

	private void showAlert(AlertType type, String title, String content) {
		Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(content);
		alert.showAndWait();
	}
}