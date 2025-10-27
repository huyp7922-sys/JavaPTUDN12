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