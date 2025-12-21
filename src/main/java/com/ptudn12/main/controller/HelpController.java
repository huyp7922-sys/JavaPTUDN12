package com.ptudn12.main.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class HelpController {

    @FXML
    private void initialize() {
        // Khởi tạo các nút để mở file PDF
        setupPdfButtons();
    }

    private void setupPdfButtons() {
        // Phương thức này sẽ được gọi khi các nút được nhấn thông qua FXML event handlers
    }

    @FXML
    public void openGiaHaoPdf() {
        openPdfViewer("documents/HDSD_TranGiaHao.pdf", "Chức năng : bán vé, đổi vé, trả vé");
    }

    @FXML
    public void openNguyenCongHuyPdf() {
        openPdfViewer("documents/HDSD_NguyenCongHuy.pdf", "Chức năng : Nhân viên, Thống kê");
    }

    @FXML
    public void openTrinhHoangKyPdf() {
        openPdfViewer("documents/HDSD_TrinhHoangKy.pdf",
                "Chức năng : Quản lí Tàu, Quản lí khách hàng, Quản lí hoá đơn");
    }

    @FXML
    public void openPhamThanhHuyPdf() {
        openPdfViewer("documents/HDSD_PhamThanhHuy.pdf",
                "Chức năng : Đăng nhập, Quản lí lịch trình, Quản lí tuyến đường");
    }

    private void openPdfViewer(String pdfPath, String windowTitle) {
        try {
            // Tải FXML của PdfViewerController
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/pdf-viewer.fxml"));
            Parent root = loader.load();

            // Lấy controller
            PdfViewerController controller = loader.getController();

            // Tải tài liệu PDF từ resources
            File pdfFile = new File(getClass().getResource("/" + pdfPath).toURI());
            controller.loadDocument(pdfFile);

            // Tạo stage mới cho PDF viewer
            Stage pdfStage = new Stage();
            pdfStage.setTitle(windowTitle);
            pdfStage.setScene(new Scene(root, 900, 700));
            controller.setStage(pdfStage);
            pdfStage.show();
        } catch (IOException e) {
            System.err.println("Error opening PDF: " + pdfPath);
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
