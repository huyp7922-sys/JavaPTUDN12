package com.ptudn12.main.dao;

import com.ptudn12.main.database.DatabaseConnection;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class EmployeeDashboardDAO {
    
    // Lấy số vé bán trong ngày hôm nay
    public int getTicketsSoldToday() throws Exception {
        Connection con = DatabaseConnection.getConnection();
        String sql = "SELECT COUNT(*) FROM VeTau vt " +
                     "JOIN ChiTietLichTrinh ctlt ON vt.chiTietLichTrinhId = ctlt.maChiTietLichTrinh " +
                     "JOIN LichTrinh lt ON ctlt.maLichTrinh = lt.maLichTrinh " +
                     "WHERE vt.trangThai IN ('DaBan', 'DaSuDung') " +
                     "AND CAST(lt.ngayKhoiHanh AS DATE) = CAST(GETDATE() AS DATE)";
        PreparedStatement ps = con.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getInt(1);
        }
        return 0;
    }
    
    // Lấy tổng số vé đã bán (tất cả thời gian)
    public int getTotalTicketsSold() throws Exception {
        Connection con = DatabaseConnection.getConnection();
        String sql = "SELECT COUNT(*) FROM VeTau WHERE trangThai IN ('DaBan', 'DaSuDung')";
        PreparedStatement ps = con.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getInt(1);
        }
        return 0;
    }
    
    // Lấy doanh thu ước tính trong ngày
    public long getRevenueToday() throws Exception {
        Connection con = DatabaseConnection.getConnection();
        String sql = "SELECT ISNULL(SUM(ctlt.giaChoNgoi), 0) " +
                     "FROM VeTau vt " +
                     "JOIN ChiTietLichTrinh ctlt ON vt.chiTietLichTrinhId = ctlt.maChiTietLichTrinh " +
                     "JOIN LichTrinh lt ON ctlt.maLichTrinh = lt.maLichTrinh " +
                     "WHERE vt.trangThai IN ('DaBan', 'DaSuDung') " +
                     "AND CAST(lt.ngayKhoiHanh AS DATE) = CAST(GETDATE() AS DATE)";
        PreparedStatement ps = con.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getLong(1);
        }
        return 0;
    }
    
    // Lấy số vé đang chờ xử lý (Reserved)
    public int getPendingTickets() throws Exception {
        Connection con = DatabaseConnection.getConnection();
        String sql = "SELECT COUNT(*) FROM VeTau WHERE trangThai = 'DaDat'";
        PreparedStatement ps = con.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getInt(1);
        }
        return 0;
    }
    
    // Lấy danh sách vé gần đây (100 vé mới nhất)
    public List<Map<String, Object>> getRecentTickets() throws Exception {
        Connection con = DatabaseConnection.getConnection();
        String sql = 
            "SELECT TOP 100 " +
            "  vt.maVe, " +
            "  kh.tenKhachHang AS hanhKhach, " +
            "  CONCAT(g1.viTriGa, ' → ', g2.viTriGa) AS tuyen, " +
            "  CAST(CAST(lt.ngayKhoiHanh AS DATE) AS DATETIME) + CAST(CAST(lt.gioKhoiHanh AS TIME) AS DATETIME) AS thoiGian, " +
            "  CONCAT(c.soThuTu, '') AS cho, " +
            "  vt.trangThai, " +
            "  ctlt.giaChoNgoi " +
            "FROM VeTau vt " +
            "LEFT JOIN KhachHang kh ON vt.khachHangId = kh.maKhachHang " +
            "LEFT JOIN ChiTietLichTrinh ctlt ON vt.chiTietLichTrinhId = ctlt.maChiTietLichTrinh " +
            "LEFT JOIN Cho c ON ctlt.maChoNgoi = c.maCho " +
            "LEFT JOIN LichTrinh lt ON ctlt.maLichTrinh = lt.maLichTrinh " +
            "LEFT JOIN TuyenDuong td ON lt.maTuyenDuong = td.maTuyen " +
            "LEFT JOIN Ga g1 ON td.diemDi = g1.maGa " +
            "LEFT JOIN Ga g2 ON td.diemDen = g2.maGa " +
            "ORDER BY CASE WHEN vt.trangThai = 'DaDat' THEN 0 ELSE 1 END, lt.ngayKhoiHanh DESC, vt.maVe DESC";
        
        PreparedStatement ps = con.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        
        List<Map<String, Object>> tickets = new ArrayList<>();
        while (rs.next()) {
            Map<String, Object> ticket = new HashMap<>();
            ticket.put("maVe", rs.getString("maVe"));
            ticket.put("hanhKhach", rs.getString("hanhKhach"));
            ticket.put("tuyen", rs.getString("tuyen"));
            ticket.put("thoiGian", rs.getTimestamp("thoiGian"));
            ticket.put("cho", rs.getString("cho"));
            ticket.put("trangThai", rs.getString("trangThai"));
            ticket.put("giaChoNgoi", rs.getLong("giaChoNgoi"));
            tickets.add(ticket);
        }
        
        return tickets;
    }
    
    // Lấy danh sách chuyến tàu sắp khởi hành (trong 24h tới)
    public List<Map<String, Object>> getUpcomingTrips() throws Exception {
        Connection con = DatabaseConnection.getConnection();
        String sql = 
            "SELECT TOP 10 " +
            "  CONCAT(g1.viTriGa, ' → ', g2.viTriGa) AS tuyen, " +
            "  CAST(CAST(lt.ngayKhoiHanh AS DATE) AS DATETIME) + CAST(CAST(lt.gioKhoiHanh AS TIME) AS DATETIME) AS thoiGianKhoiHanh, " +
            "  COUNT(CASE WHEN vt.trangThai IN ('DaBan', 'DaSuDung') THEN 1 END) AS soVe " +
            "FROM LichTrinh lt " +
            "JOIN TuyenDuong td ON lt.maTuyenDuong = td.maTuyen " +
            "JOIN Ga g1 ON td.diemDi = g1.maGa " +
            "JOIN Ga g2 ON td.diemDen = g2.maGa " +
            "LEFT JOIN ChiTietLichTrinh ctlt ON lt.maLichTrinh = ctlt.maLichTrinh " +
            "LEFT JOIN VeTau vt ON ctlt.maChiTietLichTrinh = vt.chiTietLichTrinhId " +
            "WHERE CAST(CAST(lt.ngayKhoiHanh AS DATE) AS DATETIME) + CAST(CAST(lt.gioKhoiHanh AS TIME) AS DATETIME) BETWEEN GETDATE() AND DATEADD(HOUR, 24, GETDATE()) " +
            "AND lt.trangThai IN ('ChuaKhoiHanh', 'SanSang') " +
            "GROUP BY td.maTuyen, g1.viTriGa, g2.viTriGa, lt.ngayKhoiHanh, lt.gioKhoiHanh " +
            "ORDER BY lt.ngayKhoiHanh, lt.gioKhoiHanh";
        
        PreparedStatement ps = con.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        
        List<Map<String, Object>> trips = new ArrayList<>();
        while (rs.next()) {
            Map<String, Object> trip = new HashMap<>();
            trip.put("tuyen", rs.getString("tuyen"));
            trip.put("thoiGian", rs.getTimestamp("thoiGianKhoiHanh"));
            trip.put("soVe", rs.getInt("soVe"));
            trips.add(trip);
        }
        
        return trips;
    }
    
    // Cập nhật trạng thái vé
    public boolean updateTicketStatus(String maVe, String trangThai) throws Exception {
        Connection con = DatabaseConnection.getConnection();
        String sql = "UPDATE VeTau SET trangThai = ? WHERE maVe = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, trangThai);
        ps.setString(2, maVe);
        int affectedRows = ps.executeUpdate();
        ps.close();
        con.close();
        return affectedRows > 0;
    }
    
    // Lấy danh sách vé chờ xử lý
    public List<Map<String, Object>> getPendingTicketsList() throws Exception {
        Connection con = DatabaseConnection.getConnection();
        String sql = 
            "SELECT TOP 100 " +
            "  vt.maVe, " +
            "  kh.tenKhachHang AS hanhKhach, " +
            "  CONCAT(g1.viTriGa, ' → ', g2.viTriGa) AS tuyen " +
            "FROM VeTau vt " +
            "LEFT JOIN KhachHang kh ON vt.khachHangId = kh.maKhachHang " +
            "LEFT JOIN ChiTietLichTrinh ctlt ON vt.chiTietLichTrinhId = ctlt.maChiTietLichTrinh " +
            "LEFT JOIN LichTrinh lt ON ctlt.maLichTrinh = lt.maLichTrinh " +
            "LEFT JOIN TuyenDuong td ON lt.maTuyenDuong = td.maTuyen " +
            "LEFT JOIN Ga g1 ON td.diemDi = g1.maGa " +
            "LEFT JOIN Ga g2 ON td.diemDen = g2.maGa " +
            "WHERE vt.trangThai = 'DaDat' " +
            "ORDER BY lt.ngayKhoiHanh DESC";
        
        PreparedStatement ps = con.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        
        List<Map<String, Object>> tickets = new ArrayList<>();
        while (rs.next()) {
            Map<String, Object> ticket = new HashMap<>();
            ticket.put("maVe", rs.getString("maVe"));
            ticket.put("hanhKhach", rs.getString("hanhKhach"));
            ticket.put("tuyen", rs.getString("tuyen"));
            tickets.add(ticket);
        }
        
        return tickets;
    }
}

