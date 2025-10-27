package com.ptudn12.main.entity;

import java.time.LocalDate;

public class NhanVien {
    private String maNV;
    private String hoTen;
    private String cccd;
    private LocalDate ngaySinh;
    private String gioiTinh;
    private String chucVu;
    private String sdt;
    private String email;
    private String trangThai;

    public NhanVien(String maNV, String hoTen, String cccd, LocalDate ngaySinh, String gioiTinh,
                    String chucVu, String sdt, String email, String trangThai) {
        this.maNV = maNV;
        this.hoTen = hoTen;
        this.cccd = cccd;
        this.ngaySinh = ngaySinh;
        this.gioiTinh = gioiTinh;
        this.chucVu = chucVu;
        this.sdt = sdt;
        this.email = email;
        this.trangThai = trangThai;
    }

    public NhanVien() {}

    public String getMaNV() { return maNV; }
    public void setMaNV(String maNV) { this.maNV = maNV; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public String getCccd() { return cccd; }
    public void setCccd(String cccd) { this.cccd = cccd; }

    public LocalDate getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(LocalDate ngaySinh) { this.ngaySinh = ngaySinh; }

    public String getGioiTinh() { return gioiTinh; }
    public void setGioiTinh(String gioiTinh) { this.gioiTinh = gioiTinh; }

    public String getChucVu() { return chucVu; }
    public void setChucVu(String chucVu) { this.chucVu = chucVu; }

    public String getSdt() { return sdt; }
    public void setSdt(String sdt) { this.sdt = sdt; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}
