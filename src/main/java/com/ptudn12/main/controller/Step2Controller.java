/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ptudn12.main.controller;

/**
 *
 * @author fo3cp
 */

import com.ptudn12.main.dao.ChiTietLichTrinhDAO;
import com.ptudn12.main.dao.ChiTietToaDAO;
import com.ptudn12.main.entity.ChiTietToa;
import com.ptudn12.main.entity.LichTrinh;
import com.ptudn12.main.entity.Toa;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import com.ptudn12.main.controller.VeTamThoi;
import com.ptudn12.main.enums.LoaiCho;

import java.util.*;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;

public class Step2Controller {
    @FXML private VBox toaSection;
    @FXML private Label labelTenTau;
    @FXML private Button btnQuayLai;
    @FXML private Button btnMuaVe;
    @FXML private Label labelTenTauDi;
    @FXML private Label labelKhoiHanhDi;
    @FXML private Label labelDenNoiDi;
    @FXML private VBox boxThongTinVe;
    @FXML private Label labelTenTauVe;
    @FXML private Label labelKhoiHanhVe;
    @FXML private Label labelDenNoiVe;
    @FXML private VBox cartBox; // VBox chứa giỏ vé
    private Label labelTongTien;
    private VeTamThoi veTamThoi;
    
    // --- CSS Styles ---
    private final String STYLE_TRONG = "-fx-background-color: white; -fx-border-color: black; -fx-border-width: 0.5px; -fx-text-fill: black;";
    private final String STYLE_DABAN = "-fx-background-color: #D90000; -fx-text-fill: white;";
    private final String STYLE_DANGCHON = "-fx-background-color: #008000; -fx-text-fill: white;";

    // DAOs
    private final ChiTietToaDAO chiTietToaDAO = new ChiTietToaDAO();
    private final ChiTietLichTrinhDAO chiTietLichTrinhDAO = new ChiTietLichTrinhDAO();

    // LƯU Ý: Hai danh sách chọn chỗ riêng biệt
    private final Set<Integer> danhSachChoDaChon_Di = new HashSet<>();
    private final Set<Integer> danhSachChoDaChon_Ve = new HashSet<>();
    private final Map<Integer, VeTamThoi> gioHang_Di = new HashMap<>();
    private final Map<Integer, VeTamThoi> gioHang_Ve = new HashMap<>();
    
    // Helpers
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final DecimalFormat moneyFormatter = new DecimalFormat("#,##0 'VND'");
    
    // Biến lưu trữ Label tiêu đề (tạo động)
    private Label lblChieuDiHeader = null;
    private Label lblChieuVeHeader = null;
    
    private BanVeController mainController;
    public void setMainController(BanVeController mainController) {
        this.mainController = mainController;
    }
    
    private static class ToaInfo implements Comparable<ToaInfo> {
        private final Integer maToa;
        private final String loaiToa;

        public ToaInfo(Toa toa) {
            this.maToa = toa.getMaToa();
            this.loaiToa = toa.getLoaiToa().getDescription();
        }

        public Integer getMaToa() { return maToa; }

        @Override
        public String toString() {
            return String.format("Toa %d - %s", maToa, loaiToa);
        }

        @Override
        public int compareTo(ToaInfo o) {
            return this.maToa.compareTo(o.maToa);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            ToaInfo toaInfo = (ToaInfo) obj;
            return maToa.equals(toaInfo.maToa);
        }

        @Override
        public int hashCode() {
            return Objects.hash(maToa);
        }
    }
    
    public void initData() {
        LichTrinh lichTrinhDi = (LichTrinh) mainController.getUserData("lichTrinhChieuDi");
        LichTrinh lichTrinhVe = (LichTrinh) mainController.getUserData("lichTrinhChieuVe");
        
        toaSection.getChildren().clear();
        
        // Cập nhật UI giỏ hàng (vẽ lại trạng thái rỗng)
        updateCartUI(); 
        
        // Hiển thị box Thông tin chuyến tàu
        if (lichTrinhDi != null) {
            // Hiển thị thông tin chiều đi
            labelTenTauDi.setText("Chiều đi: Tàu " + lichTrinhDi.getTau().getMacTau());
            labelKhoiHanhDi.setText(lichTrinhDi.getNgayGioKhoiHanh().format(formatter));
            labelDenNoiDi.setText(lichTrinhDi.getNgayGioDen().format(formatter));

            // Nếu là khứ hồi, hiển thị thông tin chiều về
            if (lichTrinhVe != null) {
                 labelTenTauVe.setText("Chiều về: Tàu " + lichTrinhVe.getTau().getMacTau());
                 labelKhoiHanhVe.setText(lichTrinhVe.getNgayGioKhoiHanh().format(formatter));
                 labelDenNoiVe.setText(lichTrinhVe.getNgayGioDen().format(formatter));
                 // Hiện VBox thông tin về
                 boxThongTinVe.setVisible(true);
                 boxThongTinVe.setManaged(true);
            } else {
                 // Ẩn VBox thông tin về nếu là vé 1 chiều
                 boxThongTinVe.setVisible(false);
                 boxThongTinVe.setManaged(false);
            }
        } else {
            // Xử lý lỗi không có lịch trình đi
             labelTenTauDi.setText("Lỗi - Không có lịch trình đi");
             labelKhoiHanhDi.setText("N/A");
             labelDenNoiDi.setText("N/A");
             // Ẩn box thông tin về
             boxThongTinVe.setVisible(false);
             boxThongTinVe.setManaged(false);
        }
        
        // Hiển thị box Chọn toa và ghế
        if (lichTrinhVe != null) {
            VBox blockChieuDi = createSeatSelectionBlock(lichTrinhDi, true, "Chọn chỗ Chiều đi: Tàu " + lichTrinhDi.getTau().getMacTau());
            VBox blockChieuVe = createSeatSelectionBlock(lichTrinhVe, false, "Chọn chỗ Chiều về: Tàu " + lichTrinhVe.getTau().getMacTau());
            toaSection.getChildren().addAll(blockChieuDi, blockChieuVe);
        } else if (lichTrinhDi != null) {
            VBox blockChieuDi = createSeatSelectionBlock(lichTrinhDi, true, "Chọn chỗ: Tàu " + lichTrinhDi.getTau().getMacTau());
            toaSection.getChildren().add(blockChieuDi);
        } else {
            toaSection.getChildren().add(new Label("Không có thông tin lịch trình để tải sơ đồ ghế."));
        }
    }
    
    private VBox createSeatSelectionBlock(LichTrinh lichTrinh, boolean isChieuDi, String title) {
        String maLichTrinh = lichTrinh.getMaLichTrinh();
        String maTau = lichTrinh.getTau().getMacTau();
        
        List<ChiTietToa> dsChiTiet = chiTietToaDAO.getChiTietToaByTau(maTau);
        Set<Integer> danhSachChoDaBan = chiTietLichTrinhDAO.getCacChoDaBan(maLichTrinh);

        VBox blockBox = new VBox(15); 
        blockBox.setPadding(new Insets(10));
        blockBox.getStyleClass().add("seat-selection-section");

        Label lblTitle = new Label(title);
        lblTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333;");

        ComboBox<ToaInfo> comboToa = new ComboBox<>();
        comboToa.setPrefWidth(250.0);
        comboToa.setPromptText("Chọn toa để xem sơ đồ");

        GridPane gridSeats = new GridPane();
        gridSeats.setHgap(6);
        gridSeats.setVgap(6);
        gridSeats.setPadding(new Insets(5));

        HBox comboContainer = new HBox(10, new Label("Chọn toa:"), comboToa);
        comboContainer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        if (dsChiTiet.isEmpty()) {
            blockBox.getChildren().addAll(lblTitle, new Label("Không có dữ liệu chỗ ngồi cho tàu này."));
            return blockBox;
        }

        List<ToaInfo> danhSachToaInfo = dsChiTiet.stream()
                .map(ChiTietToa::getToa)
                .distinct()
                .map(ToaInfo::new)
                .sorted()
                .collect(Collectors.toList());
        
        comboToa.setItems(FXCollections.observableArrayList(danhSachToaInfo));

        comboToa.setOnAction(e -> {
            ToaInfo selectedToaInfo = comboToa.getValue(); 
            if (selectedToaInfo == null) {
                gridSeats.getChildren().clear();
                return;
            }
            
            List<ChiTietToa> danhSachChoCuaToa = dsChiTiet.stream()
                .filter(ct -> ct.getToa().getMaToa().equals(selectedToaInfo.getMaToa()))
                .collect(Collectors.toList());
            
            // Vẽ lại GridPane
            populateGridPane(gridSeats, danhSachChoCuaToa, danhSachChoDaBan, isChieuDi, lichTrinh);
        });

        blockBox.getChildren().addAll(lblTitle, comboContainer, gridSeats);
        return blockBox;
    }
    
    /**
     * TÁCH HÀM: Tạo và style một nút ghế
     */
    private Button createSeatButton(ChiTietToa ct, Set<Integer> soldSet, Set<Integer> selectedSet, boolean isChieuDi, LichTrinh lichTrinh) {
        Button btn = new Button(String.valueOf(ct.getSoThuTu()));
        btn.setPrefSize(40, 40);

        if (soldSet.contains(ct.getCho().getMaCho())) {
            btn.setStyle(STYLE_DABAN);
            btn.setDisable(true);
        } else {
            if (selectedSet.contains(ct.getCho().getMaCho())) {
                btn.setStyle(STYLE_DANGCHON);
            } else {
                btn.setStyle(STYLE_TRONG);
            }
            btn.setOnAction(ev -> handleChonCho(btn, ct, isChieuDi, lichTrinh));
        }
        return btn;
    }


    /**
     * VIẾT LẠI: "Vẽ" các nút ghế lên GridPane theo layout linh hoạt
     */
    private void populateGridPane(GridPane grid, List<ChiTietToa> seats, Set<Integer> soldSet, boolean isChieuDi, LichTrinh lichTrinh) {
        grid.getChildren().clear();

        // 1. Sắp xếp ghế theo số thứ tự (QUAN TRỌNG)
        seats.sort(Comparator.comparing(ChiTietToa::getSoThuTu));
        
        int totalSeats = seats.size();
        if (totalSeats == 0) return;

        // 2. Định nghĩa hằng số layout
        final int NUM_ROWS_PER_BLOCK = 2; // 2 hàng cho Q1, 2 hàng cho Q2...
        final int H_GAP_ROW_INDEX = 2;    // Vị trí bắt đầu của Q2/Q4 (sau 2 hàng và 0 gap)
        final int V_GAP_COL_WIDTH = 2;    // Chiều rộng (số cột) của lối đi dọc

        // 3. Tính toán phân bổ ghế "công bằng"
        int seatsLeft = (int) Math.ceil(totalSeats / 2.0);
        int seatsRight = totalSeats - seatsLeft;

        int q1Seats = (int) Math.ceil(seatsLeft / 2.0);
        int q2Seats = seatsLeft - q1Seats;
        int q3Seats = (int) Math.ceil(seatsRight / 2.0);
        int q4Seats = seatsRight - q3Seats;

        // 4. Tính toán số cột LINH HOẠT (xx cột)
        int colsQ1 = (int) Math.ceil((double) q1Seats / NUM_ROWS_PER_BLOCK);
        int colsQ2 = (int) Math.ceil((double) q2Seats / NUM_ROWS_PER_BLOCK);
        int colsLeft = Math.max(colsQ1, colsQ2); 

        int colsQ3 = (int) Math.ceil((double) q3Seats / NUM_ROWS_PER_BLOCK);
        int colsQ4 = (int) Math.ceil((double) q4Seats / NUM_ROWS_PER_BLOCK);
        int colsRight = Math.max(colsQ3, colsQ4); 
        
        if (q1Seats == 0 && q2Seats == 0) colsLeft = 0;
        if (q3Seats == 0 && q4Seats == 0) colsRight = 0;

        // 5. Vẽ ghế
        for (ChiTietToa ct : seats) {
            Set<Integer> selectedSet = isChieuDi ? danhSachChoDaChon_Di : danhSachChoDaChon_Ve;
            Button btn = createSeatButton(ct, soldSet, selectedSet, isChieuDi, lichTrinh);
            
            int soThuTu = ct.getSoThuTu();
            int relativeSeatIndex = soThuTu - 1; // Bắt đầu từ 0

            int finalRow, finalCol;
            int seatInQuadrant;
            int rowInQuad, colInQuad;

            if (relativeSeatIndex < q1Seats) {
                // --- Quadrant 1 (Top-Left) ---
                seatInQuadrant = relativeSeatIndex;
                rowInQuad = seatInQuadrant / colsLeft;
                colInQuad = seatInQuadrant % colsLeft;
                finalRow = rowInQuad;
                finalCol = colInQuad;

            } else if (relativeSeatIndex < q1Seats + q2Seats) {
                // --- Quadrant 2 (Bottom-Left) ---
                seatInQuadrant = relativeSeatIndex - q1Seats;
                rowInQuad = seatInQuadrant / colsLeft;
                colInQuad = seatInQuadrant % colsLeft;
                finalRow = rowInQuad + H_GAP_ROW_INDEX; 
                finalCol = colInQuad;
                
            } else if (relativeSeatIndex < q1Seats + q2Seats + q3Seats) {
                // --- Quadrant 3 (Top-Right) ---
                seatInQuadrant = relativeSeatIndex - q1Seats - q2Seats;
                rowInQuad = seatInQuadrant / colsRight;
                colInQuad = seatInQuadrant % colsRight;
                finalRow = rowInQuad;
                finalCol = colInQuad + colsLeft + V_GAP_COL_WIDTH; 
                
            } else {
                // --- Quadrant 4 (Bottom-Right) ---
                seatInQuadrant = relativeSeatIndex - q1Seats - q2Seats - q3Seats;
                rowInQuad = seatInQuadrant / colsRight;
                colInQuad = seatInQuadrant % colsRight;
                finalRow = rowInQuad + H_GAP_ROW_INDEX;
                finalCol = colInQuad + colsLeft + V_GAP_COL_WIDTH;
            }
            
            grid.add(btn, finalCol, finalRow);
        }
    }

    // --- HÀM TÍNH GIÁ VÉ ---
    private double calculateTicketPrice(LichTrinh lichTrinh, ChiTietToa chiTietToa) {
        try {
            double giaTuyenCoBan = lichTrinh.getTuyenDuong().tinhGiaCoBan();
            LoaiCho loaiCho = chiTietToa.getCho().getLoaiCho();
            
            // GIẢ ĐỊNH: Enum LoaiCho của bạn có hàm getHeSoChoNgoi()
            double heSoCho = loaiCho.getHeSoChoNgoi(); 
            
            return giaTuyenCoBan * heSoCho + 2000;
        } catch (Exception e) {
            System.err.println("Lỗi tính giá vé: " + e.getMessage());
            e.printStackTrace();
            return 0.0;
        }
    }

    // --- HÀM XỬ LÝ CHỌN/BỎ CHỌN GHẾ ---
    private void handleChonCho(Button btn, ChiTietToa ct, boolean isChieuDi, LichTrinh lichTrinh) {
        Map<Integer, VeTamThoi> gioHang = isChieuDi ? gioHang_Di : gioHang_Ve;
        Set<Integer> selectedSet = isChieuDi ? danhSachChoDaChon_Di : danhSachChoDaChon_Ve;
        int maCho = ct.getCho().getMaCho();

        if (gioHang.containsKey(maCho)) {
            handleHuyVe(gioHang.get(maCho));
        } else {
            // --- CHỌN ---
            double giaVe = calculateTicketPrice(lichTrinh, ct);
            VeTamThoi ve = new VeTamThoi(lichTrinh, ct, giaVe, btn, isChieuDi);
            Node cardNode = createTicketCard(ve);
            ve.setCardNode(cardNode); 
            gioHang.put(maCho, ve);
            selectedSet.add(maCho);
            btn.setStyle(STYLE_DANGCHON);
            
            int insertIndex; // Vị trí để chèn card vé
            if (isChieuDi) {
                // Nếu chưa có header "Chiều đi", tạo và thêm nó
                if (lblChieuDiHeader == null) {
                    lblChieuDiHeader = new Label("Chiều đi");
                    lblChieuDiHeader.setStyle("-fx-font-weight: bold; -fx-padding: 8 0 2 0;");
                    // Chèn ngay sau label "Giỏ vé" (index 1)
                    cartBox.getChildren().add(1, lblChieuDiHeader);
                }
                // Chèn card vé ngay sau header "Chiều đi"
                insertIndex = cartBox.getChildren().indexOf(lblChieuDiHeader) + 1;
            } else { // Chiều về
                // Nếu chưa có header "Chiều về", tạo và thêm nó
                if (lblChieuVeHeader == null) {
                    lblChieuVeHeader = new Label("Chiều về");
                    lblChieuVeHeader.setStyle("-fx-font-weight: bold; -fx-padding: 8 0 2 0;");
                    // Chèn ngay sau header "Chiều đi" (hoặc label "Giỏ vé" nếu chỉ có vé về)
                    int baseIndex = (lblChieuDiHeader != null) ? cartBox.getChildren().indexOf(lblChieuDiHeader) : 0;
                    // Đếm số vé chiều đi để chèn header "Chiều về" vào đúng chỗ
                    int countDi = gioHang_Di.size();
                    cartBox.getChildren().add(baseIndex + countDi + 1, lblChieuVeHeader);
                }
                 // Chèn card vé ngay sau header "Chiều về"
                insertIndex = cartBox.getChildren().indexOf(lblChieuVeHeader) + 1;
            }
            // Chèn card vé vào vị trí đã xác định
            cartBox.getChildren().add(insertIndex, cardNode);
            updateTongTien();
        }
    }

    // --- HÀM TẠO CARD VÉ TRONG GIỎ HÀNG ---
    private Node createTicketCard(VeTamThoi ve) {
        HBox card = new HBox(10);
        card.setPadding(new Insets(8));
        card.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #ddd; -fx-border-radius: 4; -fx-background-radius: 4;");
        
        VBox infoBox = new VBox(5);
        Label lblTenTau = new Label("Tàu " + ve.getLichTrinh().getTau().getMacTau());
        lblTenTau.setStyle("-fx-font-weight: bold;");
        Label lblThoiGian = new Label(ve.getLichTrinh().getNgayGioKhoiHanh().format(formatter));
        Label lblToaCho = new Label("Toa " + ve.getChiTietToa().getToa().getMaToa() + " - Ghế " + ve.getChiTietToa().getSoThuTu());
        infoBox.getChildren().addAll(lblTenTau, lblThoiGian, lblToaCho);
        
        VBox priceBox = new VBox(5);
        priceBox.setAlignment(Pos.CENTER_RIGHT);
        Label lblGia = new Label(moneyFormatter.format(ve.getGiaVe()));
        lblGia.setStyle("-fx-font-weight: bold; -fx-text-fill: #c0392b;");
        
        Button btnXoa = new Button("X");
        btnXoa.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 2 6;");
        btnXoa.setOnAction(e -> handleHuyVe(ve));
        
        priceBox.getChildren().addAll(btnXoa, lblGia);
        
        HBox.setHgrow(infoBox, Priority.ALWAYS); 
        card.getChildren().addAll(infoBox, priceBox);
        return card;
    }

    // --- HÀM XỬ LÝ HỦY VÉ (NÚT X) ---
    private void handleHuyVe(VeTamThoi ve) {
        Map<Integer, VeTamThoi> gioHang = ve.isChieuDi() ? gioHang_Di : gioHang_Ve;
        Set<Integer> selectedSet = ve.isChieuDi() ? danhSachChoDaChon_Di : danhSachChoDaChon_Ve;

        gioHang.remove(ve.getMaCho());
        selectedSet.remove(ve.getMaCho());

        cartBox.getChildren().remove(ve.getCardNode());
        ve.getSeatButton().setStyle(STYLE_TRONG);
        
        if (ve.getCardNode() != null && cartBox != null) {
         cartBox.getChildren().remove(ve.getCardNode());
        }
        if (ve.getSeatButton() != null) {
             ve.getSeatButton().setStyle(STYLE_TRONG);
              ve.getSeatButton().setDisable(false);
        } else {
             System.err.println("Lỗi: Không tìm thấy SeatButton để đổi màu khi hủy vé ghế " + ve.getMaCho());
        }

        // --- XÓA LABEL HEADER NẾU HẾT VÉ ---
        if (ve.isChieuDi() && gioHang_Di.isEmpty() && lblChieuDiHeader != null) {
            cartBox.getChildren().remove(lblChieuDiHeader);
            lblChieuDiHeader = null; // Reset biến
        } else if (!ve.isChieuDi() && gioHang_Ve.isEmpty() && lblChieuVeHeader != null) {
            cartBox.getChildren().remove(lblChieuVeHeader);
            lblChieuVeHeader = null; // Reset biến
        }

        updateTongTien();
    }

    // --- HÀM CẬP NHẬT UI GIỎ HÀNG ---
    private void updateCartUI() {
        // Chỉ xóa card vé, không xóa header
        // Header được quản lý bởi handleChonCho và handleHuyVe
        List<Node> nodesToRemove = new ArrayList<>();
        for(Node node : cartBox.getChildren()){
            // Kiểm tra xem node có phải là card vé không (dựa vào style class hoặc kiểu HBox)
            // Cách đơn giản nhất là kiểm tra xem nó có phải là Label header không
            if(node != lblChieuDiHeader && node != lblChieuVeHeader && node != labelTongTien && !(node instanceof Label && ((Label)node).getText().equals("Giỏ vé")) && !(node instanceof Button)){
                 nodesToRemove.add(node);
            }
        }
        cartBox.getChildren().removeAll(nodesToRemove);

        // Thêm lại các card từ giỏ hàng (nếu có) - Cần đảm bảo thứ tự đúng
        int insertIndex = 1; // Sau Label "Giỏ vé"
        if (lblChieuDiHeader != null) {
            insertIndex++; // Bỏ qua header đi
            for (VeTamThoi ve : gioHang_Di.values()) {
                cartBox.getChildren().add(insertIndex++, ve.getCardNode());
            }
        }
        if (lblChieuVeHeader != null) {
             insertIndex = cartBox.getChildren().indexOf(lblChieuVeHeader) + 1; // Tìm vị trí sau header về
            for (VeTamThoi ve : gioHang_Ve.values()) {
                cartBox.getChildren().add(insertIndex++, ve.getCardNode());
            }
        }

        updateTongTien();
    }

    // --- HÀM CẬP NHẬT TỔNG TIỀN ---
    private void updateTongTien() {
        double tong = 0;
        for (VeTamThoi ve : gioHang_Di.values()) {
            tong += ve.getGiaVe();
        }
        for (VeTamThoi ve : gioHang_Ve.values()) {
            tong += ve.getGiaVe();
        }
        
        if (labelTongTien != null) {
            labelTongTien.setText("Tổng tiền: " + moneyFormatter.format(tong));
        }
    }
    
    public void cancelTicketBySeatId(int maCho, boolean isChieuDi) {
    Map<Integer, VeTamThoi> gioHang = isChieuDi ? gioHang_Di : gioHang_Ve;
    VeTamThoi veToCancel = gioHang.get(maCho);

    if (veToCancel != null) {
        // Gọi hàm hủy vé nội bộ để đảm bảo logic nhất quán
        handleHuyVe(veToCancel);
    } else {
        System.err.println("Step2Controller: Không tìm thấy vé để hủy cho ghế " + maCho + (isChieuDi ? " (Đi)" : " (Về)"));
    }
}

    // --- HÀM CHUYỂN BƯỚC (NÚT MUA VÉ) ---
    @FXML
    private void handleTiepTheo() {
        if (gioHang_Di.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Vui lòng chọn ít nhất một chỗ cho chiều đi.");
            return;
        }
        
        LichTrinh lichTrinhVe = (LichTrinh) mainController.getUserData("lichTrinhChieuVe");
        if (lichTrinhVe != null && gioHang_Ve.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Vui lòng chọn ít nhất một chỗ cho chiều về.");
            return;
        }

        // --- Lưu dữ liệu giỏ hàng vào MainController ---
        mainController.setUserData("gioHang_Di", new ArrayList<>(gioHang_Di.values()));
        mainController.setUserData("gioHang_Ve", new ArrayList<>(gioHang_Ve.values()));

        // Chuyển sang Step 3
        mainController.loadContent("step-3.fxml");
    }

    // --- HÀM QUAY LẠI ---
    @FXML 
    private void handleQuayLai() { 
        mainController.loadContent("step-1.fxml"); 
    }
    
    // --- HÀM HIỂN THỊ THÔNG BÁO ---
    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setTitle("Thông báo");
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    @FXML
    private void initialize() {
        // Tự tạo Label tổng tiền vì FXML không có
        labelTongTien = new Label("Tổng tiền: 0 VNĐ");
        labelTongTien.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #333;");
        labelTongTien.setPadding(new Insets(10, 0, 5, 0)); // Thêm khoảng đệm
        
        // FXML: cartBox có [Label("Giỏ vé"), Button("Mua vé")]
        // Thêm labelTongTien vào giữa (tại vị trí index 1)
        if (cartBox != null) {
            cartBox.getChildren().add(1, labelTongTien);
        } else {
            System.err.println("Lỗi FXML: cartBox bị null, không thể thêm Label tổng tiền.");
        }
    }
}
