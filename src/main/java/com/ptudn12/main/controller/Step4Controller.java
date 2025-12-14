/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ptudn12.main.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.SwingUtilities;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import com.ptudn12.main.dao.ChiTietHoaDonDAO;
import com.ptudn12.main.dao.ChiTietLichTrinhDAO;
import com.ptudn12.main.dao.HoaDonDAO;
import com.ptudn12.main.dao.KhachHangDAO;
import com.ptudn12.main.dao.VeTauDAO;
import com.ptudn12.main.entity.VeTau;
import com.ptudn12.main.enums.LoaiVe;
import com.ptudn12.main.utils.NumberUtils;
import com.ptudn12.main.utils.ReportManager;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Step4Controller {
	// --- FXML Left Section ---
	@FXML
	private VBox containerVe;
	@FXML
	private Label lblDetailTongTienVe;
	@FXML
	private Label lblDetailGiamDoiTuong;
	@FXML
	private Label lblDetailGiamDiem;
	@FXML
	private Label lblDetailBaoHiem;
	@FXML
	private Label lblDetailTongThanhToan;
	@FXML
	private Button btnXuatHoaDon;
	@FXML
	private Button btnDoiDiem;
	@FXML
	private Button btnTichDiem;

	// --- FXML Right Section ---
	@FXML
	private Label lblDisplayTongThanhToan;
	@FXML
	private TextField txtTienKhachDua;
	@FXML
	private FlowPane flowPaneSuggestions;
	@FXML
	private Label lblTienThoiLai;

	// Khai báo @FXML cho các Label header vé
	@FXML
	private HBox ticketHeaderRow;
	@FXML
	private Label headerChuyenTau;
	@FXML
	private Label headerToaCho;
	@FXML
	private Label headerHanhKhach;
	@FXML
	private Label headerLoaiVe;
	@FXML
	private Label headerDonGia;

	// --- FXML Footer ---
	@FXML
	private Button btnXacNhanVaIn;
	@FXML
	private Button btnHoanTat;
	@FXML
	private Button btnQuayLai;

	// Helpers
	private BanVeController mainController;
	private DecimalFormat moneyFormatter = new DecimalFormat("#,##0 'VNĐ'");
	private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
	private final double PHI_BAO_HIEM = 2000;

	private List<Map<String, Object>> danhSachHanhKhach;
	private Map<String, String> thongTinNguoiMua;
	private double tongThanhToanValue = 0; // Giá trị này LUÔN LÀ SỐ ĐÃ LÀM TRÒN

	// DAOs
	private final HoaDonDAO hoaDonDAO = new HoaDonDAO();
	private final VeTauDAO veTauDAO = new VeTauDAO();
	private final ChiTietHoaDonDAO chiTietHoaDonDAO = new ChiTietHoaDonDAO();
	private final KhachHangDAO khachHangDAO = new KhachHangDAO();
	private final ChiTietLichTrinhDAO chiTietLichTrinhDAO = new ChiTietLichTrinhDAO();

	public void setMainController(BanVeController mainController) {
		this.mainController = mainController;
	}

	private double giamTuDiem = 0;
	private TemplateEngine templateEngine;
	private final double TAX_RATE = 0.08; // 8%

	public Step4Controller() {
		// Khởi tạo Thymeleaf
		ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
		resolver.setPrefix("/views/");
		resolver.setSuffix(".html");
		resolver.setCharacterEncoding("UTF-8");

		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(resolver);
	}

	@FXML
	public void initialize() {
		// Listener cho ô nhập tiền khách đưa
		txtTienKhachDua.textProperty().addListener((obs, oldVal, newVal) -> calculateChange());

		// Chỉ cho phép nhập số
		txtTienKhachDua.setTextFormatter(
				new TextFormatter<>(change -> change.getControlNewText().matches("\\d*") ? change : null));

		// 6. Reset ô tiền khách đưa và tiền thối
		txtTienKhachDua.clear();
		lblTienThoiLai.setText("0 VNĐ");

		// Hiện nút In và Ẩn nút Hoàn tất
		btnXacNhanVaIn.setDisable(true);
		btnXacNhanVaIn.setVisible(true);
		btnXacNhanVaIn.setManaged(true);

		if (btnHoanTat != null)

		{
			btnHoanTat.setVisible(false);
			btnHoanTat.setManaged(false);
		}

		if (btnQuayLai != null) {
			btnQuayLai.setDisable(false);
			btnQuayLai.setVisible(true);
		}
	}

	// --- HÀM LÀM TRÒN 1000đ ---
	private double roundUpToThousand(double value) {
		if (value % 1000 == 0)
			return value;
		return Math.ceil(value / 1000.0) * 1000;
	}

	public void initData() {
		// 1. Lấy dữ liệu từ Step 3
		danhSachHanhKhach = (List<Map<String, Object>>) mainController.getUserData("danhSachHanhKhachDaNhap");
		thongTinNguoiMua = (Map<String, String>) mainController.getUserData("thongTinNguoiMua");
		String tongThanhToanStr = (String) mainController.getUserData("tongThanhTien");

		// Chuyển đổi và LÀM TRÒN tổng thành toán ngay lập tức
		double rawTotal = 0;
		try {
			if (tongThanhToanStr != null) {
				String numericString = tongThanhToanStr.replaceAll("[^\\d]", "");
				rawTotal = Double.parseDouble(numericString);
			}
		} catch (Exception e) {
			System.err.println("Lỗi chuyển đổi tổng thành tiền: " + e.getMessage());
			rawTotal = 0;
		}

		// QUAN TRỌNG: Làm tròn lên 1.000đ ngay tại đây
		// Ví dụ: 8.293.400 -> 8.294.000
		tongThanhToanValue = roundUpToThousand(rawTotal);

		if (danhSachHanhKhach == null || danhSachHanhKhach.isEmpty() || thongTinNguoiMua == null) {
			showAlert(Alert.AlertType.ERROR, "Lỗi dữ liệu", "Không thể tải thông tin từ bước trước.");
			btnXacNhanVaIn.setDisable(true);
			return;
		}

		// 2. Hiển thị bảng xác nhận vé
		populateTicketTable();

		// 3. Hiển thị chi tiết thanh toán
		displayPaymentDetails();

		// 4. Hiển thị tổng thanh toán (Đã làm tròn) bên phải
		lblDisplayTongThanhToan.setText(moneyFormatter.format(tongThanhToanValue));

		// 5. Tạo các nút gợi ý mệnh giá
		generateSuggestionButtons();

		// 6. Reset ô tiền khách đưa và tiền thối
		txtTienKhachDua.clear();
		lblTienThoiLai.setText("0 VNĐ");
		btnXacNhanVaIn.setDisable(true);
	}

	// Hiển thị bảng xác nhận thông tin vé
	private void populateTicketTable() {
		containerVe.getChildren().clear();
		boolean firstRow = true;

		for (Map<String, Object> hanhKhach : danhSachHanhKhach) {
			VeTamThoi veDi = (VeTamThoi) hanhKhach.get("veDi");
			VeTamThoi veVe = (VeTamThoi) hanhKhach.get("veVe");

			if (veDi != null) {
				Node rowNode = createTicketTableRow(veDi, hanhKhach);
				containerVe.getChildren().add(rowNode);
				if (firstRow) {
					syncTicketTableHeaderWidths((HBox) rowNode);
					firstRow = false;
				}
			}
			if (veVe != null) {
				Node rowNode = createTicketTableRow(veVe, hanhKhach);
				containerVe.getChildren().add(rowNode);
				if (firstRow) {
					syncTicketTableHeaderWidths((HBox) rowNode);
					firstRow = false;
				}
			}
		}
	}

	// Tạo một hàng (Node) cho bảng xác nhận vé
	private Node createTicketTableRow(VeTamThoi ve, Map<String, Object> hanhKhachInfo) {
		HBox row = new HBox(10.0);
		row.setAlignment(Pos.CENTER_LEFT);
		row.setStyle("-fx-padding: 8px 0; -fx-border-color: #eee; -fx-border-width: 0 0 1px 0;");

		// Cột 1: Chuyến tàu
		VBox col1 = new VBox(2);
		col1.setPrefWidth(150.0);
		col1.getChildren().addAll(new Label("Tàu " + ve.getLichTrinh().getTau().getMacTau()),
				new Label(ve.getLichTrinh().getNgayGioKhoiHanh().format(formatter)) {
					{
						setStyle("-fx-font-size: 11px;");
					}
				});

		// Cột 2: Toa - Chỗ
		Label col2 = new Label(
				"Toa " + ve.getChiTietToa().getToa().getMaToa() + " - Ghế " + ve.getChiTietToa().getSoThuTu());
		col2.setPrefWidth(150.0);

		// Cột 3: Hành khách
		VBox col3 = new VBox(2);
		HBox.setHgrow(col3, Priority.ALWAYS);
		col3.getChildren().addAll(new Label((String) hanhKhachInfo.get("hoTen")),
				new Label("ID: " + hanhKhachInfo.get("soGiayTo")) {
					{
						setStyle("-fx-font-size: 11px;");
					}
				});

		// Cột 4: Loại vé
		Label col4 = new Label(((LoaiVe) hanhKhachInfo.get("doiTuong")).getDescription());
		col4.setPrefWidth(150.0);

		// Cột 5: Đơn giá
		double giaGoc = ve.getGiaVe() - PHI_BAO_HIEM;
		double heSoGiam = ((LoaiVe) hanhKhachInfo.get("doiTuong")).getHeSoGiamGia();
		double donGia = (giaGoc * (1 - heSoGiam)) + PHI_BAO_HIEM;

		Label col5 = new Label(moneyFormatter.format(donGia));
		col5.setPrefWidth(120.0);
		col5.setAlignment(Pos.CENTER_RIGHT);
		col5.setMaxWidth(Double.MAX_VALUE);

		row.getChildren().addAll(col1, col2, col3, col4, col5);
		return row;
	}

	private void syncTicketTableHeaderWidths(HBox firstRowNode) {
		if (firstRowNode == null || ticketHeaderRow == null || firstRowNode.getChildren().size() != 5) {
			return;
		}
		applyTicketColumnSizing(headerChuyenTau, firstRowNode.getChildren().get(0));
		applyTicketColumnSizing(headerToaCho, firstRowNode.getChildren().get(1));
		applyTicketColumnSizing(headerHanhKhach, firstRowNode.getChildren().get(2));
		applyTicketColumnSizing(headerLoaiVe, firstRowNode.getChildren().get(3));
		applyTicketColumnSizing(headerDonGia, firstRowNode.getChildren().get(4));
	}

	private void applyTicketColumnSizing(Label headerLabel, Node rowColumnNode) {
		if (headerLabel == null || rowColumnNode == null || !(rowColumnNode instanceof Region))
			return;
		Region rowColumn = (Region) rowColumnNode;
		Priority hGrow = HBox.getHgrow(rowColumn);

		if (hGrow == Priority.ALWAYS) {
			HBox.setHgrow(headerLabel, Priority.ALWAYS);
			headerLabel.setMaxWidth(Double.MAX_VALUE);
			headerLabel.setMinWidth(Region.USE_PREF_SIZE);
			headerLabel.setPrefWidth(Region.USE_COMPUTED_SIZE);
		} else {
			double prefWidth = rowColumn.getPrefWidth();
			if (prefWidth > 0) {
				headerLabel.setPrefWidth(prefWidth);
				headerLabel.setMinWidth(prefWidth);
				headerLabel.setMaxWidth(prefWidth);
			}
			HBox.setHgrow(headerLabel, Priority.NEVER);
		}
	}

	// Hiển thị chi tiết thanh toán (Bảng bên trái)
	private void displayPaymentDetails() {
		double tongTienVeGoc = 0;
		double tongGiamDoiTuong = 0;
		double tongBaoHiem = 0;

		for (Map<String, Object> hanhKhach : danhSachHanhKhach) {
			VeTamThoi veDi = (VeTamThoi) hanhKhach.get("veDi");
			VeTamThoi veVe = (VeTamThoi) hanhKhach.get("veVe");
			LoaiVe loaiVe = (LoaiVe) hanhKhach.get("doiTuong");

			if (veDi != null) {
				double giaVeGoc = veDi.getGiaVe() - PHI_BAO_HIEM;
				tongTienVeGoc += giaVeGoc;
				tongGiamDoiTuong += giaVeGoc * loaiVe.getHeSoGiamGia();
				tongBaoHiem += PHI_BAO_HIEM;
			}
			if (veVe != null) {
				double giaVeGoc = veVe.getGiaVe() - PHI_BAO_HIEM;
				tongTienVeGoc += giaVeGoc;
				tongGiamDoiTuong += giaVeGoc * loaiVe.getHeSoGiamGia();
				tongBaoHiem += PHI_BAO_HIEM;
			}
		}

		// TÍNH TOÁN LẠI TỔNG THANH TOÁN
		double tongTruocKhiGiamDiem = tongTienVeGoc - tongGiamDoiTuong + tongBaoHiem;
		double tongSauKhiGiamDiem = tongTruocKhiGiamDiem - giamTuDiem;

		// Cập nhật lại biến tổng thanh toán cuối cùng (đã làm tròn)
		tongThanhToanValue = roundUpToThousand(tongSauKhiGiamDiem);

		// Hiển thị các giá trị
		lblDetailTongTienVe.setText(moneyFormatter.format(tongTienVeGoc));
		lblDetailGiamDoiTuong.setText("- " + moneyFormatter.format(tongGiamDoiTuong));
		lblDetailGiamDiem.setText("- " + moneyFormatter.format(giamTuDiem)); // HIỂN THỊ SỐ TIỀN ĐÃ ĐỔI
		lblDetailBaoHiem.setText(moneyFormatter.format(tongBaoHiem));
		lblDetailTongThanhToan.setText(moneyFormatter.format(tongThanhToanValue));

		// Cập nhật lại cả ô hiển thị bên phải
		lblDisplayTongThanhToan.setText(moneyFormatter.format(tongThanhToanValue));
		// Cập nhật lại các nút gợi ý tiền
		generateSuggestionButtons();
		// Tính lại tiền thối
		calculateChange();

//		double giamDiem = 0; // Tạm thời
//
//		lblDetailTongTienVe.setText(moneyFormatter.format(tongTienVeGoc));
//		lblDetailGiamDoiTuong.setText("- " + moneyFormatter.format(tongGiamDoiTuong));
//		lblDetailGiamDiem.setText("- " + moneyFormatter.format(giamDiem));
//		lblDetailBaoHiem.setText(moneyFormatter.format(tongBaoHiem));
//
//		// QUAN TRỌNG: Hiển thị Tổng cuối cùng phải khớp với số đã làm tròn
//		lblDetailTongThanhToan.setText(moneyFormatter.format(tongThanhToanValue));
	}

	// --- CẬP NHẬT: Tạo nút gợi ý với Style Class mới ---
	private void generateSuggestionButtons() {
		flowPaneSuggestions.getChildren().clear();
		if (tongThanhToanValue <= 0)
			return;

		double[] suggestions = calculateSmartSuggestions(tongThanhToanValue);

		for (double amount : suggestions) {
			Button btn = new Button(moneyFormatter.format(amount));
			// Áp dụng CSS class cho nút đẹp
			btn.getStyleClass().add("money-suggestion-button");

			btn.setOnAction(e -> {
				// Xóa chữ " VNĐ" và dấu chấm để lấy số raw nhập vào ô
				txtTienKhachDua.setText(String.valueOf((long) amount));
				calculateChange();
			});
			flowPaneSuggestions.getChildren().add(btn);
		}
	}

	@FXML
	private void handleXacNhanVaIn() {
		if (btnXacNhanVaIn.isDisabled())
			return;

		try {
			String maNhanVien = "NV001"; // TODO: Lấy từ session

			// 1. Lưu Khách Hàng
			int khachHangId = khachHangDAO.findOrInsertKhachHang(thongTinNguoiMua);
			if (khachHangId == -1) {
				showAlert(Alert.AlertType.ERROR, "Lỗi", "Lỗi xử lý khách hàng.");
				return;
			}

			// 2. Tạo Hóa Đơn
			String maHoaDon = hoaDonDAO.generateUniqueHoaDonId();
			if (!hoaDonDAO.createHoaDon(maHoaDon, khachHangId, maNhanVien, tongThanhToanValue)) {
				showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tạo hóa đơn.");
				return;
			}

			// 3. Tạo Vé & Chi Tiết (Lưu vào DB)
			List<String> createdTicketIds = new ArrayList<>();
			for (Map<String, Object> hanhKhach : danhSachHanhKhach) {
				VeTamThoi veDi = (VeTamThoi) hanhKhach.get("veDi");
				VeTamThoi veVe = (VeTamThoi) hanhKhach.get("veVe");
				LoaiVe loaiVe = (LoaiVe) hanhKhach.get("doiTuong");

				if (veDi != null) {
					String ma = processVe(maHoaDon, khachHangId, veDi, loaiVe);
					if (ma != null)
						createdTicketIds.add(ma);
				}
				if (veVe != null) {
					String ma = processVe(maHoaDon, khachHangId, veVe, loaiVe);
					if (ma != null)
						createdTicketIds.add(ma);
				}
			}

			// 4. HIỆN MODAL PREVIEW (IN VÉ)
			// Chạy trên luồng Swing để tránh xung đột với JavaFX nếu JasperViewer bị lag
			SwingUtilities.invokeLater(() -> {
				for (String maVe : createdTicketIds) {
					VeTau fullVe = veTauDAO.getVeTauDetail(maVe);
					if (fullVe != null) {
						// Hàm này sẽ mở cửa sổ mới
						ReportManager.printVeTau(fullVe);
					}
				}
			});

			// 5. CẬP NHẬT GIAO DIỆN -> CHỜ HOÀN TẤT
			showAlert(Alert.AlertType.INFORMATION, "Thành công", "Thanh toán thành công!\nCửa sổ in vé đang được mở.");

			// Ẩn nút In, Ẩn nút Quay lại
			btnXacNhanVaIn.setVisible(false);
			btnXacNhanVaIn.setManaged(false);

			if (btnQuayLai != null) {
				btnQuayLai.setVisible(false); // Ẩn luôn cho gọn
				btnQuayLai.setManaged(false);
			}

			// Hiện nút Hoàn tất to đùng
			if (btnHoanTat != null) {
				btnHoanTat.setVisible(true);
				btnHoanTat.setManaged(true);
				btnHoanTat.requestFocus();
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống", ex.getMessage());
		}
	}

	// Nút Hoàn tất sẽ reset transaction
	@FXML
	private void handleHoanTat() {
		mainController.startNewTransaction();
	}

	// Sửa hàm này trả về String (Mã vé) thay vì void
	private String processVe(String maHoaDon, int khachHangId, VeTamThoi ve, LoaiVe loaiVe) {
		double giaChoNgoi = ve.getGiaVe() - PHI_BAO_HIEM;

		// 1. Tạo ChiTietLichTrinh (đánh dấu ghế đã bán)
		int chiTietLichTrinhId = chiTietLichTrinhDAO.createChiTietLichTrinh(ve.getLichTrinh().getMaLichTrinh(),
				ve.getChiTietToa().getCho().getMaCho(), giaChoNgoi, "DaBan"); // Truyền đúng "DaBan" khớp với DB

		if (chiTietLichTrinhId != -1) {
			// 2. Tạo mã vé
			String maVe = veTauDAO.generateUniqueVeId();
			if (maVe != null) {
				// 3. Tạo Vé Tàu
				boolean isKhuHoi = !ve.isChieuDi();
				boolean success = veTauDAO.createVeTau(maVe, khachHangId, chiTietLichTrinhId, loaiVe.getDescription(),
						isKhuHoi, "DaBan");

				if (success) {
					// 4. Tạo Chi Tiết Hóa Đơn
					double giaGoc = giaChoNgoi;
					double giamGia = giaGoc * loaiVe.getHeSoGiamGia();
					double thanhTien = ve.getGiaVe() - giamGia; // Giá vé + BH - Giảm giá
					chiTietHoaDonDAO.createChiTietHoaDon(maHoaDon, maVe, giamGia, thanhTien);
					return maVe;
				}
			}
		}
		return null; // Thất bại
	}

	@FXML
	private void handleQuayLai() {
		mainController.loadContent("step-3.fxml");
	}

	// --- CẬP NHẬT: Thuật toán gợi ý tiền thông minh cho VNĐ (Dựa trên số đã làm
	// tròn) ---
	private double[] calculateSmartSuggestions(double total) {
		// Lúc này 'total' (tức tongThanhToanValue) đã là số chẵn nghìn (VD: 8.294.000)
		long totalLong = (long) total;

		Set<Long> suggestions = new TreeSet<>(); // Dùng TreeSet để tự sắp xếp và loại trùng

		// Gợi ý 1: Đưa đúng số tiền (Chính xác)
		suggestions.add(totalLong);

		// Gợi ý 2: Làm tròn lên các mốc chẵn chục nghìn
		suggestions.add(roundUpTo(totalLong, 10000));

		// Gợi ý 3: Làm tròn lên các mốc chẵn 50k, 100k, 500k, 1M
		suggestions.add(roundUpTo(totalLong, 50000));
		suggestions.add(roundUpTo(totalLong, 100000));
		suggestions.add(roundUpTo(totalLong, 500000));
		suggestions.add(roundUpTo(totalLong, 1000000));

		// Lọc lấy các giá trị >= tổng tiền và giới hạn 6 nút
		return suggestions.stream().filter(val -> val >= totalLong).limit(6).mapToDouble(Long::doubleValue).toArray();
	}

	// Hàm tiện ích làm tròn lên theo bội số
	private long roundUpTo(long value, long multiple) {
		if (multiple == 0)
			return value;
		long remainder = value % multiple;
		if (remainder == 0)
			return value;
		return value + multiple - remainder;
	}

	// Tính tiền thối lại
	private void calculateChange() {
		try {
			String tienKhachDuaStr = txtTienKhachDua.getText();
			if (tienKhachDuaStr == null || tienKhachDuaStr.isEmpty()) {
				lblTienThoiLai.setText("0 VNĐ");
				btnXacNhanVaIn.setDisable(true);
				return;
			}
			double tienKhachDua = Double.parseDouble(tienKhachDuaStr);
			double tienThoi = tienKhachDua - tongThanhToanValue; // Trừ đi số tiền đã làm tròn

			if (tienThoi >= 0) {
				lblTienThoiLai.setText(moneyFormatter.format(tienThoi));
				btnXacNhanVaIn.setDisable(false);
			} else {
				lblTienThoiLai.setText("Chưa đủ");
				btnXacNhanVaIn.setDisable(true);
			}
		} catch (NumberFormatException e) {
			lblTienThoiLai.setText("Lỗi nhập liệu");
			btnXacNhanVaIn.setDisable(true);
		}
	}

	@FXML
	private void handleXuatHoaDon() {
		if (danhSachHanhKhach == null || danhSachHanhKhach.isEmpty()) {
			showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Chưa có thông tin vé để xuất hóa đơn.");
			return;
		}

		File tempPdfFile = null;
		try {
			// 1. Tạo file PDF tạm thời từ dữ liệu hiện tại (không lưu DB)
			tempPdfFile = generateTempPdf();
			if (tempPdfFile == null || !tempPdfFile.exists()) {
				throw new IOException("Không thể tạo file PDF tạm thời.");
			}

			// 2. Mở trình xem PDF (PdfViewerController)
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/pdf-viewer.fxml"));
			Scene scene = new Scene(loader.load());

			Stage viewerStage = new Stage();
			viewerStage.setTitle("Xem trước Hóa đơn (Tạm tính)");
			viewerStage.initModality(Modality.APPLICATION_MODAL);

			// Set kích thước cửa sổ viewer
			viewerStage.setWidth(450);
			viewerStage.setHeight(900);
			viewerStage.setScene(scene);

			PdfViewerController viewerController = loader.getController();
			viewerController.setStage(viewerStage);
			viewerController.loadDocument(tempPdfFile);

			// Xóa file tạm khi đóng cửa sổ xem
			File finalFile = tempPdfFile;
			viewerStage.setOnCloseRequest(e -> {
				viewerController.closeDocument();
				finalFile.delete();
			});

			viewerStage.showAndWait();

		} catch (Exception e) {
			e.printStackTrace();
			showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tạo bản xem trước hóa đơn: " + e.getMessage());
		}
	}

	@FXML
	private void handleDoiDiem() {
		if (thongTinNguoiMua == null || thongTinNguoiMua.get("soGiayToIdentifier") == null) {
			showAlert(Alert.AlertType.WARNING, "Lỗi", "Không có thông tin người mua để đổi điểm.");
			return;
		}

		String identifier = thongTinNguoiMua.get("soGiayToIdentifier");
		int khachHangId = khachHangDAO.findKhachHangByIdentifier(identifier);

		if (khachHangId == -1) {
			showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Khách hàng chưa tồn tại, không có điểm để đổi.");
			return;
		}

		int diemHienTai = khachHangDAO.getDiemTich(khachHangId);
		if (diemHienTai <= 0) {
			showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Khách hàng không có điểm để đổi.");
			return;
		}

		// Tạo Dialog để nhập điểm cần đổi
		Dialog<Integer> dialog = new Dialog<>();
		dialog.setTitle("Đổi điểm tích lũy");
		dialog.setHeaderText("Đổi điểm để nhận giảm giá (1000 điểm = 1000 VNĐ)");

		ButtonType doiDiemButtonType = new ButtonType("Đổi điểm", ButtonBar.ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(doiDiemButtonType, ButtonType.CANCEL);

		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));

		TextField diemDoiField = new TextField();
		diemDoiField.setPromptText("Nhập số điểm");
		diemDoiField.setTextFormatter(
				new TextFormatter<>(change -> change.getControlNewText().matches("\\d*") ? change : null));

		grid.add(new Label("Điểm hiện tại:"), 0, 0);
		grid.add(new Label(String.valueOf(diemHienTai)), 1, 0);
		grid.add(new Label("Điểm muốn đổi:"), 0, 1);
		grid.add(diemDoiField, 1, 1);

		dialog.getDialogPane().setContent(grid);

		// Kích hoạt nút "Đổi điểm" chỉ khi có nhập liệu
		Node doiDiemButton = dialog.getDialogPane().lookupButton(doiDiemButtonType);
		doiDiemButton.setDisable(true);
		diemDoiField.textProperty().addListener((observable, oldValue, newValue) -> {
			doiDiemButton.setDisable(newValue.trim().isEmpty());
		});

		// Xử lý kết quả trả về từ dialog
		dialog.setResultConverter(dialogButton -> {
			if (dialogButton == doiDiemButtonType) {
				try {
					int diemMuonDoi = Integer.parseInt(diemDoiField.getText());
					// Auto correct nếu nhập quá số điểm hiện có
					return Math.min(diemMuonDoi, diemHienTai);
				} catch (NumberFormatException e) {
					return 0;
				}
			}
			return null;
		});

		dialog.showAndWait().ifPresent(diemDaDoi -> {
			if (diemDaDoi > 0) {
				// Cập nhật CSDL
				int diemConLai = diemHienTai - diemDaDoi;
				khachHangDAO.updateDiemTich(khachHangId, diemConLai);

				// Cập nhật giao diện
				giamTuDiem = diemDaDoi; // 1 điểm = 1 VNĐ theo yêu cầu (điểm * 1000 -> điểm)
				displayPaymentDetails(); // Cập nhật lại bảng chi tiết

				// Vô hiệu hóa các nút để tránh xung đột
				btnDoiDiem.setDisable(true);
				btnTichDiem.setDisable(true);
				showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã đổi " + diemDaDoi + " điểm thành công!");
			}
		});
	}

	// THAY THẾ TOÀN BỘ PHƯƠC THỨC handleTichDiem()
	@FXML
	private void handleTichDiem() {
		if (thongTinNguoiMua == null) {
			showAlert(Alert.AlertType.WARNING, "Lỗi", "Không có thông tin người mua để tích điểm.");
			return;
		}

		// Tìm khách hàng trước để biết là khách cũ hay mới
		String identifier = thongTinNguoiMua.get("soGiayToIdentifier");
		int khachHangId = -1;
		boolean isNewCustomer = true;

		if (identifier != null && !identifier.isEmpty()) {
			khachHangId = khachHangDAO.findKhachHangByIdentifier(identifier);
		}

		if (khachHangId != -1) {
			isNewCustomer = false;
		} else {
			// Nếu không tìm thấy, tạo khách hàng mới
			khachHangId = khachHangDAO.insertKhachHang(thongTinNguoiMua);
			if (khachHangId == -1) {
				showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tạo khách hàng mới trong CSDL.");
				return;
			}
		}

		// Bắt đầu tính điểm
		int diemHienTai = khachHangDAO.getDiemTich(khachHangId);
		int diemCong = (int) (tongThanhToanValue / 10000); // Chia lấy nguyên
		int diemMoi = diemHienTai + diemCong;

		// Cập nhật điểm vào CSDL
		boolean success = khachHangDAO.updateDiemTich(khachHangId, diemMoi);

		if (success) {
			String tenKH = thongTinNguoiMua.get("tenKhachHang");
			String soGT = thongTinNguoiMua.get("soGiayToIdentifier");
			String title = isNewCustomer ? "Tạo mới và tích điểm thành công" : "Cập nhật điểm thành công";
			String header = isNewCustomer
					? String.format("Tạo mới thành công khách hàng %s, số giấy tờ %s", tenKH, soGT)
					: String.format("Cập nhật thành công khách hàng %s", tenKH);

			String content = String.format("Điểm tích hiện tại là: %d + %d = %d", diemHienTai, diemCong, diemMoi);

			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setTitle(title);
			alert.setHeaderText(header);
			alert.setContentText(content);
			alert.showAndWait();

			// Vô hiệu hóa các nút để tránh xung đột
			btnDoiDiem.setDisable(true);
			btnTichDiem.setDisable(true);
		} else {
			showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể cập nhật điểm vào CSDL.");
		}
	}

	private File generateTempPdf() throws Exception {
		// 1. Chuẩn bị dữ liệu
		Map<String, Object> data = getTemporaryInvoiceData();
		Context context = new Context();
		context.setVariables(data);

		// 2. Render HTML
		String processedHtml = templateEngine.process("invoice-template", context);

		// 3. Render PDF
		File tempPdfFile = File.createTempFile("invoice-temp-", ".pdf");
		try (OutputStream os = new FileOutputStream(tempPdfFile)) {
			com.openhtmltopdf.pdfboxout.PdfRendererBuilder builder = new com.openhtmltopdf.pdfboxout.PdfRendererBuilder();

			builder.useFont(() -> {
				try {
					InputStream is = getClass().getResourceAsStream("/fonts/times.ttf");
					if (is == null)
						throw new IOException("Font not found");
					return is;
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			}, "Times New Roman");

			String baseUri = getClass().getResource("/views/").toExternalForm();
			builder.withHtmlContent(processedHtml, baseUri);
			builder.toStream(os);
			builder.run();
		}
		return tempPdfFile;
	}

	private Map<String, Object> getTemporaryInvoiceData() {
		Map<String, Object> data = new HashMap<>();
		// Formatter số nguyên (không thập phân)
		java.text.NumberFormat numberFormat = java.text.NumberFormat.getNumberInstance(new Locale("vi", "VN"));
		numberFormat.setMaximumFractionDigits(0);

		// A. HEADER (Dùng ngày hiện tại, ID giả)
		LocalDate now = LocalDate.now();
		String yearFull = String.valueOf(now.getYear());
		data.put("ngayLap", String.format("%02d", now.getDayOfMonth()));
		data.put("thangLap", String.format("%02d", now.getMonthValue()));
		data.put("namLap", yearFull);
		data.put("kyHieu", "1K" + yearFull.substring(2) + "TKH"); // VD: 1K25TKH
		data.put("soHD", "DRAFT"); // Số hóa đơn tạm
		data.put("idHD", "HÓA ĐƠN TẠM TÍNH");

		// B. KHÁCH HÀNG (Lấy từ thongTinNguoiMua)
		data.put("tenNguoiMua", thongTinNguoiMua.getOrDefault("tenKhachHang", ""));
		data.put("sdtNguoiMua", thongTinNguoiMua.getOrDefault("soDienThoai", ""));
		data.put("diaChiDonVi", "");
		data.put("tenDonVi", "");
		data.put("mstDonVi", "");
		data.put("hinhThucTT", "TM/CK");
		data.put("stkDonVi", "");

		// C. CHI TIẾT (Lấy từ danhSachHanhKhach)
		List<Map<String, Object>> allItems = new ArrayList<>();
		int stt = 1;
		double totalQty = 0;

		double sum8_ThanhTien = 0;
		double sum8_TienThue = 0;
		double sum8_TongCong = 0;
		double totalBaoHiem = 0;

		for (Map<String, Object> hanhKhach : danhSachHanhKhach) {
			// Xử lý vé đi và vé về (nếu có)
			VeTamThoi[] ves = { (VeTamThoi) hanhKhach.get("veDi"), (VeTamThoi) hanhKhach.get("veVe") };
			LoaiVe doiTuong = (LoaiVe) hanhKhach.get("doiTuong");

			for (VeTamThoi ve : ves) {
				if (ve == null)
					continue;

				// 1. Tính toán giá tiền
				// Giá vé gốc (Gồm BH + Thuế)
				double giaVeGoc = ve.getGiaVe();

				// Trừ bảo hiểm để ra giá chịu thuế
				double giaChiuThueGoc = giaVeGoc - PHI_BAO_HIEM;

				// Áp dụng giảm giá đối tượng (Giảm trên giá chịu thuế)
				double giaSauGiam = giaChiuThueGoc * (1 - doiTuong.getHeSoGiamGia());

				// Bóc tách thuế từ giá sau giảm
				// Giá chưa thuế = Giá sau giảm / 1.08
				double thanhTienChuaThue = giaSauGiam / (1 + TAX_RATE);
				double tienThue = giaSauGiam - thanhTienChuaThue;
				double tongCongCoThue = giaSauGiam; // Đây là tiền vé thực tế khách trả (chưa tính BH)

				// 2. Cộng dồn
				sum8_ThanhTien += thanhTienChuaThue;
				sum8_TienThue += tienThue;
				sum8_TongCong += tongCongCoThue;

				totalBaoHiem += PHI_BAO_HIEM;
				totalQty += 1;

				// 3. Tạo dòng hiển thị
				Map<String, Object> row = new HashMap<>();
				row.put("type", "ITEM");
				row.put("stt", stt++);
				row.put("maVe", "TẠM TÍNH"); // Chưa có mã vé thật

				// Tên dịch vụ: Vé HK: GaDi-GaDen-Tau-Ngay-Toa-Cho-LoaiToa
				String tenDichVu = String.format("Vé HK: %s-%s-%s-%s-Toa %s-Ghế %s",
						ve.getLichTrinh().getTuyenDuong().getDiemDi().getViTriGa(),
						ve.getLichTrinh().getTuyenDuong().getDiemDen().getViTriGa(),
						ve.getLichTrinh().getTau().getMacTau(),
						ve.getLichTrinh().getNgayGioKhoiHanh().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
						ve.getChiTietToa().getToa().getTenToa(), ve.getChiTietToa().getSoThuTu());

				row.put("tenDichVu", tenDichVu);
				row.put("dvt", "Vé");
				row.put("soLuong", "1");
				row.put("donGia", numberFormat.format(thanhTienChuaThue));
				row.put("thanhTien", numberFormat.format(thanhTienChuaThue));
				row.put("thueSuat", "8%");
				row.put("tienThue", numberFormat.format(tienThue));
				row.put("tongCong", numberFormat.format(tongCongCoThue));

				allItems.add(row);
			}
		}

		// D. PHÍ BẢO HIỂM
		double sumKCT_ThanhTien = 0;
		double sumKCT_TongCong = 0;

		if (totalQty > 0) {
			sumKCT_ThanhTien = totalBaoHiem;
			sumKCT_TongCong = totalBaoHiem;

			Map<String, Object> insRow = new HashMap<>();
			insRow.put("type", "ITEM");
			insRow.put("stt", stt++);
			insRow.put("maVe", "");
			insRow.put("tenDichVu", "Phí bảo hiểm hành khách");
			insRow.put("dvt", "Người");
			insRow.put("soLuong", numberFormat.format(totalQty));
			insRow.put("donGia", numberFormat.format(PHI_BAO_HIEM));
			insRow.put("thanhTien", numberFormat.format(totalBaoHiem));
			insRow.put("thueSuat", "KCT");
			insRow.put("tienThue", "0");
			insRow.put("tongCong", numberFormat.format(totalBaoHiem));
			allItems.add(insRow);
		}

		// E. TỔNG KẾT
		// 1. Tổng nhóm 8%
		Map<String, Object> summary8 = new HashMap<>();
		summary8.put("type", "SUMMARY_BY_TAX");
		summary8.put("description", "Tổng theo từng loại thuế suất:");
		summary8.put("thanhTien", numberFormat.format(sum8_ThanhTien));
		summary8.put("thueSuat", "8%");
		summary8.put("tienThue", numberFormat.format(sum8_TienThue));
		summary8.put("tongCong", numberFormat.format(sum8_TongCong));
		allItems.add(summary8);

		// 2. Tổng nhóm KCT
		Map<String, Object> summaryKCT = new HashMap<>();
		summaryKCT.put("type", "SUMMARY_BY_TAX");
		summaryKCT.put("description", "");
		summaryKCT.put("thanhTien", numberFormat.format(sumKCT_ThanhTien));
		summaryKCT.put("thueSuat", "KCT");
		summaryKCT.put("tienThue", "0");
		summaryKCT.put("tongCong", numberFormat.format(sumKCT_TongCong));
		allItems.add(summaryKCT);

		// 3. Tổng cộng cuối cùng (Cần trừ đi giảm giá từ điểm nếu có)
		// Lưu ý: giamTuDiem được trừ vào tổng tiền thanh toán, coi như là 1 khoản chiết
		// khấu
		// Ở đây để đơn giản cho hóa đơn VAT, ta hiển thị tổng tiền hàng trước khi trừ
		// điểm
		// Hoặc nếu muốn chính xác thì phải phân bổ giảm giá vào từng vé (phức tạp)

		double finalTotal = sum8_TongCong + sumKCT_TongCong;
		// Logic hiển thị: Tổng tiền vé + BH (chưa trừ điểm đổi).
		// Nếu muốn hiển thị số tiền khách phải trả thực (đã trừ điểm), trừ giamTuDiem ở
		// đây.
		// Tuy nhiên hóa đơn GTGT thường ghi giá trị thực của hàng hóa.
		// Để khớp với UI "Tổng thanh toán", ta có thể dùng biến tongThanhToanValue (đã
		// làm tròn và trừ điểm)

		// Cập nhật lại finalTotal để khớp với UI đã tính
		finalTotal = tongThanhToanValue;

		// Tính lại base để khớp số học (chỉ để hiển thị cho hợp lý)
		double finalTotal_Thue = sum8_TienThue;
		double finalTotal_ChuaThue = finalTotal - finalTotal_Thue;

		Map<String, Object> finalRow = new HashMap<>();
		finalRow.put("type", "FINAL_TOTAL");
		finalRow.put("description", "Tổng cộng thanh toán:"); // Đổi label chút cho rõ
		finalRow.put("thanhTien", numberFormat.format(finalTotal_ChuaThue));
		finalRow.put("tienThue", numberFormat.format(finalTotal_Thue));
		finalRow.put("tongCong", numberFormat.format(finalTotal));
		allItems.add(finalRow);

		data.put("allItems", allItems);
		data.put("tongTienBangChu", NumberUtils.docSoThanhChu((long) finalTotal));
		data.put("ghiChu", "Hóa đơn tạm tính, chưa có giá trị pháp lý.");

		// Ảnh check
		URL imgUrl = getClass().getResource("/images/check.png");
		data.put("imgCheckUrl", imgUrl != null ? imgUrl.toExternalForm() : "");

		return data;
	}

	private void clearAllUserData() {
		mainController.setUserData("lichTrinhChieuDi", null);
		mainController.setUserData("lichTrinhChieuVe", null);
		mainController.setUserData("gioHang_Di", null);
		mainController.setUserData("gioHang_Ve", null);
		mainController.setUserData("danhSachHanhKhachDaNhap", null);
		mainController.setUserData("thongTinNguoiMua", null);
		mainController.setUserData("tongThanhTien", null);
		mainController.setUserData("step1_gaDi", null);
		mainController.setUserData("step1_gaDen", null);
		mainController.setUserData("step1_ngayDi", null);
		mainController.setUserData("step1_isKhuHoi", null);
		mainController.setUserData("step1_ngayVe", null);
	}

	private void showAlert(Alert.AlertType type, String title, String message) {
		Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		Platform.runLater(alert::showAndWait);
	}
}
