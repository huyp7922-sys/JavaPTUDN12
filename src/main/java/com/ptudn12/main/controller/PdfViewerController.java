package com.ptudn12.main.controller;

import java.awt.image.BufferedImage;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.print.PrintServiceLookup;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;
import org.apache.pdfbox.rendering.PDFRenderer;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.stage.Stage;

public class PdfViewerController implements Initializable {

	@FXML
	private ScrollPane scrollPane;
	@FXML
	private ImageView pageImageView;
	@FXML
	private Label pageLabel;
	@FXML
	private Label zoomLabel;
	@FXML
	private Button prevPageButton;
	@FXML
	private Button nextPageButton;

	private Stage stage;
	private PDDocument document;
	private List<Image> renderedPages;
	private int currentPageIndex = 0;

	// 1. Định nghĩa mức zoom cơ bản (35%) mà chúng ta coi là 100% trên UI
	private static final double BASE_ZOOM_FACTOR = 0.35;
	// 2. Mỗi lần zoom sẽ tăng/giảm 10% của mức zoom CƠ BẢN
	private static final double ZOOM_INCREMENT = BASE_ZOOM_FACTOR * 0.10;
	// 3. Khởi tạo zoom hiện tại bằng mức zoom cơ bản
	private double currentZoomFactor = BASE_ZOOM_FACTOR;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// Ban đầu không có gì để hiển thị
		pageImageView.setImage(null);

		// 4. Thêm listener cho sự kiện lăn chuột trên ScrollPane
//		scrollPane.setOnScroll((ScrollEvent event) -> {
//			// Chỉ zoom khi người dùng giữ phím Control
//			if (event.isControlDown()) {
//				if (event.getDeltaY() > 0) { // Lăn lên -> Phóng to
//					handleZoomIn();
//				} else { // Lăn xuống -> Thu nhỏ
//					handleZoomOut();
//				}
//				// Ngăn sự kiện cuộn trang mặc định của ScrollPane
//				event.consume();
//			}
//		});

		// Chặn sự kiện lăn chuột trước khi ScrollPane xử lý nó
		scrollPane.addEventFilter(ScrollEvent.SCROLL, (ScrollEvent event) -> {
			// Chỉ hành động khi phím Control được giữ
			if (event.isControlDown()) {
				if (event.getDeltaY() > 0) { // Lăn lên -> Phóng to
					handleZoomIn();
				} else { // Lăn xuống -> Thu nhỏ
					handleZoomOut();
				}
				// Quan trọng: Báo cho hệ thống rằng sự kiện đã được xử lý xong
				// và không cần truyền đi nữa (ngăn ScrollPane cuộn).
				event.consume();
			}
			// Nếu không giữ Ctrl, chúng ta không làm gì cả,
			// để ScrollPane cuộn trang như bình thường.
		});
	}

	public void setStage(Stage stage) {
		this.stage = stage;
	}

	/**
	 * Phương thức chính: Tải một file PDF, render nó thành hình ảnh và hiển thị.
	 */
	public void loadDocument(File pdfFile) throws IOException {
		this.document = PDDocument.load(pdfFile);
		this.renderedPages = new ArrayList<>();
		PDFRenderer pdfRenderer = new PDFRenderer(document);

		// Render từng trang với độ phân giải cao (150 DPI)
		for (int i = 0; i < document.getNumberOfPages(); ++i) {
			BufferedImage bim = pdfRenderer.renderImageWithDPI(i, 150);
			Image fxImage = SwingFXUtils.toFXImage(bim, null);
			this.renderedPages.add(fxImage);
		}

		displayPage(0);
		applyZoom();
	}

	private void displayPage(int pageIndex) {
		if (pageIndex < 0 || pageIndex >= renderedPages.size())
			return;

		this.currentPageIndex = pageIndex;
		Image pageImage = renderedPages.get(pageIndex);
		pageImageView.setImage(pageImage);
		pageLabel.setText("Trang " + (currentPageIndex + 1) + " / " + renderedPages.size());

		// Căn lại kích thước zoom cho trang mới
		applyZoom();
	}

	@FXML
	private void handlePreviousPage() {
		displayPage(currentPageIndex - 1);
	}

	@FXML
	private void handleNextPage() {
		displayPage(currentPageIndex + 1);
	}

	@FXML
	private void handleZoomIn() {
//		currentZoomFactor += 0.1;
//		applyZoom();

		// Giới hạn zoom tối đa là 250% so với mức cơ bản
		if (currentZoomFactor < BASE_ZOOM_FACTOR * 2.5) {
			currentZoomFactor += ZOOM_INCREMENT;
			applyZoom();
		}
	}

	@FXML
	private void handleZoomOut() {
//		if (currentZoomFactor > 0.2) {
//			currentZoomFactor -= 0.1;
//			applyZoom();
//		}

		// Giới hạn zoom tối thiểu là 25% so với mức cơ bản
		if (currentZoomFactor > BASE_ZOOM_FACTOR * 0.25) {
			currentZoomFactor -= ZOOM_INCREMENT;
			applyZoom();
		}
	}

	private void applyZoom() {
		if (pageImageView.getImage() != null) {
			pageImageView.setFitWidth(pageImageView.getImage().getWidth() * currentZoomFactor);
			pageImageView.setPreserveRatio(true);
			updateZoomLabel();
		}
	}

	private void updateZoomLabel() {
		// zoomLabel.setText(Math.round(currentZoomFactor * 100) + "%");

		long percentage = Math.round((currentZoomFactor / BASE_ZOOM_FACTOR) * 100);
		zoomLabel.setText(percentage + "%");
	}

	@FXML
	private void handleFinalPrint() {
		if (document == null)
			return;
		try {
			if (PrintServiceLookup.lookupDefaultPrintService() == null) {
				showAlert(Alert.AlertType.ERROR, "Lỗi In ấn", "Không tìm thấy máy in nào.");
				return;
			}
			PrinterJob job = PrinterJob.getPrinterJob();
			job.setJobName("In Hóa Đơn");
			job.setPageable(new PDFPageable(document));

			if (job.printDialog()) {
				job.print();
				showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã gửi tín hiệu in đến máy in!");
			} else {
				showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Tác vụ in đã được hủy.");
			}
		} catch (Exception e) {
			e.printStackTrace();
			showAlert(Alert.AlertType.ERROR, "Lỗi In ấn", "Đã xảy ra lỗi: " + e.getMessage());
		}
	}

	/**
	 * Dọn dẹp tài nguyên khi cửa sổ bị đóng.
	 */
	public void closeDocument() {
		try {
			if (document != null) {
				document.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void showAlert(Alert.AlertType type, String title, String message) {
		Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}
}