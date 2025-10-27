// File: src/main/java/com/ptudn12/main/dao/KhachHangDAO.java
package com.ptudn12.main.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.ptudn12.main.database.DatabaseConnection;
import com.ptudn12.main.entity.KhachHang;

public class KhachHangDAO {

	/**
	 * Lấy tất cả khách hàng từ cơ sở dữ liệu để hiển thị lên bảng.
	 * 
	 * @return Danh sách các đối tượng KhachHang.
	 */
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

	/**
	 * Kiểm tra xem CCCD hoặc Hộ chiếu đã tồn tại trong DB hay chưa, có thể loại trừ
	 * một mã khách hàng cụ thể (dùng khi cập nhật).
	 * 
	 * @param cccd               Số CCCD cần kiểm tra.
	 * @param hoChieu            Số Hộ chiếu cần kiểm tra.
	 * @param maKhachHangHienTai ID (dạng số) của khách hàng đang sửa để loại trừ,
	 *                           hoặc null nếu đang thêm mới.
	 * @return true nếu tìm thấy trùng lặp, false nếu không.
	 */
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

		return false; // Mặc định là không trùng nếu có lỗi
	}

	/**
	 * Thêm một khách hàng mới vào cơ sở dữ liệu.
	 * 
	 * @param khachHang Đối tượng KhachHang chứa thông tin cần thêm.
	 * @return true nếu thêm thành công, false nếu thất bại.
	 */
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

	/**
	 * Cập nhật thông tin của một khách hàng đã có.
	 * 
	 * @param khachHang Đối tượng KhachHang chứa thông tin cần cập nhật.
	 * @return true nếu cập nhật thành công, false nếu thất bại.
	 */
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