package entity;

import java.util.Objects;

import enums.LoaiVe;

public class VeTau {
	private String maVe;
	private KhachHang khachHang;
	private ChiTietLichTrinh chiTietLichTrinh;
	private LoaiVe loaiVe;
	private boolean khuHoi;
	private String trangThai;
	private QRCode maQR;
	
	public VeTau() {
		super();
	}

	public VeTau(String maVe) {
		super();
		this.maVe = maVe;
	}

	public VeTau(String maVe, KhachHang khachHang, ChiTietLichTrinh chiTietLichTrinh, LoaiVe loaiVe, boolean khuHoi,
			String trangThai, QRCode maQR) {
		super();
		this.maVe = maVe;
		this.khachHang = khachHang;
		this.chiTietLichTrinh = chiTietLichTrinh;
		this.loaiVe = loaiVe;
		this.khuHoi = khuHoi;
		this.trangThai = trangThai;
		this.maQR = maQR;
	}

	public String getMaVe() {
		return maVe;
	}

	public void setMaVe(String maVe) {
		this.maVe = maVe;
	}

	public KhachHang getKhachHang() {
		return khachHang;
	}

	public void setKhachHang(KhachHang khachHang) {
		this.khachHang = khachHang;
	}

	public ChiTietLichTrinh getChiTietLichTrinh() {
		return chiTietLichTrinh;
	}

	public void setChiTietLichTrinh(ChiTietLichTrinh chiTietLichTrinh) {
		this.chiTietLichTrinh = chiTietLichTrinh;
	}

	public LoaiVe getLoaiVe() {
		return loaiVe;
	}

	public void setLoaiVe(LoaiVe loaiVe) {
		this.loaiVe = loaiVe;
	}

	public boolean isKhuHoi() {
		return khuHoi;
	}

	public void setKhuHoi(boolean khuHoi) {
		this.khuHoi = khuHoi;
	}

	public String getTrangThai() {
		return trangThai;
	}

	public void setTrangThai(String trangThai) {
		this.trangThai = trangThai;
	}

	public QRCode getMaQR() {
		return maQR;
	}

	public void setMaQR(QRCode maQR) {
		this.maQR = maQR;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(maVe);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VeTau other = (VeTau) obj;
		return Objects.equals(maVe, other.maVe);
	}

	@Override
	public String toString() {
		return "VeTau [maVe=" + maVe + ", khachHang=" + khachHang + ", khuHoi=" + khuHoi + ", trangThai=" + trangThai
				+ "]";
	}
	
}
