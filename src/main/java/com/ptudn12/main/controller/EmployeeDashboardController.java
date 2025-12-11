package com.ptudn12.main.controller;

import com.ptudn12.main.dao.EmployeeDashboardDAO;
import com.ptudn12.main.entity.NhanVien;
import com.ptudn12.main.util.SessionManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;

import java.sql.Timestamp;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EmployeeDashboardController {
    
    @FXML private Label lblDoanhSoHomNay;
    @FXML private Label lblTongVeDaBan;
    @FXML private Label lblDoanhThu;
    @FXML private Label lblChoXuLy;
    @FXML private Label lblCurrentTime;
    @FXML private Label lblEmployeeName;
    
    @FXML private TableView<TicketRow> tableTickets;
    @FXML private TableColumn<TicketRow, String> colMaVe;
    @FXML private TableColumn<TicketRow, String> colHanhKhach;
    @FXML private TableColumn<TicketRow, String> colTuyen;
    @FXML private TableColumn<TicketRow, String> colThoiGian;
    @FXML private TableColumn<TicketRow, String> colCho;
    @FXML private TableColumn<TicketRow, String> colTrangThai;
    
    @FXML private TextField txtSearch;
    @FXML private ListView<String> listUpcomingTrips;
    @FXML private TextArea txtNotes;
    
    private ObservableList<TicketRow> masterData = FXCollections.observableArrayList();
    private EmployeeDashboardDAO dao = new EmployeeDashboardDAO();
    
    @FXML
    public void initialize() {
        setupTable();
        
        // Display employee info
        NhanVien nhanVien = SessionManager.getInstance().getCurrentNhanVien();
        if (nhanVien != null && lblEmployeeName != null) {
            lblEmployeeName.setText("👤 " + nhanVien.getTenNhanVien() + " (" + nhanVien.getChucVuText() + ")");
        }
        
        if (lblCurrentTime != null) {
            setupClock();
        }
        loadData();
        
        // Auto refresh every 30 seconds
        Timeline autoRefresh = new Timeline(new KeyFrame(Duration.seconds(30), e -> loadData()));
        autoRefresh.setCycleCount(Timeline.INDEFINITE);
        autoRefresh.play();
    }
    
    private void setupTable() {
        colMaVe.setCellValueFactory(new PropertyValueFactory<>("maVe"));
        colHanhKhach.setCellValueFactory(new PropertyValueFactory<>("hanhKhach"));
        colTuyen.setCellValueFactory(new PropertyValueFactory<>("tuyen"));
        colThoiGian.setCellValueFactory(new PropertyValueFactory<>("thoiGian"));
        colCho.setCellValueFactory(new PropertyValueFactory<>("cho"));
        colTrangThai.setCellValueFactory(new PropertyValueFactory<>("trangThai"));
        
        // Format trạng thái với màu sắc
        colTrangThai.setCellFactory(column -> new TableCell<TicketRow, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(getStatusText(item));
                    String styleClass = getStatusStyleClass(item);
                    setStyle("-fx-background-color: " + styleClass + "; -fx-text-fill: white; " +
                            "-fx-font-weight: bold; -fx-padding: 5px; -fx-background-radius: 3px;");
                }
            }
        });
        
        // Add click handler for pending tickets
        tableTickets.setRowFactory(tv -> {
            TableRow<TicketRow> row = new TableRow<TicketRow>() {
                @Override
                protected void updateItem(TicketRow item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setStyle("");
                    }
                }
            };
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 1) {
                    TicketRow selectedTicket = row.getItem();
                    if (selectedTicket != null && "DaDat".equals(selectedTicket.getTrangThai())) {
                        showTicketDialog(selectedTicket);
                    }
                }
            });
            return row;
        });
    }
    
    private void setupClock() {
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            lblCurrentTime.setText(LocalDateTime.now().format(formatter));
        }), new KeyFrame(Duration.seconds(1)));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();
    }
    
    @FXML
    private void handleRefresh() {
        loadData();
    }
    
    @FXML
    private void showPendingTickets() {
        try {
            int pendingCount = dao.getPendingTickets();
            if (pendingCount == 0) {
                showAlert(Alert.AlertType.INFORMATION, "Không có vé chờ xử lý", "Tất cả vé đã được xử lý!");
                return;
            }
            
            Alert dialog = new Alert(Alert.AlertType.INFORMATION);
            dialog.setTitle("Vé chưa xử lý");
            dialog.setHeaderText("Danh sách vé chờ xử lý (" + pendingCount + " vé)");
            
            List<Map<String, Object>> pendingTickets = dao.getPendingTicketsList();
            StringBuilder content = new StringBuilder();
            
            for (Map<String, Object> ticket : pendingTickets) {
                content.append("• Vé: ").append(ticket.get("maVe"))
                       .append(" | Khách: ").append(ticket.get("hanhKhach"))
                       .append(" | Tuyến: ").append(ticket.get("tuyen"))
                       .append("\n");
            }
            
            dialog.setContentText(content.toString());
            dialog.showAndWait();
        } catch (Exception e) {
            showError("Lỗi", e.getMessage());
        }
    }
    

    
    @FXML
    private void handleSearch() {
        String filter = txtSearch.getText().toLowerCase();
        if (filter.isEmpty()) {
            tableTickets.setItems(masterData);
        } else {
            tableTickets.setItems(masterData.filtered(ticket -> 
                ticket.getMaVe().toLowerCase().contains(filter) ||
                ticket.getHanhKhach().toLowerCase().contains(filter) ||
                ticket.getTuyen().toLowerCase().contains(filter)
            ));
        }
    }
    
    private void loadData() {
        try {
            NhanVien nhanVien = SessionManager.getInstance().getCurrentNhanVien();
            if (nhanVien == null) {
                showError("Lỗi", "Không thể lấy thông tin nhân viên đăng nhập");
                return;
            }
            
            // Load statistics
            int ticketsToday = dao.getTicketsSoldToday();
            int totalTickets = dao.getTotalTicketsSold();
            long revenue = dao.getRevenueToday();
            int pending = dao.getPendingTickets();
            
            lblDoanhSoHomNay.setText(String.valueOf(ticketsToday));
            lblTongVeDaBan.setText(String.valueOf(totalTickets));
            lblDoanhThu.setText(formatCurrency(revenue));
            lblChoXuLy.setText(String.valueOf(pending));
            
            // Load tickets
            List<Map<String, Object>> tickets = dao.getRecentTickets();
            masterData.clear();
            for (Map<String, Object> ticket : tickets) {
                masterData.add(new TicketRow(
                    (String) ticket.get("maVe"),
                    (String) ticket.get("hanhKhach"),
                    (String) ticket.get("tuyen"),
                    formatDateTime((Timestamp) ticket.get("thoiGian")),
                    (String) ticket.get("cho"),
                    (String) ticket.get("trangThai")
                ));
            }
            tableTickets.setItems(masterData);
            
            // Load upcoming trips
            List<Map<String, Object>> trips = dao.getUpcomingTrips();
            ObservableList<String> tripsList = FXCollections.observableArrayList();
            for (Map<String, Object> trip : trips) {
                String timeStr = formatTime((Timestamp) trip.get("thoiGian"));
                String tripStr = trip.get("tuyen") + " (" + timeStr + ")";
                int soVe = (int) trip.get("soVe");
                tripsList.add(tripStr + " - " + soVe + " vé");
            }
            listUpcomingTrips.setItems(tripsList);
            
        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi tải dữ liệu", e.getMessage());
        }
    }
    
    private String formatCurrency(long amount) {
        return NumberFormat.getInstance(new Locale("vi", "VN")).format(amount) + " đ";
    }
    
    private String formatDateTime(Timestamp timestamp) {
        if (timestamp == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return timestamp.toLocalDateTime().format(formatter);
    }
    
    private String formatTime(Timestamp timestamp) {
        if (timestamp == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return timestamp.toLocalDateTime().format(formatter);
    }
    
    private String getStatusText(String status) {
        switch (status) {
            case "DaBan": return "DaBan";
            case "DaDat": return "DaDat";
            case "DaHuy": return "DaHuy";
            case "DaSuDung": return "DaSuDung";
            default: return status;
        }
    }
    
    private String getStatusStyleClass(String status) {
        switch (status) {
            case "DaBan": return "#4CAF50"; // Green
            case "DaDat": return "#FFA500"; // Orange
            case "DaHuy": return "#F44336"; // Red
            case "DaSuDung": return "#2196F3"; // Blue
            default: return "#9E9E9E"; // Gray
        }
    }
    
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showTicketDialog(TicketRow ticket) {
        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle("Vé chưa xử lý");
        dialog.setHeaderText(null);
        
        // Create content
        StringBuilder content = new StringBuilder();
        content.append("Mã vé: ").append(ticket.getMaVe()).append("\n");
        content.append("Khách hàng: ").append(ticket.getHanhKhach()).append("\n");
        content.append("Tuyến: ").append(ticket.getTuyen()).append("\n");
        content.append("Thời gian: ").append(ticket.getThoiGian()).append("\n");
        content.append("Ghế: ").append(ticket.getCho()).append("\n\n");
        content.append("Bạn có muốn xác nhận vé này không?");
        
        dialog.setContentText(content.toString());
        dialog.getButtonTypes().setAll(
            new javafx.scene.control.ButtonType("Xác nhận", javafx.scene.control.ButtonBar.ButtonData.YES),
            new javafx.scene.control.ButtonType("Hủy", javafx.scene.control.ButtonBar.ButtonData.NO)
        );
        
        java.util.Optional<javafx.scene.control.ButtonType> result = dialog.showAndWait();
        
        if (result.isPresent() && result.get().getButtonData() == javafx.scene.control.ButtonBar.ButtonData.YES) {
            // Update ticket status to DaBan
            try {
                boolean updated = dao.updateTicketStatus(ticket.getMaVe(), "DaBan");
                if (updated) {
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Vé " + ticket.getMaVe() + " đã được xác nhận!");
                    loadData();
                } else {
                    showError("Lỗi", "Không thể cập nhật trạng thái vé");
                }
            } catch (Exception e) {
                showError("Lỗi", e.getMessage());
            }
        }
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Inner class for table rows
    public static class TicketRow {
        private final StringProperty maVe;
        private final StringProperty hanhKhach;
        private final StringProperty tuyen;
        private final StringProperty thoiGian;
        private final StringProperty cho;
        private final StringProperty trangThai;
        
        public TicketRow(String maVe, String hanhKhach, String tuyen, 
                        String thoiGian, String cho, String trangThai) {
            this.maVe = new SimpleStringProperty(maVe);
            this.hanhKhach = new SimpleStringProperty(hanhKhach);
            this.tuyen = new SimpleStringProperty(tuyen);
            this.thoiGian = new SimpleStringProperty(thoiGian);
            this.cho = new SimpleStringProperty(cho);
            this.trangThai = new SimpleStringProperty(trangThai);
        }
        
        public String getMaVe() { return maVe.get(); }
        public String getHanhKhach() { return hanhKhach.get(); }
        public String getTuyen() { return tuyen.get(); }
        public String getThoiGian() { return thoiGian.get(); }
        public String getCho() { return cho.get(); }
        public String getTrangThai() { return trangThai.get(); }
        
        public StringProperty maVeProperty() { return maVe; }
        public StringProperty hanhKhachProperty() { return hanhKhach; }
        public StringProperty tuyenProperty() { return tuyen; }
        public StringProperty thoiGianProperty() { return thoiGian; }
        public StringProperty choProperty() { return cho; }
        public StringProperty trangThaiProperty() { return trangThai; }
    }
}
