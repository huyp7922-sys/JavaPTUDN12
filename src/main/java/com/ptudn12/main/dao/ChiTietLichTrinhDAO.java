/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ptudn12.main.dao;

/**
 *
 * @author fo3cp
 */
import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import com.ptudn12.main.database.DatabaseConnection;

public class ChiTietLichTrinhDAO {
    /**
     * Lấy danh sách mã chỗ (maChoNgoi) đã được bán cho lịch trình cụ thể
     */
    public Set<Integer> getCacChoDaBan(String maLichTrinh) {
        Set<Integer> danhSachChoDaBan = new HashSet<>();
        String sql = "SELECT maChoNgoi FROM ChiTietLichTrinh WHERE maLichTrinh = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maLichTrinh);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                danhSachChoDaBan.add(rs.getInt("maChoNgoi"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return danhSachChoDaBan;
    }
}
