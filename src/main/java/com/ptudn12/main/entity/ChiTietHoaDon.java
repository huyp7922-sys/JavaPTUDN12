package entity;

import java.util.Objects;

public class ChiTietHoaDon {
	private HoaDon hoaDon;
	private VeTau veTau;
	static final int BAO_HIEM = 2000;
	
	public ChiTietHoaDon() {
		super();
	}

	public ChiTietHoaDon(HoaDon hoaDon, VeTau veTau) {
		super();
		this.hoaDon = hoaDon;
		this.veTau = veTau;
	}

	public HoaDon getHoaDon() {
		return hoaDon;
	}

	public void setHoaDon(HoaDon hoaDon) {
		this.hoaDon = hoaDon;
	}

	public VeTau getVeTau() {
		return veTau;
	}

	public void setVeTau(VeTau veTau) {
		this.veTau = veTau;
	}
	

	@Override
	public int hashCode() {
		return Objects.hash(hoaDon, veTau);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ChiTietHoaDon other = (ChiTietHoaDon) obj;
		return Objects.equals(hoaDon, other.hoaDon) && Objects.equals(veTau, other.veTau);
	}

	@Override
	public String toString() {
		return "ChiTietHoaDon [hoaDon=" + hoaDon + ", veTau=" + veTau + "]";
	}
	
}
