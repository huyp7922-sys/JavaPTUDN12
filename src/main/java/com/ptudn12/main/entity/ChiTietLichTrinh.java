package entity;

import java.util.Objects;

public class ChiTietLichTrinh {
    private String maChiTietLichTrinh;
    private LichTrinh lichTrinh;
    private Cho cho;
    private double giaChoNgoi;

    public ChiTietLichTrinh(String maChiTietLichTrinh, LichTrinh lichTrinh, Cho cho) {
        this.maChiTietLichTrinh = maChiTietLichTrinh;
        this.lichTrinh = lichTrinh;
        this.cho = cho;
        this.giaChoNgoi = tinhGiaChoNgoi();
    }

    private double tinhGiaChoNgoi() {
//        double giaTuyenCoBan = lichTrinh.getTuyenDuong().tinhGiaCoBan();
//        double heSoCho = cho.getHeSoCho();
//        return giaTuyenCoBan * heSoCho;
    	return 1.0 ; 
    }

    // Getters and Setters
    public String getMaChiTietLichTrinh() { return maChiTietLichTrinh; }
    public void setMaChiTietLichTrinh(String maChiTietLichTrinh) { this.maChiTietLichTrinh = maChiTietLichTrinh; }
    public LichTrinh getLichTrinh() { return lichTrinh; }
    public void setLichTrinh(LichTrinh lichTrinh) { this.lichTrinh = lichTrinh; }
    public Cho getCho() { return cho; }
    public void setCho(Cho cho) { this.cho = cho; }
    public double getGiaChoNgoi() { return giaChoNgoi; }
    public TuyenDuong getTuyenDuong() { return lichTrinh.getTuyenDuong(); }
}
