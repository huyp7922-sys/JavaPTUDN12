/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ptudn12.main.controller;

/**
 *
 * @author fo3cp
 */

import javafx.scene.Node;
import javafx.scene.control.Button;
import com.ptudn12.main.entity.LichTrinh;
import com.ptudn12.main.entity.ChiTietToa;

/**
 * Lớp helper để chứa thông tin vé tạm thời trong giỏ hàng (Step 2).
 */
public class VeTamThoi {
    private LichTrinh lichTrinh;
    private ChiTietToa chiTietToa;
    private double giaVe;
    private boolean isChieuDi;
    
    // Tham chiếu đến các control UI để đồng bộ
    private Button seatButton; // Nút ghế trên grid
    private Node cardNode;     // Card UI trong giỏ hàng

    public VeTamThoi(LichTrinh lichTrinh, ChiTietToa chiTietToa, double giaVe, Button seatButton, boolean isChieuDi) {
        this.lichTrinh = lichTrinh;
        this.chiTietToa = chiTietToa;
        this.giaVe = giaVe;
        this.seatButton = seatButton;
        this.isChieuDi = isChieuDi;
    }

    // Getters
    public LichTrinh getLichTrinh() { return lichTrinh; }
    public ChiTietToa getChiTietToa() { return chiTietToa; }
    public double getGiaVe() { return giaVe; }
    public boolean isChieuDi() { return isChieuDi; }
    public Button getSeatButton() { return seatButton; }
    public Node getCardNode() { return cardNode; }
    
    // Setter cho cardNode (vì card được tạo sau)
    public void setCardNode(Node cardNode) {
        this.cardNode = cardNode;
    }
    
    // Dùng maCho làm định danh duy nhất
    public int getMaCho() {
        return this.chiTietToa.getCho().getMaCho();
    }
}
