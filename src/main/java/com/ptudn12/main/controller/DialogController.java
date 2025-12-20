package com.ptudn12.main.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import java.io.File;
import java.io.IOException;

public class DialogController {

    @FXML
    public void closeDialog(ActionEvent event) {
        Button btn = (Button) event.getSource();
        Stage stage = (Stage) btn.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void handleButtonHover(javafx.scene.input.MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle(
                "-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 40; -fx-background-radius: 5; -fx-cursor: hand;");
    }

    @FXML
    public void handleButtonExit(javafx.scene.input.MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle(
                "-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 40; -fx-background-radius: 5; -fx-cursor: hand;");
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
