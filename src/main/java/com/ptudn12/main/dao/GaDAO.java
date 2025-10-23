/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ptudn12.main.dao;

import com.ptudn12.main.database.DatabaseConnection;
import com.ptudn12.main.entity.Ga;
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
public class GaDAO {

    /**
     * Lấy tất cả ga
     */
    public List<Ga> layTatCaGa() {
        List<Ga> danhSach = new ArrayList<>();
        String sql = "SELECT * FROM dbo.Ga ORDER BY mocKm";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Ga ga = new Ga(rs.getString("viTriGa"), rs.getInt("mocKm"));
                ga.setMaGa(rs.getInt("maGa"));
                danhSach.add(ga);
            }
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách ga: " + e.getMessage());
            e.printStackTrace();
        }
        
        return danhSach;
    }

    /**
     * Lấy ga theo mã
     */
    public Ga layGaTheoMa(int maGa) {
        String sql = "SELECT * FROM dbo.Ga WHERE maGa = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, maGa);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Ga ga = new Ga(rs.getString("viTriGa"), rs.getInt("mocKm"));
                ga.setMaGa(rs.getInt("maGa"));
                return ga;
            }
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy ga: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
}