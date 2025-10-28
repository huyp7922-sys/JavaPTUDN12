package com.ptudn12.main.utils;

/**
 * Utility class để tạo mật khẩu cho tài khoản nhân viên
 * Format: [Chữ cái đầu chức vụ][6 số cuối CCCD]
 * - Quản lý: 1 + 6 số cuối CCCD (ví dụ: 1123456)
 * - Nhân viên: 0 + 6 số cuối CCCD (ví dụ: 0123456)
 */
public class PasswordGenerate {
    
    /**
     * Tạo mật khẩu dựa trên CCCD và chức vụ
     * @param cccd Số CCCD của nhân viên
     * @param isQuanLy true nếu là quản lý, false nếu là nhân viên
     * @return Mật khẩu được tạo
     */
    public static String generatePassword(String cccd, boolean isQuanLy) {
        if (cccd == null || cccd.length() < 6) {
            throw new IllegalArgumentException("CCCD phải có ít nhất 6 số!");
        }
        
        // Lấy 6 số cuối của CCCD
        String last6Digits = cccd.substring(cccd.length() - 6);
        
        // Thêm chữ số đầu dựa trên chức vụ
        String prefix = isQuanLy ? "1" : "0";
        
        return prefix + last6Digits;
    }
    
    /**
     * Kiểm tra mật khẩu có hợp lệ không
     * @param password Mật khẩu cần kiểm tra
     * @return true nếu hợp lệ, false nếu không
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() != 7) {
            return false;
        }
        
        // Kiểm tra ký tự đầu phải là 0 hoặc 1
        char firstChar = password.charAt(0);
        if (firstChar != '0' && firstChar != '1') {
            return false;
        }
        
        // Kiểm tra 6 ký tự sau phải là số
        return password.substring(1).matches("\\d{6}");
    }
    
    /**
     * Lấy chức vụ từ mật khẩu
     * @param password Mật khẩu
     * @return "Quản lý" nếu bắt đầu bằng 1, "Nhân viên" nếu bắt đầu bằng 0, null nếu không hợp lệ
     */
    public static String getChucVuFromPassword(String password) {
        if (!isValidPassword(password)) {
            return null;
        }
        
        return password.charAt(0) == '1' ? "Quản lý" : "Nhân viên";
    }
}