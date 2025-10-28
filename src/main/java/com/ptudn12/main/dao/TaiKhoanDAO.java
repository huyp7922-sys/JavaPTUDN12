package com.ptudn12.main.dao;

import com.ptudn12.main.database.DatabaseConnection;
import com.ptudn12.main.entity.TaiKhoan;

import java.sql.*;

public class TaiKhoanDAO {
    private NhanVienDAO nhanVienDao;
    
    /**
     * Thêm tài khoản mới cho nhân viên
     */
    public boolean insert(TaiKhoan tk) {
        String sql = "INSERT INTO TaiKhoan (maNhanVien, matKhau, trangThaiTK) VALUES (?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, tk.getNhanVien().getMaNhanVien());
            ps.setString(2, tk.getMatKhau());
            ps.setString(3, tk.getTrangThaiTK());
            
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Cập nhật thông tin tài khoản
     */
    public boolean update(TaiKhoan tk) {
        String sql = "UPDATE TaiKhoan SET matKhau = ?, trangThaiTK = ? WHERE maNhanVien = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, tk.getMatKhau());
            ps.setString(2, tk.getTrangThaiTK());
            ps.setString(3, tk.getNhanVien().getMaNhanVien());
            
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Xóa tài khoản
     */
    public boolean delete(String maNhanVien) {
        String sql = "DELETE FROM TaiKhoan WHERE maNhanVien = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, maNhanVien);
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Tìm tài khoản theo mã nhân viên
     */
    public TaiKhoan findById(String maNhanVien) {
        String sql = "SELECT * FROM TaiKhoan WHERE maNhanVien = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, maNhanVien);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTaiKhoan(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Kiểm tra tài khoản đã tồn tại chưa
     */
    public boolean exists(String maNhanVien) {
        String sql = "SELECT COUNT(*) FROM TaiKhoan WHERE maNhanVien = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, maNhanVien);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Đổi mật khẩu
     */
    public boolean changePassword(String maNhanVien, String newPassword) {
        String sql = "UPDATE TaiKhoan SET matKhau = ? WHERE maNhanVien = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, newPassword);
            ps.setString(2, maNhanVien);
            
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Khóa/mở khóa tài khoản
     */
    public boolean updateStatus(String maNhanVien, String status) {
        String sql = "UPDATE TaiKhoan SET trangThaiTK = ? WHERE maNhanVien = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, status);
            ps.setString(2, maNhanVien);
            
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Map ResultSet to TaiKhoan object
     */
    private TaiKhoan mapResultSetToTaiKhoan(ResultSet rs) throws SQLException {
        TaiKhoan tk = new TaiKhoan();
        tk.setNhanVien(nhanVienDao.findById(rs.getString("maNhanVien")));
        tk.setMatKhau(rs.getString("matKhau"));
        tk.setTrangThaiTK(rs.getString("trangThaiTK"));
        return tk;
    }
}
