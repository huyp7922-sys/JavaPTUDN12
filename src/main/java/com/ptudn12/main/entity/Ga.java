package com.ptudn12.main.entity;

//Phạm Thanh Huy
public class Ga {
    private int maGa;
    private String viTriGa;
    private int mocKm; // Lấy mốc Hà Nội là chuẩn là 0km

    // Constructor không tham số
    public Ga() {
    }

    // Constructor có tham số
    public Ga(String viTriGa, int mocKm) {
        this.viTriGa = viTriGa;
        this.mocKm = mocKm;
    }

    // Getters and Setters
    public void setViTriGa(String viTriGa) {
        this.viTriGa = viTriGa;
    }

    public void setMaGa(int maGa) {
        this.maGa = maGa;
    }

    public void setMocKm(int mocKm) {
        this.mocKm = mocKm;
    }

    public String getViTriGa() {
        return viTriGa;
    }

    public int getMocKm() {
        return mocKm;
    }

    public int getMaGa() {
        return maGa;
    }

    @Override
    public String toString() {
        return "Ga{" +
                "maGa=" + maGa +
                ", viTriGa='" + viTriGa + '\'' +
                ", mocKm=" + mocKm +
                " km}";
    }
}