package com.ptudn12.main.utils;

import com.ptudn12.main.database.DatabaseConnection;
import com.ptudn12.main.dao.VeTauDAO;
import com.ptudn12.main.entity.VeTau;
import com.ptudn12.main.controller.VeTamThoi;

import java.sql.*;

public class DoiVeService {

    public String thucHienDoiVe(VeTau veCu, VeTamThoi veMoi, String maNhanVien, double tongTienThu) {
        Connection conn = null;
        String maVeMoi = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // --- BẮT ĐẦU TRANSACTION ---

            // 1. Cập nhật vé cũ -> 'DaDoi'
            String sqlUpdateVeCu = "UPDATE VeTau SET trangThai = 'DaHuy' WHERE maVe = ?";
            try (PreparedStatement ps1 = conn.prepareStatement(sqlUpdateVeCu)) {
                ps1.setString(1, veCu.getMaVe());
                ps1.executeUpdate();
            }
            
            // 2. Trả chỗ cũ -> 'ConTrong'
            // Lấy ID chi tiết lịch trình cũ từ veCu (đảm bảo object veCu đã load đầy đủ)
            int idChiTietCu = -1;
            if (veCu.getChiTietLichTrinh() != null) {
                try {
                    idChiTietCu = Integer.parseInt(veCu.getChiTietLichTrinh().getMaChiTietLichTrinh());
                } catch (NumberFormatException e) {
                    System.err.println("Lỗi: Mã chi tiết lịch trình không phải là số hợp lệ.");
                    e.printStackTrace();
                }
            } else {
                // Fallback: Gọi DAO lấy ID nếu object chưa load
                idChiTietCu = new VeTauDAO().getChiTietLichTrinhIdByMaVe(veCu.getMaVe());
            }

            if (idChiTietCu != -1) {
                String sqlFreeSeat = "UPDATE ChiTietLichTrinh SET trangThai = N'ConTrong' WHERE maChiTietLichTrinh = ?";
                try (PreparedStatement ps2 = conn.prepareStatement(sqlFreeSeat)) {
                    ps2.setInt(1, idChiTietCu);
                    ps2.executeUpdate();
                }
            }

            // 3. Tạo Hóa Đơn Đổi Vé
            // Tạo mã HD unique
            String maHoaDon = "HDDV" + System.currentTimeMillis(); 
            String sqlHoaDon = "INSERT INTO HoaDon (maHoaDon, ngayLap, tongTienHoaDon, loaiHoaDon, nhanVienId, khachHangId) VALUES (?, GETDATE(), ?, N'DoiVe', ?, ?)";
            try (PreparedStatement ps3 = conn.prepareStatement(sqlHoaDon)) {
                ps3.setString(1, maHoaDon);
                ps3.setDouble(2, tongTienThu);
                ps3.setString(3, maNhanVien);
                // Parse MaKhachHang (String -> Int)
                int khachHangId = Integer.parseInt(veCu.getKhachHang().getMaKhachHang());
                ps3.setInt(4, khachHangId);
                ps3.executeUpdate();
            }

            // 4. Tạo Chi Tiết Lịch Trình MỚI (Đặt chỗ mới - Trạng thái 'DaBan')
            String sqlInsertCTLT = "INSERT INTO ChiTietLichTrinh (maLichTrinh, maChoNgoi, giaChoNgoi, trangThai) VALUES (?, ?, ?, N'DaBan')";
            int idChiTietMoi = -1;
            
            try (PreparedStatement ps4 = conn.prepareStatement(sqlInsertCTLT, Statement.RETURN_GENERATED_KEYS)) {
                ps4.setString(1, veMoi.getLichTrinh().getMaLichTrinh());
                ps4.setInt(2, veMoi.getChiTietToa().getCho().getMaCho());
                ps4.setDouble(3, veMoi.getGiaVe() - 2000); 
                ps4.executeUpdate();
                
                try (ResultSet rsKeys = ps4.getGeneratedKeys()) {
                    if (rsKeys.next()) idChiTietMoi = rsKeys.getInt(1);
                }
            }

            if (idChiTietMoi != -1) {
                // 5. Tạo Vé Tàu MỚI
                // Tạo mã vé unique (Dùng hàm của DAO hoặc tự sinh tạm thời)
                maVeMoi = new VeTauDAO().generateUniqueVeId(); // Nên dùng DAO để đảm bảo format
                if (maVeMoi == null) maVeMoi = "VE" + System.currentTimeMillis();

                String sqlInsertVe = "INSERT INTO VeTau (maVe, ngayDat, trangThai, khachHangId, chiTietLichTrinhId, loaiVe, khuHoi) VALUES (?, GETDATE(), N'DaBan', ?, ?, ?, ?)";
                try (PreparedStatement ps5 = conn.prepareStatement(sqlInsertVe)) {
                    ps5.setString(1, maVeMoi);
                    ps5.setInt(2, Integer.parseInt(veCu.getKhachHang().getMaKhachHang()));
                    ps5.setInt(3, idChiTietMoi);
                    ps5.setString(4, "DaDoi");
                    ps5.setBoolean(5, false);
                    ps5.executeUpdate();
                }

                // 6. Tạo Chi Tiết Hóa Đơn (Ghi nhận tiền vé mới)
                String sqlCTHD = "INSERT INTO ChiTietHoaDon (maHoaDon, maVe, soLuong, donGia, giaGiam, thanhTien) VALUES (?, ?, 1, ?, 0, ?)";
                try (PreparedStatement ps6 = conn.prepareStatement(sqlCTHD)) {
                    ps6.setString(1, maHoaDon);
                    ps6.setString(2, maVeMoi);
                    ps6.setDouble(3, veMoi.getGiaVe());
                    ps6.setDouble(4, veMoi.getGiaVe());
                    ps6.executeUpdate();
                }
                
                conn.commit(); // --- XÁC NHẬN THÀNH CÔNG ---
                return maVeMoi;
            } else {
                conn.rollback();
                return null;
            }

        } catch (Exception e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) {}
            e.printStackTrace();
            return null;
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) {}
        }
    }
}