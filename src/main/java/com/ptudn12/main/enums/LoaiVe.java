package com.ptudn12.main.enums;

public enum LoaiVe {
    VE_BINH_THUONG("Vé thường", 0),
    VE_NGUOI_LON_TUOI("Vé người lớn tuổi", 0.15),
    VE_TRE_EM("Vé trẻ em", 0.25),
    VE_HSSV("Vé học sinh - sinh viên", 0.1);

    private final String description;
    private final double heSoGiamGia;

    LoaiVe(String description, double heSoGiamGia) {
        this.description = description;
        this.heSoGiamGia = heSoGiamGia;
    }

    public String getDescription() {
        return description;
    }

    public double getHeSoGiamGia() {
        return heSoGiamGia;
    }
    
    @Override
    public String toString() {
        return this.description;
    }

    public static LoaiVe fromDescription(String description) {
        for (LoaiVe loai : values()) {
            if (loai.description.equals(description)) {
                return loai;
            }
        }
        return VE_BINH_THUONG;
    }
}
