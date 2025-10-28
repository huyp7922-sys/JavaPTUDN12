package com.ptudn12.main.enums;

public enum LoaiToa {
	NGOI_CUNG("Ngồi cứng"), NGOI_MEM("Ngồi mềm"), GIUONG_NAM_KHOANG_6("Giường nằm khoang 6"),
	GIUONG_NAM_KHOANG_4("Giường nằm khoang 4"), GIUONG_NAM_VIP("Giường nằm VIP");

	private final String description;

	LoaiToa(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public int getSoChoMacDinh(LoaiToa lt) {
		switch (lt) {
		case NGOI_CUNG:
			return 72;
		case NGOI_MEM:
			return 64;
		case GIUONG_NAM_KHOANG_4:
			return 42;
		case GIUONG_NAM_KHOANG_6:
			return 28;
		case GIUONG_NAM_VIP:
			return 14;
		default:
			return 0;
		}
	}

	// Thêm hàm này để chuyển đổi từ String trong DB sang Enum
	public static LoaiToa fromDescription(String description) {
		for (LoaiToa loai : values()) {
			if (loai.description.equals(description)) {
				return loai;
			}
		}
		throw new IllegalArgumentException("Không tìm thấy LoaiToa với description: " + description);
	}

	@Override
	public String toString() {
		return this.description;
	}
}