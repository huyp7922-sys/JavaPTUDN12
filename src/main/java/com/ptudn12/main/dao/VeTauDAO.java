
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
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
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
                ve.setLoaiVe(LoaiVe.fromDescription(rs.getString("loaiVe")));
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
                ctlt.setGiaChoNgoi(rs.getDouble("giaChoNgoi")); 
                
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
    
    public int autoInvalidateExpiredTickets() {
        String sql = """
            UPDATE VeTau
            SET maQR = CONCAT('INVALID_', maQR) 
            FROM VeTau v
            JOIN ChiTietLichTrinh ct ON v.chiTietLichTrinhId = ct.maChiTietLichTrinh
            JOIN LichTrinh lt ON ct.maLichTrinh = lt.maLichTrinh
            WHERE v.trangThai = 'DaBan' 
              AND lt.ngayGioKhoiHanh < GETDATE()
              AND v.maQR NOT LIKE 'INVALID_%'
        """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            return ps.executeUpdate();
        } catch (SQLException e) {
            return 0;
        }
    }
    
    /**
     * Tạo mã vé: YYMMDD (6 số) + 6 số thứ tự.
     * Ví dụ: 251216000001 (Ngày 16/12/2025, vé số 1)
     * Max: 1 triệu vé/ngày.
     */
    public String generateUniqueVeId() {
        // Format ngày chỉ lấy 2 số cuối của năm: YYMMDD (Ví dụ: 251216)
        String prefix = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyMMdd"));
        
        int nextSequence = 1;
        
        // SQL: Lấy 6 ký tự cuối cùng (Bỏ 6 ký tự đầu là ngày tháng)
        String sqlQuery = "SELECT MAX(CAST(SUBSTRING(maVe, 7, 6) AS INT)) FROM VeTau WHERE maVe LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement psQuery = conn.prepareStatement(sqlQuery)) {

            psQuery.setString(1, prefix + "%");
            ResultSet rs = psQuery.executeQuery();
            if (rs.next()) {
                int maxSeq = rs.getInt(1);
                if (maxSeq > 0) nextSequence = maxSeq + 1;
            }
            
            // Format thành 6 chữ số (Tổng: 6 ngày + 6 số = 12 ký tự)
            return String.format("%s%06d", prefix, nextSequence);

        } catch (SQLException e) {
            System.err.println("Lỗi khi tạo mã vé: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    public List<VeDaMua> layLichSuMuaVeTheoKhachHang(int maKhachHang) {
    List<VeDaMua> danhSach = new ArrayList<>();
    String sql = "{call sp_XemVeKhachHang(?)}";

    try (Connection conn = DatabaseConnection.getConnection(); 
         CallableStatement stmt = conn.prepareCall(sql)) {

        stmt.setInt(1, maKhachHang);
        ResultSet rs = stmt.executeQuery();

        int sttCounter = 1;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
        Locale vietnameseLocale = new Locale("vi", "VN");
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(vietnameseLocale);

        while (rs.next()) {
            java.sql.Date ngayKhoiHanhSQL = rs.getDate("ngayKhoiHanh");
            Time gioKhoiHanhSQL = rs.getTime("gioKhoiHanh");
            
            int thoiGianDuKienGio = rs.getInt("thoiGianDuKien"); 
            
            LocalDateTime thoiGianDi = ngayKhoiHanhSQL.toLocalDate().atTime(gioKhoiHanhSQL.toLocalTime());
            LocalDateTime thoiGianDen = thoiGianDi.plusHours(thoiGianDuKienGio);
            String thoiGianDiDenFormatted = thoiGianDi.format(formatter) + " - " + thoiGianDen.format(formatter);

            String tenToa = rs.getString("tenToa");
            String loaiCho = rs.getString("loaiCho");
            
            // Ghép chuỗi: "Toa 1 - Ngồi mềm điều hòa"
            String toaLoaiChoFormatted = (tenToa != null ? tenToa : "") + " - " + (loaiCho != null ? loaiCho : "");

            String macTau = rs.getString("maTau");
            java.sql.Date ngayMuaSQL = rs.getDate("NgayMua");
            String ngayMuaFormatted = new SimpleDateFormat("dd/MM/yyyy").format(ngayMuaSQL);
            String hanhTrinh = rs.getString("DiemDi") + " - " + rs.getString("DiemDen");
            
            // Lưu ý: SQL trả về cột tên là "ViTriCho", code cũ lấy "ViTriCho" là đúng
            int soCho = rs.getInt("ViTriCho"); 
            double thanhTien = rs.getDouble("thanhTien");

            VeDaMua ve = new VeDaMua(sttCounter++, ngayMuaFormatted, rs.getString("maVe"), macTau, hanhTrinh,
                                    thoiGianDiDenFormatted, 
                                    toaLoaiChoFormatted, // <-- Chuỗi đã sửa
                                    soCho, currencyFormatter.format(thanhTien));
            danhSach.add(ve);
        }

    } catch (SQLException e) {
        System.err.println("Lỗi khi lấy lịch sử mua vé cho khách hàng " + maKhachHang + ": " + e.getMessage());
        e.printStackTrace();
    }

    return danhSach;
}
}