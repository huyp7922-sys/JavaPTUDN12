/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ptudn12.main.controller;

/**
 *
 * @author fo3cp
 */

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols; 
import java.util.ArrayList;
import java.util.List;
import java.util.Locale; 
import java.util.Map;
import java.util.stream.Collectors;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;

public class Step3Controller {
   @FXML private TableView<HanhKhachVe> tableViewHanhKhach;
    @FXML private TableColumn<HanhKhachVe, String> colHoTen;
    @FXML private TableColumn<HanhKhachVe, String> colDoiTuong;
    @FXML private TableColumn<HanhKhachVe, String> colSoGiayTo;
    @FXML private TableColumn<HanhKhachVe, String> colThongTinCho;
    @FXML private TableColumn<HanhKhachVe, Number> colGiaVe;
    @FXML private TableColumn<HanhKhachVe, Number> colGiamGia;
    @FXML private TableColumn<HanhKhachVe, Number> colBaoHiem;
    @FXML private TableColumn<HanhKhachVe, Number> colThanhTien;
    @FXML private Label labelTongTien;
    @FXML private TextField txtNguoiMuaHoTen;
    @FXML private TextField txtNguoiMuaCMND;
    @FXML private TextField txtNguoiMuaEmail;
    @FXML private TextField txtNguoiMuaSDT;
    @FXML private Button btnTiepTheo;

    private BanVeController mainController;
    private ObservableList<HanhKhachVe> dsHanhKhach;

    // Danh sách các đối tượng
    private final ObservableList<String> doiTuongList = FXCollections.observableArrayList(
            "Người bình thường", "Người lớn tuổi", "Trẻ em", "Học sinh – sinh viên");

    // SỬA LỖI: Định dạng tiền tệ
    private final DecimalFormat currencyFormat;
    
    // Khối khởi tạo để setup DecimalFormat
    {
        // Dùng Locale VIỆT NAM để có dấu chấm "." phân cách ngàn
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("vi", "VN"));
        // Dùng #,##0 là pattern chuẩn, và ' đồng' là chữ
        currencyFormat = new DecimalFormat("#,##0' đồng'", symbols);
    }

    /**
     * Hàm này được BanVeController gọi
     */
    public void setMainController(BanVeController mainController) {
        this.mainController = mainController;
        initData(); // Gọi logic khởi tạo sau khi đã có mainController
    }

    /**
     * Để trống, vì logic được chuyển sang initData()
     */
    @FXML public void initialize() { /* Để trống */ }

    /**
     * Hàm khởi tạo logic chính
     */
    public void initData() {
        setupTableColumns();
        loadDataFromStep2(); 
    }
    
    /**
     * Cấu hình các cột của TableView
     * GHI CHÚ: "3 dòng input" của bạn chính là các ô có thể chỉnh sửa.
     * Hãy NHẤN ĐÚP CHUỘT (DOUBLE-CLICK) vào các ô "Họ tên", "Đối tượng", "Số giấy tờ"
     * để nhập liệu.
     */
    private void setupTableColumns() {
        // Cột Họ Tên (TextField)
        colHoTen.setCellValueFactory(new PropertyValueFactory<>("hoTen"));
        colHoTen.setCellFactory(TextFieldTableCell.forTableColumn());
        colHoTen.setOnEditCommit(event -> 
            event.getTableView().getItems().get(event.getTablePosition().getRow())
                 .setHoTen(event.getNewValue())
        );

        // Cột Số Giấy Tờ (TextField)
        colSoGiayTo.setCellValueFactory(new PropertyValueFactory<>("soGiayTo"));
        colSoGiayTo.setCellFactory(TextFieldTableCell.forTableColumn());
        colSoGiayTo.setOnEditCommit(event -> 
            event.getTableView().getItems().get(event.getTablePosition().getRow())
                 .setSoGiayTo(event.getNewValue())
        );

        // Cột Đối Tượng (ComboBox)
        colDoiTuong.setCellValueFactory(new PropertyValueFactory<>("doiTuong"));
        colDoiTuong.setCellFactory(ComboBoxTableCell.forTableColumn(doiTuongList));
        colDoiTuong.setOnEditCommit(event -> 
            event.getTableView().getItems().get(event.getTablePosition().getRow())
                 .setDoiTuong(event.getNewValue())
        );
        
        // --- CÁC CỘT CHỈ HIỂN THỊ ---
        colThongTinCho.setCellValueFactory(new PropertyValueFactory<>("thongTinCho"));
        colGiaVe.setCellValueFactory(new PropertyValueFactory<>("giaVeGoc"));
        colBaoHiem.setCellValueFactory(new PropertyValueFactory<>("baoHiem"));
        colGiamGia.setCellValueFactory(new PropertyValueFactory<>("giamDoiTuong"));
        colThanhTien.setCellValueFactory(new PropertyValueFactory<>("thanhTien"));

        // Áp dụng định dạng tiền tệ cho các cột
        colGiaVe.setCellFactory(col -> createCurrencyCell());
        colBaoHiem.setCellFactory(col -> createCurrencyCell());
        colGiamGia.setCellFactory(col -> createCurrencyCell());
        colThanhTien.setCellFactory(col -> createCurrencyCell());
        
        tableViewHanhKhach.setEditable(true);
    }
    
    /**
     * Hàm tiện ích tạo TableCell để định dạng số thành tiền tệ
     */
    private TableCell<HanhKhachVe, Number> createCurrencyCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(currencyFormat.format(item.doubleValue()));
                }
            }
        };
    }

    /**
     * SỬA: Dùng data MOCK (giả) như bạn yêu cầu
     * để hiển thị UI mà không cần data từ Step 2
     */
    private void loadDataFromStep2() {
        // Tạo data mock
        List<VeDaChonMock> dsVe = generateSampleVe(); 
        
        // Chuyển đổi dsVe (data) thành dsHanhKhach (model)
        List<HanhKhachVe> list = dsVe.stream()
                .map(ve -> {
                    HanhKhachVe hkv = new HanhKhachVe(ve);
                    // Lắng nghe thay đổi để tính lại tổng tiền
                    hkv.thanhTienProperty().addListener((obs) -> calculateTotalPrice());
                    return hkv;
                })
                .collect(Collectors.toList());

        this.dsHanhKhach = FXCollections.observableArrayList(list);
        tableViewHanhKhach.setItems(this.dsHanhKhach);
        
        // Tự động điền thông tin người mua vé (lấy từ vé mock đầu tiên)
        if (!dsHanhKhach.isEmpty()) {
            txtNguoiMuaHoTen.setText(dsHanhKhach.get(0).getHoTen());
            txtNguoiMuaCMND.setText(dsHanhKhach.get(0).getSoGiayTo());
        }
        
        // Tính tổng tiền lần đầu
        calculateTotalPrice();
    }

    /**
     * Tính toán và cập nhật lại Label Tổng Tiền
     */
    private void calculateTotalPrice() {
        double total = 0;
        if (dsHanhKhach != null) {
             for (HanhKhachVe hk : dsHanhKhach) {
                total += hk.getThanhTien();
            }
        }
        labelTongTien.setText(currencyFormat.format(total));
    }
    
    /**
     * --- MOCK: TẠO DỮ LIỆU GIẢ LẬP VÉ TỪ BƯỚC 2 ---
     */
    private List<VeDaChonMock> generateSampleVe() {
        List<VeDaChonMock> list = new ArrayList<>();
        // Giả lập có 1 vé như trong hình
        list.add(new VeDaChonMock("SE1 - Thống nhất", "Toa 1 - Chỗ 36", 1100000));
        
        // Thử thêm 1 vé nữa (bạn có thể bỏ comment dòng dưới để test tổng tiền)
        // list.add(new VeDaChonMock("SE1 - Thống nhất", "Toa 1 - Chỗ 37", 1100000));
        
        return list;
    }
    
    /**
     * --- MOCK: Class mock đại diện cho 1 vé ---
     */
    public class VeDaChonMock {
        private String chuyenTauInfo;
        private String toaChoInfo;
        private double giaVeGoc;
        private final double baoHiem = 2000.0;

        public VeDaChonMock(String chuyenTau, String toaCho, double gia) {
            this.chuyenTauInfo = chuyenTau;
            this.toaChoInfo = toaCho;
            this.giaVeGoc = gia;
        }
        
        public String getThongTinCho() {
            // Giống trong mockup
            return chuyenTauInfo + "\n" + "24/12/2025 6:00" + "\n" + toaChoInfo + "\nNgồi cứng";
        }
        public double getGiaVeGoc() { return giaVeGoc; }
        public double getBaoHiem() { return baoHiem; }
    }


    // --- Navigation ---
    @FXML
    private void handleQuayLai() {
        // (Lưu ý: Khi chạy thật, bạn nên lưu lại data đã nhập vào mainController)
        mainController.loadContent("step-2.fxml");
    }

    @FXML
    private void handleTiepTheo() {
        // Kiểm tra validation (họ tên, số giấy tờ không rỗng)
        for (HanhKhachVe hk : dsHanhKhach) {
            if (hk.getHoTen() == null || hk.getHoTen().trim().isEmpty() ||
                hk.getSoGiayTo() == null || hk.getSoGiayTo().trim().isEmpty()) {
                
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng nhập đầy đủ Họ tên và Số giấy tờ cho tất cả hành khách.");
                return;
            }
        }
        
        // Lưu dữ liệu vào mainController (khi chạy thật)
        mainController.setUserData("danhSachHanhKhach", new ArrayList<>(dsHanhKhach));
        
        // Chuyển sang Step 4
        mainController.loadContent("step-4.fxml");
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * ========================================================================
     * CLASS NỘI TẠI (INNER CLASS)
     * Đây là Model cho mỗi hàng trong TableView.
     * Chứa logic tự động tính toán.
     * ========================================================================
     */
    public class HanhKhachVe {
        private final VeDaChonMock veDaChon; // SỬA: Dùng class mock

        // Các cột có thể sửa
        private final StringProperty hoTen;
        private final StringProperty soGiayTo;
        private final ObjectProperty<String> doiTuong;
        
        // Các cột hiển thị (lấy từ veDaChon)
        private final StringProperty thongTinCho;
        private final DoubleProperty giaVeGoc;
        private final DoubleProperty baoHiem;
        
        // Các cột được tính toán
        private final DoubleProperty giamDoiTuong;
        private final DoubleProperty thanhTien;
        
        // Bảng hệ số giảm giá
        private final Map<String, Double> discountMap = Map.of(
            "Người bình thường", 0.0,
            "Người lớn tuổi", 0.15,
            "Trẻ em", 0.25,
            "Học sinh – sinh viên", 0.1
        );

        // SỬA: Constructor dùng `VeDaChonMock`
        public HanhKhachVe(VeDaChonMock ve) {
            this.veDaChon = ve;
            
            // Dữ liệu mock ban đầu (Giống mockup)
            this.hoTen = new SimpleStringProperty("Nguyễn Văn A"); 
            this.soGiayTo = new SimpleStringProperty("0123456789"); 
            this.doiTuong = new SimpleObjectProperty<>("Người lớn"); // Giống mockup
            
            this.thongTinCho = new SimpleStringProperty(ve.getThongTinCho());
            this.giaVeGoc = new SimpleDoubleProperty(ve.getGiaVeGoc());
            this.baoHiem = new SimpleDoubleProperty(ve.getBaoHiem());
            
            // Dữ liệu tính toán ban đầu
            this.giamDoiTuong = new SimpleDoubleProperty(0);
            this.thanhTien = new SimpleDoubleProperty(ve.getGiaVeGoc() + ve.getBaoHiem());
            
            // **LOGIC CỐT LÕI:**
            // Khi thuộc tính "doiTuong" thay đổi, tự động tính toán lại
            this.doiTuong.addListener((obs, oldVal, newVal) -> {
                recalculate();
                
                // Hiển thị modal nếu là đối tượng cần xác minh
                if (newVal.equals("Người lớn tuổi") || newVal.equals("Trẻ em")) {
                    showAgeVerificationModal(newVal); 
                }
            });
            
            // Tính toán lại ngay lập tức dựa trên giá trị "Người lớn" ban đầu
            recalculate();
        }
        
        /**
         * Tính toán lại Giảm giá và Thành tiền
         */
        private void recalculate() {
            String doiTuongHienTai = doiTuong.get();
            // SỬA: Map "Người lớn" (từ mockup) về "Người bình thường" (từ logic)
            if (doiTuongHienTai.equals("Người lớn")) {
                doiTuongHienTai = "Người bình thường";
            }
            
            double discountRate = discountMap.getOrDefault(doiTuongHienTai, 0.0);
            double giam = giaVeGoc.get() * discountRate;
            this.giamDoiTuong.set(giam);
            
            double thanhTienMoi = (giaVeGoc.get() - giam) + baoHiem.get();
            this.thanhTien.set(thanhTienMoi);
        }
        
        /**
         * --- MOCK MODAL ---
         * Hiển thị cửa sổ popup (DatePicker) để xác nhận tuổi
         */
        private void showAgeVerificationModal(String doiTuong) {
            System.out.println("LOGIC: Hiển thị modal chọn ngày sinh cho: " + doiTuong);
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Xác nhận đối tượng");
            alert.setHeaderText("Vui lòng nhập ngày sinh cho " + doiTuong);
            alert.setContentText("Đây là nơi DatePicker modal sẽ xuất hiện.");
            alert.showAndWait();
            
            // (Sau khi xác nhận, nếu tuổi không hợp lệ, bạn có thể set đối tượng về "Người bình thường")
            // if (!isAgeValid) {
            //     this.doiTuong.set("Người bình thường");
            // }
        }

        // --- Getters & Setters (Bắt buộc cho PropertyValueFactory) ---
        
        public String getHoTen() { return hoTen.get(); }
        public void setHoTen(String hoTen) { this.hoTen.set(hoTen); }
        public StringProperty hoTenProperty() { return hoTen; }

        public String getSoGiayTo() { return soGiayTo.get(); }
        public void setSoGiayTo(String soGiayTo) { this.soGiayTo.set(soGiayTo); }
        public StringProperty soGiayToProperty() { return soGiayTo; }

        public String getDoiTuong() { return doiTuong.get(); }
        public void setDoiTuong(String doiTuong) { this.doiTuong.set(doiTuong); }
        public ObjectProperty<String> doiTuongProperty() { return doiTuong; }

        public String getThongTinCho() { return thongTinCho.get(); }
        public StringProperty thongTinChoProperty() { return thongTinCho; }

        public double getGiaVeGoc() { return giaVeGoc.get(); }
        public DoubleProperty giaVeGocProperty() { return giaVeGoc; }

        public double getBaoHiem() { return baoHiem.get(); }
        public DoubleProperty baoHiemProperty() { return baoHiem; }

        public double getGiamDoiTuong() { return giamDoiTuong.get(); }
        public DoubleProperty giamDoiTuongProperty() { return giamDoiTuong; }

        public double getThanhTien() { return thanhTien.get(); }
        public DoubleProperty thanhTienProperty() { return thanhTien; }
    }
}
