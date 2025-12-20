package com.ptudn12.main.controller;

import com.ptudn12.main.dao.ChiTietLichTrinhDAO;
import com.ptudn12.main.dao.KhachHangDAO;
import com.ptudn12.main.dao.VeTauDAO;
import com.ptudn12.main.entity.KhachHang;
import com.ptudn12.main.entity.Toa;
import com.ptudn12.main.entity.VeTau;
import com.ptudn12.main.utils.TraVeService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.view.JasperViewer;
import java.util.HashMap;
import java.util.Map;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar.ButtonData;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import net.sf.jasperreports.engine.JasperPrintManager;

public class TraVeController {
    // --- FXML Elements ---
    @FXML private TextField txtSearchCCCD;
    @FXML private Button btnTimKiem;
    @FXML private Label lblTieuDe;
    
    @FXML private TableView<VeTau> tblDanhSachVe;
    @FXML private TableColumn<VeTau, String> colMaVe;
    @FXML private TableColumn<VeTau, String> colTau;
    @FXML private TableColumn<VeTau, String> colHanhTrinh;
    @FXML private TableColumn<VeTau, String> colNgayDi;
    @FXML private TableColumn<VeTau, String> colGhe;
    @FXML private TableColumn<VeTau, String> colGiaVe;
    @FXML private TableColumn<VeTau, String> colTrangThai;

    // --- Pane Chi Tiết ---
    @FXML private VBox paneChiTietTra;
    
    // Thông tin cơ bản vé (Dùng chung cho cả 2 mode)
    @FXML private Label lblMaVeChon;
    @FXML private Label lblTau;
    @FXML private Label lblHanhTrinh;
    @FXML private Label lblNgayDi;
    @FXML private Label lblGhe;
    @FXML private Label lblThoiGianConLai;
    @FXML private Label lblThongBaoLoi;
    
    // Phần tính tiền (Chỉ dùng cho TRẢ VÉ)
    @FXML private VBox boxTinhTien; 
    @FXML private Label lblDieuKienVe;
    @FXML private Label lblGiaVeGoc;
    @FXML private Label lblPhiTraVe;
    @FXML private Label lblTienHoanLai;
    @FXML private Button btnXacNhanTra;
    
    // Nút đổi vé (Chỉ dùng cho ĐỔI VÉ)
    @FXML private Button btnDoiVe;
    
    // --- Helpers & DAOs ---
    private final VeTauDAO veTauDAO = new VeTauDAO();
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();
    private final ChiTietLichTrinhDAO chiTietLichTrinhDAO = new ChiTietLichTrinhDAO();
    private final DecimalFormat moneyFormatter = new DecimalFormat("#,##0 'VNĐ'");
    private VeTau selectedVe = null;
    private double calculatedRefundAmount = 0;
    private final TraVeService traVeService = new TraVeService();

    
    private BanVeController mainController;
    public void setMainController(BanVeController mainController) {
        this.mainController = mainController;
        setupMode(); 
    }
    
    private void setupMode() {
        if (mainController == null) return;
        
        String mode = (String) mainController.getUserData("transactionType");
        
        // Mặc định luôn hiện Pane Chi tiết (nhưng disable) để UI đỡ trống
        if (paneChiTietTra != null) {
            paneChiTietTra.setVisible(true);
            paneChiTietTra.setManaged(true);
            paneChiTietTra.setDisable(true); 
        }

        if (BanVeController.MODE_DOI_VE.equals(mode)) {
            // --- CHẾ ĐỘ ĐỔI VÉ ---
            if (lblTieuDe != null) lblTieuDe.setText("TRA CỨU VÉ ĐỂ ĐỔI");
            
            // Ẩn Box tính tiền
            if (boxTinhTien != null) {
                boxTinhTien.setVisible(false);
                boxTinhTien.setManaged(false);
            }
            // Hiện nút Đổi vé (trong Pane)
            if (btnDoiVe != null) {
                btnDoiVe.setVisible(true);
                btnDoiVe.setManaged(true);
                btnDoiVe.setDisable(true);
            }
            
        } else if (BanVeController.MODE_TRA_VE.equals(mode)) {
            // --- CHẾ ĐỘ TRẢ VÉ ---
            if (lblTieuDe != null) lblTieuDe.setText("TRA CỨU VÉ ĐỂ TRẢ");
            
            // Hiện Box tính tiền
            if (boxTinhTien != null) {
                boxTinhTien.setVisible(true);
                boxTinhTien.setManaged(true);
            }
            // Ẩn nút Đổi vé
            if (btnDoiVe != null) {
                btnDoiVe.setVisible(false);
                btnDoiVe.setManaged(false);
            }
        }
    }
    
    @FXML
    public void initialize() {
        // Cấu hình TableView (Giữ nguyên code cũ của bạn)
        colMaVe.setCellValueFactory(new PropertyValueFactory<>("maVe"));
        colTrangThai.setCellValueFactory(new PropertyValueFactory<>("trangThai"));
        
        colTau.setCellValueFactory(cell -> {
            if (checkNested(cell.getValue())) 
                return new SimpleStringProperty(cell.getValue().getChiTietLichTrinh().getLichTrinh().getTau().getMacTau());
            return new SimpleStringProperty("-");
        });

        colHanhTrinh.setCellValueFactory(cell -> {
            if (checkNested(cell.getValue())) {
                String di = cell.getValue().getChiTietLichTrinh().getLichTrinh().getTuyenDuong().getDiemDi().getViTriGa();
                String den = cell.getValue().getChiTietLichTrinh().getLichTrinh().getTuyenDuong().getDiemDen().getViTriGa();
                return new SimpleStringProperty(di + " -> " + den);
            }
            return new SimpleStringProperty("-");
        });
        
        colNgayDi.setCellValueFactory(cell -> {
            if (checkNested(cell.getValue())) {
                return new SimpleStringProperty(cell.getValue().getChiTietLichTrinh().getLichTrinh().getNgayGioKhoiHanhFormatted());
            }
            return new SimpleStringProperty("-");
        });
        
        colGhe.setCellValueFactory(cell -> {
            if (cell.getValue().getChiTietLichTrinh() != null && cell.getValue().getChiTietLichTrinh().getCho() != null) {
                Toa toa = cell.getValue().getChiTietLichTrinh().getCho().getToa();
                String loaiToaDesc = "";
                
                if (toa.getLoaiToa() != null) {
                        loaiToaDesc = toa.getLoaiToa().getDescription();
                    } else {
                        System.err.println("Warning: Loại toa bị null ở vé " + cell.getValue().getMaVe());
                }
                
                return new SimpleStringProperty("Toa " + cell.getValue().getChiTietLichTrinh().getCho().getToa().getLoaiToa().getDescription() + 
                                              " - Ghế " + cell.getValue().getChiTietLichTrinh().getCho().getSoThuTu());
            }
            return new SimpleStringProperty("-");
        });
        
        colGiaVe.setCellValueFactory(cell -> {
            if (cell.getValue().getChiTietLichTrinh() != null) {
                return new SimpleStringProperty(moneyFormatter.format(cell.getValue().getChiTietLichTrinh().getGiaChoNgoi()));
            }
            return new SimpleStringProperty("0 VNĐ");
        });

        // Sự kiện
        tblDanhSachVe.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) handleChonVe(newVal);
            else clearDetailView();
        });

        txtSearchCCCD.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) handleTimKiem();
        });
    }
    
    // Helper check null
    private boolean checkNested(VeTau v) {
        return v.getChiTietLichTrinh() != null && 
               v.getChiTietLichTrinh().getLichTrinh() != null && 
               v.getChiTietLichTrinh().getLichTrinh().getTuyenDuong() != null;
    }
    
    @FXML
    private void handleTimKiem() {
        String identifier = txtSearchCCCD.getText().trim();
        String mode = (String) mainController.getUserData("transactionType");

        if (identifier.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Vui lòng nhập CCCD/Hộ chiếu hoặc Mã vé.");
            return;
        }

        tblDanhSachVe.setItems(FXCollections.emptyObservableList());
        clearDetailView();

        // --- TÌM THEO MÃ VÉ ---
        if (identifier.matches("\\d{12}")) {
            VeTau ve = veTauDAO.getVeTauDetail(identifier);
            if (ve != null) {
                // Logic lọc cũ: Chỉ cần khớp trạng thái là hiện
                boolean isValid = false;
                if (BanVeController.MODE_DOI_VE.equals(mode)) {
                    if ("DaBan".equals(ve.getTrangThai())) isValid = true;
                } else {
                    if ("DaBan".equals(ve.getTrangThai()) || "DaDoi".equals(ve.getTrangThai())) isValid = true;
                }

                if (isValid) {
                    tblDanhSachVe.setItems(FXCollections.observableArrayList(List.of(ve)));
                    return;
                }
            }
        }

        // --- TÌM THEO KHÁCH HÀNG ---
        KhachHang kh = khachHangDAO.timKhachHangTheoGiayTo(identifier);
        if (kh == null) {
            showAlert(Alert.AlertType.INFORMATION, "Không tìm thấy thông tin phù hợp.");
            return;
        }

        try {
            int maKH = Integer.parseInt(kh.getMaKhachHang().replace("KH", ""));
            List<VeTau> listVe = veTauDAO.getLichSuVeCuaKhachHang(maKH);

            if (listVe != null && !listVe.isEmpty()) {
                List<VeTau> filteredList = listVe.stream()
                    .filter(v -> {
                        if (BanVeController.MODE_DOI_VE.equals(mode)) {
                            return "DaBan".equals(v.getTrangThai());
                        } else {
                            return "DaBan".equals(v.getTrangThai()) || "DaDoi".equals(v.getTrangThai());
                        }
                    })
                    .sorted((v1, v2) -> v2.getMaVe().compareTo(v1.getMaVe()))
                    .toList();

                if (filteredList.isEmpty()) {
                    showAlert(Alert.AlertType.INFORMATION, "Khách hàng không có vé phù hợp để thao tác.");
                } else {
                    tblDanhSachVe.setItems(FXCollections.observableArrayList(filteredList));
                }
            } else {
                showAlert(Alert.AlertType.INFORMATION, "Khách hàng chưa mua vé nào.");
            }
        } catch (Exception e) {
             showAlert(Alert.AlertType.ERROR, "Lỗi dữ liệu khách hàng.");
        }
    }

    private void handleChonVe(VeTau ve) {
        this.selectedVe = ve;
        paneChiTietTra.setDisable(false); // Mở khóa pane chi tiết
        lblThongBaoLoi.setText("");

        // --- 1. HIỂN THỊ THÔNG TIN CƠ BẢN (CHUNG CHO CẢ 2 MODE) ---
        lblMaVeChon.setText(ve.getMaVe());
        
        if (checkNested(ve)) {
            lblTau.setText(ve.getChiTietLichTrinh().getLichTrinh().getTau().getMacTau());
            String di = ve.getChiTietLichTrinh().getLichTrinh().getTuyenDuong().getDiemDi().getViTriGa();
            String den = ve.getChiTietLichTrinh().getLichTrinh().getTuyenDuong().getDiemDen().getViTriGa();
            lblHanhTrinh.setText(di + " -> " + den);
            lblNgayDi.setText(ve.getChiTietLichTrinh().getLichTrinh().getNgayGioKhoiHanhFormatted());
        }
        
        if (ve.getChiTietLichTrinh() != null && ve.getChiTietLichTrinh().getCho() != null) {
            lblGhe.setText("Toa " + ve.getChiTietLichTrinh().getCho().getToa().getLoaiToa().getDescription() + 
                           " - Ghế " + ve.getChiTietLichTrinh().getCho().getSoThuTu());
        }

        // --- 2. TÍNH THỜI GIAN CÒN LẠI ---
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime gioKhoiHanh = ve.getChiTietLichTrinh().getLichTrinh().getNgayGioKhoiHanh();
        long hoursDiff = ChronoUnit.HOURS.between(now, gioKhoiHanh);
        
        if (hoursDiff > 0) lblThoiGianConLai.setText(hoursDiff + " giờ");
        else lblThoiGianConLai.setText("Đã khởi hành / Quá hạn");

        String mode = (String) mainController.getUserData("transactionType");

        // --- 3. LOGIC RIÊNG CHO TỪNG MODE ---
        
        if (BanVeController.MODE_TRA_VE.equals(mode)) {
            btnXacNhanTra.setDisable(true); // Disable trước khi tính xong
            
            double giaVeHienTai = ve.getChiTietLichTrinh().getGiaChoNgoi();
            lblGiaVeGoc.setText(moneyFormatter.format(giaVeHienTai));

            // Check điều kiện Trả vé (ví dụ < 4h)
            if (hoursDiff < 4) {
                lblThongBaoLoi.setText("Không thể trả: Quá hạn (phải trước 4h).");
                lblDieuKienVe.setText("Không đạt");
                lblPhiTraVe.setText("-");
                lblTienHoanLai.setText("-");
                return;
            }

            // Tính phí trả
            double tyLeKhauTru = ("DaDoi".equalsIgnoreCase(ve.getTrangThai())) ? 0.30 : (hoursDiff < 24 ? 0.20 : 0.10);
            String lyDo = ("DaDoi".equalsIgnoreCase(ve.getTrangThai())) ? "Vé đã đổi (30%)" : (hoursDiff < 24 ? "Trước 4h-24h (20%)" : "Trước >24h (10%)");
            
            double phiTraVe = Math.max(giaVeHienTai * tyLeKhauTru, 10000); // Min 10k
            phiTraVe = Math.ceil(phiTraVe / 1000.0) * 1000;
            double tienHoanLai = giaVeHienTai - phiTraVe;
            this.calculatedRefundAmount = tienHoanLai;

            lblDieuKienVe.setText(lyDo);
            lblPhiTraVe.setText(moneyFormatter.format(phiTraVe));
            lblTienHoanLai.setText(moneyFormatter.format(tienHoanLai));
            
            btnXacNhanTra.setDisable(false); // Enable nút
        }
        
        else if (BanVeController.MODE_DOI_VE.equals(mode)) {
            btnDoiVe.setDisable(true);
            
            // Check điều kiện Đổi vé (ví dụ < 24h)
            if (hoursDiff < 24) {
                lblThongBaoLoi.setText("Không thể đổi: Quá hạn (phải trước 24h).");
                lblThoiGianConLai.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                return;
            }
            
            // OK -> Enable nút đổi
            lblThoiGianConLai.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
            btnDoiVe.setDisable(false);
        }
    }
    
    @FXML
    private void handleXacNhanTra() {
        if (selectedVe == null) return;
        
        // 1. Alert Xác nhận
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận trả vé");
        confirm.setHeaderText("Bạn có chắc chắn muốn trả vé này?");
        confirm.setContentText("Mã vé: " + selectedVe.getMaVe() + "\nSố tiền hoàn: " + moneyFormatter.format(calculatedRefundAmount));

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        // 2. Gọi Service xử lý Transaction (DB)
        String maNhanVien = mainController.getNhanVien().getMaNhanVien();
        boolean success = traVeService.processTraVe(selectedVe, this.calculatedRefundAmount, maNhanVien);

        if (success) {
            if (selectedVe.getKhachHang() == null || selectedVe.getKhachHang().getTenKhachHang() == null) {
                // Gọi DAO lấy thông tin hành khách dựa trên mã vé
                KhachHang kh = khachHangDAO.getHanhKhachByMaVe(selectedVe.getMaVe());
                
                // Nếu không tìm thấy người đi, thử tìm người mua (fallback)
                if (kh == null) {
                    kh = khachHangDAO.getNguoiMuaByMaVe(selectedVe.getMaVe());
                }
                
                if (kh != null) {
                    selectedVe.setKhachHang(kh);
                }
            }
            
            // 3. Chuẩn bị dữ liệu in
            double giaGoc = selectedVe.getChiTietLichTrinh().getGiaChoNgoi();
            double thucNhan = this.calculatedRefundAmount;
            double phiTra = giaGoc - thucNhan;
            
            // 4. In và hiển thị Preview
            JasperPrint jasperPrint = createRefundReport(selectedVe, giaGoc, phiTra, thucNhan);
            
            if (jasperPrint != null) {
                showPreviewDialog(jasperPrint);
            } else {
                showAlert(Alert.AlertType.INFORMATION, "Trả vé thành công! (Lỗi tạo file in)");
                handleTimKiem(); // Refresh danh sách vé
            }
            
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống: Không thể thực hiện trả vé.");
        }
    }
    
    private JasperPrint createRefundReport(VeTau ve, double giaGoc, double phiTra, double thucNhan) {
        try {
            // Đường dẫn file .jrxml trong resources
            String reportPath = "/views/bien-lai-tra-ve.xml"; 
            InputStream reportStream = getClass().getResourceAsStream(reportPath);
            
            if (reportStream == null) {
                System.err.println("Không tìm thấy file: " + reportPath);
                return null;
            }

            JasperDesign jasperDesign = JRXmlLoader.load(reportStream);
            JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);

            // Chuẩn bị tham số
            Map<String, Object> parameters = new HashMap<>();
            
            parameters.put("p_MaGiaoDich", "GD-" + System.currentTimeMillis());
            parameters.put("p_NgayTra", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            
            // Lấy tên nhân viên an toàn
            String tenNV = "Admin";
            if (mainController != null && mainController.getNhanVien() != null) {
                tenNV = mainController.getNhanVien().getTenNhanVien();
            }
            parameters.put("p_NhanVien", tenNV);
            
            // Thông tin vé
            parameters.put("p_MaVe", ve.getMaVe());
            parameters.put("p_KhachHang", ve.getKhachHang() != null ? ve.getKhachHang().getTenKhachHang() : "Khách lẻ");
            
            String giayTo = "-";
            if (ve.getKhachHang() != null) {
                giayTo = (ve.getKhachHang().getSoCCCD() != null && !ve.getKhachHang().getSoCCCD().isEmpty()) 
                         ? ve.getKhachHang().getSoCCCD() 
                         : ve.getKhachHang().getHoChieu();
            }
            parameters.put("p_SoGiayTo", giayTo);
            
            // Hành trình
            parameters.put("p_Tau", ve.getChiTietLichTrinh().getLichTrinh().getTau().getMacTau());
            parameters.put("p_GaDi", ve.getChiTietLichTrinh().getLichTrinh().getTuyenDuong().getDiemDi().getViTriGa());
            parameters.put("p_GaDen", ve.getChiTietLichTrinh().getLichTrinh().getTuyenDuong().getDiemDen().getViTriGa());
            parameters.put("p_NgayDi", ve.getChiTietLichTrinh().getLichTrinh().getNgayGioKhoiHanh().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            
            parameters.put("p_Toa", ve.getChiTietLichTrinh().getCho().getToa().getTenToa());
            parameters.put("p_Ghe", String.valueOf(ve.getChiTietLichTrinh().getCho().getSoThuTu()));
            
            // Tiền tệ
            parameters.put("p_GiaVeGoc", giaGoc);
            parameters.put("p_LePhi", phiTra);
            parameters.put("p_ThucNhan", thucNhan);

            // Fill report với tham số và không dùng datasource (vì data đã nằm trong param)
            return JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource());

        } catch (JRException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Hiển thị Dialog Preview (Ảnh hóa đơn)
     */
    private void showPreviewDialog(JasperPrint jasperPrint) {
        try {
            // Chuyển trang đầu tiên thành ảnh (zoom 1.6f cho nét)
            BufferedImage bufferedImage = (BufferedImage) JasperPrintManager.printPageToImage(jasperPrint, 0, 1.6f);
            Image fxImage = SwingFXUtils.toFXImage(bufferedImage, null);

            ImageView imageView = new ImageView(fxImage);
            imageView.setPreserveRatio(true);
            imageView.setFitHeight(500); // Chiều cao ảnh trong dialog

            // Tạo Dialog
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Xem trước Biên lai trả vé");
            dialog.setHeaderText("Giao dịch thành công! Kiểm tra biên lai trước khi kết thúc.");
            
            VBox content = new VBox(imageView);
            content.setPadding(new Insets(10));
            content.setStyle("-fx-alignment: center; -fx-background-color: #eee;");
            dialog.getDialogPane().setContent(content);

            // Nút bấm
            ButtonType btnTypePrint = new ButtonType("In Biên Lai", ButtonData.OTHER);
            ButtonType btnTypeFinish = new ButtonType("Kết thúc", ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(btnTypePrint, btnTypeFinish);

            // Xử lý nút In (không đóng dialog)
            final Button btnPrint = (Button) dialog.getDialogPane().lookupButton(btnTypePrint);
            btnPrint.addEventFilter(ActionEvent.ACTION, event -> {
                event.consume(); // Chặn đóng dialog
                try {
                    // In ra máy in mặc định (true = hiện hộp thoại chọn máy in)
                    JasperPrintManager.printReport(jasperPrint, true);
                    showAlert(Alert.AlertType.INFORMATION, "Đã gửi lệnh in!");
                } catch (JRException e) {
                    showAlert(Alert.AlertType.ERROR, "Lỗi máy in: " + e.getMessage());
                }
            });

            // Hiển thị và chờ người dùng bấm Kết thúc
            dialog.showAndWait();
            
            // Sau khi đóng dialog -> Reset màn hình
            handleTimKiem();

        } catch (JRException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi tạo ảnh xem trước: " + e.getMessage());
            // Dù lỗi preview vẫn phải reset màn hình vì giao dịch đã thành công
            handleTimKiem();
        }
    }
    
    @FXML
    private void handleDoiVe() {
        if (selectedVe == null) return;

        // 1. Kiểm tra trạng thái
        if (!"DaBan".equals(selectedVe.getTrangThai())) {
            showAlert(Alert.AlertType.ERROR, "Chỉ được đổi vé 1 lần duy nhất!");
            return;
        }

        // 2. Kiểm tra thời gian (Quy định: Trước 24h)
        LocalDateTime now = LocalDateTime.now();
        
        // Cần check null an toàn cho Lịch trình
        if (selectedVe.getChiTietLichTrinh() == null || selectedVe.getChiTietLichTrinh().getLichTrinh() == null) {
             showAlert(Alert.AlertType.ERROR, "Lỗi dữ liệu vé (Không tìm thấy lịch trình).");
             return;
        }
        
        LocalDateTime gioKhoiHanh = selectedVe.getChiTietLichTrinh().getLichTrinh().getNgayGioKhoiHanh();
        long hoursDiff = ChronoUnit.HOURS.between(now, gioKhoiHanh);

        if (hoursDiff < 24) {
            showAlert(Alert.AlertType.ERROR, "Không đủ điều kiện đổi vé.\nPhải đổi trước giờ tàu chạy tối thiểu 24 giờ.\nThời gian còn lại: " + hoursDiff + " giờ.");
            return;
        }
        
        if (selectedVe.getKhachHang() == null) {
            KhachHang khachDi = khachHangDAO.getHanhKhachByMaVe(selectedVe.getMaVe());
            if (khachDi != null) {
                selectedVe.setKhachHang(khachDi);
                System.out.println("DEBUG: Đã nạp thông tin khách hàng vào vé: " + khachDi.getTenKhachHang());
            } else {
                System.err.println("ERROR: Không tìm thấy khách hàng cho vé này!");
            }
        }

        // --- 3. HIỆN THÔNG BÁO THÀNH CÔNG (UX Only) ---
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Đủ điều kiện đổi vé");
        alert.setHeaderText(null);
        alert.setContentText("Vé đủ điều kiện đổi.\nTrạng thái chỗ ngồi sẽ được cập nhật sau khi hoàn tất chọn vé mới.\nVui lòng chọn lại lịch trình mong muốn.");
        alert.showAndWait();

        // --- 4. RESET CACHE & CHUYỂN TRANG ---
        // a. Xóa sạch cache cũ để tránh lỗi dính giao diện (Fix lỗi Step 2)
        mainController.prepareForNewDoiVeTransaction();
        // b. Set lại mode (vì hàm prepare ở trên có thể chưa set hoặc lỡ xóa)
        mainController.setUserData("transactionType", BanVeController.MODE_DOI_VE);
        // c. Lưu vé cũ vào Session
        mainController.setUserData("veCuCanDoi", selectedVe);
        // d. Chuyển màn hình
        mainController.loadContent("step-1.fxml");
    }
    
    private void clearOldTransactionData() {
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
    }

    private void clearDetailView() {
        selectedVe = null;
        lblMaVeChon.setText("...");
        lblTau.setText("...");
        lblHanhTrinh.setText("...");
        lblNgayDi.setText("...");
        lblGhe.setText("...");
        lblThoiGianConLai.setText("...");
        lblDieuKienVe.setText("...");
        lblGiaVeGoc.setText("0 VNĐ");
        lblPhiTraVe.setText("0 VNĐ");
        lblTienHoanLai.setText("0 VNĐ");
        lblThongBaoLoi.setText("");
        
        if (btnXacNhanTra != null) btnXacNhanTra.setDisable(true);
        if (btnDoiVe != null) btnDoiVe.setDisable(true);
        if (paneChiTietTra != null) paneChiTietTra.setDisable(true);
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}