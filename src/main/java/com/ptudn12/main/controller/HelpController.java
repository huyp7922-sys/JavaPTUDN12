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
        // Initialize buttons to open PDF files
        setupPdfButtons();
    }

    private void setupPdfButtons() {
        // This will be called when buttons are clicked via FXML event handlers
    }

    @FXML
    public void openGiaHaoPdf() {
        openPdfViewer("documents/GiaHao_HDSD.pdf", "Gia Hào - Hướng Dẫn Sử Dụng");
    }

    @FXML
    public void openNguyenCongHuyPdf() {
        openPdfViewer("documents/HDSD - Nguyễn Công Huy.pdf", "Nguyễn Công Huy - Hướng Dẫn Sử Dụng");
    }

    @FXML
    public void openTrinhHoangKyPdf() {
        openPdfViewer("documents/HDSD - Trịnh Hoàng Kỳ.pdf", "Trịnh Hoàng Kỳ - Hướng Dẫn Sử Dụng");
    }

    @FXML
    public void openPhamThanhHuyPdf() {
        openPdfViewer("documents/HDSD_PhamThanhHuy.pdf", "Phạm Thành Huy - Hướng Dẫn Sử Dụng");
    }

    private void openPdfViewer(String pdfPath, String windowTitle) {
        try {
            // Load PdfViewerController FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/pdf-viewer.fxml"));
            Parent root = loader.load();
            
            // Get the controller
            PdfViewerController controller = loader.getController();
            
            // Load the PDF document from resources
            File pdfFile = new File(getClass().getResource("/" + pdfPath).toURI());
            controller.loadDocument(pdfFile);
            
            // Create a new stage for the PDF viewer
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
