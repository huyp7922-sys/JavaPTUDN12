package com.ptudn12.main.entity;

public class TaiKhoan {
    private NhanVien nhanVien;
    private String matKhau;
    private String trangThaiTK; // "danghoatdong", "tamngung", "ngunghan"

    public TaiKhoan() {}

    public TaiKhoan(NhanVien nhanVien, String matKhau, String trangThaiTK) {
        this.nhanVien = nhanVien;
        this.matKhau = matKhau;
        this.trangThaiTK = trangThaiTK;
    }

    public NhanVien getNhanVien() {
        return nhanVien;
    }

    public void setNhanVien(NhanVien nhanVien) {
        this.nhanVien = nhanVien;
    }

    public String getMatKhau() {
        return matKhau;
    }

    public void setMatKhau(String matKhau) {
        this.matKhau = matKhau;
    }

    public String getTrangThaiTK() {
        return trangThaiTK;
    }

    public void setTrangThaiTK(String trangThaiTK) {
        this.trangThaiTK = trangThaiTK;
    }

    @Override
    public String toString() {
        return "TaiKhoan{" +
                "nhanVien=" + (nhanVien != null ? nhanVien.getMaNhanVien() : "null") +
                ", trangThaiTK='" + trangThaiTK + '\'' +
                '}';
    }
}
