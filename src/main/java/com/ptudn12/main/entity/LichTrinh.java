package com.ptudn12.main.entity;

import com.ptudn12.main.enums.TrangThai;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
//Phạm Thanh Huy
public class LichTrinh {
    private String maLichTrinh;           // DiemDi_DiemDen_NgayDi_STT
    private Tau tau;
    private TuyenDuong tuyenDuong;
    private LocalDateTime ngayGioKhoiHanh;  // Ngày giờ khởi hành
    private LocalDateTime ngayGioDen;       // Ngày giờ đến dự kiến
    private float giaCoBan;                 // Giá cơ bản
    private int soGheTrong;                 // Số ghế trống
    private int tongSoGhe;                  // Tổng số ghế
    private TrangThai trangThai;            // Enum TrangThai

    // Constructor không tham số
    public LichTrinh() {
    }

    // Constructor có tham số
    public LichTrinh(TuyenDuong tuyen, Tau tau, LocalDateTime ngayGioKhoiHanh) {
        this.tuyenDuong = tuyen;
        this.tau = tau;
        this.ngayGioKhoiHanh = ngayGioKhoiHanh;
        this.giaCoBan = tuyen != null ? tuyen.getGiaCoBan() : 0;
        this.tongSoGhe = tau != null ? tau.getSoLuongChoNgoi() : 0;
        this.soGheTrong = this.tongSoGhe; // Mặc định tất cả ghế đều trống
        this.trangThai = TrangThai.Nhap; // Mặc định
        this.ngayGioDen = tinhNgayGioDen();
        this.maLichTrinh = generateMaLichTrinh();
    }

    // Getters and Setters
    public void setNgayGioKhoiHanh(LocalDateTime ngayGioKhoiHanh) {
        if (ngayGioKhoiHanh.isAfter(LocalDateTime.now())) {
            this.ngayGioKhoiHanh = ngayGioKhoiHanh;
            this.ngayGioDen = tinhNgayGioDen();
        }
    }

    public void setNgayGioDen(LocalDateTime ngayGioDen) {
        this.ngayGioDen = ngayGioDen;
    }

    public void setTrangThai(TrangThai trangThai) {
        this.trangThai = trangThai;
    }

    public void setTau(Tau tau) {
        this.tau = tau;
        this.tongSoGhe = tau != null ? tau.getSoLuongChoNgoi() : 0;
    }

    public void setTuyenDuong(TuyenDuong tuyenDuong) {
        this.tuyenDuong = tuyenDuong;
        this.giaCoBan = tuyenDuong != null ? tuyenDuong.getGiaCoBan() : 0;
    }

    public void setMaLichTrinh(String maLichTrinh) {
        this.maLichTrinh = maLichTrinh;
    }

    public void setGiaCoBan(float giaCoBan) {
        this.giaCoBan = giaCoBan;
    }

    public void setSoGheTrong(int soGheTrong) {
        this.soGheTrong = soGheTrong;
    }

    public void setTongSoGhe(int tongSoGhe) {
        this.tongSoGhe = tongSoGhe;
    }

    public String getMaLichTrinh() {
        return maLichTrinh;
    }

    public TuyenDuong getTuyenDuong() {
        return tuyenDuong;
    }

    public Tau getTau() {
        return tau;
    }

    public LocalDateTime getNgayGioKhoiHanh() {
        return ngayGioKhoiHanh;
    }

    public LocalDateTime getNgayGioDen() {
        return ngayGioDen;
    }

    public float getGiaCoBan() {
        return giaCoBan;
    }

    public int getSoGheTrong() {
        return soGheTrong;
    }

    public int getTongSoGhe() {
        return tongSoGhe;
    }

    public TrangThai getTrangThai() {
        return trangThai;
    }

    /**
     * Tính ngày giờ đến dự kiến = ngày giờ khởi hành + thời gian dự kiến
     */
    private LocalDateTime tinhNgayGioDen() {
        if (ngayGioKhoiHanh == null || tuyenDuong == null) {
            return null;
        }
        return ngayGioKhoiHanh.plusHours(tuyenDuong.getThoiGianDuKien());
    }

    /**
     * Tạo mã lịch trình theo format: DiemDi_DiemDen_NgayDi_STT
     * Ví dụ: SaiGon_HaNoi_27092025_1
     */
    private String generateMaLichTrinh() {
        if (tuyenDuong == null || ngayGioKhoiHanh == null) {
            return "LT_TEMP";
        }

        String diemDi = tuyenDuong.getDiemDi().getViTriGa().replaceAll("\\s+|Ga\\s+", "");
        String diemDen = tuyenDuong.getDiemDen().getViTriGa().replaceAll("\\s+|Ga\\s+", "");
        String ngay = ngayGioKhoiHanh.format(DateTimeFormatter.ofPattern("ddMMyyyy"));
        int stt = (int) (Math.random() * 10) + 1; // Random STT 1-10 for demo

        return String.format("%s_%s_%s_%d", diemDi, diemDen, ngay, stt);
    }

    // Helper methods for TableView
    public String getMaTauDisplay() {
        return tau != null ? tau.getMacTau() : "";
    }


    public String getNgayGioKhoiHanhFormatted() {
        return ngayGioKhoiHanh != null ? 
            ngayGioKhoiHanh.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "";
    }

    public String getNgayGioDenFormatted() {
        return ngayGioDen != null ? 
            ngayGioDen.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "";
    }

    public String getGiaCoBanFormatted() {
        return String.format("%,.0fd", giaCoBan);
    }

    public String getSoGheDisplay() {
        return soGheTrong + "/" + tongSoGhe;
    }

    public String getTrangThaiDisplay() {
        return trangThai != null ? trangThai.getTenTrangThai() : "";
    }
    public String getTuyenDuongDisplay() {
        if (tuyenDuong == null) return "";

        // Nếu đã có mã tuyến được set sẵn, dùng luôn
        if (tuyenDuong.getMaTuyen() != null && !tuyenDuong.getMaTuyen().isEmpty()) {
            return tuyenDuong.getMaTuyen();
        }

        // Nếu chưa có, tự động tạo từ tên ga
        String diemDi = tuyenDuong.getDiemDi().getViTriGa().replace("Ga ", "").trim();
        String diemDen = tuyenDuong.getDiemDen().getViTriGa().replace("Ga ", "").trim();

        String maDiemDi = getShortCode(diemDi);
        String maDiemDen = getShortCode(diemDen);

        return maDiemDi + "-" + maDiemDen;
    }

    private String getShortCode(String tenGa) {
        if (tenGa == null || tenGa.isEmpty()) return "";

        switch (tenGa.trim()) {
            case "Hà Nội": return "HN";
            case "Sài Gòn": return "SG";
            case "Đà Nẵng": return "DN";
            case "Nha Trang": return "NT";
            case "Hải Phòng": return "HP";
            case "Vinh": return "V";
            case "Huế": return "HU";
            case "Quảng Ngãi": return "QN";
            default:
                return tenGa.substring(0, Math.min(2, tenGa.length())).toUpperCase();
        }
    }
    @Override
    public String toString() {
        return "LichTrinh{" +
                "maLichTrinh='" + maLichTrinh + '\'' +
                ", tau=" + (tau != null ? tau.getMacTau() : "null") +
                ", tuyenDuong=" + (tuyenDuong != null ? tuyenDuong.getMaTuyen() : "null") +
                ", ngayGioKhoiHanh=" + ngayGioKhoiHanh +
                ", ngayGioDen=" + ngayGioDen +
                ", giaCoBan=" + giaCoBan +
                ", soGheTrong=" + soGheTrong + "/" + tongSoGhe +
                ", trangThai=" + (trangThai != null ? trangThai.getTenTrangThai() : "null") +
                '}';
    }
}