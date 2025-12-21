package com.ptudn12.main.controller;

import com.ptudn12.main.dao.ThongKeDAO;
import com.ptudn12.main.entity.ThongKe;
import com.ptudn12.main.entity.ThongKeKhachHang;
import com.ptudn12.main.utils.SessionManager;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.font.PdfFontFactory.EmbeddingStrategy;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import javafx.collections.*;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.FileOutputStream;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class StatisticsManagementController {

    @FXML
    private StackPane loadingPane;

    @FXML
    private TableView<ThongKe> tableStats;
    @FXML
    private TableColumn<ThongKe, String> colMaTuyen, colTenTuyen;
    @FXML
    private TableColumn<ThongKe, Integer> colTongVe, colSoChuyen, colSoVeTrong;
    @FXML
    private TableColumn<ThongKe, Double> colTiLe;
    @FXML
    private TableColumn<ThongKe, Long> colDoanhThu, colDoanhThuTB;

    @FXML
    private TableView<ThongKeKhachHang> tableTopCustomers;
    @FXML
    private TableColumn<ThongKeKhachHang, String> colMaKH, colTenKH, colSDT;
    @FXML
    private TableColumn<ThongKeKhachHang, Integer> colSoVeDaMua;
    @FXML
    private TableColumn<ThongKeKhachHang, Long> colTongTienKH;

    @FXML
    private TextField txtSearch, txtMaTuyen, txtTiLe;
    @FXML
    private Button btnExportPDF, btnExportExcel;

    @FXML
    private HBox dateFilterBox;
    @FXML
    private VBox monthYearFilterBox;
    @FXML
    private DatePicker dpDate;
    @FXML
    private ComboBox<Integer> cbMonth;
    @FXML
    private Spinner<Integer> spYear, spYearOnly;

    private ObservableList<ThongKe> masterData = FXCollections.observableArrayList();
    private ObservableList<ThongKeKhachHang> customerData = FXCollections.observableArrayList();

    public void initialize() {
        // Setup date/month/year filter based on role
        setupDateFilters();
        
        // Route Stats
        colMaTuyen.setCellValueFactory(new PropertyValueFactory<>("maTuyen"));
        colTenTuyen.setCellValueFactory(new PropertyValueFactory<>("tenTuyen"));
        colTongVe.setCellValueFactory(new PropertyValueFactory<>("tongVe"));
        colSoVeTrong.setCellValueFactory(new PropertyValueFactory<>("soVeTrong"));
        colTiLe.setCellValueFactory(new PropertyValueFactory<>("tyLe"));
        colSoChuyen.setCellValueFactory(new PropertyValueFactory<>("soChuyen"));
        colDoanhThu.setCellValueFactory(new PropertyValueFactory<>("doanhThu"));
        colDoanhThuTB.setCellValueFactory(new PropertyValueFactory<>("doanhThuTrungBinh"));

        // Customer Stats
        colMaKH.setCellValueFactory(new PropertyValueFactory<>("maKH"));
        colTenKH.setCellValueFactory(new PropertyValueFactory<>("tenKhachHang"));
        colSDT.setCellValueFactory(new PropertyValueFactory<>("soDienThoai"));
        colSoVeDaMua.setCellValueFactory(new PropertyValueFactory<>("soVeDaMua"));
        colTongTienKH.setCellValueFactory(new PropertyValueFactory<>("tongTien"));

        // Show full row details on double click
        tableStats.setRowFactory(tv -> {
            TableRow<ThongKe> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    ThongKe item = row.getItem();
                    String message = String.format(
                            "Mã tuyến: %s\nTên tuyến: %s\nTổng vé bán: %d\nSố vé trống: %d\nTỷ lệ lấp đầy: %.2f%%\nChuyến khởi hành: %d\nDoanh thu: %,d VNĐ\nDoanh thu TB/Chuyến: %,d VNĐ",
                            item.getMaTuyen(),
                            item.getTenTuyen(),
                            item.getTongVe(),
                            item.getSoVeTrong(),
                            item.getTyLe(),
                            item.getSoChuyen(),
                            item.getDoanhThu(),
                            item.getDoanhThuTrungBinh());
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Thông tin chuyến");
                    alert.setHeaderText(null);
                    alert.setContentText(message);
                    alert.showAndWait();
                }
            });
            return row;
        });

        // Format tỉ lệ
        colTiLe.setCellFactory(column -> new TableCell<ThongKe, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f%%", item));
                }
            }
        });

        // Format doanh thu với dấu phân cách hàng nghìn
        colDoanhThu.setCellFactory(column -> new TableCell<ThongKe, Long>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%,d VNĐ", item));
                }
            }
        });
        
        colDoanhThuTB.setCellFactory(column -> new TableCell<ThongKe, Long>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%,d VNĐ", item));
                }
            }
        });

        colTongTienKH.setCellFactory(column -> new TableCell<ThongKeKhachHang, Long>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%,d VNĐ", item));
                }
            }
        });

        loadData();
    }

    private void loadData() {
        loadingPane.setVisible(true);
        
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                ThongKeDAO dao = new ThongKeDAO();
                List<ThongKe> stats = dao.getAllStatistics();
                List<ThongKeKhachHang> customers = dao.getTopCustomers(10);
                
                javafx.application.Platform.runLater(() -> {
                    masterData.setAll(stats);
                    tableStats.setItems(masterData);
                    
                    customerData.setAll(customers);
                    tableTopCustomers.setItems(customerData);
                });
                return null;
            }
        };

        task.setOnSucceeded(e -> loadingPane.setVisible(false));
        task.setOnFailed(e -> {
            loadingPane.setVisible(false);
            e.getSource().getException().printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setHeaderText("Không thể tải dữ liệu");
            alert.setContentText(e.getSource().getException().getMessage());
            alert.showAndWait();
        });

        new Thread(task).start();
    }

    public void handleSearch() {
        applyAllFilters(false);
    }

    public void handleFilterByCode() {
        applyAllFilters(false);
    }

    public void handleFilterByRatio() {
        applyAllFilters(false);
    }

    @FXML
    private void handleExportExcel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Lưu file Excel");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File file = fileChooser.showSaveDialog(tableStats.getScene().getWindow());

        if (file != null) {
            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    try (Workbook workbook = new XSSFWorkbook()) {
                        // Sheet 1: Thống kê tuyến
                        Sheet sheet1 = workbook.createSheet("Thống kê tuyến");
                        createHeaderRow(sheet1, new String[]{"Mã Tuyến", "Tên Tuyến", "Tổng Vé", "Số Chuyến", "Số Vé Trống", "Tỉ Lệ Lấp Đầy (%)", "Doanh Thu", "Doanh Thu TB"});
                        
                        int rowNum = 1;
                        for (ThongKe tk : tableStats.getItems()) {
                            Row row = sheet1.createRow(rowNum++);
                            row.createCell(0).setCellValue(tk.getMaTuyen());
                            row.createCell(1).setCellValue(tk.getTenTuyen());
                            row.createCell(2).setCellValue(tk.getTongVe());
                            row.createCell(3).setCellValue(tk.getSoChuyen());
                            row.createCell(4).setCellValue(tk.getSoVeTrong());
                            row.createCell(5).setCellValue(tk.getTyLe());
                            row.createCell(6).setCellValue(tk.getDoanhThu());
                            row.createCell(7).setCellValue(tk.getDoanhThuTrungBinh());
                        }
                        for(int i=0; i<8; i++) sheet1.autoSizeColumn(i);

                        // Sheet 2: Top 10 Khách hàng
                        Sheet sheet2 = workbook.createSheet("Top 10 Khách Hàng");
                        createHeaderRow(sheet2, new String[]{"Mã KH", "Tên Khách Hàng", "SĐT", "Số Vé Đã Mua", "Tổng Tiền"});
                        
                        rowNum = 1;
                        for (ThongKeKhachHang tk : tableTopCustomers.getItems()) {
                            Row row = sheet2.createRow(rowNum++);
                            row.createCell(0).setCellValue(tk.getMaKH());
                            row.createCell(1).setCellValue(tk.getTenKhachHang());
                            row.createCell(2).setCellValue(tk.getSoDienThoai());
                            row.createCell(3).setCellValue(tk.getSoVeDaMua());
                            row.createCell(4).setCellValue(tk.getTongTien());
                        }
                        for(int i=0; i<5; i++) sheet2.autoSizeColumn(i);

                        try (FileOutputStream fileOut = new FileOutputStream(file)) {
                            workbook.write(fileOut);
                        }
                    }
                    return null;
                }
            };

            task.setOnSucceeded(e -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Thành công");
                alert.setHeaderText(null);
                alert.setContentText("Xuất file Excel thành công!");
                alert.showAndWait();
            });
            
            task.setOnFailed(e -> {
                e.getSource().getException().printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Lỗi");
                alert.setHeaderText(null);
                alert.setContentText("Có lỗi xảy ra khi xuất file Excel.");
                alert.showAndWait();
            });

            new Thread(task).start();
        }
    }

    private void createHeaderRow(Sheet sheet, String[] headers) {
        Row headerRow = sheet.createRow(0);
        CellStyle style = sheet.getWorkbook().createCellStyle();
        org.apache.poi.ss.usermodel.Font font = sheet.getWorkbook().createFont();
        font.setBold(true);
        style.setFont(font);
        
        for (int i = 0; i < headers.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(style);
        }
    }

    public void handleResetFilters() {
        txtSearch.clear();
        txtMaTuyen.clear();
        txtTiLe.clear();
        tableStats.setItems(masterData);
    }

    private void applyAllFilters(boolean showAlert) {
        String searchText = normalize(txtSearch.getText());
        String maText = normalize(txtMaTuyen.getText());
        String tileText = txtTiLe.getText().trim();

        List<ThongKe> filtered = masterData.stream()
                .filter(x -> normalize(x.getTenTuyen()).contains(searchText))
                .filter(x -> normalize(x.getMaTuyen()).contains(maText))
                .filter(x -> {
                    if (tileText.isEmpty())
                        return true;
                    String sanitized = tileText.replace("%", "").trim();
                    if (sanitized.isEmpty())
                        return true;
                    try {
                        double ratio = Double.parseDouble(sanitized);
                        return x.getTyLe() >= ratio;
                    } catch (NumberFormatException e) {
                        return true;
                    }
                })
                .collect(Collectors.toList());

        tableStats.setItems(FXCollections.observableArrayList(filtered));

        if (showAlert && filtered.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Kết quả lọc");
            alert.setHeaderText(null);
            alert.setContentText("Không có tuyến phù hợp.");
            alert.showAndWait();
        }
    }

    public void handleExportPDF() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Lưu file PDF");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            File file = fileChooser.showSaveDialog(tableStats.getScene().getWindow());

            if (file != null) {
                exportToPDF(file);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Thành công");
                alert.setHeaderText(null);
                alert.setContentText("Xuất file PDF thành công!");
                alert.showAndWait();
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setHeaderText(null);
            alert.setContentText("Lỗi xuất file PDF: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void exportToPDF(File file) throws Exception {
        // Create PDF with proper formatting using iText7
        PdfWriter writer = new PdfWriter(file.getAbsolutePath());
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Load Vietnamese-supporting font (Arial) using iText7 API
        PdfFont fontDefault = PdfFontFactory.createFont("C:\\Windows\\Fonts\\arial.ttf", "Identity-H",
                EmbeddingStrategy.PREFER_EMBEDDED);
        PdfFont fontBold = PdfFontFactory.createFont("C:\\Windows\\Fonts\\arialbd.ttf", "Identity-H",
                EmbeddingStrategy.PREFER_EMBEDDED);

        // Add title
        Paragraph title = new Paragraph("BÁO CÁO THỐNG KÊ TÀU")
                .setFont(fontBold)
                .setFontSize(18)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);
        document.add(title);

        // Add empty line
        document.add(new Paragraph("\n"));

        // Create table with 5 columns
        Table table = new Table(new float[] { 1.2f, 2.8f, 1.0f, 1.0f, 1.5f });
        table.setWidth(com.itextpdf.layout.properties.UnitValue.createPercentValue(100));

        // Header row
        String[] headers = { "Mã Tuyến", "Tên Tuyến", "Tổng Vé", "Tỷ Lệ", "Doanh Thu" };
        for (String header : headers) {
            Cell cell = new Cell()
                    .add(new Paragraph(header).setFont(fontBold).setBold())
                    .setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY)
                    .setTextAlignment(TextAlignment.CENTER);
            table.addCell(cell);
        }

        // Data rows
        int totalTickets = 0;
        long totalRevenue = 0L;
        for (ThongKe item : tableStats.getItems()) {
            table.addCell(new Cell().add(new Paragraph(item.getMaTuyen()).setFont(fontDefault))
                    .setTextAlignment(TextAlignment.CENTER));
            table.addCell(new Cell().add(new Paragraph(item.getTenTuyen()).setFont(fontDefault)));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(item.getTongVe())).setFont(fontDefault))
                    .setTextAlignment(TextAlignment.CENTER));
            table.addCell(new Cell().add(new Paragraph(String.format("%.2f%%", item.getTyLe())).setFont(fontDefault))
                    .setTextAlignment(TextAlignment.CENTER));
            table.addCell(new Cell().add(new Paragraph(String.format("%,d", item.getDoanhThu())).setFont(fontDefault))
                    .setTextAlignment(TextAlignment.RIGHT));

            totalTickets += item.getTongVe();
            totalRevenue += item.getDoanhThu();
        }

        // Footer row with totals
        Cell footerLabel = new Cell()
                .add(new Paragraph("TỔNG CỘNG").setFont(fontBold).setBold())
                .setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY)
                .setTextAlignment(TextAlignment.CENTER);
        table.addCell(footerLabel);

        // Empty cells in footer
        for (int i = 0; i < 2; i++) {
            Cell emptyCell = new Cell()
                    .add(new Paragraph("").setFont(fontDefault))
                    .setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY);
            table.addCell(emptyCell);
        }

        Cell footerTotal = new Cell()
                .add(new Paragraph(String.valueOf(totalTickets)).setFont(fontBold).setBold())
                .setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY)
                .setTextAlignment(TextAlignment.CENTER);
        table.addCell(footerTotal);

        Cell footerRevenue = new Cell()
                .add(new Paragraph(String.format("%,d", totalRevenue)).setFont(fontBold).setBold())
                .setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY)
                .setTextAlignment(TextAlignment.RIGHT);
        table.addCell(footerRevenue);

        document.add(table);

        // Add export info
        Paragraph footer = new Paragraph(
                "\nNgày xuất: " + new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date()))
                .setFont(fontDefault)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.RIGHT);
        document.add(footer);

        document.close();
    }

    private String normalize(String text) {
        if (text == null)
            return "";
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        String stripped = normalized.replaceAll("\\p{M}+", "");
        String replaced = stripped
                .replace('đ', 'd')
                .replace('Đ', 'D');
        return replaced.toLowerCase(Locale.ROOT).trim();
    }

    private void setupDateFilters() {
        SessionManager session = SessionManager.getInstance();
        boolean isManager = session.isAdmin();

        // Show date filter for employees, month/year for managers
        dateFilterBox.setVisible(!isManager);
        monthYearFilterBox.setVisible(isManager);

        if (!isManager) {
            dpDate.setValue(LocalDate.now());
        } else {
            // Setup month combobox
            ObservableList<Integer> months = FXCollections.observableArrayList();
            for (int i = 1; i <= 12; i++) {
                months.add(i);
            }
            cbMonth.setItems(months);
            cbMonth.setValue(LocalDate.now().getMonthValue());

            // Setup year spinner
            int currentYear = LocalDate.now().getYear();
            SpinnerValueFactory<Integer> yearFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(2020, 2100, currentYear);
            spYear.setValueFactory(yearFactory);
            spYear.setPrefWidth(100);

            SpinnerValueFactory<Integer> yearOnlyFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(2020, 2100, currentYear);
            spYearOnly.setValueFactory(yearOnlyFactory);
            spYearOnly.setPrefWidth(100);
        }
    }

    public void handleFilterByDate() {
        if (dpDate.getValue() == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Cảnh báo");
            alert.setHeaderText(null);
            alert.setContentText("Vui lòng chọn ngày!");
            alert.showAndWait();
            return;
        }

        loadingPane.setVisible(true);
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                ThongKeDAO dao = new ThongKeDAO();
                String dateStr = dpDate.getValue().toString();
                List<ThongKe> stats = dao.getStatisticsByDate(dateStr);
                List<ThongKeKhachHang> customers = dao.getTopCustomers(10);

                javafx.application.Platform.runLater(() -> {
                    masterData.setAll(stats);
                    tableStats.setItems(masterData);
                    customerData.setAll(customers);
                    tableTopCustomers.setItems(customerData);
                });
                return null;
            }
        };

        task.setOnSucceeded(e -> loadingPane.setVisible(false));
        task.setOnFailed(e -> {
            loadingPane.setVisible(false);
            e.getSource().getException().printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setHeaderText("Không thể tải dữ liệu");
            alert.setContentText(e.getSource().getException().getMessage());
            alert.showAndWait();
        });

        new Thread(task).start();
    }

    public void handleFilterByMonthYear() {
        if (cbMonth.getValue() == null || spYear.getValue() == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Cảnh báo");
            alert.setHeaderText(null);
            alert.setContentText("Vui lòng chọn tháng và năm!");
            alert.showAndWait();
            return;
        }

        loadingPane.setVisible(true);
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                ThongKeDAO dao = new ThongKeDAO();
                List<ThongKe> stats = dao.getStatisticsByMonth(cbMonth.getValue(), spYear.getValue());
                List<ThongKeKhachHang> customers = dao.getTopCustomers(10);

                javafx.application.Platform.runLater(() -> {
                    masterData.setAll(stats);
                    tableStats.setItems(masterData);
                    customerData.setAll(customers);
                    tableTopCustomers.setItems(customerData);
                });
                return null;
            }
        };

        task.setOnSucceeded(e -> loadingPane.setVisible(false));
        task.setOnFailed(e -> {
            loadingPane.setVisible(false);
            e.getSource().getException().printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setHeaderText("Không thể tải dữ liệu");
            alert.setContentText(e.getSource().getException().getMessage());
            alert.showAndWait();
        });

        new Thread(task).start();
    }

    public void handleFilterByYear() {
        if (spYearOnly.getValue() == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Cảnh báo");
            alert.setHeaderText(null);
            alert.setContentText("Vui lòng chọn năm!");
            alert.showAndWait();
            return;
        }

        loadingPane.setVisible(true);
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                ThongKeDAO dao = new ThongKeDAO();
                List<ThongKe> stats = dao.getStatisticsByYear(spYearOnly.getValue());
                List<ThongKeKhachHang> customers = dao.getTopCustomers(10);

                javafx.application.Platform.runLater(() -> {
                    masterData.setAll(stats);
                    tableStats.setItems(masterData);
                    customerData.setAll(customers);
                    tableTopCustomers.setItems(customerData);
                });
                return null;
            }
        };

        task.setOnSucceeded(e -> loadingPane.setVisible(false));
        task.setOnFailed(e -> {
            loadingPane.setVisible(false);
            e.getSource().getException().printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setHeaderText("Không thể tải dữ liệu");
            alert.setContentText(e.getSource().getException().getMessage());
            alert.showAndWait();
        });

        new Thread(task).start();
    }
}
