/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ptudn12.main.dao;

import com.ptudn12.main.database.DatabaseConnection;
import com.ptudn12.main.entity.Tau;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Huy
 */
public class TauDAO {

    /**
     * Lấy tất cả tàu
     */
    public List<Tau> layTatCaTau() {
        List<Tau> danhSach = new ArrayList<>();
        String sql = "SELECT * FROM Tau ORDER BY maTau";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Tau tau = new Tau(rs.getString("maTau"));
                danhSach.add(tau);
            }
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách tàu: " + e.getMessage());
            e.printStackTrace();
        }
        
        return danhSach;
    }

    /**
     * Lấy tàu theo mã
     */
    public Tau layTauTheoMa(String maTau) {
        String sql = "SELECT * FROM Tau WHERE maTau = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, maTau);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return new Tau(rs.getString("maTau"));
            }
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy tàu: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
}