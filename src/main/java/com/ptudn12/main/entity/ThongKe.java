package com.ptudn12.main.entity;

public class ThongKe {
    private String maTuyen, tenTuyen;
    private int tongVe, soChuyen;
    private double tyLe;
    private long doanhThu;

    public ThongKe(String maTuyen, String tenTuyen, int tongVe, double tyLe,
                             int soChuyen, long doanhThu) {
        this.maTuyen = maTuyen;
        this.tenTuyen = tenTuyen;
        this.tongVe = tongVe;
        this.tyLe = tyLe;
        this.soChuyen = soChuyen;
        this.doanhThu = doanhThu;
    }

    public String getMaTuyen() { return maTuyen; }
    public String getTenTuyen() { return tenTuyen; }
    public int getTongVe() { return tongVe; }
    public double getTyLe() { return tyLe; }
    public int getSoChuyen() { return soChuyen; }
    public long getDoanhThu() { return doanhThu; }
}
