/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ptudn12.main.controller;

/**
 *
 * @author fo3cp
 */

import com.ptudn12.main.dao.GaDAO;
import com.ptudn12.main.dao.LichTrinhDAO;
import com.ptudn12.main.dao.TuyenDuongDAO;
import com.ptudn12.main.entity.Ga;
import com.ptudn12.main.entity.LichTrinh;
import com.ptudn12.main.entity.TuyenDuong;
import com.ptudn12.main.enums.TrangThai;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.TilePane;

public class Step1Controller {
    @FXML
    private ComboBox<String> comboGaDi;
    @FXML
    private ComboBox<String> comboGaDen;
    @FXML
    private DatePicker datePickerNgayKhoiHanh;
    @FXML
    private Button btnTimChuyen;
    @FXML
    private Pane paneDanhSachChuyenTau;
    @FXML
    private RadioButton radioKhuHoi;
    @FXML
    private RadioButton radioMotChieu;
    @FXML
    private DatePicker dateNgayVe;

    private final LichTrinhDAO lichTrinhDAO = new LichTrinhDAO();
    private final GaDAO gaDAO = new GaDAO();
    private final TuyenDuongDAO tuyenDuongDAO = new TuyenDuongDAO();
    
    private LichTrinh lichTrinhChieuDi = null;
    private LichTrinh lichTrinhChieuVe = null;
    private Node cardChieuDi = null;
    private Node cardChieuVe = null;
    
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    private BanVeController mainController;
    public void setMainController(BanVeController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        // Load danh sách ga vào combobox
        List<String> danhSachGa = gaDAO.layViTriGa(); // Hàm này trả về List<String>
        ObservableList<String> gaList = FXCollections.observableArrayList(danhSachGa);
        comboGaDi.setItems(gaList); // Giờ ComboBox chứa String
        comboGaDen.setItems(gaList); // Giờ ComboBox chứa String

        // Optional: set default date = today
        datePickerNgayKhoiHanh.setValue(LocalDate.now());
        
        dateNgayVe.setDisable(true);
        radioKhuHoi.selectedProperty().addListener((observable, oldValue, newValue) -> {
            dateNgayVe.setDisable(!newValue);
        });
    }

    @FXML
    private void handleTimKiem() {
        // --- 1. Lấy thông tin cơ bản ---
        String tenGaDi = comboGaDi.getValue();
        String tenGaDen = comboGaDen.getValue();
        LocalDate ngayKhoiHanh = datePickerNgayKhoiHanh.getValue();

        // --- 2. Validation cơ bản ---
        if (tenGaDi == null || tenGaDen == null || tenGaDi.isEmpty() || tenGaDen.isEmpty() || ngayKhoiHanh == null) {
            showAlert(Alert.AlertType.WARNING, "Vui lòng chọn đầy đủ thông tin (Ga đi, Ga đến, Ngày đi)");
            return;
        }
        if (tenGaDi.equals(tenGaDen)) {
            showAlert(Alert.AlertType.WARNING, "Ga đi và ga đến không được trùng nhau!");
            return;
        }

        // --- 3. Lấy đối tượng Ga ---
        Ga gaDi = gaDAO.layGaTheoViTri(tenGaDi);
        Ga gaDen = gaDAO.layGaTheoViTri(tenGaDen);
        if (gaDi == null || gaDen == null) {
             showAlert(Alert.AlertType.ERROR, "Lỗi dữ liệu ga. Không tìm thấy thông tin chi tiết cho ga đã chọn.");
            return;
        }
        
        // --- 4. Tìm tuyến đi và Lịch trình đi ---
        List<TuyenDuong> tuyensDi = tuyenDuongDAO.timTuyenDuong(gaDi.getMaGa(), gaDen.getMaGa(), TrangThai.SanSang);
        if (tuyensDi.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Không tìm thấy tuyến đi phù hợp với ga đã chọn!");
            return;
        }
        TuyenDuong tuyenDi = tuyensDi.get(0);

        LocalDateTime tuNgayDi = ngayKhoiHanh.atStartOfDay();
        LocalDateTime denNgayDi = ngayKhoiHanh.plusDays(1).atStartOfDay();
        List<LichTrinh> ketQuaDi = lichTrinhDAO.timLichTrinh(
                Integer.parseInt(tuyenDi.getMaTuyen()), null, tuNgayDi, denNgayDi, TrangThai.ChuaKhoiHanh
        );

        // --- 5. Xử lý logic Khứ hồi / Một chiều ---
        if (radioKhuHoi.isSelected()) {
            LocalDate ngayVe = dateNgayVe.getValue();
            
            // Validation cho ngày về
            if (ngayVe == null) {
                showAlert(Alert.AlertType.WARNING, "Vui lòng chọn ngày về cho vé khứ hồi.");
                return;
            }
            if (ngayVe.isBefore(ngayKhoiHanh)) {
                showAlert(Alert.AlertType.WARNING, "Ngày về không được trước ngày đi.");
                return;
            }

            // Tìm tuyến về (B -> A)
            List<TuyenDuong> tuyensVe = tuyenDuongDAO.timTuyenDuong(gaDen.getMaGa(), gaDi.getMaGa(), TrangThai.SanSang);
            if (tuyensVe.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Không tìm thấy tuyến về phù hợp.");
                showLichTrinhTrongPane(ketQuaDi, null);
                return;
            }
            TuyenDuong tuyenVe = tuyensVe.get(0);

            // Tìm lịch trình về
            LocalDateTime tuNgayVe = ngayVe.atStartOfDay();
            LocalDateTime denNgayVe = ngayVe.plusDays(1).atStartOfDay();
            List<LichTrinh> ketQuaVe = lichTrinhDAO.timLichTrinh(
                    Integer.parseInt(tuyenVe.getMaTuyen()), null, tuNgayVe, denNgayVe, TrangThai.ChuaKhoiHanh
            );
            
            // Hiển thị cả 2 danh sách
            showLichTrinhTrongPane(ketQuaDi, ketQuaVe);

        } else {
            // Hiển thị 1 danh sách (như cũ)
            showLichTrinhTrongPane(ketQuaDi, null);
        }
    }

    private void showLichTrinhTrongPane(List<LichTrinh> danhSachDi, List<LichTrinh> danhSachVe) {
        // --- 1. Xóa kết quả cũ (VBox) ---
        Node oldResultBox = paneDanhSachChuyenTau.lookup("#result-container");
        if (oldResultBox != null) {
            paneDanhSachChuyenTau.getChildren().remove(oldResultBox);
        }
        
        // --- 2. Reset lựa chọn ---
        lichTrinhChieuDi = null;
        lichTrinhChieuVe = null;
        cardChieuDi = null;
        cardChieuVe = null;

        // --- 3. Tạo VBox container chính ---
        VBox resultContainer = new VBox(10); // 10px spacing
        resultContainer.setId("result-container");
        resultContainer.setLayoutY(50); // Nằm dưới tiêu đề "Danh sách chuyến tàu"
        
        // Bind kích thước VBox với Pane
        resultContainer.prefWidthProperty().bind(paneDanhSachChuyenTau.widthProperty());
        resultContainer.prefHeightProperty().bind(paneDanhSachChuyenTau.heightProperty().subtract(50));
        
        // --- 4. Tạo và thêm danh sách ---
        
        if (danhSachVe != null) {
            // --- Chế độ Khứ Hồi ---
            
            // Tiêu đề chiều đi
            Label lblChieuDi = new Label("Chọn tàu cho chiều đi");
            lblChieuDi.getStyleClass().add("trip-direction-label");
            VBox.setMargin(lblChieuDi, new Insets(0, 0, 0, 15)); // Thêm lề trái
            
            // ScrollPane chiều đi
            ScrollPane scrollDi = createTrainCardScrollPane(danhSachDi, true);
            
            // Tiêu đề chiều về
            Label lblChieuVe = new Label("Chọn tàu cho chiều về");
            lblChieuVe.getStyleClass().add("trip-direction-label");
            VBox.setMargin(lblChieuVe, new Insets(0, 0, 0, 15)); // Thêm lề trái
            
            // ScrollPane chiều về
            ScrollPane scrollVe = createTrainCardScrollPane(danhSachVe, false);

            // Cho cả 2 ScrollPane tự co giãn
            VBox.setVgrow(scrollDi, javafx.scene.layout.Priority.ALWAYS);
            VBox.setVgrow(scrollVe, javafx.scene.layout.Priority.ALWAYS);

            resultContainer.getChildren().addAll(lblChieuDi, scrollDi, lblChieuVe, scrollVe);

        } else {
            // --- Chế độ Một Chiều (như cũ) ---
            ScrollPane scrollDi = createTrainCardScrollPane(danhSachDi, true);
            VBox.setVgrow(scrollDi, javafx.scene.layout.Priority.ALWAYS); // Cho nó lấp đầy
            resultContainer.getChildren().add(scrollDi);
        }

        paneDanhSachChuyenTau.getChildren().add(resultContainer);
        
        // Kiểm tra và báo lỗi nếu không có kết quả
        if (danhSachDi.isEmpty() && (danhSachVe == null || danhSachVe.isEmpty())) {
             showAlert(Alert.AlertType.INFORMATION, "Không tìm thấy chuyến tàu nào phù hợp!");
        } else if (danhSachDi.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Không tìm thấy chuyến tàu cho chiều đi!");
        } else if (danhSachVe != null && danhSachVe.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Không tìm thấy chuyến tàu cho chiều về!");
        }
    }
    
   private ScrollPane createTrainCardScrollPane(List<LichTrinh> danhSach, boolean isChieuDi) {
        TilePane tilePane = new TilePane();
        tilePane.setPadding(new Insets(15));
        tilePane.setHgap(15);
        tilePane.setVgap(15);
        
        for (LichTrinh lt : danhSach) {
            // 1. Gọi hàm DAO mới 1 LẦN DUY NHẤT
            // Hàm này trả về: [0]=tổng, [1]=đã bán, [2]=còn trống, [3]=status
            int[] soGhe = lichTrinhDAO.layThongTinChoNgoiTau(lt.getMaLichTrinh());

            int gheDat = 0;
            int gheTrong = 0;

            // 2. Kiểm tra xem DAO có trả về thành công không (status == 0)
            if (soGhe[3] == 0) {
                gheDat = soGhe[1];   // Index [1] là "SoChoDaBan" (Đã bán)
                gheTrong = soGhe[2]; // Index [2] là "SoChoConTrong" (Còn trống)
            } else {
                // Xử lý nếu có lỗi từ Stored Procedure (ví dụ: in ra console)
                System.err.println("Lỗi: Không thể lấy thông tin ghế cho Lịch trình " + lt.getMaLichTrinh());
            }
            
            // SỬA: Truyền 'isChieuDi' vào hàm tạo card
            Node trainCard = createTrainCard(lt, gheDat, gheTrong, isChieuDi);
            tilePane.getChildren().add(trainCard);
        }
        
        ScrollPane scrollPane = new ScrollPane(tilePane);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("no-border-scroll-pane");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // Tắt thanh cuộn ngang

        return scrollPane;
    }
    
    private Node createTrainCard(LichTrinh lt, int gheDat, int gheTrong, boolean isChieuDi) {
        GridPane card = new GridPane();
        // ... (Giữ nguyên code setHgap, setVgap, setPadding, getStyleClass)
        card.setHgap(10);
        card.setVgap(8);
        card.setPadding(new Insets(10));
        card.getStyleClass().add("train-card"); 

        // ... (Giữ nguyên code tạo các Label: lblTenTau, lblGheDat, lblGheTrong, etc.)
        Label lblTenTau = new Label(lt.getTau().getMacTau());
        lblTenTau.getStyleClass().add("train-card-title");
        card.add(lblTenTau, 0, 0, 4, 1); 
        card.add(new Label("Thời gian đi:"), 0, 1);
        card.add(new Label(lt.getNgayGioKhoiHanh().format(formatter)), 1, 1);
        card.add(new Label("Số lượng chỗ đặt:"), 2, 1);
        Label lblGheDat = new Label(String.valueOf(gheDat));
        lblGheDat.getStyleClass().add("train-card-booked");
        card.add(lblGheDat, 3, 1);
        card.add(new Label("Thời gian đến:"), 0, 2);
        card.add(new Label(lt.getNgayGioDen().format(formatter)), 1, 2);
        card.add(new Label("Số lượng chỗ trống:"), 2, 2);
        Label lblGheTrong = new Label(String.valueOf(gheTrong));
        lblGheTrong.getStyleClass().add("train-card-available");
        card.add(lblGheTrong, 3, 2);


        // --- SỬA: Cập nhật sự kiện Click ---
        card.setOnMouseClicked(event -> {
            if (isChieuDi) {
                // Click chọn chiều đi
                lichTrinhChieuDi = lt; 
                if (cardChieuDi != null) { // Bỏ chọn card cũ
                    cardChieuDi.getStyleClass().remove("train-card-selected");
                }
                card.getStyleClass().add("train-card-selected");
                cardChieuDi = card; // Lưu card mới
            } else {
                // Click chọn chiều về
                lichTrinhChieuVe = lt;
                if (cardChieuVe != null) { // Bỏ chọn card cũ
                    cardChieuVe.getStyleClass().remove("train-card-selected");
                }
                card.getStyleClass().add("train-card-selected");
                cardChieuVe = card; // Lưu card mới
            }
        });

        return card;
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setTitle("Thông báo");
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleTiepTheo() {
        // Gửi dữ liệu qua MainController
        mainController.setUserData("isKhuHoi", radioKhuHoi.isSelected());
        
        if (radioKhuHoi.isSelected()) {
            // --- Chế độ Khứ Hồi ---
            if (lichTrinhChieuDi == null || lichTrinhChieuVe == null) {
                showAlert(Alert.AlertType.WARNING, "Vui lòng chọn chuyến tàu cho cả chiều đi và chiều về.");
                return;
            }
            mainController.setUserData("lichTrinhChieuDi", lichTrinhChieuDi);
            mainController.setUserData("lichTrinhChieuVe", lichTrinhChieuVe);
            
        } else {
            // --- Chế độ Một Chiều ---
            if (lichTrinhChieuDi == null) {
                showAlert(Alert.AlertType.WARNING, "Vui lòng chọn một chuyến tàu trước khi tiếp tục.");
                return;
            }
            mainController.setUserData("lichTrinhChieuDi", lichTrinhChieuDi);
            mainController.setUserData("lichTrinhChieuVe", null); // Xóa vé về (nếu có)
        }
        
        mainController.loadContent("step-2.fxml");
    }

    @FXML
    private void handleQuayLai() {
        mainController.handleLogout();
    }
}
