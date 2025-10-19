package entity;

public class QRCode {
	private String maQR; // Temporary
	
	public QRCode() {
		super();
	}

	public QRCode(String maQR) {
		super();
		this.maQR = maQR;
	}

	@Override
	public String toString() {
		return "QRCode [maQR=" + maQR + "]";
	}
	
}
