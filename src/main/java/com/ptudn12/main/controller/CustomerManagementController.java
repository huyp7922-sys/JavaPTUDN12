package com.ptudn12.main.controller;

import java.io.IOException;
import java.util.List;
import com.ptudn12.main.dao.KhachHangDAO;
import com.ptudn12.main.entity.KhachHang;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class CustomerManagementController {

    @FXML private TableView<KhachHang> customerTable;
    @FXML private TableColumn<KhachHang, String> idColumn;
    @FXML private TableColumn<KhachHang, String> nameColumn;
    @FXML private TableColumn<KhachHang, String> cccdColumn;
    @FXML private TableColumn<KhachHang, String> passportColumn;
    @FXML private TableColumn<KhachHang, String> phoneColumn;
    @FXML private TableColumn<KhachHang, Integer> pointsColumn;
    @FXML private TextField searchField;
    @FXML private Label customerCountLabel;

    private final KhachHangDAO khachHangDAO = new KhachHangDAO();
    private ObservableList<KhachHang> masterData = FXCollections.observableArrayList();
    private FilteredList<KhachHang> filteredData;

    @FXML
    public void initialize() {
        // Cấu hình cột
        idColumn.setCellValueFactory(new PropertyValueFactory<>("maKhachHang"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("tenKhachHang"));
        cccdColumn.setCellValueFactory(new PropertyValueFactory<>("soCCCD"));
        passportColumn.setCellValueFactory(new PropertyValueFactory<>("hoChieu"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("soDienThoai"));
        pointsColumn.setCellValueFactory(new PropertyValueFactory<>("diemTich"));

        // Thiết lập danh sách lọc
        filteredData = new FilteredList<>(masterData, p -> true);
        customerTable.setItems(filteredData);

        loadDataFromDatabase();
    }

    private void loadDataFromDatabase() {
        List<KhachHang> danhSach = khachHangDAO.layTatCaKhachHang();
        masterData.setAll(danhSach);
        updateCount();
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText().toLowerCase().trim();
        
        filteredData.setPredicate(kh -> {
            if (keyword.isEmpty()) return true;

            return kh.getTenKhachHang().toLowerCase().contains(keyword) ||
                   (kh.getSoCCCD() != null && kh.getSoCCCD().contains(keyword)) ||
                   (kh.getHoChieu() != null && kh.getHoChieu().toLowerCase().contains(keyword)) ||
                   (kh.getSoDienThoai() != null && kh.getSoDienThoai().contains(keyword));
        });
        updateCount();
    }

    @FXML
    private void handleShowAll() {
        searchField.clear();
        handleSearch();
    }

    @FXML
    private void handleRefresh() {
        loadDataFromDatabase();
        searchField.clear();
        handleSearch();
        showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Đã tải lại danh sách khách hàng!");
    }

    @FXML
    private void handleAddCustomer() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/add-customer-dialog.fxml"));
            Scene scene = new Scene(loader.load());
            AddCustomerDialogController controller = loader.getController();

            Stage stage = new Stage();
            stage.setTitle("Thêm Khách Hàng Mới");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.showAndWait();

            if (controller.isSaveClicked()) loadDataFromDatabase();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở form thêm!");
        }
    }

    @FXML
    private void handleEditCustomer() {
        KhachHang selected = customerTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn khách hàng cần sửa!");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/add-customer-dialog.fxml"));
            Scene scene = new Scene(loader.load());
            AddCustomerDialogController controller = loader.getController();
            controller.loadCustomerForEdit(selected);

            Stage stage = new Stage();
            stage.setTitle("Sửa Thông Tin");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.showAndWait();

            if (controller.isSaveClicked()) {
                loadDataFromDatabase();
                customerTable.refresh();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleViewHistory() {
        KhachHang selected = customerTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn khách hàng!");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/customer-history-view.fxml"));
            Scene scene = new Scene(loader.load());
            CustomerHistoryController controller = loader.getController();
            controller.loadCustomerData(selected);

            Stage stage = new Stage();
            stage.setTitle("Lịch Sử: " + selected.getTenKhachHang());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateCount() {
        if (customerCountLabel != null) {
            customerCountLabel.setText("Hiển thị: " + filteredData.size() + " / Tổng: " + masterData.size());
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