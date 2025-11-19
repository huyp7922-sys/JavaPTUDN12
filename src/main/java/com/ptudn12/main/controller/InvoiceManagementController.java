package com.ptudn12.main.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import com.ptudn12.main.dao.HoaDonDAO;
import com.ptudn12.main.entity.HoaDon;
import com.ptudn12.main.enums.LoaiHoaDon;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

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
	private TableColumn<HoaDon, String> dateColumn;
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
	private TemplateEngine templateEngine;

	final double TAX_RATE = 0.08;

	// Danh sách dữ liệu hóa đơn
	private ObservableList<HoaDon> invoiceData = FXCollections.observableArrayList();

	@FXML
	public void initialize() {
		// Liên kết các cột với thuộc tính của đối tượng HoaDon
		idColumn.setCellValueFactory(new PropertyValueFactory<>("maHoaDon"));
		dateColumn.setCellValueFactory(cellData -> {
			// Lấy về đối tượng LocalDateTime từ Hóa đơn
			LocalDateTime ngayLap = cellData.getValue().getNgayLap();

			// Kiểm tra nếu ngày lập không null
			if (ngayLap != null) {
				// Tạo một formatter theo mẫu bạn yêu cầu
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
				// Trả về chuỗi đã được định dạng
				return new SimpleStringProperty(ngayLap.format(formatter));
			} else {
				// Trả về chuỗi rỗng nếu không có ngày
				return new SimpleStringProperty("");
			}
		});
		typeColumn.setCellValueFactory(new PropertyValueFactory<>("loaiHoaDon"));
		employeeColumn.setCellValueFactory(
				cellData -> new SimpleStringProperty(cellData.getValue().getNhanVien().getTenNhanVien()));

		customerColumn.setCellValueFactory(
				cellData -> new SimpleStringProperty(cellData.getValue().getKhachHang().getTenKhachHang()));

		// Tải dữ liệu từ cơ sở dữ liệu
		setupFilterControls();
		loadDataFromDatabase();
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

	public InvoiceManagementController() {
		// Khởi tạo Thymeleaf engine ngay khi controller được tạo
		ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
		resolver.setPrefix("/views/"); // Thư mục chứa template
		resolver.setSuffix(".html");
		resolver.setCharacterEncoding("UTF-8");

		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(resolver);
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
		// 1. Lấy hóa đơn được chọn từ bảng
		HoaDon selectedInvoice = invoiceTable.getSelectionModel().getSelectedItem();
		if (selectedInvoice == null) {
			showAlert(Alert.AlertType.WARNING, "Chưa chọn hóa đơn", "Vui lòng chọn một hóa đơn trong danh sách để in.");
			return;
		}

		// Khai báo file tạm ở ngoài để khối `finally` có thể truy cập
		File tempPdfFile = null;
		try {
			// 2. GỌI "NHÀ MÁY" ĐỂ SẢN XUẤT FILE PDF
			// Phương thức này sẽ dùng Thymeleaf, CSS để tạo ra một file PDF vật lý
			tempPdfFile = generateInvoicePdf(selectedInvoice);
			if (tempPdfFile == null || !tempPdfFile.exists()) {
				throw new IOException("Không thể tạo file PDF tạm thời.");
			}

			// 3. TẢI GIAO DIỆN CỦA "PHÒNG TRƯNG BÀY" (PDF VIEWER)
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/pdf-viewer.fxml"));
			Scene scene = new Scene(loader.load());

			// 4. TẠO CỬA SỔ MỚI CHO TRÌNH XEM PDF
			Stage viewerStage = new Stage();
			viewerStage.setTitle("Xem trước Hóa đơn: " + selectedInvoice.getMaHoaDon());
			viewerStage.initModality(Modality.APPLICATION_MODAL); // Khóa cửa sổ chính

			// Lấy kích thước màn hình để đặt kích thước cửa sổ cho phù hợp
//			javafx.geometry.Rectangle2D primaryScreenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
//			viewerStage.setWidth(primaryScreenBounds.getWidth() * 0.85);
//			viewerStage.setHeight(primaryScreenBounds.getHeight() * 0.85);
			viewerStage.setWidth(700);
			viewerStage.setHeight(800);

			viewerStage.setScene(scene);

			// 5. LẤY CONTROLLER CỦA TRÌNH XEM VÀ TRUYỀN DỮ LIỆU VÀO
			PdfViewerController viewerController = loader.getController();
			viewerController.setStage(viewerStage);
			viewerController.loadDocument(tempPdfFile); // <-- Đây là bước quan trọng nhất

			// 6. THIẾT LẬP CƠ CHẾ DỌN DẸP KHI CỬA SỔ BỊ ĐÓNG
			// Khi người dùng đóng cửa sổ viewer, chúng ta cần đóng tài liệu PDF trong bộ
			// nhớ
			viewerStage.setOnCloseRequest(e -> {
				viewerController.closeDocument();
			});

			// 7. HIỂN THỊ CỬA SỔ VÀ CHỜ
			// Code sẽ dừng ở đây cho đến khi người dùng đóng cửa sổ xem trước
			viewerStage.showAndWait();

		} catch (Exception e) {
			e.printStackTrace();
			showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở trình xem trước PDF: " + e.getMessage());
		} finally {
			// 8. DỌN DẸP FILE TẠM
			// Khối `finally` đảm bảo rằng file PDF tạm thời luôn được xóa
			// sau khi cửa sổ xem trước đã được đóng, dù có lỗi hay không.
			if (tempPdfFile != null && tempPdfFile.exists()) {
				tempPdfFile.delete();
			}
		}
	}

	/**
	 * Phương thức này chỉ làm một việc: tạo ra file PDF và trả về đối tượng File.
	 */
	private File generateInvoicePdf(HoaDon invoiceData) throws Exception {
		// BƯỚC 1: LẤY DỮ LIỆU VÀ CHUẨN BỊ CONTEXT CHO THYMELEAF
		// Trong thực tế, bạn sẽ thay thế createDummyData bằng lời gọi đến DAO
		Map<String, Object> data = createDummyData(invoiceData.getMaHoaDon());
		Context context = new Context();
		context.setVariables(data);

		// BƯỚC 2: DÙNG THYMELEAF ĐỂ TẠO RA CHUỖI HTML HOÀN CHỈNH
		// templateEngine đã được khởi tạo trong constructor của class
		String processedHtml = templateEngine.process("invoice-template", context);

		// BƯỚC 3: TẠO FILE PDF TẠM THỜI TỪ CHUỖI HTML
		File tempPdfFile = File.createTempFile("invoice-preview-", ".pdf");
		try (OutputStream os = new FileOutputStream(tempPdfFile)) {
			com.openhtmltopdf.pdfboxout.PdfRendererBuilder builder = new com.openhtmltopdf.pdfboxout.PdfRendererBuilder();

			// Cung cấp font để hiển thị tiếng Việt đúng
			builder.useFont(() -> {
				try {
					InputStream is = getClass().getResourceAsStream("/fonts/times.ttf");
					if (is == null) {
						throw new IOException("Không tìm thấy file font tại: /fonts/times.ttf");
					}
					return is;
				} catch (IOException e) {
					// Bọc lỗi để tuân thủ quy tắc của lambda
					throw new UncheckedIOException(e);
				}
			}, "Times New Roman");

			// Cung cấp HTML và đường dẫn gốc (để OpenHTMLToPdf tìm thấy file CSS)
			String baseUri = getClass().getResource("/views/").toExternalForm();
			builder.withHtmlContent(processedHtml, baseUri);
			builder.toStream(os);
			builder.run();
		}

		// BƯỚC 4: TRẢ VỀ ĐỐI TƯỢNG FILE TẠM ĐỂ PDF VIEWER SỬ DỤNG
		return tempPdfFile;
	}

//	private Map<String, Object> createDummyData(String maHoaDon) {
//		Map<String, Object> data = new HashMap<>();
//		NumberFormat currencyFormatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
//
//		// --- CÁC HẰNG SỐ CẤU HÌNH ---
//		final int MINIMUM_ROWS_IN_TABLE = 15; // Tổng số hàng tối thiểu trong bảng
//		final double INSURANCE_PRICE = 5000.0;
//
//		// --- BƯỚC 1: TẠO DỮ LIỆU VÉ GỐC ---
//		// Trong thực tế, bạn sẽ lấy danh sách này từ DAO
//		List<Map<String, Object>> tickets = new ArrayList<>();
//		tickets.add(Map.of("maVe", "143356995", "tenDichVu", "Vé HK: DIA-DTR-SE30", "dvt", "Vé", "soLuong", 1.0,
//				"donGia", 1113889.0, "thueSuat", 0.08));
//		tickets.add(Map.of("maVe", "143356996", "tenDichVu", "Vé HK: DIA-DTR-SE30", "dvt", "Vé", "soLuong", 1.0,
//				"donGia", 1002778.0, "thueSuat", 0.08));
//		// Thêm vé khác ở đây nếu muốn...
//
//		// --- BƯỚC 2: XỬ LÝ VÀ TÍNH TOÁN DỮ LIỆU ---
//		List<Map<String, Object>> finalItems = new ArrayList<>();
//		double totalTicketQuantity = 0;
//
//		// Xử lý từng vé
//		int stt = 1;
//		for (Map<String, Object> ticket : tickets) {
//			Map<String, Object> item = new HashMap<>();
//			double soLuong = (double) ticket.get("soLuong");
//			double donGia = (double) ticket.get("donGia");
//			double thueSuat = (double) ticket.get("thueSuat");
//
//			double thanhTien = soLuong * donGia;
//			double tienThue = thanhTien * thueSuat;
//			double tongCong = thanhTien + tienThue;
//
//			item.put("stt", stt++);
//			item.put("maVe", ticket.get("maVe"));
//			item.put("tenDichVu", ticket.get("tenDichVu"));
//			item.put("dvt", ticket.get("dvt"));
//			item.put("soLuong", currencyFormatter.format(soLuong));
//			item.put("donGia", currencyFormatter.format(donGia));
//			item.put("thanhTien", currencyFormatter.format(thanhTien));
//			item.put("thueSuat", (thueSuat > 0) ? (int) (thueSuat * 100) + "%" : "KCT");
//			item.put("tienThue", currencyFormatter.format(tienThue));
//			item.put("tongCong", currencyFormatter.format(tongCong));
//
//			finalItems.add(item);
//			totalTicketQuantity += soLuong;
//		}
//
//		// Thêm dòng Phí bảo hiểm
//		if (totalTicketQuantity > 0) {
//			Map<String, Object> insuranceItem = new HashMap<>();
//			double thanhTienBH = totalTicketQuantity * INSURANCE_PRICE;
//			insuranceItem.put("stt", stt++);
//			insuranceItem.put("maVe", null);
//			insuranceItem.put("tenDichVu", "Phí bảo hiểm hành khách");
//			insuranceItem.put("dvt", "Người");
//			insuranceItem.put("soLuong", currencyFormatter.format(totalTicketQuantity));
//			insuranceItem.put("donGia", currencyFormatter.format(INSURANCE_PRICE));
//			insuranceItem.put("thanhTien", currencyFormatter.format(thanhTienBH));
//			insuranceItem.put("thueSuat", "KCT");
//			insuranceItem.put("tienThue", currencyFormatter.format(0));
//			insuranceItem.put("tongCong", currencyFormatter.format(thanhTienBH));
//			finalItems.add(insuranceItem);
//		}
//
//		// Thêm các hàng trống
//		int currentItemCount = finalItems.size();
//		if (currentItemCount == 0) {
//			currentItemCount = 1; // Đảm bảo có ít nhất 1 hàng trống nếu không có dữ liệu
//		}
//		int blankRowsToAdd = Math.max(0, MINIMUM_ROWS_IN_TABLE - currentItemCount);
//		for (int i = 0; i < blankRowsToAdd; i++) {
//			Map<String, Object> blankItem = new HashMap<>();
//			blankItem.put("stt", stt++);
//			finalItems.add(blankItem);
//		}
//		data.put("items", finalItems);
//
//		// --- BƯỚC 3: TÍNH TOÁN CÁC DÒNG TỔNG KẾT ---
//		// (Đây là phần giả lập, code thật sẽ phức tạp hơn)
//		List<Map<String, Object>> summaries = new ArrayList<>();
//		summaries.add(Map.of("description", "Tổng theo từng loại thuế suất:", "thanhTien", "2.116.667", "thueSuat",
//				"8%", "tienThue", "169.333", "tongCong", "2.286.000"));
//		summaries.add(Map.of("description", "", "thanhTien",
//				currencyFormatter.format(totalTicketQuantity * INSURANCE_PRICE), "thueSuat", "KCT", "tienThue", "0",
//				"tongCong", currencyFormatter.format(totalTicketQuantity * INSURANCE_PRICE)));
//		data.put("summariesByTax", summaries);
//
//		data.put("tongTienHang", "2.122.667");
//		data.put("tongTienThue", "169.333");
//		data.put("tongThanhToan", "2.292.000");
//		data.put("tongTienBangChu", "Hai triệu hai trăm chín mươi hai nghìn đồng.");
//
//		// Các trường dữ liệu khác...
//		data.put("ngayLap", "28");
//		data.put("thangLap", "10");
//		data.put("namLap", "2025");
//		// ...
//
//		return data;
//	}

	private Map<String, Object> createDummyData(String maHoaDon) {
		Map<String, Object> data = new HashMap<>();
		NumberFormat currencyFormatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
		currencyFormatter.setMaximumFractionDigits(0);
		// --- CẤU HÌNH ---
		final int DESIRED_ROWS_IF_FEW = 10; // Nếu ít hơn số này, sẽ fill cho đủ
		final double INSURANCE_PRICE = 5000.0;

		// --- DANH SÁCH CHỨA TẤT CẢ CÁC HÀNG CỦA BẢNG ---
		List<Map<String, Object>> allItems = new ArrayList<>();

		// --- BƯỚC 1: DỮ LIỆU VÉ GỐC (Lấy từ DAO) ---
		List<Map<String, Object>> tickets = new ArrayList<>();
		// Bỏ comment dòng dưới để test trường hợp không có vé
		// tickets.clear();
		tickets.add(Map.of("maVe", "143356995", "tenDichVu", "Vé HK: DIA-DTR-SE30", "dvt", "Vé", "soLuong", 1.0,
				"donGia", 1113889.0, "thueSuat", TAX_RATE));
		tickets.add(Map.of("maVe", "143356996", "tenDichVu", "Vé HK: DIA-DTR-SE30", "dvt", "Vé", "soLuong", 1.0,
				"donGia", 1002778.0, "thueSuat", TAX_RATE));

		// --- BƯỚC 2: XỬ LÝ DỮ LIỆU GỐC VÀ THÊM VÀO `allItems` ---
		int stt = 1;
		double totalTicketQuantity = 0;

		// Xử lý vé
		for (Map<String, Object> ticket : tickets) {
			Map<String, Object> item = new HashMap<>();
			// ... (phần tính toán `thanhTien`, `tienThue`... giữ nguyên) ...
			double soLuong = (double) ticket.get("soLuong");
			double donGia = (double) ticket.get("donGia");
			double thueSuat = (double) ticket.get("thueSuat");
			double thanhTien = soLuong * donGia;
			double tienThue = thanhTien * thueSuat;
			double tongCong = thanhTien + tienThue;

			item.put("type", "ITEM"); // Đánh dấu loại hàng
			item.put("stt", stt++);
			item.put("maVe", ticket.get("maVe"));
			item.put("tenDichVu", ticket.get("tenDichVu"));
			item.put("dvt", ticket.get("dvt"));
			item.put("soLuong", currencyFormatter.format(soLuong));
			item.put("donGia", currencyFormatter.format(donGia));
			item.put("thanhTien", currencyFormatter.format(thanhTien));
			item.put("thueSuat", (thueSuat > 0) ? "8%" : "KCT");
			item.put("tienThue", currencyFormatter.format(tienThue));
			item.put("tongCong", currencyFormatter.format(tongCong));
			allItems.add(item);
			totalTicketQuantity += soLuong;
		}

		// Xử lý phí bảo hiểm
		if (totalTicketQuantity > 0) {
			Map<String, Object> insuranceItem = new HashMap<>();
			double thanhTienBH = totalTicketQuantity * INSURANCE_PRICE;
			insuranceItem.put("type", "ITEM");
			insuranceItem.put("stt", stt++);
			insuranceItem.put("tenDichVu", "Phí bảo hiểm hành khách");
			insuranceItem.put("dvt", "Người");
			insuranceItem.put("soLuong", currencyFormatter.format(totalTicketQuantity));
			insuranceItem.put("donGia", currencyFormatter.format(INSURANCE_PRICE));
			insuranceItem.put("thanhTien", currencyFormatter.format(thanhTienBH));
			insuranceItem.put("thueSuat", "KCT");
			insuranceItem.put("tienThue", "0");
			insuranceItem.put("tongCong", currencyFormatter.format(thanhTienBH));
			allItems.add(insuranceItem);
		}

		// --- BƯỚC 3: ÁP DỤNG LOGIC FILLER MỚI ---
		int contentRowCount = allItems.size();
		int blankRowsToAdd = 0;
		if (contentRowCount < DESIRED_ROWS_IF_FEW) {
			blankRowsToAdd = DESIRED_ROWS_IF_FEW - contentRowCount;
		}

		for (int i = 0; i < blankRowsToAdd; i++) {
			Map<String, Object> blankItem = new HashMap<>();
			blankItem.put("type", "ITEM");
			blankItem.put("stt", ""); // Hàng trống không có STT
			allItems.add(blankItem);
		}

		// --- BƯỚC 4: THÊM CÁC HÀNG TỔNG KẾT VÀO `allItems` ---
		// (Giả lập, logic thật sẽ tính toán dựa trên dữ liệu thật)
		Map<String, Object> summaryTax = new HashMap<>();
		summaryTax.put("type", "SUMMARY_BY_TAX");
		summaryTax.put("description", "Tổng theo từng loại thuế suất:");
		summaryTax.put("thanhTien", "2.116.667");
		summaryTax.put("thueSuat", "8%");
		summaryTax.put("tienThue", "169.333");
		summaryTax.put("tongCong", "2.286.000");
		allItems.add(summaryTax);

		Map<String, Object> summaryKCT = new HashMap<>();
		summaryKCT.put("type", "SUMMARY_BY_TAX");
		summaryKCT.put("description", "");
		summaryKCT.put("thanhTien", currencyFormatter.format(totalTicketQuantity * INSURANCE_PRICE));
		summaryKCT.put("thueSuat", "KCT");
		summaryKCT.put("tienThue", "0");
		summaryKCT.put("tongCong", currencyFormatter.format(totalTicketQuantity * INSURANCE_PRICE));
		allItems.add(summaryKCT);

		Map<String, Object> finalTotal = new HashMap<>();
		finalTotal.put("type", "FINAL_TOTAL");
		finalTotal.put("description", "Tổng cộng:");
		finalTotal.put("thanhTien", "2.122.667");
		finalTotal.put("tienThue", "169.333");
		finalTotal.put("tongCong", "2.292.000");
		allItems.add(finalTotal);

		// --- BƯỚC 5: ĐƯA DANH SÁCH TỔNG HỢP VÀO DATA ---
		data.put("allItems", allItems);

		// Các trường dữ liệu ngoài bảng
		data.put("tongTienBangChu", "Hai triệu hai trăm chín mươi hai nghìn đồng.");
		data.put("ngayLap", "28");
		data.put("thangLap", "10");
		data.put("namLap", "2025");
		// ...

		return data;
	}

	private void showAlert(Alert.AlertType type, String title, String message) {
		Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}
}