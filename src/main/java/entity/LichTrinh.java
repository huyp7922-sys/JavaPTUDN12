package entity;

import java.time.LocalDateTime;
import java.util.List;

import enums.TrangThai;

public class LichTrinh {
    private String maLichTrinh;
    private TuyenDuong tuyenDuong;
    private Tau tau;
    private LocalDateTime thoiGianKhoiHanh;
    private LocalDateTime thoiGianKetThuc;
    private List<ChiTietLichTrinh> chiTietLichTrinhs;
    private TrangThai trangThai ;
    
    public LichTrinh(String maLichTrinh, TuyenDuong tuyenDuong, Tau tau, LocalDateTime thoiGianKhoiHanh, LocalDateTime thoiGianKetThuc) {
        this.maLichTrinh = maLichTrinh;
        this.tuyenDuong = tuyenDuong;
        this.tau = tau;
        this.thoiGianKhoiHanh = thoiGianKhoiHanh;
        this.thoiGianKetThuc = thoiGianKetThuc;
    }

    // Getters and Setters
    public String getMaLichTrinh() { return maLichTrinh; }
    public void setMaLichTrinh(String maLichTrinh) { this.maLichTrinh = maLichTrinh; }
    public TuyenDuong getTuyenDuong() { return tuyenDuong; }
    public void setTuyenDuong(TuyenDuong tuyenDuong) { this.tuyenDuong = tuyenDuong; }
    public Tau getTau() { return tau; }
    public void setTau(Tau tau) { this.tau = tau; }
    public LocalDateTime getThoiGianKhoiHanh() { return thoiGianKhoiHanh; }
    public void setThoiGianKhoiHanh(LocalDateTime thoiGianKhoiHanh) { this.thoiGianKhoiHanh = thoiGianKhoiHanh; }
    public LocalDateTime getThoiGianKetThuc() { return thoiGianKetThuc; }
    public void setThoiGianKetThuc(LocalDateTime thoiGianKetThuc) { this.thoiGianKetThuc = thoiGianKetThuc; }
    public List<ChiTietLichTrinh> getChiTietLichTrinhs() { return chiTietLichTrinhs; }
    public void setChiTietLichTrinhs(List<ChiTietLichTrinh> chiTietLichTrinhs) { this.chiTietLichTrinhs = chiTietLichTrinhs; }
}