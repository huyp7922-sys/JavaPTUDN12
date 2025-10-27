package com.ptudn12.main.entity;

import java.util.Objects;

import com.ptudn12.main.enums.LoaiToa;

public class Toa {
	// Thuộc tính
	private String maToa; // duy nhất (id)
	private int soThuTu; // > 0 và < 17
	private LoaiToa loaiToa;

	// Constructor có tham số LoaiToa
	public Toa(LoaiToa loaiToa) {
		this.soThuTu = 0; // mặc định
		this.loaiToa = loaiToa;
	}

	// Constructor mặc định
	public Toa() {
		this.soThuTu = 0;
		this.loaiToa = LoaiToa.NgoiMem; // mặc định
	}

	// Getter & Setter
	public String getMaToa() {
		return maToa;
	}

	public void setMaToa(String maToa) {
		this.maToa = maToa;
	}

	public int getSoThuTu() {
		return soThuTu;
	}

	public void setSoThuTu(int soThuTu) {
		if (soThuTu > 0 && soThuTu < 17) {
			this.soThuTu = soThuTu;
		} else {
			throw new IllegalArgumentException("Số thứ tự phải lớn hơn 0 và nhỏ hơn 17.");
		}
	}

	public LoaiToa getLoaiToa() {
		return loaiToa;
	}

	public void setLoaiToa(LoaiToa loaiToa) {
		this.loaiToa = loaiToa;
	}

	@Override
	public String toString() {
		return "Toa [maToa=" + maToa + ", soThuTu=" + soThuTu + ", loaiToa=" + loaiToa + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(loaiToa, maToa, soThuTu);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Toa other = (Toa) obj;
		return loaiToa == other.loaiToa && Objects.equals(maToa, other.maToa) && soThuTu == other.soThuTu;
	}
}
