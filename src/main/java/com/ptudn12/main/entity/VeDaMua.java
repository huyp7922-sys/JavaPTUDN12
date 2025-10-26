// File: src/main/java/com/ptudn12/main/entity/VeDaMua.java
package com.ptudn12.main.entity;

public class VeDaMua {
	private int stt;
	private String ngayMuaVe;
	private String maVe;
	private String macTau;
	private String hanhTrinh;
	private String thoiGianDiDen;
	private String toa;
	private int soCho;
	private String giaVe;

	public VeDaMua(int stt, String ngayMuaVe, String maVe, String macTau, String hanhTrinh, String thoiGianDiDen,
			String toa, int soCho, String giaVe) {
		this.stt = stt;
		this.ngayMuaVe = ngayMuaVe;
		this.maVe = maVe;
		this.macTau = macTau;
		this.hanhTrinh = hanhTrinh;
		this.thoiGianDiDen = thoiGianDiDen;
		this.toa = toa;
		this.soCho = soCho;
		this.giaVe = giaVe;
	}

	// --- GETTERS ---
	// Các getters này là bắt buộc để PropertyValueFactory hoạt động

	public int getStt() {
		return stt;
	}

	public String getNgayMuaVe() {
		return ngayMuaVe;
	}

	public String getMaVe() {
		return maVe;
	}

	public String getMacTau() {
		return macTau;
	}

	public String getHanhTrinh() {
		return hanhTrinh;
	}

	public String getThoiGianDiDen() {
		return thoiGianDiDen;
	}

	public String getToa() {
		return toa;
	}

	public int getSoCho() {
		return soCho;
	}

	public String getGiaVe() {
		return giaVe;
	}
}