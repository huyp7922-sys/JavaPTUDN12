/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ptudn12.main.dao;

/**
 *
 * @author fo3cp
 */
import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import com.ptudn12.main.database.DatabaseConnection;

public class ChiTietLichTrinhDAO {
    /**
     * Lấy danh sách mã chỗ (maChoNgoi) đã được bán cho lịch trình cụ thể
     */
//    Cũ:
//    public Set<Integer> getCacChoDaBan(String maLichTrinh) {
//        Set<Integer> danhSachChoDaBan = new HashSet<>();
//        String sql = "SELECT maChoNgoi FROM ChiTietLichTrinh WHERE maLichTrinh = ?";
//
//        try (Connection conn = DatabaseConnection.getConnection();
//             PreparedStatement ps = conn.prepareStatement(sql)) {
//
//            ps.setString(1, maLichTrinh);
//            ResultSet rs = ps.executeQuery();
//
//            while (rs.next()) {
//                danhSachChoDaBan.add(rs.getInt("maChoNgoi"));
//            }
//
//        } catch (SQLException e) {
//            System.err.println("Lỗi khi lấy danh sách chỗ đã bán cho LT " + maLichTrinh + ": " + e.getMessage());
//            e.printStackTrace();
//        }
//
//        return danhSachChoDaBan;
//    }
    
    public Set<Integer> getCacChoDaBan(String maLichTrinh) {
        Set<Integer> danhSachChoDaBan = new HashSet<>();
        
        // CÂU SQL ĐÃ SỬA: Thêm điều kiện lọc trangThai
        String sql = "SELECT maChoNgoi FROM ChiTietLichTrinh " +
                     "WHERE maLichTrinh = ? " +
                     "AND trangThai IN (N'DaBan', N'DaDat')"; 
        
        // Lưu ý: DB của bạn dùng NVARCHAR có dấu (N'DaBan'), nên Java chuỗi cũng phải khớp.
        // Tốt nhất là dùng N'' trong SQL hoặc đảm bảo chuỗi Java gửi xuống đúng encoding.

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maLichTrinh);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                danhSachChoDaBan.add(rs.getInt("maChoNgoi"));
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách chỗ đã bán cho LT " + maLichTrinh + ": " + e.getMessage());
            e.printStackTrace();
        }

        return danhSachChoDaBan;
    }
    
    public int createChiTietLichTrinh(String maLichTrinh, int maCho, double giaChoNgoi, String trangThai) {
        String sql = "INSERT INTO ChiTietLichTrinh (maLichTrinh, maChoNgoi, giaChoNgoi, trangThai) VALUES (?, ?, ?, ?)";
        int generatedId = -1;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) { // Lấy ID tự tăng

            ps.setString(1, maLichTrinh);
            ps.setInt(2, maCho);
            ps.setDouble(3, giaChoNgoi);
            ps.setString(4, trangThai);

            int affectedRows = ps.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        generatedId = generatedKeys.getInt(1); // Lấy maChiTietLichTrinh vừa tạo
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi tạo chi tiết lịch trình cho LT " + maLichTrinh + ", Chỗ " + maCho + ": " + e.getMessage());
            e.printStackTrace();
        }
        return generatedId;
    }
    
    /**
     * Cập nhật trạng thái chỗ ngồi (Dùng khi Trả vé -> set lại thành N'ConTrong')
     */
    public boolean updateTrangThaiCho(int maChiTietLichTrinh, String trangThaiMoi) {
        String sql = "UPDATE ChiTietLichTrinh SET trangThai = ? WHERE maChiTietLichTrinh = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setNString(1, trangThaiMoi); 
            ps.setInt(2, maChiTietLichTrinh);
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
