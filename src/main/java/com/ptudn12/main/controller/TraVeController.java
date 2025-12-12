package com.ptudn12.main.controller;

import com.ptudn12.main.dao.ChiTietLichTrinhDAO;
import com.ptudn12.main.dao.KhachHangDAO;
import com.ptudn12.main.dao.VeTauDAO;
import com.ptudn12.main.entity.KhachHang;
import com.ptudn12.main.entity.VeTau;
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

public class TraVeController {

    // --- FXML Elements ---
    @FXML private TextField txtSearchCCCD;
    @FXML private Button btnTimKiem;
    
    @FXML private TableView<VeTau> tblDanhSachVe;
    @FXML private TableColumn<VeTau, String> colMaVe;
    @FXML private TableColumn<VeTau, String> colTau;
    @FXML private TableColumn<VeTau, String> colHanhTrinh;
    @FXML private TableColumn<VeTau, String> colNgayDi;
    @FXML private TableColumn<VeTau, String> colGhe;
    @FXML private TableColumn<VeTau, String> colGiaVe;
    @FXML private TableColumn<VeTau, String> colTrangThai;

    // Khu vực chi tiết
    @FXML private VBox paneChiTietTra;
    @FXML private Label lblMaVeChon;
    @FXML private Label lblThoiGianConLai;
    @FXML private Label lblDieuKienVe;
    @FXML private Label lblGiaVeGoc;
    @FXML private Label lblPhiTraVe;
    @FXML private Label lblTienHoanLai;
    @FXML private Label lblThongBaoLoi;
    @FXML private Button btnXacNhanTra;

    // --- Helpers & DAOs ---
    private final VeTauDAO veTauDAO = new VeTauDAO();
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();
    private final ChiTietLichTrinhDAO chiTietLichTrinhDAO = new ChiTietLichTrinhDAO();
    
    private final DecimalFormat moneyFormatter = new DecimalFormat("#,##0 'VNĐ'");
    
    private VeTau selectedVe = null;
    private double calculatedRefundAmount = 0;

    @FXML
    public void initialize() {
        // Cấu hình cột bảng
        colMaVe.setCellValueFactory(new PropertyValueFactory<>("maVe"));
        colTrangThai.setCellValueFactory(new PropertyValueFactory<>("trangThai"));
        
        // Map dữ liệu từ các object con (Tránh NullPointer)
        colTau.setCellValueFactory(cell -> {
            if (cell.getValue().getChiTietLichTrinh() != null && 
                cell.getValue().getChiTietLichTrinh().getCho() != null &&
                cell.getValue().getChiTietLichTrinh().getCho().getToa() != null) {
                // Hiển thị tên toa vì SP không trả về mác tàu
                return new SimpleStringProperty("Toa: " + cell.getValue().getChiTietLichTrinh().getCho().getToa().getTenToa());
            }
            return new SimpleStringProperty("-");
        });

        colHanhTrinh.setCellValueFactory(cell -> {
            if (cell.getValue().getChiTietLichTrinh() != null && 
                cell.getValue().getChiTietLichTrinh().getLichTrinh() != null &&
                cell.getValue().getChiTietLichTrinh().getLichTrinh().getTuyenDuong() != null) {
                String di = cell.getValue().getChiTietLichTrinh().getLichTrinh().getTuyenDuong().getDiemDi().getViTriGa();
                String den = cell.getValue().getChiTietLichTrinh().getLichTrinh().getTuyenDuong().getDiemDen().getViTriGa();
                return new SimpleStringProperty(di + " -> " + den);
            }
            return new SimpleStringProperty("-");
        });
        
        colNgayDi.setCellValueFactory(cell -> {
            if (cell.getValue().getChiTietLichTrinh() != null && 
                cell.getValue().getChiTietLichTrinh().getLichTrinh() != null &&
                // THÊM DÒNG NÀY: Kiểm tra ngày giờ khởi hành có null không
                cell.getValue().getChiTietLichTrinh().getLichTrinh().getNgayGioKhoiHanhFormatted() != null) {
                   
                System.out.println("Ngay gio khoi hanh: " + cell.getValue().getChiTietLichTrinh().getLichTrinh().getNgayGioKhoiHanhFormatted());
                
                return new SimpleStringProperty(
                    cell.getValue().getChiTietLichTrinh().getLichTrinh().getNgayGioKhoiHanhFormatted()
                );
            }
            return new SimpleStringProperty("-");
        });
        
        colGhe.setCellValueFactory(cell -> {
            if (cell.getValue().getChiTietLichTrinh() != null && 
                cell.getValue().getChiTietLichTrinh().getCho() != null) {
                return new SimpleStringProperty("G" + cell.getValue().getChiTietLichTrinh().getCho().getSoThuTu());
            }
            return new SimpleStringProperty("-");
        });
        
        colGiaVe.setCellValueFactory(cell -> {
            if (cell.getValue().getChiTietLichTrinh() != null) {
                return new SimpleStringProperty(moneyFormatter.format(cell.getValue().getChiTietLichTrinh().getGiaChoNgoi()));
            }
            return new SimpleStringProperty("0 VNĐ");
        });

        // Sự kiện chọn dòng
        tblDanhSachVe.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) handleChonVe(newVal);
            else clearDetailView();
        });

        txtSearchCCCD.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) handleTimKiem();
        });
        
        paneChiTietTra.setDisable(true);
    }

    @FXML
    private void handleTimKiem() {
        String identifier = txtSearchCCCD.getText().trim();
        if (identifier.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Vui lòng nhập CCCD/Hộ chiếu.");
            return;
        }

        // 1. Tìm Khách Hàng
        KhachHang kh = khachHangDAO.timKhachHangTheoGiayTo(identifier);
        if (kh == null) {
            tblDanhSachVe.setItems(FXCollections.emptyObservableList());
            showAlert(Alert.AlertType.INFORMATION, "Không tìm thấy khách hàng với thông tin này.");
            return;
        }

        // 2. Lấy danh sách vé từ SP
        // Model cũ của bạn maKhachHang là String, mình parse về Int để gọi SP
        int maKH = 0;
        try {
            maKH = Integer.parseInt(kh.getMaKhachHang());
        } catch (NumberFormatException e) {
            System.err.println("Lỗi parse ID khách hàng: " + kh.getMaKhachHang());
        }

        List<VeTau> listVe = veTauDAO.getLichSuVeCuaKhachHang(maKH);

        if (listVe == null || listVe.isEmpty()) {
            tblDanhSachVe.setItems(FXCollections.emptyObservableList());
            showAlert(Alert.AlertType.INFORMATION, "Khách hàng này chưa mua vé nào.");
        } else {
            // Lọc: Chỉ hiển thị vé 'DaBan' hoặc 'DaDoi' (Vé có thể trả)
            List<VeTau> filteredList = listVe.stream()
                .filter(v -> "DaBan".equals(v.getTrangThai()) || "DaDoi".equals(v.getTrangThai()))
                .toList();
            tblDanhSachVe.setItems(FXCollections.observableArrayList(filteredList));
        }
        
        clearDetailView();
    }

    private void handleChonVe(VeTau ve) {
        this.selectedVe = ve;
        paneChiTietTra.setDisable(false);
        lblThongBaoLoi.setText("");
        btnXacNhanTra.setDisable(true);

        lblMaVeChon.setText(ve.getMaVe());
        double giaVeHienTai = ve.getChiTietLichTrinh().getGiaChoNgoi();
        lblGiaVeGoc.setText(moneyFormatter.format(giaVeHienTai));

        // 1. Tính thời gian còn lại
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime gioKhoiHanh = ve.getChiTietLichTrinh().getLichTrinh().getNgayGioKhoiHanh();
        long hoursDiff = ChronoUnit.HOURS.between(now, gioKhoiHanh);

        lblThoiGianConLai.setText(hoursDiff + " giờ");

        // 2. Kiểm tra điều kiện (< 4h)
        if (hoursDiff < 4) {
            lblDieuKienVe.setText("Không đủ điều kiện (Quá hạn)");
            lblDieuKienVe.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            lblThongBaoLoi.setText("Vé chỉ được trả trước giờ tàu chạy tối thiểu 4 giờ.");
            lblPhiTraVe.setText("-");
            lblTienHoanLai.setText("-");
            return;
        }

        // 3. Tính phí
        double tyLeKhauTru = 0;
        String lyDoKhauTru = "";
        boolean isDaDoi = "DaDoi".equalsIgnoreCase(ve.getTrangThai());

        if (isDaDoi) {
            tyLeKhauTru = 0.30;
            lyDoKhauTru = "Vé đã đổi (30%)";
        } else {
            if (hoursDiff >= 4 && hoursDiff < 24) {
                tyLeKhauTru = 0.20;
                lyDoKhauTru = "Trả trước 4h-24h (20%)";
            } else {
                tyLeKhauTru = 0.10;
                lyDoKhauTru = "Trả trước >24h (10%)";
            }
        }

        double phiTraVe = giaVeHienTai * tyLeKhauTru;
        if (phiTraVe < 10000) {
            phiTraVe = 10000;
            lyDoKhauTru += " - Phí tối thiểu";
        }
        phiTraVe = Math.ceil(phiTraVe / 1000.0) * 1000; // Làm tròn

        double tienHoanLai = giaVeHienTai - phiTraVe;
        this.calculatedRefundAmount = tienHoanLai;

        lblDieuKienVe.setText(lyDoKhauTru);
        lblDieuKienVe.setStyle("-fx-text-fill: #2980b9;");
        lblPhiTraVe.setText(moneyFormatter.format(phiTraVe));
        lblTienHoanLai.setText(moneyFormatter.format(tienHoanLai));
        
        btnXacNhanTra.setDisable(false);
    }

    @FXML
    private void handleXacNhanTra() {
        if (selectedVe == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận trả vé");
        confirm.setHeaderText("Xác nhận hoàn tiền cho vé " + selectedVe.getMaVe());
        confirm.setContentText("Số tiền hoàn lại: " + moneyFormatter.format(calculatedRefundAmount));

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // 1. Cập nhật VeTau -> 'DaHuy'
            boolean updateVeSuccess = veTauDAO.updateTrangThaiVe(selectedVe.getMaVe(), "DaHuy");
            
            // 2. Cập nhật Chỗ -> 'ConTrong'
            // Lấy ID chi tiết lịch trình từ DB (vì SP XemVeKhachHang không trả về ID này, 
            // hoặc nếu bạn đã map thì dùng getter)
            // Cách an toàn: gọi DAO lấy ID
            int ctlTrinhId = veTauDAO.getChiTietLichTrinhIdByMaVe(selectedVe.getMaVe());
            boolean updateChoSuccess = false;
            
            if (ctlTrinhId != -1) {
                // DB ver2 yêu cầu N'ConTrong'
                updateChoSuccess = chiTietLichTrinhDAO.updateTrangThaiCho(ctlTrinhId, "ConTrong");
            }
            
            if (updateVeSuccess && updateChoSuccess) {
                showAlert(Alert.AlertType.INFORMATION, "Trả vé thành công! Chỗ ngồi đã được giải phóng.");
                handleTimKiem(); // Refresh
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống: Không thể hoàn tất trả vé.");
            }
        }
    }

    private void clearDetailView() {
        selectedVe = null;
        lblMaVeChon.setText("...");
        lblThoiGianConLai.setText("...");
        lblDieuKienVe.setText("...");
        lblGiaVeGoc.setText("0 VNĐ");
        lblPhiTraVe.setText("0 VNĐ");
        lblTienHoanLai.setText("0 VNĐ");
        lblThongBaoLoi.setText("");
        btnXacNhanTra.setDisable(true);
        paneChiTietTra.setDisable(true);
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}