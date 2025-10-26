/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ptudn12.main.dao;

/**
 *
 * @author fo3cp
 */

import com.ptudn12.main.entity.*;
import com.ptudn12.main.enums.TrangThai;
import com.ptudn12.main.database.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class LichTrinh_DAO {
    // ================================
    // 1️ LẤY DANH SÁCH GA
    // ================================
    public List<Ga> getTatCaGa() throws SQLException {
        List<Ga> dsGa = new ArrayList<>();
        String sql = "SELECT maGa, viTriGa, mocKm FROM Ga ORDER BY mocKm ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Ga g = new Ga(
                        rs.getInt("maGa"),
                        rs.getString("viTriGa"),
                        rs.getInt("mocKm")
                );
                dsGa.add(g);
            }
        }
        return dsGa;
    }

    // ================================
    // 2️ LẤY DANH SÁCH TUYẾN ĐƯỜNG
    // ================================
    public List<TuyenDuong> getTatCaTuyenDuong() throws SQLException {
        List<TuyenDuong> ds = new ArrayList<>();
        String sql = """
            SELECT T.maTuyen, 
                   G1.maGa AS maGaDi, G1.viTriGa AS tenGaDi, G1.mocKm AS kmDi,
                   G2.maGa AS maGaDen, G2.viTriGa AS tenGaDen, G2.mocKm AS kmDen,
                   T.thoiGianDuKien, T.giaCoBan, T.trangThai
            FROM TuyenDuong T
            INNER JOIN Ga G1 ON T.diemDi = G1.maGa
            INNER JOIN Ga G2 ON T.diemDen = G2.maGa
            ORDER BY G1.mocKm
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Ga gaDi = new Ga(rs.getInt("maGaDi"), rs.getString("tenGaDi"), rs.getInt("kmDi"));
                Ga gaDen = new Ga(rs.getInt("maGaDen"), rs.getString("tenGaDen"), rs.getInt("kmDen"));

                TuyenDuong td = new TuyenDuong(
                        rs.getString("maTuyen"),
                        gaDi,
                        gaDen,
                        rs.getFloat("thoiGianDuKien"),
                        rs.getFloat("giaCoBan"),
                        rs.getString("trangThai")
                );
                ds.add(td);
            }
        }
        return ds;
    }

    // ================================
    // 3️ LẤY DANH SÁCH LỊCH TRÌNH THEO HÀNH TRÌNH
    // ================================
    public List<LichTrinh> getLichTrinhTheoHanhTrinh(int maGaDi, int maGaDen, LocalDate ngayKhoiHanh) throws SQLException {
        List<LichTrinh> ds = new ArrayList<>();
        String sql = """
            SELECT 
                L.maLichTrinh, L.maTau, L.ngayKhoiHanh, L.gioKhoiHanh, L.trangThai,
                T.maTuyen, T.thoiGianDuKien, T.giaCoBan,
                G1.maGa AS maGaDi, G1.viTriGa AS tenGaDi, G1.mocKm AS kmDi,
                G2.maGa AS maGaDen, G2.viTriGa AS tenGaDen, G2.mocKm AS kmDen,
                Tau.soLuongChoNgoi
            FROM LichTrinh L
            INNER JOIN TuyenDuong T ON L.maTuyenDuong = T.maTuyen
            INNER JOIN Ga G1 ON T.diemDi = G1.maGa
            INNER JOIN Ga G2 ON T.diemDen = G2.maGa
            INNER JOIN Tau ON L.maTau = Tau.maTau
            WHERE T.diemDi = ? AND T.diemDen = ? AND L.ngayKhoiHanh = ?
            ORDER BY L.gioKhoiHanh ASC
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, maGaDi);
            ps.setInt(2, maGaDen);
            ps.setDate(3, Date.valueOf(ngayKhoiHanh));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // Ga
                    Ga gaDi = new Ga(rs.getInt("maGaDi"), rs.getString("tenGaDi"), rs.getInt("kmDi"));
                    Ga gaDen = new Ga(rs.getInt("maGaDen"), rs.getString("tenGaDen"), rs.getInt("kmDen"));

                    // TuyenDuong
                    TuyenDuong tuyen = new TuyenDuong(
                            rs.getString("maTuyen"),
                            gaDi,
                            gaDen,
                            rs.getFloat("thoiGianDuKien"),
                            rs.getFloat("giaCoBan"),
                            rs.getString("trangThai")
                    );

                    // Tau
                    Tau tau = new Tau(
                            rs.getString("maTau"),
                            rs.getString("maTau"), // nếu có cột tên thì thay
                            rs.getInt("soLuongChoNgoi")
                    );

                    // LocalDateTime
                    LocalDateTime ngayGioKhoiHanh = LocalDateTime.of(
                            rs.getDate("ngayKhoiHanh").toLocalDate(),
                            LocalTime.parse(rs.getString("gioKhoiHanh"))
                    );

                    // LichTrinh entity
                    LichTrinh lt = new LichTrinh(tuyen, tau, ngayGioKhoiHanh);
                    lt.setMaLichTrinh(rs.getString("maLichTrinh"));
                    lt.setTrangThai(TrangThai.fromString(rs.getString("trangThai")));

                    ds.add(lt);
                }
            }
        }
        return ds;
    }

    // ================================
    // 4️ ĐẾM SỐ CHUYẾN TRONG NGÀY
    // ================================
    public int getSoChuyenTheoNgay(LocalDate ngay) throws SQLException {
        String sql = "SELECT COUNT(*) AS soChuyen FROM LichTrinh WHERE ngayKhoiHanh = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(ngay));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("soChuyen");
            }
        }
        return 0;
    }
}
