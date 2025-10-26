package com.ptudn12.main.dao;

import com.ptudn12.main.database.DatabaseConnection;
import com.ptudn12.main.entity.Ga;
import com.ptudn12.main.entity.LichTrinh;
import com.ptudn12.main.entity.Tau;
import com.ptudn12.main.entity.TuyenDuong;
import com.ptudn12.main.enums.TrangThai;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO cho Lịch Trình
 */
public class LichTrinhDAO {

    private TuyenDuongDAO tuyenDuongDAO = new TuyenDuongDAO();

    /**
     * Thêm lịch trình mới
     */
    public boolean themLichTrinh(LichTrinh lichTrinh) {
        String sql = "INSERT INTO LichTrinh (maLichTrinh, maTuyenDuong, maTau, ngayKhoiHanh, gioKhoiHanh, trangThai) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, "TEMP");
            stmt.setInt(2, Integer.parseInt(lichTrinh.getTuyenDuong().getMaTuyen()));
            stmt.setString(3, lichTrinh.getTau().getMacTau());
            stmt.setDate(4, Date.valueOf(lichTrinh.getNgayGioKhoiHanh().toLocalDate()));
            stmt.setTime(5, Time.valueOf(lichTrinh.getNgayGioKhoiHanh().toLocalTime()));
            stmt.setString(6, lichTrinh.getTrangThai().getTenTrangThai());
            
            boolean result = stmt.executeUpdate() > 0;
            if (result) {
                System.out.println("Thêm lịch trình thành công!");
            }
            return result;
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm lịch trình: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Cập nhật lịch trình
     */
    public boolean capNhatLichTrinh(LichTrinh lichTrinh) {
        String sql = "UPDATE LichTrinh SET maTuyenDuong = ?, maTau = ?, ngayKhoiHanh = ?, gioKhoiHanh = ?, trangThai = ? " +
                    "WHERE maLichTrinh = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, Integer.parseInt(lichTrinh.getTuyenDuong().getMaTuyen()));
            stmt.setString(2, lichTrinh.getTau().getMacTau());
            stmt.setDate(3, Date.valueOf(lichTrinh.getNgayGioKhoiHanh().toLocalDate()));
            stmt.setTime(4, Time.valueOf(lichTrinh.getNgayGioKhoiHanh().toLocalTime()));
            stmt.setString(5, lichTrinh.getTrangThai().getTenTrangThai());
            stmt.setString(6, lichTrinh.getMaLichTrinh());
            
            boolean result = stmt.executeUpdate() > 0;
            if (result) {
                System.out.println("Cập nhật lịch trình thành công!");
            }
            return result;
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật lịch trình: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Xóa lịch trình
     */
    public boolean xoaLichTrinh(String maLichTrinh) {
        LichTrinh lichTrinh = layLichTrinhTheoMa(maLichTrinh);
        if (lichTrinh == null) {
            System.err.println(" Không tìm thấy lịch trình!");
            return false;
        }
        
        if (lichTrinh.getTrangThai() == TrangThai.Nhap) {
            String sql = "DELETE FROM LichTrinh WHERE maLichTrinh = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, maLichTrinh);
                boolean result = stmt.executeUpdate() > 0;
                if (result) {
                    System.out.println("Xóa lịch trình thành công!");
                }
                return result;
                
            } catch (SQLException e) {
                System.err.println("Lỗi khi xóa lịch trình: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        } else {
            String sql = "UPDATE LichTrinh SET trangThai = 'TamNgung' WHERE maLichTrinh = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, maLichTrinh);
                boolean result = stmt.executeUpdate() > 0;
                if (result) {
                    System.out.println("Lịch trình không thể xóa, đã chuyển sang Tạm Ngưng!");
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
     * Lấy lịch trình theo mã
     */
    public LichTrinh layLichTrinhTheoMa(String maLichTrinh) {
        String sql = "SELECT " +
                    "L.maLichTrinh, L.maTau, L.maTuyenDuong, L.ngayKhoiHanh, L.gioKhoiHanh, " +
                    "L.trangThai AS trangThaiLichTrinh, " +
                    "T.maTuyen, T.thoiGianDuKien, T.giaCoBan, T.trangThai AS trangThaiTuyen, " +
                    "G1.maGa AS maGaDi, G1.viTriGa AS viTriGaDi, G1.mocKm AS mocKmDi, " +
                    "G2.maGa AS maGaDen, G2.viTriGa AS viTriGaDen, G2.mocKm AS mocKmDen " +
                    "FROM LichTrinh L " +
                    "INNER JOIN TuyenDuong T ON L.maTuyenDuong = T.maTuyen " +
                    "INNER JOIN Ga G1 ON T.diemDi = G1.maGa " +
                    "INNER JOIN Ga G2 ON T.diemDen = G2.maGa " +
                    "WHERE L.maLichTrinh = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, maLichTrinh);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToLichTrinh(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy lịch trình: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * Lấy tất cả lịch trình
     */
    public List<LichTrinh> layTatCaLichTrinh() {
        List<LichTrinh> danhSach = new ArrayList<>();
        
        String sql = "SELECT " +
                    "L.maLichTrinh, L.maTau, L.maTuyenDuong, L.ngayKhoiHanh, L.gioKhoiHanh, " +
                    "L.trangThai AS trangThaiLichTrinh, " +
                    "T.maTuyen, T.thoiGianDuKien, T.giaCoBan, T.trangThai AS trangThaiTuyen, " +
                    "G1.maGa AS maGaDi, G1.viTriGa AS viTriGaDi, G1.mocKm AS mocKmDi, " +
                    "G2.maGa AS maGaDen, G2.viTriGa AS viTriGaDen, G2.mocKm AS mocKmDen " +
                    "FROM LichTrinh L " +
                    "INNER JOIN TuyenDuong T ON L.maTuyenDuong = T.maTuyen " +
                    "INNER JOIN Ga G1 ON T.diemDi = G1.maGa " +
                    "INNER JOIN Ga G2 ON T.diemDen = G2.maGa " +
                    "ORDER BY L.ngayKhoiHanh DESC, L.gioKhoiHanh DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                LichTrinh lt = mapResultSetToLichTrinh(rs);
                if (lt != null) {
                    danhSach.add(lt);
                }
            }
            
            System.out.println("Lấy được " + danhSach.size() + " lịch trình");
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách lịch trình: " + e.getMessage());
            e.printStackTrace();
        }
        
        return danhSach;
    }

    /**
     * Tìm kiếm lịch trình
     */
    public List<LichTrinh> timLichTrinh(Integer maTuyen, String maTau, LocalDateTime tuNgay, LocalDateTime denNgay, TrangThai trangThai) {
        List<LichTrinh> danhSach = new ArrayList<>();
        
        StringBuilder sql = new StringBuilder(
            "SELECT " +
            "L.maLichTrinh, L.maTau, L.maTuyenDuong, L.ngayKhoiHanh, L.gioKhoiHanh, " +
            "L.trangThai AS trangThaiLichTrinh, " +
            "T.maTuyen, T.thoiGianDuKien, T.giaCoBan, T.trangThai AS trangThaiTuyen, " +
            "G1.maGa AS maGaDi, G1.viTriGa AS viTriGaDi, G1.mocKm AS mocKmDi, " +
            "G2.maGa AS maGaDen, G2.viTriGa AS viTriGaDen, G2.mocKm AS mocKmDen " +
            "FROM LichTrinh L " +
            "INNER JOIN TuyenDuong T ON L.maTuyenDuong = T.maTuyen " +
            "INNER JOIN Ga G1 ON T.diemDi = G1.maGa " +
            "INNER JOIN Ga G2 ON T.diemDen = G2.maGa " +
            "WHERE 1=1"
        );
        
        if (maTuyen != null) sql.append(" AND L.maTuyenDuong = ?");
        if (maTau != null && !maTau.isEmpty()) sql.append(" AND L.maTau = ?");
        if (tuNgay != null) sql.append(" AND CAST(L.ngayKhoiHanh AS DATETIME) + CAST(L.gioKhoiHanh AS DATETIME) >= ?");
        if (denNgay != null) sql.append(" AND CAST(L.ngayKhoiHanh AS DATETIME) + CAST(L.gioKhoiHanh AS DATETIME) <= ?");
        if (trangThai != null) sql.append(" AND L.trangThai = ?");
        
        sql.append(" ORDER BY L.ngayKhoiHanh DESC, L.gioKhoiHanh DESC");
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            int paramIndex = 1;
            if (maTuyen != null) stmt.setInt(paramIndex++, maTuyen);
            if (maTau != null && !maTau.isEmpty()) stmt.setString(paramIndex++, maTau);
            if (tuNgay != null) stmt.setTimestamp(paramIndex++, Timestamp.valueOf(tuNgay));
            if (denNgay != null) stmt.setTimestamp(paramIndex++, Timestamp.valueOf(denNgay));
            if (trangThai != null) stmt.setString(paramIndex++, trangThai.getTenTrangThai());
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                LichTrinh lt = mapResultSetToLichTrinh(rs);
                if (lt != null) danhSach.add(lt);
            }
            
            System.out.println("Tìm được " + danhSach.size() + " lịch trình");
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm kiếm lịch trình: " + e.getMessage());
            e.printStackTrace();
        }
        
        return danhSach;
    }

    /**
     * Map ResultSet sang LichTrinh
     */
    private LichTrinh mapResultSetToLichTrinh(ResultSet rs) throws SQLException {
        // Map Ga đi
        Ga gaDi = new Ga(rs.getInt("maGaDi"), rs.getString("viTriGaDi"), rs.getInt("mocKmDi"));
        
        // Map Ga đến
        Ga gaDen = new Ga(rs.getInt("maGaDen"), rs.getString("viTriGaDen"), rs.getInt("mocKmDen"));
        
        // Map TuyenDuong
        TuyenDuong tuyenDuong = new TuyenDuong(gaDi, gaDen, rs.getInt("thoiGianDuKien"));
        tuyenDuong.setMaTuyen(String.valueOf(rs.getInt("maTuyen")));
        tuyenDuong.setGiaCoBan(rs.getFloat("giaCoBan"));
        tuyenDuong.setTrangThai(TrangThai.valueOf(rs.getString("trangThaiTuyen")));
        
        // Map Tau
        Tau tau = new Tau(rs.getString("maTau"));
        
        // Parse datetime
        Date ngayKhoiHanh = rs.getDate("ngayKhoiHanh");
        Time gioKhoiHanh = rs.getTime("gioKhoiHanh");
        LocalDateTime dateTime = LocalDateTime.of(
            ngayKhoiHanh.toLocalDate(),
            gioKhoiHanh.toLocalTime()
        );
        
        // Tạo LichTrinh
        LichTrinh lichTrinh = new LichTrinh(tuyenDuong, tau, dateTime);
        lichTrinh.setMaLichTrinh(rs.getString("maLichTrinh"));
        lichTrinh.setGiaCoBan(rs.getFloat("giaCoBan"));
        lichTrinh.setTrangThai(TrangThai.valueOf(rs.getString("trangThaiLichTrinh")));
        
        // Tính ngày giờ đến
        LocalDateTime ngayGioDen = dateTime.plusHours(tuyenDuong.getThoiGianDuKien());
        lichTrinh.setNgayGioDen(ngayGioDen);
        
        return lichTrinh;
    }
    
    /**
     * Get seat information for schedule (total seats and available seats)
     * Query from Tau_Toa, Cho and ChiTietLichTrinh tables
     * @param maLichTrinh Schedule ID
     * @return int[2] - [0]: total seats, [1]: available seats
     */
    public int[] laySoGheCuaLichTrinh(String maLichTrinh) {
        String sql = 
            "SELECT " +
            "    COUNT(DISTINCT C.maCho) AS tongCho, " +
            "    COUNT(DISTINCT C.maCho) - ISNULL(COUNT(DISTINCT CTLT.maChiTietLichTrinh), 0) AS conTrong " +
            "FROM LichTrinh L " +
            "INNER JOIN Tau_Toa TT ON L.maTau = TT.maTau " +
            "INNER JOIN Cho C ON TT.maToa = C.maToa " +
            "LEFT JOIN ChiTietLichTrinh CTLT ON C.maCho = CTLT.maChoNgoi AND L.maLichTrinh = CTLT.maLichTrinh " +
            "WHERE L.maLichTrinh = ? " +
            "GROUP BY L.maLichTrinh";
        
        int[] result = new int[2]; // [0] = total seats, [1] = available seats
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, maLichTrinh);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                result[0] = rs.getInt("tongCho");      // Total seats
                result[1] = rs.getInt("conTrong");     // Available seats
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting seat count for schedule " + maLichTrinh + ": " + e.getMessage());
            e.printStackTrace();
            result[0] = 0;
            result[1] = 0;
        }
        
        return result;
    }
    
    /**
     * Get available seats for schedule (helper method)
     * @param maLichTrinh Schedule ID
     * @return Available seats count
     */
    public int laySoGheTrong(String maLichTrinh) {
        int[] soGhe = laySoGheCuaLichTrinh(maLichTrinh);
        return soGhe[1]; // Return available seats
    }
    
    /**
     * Get total seats for schedule (helper method)
     * @param maLichTrinh Schedule ID
     * @return Total seats count
     */
    public int layTongSoGhe(String maLichTrinh) {
        int[] soGhe = laySoGheCuaLichTrinh(maLichTrinh);
        return soGhe[0]; // Return total seats
    }
    
    // Hàm tìm lịch trình theo ga đi, ga đến
    public List<LichTrinh> timLichTrinhTheoGa(int maGaDi, int maGaDen) {
        List<LichTrinh> danhSach = new ArrayList<>();

        String sql = """
            SELECT 
                L.maLichTrinh, L.maTau, L.maTuyenDuong, L.ngayKhoiHanh, L.gioKhoiHanh, 
                L.trangThai AS trangThaiLichTrinh,
                T.maTuyen, T.thoiGianDuKien, T.giaCoBan, T.trangThai AS trangThaiTuyen,
                G1.maGa AS maGaDi, G1.viTriGa AS viTriGaDi, G1.mocKm AS mocKmDi,
                G2.maGa AS maGaDen, G2.viTriGa AS viTriGaDen, G2.mocKm AS mocKmDen
            FROM LichTrinh L
            INNER JOIN TuyenDuong T ON L.maTuyenDuong = T.maTuyen
            INNER JOIN Ga G1 ON T.diemDi = G1.maGa
            INNER JOIN Ga G2 ON T.diemDen = G2.maGa
            WHERE G1.maGa = ? AND G2.maGa = ?
            ORDER BY L.ngayKhoiHanh ASC, L.gioKhoiHanh ASC
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, maGaDi);
            stmt.setInt(2, maGaDen);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                LichTrinh lt = mapResultSetToLichTrinh(rs);
                if (lt != null) danhSach.add(lt);
            }

            System.out.println("Tìm thấy " + danhSach.size() + " lịch trình giữa hai ga.");
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm lịch trình theo ga: " + e.getMessage());
            e.printStackTrace();
        }

        return danhSach;
    }
}
