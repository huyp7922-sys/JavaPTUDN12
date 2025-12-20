/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package com.ptudn12.main.enums;

/**
 *
 * @author fo3cp
 */
public enum LoaiHoaDon {
    BAN_VE("Hóa đơn bán vé"),
    HOAN_TIEN("Hóa đơn trả vé");

    private final String description;

    LoaiHoaDon(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
