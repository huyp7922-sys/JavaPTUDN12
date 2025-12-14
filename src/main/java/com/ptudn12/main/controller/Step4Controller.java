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
import com.ptudn12.main.dao.ChiTietHoaDonDAO;
import com.ptudn12.main.dao.ChiTietLichTrinhDAO;
import com.ptudn12.main.dao.HoaDonDAO;
import com.ptudn12.main.dao.KhachHangDAO;
import com.ptudn12.main.dao.VeTauDAO;
import com.ptudn12.main.entity.VeTau;
import com.ptudn12.main.utils.ReportManager;
import javafx.stage.Modality;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javax.swing.SwingUtilities;

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
    
    // DAOs
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
        
        // Hiện nút In và Ẩn nút Hoàn tất
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
        showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Chức năng Xuất hóa đơn VAT đang được phát triển.");
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
            String maNhanVien = "NV001"; // Giá trị default đề phòng lỗi
            
            if (mainController.getNhanVien() != null) {
                maNhanVien = mainController.getNhanVien().getMaNhanVien();
            } else {
                System.err.println("Warning: Chưa có thông tin nhân viên đăng nhập. Đang dùng NV001.");
            }

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

            // 3. Tạo Vé & Chi Tiết (Lưu DB)
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

            // 4. Hiện modal danh sách vé
            if (!createdTicketIds.isEmpty()) {
                showPrintListDialog(createdTicketIds);
            }

            // 5. Cập nhật giao diện Step 4
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Thanh toán thành công!");
            
            btnXacNhanVaIn.setVisible(false);

            if (btnHoanTat != null) {
                btnHoanTat.setVisible(true);
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

    // HÀM MỚI ĐỂ MỞ MODAL LIST
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
