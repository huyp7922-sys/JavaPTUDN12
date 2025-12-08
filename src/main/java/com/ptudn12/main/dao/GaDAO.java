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
                Ga ga = new Ga(rs.getInt("maGa"), rs.getString("viTriGa"), rs.getInt("mocKm"));
                danhSach.add(ga);
            }
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách ga: " + e.getMessage());
            e.printStackTrace();
        }
        
        return danhSach;
    }
    
    /**
     * Lấy tên ga để bán vế
     */
    public List<String> layViTriGa() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT viTriGa FROM dbo.Ga ORDER BY mocKm";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                String viTriGa = rs.getString("viTriGa");
                list.add(viTriGa);
            }
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách ga: " + e.getMessage());
            e.printStackTrace();
        }
        
        return list;
    }
    
    /**
     * Lấy thông tin Ga đầy đủ dựa vào viTriGa (tên ga)
     */
    public Ga layGaTheoViTri(String viTriGa) {
        String sql = "SELECT * FROM dbo.Ga WHERE viTriGa = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, viTriGa);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Ga ga = new Ga(
                    rs.getInt("maGa"), 
                    rs.getString("viTriGa"), 
                    rs.getInt("mocKm")
                );
                return ga;
            }
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy ga theo vị trí: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null; // Không tìm thấy
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
                Ga ga = new Ga(rs.getInt("maGa") , rs.getString("viTriGa"), rs.getInt("mocKm"));
                return ga;
            }
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy ga: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
}