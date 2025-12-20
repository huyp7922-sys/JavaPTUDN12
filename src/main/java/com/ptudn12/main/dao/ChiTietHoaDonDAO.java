
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
import com.ptudn12.main.entity.ChiTietHoaDon;
import java.sql.*;

public class ChiTietHoaDonDAO {
    public boolean createChiTietHoaDon(String maHoaDon, String maVe, double giaGiam, double thanhTien) {
        String sql = "INSERT INTO ChiTietHoaDon (maHoaDon, maVe, giaGiam, thanhTien, BAO_HIEM, isTraVe, soTienHoanLai) VALUES (?, ?, ?, ?, ?, 0, 0)";
        final int PHI_BAO_HIEM_DEFAULT = 2000;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maHoaDon);
            ps.setString(2, maVe);
            ps.setDouble(3, giaGiam);
            ps.setDouble(4, thanhTien);
            ps.setInt(5, PHI_BAO_HIEM_DEFAULT);

            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi khi tạo chi tiết hóa đơn cho HD " + maHoaDon + ", Vé " + maVe + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public String getMaHoaDonByMaVe(String maVe) {
        String sql = "SELECT maHoaDon FROM ChiTietHoaDon WHERE maVe = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maVe);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("maHoaDon");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public double getGiaThucTeDaTra(String maVe) {
        String sql = "SELECT thanhTien FROM ChiTietHoaDon WHERE maVe = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maVe);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble("thanhTien");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0; // Mặc định trả về 0 nếu lỗi
    }
}

