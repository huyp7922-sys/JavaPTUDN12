package com.ptudn12.main.entity;

import java.util.Objects;

import com.ptudn12.main.enums.LoaiCho;

public class Cho {
	// Thuộc tính
	private String maCho; // duy nhất (id)
	private int soThuTu; // > 0 và <= SoChoMacDinh
	private LoaiCho loaiCho;

	// Constructor có tham số LoaiCho
	public Cho(LoaiCho loaiCho) {
		this.soThuTu = 0; // mặc định
		this.loaiCho = loaiCho;
	}

	// Getter & Setter
	public String getMaCho() {
		return maCho;
	}

	public void setMaCho(String maCho) {
		this.maCho = maCho;
	}

	public int getSoThuTu() {
		return soThuTu;
	}

	public void setSoThuTu(int soThuTu) {
		this.soThuTu = soThuTu;
	}

	public LoaiCho getLoaiCho() {
		return loaiCho;
	}

	public void setLoaiCho(LoaiCho loaiCho) {
		this.loaiCho = loaiCho;
	}

	@Override
	public String toString() {
		return "Cho [maCho=" + maCho + ", soThuTu=" + soThuTu + ", loaiCho=" + loaiCho + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(loaiCho, maCho, soThuTu);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Cho other = (Cho) obj;
		return loaiCho == other.loaiCho && Objects.equals(maCho, other.maCho) && soThuTu == other.soThuTu;
	}

}
