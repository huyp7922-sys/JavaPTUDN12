package com.ptudn12.main.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import com.ptudn12.main.dao.HoaDonDAO;
import com.ptudn12.main.entity.HoaDon;
import com.ptudn12.main.enums.LoaiHoaDon;
import com.ptudn12.main.utils.NumberUtils;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class InvoiceManagementController {

    @FXML private TableView<HoaDon> invoiceTable;
    @FXML private TableColumn<HoaDon, String> idColumn;
    @FXML private TableColumn<HoaDon, String> employeeColumn;
    @FXML private TableColumn<HoaDon, String> customerColumn;
    @FXML private TableColumn<HoaDon, String> dateColumn;
    @FXML private TableColumn<HoaDon, LoaiHoaDon> typeColumn;

    @FXML private Label totalInvoicesLabel;
    @FXML private ComboBox<Integer> dayFilterCombo;
    @FXML private ComboBox<Integer> monthFilterCombo;
    @FXML private ComboBox<Integer> yearFilterCombo;
    @FXML private TextField searchField;

    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();
    private LocalDate currentFilterDate = null;
    private TemplateEngine templateEngine;
    private ObservableList<HoaDon> invoiceData = FXCollections.observableArrayList();
    private FilteredList<HoaDon> filteredData;

    final double TAX_RATE = 0.08;

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("maHoaDon"));
        dateColumn.setCellValueFactory(cellData -> {
            LocalDateTime ngayLap = cellData.getValue().getNgayLap();
            if (ngayLap != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
                return new SimpleStringProperty(ngayLap.format(formatter));
            } else {
                return new SimpleStringProperty("");
            }
        });
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("loaiHoaDon"));
        employeeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNhanVien().getTenNhanVien()));
        customerColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getKhachHang().getTenKhachHang()));

        filteredData = new FilteredList<>(invoiceData, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(hoaDon -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = removeAccent(newValue.toLowerCase());
                
                if (hoaDon.getKhachHang() != null && hoaDon.getKhachHang().getTenKhachHang() != null) {
                    String customerName = removeAccent(hoaDon.getKhachHang().getTenKhachHang().toLowerCase());
                    return customerName.contains(lowerCaseFilter);
                }
                return false;
            });
            updateTotalInvoicesLabel();
        });

        SortedList<HoaDon> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(invoiceTable.comparatorProperty());
        invoiceTable.setItems(sortedData);

        setupFilterControls();
        loadDataFromDatabase();
    }

    public static String removeAccent(String s) {
        if (s == null) return "";
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("").replaceAll("đ", "d").replaceAll("Đ", "D");
    }

    @FXML
    private void handleFilterByDate() {
        Integer day = dayFilterCombo.getValue();
        Integer month = monthFilterCombo.getValue();
        Integer year = yearFilterCombo.getValue();

        if (day == null || month == null || year == null) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng chọn đầy đủ ngày, tháng, và năm để lọc.");
            return;
        }

        this.currentFilterDate = LocalDate.of(year, month, day);
        loadFilteredData(currentFilterDate);
    }

    @FXML
    private void handleRefresh() {
        if (currentFilterDate != null) {
            loadFilteredData(currentFilterDate);
        } else {
            loadAllData();
        }
        searchField.clear();
        showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Dữ liệu đã được làm mới thành công!");
    }

    @FXML
    private void handleShowAll() {
        this.currentFilterDate = null;
        dayFilterCombo.setValue(null);
        monthFilterCombo.setValue(null);
        yearFilterCombo.setValue(null);
        searchField.clear();
        loadAllData();
    }

    public InvoiceManagementController() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("/views/");
        resolver.setSuffix(".html");
        resolver.setCharacterEncoding("UTF-8");

        this.templateEngine = new TemplateEngine();
        this.templateEngine.setTemplateResolver(resolver);
    }

    private void loadAllData() {
        invoiceData.clear();
        List<HoaDon> danhSach = hoaDonDAO.layTatCaHoaDon();
        invoiceData.addAll(danhSach);
        updateTotalInvoicesLabel();
    }

    private void loadFilteredData(LocalDate startDate) {
        invoiceData.clear();
        List<HoaDon> filteredList = hoaDonDAO.layHoaDonTuNgay(startDate);
        invoiceData.setAll(filteredList);
        updateTotalInvoicesLabel();
    }

    private void updateTotalInvoicesLabel() {
        int count = filteredData.size();
        totalInvoicesLabel.setText("Có " + count + " hóa đơn");
    }

    private void setupFilterControls() {
        monthFilterCombo.setItems(FXCollections.observableArrayList(IntStream.rangeClosed(1, 12).boxed().collect(Collectors.toList())));

        int oldestYear = hoaDonDAO.getOldestInvoiceYear();
        int currentYear = LocalDate.now().getYear();
        if (oldestYear != -1 && oldestYear < currentYear) {
            yearFilterCombo.setItems(FXCollections.observableArrayList(IntStream.rangeClosed(oldestYear, currentYear).boxed().collect(Collectors.toList())));
        } else {
            yearFilterCombo.getItems().add(currentYear);
        }

        monthFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> updateDayComboBox());
        yearFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> updateDayComboBox());

        updateDayComboBox();
    }

    private void updateDayComboBox() {
        Integer selectedMonth = monthFilterCombo.getValue();
        Integer selectedYear = yearFilterCombo.getValue();

        if (selectedMonth == null) selectedMonth = LocalDate.now().getMonthValue();
        if (selectedYear == null) selectedYear = LocalDate.now().getYear();

        int daysInMonth = YearMonth.of(selectedYear, selectedMonth).lengthOfMonth();

        Integer previouslySelectedDay = dayFilterCombo.getValue();
        dayFilterCombo.setItems(FXCollections.observableArrayList(IntStream.rangeClosed(1, daysInMonth).boxed().collect(Collectors.toList())));

        if (previouslySelectedDay != null) {
            if (previouslySelectedDay > daysInMonth) {
                dayFilterCombo.setValue(daysInMonth);
            } else {
                dayFilterCombo.setValue(previouslySelectedDay);
            }
        }
    }

    private void loadDataFromDatabase() {
        invoiceData.clear();
        List<HoaDon> danhSach = hoaDonDAO.layTatCaHoaDon();
        invoiceData.addAll(danhSach);
        updateTotalInvoicesLabel();
    }

    @FXML
    private void handlePrintInvoice() {
        HoaDon selectedInvoice = invoiceTable.getSelectionModel().getSelectedItem();
        if (selectedInvoice == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn hóa đơn", "Vui lòng chọn một hóa đơn trong danh sách để in.");
            return;
        }

        File tempPdfFile = null;
        try {
            tempPdfFile = generateInvoicePdf(selectedInvoice);
            if (tempPdfFile == null || !tempPdfFile.exists()) {
                throw new IOException("Không thể tạo file PDF tạm thời.");
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/pdf-viewer.fxml"));
            Scene scene = new Scene(loader.load());

            Stage viewerStage = new Stage();
            viewerStage.setTitle("Xem trước Hóa đơn: " + selectedInvoice.getMaHoaDon());
            viewerStage.initModality(Modality.APPLICATION_MODAL);
            viewerStage.setWidth(700);
            viewerStage.setHeight(800);
            viewerStage.setScene(scene);

            PdfViewerController viewerController = loader.getController();
            viewerController.setStage(viewerStage);
            viewerController.loadDocument(tempPdfFile);

            viewerStage.setOnCloseRequest(e -> {
                viewerController.closeDocument();
            });

            viewerStage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở trình xem trước PDF: " + e.getMessage());
        } finally {
            if (tempPdfFile != null && tempPdfFile.exists()) {
                tempPdfFile.delete();
            }
        }
    }

    private File generateInvoicePdf(HoaDon invoiceData) throws Exception {
        Map<String, Object> data = getRealInvoiceData(invoiceData);

        Context context = new Context();
        context.setVariables(data);

        String processedHtml = templateEngine.process("invoice-template", context);

        File tempPdfFile = File.createTempFile("invoice-preview-", ".pdf");
        try (OutputStream os = new FileOutputStream(tempPdfFile)) {
            com.openhtmltopdf.pdfboxout.PdfRendererBuilder builder = new com.openhtmltopdf.pdfboxout.PdfRendererBuilder();

            builder.useFont(() -> {
                try {
                    InputStream is = getClass().getResourceAsStream("/fonts/times.ttf");
                    if (is == null) {
                        throw new IOException("Không tìm thấy file font tại: /fonts/times.ttf");
                    }
                    return is;
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }, "Times New Roman");

            String baseUri = getClass().getResource("/views/").toExternalForm();
            builder.withHtmlContent(processedHtml, baseUri);
            builder.toStream(os);
            builder.run();
        }

        return tempPdfFile;
    }

    private Map<String, Object> getRealInvoiceData(HoaDon hd) {
        Map<String, Object> data = new HashMap<>();
        NumberFormat currencyFormatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        currencyFormatter.setMaximumFractionDigits(0);

        LocalDateTime ngayLap = hd.getNgayLap();
        String yearFull = String.valueOf(ngayLap.getYear());
        String yearShort = yearFull.substring(2);

        data.put("ngayLap", String.format("%02d", ngayLap.getDayOfMonth()));
        data.put("thangLap", String.format("%02d", ngayLap.getMonthValue()));
        data.put("namLap", yearFull);

        data.put("kyHieu", "1K" + yearShort + "TKH");

        String rawId = hd.getMaHoaDon();
        String numberOnly = rawId.replaceAll("[^0-9]", "");
        if (numberOnly.isEmpty()) numberOnly = "0";

        long soHoaDonVal = Long.parseLong(numberOnly);
        data.put("soHD", String.format("%08d", soHoaDonVal));
        data.put("idHD", rawId);

        if (hd.getKhachHang() != null) {
            data.put("tenNguoiMua", hd.getKhachHang().getTenKhachHang());
            String sdt = hd.getKhachHang().getSoDienThoai();
            data.put("sdtNguoiMua", (sdt != null) ? sdt : "");
        } else {
            data.put("tenNguoiMua", "");
            data.put("sdtNguoiMua", "");
        }
        
        data.put("diaChiDonVi", "");
        data.put("tenDonVi", "");
        data.put("mstDonVi", "");
        data.put("hinhThucTT", "TM/CK");
        data.put("stkDonVi", "");

        List<Map<String, Object>> rawItems = hoaDonDAO.getChiTietHoaDonById(hd.getMaHoaDon());
        List<Map<String, Object>> allItems = new ArrayList<>();

        int stt = 1;
        double totalTicketQty = 0;
        double sum8_ThanhTien = 0;
        double sum8_TienThue = 0;
        double sum8_TongCong = 0;
        double totalBaoHiem = 0;

        for (Map<String, Object> itemDB : rawItems) {
            Object soLuongObj = itemDB.get("soLuong");
            double soLuong = (soLuongObj != null) ? ((Number) soLuongObj).doubleValue() : 1.0;

            Object tongTienGocObj = itemDB.get("thanhTienGoc");
            double tongTienGoc = (tongTienGocObj != null) ? ((Number) tongTienGocObj).doubleValue() : 0.0;

            Object phiBaoHiemObj = itemDB.get("baoHiem");
            double phiBaoHiem = (phiBaoHiemObj != null) ? ((Number) phiBaoHiemObj).doubleValue() : 0.0;

            double giaChiuThue = tongTienGoc - phiBaoHiem;
            double thanhTienChuaThue = giaChiuThue / (1 + TAX_RATE);
            double tienThue = giaChiuThue - thanhTienChuaThue;
            double donGiaHienThi = (soLuong > 0) ? (thanhTienChuaThue / soLuong) : 0;

            sum8_ThanhTien += thanhTienChuaThue;
            sum8_TienThue += tienThue;
            sum8_TongCong += giaChiuThue;

            totalBaoHiem += phiBaoHiem;
            totalTicketQty += soLuong;

            Map<String, Object> row = new HashMap<>();
            row.put("type", "ITEM");
            row.put("stt", stt++);
            row.put("maVe", itemDB.get("maVe"));
            row.put("tenDichVu", itemDB.get("tenDichVu"));
            row.put("dvt", itemDB.get("dvt"));
            row.put("soLuong", currencyFormatter.format(soLuong));
            row.put("donGia", currencyFormatter.format(donGiaHienThi));
            row.put("thanhTien", currencyFormatter.format(thanhTienChuaThue));
            row.put("thueSuat", "8%");
            row.put("tienThue", currencyFormatter.format(tienThue));
            row.put("tongCong", currencyFormatter.format(giaChiuThue));

            allItems.add(row);
        }

        if (totalBaoHiem > 0) {
            double donGiaBH = (totalTicketQty > 0) ? (totalBaoHiem / totalTicketQty) : 0;

            Map<String, Object> insRow = new HashMap<>();
            insRow.put("type", "ITEM");
            insRow.put("stt", stt++);
            insRow.put("maVe", "");
            insRow.put("tenDichVu", "Phí bảo hiểm hành khách");
            insRow.put("dvt", "Người");
            insRow.put("soLuong", currencyFormatter.format(totalTicketQty));
            insRow.put("donGia", currencyFormatter.format(donGiaBH));
            insRow.put("thanhTien", currencyFormatter.format(totalBaoHiem));
            insRow.put("thueSuat", "KCT");
            insRow.put("tienThue", "0");
            insRow.put("tongCong", currencyFormatter.format(totalBaoHiem));

            allItems.add(insRow);
        }

        Map<String, Object> summary8 = new HashMap<>();
        summary8.put("type", "SUMMARY_BY_TAX");
        summary8.put("description", "Tổng theo từng loại thuế suất:");
        summary8.put("thanhTien", currencyFormatter.format(sum8_ThanhTien));
        summary8.put("thueSuat", "8%");
        summary8.put("tienThue", currencyFormatter.format(sum8_TienThue));
        summary8.put("tongCong", currencyFormatter.format(sum8_TongCong));
        allItems.add(summary8);

        Map<String, Object> summaryKCT = new HashMap<>();
        summaryKCT.put("type", "SUMMARY_BY_TAX");
        summaryKCT.put("description", "");
        summaryKCT.put("thanhTien", currencyFormatter.format(totalBaoHiem));
        summaryKCT.put("thueSuat", "KCT");
        summaryKCT.put("tienThue", "0");
        summaryKCT.put("tongCong", currencyFormatter.format(totalBaoHiem));
        allItems.add(summaryKCT);

        double finalTotal = sum8_TongCong + totalBaoHiem;
        double finalTotal_ChuaThue = sum8_ThanhTien + totalBaoHiem;
        double finalTotal_Thue = sum8_TienThue;

        Map<String, Object> finalRow = new HashMap<>();
        finalRow.put("type", "FINAL_TOTAL");
        finalRow.put("description", "Tổng cộng:");
        finalRow.put("thanhTien", currencyFormatter.format(finalTotal_ChuaThue));
        finalRow.put("tienThue", currencyFormatter.format(finalTotal_Thue));
        finalRow.put("tongCong", currencyFormatter.format(finalTotal));
        allItems.add(finalRow);

        data.put("allItems", allItems);

        long totalLong = Math.round(finalTotal);
        data.put("tongTienBangChu", NumberUtils.docSoThanhChu(totalLong));
        data.put("ghiChu", "");

        URL imgUrl = getClass().getResource("/images/check.png");
        data.put("imgCheckUrl", imgUrl != null ? imgUrl.toExternalForm() : "");

        return data;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}