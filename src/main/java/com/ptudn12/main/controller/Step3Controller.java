package com.ptudn12.main.controller;

import com.ptudn12.main.dao.KhachHangDAO;
import com.ptudn12.main.entity.KhachHang;
import com.ptudn12.main.controller.VeTamThoi;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Step3Controller {
    @FXML private ScrollPane scrollPaneHanhKhach;
    @FXML private VBox containerHanhKhach;
    @FXML private Label lblTongThanhTien;
    @FXML private Button btnQuayLai;
    @FXML private Button btnTiepTheo;

    // Thông tin người mua
    @FXML private TextField txtNguoiMuaHoTen;
    @FXML private TextField txtNguoiMuaSoGiayTo;
    @FXML private TextField txtNguoiMuaEmail;
    @FXML private TextField txtNguoiMuaSDT;
    
    @FXML private HBox headerRow;
    @FXML private Label headerHanhKhach;
    @FXML private Label headerChuyenTau;
    @FXML private Label headerChoNgoi;
    @FXML private Label headerGiaVe;
    @FXML private Label headerGiamGia;
    @FXML private Label headerBaoHiem;
    @FXML private Label headerThanhTien;
    
    private BanVeController mainController;
    private final DecimalFormat moneyFormatter = new DecimalFormat("#,##0 'VNĐ'");
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();
    
    // Danh sách các controller của từng hàng
    private final List<HanhKhachRowController> rowControllers = new ArrayList<>();
    
    // Cờ kiểm soát việc auto-fill từ Row 1 xuống Người mua
    private boolean isSyncFromFirstPassenger = true;

    public void setMainController(BanVeController mainController) {
        this.mainController = mainController;
    }
    
    @FXML
    public void initialize() {
        // 1. Tính năng tra cứu người mua (Enter)
        txtNguoiMuaSoGiayTo.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                timThongTinNguoiMua();
            }
        });

        // 2. Logic ngắt đồng bộ: Nếu nhân viên TỰ SỬA ô người mua -> Ngừng auto-fill từ trên xuống
        ChangeListener<String> manualEditListener = (obs, oldVal, newVal) -> {
            // Chỉ ngắt đồng bộ nếu TextField đang được focus (người dùng đang gõ)
            if (txtNguoiMuaHoTen.isFocused() || txtNguoiMuaSoGiayTo.isFocused()) {
                isSyncFromFirstPassenger = false;
            }
        };
        
        txtNguoiMuaHoTen.textProperty().addListener(manualEditListener);
        txtNguoiMuaSoGiayTo.textProperty().addListener(manualEditListener);
    }
    
    public void initData() {
        // 1. Dọn dẹp
        containerHanhKhach.getChildren().clear();
        rowControllers.clear();
        isSyncFromFirstPassenger = true; // Reset cờ khi vào lại màn hình

        // 2. Lấy dữ liệu giỏ hàng từ Step 2
        List<VeTamThoi> gioHangDi = (List<VeTamThoi>) mainController.getUserData("gioHang_Di");
        List<VeTamThoi> gioHangVe = (List<VeTamThoi>) mainController.getUserData("gioHang_Ve");

        if (gioHangDi == null || gioHangDi.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Lỗi: Không có vé chiều đi trong giỏ hàng.");
            return;
        }

        // 3. Kiểm tra số lượng vé khứ hồi
        boolean isRoundTrip = (gioHangVe != null && !gioHangVe.isEmpty());
        int passengerCount = gioHangDi.size();

        if (isRoundTrip && gioHangDi.size() != gioHangVe.size()) {
             showAlert(Alert.AlertType.WARNING, "Cảnh báo: Số lượng vé đi (" + gioHangDi.size() + ") và vé về (" + gioHangVe.size() + ") không khớp.");
             btnTiepTheo.setDisable(true);
             return;
        } else {
             btnTiepTheo.setDisable(false);
        }

        // 4. Tạo các hàng hành khách
        boolean isFirstRow = true;
    
        for (int i = 0; i < passengerCount; i++) { 
            VeTamThoi veDi = gioHangDi.get(i);
            VeTamThoi veVe = isRoundTrip ? gioHangVe.get(i) : null;

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/hanhkhach-row.fxml"));
                Node rowNode = loader.load();
                HanhKhachRowController rowController = loader.getController();

                rowController.setData(veDi, veVe, this);

                // --- LOGIC AUTO-FILL CẢI TIẾN (FINAL VERSION) ---
                if (i == 0) {
                    // 1. Listener cho HỌ TÊN: Gõ đến đâu, dưới nhảy đến đó (An toàn vì không query DB)
                    rowController.getTxtHoTen().textProperty().addListener((obs, oldVal, newVal) -> {
                        if (isSyncFromFirstPassenger) {
                            txtNguoiMuaHoTen.setText(newVal);
                        }
                    });

                    // 2. Listener cho SỐ GIẤY TỜ (CCCD): CHỈ COPY CHỮ, KHÔNG TRA CỨU
                    rowController.getTxtSoGiayTo().textProperty().addListener((obs, oldVal, newVal) -> {
                        if (isSyncFromFirstPassenger) {
                            txtNguoiMuaSoGiayTo.setText(newVal);
                        }
                    });

                    // 3. Sự kiện ENTER: Lúc này mới tra cứu DB và điền SĐT/Email
                    rowController.getTxtSoGiayTo().setOnKeyPressed(event -> {
                        if (event.getCode() == KeyCode.ENTER) {
                            String currentId = rowController.getTxtSoGiayTo().getText().trim();
                            KhachHang kh = khachHangDAO.timKhachHangTheoGiayTo(currentId);
                            if (kh != null) {
                                // Điền tên khách tìm được vào ô Họ tên của dòng 1
                                rowController.getTxtHoTen().setText(kh.getTenKhachHang());
                            }

                            if (isSyncFromFirstPassenger) {
                                // Tìm kiếm chủ đích cho người mua
                                timThongTinNguoiMua(false); 
                            }
                        }
                    });
                    

                    // Đồng bộ lần đầu (nếu có sẵn dữ liệu từ vé đã chọn)
                    syncFirstPassengerToBuyer(rowController);
                }

                containerHanhKhach.getChildren().add(rowNode);
                rowControllers.add(rowController);

                if (isFirstRow) {
                    syncHeaderWidths(rowController);
                    isFirstRow = false;
                }

            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Lỗi tải giao diện hàng hành khách: " + e.getMessage());
            }
        }

        updateTongThanhTien();
    }
    
    // --- HÀM ĐỒNG BỘ TỪ KHÁCH #1 -> NGƯỜI MUA ---
    private void syncFirstPassengerToBuyer(HanhKhachRowController firstRow) {
        if (!isSyncFromFirstPassenger) return;

        String hoTen = firstRow.getHoTen();
        String giayTo = firstRow.getSoGiayTo();

        // Chỉ copy nếu các trường có dữ liệu
        if (hoTen != null && !hoTen.isEmpty()) {
            txtNguoiMuaHoTen.setText(hoTen);
        }
        if (giayTo != null && !giayTo.isEmpty()) {
            txtNguoiMuaSoGiayTo.setText(giayTo);
            // Có thể tự động tìm email/sdt nếu muốn
            timThongTinNguoiMua(); 
        }
    }

    private void timThongTinNguoiMua(boolean isSilent) {
        String soGiayTo = txtNguoiMuaSoGiayTo.getText().trim();
        if (soGiayTo.isEmpty()) return;

        KhachHang kh = khachHangDAO.timKhachHangTheoGiayTo(soGiayTo);
        
        if (kh != null) {
            // Tìm thấy: Luôn điền thông tin
            // Nếu đang sync, điền tên từ DB (ưu tiên DB nếu khớp số giấy tờ)
            if (isSyncFromFirstPassenger || txtNguoiMuaHoTen.getText().isEmpty()) {
                txtNguoiMuaHoTen.setText(kh.getTenKhachHang());
            }
            
            txtNguoiMuaSDT.setText(kh.getSoDienThoai());
            String email = khachHangDAO.getEmailKhachHang(soGiayTo);
            txtNguoiMuaEmail.setText(email);
        } else {
            // Không tìm thấy:
            if (!isSilent) {
                // Nếu người dùng chủ động ấn Enter (không phải silent), có thể báo lỗi hoặc clear
                // Nhưng UX tốt nhất là KHÔNG làm gì cả để họ tự nhập tiếp
            }
            
            // QUAN TRỌNG: Nếu là khách mới (kh == null), TUYỆT ĐỐI KHÔNG XÓA 
            // những gì người dùng đang gõ ở ô Tên.
            // Chỉ clear SĐT/Email để họ nhập mới
            if (!isSilent) { // Chỉ clear khi tìm kiếm chủ đích, còn đang gõ thì cứ để yên
                 txtNguoiMuaSDT.clear();
                 txtNguoiMuaEmail.clear();
            }
        }
    }

    // Nạp chồng (Overload) hàm cũ để tương thích với các lệnh gọi cũ (như sự kiện Enter)
    private void timThongTinNguoiMua() {
        timThongTinNguoiMua(false); // Mặc định là không silent (tìm kiếm chủ đích)
    }

    // --- HÀM BINDING ĐỘ RỘNG MỚI ---
    private void syncHeaderWidths(HanhKhachRowController firstRowController) {
        if (firstRowController == null || headerRow == null) return;
        
        // Bind độ rộng của Header Label theo VBox cột tương ứng của dòng đầu tiên
        // Khi cửa sổ resize -> VBox resize -> Header Label resize theo
        bindWidth(headerHanhKhach, firstRowController.getColumnHanhKhach());
        bindWidth(headerChuyenTau, firstRowController.getColumnChuyenTau());
        bindWidth(headerChoNgoi, firstRowController.getColumnChoNgoi());
        bindWidth(headerGiaVe, firstRowController.getColumnGiaVe());
        bindWidth(headerGiamGia, firstRowController.getColumnGiamGia());
        bindWidth(headerBaoHiem, firstRowController.getColumnBaoHiem());
        bindWidth(headerThanhTien, firstRowController.getColumnThanhTien());
        
        headerRow.setSpacing(10.0);
    }
    
    private void bindWidth(Label headerLabel, VBox rowColumn) {
        if (headerLabel == null || rowColumn == null) return;
        // Ràng buộc 2 chiều: header theo row
        headerLabel.prefWidthProperty().bind(rowColumn.widthProperty());
        headerLabel.minWidthProperty().bind(rowColumn.minWidthProperty());
        headerLabel.maxWidthProperty().bind(rowColumn.maxWidthProperty());
    }

     private void applyColumnSizing(Label headerLabel, VBox rowColumn) {
         if (headerLabel == null || rowColumn == null) return;
         Priority hGrow = HBox.getHgrow(rowColumn);
         if (hGrow == Priority.ALWAYS) {
             HBox.setHgrow(headerLabel, Priority.ALWAYS);
             headerLabel.setMaxWidth(Double.MAX_VALUE);
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

    // Hàm này được gọi bởi HanhKhachRowController
    public void updateTongThanhTien() {
        double tong = 0;
        for (HanhKhachRowController row : rowControllers) {
            tong += row.getThanhTien();
        }
        lblTongThanhTien.setText(moneyFormatter.format(tong));
    }
    
    public void requestCancelTicket(int maCho, boolean isChieuDi) {
        if (mainController != null) {
            mainController.requestCancelTicketInCart(maCho, isChieuDi);
        }
    }

    @FXML
    private void handleTiepTheo() {
        // 1. Validation
        for (HanhKhachRowController row : rowControllers) {
            if (row.getHoTen().isEmpty() || row.getSoGiayTo().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Vui lòng nhập đầy đủ thông tin (Họ tên, Số giấy tờ) cho tất cả hành khách.");
                return;
            }
        }
        
        if (txtNguoiMuaHoTen.getText().isEmpty() || txtNguoiMuaSoGiayTo.getText().isEmpty() ||
            txtNguoiMuaSDT.getText().isEmpty()) { // Email có thể optional tùy quy định
            showAlert(Alert.AlertType.WARNING, "Vui lòng nhập đầy đủ thông tin người mua vé.");
            return;
        }

        // 2. Thu thập dữ liệu
        List<Map<String, Object>> danhSachHanhKhach = new ArrayList<>();
        for (HanhKhachRowController row : rowControllers) {
            Map<String, Object> hanhKhach = new HashMap<>();
            hanhKhach.put("hoTen", row.getHoTen());
            hanhKhach.put("soGiayTo", row.getSoGiayTo());
            hanhKhach.put("doiTuong", row.getDoiTuong()); 
            hanhKhach.put("thanhTien", row.getThanhTien()); 
            hanhKhach.put("veDi", row.getVeDi()); 
            hanhKhach.put("veVe", row.getVeVe()); 
            danhSachHanhKhach.add(hanhKhach);
        }

        Map<String, String> nguoiMuaVe = new HashMap<>();
        nguoiMuaVe.put("tenKhachHang", txtNguoiMuaHoTen.getText());
        nguoiMuaVe.put("soGiayToIdentifier", txtNguoiMuaSoGiayTo.getText());
        nguoiMuaVe.put("email", txtNguoiMuaEmail.getText());
        nguoiMuaVe.put("soDienThoai", txtNguoiMuaSDT.getText());

        // 3. Gửi dữ liệu qua MainController
        mainController.setUserData("danhSachHanhKhachDaNhap", danhSachHanhKhach);
        mainController.setUserData("thongTinNguoiMua", nguoiMuaVe);
        mainController.setUserData("tongThanhTien", lblTongThanhTien.getText());

        // 4. Chuyển bước
        mainController.loadContent("step-4.fxml");
    }
    
    @FXML private void handleQuayLai() { mainController.loadContent("step-2.fxml"); }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setTitle("Thông báo");
        alert.setContentText(message);
        alert.showAndWait();
    }
}