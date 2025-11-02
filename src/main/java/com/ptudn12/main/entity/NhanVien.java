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
    private Boolean chucVu; // TRUE (1) = Quản lý, FALSE (0) = Nhân viên  
    private String tinhTrangCV; // "đang làm", "tạm nghỉ", "đã nghỉ"
    
    // Constructors
    public NhanVien() {}
    
    public NhanVien(String maNhanVien, String tenNhanVien, String soCCCD, 
                   LocalDate ngaySinh, boolean gioiTinh, Boolean chucVu,  
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
    
    public Boolean getChucVu() {  
        return chucVu;
    }
    
    public void setChucVu(Boolean chucVu) {  
        this.chucVu = chucVu;
    }
    
    public String getChucVuText() {
        if (chucVu == null) return "Không xác định";
        return chucVu ? "Quản lý" : "Nhân viên";
    }
    
    public boolean isQuanLy() {
        return chucVu != null && chucVu;
    }
    
    public String getGioiTinhText() {
        return gioiTinh ? "Nam" : "Nữ";
    }
    
    // ... các getter/setter khác giữ nguyên
    
    public String getMaNhanVien() { return maNhanVien; }
    public void setMaNhanVien(String maNhanVien) { this.maNhanVien = maNhanVien; }
    
    public String getTenNhanVien() { return tenNhanVien; }
    public void setTenNhanVien(String tenNhanVien) { this.tenNhanVien = tenNhanVien; }
    
    public String getSoCCCD() { return soCCCD; }
    public void setSoCCCD(String soCCCD) { this.soCCCD = soCCCD; }
    
    public LocalDate getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(LocalDate ngaySinh) { this.ngaySinh = ngaySinh; }
    
    public boolean isGioiTinh() { return gioiTinh; }
    public void setGioiTinh(boolean gioiTinh) { this.gioiTinh = gioiTinh; }
    
    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getTinhTrangCV() { return tinhTrangCV; }
    public void setTinhTrangCV(String tinhTrangCV) { this.tinhTrangCV = tinhTrangCV; }
    
    @Override
    public String toString() {
        return "NhanVien{" +
                "maNhanVien='" + maNhanVien + '\'' +
                ", tenNhanVien='" + tenNhanVien + '\'' +
                ", chucVu=" + getChucVuText() +
                ", tinhTrangCV='" + tinhTrangCV + '\'' +
                '}';
    }
}