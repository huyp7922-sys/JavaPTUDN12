package com.ptudn12.main.utils;

import com.ptudn12.main.database.DatabaseConnection;
import com.ptudn12.main.dao.VeTauDAO;
import com.ptudn12.main.dao.HoaDonDAO;
import com.ptudn12.main.entity.VeTau;
import com.ptudn12.main.controller.VeTamThoi;
import com.ptudn12.main.dao.KhachHangDAO;

import java.sql.*;

public class DoiVeService {

    private final VeTauDAO veTauDAO = new VeTauDAO();
    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();
    
    private final double PHI_BAO_HIEM = 2000;

    public String thucHienDoiVe(VeTau veCu, VeTamThoi veMoi, String maNhanVien, double tongTienThu) {
        Connection conn = null;
        String maVeMoi = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // --- BẮT ĐẦU TRANSACTION ---

            // ========================================================================
            // PHẦN 1: XỬ LÝ VÉ CŨ
            // ========================================================================

            // 1.1. Cập nhật vé cũ -> 'DaHuy' (Thay vì DaHuy để phân biệt)
            String sqlUpdateVeCu = "UPDATE VeTau SET trangThai = ? WHERE maVe = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlUpdateVeCu)) {
                ps.setString(1, "DaHuy"); 
                ps.setString(2, veCu.getMaVe());
                ps.executeUpdate();
            }

            // 1.2. Lấy ID chi tiết lịch trình cũ (Query trực tiếp trên conn để tránh lỗi)
            int idChiTietCu = -1;
            String sqlGetIdCTLT = "SELECT chiTietLichTrinhId FROM VeTau WHERE maVe = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlGetIdCTLT)) {
                ps.setString(1, veCu.getMaVe());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) idChiTietCu = rs.getInt(1);
            }

            // 1.3. Trả chỗ cũ -> 'ConTrong'
            if (idChiTietCu > 0) {
                String sqlFreeSeat = "UPDATE ChiTietLichTrinh SET trangThai = ? WHERE maChiTietLichTrinh = ?";
                try (PreparedStatement ps = conn.prepareStatement(sqlFreeSeat)) {
                    ps.setNString(1, "ConTrong");
                    ps.setInt(2, idChiTietCu);
                    ps.executeUpdate();
                }
            } else {
                throw new SQLException("Không tìm thấy thông tin chỗ ngồi của vé cũ.");
            }

            // ========================================================================
            // PHẦN 2: TẠO HÓA ĐƠN ĐỔI VÉ
            // ========================================================================
            
            // 2.1. Tự sinh mã hóa đơn (Logic giống HoaDonDAO nhưng chạy trên conn này)
            String maHoaDon = hoaDonDAO.generateUniqueHoaDonId();

            // 2.2. Insert Hóa Đơn
            String sqlHoaDon = "INSERT INTO HoaDon (maHoaDon, khachHangId, nhanVienId, ngayLap, tongTienHoaDon, loaiHoaDon) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlHoaDon)) {
                ps.setString(1, maHoaDon);
                
                // Parse ID khách hàng
                int khachHangId = 0;
                try {
                    String strId = veCu.getKhachHang().getMaKhachHang(); 
                    if (strId.toUpperCase().startsWith("KH")) {
                        khachHangId = Integer.parseInt(strId.substring(2));
                    } else {
                        khachHangId = Integer.parseInt(strId);
                    }
                } catch (Exception e) {
                    System.err.println("Lỗi parse KH ID: " + e.getMessage());
                }
                
                ps.setInt(2, khachHangId);
                ps.setString(3, maNhanVien);
                ps.setTimestamp(4, Timestamp.valueOf(java.time.LocalDateTime.now()));
                ps.setDouble(5, tongTienThu);
                ps.setString(6, "DoiVe"); // Check constraint DB phải có giá trị này
                ps.executeUpdate();
            }

            // ========================================================================
            // PHẦN 3: TẠO VÉ MỚI (Logic của processVe tái sử dụng tại đây)
            // ========================================================================

            double giaChoNgoiMoi = veMoi.getGiaVe() - PHI_BAO_HIEM;

            // 3.1. Insert Chi Tiết Lịch Trình (Đặt chỗ mới)
            String sqlInsertCTLT = "INSERT INTO ChiTietLichTrinh (maLichTrinh, maChoNgoi, giaChoNgoi, trangThai) VALUES (?, ?, ?, ?)";
            int idChiTietMoi = -1;

            try (PreparedStatement ps = conn.prepareStatement(sqlInsertCTLT, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, veMoi.getLichTrinh().getMaLichTrinh());
                ps.setInt(2, veMoi.getChiTietToa().getCho().getMaCho());
                ps.setDouble(3, giaChoNgoiMoi);
                ps.setString(4, "DaBan"); // Quan trọng: Đánh dấu đã bán
                
                if (ps.executeUpdate() > 0) {
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) idChiTietMoi = rs.getInt(1);
                    }
                }
            }

            if (idChiTietMoi != -1) {
                // 3.2. Sinh Mã Vé Mới (Chạy trên conn để tránh Deadlock)
                maVeMoi = generateVeIdOnConnection(conn);
                String maQR = "QR_" + maVeMoi + "_" + System.currentTimeMillis();

                // 3.3. Insert Vé Tàu (Giống processVe)
                String sqlInsertVe = "INSERT INTO VeTau (maVe, khachHangId, chiTietLichTrinhId, loaiVe, khuHoi, trangThai, maQR) VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sqlInsertVe)) {
                    ps.setString(1, maVeMoi);
                    
                    int khachHangId = Integer.parseInt(veCu.getKhachHang().getMaKhachHang().replace("KH", ""));
                    ps.setInt(2, khachHangId);
                    ps.setInt(3, idChiTietMoi);
                    
                    // Lấy loại vé từ vé cũ để giữ nguyên đối tượng ưu đãi
                    String moTaLoaiVe = (veCu.getLoaiVe() != null) ? veCu.getLoaiVe().getDescription() : "Vé bình thường";
                    ps.setString(4, moTaLoaiVe);
                    
                    ps.setBoolean(5, false); // Vé đổi tính là 1 chiều
                    ps.setString(6, "DaBan");
                    ps.setString(7, maQR);
                    
                    ps.executeUpdate();
                }

                // 3.4. Insert Chi Tiết Hóa Đơn (Giống processVe)
                String sqlCTHD = "INSERT INTO ChiTietHoaDon (maHoaDon, maVe, giaGiam, thanhTien, BAO_HIEM, isTraVe, soTienHoanLai) VALUES (?, ?, ?, ?, ?, 0, 0)";
                try (PreparedStatement ps = conn.prepareStatement(sqlCTHD)) {
                    // Tính lại giá giảm dựa trên loại vé cũ
                    double heSoGiam = (veCu.getLoaiVe() != null) ? veCu.getLoaiVe().getHeSoGiamGia() : 0;
                    double tienGiam = giaChoNgoiMoi * heSoGiam;
                    // Thành tiền này là giá trị thực của vé mới (để lưu sổ sách), không phải số tiền khách đóng thêm
                    double thanhTienLuuSo = veMoi.getGiaVe() - tienGiam;

                    ps.setString(1, maHoaDon);
                    ps.setString(2, maVeMoi);
                    ps.setDouble(3, tienGiam);
                    ps.setDouble(4, thanhTienLuuSo);
                    ps.setInt(5, (int)PHI_BAO_HIEM);
                    
                    ps.executeUpdate();
                }

                // --- THÀNH CÔNG HẾT MỚI COMMIT ---
                conn.commit();
                return maVeMoi;

            } else {
                conn.rollback();
                return null;
            }

        } catch (Exception e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) {}
            System.err.println("Lỗi Transaction Đổi vé: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            try { if (conn != null) { conn.setAutoCommit(true); conn.close(); } } catch (SQLException ex) {}
        }
    }


    private String generateVeIdOnConnection(Connection conn) throws SQLException {
        // Format ngày YYMMDD (6 ký tự)
        String prefix = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyMMdd"));
        int nextSequence = 1;
        
        // Cắt chuỗi từ vị trí thứ 7, lấy 6 ký tự
        String sql = "SELECT MAX(CAST(SUBSTRING(maVe, 7, 6) AS INT)) FROM VeTau WHERE maVe LIKE ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, prefix + "%");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int maxSeq = rs.getInt(1);
                if (maxSeq > 0) nextSequence = maxSeq + 1;
            }
        }
        // Trả về chuỗi 12 ký tự số
        return String.format("%s%06d", prefix, nextSequence);
    }
}