package com.ptudn12.main.enums;

public enum LoaiToa {
	// Các hằng số
	NgoiCung("Toa ngồi cứng", 72), NgoiMem("Toa ngồi mềm", 64), Giuong6("Toa giường khoang 6", 42),
	Giuong4("Toa giường khoang 4", 28), GiuongVIP("Toa giường VIP", 14);

	// Thuộc tính
	private final String tenLoaiToa;
	private final int soChoMacDinh;

	// Constructor
	private LoaiToa(String tenLoaiToa, int soChoMacDinh) {
		this.tenLoaiToa = tenLoaiToa;
		this.soChoMacDinh = soChoMacDinh;
	}

	// Getter
	public String getTenLoaiToa() {
		return tenLoaiToa;
	}

	public int getSoChoMacDinh() {
		return soChoMacDinh;
	}

	// Phương thức toString()
	@Override
	public String toString() {
		return tenLoaiToa;
	}
}
