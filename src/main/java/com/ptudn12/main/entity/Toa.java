package com.ptudn12.main.entity;

import com.ptudn12.main.enums.LoaiToa;
import java.util.Objects;

public class Toa {

    private Integer maToa;
    private String tenToa;
    private LoaiToa loaiToa; // Sử dụng Enum thay vì String

    public Toa() {
    }

    public Toa(Integer maToa) {
        this.maToa = maToa;
    }

    public Toa(Integer maToa, String tenToa, LoaiToa loaiToa) {
        this.maToa = maToa;
        this.tenToa = tenToa;
        this.loaiToa = loaiToa;
    }

    // --- Getters and Setters ---

    public Integer getMaToa() {
        return maToa;
    }

    public void setMaToa(Integer maToa) {
        this.maToa = maToa;
    }

    public String getTenToa() {
        return tenToa;
    }

    public void setTenToa(String tenToa) {
        this.tenToa = tenToa;
    }

    public LoaiToa getLoaiToa() {
        return loaiToa;
    }

    public void setLoaiToa(LoaiToa loaiToa) {
        this.loaiToa = loaiToa;
    }

    // --- equals, hashCode, toString ---

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + Objects.hashCode(this.maToa);
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
        final Toa other = (Toa) obj;
        return Objects.equals(this.maToa, other.maToa);
    }

    @Override
    public String toString() {
        return "Toa{" + "maToa=" + maToa + ", tenToa=" + tenToa + ", loaiToa=" + loaiToa + '}';
    }
}
