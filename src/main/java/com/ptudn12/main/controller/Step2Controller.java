/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ptudn12.main.controller;

/**
 *
 * @author fo3cp
 */

import com.ptudn12.main.controller.Step1Controller.ChuyenTauMock; 
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class Step2Controller {
    @FXML private Label labelTenTau;
    @FXML private Label labelThoiGianDi;
    @FXML private Label labelThoiGianDen;
    @FXML private ComboBox<String> comboChonToa;
    @FXML private FlowPane seatMapPane;
    @FXML private VBox gioVeContainer;
    @FXML private Button btnMuaVe;
    
    // Dành cho khứ hồi
    @FXML private HBox chieuVeToggleBox;
    @FXML private ToggleButton toggleChieuDi;
    @FXML private ToggleButton toggleChieuVe;

    // SỬA: Đổi loại Controller chính
    private BanVeController mainController;
    private ChuyenTauMock selectedTrain;
    private boolean isKhuHoi;
    
    // --- MOCK: Lưu trạng thái ghế ---
    private Map<String, Map<Integer, SeatState>> dsToaChieuDi = new HashMap<>();
    private Map<String, Map<Integer, SeatState>> dsToaChieuVe = new HashMap<>();
    
    private enum SeatState { EMPTY, SOLD, PENDING }

    public void setMainController(BanVeController mainController) {
        this.mainController = mainController;
        
        // GỌI HÀM INITDATA TẠI ĐÂY!
        initData();
    }

    @FXML
    public void initialize() {
        // Để trống
    }
    
    public void initData() {
        // 1. Lấy dữ liệu từ controller chính (Giờ đã an toàn)
        this.selectedTrain = (ChuyenTauMock) mainController.getUserData("selectedTrain");
        
        if (mainController.getUserData("isKhuHoi") != null) {
            this.isKhuHoi = (boolean) mainController.getUserData("isKhuHoi");
        } else {
            this.isKhuHoi = false;
        }

        // 2. Hiển thị thông tin chuyến tàu
        if (selectedTrain != null) {
             labelTenTau.setText(selectedTrain.tenTau);
            labelThoiGianDi.setText("Thời gian đi: " + selectedTrain.tgDi);
            labelThoiGianDen.setText("Thời gian đến: " + selectedTrain.tgDen);
        }

        // 3. --- MOCK: TẠO DATA TOA VÀ GHẾ ---
        generateSampleToa(dsToaChieuDi);
        if (isKhuHoi) {
            generateSampleToa(dsToaChieuVe);
            chieuVeToggleBox.setVisible(true);
            chieuVeToggleBox.setManaged(true);
            
            ToggleGroup chieuGroup = new ToggleGroup();
            toggleChieuDi.setToggleGroup(chieuGroup);
            toggleChieuVe.setToggleGroup(chieuGroup);
            
            chieuGroup.selectedToggleProperty().addListener((obs, o, n) -> loadToaList());
        }

        // 4. Load danh sách toa
        loadToaList();
        
        // 5. Thêm listener
        comboChonToa.getSelectionModel().selectedItemProperty().addListener((obs, oldToa, newToa) -> {
            if (newToa != null) {
                drawSeatMap(newToa);
            }
        });
        
        // 6. Cập nhật giỏ vé
        updateGioVe();
    }

    // --- MOCK: HÀM TẠO TOA VÀ GHẾ MẪU ---
    private void generateSampleToa(Map<String, Map<Integer, SeatState>> dsToa) {
        Random rand = new Random();
        for (int i = 1; i <= 5; i++) {
            String tenToa = "Toa " + i + " - Ngồi cứng";
            Map<Integer, SeatState> seats = new HashMap<>();
            for (int j = 1; j <= 64; j++) { // 64 ghế
                // Giả lập 20% ghế đã bán
                if (rand.nextInt(5) == 0) {
                    seats.put(j, SeatState.SOLD);
                } else {
                    seats.put(j, SeatState.EMPTY);
                }
            }
            // Mock ghế đã bán trong hình
            seats.put(2, SeatState.SOLD);
            seats.put(5, SeatState.SOLD);
            seats.put(31, SeatState.SOLD);
            
            dsToa.put(tenToa, seats);
        }
    }
    
    // Load danh sách toa dựa trên chiều Đi/Về
    private void loadToaList() {
        comboChonToa.getItems().clear();
        if (isKhuHoi && toggleChieuVe.isSelected()) {
            comboChonToa.getItems().addAll(dsToaChieuVe.keySet());
        } else {
            comboChonToa.getItems().addAll(dsToaChieuDi.keySet());
        }
        comboChonToa.getSelectionModel().selectFirst();
    }
    
    // Vẽ sơ đồ ghế
    private void drawSeatMap(String tenToa) {
        seatMapPane.getChildren().clear();
        
        Map<String, Map<Integer, SeatState>> dsToa = (isKhuHoi && toggleChieuVe.isSelected()) ? dsToaChieuVe : dsToaChieuDi;
        if (dsToa == null || !dsToa.containsKey(tenToa)) return;
        
        Map<Integer, SeatState> seats = dsToa.get(tenToa);
        
        for (int seatNum : seats.keySet()) {
            SeatState state = seats.get(seatNum);
            Button seatButton = new Button(String.valueOf(seatNum));
            seatButton.getStyleClass().add("seat");

            switch (state) {
                case EMPTY:
                    seatButton.getStyleClass().add("seat-empty");
                    seatButton.setOnAction(e -> handleSeatClick(tenToa, seatNum, seatButton));
                    break;
                case SOLD:
                    seatButton.getStyleClass().add("seat-sold");
                    seatButton.setDisable(true);
                    break;
                case PENDING:
                    seatButton.getStyleClass().add("seat-pending");
                    seatButton.setOnAction(e -> handleSeatClick(tenToa, seatNum, seatButton));
                    break;
            }
            seatMapPane.getChildren().add(seatButton);
        }
    }
    
    // Xử lý khi click vào 1 ghế
    private void handleSeatClick(String tenToa, int seatNum, Button seatButton) {
        Map<String, Map<Integer, SeatState>> dsToa = (isKhuHoi && toggleChieuVe.isSelected()) ? dsToaChieuVe : dsToaChieuDi;
        Map<Integer, SeatState> seats = dsToa.get(tenToa);
        
        if (seats.get(seatNum) == SeatState.EMPTY) {
            // Chọn ghế
            seats.put(seatNum, SeatState.PENDING);
            seatButton.getStyleClass().remove("seat-empty");
            seatButton.getStyleClass().add("seat-pending");
        } else {
            // Bỏ chọn ghế
            seats.put(seatNum, SeatState.EMPTY);
            seatButton.getStyleClass().remove("seat-pending");
            seatButton.getStyleClass().add("seat-empty");
        }
        
        // Cập nhật lại giỏ vé
        updateGioVe();
    }
    
    // Cập nhật giỏ vé
    private void updateGioVe() {
        gioVeContainer.getChildren().clear();
        btnMuaVe.setDisable(true); // Vô hiệu hóa cho đến khi có vé
        
        // Thêm vé chiều đi
        addTicketsToCart(dsToaChieuDi, " (Chiều đi)");
        
        // Thêm vé chiều về (nếu có)
        if (isKhuHoi) {
            addTicketsToCart(dsToaChieuVe, " (Chiều về)");
        }
    }
    
    private void addTicketsToCart(Map<String, Map<Integer, SeatState>> dsToa, String chieu) {
        if (selectedTrain == null) return;
        
        for (String tenToa : dsToa.keySet()) {
            for (int seatNum : dsToa.get(tenToa).keySet()) {
                if (dsToa.get(tenToa).get(seatNum) == SeatState.PENDING) {
                    
                    // --- MOCK: Giá vé ---
                    double giaVe = 1100000.0; 
                    
                    HBox ticketItem = createTicketItemView(tenToa, seatNum, giaVe, chieu);
                    gioVeContainer.getChildren().add(ticketItem);
                    btnMuaVe.setDisable(false); // Kích hoạt nút mua vé
                }
            }
        }
    }
    
    // Tạo giao diện cho 1 vé trong giỏ
    private HBox createTicketItemView(String tenToa, int seatNum, double giaVe, String chieu) {
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(5));
        hbox.setStyle("-fx-border-color: #eee; -fx-border-width: 0 0 1 0;");
        
        VBox info = new VBox();
        info.getChildren().add(new Label(selectedTrain.tenTau + chieu));
        info.getChildren().add(new Label(tenToa + " - Chỗ " + seatNum));
        info.getChildren().add(new Label(String.format("%,.0f đồng", giaVe)));
        
        Button btnXoa = new Button("X");
        btnXoa.setStyle("-fx-text-fill: red; -fx-background-color: transparent; -fx-cursor: hand;");
        btnXoa.setOnAction(e -> {
            // Tìm và xóa vé này
            Map<String, Map<Integer, SeatState>> dsToa = (chieu.contains("Chiều về")) ? dsToaChieuVe : dsToaChieuDi;
            dsToa.get(tenToa).put(seatNum, SeatState.EMPTY);
            
            // Vẽ lại map và giỏ vé
            drawSeatMap(comboChonToa.getValue());
            updateGioVe();
        });
        
        hbox.getChildren().addAll(info, btnXoa);
        return hbox;
    }

    @FXML
    private void handleTiepTheo() {
        // Lưu vé đã chọn vào mainController
        // (Bạn có thể tạo 1 class VeDaChon và lưu 1 List<VeDaChon> vào userData)
        
        // SỬA: Tải file step-3.fxml
        mainController.loadContent("step-3.fxml");
    }

    @FXML
    private void handleQuayLai() {
        // SỬA: Tải file step-1.fxml
        mainController.loadContent("step-1.fxml");
    }
}
