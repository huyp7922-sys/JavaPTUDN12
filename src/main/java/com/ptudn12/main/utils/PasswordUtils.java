package com.ptudn12.main.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class PasswordUtils {

    private PasswordUtils() {}

    /**
     * Hash password với MD5 và lấy 20 ký tự đầu (vừa với VARCHAR(20))
     */
    public static String hash(String plain) {
        if (plain == null || plain.isBlank()) {
            throw new IllegalArgumentException("Password is empty");
        }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(plain.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.substring(0, 20);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }

    /**
     * Verify password bằng cách hash lại và so sánh
     */
    public static boolean verify(String plain, String hashed) {
        if (plain == null || hashed == null) return false;
        try {
            String hashedPlain = hash(plain);
            return hashedPlain.equals(hashed);
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Kiểm tra xem có phải hash hay plain text
     */
    public static boolean isHashed(String password) {
        // Hash MD5 (20 ký tự) chỉ chứa hex (0-9, a-f)
        return password != null && password.length() == 20 && password.matches("[0-9a-f]{20}");
    }
}
