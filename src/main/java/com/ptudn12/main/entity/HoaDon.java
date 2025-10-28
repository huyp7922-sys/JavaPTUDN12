package com.ptudn12.main.entity;

import java.time.LocalDateTime;
import java.util.Objects;

import com.ptudn12.main.enums.LoaiHoaDon;

public class HoaDon {
	private String maHoaDon;
	private NhanVien nhanVien;
	private KhachHang khachHang;
	private LocalDateTime ngayLap;

	private LoaiHoaDon loaiHoaDon;

	public HoaDon() {
		super();
	}

	public HoaDon(String maHoaDon) {
		super();
		this.maHoaDon = maHoaDon;
	}

	public HoaDon(String maHoaDon, NhanVien nhanVien, KhachHang khachHang, LocalDateTime ngayLap,
			LoaiHoaDon loaiHoaDon) {
		this.maHoaDon = maHoaDon;
		this.nhanVien = nhanVien;
		this.khachHang = khachHang;
		this.ngayLap = ngayLap;
		this.loaiHoaDon = loaiHoaDon;
	}

	public String getMaHoaDon() {
		return maHoaDon;
	}

	public void setMaHoaDon(String maHoaDon) {
		this.maHoaDon = maHoaDon;
	}

	public NhanVien getNhanVien() {
		return nhanVien;
	}

	public void setNhanVien(NhanVien nhanVien) {
		this.nhanVien = nhanVien;
	}

	public KhachHang getKhachHang() {
		return khachHang;
	}

	public void setKhachHang(KhachHang khachHang) {
		this.khachHang = khachHang;
	}

	public LocalDateTime getNgayLap() {
		return ngayLap;
	}

	public void setNgayLap(LocalDateTime ngayLap) {
		this.ngayLap = ngayLap;
	}

	public LoaiHoaDon getLoaiHoaDon() {
		return loaiHoaDon;
	}

	public void setLoaiHoaDon(LoaiHoaDon loaiHoaDon) {
		this.loaiHoaDon = loaiHoaDon;
	}

	@Override
	public int hashCode() {
		return Objects.hash(maHoaDon);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HoaDon other = (HoaDon) obj;
		return Objects.equals(maHoaDon, other.maHoaDon);
	}

	@Override
	public String toString() {
		return "HoaDon{" + "maHoaDon=" + maHoaDon + ", nhanVien=" + nhanVien + ", khachHang=" + khachHang + ", ngayLap="
				+ ngayLap + ", loaiHoaDon=" + loaiHoaDon + '}';
	}
}
