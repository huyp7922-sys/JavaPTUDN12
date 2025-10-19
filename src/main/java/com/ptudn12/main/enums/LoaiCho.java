package com.ptudn12.main.enums;

public enum LoaiCho {
    GIUONG_NAM_TANG_1("Giường nằm tầng 1"),
    GIUONG_NAM_TANG_2("Giường nằm tầng 2"),
    GHE_CUNG("Ghế cứng"),
    GHE_MEM("Ghế mềm"),
    GHE_VIP("Ghế VIP");

    private final String description;

    LoaiCho(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
