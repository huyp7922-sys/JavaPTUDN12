package com.ptudn12.main.entity;

public class TaiKhoan {
    private String maNhanVien;
    private String matKhau;
    private String trangThaiTK; // "danghoatdong", "tamngung", "ngunghan"
    
    // Constructors
    public TaiKhoan() {}
    
    public TaiKhoan(String maNhanVien, String matKhau, String trangThaiTK) {
        this.maNhanVien = maNhanVien;
        this.matKhau = matKhau;
        this.trangThaiTK = trangThaiTK;
    }
    
    // Getters and Setters
    public String getMaNhanVien() {
        return maNhanVien;
    }
    
    public void setMaNhanVien(String maNhanVien) {
        this.maNhanVien = maNhanVien;
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
                "maNhanVien='" + maNhanVien + '\'' +
                ", trangThaiTK='" + trangThaiTK + '\'' +
                '}';
    }
}