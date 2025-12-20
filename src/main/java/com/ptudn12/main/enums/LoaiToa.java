package com.ptudn12.main.enums;

public enum LoaiToa {
	NGOI_CUNG("Ngồi cứng"),
        NGOI_MEM("Ngồi mềm"),
        GIUONG_NAM_KHOANG_6("Giường nằm khoang 6"),
	GIUONG_NAM_KHOANG_4("Giường nằm khoang 4"),
        GIUONG_NAM_VIP("Giường nằm VIP");

	private final String description;

	LoaiToa(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public int getSoChoMacDinh(LoaiToa lt) { // Bỏ tham số LoaiToa lt
		switch (lt) { // Dùng this thay vì lt
		case NGOI_CUNG:
			return 72;
		case NGOI_MEM:
			return 64;
		case GIUONG_NAM_KHOANG_6:
			return 42; // Sửa số chỗ từ 28 -> 42
		case GIUONG_NAM_KHOANG_4:
			return 28; // Sửa số chỗ từ 42 -> 28
		case GIUONG_NAM_VIP:
			return 14;
		default:
			return 0;
		}
	}

	public static LoaiToa fromDescription(String description) {
        if (description == null) {
            throw new IllegalArgumentException("Description cannot be null");
        }
        
        String input = description.trim();
        
        for (LoaiToa loai : values()) {
            if (loai.description.equalsIgnoreCase(input)) {
                return loai;
            }
            
            if (input.toLowerCase().contains("ngồi cứng") && loai == NGOI_CUNG) return NGOI_CUNG;
            if (input.toLowerCase().contains("ngồi mềm") && loai == NGOI_MEM) return NGOI_MEM;
            if (input.toLowerCase().contains("giường nằm khoang 6") && loai == GIUONG_NAM_KHOANG_6) return GIUONG_NAM_KHOANG_6;
            if (input.toLowerCase().contains("giường nằm khoang 4") && loai == GIUONG_NAM_KHOANG_4) return GIUONG_NAM_KHOANG_4;
            if (input.toLowerCase().contains("giường nằm vip") && loai == GIUONG_NAM_VIP) return GIUONG_NAM_VIP;
        }
        throw new IllegalArgumentException("Không tìm thấy LoaiToa với description: " + description);
    }

	@Override
	public String toString() {
		return this.description;
	}
}