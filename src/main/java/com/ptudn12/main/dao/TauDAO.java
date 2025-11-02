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

	public List<Tau> layTatCaTau() {
		List<Tau> danhSach = new ArrayList<>();
		String sql = "WITH CarriageCounts AS ( SELECT ctt.maTau, t.loaiToa, COUNT(DISTINCT ctt.maToa) AS SoLuongToa FROM ChiTietToa ctt JOIN Toa t ON ctt.maToa = t.maToa GROUP BY ctt.maTau, t.loaiToa ), ToaStructure AS ( SELECT maTau, STRING_AGG(CAST(SoLuongToa AS NVARCHAR(MAX)) + 'x ' + loaiToa, ', ') WITHIN GROUP (ORDER BY loaiToa) AS CauTruc FROM CarriageCounts GROUP BY maTau )  SELECT t.maTau, t.trangThai, ISNULL(COUNT(DISTINCT ctt.maToa), 0) AS SoToa, ISNULL(COUNT(ctt.maCho), 0) AS TongChoNgoi, ISNULL(ts.CauTruc, N'Chưa có cấu hình') AS CauTrucTau FROM Tau t LEFT JOIN ChiTietToa ctt ON t.maTau = ctt.maTau LEFT JOIN ToaStructure ts ON t.maTau = ts.maTau GROUP BY t.maTau, t.trangThai, ts.CauTruc ORDER BY t.maTau;";

		try (Connection conn = DatabaseConnection.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next()) {
				String maTau = rs.getString("maTau");
				String trangThai = rs.getString("trangThai");
				int soToa = rs.getInt("SoToa");
				int tongChoNgoi = rs.getInt("TongChoNgoi");
				String cauTrucTau = rs.getString("CauTrucTau");

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

	public boolean themToaMoi(String tenToa, LoaiToa loaiToa) {
		String sqlThemToa = "INSERT INTO Toa (tenToa, loaiToa) VALUES (?, ?)";
		Connection conn = null;
		PreparedStatement stmtThemToa = null;
		CallableStatement stmtTaoCho = null;
		ResultSet generatedKeys = null;

		try {
			conn = DatabaseConnection.getConnection();
			conn.setAutoCommit(false);

			stmtThemToa = conn.prepareStatement(sqlThemToa, Statement.RETURN_GENERATED_KEYS);
			stmtThemToa.setString(1, tenToa);
			stmtThemToa.setString(2, loaiToa.getDescription());

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

			stmtTaoCho = conn.prepareCall("{CALL sp_TaoChoChoToa(?, ?, ?, ?, ?, ?)}");
			stmtTaoCho.setInt(1, maToaMoi);

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

			conn.commit();
			return true;

		} catch (SQLException e) {
			System.err.println("Lỗi khi thêm toa mới: " + e.getMessage());
			e.printStackTrace();
			if (conn != null) {
				try {
					conn.rollback();
				} catch (SQLException ex) {
					ex.printStackTrace();
				}
			}
			return false;
		} finally {
			try {
				if (generatedKeys != null)
					generatedKeys.close();
			} catch (SQLException e) {
			}
			try {
				if (stmtThemToa != null)
					stmtThemToa.close();
			} catch (SQLException e) {
			}
			try {
				if (stmtTaoCho != null)
					stmtTaoCho.close();
			} catch (SQLException e) {
			}
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
			}
		}
	}

	private LoaiToa mapStringToLoaiToa(String loaiToaDB) {
		for (LoaiToa loai : LoaiToa.values()) {
			if (loai.getDescription().equalsIgnoreCase(loaiToaDB)) {
				return loai;
			}
		}
		throw new IllegalArgumentException("Không tìm thấy LoaiToa tương ứng cho: " + loaiToaDB);
	}
	
	
	/**
	 * Lấy tất cả tàu (alias cho layTatCaTau)
	 * Method này để tương thích với GenerateSchedulesDialogController
	 */
	public List<Tau> getAllTau() {
		return layTatCaTau();
	}
	
	/**
	 * Tìm tàu theo mã tàu
	 */
	public Tau findById(String maTau) {
		String sql = "SELECT t.maTau, t.trangThai, " +
					"ISNULL(COUNT(DISTINCT ctt.maToa), 0) AS SoToa, " +
					"ISNULL(COUNT(ctt.maCho), 0) AS TongChoNgoi " +
					"FROM Tau t " +
					"LEFT JOIN ChiTietToa ctt ON t.maTau = ctt.maTau " +
					"WHERE t.maTau = ? " +
					"GROUP BY t.maTau, t.trangThai";
		
		try (Connection conn = DatabaseConnection.getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql)) {
			
			stmt.setString(1, maTau);
			ResultSet rs = stmt.executeQuery();
			
			if (rs.next()) {
				Tau tau = new Tau(rs.getString("maTau"));
				tau.setTrangThai(rs.getString("trangThai"));
				tau.setSoToa(rs.getInt("SoToa"));
				tau.setTongChoNgoi(rs.getInt("TongChoNgoi"));
				return tau;
			}
			
		} catch (SQLException e) {
			System.err.println("Lỗi khi tìm tàu: " + e.getMessage());
			e.printStackTrace();
		}
		
		return null;
	}
}