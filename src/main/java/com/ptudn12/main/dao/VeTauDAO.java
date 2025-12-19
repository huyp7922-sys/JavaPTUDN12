package com.ptudn12.main.dao;

import com.ptudn12.main.database.DatabaseConnection;
import com.ptudn12.main.entity.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class VeTauDAO {

    /**
     * Tạo mới một vé tàu
     */
    public boolean createVeTau(String maVe, int khachHangId, int chiTietLichTrinhId, String loaiVe, boolean khuHoi, String trangThai) {
        String sql = "INSERT INTO VeTau (maVe, khachHangId, chiTietLichTrinhId, loaiVe, khuHoi, trangThai, maQR) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String maQR = "QR_" + maVe + "_" + System.currentTimeMillis();

        try (Connection conn = DatabaseConnection.getConnection(); 
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maVe);
            ps.setInt(2, khachHangId);
            ps.setInt(3, chiTietLichTrinhId);
            ps.setString(4, loaiVe);
            ps.setBoolean(5, khuHoi);
            ps.setString(6, trangThai);
            ps.setString(7, maQR);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi tạo vé tàu " + maVe + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Lấy danh sách vé đã mua của khách hàng
     */
    public List<VeTau> getLichSuVeCuaKhachHang(int maKhachHang) {
        List<VeTau> listVe = new ArrayList<>();
        String sql = "{call sp_XemVeKhachHang(?)}";

        try (Connection conn = DatabaseConnection.getConnection(); 
             CallableStatement cs = conn.prepareCall(sql)) {

            cs.setInt(1, maKhachHang);
            ResultSet rs = cs.executeQuery();

            while (rs.next()) {
                VeTau ve = new VeTau();
                ve.setMaVe(rs.getString("maVe"));
                ve.setKhuHoi(rs.getBoolean("khuHoi"));
                ve.setTrangThai(rs.getString("trangThai"));
                // ve.setLoaiVe(LoaiVe.valueOf(rs.getString("loaiVe"))); // Nếu dùng Enum

                // 1. Mapping Lịch Trình
                LichTrinh lt = new LichTrinh();
                lt.setMaLichTrinh(rs.getString("maLichTrinh"));

                // Xử lý Ngày & Giờ khởi hành
                Date ngayDi = rs.getDate("ngayKhoiHanh");
                Time gioDi = rs.getTime("gioKhoiHanh");
                if (ngayDi != null) {
                    LocalDateTime dt = (gioDi != null) 
                        ? LocalDateTime.of(ngayDi.toLocalDate(), gioDi.toLocalTime()) 
                        : ngayDi.toLocalDate().atStartOfDay();
                    lt.setNgayGioKhoiHanh(dt);
                }

                // 2. Mapping Tuyến Đường (Ga đi - Ga đến)
                TuyenDuong td = new TuyenDuong();
                Ga gDi = new Ga();
                gDi.setViTriGa(rs.getString("DiemDi")); // Giả định cột là DiemDi
                Ga gDen = new Ga();
                gDen.setViTriGa(rs.getString("DiemDen"));
                td.setDiemDi(gDi);
                td.setDiemDen(gDen);
                lt.setTuyenDuong(td);

                // 3. Mapping Tàu
                Tau tau = new Tau();
                tau.setMacTau(rs.getString("tenTau")); // Cần kiểm tra SP có trả về tenTau không
                lt.setTau(tau);

                // 4. Mapping Chi Tiết Lịch Trình (Giá vé & Chỗ ngồi)
                ChiTietLichTrinh ctlt = new ChiTietLichTrinh();
                ctlt.setLichTrinh(lt);
                ctlt.setGiaChoNgoi(rs.getDouble("giaChoNgoi"));
                
                // Giả định SP có trả về thông tin Toa và Chỗ
                Cho cho = new Cho();
                cho.setSoThuTu(rs.getInt("soGhe"));
                Toa toa = new Toa();
                toa.setTenToa(rs.getString("tenToa"));
                cho.setToa(toa);
                ctlt.setCho(cho);

                ve.setChiTietLichTrinh(ctlt);

                // Thêm vào danh sách
                listVe.add(ve);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy lịch sử vé: " + e.getMessage());
            e.printStackTrace();
        }
        return listVe;
    }

    /**
     * Bổ sung hàm lấy ID chi tiết lịch trình để phục vụ việc hoàn trả vé
     */
    public int getChiTietLichTrinhIdByMaVe(String maVe) {
        String sql = "SELECT chiTietLichTrinhId FROM VeTau WHERE maVe = ?";
        try (Connection conn = DatabaseConnection.getConnection(); 
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maVe);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Tạo mã vé unique
     */
    public String generateUniqueVeId() {
        String sql = "SELECT CONCAT('VE', LPAD(IFNULL(MAX(CAST(SUBSTRING(maVe, 3) AS UNSIGNED)), 0) + 1, 8, '0')) AS newId FROM VeTau";
        try (Connection conn = DatabaseConnection.getConnection(); 
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getString("newId");
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tạo mã vé: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Lấy thông tin chi tiết vé tàu theo mã vé
     */
    public VeTau getVeTauDetail(String maVe) {
        String sql = "{call sp_XemVeKhachHang_ByMaVe(?)}"; // Giả định có stored procedure này
        
        try (Connection conn = DatabaseConnection.getConnection(); 
             CallableStatement cs = conn.prepareCall(sql)) {
            
            cs.setString(1, maVe);
            ResultSet rs = cs.executeQuery();

            if (rs.next()) {
                VeTau ve = new VeTau();
                ve.setMaVe(rs.getString("maVe"));
                ve.setKhuHoi(rs.getBoolean("khuHoi"));
                ve.setTrangThai(rs.getString("trangThai"));

                // Mapping Lịch Trình
                LichTrinh lt = new LichTrinh();
                lt.setMaLichTrinh(rs.getString("maLichTrinh"));

                Date ngayDi = rs.getDate("ngayKhoiHanh");
                Time gioDi = rs.getTime("gioKhoiHanh");
                if (ngayDi != null) {
                    LocalDateTime dt = (gioDi != null) 
                        ? LocalDateTime.of(ngayDi.toLocalDate(), gioDi.toLocalTime()) 
                        : ngayDi.toLocalDate().atStartOfDay();
                    lt.setNgayGioKhoiHanh(dt);
                }

                // Mapping Tuyến Đường
                TuyenDuong td = new TuyenDuong();
                Ga gDi = new Ga();
                gDi.setViTriGa(rs.getString("DiemDi"));
                Ga gDen = new Ga();
                gDen.setViTriGa(rs.getString("DiemDen"));
                td.setDiemDi(gDi);
                td.setDiemDen(gDen);
                lt.setTuyenDuong(td);

                // Mapping Tàu
                Tau tau = new Tau();
                tau.setMacTau(rs.getString("tenTau"));
                lt.setTau(tau);

                // Mapping Chi Tiết Lịch Trình
                ChiTietLichTrinh ctlt = new ChiTietLichTrinh();
                ctlt.setLichTrinh(lt);
                ctlt.setGiaChoNgoi(rs.getDouble("giaChoNgoi"));
                
                Cho cho = new Cho();
                cho.setSoThuTu(rs.getInt("soGhe"));
                Toa toa = new Toa();
                toa.setTenToa(rs.getString("tenToa"));
                cho.setToa(toa);
                ctlt.setCho(cho);

                ve.setChiTietLichTrinh(ctlt);
                return ve;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy chi tiết vé: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}