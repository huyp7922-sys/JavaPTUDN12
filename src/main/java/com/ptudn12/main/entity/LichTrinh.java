package com.ptudn12.main.entity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.ptudn12.main.enums.TrangThai;

public class LichTrinh {
	private String maLichTrinh;
	private Tau tau;
	private TuyenDuong tuyenDuong;
	private LocalDateTime ngayGioKhoiHanh;
	private LocalDateTime ngayGioDen;
	private float giaCoBan;
	private TrangThai trangThai;

	// Constructor không tham số
	public LichTrinh() {
	}

	// Constructor có tham số
	public LichTrinh(TuyenDuong tuyen, Tau tau, LocalDateTime ngayGioKhoiHanh) {
		this.tuyenDuong = tuyen;
		this.tau = tau;
		this.ngayGioKhoiHanh = ngayGioKhoiHanh;
		this.giaCoBan = tuyen != null ? tuyen.getGiaCoBan() : 0;
		this.ngayGioDen = tinhNgayGioDen();
		this.maLichTrinh = generateMaLichTrinh();
	}

	// Getters and Setters
	public void setNgayGioKhoiHanh(LocalDateTime ngayGioKhoiHanh) {
		if (ngayGioKhoiHanh.isAfter(LocalDateTime.now())) {
			this.ngayGioKhoiHanh = ngayGioKhoiHanh;
			this.ngayGioDen = tinhNgayGioDen();
		}
	}

	public void setNgayGioDen(LocalDateTime ngayGioDen) {
		this.ngayGioDen = ngayGioDen;
	}

	public void setTrangThai(TrangThai trangThai) {
		this.trangThai = trangThai;
	}

	public void setTau(Tau tau) {
		this.tau = tau;

	}

	public void setTuyenDuong(TuyenDuong tuyenDuong) {
		this.tuyenDuong = tuyenDuong;
		this.giaCoBan = tuyenDuong != null ? tuyenDuong.getGiaCoBan() : 0;
	}

	public void setMaLichTrinh(String maLichTrinh) {
		this.maLichTrinh = maLichTrinh;
	}

	public void setGiaCoBan(float giaCoBan) {
		this.giaCoBan = giaCoBan;
	}

	public String getMaLichTrinh() {
		return maLichTrinh;
	}

	public TuyenDuong getTuyenDuong() {
		return tuyenDuong;
	}

	public Tau getTau() {
		return tau;
	}

	public LocalDateTime getNgayGioKhoiHanh() {
		return ngayGioKhoiHanh;
	}

	public LocalDateTime getNgayGioDen() {
		return ngayGioDen;
	}

	public float getGiaCoBan() {
		return giaCoBan;
	}

	public TrangThai getTrangThai() {
		return trangThai;
	}

	private LocalDateTime tinhNgayGioDen() {
		if (ngayGioKhoiHanh == null || tuyenDuong == null) {
			return null;
		}
		return ngayGioKhoiHanh.plusHours(tuyenDuong.getThoiGianDuKien());
	}

	private String generateMaLichTrinh() {
		if (tuyenDuong == null || ngayGioKhoiHanh == null) {
			return "LT_TEMP";
		}

		String diemDi = tuyenDuong.getDiemDi().getViTriGa().replaceAll("\\s+|Ga\\s+", "");
		String diemDen = tuyenDuong.getDiemDen().getViTriGa().replaceAll("\\s+|Ga\\s+", "");
		String ngay = ngayGioKhoiHanh.format(DateTimeFormatter.ofPattern("ddMMyyyy"));
		int stt = (int) (Math.random() * 10) + 1;

		return String.format("%s_%s_%s_%d", diemDi, diemDen, ngay, stt);
	}

	// Helper methods for TableView
	public String getMaTauDisplay() {
		return tau != null ? tau.getMacTau() : "";
	}

	public String getNgayGioKhoiHanhFormatted() {
		return ngayGioKhoiHanh != null ? ngayGioKhoiHanh.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "";
	}

	public String getGiaCoBanFormatted() {
		return String.format("%,.0fd", giaCoBan);
	}

	public String getSoGheDisplay() {
		// Query động từ database để lấy số ghế trống/tổng số ghế
		if (maLichTrinh == null || maLichTrinh.isEmpty()) {
			return "N/A";
		}

		try {
			com.ptudn12.main.dao.LichTrinhDAO dao = new com.ptudn12.main.dao.LichTrinhDAO();
			// sử dụng phương thức mới trả về format "Còn/Tổng"
			String info = dao.layThongTinChoNgoiFormat(maLichTrinh);
			return info;
		} catch (Exception e) {
			return "Lỗi query";
		}
	}

	public String getTrangThaiDisplay() {
		return trangThai != null ? trangThai.getTenTrangThai() : "";
	}

	public String getTuyenDuongDisplay() {
		if (tuyenDuong == null)
			return "";

		String diemDi = tuyenDuong.getDiemDi().getViTriGa().replace("Ga ", "").trim();
		String diemDen = tuyenDuong.getDiemDen().getViTriGa().replace("Ga ", "").trim();

		String maDiemDi = getShortCode(diemDi);
		String maDiemDen = getShortCode(diemDen);

		return maDiemDi + "-" + maDiemDen;
	}

	private String getShortCode(String tenGa) {
		if (tenGa == null || tenGa.isEmpty())
			return "";

		switch (tenGa.trim()) {
		case "Hà Nội":
			return "HN";
		case "Sài Gòn":
			return "SG";
		case "Đà Nẵng":
			return "DN";
		case "Nha Trang":
			return "NT";
		case "Hải Phòng":
			return "HP";
		case "Vinh":
			return "V";
		case "Huế":
			return "HU";
		case "Quảng Ngãi":
			return "QN";
		default:
			return tenGa.substring(0, Math.min(2, tenGa.length())).toUpperCase();
		}
	}

	@Override
	public String toString() {
		return "LichTrinh{" + "maLichTrinh='" + maLichTrinh + '\'' + ", tau=" + (tau != null ? tau.getMacTau() : "null")
				+ ", tuyenDuong=" + (tuyenDuong != null ? tuyenDuong.getMaTuyen() : "null") + ", ngayGioKhoiHanh="
				+ ngayGioKhoiHanh + ", ngayGioDen=" + ngayGioDen + ", giaCoBan=" + giaCoBan + ", trangThai="
				+ (trangThai != null ? trangThai.getTenTrangThai() : "null") + '}';
	}
}