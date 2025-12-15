
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
import com.ptudn12.main.entity.*;
import com.ptudn12.main.enums.LoaiCho;
import com.ptudn12.main.enums.LoaiToa;
import com.ptudn12.main.enums.LoaiVe;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.*;


public class VeTauDAO {
    public boolean createVeTau(String maVe, int khachHangId, int chiTietLichTrinhId, String loaiVe, boolean khuHoi, String trangThai) {
        String sql = "INSERT INTO VeTau (maVe, khachHangId, chiTietLichTrinhId, loaiVe, khuHoi, trangThai, maQR) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String maQR = "QR_" + maVe + "_" + System.currentTimeMillis(); // Mã QR tạm thời

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maVe);
            ps.setInt(2, khachHangId);
            ps.setInt(3, chiTietLichTrinhId);
            ps.setString(4, loaiVe);
            ps.setBoolean(5, khuHoi);
            ps.setString(6, trangThai);
            ps.setString(7, maQR);

            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
             System.err.println("Lỗi khi tạo vé tàu " + maVe + ": " + e.getMessage());
             e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Lấy danh sách vé đã mua của khách hàng bằng Stored Procedure
     * @param maKhachHang ID (int) của khách hàng
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
                
                // 1. Map Lịch Trình & Tuyến Đường & Ga & Tàu
                LichTrinh lt = new LichTrinh();
                lt.setMaLichTrinh(rs.getString("maLichTrinh"));
                
                // Ngày giờ
                java.sql.Date ngayDi = rs.getDate("ngayKhoiHanh");
                java.sql.Time gioDi = rs.getTime("gioKhoiHanh");
                if (ngayDi != null) {
                    LocalDateTime dt;
                    if (gioDi != null) {
                        dt = LocalDateTime.of(ngayDi.toLocalDate(), gioDi.toLocalTime());
                    } else {
                        dt = ngayDi.toLocalDate().atStartOfDay();
                    }
                    lt.setNgayGioKhoiHanh(dt);
                }
                
                // Tuyến
                TuyenDuong td = new TuyenDuong();
                Ga gDi = new Ga(); gDi.setViTriGa(rs.getString("DiemDi"));
                Ga gDen = new Ga(); gDen.setViTriGa(rs.getString("DiemDen"));
                td.setDiemDi(gDi);
                td.setDiemDen(gDen);
                lt.setTuyenDuong(td);
                
                Tau tau = new Tau();
                tau.setMacTau(rs.getString("maTau"));
                lt.setTau(tau);
                
                ve.setChiTietLichTrinh(new ChiTietLichTrinh()); // Init trước
                ve.getChiTietLichTrinh().setLichTrinh(lt);
                
                // 2. Map Chi Tiết Lịch Trình & Chỗ & Toa
                ChiTietLichTrinh ctlt = ve.getChiTietLichTrinh();
                // Set giá từ DB (Quan trọng cho việc hoàn tiền)
                ctlt.setGiaChoNgoi(rs.getDouble("giaChoNgoi")); 
                // Set ID để update trạng thái sau này (trong DB là int, entity là String)
                // Cần truy vấn thêm ID này nếu SP chưa trả về (SP hiện tại chưa trả về maChiTietLichTrinh ID)
                // Tuy nhiên, logic trả vé cần update trạng thái chỗ.
                // -> GIẢI PHÁP: Trong TraVeController, ta sẽ dùng hàm getChiTietLichTrinhIdByMaVe để lấy ID chính xác.
                
                Cho cho = new Cho();
                cho.setSoThuTu(rs.getInt("ViTriCho"));
                String strLoaiCho = rs.getString("loaiCho");
                
                Toa t = new Toa();
                t.setTenToa(rs.getString("tenToa"));
                if (strLoaiCho != null) {
                    try {
                        LoaiToa loaiToaEnum = LoaiToa.fromDescription(strLoaiCho); 
                        t.setLoaiToa(loaiToaEnum);
                    } catch (IllegalArgumentException e) {
                        System.err.println("Không map được loại toa: " + strLoaiCho);
                    }
                }

                cho.setToa(t);
                
                ctlt.setCho(cho);
                
                listVe.add(ve);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy lịch sử vé: " + e.getMessage());
            e.printStackTrace();
        }
        return listVe;
    }

    /**
     * Cập nhật trạng thái vé (DaHuy)
     */
    public boolean updateTrangThaiVe(String maVe, String trangThaiMoi) {
        String sql = "UPDATE VeTau SET trangThai = ? WHERE maVe = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, trangThaiMoi);
            ps.setString(2, maVe);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Lấy ID Chi Tiết Lịch Trình từ Mã Vé (Cần để giải phóng chỗ)
     */
    public int getChiTietLichTrinhIdByMaVe(String maVe) {
        String sql = "SELECT chiTietLichTrinhId FROM VeTau WHERE maVe = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maVe);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("chiTietLichTrinhId");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    public VeTau getVeTauDetail(String maVe) {
        VeTau ve = null;
        
        // SQL JOIN đã được điều chỉnh theo đúng schema DB mới
        String sql = "SELECT VT.maVe, VT.loaiVe, VT.trangThai, VT.maQR, " +
                     "       KH.tenKhachHang, KH.soCCCD, KH.hoChieu, " +
                     "       L.ngayKhoiHanh, L.gioKhoiHanh, " +
                     "       T.maTau, " + // Bảng Tau cột maTau
                     "       G1.viTriGa AS tenGaDi, " +
                     "       G2.viTriGa AS tenGaDen, " +
                     "       TOA.tenToa, TOA.maToa, " +
                     "       C.soThuTu AS soGhe, C.loaiCho AS tenLoaiCho, " +
                     "       CTLT.giaChoNgoi " +
                     "FROM VeTau VT " +
                     "JOIN KhachHang KH ON VT.khachHangId = KH.maKhachHang " +
                     "JOIN ChiTietLichTrinh CTLT ON VT.chiTietLichTrinhId = CTLT.maChiTietLichTrinh " +
                     "JOIN LichTrinh L ON CTLT.maLichTrinh = L.maLichTrinh " +
                     "JOIN Tau T ON L.maTau = T.maTau " +
                     "JOIN TuyenDuong TD ON L.maTuyenDuong = TD.maTuyen " +
                     "JOIN Ga G1 ON TD.diemDi = G1.maGa " +
                     "JOIN Ga G2 ON TD.diemDen = G2.maGa " +
                     "JOIN Cho C ON CTLT.maChoNgoi = C.maCho " +
                     "JOIN Toa TOA ON C.maToa = TOA.maToa " + // JOIN qua bảng Cho để lấy Toa chính xác
                     "WHERE VT.maVe = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maVe);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                ve = new VeTau();
                ve.setMaVe(rs.getString("maVe"));
                ve.setMaQR(rs.getString("maQR")); 
                
                // 1. Khách Hàng
                KhachHang kh = new KhachHang();
                kh.setTenKhachHang(rs.getString("tenKhachHang"));
                kh.setSoCCCD(rs.getString("soCCCD"));
                kh.setHoChieu(rs.getString("hoChieu"));
                ve.setKhachHang(kh);
                
                // 2. Lịch Trình (Ngày giờ, Tàu, Ga)
                LichTrinh lt = new LichTrinh();
                
                java.sql.Date ngayDi = rs.getDate("ngayKhoiHanh");
                java.sql.Time gioDi = rs.getTime("gioKhoiHanh");
                if (ngayDi != null) {
                    LocalDateTime dt = (gioDi != null) 
                        ? LocalDateTime.of(ngayDi.toLocalDate(), gioDi.toLocalTime()) 
                        : ngayDi.toLocalDate().atStartOfDay();
                    lt.setNgayGioKhoiHanh(dt);
                }
                
                Tau tau = new Tau(rs.getString("maTau"));
                tau.setMacTau(rs.getString("maTau"));
                lt.setTau(tau);
                
                TuyenDuong td = new TuyenDuong();
                Ga gaDi = new Ga(); gaDi.setViTriGa(rs.getString("tenGaDi"));
                Ga gaDen = new Ga(); gaDen.setViTriGa(rs.getString("tenGaDen"));
                td.setDiemDi(gaDi);
                td.setDiemDen(gaDen);
                lt.setTuyenDuong(td);
                
                // 3. Chi Tiết (Chỗ, Toa, Giá)
                ChiTietLichTrinh ctlt = new ChiTietLichTrinh();
                ctlt.setLichTrinh(lt);
                ctlt.setGiaChoNgoi(rs.getDouble("giaChoNgoi"));
                
                Cho cho = new Cho();
                cho.setSoThuTu(rs.getInt("soGhe"));
                
                // Map loại chỗ từ chuỗi DB (Ví dụ: "Ghế ngồi mềm")
                String tenLoaiChoDB = rs.getString("tenLoaiCho");
                try {
                    // Gọi hàm map từ String sang Enum mà mình đã fix cho bạn ở LoaiCho.java
                    cho.setLoaiCho(LoaiCho.fromDescription(tenLoaiChoDB));
                } catch (Exception e) {
                    System.err.println("Warning: Không map được loại chỗ '" + tenLoaiChoDB + "' sang Enum.");
                }
                
                Toa t = new Toa();
                t.setTenToa(rs.getString("tenToa"));
                t.setMaToa(rs.getInt("maToa")); 
                cho.setToa(t);
                
                ctlt.setCho(cho);
                ve.setChiTietLichTrinh(ctlt);
            }

        } catch (SQLException e) {
            System.err.println("Lỗi lấy chi tiết vé (SQL): " + e.getMessage());
            e.printStackTrace();
        }
        
        return ve;
    }
    
    /**
     * Tạo mã vé duy nhất (YYYYMMDD + 4 số).
     * @return Mã vé mới hoặc null nếu lỗi.
     */
    public String generateUniqueVeId() {
        LocalDate today = LocalDate.now();
        String prefix = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int nextSequence = 1;
        String sqlQuery = "SELECT MAX(CAST(SUBSTRING(maVe, 9, 4) AS INT)) FROM VeTau WHERE maVe LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement psQuery = conn.prepareStatement(sqlQuery)) {

            psQuery.setString(1, prefix + "%");
            ResultSet rs = psQuery.executeQuery();
            if (rs.next()) {
                nextSequence = rs.getInt(1) + 1;
            }
            return String.format("%s%04d", prefix, nextSequence);

        } catch (SQLException e) {
            System.err.println("Lỗi khi tạo mã vé: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
