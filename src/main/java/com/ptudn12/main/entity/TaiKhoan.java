package com.ptudn12.main.entity;

public class TaiKhoan {
    private String username;
    private String password;
    private String role;
    private String maNV;
    private String trangThai; // "Đang hoạt động" | "Đã khóa"

    public TaiKhoan(String username, String password, String role, String maNV, String trangThai) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.maNV = maNV;
        this.trangThai = trangThai;
    }

    public TaiKhoan() {}

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getMaNV() { return maNV; }
    public void setMaNV(String maNV) { this.maNV = maNV; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}
