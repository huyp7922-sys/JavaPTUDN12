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
import com.ptudn12.main.entity.*;
import com.ptudn12.main.enums.TrangThai;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Step1Controller {
    @FXML private ComboBox<String> comboGaDi;
    @FXML private ComboBox<String> comboGaDen;
    @FXML private DatePicker datePickerNgayKhoiHanh;
    @FXML private Button btnTimChuyen;
    @FXML private Pane paneDanhSachChuyenTau;
    @FXML private RadioButton radioKhuHoi;
    @FXML private RadioButton radioMotChieu;
    @FXML private DatePicker dateNgayVe;
    @FXML private Label lblTitle;

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
    
    public void initData() {
        if (mainController == null) return;

        String mode = (String) mainController.getUserData("transactionType");
        
        // --- LOGIC ĐỔI VÉ ---
        if (BanVeController.MODE_DOI_VE.equals(mode)) {
            VeTau veCu = (VeTau) mainController.getUserData("veCuCanDoi");
            if (veCu != null) {
                // 1. Fill thông tin hành trình cũ (Phải đúng ga)
                String gaDiCu = veCu.getChiTietLichTrinh().getLichTrinh().getTuyenDuong().getDiemDi().getViTriGa();
                for (String ga : comboGaDi.getItems()) {
                    if (ga.equalsIgnoreCase(gaDiCu)) {
                        comboGaDi.setValue(ga);
                        break;
                    }
                }
                String gaDenCu = veCu.getChiTietLichTrinh().getLichTrinh().getTuyenDuong().getDiemDen().getViTriGa();
                for (String ga : comboGaDen.getItems()) {
                    if (ga.equalsIgnoreCase(gaDenCu)) {
                        comboGaDen.setValue(ga);
                        break;
                    }
                }

                // 2. KHÓA KHÔNG CHO ĐỔI GA
                comboGaDi.setDisable(true);
                comboGaDen.setDisable(true);
                comboGaDi.setStyle("-fx-opacity: 1; -fx-background-color: #eee;");
                comboGaDen.setStyle("-fx-opacity: 1; -fx-background-color: #eee;");

                // 3. Ép kiểu 1 chiều (Đổi từng vé)
                radioMotChieu.setSelected(true);
                radioKhuHoi.setDisable(true);
                dateNgayVe.setDisable(true);

                // 4. Ngày đi mặc định
                LocalDate today = LocalDate.now();
                LocalDate oldDate = veCu.getChiTietLichTrinh().getLichTrinh().getNgayGioKhoiHanh().toLocalDate();
                
                // Nếu ngày của vé cũ là tương lai -> Set ngày đó cho tiện
                if (oldDate.isAfter(today) || oldDate.isEqual(today)) {
                    datePickerNgayKhoiHanh.setValue(oldDate);
                } else {
                    // Nếu vé cũ là quá khứ -> Set ngày mai (để tăng cơ hội có tàu khi test)
                    datePickerNgayKhoiHanh.setValue(today.plusDays(1)); 
                }
                
                // Cách đơn giản nhất: TỰ ĐỘNG TÌM NHƯNG KHÔNG BÁO LỖI NẾU RỖNG
                timKiemTuDongKhongBaoLoi();
                
                if(lblTitle != null) lblTitle.setText("ĐỔI VÉ - CHỌN TÀU MỚI");
            }
        }
        // --- LOGIC BÁN VÉ THƯỜNG ---
        else {
            comboGaDi.setDisable(false);
            comboGaDen.setDisable(false);
            radioKhuHoi.setDisable(false);
            comboGaDi.setStyle("");
            comboGaDen.setStyle("");
            if(lblTitle != null) lblTitle.setText("TÌM KIẾM CHUYẾN TÀU");
            
            // Khôi phục data cũ nếu quay lại từ bước sau
            restoreSavedData();
        }
    }

    private void restoreSavedData() {
        String savedGaDi = (String) mainController.getUserData("step1_gaDi");
        String savedGaDen = (String) mainController.getUserData("step1_gaDen");
        LocalDate savedNgayDi = (LocalDate) mainController.getUserData("step1_ngayDi");
        Boolean savedIsKhuHoi = (Boolean) mainController.getUserData("step1_isKhuHoi");
        LocalDate savedNgayVe = (LocalDate) mainController.getUserData("step1_ngayVe");

        if (savedGaDi != null) comboGaDi.setValue(savedGaDi);
        if (savedGaDen != null) comboGaDen.setValue(savedGaDen);
        if (savedNgayDi != null) datePickerNgayKhoiHanh.setValue(savedNgayDi);

        if (savedIsKhuHoi != null) {
            radioKhuHoi.setSelected(savedIsKhuHoi);
            dateNgayVe.setDisable(!savedIsKhuHoi);
            if (savedIsKhuHoi && savedNgayVe != null) {
                dateNgayVe.setValue(savedNgayVe);
            }
        } else {
            radioMotChieu.setSelected(true);
        }

        // Nếu đủ dữ liệu thì tìm lại luôn để hiện danh sách tàu đã chọn trước đó
        if (savedGaDi != null && savedGaDen != null && savedNgayDi != null) {
            handleTimKiem();
        }
    }
    
    private void timKiemTuDongKhongBaoLoi() {
        String tenGaDi = comboGaDi.getValue();
        String tenGaDen = comboGaDen.getValue();
        LocalDate ngayKhoiHanh = datePickerNgayKhoiHanh.getValue();

        if (tenGaDi == null || tenGaDen == null || ngayKhoiHanh == null) return;

        Ga gaDi = gaDAO.layGaTheoViTri(tenGaDi);
        Ga gaDen = gaDAO.layGaTheoViTri(tenGaDen);
        if (gaDi == null || gaDen == null) return;
        
        List<TuyenDuong> tuyensDi = tuyenDuongDAO.timTuyenDuong(gaDi.getMaGa(), gaDen.getMaGa(), TrangThai.SanSang);
        if (tuyensDi.isEmpty()) {
            System.err.println("DEBUG: Không tìm thấy Tuyến Đường nào từ " + tenGaDi + " đến " + tenGaDen);
            // Có thể hiện label báo lỗi ở đây nếu muốn
            return;
        }
        
        TuyenDuong tuyenDi = tuyensDi.get(0);
        LocalDateTime tuNgayDi = ngayKhoiHanh.atStartOfDay();
        LocalDateTime denNgayDi = ngayKhoiHanh.plusDays(1).atStartOfDay();
        
        // DEBUG IN RA CONSOLE ĐỂ KIỂM TRA
        System.out.println("DEBUG: Đang tìm tàu...");
        System.out.println(" - Tuyến: " + tuyenDi.getMaTuyen() + " (" + tenGaDi + " -> " + tenGaDen + ")");
        System.out.println(" - Thời gian từ: " + tuNgayDi + " đến " + denNgayDi);
        
        List<LichTrinh> ketQuaDi = lichTrinhDAO.timLichTrinh(
                Integer.parseInt(tuyenDi.getMaTuyen()), null, tuNgayDi, denNgayDi, TrangThai.ChuaKhoiHanh
        );
        
        // Chỉ hiển thị nếu có kết quả, không thì thôi (để trống pane)
        if (!ketQuaDi.isEmpty()) {
            showLichTrinhTrongPane(ketQuaDi, null);
        } else {
             // Có thể hiện một Label nhẹ nhàng: "Không có tàu trong ngày này" thay vì Alert
             showPlaceholderMessage("Không tìm thấy chuyến tàu nào trong ngày " + formatter.format(tuNgayDi));
        }
    }
    
    private void showPlaceholderMessage(String msg) {
        paneDanhSachChuyenTau.getChildren().clear();
        Label lbl = new Label(msg);
        lbl.setStyle("-fx-font-size: 16px; -fx-text-fill: #999; -fx-padding: 20;");
        paneDanhSachChuyenTau.getChildren().add(lbl);
    }

    @FXML
    public void initialize() {
        // 1. Load danh sách ga
        List<String> danhSachGa = gaDAO.layViTriGa();
        ObservableList<String> gaList = FXCollections.observableArrayList(danhSachGa);
        comboGaDi.setItems(gaList);
        comboGaDen.setItems(gaList);

        // 2. Cấu hình Ngày Khởi Hành: Không cho chọn quá khứ
        datePickerNgayKhoiHanh.setValue(LocalDate.now());
        datePickerNgayKhoiHanh.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                // Disable các ngày trước ngày hiện tại
                if (date.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #ffc0cb;");
                }
            }
        });

        // 3. Cấu hình Ngày Về
        dateNgayVe.setDisable(true);
        // Khi ngày khởi hành thay đổi -> Reset ngày về nếu nó bị vô lý (trước ngày đi mới)
        datePickerNgayKhoiHanh.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (newDate != null && radioKhuHoi.isSelected()) {
                // Nếu ngày về đang chọn mà bé hơn ngày đi mới -> Xóa hoặc set lại bằng ngày đi
                if (dateNgayVe.getValue() != null && dateNgayVe.getValue().isBefore(newDate)) {
                    dateNgayVe.setValue(newDate);
                }
            }
        });

        // Factory cho ngày về
        dateNgayVe.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                LocalDate ngayDi = datePickerNgayKhoiHanh.getValue();
                if (ngayDi == null) ngayDi = LocalDate.now();
                if (item.isBefore(ngayDi)) {
                    setDisable(true);
                    setStyle("-fx-background-color: #ffc0cb;");
                }
            }
        });

        // 4. Radio Button Logic
        radioKhuHoi.selectedProperty().addListener((observable, oldValue, newValue) -> {
            dateNgayVe.setDisable(!newValue);
            if (newValue) {
                if (dateNgayVe.getValue() == null || dateNgayVe.getValue().isBefore(datePickerNgayKhoiHanh.getValue())) {
                    dateNgayVe.setValue(datePickerNgayKhoiHanh.getValue());
                }
            }
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
        VBox resultContainer = new VBox(10);
        resultContainer.setId("result-container");
        resultContainer.setLayoutY(50);
        
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
        card.setHgap(10);
        card.setVgap(8);
        card.setPadding(new Insets(10));
        card.getStyleClass().add("train-card"); 

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
        // --- Validation chọn tàu (giữ nguyên) ---
        if (radioKhuHoi.isSelected()) {
            if (lichTrinhChieuDi == null || lichTrinhChieuVe == null) {
                showAlert(Alert.AlertType.WARNING, "Vui lòng chọn chuyến tàu cho cả chiều đi và chiều về.");
                return;
            }
        } else {
            if (lichTrinhChieuDi == null) {
                showAlert(Alert.AlertType.WARNING, "Vui lòng chọn một chuyến tàu trước khi tiếp tục.");
                return;
            }
        }

        // --- LƯU TRẠNG THÁI FORM VÀO MAINCONTROLLER ---
        mainController.setUserData("step1_gaDi", comboGaDi.getValue());
        mainController.setUserData("step1_gaDen", comboGaDen.getValue());
        mainController.setUserData("step1_ngayDi", datePickerNgayKhoiHanh.getValue());
        mainController.setUserData("step1_isKhuHoi", radioKhuHoi.isSelected());
        mainController.setUserData("step1_ngayVe", dateNgayVe.getValue()); // Lưu cả ngày về (nếu có)

        mainController.setUserData("lichTrinhChieuDi", lichTrinhChieuDi);
        mainController.setUserData("lichTrinhChieuVe", lichTrinhChieuVe); // Sẽ là null nếu không khứ hồi
        
        mainController.loadContent("step-2.fxml");
    }

    @FXML
    private void handleQuayLai() {
        mainController.handleLogout();
    }
}