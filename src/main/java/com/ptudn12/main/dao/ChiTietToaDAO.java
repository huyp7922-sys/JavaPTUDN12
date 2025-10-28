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
import com.ptudn12.main.entity.ChiTietToa;
import com.ptudn12.main.entity.Cho;
import com.ptudn12.main.entity.Tau;
import com.ptudn12.main.entity.Toa;
import com.ptudn12.main.enums.LoaiCho;
import com.ptudn12.main.enums.LoaiToa;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChiTietToaDAO {
    public List<ChiTietToa> getChiTietToaByTau(String maTau) {
    List<ChiTietToa> list = new ArrayList<>();
    
    // 1. SỬA SQL: Thêm JOIN với bảng 'Cho' (c)
    String sql = "SELECT ct.*, t.loaiToa, c.loaiCho " + // Lấy thêm c.loaiCho
                 "FROM chitiettoa ct " +
                 "JOIN Toa t ON ct.maToa = t.maToa " +
                 "JOIN Cho c ON ct.maCho = c.maCho " +  // JOIN thêm bảng Cho
                 "WHERE ct.maTau = ? " +
                 "ORDER BY ct.maToa, ct.soThuTu";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setString(1, maTau);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            
            Tau tau = new Tau(rs.getString("maTau"));

            // --- 2. TẠO TOA (Đã có loaiToa) ---
            int maToa = rs.getInt("maToa");
            String loaiToaString = rs.getString("loaiToa");
            LoaiToa loaiToaEnum = LoaiToa.fromDescription(loaiToaString); 
            Toa toa = new Toa(maToa);
            toa.setLoaiToa(loaiToaEnum); 
            
            // --- 3. TẠO CHO (THÊM loaiCho) ---
            int maCho = rs.getInt("maCho");
            String loaiChoString = rs.getString("loaiCho"); // Lấy loaiCho từ SQL
            LoaiCho loaiChoEnum = LoaiCho.fromDescription(loaiChoString); // Chuyển sang Enum
            Cho cho = new Cho(maCho);
            cho.setLoaiCho(loaiChoEnum); // Gán giá trị Enum
            // (Bạn cần đảm bảo class Cho có setter: public void setLoaiCho(LoaiCho loaiCho))


            // 4. Tạo ChiTietToa với các đối tượng đã đầy đủ thông tin
            list.add(new ChiTietToa(
                rs.getInt("maChiTietToa"),
                tau,
                toa,    // Đã có loaiToa
                cho,    // ĐÃ CÓ loaiCho
                rs.getInt("soThuTu")
            ));
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }

    return list;
}
}
