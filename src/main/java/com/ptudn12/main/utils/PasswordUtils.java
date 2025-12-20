package com.ptudn12.main.utils;

import org.mindrot.jbcrypt.BCrypt;

public final class PasswordUtils {
    private static final int COST = 12; 

    private PasswordUtils() {}

    public static String hash(String plain) {
        if (plain == null || plain.isBlank()) {
            throw new IllegalArgumentException("Password is empty");
        }
        return BCrypt.hashpw(plain, BCrypt.gensalt(COST));
    }

    public static boolean verify(String plain, String hashed) {
        if (plain == null || hashed == null) return false;
        try {
            return BCrypt.checkpw(plain, hashed);
        } catch (Exception ex) {
            return false;
        }
    }

    public static boolean looksLikeBCrypt(String s) {
        return s != null && (s.startsWith("$2a$") || s.startsWith("$2b$") || s.startsWith("$2y$"));
    }
}
