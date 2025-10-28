package com.ptudn12.main.dao;

/**
 *
 * @author fo3cp
 */

import com.ptudn12.main.database.DatabaseConnection;
import com.ptudn12.main.entity.HoaDon;
import com.ptudn12.main.entity.KhachHang;
import com.ptudn12.main.entity.NhanVien;
import com.ptudn12.main.enums.LoaiHoaDon;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HoaDonDAO {
    public boolean createHoaDon(String maHoaDon, int khachHangId, String maNhanVien, double tongThanhToan) {
        String sql = "INSERT INTO HoaDon (maHoaDon, khachHangId, nhanVienId, ngayLap, tongTienHoaDon, loaiHoaDon) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maHoaDon);
            ps.setInt(2, khachHangId);
            ps.setString(3, maNhanVien);
            ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            ps.setDouble(5, tongThanhToan);
            ps.setString(6, "Bán vé");

            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi khi tạo hóa đơn " + maHoaDon + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

     /**
     * Tạo mã hóa đơn duy nhất ("HD" + YYYY + 6 số).
     * @return Mã hóa đơn mới hoặc null nếu lỗi.
     */
     public String generateUniqueHoaDonId() {
        int currentYear = LocalDate.now().getYear();
        String prefix = "HD" + currentYear;
        int nextSequence = 1;
        String sqlQuery = "SELECT MAX(CAST(SUBSTRING(maHoaDon, 7, 6) AS INT)) FROM HoaDon WHERE maHoaDon LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement psQuery = conn.prepareStatement(sqlQuery)) {

            psQuery.setString(1, prefix + "%");
            ResultSet rs = psQuery.executeQuery();
            if (rs.next()) {
                nextSequence = rs.getInt(1) + 1;
            }
            return String.format("%s%06d", prefix, nextSequence);

        } catch (SQLException e) {
            System.err.println("Lỗi khi tạo mã hóa đơn: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
     
     /**
	 * Lấy tất cả hóa đơn từ cơ sở dữ liệu để hiển thị lên bảng. Dữ liệu được kết
	 * (JOIN) từ các bảng HoaDon, KhachHang, và NhanVien.
	 *
	 * @return Danh sách các đối tượng HoaDon.
	 */
	public List<HoaDon> layTatCaHoaDon() {
		List<HoaDon> danhSach = new ArrayList<>();
		// Câu lệnh SQL kết nối 3 bảng để lấy thông tin cần thiết
		String sql = "SELECT " + "  hd.maHoaDon, hd.ngayLap, hd.loaiHoaDon, " + "  kh.maKhachHang, kh.tenKhachHang, "
				+ "  nv.maNhanVien, nv.tenNhanVien " + "FROM HoaDon hd "
				+ "JOIN KhachHang kh ON hd.khachHangId = kh.maKhachHang "
				+ "JOIN NhanVien nv ON hd.nhanVienId = nv.maNhanVien " + "ORDER BY hd.ngayLap DESC"; // Sắp xếp theo
																										// ngày mới nhất
																										// lên đầu

		try (Connection conn = DatabaseConnection.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next()) {
				HoaDon hd = mapResultSetToHoaDon(rs);
				danhSach.add(hd);
			}

		} catch (SQLException e) {
			System.err.println("Lỗi khi lấy danh sách hóa đơn: " + e.getMessage());
			e.printStackTrace();
		}

		return danhSach;
	}

	/**
	 * Phương thức hỗ trợ để chuyển đổi một dòng ResultSet thành đối tượng HoaDon.
	 *
	 * @param rs ResultSet đang trỏ đến một hàng dữ liệu.
	 * @return Một đối tượng HoaDon đã được điền đầy đủ thông tin.
	 * @throws SQLException
	 */
	private HoaDon mapResultSetToHoaDon(ResultSet rs) throws SQLException {
		// Lấy dữ liệu từ các cột của ResultSet
		String maHoaDon = rs.getString("maHoaDon");
		LocalDateTime ngayLap = rs.getTimestamp("ngayLap").toLocalDateTime();
		String loaiHoaDonStr = rs.getString("loaiHoaDon");

		// --- Xử lý tạo đối tượng KhachHang ---
		int maKhachHangInt = rs.getInt("maKhachHang");
		String tenKhachHang = rs.getString("tenKhachHang");

		// Tạo một đối tượng KhachHang đơn giản chỉ với thông tin cần hiển thị
		KhachHang khachHang = new KhachHang();
		khachHang.setMaKH(String.format("KH%09d", maKhachHangInt)); // Định dạng lại mã
		khachHang.setTenKhachHang(tenKhachHang);

		// --- Xử lý tạo đối tượng NhanVien ---
		String maNhanVien = rs.getString("maNhanVien");
		String tenNhanVien = rs.getString("tenNhanVien");

		// Tạo một đối tượng NhanVien đơn giản
		NhanVien nhanVien = new NhanVien();
		nhanVien.setMaNhanVien(maNhanVien);
		nhanVien.setTenNhanVien(tenNhanVien);

		// --- Chuyển đổi String từ DB sang Enum LoaiHoaDon ---
		// Giả định rằng Enum của bạn có các hằng số là BAN_VE, HOAN_TIEN, DOI_VE
		LoaiHoaDon loaiHoaDon;
		switch (loaiHoaDonStr) {
		case "Bán vé":
			loaiHoaDon = LoaiHoaDon.BAN_VE;
			break;
		case "Hoàn tiền":
			loaiHoaDon = LoaiHoaDon.HOAN_TIEN;
			break;
		// Trường hợp "Đổi vé" từ DB không có trong Enum, sẽ rơi vào default
		default:
			loaiHoaDon = null;
			break;
		}

		// Tạo đối tượng HoaDon hoàn chỉnh
		return new HoaDon(maHoaDon, nhanVien, khachHang, ngayLap, loaiHoaDon);
	}

	/**
	 * Lấy tất cả hóa đơn từ một ngày bắt đầu cho đến hiện tại.
	 * 
	 * @param startDate Ngày bắt đầu để lọc.
	 * @return Danh sách các đối tượng HoaDon.
	 */
	public List<HoaDon> layHoaDonTuNgay(LocalDate startDate) {
		List<HoaDon> danhSach = new ArrayList<>();
		String sql = "SELECT " + "  hd.maHoaDon, hd.ngayLap, hd.loaiHoaDon, " + "  kh.maKhachHang, kh.tenKhachHang, "
				+ "  nv.maNhanVien, nv.tenNhanVien " + "FROM HoaDon hd "
				+ "JOIN KhachHang kh ON hd.khachHangId = kh.maKhachHang "
				+ "JOIN NhanVien nv ON hd.nhanVienId = nv.maNhanVien " + "WHERE hd.ngayLap >= ? " + // Điều kiện lọc
																									// theo ngày
				"ORDER BY hd.ngayLap DESC";

		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {

			// Chuyển LocalDate thành Timestamp để truy vấn
			stmt.setTimestamp(1, Timestamp.valueOf(startDate.atStartOfDay()));

			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					HoaDon hd = mapResultSetToHoaDon(rs);
					danhSach.add(hd);
				}
			}

		} catch (SQLException e) {
			System.err.println("Lỗi khi lấy danh sách hóa đơn theo ngày: " + e.getMessage());
			e.printStackTrace();
		}
		return danhSach;
	}

	/**
	 * Lấy năm xưa nhất có hóa đơn trong cơ sở dữ liệu.
	 * 
	 * @return Năm nhỏ nhất, hoặc -1 nếu không có hóa đơn nào.
	 */
	public int getOldestInvoiceYear() {
		String sql = "SELECT MIN(YEAR(ngayLap)) FROM HoaDon";
		try (Connection conn = DatabaseConnection.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {
			if (rs.next()) {
				return rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1; // Trả về giá trị đặc biệt nếu có lỗi hoặc không có dữ liệu
	}
}
