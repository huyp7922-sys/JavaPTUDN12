package entity;

import java.util.Objects;

public class KhachHang {
	private String maKH;
	private String tenKH;
	private String soCMND;
	private String hoChieu;
	
	public KhachHang() {
		super();
	}

	public KhachHang(String maKH, String tenKH, String soCMND, String hoChieu) {
		super();
		this.maKH = maKH;
		this.tenKH = tenKH;
		this.soCMND = soCMND;
		this.hoChieu = hoChieu;
	}

	public String getMaKH() {
		return maKH;
	}

	public void setMaKH(String maKH) {
		this.maKH = maKH;
	}

	public String getTenKH() {
		return tenKH;
	}

	public void setTenKH(String tenKH) {
		this.tenKH = tenKH;
	}

	public String getSoCMND() {
		return soCMND;
	}

	public void setSoCMND(String soCMND) {
		this.soCMND = soCMND;
	}

	public String getHoChieu() {
		return hoChieu;
	}

	public void setHoChieu(String hoChieu) {
		this.hoChieu = hoChieu;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(maKH);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		KhachHang other = (KhachHang) obj;
		return Objects.equals(maKH, other.maKH);
	}

	@Override
	public String toString() {
		return "KhachHang [maKH=" + maKH + ", tenKH=" + tenKH + ", soCMND=" + soCMND + ", hoChieu=" + hoChieu + "]";
	}
	
}
