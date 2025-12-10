package com. ptudn12.main.util;

import com.ptudn12.main.entity.NhanVien;
import com.ptudn12.main.entity. TaiKhoan;

/**
 * Quản lý phiên đăng nhập hiện tại
 */
public class SessionManager {
    
    private static SessionManager instance;
    private NhanVien currentNhanVien;
    private TaiKhoan currentTaiKhoan;
    private String currentUsername;
    private boolean isAdmin;
    
    private SessionManager() {}
    
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
        
        // Kiểm tra quyền:  true = Quản lý (Admin), false = Nhân viên
        if (nhanVien != null) {
            // ===== SỬA:  getChucVu() TRẢ VỀ BOOLEAN =====
            // true (1 trong DB) = Quản lý
            // false (0 trong DB) = Nhân viên
            this.isAdmin = nhanVien.getChucVu();
            // ===========================================
        } else {
            // Tài khoản test
            this.isAdmin = username. equals("admin");
        }
    }
    
    /**
     * Đăng xuất
     */
    public void logout() {
        this.currentUsername = null;
        this. currentNhanVien = null;
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
            return currentNhanVien. getMaNhanVien();
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
     * Lấy vai trò (text để hiển thị)
     */
    public String getRole() {
        return isAdmin ? "Quản lý" : "Nhân viên";
    }
    
    /**
     * Lấy mã chức vụ (1 = Quản lý, 0 = Nhân viên)
     */
    public int getRoleCode() {
        return isAdmin ? 1 :  0;
    }
    
    /**
     * Lấy boolean chức vụ (true = Quản lý, false = Nhân viên)
     */
    public boolean getRoleBoolean() {
        return isAdmin;
    }
    
    /**
     * Chuyển đổi boolean sang text để hiển thị
     */
    public static String getRoleText(boolean chucVu) {
        return chucVu ? "Quản lý" : "Nhân viên";
    }
    
    /**
     * Chuyển đổi int (từ DB) sang text để hiển thị
     */
    public static String getRoleText(int chucVu) {
        return chucVu == 1 ? "Quản lý" : "Nhân viên";
    }
}