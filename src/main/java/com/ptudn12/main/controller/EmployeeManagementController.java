package com.ptudn12.main.controller;

import com.ptudn12.main.dao.NhanVienDAO;
import com.ptudn12.main.dao.TaiKhoanDAO;
import com.ptudn12.main.entity.NhanVien;
import com.ptudn12.main.entity.TaiKhoan;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

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
    
    private NhanVienDAO nhanVienDAO;
    private TaiKhoanDAO taiKhoanDAO;
    private ObservableList<NhanVien> employeeList;
    private FilteredList<NhanVien> filteredData;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    @FXML
    public void initialize() {
        nhanVienDAO = new NhanVienDAO();
        taiKhoanDAO = new TaiKhoanDAO();
        
        setupTableColumns();
        loadEmployeeData();
        setupFilter();
    }
    
    // Thiết lập các cột của bảng
    private void setupTableColumns() {
        maNVCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getMaNhanVien()));
        
        hoTenCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getTenNhanVien()));
        
        cccdCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getSoCCCD()));
        
        ngaySinhCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getNgaySinh().format(DATE_FORMATTER)));
        
        gioiTinhCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getGioiTinhText()));
        
        chucVuCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getChucVuText()));
        
        sdtCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getSoDienThoai()));
        
        emailCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getEmail()));
        
        trangThaiCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getTinhTrangCV()));
    }
    
    // Load dữ liệu nhân viên
    private void loadEmployeeData() {
        employeeList = FXCollections.observableArrayList(nhanVienDAO.getAllNhanVien());
        filteredData = new FilteredList<>(employeeList, p -> true);
        employeeTable.setItems(filteredData);
    }
    
    // Thiết lập bộ lọc
    private void setupFilter() {
        filterField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(nhanVien -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                
                String lowerCaseFilter = newValue.toLowerCase();
                
                if (nhanVien.getMaNhanVien().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (nhanVien.getTenNhanVien().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (nhanVien.getSoCCCD().contains(lowerCaseFilter)) {
                    return true;
                } else if (nhanVien.getSoDienThoai().contains(lowerCaseFilter)) {
                    return true;
                } else if (nhanVien.getEmail().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                
                return false;
            });
        });
    }
    
    // Thêm nhân viên mới
    @FXML
    private void addEmployee() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/model-employee-management.fxml"));
            Parent root = loader.load();
            
            EmployeeFormController controller = loader.getController();
            controller.setMode(EmployeeFormController.FormMode.ADD);
            controller.setParentController(this);
            
            Stage stage = new Stage();
            stage.setTitle("Thêm Nhân Viên Mới");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở form thêm nhân viên!");
        }
    }
    
    // Sửa thông tin nhân viên
    @FXML
    private void editEmployee() {
        NhanVien selected = employeeTable.getSelectionModel().getSelectedItem();
        
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn nhân viên cần sửa!");
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/model-employee-management.fxml"));
            Parent root = loader.load();
            
            EmployeeFormController controller = loader.getController();
            controller.setMode(EmployeeFormController.FormMode.EDIT);
            controller.setParentController(this);
            controller.loadEmployeeData(selected);
            
            Stage stage = new Stage();
            stage.setTitle("Sửa Thông Tin Nhân Viên");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở form sửa nhân viên!");
        }
    }
    
    @FXML
    private void deleteEmployee() {
        NhanVien selected = employeeTable.getSelectionModel().getSelectedItem();
        
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn nhân viên cần xóa!");
            return;
        }
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác nhận");
        confirmAlert.setHeaderText("Xóa nhân viên");
        confirmAlert.setContentText("Bạn có chắc chắn muốn xóa nhân viên: " + selected.getTenNhanVien() + "?");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (nhanVienDAO.delete(selected.getMaNhanVien())) {
            
                if (taiKhoanDAO.exists(selected.getMaNhanVien())) {  
                    taiKhoanDAO.updateStatus(selected.getMaNhanVien(), "ngunghan");  
                }
                
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Xóa nhân viên thành công!");
                refreshTable();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xóa nhân viên!");
            }
        }
    }
    // Refresh bảng dữ liệu
    public void refreshTable() {
        employeeList.setAll(nhanVienDAO.getAllNhanVien());
    }
    
    // Hiển thị thông báo
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}