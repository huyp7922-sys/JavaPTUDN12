
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ptudn12.main.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author fo3cp
 */

import com.ptudn12.main.database.DatabaseConnection;
import com.ptudn12.main.entity.VeDaMua;

public class VeTauDAO {
	public boolean createVeTau(String maVe, int khachHangId, int chiTietLichTrinhId, String loaiVe, boolean khuHoi,
			String trangThai) {
		String sql = "INSERT INTO VeTau (maVe, khachHangId, chiTietLichTrinhId, loaiVe, khuHoi, trangThai, maQR) VALUES (?, ?, ?, ?, ?, ?, ?)";
		String maQR = "QR_" + maVe + "_" + System.currentTimeMillis(); // Mã QR tạm thời

		try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

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
	 * Tạo mã vé duy nhất (YYYYMMDD + 4 số).
	 * 
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

	/**
	 * Lấy lịch sử mua vé của một khách hàng bằng cách gọi Stored Procedure.
	 * 
	 * @param maKhachHang ID (dạng số) của khách hàng.
	 * @return Một danh sách các đối tượng VeDaMua.
	 */
	public List<VeDaMua> layLichSuMuaVeTheoKhachHang(int maKhachHang) {
		List<VeDaMua> danhSach = new ArrayList<>();
		String sql = "{call sp_XemVeKhachHang(?)}";

		try (Connection conn = DatabaseConnection.getConnection(); CallableStatement stmt = conn.prepareCall(sql)) {

			stmt.setInt(1, maKhachHang);
			ResultSet rs = stmt.executeQuery();

			int sttCounter = 1;
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
			Locale vietnameseLocale = new Locale("vi", "VN");
			NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(vietnameseLocale);

			while (rs.next()) {
				// --- 1. TÍNH TOÁN THỜI GIAN ĐI - ĐẾN (PHIÊN BẢN CHUẨN) ---

				// Lấy đúng kiểu java.sql.Date và java.sql.Time
				Date ngayKhoiHanhSQL = rs.getDate("ngayKhoiHanh");
				Time gioKhoiHanhSQL = rs.getTime("gioKhoiHanh");
				int thoiGianDuKienGio = rs.getInt("thoiGianDuKien");

				// Chuyển đổi sang các kiểu java.time một cách an toàn, không bị ảnh hưởng bởi
				// múi giờ
				LocalDateTime thoiGianDi = ngayKhoiHanhSQL.toLocalDate().atTime(gioKhoiHanhSQL.toLocalTime());

				LocalDateTime thoiGianDen = thoiGianDi.plusHours(thoiGianDuKienGio);

				String thoiGianDiDenFormatted = thoiGianDi.format(formatter) + " - " + thoiGianDen.format(formatter);

				// --- CÁC PHẦN CÒN LẠI GIỮ NGUYÊN ---
				String loaiCho = rs.getString("loaiCho");
				String macTau = rs.getString("maTau");
				Date ngayMuaSQL = rs.getDate("NgayMua");
				String ngayMuaFormatted = new SimpleDateFormat("dd/MM/yyyy").format(ngayMuaSQL);
				String hanhTrinh = rs.getString("DiemDi") + " - " + rs.getString("DiemDen");
				int soCho = rs.getInt("ViTriCho");
				double thanhTien = rs.getDouble("thanhTien");

				VeDaMua ve = new VeDaMua(sttCounter++, ngayMuaFormatted, rs.getString("maVe"), macTau, hanhTrinh,
						thoiGianDiDenFormatted, loaiCho, soCho, currencyFormatter.format(thanhTien));
				danhSach.add(ve);
			}

		} catch (SQLException e) {
			System.err.println("Lỗi khi lấy lịch sử mua vé cho khách hàng " + maKhachHang + ": " + e.getMessage());
			e.printStackTrace();
		}

		return danhSach;
	}
}
