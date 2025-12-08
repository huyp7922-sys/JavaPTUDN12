package com.ptudn12.main.dao;

import com.ptudn12.main.entity.NhanVien;
import com.ptudn12.main.database.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class NhanVienDAO {
    
    /**
     * Lấy tất cả nhân viên
     */
    public List<NhanVien> getAllNhanVien() {
        List<NhanVien> list = new ArrayList<>();
        String sql = "SELECT * FROM NhanVien ORDER BY maNhanVien";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                list.add(mapResultSetToNhanVien(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return list;
    }
    
    /**
     * Tìm nhân viên theo mã
     */
    public NhanVien findById(String maNhanVien) {
        String sql = "SELECT * FROM NhanVien WHERE maNhanVien = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, maNhanVien);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToNhanVien(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Thêm nhân viên mới
     */
    public boolean insert(NhanVien nv) {
        String sql = "INSERT INTO NhanVien (maNhanVien, tenNhanVien, soCCCD, ngaySinh, " +
                     "gioiTinh, soDienThoai, email, chucVu, tinhTrangCV) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, nv.getMaNhanVien());
            ps.setString(2, nv.getTenNhanVien());
            ps.setString(3, nv.getSoCCCD());
            ps.setDate(4, Date.valueOf(nv.getNgaySinh()));
            ps.setBoolean(5, nv.isGioiTinh());
            ps.setString(6, nv.getSoDienThoai());
            ps.setString(7, nv.getEmail());
            
            if (nv.getChucVu() != null) {
                ps.setBoolean(8, nv.getChucVu());
            } else {
                ps.setNull(8, Types.BOOLEAN);
            }
            
            ps.setString(9, nv.getTinhTrangCV());
            
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Cập nhật thông tin nhân viên
     */
    public boolean update(NhanVien nv) {
        String sql = "UPDATE NhanVien SET tenNhanVien = ?, soCCCD = ?, ngaySinh = ?, " +
                     "gioiTinh = ?, soDienThoai = ?, email = ?, chucVu = ?, tinhTrangCV = ? " +
                     "WHERE maNhanVien = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, nv.getTenNhanVien());
            ps.setString(2, nv.getSoCCCD());
            ps.setDate(3, Date.valueOf(nv.getNgaySinh()));
            ps.setBoolean(4, nv.isGioiTinh());
            ps.setString(5, nv.getSoDienThoai());
            ps.setString(6, nv.getEmail());
            
            if (nv.getChucVu() != null) {
                ps.setBoolean(7, nv.getChucVu());
            } else {
                ps.setNull(7, Types.BOOLEAN);
            }
            
            ps.setString(8, nv.getTinhTrangCV());
            ps.setString(9, nv.getMaNhanVien());
            
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Xóa nhân viên (thay đổi trạng thái thành "đã nghỉ")
     */
    public boolean delete(String maNhanVien) {
        String sql = "UPDATE NhanVien SET tinhTrangCV = N'đã nghỉ' WHERE maNhanVien = ?";
        
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
     * Kiểm tra CCCD đã tồn tại chưa
     */
    public boolean isCCCDExists(String cccd) {
        String sql = "SELECT COUNT(*) FROM NhanVien WHERE soCCCD = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, cccd);
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
     * Kiểm tra email đã tồn tại chưa
     */
    public boolean isEmailExists(String email) {
        String sql = "SELECT COUNT(*) FROM NhanVien WHERE email = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, email);
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
     * Kiểm tra SĐT đã tồn tại chưa
     */
    public boolean isSDTExists(String sdt) {
        String sql = "SELECT COUNT(*) FROM NhanVien WHERE soDienThoai = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, sdt);
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
     * Tạo mã nhân viên tự động
     */
    public String generateMaNhanVien() {
        String sql = "SELECT TOP 1 maNhanVien FROM NhanVien " +
                     "ORDER BY CAST(SUBSTRING(maNhanVien, 3, LEN(maNhanVien)) AS INT) DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            if (rs.next()) {
                String lastMa = rs.getString(1);
                int number = Integer.parseInt(lastMa.substring(2));
                return String.format("NV%03d", number + 1);
            } else {
                return "NV001";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "NV001";
        }
    }
    
    /**
     * Tìm kiếm nhân viên theo từ khóa
     */
    public List<NhanVien> search(String keyword) {
        List<NhanVien> list = new ArrayList<>();
        String sql = "SELECT * FROM NhanVien WHERE " +
                     "maNhanVien LIKE ? OR " +
                     "tenNhanVien LIKE ? OR " +
                     "soCCCD LIKE ? OR " +
                     "soDienThoai LIKE ? OR " +
                     "email LIKE ? " +
                     "ORDER BY maNhanVien";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            String searchPattern = "%" + keyword + "%";
            for (int i = 1; i <= 5; i++) {
                ps.setString(i, searchPattern);
            }
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToNhanVien(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return list;
    }
    
    /**
     * Lấy danh sách nhân viên đang làm việc
     */
    public List<NhanVien> getNhanVienDangLam() {
        List<NhanVien> list = new ArrayList<>();
        String sql = "SELECT * FROM NhanVien WHERE tinhTrangCV = N'đang làm' ORDER BY maNhanVien";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                list.add(mapResultSetToNhanVien(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return list;
    }
    
    /**
     * Map ResultSet to NhanVien object - ĐÃ SỬA
     */
    private NhanVien mapResultSetToNhanVien(ResultSet rs) throws SQLException {
        NhanVien nv = new NhanVien();
        nv.setMaNhanVien(rs.getString("maNhanVien"));
        nv.setTenNhanVien(rs.getString("tenNhanVien"));
        nv.setSoCCCD(rs.getString("soCCCD"));
        
        Date ngaySinhDate = rs.getDate("ngaySinh");
        if (ngaySinhDate != null) {
            nv.setNgaySinh(ngaySinhDate.toLocalDate());
        }
        
        nv.setGioiTinh(rs.getBoolean("gioiTinh"));
        nv.setSoDienThoai(rs.getString("soDienThoai"));
        nv.setEmail(rs.getString("email"));
        
        Boolean chucVu = rs.getBoolean("chucVu");
        if (rs.wasNull()) {
            nv.setChucVu(null);
        } else {
            nv.setChucVu(chucVu);
        }
        
        nv.setTinhTrangCV(rs.getString("tinhTrangCV"));
        
        // DEBUG: In ra để kiểm tra
        System.out.println("🔍 DAO loaded: " + nv.getMaNhanVien() + 
                          " | Chức vụ (Boolean): " + nv.getChucVu() + 
                          " | Text: " + nv.getChucVuText() +
                          " | Tình trạng: " + nv.getTinhTrangCV());
        
        return nv;
    }
}