package com.ptudn12.main.dao;

import com.ptudn12.main.database.DatabaseConnection;
import com.ptudn12.main.entity.TaiKhoan;

import java.sql.*;

public class TaiKhoanDAO {

    public boolean themTaiKhoan(TaiKhoan tk) {
        String sql = "INSERT INTO taikhoan (username, password, role, maNV, trangThai) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tk.getUsername());
            ps.setString(2, tk.getPassword());
            ps.setString(3, tk.getRole());
            ps.setString(4, tk.getMaNV());
            ps.setString(5, tk.getTrangThai());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean capNhatTaiKhoan(TaiKhoan tk) {
        String sql = "UPDATE taikhoan SET password=?, role=?, trangThai=? WHERE username=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tk.getPassword());
            ps.setString(2, tk.getRole());
            ps.setString(3, tk.getTrangThai());
            ps.setString(4, tk.getUsername());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Khi khóa nhân viên: chỉ cập nhật trạng thái tài khoản thành 'Đã khóa' */
    public boolean khoaTaiKhoanTheoMaNV(String maNV) {
        String sql = "UPDATE taikhoan SET trangThai = 'Đã khóa' WHERE maNV = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNV);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Tùy chọn: kiểm tra username tồn tại */
    public boolean existsUsername(String username) {
        String sql = "SELECT 1 FROM taikhoan WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
