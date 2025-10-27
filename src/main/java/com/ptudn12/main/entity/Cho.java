package com.ptudn12.main.entity;

import com.ptudn12.main.enums.LoaiCho;
import java.util.Objects;

public class Cho {
    private Integer maCho;
    private Toa toa;
    private LoaiCho loaiCho;
    private Integer soThuTu;

    public Cho() {
    }
    
    public Cho(Integer maCho) {
        this.maCho = maCho;
    }

    public Cho(Integer maCho, Toa toa, LoaiCho loaiCho, Integer soThuTu) {
        this.maCho = maCho;
        this.toa = toa;
        this.loaiCho = loaiCho;
        this.soThuTu = soThuTu;
    }

    // --- Getters and Setters ---

    public Integer getMaCho() {
        return maCho;
    }

    public void setMaCho(Integer maCho) {
        this.maCho = maCho;
    }

    public Toa getToa() {
        return toa;
    }

    public void setToa(Toa toa) {
        this.toa = toa;
    }

    public LoaiCho getLoaiCho() {
        return loaiCho;
    }

    public void setLoaiCho(LoaiCho loaiCho) {
        this.loaiCho = loaiCho;
    }

    public Integer getSoThuTu() {
        return soThuTu;
    }

    public void setSoThuTu(Integer soThuTu) {
        this.soThuTu = soThuTu;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + Objects.hashCode(this.maCho);
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
        final Cho other = (Cho) obj;
        return Objects.equals(this.maCho, other.maCho);
    }

    @Override
    public String toString() {
        return "Cho{" + "maCho=" + maCho + ", toa=" + (toa != null ? toa.getMaToa() : "null") + ", loaiCho=" + loaiCho + ", soThuTu=" + soThuTu + '}';
    }
}
