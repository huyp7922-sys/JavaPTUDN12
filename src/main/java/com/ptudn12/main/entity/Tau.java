package com.ptudn12.main.entity;

public class Tau {
    private String macTau;
    private int soLuongChoNgoi;
    // TODO: Add other fields based on your specification

    public Tau() {
    }

    public Tau(String macTau) {
        this.macTau = macTau;
    }

    public Tau(String macTau, int soLuongChoNgoi) {
        this.macTau = macTau;
        this.soLuongChoNgoi = soLuongChoNgoi;
    }

    public String getMacTau() {
        return macTau;
    }

    public void setMacTau(String macTau) {
        this.macTau = macTau;
    }

    public int getSoLuongChoNgoi() {
        return soLuongChoNgoi;
    }

    public void setSoLuongChoNgoi(int soLuongChoNgoi) {
        this.soLuongChoNgoi = soLuongChoNgoi;
    }

    @Override
    public String toString() {
        return macTau;
    }
}