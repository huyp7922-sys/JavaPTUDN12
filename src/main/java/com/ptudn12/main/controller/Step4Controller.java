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
import com.ptudn12.main.database.DatabaseConnection;
import com.ptudn12.main.dao.ChiTietHoaDonDAO;
import com.ptudn12.main.dao.ChiTietLichTrinhDAO;
import com.ptudn12.main.dao.HoaDonDAO;
import com.ptudn12.main.dao.KhachHangDAO;
import com.ptudn12.main.dao.VeTauDAO;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Step4Controller {
    // --- FXML Left Section ---
    @FXML private ScrollPane scrollPaneVe;
    @FXML private VBox containerVe;
    @FXML private GridPane gridPaymentDetails;
    @FXML private Label lblDetailTongTienVe;
    @FXML private Label lblDetailGiamDoiTuong;
    @FXML private Label lblDetailGiamDiem;
    @FXML private Label lblDetailBaoHiem;
    @FXML private Label lblDetailTongThanhToan;
    @FXML private Button btnXuatHoaDon;
    @FXML private Button btnDoiDiem;
    @FXML private Button btnTichDiem;

    // --- FXML Right Section ---
    @FXML private Label lblDisplayTongThanhToan;
    @FXML private TextField txtTienKhachDua;
    @FXML private FlowPane flowPaneSuggestions;
    @FXML private Label lblTienThoiLai;
    @FXML private Button btnXacNhanVaIn;
    
    // Khai báo @FXML cho các Label header vé
    @FXML private HBox ticketHeaderRow;
    @FXML private Label headerChuyenTau;
    @FXML private Label headerToaCho;
    @FXML private Label headerHanhKhach;
    @FXML private Label headerLoaiVe;
    @FXML private Label headerDonGia;

    // --- FXML Footer ---
    @FXML private Button btnQuayLai;
    
    // Helpers
    private BanVeController mainController;
    private DecimalFormat moneyFormatter = new DecimalFormat("#,##0 'VNĐ'");
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final double PHI_BAO_HIEM = 2000;
    
    private List<Map<String, Object>> danhSachHanhKhach;
    private Map<String, String> thongTinNguoiMua;
    private double tongThanhToanValue = 0;
    
    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();
    private final VeTauDAO veTauDAO = new VeTauDAO();
    private final ChiTietHoaDonDAO chiTietHoaDonDAO = new ChiTietHoaDonDAO();
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();
    private final ChiTietLichTrinhDAO chiTietLichTrinhDAO = new ChiTietLichTrinhDAO();
    
    public void setMainController(BanVeController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        // Listener cho ô nhập tiền khách đưa
        txtTienKhachDua.textProperty().addListener((obs, oldVal, newVal) -> calculateChange());

        // Chỉ cho phép nhập số
        txtTienKhachDua.setTextFormatter(new TextFormatter<>(change ->
            change.getControlNewText().matches("\\d*") ? change : null
        ));

         // Disable nút xác nhận ban đầu
        btnXacNhanVaIn.setDisable(true);
    }
    
    public void initData() {
        // 1. Lấy dữ liệu từ Step 3
        danhSachHanhKhach = (List<Map<String, Object>>) mainController.getUserData("danhSachHanhKhachDaNhap");
        thongTinNguoiMua = (Map<String, String>) mainController.getUserData("thongTinNguoiMua");
        String tongThanhToanStr = (String) mainController.getUserData("tongThanhTien");

        // Chuyển đổi tổng thành toán String -> double
        try {
            String numericString = tongThanhToanStr.replaceAll("[^\\d]", "");
            tongThanhToanValue = Double.parseDouble(numericString);
        } catch (Exception e) {
            System.err.println("Lỗi chuyển đổi tổng thành tiền: " + e.getMessage());
            tongThanhToanValue = 0;
        }

        if (danhSachHanhKhach == null || danhSachHanhKhach.isEmpty() || thongTinNguoiMua == null) {
            showAlert(Alert.AlertType.ERROR, "Lỗi dữ liệu", "Không thể tải thông tin từ bước trước.");
            btnXacNhanVaIn.setDisable(true);
            return;
        }

        // 2. Hiển thị bảng xác nhận vé
        populateTicketTable();

        // 3. Hiển thị chi tiết thanh toán
        displayPaymentDetails();

        // 4. Hiển thị tổng thanh toán bên phải
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
                    syncTicketTableHeaderWidths((HBox) rowNode); // Đồng bộ header
                    firstRow = false;
                }
            }
            if (veVe != null) {
                 Node rowNode = createTicketTableRow(veVe, hanhKhach);
                 containerVe.getChildren().add(rowNode);
                 if (firstRow) { // Nếu chưa có vé đi nào được thêm
                     syncTicketTableHeaderWidths((HBox) rowNode);
                     firstRow = false;
                 }
            }
        }
    }

    // Tạo một hàng (Node) cho bảng xác nhận vé
    private Node createTicketTableRow(VeTamThoi ve, Map<String, Object> hanhKhachInfo) {
        HBox row = new HBox(10.0); // Giữ spacing của header
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 8px 0; -fx-border-color: #eee; -fx-border-width: 0 0 1px 0;");

        // Cột 1: Chuyến tàu
        VBox col1 = new VBox(2);
        col1.setPrefWidth(150.0); // Sizing cột 1
        col1.getChildren().addAll(
                new Label("Tàu " + ve.getLichTrinh().getTau().getMacTau()),
                new Label(ve.getLichTrinh().getNgayGioKhoiHanh().format(formatter)){{ setStyle("-fx-font-size: 11px;");}}
        );

        // Cột 2: Toa - Chỗ
        Label col2 = new Label("Toa " + ve.getChiTietToa().getToa().getMaToa() + " - Ghế " + ve.getChiTietToa().getSoThuTu());
        col2.setPrefWidth(150.0); // Sizing cột 2

        // Cột 3: Hành khách
        VBox col3 = new VBox(2);
        HBox.setHgrow(col3, Priority.ALWAYS); // Sizing cột 3
        col3.getChildren().addAll(
                new Label((String) hanhKhachInfo.get("hoTen")),
                new Label("ID: " + hanhKhachInfo.get("soGiayTo")){{ setStyle("-fx-font-size: 11px;");}}
        );

        // Cột 4: Loại vé
        Label col4 = new Label(((LoaiVe) hanhKhachInfo.get("doiTuong")).getDescription());
        col4.setPrefWidth(150.0); // Sizing cột 4

        // Cột 5: Đơn giá
        double donGia = 0;
        double giaGoc = ve.getGiaVe() - PHI_BAO_HIEM;
        double heSoGiam = ((LoaiVe) hanhKhachInfo.get("doiTuong")).getHeSoGiamGia();
        donGia = (giaGoc * (1 - heSoGiam)) + PHI_BAO_HIEM;

        Label col5 = new Label(moneyFormatter.format(donGia));
        col5.setPrefWidth(120.0); // Sizing cột 5
        col5.setAlignment(Pos.CENTER_RIGHT);
        col5.setMaxWidth(Double.MAX_VALUE);

        row.getChildren().addAll(col1, col2, col3, col4, col5);
        return row;
    }

    // Đồng bộ độ rộng header vé với hàng đầu tiên
    private void syncTicketTableHeaderWidths(HBox firstRowNode) {
        if (firstRowNode == null || ticketHeaderRow == null || firstRowNode.getChildren().size() != 5) {
             System.err.println("Lỗi đồng bộ header vé: Hàng đầu tiên không hợp lệ.");
             return;
        }

        Node col1 = firstRowNode.getChildren().get(0);
        Node col2 = firstRowNode.getChildren().get(1);
        Node col3 = firstRowNode.getChildren().get(2);
        Node col4 = firstRowNode.getChildren().get(3);
        Node col5 = firstRowNode.getChildren().get(4);

        applyTicketColumnSizing(headerChuyenTau, col1);
        applyTicketColumnSizing(headerToaCho, col2);
        applyTicketColumnSizing(headerHanhKhach, col3);
        applyTicketColumnSizing(headerLoaiVe, col4);
        applyTicketColumnSizing(headerDonGia, col5);

        // ticketHeaderRow.setSpacing(10.0); // Đảm bảo spacing khớp
    }

    // Hàm trợ giúp áp dụng sizing cho header vé
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
    
    // Hiển thị chi tiết thanh toán
    private void displayPaymentDetails() {
        double tongTienVeGoc = 0;
        double tongGiamDoiTuong = 0;
        double tongBaoHiem = 0;

        for (Map<String, Object> hanhKhach : danhSachHanhKhach) {
            VeTamThoi veDi = (VeTamThoi) hanhKhach.get("veDi");
            VeTamThoi veVe = (VeTamThoi) hanhKhach.get("veVe");
            LoaiVe loaiVe = (LoaiVe) hanhKhach.get("doiTuong");

            double giaVeGocDi = (veDi != null) ? veDi.getGiaVe() - PHI_BAO_HIEM : 0;
            double giaVeGocVe = (veVe != null) ? veVe.getGiaVe() - PHI_BAO_HIEM : 0;
            double giaVeGocHanhKhach = giaVeGocDi + giaVeGocVe;

            tongTienVeGoc += giaVeGocHanhKhach;
            tongGiamDoiTuong += giaVeGocHanhKhach * loaiVe.getHeSoGiamGia();
            tongBaoHiem += (veDi != null ? PHI_BAO_HIEM : 0) + (veVe != null ? PHI_BAO_HIEM : 0);
        }

        double giamDiem = 0; // Tạm thời
        tongThanhToanValue = tongTienVeGoc - tongGiamDoiTuong - giamDiem + tongBaoHiem;

        lblDetailTongTienVe.setText(moneyFormatter.format(tongTienVeGoc));
        lblDetailGiamDoiTuong.setText("- " + moneyFormatter.format(tongGiamDoiTuong));
        lblDetailGiamDiem.setText("- " + moneyFormatter.format(giamDiem));
        lblDetailBaoHiem.setText(moneyFormatter.format(tongBaoHiem));
        lblDetailTongThanhToan.setText(moneyFormatter.format(tongThanhToanValue));
    }

    // Tạo các nút gợi ý mệnh giá
    private void generateSuggestionButtons() {
        flowPaneSuggestions.getChildren().clear();
        if (tongThanhToanValue <= 0) return;

        double[] suggestions = calculateSuggestions(tongThanhToanValue);

        for (double amount : suggestions) {
            Button btn = new Button(moneyFormatter.format(amount));
            btn.setOnAction(e -> {
                txtTienKhachDua.setText(String.valueOf((long)amount));
                calculateChange();
            });
            flowPaneSuggestions.getChildren().add(btn);
        }
    }

    // Thuật toán đơn giản để tính mệnh giá gợi ý
    private double[] calculateSuggestions(double total) {
        long totalLong = (long) Math.ceil(total);
        List<Double> suggestionList = new ArrayList<>();
        suggestionList.add((double) totalLong);
        long rounded10k = (long) (Math.ceil(totalLong / 10000.0) * 10000);
        if (rounded10k > totalLong) suggestionList.add((double) rounded10k);
        long rounded50k = (long) (Math.ceil(totalLong / 50000.0) * 50000);
         if (rounded50k > totalLong && !suggestionList.contains((double)rounded50k)) suggestionList.add((double) rounded50k);
        long rounded100k = (long) (Math.ceil(totalLong / 100000.0) * 100000);
         if (rounded100k > totalLong && !suggestionList.contains((double)rounded100k)) suggestionList.add((double) rounded100k);
        long plus20k = (long) (Math.ceil(totalLong / 20000.0) * 20000);
        if (plus20k > totalLong && !suggestionList.contains((double)plus20k)) suggestionList.add((double) plus20k);
        else if (plus20k <= totalLong && !suggestionList.contains((double)(plus20k + 20000))) suggestionList.add((double)(plus20k + 20000));
        long plus50k_variant = (long) (Math.ceil(totalLong / 50000.0) * 50000 + 50000);
        if (plus50k_variant > totalLong && !suggestionList.contains((double)plus50k_variant)) suggestionList.add((double) plus50k_variant);

        return suggestionList.stream().distinct().sorted().limit(6).mapToDouble(Double::doubleValue).toArray();
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
            btnXacNhanVaIn.setDisable(true);
        }
    }
    
    
    @FXML
    private void handleXuatHoaDon() {
        // Lấy thông tin người mua từ biến thongTinNguoiMua
        showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Chức năng Xuất hóa đơn VAT đang được phát triển.");
    }

    @FXML
    private void handleDoiDiem() {
        // TODO: Implement logic đổi điểm
        showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Chức năng Đổi điểm tích lũy đang được phát triển.");
    }

    @FXML
    private void handleTichDiem() {
        // TODO: Implement logic tích điểm
        showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Chức năng Tích điểm khách hàng đang được phát triển.");
    }

    @FXML
    private void handleXacNhanVaIn() {
        if (btnXacNhanVaIn.isDisabled()) {
            showAlert(Alert.AlertType.WARNING, "Thanh toán chưa hợp lệ", "Vui lòng kiểm tra lại số tiền khách đưa.");
            return;
        }

        try {
            String maNhanVien = "NV001"; // Lấy từ session hoặc nơi khác

            // --- a. Tìm hoặc Tạo Khách Hàng ---
            
            int khachHangId = khachHangDAO.findOrInsertKhachHang(thongTinNguoiMua);
            if (khachHangId == -1) {
                showAlert(Alert.AlertType.ERROR, "Lỗi khách hàng", "Không thể xử lý thông tin khách hàng.");
                return;
            }

            // --- b. Tạo Hóa Đơn ---
            String maHoaDon = hoaDonDAO.generateUniqueHoaDonId();
            if (maHoaDon == null) {
                 showAlert(Alert.AlertType.ERROR, "Lỗi tạo mã", "Không thể tạo mã hóa đơn.");
                 return;
            }
            boolean hoaDonCreated = hoaDonDAO.createHoaDon(maHoaDon, khachHangId, maNhanVien, tongThanhToanValue);
            if (!hoaDonCreated) {
                showAlert(Alert.AlertType.ERROR, "Lỗi tạo hóa đơn", "Không thể lưu thông tin hóa đơn.");
                return;
            }

            // --- c. Tạo Vé và Chi Tiết Hóa Đơn ---
            for (Map<String, Object> hanhKhach : danhSachHanhKhach) {
                VeTamThoi veDi = (VeTamThoi) hanhKhach.get("veDi");
                VeTamThoi veVe = (VeTamThoi) hanhKhach.get("veVe");
                LoaiVe loaiVe = (LoaiVe) hanhKhach.get("doiTuong");

                // Xử lý vé đi
                if (veDi != null) {
                    double giaChoNgoiDi = veDi.getGiaVe() - PHI_BAO_HIEM;
                     int chiTietLichTrinhIdDi = chiTietLichTrinhDAO.createChiTietLichTrinh(
                             veDi.getLichTrinh().getMaLichTrinh(),
                             veDi.getChiTietToa().getCho().getMaCho(),
                             giaChoNgoiDi, "DaBan");
                    
                    if (chiTietLichTrinhIdDi == -1) {
                         showAlert(Alert.AlertType.ERROR, "Lỗi tạo CTLT", "Không thể tạo chi tiết lịch trình cho vé đi.");
                         return;
                    }

                    String maVeDi = veTauDAO.generateUniqueVeId();
                    if (maVeDi == null) { 
                        showAlert(Alert.AlertType.ERROR, "Lỗi mã vé", "Không thể sinh ra mã vé đi tự động."); 
                        return; 
                    }
                    boolean veDiCreated = veTauDAO.createVeTau(maVeDi, khachHangId, chiTietLichTrinhIdDi, loaiVe.getDescription(), false, "DaBan");
                    if (!veDiCreated) { 
                        showAlert(Alert.AlertType.ERROR, "Lỗi tạo vé", "Không thể tạo được vé tàu đi."); 
                        return;  
                    }

                    // Tính giảm giá và thành tiền
                    double giaGocDi = giaChoNgoiDi;
                    double giamGiaDi = giaGocDi * loaiVe.getHeSoGiamGia();
                    double thanhTienVeDi = veDi.getGiaVe() - giamGiaDi;
                    boolean cthdDiCreated = chiTietHoaDonDAO.createChiTietHoaDon(maHoaDon, maVeDi, giamGiaDi, thanhTienVeDi);

                    if (!cthdDiCreated)  { 
                        showAlert(Alert.AlertType.ERROR, "Lỗi tạo chi tiết hoá đơn", "Không thể tạo được chi tiết hoá đơn"); 
                        return;  
                    }
                }

                // Xử lý vé về (tương tự vé đi)
                if (veVe != null) {
                      double giaChoNgoiVe = veVe.getGiaVe() - PHI_BAO_HIEM;
                      int chiTietLichTrinhIdVe = chiTietLichTrinhDAO.createChiTietLichTrinh(
                              veVe.getLichTrinh().getMaLichTrinh(),
                              veVe.getChiTietToa().getCho().getMaCho(),
                              giaChoNgoiVe, "DaBan");
                     
                     if (chiTietLichTrinhIdVe == -1)  {
                         showAlert(Alert.AlertType.ERROR, "Lỗi tạo CTLT", "Không thể tạo chi tiết lịch trình cho vé về.");
                         return;
                     }

                     // Tạo Vé về
                     String maVeVe = veTauDAO.generateUniqueVeId();
                     if (maVeVe == null) { 
                        showAlert(Alert.AlertType.ERROR, "Lỗi mã vé", "Không thể sinh ra mã vé về tự động."); 
                        return; 
                    }
                     boolean veVeCreated = veTauDAO.createVeTau(maVeVe, khachHangId, chiTietLichTrinhIdVe, loaiVe.getDescription(), true, "DaBan");
                     if (!veVeCreated) { 
                        showAlert(Alert.AlertType.ERROR, "Lỗi tạo vé", "Không thể tạo được vé tàu về."); 
                        return;  
                    }

                     // Tính giảm giá, thành tiền
                     double giaGocVe = giaChoNgoiVe;
                     double giamGiaVe = giaGocVe * loaiVe.getHeSoGiamGia();
                     double thanhTienVeVe = veVe.getGiaVe() - giamGiaVe;
                     boolean cthdVeCreated = chiTietHoaDonDAO.createChiTietHoaDon(maHoaDon, maVeVe, giamGiaVe, thanhTienVeVe);
                     
                     if (!cthdVeCreated) { 
                        showAlert(Alert.AlertType.ERROR, "Lỗi tạo chi tiết hoá đơn", "Không thể tạo được chi tiết hoá đơn"); 
                        return;  
                    }
                }
           }

            // TODO: Mở dialog in vé
            showAlert(Alert.AlertType.INFORMATION, "In vé", "Đã in vé thành công!");
            
            clearAllUserData();
            mainController.loadContent("step-1.fxml");

        } catch (Exception ex) {
            System.err.println("Lỗi không xác định khi xác nhận: " + ex.getMessage());
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống", "Đã xảy ra lỗi không mong muốn khi xử lý thanh toán.");
        }
    }


    // Hàm xóa dữ liệu người dùng khỏi MainController (sau khi thanh toán thành công)
    private void clearAllUserData() {
         mainController.setUserData("lichTrinhChieuDi", null);
         mainController.setUserData("lichTrinhChieuVe", null);
         mainController.setUserData("gioHang_Di", null);
         mainController.setUserData("gioHang_Ve", null);
         mainController.setUserData("danhSachHanhKhachDaNhap", null);
         mainController.setUserData("thongTinNguoiMua", null);
         mainController.setUserData("tongThanhTien", null);
         // Xóa cả state của Step 1
         mainController.setUserData("step1_gaDi", null);
         mainController.setUserData("step1_gaDen", null);
         mainController.setUserData("step1_ngayDi", null);
         mainController.setUserData("step1_isKhuHoi", null);
         mainController.setUserData("step1_ngayVe", null);
    }


    @FXML
    private void handleQuayLai() {
        // Quay lại Step 3
        mainController.loadContent("step-3.fxml");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        Platform.runLater(alert::showAndWait);
    }
}
