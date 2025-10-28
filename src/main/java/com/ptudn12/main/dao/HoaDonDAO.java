/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ptudn12.main.dao;

/**
 *
 * @author fo3cp
 */

import com.ptudn12.main.database.DatabaseConnection;
import com.ptudn12.main.entity.HoaDon;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class HoaDonDAO {
    public boolean createHoaDon(String maHoaDon, int khachHangId, String maNhanVien, double tongThanhToan) {
        String sql = "INSERT INTO HoaDon (maHoaDon, khachHangId, nhanVienId, ngayLap, tongTienHoaDon, loaiHoaDon) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maHoaDon);
            ps.setInt(2, khachHangId);
            ps.setString(3, maNhanVien);
            ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            ps.setDouble(5, tongThanhToan);
            ps.setString(6, "Bán vé");

            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi khi tạo hóa đơn " + maHoaDon + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

     /**
     * Tạo mã hóa đơn duy nhất ("HD" + YYYY + 6 số).
     * @return Mã hóa đơn mới hoặc null nếu lỗi.
     */
     public String generateUniqueHoaDonId() {
        int currentYear = LocalDate.now().getYear();
        String prefix = "HD" + currentYear;
        int nextSequence = 1;
        String sqlQuery = "SELECT MAX(CAST(SUBSTRING(maHoaDon, 7, 6) AS INT)) FROM HoaDon WHERE maHoaDon LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement psQuery = conn.prepareStatement(sqlQuery)) {

            psQuery.setString(1, prefix + "%");
            ResultSet rs = psQuery.executeQuery();
            if (rs.next()) {
                nextSequence = rs.getInt(1) + 1;
            }
            return String.format("%s%06d", prefix, nextSequence);

        } catch (SQLException e) {
            System.err.println("Lỗi khi tạo mã hóa đơn: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
