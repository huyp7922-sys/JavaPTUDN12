package com.ptudn12.main.utils;

import com.ptudn12.main.entity.NhanVien;
import com.ptudn12.main.entity.TaiKhoan;

/**
 * Quản lý phiên đăng nhập hiện tại
 */
public class SessionManager {

	private static SessionManager instance;
	private NhanVien currentNhanVien;
	private TaiKhoan currentTaiKhoan;
	private String currentUsername;
	private boolean isAdmin;

	private SessionManager() {
	}

	public static SessionManager getInstance() {
		if (instance == null) {
			instance = new SessionManager();
		}
		return instance;
	}

	/**
	 * Đăng nhập
	 */
	public void login(String username, NhanVien nhanVien, TaiKhoan taiKhoan) {
		this.currentUsername = username;
		this.currentNhanVien = nhanVien;
		this.currentTaiKhoan = taiKhoan;

		// Kiểm tra quyền: true = Quản lý, false = Nhân viên
		if (nhanVien != null) {
			// ===== SỬA DÒNG NÀY =====
			// Kiểm tra chức vụ: "Quản lý" = true, "Nhân viên" = false
			String chucVu = nhanVien.getChucVuText();
			this.isAdmin = chucVu != null && chucVu.equalsIgnoreCase("Quản lý");
			// ========================
		} else {
			// Tài khoản test
			this.isAdmin = username.equals("admin");
		}
	}

	/**
	 * Đăng xuất
	 */
	public void logout() {
		this.currentUsername = null;
		this.currentNhanVien = null;
		this.currentTaiKhoan = null;
		this.isAdmin = false;
	}

	/**
	 * Kiểm tra đã đăng nhập chưa
	 */
	public boolean isLoggedIn() {
		return currentUsername != null;
	}

	/**
	 * Kiểm tra có phải Quản lý không
	 */
	public boolean isAdmin() {
		return isAdmin;
	}

	/**
	 * Lấy nhân viên hiện tại
	 */
	public NhanVien getCurrentNhanVien() {
		return currentNhanVien;
	}

	/**
	 * Lấy tài khoản hiện tại
	 */
	public TaiKhoan getCurrentTaiKhoan() {
		return currentTaiKhoan;
	}

	/**
	 * Lấy username hiện tại
	 */
	public String getCurrentUsername() {
		return currentUsername;
	}

	/**
	 * Lấy mã nhân viên hiện tại
	 */
	public String getCurrentMaNhanVien() {
		if (currentNhanVien != null) {
			return currentNhanVien.getMaNhanVien();
		}
		return currentUsername; // Trả về username nếu là tài khoản test
	}

	/**
	 * Lấy tên hiển thị
	 */
	public String getDisplayName() {
		if (currentNhanVien != null) {
			return currentNhanVien.getTenNhanVien();
		}
		return currentUsername; // Trả về username nếu là tài khoản test
	}

	/**
	 * Lấy vai trò
	 */
	public String getRole() {
		return isAdmin ? "Quản lý" : "Nhân viên";
	}
}