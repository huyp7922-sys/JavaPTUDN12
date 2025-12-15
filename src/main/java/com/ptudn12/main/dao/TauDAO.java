package com.ptudn12.main.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.ptudn12.main.database.DatabaseConnection;
import com.ptudn12.main.entity.Tau;
import com.ptudn12.main.entity.Toa;
import com.ptudn12.main.enums.LoaiToa;

public class TauDAO {

	public List<Tau> layTatCaTau() {
		List<Tau> danhSach = new ArrayList<>();
//		String sql = "WITH CarriageCounts AS ( SELECT ctt.maTau, t.loaiToa, COUNT(DISTINCT ctt.maToa) AS SoLuongToa FROM ChiTietToa ctt JOIN Toa t ON ctt.maToa = t.maToa GROUP BY ctt.maTau, t.loaiToa ), ToaStructure AS ( SELECT maTau, STRING_AGG(CAST(SoLuongToa AS NVARCHAR(MAX)) + 'x ' + loaiToa, ', ') WITHIN GROUP (ORDER BY loaiToa) AS CauTruc FROM CarriageCounts GROUP BY maTau )  SELECT t.maTau, t.trangThai, ISNULL(COUNT(DISTINCT ctt.maToa), 0) AS SoToa, ISNULL(COUNT(ctt.maCho), 0) AS TongChoNgoi, ISNULL(ts.CauTruc, N'Chưa có cấu hình') AS CauTrucTau FROM Tau t LEFT JOIN ChiTietToa ctt ON t.maTau = ctt.maTau LEFT JOIN ToaStructure ts ON t.maTau = ts.maTau GROUP BY t.maTau, t.trangThai, ts.CauTruc ORDER BY t.maTau;";
		String sql = "WITH CarriageCounts AS (\r\n" + "                SELECT\r\n"
				+ "                    ctt.maTau,\r\n" + "                    t.loaiToa,\r\n"
				+ "                    COUNT(DISTINCT ctt.maToa) AS SoLuongToa\r\n"
				+ "                FROM ChiTietToa ctt\r\n" + "                JOIN Toa t ON ctt.maToa = t.maToa\r\n"
				+ "                GROUP BY ctt.maTau, t.loaiToa\r\n" + "            ),\r\n"
				+ "            ToaStructure AS (\r\n" + "                SELECT\r\n" + "                    maTau,\r\n"
				+ "                    STRING_AGG(CAST(SoLuongToa AS NVARCHAR(MAX)) + 'x ' + loaiToa, ', ') \r\n"
				+ "                    WITHIN GROUP (ORDER BY loaiToa) AS CauTruc\r\n"
				+ "                FROM CarriageCounts\r\n" + "                GROUP BY maTau\r\n"
				+ "            ),\r\n" + "            TrainAggregates AS (\r\n" + "                SELECT\r\n"
				+ "                    maTau,\r\n" + "                    COUNT(DISTINCT maToa) AS SoToa,\r\n"
				+ "                    COUNT(maCho) AS TongChoNgoi\r\n" + "                FROM ChiTietToa\r\n"
				+ "                GROUP BY maTau\r\n" + "            )\r\n" + "            SELECT\r\n"
				+ "                t.maTau,\r\n" + "                t.trangThai,\r\n"
				+ "                ISNULL(ta.SoToa, 0) AS SoToa,\r\n"
				+ "                ISNULL(ta.TongChoNgoi, 0) AS TongChoNgoi,\r\n"
				+ "                ISNULL(ts.CauTruc, N'Chưa có cấu hình') AS CauTrucTau\r\n" + "            FROM \r\n"
				+ "                Tau t\r\n" + "                LEFT JOIN TrainAggregates ta ON t.maTau = ta.maTau\r\n"
				+ "                LEFT JOIN ToaStructure ts ON t.maTau = ts.maTau\r\n" + "            ORDER BY \r\n"
				+ "                t.maTau;";

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
	 * Lấy tất cả tàu (alias cho layTatCaTau) Method này để tương thích với
	 * GenerateSchedulesDialogController
	 */
	public List<Tau> getAllTau() {
		return layTatCaTau();
	}

	/**
	 * Tìm tàu theo mã tàu
	 */
	public Tau findById(String maTau) {
		String sql = "SELECT t.maTau, t.trangThai, " + "ISNULL(COUNT(DISTINCT ctt.maToa), 0) AS SoToa, "
				+ "ISNULL(COUNT(ctt.maCho), 0) AS TongChoNgoi " + "FROM Tau t "
				+ "LEFT JOIN ChiTietToa ctt ON t.maTau = ctt.maTau " + "WHERE t.maTau = ? "
				+ "GROUP BY t.maTau, t.trangThai";

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

	/**
	 * ✅ THÊM MỚI: Lấy danh sách các toa chưa được gán cho BẤT KỲ tàu nào.
	 * 
	 * @return Danh sách các đối tượng Toa khả dụng.
	 */
	public List<Toa> layTatCaToaChuaSuDung() {
		List<Toa> danhSach = new ArrayList<>();
		// Query này chọn tất cả Toa có maToa KHÔNG NẰM trong bảng ChiTietToa
		String sql = "SELECT maToa, tenToa, loaiToa				    FROM Toa				    WHERE maToa NOT IN (SELECT DISTINCT maToa FROM ChiTietToa)				    ORDER BY tenToa				";

		try (Connection conn = DatabaseConnection.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next()) {
				int maToa = rs.getInt("maToa");
				String tenToa = rs.getString("tenToa");
				String loaiToaStr = rs.getString("loaiToa");

				// Sử dụng hàm fromDescription trong Enum để chuyển đổi String -> Enum
				LoaiToa loaiToaEnum = LoaiToa.fromDescription(loaiToaStr);

				danhSach.add(new Toa(maToa, tenToa, loaiToaEnum));
			}
		} catch (SQLException e) {
			System.err.println("Lỗi khi lấy danh sách toa chưa sử dụng: " + e.getMessage());
			e.printStackTrace();
		}
		return danhSach;
	}

	/**
	 * ✅ THÊM MỚI: Lấy cấu trúc chi tiết (các toa và số thứ tự) của một tàu cụ thể.
	 * 
	 * @param maTau Mã của tàu cần lấy cấu trúc.
	 * @return Một Map với Key là số thứ tự toa, và Value là đối tượng Toa. Sử dụng
	 *         LinkedHashMap để đảm bảo thứ tự được giữ nguyên.
	 */
	public Map<Integer, Toa> layCauTrucToaCuaTau(String maTau) {
		Map<Integer, Toa> cauTruc = new LinkedHashMap<>();
		// Query này lấy thông tin toa và số thứ tự của nó trong tàu
		String sql = " SELECT ctt.soThuTuToa, t.maToa, t.tenToa, t.loaiToa    FROM ChiTietToa ctt    JOIN Toa t ON ctt.maToa = t.maToa    WHERE ctt.maTau = ?    ORDER BY ctt.soThuTuToa ASC";

		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, maTau);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				int soThuTuToa = rs.getInt("soThuTuToa");
				int maToa = rs.getInt("maToa");
				String tenToa = rs.getString("tenToa");
				LoaiToa loaiToa = LoaiToa.fromDescription(rs.getString("loaiToa"));

				cauTruc.put(soThuTuToa, new Toa(maToa, tenToa, loaiToa));
			}
		} catch (SQLException e) {
			System.err.println("Lỗi khi lấy cấu trúc toa của tàu " + maTau + ": " + e.getMessage());
			e.printStackTrace();
		}
		return cauTruc;
	}

	/**
	 * ✅ THÊM MỚI: Lập một tàu mới và gán cấu trúc toa cho nó. Thực hiện trong một
	 * transaction để đảm bảo toàn vẹn dữ liệu.
	 * 
	 * @param tau         Đối tượng Tau mới (chỉ cần macTau).
	 * @param danhSachToa Danh sách các Toa sẽ được gán vào tàu.
	 * @return true nếu tất cả thao tác thành công.
	 */
	public boolean lapTauMoi(Tau tau, List<Toa> danhSachToa) {
		Connection conn = null;
		try {
			conn = DatabaseConnection.getConnection();
			conn.setAutoCommit(false); // Bắt đầu Transaction

			// Bước 1: Thêm tàu mới vào bảng Tau
			String sqlInsertTau = "INSERT INTO Tau (maTau, trangThai) VALUES (?, ?)";
			try (PreparedStatement stmt = conn.prepareStatement(sqlInsertTau)) {
				stmt.setString(1, tau.getMacTau());
				stmt.setString(2, "SanSang"); // Trạng thái mặc định khi tạo mới
				stmt.executeUpdate();
			}

			// Bước 2: Thêm từng toa vào tàu bằng Stored Procedure
			String sqlCallSP = "{CALL sp_ThemToaVaoTau(?, ?, ?)}";
			try (CallableStatement cstmt = conn.prepareCall(sqlCallSP)) {
				for (int i = 0; i < danhSachToa.size(); i++) {
					Toa toa = danhSachToa.get(i);
					int soThuTuToa = i + 1; // Số thứ tự từ 1 đến N

					cstmt.setString(1, tau.getMacTau());
					cstmt.setInt(2, toa.getMaToa());
					cstmt.setInt(3, soThuTuToa);
					cstmt.execute();
				}
			}

			conn.commit(); // Hoàn tất Transaction thành công
			return true;

		} catch (SQLException e) {
			System.err.println("Lỗi khi lập tàu mới: " + e.getMessage());
			e.printStackTrace();
			if (conn != null) {
				try {
					conn.rollback(); // Hoàn tác tất cả thay đổi nếu có lỗi
				} catch (SQLException ex) {
					ex.printStackTrace();
				}
			}
			return false;
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * ✅ THÊM MỚI: Kiểm tra xem một mác tàu đã tồn tại trong CSDL hay chưa.
	 * 
	 * @param macTau Mác tàu cần kiểm tra.
	 * @return true nếu đã tồn tại, false nếu chưa.
	 */
	public boolean kiemTraMacTauTonTai(String macTau) {
		String sql = "SELECT COUNT(*) FROM Tau WHERE maTau = ?";
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, macTau);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				return rs.getInt(1) > 0;
			}
		} catch (SQLException e) {
			System.err.println("Lỗi khi kiểm tra mác tàu tồn tại: " + e.getMessage());
			e.printStackTrace();
		}
		return false; // Mặc định là không tồn tại nếu có lỗi
	}

	/**
	 * ✅ THÊM MỚI: Cập nhật lại toàn bộ cấu trúc toa cho một tàu đã tồn tại. Thực
	 * hiện trong một transaction: Xóa tất cả các toa cũ và thêm lại danh sách toa
	 * mới.
	 * 
	 * @param maTau      Mã của tàu cần cập nhật.
	 * @param cauTrucMoi Một Map chứa cấu trúc mới, với Key là số thứ tự và Value là
	 *                   đối tượng Toa.
	 * @return true nếu cập nhật thành công.
	 */
	public boolean capNhatCauTrucTau(String maTau, Map<Integer, Toa> cauTrucMoi) {
		Connection conn = null;
		try {
			conn = DatabaseConnection.getConnection();
			conn.setAutoCommit(false); // Bắt đầu Transaction

			// Bước 1: Xóa toàn bộ cấu trúc toa cũ của tàu này trong bảng ChiTietToa
			String sqlDelete = "DELETE FROM ChiTietToa WHERE maTau = ?";
			try (PreparedStatement stmt = conn.prepareStatement(sqlDelete)) {
				stmt.setString(1, maTau);
				stmt.executeUpdate();
			}

			// Bước 2: Thêm lại từng toa trong cấu trúc mới bằng Stored Procedure
			String sqlCallSP = "{CALL sp_ThemToaVaoTau(?, ?, ?)}";
			try (CallableStatement cstmt = conn.prepareCall(sqlCallSP)) {
				for (Map.Entry<Integer, Toa> entry : cauTrucMoi.entrySet()) {
					int soThuTuToa = entry.getKey();
					Toa toa = entry.getValue();

					cstmt.setString(1, maTau);
					cstmt.setInt(2, toa.getMaToa());
					cstmt.setInt(3, soThuTuToa);
					cstmt.execute();
				}
			}

			conn.commit(); // Hoàn tất Transaction thành công
			return true;

		} catch (SQLException e) {
			System.err.println("Lỗi khi cập nhật cấu trúc tàu " + maTau + ": " + e.getMessage());
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
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
}