package com.ptudn12.main.entity;

import java.util.Objects;

public class Tau {
	private String macTau;
	private String TrangThai;

	public Tau(String macTau) {
		this.macTau = macTau;
	}

	public String getMacTau() {
		return macTau;
	}

	public void setMacTau(String macTau) {
		this.macTau = macTau;
	}

	public String getTrangThai() {
		return TrangThai;
	}

	public void setTrangThai(String trangThai) {
		TrangThai = trangThai;
	}

	@Override
	public int hashCode() {
		return Objects.hash(TrangThai, macTau);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Tau other = (Tau) obj;
		return Objects.equals(TrangThai, other.TrangThai) && Objects.equals(macTau, other.macTau);
	}

	@Override
	public String toString() {
		return "Tau [macTau=" + macTau + ", TrangThai=" + TrangThai + "]";
	}
}
