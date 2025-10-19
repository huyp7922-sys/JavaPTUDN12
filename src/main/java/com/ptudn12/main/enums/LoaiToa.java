package com.ptudn12.main.enums;


public enum LoaiToa {
    TOA_NAM_CUNG("Toa nằm cứng"),
    TOA_NAM_MEM("Toa nằm mềm"),
    TOA_NGOI_CUNG("Toa ngồi cứng"),
    TOA_NGOI_MEM("Toa ngồi mềm"),
    TOA_VIP("Toa VIP");

    private final String description;

    LoaiToa(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}