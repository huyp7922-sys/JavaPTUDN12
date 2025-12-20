/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ptudn12.main.entity;

import java.util.Objects;

/**
 *
 * @author fo3cp
 */
public class ChiTietToa {
    private Integer maChiTietToa;
    private Tau tau;
    private Toa toa;
    private Cho cho;
    private Integer soThuTu;

    public ChiTietToa() {
    }

    public ChiTietToa(Integer maChiTietToa, Tau tau, Toa toa, Cho cho, Integer soThuTu) {
        this.maChiTietToa = maChiTietToa;
        this.tau = tau;
        this.toa = toa;
        this.cho = cho;
        this.soThuTu = soThuTu;
    }

    public Integer getMaChiTietToa() {
        return maChiTietToa;
    }

    public void setMaChiTietToa(Integer maChiTietToa) {
        this.maChiTietToa = maChiTietToa;
    }

    public Tau getTau() {
        return tau;
    }

    public void setTau(Tau tau) {
        this.tau = tau;
    }

    public Toa getToa() {
        return toa;
    }

    public void setToa(Toa toa) {
        this.toa = toa;
    }

    public Cho getCho() {
        return cho;
    }

    public void setCho(Cho cho) {
        this.cho = cho;
    }

    public Integer getSoThuTu() {
        return soThuTu;
    }

    public void setSoThuTu(Integer soThuTu) {
        this.soThuTu = soThuTu;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + Objects.hashCode(this.maChiTietToa);
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
        final ChiTietToa other = (ChiTietToa) obj;
        return Objects.equals(this.maChiTietToa, other.maChiTietToa);
    }

    @Override
    public String toString() {
        return "ChiTietToa{" + "maChiTietToa=" + maChiTietToa + ", tau=" + tau + ", toa=" + toa + ", cho=" + cho + ", soThuTu=" + soThuTu + '}';
    }
}
