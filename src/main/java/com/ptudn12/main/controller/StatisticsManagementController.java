package com.ptudn12.main.controller;

import com.ptudn12.main.dao.ThongKeDAO;
import com.ptudn12.main.entity.ThongKe;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class StatisticsManagementController {

    @FXML private TableView<ThongKe> tableStats;
    @FXML private TableColumn<ThongKe, String> colMaTuyen, colTenTuyen;
    @FXML private TableColumn<ThongKe, Integer> colTongVe, colSoChuyen;
    @FXML private TableColumn<ThongKe, Double> colTiLe;
    @FXML private TableColumn<ThongKe, Long> colDoanhThu;

    @FXML private TextField txtSearch;

    private ObservableList<ThongKe> masterData = FXCollections.observableArrayList();

    public void initialize() {
        colMaTuyen.setCellValueFactory(new PropertyValueFactory<>("maTuyen"));
        colTenTuyen.setCellValueFactory(new PropertyValueFactory<>("tenTuyen"));
        colTongVe.setCellValueFactory(new PropertyValueFactory<>("tongVe"));
        colTiLe.setCellValueFactory(new PropertyValueFactory<>("tyLe"));
        colSoChuyen.setCellValueFactory(new PropertyValueFactory<>("soChuyen"));
        colDoanhThu.setCellValueFactory(new PropertyValueFactory<>("doanhThu"));

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
        String filter = txtSearch.getText().toLowerCase();
        tableStats.setItems(masterData.filtered(
                x -> x.getTenTuyen().toLowerCase().contains(filter)
        ));
    }

    public void handleFilter() {
        System.out.println("Chức năng lọc thời gian sẽ mở tại đây...");
    }
}
