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

    public TuyenDuong findTuyenNguoc(String maTuyen) {
        TuyenDuong tuyenGoc = layTuyenDuongTheoMa(Integer.parseInt(maTuyen));
        
        if (tuyenGoc == null) {
            return null;
        }
        
        String sql = "SELECT T.*, " +
                     "G1.maGa AS maGaDi, G1.viTriGa AS viTriGaDi, G1.mocKm AS mocKmDi, " +
                     "G2.maGa AS maGaDen, G2.viTriGa AS viTriGaDen, G2.mocKm AS mocKmDen " +
                     "FROM TuyenDuong T " +
                     "INNER JOIN Ga G1 ON T.diemDi = G1.maGa " +
                     "INNER JOIN Ga G2 ON T.diemDen = G2.maGa " +
                     "WHERE T.diemDi = ? AND T.diemDen = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, tuyenGoc.getDiemDen().getMaGa());
            stmt.setInt(2, tuyenGoc.getDiemDi().getMaGa());
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                TuyenDuong tuyenNguoc = mapResultSetToTuyenDuong(rs);
                return tuyenNguoc;
            } else {
                return null; 
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            return null; 
        }
    }

    public boolean kiemTraTuyenCoLichTrinh(String maTuyen) {
        // Câu lệnh SQL đếm số lịch trình thuộc tuyến này
        String sql = "SELECT COUNT(*) FROM LichTrinh WHERE maTuyenDuong = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, Integer.parseInt(maTuyen));
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                int count = rs.getInt(1);
                return count > 0; // Trả về true nếu có ít nhất 1 lịch trình
            }
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi kiểm tra lịch trình của tuyến: " + e.getMessage());
            e.printStackTrace();
        }
        return false; // Mặc định trả về false nếu lỗi hoặc không có dữ liệu
    }

    public boolean xoaTuyenDuong(int maTuyen) {
        TuyenDuong tuyen = layTuyenDuongTheoMa(maTuyen);
        if (tuyen == null) {
            System.err.println("Không tìm thấy tuyến đường!");
            return false;
        }
        
        if (tuyen.getTrangThai() == TrangThai.Nhap) {
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

    private void capNhatGiaCoBan(int maTuyen) {
        String sql = "{CALL sp_TinhGiaCoBan(?)}";
        
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            stmt.setInt(1, maTuyen);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                float giaCoBan = rs.getFloat("giaCoBan");
                
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

    private TuyenDuong mapResultSetToTuyenDuong(ResultSet rs) throws SQLException {
        Ga gaDi = new Ga(rs.getInt("maGaDi"), rs.getString("viTriGaDi"), rs.getInt("mocKmDi"));
        Ga gaDen = new Ga(rs.getInt("maGaDen"), rs.getString("viTriGaDen"), rs.getInt("mocKmDen"));
        
        TuyenDuong tuyen = new TuyenDuong(gaDi, gaDen, rs.getInt("thoiGianDuKien"));
        tuyen.setMaTuyen(String.valueOf(rs.getInt("maTuyen")));
        tuyen.setGiaCoBan(rs.getFloat("giaCoBan"));
        tuyen.setTrangThai(TrangThai.valueOf(rs.getString("trangThai")));
        
        return tuyen;
    }

    
    /**
     * Lấy tất cả tuyến đường (alias cho layTatCaTuyenDuong)
     * Method này để tương thích với GenerateSchedulesDialogController
     */
    public List<TuyenDuong> getAllTuyenDuong() {
        return layTatCaTuyenDuong();
    }

}