package com.ptudn12.main.dao;

/**
 *
 * @author fo3cp
 */

import java.sql.*;
import java.util.Map;
import com.ptudn12.main.database.DatabaseConnection;
import java.util.ArrayList;
import java.util.List;
import com.ptudn12.main.entity.KhachHang;


public class KhachHangDAO {
    public int findKhachHangByIdentifier(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            return -1;
        }

        String sql;
        boolean isLikelyCCCD = identifier.matches("\\d{9}|\\d{12}"); // Kiểm tra có phải 9 hoặc 12 số

        if (isLikelyCCCD) {
            sql = "SELECT maKhachHang FROM KhachHang WHERE soCCCD = ?";
        } else {
            sql = "SELECT maKhachHang FROM KhachHang WHERE hoChieu = ?";
        }

        int khachHangId = -1;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, identifier);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                khachHangId = rs.getInt("maKhachHang");
            }
            else if (!isLikelyCCCD) {
            }


        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm khách hàng bằng định danh: " + e.getMessage());
            e.printStackTrace();
        }
        return khachHangId;
    }
    
    public int findOrInsertKhachHang(Map<String, String> thongTinNguoiMua) {
        String identifier = thongTinNguoiMua.get("soGiayToIdentifier");

        // Thử tìm kiếm trước nếu có identifier
        int existingId = -1;
        if (identifier != null && !identifier.isEmpty()) {
             existingId = findKhachHangByIdentifier(identifier);
        } else {
             System.out.println("Cảnh báo: Không có CCCD/Hộ chiếu để tìm khách hàng.");
        }

        if (existingId != -1) {
            return existingId;
        } else {
            // Thêm khách hàng mới
            return insertKhachHang(thongTinNguoiMua);
        }
    }
    
    public int insertKhachHang(Map<String, String> thongTinNguoiMua) {
        String sql = "INSERT INTO KhachHang (tenKhachHang, soCCCD, hoChieu, soDienThoai, email, diaChi) VALUES (?, ?, ?, ?, ?, ?)";
        int generatedId = -1;

        String identifier = thongTinNguoiMua.get("soGiayToIdentifier"); // Lấy giá trị giấy tờ
        String soCCCD = null;
        String hoChieu = null;

        // Phân loại giá trị giấy tờ
        if (identifier != null && !identifier.isEmpty()) {
            if (identifier.matches("\\d{9}|\\d{12}")) {
                soCCCD = identifier;
            } else {
                hoChieu = identifier;
            }
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, thongTinNguoiMua.get("tenKhachHang")); // Đổi key
            if (soCCCD != null) ps.setString(2, soCCCD); else ps.setNull(2, Types.VARCHAR);
            if (hoChieu != null) ps.setString(3, hoChieu); else ps.setNull(3, Types.VARCHAR);
            ps.setString(4, thongTinNguoiMua.get("soDienThoai")); // Đổi key
            ps.setString(5, thongTinNguoiMua.get("email"));
            ps.setString(6, thongTinNguoiMua.get("diaChi")); // Thêm địa chỉ (có thể null)

            int affectedRows = ps.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        generatedId = generatedKeys.getInt(1);
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm khách hàng: " + e.getMessage());
            if (e.getMessage().contains("CK_KhachHang_CCCD_HoChieu")) {
                 System.err.println("Lỗi constraint: Không thể thêm cả CCCD và Hộ chiếu.");
            }
            e.printStackTrace();
        }
        return generatedId;
    }

    public List<KhachHang> layTatCaKhachHang() {
        List<KhachHang> danhSach = new ArrayList<>();
        String sql = "SELECT * FROM KhachHang ORDER BY maKhachHang";

        try (Connection conn = DatabaseConnection.getConnection();
                        Statement stmt = conn.createStatement();
                        ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                        KhachHang kh = mapResultSetToKhachHang(rs);
                        danhSach.add(kh);
                }

        } catch (SQLException e) {
                System.err.println("Lỗi khi lấy danh sách khách hàng: " + e.getMessage());
                e.printStackTrace();
        }

        return danhSach;
    }

    public boolean kiemTraTrungLap(String cccd, String hoChieu, Integer maKhachHangHienTai) {
            // Query sẽ kiểm tra CCCD hoặc Hộ chiếu, và loại trừ mã KH hiện tại nếu có
            String sql = "SELECT COUNT(*) FROM KhachHang WHERE (soCCCD = ? OR hoChieu = ?) AND (? IS NULL OR maKhachHang != ?)";

            try (Connection conn = DatabaseConnection.getConnection();
                            PreparedStatement stmt = conn.prepareStatement(sql)) {

                    stmt.setString(1, cccd);
                    stmt.setString(2, hoChieu);

                    // Xử lý tham số cho việc loại trừ ID
                    if (maKhachHangHienTai != null) {
                            stmt.setInt(3, maKhachHangHienTai);
                            stmt.setInt(4, maKhachHangHienTai);
                    } else {
                            // Nếu là thêm mới, không cần loại trừ ai cả
                            stmt.setNull(3, java.sql.Types.INTEGER);
                            stmt.setNull(4, java.sql.Types.INTEGER);
                    }

                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                            return rs.getInt(1) > 0; // Nếu count > 0, tức là có trùng
                    }

            } catch (SQLException e) {
                    System.err.println("Lỗi khi kiểm tra trùng lặp khách hàng: " + e.getMessage());
                    e.printStackTrace();
            }

            return false;
    }


    public boolean themKhachHang(KhachHang khachHang) {
        String sql = "INSERT INTO KhachHang (tenKhachHang, soCCCD, hoChieu, soDienThoai) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                        PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, khachHang.getTenKhachHang());

                // Xử lý logic CCCD và Hộ chiếu
                if (khachHang.getSoCCCD() != null && !khachHang.getSoCCCD().isEmpty()) {
                        stmt.setString(2, khachHang.getSoCCCD());
                        stmt.setNull(3, java.sql.Types.VARCHAR);
                } else {
                        stmt.setNull(2, java.sql.Types.VARCHAR);
                        stmt.setString(3, khachHang.getHoChieu());
                }

                stmt.setString(4, khachHang.getSoDienThoai());

                int rowsAffected = stmt.executeUpdate();
                return rowsAffected > 0;

        } catch (SQLException e) {
                System.err.println("Lỗi khi thêm khách hàng: " + e.getMessage());
                e.printStackTrace();
                return false;
        }
    }

    public boolean capNhatKhachHang(KhachHang khachHang) {
            String sql = "UPDATE KhachHang SET tenKhachHang = ?, soCCCD = ?, hoChieu = ?, soDienThoai = ? WHERE maKhachHang = ?";

            try (Connection conn = DatabaseConnection.getConnection();
                            PreparedStatement stmt = conn.prepareStatement(sql)) {

                    stmt.setString(1, khachHang.getTenKhachHang());

                    // Xử lý logic CCCD và Hộ chiếu
                    if (khachHang.getSoCCCD() != null && !khachHang.getSoCCCD().isEmpty()) {
                            stmt.setString(2, khachHang.getSoCCCD());
                            stmt.setNull(3, java.sql.Types.VARCHAR);
                    } else {
                            stmt.setNull(2, java.sql.Types.VARCHAR);
                            stmt.setString(3, khachHang.getHoChieu());
                    }

                    stmt.setString(4, khachHang.getSoDienThoai());

                    // Chuyển đổi maKhachHang từ "KH001" về dạng số
                    int maKH = Integer.parseInt(khachHang.getMaKhachHang().substring(2));
                    stmt.setInt(5, maKH);

                    int rowsAffected = stmt.executeUpdate();
                    return rowsAffected > 0;

            } catch (SQLException | NumberFormatException e) {
                    System.err.println("Lỗi khi cập nhật khách hàng: " + e.getMessage());
                    e.printStackTrace();
                    return false;
            }
    }

    // Lưu ý: Chức năng "Xem Lịch sử mua vé" sẽ được thực hiện ở một DAO khác,
    // ví dụ VeTauDAO, bằng cách gọi stored procedure sp_XemVeKhachHang.
    // Dưới đây chỉ là phương thức hỗ trợ cho lớp này.

    /**
     * Phương thức hỗ trợ để chuyển đổi một dòng ResultSet thành đối tượng
     * KhachHang.
     * 
     * @param rs ResultSet đang trỏ đến một hàng dữ liệu.
     * @return Một đối tượng KhachHang.
     * @throws SQLException
     */
    private KhachHang mapResultSetToKhachHang(ResultSet rs) throws SQLException {
        // Lấy dữ liệu từ các cột
        int maKhachHangInt = rs.getInt("maKhachHang");
        String tenKhachHang = rs.getString("tenKhachHang");
        String soCCCD = rs.getString("soCCCD");
        String hoChieu = rs.getString("hoChieu");
        String soDienThoai = rs.getString("soDienThoai");

        // Định dạng lại mã khách hàng theo yêu cầu của GUI
        String maKHFormatted = String.format("KH%09d", maKhachHangInt);

        // Chuẩn bị dữ liệu cho constructor của Entity
        String idGiayTo;
        boolean laNguoiNuocNgoai;

        if (hoChieu != null && !hoChieu.trim().isEmpty()) {
                idGiayTo = hoChieu;
                laNguoiNuocNgoai = true;
        } else {
                idGiayTo = soCCCD;
                laNguoiNuocNgoai = false;
        }

        // Lưu ý: Cột 'diemTich' không có trong DB.
        // Cung cấp một giá trị mặc định để GUI có thể hiển thị.
        int diemTich = 0; // Giá trị mặc định

        // Tạo đối tượng KhachHang bằng constructor đã có
        return new KhachHang(maKHFormatted, tenKhachHang, idGiayTo, laNguoiNuocNgoai, soDienThoai, diemTich);
    }
}
