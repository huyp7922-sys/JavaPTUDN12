package com.ptudn12.main.dao;

import com.ptudn12.main.database.DatabaseConnection;
import com.ptudn12.main.entity.NhanVien;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class NhanVienDAO {

    /** Lấy toàn bộ danh sách nhân viên */
    public List<NhanVien> getAllNhanVien() {
        List<NhanVien> list = new ArrayList<>();
        String sql = "SELECT * FROM nhanvien ORDER BY maNV ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Date d = rs.getDate("ngaySinh");
                LocalDate ngay = d != null ? d.toLocalDate() : null;
                list.add(new NhanVien(
                        rs.getString("maNV"),
                        rs.getString("hoTen"),
                        rs.getString("cccd"),
                        ngay,
                        rs.getString("gioiTinh"),
                        rs.getString("chucVu"),
                        rs.getString("sdt"),
                        rs.getString("email"),
                        rs.getString("trangThai")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Lấy nhân viên theo mã */
    public NhanVien getNhanVienByMa(String maNV) {
        String sql = "SELECT * FROM nhanvien WHERE maNV = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNV);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new NhanVien(
                        rs.getString("maNV"),
                        rs.getString("hoTen"),
                        rs.getString("cccd"),
                        rs.getDate("ngaySinh").toLocalDate(),
                        rs.getString("gioiTinh"),
                        rs.getString("chucVu"),
                        rs.getString("sdt"),
                        rs.getString("email"),
                        rs.getString("trangThai")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Thêm nhân viên */
    public boolean themNhanVien(NhanVien nv) {
        String sql = "INSERT INTO nhanvien (maNV, hoTen, cccd, ngaySinh, gioiTinh, chucVu, sdt, email, trangThai)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nv.getMaNV());
            ps.setString(2, nv.getHoTen());
            ps.setString(3, nv.getCccd());
            ps.setDate(4, Date.valueOf(nv.getNgaySinh()));
            ps.setString(5, nv.getGioiTinh());
            ps.setString(6, nv.getChucVu());
            ps.setString(7, nv.getSdt());
            ps.setString(8, nv.getEmail());
            ps.setString(9, nv.getTrangThai());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** Cập nhật nhân viên */
    public boolean capNhatNhanVien(NhanVien nv) {
        String sql = "UPDATE nhanvien SET hoTen=?, cccd=?, ngaySinh=?, gioiTinh=?, chucVu=?, sdt=?, email=?, trangThai=? WHERE maNV=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nv.getHoTen());
            ps.setString(2, nv.getCccd());
            ps.setDate(3, java.sql.Date.valueOf(nv.getNgaySinh()));
            ps.setString(4, nv.getGioiTinh());
            ps.setString(5, nv.getChucVu());
            ps.setString(6, nv.getSdt());
            ps.setString(7, nv.getEmail());
            ps.setString(8, nv.getTrangThai());
            ps.setString(9, nv.getMaNV());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** Soft delete: khóa tài khoản */
    public boolean khoaNhanVien(String maNV) {
        String sql = "UPDATE nhanvien SET trangThai = 'Đã khóa' WHERE maNV = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNV);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** Sinh mã NV tự động: NV001, NV002, ... */
    public static String generateMaNV() {
        String sql = "SELECT maNV FROM nhanvien WHERE maNV LIKE 'NV%' ORDER BY maNV DESC LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                String last = rs.getString(1);
                int number = Integer.parseInt(last.substring(2));
                return String.format("NV%03d", number + 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "NV001";
    }

    /** Check trùng CCCD */
    public boolean isDuplicateCCCD(String cccd) {
        return checkDuplicate("cccd", cccd);
    }

    /** Check trùng SĐT */
    public boolean isDuplicateSDT(String sdt) {
        return checkDuplicate("sdt", sdt);
    }

    /** Check trùng Email */
    public boolean isDuplicateEmail(String email) {
        return checkDuplicate("email", email);
    }

    private boolean checkDuplicate(String field, String value) {
        String sql = "SELECT COUNT(*) FROM nhanvien WHERE " + field + " = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, value);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
