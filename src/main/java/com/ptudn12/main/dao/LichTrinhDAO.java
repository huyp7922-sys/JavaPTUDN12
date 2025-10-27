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
import java.util.Map;

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

     * Lấy thông tin tổng số chỗ và chỗ đã bán của tàu trong lịch trình
     * Sử dụng stored procedure sp_DemTongSoChoVaChoDaBan
     * @param maLichTrinh Mã lịch trình
     * @return int[4] - [0]: tổng chỗ, [1]: đã bán, [2]: còn trống, [3]: -1 nếu lỗi
     */
    public int[] layThongTinChoNgoiTau(String maLichTrinh) {
        int[] result = new int[4]; // [0]=tổng, [1]=đã bán, [2]=còn trống
        result[3] = -1; // Mặc định là lỗi

        // Đầu tiên lấy mã tàu từ lịch trình
        String sqlGetTau = "SELECT maTau FROM LichTrinh WHERE maLichTrinh = ?";
        String maTau = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlGetTau)) {

            stmt.setString(1, maLichTrinh);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                maTau = rs.getString("maTau");
            } else {
                System.err.println("Không tìm thấy lịch trình: " + maLichTrinh);
                return result;
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy mã tàu: " + e.getMessage());
            e.printStackTrace();
            return result;
        }

        // Gọi stored procedure để lấy thông tin chỗ
        String sql = "{CALL sp_DemTongSoChoVaChoDaBan(?)}";

        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, maTau);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                result[0] = rs.getInt("TongSoCho");      // Tổng chỗ
                result[1] = rs.getInt("SoChoDaBan");     // Đã bán
                result[2] = rs.getInt("SoChoConTrong");  // Còn trống
                result[3] = 0; // Thành công
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi gọi sp_DemTongSoChoVaChoDaBan: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Lấy thông tin chỗ ngồi theo format "Còn trống/Tổng"
     * Ví dụ: "399/400"
     * @param maLichTrinh Mã lịch trình
     * @return Chuỗi format "Còn trống/Tổng"
     */
    public String layThongTinChoNgoiFormat(String maLichTrinh) {
        int[] info = layThongTinChoNgoiTau(maLichTrinh);

        if (info[3] == -1) {
            return "N/A";
        }

        return info[2] + "/" + info[0]; // Còn trống / Tổng
    }

    /**
     * Lấy tổng số chỗ của tàu
     * @param maLichTrinh Mã lịch trình
     * @return Tổng số chỗ
     */
    public int layTongSoCho(String maLichTrinh) {
        int[] info = layThongTinChoNgoiTau(maLichTrinh);
        return info[3] == 0 ? info[0] : 0;
    }

    /**
     * Lấy số chỗ đã bán
     * @param maLichTrinh Mã lịch trình
     * @return Số chỗ đã bán
     */
    public int layChoNgoiDaBan(String maLichTrinh) {
        int[] info = layThongTinChoNgoiTau(maLichTrinh);
        return info[3] == 0 ? info[1] : 0;
    }

    /**
     * Lấy số chỗ còn trống
     * @param maLichTrinh Mã lịch trình
     * @return Số chỗ còn trống
     */
    public int layChoNgoiConTrong(String maLichTrinh) {
        int[] info = layThongTinChoNgoiTau(maLichTrinh);
        return info[3] == 0 ? info[2] : 0;
    }

    /**
     * Lấy tỷ lệ phần trăm chỗ đã bán
     * @param maLichTrinh Mã lịch trình
     * @return Tỷ lệ phần trăm (0-100)
     */
    public double layTyLeChoNgoiDaBan(String maLichTrinh) {
        int[] info = layThongTinChoNgoiTau(maLichTrinh);

        if (info[3] == -1 || info[0] == 0) {
            return 0.0;
        }

        return (double) info[1] / info[0] * 100;
    }

    /**
     * Kiểm tra còn chỗ trống không
     * @param maLichTrinh Mã lịch trình
     * @return true nếu còn chỗ trống
     */
    public boolean conChoTrong(String maLichTrinh) {
        return layChoNgoiConTrong(maLichTrinh) > 0;
    }

    /**
     * Lấy danh sách chi tiết các chỗ đã bán của lịch trình
     * Sử dụng stored procedure sp_LietKeChoDaBan
     * @param maLichTrinh Mã lịch trình
     * @return Danh sách thông tin các chỗ đã bán
     */
    public List<Map<String, Object>> layDanhSachChoDaBan(String maLichTrinh) {
        List<Map<String, Object>> danhSach = new ArrayList<>();

        // Lấy mã tàu từ lịch trình
        String sqlGetTau = "SELECT maTau FROM LichTrinh WHERE maLichTrinh = ?";
        String maTau = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlGetTau)) {

            stmt.setString(1, maLichTrinh);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                maTau = rs.getString("maTau");
            } else {
                System.err.println("Không tìm thấy lịch trình: " + maLichTrinh);
                return danhSach;
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy mã tàu: " + e.getMessage());
            e.printStackTrace();
            return danhSach;
        }

        // Gọi stored procedure
        String sql = "{CALL sp_LietKeChoDaBan(?)}";

        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, maTau);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                // Chỉ lấy các chỗ của lịch trình hiện tại
                String lichTrinhTrongDB = rs.getString("maLichTrinh");
                if (lichTrinhTrongDB != null && lichTrinhTrongDB.equals(maLichTrinh)) {
                    Map<String, Object> row = new java.util.HashMap<>();
                    row.put("maToa", rs.getString("maToa"));
                    row.put("tenToa", rs.getString("tenToa"));
                    row.put("viTriCho", rs.getInt("ViTriCho"));
                    row.put("loaiCho", rs.getString("loaiCho"));
                    row.put("maVe", rs.getString("maVe"));
                    row.put("trangThaiVe", rs.getString("TrangThaiVe"));
                    row.put("maLichTrinh", lichTrinhTrongDB);
                    row.put("diemDi", rs.getString("DiemDi"));
                    row.put("diemDen", rs.getString("DiemDen"));
                    row.put("ngayKhoiHanh", rs.getDate("ngayKhoiHanh"));
                    row.put("gioKhoiHanh", rs.getTime("gioKhoiHanh"));

                    danhSach.add(row);
                }
            }


        } catch (SQLException e) {
            System.err.println("Lỗi khi gọi sp_LietKeChoDaBan: " + e.getMessage());
            e.printStackTrace();
        }

        return danhSach;

}
}