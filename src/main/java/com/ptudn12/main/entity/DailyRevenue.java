package com.ptudn12.main.entity;

public class DailyRevenue {
    private String ngay;
    private long doanhThu;

    public DailyRevenue(String ngay, long doanhThu) {
        this.ngay = ngay;
        this.doanhThu = doanhThu;
    }

    public String getNgay() {
        return ngay;
    }

    public void setNgay(String ngay) {
        this.ngay = ngay;
    }

    public long getDoanhThu() {
        return doanhThu;
    }

    public void setDoanhThu(long doanhThu) {
        this.doanhThu = doanhThu;
    }
}
