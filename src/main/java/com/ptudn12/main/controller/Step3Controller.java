/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ptudn12.main.controller;

/**
 *
 * @author fo3cp
 */

//import com.ptudn12.main.Controller.VeTamThoi;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

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
    private DecimalFormat moneyFormatter = new DecimalFormat("#,##0 'VNĐ'");
    
    // Danh sách các controller của từng hàng
    private List<HanhKhachRowController> rowControllers = new ArrayList<>();
    private List<VeTamThoi> allTickets = new ArrayList<>();
    
    // Listeners cho auto-fill
    private ChangeListener<String> autoFillHoTenListener;
    private ChangeListener<String> autoFillGiayToListener;
    private boolean isAutoFillActive = true; 

    public void setMainController(BanVeController mainController) {
        this.mainController = mainController;
    }
    
    @FXML
    public void initialize() {
        // Thêm listeners để TẮT auto-fill nếu người dùng tự nhập
        txtNguoiMuaHoTen.textProperty().addListener((obs, oldVal, newVal) -> {
            if (isAutoFillActive && !newVal.equals(oldVal)) isAutoFillActive = false;
        });
        txtNguoiMuaSoGiayTo.textProperty().addListener((obs, oldVal, newVal) -> {
            if (isAutoFillActive && !newVal.equals(oldVal)) isAutoFillActive = false;
        });
    }
    
    public void initData() {
        // 1. Dọn dẹp
        containerHanhKhach.getChildren().clear();
        rowControllers.clear();

        // 2. Lấy dữ liệu giỏ hàng từ Step 2
        List<VeTamThoi> gioHangDi = (List<VeTamThoi>) mainController.getUserData("gioHang_Di");
        List<VeTamThoi> gioHangVe = (List<VeTamThoi>) mainController.getUserData("gioHang_Ve");

        if (gioHangDi == null || gioHangDi.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Lỗi: Không có vé chiều đi trong giỏ hàng.");
            return;
        }

        // 3. Tạo các hàng hành khách
        boolean isRoundTrip = (gioHangVe != null && !gioHangVe.isEmpty());
        int passengerCount = gioHangDi.size(); // Số hành khách = số vé chiều đi

        // Kiểm tra số lượng vé khứ hồi (nếu có)
        if (isRoundTrip && gioHangDi.size() != gioHangVe.size()) {
             showAlert(Alert.AlertType.WARNING, "Cảnh báo: Số lượng vé đi (" + gioHangDi.size() +
                       ") và vé về (" + gioHangVe.size() + ") không khớp. Mỗi hành khách cần đủ vé đi và về.");
             // TẠM DỪNG: Không được đi tiếp nếu số vé không khớp
             btnTiepTheo.setDisable(true);
             return;
        } else {
             btnTiepTheo.setDisable(false);
        }

        // 4. Tạo các hàng hành khách
        boolean isFirstRow = true;
        for (int i = 0; i < passengerCount; i++) { // Lặp theo số hành khách
            VeTamThoi veDi = gioHangDi.get(i);
            VeTamThoi veVe = null; // Mặc định là null (cho vé 1 chiều)

            // Nếu là khứ hồi, lấy vé về tương ứng
            if (isRoundTrip) {
                veVe = gioHangVe.get(i);
            }

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/hanhkhach-row.fxml"));
                Node rowNode = loader.load();
                HanhKhachRowController rowController = loader.getController();

                // Nạp dữ liệu vào hàng (truyền veDi và veVe (có thể null))
                rowController.setData(veDi, veVe, this);

                containerHanhKhach.getChildren().add(rowNode);
                rowControllers.add(rowController);

                if (isFirstRow) {
                    setupAutoFill(rowController);
                    syncHeaderWidths(rowController);
                    isFirstRow = false;
                }

            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Lỗi khi tải giao diện hàng hành khách:\n" + e.getMessage());
            }
        }
        
        // 5. Cập nhật tổng tiền lần đầu
        updateTongThanhTien();
    }
    
    private void syncHeaderWidths(HanhKhachRowController firstRowController) {
        if (firstRowController == null || headerRow == null) return;

        // Lấy các cột VBox từ hàng đầu tiên
        VBox colHanhKhach = firstRowController.getColumnHanhKhach();
        VBox colChuyenTau = firstRowController.getColumnChuyenTau();
        VBox colChoNgoi = firstRowController.getColumnChoNgoi();
        VBox colGiaVe = firstRowController.getColumnGiaVe();
        VBox colGiamGia = firstRowController.getColumnGiamGia();
        VBox colBaoHiem = firstRowController.getColumnBaoHiem();
        VBox colThanhTien = firstRowController.getColumnThanhTien();

        // Áp dụng độ rộng và HGrow cho header tương ứng
        applyColumnSizing(headerHanhKhach, colHanhKhach);
        applyColumnSizing(headerChuyenTau, colChuyenTau);
        applyColumnSizing(headerChoNgoi, colChoNgoi);
        applyColumnSizing(headerGiaVe, colGiaVe);
        applyColumnSizing(headerGiamGia, colGiamGia);
        applyColumnSizing(headerBaoHiem, colBaoHiem);
        applyColumnSizing(headerThanhTien, colThanhTien);
        
        headerRow.setSpacing(10.0); // Đảm bảo spacing khớp với hàng
    }

    /**
     * Hàm trợ giúp để áp dụng sizing
     */
     private void applyColumnSizing(Label headerLabel, VBox rowColumn) {
         if (headerLabel == null || rowColumn == null) return;

         // Lấy HGrow từ cột VBox của hàng
         Priority hGrow = HBox.getHgrow(rowColumn);

         if (hGrow == Priority.ALWAYS) {
             // Nếu cột hàng co giãn -> header cũng co giãn
             HBox.setHgrow(headerLabel, Priority.ALWAYS);
             headerLabel.setMaxWidth(Double.MAX_VALUE); // Cho phép co giãn tối đa
             headerLabel.setMinWidth(Region.USE_PREF_SIZE); // Ít nhất bằng nội dung
             headerLabel.setPrefWidth(Region.USE_COMPUTED_SIZE); // Xóa prefWidth cũ (nếu có)

         } else {
             // Nếu cột hàng có độ rộng cố định
             double prefWidth = rowColumn.getPrefWidth();
             if (prefWidth > 0) { // Chỉ set nếu prefWidth hợp lệ
                 headerLabel.setPrefWidth(prefWidth);
                 headerLabel.setMinWidth(prefWidth); // Giữ cố định
                 headerLabel.setMaxWidth(prefWidth); // Giữ cố định
             }
              // Đảm bảo header không co giãn
             HBox.setHgrow(headerLabel, Priority.NEVER);
         }
     }

    // Thiết lập tính năng auto-fill
    private void setupAutoFill(HanhKhachRowController firstRowController) {
        isAutoFillActive = true; 
        
        // Xóa listener cũ (nếu có)
        if (autoFillHoTenListener != null) {
            firstRowController.getTxtHoTen().textProperty().removeListener(autoFillHoTenListener);
        }
        if (autoFillGiayToListener != null) {
            firstRowController.getTxtSoGiayTo().textProperty().removeListener(autoFillGiayToListener);
        }

        // Tạo listener mới
        autoFillHoTenListener = (obs, oldVal, newVal) -> {
            if (isAutoFillActive) txtNguoiMuaHoTen.setText(newVal);
        };
        autoFillGiayToListener = (obs, oldVal, newVal) -> {
            if (isAutoFillActive) txtNguoiMuaSoGiayTo.setText(newVal);
        };

        // Gán listener
        firstRowController.getTxtHoTen().textProperty().addListener(autoFillHoTenListener);
        firstRowController.getTxtSoGiayTo().textProperty().addListener(autoFillGiayToListener);
    }
    
    /**
    * Được gọi bởi HanhKhachRowController để yêu cầu hủy vé ở Step 2.
    */
    public void requestCancelTicket(int maCho, boolean isChieuDi) {
        if (mainController != null) {
            // Gọi hàm của BanVeController để chuyển tiếp
            mainController.requestCancelTicketInCart(maCho, isChieuDi);
        } else {
             System.err.println("Step3Controller: mainController bị null, không thể yêu cầu hủy vé.");
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
            txtNguoiMuaEmail.getText().isEmpty() || txtNguoiMuaSDT.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Vui lòng nhập đầy đủ thông tin người mua vé.");
            return;
        }

        // 2. Thu thập dữ liệu
        List<Map<String, Object>> danhSachHanhKhach = new ArrayList<>();
        // Lặp qua từng HÀNG (mỗi hàng là 1 hành khách)
        for (HanhKhachRowController row : rowControllers) {
            Map<String, Object> hanhKhach = new HashMap<>();

            // Lấy thông tin nhập từ UI của hàng đó
            hanhKhach.put("hoTen", row.getHoTen());
            hanhKhach.put("soGiayTo", row.getSoGiayTo());
            hanhKhach.put("doiTuong", row.getDoiTuong()); // LoaiVe
            hanhKhach.put("thanhTien", row.getThanhTien()); // Tổng thành tiền của hàng

            // Lấy thông tin vé gốc từ hàng đó
            hanhKhach.put("veDi", row.getVeDi()); // Vé đi của hành khách này
            hanhKhach.put("veVe", row.getVeVe()); // Vé về của hành khách này (có thể null)

            danhSachHanhKhach.add(hanhKhach);
        }

        Map<String, String> nguoiMuaVe = new HashMap<>();
        nguoiMuaVe.put("hoTen", txtNguoiMuaHoTen.getText());
        nguoiMuaVe.put("soGiayTo", txtNguoiMuaSoGiayTo.getText());
        nguoiMuaVe.put("email", txtNguoiMuaEmail.getText());
        nguoiMuaVe.put("sdt", txtNguoiMuaSDT.getText());

        // 3. Gửi dữ liệu qua MainController
        mainController.setUserData("danhSachHanhKhachDaNhap", danhSachHanhKhach); // List các Map, mỗi Map là 1 hành khách
        mainController.setUserData("thongTinNguoiMua", nguoiMuaVe);
        mainController.setUserData("tongThanhTien", lblTongThanhTien.getText());

        // 4. Chuyển bước
        mainController.loadContent("step-4.fxml");
    }
    
    @FXML 
    private void handleQuayLai() { 
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
