package com.ptudn12.main.controller;

import com.ptudn12.main.dao.KhachHangDAO;
import com.ptudn12.main.entity.KhachHang;
import com.ptudn12.main.controller.VeTamThoi;
import com.ptudn12.main.entity.VeTau;
import com.ptudn12.main.enums.LoaiVe;
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
import java.time.LocalDate;
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
        isSyncFromFirstPassenger = true; 

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
             showAlert(Alert.AlertType.WARNING, "Cảnh báo: Số lượng vé đi và vé về không khớp.");
             btnTiepTheo.setDisable(true);
             return;
        } else {
             btnTiepTheo.setDisable(false);
        }
        
        // Lấy dữ liệu tạm
        List<Map<String, Object>> tempData = (List<Map<String, Object>>) mainController.getUserData("TEMP_STEP3_DATA");
        Map<String, String> tempBuyer = (Map<String, String>) mainController.getUserData("TEMP_STEP3_BUYER");

        // Khôi phục người mua
        if (tempBuyer != null) {
            txtNguoiMuaHoTen.setText(tempBuyer.get("ten"));
            txtNguoiMuaSoGiayTo.setText(tempBuyer.get("cccd"));
            txtNguoiMuaSDT.setText(tempBuyer.get("sdt"));
            txtNguoiMuaEmail.setText(tempBuyer.get("email"));
            isSyncFromFirstPassenger = false; 
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
                
                // Khôi phục dữ liệu cho từng dòng
                if (tempData != null && i < tempData.size()) {
                    Map<String, Object> data = tempData.get(i);

                    // Khôi phục Tên & ID
                    if (data.get("hoTen") != null) rowController.getTxtHoTen().setText((String) data.get("hoTen"));
                    if (data.get("soGiayTo") != null) rowController.getTxtSoGiayTo().setText((String) data.get("soGiayTo"));

                    // Khôi phục Loại Vé (Đối tượng)
                    if (data.get("doiTuong") != null) {
                        LoaiVe savedLoaiVe = (LoaiVe) data.get("doiTuong");
                        rowController.getComboDoiTuong().setValue(savedLoaiVe);
                    }

                    if (data.get("ngaySinh") != null) {
                        LocalDate savedDob = (LocalDate) data.get("ngaySinh");
                        rowController.setNgaySinh(savedDob);
                    }
                }

                // --- LOGIC AUTO-FILL CẢI TIẾN (FINAL VERSION) ---
                if (i == 0) {
                    rowController.getTxtHoTen().textProperty().addListener((obs, oldVal, newVal) -> {
                        if (isSyncFromFirstPassenger) txtNguoiMuaHoTen.setText(newVal);
                    });
                    rowController.getTxtSoGiayTo().textProperty().addListener((obs, oldVal, newVal) -> {
                        if (isSyncFromFirstPassenger) txtNguoiMuaSoGiayTo.setText(newVal);
                    });
                    rowController.getTxtSoGiayTo().setOnKeyPressed(event -> {
                        if (event.getCode() == KeyCode.ENTER) {
                            String currentId = rowController.getTxtSoGiayTo().getText().trim();
                            KhachHang kh = khachHangDAO.timKhachHangTheoGiayTo(currentId);
                            if (kh != null) rowController.getTxtHoTen().setText(kh.getTenKhachHang());
                            if (isSyncFromFirstPassenger) timThongTinNguoiMua(false); 
                        }
                    });
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
        
        // --- LOGIC ĐỔI VÉ ---
        String mode = (String) mainController.getUserData("transactionType");
        if (BanVeController.MODE_DOI_VE.equals(mode)) {
            VeTau veCu = (VeTau) mainController.getUserData("veCuCanDoi");
            
            if (veCu != null) {
                // A. FILL THÔNG TIN NGƯỜI ĐI (HÀNH KHÁCH - Dòng đầu tiên)
                // Vì đổi vé chỉ làm việc với 1 vé (hoặc 1 cặp vé) của 1 người, 
                // nên ta chỉ fill vào row đầu tiên (index 0)
                if (!rowControllers.isEmpty()) {
                    HanhKhachRowController row = rowControllers.get(0);
                    KhachHang khachDi = veCu.getKhachHang();

                    if (khachDi != null) {
                        // Fill Họ tên
                        if (row.getTxtHoTen() != null) {
                            row.getTxtHoTen().setText(khachDi.getTenKhachHang());
                            row.getTxtHoTen().setDisable(true); // Khóa
                        }
                        
                        // Fill Giấy tờ
                        String giayTo = (khachDi.getSoCCCD() != null && !khachDi.getSoCCCD().isEmpty()) 
                                        ? khachDi.getSoCCCD() 
                                        : khachDi.getHoChieu();
                        
                        if (row.getTxtSoGiayTo() != null) {
                            row.getTxtSoGiayTo().setText(giayTo);
                            row.getTxtSoGiayTo().setDisable(true); // Khóa
                        }
                        
                        ComboBox<LoaiVe> comboDoiTuong = row.getComboDoiTuong(); 

                        if (comboDoiTuong != null) {
                            LoaiVe loaiVeCu = veCu.getLoaiVe();

                            if (loaiVeCu != null) {
                                comboDoiTuong.setValue(loaiVeCu);

                                // Khóa lại
                                comboDoiTuong.setDisable(true);
                                comboDoiTuong.setStyle("-fx-opacity: 1; -fx-text-fill: black;");
                            }
                        }
                    }
                }

                // B. FILL THÔNG TIN NGƯỜI MUA (NGƯỜI ĐẠI DIỆN)
                // Tìm người mua gốc (A)
                KhachHang nguoiMua = khachHangDAO.getNguoiMuaByMaVe(veCu.getMaVe());
                
                // Nếu ko tìm thấy người mua gốc thì lấy chính người đi
                if (nguoiMua == null) nguoiMua = veCu.getKhachHang();

                if (nguoiMua != null) {
                    txtNguoiMuaHoTen.setText(nguoiMua.getTenKhachHang());
                    
                    String giayToMua = (nguoiMua.getSoCCCD() != null && !nguoiMua.getSoCCCD().isEmpty()) 
                                       ? nguoiMua.getSoCCCD() : nguoiMua.getHoChieu();
                    txtNguoiMuaSoGiayTo.setText(giayToMua);
                    
                    // Email
                    String email = khachHangDAO.getEmailKhachHang(giayToMua);
                    txtNguoiMuaEmail.setText(email);
                    txtNguoiMuaSDT.setText(nguoiMua.getSoDienThoai());
                }

                // --- KHÓA INPUT NGƯỜI MUA ---
                txtNguoiMuaHoTen.setDisable(true);
                txtNguoiMuaSoGiayTo.setDisable(true);
                txtNguoiMuaEmail.setDisable(true);
                txtNguoiMuaSDT.setDisable(true);
            }
        } 
        else {
            // --- LOGIC BÁN VÉ THƯỜNG: Mở khóa ---
            txtNguoiMuaHoTen.setDisable(false);
            txtNguoiMuaSoGiayTo.setDisable(false);
            txtNguoiMuaEmail.setDisable(false);
            txtNguoiMuaSDT.setDisable(false);
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
        // 1. VALIDATION DANH SÁCH HÀNH KHÁCH
        for (HanhKhachRowController row : rowControllers) {
            String hoTenHK = row.getHoTen().trim();
            String giayToHK = row.getSoGiayTo().trim();

            // 1.1 Check rỗng
            if (hoTenHK.isEmpty() || giayToHK.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Vui lòng nhập đầy đủ thông tin (Họ tên, Số giấy tờ) cho tất cả hành khách.");
                return;
            }

            // 1.2 Check định dạng Tên Hành Khách (Chỉ chữ và khoảng trắng)
            if (!hoTenHK.matches("^[\\p{L}\\s]+$")) {
                showAlert(Alert.AlertType.WARNING, "Họ tên hành khách không hợp lệ (chứa số hoặc ký tự đặc biệt):\n" + hoTenHK);
                return;
            }
            
            // 1.3 Check định dạng Giấy tờ Hành Khách (Chỉ số, 9-12 ký tự)
            // (Nếu chấp nhận Hộ chiếu có chữ thì sửa regex này, ở đây giả sử là CCCD/CMND)
            boolean isCMND = giayToHK.matches("\\d{9}");
            boolean isCCCD = giayToHK.matches("\\d{12}");
            boolean isPassport = giayToHK.matches("^[a-zA-Z0-9]{6,15}$");
            if (!isCMND && !isCCCD && !isPassport) {
                showAlert(Alert.AlertType.WARNING, "Giấy tờ hành khách không hợp lệ!\nPhải là:\n- CMND (9 số)\n- CCCD (12 số)\n- Hoặc Hộ chiếu (6-15 ký tự chữ/số)");
                return;
            }
        }
        
        // 2. VALIDATION NGƯỜI MUA VÉ
        String tenMua = txtNguoiMuaHoTen.getText().trim();
        String cccdMua = txtNguoiMuaSoGiayTo.getText().trim();
        String sdtMua = txtNguoiMuaSDT.getText().trim();
        String emailMua = txtNguoiMuaEmail.getText().trim();

        // 2.1 Check rỗng
        if (tenMua.isEmpty() || cccdMua.isEmpty() || sdtMua.isEmpty()) { 
            showAlert(Alert.AlertType.WARNING, "Vui lòng nhập đầy đủ thông tin người mua vé (Họ tên, CCCD, SĐT).");
            return;
        }

        // 2.2 Check Tên Người Mua
        if (!tenMua.matches("^[\\p{L}\\s]+$")) {
             showAlert(Alert.AlertType.WARNING, "Họ tên người mua không được chứa số hoặc ký tự đặc biệt.");
             return;
        }

        // 2.3 Check CCCD Người Mua (9 hoặc 12 số)
        if (!cccdMua.matches("\\d{9}") && !cccdMua.matches("\\d{12}")) {
            showAlert(Alert.AlertType.WARNING, "Số giấy tờ người mua phải là 9 hoặc 12 số.");
            txtNguoiMuaSoGiayTo.requestFocus();
            return;
        }

        // 2.4 Check Số Điện Thoại (Bắt đầu bằng 0, 10 số)
        if (!sdtMua.matches("^0\\d{9}$")) {
            showAlert(Alert.AlertType.WARNING, "Số điện thoại không hợp lệ (Phải bắt đầu bằng 0 và có 10 số).");
            txtNguoiMuaSDT.requestFocus();
            return;
        }

        // 2.5 Check Email
        if (!emailMua.isEmpty()) {
            String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
            if (!emailMua.matches(emailRegex)) {
                showAlert(Alert.AlertType.WARNING, "Email không đúng định dạng (Ví dụ: abc@gmail.com).");
                txtNguoiMuaEmail.requestFocus();
                return;
            }
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
    
    @FXML
    private void handleQuayLai() {
        // --- FIX: Lưu tạm dữ liệu form hiện tại vào cache ---
        List<Map<String, Object>> tempData = new ArrayList<>();

        for (HanhKhachRowController row : rowControllers) {
            Map<String, Object> rowData = new HashMap<>();
            rowData.put("hoTen", row.getHoTen());
            rowData.put("soGiayTo", row.getSoGiayTo());

            // Lưu Loại Vé (Đối tượng) - Quan trọng!
            // Lưu ý: row.getDoiTuong() trả về Enum LoaiVe
            rowData.put("doiTuong", row.getDoiTuong()); 

            // Lưu ngày sinh (nếu có - dùng cho Trẻ em/Người già)
            if (row.getNgaySinh() != null) {
                rowData.put("ngaySinh", row.getNgaySinh());
            }

            tempData.add(rowData);
        }
        mainController.setUserData("TEMP_STEP3_DATA", tempData);

        // Lưu thông tin người mua (Giữ nguyên)
        Map<String, String> tempBuyer = new HashMap<>();
        tempBuyer.put("ten", txtNguoiMuaHoTen.getText());
        tempBuyer.put("cccd", txtNguoiMuaSoGiayTo.getText());
        tempBuyer.put("sdt", txtNguoiMuaSDT.getText());
        tempBuyer.put("email", txtNguoiMuaEmail.getText());
        mainController.setUserData("TEMP_STEP3_BUYER", tempBuyer);
        // ---------------------------------------------------

        mainController.loadContent("step-2.fxml");
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setTitle("Thông báo");
        alert.setContentText(message);
        alert.showAndWait();
    }
}