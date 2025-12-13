package com.ptudn12.main.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.ptudn12.main.dao.TauDAO;
import com.ptudn12.main.entity.Toa;
import com.ptudn12.main.enums.LoaiToa;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class AddCarriageSelectionDialogController {

	@FXML
	private ComboBox<String> filterComboBox;
	@FXML
	private TableView<Toa> availableCarriagesTable;
	@FXML
	private TableColumn<Toa, String> carriageIdColumn; // tenToa là String -> Đúng
	@FXML
	private TableColumn<Toa, LoaiToa> carriageTypeColumn; // loaiToa là LoaiToa -> Đúng
	@FXML
	private StackPane carriageDetailContainer;
	@FXML
	private Button selectButton;
	@FXML
	private Button exitButton;

	private ObservableList<Toa> displayList = FXCollections.observableArrayList();
	private List<Toa> masterList; // Danh sách gốc từ DB
	private Toa selectedToa = null;
	private TauDAO tauDAO;

	@FXML
	public void initialize() {
		this.masterList = new ArrayList<>();
		// --- Cài đặt bảng ---
		carriageIdColumn.setCellValueFactory(new PropertyValueFactory<>("tenToa"));
		carriageTypeColumn.setCellValueFactory(new PropertyValueFactory<>("loaiToa"));

		// --- Cài đặt ComboBox lọc ---
		List<String> filterOptions = new ArrayList<>();
		filterOptions.add("Tất cả toa");
		for (LoaiToa type : LoaiToa.values()) {
			filterOptions.add(type.getDescription());
		}
		filterComboBox.setItems(FXCollections.observableArrayList(filterOptions));
		filterComboBox.setValue("Tất cả toa");
		filterComboBox.setOnAction(event -> applyFilter());

		// --- Cài đặt listener cho bảng ---
		availableCarriagesTable.getSelectionModel().selectedItemProperty()
				.addListener((obs, oldSelection, newSelection) -> {
					if (newSelection != null) {
						displayCarriageDetails(newSelection);
						selectButton.setDisable(false);
					} else {
						selectButton.setDisable(true);
					}
				});

		// --- Trạng thái ban đầu ---
		selectButton.setDisable(true);
		loadMockData(); // Tạm thời dùng dữ liệu giả
		availableCarriagesTable.setItems(displayList);
	}

	public void initData(TauDAO tauDAO) {
		this.tauDAO = tauDAO;
		loadDataFromDatabase();
	}

	private void loadDataFromDatabase() {
		masterList = tauDAO.layTatCaToaChuaSuDung();
		displayList.setAll(masterList);
		availableCarriagesTable.setItems(displayList);
	}

	private void applyFilter() {
		// TODO: Viết logic lọc
	}

	private void displayCarriageDetails(Toa toa) {
		try {
			URL frameUrl = getClass().getResource("/views/carriage-frame.fxml");
			FXMLLoader frameLoader = new FXMLLoader(frameUrl);
			Node carriageFrameNode = frameLoader.load();
			CarriageFrameController frameController = frameLoader.getController();

			Node carLayoutContent = null;
			LoaiToa type = toa.getLoaiToa();

			// ✅ SỬA LẠI: Dùng switch để tải đúng layout và gọi đúng hàm getSoChoMacDinh()
			switch (type) {
			case NGOI_MEM:
				URL seatUrl = getClass().getResource("/views/soft-seat-car-layout.fxml");
				FXMLLoader seatLoader = new FXMLLoader(seatUrl);
				carLayoutContent = seatLoader.load();
				SoftSeatCarController seatCtrl = seatLoader.getController();
				seatCtrl.initViewOnlyMode(type.getSoChoMacDinh(type), null); // Gọi hàm đã sửa
				break;
			case NGOI_CUNG:
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
				SleeperCarController sleeperCtrl = sleeperLoader.getController();
				int tiers = (type == LoaiToa.GIUONG_NAM_KHOANG_6) ? 3 : (type == LoaiToa.GIUONG_NAM_KHOANG_4) ? 2 : 1;
				sleeperCtrl.initViewOnlyMode(tiers, null);
				break;
			}

			String title = String.format("Cấu trúc của toa %s, loại %s", toa.getTenToa(),
					toa.getLoaiToa().getDescription());
			frameController.setTitle(title);
			frameController.setContent(carLayoutContent);

			carriageDetailContainer.getChildren().setAll(carriageFrameNode);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	private void handleSelect() {
		this.selectedToa = availableCarriagesTable.getSelectionModel().getSelectedItem();
		closeStage();
	}

	@FXML
	private void handleExit() {
		this.selectedToa = null;
		closeStage();
	}

	// Phương thức để controller cha lấy kết quả
	public Toa getSelectedToa() {
		return this.selectedToa;
	}

	private void closeStage() {
		Stage stage = (Stage) exitButton.getScene().getWindow();
		stage.close();
	}

	// Dữ liệu giả để test giao diện
	private void loadMockData() {
		masterList.clear(); // Xóa dữ liệu cũ
		// Giờ chúng ta sẽ tạo đối tượng Toa bằng constructor đầy đủ
		masterList.add(new Toa(101, "NM08", LoaiToa.NGOI_MEM));
		masterList.add(new Toa(102, "NM09", LoaiToa.NGOI_MEM));
		masterList.add(new Toa(201, "GN405", LoaiToa.GIUONG_NAM_KHOANG_4));
		masterList.add(new Toa(301, "GN606", LoaiToa.GIUONG_NAM_KHOANG_6));
		masterList.add(new Toa(401, "VIP05", LoaiToa.GIUONG_NAM_VIP));
		displayList.setAll(masterList);
	}
}