
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ptudn12.main.dao;

/**
 *
 * @author fo3cp
 */

import com.ptudn12.main.database.DatabaseConnection;
import com.ptudn12.main.entity.*;
import com.ptudn12.main.enums.LoaiCho;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.*;


public class VeTauDAO {
    public boolean createVeTau(String maVe, int khachHangId, int chiTietLichTrinhId, String loaiVe, boolean khuHoi, String trangThai) {
        String sql = "INSERT INTO VeTau (maVe, khachHangId, chiTietLichTrinhId, loaiVe, khuHoi, trangThai, maQR) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String maQR = "QR_" + maVe + "_" + System.currentTimeMillis(); // Mã QR tạm thời

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maVe);
            ps.setInt(2, khachHangId);
            ps.setInt(3, chiTietLichTrinhId);
            ps.setString(4, loaiVe);
            ps.setBoolean(5, khuHoi);
            ps.setString(6, trangThai);
            ps.setString(7, maQR);

            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
             System.err.println("Lỗi khi tạo vé tàu " + maVe + ": " + e.getMessage());
             e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Lấy danh sách vé đã mua của khách hàng bằng Stored Procedure
     * @param maKhachHang ID (int) của khách hàng
     */
    public List<VeTau> getLichSuVeCuaKhachHang(int maKhachHang) {
        List<VeTau> listVe = new ArrayList<>();
        // Gọi SP: sp_XemVeKhachHang @maKhachHang
        String sql = "{call sp_XemVeKhachHang(?)}";

        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {

            cs.setInt(1, maKhachHang);
            
            ResultSet rs = cs.executeQuery();
            while (rs.next()) {
                VeTau ve = new VeTau();
                ve.setMaVe(rs.getString("maVe"));
                // ve.setLoaiVe(...); // Cần convert String sang Enum nếu cần
                ve.setKhuHoi(rs.getBoolean("khuHoi"));
                ve.setTrangThai(rs.getString("trangThai"));
                
                // 1. Map Lịch Trình & Tuyến Đường & Ga & Tàu
                LichTrinh lt = new LichTrinh();
                lt.setMaLichTrinh(rs.getString("maLichTrinh"));
                
                // Ngày giờ
                java.sql.Date ngayDi = rs.getDate("ngayKhoiHanh");
                java.sql.Time gioDi = rs.getTime("gioKhoiHanh");
                if (ngayDi != null) {
                    LocalDateTime dt;
                    if (gioDi != null) {
                        dt = LocalDateTime.of(ngayDi.toLocalDate(), gioDi.toLocalTime());
                    } else {
                        dt = ngayDi.toLocalDate().atStartOfDay();
                    }
                    lt.setNgayGioKhoiHanh(dt);
                }
                
                // Tuyến
                TuyenDuong td = new TuyenDuong();
                Ga gDi = new Ga(); gDi.setViTriGa(rs.getString("DiemDi"));
                Ga gDen = new Ga(); gDen.setViTriGa(rs.getString("DiemDen"));
                td.setDiemDi(gDi);
                td.setDiemDen(gDen);
                lt.setTuyenDuong(td);
                
                // Tàu (SP trả về tenToa nhưng không trả về macTau, 
                // nhưng UI cần hiển thị Tàu. Ta tạm dùng tenToa hoặc sửa SP để lấy thêm macTau.
                // Ở đây mình new Tau giả để tránh NullPointer khi hiển thị)
                Tau tau = new Tau("Unknown"); 
                lt.setTau(tau);
                
                ve.setChiTietLichTrinh(new ChiTietLichTrinh()); // Init trước
                ve.getChiTietLichTrinh().setLichTrinh(lt);
                
                // 2. Map Chi Tiết Lịch Trình & Chỗ & Toa
                ChiTietLichTrinh ctlt = ve.getChiTietLichTrinh();
                // Set giá từ DB (Quan trọng cho việc hoàn tiền)
                ctlt.setGiaChoNgoi(rs.getDouble("giaChoNgoi")); 
                // Set ID để update trạng thái sau này (trong DB là int, entity là String)
                // Cần truy vấn thêm ID này nếu SP chưa trả về (SP hiện tại chưa trả về maChiTietLichTrinh ID)
                // Tuy nhiên, logic trả vé cần update trạng thái chỗ.
                // -> GIẢI PHÁP: Trong TraVeController, ta sẽ dùng hàm getChiTietLichTrinhIdByMaVe để lấy ID chính xác.
                
                Cho cho = new Cho();
                cho.setSoThuTu(rs.getInt("ViTriCho"));
                // Convert String db "loaiCho" -> Enum LoaiCho
                String strLoaiCho = rs.getString("loaiCho");
                // Tìm enum tương ứng (Cần hàm tìm trong Enum, ở đây mình ví dụ)
                // cho.setLoaiCho(...); 
                
                Toa t = new Toa();
                t.setTenToa(rs.getString("tenToa"));
                cho.setToa(t);
                
                ctlt.setCho(cho);
                
                listVe.add(ve);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy lịch sử vé: " + e.getMessage());
            e.printStackTrace();
        }
        return listVe;
    }

    /**
     * Cập nhật trạng thái vé (DaHuy)
     */
    public boolean updateTrangThaiVe(String maVe, String trangThaiMoi) {
        String sql = "UPDATE VeTau SET trangThai = ? WHERE maVe = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, trangThaiMoi);
            ps.setString(2, maVe);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Lấy ID Chi Tiết Lịch Trình từ Mã Vé (Cần để giải phóng chỗ)
     */
    public int getChiTietLichTrinhIdByMaVe(String maVe) {
        String sql = "SELECT chiTietLichTrinhId FROM VeTau WHERE maVe = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maVe);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("chiTietLichTrinhId");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    public VeTau getVeTauDetail(String maVe) {
        VeTau ve = null;
        
        // Query "khủng" để lấy tất cả thông tin cần thiết
        String sql = "SELECT v.maVe, v.loaiVe, v.trangThai, v.maQR, " +
                     "k.tenKhachHang, k.soCCCD, k.hoChieu, " +
                     "lt.ngayKhoiHanh, lt.gioKhoiHanh, " +
                     "t.macTau, " +
                     "gDi.viTriGa AS tenGaDi, gDen.viTriGa AS tenGaDen, " +
                     "toa.tenToa, toa.maToa, " +
                     "c.soThuTu AS soGhe, c.loaiCho, " +
                     "ctlt.giaChoNgoi " +
                     "FROM VeTau v " +
                     "JOIN KhachHang k ON v.khachHangId = k.maKhachHang " +
                     "JOIN ChiTietLichTrinh ctlt ON v.chiTietLichTrinhId = ctlt.maChiTietLichTrinh " +
                     "JOIN LichTrinh lt ON ctlt.maLichTrinh = lt.maLichTrinh " +
                     "JOIN Tau t ON lt.maTau = t.maTau " +
                     "JOIN TuyenDuong td ON lt.maTuyenDuong = td.maTuyen " +
                     "JOIN Ga gDi ON td.diemDi = gDi.maGa " +
                     "JOIN Ga gDen ON td.diemDen = gDen.maGa " +
                     "JOIN Cho c ON ctlt.maChoNgoi = c.maCho " +
                     "JOIN Toa toa ON c.maToa = toa.maToa " +
                     "WHERE v.maVe = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maVe);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                ve = new VeTau();
                ve.setMaVe(rs.getString("maVe"));
                ve.setMaQR(new QRCode(rs.getString("maQR"))); // Giả sử bạn có class QRCode wrapper
                // Hoặc nếu maQR là String trong VeTau thì: ve.setMaQR(rs.getString("maQR"));
                
                // Map Khách Hàng
                KhachHang kh = new KhachHang();
                kh.setTenKhachHang(rs.getString("tenKhachHang"));
                kh.setSoCCCD(rs.getString("soCCCD"));
                kh.setHoChieu(rs.getString("hoChieu"));
                ve.setKhachHang(kh);
                
                // Map Loại Vé (String -> Enum nếu cần, hoặc để String để hiển thị)
                // Ở đây mình set tạm vào thuộc tính loaiVe (nếu entity hỗ trợ setter String hoặc Enum)
                // ve.setLoaiVe(LoaiVe.valueOf(rs.getString("loaiVe"))); // Cẩn thận nếu DB lưu tiếng Việt có dấu
                
                // Map Lịch Trình & Tàu & Ga
                LichTrinh lt = new LichTrinh();
                
                java.sql.Date ngayDi = rs.getDate("ngayKhoiHanh");
                java.sql.Time gioDi = rs.getTime("gioKhoiHanh");
                if (ngayDi != null) {
                    LocalDateTime dt = (gioDi != null) 
                        ? LocalDateTime.of(ngayDi.toLocalDate(), gioDi.toLocalTime()) 
                        : ngayDi.toLocalDate().atStartOfDay();
                    lt.setNgayGioKhoiHanh(dt);
                }
                
                Tau tau = new Tau(rs.getString("macTau"));
                lt.setTau(tau);
                
                TuyenDuong td = new TuyenDuong();
                Ga gaDi = new Ga(); gaDi.setViTriGa(rs.getString("tenGaDi"));
                Ga gaDen = new Ga(); gaDen.setViTriGa(rs.getString("tenGaDen"));
                td.setDiemDi(gaDi);
                td.setDiemDen(gaDen);
                lt.setTuyenDuong(td);
                
                // Map Chi Tiết Lịch Trình & Chỗ & Toa & Giá
                ChiTietLichTrinh ctlt = new ChiTietLichTrinh();
                ctlt.setLichTrinh(lt);
                ctlt.setGiaChoNgoi(rs.getDouble("giaChoNgoi"));
                
                Cho cho = new Cho();
                cho.setSoThuTu(rs.getInt("soGhe"));
                
                // Map Loại Chỗ (String -> Enum)
                String loaiChoStr = rs.getString("loaiCho");
                try {
                    cho.setLoaiCho(LoaiCho.fromDescription(loaiChoStr));
                } catch (Exception e) {
                    // Fallback nếu không map được enum
                    System.err.println("Warning: Không map được loại chỗ " + loaiChoStr);
                }
                
                Toa t = new Toa();
                t.setTenToa(rs.getString("tenToa"));
                t.setMaToa(rs.getInt("maToa")); // Cần thiết cho in ấn (Toa số mấy)
                cho.setToa(t);
                
                ctlt.setCho(cho);
                ve.setChiTietLichTrinh(ctlt);
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy chi tiết vé tàu để in: " + e.getMessage());
            e.printStackTrace();
        }
        
        return ve;
    }
    
    /**
     * Tạo mã vé duy nhất (YYYYMMDD + 4 số).
     * @return Mã vé mới hoặc null nếu lỗi.
     */
    public String generateUniqueVeId() {
        LocalDate today = LocalDate.now();
        String prefix = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int nextSequence = 1;
        String sqlQuery = "SELECT MAX(CAST(SUBSTRING(maVe, 9, 4) AS INT)) FROM VeTau WHERE maVe LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement psQuery = conn.prepareStatement(sqlQuery)) {

            psQuery.setString(1, prefix + "%");
            ResultSet rs = psQuery.executeQuery();
            if (rs.next()) {
                nextSequence = rs.getInt(1) + 1;
            }
            return String.format("%s%04d", prefix, nextSequence);

        } catch (SQLException e) {
            System.err.println("Lỗi khi tạo mã vé: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
