/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ptudn12.main.controller;

/**
 *
 * @author fo3cp
 */

import com.ptudn12.main.controller.VeTamThoi;
import com.ptudn12.main.enums.LoaiVe;
import com.ptudn12.main.dao.*;
import com.ptudn12.main.entity.HoaDon;
import com.ptudn12.main.entity.VeTau;
import com.ptudn12.main.utils.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URL;

import javafx.stage.Modality;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javax.swing.SwingUtilities;
import org.thymeleaf.context.Context;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

public class Step4Controller {
    // --- FXML Left Section ---
    @FXML private VBox containerVe;
    @FXML private Label lblDetailTongTienVe;
    @FXML private Label lblDetailGiamDoiTuong;
    @FXML private Label lblDetailGiamDiem;
    @FXML private Label lblDetailBaoHiem;
    @FXML private Label lblDetailTongThanhToan;
    @FXML private Button btnXuatHoaDon;
    @FXML private Button btnDoiDiem;
    @FXML private Button btnTichDiem;
    @FXML private Label lblTitleTongTien;
    @FXML private Label lblTitleGiamDoiTuong;
    @FXML private Label lblTitleBaoHiem;
    

    // --- FXML Right Section ---
    @FXML private Label lblDisplayTongThanhToan;
    @FXML private TextField txtTienKhachDua;
    @FXML private FlowPane flowPaneSuggestions;
    @FXML private Label lblTienThoiLai;
    
    // Khai báo @FXML cho các Label header vé
    @FXML private HBox ticketHeaderRow;
    @FXML private Label headerChuyenTau;
    @FXML private Label headerToaCho;
    @FXML private Label headerHanhKhach;
    @FXML private Label headerLoaiVe;
    @FXML private Label headerDonGia;

    // --- FXML Footer ---
    @FXML private Button btnXacNhanVaIn;
    @FXML private Button btnHoanTat;
    @FXML private Button btnQuayLai;
    
    // Helpers
    private BanVeController mainController;
    private DecimalFormat moneyFormatter = new DecimalFormat("#,##0 'VNĐ'");
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final double PHI_BAO_HIEM = 2000;
    
    private List<Map<String, Object>> danhSachHanhKhach;
    private Map<String, String> thongTinNguoiMua;
    private double tongThanhToanValue = 0; // Giá trị này LUÔN LÀ SỐ ĐÃ LÀM TRÒN
    
    private double giamTuDiem = 0;
    private TemplateEngine templateEngine;
    private final double TAX_RATE = 0.08; // 8%
    private String completedMaHoaDon = null;
    private static final String GITHUB_PAGES_URL = "https://huyp7922-sys.github.io/railway-ticket-verify/";
    
    // DAOs
    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();
    private final VeTauDAO veTauDAO = new VeTauDAO();
    private final ChiTietHoaDonDAO chiTietHoaDonDAO = new ChiTietHoaDonDAO();
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();
    private final ChiTietLichTrinhDAO chiTietLichTrinhDAO = new ChiTietLichTrinhDAO();
    
    public void setMainController(BanVeController mainController) {
        this.mainController = mainController;
    }
    
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
        txtTienKhachDua.textProperty().addListener((obs, oldVal, newVal) -> {
            // Chặn nhập chữ
            if (!newVal.matches("\\d*")) {
                txtTienKhachDua.setText(oldVal);
                return;
            }
            
            // Giới hạn 10 chữ số (tỷ đồng)
            if (newVal.length() > 10) { 
                txtTienKhachDua.setText(oldVal);
                return;
            }

            calculateChange();
        });

        // Disable nút xác nhận ban đầu
        btnXacNhanVaIn.setDisable(true);
        if (btnHoanTat != null) {
            btnHoanTat.setVisible(false);
            btnHoanTat.setManaged(false);
        }
    }
    
    public void initData() {
        // 1. Lấy dữ liệu từ Step 3
        danhSachHanhKhach = (List<Map<String, Object>>) mainController.getUserData("danhSachHanhKhachDaNhap");
        thongTinNguoiMua = (Map<String, String>) mainController.getUserData("thongTinNguoiMua");
        String tongThanhToanStr = (String) mainController.getUserData("tongThanhTien");
        
        double rawNewPrice = 0;
        try {
            if (tongThanhToanStr != null) {
                String numericString = tongThanhToanStr.replaceAll("[^\\d]", "");
                rawNewPrice = Double.parseDouble(numericString);
            }
        } catch (Exception e) {
            System.err.println("Lỗi parse tiền: " + e.getMessage());
        }

        String mode = (String) mainController.getUserData("transactionType");
        
        // --- CHECK MODE ĐỂ HIỂN THỊ ---
        if (BanVeController.MODE_DOI_VE.equals(mode)) {
            // CHẾ ĐỘ ĐỔI VÉ
            
            // Vô hiệu hoá nút tích điểm
            if (btnTichDiem != null) {
                btnTichDiem.setDisable(true);
                btnTichDiem.setVisible(false);
            }
            
            VeTau veCu = (VeTau) mainController.getUserData("veCuCanDoi");
            
            // a. Lấy giá vé cũ (Giá thực tế đã mua trong CTHD)
            // Tạm thời lấy giá niêm yết nếu chưa query được CTHD
            double giaVeCu = chiTietHoaDonDAO.getGiaThucTeDaTra(veCu.getMaVe());
            if (giaVeCu == 0 && veCu.getChiTietLichTrinh() != null) {
                giaVeCu = veCu.getChiTietLichTrinh().getGiaChoNgoi();
            }
            
            double phiDoiVe = 20000; // Phí cố định
            
            // b. Tính toán chênh lệch
            double chenhLechGiaVe = rawNewPrice - giaVeCu;
            if (chenhLechGiaVe < 0) chenhLechGiaVe = 0; 
            
            double tongPhaiThu = chenhLechGiaVe + phiDoiVe;
            this.tongThanhToanValue = roundUpToThousand(tongPhaiThu);

            // c. Cập nhật LABEL hiển thị
            // 1. Dòng Giá vé mới
            lblTitleTongTien.setText("Giá vé mới:"); // Set tiêu đề bên trái
            lblDetailTongTienVe.setText(moneyFormatter.format(rawNewPrice));
            lblDetailTongTienVe.setStyle("-fx-text-fill: #008000; -fx-font-weight: bold;"); 
            
            // 2. Dòng Giá vé cũ
            lblTitleGiamDoiTuong.setText("Trừ giá vé cũ:");
            lblDetailGiamDoiTuong.setText(moneyFormatter.format(giaVeCu));
            lblDetailGiamDoiTuong.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;"); 
            
            // 3. Dòng Phí đổi vé
            lblTitleBaoHiem.setText("Lệ phí đổi vé:");
            lblDetailBaoHiem.setText("+" + moneyFormatter.format(phiDoiVe));
            lblDetailBaoHiem.setStyle("-fx-text-fill: #333;");

            lblDetailGiamDiem.setText(""); 
            
            btnXacNhanVaIn.setText("XÁC NHẬN ĐỔI VÉ");

        } else {
            // ============================================================
            // CHẾ ĐỘ BÁN VÉ THƯỜNG
            // ============================================================
            this.tongThanhToanValue = roundUpToThousand(rawNewPrice);
            
            // Tính toán chi tiết hiển thị
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

            // Set lại text chuẩn cho bán vé
            lblDetailTongTienVe.setText(moneyFormatter.format(tongTienVeGoc));
            lblDetailGiamDoiTuong.setText(moneyFormatter.format(tongGiamDoiTuong));
            lblDetailGiamDoiTuong.setStyle("-fx-text-fill: #e74c3c;"); // Reset style nếu cần
            lblDetailBaoHiem.setText(moneyFormatter.format(tongBaoHiem));
            lblDetailBaoHiem.setStyle("-fx-text-fill: #333;");
//            lblDetailGiamDiem.setText("Giảm điểm tích lũy: 0 VND"); // Reset
            btnXacNhanVaIn.setText("THANH TOÁN & IN VÉ");
        }
        

        // --- CÁC BƯỚC CHUNG (VALIDATION & UI) ---
        if (danhSachHanhKhach == null || danhSachHanhKhach.isEmpty() || thongTinNguoiMua == null) {
            showAlert(Alert.AlertType.ERROR, "Lỗi dữ liệu", "Không thể tải thông tin từ bước trước.");
            btnXacNhanVaIn.setDisable(true);
            return;
        }

        populateTicketTable();

        lblDetailTongThanhToan.setText(moneyFormatter.format(tongThanhToanValue));
        lblDisplayTongThanhToan.setText(moneyFormatter.format(tongThanhToanValue));

        generateSuggestionButtons();

        txtTienKhachDua.clear();
        lblTienThoiLai.setText("0 VNĐ");
        
        btnXacNhanVaIn.setDisable(true);
        btnXacNhanVaIn.setVisible(true);
        btnXacNhanVaIn.setManaged(true);
        
        if (btnHoanTat != null) {
            btnHoanTat.setVisible(false);
            btnHoanTat.setManaged(false);
        }
        
        if (btnQuayLai != null) {
            btnQuayLai.setDisable(false);
            btnQuayLai.setVisible(true);
        }
        
        if (btnXuatHoaDon != null) {
            btnXuatHoaDon.setDisable(true);
        }
    }
    
    // --- HÀM LÀM TRÒN 1000đ ---
    private double roundUpToThousand(double value) {
        if (value % 1000 == 0) return value;
        return Math.ceil(value / 1000.0) * 1000;
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
        col1.getChildren().addAll(
                new Label("Tàu " + ve.getLichTrinh().getTau().getMacTau()),
                new Label(ve.getLichTrinh().getNgayGioKhoiHanh().format(formatter)){{ setStyle("-fx-font-size: 11px;");}}
        );

        // Cột 2: Toa - Chỗ
        Label col2 = new Label("Toa " + ve.getChiTietToa().getToa().getMaToa() + " - Ghế " + ve.getChiTietToa().getSoThuTu());
        col2.setPrefWidth(150.0);

        // Cột 3: Hành khách
        VBox col3 = new VBox(2);
        HBox.setHgrow(col3, Priority.ALWAYS);
        col3.getChildren().addAll(
                new Label((String) hanhKhachInfo.get("hoTen")),
                new Label("ID: " + hanhKhachInfo.get("soGiayTo")){{ setStyle("-fx-font-size: 11px;");}}
        );

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
         if (headerLabel == null || rowColumnNode == null || !(rowColumnNode instanceof Region)) return;
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
    private void displayPaymentDetailsStandard() {
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

        double giamDiem = 0; // Tạm thời

        lblDetailTongTienVe.setText(moneyFormatter.format(tongTienVeGoc));
        lblDetailGiamDoiTuong.setText("- " + moneyFormatter.format(tongGiamDoiTuong));
        lblDetailGiamDiem.setText("- " + moneyFormatter.format(giamDiem));
        lblDetailBaoHiem.setText(moneyFormatter.format(tongBaoHiem));
        
        // QUAN TRỌNG: Hiển thị Tổng cuối cùng phải khớp với số đã làm tròn
        lblDetailTongThanhToan.setText(moneyFormatter.format(tongThanhToanValue));
    }

    // --- CẬP NHẬT: Tạo nút gợi ý với Style Class mới ---
    private void generateSuggestionButtons() {
        flowPaneSuggestions.getChildren().clear();
        if (tongThanhToanValue <= 0) return;

        double[] suggestions = calculateSmartSuggestions(tongThanhToanValue);

        for (double amount : suggestions) {
            Button btn = new Button(moneyFormatter.format(amount));
            // Áp dụng CSS class cho nút đẹp
            btn.getStyleClass().add("money-suggestion-button");
            
            btn.setOnAction(e -> {
                // Xóa chữ " VNĐ" và dấu chấm để lấy số raw nhập vào ô
                txtTienKhachDua.setText(String.valueOf((long)amount));
                calculateChange();
            });
            flowPaneSuggestions.getChildren().add(btn);
        }
    }

    // --- CẬP NHẬT: Thuật toán gợi ý tiền thông minh cho VNĐ (Dựa trên số đã làm tròn) ---
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
        return suggestions.stream()
                .filter(val -> val >= totalLong)
                .limit(6)
                .mapToDouble(Long::doubleValue)
                .toArray();
    }
    
    // Hàm tiện ích làm tròn lên theo bội số
    private long roundUpTo(long value, long multiple) {
        if (multiple == 0) return value;
        long remainder = value % multiple;
        if (remainder == 0) return value;
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
        if (this.completedMaHoaDon == null) {
                showAlert(Alert.AlertType.WARNING, "Chưa có hóa đơn",
                                "Chưa có giao dịch nào được hoàn tất để xuất hóa đơn.");
                return;
        }

        // 1. Lấy dữ liệu hóa đơn thật từ CSDL
        HoaDon selectedInvoice = hoaDonDAO.layHoaDonTheoMa(this.completedMaHoaDon);
        if (selectedInvoice == null) {
                showAlert(Alert.AlertType.ERROR, "Lỗi",
                                "Không tìm thấy hóa đơn với mã " + this.completedMaHoaDon + " trong cơ sở dữ liệu.");
                return;
        }

        File tempPdfFile = null;
        try {
                // 2. Tạo file PDF từ dữ liệu thật
                tempPdfFile = generateInvoicePdf(selectedInvoice);
                if (tempPdfFile == null || !tempPdfFile.exists()) {
                        throw new IOException("Không thể tạo file PDF tạm thời.");
                }

                // 3. Mở cửa sổ xem PDF (logic giống hệt InvoiceManagementController)
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/pdf-viewer.fxml"));
                Scene scene = new Scene(loader.load());

                Stage viewerStage = new Stage();
                viewerStage.setTitle("Xem trước Hóa đơn: " + selectedInvoice.getMaHoaDon());
                viewerStage.initModality(Modality.APPLICATION_MODAL);
                viewerStage.setWidth(700);
                viewerStage.setHeight(800);
                viewerStage.setScene(scene);

                PdfViewerController viewerController = loader.getController();
                viewerController.setStage(viewerStage);
                viewerController.loadDocument(tempPdfFile);

                viewerStage.setOnCloseRequest(e -> viewerController.closeDocument());
                viewerStage.showAndWait();

        } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở trình xem trước PDF: " + e.getMessage());
        } finally {
                if (tempPdfFile != null && tempPdfFile.exists()) {
                        tempPdfFile.delete();
                }
        }
    }

    @FXML
    private void handleDoiDiem() {
        if (thongTinNguoiMua == null || thongTinNguoiMua.get("soGiayToIdentifier") == null) {
            showAlert(Alert.AlertType.WARNING, "Lỗi", "Không có thông tin người mua để đổi điểm.");
            return;
        }

        // 1. CHECK NGHIỆP VỤ: KHÔNG ÁP DỤNG TRỒNG CHÉO KHUYẾN MÃI
        boolean daCoGiamGiaDoiTuong = false;
        if (danhSachHanhKhach != null) {
            for (Map<String, Object> hanhKhach : danhSachHanhKhach) {
                LoaiVe loaiVe = (LoaiVe) hanhKhach.get("doiTuong");
                if (loaiVe != null && loaiVe.getHeSoGiamGia() > 0) {
                    daCoGiamGiaDoiTuong = true;
                    break;
                }
            }
        }
        if (daCoGiamGiaDoiTuong) {
            showAlert(Alert.AlertType.WARNING, "Không thể áp dụng", 
                "Đơn hàng này đã có vé được giảm giá theo đối tượng.\n" +
                "Theo quy định, không được áp dụng đồng thời với đổi điểm tích lũy.");
            return;
        }

        // 2. CHUẨN BỊ DỮ LIỆU ĐIỂM
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

        // --- TÍNH TOÁN GIỚI HẠN 10% ---
        // 1 điểm = 1000 VNĐ. Giảm tối đa 10% tổng tiền.
        double maxTienGiam = tongThanhToanValue * 0.10; 
        int maxDiemTheoQuyDinh = (int) (maxTienGiam / 1000); // Làm tròn xuống số nguyên
        
        // Số điểm thực tế được phép đổi là Min(Điểm đang có, Giới hạn 10%)
        int diemToiDaDuocDoi = Math.min(diemHienTai, maxDiemTheoQuyDinh);
        
        if (diemToiDaDuocDoi <= 0) {
             showAlert(Alert.AlertType.INFORMATION, "Thông báo", 
                 "Giá trị đơn hàng quá nhỏ hoặc hạn mức 10% không đủ để đổi điểm (Tối thiểu 1000đ).");
             return;
        }

        // 3. HIỂN THỊ DIALOG
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Đổi điểm tích lũy");
        dialog.setHeaderText("Quy đổi: 1 điểm = 1.000 VNĐ");

        ButtonType doiDiemButtonType = new ButtonType("Đổi điểm", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(doiDiemButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField diemDoiField = new TextField();
        diemDoiField.setPromptText("Nhập số điểm");
        
        // Label hiển thị giới hạn cho khách biết
        Label lblGioiHan = new Label("Tối đa được đổi: " + diemToiDaDuocDoi + " điểm");
        lblGioiHan.setStyle("-fx-text-fill: #e74c3c; -fx-font-style: italic; -fx-font-size: 11px;");

        // Label hiển thị số tiền tương ứng khi gõ
        Label lblQuyDoiTien = new Label("= 0 VNĐ");
        lblQuyDoiTien.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");

        grid.add(new Label("Điểm hiện tại:"), 0, 0);
        grid.add(new Label(String.valueOf(diemHienTai)), 1, 0);
        
        grid.add(new Label("Điểm muốn đổi:"), 0, 1);
        grid.add(diemDoiField, 1, 1);
        
        grid.add(lblGioiHan, 1, 2);
        grid.add(lblQuyDoiTien, 2, 1);

        dialog.getDialogPane().setContent(grid);
        
        // Validate Input
        Node doiDiemButton = dialog.getDialogPane().lookupButton(doiDiemButtonType);
        doiDiemButton.setDisable(true);
        
        diemDoiField.textProperty().addListener((observable, oldValue, newValue) -> {
            // Chỉ cho nhập số
            if (!newValue.matches("\\d*")) {
                diemDoiField.setText(newValue.replaceAll("[^\\d]", ""));
                return;
            }

            if (newValue.trim().isEmpty()) {
                doiDiemButton.setDisable(true);
                lblQuyDoiTien.setText("= 0 VNĐ");
                return;
            }

            try {
                int inputDiem = Integer.parseInt(newValue);
                
                // Hiển thị số tiền tương ứng
                lblQuyDoiTien.setText("= " + moneyFormatter.format(inputDiem * 1000));

                // Logic chặn nút: Nếu nhập quá giới hạn -> Disable nút OK
                if (inputDiem > diemToiDaDuocDoi) {
                    doiDiemButton.setDisable(true);
                    lblGioiHan.setText("Quá giới hạn! Tối đa chỉ: " + diemToiDaDuocDoi + " điểm");
                    lblGioiHan.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    diemDoiField.setStyle("-fx-text-fill: red; -fx-border-color: red;");
                } else if (inputDiem <= 0) {
                    doiDiemButton.setDisable(true);
                } else {
                    // Hợp lệ
                    doiDiemButton.setDisable(false);
                    lblGioiHan.setText("Tối đa được đổi: " + diemToiDaDuocDoi + " điểm");
                    lblGioiHan.setStyle("-fx-text-fill: #e74c3c; -fx-font-style: italic;");
                    diemDoiField.setStyle("");
                }
            } catch (NumberFormatException e) {
                doiDiemButton.setDisable(true);
            }
        });

        // Xử lý kết quả trả về
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == doiDiemButtonType) {
                try {
                    int diemMuonDoi = Integer.parseInt(diemDoiField.getText());
                    // Chốt chặn cuối cùng (Double check)
                    return Math.min(diemMuonDoi, diemToiDaDuocDoi);
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(diemDaDoi -> {
            if (diemDaDoi > 0) {
                int diemConLai = diemHienTai - diemDaDoi;
                khachHangDAO.updateDiemTich(khachHangId, diemConLai);

                giamTuDiem = diemDaDoi * 1000.0;
                
                // Cập nhật lại giao diện thanh toán với số tiền mới
                displayPaymentDetailsStandard();
                
                showAlert(Alert.AlertType.INFORMATION, "Thành công", 
                    "Đã đổi " + diemDaDoi + " điểm thành " + moneyFormatter.format(giamTuDiem));
            }
        });
    }

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

    @FXML
    private void handleXacNhanVaIn() {
        if (btnXacNhanVaIn.isDisabled()) return;

        try {
            String maNhanVien = "NV001"; 
            if (mainController.getNhanVien() != null) {
                maNhanVien = mainController.getNhanVien().getMaNhanVien();
            }

            String mode = (String) mainController.getUserData("transactionType");

            // --- TRƯỜNG HỢP 1: ĐỔI VÉ ---
            if (BanVeController.MODE_DOI_VE.equals(mode)) {
                
                VeTau veCu = (VeTau) mainController.getUserData("veCuCanDoi");
                Map<String, Object> hanhKhachInfo = danhSachHanhKhach.get(0);
                VeTamThoi veMoiData = (VeTamThoi) hanhKhachInfo.get("veDi");
                if (veMoiData == null) veMoiData = (VeTamThoi) hanhKhachInfo.get("veVe");
                
                DoiVeService service = new DoiVeService();
                String maVeMoi = service.thucHienDoiVe(veCu, veMoiData, maNhanVien, tongThanhToanValue);
                
                if (maVeMoi != null) {
                    this.completedMaHoaDon = chiTietHoaDonDAO.getMaHoaDonByMaVe(maVeMoi);

                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đổi vé thành công!");
                    List<String> ids = new ArrayList<>();
                    ids.add(maVeMoi);
                    showPrintListDialog(ids);
                    
                    updateUIAfterSuccess();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Giao dịch đổi vé thất bại.");
                }
                return;
            }

            // --- TRƯỜNG HỢP 2: BÁN VÉ MỚI ---
            
            int khachHangId = khachHangDAO.findOrInsertKhachHang(thongTinNguoiMua);
            if (khachHangId == -1) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Lỗi xử lý khách hàng.");
                return;
            }

            String maHoaDon = hoaDonDAO.generateUniqueHoaDonId();
            if (!hoaDonDAO.createHoaDon(maHoaDon, khachHangId, maNhanVien, tongThanhToanValue)) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tạo hóa đơn.");
                return;
            }

            // --- FIX: LƯU LẠI MÃ HÓA ĐƠN ĐỂ IN ---
            this.completedMaHoaDon = maHoaDon;
            // -------------------------------------

            List<String> createdTicketIds = new ArrayList<>();
            for (Map<String, Object> hanhKhach : danhSachHanhKhach) {
                VeTamThoi veDi = (VeTamThoi) hanhKhach.get("veDi");
                VeTamThoi veVe = (VeTamThoi) hanhKhach.get("veVe");
                LoaiVe loaiVe = (LoaiVe) hanhKhach.get("doiTuong");

                if (veDi != null) {
                    String ma = processVe(maHoaDon, khachHangId, veDi, loaiVe);
                    if (ma != null) createdTicketIds.add(ma);
                }
                if (veVe != null) {
                    String ma = processVe(maHoaDon, khachHangId, veVe, loaiVe);
                    if (ma != null) createdTicketIds.add(ma);
                }
            }

            if (!createdTicketIds.isEmpty()) {
                showPrintListDialog(createdTicketIds);
            }

            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Thanh toán thành công!");
            updateUIAfterSuccess();

        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống", ex.getMessage());
        }
    }

    // Hàm phụ để cập nhật UI gọn gàng hơn
    private void updateUIAfterSuccess() {
        btnXacNhanVaIn.setVisible(false);
        if (btnHoanTat != null) {
            btnHoanTat.setVisible(true);
            btnQuayLai.setVisible(false);
            btnHoanTat.setManaged(true);
            btnHoanTat.requestFocus();
        }
        if (btnXuatHoaDon != null) {
            btnXuatHoaDon.setDisable(false); // Enable nút xuất hóa đơn
        }
    }

    private void showPrintListDialog(List<String> ticketIds) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/print-list-view.fxml"));
            Parent root = loader.load();

            PrintListController controller = loader.getController();
            
            // Tạo Stage mới cho modal
            Stage stage = new Stage();
            stage.setTitle("Danh sách vé đã xuất");
            stage.initModality(Modality.APPLICATION_MODAL); // Chặn tương tác cửa sổ chính
            stage.setScene(new Scene(root));
            
            // Truyền dữ liệu vào Controller
            controller.setDialogStage(stage);
            controller.setTicketIds(ticketIds);

            stage.showAndWait(); // Chờ đóng modal rồi mới chạy tiếp (hoặc show() nếu muốn chạy song song)
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi hiển thị", "Không thể mở danh sách in vé: " + e.getMessage());
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
        int chiTietLichTrinhId = chiTietLichTrinhDAO.createChiTietLichTrinh(
                ve.getLichTrinh().getMaLichTrinh(),
                ve.getChiTietToa().getCho().getMaCho(),
                giaChoNgoi, "DaBan"); // Truyền đúng "DaBan" khớp với DB
        
        if (chiTietLichTrinhId != -1) {
            // 2. Tạo mã vé
            String maVe = veTauDAO.generateUniqueVeId();
            if (maVe != null) {
                // 3. Tạo Vé Tàu
                boolean isKhuHoi = !ve.isChieuDi();
                boolean success = veTauDAO.createVeTau(maVe, khachHangId, chiTietLichTrinhId, loaiVe.getDescription(), isKhuHoi, "DaBan");
                
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