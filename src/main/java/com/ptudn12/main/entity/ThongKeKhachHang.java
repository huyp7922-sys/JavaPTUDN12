package com.ptudn12.main.entity;

public class ThongKeKhachHang {
    private String maKH;
    private String tenKhachHang;
    private String soDienThoai;
    private int soVeDaMua;
    private long tongTien;

    public ThongKeKhachHang(String maKH, String tenKhachHang, String soDienThoai, int soVeDaMua, long tongTien) {
        this.maKH = maKH;
        this.tenKhachHang = tenKhachHang;
        this.soDienThoai = soDienThoai;
        this.soVeDaMua = soVeDaMua;
        this.tongTien = tongTien;
    }

    public String getMaKH() { return maKH; }
    public String getTenKhachHang() { return tenKhachHang; }
    public String getSoDienThoai() { return soDienThoai; }
    public int getSoVeDaMua() { return soVeDaMua; }
    public long getTongTien() { return tongTien; }
}
