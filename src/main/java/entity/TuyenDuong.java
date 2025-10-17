package entity;

import enums.TrangThai;

public class TuyenDuong {
    private String maTuyen;
    private Ga gaDi;
    private Ga gaDen;
    private double soKm;
    private double heSoKhoangCach;
    private TrangThai trangThai ; 

    public TuyenDuong(String maTuyen, Ga gaDi, Ga gaDen, double soKm, double heSoKhoangCach) {
        this.maTuyen = maTuyen;
        this.gaDi = gaDi;
        this.gaDen = gaDen;
        this.soKm = soKm;
        this.heSoKhoangCach = heSoKhoangCach;
    }

    public double tinhGiaCoBan() {
        return soKm * 500 * heSoKhoangCach;
    }

    // Getters and Setters
    public String getMaTuyen() { return maTuyen; }
    public void setMaTuyen(String maTuyen) { this.maTuyen = maTuyen; }
    public Ga getGaDi() { return gaDi; }
    public void setGaDi(Ga gaDi) { this.gaDi = gaDi; }
    public Ga getGaDen() { return gaDen; }
    public void setGaDen(Ga gaDen) { this.gaDen = gaDen; }
    public double getSoKm() { return soKm; }
    public void setSoKm(double soKm) { this.soKm = soKm; }
    public double getHeSoKhoangCach() { return heSoKhoangCach; }
    public void setHeSoKhoangCach(double heSoKhoangCach) { this.heSoKhoangCach = heSoKhoangCach; }
}
