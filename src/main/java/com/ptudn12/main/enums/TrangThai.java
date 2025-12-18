/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ptudn12.main.enums;

//Phạm Thanh Huy
public enum TrangThai {
    Nhap("Nhap"),
    ChuaKhoiHanh("ChuaKhoiHanh"),
    DangChay("DangChay"),
    TamNgung("TamNgung"),
    TamHoan("TamHoan"),
    SanSang("SanSang"),
    DaKetThuc("DaKetThuc");

    private String tenTrangThai;

    TrangThai(String trangThai) {
        this.tenTrangThai = trangThai;
    }

    public String getTenTrangThai() {
        return tenTrangThai;
    }

    @Override
    public String toString() {
        return tenTrangThai;
    }
}