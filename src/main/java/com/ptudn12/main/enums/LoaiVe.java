package com.ptudn12.main.enums;

public enum LoaiVe {
    VE_THUONG("Vé thường"),
    VE_GIAM_GIA("Vé giảm giá"),
    VE_UU_TIEN("Vé ưu tiên");

    private final String description;

    LoaiVe(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
