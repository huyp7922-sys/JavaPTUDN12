package com.ptudn12.main.entity;

import com.ptudn12.main.enums.TrangThai;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
//Phạm Thanh Huy
public class LichTrinh {
    private String maLichTrinh;           // DiemDi_DiemDen_NgayDi_STT
    private Tau tau;
    private TuyenDuong tuyenDuong;
    private LocalDateTime ngayGioKhoiHanh;  // Ngày giờ khởi hành
    private LocalDateTime ngayGioDen;       // Ngày giờ đến dự kiến
    private float giaCoBan;                 // Giá cơ bản
    private TrangThai trangThai;            // Enum TrangThai
    // ✅ Xóa soGheTrong và tongSoGhe - sẽ query động khi cần

    // Constructor không tham số
    public LichTrinh() {
    }

    // Constructor có tham số
    public LichTrinh(TuyenDuong tuyen, Tau tau, LocalDateTime ngayGioKhoiHanh) {
        this.tuyenDuong = tuyen;
        this.tau = tau;
        this.ngayGioKhoiHanh = ngayGioKhoiHanh;
        this.giaCoBan = tuyen != null ? tuyen.getGiaCoBan() : 0;
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
        // ✅ Không tự động set tongSoGhe nữa - sẽ được set từ DAO qua query Tau_Toa
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
        // Query động từ database để lấy số ghế trống/tổng số ghế
        if (maLichTrinh == null || maLichTrinh.isEmpty()) {
            return "N/A";
        }
        
        try {
            com.ptudn12.main.dao.LichTrinhDAO dao = new com.ptudn12.main.dao.LichTrinhDAO();
            int[] soGhe = dao.laySoGheCuaLichTrinh(maLichTrinh);
            return soGhe[1] + "/" + soGhe[0]; // số ghế trống / tổng số ghế
        } catch (Exception e) {
            return "Lỗi query";
        }
    }

    public String getTrangThaiDisplay() {
        return trangThai != null ? trangThai.getTenTrangThai() : "";
    }
    public String getTuyenDuongDisplay() {
        if (tuyenDuong == null) return "";

        // Luôn tạo format DiemDi-DiemDen (ví dụ: HN-SG) thay vì hiển thị mã số
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
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.maLichTrinh);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LichTrinh other = (LichTrinh) obj;
        return Objects.equals(this.maLichTrinh, other.maLichTrinh);
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
                ", trangThai=" + (trangThai != null ? trangThai.getTenTrangThai() : "null") +
                '}';
    }
}