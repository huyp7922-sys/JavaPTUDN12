package com.ptudn12.main.controller;

import com.ptudn12.main.dao.NhanVienDAO;
import com.ptudn12.main.entity.NhanVien;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class EmployeeManagementController {

    @FXML private TableView<NhanVien> employeeTable;
    @FXML private TableColumn<NhanVien, String> maNVCol;
    @FXML private TableColumn<NhanVien, String> hoTenCol;
    @FXML private TableColumn<NhanVien, String> cccdCol;
    @FXML private TableColumn<NhanVien, String> ngaySinhCol;
    @FXML private TableColumn<NhanVien, String> gioiTinhCol;
    @FXML private TableColumn<NhanVien, String> chucVuCol;
    @FXML private TableColumn<NhanVien, String> sdtCol;
    @FXML private TableColumn<NhanVien, String> emailCol;
    @FXML private TableColumn<NhanVien, String> trangThaiCol;

    @FXML private TextField filterField;

    private final NhanVienDAO dao = new NhanVienDAO();
    private ObservableList<NhanVien> data;

    @FXML
    public void initialize() {
        maNVCol.setCellValueFactory(new PropertyValueFactory<>("maNV"));
        hoTenCol.setCellValueFactory(new PropertyValueFactory<>("hoTen"));
        cccdCol.setCellValueFactory(new PropertyValueFactory<>("cccd"));
        ngaySinhCol.setCellValueFactory(new PropertyValueFactory<>("ngaySinh"));
        gioiTinhCol.setCellValueFactory(new PropertyValueFactory<>("gioiTinh"));
        chucVuCol.setCellValueFactory(new PropertyValueFactory<>("chucVu"));
        sdtCol.setCellValueFactory(new PropertyValueFactory<>("soDienThoai"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        trangThaiCol.setCellValueFactory(new PropertyValueFactory<>("trangThai"));

        loadData();  

        filterField.textProperty().addListener((obs, oldVal, newVal) -> filterData(newVal));
    }

    private void loadData() {
        data = FXCollections.observableArrayList(dao.getAllNhanVien());
        employeeTable.setItems(data);
    }

    private void filterData(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            employeeTable.setItems(data);
            return;
        }
        var filtered = data.filtered(nv ->
            nv.getTenNhanVien().toLowerCase().contains(keyword.toLowerCase()) ||
            String.valueOf(nv.getMaNhanVien()).contains(keyword)
        );
        employeeTable.setItems(filtered);
    }

    @FXML
    private void addEmployee() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ptudn12/main/view/model-employee-management.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Thêm nhân viên");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();

            loadData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void editEmployee() {
        new Alert(Alert.AlertType.INFORMATION, "Chức năng đang phát triển nhé!").show();
    }

    @FXML
    private void deleteEmployee() {
        NhanVien nv = employeeTable.getSelectionModel().getSelectedItem();
        if (nv == null) {
            new Alert(Alert.AlertType.WARNING, "Bạn chưa chọn nhân viên để xóa").show();
            return;
        }
//
//        if (dao.khoaNhanVien(nv.getMaNhanVien())) {
//            loadData();
//        }
    }
}
