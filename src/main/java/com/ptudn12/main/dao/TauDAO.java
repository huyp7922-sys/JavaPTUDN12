package com.ptudn12.main.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.ptudn12.main.database.DatabaseConnection;
import com.ptudn12.main.entity.Tau;
import com.ptudn12.main.enums.LoaiToa;

public class TauDAO {

	/**
	 * ✅ THAY THẾ HOÀN TOÀN HÀM NÀY Lấy tất cả các tàu cùng với thông tin chi tiết
	 * (số toa, tổng chỗ, cấu trúc) được tính toán sẵn từ CSDL.
	 * 
	 * @return Danh sách các đối tượng Tau đã có đầy đủ thông tin.
	 */
	public List<Tau> layTatCaTau() {
		List<Tau> danhSach = new ArrayList<>();
		// Câu query này sử dụng Common Table Expression (CTE) để tính toán trước
		// chuỗi cấu trúc, sau đó LEFT JOIN để đảm bảo các tàu không có toa vẫn hiện ra.
//		String sql = "WITH ToaCounts AS ( SELECT ctt.maTau, t.loaiToa, COUNT(*) AS SoLuong FROM ChiTietToa ctt JOIN Toa t ON ctt.maToa = t.maToa GROUP BY ctt.maTau, t.loaiToa ), ToaStructure AS (  SELECT maTau, STRING_AGG(CAST(SoLuong AS NVARCHAR(MAX)) + 'x ' + loaiToa, ', ') WITHIN GROUP (ORDER BY loaiToa) AS CauTruc FROM ToaCounts GROUP BY maTau )  SELECT t.maTau, t.trangThai, ISNULL(COUNT(DISTINCT ctt.maToa), 0) AS SoToa, ISNULL(COUNT(ctt.maCho), 0) AS TongChoNgoi, ISNULL(ts.CauTruc, N'Chưa có cấu hình') AS CauTrucTau FROM Tau t LEFT JOIN ChiTietToa ctt ON t.maTau = ctt.maTau LEFT JOIN ToaStructure ts ON t.maTau = ts.maTau GROUP BY t.maTau, t.trangThai, ts.CauTruc ORDER BY t.maTau;";
		String sql = "WITH CarriageCounts AS ( SELECT ctt.maTau, t.loaiToa, COUNT(DISTINCT ctt.maToa) AS SoLuongToa FROM ChiTietToa ctt JOIN Toa t ON ctt.maToa = t.maToa GROUP BY ctt.maTau, t.loaiToa ), ToaStructure AS ( SELECT maTau, STRING_AGG(CAST(SoLuongToa AS NVARCHAR(MAX)) + 'x ' + loaiToa, ', ') WITHIN GROUP (ORDER BY loaiToa) AS CauTruc FROM CarriageCounts GROUP BY maTau )  SELECT t.maTau, t.trangThai, ISNULL(COUNT(DISTINCT ctt.maToa), 0) AS SoToa, ISNULL(COUNT(ctt.maCho), 0) AS TongChoNgoi, ISNULL(ts.CauTruc, N'Chưa có cấu hình') AS CauTrucTau FROM Tau t LEFT JOIN ChiTietToa ctt ON t.maTau = ctt.maTau LEFT JOIN ToaStructure ts ON t.maTau = ts.maTau GROUP BY t.maTau, t.trangThai, ts.CauTruc ORDER BY t.maTau;";

		try (Connection conn = DatabaseConnection.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next()) {
				// Đọc dữ liệu từ các cột đã được tính toán
				String maTau = rs.getString("maTau");
				String trangThai = rs.getString("trangThai");
				int soToa = rs.getInt("SoToa");
				int tongChoNgoi = rs.getInt("TongChoNgoi");
				String cauTrucTau = rs.getString("CauTrucTau");

				// Tạo đối tượng Tau và set dữ liệu
				Tau tau = new Tau(maTau);
				tau.setTrangThai(trangThai);
				tau.setSoToa(soToa);
				tau.setTongChoNgoi(tongChoNgoi);
				tau.setCauTrucTau(cauTrucTau);

				danhSach.add(tau);
			}
		} catch (SQLException e) {
			System.err.println("Lỗi khi lấy danh sách tàu chi tiết: " + e.getMessage());
			e.printStackTrace();
		}
		return danhSach;
	}

	/**
	 * Lấy cấu trúc chi tiết của tất cả các tàu (danh sách các toa cho mỗi tàu).
	 * 
	 * @return Một Map với key là mã tàu và value là danh sách các Toa của tàu đó.
	 */
//	public Map<String, List<Toa>> layCauTrucTatCaTau() {
//		Map<String, List<Toa>> cauTrucTau = new HashMap<>();
//		String sql = "SELECT ct.maTau, t.maToa, t.tenToa, t.loaiToa "
//				+ "FROM ChiTietToa ct JOIN Toa t ON ct.maToa = t.maToa";
//
//		try (Connection conn = DatabaseConnection.getConnection();
//				Statement stmt = conn.createStatement();
//				ResultSet rs = stmt.executeQuery(sql)) {
//
//			while (rs.next()) {
//				String maTau = rs.getString("maTau");
//
//				// Chuyển đổi chuỗi loaiToa từ DB về enum LoaiToa
//				String loaiToaDB = rs.getString("loaiToa");
//				LoaiToa loaiToaEnum = mapStringToLoaiToa(loaiToaDB);
//
//				Toa toa = new Toa(loaiToaEnum);
//				toa.setMaToa(Integer.parseInt(String.valueOf(rs.getInt("maToa"))));
//				// Có thể set thêm tenToa nếu cần
//
//				// Thêm toa vào danh sách của tàu tương ứng
//				cauTrucTau.computeIfAbsent(maTau, k -> new ArrayList<>()).add(toa);
//			}
//		} catch (SQLException e) {
//			System.err.println("Lỗi khi lấy cấu trúc tàu: " + e.getMessage());
//			e.printStackTrace();
//		}
//		return cauTrucTau;
//	}

	/**
	 * Thêm một toa mới vào bảng Toa và tự động tạo các Cho tương ứng.
	 * 
	 * @param tenToa  Tên định danh duy nhất cho toa mới (ví dụ: "NM06").
	 * @param loaiToa Enum LoaiToa để xác định loại toa.
	 * @return true nếu thêm thành công, false nếu thất bại.
	 */
	public boolean themToaMoi(String tenToa, LoaiToa loaiToa) {
		String sqlThemToa = "INSERT INTO Toa (tenToa, loaiToa) VALUES (?, ?)";
		Connection conn = null;
		PreparedStatement stmtThemToa = null;
		CallableStatement stmtTaoCho = null;
		ResultSet generatedKeys = null;

		try {
			conn = DatabaseConnection.getConnection();
			conn.setAutoCommit(false); // Bắt đầu transaction

			// Bước 1: Thêm toa vào bảng Toa và lấy maToa vừa được tạo
			stmtThemToa = conn.prepareStatement(sqlThemToa, Statement.RETURN_GENERATED_KEYS);
			stmtThemToa.setString(1, tenToa);
			stmtThemToa.setString(2, loaiToa.getDescription()); // Dùng tên từ Enum

			int affectedRows = stmtThemToa.executeUpdate();
			if (affectedRows == 0) {
				throw new SQLException("Thêm toa thất bại, không có hàng nào được thêm.");
			}

			generatedKeys = stmtThemToa.getGeneratedKeys();
			int maToaMoi;
			if (generatedKeys.next()) {
				maToaMoi = generatedKeys.getInt(1);
			} else {
				throw new SQLException("Thêm toa thất bại, không lấy được ID.");
			}

			// Bước 2: Gọi stored procedure để tạo các Chỗ cho toa mới
			stmtTaoCho = conn.prepareCall("{CALL sp_TaoChoChoToa(?, ?, ?, ?, ?, ?)}");
			stmtTaoCho.setInt(1, maToaMoi);

			// Set số lượng chỗ dựa vào loại toa
			stmtTaoCho.setInt(2, (loaiToa == LoaiToa.NGOI_CUNG) ? loaiToa.getSoChoMacDinh(LoaiToa.NGOI_CUNG) : 0);
			stmtTaoCho.setInt(3, (loaiToa == LoaiToa.NGOI_MEM) ? loaiToa.getSoChoMacDinh(LoaiToa.NGOI_MEM) : 0);
			stmtTaoCho.setInt(4,
					(loaiToa == LoaiToa.GIUONG_NAM_KHOANG_6) ? loaiToa.getSoChoMacDinh(LoaiToa.GIUONG_NAM_KHOANG_6)
							: 0);
			stmtTaoCho.setInt(5,
					(loaiToa == LoaiToa.GIUONG_NAM_KHOANG_4) ? loaiToa.getSoChoMacDinh(LoaiToa.GIUONG_NAM_KHOANG_4)
							: 0);
			stmtTaoCho.setInt(6,
					(loaiToa == LoaiToa.GIUONG_NAM_VIP) ? loaiToa.getSoChoMacDinh(LoaiToa.GIUONG_NAM_VIP) : 0);

			stmtTaoCho.execute();

			conn.commit(); // Hoàn tất transaction
			return true;

		} catch (SQLException e) {
			System.err.println("Lỗi khi thêm toa mới: " + e.getMessage());
			e.printStackTrace();
			if (conn != null) {
				try {
					conn.rollback(); // Hoàn tác nếu có lỗi
				} catch (SQLException ex) {
					ex.printStackTrace();
				}
			}
			return false;
		} finally {
			// Đóng các tài nguyên
			try {
				if (generatedKeys != null)
					generatedKeys.close();
			} catch (SQLException e) {
				/* ignored */ }
			try {
				if (stmtThemToa != null)
					stmtThemToa.close();
			} catch (SQLException e) {
				/* ignored */ }
			try {
				if (stmtTaoCho != null)
					stmtTaoCho.close();
			} catch (SQLException e) {
				/* ignored */ }
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				/* ignored */ }
		}
	}

	/**
	 * Phương thức hỗ trợ để chuyển đổi chuỗi từ DB sang Enum LoaiToa.
	 */
	private LoaiToa mapStringToLoaiToa(String loaiToaDB) {
		for (LoaiToa loai : LoaiToa.values()) {
			if (loai.getDescription().equalsIgnoreCase(loaiToaDB)) {
				return loai;
			}
		}
		// Trả về một giá trị mặc định hoặc ném exception nếu không tìm thấy
		throw new IllegalArgumentException("Không tìm thấy LoaiToa tương ứng cho: " + loaiToaDB);
	}
}