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
    
    // Container nằm trong ScrollPane của giỏ vé
    @FXML private VBox ticketListContainer;
    
    // Label tổng tiền đã có trong FXML
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

    // --- MỚI: Biến lưu trữ danh sách ghế vừa chọn hàng loạt để Undo ---
    private final List<Integer> recentBatchSeats_Di = new ArrayList<>();
    private final List<Integer> recentBatchSeats_Ve = new ArrayList<>();

    // Helpers
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final DecimalFormat moneyFormatter = new DecimalFormat("#,##0 'VND'");

    // Biến lưu trữ Label tiêu đề (tạo động)
    private Label lblChieuDiHeader = null;
    private Label lblChieuVeHeader = null;
    
    // Biến lưu nút xóa tất cả (trong giỏ vé)
    private Button btnClearAllDi = null;
    private Button btnClearAllVe = null;

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
        LichTrinh lichTrinhDi = (LichTrinh) mainController.getUserData("lichTrinhChieuDi");
        LichTrinh lichTrinhVe = (LichTrinh) mainController.getUserData("lichTrinhChieuVe");

        toaSection.getChildren().clear();
        updateCartUI();

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
        }

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
        
        blockBox.setMinHeight(300);
        
        Label lblTitle = new Label(title);
        lblTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333;");

        // --- UI Control (Giữ nguyên logic cũ) ---
        ComboBox<ToaInfo> comboToa = new ComboBox<>();
        comboToa.setPrefWidth(200.0);
        comboToa.setPromptText("Chọn toa");

        TextField txtSoLuong = new TextField();
        txtSoLuong.setPromptText("SL");
        txtSoLuong.setPrefWidth(50);
        
        Button btnChonNhanh = new Button("Chọn nhanh");
        btnChonNhanh.getStyleClass().add("btn-action-secondary"); 
        
        Button btnUndo = new Button("Hủy chọn");
        btnUndo.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white;");
        btnUndo.setDisable(true);

        HBox comboContainer = new HBox(10,
                new Label("Toa:"), comboToa,
                new Separator(Orientation.VERTICAL),
                new Label("Chọn nhanh:"), txtSoLuong, btnChonNhanh, btnUndo
        );
        comboContainer.setAlignment(Pos.CENTER_LEFT);

        // --- GRID SƠ ĐỒ GHẾ ---
        GridPane gridSeats = new GridPane();
        gridSeats.setHgap(6);
        gridSeats.setVgap(6);
        gridSeats.setPadding(new Insets(5));
        
        // Chú thích màu
        HBox legendBox = createLegendBox();

        // Xử lý dữ liệu rỗng
        if (dsChiTiet.isEmpty()) {
            blockBox.getChildren().addAll(lblTitle, new Label("Không có dữ liệu chỗ ngồi cho tàu này."));
            return blockBox;
        }

        // Load dữ liệu ComboBox (Giữ nguyên)
        List<ToaInfo> danhSachToaInfo = dsChiTiet.stream()
                .map(ChiTietToa::getToa).distinct().map(ToaInfo::new).sorted().collect(Collectors.toList());
        comboToa.setItems(FXCollections.observableArrayList(danhSachToaInfo));

        // Sự kiện chọn toa
        comboToa.setOnAction(e -> {
            ToaInfo selectedToaInfo = comboToa.getValue();
            if (selectedToaInfo == null) {
                gridSeats.getChildren().clear(); return;
            }
            List<ChiTietToa> danhSachChoCuaToa = dsChiTiet.stream()
                    .filter(ct -> ct.getToa().getMaToa().equals(selectedToaInfo.getMaToa()))
                    .collect(Collectors.toList());
            populateGridPane(gridSeats, danhSachChoCuaToa, danhSachChoDaBan, isChieuDi, lichTrinh);
        });
        
        // Sự kiện Chọn Nhanh (Giữ nguyên logic đã fix limit 10 vé)
        btnChonNhanh.setOnAction(e -> {
            ToaInfo selectedToa = comboToa.getValue();
            if (selectedToa == null) {
                showAlert(Alert.AlertType.WARNING, "Vui lòng chọn toa trước!"); return;
            }
            String strSL = txtSoLuong.getText();
            if (strSL == null || strSL.isEmpty() || !strSL.matches("\\d+")) {
                showAlert(Alert.AlertType.WARNING, "Vui lòng nhập số lượng hợp lệ!"); return;
            }
            int soLuongCanChon = Integer.parseInt(strSL);
            if (soLuongCanChon <= 0) {
                 showAlert(Alert.AlertType.WARNING, "Số lượng phải > 0!"); return;
            }
            List<ChiTietToa> currentToaSeats = dsChiTiet.stream()
                    .filter(ct -> ct.getToa().getMaToa().equals(selectedToa.getMaToa()))
                    .sorted(Comparator.comparing(ChiTietToa::getSoThuTu))
                    .collect(Collectors.toList());
            handleBatchSelection(soLuongCanChon, currentToaSeats, lichTrinh, isChieuDi, gridSeats, btnUndo);
        });
        
        // Sự kiện Undo (Giữ nguyên)
        btnUndo.setOnAction(e -> {
            handleUndoBatchSelection(isChieuDi);
            btnUndo.setDisable(true);
        });

        // --- THAY ĐỔI Ở ĐÂY: Add trực tiếp Grid, KHÔNG BỌC ScrollPane, KHÔNG VGrow ---
        // Việc này giúp Grid tự chiếm chiều cao nó cần, nút sẽ giữ nguyên kích thước 40x40
        blockBox.getChildren().addAll(lblTitle, comboContainer, legendBox, gridSeats);
        
        return blockBox;
    }

    /**
     * Logic chọn ghế hàng loạt và ghi nhớ để Undo
     */
    private void handleBatchSelection(int amountNeeded, List<ChiTietToa> allSeatsInToa, LichTrinh lichTrinh, boolean isChieuDi, GridPane gridSeats, Button btnUndo) {
        // Fix Vấn đề 3: Lấy giỏ hàng tương ứng để check
        Map<Integer, VeTamThoi> currentCart = isChieuDi ? gioHang_Di : gioHang_Ve;

        if (currentCart.size() + amountNeeded > 10) {
            String chieu = isChieuDi ? "chiều đi" : "chiều về";
            showAlert(Alert.AlertType.WARNING, "Mỗi lượt (" + chieu + ") chỉ được mua tối đa 10 vé.\nHiện tại đã có: " + currentCart.size() + " vé.");
            return;
        }

        List<ChiTietToa> seatsToSelect = new ArrayList<>();
        Set<Integer> soldSet = chiTietLichTrinhDAO.getCacChoDaBan(lichTrinh.getMaLichTrinh());
        Set<Integer> currentSelectedSet = isChieuDi ? danhSachChoDaChon_Di : danhSachChoDaChon_Ve;

        // Thuật toán tìm ghế liền kề
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

        // --- GHI NHỚ ĐỂ UNDO ---
        List<Integer> targetRecentList = isChieuDi ? recentBatchSeats_Di : recentBatchSeats_Ve;
        targetRecentList.clear(); // Xóa lịch sử cũ

        for (ChiTietToa seat : seatsToSelect) {
            // Click chọn
            for (Node node : gridSeats.getChildren()) {
                if (node instanceof Button) {
                    Button btn = (Button) node;
                    if (btn.getText().equals(String.valueOf(seat.getSoThuTu()))) {
                        handleChonCho(btn, seat, isChieuDi, lichTrinh);
                        // Thêm vào danh sách undo
                        targetRecentList.add(seat.getCho().getMaCho());
                        break;
                    }
                }
            }
        }
        
        // Enable nút Undo
        btnUndo.setDisable(false);
    }
    
    /**
     * MỚI: Logic Undo (Hủy chọn các ghế vừa chọn tự động)
     */
    private void handleUndoBatchSelection(boolean isChieuDi) {
        List<Integer> targetRecentList = isChieuDi ? recentBatchSeats_Di : recentBatchSeats_Ve;
        Map<Integer, VeTamThoi> currentCart = isChieuDi ? gioHang_Di : gioHang_Ve;
        
        if (targetRecentList.isEmpty()) return;
        
        // Duyệt qua danh sách các ID vừa chọn để hủy
        for (Integer seatId : targetRecentList) {
            if (currentCart.containsKey(seatId)) {
                handleHuyVe(currentCart.get(seatId));
            }
        }
        
        // Clear danh sách sau khi đã hủy
        targetRecentList.clear();
    }
    
    // --- Fix Vấn đề 2: Xóa tất cả trong giỏ ---
    private void clearAllTickets(boolean isChieuDi) {
        Map<Integer, VeTamThoi> targetCart = isChieuDi ? gioHang_Di : gioHang_Ve;
        if (targetCart.isEmpty()) return;
        
        List<VeTamThoi> ticketsToRemove = new ArrayList<>(targetCart.values());
        for (VeTamThoi ve : ticketsToRemove) {
            handleHuyVe(ve);
        }
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

    private void handleChonCho(Button btn, ChiTietToa ct, boolean isChieuDi, LichTrinh lichTrinh) {
        Map<Integer, VeTamThoi> gioHang = isChieuDi ? gioHang_Di : gioHang_Ve;
        Set<Integer> selectedSet = isChieuDi ? danhSachChoDaChon_Di : danhSachChoDaChon_Ve;
        int maCho = ct.getCho().getMaCho();

        if (gioHang.containsKey(maCho)) {
            handleHuyVe(gioHang.get(maCho));
        } else {
            // Fix Vấn đề 3: Check đúng giỏ hàng
            if (gioHang.size() >= 10) {
                String chieu = isChieuDi ? "chiều đi" : "chiều về";
                showAlert(Alert.AlertType.WARNING, "Bạn chỉ được chọn tối đa 10 vé cho " + chieu + ".");
                return;
            }
            double giaVe = calculateTicketPrice(lichTrinh, ct);
            VeTamThoi ve = new VeTamThoi(lichTrinh, ct, giaVe, btn, isChieuDi);
            Node cardNode = createTicketCard(ve);
            ve.setCardNode(cardNode);
            gioHang.put(maCho, ve);
            selectedSet.add(maCho);
            btn.setStyle(STYLE_DANGCHON);
            updateCartUI();
        }
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
        btnXoa.setOnAction(e -> handleHuyVe(ve));

        priceBox.getChildren().addAll(btnXoa, lblGia);
        HBox.setHgrow(infoBox, Priority.ALWAYS);
        card.getChildren().addAll(infoBox, priceBox);
        return card;
    }

    private void handleHuyVe(VeTamThoi ve) {
        Map<Integer, VeTamThoi> gioHang = ve.isChieuDi() ? gioHang_Di : gioHang_Ve;
        Set<Integer> selectedSet = ve.isChieuDi() ? danhSachChoDaChon_Di : danhSachChoDaChon_Ve;

        gioHang.remove(ve.getMaCho());
        selectedSet.remove(ve.getMaCho());

        if (ve.getCardNode() != null) ticketListContainer.getChildren().remove(ve.getCardNode());
        if (ve.getSeatButton() != null) {
            ve.getSeatButton().setStyle(STYLE_TRONG);
            ve.getSeatButton().setDisable(false);
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
            
            // Fix Vấn đề 2: Nút xóa tất cả
            btnClearAllDi = new Button("Xóa tất cả");
            btnClearAllDi.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 3px 5px; -fx-background-color: #c0392b");
            btnClearAllDi.setOnAction(e -> clearAllTickets(true));
            
            HBox headerBox = new HBox(10, lblChieuDiHeader, btnClearAllDi);
            headerBox.setAlignment(Pos.CENTER_LEFT);
            ticketListContainer.getChildren().add(headerBox);
            
            for (VeTamThoi ve : gioHang_Di.values()) ticketListContainer.getChildren().add(ve.getCardNode());
        }
        
        if (!gioHang_Ve.isEmpty()) {
            lblChieuVeHeader = new Label("Chiều về");
            lblChieuVeHeader.setStyle("-fx-font-weight: bold; -fx-padding: 8 0 2 0; -fx-text-fill: #333;");
            
            btnClearAllVe = new Button("Xóa tất cả");
            btnClearAllVe.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 3px 5px; -fx-background-color: #c0392b");
            btnClearAllVe.setOnAction(e -> clearAllTickets(false));
            
            HBox headerBox = new HBox(10, lblChieuVeHeader, btnClearAllVe);
            headerBox.setAlignment(Pos.CENTER_LEFT);
            ticketListContainer.getChildren().add(headerBox);
            
            for (VeTamThoi ve : gioHang_Ve.values()) ticketListContainer.getChildren().add(ve.getCardNode());
        }
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
        if (ve != null) handleHuyVe(ve);
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
        labelTongTien = new Label("Tổng tiền: 0 VNĐ");
        labelTongTien.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #333;");
        labelTongTien.setPadding(new Insets(10, 0, 5, 0)); 
    }
}
