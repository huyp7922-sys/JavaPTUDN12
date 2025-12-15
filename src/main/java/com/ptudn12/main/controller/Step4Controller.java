/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ptudn12.main. controller;

/**
 *
 * @author fo3cp
 */
import com.ptudn12.main.entity.TuyenDuong;
import com.ptudn12.main.controller.VeTamThoi;
import com.ptudn12.main. enums.LoaiVe;
import com.ptudn12.main.dao.ChiTietHoaDonDAO;
import com.ptudn12.main. dao.ChiTietLichTrinhDAO;
import com.ptudn12.main. dao.HoaDonDAO;
import com. ptudn12.main.dao.KhachHangDAO;
import com.ptudn12.main.dao.VeTauDAO;
import com. ptudn12.main.entity.VeTau;
import com.ptudn12.main.utils.ReportManager;
import javafx.stage. Modality;

import javafx.application.Platform;
import javafx. fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene. Node;
import javafx.scene. control.*;
import javafx.scene. layout.*;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util. ArrayList;
import java.util. List;
import java.util. Map;
import java.util. Set;
import java.util.TreeSet;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javax.swing. SwingUtilities;

import com.google.zxing.BarcodeFormat;
import com.google. zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;
import org.json.JSONObject;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Base64;

public class Step4Controller {
    @FXML private VBox containerVe;
    @FXML private Label lblDetailTongTienVe;
    @FXML private Label lblDetailGiamDoiTuong;
    @FXML private Label lblDetailGiamDiem;
    @FXML private Label lblDetailBaoHiem;
    @FXML private Label lblDetailTongThanhToan;
    @FXML private Button btnXuatHoaDon;
    @FXML private Button btnDoiDiem;
    @FXML private Button btnTichDiem;

    @FXML private Label lblDisplayTongThanhToan;
    @FXML private TextField txtTienKhachDua;
    @FXML private FlowPane flowPaneSuggestions;
    @FXML private Label lblTienThoiLai;
    
    @FXML private HBox ticketHeaderRow;
    @FXML private Label headerChuyenTau;
    @FXML private Label headerToaCho;
    @FXML private Label headerHanhKhach;
    @FXML private Label headerLoaiVe;
    @FXML private Label headerDonGia;

    @FXML private Button btnXacNhanVaIn;
    @FXML private Button btnHoanTat;
    @FXML private Button btnQuayLai;
    
    private BanVeController mainController;
    private DecimalFormat moneyFormatter = new DecimalFormat("#,##0 'VNĐ'");
    private DateTimeFormatter formatter = DateTimeFormatter. ofPattern("dd/MM/yyyy HH:mm");
    private final double PHI_BAO_HIEM = 2000;
    
    private List<Map<String, Object>> danhSachHanhKhach;
    private Map<String, String> thongTinNguoiMua;
    private double tongThanhToanValue = 0;
    
    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();
    private final VeTauDAO veTauDAO = new VeTauDAO();
    private final ChiTietHoaDonDAO chiTietHoaDonDAO = new ChiTietHoaDonDAO();
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();
    private final ChiTietLichTrinhDAO chiTietLichTrinhDAO = new ChiTietLichTrinhDAO();
    
    private static final String GITHUB_PAGES_URL = "https://huyp7922-sys.github.io/railway-ticket-verify/";
    
    public void setMainController(BanVeController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        txtTienKhachDua. textProperty().addListener((obs, oldVal, newVal) -> calculateChange());

        txtTienKhachDua. setTextFormatter(new TextFormatter<>(change ->
            change.getControlNewText().matches("\\d*") ? change : null
        ));

        btnXacNhanVaIn. setDisable(true);
        if (btnHoanTat != null) {
            btnHoanTat.setVisible(false);
            btnHoanTat.setManaged(false);
        }
    }
    
    public void initData() {
        danhSachHanhKhach = (List<Map<String, Object>>) mainController.getUserData("danhSachHanhKhachDaNhap");
        thongTinNguoiMua = (Map<String, String>) mainController.getUserData("thongTinNguoiMua");
        String tongThanhToanStr = (String) mainController.getUserData("tongThanhTien");

        double rawTotal = 0;
        try {
            if (tongThanhToanStr != null) {
                String numericString = tongThanhToanStr. replaceAll("[^\\d]", "");
                rawTotal = Double.parseDouble(numericString);
            }
        } catch (Exception e) {
            System.err.println("Lỗi chuyển đổi tổng thành tiền: " + e.getMessage());
            rawTotal = 0;
        }
        
        tongThanhToanValue = roundUpToThousand(rawTotal);

        if (danhSachHanhKhach == null || danhSachHanhKhach.isEmpty() || thongTinNguoiMua == null) {
            showAlert(Alert.AlertType.ERROR, "Lỗi dữ liệu", "Không thể tải thông tin từ bước trước.");
            btnXacNhanVaIn. setDisable(true);
            return;
        }

        populateTicketTable();
        displayPaymentDetails();
        lblDisplayTongThanhToan. setText(moneyFormatter.format(tongThanhToanValue));
        generateSuggestionButtons();

        txtTienKhachDua. clear();
        lblTienThoiLai. setText("0 VNĐ");
        
        btnXacNhanVaIn. setDisable(true);
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
    
    private double roundUpToThousand(double value) {
        if (value % 1000 == 0) return value;
        return Math.ceil(value / 1000.0) * 1000;
    }

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

    private Node createTicketTableRow(VeTamThoi ve, Map<String, Object> hanhKhachInfo) {
        HBox row = new HBox(10.0);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 8px 0; -fx-border-color: #eee; -fx-border-width: 0 0 1px 0;");

        VBox col1 = new VBox(2);
        col1.setPrefWidth(150.0);
        col1.getChildren().addAll(
                new Label("Tàu " + ve.getLichTrinh().getTau().getMacTau()),
                new Label(ve.getLichTrinh().getNgayGioKhoiHanh().format(formatter)){{ setStyle("-fx-font-size: 11px;");}}
        );

        Label col2 = new Label("Toa " + ve.getChiTietToa().getToa().getMaToa() + " - Ghế " + ve.getChiTietToa().getSoThuTu());
        col2.setPrefWidth(150.0);

        VBox col3 = new VBox(2);
        HBox. setHgrow(col3, Priority.ALWAYS);
        col3.getChildren().addAll(
                new Label((String) hanhKhachInfo.get("hoTen")),
                new Label("ID: " + hanhKhachInfo.get("soGiayTo")){{ setStyle("-fx-font-size: 11px;");}}
        );

        Label col4 = new Label(((LoaiVe) hanhKhachInfo.get("doiTuong")).getDescription());
        col4.setPrefWidth(150.0);

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
        applyTicketColumnSizing(headerChuyenTau, firstRowNode. getChildren().get(0));
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
                double giaVeGoc = veVe. getGiaVe() - PHI_BAO_HIEM;
                tongTienVeGoc += giaVeGoc;
                tongGiamDoiTuong += giaVeGoc * loaiVe.getHeSoGiamGia();
                tongBaoHiem += PHI_BAO_HIEM;
            }
        }

        double giamDiem = 0;

        lblDetailTongTienVe.setText(moneyFormatter.format(tongTienVeGoc));
        lblDetailGiamDoiTuong.setText("- " + moneyFormatter.format(tongGiamDoiTuong));
        lblDetailGiamDiem.setText("- " + moneyFormatter.format(giamDiem));
        lblDetailBaoHiem.setText(moneyFormatter.format(tongBaoHiem));
        lblDetailTongThanhToan. setText(moneyFormatter.format(tongThanhToanValue));
    }

    private void generateSuggestionButtons() {
        flowPaneSuggestions.getChildren().clear();
        if (tongThanhToanValue <= 0) return;

        double[] suggestions = calculateSmartSuggestions(tongThanhToanValue);

        for (double amount : suggestions) {
            Button btn = new Button(moneyFormatter.format(amount));
            btn.getStyleClass().add("money-suggestion-button");
            
            btn.setOnAction(e -> {
                txtTienKhachDua. setText(String.valueOf((long)amount));
                calculateChange();
            });
            flowPaneSuggestions.getChildren().add(btn);
        }
    }

    private double[] calculateSmartSuggestions(double total) {
        long totalLong = (long) total;
        Set<Long> suggestions = new TreeSet<>();
        
        suggestions.add(totalLong);
        suggestions.add(roundUpTo(totalLong, 10000));
        suggestions.add(roundUpTo(totalLong, 50000));
        suggestions.add(roundUpTo(totalLong, 100000));
        suggestions. add(roundUpTo(totalLong, 500000));
        suggestions.add(roundUpTo(totalLong, 1000000));

        return suggestions.stream()
                .filter(val -> val >= totalLong)
                .limit(6)
                .mapToDouble(Long::doubleValue)
                .toArray();
    }
    
    private long roundUpTo(long value, long multiple) {
        if (multiple == 0) return value;
        long remainder = value % multiple;
        if (remainder == 0) return value;
        return value + multiple - remainder;
    }

    private void calculateChange() {
        try {
            String tienKhachDuaStr = txtTienKhachDua.getText();
            if (tienKhachDuaStr == null || tienKhachDuaStr.isEmpty()) {
                lblTienThoiLai. setText("0 VNĐ");
                btnXacNhanVaIn. setDisable(true);
                return;
            }
            double tienKhachDua = Double.parseDouble(tienKhachDuaStr);
            double tienThoi = tienKhachDua - tongThanhToanValue;

            if (tienThoi >= 0) {
                lblTienThoiLai.setText(moneyFormatter.format(tienThoi));
                btnXacNhanVaIn.setDisable(false);
            } else {
                lblTienThoiLai.setText("Chưa đủ");
                btnXacNhanVaIn.setDisable(true);
            }
        } catch (NumberFormatException e) {
            lblTienThoiLai.setText("Lỗi nhập liệu");
            btnXacNhanVaIn. setDisable(true);
        }
    }
    
    @FXML
    private void handleXuatHoaDon() {
        showAlert(Alert.AlertType. INFORMATION, "Thông báo", "Chức năng Xuất hóa đơn VAT đang được phát triển.");
    }

    @FXML
    private void handleDoiDiem() {
        showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Chức năng Đổi điểm tích lũy đang được phát triển.");
    }

    @FXML
    private void handleTichDiem() {
        showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Chức năng Tích điểm khách hàng đang được phát triển.");
    }

    @FXML
    private void handleXacNhanVaIn() {
        if (btnXacNhanVaIn.isDisabled()) return;

        try {
            String maNhanVien = "NV001";
            
            if (mainController.getNhanVien() != null) {
                maNhanVien = mainController.getNhanVien().getMaNhanVien();
            } else {
                System.err.println("Warning: Chưa có thông tin nhân viên đăng nhập.  Đang dùng NV001.");
            }

            int khachHangId = khachHangDAO.findOrInsertKhachHang(thongTinNguoiMua);
            if (khachHangId == -1) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Lỗi xử lý khách hàng.");
                return;
            }

            String maHoaDon = hoaDonDAO.generateUniqueHoaDonId();
            if (! hoaDonDAO.createHoaDon(maHoaDon, khachHangId, maNhanVien, tongThanhToanValue)) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tạo hóa đơn.");
                return;
            }

            List<String> createdTicketIds = new ArrayList<>();
            for (Map<String, Object> hanhKhach : danhSachHanhKhach) {
                VeTamThoi veDi = (VeTamThoi) hanhKhach.get("veDi");
                VeTamThoi veVe = (VeTamThoi) hanhKhach.get("veVe");
                LoaiVe loaiVe = (LoaiVe) hanhKhach.get("doiTuong");

                if (veDi != null) {
                    String ma = processVe(maHoaDon, khachHangId, veDi, loaiVe, hanhKhach);
                    if (ma != null) createdTicketIds.add(ma);
                }
                if (veVe != null) {
                    String ma = processVe(maHoaDon, khachHangId, veVe, loaiVe, hanhKhach);
                    if (ma != null) createdTicketIds.add(ma);
                }
            }

            if (! createdTicketIds.isEmpty()) {
                showPrintListDialog(createdTicketIds);
            }

            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Thanh toán thành công!");
            
            btnXacNhanVaIn. setVisible(false);

            if (btnHoanTat != null) {
                btnHoanTat. setVisible(true);
                btnQuayLai.setVisible(false);
                btnHoanTat.setManaged(true);
                btnHoanTat.requestFocus();
            }
            
            if (btnXuatHoaDon != null) {
                btnXuatHoaDon.setDisable(false); 
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống", ex.getMessage());
        }
    }

    private void showPrintListDialog(List<String> ticketIds) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/print-list-view.fxml"));
            Parent root = loader.load();

            PrintListController controller = loader.getController();
            
            Stage stage = new Stage();
            stage.setTitle("Danh sách vé đã xuất");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage. setScene(new Scene(root));
            
            controller.setDialogStage(stage);
            controller.setTicketIds(ticketIds);

            stage.showAndWait();
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi hiển thị", "Không thể mở danh sách in vé:  " + e.getMessage());
        }
    }
    
    @FXML
    private void handleHoanTat() {
        mainController.startNewTransaction();
    }
    
    private String processVe(String maHoaDon, int khachHangId, VeTamThoi ve, LoaiVe loaiVe, Map<String, Object> hanhKhachInfo) {
        double giaChoNgoi = ve.getGiaVe() - PHI_BAO_HIEM;
        
        int chiTietLichTrinhId = chiTietLichTrinhDAO.createChiTietLichTrinh(
                ve.getLichTrinh().getMaLichTrinh(),
                ve.getChiTietToa().getCho().getMaCho(),
                giaChoNgoi, "DaBan");
        
        if (chiTietLichTrinhId != -1) {
            String maVe = veTauDAO.generateUniqueVeId();
            if (maVe != null) {
                boolean isKhuHoi = ! ve.isChieuDi();
                boolean success = veTauDAO.createVeTau(maVe, khachHangId, chiTietLichTrinhId, loaiVe.getDescription(), isKhuHoi, "DaBan");
                
                if (success) {
                    double giaGoc = giaChoNgoi;
                    double giamGia = giaGoc * loaiVe.getHeSoGiamGia();
                    double thanhTien = ve.getGiaVe() - giamGia;
                    chiTietHoaDonDAO.createChiTietHoaDon(maHoaDon, maVe, giamGia, thanhTien);
                    
                    generateTicketQRCode(maVe, ve, hanhKhachInfo);
                    
                    return maVe;
                }
            }
        }
        return null;
    }
    
    private void generateTicketQRCode(String maVe, VeTamThoi ve, Map<String, Object> hanhKhachInfo) {
    try {
        String ngayGioKhoiHanh = ve.getLichTrinh().getNgayGioKhoiHanh().toString();
        
        // ✅ FIX: Bỏ dấu cách sau dấu ?
        String qrUrl = GITHUB_PAGES_URL + "?maVe=" + maVe + "&ngay=" + ngayGioKhoiHanh;
        
        BufferedImage qrImage = generateQRImage(qrUrl, 300, 300);
        
        File qrFile = new File("qrcodes/" + maVe + ".png");
        qrFile.getParentFile().mkdirs();
        ImageIO.write(qrImage, "PNG", qrFile);
        
        veTauDAO.updateQRCode(maVe, "qrcodes/" + maVe + ". png");
        
        System.out.println("✅ QR created:  " + qrFile.getAbsolutePath());
        System.out.println("🔗 URL: " + qrUrl);
        
    } catch (Exception e) {
        e.printStackTrace();
    }
}
    private String urlEncode(String text) {
        try {
            return java.net.URLEncoder.encode(text, "UTF-8");
        } catch (Exception e) {
            return text;
        }
    }
    
    private BufferedImage generateQRImage(String data, int width, int height) throws WriterException {
        QRCodeWriter qrWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrWriter.encode(data, BarcodeFormat.QR_CODE, width, height);
        
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }
        return image;
    }
    
    @FXML
    private void handleQuayLai() {
        mainController.loadContent("step-3. fxml");
    }

    private void clearAllUserData() {
         mainController.setUserData("lichTrinhChieuDi", null);
         mainController.setUserData("lichTrinhChieuVe", null);
         mainController.setUserData("gioHang_Di", null);
         mainController. setUserData("gioHang_Ve", null);
         mainController.setUserData("danhSachHanhKhachDaNhap", null);
         mainController.setUserData("thongTinNguoiMua", null);
         mainController.setUserData("tongThanhTien", null);
         mainController. setUserData("step1_gaDi", null);
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