package com.ptudn12.main.controller;

import com.ptudn12.main.dao.VeTauDAO;
import com.ptudn12.main.entity.VeTau;
import com.ptudn12.main.utils.ReportManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import javax.swing.SwingUtilities;
import java.util.List;

public class PrintListController {

    @FXML private TableView<VeTau> tblVe;
    @FXML private TableColumn<VeTau, String> colMaVe;
    @FXML private TableColumn<VeTau, String> colHanhTrinh;
    @FXML private TableColumn<VeTau, String> colCho;
    @FXML private TableColumn<VeTau, String> colTenKhach;
    @FXML private TableColumn<VeTau, Void> colAction; // Cột chứa nút bấm

    @FXML private Button btnDong;

    private final VeTauDAO veTauDAO = new VeTauDAO();
    private Stage dialogStage;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    @FXML
    public void initialize() {
        // Map dữ liệu vào cột
        colMaVe.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getMaVe()));
        
        colHanhTrinh.setCellValueFactory(cell -> {
            VeTau v = cell.getValue();
            if (v.getChiTietLichTrinh() != null && v.getChiTietLichTrinh().getLichTrinh() != null && v.getChiTietLichTrinh().getLichTrinh().getTuyenDuong() != null) {
                String di = v.getChiTietLichTrinh().getLichTrinh().getTuyenDuong().getDiemDi().getViTriGa();
                String den = v.getChiTietLichTrinh().getLichTrinh().getTuyenDuong().getDiemDen().getViTriGa();
                return new SimpleStringProperty(di + " -> " + den);
            }
            return new SimpleStringProperty("N/A");
        });

        colCho.setCellValueFactory(cell -> {
            VeTau v = cell.getValue();
            if (v.getChiTietLichTrinh() != null && v.getChiTietLichTrinh().getCho() != null) {
                String toa = v.getChiTietLichTrinh().getCho().getToa().getTenToa();
                int ghe = v.getChiTietLichTrinh().getCho().getSoThuTu();
                return new SimpleStringProperty(toa + " - Ghế " + ghe);
            }
            return new SimpleStringProperty("-");
        });
        
        colTenKhach.setCellValueFactory(cell -> {
             if (cell.getValue().getKhachHang() != null) {
                 return new SimpleStringProperty(cell.getValue().getKhachHang().getTenKhachHang());
             }
             return new SimpleStringProperty("-");
        });

        // TẠO NÚT "XEM / IN" TRONG BẢNG
        colAction.setCellFactory(new Callback<>() {
            @Override
            public TableCell<VeTau, Void> call(final TableColumn<VeTau, Void> param) {
                return new TableCell<>() {
                    private final Button btn = new Button("In Vé");

                    {
                        btn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 11px;");
                        btn.setOnAction(event -> {
                            VeTau ve = getTableView().getItems().get(getIndex());
                            handlePrintPreview(ve);
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(btn);
                        }
                    }
                };
            }
        });
    }

    // Hàm nhận danh sách ID vé từ Step4Controller
    public void setTicketIds(List<String> ticketIds) {
        ObservableList<VeTau> listVe = FXCollections.observableArrayList();
        for (String id : ticketIds) {
            // Dùng hàm getVeTauDetail (đã viết ở VeTauDAO) để lấy full thông tin
            VeTau ve = veTauDAO.getVeTauDetail(id);
            if (ve != null) {
                listVe.add(ve);
            }
        }
        tblVe.setItems(listVe);
    }

    private void handlePrintPreview(VeTau ve) {
        // Mở JasperViewer trên luồng Swing
        SwingUtilities.invokeLater(() -> {
            ReportManager.printVeTau(ve);
        });
    }

    @FXML
    private void closeDialog() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }
}