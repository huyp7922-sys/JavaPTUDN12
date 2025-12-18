/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ptudn12.main.utils;

import com.ptudn12.main.database.DatabaseConnection;
import com.ptudn12.main.dao.HoaDonDAO;
import com.ptudn12.main.dao.VeTauDAO;
import com.ptudn12.main.dao.KhachHangDAO;
import com.ptudn12.main.entity.KhachHang;
import com.ptudn12.main.entity.VeTau;

import java.sql.*;
import java.time.LocalDateTime;
/**
 *
 * @author fo3cp
 */
public class TraVeService {
    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();
    private final VeTauDAO veTauDAO = new VeTauDAO();
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();
    
    /**
     * Xử lý nghiệp vụ Trả vé trọn gói (Transaction):
     * 1. Update trạng thái Vé -> DaHuy
     * 2. Update chỗ ngồi -> ConTrong
     * 3. Tạo Hóa đơn hoàn tiền (Tổng tiền = 0)
     * 4. Tạo Chi tiết hóa đơn (Ghi nhận số tiền hoàn)
     */
    public boolean processTraVe(VeTau ve, double soTienHoanLai, String maNhanVien) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // --- BẮT ĐẦU TRANSACTION ---

            // ========================================================================
            // BƯỚC 1: XỬ LÝ VÉ CŨ (Hủy vé & Trả chỗ)
            // ========================================================================

            String sqlUpdateVe = "UPDATE VeTau SET trangThai = ? WHERE maVe = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlUpdateVe)) {
                ps.setString(1, "DaHuy");
                ps.setString(2, ve.getMaVe());
                ps.executeUpdate();
            }

            int idChiTiet = -1;
            try (PreparedStatement ps = conn.prepareStatement("SELECT chiTietLichTrinhId FROM VeTau WHERE maVe = ?")) {
                ps.setString(1, ve.getMaVe());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) idChiTiet = rs.getInt(1);
            }

            if (idChiTiet > 0) {
                String sqlFreeSeat = "UPDATE ChiTietLichTrinh SET trangThai = ? WHERE maChiTietLichTrinh = ?";
                try (PreparedStatement ps = conn.prepareStatement(sqlFreeSeat)) {
                    ps.setNString(1, "ConTrong");
                    ps.setInt(2, idChiTiet);
                    ps.executeUpdate();
                }
            } else {
                throw new SQLException("Không tìm thấy dữ liệu chỗ ngồi của vé: " + ve.getMaVe());
            }

            // ========================================================================
            // BƯỚC 2: TẠO HÓA ĐƠN HOÀN TIỀN
            // ========================================================================

            String maHoaDon = hoaDonDAO.generateUniqueHoaDonId();
            if (maHoaDon == null) maHoaDon = "HD" + System.currentTimeMillis();

            // >>> FIX LỖI TẠI ĐÂY: LẤY ID KHÁCH HÀNG AN TOÀN <<<
            int khachHangId = 0;
            
            // Cách 1: Lấy từ object truyền vào (nếu có)
            if (ve.getKhachHang() != null) {
                try {
                    String strId = ve.getKhachHang().getMaKhachHang(); // VD: "KH000001" hoặc "1"
                    if (strId != null) {
                        khachHangId = Integer.parseInt(strId.toUpperCase().replace("KH", ""));
                    }
                } catch (Exception e) {
                    System.err.println("Lỗi parse ID từ object: " + e.getMessage());
                }
            }

            // Cách 2: Nếu Cách 1 thất bại (ID vẫn = 0), Query DB để tìm người mua vé gốc
            if (khachHangId == 0) {
                // Hàm này sẽ tìm xem ai là người sở hữu vé này trong DB
                KhachHang kh = khachHangDAO.getNguoiMuaByMaVe(ve.getMaVe());
                if (kh == null) {
                    // Fallback: Tìm hành khách đi tàu nếu không tìm thấy người mua
                    kh = khachHangDAO.getHanhKhachByMaVe(ve.getMaVe());
                }
                
                if (kh != null) {
                    try {
                        khachHangId = Integer.parseInt(kh.getMaKhachHang().toUpperCase().replace("KH", ""));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }

            // Kiểm tra lần cuối: Nếu vẫn không có ID khách hàng -> Chặn lại, không cho Insert để tránh lỗi SQL
            if (khachHangId == 0) {
                throw new SQLException("LỖI DỮ LIỆU: Không tìm thấy thông tin Khách hàng của vé " + ve.getMaVe() + ". Không thể tạo hóa đơn hoàn tiền.");
            }
            // -----------------------------------------------------------

            String sqlHoaDon = "INSERT INTO HoaDon (maHoaDon, khachHangId, nhanVienId, ngayLap, tongTienHoaDon, loaiHoaDon) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlHoaDon)) {
                ps.setString(1, maHoaDon);
                ps.setInt(2, khachHangId); // Insert ID đã tìm được
                ps.setString(3, maNhanVien);
                ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                ps.setDouble(5, 0); 
                ps.setString(6, "HoanTien");
                ps.executeUpdate();
            }

            // 2.3. Insert Chi Tiết Hóa Đơn
            String sqlCTHD = "INSERT INTO ChiTietHoaDon (maHoaDon, maVe, giaGiam, thanhTien, BAO_HIEM, isTraVe, soTienHoanLai) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlCTHD)) {
                ps.setString(1, maHoaDon);
                ps.setString(2, ve.getMaVe());
                ps.setDouble(3, 0); 
                ps.setDouble(4, 0); 
                ps.setDouble(5, 2000); 
                ps.setBoolean(6, true); 
                ps.setDouble(7, soTienHoanLai); 
                ps.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) {}
            return false;
        } finally {
            try { if (conn != null) { conn.setAutoCommit(true); conn.close(); } } catch (SQLException ex) {}
        }
    }
}