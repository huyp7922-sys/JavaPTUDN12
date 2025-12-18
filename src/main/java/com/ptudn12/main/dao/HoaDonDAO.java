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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            ps.setString(6, "BanVe");

            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi khi tạo hóa đơn " + maHoaDon + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

     /**
     * Tạo mã hóa đơn: HD + YY (2 số cuối năm) + 8 số thứ tự.
     * Ví dụ: HD2500000001 (Năm 2025, hóa đơn số 1)
     * Max: 100 triệu hóa đơn/năm.
     */
    public String generateUniqueHoaDonId() {
        // Lấy 2 số cuối của năm (Ví dụ: 2025 -> "25")
        String yearSuffix = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yy"));
        String prefix = "HD" + yearSuffix; 
        
        int nextSequence = 1;
        
        // SQL: Lấy 8 ký tự cuối cùng để chuyển thành số (Bỏ 'HD' và 2 số năm = 4 ký tự đầu)
        // SUBSTRING(cột, vị_trí_bắt_đầu, độ_dài) -> Bắt đầu từ 5
        String sqlQuery = "SELECT MAX(CAST(SUBSTRING(maHoaDon, 5, 8) AS BIGINT)) FROM HoaDon WHERE maHoaDon LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement psQuery = conn.prepareStatement(sqlQuery)) {

            psQuery.setString(1, prefix + "%");
            ResultSet rs = psQuery.executeQuery();
            if (rs.next()) {
                // Nếu tìm thấy, tăng số thứ tự lên 1
                long maxId = rs.getLong(1); 
                if (maxId > 0) nextSequence = (int) (maxId + 1);
            }
            
            // Format thành 8 chữ số (để tổng là 2 + 2 + 8 = 12)
            return String.format("%s%08d", prefix, nextSequence);

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
        
        public List<Map<String, Object>> getChiTietHoaDonById(String maHoaDon) {
		List<Map<String, Object>> listItems = new ArrayList<>();

		// Cập nhật SQL: Join thêm bảng Cho và Toa
		String sql = "SELECT " + "  cthd.maVe, " + "  cthd.thanhTien, " + "  cthd.BAO_HIEM, " + "  g1.viTriGa AS gaDi, "
				+ "  g2.viTriGa AS gaDen, " + "  l.maTau, " + "  l.ngayKhoiHanh, " + "  t2.tenToa, " + // Lấy tên toa
																										// (VD: NC01)
				"  t2.loaiToa, " + // Lấy loại toa (VD: Ngồi cứng)
				"  c.soThuTu AS soCho " + // Lấy số ghế
				"FROM ChiTietHoaDon cthd " + "JOIN VeTau vt ON cthd.maVe = vt.maVe "
				+ "JOIN ChiTietLichTrinh ctlt ON vt.chiTietLichTrinhId = ctlt.maChiTietLichTrinh "
				+ "JOIN LichTrinh l ON ctlt.maLichTrinh = l.maLichTrinh "
				+ "JOIN TuyenDuong td ON l.maTuyenDuong = td.maTuyen " + "JOIN Ga g1 ON td.diemDi = g1.maGa "
				+ "JOIN Ga g2 ON td.diemDen = g2.maGa " + "JOIN Cho c ON ctlt.maChoNgoi = c.maCho " + // JOIN MỚI
				"JOIN Toa t2 ON c.maToa = t2.maToa " + // JOIN MỚI
				"WHERE cthd.maHoaDon = ?";

		try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, maHoaDon);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				Map<String, Object> item = new HashMap<>();
				item.put("maVe", rs.getString("maVe"));

				// 1. Xử lý Loại Toa -> Mã
				String loaiToaFull = rs.getString("loaiToa");
				String maLoaiToa = "K"; // Mặc định
				if (loaiToaFull != null) {
					if (loaiToaFull.contains("Ngồi mềm"))
						maLoaiToa = "NM";
					else if (loaiToaFull.contains("Ngồi cứng"))
						maLoaiToa = "NC";
					else if (loaiToaFull.contains("khoang 4"))
						maLoaiToa = "G4";
					else if (loaiToaFull.contains("khoang 6"))
						maLoaiToa = "G6";
					else if (loaiToaFull.contains("VIP"))
						maLoaiToa = "VIP";
				}

				// 2. Xử lý Số Toa (Lấy số từ tên toa, VD: NC01 -> 1)
				String tenToa = rs.getString("tenToa");
				String soToa = tenToa.replaceAll("[^0-9]", ""); // Chỉ giữ lại số

				// 3. Format Ngày
				String ngayDi = rs.getDate("ngayKhoiHanh").toLocalDate()
						.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));

				// 4. Tạo chuỗi Tên Dịch Vụ đầy đủ
				// Format: [GaDi]-[GaDen]-[Tau]-[Ngay]-[Toa]-[SoCho]-[LoaiToa]
				String tenDichVu = String.format("Vé HK: %s-%s-%s-%s-%s-%s-%s", rs.getString("gaDi"), // Ga Đi
						rs.getString("gaDen"), // Ga Đến
						rs.getString("maTau"), // Tàu
						ngayDi, // Ngày
						soToa, // Số toa
						rs.getString("soCho"), // Số chỗ
						maLoaiToa // Loại toa
				);

				item.put("tenDichVu", tenDichVu);
				item.put("dvt", "Vé");
				item.put("soLuong", 1.0);
				item.put("thanhTienGoc", rs.getDouble("thanhTien"));
				item.put("baoHiem", rs.getDouble("BAO_HIEM"));

				listItems.add(item);
			}

		} catch (SQLException e) {
			System.err.println("Lỗi lấy chi tiết hóa đơn: " + e.getMessage());
			e.printStackTrace();
		}
		return listItems;
	}
}