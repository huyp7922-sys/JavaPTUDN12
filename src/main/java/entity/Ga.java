package entity;

public class Ga {
    private String maGa;
    private String tenGa;
    private double mocKm = 0; 

    public Ga(String maGa, String tenGa, double mocKm) {
        this.maGa = maGa;
        this.tenGa = tenGa;
        this.mocKm = mocKm;
    }

    // Getters and Setters
    public String getMaGa() { return maGa; }
    public void setMaGa(String maGa) { this.maGa = maGa; }
    public String getTenGa() { return tenGa; }
    public void setTenGa(String tenGa) { this.tenGa = tenGa; }
    public double getKhoangCachTuHaNoi() { return mocKm; }
    public void setKhoangCachTuHaNoi(double khoangCachTuHaNoi) { this.mocKm = khoangCachTuHaNoi; }
}