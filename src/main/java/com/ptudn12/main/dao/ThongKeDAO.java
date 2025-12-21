package com.ptudn12.main.dao;

import com.ptudn12.main.database.DatabaseConnection;
import com.ptudn12.main.entity.ThongKe;
import com.ptudn12.main.entity.ThongKeKhachHang;
import java.sql.*;
import java.util.*;

public class ThongKeDAO {

    public List<ThongKe> getAllStatistics() throws Exception {
        Connection con = DatabaseConnection.getConnection();
        String sql = "SELECT " +
                "  td.maTuyen, " +
                "  CONCAT(g1.viTriGa, N' Đến ', g2.viTriGa) AS tenTuyen, " +
                "  COUNT(DISTINCT ctlt.maChiTietLichTrinh) AS tongVe, " +
                "  COUNT(DISTINCT CASE WHEN vt.trangThai IN ('DaBan', 'DaSuDung') THEN vt.maVe END) AS soVeBan, " +
                "  ISNULL(CAST(100.0 * COUNT(DISTINCT CASE WHEN vt.trangThai IN ('DaBan', 'DaSuDung') THEN vt.maVe END) / " +
                "           NULLIF(COUNT(DISTINCT ctlt.maChiTietLichTrinh), 0) AS FLOAT), 0) AS tyLe, " +
                "  COUNT(DISTINCT l.maLichTrinh) AS soChuyen, " +
                "  ISNULL(SUM(CASE WHEN vt.trangThai IN ('DaBan', 'DaSuDung') THEN ctlt.giaChoNgoi ELSE 0 END), 0) AS doanhThu "
                +
                "FROM TuyenDuong td " +
                "LEFT JOIN Ga g1 ON td.diemDi = g1.maGa " +
                "LEFT JOIN Ga g2 ON td.diemDen = g2.maGa " +
                "LEFT JOIN LichTrinh l ON td.maTuyen = l.maTuyenDuong " +
                "LEFT JOIN ChiTietLichTrinh ctlt ON l.maLichTrinh = ctlt.maLichTrinh " +
                "LEFT JOIN VeTau vt ON ctlt.maChiTietLichTrinh = vt.chiTietLichTrinhId " +
                "WHERE l.maLichTrinh IS NOT NULL " +
                "GROUP BY td.maTuyen, g1.viTriGa, g2.viTriGa " +
                "ORDER BY td.maTuyen";

        PreparedStatement ps = con.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        List<ThongKe> list = new ArrayList<>();
        while (rs.next()) {
            list.add(new ThongKe(
                    String.valueOf(rs.getInt("maTuyen")),
                    rs.getString("tenTuyen"),
                    rs.getInt("tongVe"),
                    rs.getInt("soVeBan"),
                    rs.getDouble("tyLe"),
                    rs.getInt("soChuyen"),
                    rs.getLong("doanhThu")));
        }

        return list;
    }

    public List<ThongKeKhachHang> getTopCustomers(int limit) throws Exception {
        Connection con = DatabaseConnection.getConnection();
        String sql = "SELECT TOP (?) " +
                "  kh.maKhachHang, " +
                "  kh.tenKhachHang, " +
                "  kh.soDienThoai, " +
                "  COUNT(vt.maVe) AS soVeDaMua, " +
                "  SUM(ctlt.giaChoNgoi) AS tongTien " +
                "FROM KhachHang kh " +
                "JOIN VeTau vt ON kh.maKhachHang = vt.khachHangId " +
                "JOIN ChiTietLichTrinh ctlt ON vt.chiTietLichTrinhId = ctlt.maChiTietLichTrinh " +
                "WHERE vt.trangThai IN ('DaBan', 'DaSuDung') " +
                "GROUP BY kh.maKhachHang, kh.tenKhachHang, kh.soDienThoai " +
                "ORDER BY soVeDaMua DESC, tongTien DESC";

        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, limit);
        ResultSet rs = ps.executeQuery();

        List<ThongKeKhachHang> list = new ArrayList<>();
        while (rs.next()) {
            list.add(new ThongKeKhachHang(
                    String.valueOf(rs.getInt("maKhachHang")),
                    rs.getString("tenKhachHang"),
                    rs.getString("soDienThoai"),
                    rs.getInt("soVeDaMua"),
                    rs.getLong("tongTien")));
        }
        return list;
    }

    public List<ThongKe> getStatisticsByDate(String date) throws Exception {
        Connection con = DatabaseConnection.getConnection();
        String sql = "SELECT " +
                "  td.maTuyen, " +
                "  CONCAT(g1.viTriGa, N' Đến ', g2.viTriGa) AS tenTuyen, " +
                "  COUNT(DISTINCT ctlt.maChiTietLichTrinh) AS tongVe, " +
                "  COUNT(DISTINCT CASE WHEN vt.trangThai IN ('DaBan', 'DaSuDung') THEN vt.maVe END) AS soVeBan, " +
                "  ISNULL(CAST(100.0 * COUNT(DISTINCT CASE WHEN vt.trangThai IN ('DaBan', 'DaSuDung') THEN vt.maVe END) / " +
                "           NULLIF(COUNT(DISTINCT ctlt.maChiTietLichTrinh), 0) AS FLOAT), 0) AS tyLe, " +
                "  COUNT(DISTINCT l.maLichTrinh) AS soChuyen, " +
                "  ISNULL(SUM(CASE WHEN vt.trangThai IN ('DaBan', 'DaSuDung') THEN ctlt.giaChoNgoi ELSE 0 END), 0) AS doanhThu " +
                "FROM TuyenDuong td " +
                "LEFT JOIN Ga g1 ON td.diemDi = g1.maGa " +
                "LEFT JOIN Ga g2 ON td.diemDen = g2.maGa " +
                "LEFT JOIN LichTrinh l ON td.maTuyen = l.maTuyenDuong AND CAST(l.ngayKhoiHanh AS DATE) = ? " +
                "LEFT JOIN ChiTietLichTrinh ctlt ON l.maLichTrinh = ctlt.maLichTrinh " +
                "LEFT JOIN VeTau vt ON ctlt.maChiTietLichTrinh = vt.chiTietLichTrinhId " +
                "WHERE l.maLichTrinh IS NOT NULL " +
                "GROUP BY td.maTuyen, g1.viTriGa, g2.viTriGa " +
                "ORDER BY td.maTuyen";

        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, date);
        ResultSet rs = ps.executeQuery();

        List<ThongKe> list = new ArrayList<>();
        while (rs.next()) {
            list.add(new ThongKe(
                    String.valueOf(rs.getInt("maTuyen")),
                    rs.getString("tenTuyen"),
                    rs.getInt("tongVe"),
                    rs.getInt("soVeBan"),
                    rs.getDouble("tyLe"),
                    rs.getInt("soChuyen"),
                    rs.getLong("doanhThu")));
        }
        return list;
    }

    public List<ThongKe> getStatisticsByMonth(int month, int year) throws Exception {
        Connection con = DatabaseConnection.getConnection();
        String sql = "SELECT " +
                "  td.maTuyen, " +
                "  CONCAT(g1.viTriGa, N' Đến ', g2.viTriGa) AS tenTuyen, " +
                "  COUNT(DISTINCT ctlt.maChiTietLichTrinh) AS tongVe, " +
                "  COUNT(DISTINCT CASE WHEN vt.trangThai IN ('DaBan', 'DaSuDung') THEN vt.maVe END) AS soVeBan, " +
                "  ISNULL(CAST(100.0 * COUNT(DISTINCT CASE WHEN vt.trangThai IN ('DaBan', 'DaSuDung') THEN vt.maVe END) / " +
                "           NULLIF(COUNT(DISTINCT ctlt.maChiTietLichTrinh), 0) AS FLOAT), 0) AS tyLe, " +
                "  COUNT(DISTINCT l.maLichTrinh) AS soChuyen, " +
                "  ISNULL(SUM(CASE WHEN vt.trangThai IN ('DaBan', 'DaSuDung') THEN ctlt.giaChoNgoi ELSE 0 END), 0) AS doanhThu " +
                "FROM TuyenDuong td " +
                "LEFT JOIN Ga g1 ON td.diemDi = g1.maGa " +
                "LEFT JOIN Ga g2 ON td.diemDen = g2.maGa " +
                "LEFT JOIN LichTrinh l ON td.maTuyen = l.maTuyenDuong AND MONTH(l.ngayKhoiHanh) = ? AND YEAR(l.ngayKhoiHanh) = ? " +
                "LEFT JOIN ChiTietLichTrinh ctlt ON l.maLichTrinh = ctlt.maLichTrinh " +
                "LEFT JOIN VeTau vt ON ctlt.maChiTietLichTrinh = vt.chiTietLichTrinhId " +
                "WHERE l.maLichTrinh IS NOT NULL " +
                "GROUP BY td.maTuyen, g1.viTriGa, g2.viTriGa " +
                "ORDER BY td.maTuyen";

        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, month);
        ps.setInt(2, year);
        ResultSet rs = ps.executeQuery();

        List<ThongKe> list = new ArrayList<>();
        while (rs.next()) {
            list.add(new ThongKe(
                    String.valueOf(rs.getInt("maTuyen")),
                    rs.getString("tenTuyen"),
                    rs.getInt("tongVe"),
                    rs.getInt("soVeBan"),
                    rs.getDouble("tyLe"),
                    rs.getInt("soChuyen"),
                    rs.getLong("doanhThu")));
        }
        return list;
    }

    public List<ThongKe> getStatisticsByYear(int year) throws Exception {
        Connection con = DatabaseConnection.getConnection();
        String sql = "SELECT " +
                "  td.maTuyen, " +
                "  CONCAT(g1.viTriGa, N' Đến ', g2.viTriGa) AS tenTuyen, " +
                "  COUNT(DISTINCT ctlt.maChiTietLichTrinh) AS tongVe, " +
                "  COUNT(DISTINCT CASE WHEN vt.trangThai IN ('DaBan', 'DaSuDung') THEN vt.maVe END) AS soVeBan, " +
                "  ISNULL(CAST(100.0 * COUNT(DISTINCT CASE WHEN vt.trangThai IN ('DaBan', 'DaSuDung') THEN vt.maVe END) / " +
                "           NULLIF(COUNT(DISTINCT ctlt.maChiTietLichTrinh), 0) AS FLOAT), 0) AS tyLe, " +
                "  COUNT(DISTINCT l.maLichTrinh) AS soChuyen, " +
                "  ISNULL(SUM(CASE WHEN vt.trangThai IN ('DaBan', 'DaSuDung') THEN ctlt.giaChoNgoi ELSE 0 END), 0) AS doanhThu " +
                "FROM TuyenDuong td " +
                "LEFT JOIN Ga g1 ON td.diemDi = g1.maGa " +
                "LEFT JOIN Ga g2 ON td.diemDen = g2.maGa " +
                "LEFT JOIN LichTrinh l ON td.maTuyen = l.maTuyenDuong AND YEAR(l.ngayKhoiHanh) = ? " +
                "LEFT JOIN ChiTietLichTrinh ctlt ON l.maLichTrinh = ctlt.maLichTrinh " +
                "LEFT JOIN VeTau vt ON ctlt.maChiTietLichTrinh = vt.chiTietLichTrinhId " +
                "WHERE l.maLichTrinh IS NOT NULL " +
                "GROUP BY td.maTuyen, g1.viTriGa, g2.viTriGa " +
                "ORDER BY td.maTuyen";

        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, year);
        ResultSet rs = ps.executeQuery();

        List<ThongKe> list = new ArrayList<>();
        while (rs.next()) {
            list.add(new ThongKe(
                    String.valueOf(rs.getInt("maTuyen")),
                    rs.getString("tenTuyen"),
                    rs.getInt("tongVe"),
                    rs.getInt("soVeBan"),
                    rs.getDouble("tyLe"),
                    rs.getInt("soChuyen"),
                    rs.getLong("doanhThu")));
        }
        return list;
    }
}
