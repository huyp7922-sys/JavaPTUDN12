package com.ptudn12.main.controller;

import com.ptudn12.main.dao.ThongKeDAO;
import com.ptudn12.main.entity.ThongKe;
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
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import java.io.File;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class StatisticsManagementController {

    @FXML
    private TableView<ThongKe> tableStats;
    @FXML
    private TableColumn<ThongKe, String> colMaTuyen, colTenTuyen;
    @FXML
    private TableColumn<ThongKe, Integer> colTongVe, colSoChuyen;
    @FXML
    private TableColumn<ThongKe, Double> colTiLe;
    @FXML
    private TableColumn<ThongKe, Long> colDoanhThu;

    @FXML
    private TextField txtSearch, txtMaTuyen, txtTiLe;
    @FXML
    private Button btnExportPDF;

    private ObservableList<ThongKe> masterData = FXCollections.observableArrayList();

    public void initialize() {
        colMaTuyen.setCellValueFactory(new PropertyValueFactory<>("maTuyen"));
        colTenTuyen.setCellValueFactory(new PropertyValueFactory<>("tenTuyen"));
        colTongVe.setCellValueFactory(new PropertyValueFactory<>("tongVe"));
        colTiLe.setCellValueFactory(new PropertyValueFactory<>("tyLe"));
        colSoChuyen.setCellValueFactory(new PropertyValueFactory<>("soChuyen"));
        colDoanhThu.setCellValueFactory(new PropertyValueFactory<>("doanhThu"));

        // Show full row details on double click
        tableStats.setRowFactory(tv -> {
            TableRow<ThongKe> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    ThongKe item = row.getItem();
                    String message = String.format(
                            "Mã tuyến: %s\nTên tuyến: %s\nTổng vé bán: %d\nTỷ lệ lấp đầy: %.2f%%\nChuyến khởi hành: %d\nDoanh thu ước tính: %,d VNĐ",
                            item.getMaTuyen(),
                            item.getTenTuyen(),
                            item.getTongVe(),
                            item.getTyLe(),
                            item.getSoChuyen(),
                            item.getDoanhThu());
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Thông tin chuyến");
                    alert.setHeaderText(null);
                    alert.setContentText(message);
                    alert.showAndWait();
                }
            });
            return row;
        });

        // Format tỉ lệ lắp đầy với ký hiệu %
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

        loadData();
    }

    private void loadData() {
        try {
            ThongKeDAO dao = new ThongKeDAO();
            masterData.setAll(dao.getAllStatistics());
            tableStats.setItems(masterData);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public void handleApplyFilters() {
        applyAllFilters(true);
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

        // Create table with 6 columns
        Table table = new Table(new float[] { 1.2f, 2.8f, 1.0f, 1.0f, 1.0f, 1.5f });
        table.setWidth(com.itextpdf.layout.properties.UnitValue.createPercentValue(100));

        // Header row
        String[] headers = { "Mã Tuyến", "Tên Tuyến", "Tổng Vé", "Tỷ Lệ", "Chuyến", "Doanh Thu" };
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
            table.addCell(new Cell().add(new Paragraph(String.valueOf(item.getSoChuyen())).setFont(fontDefault))
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
        for (int i = 0; i < 3; i++) {
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
}
