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
import com.ptudn12.main.enums.*;
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
    @FXML private VBox ticketListContainer;
    @FXML private Label labelTongTien;

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
    
    // Biến cho các nút "Xóa tất cả"
    private Button btnXoaTatCaDi;
    private Button btnXoaTatCaVe;
    
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
        
        // Xóa nội dung cũ trong toaSection để vẽ mới
        toaSection.getChildren().clear();
        
        // CẬP NHẬT: Gọi hàm updateCartUI để rebuild giỏ hàng
        // Hàm này sẽ vẽ lại giỏ hàng dựa trên `gioHang_Di` và `gioHang_Ve`
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

        // --- 1. COMBOBOX CHỌN TOA ---
        ComboBox<ToaInfo> comboToa = new ComboBox<>();
        comboToa.setPrefWidth(200.0);
        comboToa.setPromptText("Chọn toa");
        
        // --- 2. GIAO DIỆN CHỌN NHANH ---
        TextField txtSoLuong = new TextField();
        txtSoLuong.setPromptText("Số lượng: ");
        txtSoLuong.setPrefWidth(150);
        
        Button btnChonNhanh = new Button("Chọn nhanh");
        btnChonNhanh.getStyleClass().add("btn-secondary");
        
        HBox comboContainer = new HBox(10,
                new Label("Toa:"), comboToa,
                new Separator(Orientation.VERTICAL), // Đường gạch dọc
                new Label("Chọn nhanh:"), txtSoLuong, btnChonNhanh
        );
        comboContainer.setAlignment(Pos.CENTER_LEFT);
        
        // --- 3. GRID SƠ ĐỒ GHẾ ---
        GridPane gridSeats = new GridPane();
        gridSeats.setHgap(6);
        gridSeats.setVgap(6);
        gridSeats.setPadding(new Insets(5));
        
        // --- 4. CHÚ THÍCH MÀU (Tạo lại vì VBox cha đã clear) ---
        HBox legendBox = createLegendBox();

        if (dsChiTiet.isEmpty()) {
            blockBox.getChildren().addAll(lblTitle, new Label("Không có dữ liệu chỗ ngồi cho tàu này."));
            return blockBox;
        }

        // XỬ LÝ DỮ LIỆU COMBOBOX
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

        // SỰ KIỆN KHI CHỌN TOA
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
        
        // --- XỬ LÝ SỰ KIỆN NÚT CHỌN NHANH (MỚI) ---
        btnChonNhanh.setOnAction(e -> {
            ToaInfo selectedToa = comboToa.getValue();
            if (selectedToa == null) {
                showAlert(Alert.AlertType.WARNING, "Vui lòng chọn toa trước khi dùng chức năng chọn nhanh!");
                return;
            }

            String strSL = txtSoLuong.getText();
            if (strSL == null || strSL.isEmpty() || !strSL.matches("\\d+")) {
                showAlert(Alert.AlertType.WARNING, "Vui lòng nhập số lượng ghế hợp lệ (số nguyên dương)!");
                return;
            }

            int soLuongCanChon = Integer.parseInt(strSL);
            if (soLuongCanChon <= 0) {
                 showAlert(Alert.AlertType.WARNING, "Số lượng phải lớn hơn 0!");
                 return;
            }

            // Lấy danh sách ghế của toa hiện tại
            List<ChiTietToa> currentToaSeats = dsChiTiet.stream()
                    .filter(ct -> ct.getToa().getMaToa().equals(selectedToa.getMaToa()))
                    .sorted(Comparator.comparing(ChiTietToa::getSoThuTu)) // Sắp xếp để chọn liền kề
                    .collect(Collectors.toList());

            // Gọi hàm logic chọn hàng loạt
            handleBatchSelection(soLuongCanChon, currentToaSeats, lichTrinh, isChieuDi, gridSeats);
        });

        blockBox.getChildren().addAll(lblTitle, comboContainer, legendBox, gridSeats);
        return blockBox;
    }
    
    // Hàm logic xử lý chọn ghế hàng loạt
    private void handleBatchSelection(int amountNeeded, List<ChiTietToa> allSeatsInToa, LichTrinh lichTrinh, boolean isChieuDi, GridPane gridSeats) {
        Set<Integer> soldSet = chiTietLichTrinhDAO.getCacChoDaBan(lichTrinh.getMaLichTrinh());
        Set<Integer> currentSelectedSet = isChieuDi ? danhSachChoDaChon_Di : danhSachChoDaChon_Ve;
        Map<Integer, VeTamThoi> currentCart = isChieuDi ? gioHang_Di : gioHang_Ve;

        // Kiểm tra xem giỏ hàng có bị quá giới hạn không
        if (currentCart.size() + amountNeeded > 10) {
            showAlert(Alert.AlertType.WARNING, "Tổng số vé sẽ vượt quá giới hạn 10 vé!");
            return;
        }

        List<ChiTietToa> seatsToSelect = new ArrayList<>();

        // --- THUẬT TOÁN: Tìm n ghế trống LIỀN KỀ ---
        for (int i = 0; i < allSeatsInToa.size(); i++) {
            seatsToSelect.clear();
            for (int j = i; j < allSeatsInToa.size(); j++) {
                ChiTietToa seat = allSeatsInToa.get(j);
                int seatId = seat.getCho().getMaCho();

                // Nếu ghế chưa bán VÀ chưa được chọn trong giỏ
                if (!soldSet.contains(seatId) && !currentSelectedSet.contains(seatId)) {
                    seatsToSelect.add(seat);
                    if (seatsToSelect.size() == amountNeeded) {
                        break; 
                    }
                } else {
                    seatsToSelect.clear();
                    i = j; // Nhảy cóc
                    break;
                }
            }
            if (seatsToSelect.size() == amountNeeded) {
                break;
            }
        }

        // --- KẾT QUẢ ---
        if (seatsToSelect.size() < amountNeeded) {
            showAlert(Alert.AlertType.INFORMATION, "Không tìm thấy " + amountNeeded + " ghế liền kề trống trong toa này.\nVui lòng chọn toa khác hoặc chọn thủ công.");
            return;
        }

        // --- THỰC HIỆN CHỌN (Click giả lập) ---
        for (ChiTietToa seat : seatsToSelect) {
            // Tìm nút button tương ứng trên GridPane để gọi hàm click
            for (Node node : gridSeats.getChildren()) {
                if (node instanceof Button) {
                    Button btn = (Button) node;
                    // So sánh text của button với số thứ tự ghế
                    if (btn.getText().equals(String.valueOf(seat.getSoThuTu()))) {
                        handleChonCho(btn, seat, isChieuDi, lichTrinh);
                        break;
                    }
                }
            }
        }
    }
    
    // Helper tạo chú thích màu (để tái sử dụng)
    private HBox createLegendBox() {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        
        HBox boxTrong = new HBox(5, new Rectangle(20, 20, javafx.scene.paint.Color.WHITE) {{ setStroke(javafx.scene.paint.Color.BLACK); }}, new Label("Trống"));
        boxTrong.setAlignment(Pos.CENTER);
        
        HBox boxDaBan = new HBox(5, new Rectangle(20, 20, javafx.scene.paint.Color.RED), new Label("Đã bán"));
        boxDaBan.setAlignment(Pos.CENTER);
        
        HBox boxDangChon = new HBox(5, new Rectangle(20, 20, javafx.scene.paint.Color.GREEN), new Label("Đang chọn"));
        boxDangChon.setAlignment(Pos.CENTER);
        
        box.getChildren().addAll(boxTrong, boxDaBan, boxDangChon, new Label("| Lưu ý: Tối đa 10 vé/giao dịch"));
        return box;
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

    // CẬP NHẬT: Hàm `handleChonCho`
    private void handleChonCho(Button btn, ChiTietToa ct, boolean isChieuDi, LichTrinh lichTrinh) {
        Map<Integer, VeTamThoi> gioHang = isChieuDi ? gioHang_Di : gioHang_Ve;
        Set<Integer> selectedSet = isChieuDi ? danhSachChoDaChon_Di : danhSachChoDaChon_Ve;
        int maCho = ct.getCho().getMaCho();

        if (gioHang.containsKey(maCho)) {
            handleHuyVe(gioHang.get(maCho));
        } else {
            if (gioHang.size() >= 10) {
                showAlert(Alert.AlertType.WARNING, "Bạn chỉ được chọn tối đa 10 vé cho " + (isChieuDi ? "chiều đi" : "chiều về") + ".");
                return;
            }
            double giaVe = calculateTicketPrice(lichTrinh, ct);
            VeTamThoi ve = new VeTamThoi(lichTrinh, ct, giaVe, btn, isChieuDi);
            Node cardNode = createTicketCard(ve);
            ve.setCardNode(cardNode);
            gioHang.put(maCho, ve);
            selectedSet.add(maCho);
            btn.setStyle(STYLE_DANGCHON);

            // THÊM VÀO TICKET LIST CONTAINER (Trong ScrollPane)
            int insertIndex;
            if (isChieuDi) {
                if (lblChieuDiHeader == null) {
                    lblChieuDiHeader = new Label("Chiều đi");
                    lblChieuDiHeader.setStyle("-fx-font-weight: bold; -fx-padding: 8 0 2 0;");
                    ticketListContainer.getChildren().add(0, lblChieuDiHeader); // Luôn ở đầu
                    ticketListContainer.getChildren().add(1, btnXoaTatCaDi);
                }
                insertIndex = ticketListContainer.getChildren().indexOf(btnXoaTatCaDi) + 1;
            } else {
                if (lblChieuVeHeader == null) {
                    lblChieuVeHeader = new Label("Chiều về");
                    lblChieuVeHeader.setStyle("-fx-font-weight: bold; -fx-padding: 8 0 2 0;");
                    int baseIndex = 0;
                    if (lblChieuDiHeader != null) {
                        baseIndex = ticketListContainer.getChildren().indexOf(btnXoaTatCaDi) + 1 + gioHang_Di.size();
                    }
                    ticketListContainer.getChildren().add(baseIndex, lblChieuVeHeader);
                    ticketListContainer.getChildren().add(baseIndex + 1, btnXoaTatCaVe);
                }
                insertIndex = ticketListContainer.getChildren().indexOf(btnXoaTatCaVe) + 1;
            }
            ticketListContainer.getChildren().add(insertIndex, cardNode);
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
        btnXoa.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 2 6;");
        btnXoa.setOnAction(e -> handleHuyVe(ve));
        
        priceBox.getChildren().addAll(btnXoa, lblGia);
        
        HBox.setHgrow(infoBox, Priority.ALWAYS); 
        card.getChildren().addAll(infoBox, priceBox);
        return card;
    }

    // CẬP NHẬT: Hàm `handleHuyVe`
    private void handleHuyVe(VeTamThoi ve) {
        Map<Integer, VeTamThoi> gioHang = ve.isChieuDi() ? gioHang_Di : gioHang_Ve;
        Set<Integer> selectedSet = ve.isChieuDi() ? danhSachChoDaChon_Di : danhSachChoDaChon_Ve;

        gioHang.remove(ve.getMaCho());
        selectedSet.remove(ve.getMaCho());

        if (ve.getCardNode() != null) {
            ticketListContainer.getChildren().remove(ve.getCardNode());
        }
        if (ve.getSeatButton() != null) {
            ve.getSeatButton().setStyle(STYLE_TRONG);
            ve.getSeatButton().setDisable(false);
        }

        if (ve.isChieuDi() && gioHang_Di.isEmpty() && lblChieuDiHeader != null) {
            ticketListContainer.getChildren().remove(lblChieuDiHeader);
            ticketListContainer.getChildren().remove(btnXoaTatCaDi);
            lblChieuDiHeader = null;
        } else if (!ve.isChieuDi() && gioHang_Ve.isEmpty() && lblChieuVeHeader != null) {
            ticketListContainer.getChildren().remove(lblChieuVeHeader);
            ticketListContainer.getChildren().remove(btnXoaTatCaVe);
            lblChieuVeHeader = null;
        }
        updateTongTien();
    }
    
    /**
     * MỚI: (Phản hồi 1) Hàm xóa tất cả vé trong giỏ hàng cho một chiều
     */
    private void clearCartSection(boolean isChieuDi) {
        Map<Integer, VeTamThoi> gioHang = isChieuDi ? gioHang_Di : gioHang_Ve;
        
        // Phải tạo 1 list mới để tránh lỗi ConcurrentModificationException
        List<VeTamThoi> veToCancel = new ArrayList<>(gioHang.values());
        
        for (VeTamThoi ve : veToCancel) {
            handleHuyVe(ve); // Tái sử dụng logic hủy từng vé
        }
    }

    private void updateCartUI() {
        ticketListContainer.getChildren().clear(); // Clear vùng scroll
        lblChieuDiHeader = null;
        lblChieuVeHeader = null;

        if (!gioHang_Di.isEmpty()) {
            lblChieuDiHeader = new Label("Chiều đi");
            lblChieuDiHeader.setStyle("-fx-font-weight: bold; -fx-padding: 8 0 2 0; -fx-text-fill: black;");
            ticketListContainer.getChildren().add(lblChieuDiHeader);
            ticketListContainer.getChildren().add(btnXoaTatCaDi);
            for (VeTamThoi ve : gioHang_Di.values()) {
                ticketListContainer.getChildren().add(ve.getCardNode());
            }
        }
        if (!gioHang_Ve.isEmpty()) {
            lblChieuVeHeader = new Label("Chiều về");
            lblChieuVeHeader.setStyle("-fx-font-weight: bold; -fx-padding: 8 0 2 0; -fx-text-fill: black;");
            ticketListContainer.getChildren().add(lblChieuVeHeader);
            ticketListContainer.getChildren().add(btnXoaTatCaVe);
            for (VeTamThoi ve : gioHang_Ve.values()) {
                ticketListContainer.getChildren().add(ve.getCardNode());
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

    // CẬP NHẬT: Hàm `handleTiepTheo`
    @FXML
    private void handleTiepTheo() {
        if (gioHang_Di.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Vui lòng chọn ít nhất một chỗ cho chiều đi.");
            return;
        }
        
        LichTrinh lichTrinhVe = (LichTrinh) mainController.getUserData("lichTrinhChieuVe");
        if (lichTrinhVe != null) {
            // Đây là vé khứ hồi
            if (gioHang_Ve.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Vui lòng chọn ít nhất một chỗ cho chiều về.");
                return;
            }
            
            // MỚI: (Phản hồi 2) KIỂM TRA SỐ LƯỢNG VÉ ĐI/VỀ TƯƠNG XỨNG
            if (gioHang_Di.size() != gioHang_Ve.size()) {
                showAlert(Alert.AlertType.WARNING, 
                    "Số lượng vé không tương xứng.\n\n" +
                    "Bạn đã chọn " + gioHang_Di.size() + " vé chiều đi và " + 
                    gioHang_Ve.size() + " vé chiều về.\n" +
                    "Vui lòng chọn số lượng vé bằng nhau cho cả hai chiều."
                );
                return;
            }
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
        btnXoaTatCaDi = new Button("Xóa tất cả (Đi)");
        btnXoaTatCaDi.setStyle("-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-underline: true; -fx-cursor: hand;");
        btnXoaTatCaDi.setOnAction(e -> clearCartSection(true));
        
        btnXoaTatCaVe = new Button("Xóa tất cả (Về)");
        btnXoaTatCaVe.setStyle("-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-underline: true; -fx-cursor: hand;");
        btnXoaTatCaVe.setOnAction(e -> clearCartSection(false));
    }
}
