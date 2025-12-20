package com.ptudn12.main.dao;

import java.sql.*;
import java.util.Map;
import com.ptudn12.main.database.DatabaseConnection;
import java.util.ArrayList;
import java.util.List;
import com.ptudn12.main.entity.KhachHang;

public class KhachHangDAO {
    
    public int findKhachHangByIdentifier(String identifier) {
        if (identifier != null) identifier = identifier.trim();
        if (identifier == null || identifier.isEmpty()) return -1;

        String sql = identifier.matches("\\d{9}|\\d{12}") 
            ? "SELECT maKhachHang FROM KhachHang WHERE soCCCD = ?" 
            : "SELECT maKhachHang FROM KhachHang WHERE hoChieu = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, identifier);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("maKhachHang");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int findOrInsertKhachHang(Map<String, String> thongTinNguoiMua) {
        String identifier = thongTinNguoiMua.get("soGiayToIdentifier");
        int existingId = (identifier != null && !identifier.isEmpty()) ? findKhachHangByIdentifier(identifier) : -1;
        return (existingId != -1) ? existingId : insertKhachHang(thongTinNguoiMua);
    }

    public int insertKhachHang(Map<String, String> info) {
        String sql = "INSERT INTO KhachHang (tenKhachHang, soCCCD, hoChieu, soDienThoai, email, diaChi) VALUES (?, ?, ?, ?, ?, ?)";
        String iden = info.get("soGiayToIdentifier") != null ? info.get("soGiayToIdentifier").trim() : "";
        String soCCCD = iden.matches("\\d{9}|\\d{12}") ? iden : null;
        String hoChieu = (soCCCD == null && !iden.isEmpty()) ? iden : null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, info.get("tenKhachHang"));
            ps.setString(2, soCCCD);
            ps.setString(3, hoChieu);
            ps.setString(4, info.get("soDienThoai"));
            ps.setString(5, info.get("email"));
            ps.setString(6, info.get("diaChi"));

            if (ps.executeUpdate() > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public List<KhachHang> layTatCaKhachHang() {
        List<KhachHang> danhSach = new ArrayList<>();
        String sql = "SELECT * FROM KhachHang ORDER BY maKhachHang";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) danhSach.add(mapResultSetToKhachHang(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return danhSach;
    }

    public boolean kiemTraTrungLap(String cccd, String hoChieu, Integer currentId) {
        String sql = "SELECT COUNT(*) FROM KhachHang WHERE (soCCCD = ? OR hoChieu = ?) AND (? IS NULL OR maKhachHang != ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cccd);
            ps.setString(2, hoChieu);
            if (currentId != null) {
                ps.setInt(3, currentId); ps.setInt(4, currentId);
            } else {
                ps.setNull(3, Types.INTEGER); ps.setNull(4, Types.INTEGER);
            }
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean themKhachHang(KhachHang kh) {
        String sql = "INSERT INTO KhachHang (tenKhachHang, soCCCD, hoChieu, soDienThoai) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kh.getTenKhachHang());
            if (kh.getSoCCCD() != null && !kh.getSoCCCD().isEmpty()) {
                ps.setString(2, kh.getSoCCCD()); ps.setNull(3, Types.VARCHAR);
            } else {
                ps.setNull(2, Types.VARCHAR); ps.setString(3, kh.getHoChieu());
            }
            ps.setString(4, kh.getSoDienThoai());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean capNhatKhachHang(KhachHang kh) {
        String sql = "UPDATE KhachHang SET tenKhachHang = ?, soCCCD = ?, hoChieu = ?, soDienThoai = ? WHERE maKhachHang = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kh.getTenKhachHang());
            ps.setString(2, kh.getSoCCCD());
            ps.setString(3, kh.getHoChieu());
            ps.setString(4, kh.getSoDienThoai());
            int id = Integer.parseInt(kh.getMaKhachHang().replaceAll("[^0-9]", ""));
            ps.setInt(5, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
            return false;
        }
    }

    private KhachHang mapResultSetToKhachHang(ResultSet rs) throws SQLException {
        int idInt = rs.getInt("maKhachHang");
        String cccd = rs.getString("soCCCD");
        String hoChieu = rs.getString("hoChieu");
        String maKH = String.format("KH%09d", idInt);
        String idGiayTo = (hoChieu != null && !hoChieu.trim().isEmpty()) ? hoChieu : cccd;
        boolean isForeigner = (hoChieu != null && !hoChieu.trim().isEmpty());
        return new KhachHang(maKH, rs.getString("tenKhachHang"), idGiayTo, isForeigner, rs.getString("soDienThoai"), 0);
    }

    public KhachHang timKhachHangTheoGiayTo(String iden) {
        if (iden == null || iden.isEmpty()) return null;
        String sql = "SELECT * FROM KhachHang WHERE soCCCD = ? OR hoChieu = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, iden); ps.setString(2, iden);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                KhachHang kh = new KhachHang();
                kh.setMaKH(String.valueOf(rs.getInt("maKhachHang")));
                kh.setTenKhachHang(rs.getString("tenKhachHang"));
                kh.setSoCCCD(rs.getString("soCCCD"));
                kh.setHoChieu(rs.getString("hoChieu"));
                kh.setSoDienThoai(rs.getString("soDienThoai"));
                return kh;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public String getEmailKhachHang(String iden) {
        String sql = "SELECT email FROM KhachHang WHERE soCCCD = ? OR hoChieu = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, iden); ps.setString(2, iden);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("email");
        } catch (SQLException e) { e.printStackTrace(); }
        return "";
    }

    public KhachHang getNguoiMuaByMaVe(String maVe) {
        String sql = "SELECT KH.* FROM KhachHang KH JOIN HoaDon HD ON KH.maKhachHang = HD.khachHangId " +
                     "JOIN ChiTietHoaDon CTHD ON HD.maHoaDon = CTHD.maHoaDon " +
                     "WHERE CTHD.maVe = ? AND (HD.loaiHoaDon = 'BanVe' OR HD.loaiHoaDon = 'DoiVe')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maVe);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                KhachHang kh = new KhachHang();
                kh.setMaKH(rs.getInt("maKhachHang") + "");
                kh.setTenKhachHang(rs.getString("tenKhachHang"));
                kh.setSoCCCD(rs.getString("soCCCD"));
                kh.setHoChieu(rs.getString("hoChieu"));
                kh.setSoDienThoai(rs.getString("soDienThoai"));
                return kh;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public KhachHang getHanhKhachByMaVe(String maVe) {
        String sql = "SELECT KH.* FROM KhachHang KH JOIN VeTau VT ON KH.maKhachHang = VT.khachHangId WHERE VT.maVe = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maVe);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                KhachHang kh = new KhachHang();
                kh.setMaKH(String.valueOf(rs.getInt("maKhachHang")));
                kh.setTenKhachHang(rs.getString("tenKhachHang"));
                kh.setSoCCCD(rs.getString("soCCCD"));
                kh.setHoChieu(rs.getString("hoChieu"));
                kh.setSoDienThoai(rs.getString("soDienThoai"));
                return kh;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }
}