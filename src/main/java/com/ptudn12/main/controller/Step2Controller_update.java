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
import com.ptudn12.main.entity.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;

import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Step2Controller_update {
    @FXML private VBox toaSection;
    @FXML private Label labelTenTauDi;
    @FXML private Label labelKhoiHanhDi;
    @FXML private Label labelDenNoiDi;
    @FXML private VBox boxThongTinVe;
    @FXML private Label labelTenTauVe;
    @FXML private Label labelKhoiHanhVe;
    @FXML private Label labelDenNoiVe;
    @FXML private VBox ticketListContainer;
    @FXML private Label labelTongTien;
    @FXML private Button btnMuaVe;
    @FXML private Button btnQuayLai;

    // --- CSS Styles ---
    private final String STYLE_TRONG = "-fx-background-color: white; -fx-border-color: black; -fx-border-width: 0.5px; -fx-text-fill: black;";
    private final String STYLE_DABAN = "-fx-background-color: #D90000; -fx-text-fill: white;";
    private final String STYLE_DANGCHON = "-fx-background-color: #008000; -fx-text-fill: white;";

    // DAOs
    private final ChiTietToaDAO chiTietToaDAO = new ChiTietToaDAO();
    private final ChiTietLichTrinhDAO chiTietLichTrinhDAO = new ChiTietLichTrinhDAO();

    // Dữ liệu giỏ hàng
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
    
    // Biến lưu nút xóa tất cả (trong giỏ vé)
    private Button btnClearAllDi = null;
    private Button btnClearAllVe = null;
    private ToaInfo selectedToa = null;

    private BanVeController mainController;
    public void setMainController(BanVeController mainController) {
        this.mainController = mainController;
    }

    private static class ToaInfo implements Comparable<ToaInfo> {
        private final Integer maToa;
        private final String loaiToa;
        public ToaInfo(Toa toa) { this.maToa = toa.getMaToa(); this.loaiToa = toa.getLoaiToa().getDescription(); }
        public Integer getMaToa() { return maToa; }
        @Override public String toString() { return String.format("Toa %d - %s", maToa, loaiToa); }
        @Override public int compareTo(ToaInfo o) { return this.maToa.compareTo(o.maToa); }
        @Override public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            ToaInfo toaInfo = (ToaInfo) obj;
            return maToa.equals(toaInfo.maToa);
        }
        @Override public int hashCode() { return Objects.hash(maToa); }
    }

    public void initData() {
        toaSection.getChildren().clear();
        
        // 1. Reset sạch sẽ giao diện và dữ liệu cục bộ để tránh trùng lặp
        toaSection.getChildren().clear();
        gioHang_Di.clear();
        danhSachChoDaChon_Di.clear();
        gioHang_Ve.clear();
        danhSachChoDaChon_Ve.clear();
        
        // 2. Khôi phục dữ liệu giỏ hàng từ MainController (FIX LỖI MẤT GIỎ HÀNG)
        List<VeTamThoi> savedGioHangDi = (List<VeTamThoi>) mainController.getUserData("gioHang_Di");
        if (savedGioHangDi != null) {
            for (VeTamThoi v : savedGioHangDi) {
                gioHang_Di.put(v.getChiTietToa().getCho().getMaCho(), v);
                danhSachChoDaChon_Di.add(v.getChiTietToa().getCho().getMaCho());
            }
        }
        
        LichTrinh lichTrinhVe = (LichTrinh) mainController.getUserData("lichTrinhChieuVe"); 
        List<VeTamThoi> savedGioHangVe = (List<VeTamThoi>) mainController.getUserData("gioHang_Ve");
        if (lichTrinhVe != null && savedGioHangVe != null) {
            for (VeTamThoi v : savedGioHangVe) {
                gioHang_Ve.put(v.getChiTietToa().getCho().getMaCho(), v);
                danhSachChoDaChon_Ve.add(v.getChiTietToa().getCho().getMaCho());
            }
        }
        
        // 3. Cập nhật giao diện giỏ hàng bên phải ngay lập tức
        updateCartUI();
        
        this.selectedToa = (ToaInfo) mainController.getUserData("step2_selectedToa");
        
        // -------------------------------------------------------------
        LichTrinh lichTrinhDi = (LichTrinh) mainController.getUserData("lichTrinhChieuDi");
        if (lichTrinhDi != null) {
            labelTenTauDi.setText("Chiều đi: Tàu " + lichTrinhDi.getTau().getMacTau());
            labelKhoiHanhDi.setText(lichTrinhDi.getNgayGioKhoiHanh().format(formatter));
            labelDenNoiDi.setText(lichTrinhDi.getNgayGioDen().format(formatter));

            if (lichTrinhVe != null) {
                labelTenTauVe.setText("Chiều về: Tàu " + lichTrinhVe.getTau().getMacTau());
                labelKhoiHanhVe.setText(lichTrinhVe.getNgayGioKhoiHanh().format(formatter));
                labelDenNoiVe.setText(lichTrinhVe.getNgayGioDen().format(formatter));
                boxThongTinVe.setVisible(true);
                boxThongTinVe.setManaged(true);
            } else {
                boxThongTinVe.setVisible(false);
                boxThongTinVe.setManaged(false);
            }
        } else {
            labelTenTauDi.setText("Lỗi - Không có lịch trình đi");
            boxThongTinVe.setVisible(false);
            boxThongTinVe.setManaged(false);
            return; // Không có lịch trình thì dừng luôn
        }
        
        VBox blockChieuDi = createSeatSelectionBlock(lichTrinhDi, true, "Chọn chỗ Chiều đi: Tàu " + lichTrinhDi.getTau().getMacTau());
        toaSection.getChildren().add(blockChieuDi);
        
        // -- Vẽ sơ đồ ghế Chiều về (Nếu có) --
        if (lichTrinhVe != null) {
            VBox blockChieuVe = createSeatSelectionBlock(lichTrinhVe, false, "Chọn chỗ Chiều về: Tàu " + lichTrinhVe.getTau().getMacTau());
            toaSection.getChildren().add(blockChieuVe);
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
        comboToa.setPrefWidth(200.0);
        comboToa.setPromptText("Chọn toa");
        TextField txtSoLuong = new TextField(); txtSoLuong.setPromptText("SL"); txtSoLuong.setPrefWidth(50);
        Button btnChonNhanh = new Button("Chọn nhanh"); btnChonNhanh.getStyleClass().add("btn-action-secondary");
        Button btnUndo = new Button("Hủy chọn (Xóa hết)"); btnUndo.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

        HBox comboContainer = new HBox(10, new Label("Toa:"), comboToa, new Separator(Orientation.VERTICAL), new Label("Chọn nhanh:"), txtSoLuong, btnChonNhanh, btnUndo);
        comboContainer.setAlignment(Pos.CENTER_LEFT);
        GridPane gridSeats = new GridPane();
        gridSeats.setHgap(6); gridSeats.setVgap(6); gridSeats.setPadding(new Insets(5));
        gridSeats.setPrefHeight(120); gridSeats.setMinHeight(120);
        HBox legendBox = createLegendBox();

        if (dsChiTiet.isEmpty()) {
            blockBox.getChildren().addAll(lblTitle, new Label("Không có dữ liệu chỗ ngồi cho tàu này."));
            return blockBox;
        }

        List<ToaInfo> danhSachToaInfo = dsChiTiet.stream().map(ChiTietToa::getToa).distinct().map(ToaInfo::new).sorted().collect(Collectors.toList());
        comboToa.setItems(FXCollections.observableArrayList(danhSachToaInfo));

        comboToa.setOnAction(e -> {
            ToaInfo selectedToaInfo = comboToa.getValue();
            if (selectedToaInfo == null) { gridSeats.getChildren().clear(); return; }
            this.selectedToa = selectedToaInfo; // Lưu lại hành động click cuối cùng
            List<ChiTietToa> danhSachChoCuaToa = dsChiTiet.stream().filter(ct -> ct.getToa().getMaToa().equals(selectedToaInfo.getMaToa())).collect(Collectors.toList());
            populateGridPane(gridSeats, danhSachChoCuaToa, danhSachChoDaBan, isChieuDi, lichTrinh);
        });

        btnChonNhanh.setOnAction(e -> {
            ToaInfo selectedToa = comboToa.getValue();
            if (selectedToa == null) { showAlert(Alert.AlertType.WARNING, "Vui lòng chọn toa trước!"); return; }
            String strSL = txtSoLuong.getText();
            if (strSL == null || strSL.isEmpty() || !strSL.matches("\\d+")) { showAlert(Alert.AlertType.WARNING, "Vui lòng nhập số lượng hợp lệ!"); return; }
            int soLuongCanChon = Integer.parseInt(strSL);
            if (soLuongCanChon <= 0) { showAlert(Alert.AlertType.WARNING, "Số lượng phải > 0!"); return; }
            List<ChiTietToa> currentToaSeats = dsChiTiet.stream().filter(ct -> ct.getToa().getMaToa().equals(selectedToa.getMaToa())).sorted(Comparator.comparing(ChiTietToa::getSoThuTu)).collect(Collectors.toList());
            handleBatchSelection(soLuongCanChon, currentToaSeats, lichTrinh, isChieuDi, gridSeats);
            ToaInfo currentToa = comboToa.getValue();
            if (currentToa != null) {
                 List<ChiTietToa> refreshSeats = dsChiTiet.stream().filter(ct -> ct.getToa().getMaToa().equals(currentToa.getMaToa())).collect(Collectors.toList());
                 populateGridPane(gridSeats, refreshSeats, danhSachChoDaBan, isChieuDi, lichTrinh);
            }
        });

        btnUndo.setOnAction(e -> {
            clearAllTickets(isChieuDi);
            ToaInfo currentToa = comboToa.getValue();
            if (currentToa != null) {
                 List<ChiTietToa> refreshSeats = dsChiTiet.stream().filter(ct -> ct.getToa().getMaToa().equals(currentToa.getMaToa())).collect(Collectors.toList());
                 populateGridPane(gridSeats, refreshSeats, danhSachChoDaBan, isChieuDi, lichTrinh);
            }
        });

        ToaInfo toaCanChon = null;

        // Ưu tiên 1: Kiểm tra xem trong GIỎ HÀNG của chiều này có vé nào không?
        Map<Integer, VeTamThoi> targetCart = isChieuDi ? gioHang_Di : gioHang_Ve;
        if (!targetCart.isEmpty()) {
            // Lấy vé đầu tiên trong giỏ để xem nó thuộc toa nào
            VeTamThoi firstTicket = targetCart.values().iterator().next();
            Integer maToaTrongGio = firstTicket.getChiTietToa().getToa().getMaToa();
            
            // Tìm ToaInfo tương ứng
            for (ToaInfo info : danhSachToaInfo) {
                if (info.getMaToa().equals(maToaTrongGio)) {
                    toaCanChon = info;
                    break;
                }
            }
        }
        
        // Ưu tiên 2: Nếu giỏ rỗng, kiểm tra xem người dùng có vừa click chọn toa nào không (selectedToa)
        if (toaCanChon == null && this.selectedToa != null) {
            for (ToaInfo info : danhSachToaInfo) {
                if (info.getMaToa().equals(this.selectedToa.getMaToa())) {
                    toaCanChon = info;
                    break;
                }
            }
        }

        // Ưu tiên 3: Nếu vẫn null thì chọn toa đầu tiên trong danh sách (Mặc định)
        if (toaCanChon == null && !danhSachToaInfo.isEmpty()) {
            toaCanChon = danhSachToaInfo.get(0);
        }

        // --- THỰC HIỆN CHỌN VÀ VẼ ---
        if (toaCanChon != null) {
            comboToa.setValue(toaCanChon); // Set hiển thị ComboBox
            
            // Gọi hàm vẽ ghế ngay lập tức
            List<ChiTietToa> danhSachChoCuaToa = dsChiTiet.stream()
                    .filter(ct -> ct.getToa().getMaToa().equals(comboToa.getValue().getMaToa()))
                    .collect(Collectors.toList());
            populateGridPane(gridSeats, danhSachChoCuaToa, danhSachChoDaBan, isChieuDi, lichTrinh);
        }

        blockBox.getChildren().addAll(lblTitle, comboContainer, legendBox, gridSeats);
        return blockBox;
    }

    private void handleBatchSelection(int amountNeeded, List<ChiTietToa> allSeatsInToa, LichTrinh lichTrinh, boolean isChieuDi, GridPane gridSeats) {
        Map<Integer, VeTamThoi> currentCart = isChieuDi ? gioHang_Di : gioHang_Ve;

        if (currentCart.size() + amountNeeded > 10) {
            String chieu = isChieuDi ? "chiều đi" : "chiều về";
            showAlert(Alert.AlertType.WARNING, "Mỗi lượt (" + chieu + ") chỉ được mua tối đa 10 vé.\nHiện tại đã có: " + currentCart.size() + " vé.");
            return;
        }

        List<ChiTietToa> seatsToSelect = new ArrayList<>();
        Set<Integer> soldSet = chiTietLichTrinhDAO.getCacChoDaBan(lichTrinh.getMaLichTrinh());
        Set<Integer> currentSelectedSet = isChieuDi ? danhSachChoDaChon_Di : danhSachChoDaChon_Ve;

        for (int i = 0; i < allSeatsInToa.size(); i++) {
            seatsToSelect.clear();
            for (int j = i; j < allSeatsInToa.size(); j++) {
                ChiTietToa seat = allSeatsInToa.get(j);
                int seatId = seat.getCho().getMaCho();

                if (!soldSet.contains(seatId) && !currentSelectedSet.contains(seatId)) {
                    seatsToSelect.add(seat);
                    if (seatsToSelect.size() == amountNeeded) break;
                } else {
                    seatsToSelect.clear();
                    i = j; 
                    break;
                }
            }
            if (seatsToSelect.size() == amountNeeded) break;
        }

        if (seatsToSelect.size() < amountNeeded) {
            showAlert(Alert.AlertType.INFORMATION, "Không tìm thấy " + amountNeeded + " ghế liền kề trống trong toa này.");
            return;
        }

        for (ChiTietToa seat : seatsToSelect) {
            addToCart(seat, isChieuDi, lichTrinh); 
        }
        updateCartUI(); // Cập nhật giỏ hàng 1 lần cuối
    }
    
    private void clearAllTickets(boolean isChieuDi) {
        Map<Integer, VeTamThoi> targetCart = isChieuDi ? gioHang_Di : gioHang_Ve;
        Set<Integer> targetSet = isChieuDi ? danhSachChoDaChon_Di : danhSachChoDaChon_Ve;
        
        // --- BƯỚC 1: Duyệt qua tất cả vé để Reset giao diện (Đổi màu nút về Trắng) ---
        // Phải thực hiện bước này TRƯỚC KHI xóa dữ liệu trong Map
        for (VeTamThoi ve : targetCart.values()) {
            if (ve.getSeatButton() != null) {
                ve.getSeatButton().setStyle(STYLE_TRONG); // Trả về màu trắng
                ve.getSeatButton().setDisable(false);     // Enable lại nếu cần
            }
        }
        
        // --- BƯỚC 2: Xóa dữ liệu trong bộ nhớ ---
        targetCart.clear();
        targetSet.clear();
        
        // --- BƯỚC 3: Cập nhật lại UI giỏ vé ---
        updateCartUI();
    }
    
    private HBox createLegendBox() {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        box.getChildren().addAll(
            new HBox(5, new Rectangle(20, 20, javafx.scene.paint.Color.WHITE) {{ setStroke(javafx.scene.paint.Color.BLACK); }}, new Label("Trống")),
            new HBox(5, new Rectangle(20, 20, javafx.scene.paint.Color.RED), new Label("Đã bán")),
            new HBox(5, new Rectangle(20, 20, javafx.scene.paint.Color.GREEN), new Label("Đang chọn")),
            new Label("| Lưu ý: Tối đa 10 vé/giao dịch")
        );
        return box;
    }

    private Button createSeatButton(ChiTietToa ct, Set<Integer> soldSet, Set<Integer> selectedSet, boolean isChieuDi, LichTrinh lichTrinh) {
        Button btn = new Button(String.valueOf(ct.getSoThuTu()));
        btn.setPrefSize(40, 40);
        if (soldSet.contains(ct.getCho().getMaCho())) {
            btn.setStyle(STYLE_DABAN);
            btn.setDisable(true);
        } else {
            // --- FIX VẤN ĐỀ 1: Kiểm tra trạng thái đã chọn khi vẽ lại ---
            if (selectedSet.contains(ct.getCho().getMaCho())) {
                btn.setStyle(STYLE_DANGCHON);
            } else {
                btn.setStyle(STYLE_TRONG);
            }
            btn.setOnAction(ev -> handleChonCho(btn, ct, isChieuDi, lichTrinh));
        }
        return btn;
    }

    private void populateGridPane(GridPane grid, List<ChiTietToa> seats, Set<Integer> soldSet, boolean isChieuDi, LichTrinh lichTrinh) {
        grid.getChildren().clear();
        seats.sort(Comparator.comparing(ChiTietToa::getSoThuTu));
        if (seats.isEmpty()) return;

        final int NUM_ROWS_PER_BLOCK = 2;
        final int H_GAP_ROW_INDEX = 2;
        final int V_GAP_COL_WIDTH = 2;

        int totalSeats = seats.size();
        int seatsLeft = (int) Math.ceil(totalSeats / 2.0);
        int seatsRight = totalSeats - seatsLeft;
        int q1Seats = (int) Math.ceil(seatsLeft / 2.0);
        int q2Seats = seatsLeft - q1Seats;
        int q3Seats = (int) Math.ceil(seatsRight / 2.0);
        int q4Seats = seatsRight - q3Seats;

        int colsQ1 = (int) Math.ceil((double) q1Seats / NUM_ROWS_PER_BLOCK);
        int colsQ2 = (int) Math.ceil((double) q2Seats / NUM_ROWS_PER_BLOCK);
        int colsLeft = Math.max(colsQ1, colsQ2);
        int colsQ3 = (int) Math.ceil((double) q3Seats / NUM_ROWS_PER_BLOCK);
        int colsQ4 = (int) Math.ceil((double) q4Seats / NUM_ROWS_PER_BLOCK);
        int colsRight = Math.max(colsQ3, colsQ4);

        if (q1Seats == 0 && q2Seats == 0) colsLeft = 0;
        if (q3Seats == 0 && q4Seats == 0) colsRight = 0;

        for (ChiTietToa ct : seats) {
            Button btn = createSeatButton(ct, soldSet, (isChieuDi ? danhSachChoDaChon_Di : danhSachChoDaChon_Ve), isChieuDi, lichTrinh);
            int idx = ct.getSoThuTu() - 1;
            int r = 0, c = 0;
            if (idx < q1Seats) {
                r = (idx / colsLeft); c = (idx % colsLeft);
            } else if (idx < q1Seats + q2Seats) {
                int qIdx = idx - q1Seats; r = (qIdx / colsLeft) + H_GAP_ROW_INDEX; c = (qIdx % colsLeft);
            } else if (idx < q1Seats + q2Seats + q3Seats) {
                int qIdx = idx - q1Seats - q2Seats; r = (qIdx / colsRight); c = (qIdx % colsRight) + colsLeft + V_GAP_COL_WIDTH;
            } else {
                int qIdx = idx - q1Seats - q2Seats - q3Seats; r = (qIdx / colsRight) + H_GAP_ROW_INDEX; c = (qIdx % colsRight) + colsLeft + V_GAP_COL_WIDTH;
            }
            grid.add(btn, c, r);
        }
    }

    private double calculateTicketPrice(LichTrinh lichTrinh, ChiTietToa chiTietToa) {
        try {
            return lichTrinh.getTuyenDuong().tinhGiaCoBan() * chiTietToa.getCho().getLoaiCho().getHeSoChoNgoi() + 2000;
        } catch (Exception e) { return 0.0; }
    }
    
    // Helper method: Add to Cart (Logic only)
    private void addToCart(ChiTietToa ct, boolean isChieuDi, LichTrinh lichTrinh) {
        Map<Integer, VeTamThoi> gioHang = isChieuDi ? gioHang_Di : gioHang_Ve;
        Set<Integer> selectedSet = isChieuDi ? danhSachChoDaChon_Di : danhSachChoDaChon_Ve;
        int maCho = ct.getCho().getMaCho();
        
        if (!gioHang.containsKey(maCho)) {
             double giaVe = calculateTicketPrice(lichTrinh, ct);
             VeTamThoi ve = new VeTamThoi(lichTrinh, ct, giaVe, null, isChieuDi);
             gioHang.put(maCho, ve);
             selectedSet.add(maCho);
        }
    }

    private void handleChonCho(Button btn, ChiTietToa ct, boolean isChieuDi, LichTrinh lichTrinh) {
        Map<Integer, VeTamThoi> gioHang = isChieuDi ? gioHang_Di : gioHang_Ve;
        Set<Integer> selectedSet = isChieuDi ? danhSachChoDaChon_Di : danhSachChoDaChon_Ve;
        int maCho = ct.getCho().getMaCho();

        if (gioHang.containsKey(maCho)) {
            // Bỏ chọn (Hủy)
            gioHang.remove(maCho);
            selectedSet.remove(maCho);
            btn.setStyle(STYLE_TRONG);
        } else {
            // Chọn mới
            if (gioHang.size() >= 10) {
                String chieu = isChieuDi ? "chiều đi" : "chiều về";
                showAlert(Alert.AlertType.WARNING, "Bạn chỉ được chọn tối đa 10 vé cho " + chieu + ".");
                return;
            }
            
            String mode = (String) mainController.getUserData("transactionType");
            if (BanVeController.MODE_DOI_VE.equals(mode)) {
                if (!gioHang.isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "Chế độ Đổi vé chỉ cho phép chọn 1 ghế mới.");
                    return;
                }
            }
            
            double giaVe = calculateTicketPrice(lichTrinh, ct);
            VeTamThoi ve = new VeTamThoi(lichTrinh, ct, giaVe, btn, isChieuDi);
            // ve.setSeatButton(btn); // Lưu button để đổi màu khi hủy từ giỏ
            gioHang.put(maCho, ve);
            selectedSet.add(maCho);
            btn.setStyle(STYLE_DANGCHON);
        }
        updateCartUI(); // Cập nhật giỏ hàng và tổng tiền
    }

    private Node createTicketCard(VeTamThoi ve) {
        HBox card = new HBox(10);
        card.setPadding(new Insets(8));
        card.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #ddd; -fx-border-radius: 4; -fx-background-radius: 4;");
        
        VBox infoBox = new VBox(5);
        Label lblTenTau = new Label("Tàu " + ve.getLichTrinh().getTau().getMacTau());
        lblTenTau.setStyle("-fx-font-weight: bold; -fx-text-fill: black;");
        Label lblThoiGian = new Label(ve.getLichTrinh().getNgayGioKhoiHanh().format(formatter));
        lblThoiGian.setStyle("-fx-text-fill: black;");
        Label lblToaCho = new Label("Toa " + ve.getChiTietToa().getToa().getMaToa() + " - Ghế " + ve.getChiTietToa().getSoThuTu());
        lblToaCho.setStyle("-fx-text-fill: black;");
        infoBox.getChildren().addAll(lblTenTau, lblThoiGian, lblToaCho);

        VBox priceBox = new VBox(5);
        priceBox.setAlignment(Pos.CENTER_RIGHT);
        Label lblGia = new Label(moneyFormatter.format(ve.getGiaVe()));
        lblGia.setStyle("-fx-font-weight: bold; -fx-text-fill: #c0392b;");
        
        Button btnXoa = new Button("X");
        btnXoa.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 10px;");
        btnXoa.setOnAction(e -> {
            // Khi xóa từ giỏ, cần cập nhật lại cả Grid ghế
            // Lấy controller hiện tại để refresh
            handleHuyVeFromCart(ve); 
        });

        priceBox.getChildren().addAll(btnXoa, lblGia);
        HBox.setHgrow(infoBox, Priority.ALWAYS);
        card.getChildren().addAll(infoBox, priceBox);
        return card;
    }

    // Hàm riêng để xử lý xóa từ giỏ (cần refresh grid)
    private void handleHuyVeFromCart(VeTamThoi ve) {
        Map<Integer, VeTamThoi> gioHang = ve.isChieuDi() ? gioHang_Di : gioHang_Ve;
        Set<Integer> selectedSet = ve.isChieuDi() ? danhSachChoDaChon_Di : danhSachChoDaChon_Ve;
        
        gioHang.remove(ve.getMaCho());
        selectedSet.remove(ve.getMaCho());
        
        // Nếu có button liên kết thì đổi màu (cho trường hợp click tay)
        if (ve.getSeatButton() != null) {
            ve.getSeatButton().setStyle(STYLE_TRONG);
        }
        
        updateCartUI();
    }

    private void updateCartUI() {
        ticketListContainer.getChildren().clear();
        lblChieuDiHeader = null;
        lblChieuVeHeader = null;
        btnClearAllDi = null;
        btnClearAllVe = null;

        if (!gioHang_Di.isEmpty()) {
            lblChieuDiHeader = new Label("Chiều đi");
            lblChieuDiHeader.setStyle("-fx-font-weight: bold; -fx-padding: 8 0 2 0; -fx-text-fill: #333;");
            
            btnClearAllDi = new Button("Xóa tất cả");
            btnClearAllDi.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5 10px; -fx-background-color: #c0392b");
            btnClearAllDi.setOnAction(e -> clearAllTickets(true));
            
            HBox headerBox = new HBox(10, lblChieuDiHeader, btnClearAllDi);
            headerBox.setAlignment(Pos.CENTER_LEFT);
            ticketListContainer.getChildren().add(headerBox);
            
            for (VeTamThoi ve : gioHang_Di.values()) {
                ve.setCardNode(createTicketCard(ve)); // Tạo card mới mỗi lần update
                ticketListContainer.getChildren().add(ve.getCardNode());
            }
        }
        
        if (!gioHang_Ve.isEmpty()) {
            lblChieuVeHeader = new Label("Chiều về");
            lblChieuVeHeader.setStyle("-fx-font-weight: bold; -fx-padding: 8 0 2 0; -fx-text-fill: #333;");
            
            btnClearAllVe = new Button("Xóa tất cả");
            btnClearAllVe.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5 10px; -fx-background-color: #c0392b");
            btnClearAllVe.setOnAction(e -> clearAllTickets(false));
            
            HBox headerBox = new HBox(10, lblChieuVeHeader, btnClearAllVe);
            headerBox.setAlignment(Pos.CENTER_LEFT);
            ticketListContainer.getChildren().add(headerBox);
            
            for (VeTamThoi ve : gioHang_Ve.values()) {
                ve.setCardNode(createTicketCard(ve));
                ticketListContainer.getChildren().add(ve.getCardNode());
            }
        }
        
        // --- FIX VẤN ĐỀ 2: Gọi updateTongTien() cuối cùng ---
        updateTongTien();
    }

    private void updateTongTien() {
        double tong = 0;
        for (VeTamThoi ve : gioHang_Di.values()) tong += ve.getGiaVe();
        for (VeTamThoi ve : gioHang_Ve.values()) tong += ve.getGiaVe();
        if (labelTongTien != null) labelTongTien.setText("Tổng tiền: " + moneyFormatter.format(tong));
    }

    public void cancelTicketBySeatId(int maCho, boolean isChieuDi) {
        Map<Integer, VeTamThoi> gioHang = isChieuDi ? gioHang_Di : gioHang_Ve;
        VeTamThoi ve = gioHang.get(maCho);
        if (ve != null) handleHuyVeFromCart(ve);
    }

    @FXML
    private void handleTiepTheo() {
        if (gioHang_Di.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Vui lòng chọn ít nhất một chỗ cho chiều đi.");
            return;
        }
        LichTrinh lichTrinhVe = (LichTrinh) mainController.getUserData("lichTrinhChieuVe");
        if (lichTrinhVe != null) {
            if (gioHang_Ve.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Vui lòng chọn ít nhất một chỗ cho chiều về.");
                return;
            }
            if (gioHang_Di.size() != gioHang_Ve.size()) {
                showAlert(Alert.AlertType.WARNING, "Số lượng vé không tương xứng.\nVui lòng chọn số lượng vé bằng nhau cho cả hai chiều.");
                return;
            }
        }
        mainController.setUserData("gioHang_Di", new ArrayList<>(gioHang_Di.values()));
        mainController.setUserData("gioHang_Ve", new ArrayList<>(gioHang_Ve.values()));
        if (this.selectedToa != null) {
            mainController.setUserData("step2_selectedToa", this.selectedToa);
        }
        mainController.loadContent("step-3.fxml");
    }

    @FXML private void handleQuayLai() { mainController.loadContent("step-1.fxml"); }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void initialize() {
        // Label tổng tiền đã có trong FXML (bọc trong VBox) nên không cần new Label
        // Nếu FXML chưa có thì code này sẽ lỗi, hãy đảm bảo FXML step-2 có labelTongTien
    }
}