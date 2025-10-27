package com.ptudn12.main.controller;

import com.ptudn12.main.dao.NhanVienDAO;
import com.ptudn12.main.entity.NhanVien;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;

public class NhanVienTableController {

    @FXML private TableView<NhanVien> tableNhanVien;
    private static TableView<NhanVien> STATIC_TABLE;

    public void initialize() {
        STATIC_TABLE = tableNhanVien;
        refreshTable();
    }

    public static void refreshTable() {
        if (STATIC_TABLE == null) return;
        ObservableList<NhanVien> list = FXCollections.observableArrayList(new NhanVienDAO().getAllNhanVien());
        STATIC_TABLE.setItems(list);
    }
}
