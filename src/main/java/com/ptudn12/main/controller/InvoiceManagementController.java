package com.ptudn12.main.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.ptudn12.main.dao.HoaDonDAO;
import com.ptudn12.main.entity.HoaDon;
import com.ptudn12.main.enums.LoaiHoaDon;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class InvoiceManagementController {

	@FXML
	private TableView<HoaDon> invoiceTable;
	@FXML
	private TableColumn<HoaDon, String> idColumn;
	@FXML
	private TableColumn<HoaDon, String> employeeColumn;
	@FXML
	private TableColumn<HoaDon, String> customerColumn;
	@FXML
	private TableColumn<HoaDon, LocalDateTime> dateColumn;
	@FXML
	private TableColumn<HoaDon, LoaiHoaDon> typeColumn;

	@FXML
	private Label totalInvoicesLabel;
	@FXML
	private ComboBox<Integer> dayFilterCombo;
	@FXML
	private ComboBox<Integer> monthFilterCombo;
	@FXML
	private ComboBox<Integer> yearFilterCombo;

	private final HoaDonDAO hoaDonDAO = new HoaDonDAO();
	private LocalDate currentFilterDate = null;

	// Danh sách dữ liệu hóa đơn
	private ObservableList<HoaDon> invoiceData = FXCollections.observableArrayList();

	@FXML
	public void initialize() {
		// Liên kết các cột với thuộc tính của đối tượng HoaDon
		idColumn.setCellValueFactory(new PropertyValueFactory<>("maHoaDon"));
		dateColumn.setCellValueFactory(new PropertyValueFactory<>("ngayLap"));
		typeColumn.setCellValueFactory(new PropertyValueFactory<>("loaiHoaDon"));

		// Sử dụng lambda để truy cập thuộc tính lồng nhau (nested property)
		// Lấy tên nhân viên từ đối tượng NhanVien bên trong HoaDon
		employeeColumn.setCellValueFactory(
				cellData -> new SimpleStringProperty(cellData.getValue().getNhanVien().getTenNhanVien()));

		// Lấy tên khách hàng từ đối tượng KhachHang bên trong HoaDon
		customerColumn.setCellValueFactory(
				cellData -> new SimpleStringProperty(cellData.getValue().getKhachHang().getTenKhachHang()));

		// Tải dữ liệu từ cơ sở dữ liệu
		setupFilterControls();
		loadDataFromDatabase();
	}

	/**
	 * Tải TOÀN BỘ dữ liệu từ database và cập nhật bảng.
	 */
	private void loadAllData() {
		invoiceData.clear();
		List<HoaDon> danhSach = hoaDonDAO.layTatCaHoaDon();
		invoiceData.addAll(danhSach);
		invoiceTable.setItems(invoiceData);
		updateTotalInvoicesLabel();
	}

	/**
	 * Tải dữ liệu ĐÃ LỌC từ database và cập nhật bảng.
	 * 
	 * @param startDate Ngày bắt đầu lọc.
	 */
	private void loadFilteredData(LocalDate startDate) {
		invoiceData.clear();
		List<HoaDon> filteredList = hoaDonDAO.layHoaDonTuNgay(startDate);
		invoiceData.setAll(filteredList);
		updateTotalInvoicesLabel();
	}

	private void updateTotalInvoicesLabel() {
		int count = invoiceData.size();
		totalInvoicesLabel.setText("Có " + count + " hóa đơn");
	}

	/**
	 * Xử lý sự kiện khi nhấn nút "Lọc".
	 */
	@FXML
	private void handleFilterByDate() {
		Integer day = dayFilterCombo.getValue();
		Integer month = monthFilterCombo.getValue();
		Integer year = yearFilterCombo.getValue();

		if (day == null || month == null || year == null) {
			showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng chọn đầy đủ ngày, tháng, và năm để lọc.");
			return;
		}

		// Lưu lại ngày đang lọc
		this.currentFilterDate = LocalDate.of(year, month, day);

		// Tải dữ liệu theo bộ lọc
		loadFilteredData(currentFilterDate);
	}

	/**
	 * Xử lý sự kiện cho nút "Làm mới": Tải lại dữ liệu theo trạng thái hiện tại.
	 */
	@FXML
	private void handleRefresh() {
		if (currentFilterDate != null) {
			// Nếu đang có bộ lọc, chạy lại query lọc
			loadFilteredData(currentFilterDate);
		} else {
			// Nếu không có bộ lọc, tải lại tất cả
			loadAllData();
		}
		showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Dữ liệu đã được làm mới thành công!");
	}

	/**
	 * Xử lý sự kiện cho nút "Hiển thị tất cả": Reset bộ lọc và tải lại toàn bộ.
	 */
	@FXML
	private void handleShowAll() {
		// Reset trạng thái lọc
		this.currentFilterDate = null;

		// Xóa các lựa chọn trong ComboBox
		dayFilterCombo.setValue(null);
		monthFilterCombo.setValue(null);
		yearFilterCombo.setValue(null);

		// Tải lại toàn bộ dữ liệu
		loadAllData();
	}

	private void setupFilterControls() {
		// Populate tháng (1-12)
		monthFilterCombo.setItems(
				FXCollections.observableArrayList(IntStream.rangeClosed(1, 12).boxed().collect(Collectors.toList())));

		// Populate năm (từ năm cũ nhất trong DB đến năm hiện tại)
		int oldestYear = hoaDonDAO.getOldestInvoiceYear();
		int currentYear = LocalDate.now().getYear();
		if (oldestYear != -1 && oldestYear < currentYear) {
			yearFilterCombo.setItems(FXCollections.observableArrayList(
					IntStream.rangeClosed(oldestYear, currentYear).boxed().collect(Collectors.toList())));
		} else {
			yearFilterCombo.getItems().add(currentYear); // Nếu không có dữ liệu cũ
		}

		// Thêm listener để cập nhật số ngày khi tháng hoặc năm thay đổi
		monthFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> updateDayComboBox());
		yearFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> updateDayComboBox());

		// Khởi tạo ComboBox ngày
		updateDayComboBox();
	}

	private void updateDayComboBox() {
		Integer selectedMonth = monthFilterCombo.getValue();
		Integer selectedYear = yearFilterCombo.getValue();

		// Nếu chưa chọn tháng/năm, mặc định là tháng/năm hiện tại để tính số ngày
		if (selectedMonth == null)
			selectedMonth = LocalDate.now().getMonthValue();
		if (selectedYear == null)
			selectedYear = LocalDate.now().getYear();

		int daysInMonth = YearMonth.of(selectedYear, selectedMonth).lengthOfMonth();

		Integer previouslySelectedDay = dayFilterCombo.getValue();
		dayFilterCombo.setItems(FXCollections
				.observableArrayList(IntStream.rangeClosed(1, daysInMonth).boxed().collect(Collectors.toList())));

		// Nếu ngày đã chọn trước đó lớn hơn ngày tối đa của tháng mới, chọn ngày cuối
		// tháng
		if (previouslySelectedDay != null) {
			if (previouslySelectedDay > daysInMonth) {
				dayFilterCombo.setValue(daysInMonth);
			} else {
				dayFilterCombo.setValue(previouslySelectedDay);
			}
		}
	}

	private void loadDataFromDatabase() {
		invoiceData.clear();
		List<HoaDon> danhSach = hoaDonDAO.layTatCaHoaDon();
		invoiceData.addAll(danhSach);
		invoiceTable.setItems(invoiceData);
		updateTotalInvoicesLabel();
	}

	@FXML
	private void handlePrintInvoice() {
		HoaDon selectedInvoice = invoiceTable.getSelectionModel().getSelectedItem();

		if (selectedInvoice == null) {
			showAlert(Alert.AlertType.WARNING, "Chưa chọn hóa đơn", "Vui lòng chọn một hóa đơn trong danh sách để in.");
			return;
		}

		// TODO: Thêm logic xử lý in hóa đơn ở đây.
		// Ví dụ: tạo một file PDF, gọi một service in ấn, v.v.

//		showAlert(Alert.AlertType.INFORMATION, "Thông báo",
//				"Đã gửi yêu cầu in cho hóa đơn mã: " + selectedInvoice.getMaHoaDon());
		showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Chức năng đang phát triển");
	}

	private void showAlert(Alert.AlertType type, String title, String message) {
		Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}
}