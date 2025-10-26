/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ptudn12.main.dao;

/**
 *
 * @author fo3cp
 */

import com.ptudn12.main.entity.Ga;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Ga_DAO {
    private final ObservableList<Ga> danhSachGa = FXCollections.observableArrayList();

    public Ga_DAO() {
        danhSachGa.addAll(
                new Ga("GA001", "Ga Hà Nội", 0.0),
                new Ga("GA002", "Ga Đà Nẵng", 791.0),
                new Ga("GA003", "Ga Nha Trang", 1315.0),
                new Ga("GA004", "Ga Sài Gòn", 1726.0)
        );
    }

    public ObservableList<Ga> getAll() {
        return danhSachGa;
    }
}
