package com.ptudn12.main.entity;

public class ScheduleStatus {
    private String trangThai;
    private int soLuong;

    public ScheduleStatus(String trangThai, int soLuong) {
        this.trangThai = trangThai;
        this.soLuong = soLuong;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
    }

    // Hàm để convert trangThai thành text Việt
    public String getTrangThaiVi() {
        switch (trangThai) {
            case "ChuaKhoiHanh":
                return "Chưa khởi hành";
            case "DangChay":
                return "Đang chạy";
            case "DaKetThuc":
                return "Đã kết thúc";
            case "TamHoan":
                return "Tạm hoãn";
            case "TamNgung":
                return "Tạm ngừng";
            default:
                return trangThai;
        }
    }
}
