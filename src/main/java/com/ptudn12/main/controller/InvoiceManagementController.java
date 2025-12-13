package com.ptudn12.main.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URL;
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
import com.ptudn12.main.utils.NumberUtils;

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

		// Map<String, Object> data = createDummyData(invoiceData.getMaHoaDon());

		Map<String, Object> data = getRealInvoiceData(invoiceData);

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

	private Map<String, Object> getRealInvoiceData(HoaDon hd) {
		Map<String, Object> data = new HashMap<>();
		NumberFormat currencyFormatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
		currencyFormatter.setMaximumFractionDigits(0);

		final double TAX_RATE = 0.08; // Thuế suất 8% theo yêu cầu

		// 1. HEADER (Ngày, Ký Hiệu, Số)
		LocalDateTime ngayLap = hd.getNgayLap();
		String yearFull = String.valueOf(ngayLap.getYear());
		String yearShort = yearFull.substring(2); // 25

		data.put("ngayLap", String.format("%02d", ngayLap.getDayOfMonth()));
		data.put("thangLap", String.format("%02d", ngayLap.getMonthValue()));
		data.put("namLap", yearFull);

		// Ký hiệu: 1K + Năm + T + KH (Ví dụ: 1K25TKH)
		data.put("kyHieu", "1K" + yearShort + "TKH");

		// Số hóa đơn: Lấy số từ mã (VD: HD001 -> 00000001)
		String rawId = hd.getMaHoaDon();
		// Dùng regex lấy tất cả số trong chuỗi
		String numberOnly = rawId.replaceAll("[^0-9]", "");
		if (numberOnly.isEmpty())
			numberOnly = "0"; // Fallback nếu ko có số

		// Parse sang long rồi format lại thành 8 chữ số
		long soHoaDonVal = Long.parseLong(numberOnly);
		data.put("soHD", String.format("%08d", soHoaDonVal));

		// Mã tra cứu (giữ nguyên ID gốc)
		data.put("idHD", rawId);

		// 2. THÔNG TIN KHÁCH HÀNG
		if (hd.getKhachHang() != null) {
			data.put("tenNguoiMua", hd.getKhachHang().getTenKhachHang());
			String sdt = hd.getKhachHang().getSoDienThoai();
			data.put("sdtNguoiMua", (sdt != null) ? sdt : "");
		} else {
			data.put("tenNguoiMua", "");
			data.put("sdtNguoiMua", "");
		}
		// Địa chỉ và thông tin doanh nghiệp để trống theo yêu cầu
		data.put("diaChiDonVi", "");
		data.put("tenDonVi", "");
		data.put("mstDonVi", "");
		data.put("hinhThucTT", "TM/CK");
		data.put("stkDonVi", "");

		// 3. XỬ LÝ CHI TIẾT VÉ & TÍNH TOÁN TIỀN
		List<Map<String, Object>> rawItems = hoaDonDAO.getChiTietHoaDonById(hd.getMaHoaDon());
		List<Map<String, Object>> allItems = new ArrayList<>();

		int stt = 1;
		double totalTicketQty = 0;

		// Các biến tổng
		double sum8_ThanhTien = 0; // Tiền vé trước thuế
		double sum8_TienThue = 0; // Tiền thuế
		double sum8_TongCong = 0; // Tiền vé sau thuế (đã trừ BH)

		double totalBaoHiem = 0; // Tổng tiền bảo hiểm

		for (Map<String, Object> itemDB : rawItems) {
			// SỬA LỖI: Dùng phương thức getOrDefault hoặc kiểm tra null
			// DAO trả về "thanhTienGoc", không phải "thanhTien"

			Object soLuongObj = itemDB.get("soLuong");
			double soLuong = (soLuongObj != null) ? ((Number) soLuongObj).doubleValue() : 1.0;

			Object tongTienGocObj = itemDB.get("thanhTienGoc"); // SỬA KEY Ở ĐÂY
			double tongTienGoc = (tongTienGocObj != null) ? ((Number) tongTienGocObj).doubleValue() : 0.0;

			Object phiBaoHiemObj = itemDB.get("baoHiem");
			double phiBaoHiem = (phiBaoHiemObj != null) ? ((Number) phiBaoHiemObj).doubleValue() : 0.0;

			// ... (phần còn lại giữ nguyên logic tính toán) ...

			// Tách Bảo Hiểm ra khỏi giá vé để tính thuế
			double giaChiuThue = tongTienGoc - phiBaoHiem;

			// Tính ngược: Giá chịu thuế = Giá chưa thuế * 1.08
			double thanhTienChuaThue = giaChiuThue / (1 + TAX_RATE);
			double tienThue = giaChiuThue - thanhTienChuaThue;

			// Đơn giá hiển thị = Giá chưa thuế / số lượng
			double donGiaHienThi = (soLuong > 0) ? (thanhTienChuaThue / soLuong) : 0;

			// Cộng dồn
			sum8_ThanhTien += thanhTienChuaThue;
			sum8_TienThue += tienThue;
			sum8_TongCong += giaChiuThue;

			totalBaoHiem += phiBaoHiem;
			totalTicketQty += soLuong;

			// Tạo dòng hiển thị cho VÉ
			Map<String, Object> row = new HashMap<>();
			row.put("type", "ITEM");
			row.put("stt", stt++);
			row.put("maVe", itemDB.get("maVe")); // Có thể null nếu vé không có mã
			row.put("tenDichVu", itemDB.get("tenDichVu"));
			row.put("dvt", itemDB.get("dvt"));
			row.put("soLuong", currencyFormatter.format(soLuong));
			row.put("donGia", currencyFormatter.format(donGiaHienThi));
			row.put("thanhTien", currencyFormatter.format(thanhTienChuaThue));
			row.put("thueSuat", "8%");
			row.put("tienThue", currencyFormatter.format(tienThue));
			row.put("tongCong", currencyFormatter.format(giaChiuThue));

			allItems.add(row);
		}

		// 4. DÒNG PHÍ BẢO HIỂM (KCT)
		// Gom tất cả bảo hiểm lại thành 1 dòng như mẫu
		if (totalBaoHiem > 0) {
			// Tính đơn giá bảo hiểm trung bình để hiển thị (thường là 2000)
			double donGiaBH = (totalTicketQty > 0) ? (totalBaoHiem / totalTicketQty) : 0;

			Map<String, Object> insRow = new HashMap<>();
			insRow.put("type", "ITEM");
			insRow.put("stt", stt++);
			insRow.put("maVe", "");
			insRow.put("tenDichVu", "Phí bảo hiểm hành khách");
			insRow.put("dvt", "Người");
			insRow.put("soLuong", currencyFormatter.format(totalTicketQty));
			insRow.put("donGia", currencyFormatter.format(donGiaBH));
			insRow.put("thanhTien", currencyFormatter.format(totalBaoHiem)); // KCT: Thành tiền = Tổng cộng
			insRow.put("thueSuat", "KCT");
			insRow.put("tienThue", "0");
			insRow.put("tongCong", currencyFormatter.format(totalBaoHiem));

			allItems.add(insRow);
		}

		// 5. TỔNG KẾT (Luôn hiển thị)

		// Tổng nhóm 8%
		Map<String, Object> summary8 = new HashMap<>();
		summary8.put("type", "SUMMARY_BY_TAX");
		summary8.put("description", "Tổng theo từng loại thuế suất:");
		summary8.put("thanhTien", currencyFormatter.format(sum8_ThanhTien));
		summary8.put("thueSuat", "8%");
		summary8.put("tienThue", currencyFormatter.format(sum8_TienThue));
		summary8.put("tongCong", currencyFormatter.format(sum8_TongCong));
		allItems.add(summary8);

		// Tổng nhóm KCT (Bảo hiểm)
		Map<String, Object> summaryKCT = new HashMap<>();
		summaryKCT.put("type", "SUMMARY_BY_TAX");
		summaryKCT.put("description", ""); // Rỗng để gộp ô
		summaryKCT.put("thanhTien", currencyFormatter.format(totalBaoHiem));
		summaryKCT.put("thueSuat", "KCT");
		summaryKCT.put("tienThue", "0");
		summaryKCT.put("tongCong", currencyFormatter.format(totalBaoHiem));
		allItems.add(summaryKCT);

		// Tổng cộng cuối cùng (Tiền vé sau thuế + Tiền bảo hiểm)
		double finalTotal = sum8_TongCong + totalBaoHiem;
		double finalTotal_ChuaThue = sum8_ThanhTien + totalBaoHiem; // Tiền hàng gồm vé gốc + bảo hiểm
		double finalTotal_Thue = sum8_TienThue;

		Map<String, Object> finalRow = new HashMap<>();
		finalRow.put("type", "FINAL_TOTAL");
		finalRow.put("description", "Tổng cộng:");
		finalRow.put("thanhTien", currencyFormatter.format(finalTotal_ChuaThue));
		finalRow.put("tienThue", currencyFormatter.format(finalTotal_Thue));
		finalRow.put("tongCong", currencyFormatter.format(finalTotal));
		allItems.add(finalRow);

		data.put("allItems", allItems);

		long totalLong = Math.round(finalTotal);
		data.put("tongTienBangChu", NumberUtils.docSoThanhChu(totalLong));
		data.put("ghiChu", "");

		URL imgUrl = getClass().getResource("/images/check.png");
		data.put("imgCheckUrl", imgUrl != null ? imgUrl.toExternalForm() : "");

		return data;
	}

	private Map<String, Object> createDummyData(String maHoaDon) {
		Map<String, Object> data = new HashMap<>();
		NumberFormat currencyFormatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
		currencyFormatter.setMaximumFractionDigits(0);

		// Cấu hình hằng số
		final double TAX_RATE = 0.08;
		final double INSURANCE_PRICE = 1000.0;

		// 1. Xử lý Ngày tháng & Ký hiệu hóa đơn
		LocalDate now = LocalDate.now();
		String day = String.format("%02d", now.getDayOfMonth());
		String month = String.format("%02d", now.getMonthValue());
		String yearFull = String.valueOf(now.getYear());
		String yearShort = yearFull.substring(2); // Lấy 2 số cuối (ví dụ 25)

		data.put("ngayLap", day);
		data.put("thangLap", month);
		data.put("namLap", yearFull);

		// Tạo ký hiệu chuẩn: 1C + Năm + T + KH => Ví dụ: 1C25TKH
		// 1: Loại hóa đơn (GTGT)
		// C: Có mã của cơ quan thuế (Giả lập)
		// K: Không có mã của cơ quan thuế
		// yearShort: 25
		// T: Áp dụng cho các tổ chức, doanh nghiệp
		// KH: Mã quản lý nội bộ (Khách Hàng)
		String kyHieuChuan = "1K" + yearShort + "TKH";
		data.put("kyHieu", kyHieuChuan);

		// Số hóa đơn (Giả lập lấy từ DB)
		data.put("soHD", "0000123");

		// 2. Dữ liệu vé (Mock)
		List<Map<String, Object>> rawTickets = new ArrayList<>();
		rawTickets.add(Map.of("maVe", "143356995", "tenDichVu", "Vé HK: DIA-DTR-SE30-26/01/2025-3-25-NML", "dvt", "Vé",
				"soLuong", 1.0, "donGia", 1113889.0));
		rawTickets.add(Map.of("maVe", "143356996", "tenDichVu", "Vé HK: DIA-DTR-SE30-26/01/2025-3-26-NML", "dvt", "Vé",
				"soLuong", 1.0, "donGia", 1002778.0));

		// 3. Tính toán chi tiết
		List<Map<String, Object>> allItems = new ArrayList<>();
		int stt = 1;
		double totalQty = 0;
		double sum8_ThanhTien = 0, sum8_TienThue = 0, sum8_TongCong = 0;

		for (Map<String, Object> ticket : rawTickets) {
			double soLuong = (double) ticket.get("soLuong");
			double donGia = (double) ticket.get("donGia");
			double thanhTien = soLuong * donGia;
			double tienThue = thanhTien * TAX_RATE;
			double tongCong = thanhTien + tienThue;

			sum8_ThanhTien += thanhTien;
			sum8_TienThue += tienThue;
			sum8_TongCong += tongCong;
			totalQty += soLuong;

			Map<String, Object> row = new HashMap<>();
			row.put("type", "ITEM");
			row.put("stt", stt++);
			row.put("maVe", ticket.get("maVe"));
			row.put("tenDichVu", ticket.get("tenDichVu"));
			row.put("dvt", ticket.get("dvt"));
			row.put("soLuong", currencyFormatter.format(soLuong));
			row.put("donGia", currencyFormatter.format(donGia));
			row.put("thanhTien", currencyFormatter.format(thanhTien));
			row.put("thueSuat", "8%");
			row.put("tienThue", currencyFormatter.format(tienThue));
			row.put("tongCong", currencyFormatter.format(tongCong));
			allItems.add(row);
		}

		// 4. Tính bảo hiểm
		double sumKCT_ThanhTien = 0, sumKCT_TongCong = 0;
		if (totalQty > 0) {
			double bh_ThanhTien = totalQty * INSURANCE_PRICE;
			double bh_TongCong = bh_ThanhTien;
			sumKCT_ThanhTien += bh_ThanhTien;
			sumKCT_TongCong += bh_TongCong;

			Map<String, Object> insRow = new HashMap<>();
			insRow.put("type", "ITEM");
			insRow.put("stt", stt++);
			insRow.put("maVe", "");
			insRow.put("tenDichVu", "Phí bảo hiểm hành khách");
			insRow.put("dvt", "Người");
			insRow.put("soLuong", currencyFormatter.format(totalQty));
			insRow.put("donGia", currencyFormatter.format(INSURANCE_PRICE));
			insRow.put("thanhTien", currencyFormatter.format(bh_ThanhTien));
			insRow.put("thueSuat", "KCT");
			insRow.put("tienThue", "");
			insRow.put("tongCong", currencyFormatter.format(bh_TongCong));
			allItems.add(insRow);
		}

		// KHÔNG CÒN LOGIC FILLER (HÀNG TRỐNG) Ở ĐÂY NỮA

		// 5. Các dòng tổng kết
		Map<String, Object> sum8 = new HashMap<>();
		sum8.put("type", "SUMMARY_BY_TAX");
		sum8.put("description", "Tổng theo từng loại thuế suất:");
		sum8.put("thanhTien", currencyFormatter.format(sum8_ThanhTien));
		sum8.put("thueSuat", "8%");
		sum8.put("tienThue", currencyFormatter.format(sum8_TienThue));
		sum8.put("tongCong", currencyFormatter.format(sum8_TongCong));
		allItems.add(sum8);

		Map<String, Object> sumKCT = new HashMap<>();
		sumKCT.put("type", "SUMMARY_BY_TAX");
		sumKCT.put("description", "");
		sumKCT.put("thanhTien", currencyFormatter.format(sumKCT_ThanhTien));
		sumKCT.put("thueSuat", "KCT");
		sumKCT.put("tienThue", "0");
		sumKCT.put("tongCong", currencyFormatter.format(sumKCT_TongCong));
		allItems.add(sumKCT);

		double finalTotal = sum8_TongCong + sumKCT_TongCong;
		Map<String, Object> finalRow = new HashMap<>();
		finalRow.put("type", "FINAL_TOTAL");
		finalRow.put("description", "Tổng cộng:");
		finalRow.put("thanhTien", currencyFormatter.format(sum8_ThanhTien + sumKCT_ThanhTien));
		finalRow.put("tienThue", currencyFormatter.format(sum8_TienThue));
		finalRow.put("tongCong", currencyFormatter.format(finalTotal));
		allItems.add(finalRow);

		data.put("allItems", allItems);
		data.put("tongTienBangChu", NumberUtils.docSoThanhChu((long) finalTotal));

		// Thông tin khách hàng (Mock)
		data.put("tenNguoiMua", "Công ty TNHH Giao nhận và Vận tải Châu Kỳ");
		data.put("sdtNguoiMua", "0912938469");
		data.put("ghiChu", "");

		// Dấu tích

		URL imgUrl = getClass().getResource("/images/check.png");
		if (imgUrl != null) {
			data.put("imgCheckUrl", imgUrl.toExternalForm());
		} else {
			data.put("imgCheckUrl", ""); // Tránh lỗi null nếu quên copy ảnh
		}

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