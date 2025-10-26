// File: src/main/java/com/ptudn12/main/entity/KhachHang.java
package com.ptudn12.main.entity;

import java.util.Objects;

public class KhachHang {
	private String maKH;
	private String tenKhachHang;
	private String soCCCD;
	private String hoChieu;
	private String soDienThoai;
	private int diemTich;

	public KhachHang(String maKH, String tenKhachHang, String soCCCD_HoChieu, boolean laNguoiNuocNgoai,
			String soDienThoai, int diemTich) {
		this.maKH = maKH;
		this.tenKhachHang = tenKhachHang;
		this.soDienThoai = soDienThoai;
		this.diemTich = diemTich;
		if (laNguoiNuocNgoai) {
			this.hoChieu = soCCCD_HoChieu;
			this.soCCCD = null; // Hoặc "" để đảm bảo không có dữ liệu rác
		} else {
			this.soCCCD = soCCCD_HoChieu;
			this.hoChieu = null; // Hoặc ""
		}
	}

	// --- GETTERS VÀ SETTERS ---
	// PropertyValueFactory trong JavaFX cần các getter này để lấy dữ liệu

	public String getMaKhachHang() {
		return maKH;
	}

	public void setMaKH(String maKH) {
		this.maKH = maKH;
	}

	public String getTenKhachHang() {
		return tenKhachHang;
	}

	public void setTenKhachHang(String tenKhachHang) {
		this.tenKhachHang = tenKhachHang;
	}

	public String getSoCCCD() {
		return soCCCD;
	}

	public void setSoCCCD(String soCCCD) {
		this.soCCCD = soCCCD;
	}

	public String getHoChieu() {
		return hoChieu;
	}

	public void setHoChieu(String hoChieu) {
		this.hoChieu = hoChieu;
	}

	public String getSoDienThoai() {
		return soDienThoai;
	}

	public void setSoDienThoai(String soDienThoai) {
		this.soDienThoai = soDienThoai;
	}

	public int getDiemTich() {
		return diemTich;
	}

	public void setDiemTich(int diemTich) {
		this.diemTich = diemTich;
	}

	@Override
	public int hashCode() {
		return Objects.hash(maKH);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		KhachHang other = (KhachHang) obj;
		return Objects.equals(maKH, other.maKH);
	}

	@Override
	public String toString() {
		return "KhachHang [maKH=" + maKH + ", tenKhachHang=" + tenKhachHang + "]";
	}
}