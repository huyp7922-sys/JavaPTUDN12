package com.ptudn12.main.dao;

import com.ptudn12.main.database.DatabaseConnection;
import com.ptudn12.main.entity.Ga;
import com.ptudn12.main.entity.TuyenDuong;
import com.ptudn12.main.enums.TrangThai;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO cho Tuyến Đường
 */
public class TuyenDuongDAO {

    /**
     * Thêm tuyến đường mới
     * Giá cơ bản được tính tự động: soKm * 500 * heSoKhoangCach
     */
    public boolean themTuyenDuong(TuyenDuong tuyenDuong) {
        String sql = "{CALL sp_ThemTuyenDuong(?, ?, ?, ?)}";
        
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            stmt.setInt(1, tuyenDuong.getDiemDi().getMaGa());
            stmt.setInt(2, tuyenDuong.getDiemDen().getMaGa());
            stmt.setInt(3, tuyenDuong.getThoiGianDuKien());
            stmt.setString(4, tuyenDuong.getTrangThai().getTenTrangThai());
            
            int result = stmt.executeUpdate();
            System.out.println("Thêm tuyến đường thành công!");
            return result > 0;
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm tuyến đường: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Cập nhật tuyến đường
     */
    public boolean capNhatTuyenDuong(TuyenDuong tuyenDuong) {
        String sql = "UPDATE TuyenDuong SET diemDi = ?, diemDen = ?, thoiGianDuKien = ?, trangThai = ? WHERE maTuyen = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, tuyenDuong.getDiemDi().getMaGa());
            stmt.setInt(2, tuyenDuong.getDiemDen().getMaGa());
            stmt.setInt(3, tuyenDuong.getThoiGianDuKien());
            stmt.setString(4, tuyenDuong.getTrangThai().getTenTrangThai());
            stmt.setInt(5, Integer.parseInt(tuyenDuong.getMaTuyen()));
            
            int rows = stmt.executeUpdate();
            
            // Cập nhật lại giá cơ bản sau khi sửa
            if (rows > 0) {
                capNhatGiaCoBan(Integer.parseInt(tuyenDuong.getMaTuyen()));
                System.out.println("Cập nhật tuyến đường thành công!");
            }
            
            return rows > 0;
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật tuyến đường: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Xóa tuyến đường
     * - Chỉ xóa được tuyến có trạng thái "Nhap"
     * - Nếu không phải "Nhap" thì chuyển thành "TamNgung"
     */
    public boolean xoaTuyenDuong(int maTuyen) {
        // Kiểm tra trạng thái
        TuyenDuong tuyen = layTuyenDuongTheoMa(maTuyen);
        if (tuyen == null) {
            System.err.println("Không tìm thấy tuyến đường!");
            return false;
        }
        
        if (tuyen.getTrangThai() == TrangThai.Nhap) {
            // Xóa tuyến
            String sql = "DELETE FROM TuyenDuong WHERE maTuyen = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setInt(1, maTuyen);
                boolean result = stmt.executeUpdate() > 0;
                if (result) {
                    System.out.println("Xóa tuyến đường thành công!");
                }
                return result;
                
            } catch (SQLException e) {
                System.err.println("Lỗi khi xóa tuyến đường: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        } else {
            // Chuyển thành tạm ngưng
            String sql = "UPDATE TuyenDuong SET trangThai = 'TamNgung' WHERE maTuyen = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setInt(1, maTuyen);
                boolean result = stmt.executeUpdate() > 0;
                if (result) {
                    System.out.println("Tuyến đường không thể xóa, đã chuyển sang trạng thái Tạm Ngưng!");
                }
                return result;
                
            } catch (SQLException e) {
                System.err.println("Lỗi khi cập nhật trạng thái: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }
    }

    /**
     * Lấy tuyến đường theo mã
     */
    public TuyenDuong layTuyenDuongTheoMa(int maTuyen) {
        String sql = "SELECT T.*, " +
                    "G1.maGa AS maGaDi, G1.viTriGa AS viTriGaDi, G1.mocKm AS mocKmDi, " +
                    "G2.maGa AS maGaDen, G2.viTriGa AS viTriGaDen, G2.mocKm AS mocKmDen " +
                    "FROM TuyenDuong T " +
                    "INNER JOIN Ga G1 ON T.diemDi = G1.maGa " +
                    "INNER JOIN Ga G2 ON T.diemDen = G2.maGa " +
                    "WHERE T.maTuyen = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, maTuyen);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToTuyenDuong(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy tuyến đường: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * Lấy tất cả tuyến đường
     */
    public List<TuyenDuong> layTatCaTuyenDuong() {
        List<TuyenDuong> danhSach = new ArrayList<>();
        String sql = "SELECT T.*, " +
                    "G1.maGa AS maGaDi, G1.viTriGa AS viTriGaDi, G1.mocKm AS mocKmDi, " +
                    "G2.maGa AS maGaDen, G2.viTriGa AS viTriGaDen, G2.mocKm AS mocKmDen " +
                    "FROM TuyenDuong T " +
                    "INNER JOIN Ga G1 ON T.diemDi = G1.maGa " +
                    "INNER JOIN Ga G2 ON T.diemDen = G2.maGa " +
                    "ORDER BY T.maTuyen";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                danhSach.add(mapResultSetToTuyenDuong(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách tuyến đường: " + e.getMessage());
            e.printStackTrace();
        }
        
        return danhSach;
    }

    /**
     * Tìm kiếm tuyến đường theo điều kiện
     */
    public List<TuyenDuong> timTuyenDuong(Integer maGaDi, Integer maGaDen, TrangThai trangThai) {
        List<TuyenDuong> danhSach = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT T.*, " +
            "G1.maGa AS maGaDi, G1.viTriGa AS viTriGaDi, G1.mocKm AS mocKmDi, " +
            "G2.maGa AS maGaDen, G2.viTriGa AS viTriGaDen, G2.mocKm AS mocKmDen " +
            "FROM TuyenDuong T " +
            "INNER JOIN Ga G1 ON T.diemDi = G1.maGa " +
            "INNER JOIN Ga G2 ON T.diemDen = G2.maGa " +
            "WHERE 1=1"
        );
        
        if (maGaDi != null) {
            sql.append(" AND T.diemDi = ?");
        }
        if (maGaDen != null) {
            sql.append(" AND T.diemDen = ?");
        }
        if (trangThai != null) {
            sql.append(" AND T.trangThai = ?");
        }
        
        sql.append(" ORDER BY T.maTuyen");
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            int paramIndex = 1;
            if (maGaDi != null) {
                stmt.setInt(paramIndex++, maGaDi);
            }
            if (maGaDen != null) {
                stmt.setInt(paramIndex++, maGaDen);
            }
            if (trangThai != null) {
                stmt.setString(paramIndex++, trangThai.getTenTrangThai());
            }
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                danhSach.add(mapResultSetToTuyenDuong(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm kiếm tuyến đường: " + e.getMessage());
            e.printStackTrace();
        }
        
        return danhSach;
    }

    /**
     * Cập nhật giá cơ bản cho tuyến đường
     */
    private void capNhatGiaCoBan(int maTuyen) {
        String sql = "{CALL sp_TinhGiaCoBan(?)}";
        
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            stmt.setInt(1, maTuyen);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                float giaCoBan = rs.getFloat("giaCoBan");
                
                // Update giá vào database
                String updateSql = "UPDATE TuyenDuong SET giaCoBan = ? WHERE maTuyen = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setFloat(1, giaCoBan);
                    updateStmt.setInt(2, maTuyen);
                    updateStmt.executeUpdate();
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật giá cơ bản: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Map ResultSet sang TuyenDuong object
     */
    private TuyenDuong mapResultSetToTuyenDuong(ResultSet rs) throws SQLException {
        // Map Ga đi
        Ga gaDi = new Ga(rs.getString("viTriGaDi"), rs.getInt("mocKmDi"));
        gaDi.setMaGa(rs.getInt("maGaDi"));
        
        // Map Ga đến
        Ga gaDen = new Ga(rs.getString("viTriGaDen"), rs.getInt("mocKmDen"));
        gaDen.setMaGa(rs.getInt("maGaDen"));
        
        // Tạo TuyenDuong
        TuyenDuong tuyen = new TuyenDuong(gaDi, gaDen, rs.getInt("thoiGianDuKien"));
        tuyen.setMaTuyen(String.valueOf(rs.getInt("maTuyen")));
        tuyen.setGiaCoBan(rs.getFloat("giaCoBan"));
        tuyen.setTrangThai(TrangThai.valueOf(rs.getString("trangThai")));
        
        return tuyen;
    }
}