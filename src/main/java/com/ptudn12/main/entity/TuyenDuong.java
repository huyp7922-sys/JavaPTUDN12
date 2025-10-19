package com.ptudn12.main.entity;

import com.ptudn12.main.enums.TrangThai;

//Phạm Thanh Huy
public class TuyenDuong {
    private String maTuyen; // Sinh tự động, duy nhất
    private Ga diemDi;      // Ga xuất phát
    private Ga diemDen;     // Ga kết thúc
    private int thoiGianDuKien; // Thời gian (giờ), >= 1
    private float giaCoBan;     // Tính theo km * hệ số khoảng cách, >= 0
    private TrangThai trangThai;   // Enum TrangThai

    // Constructor không tham số
    public TuyenDuong() {
    }

    // Constructor có tham số (không có maTuyen)
    public TuyenDuong(Ga diemDi, Ga diemDen, int thoiGianDuKien) {
        this.diemDi = diemDi;
        this.diemDen = diemDen;
        this.thoiGianDuKien = thoiGianDuKien;
        this.giaCoBan = tinhGiaCoBan();
        this.trangThai = TrangThai.Nhap; // Mặc định
    }

    // Getters and Setters
    public void setDiemDi(Ga diemDi) {
        this.diemDi = diemDi;
    }

    public void setDiemDen(Ga diemDen) {
        this.diemDen = diemDen;
    }

    public void setThoiGianDuKien(int thoiGianDuKien) {
        if (thoiGianDuKien >= 1) {
            this.thoiGianDuKien = thoiGianDuKien;
        }
    }

    public void setTrangThai(TrangThai trangThai) {
        this.trangThai = trangThai;
    }

    public void setMaTuyen(String maTuyen) {
        this.maTuyen = maTuyen;
    }

    public void setGiaCoBan(float giaCoBan) {
        this.giaCoBan = giaCoBan;
    }

    public Ga getDiemDi() {
        return diemDi;
    }

    public Ga getDiemDen() {
        return diemDen;
    }

    public int getThoiGianDuKien() {
        return thoiGianDuKien;
    }

    public TrangThai getTrangThai() {
        return trangThai;
    }

    public String getMaTuyen() {
        return maTuyen;
    }

    public float getGiaCoBan() {
        return giaCoBan;
    }

    /**
     * Tính giá cơ bản = Số km * hệ số km
     * Hệ số khoảng cách:
     * - 0–100 km: 1.1
     * - 101–300 km: 1.25
     * - 301–800 km: 1.5
     * - >800 km: 2.0
     */
    public float tinhGiaCoBan() {
        if (diemDi == null || diemDen == null) {
            return 0;
        }

        int soKm = Math.abs(diemDen.getMocKm() - diemDi.getMocKm());
        float heSoKhoangCach;

        if (soKm <= 100) {
            heSoKhoangCach = 1.1f;
        } else if (soKm <= 300) {
            heSoKhoangCach = 1.25f;
        } else if (soKm <= 800) {
            heSoKhoangCach = 1.5f;
        } else {
            heSoKhoangCach = 2.0f;
        }

        return soKm * heSoKhoangCach * 500; // Giá cơ bản: 500 đồng/km
    }

    public int getSoKm() {
        if (diemDi == null || diemDen == null) {
            return 0;
        }
        return Math.abs(diemDen.getMocKm() - diemDi.getMocKm());
    }

    // Helper method for TableView
    public String getTenDiemDi() {
        return diemDi != null ? diemDi.getViTriGa() : "";
    }

    public String getTenDiemDen() {
        return diemDen != null ? diemDen.getViTriGa() : "";
    }

    public String getTrangThaiDisplay() {
        return trangThai != null ? trangThai.getTenTrangThai() : "";
    }

    public String getThoiGianDuKienFormatted() {
        return thoiGianDuKien + "h";
    }

    public String getGiaCoBanFormatted() {
        return String.format("%,.0f đ", giaCoBan);
    }

    @Override
    public String toString() {
        return "TuyenDuong{" +
                "maTuyen='" + maTuyen + '\'' +
                ", diemDi=" + (diemDi != null ? diemDi.getViTriGa() : "null") +
                ", diemDen=" + (diemDen != null ? diemDen.getViTriGa() : "null") +
                ", soKm=" + getSoKm() +
                ", thoiGianDuKien=" + thoiGianDuKien + " giờ" +
                ", giaCoBan=" + String.format("%,.0f", giaCoBan) + " đ" +
                ", trangThai=" + (trangThai != null ? trangThai.getTenTrangThai() : "null") +
                '}';
    }
}