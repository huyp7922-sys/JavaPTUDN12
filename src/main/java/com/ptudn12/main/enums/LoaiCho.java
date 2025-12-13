package com.ptudn12.main.enums;

public enum LoaiCho {

    GHE_CUNG("Ghế ngồi cứng", 1.0),
    GHE_NGOI_MEM("Ghế ngồi mềm", 1.2),
    GIUONG_4("Giường nằm khoang 4", 1.4),
    GIUONG_66("Giường nằm khoang 6", 1.6),
    GIUONG_VIP("Giường nằm VIP", 2.0);
    
    // Thuộc tính
    private final String tenLoaiCho;
    private final double heSoChoNgoi;

    // Constructor
    private LoaiCho(String tenLoaiCho, double heSoChoNgoi) {
            this.tenLoaiCho = tenLoaiCho;
            this.heSoChoNgoi = heSoChoNgoi;
    }
    
    public String getDescription() {
       return tenLoaiCho;
    }


    // Getter
    public String getTenLoaiCho() {
            return tenLoaiCho;
    }

    public double getHeSoChoNgoi() {
            return heSoChoNgoi;
    }

    // toString
    @Override
    public String toString() {
            return tenLoaiCho;
    }

    /**
     * Chuyển đổi một String (từ database) thành Enum LoaiCho tương ứng.
     * 
     * @param tenLoaiCho Tên loại chỗ (ví dụ: "Ghế ngồi cứng")
     * @return Enum LoaiCho (ví dụ: LoaiCho.GheCung)
     * @throws IllegalArgumentException nếu không tìm thấy String
     */
    public static LoaiCho fromDescription(String tenLoaiCho) {
            for (LoaiCho loai : values()) {
                    if (loai.tenLoaiCho.equals(tenLoaiCho)) {
                            return loai;
                    }
            }
            throw new IllegalArgumentException("Không tìm thấy LoaiCho với tên: " + tenLoaiCho);
    }
}
