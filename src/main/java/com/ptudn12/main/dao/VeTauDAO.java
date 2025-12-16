
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
import com.ptudn12.main.entity.ChiTietLichTrinh;
import com.ptudn12.main.entity.Cho;
import com.ptudn12.main.entity.Ga;
import com.ptudn12.main.entity.KhachHang;
import com.ptudn12.main.entity.LichTrinh;
import com.ptudn12.main.entity.Tau;
import com.ptudn12.main.entity.Toa;
import com.ptudn12.main.entity.TuyenDuong;
import com.ptudn12.main.entity.VeDaMua;
import com.ptudn12.main.entity.VeTau;
import com.ptudn12.main.enums.LoaiCho;

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
	 * Lấy danh sách vé đã mua của khách hàng bằng Stored Procedure
	 * 
	 * @param maKhachHang ID (int) của khách hàng
	 */
	public List<VeTau> getLichSuVeCuaKhachHang(int maKhachHang) {
		List<VeTau> listVe = new ArrayList<>();
		// Gọi SP: sp_XemVeKhachHang @maKhachHang
		String sql = "{call sp_XemVeKhachHang(?)}";

		try (Connection conn = DatabaseConnection.getConnection(); CallableStatement cs = conn.prepareCall(sql)) {

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
				Ga gDi = new Ga();
				gDi.setViTriGa(rs.getString("DiemDi"));
				Ga gDen = new Ga();
				gDen.setViTriGa(rs.getString("DiemDen"));
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
				// Cần truy vấn thêm ID này nếu SP chưa trả về (SP hiện tại chưa trả về
				// maChiTietLichTrinh ID)
				// Tuy nhiên, logic trả vé cần update trạng thái chỗ.
				// -> GIẢI PHÁP: Trong TraVeController, ta sẽ dùng hàm
				// getChiTietLichTrinhIdByMaVe để lấy ID chính xác.

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

	// Thêm method này vào VeTauDAO. java
	public void updateQRCode(String maVe, String base64QR) {
		String sql = "UPDATE VeTau SET maQR = ? WHERE maVe = ?";

		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, base64QR);
			pstmt.setString(2, maVe);
			int rowsAffected = pstmt.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public VeTau getVeTauDetail(String maVe) {
		VeTau ve = null;

		// SQL JOIN đã được điều chỉnh theo đúng schema DB mới
		String sql = "SELECT VT.maVe, VT.loaiVe, VT.trangThai, VT.maQR, "
				+ "       KH.tenKhachHang, KH.soCCCD, KH.hoChieu, " + "       L.ngayKhoiHanh, L.gioKhoiHanh, "
				+ "       T.maTau, " + // Bảng Tau cột maTau
				"       G1.viTriGa AS tenGaDi, " + "       G2.viTriGa AS tenGaDen, " + "       TOA.tenToa, TOA.maToa, "
				+ "       C.soThuTu AS soGhe, C.loaiCho AS tenLoaiCho, " + "       CTLT.giaChoNgoi " + "FROM VeTau VT "
				+ "JOIN KhachHang KH ON VT.khachHangId = KH.maKhachHang "
				+ "JOIN ChiTietLichTrinh CTLT ON VT.chiTietLichTrinhId = CTLT.maChiTietLichTrinh "
				+ "JOIN LichTrinh L ON CTLT.maLichTrinh = L.maLichTrinh " + "JOIN Tau T ON L.maTau = T.maTau "
				+ "JOIN TuyenDuong TD ON L.maTuyenDuong = TD.maTuyen " + "JOIN Ga G1 ON TD.diemDi = G1.maGa "
				+ "JOIN Ga G2 ON TD.diemDen = G2.maGa " + "JOIN Cho C ON CTLT.maChoNgoi = C.maCho "
				+ "JOIN Toa TOA ON C.maToa = TOA.maToa " + // JOIN qua bảng Cho để lấy Toa chính xác
				"WHERE VT.maVe = ?";

		try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, maVe);
			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				ve = new VeTau();
				ve.setMaVe(rs.getString("maVe"));
				ve.setMaQR(rs.getString("maQR"));

				// 1. Khách Hàng
				KhachHang kh = new KhachHang();
				kh.setTenKhachHang(rs.getString("tenKhachHang"));
				kh.setSoCCCD(rs.getString("soCCCD"));
				kh.setHoChieu(rs.getString("hoChieu"));
				ve.setKhachHang(kh);

				// 2. Lịch Trình (Ngày giờ, Tàu, Ga)
				LichTrinh lt = new LichTrinh();

				java.sql.Date ngayDi = rs.getDate("ngayKhoiHanh");
				java.sql.Time gioDi = rs.getTime("gioKhoiHanh");
				if (ngayDi != null) {
					LocalDateTime dt = (gioDi != null) ? LocalDateTime.of(ngayDi.toLocalDate(), gioDi.toLocalTime())
							: ngayDi.toLocalDate().atStartOfDay();
					lt.setNgayGioKhoiHanh(dt);
				}

				Tau tau = new Tau(rs.getString("maTau"));
				tau.setMacTau(rs.getString("maTau"));
				lt.setTau(tau);

				TuyenDuong td = new TuyenDuong();
				Ga gaDi = new Ga();
				gaDi.setViTriGa(rs.getString("tenGaDi"));
				Ga gaDen = new Ga();
				gaDen.setViTriGa(rs.getString("tenGaDen"));
				td.setDiemDi(gaDi);
				td.setDiemDen(gaDen);
				lt.setTuyenDuong(td);

				// 3. Chi Tiết (Chỗ, Toa, Giá)
				ChiTietLichTrinh ctlt = new ChiTietLichTrinh();
				ctlt.setLichTrinh(lt);
				ctlt.setGiaChoNgoi(rs.getDouble("giaChoNgoi"));

				Cho cho = new Cho();
				cho.setSoThuTu(rs.getInt("soGhe"));

				// Map loại chỗ từ chuỗi DB (Ví dụ: "Ghế ngồi mềm")
				String tenLoaiChoDB = rs.getString("tenLoaiCho");
				try {
					// Gọi hàm map từ String sang Enum mà mình đã fix cho bạn ở LoaiCho.java
					cho.setLoaiCho(LoaiCho.fromDescription(tenLoaiChoDB));
				} catch (Exception e) {
					System.err.println("Warning: Không map được loại chỗ '" + tenLoaiChoDB + "' sang Enum.");
				}

				Toa t = new Toa();
				t.setTenToa(rs.getString("tenToa"));
				t.setMaToa(rs.getInt("maToa"));
				cho.setToa(t);

				ctlt.setCho(cho);
				ve.setChiTietLichTrinh(ctlt);
			}

		} catch (SQLException e) {
			System.err.println("Lỗi lấy chi tiết vé (SQL): " + e.getMessage());
			e.printStackTrace();
		}

		return ve;
	}

	/**
	 * Cập nhật trạng thái vé (DaHuy)
	 */
	public boolean updateTrangThaiVe(String maVe, String trangThaiMoi) {
		String sql = "UPDATE VeTau SET trangThai = ? WHERE maVe = ?";
		try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

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
		try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, maVe);
			ResultSet rs = ps.executeQuery();
			if (rs.next())
				return rs.getInt("chiTietLichTrinhId");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
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

			// ... code khai báo formatter giữ nguyên ...
			int sttCounter = 1;
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
			Locale vietnameseLocale = new Locale("vi", "VN");
			NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(vietnameseLocale);

			while (rs.next()) {
				// ... code tính toán thời gian đi - đến giữ nguyên ...
				Date ngayKhoiHanhSQL = rs.getDate("ngayKhoiHanh");
				Time gioKhoiHanhSQL = rs.getTime("gioKhoiHanh");
				int thoiGianDuKienGio = rs.getInt("thoiGianDuKien");
				LocalDateTime thoiGianDi = ngayKhoiHanhSQL.toLocalDate().atTime(gioKhoiHanhSQL.toLocalTime());
				LocalDateTime thoiGianDen = thoiGianDi.plusHours(thoiGianDuKienGio);
				String thoiGianDiDenFormatted = thoiGianDi.format(formatter) + " - " + thoiGianDen.format(formatter);

				// ==========================================================
				// === CẬP NHẬT LOGIC HIỂN THỊ "TOA - LOẠI CHỖ" TẠI ĐÂY ===
				// ==========================================================
				int soThuTuToa = rs.getInt("soThuTuToa");
				String loaiCho = rs.getString("loaiCho");

				// Nếu tìm thấy số thứ tự toa (khác 0), hiển thị nó. Ngược lại, chỉ hiển thị
				// loại chỗ.
				String toaLoaiChoFormatted = (soThuTuToa > 0) ? soThuTuToa + " - " + loaiCho : loaiCho;
				// ==========================================================

				// ... code lấy các thông tin còn lại giữ nguyên ...
				String macTau = rs.getString("maTau");
				Date ngayMuaSQL = rs.getDate("NgayMua");
				String ngayMuaFormatted = new SimpleDateFormat("dd/MM/yyyy").format(ngayMuaSQL);
				String hanhTrinh = rs.getString("DiemDi") + " - " + rs.getString("DiemDen");
				int soCho = rs.getInt("ViTriCho");
				double thanhTien = rs.getDouble("thanhTien");

				VeDaMua ve = new VeDaMua(sttCounter++, ngayMuaFormatted, rs.getString("maVe"), macTau, hanhTrinh,
						thoiGianDiDenFormatted, toaLoaiChoFormatted, // <-- Sử dụng chuỗi đã được định dạng mới
						soCho, currencyFormatter.format(thanhTien));
				danhSach.add(ve);
			}

		} catch (SQLException e) {
			System.err.println("Lỗi khi lấy lịch sử mua vé cho khách hàng " + maKhachHang + ": " + e.getMessage());
			e.printStackTrace();
		}

		return danhSach;
	}
}
