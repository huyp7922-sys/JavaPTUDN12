package com.ptudn12.main.entity;

import java.time.LocalDate;

public class NhanVien {
    private String maNhanVien;
    private String tenNhanVien;
    private String soCCCD;
    private LocalDate ngaySinh;
    private boolean gioiTinh; // true = Nam, false = Nữ
    private String soDienThoai;
    private String email;
    private boolean chucVu; // true = Quản lý, false = Nhân viên
    private String tinhTrangCV; // "đang làm", "tạm nghỉ", "đã nghỉ"
    
    // Constructors
    public NhanVien() {}
    
    
    public NhanVien(String maNhanVien, String tenNhanVien, String soCCCD, 
                   LocalDate ngaySinh, boolean gioiTinh, boolean chucVu,
                   String soDienThoai, String email, String tinhTrangCV) {
       this.maNhanVien = maNhanVien;
       this.tenNhanVien = tenNhanVien;
       this.soCCCD = soCCCD;
       this.ngaySinh = ngaySinh;
       this.gioiTinh = gioiTinh;
       this.chucVu = chucVu;
       this.soDienThoai = soDienThoai;
       this.email = email;
       this.tinhTrangCV = tinhTrangCV;
   }
    
    // Getters and Setters
    public String getMaNhanVien() {
        return maNhanVien;
    }
    
    public void setMaNhanVien(String maNhanVien) {
        this.maNhanVien = maNhanVien;
    }
    
    public String getTenNhanVien() {
        return tenNhanVien;
    }
    
    public void setTenNhanVien(String tenNhanVien) {
        this.tenNhanVien = tenNhanVien;
    }
    
    public String getSoCCCD() {
        return soCCCD;
    }
    
    public void setSoCCCD(String soCCCD) {
        this.soCCCD = soCCCD;
    }
    
    public LocalDate getNgaySinh() {
        return ngaySinh;
    }
    
    public void setNgaySinh(LocalDate ngaySinh) {
        this.ngaySinh = ngaySinh;
    }
    
    public boolean isGioiTinh() {
        return gioiTinh;
    }
    
    public void setGioiTinh(boolean gioiTinh) {
        this.gioiTinh = gioiTinh;
    }
    
    public String getSoDienThoai() {
        return soDienThoai;
    }
    
    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public boolean isChucVu() {
        return chucVu;
    }
    
    public void setChucVu(boolean chucVu) {
        this.chucVu = chucVu;
    }
    
    public String getTinhTrangCV() {
        return tinhTrangCV;
    }
    
    public void setTinhTrangCV(String tinhTrangCV) {
        this.tinhTrangCV = tinhTrangCV;
    }
    
    // Helper methods for display
    public String getGioiTinhText() {
        return gioiTinh ? "Nam" : "Nữ";
    }
    
    public String getChucVuText() {
        return chucVu ? "Quản lý" : "Nhân viên";
    }
    
    @Override
    public String toString() {
        return "NhanVien{" +
                "maNhanVien='" + maNhanVien + '\'' +
                ", tenNhanVien='" + tenNhanVien + '\'' +
                ", soCCCD='" + soCCCD + '\'' +
                ", ngaySinh=" + ngaySinh +
                ", gioiTinh=" + gioiTinh +
                ", soDienThoai='" + soDienThoai + '\'' +
                ", email='" + email + '\'' +
                ", chucVu=" + chucVu +
                ", tinhTrangCV='" + tinhTrangCV + '\'' +
                '}';
    }
}