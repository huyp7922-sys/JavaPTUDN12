package com.ptudn12.main.enums;

public enum LoaiCho {
	// Các hằng số
	GheCung("Ghế ngồi cứng", 1.0), GheNgoiMem("Ghế ngồi mềm thường", 1.2), Giuong4("Giường nằm khoang 4", 1.4),
	Giuong6("Giường nằm khoang 6", 1.6), GiuongVIP("Giường nằm VIP", 2.0);

	// Thuộc tính
	private final String tenLoaiCho;
	private final double heSoChoNgoi;

	// Constructor
	private LoaiCho(String tenLoaiCho, double heSoChoNgoi) {
		this.tenLoaiCho = tenLoaiCho;
		this.heSoChoNgoi = heSoChoNgoi;
	}

	// Getter
	public String getTenLoaiCho() {
		return tenLoaiCho;
	}

	public double getHeSoChoNgoi() {
		return heSoChoNgoi;
	}

	// toString
	@Override
	public String toString() {
		return tenLoaiCho;
	}
}
