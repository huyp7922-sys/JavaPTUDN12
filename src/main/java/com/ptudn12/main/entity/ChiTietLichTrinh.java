package com.ptudn12.main.entity;

import com.ptudn12.main.enums.LoaiCho;

public class ChiTietLichTrinh {
    private String maChiTietLichTrinh;
    private LichTrinh lichTrinh;
    private Cho cho;
    private double giaChoNgoi;
    private LoaiCho loaiCho;

    // Constructor đầy đủ
    public ChiTietLichTrinh(String maChiTietLichTrinh, LichTrinh lichTrinh, Cho cho) {
        this.maChiTietLichTrinh = maChiTietLichTrinh;
        this.lichTrinh = lichTrinh;
        this.cho = cho;
        this.giaChoNgoi = tinhGiaChoNgoi();
    }

    public ChiTietLichTrinh() {
    }

    private double tinhGiaChoNgoi() {
        if (lichTrinh != null && lichTrinh.getTuyenDuong() != null && loaiCho != null) {
            double giaTuyenCoBan = lichTrinh.getTuyenDuong().tinhGiaCoBan();
            double heSoCho = loaiCho.getHeSoChoNgoi();
            return giaTuyenCoBan * heSoCho;
        }
        return 0;
    }

    // --- Getters and Setters ---
    public String getMaChiTietLichTrinh() { return maChiTietLichTrinh; }
    public void setMaChiTietLichTrinh(String maChiTietLichTrinh) { this.maChiTietLichTrinh = maChiTietLichTrinh; }
    
    public LichTrinh getLichTrinh() { return lichTrinh; }
    public void setLichTrinh(LichTrinh lichTrinh) { this.lichTrinh = lichTrinh; }
    
    public Cho getCho() { return cho; }
    public void setCho(Cho cho) { this.cho = cho; }
    
    public double getGiaChoNgoi() { return giaChoNgoi; }
    
    // Để DAO gán giá trị thực tế lấy từ DB vào
    public void setGiaChoNgoi(double giaChoNgoi) { this.giaChoNgoi = giaChoNgoi; }
    
    public TuyenDuong getTuyenDuong() { 
        return (lichTrinh != null) ? lichTrinh.getTuyenDuong() : null; 
    }
}
