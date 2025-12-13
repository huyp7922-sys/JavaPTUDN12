package com.ptudn12.main.dao;

import com.ptudn12.main.database.DatabaseConnection;
import com.ptudn12.main.entity.DailyRevenue;
import com.ptudn12.main.entity.ScheduleStatus;
import java.sql.*;
import java.util.*;

public class DashboardDAO {

    // Lấy tổng số tuyến đường
    public int getTotalRoutes() throws Exception {
        Connection con = DatabaseConnection.getConnection();
        String sql = "SELECT COUNT(*) AS total FROM TuyenDuong WHERE trangThai = 'SanSang'";
        
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        
        int total = 0;
        if (rs.next()) {
            total = rs.getInt("total");
        }
        
        rs.close();
        stmt.close();
        con.close();
        return total;
    }

    // Lấy lịch trình hôm nay
    public int getTodaySchedules() throws Exception {
        Connection con = DatabaseConnection.getConnection();
        String sql = "SELECT COUNT(*) AS total FROM LichTrinh WHERE CAST(ngayKhoiHanh AS DATE) = CAST(GETDATE() AS DATE)";
        
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        
        int total = 0;
        if (rs.next()) {
            total = rs.getInt("total");
        }
        
        rs.close();
        stmt.close();
        con.close();
        return total;
    }

    // Lấy tổng số tàu
    public int getTotalTrains() throws Exception {
        Connection con = DatabaseConnection.getConnection();
        String sql = "SELECT COUNT(*) AS total FROM Tau";
        
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        
        int total = 0;
        if (rs.next()) {
            total = rs.getInt("total");
        }
        
        rs.close();
        stmt.close();
        con.close();
        return total;
    }

    // Lấy doanh thu tháng này
    public long getMonthlyRevenue() throws Exception {
        Connection con = DatabaseConnection.getConnection();
        String sql = "SELECT ISNULL(SUM(tongTienHoaDon), 0) AS total FROM HoaDon " +
                     "WHERE MONTH(ngayLap) = MONTH(GETDATE()) AND YEAR(ngayLap) = YEAR(GETDATE())";
        
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        
        long total = 0;
        if (rs.next()) {
            total = rs.getLong("total");
        }
        
        rs.close();
        stmt.close();
        con.close();
        return total;
    }

    // Lấy doanh thu 7 ngày gần nhất
    public List<DailyRevenue> getDailyRevenueLastWeek() throws Exception {
        Connection con = DatabaseConnection.getConnection();
        
        // Tạo Map để lưu doanh thu theo ngày
        java.util.Map<String, Long> revenueMap = new java.util.HashMap<>();
        
        // Khởi tạo 7 ngày với doanh thu = 0
        java.time.LocalDate today = java.time.LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            java.time.LocalDate d = today.minusDays(i);
            revenueMap.put(d.toString(), 0L);
        }
        
        // Lấy doanh thu thực từ database
        String sql = "SELECT CAST(ngayLap AS DATE) AS ngay, ISNULL(SUM(tongTienHoaDon), 0) AS doanhThu " +
                     "FROM HoaDon " +
                     "WHERE ngayLap >= DATEADD(DAY, -6, CAST(GETDATE() AS DATE)) " +
                     "GROUP BY CAST(ngayLap AS DATE)";
        
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        
        // Cập nhật doanh thu thực vào map
        while (rs.next()) {
            java.sql.Date sqlDate = rs.getDate("ngay");
            long doanhThu = rs.getLong("doanhThu");
            String dateStr = sqlDate.toLocalDate().toString();
            revenueMap.put(dateStr, doanhThu);
        }
        
        // Chuyển map thành list và sắp xếp
        List<DailyRevenue> list = new ArrayList<>();
        for (java.util.Map.Entry<String, Long> entry : revenueMap.entrySet()) {
            list.add(new DailyRevenue(entry.getKey(), entry.getValue()));
        }
        list.sort((a, b) -> a.getNgay().compareTo(b.getNgay()));
        
        rs.close();
        stmt.close();
        con.close();
        return list;
    }

    // Lấy trạng thái lịch trình
    public List<ScheduleStatus> getScheduleStatus() throws Exception {
        Connection con = DatabaseConnection.getConnection();
        String sql = "SELECT trangThai, COUNT(*) AS soLuong FROM LichTrinh GROUP BY trangThai";
        
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        
        List<ScheduleStatus> list = new ArrayList<>();
        while (rs.next()) {
            String trangThai = rs.getString("trangThai");
            int soLuong = rs.getInt("soLuong");
            list.add(new ScheduleStatus(trangThai, soLuong));
        }
        
        rs.close();
        stmt.close();
        con.close();
        return list;
    }

    // Lấy hoạt động gần đây (10 hoạt động mới nhất)
    public List<String> getRecentActivities() throws Exception {
        Connection con = DatabaseConnection.getConnection();
        String sql = "SELECT TOP 10 " +
                     "  'Hóa đơn: ' + h.maHoaDon + ' | Ngày: ' + CONVERT(VARCHAR(10), h.ngayLap, 103) + " +
                     "   ' | Tiền: ' + CAST(h.tongTienHoaDon AS VARCHAR) AS activity " +
                     "FROM HoaDon h " +
                     "ORDER BY h.ngayLap DESC";
        
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        
        List<String> list = new ArrayList<>();
        while (rs.next()) {
            list.add(rs.getString("activity"));
        }
        
        rs.close();
        stmt.close();
        con.close();
        return list;
    }
}
