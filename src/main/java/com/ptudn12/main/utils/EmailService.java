package com.ptudn12.main.utils;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;
import java.util.Random;

public class EmailService {
    
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String EMAIL_FROM = "contact.phamthanhhuy@gmail.com"; // EMAIL của bản thân 
    private static final String EMAIL_PASSWORD = "dujruwxeuczkhgzt"; // Vào Apppassword authetication gg tạo auth rồi cấp app mật khẩu
    
    /**
     * Gửi mật khẩu mới qua email
     */
    public static boolean sendPasswordResetEmail(String toEmail, String newPassword, String tenNhanVien) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", SMTP_PORT);
            props.put("mail.smtp.ssl.trust", SMTP_HOST);
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");
            
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(EMAIL_FROM, EMAIL_PASSWORD);
                }
            });
            
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_FROM));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Khôi phục mật khẩu - Hệ Thống Quản Lý Bán Vé Tàu");
            
            String htmlContent = "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <style>" +
                "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                "        .container { max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px; }" +
                "        .header { background-color: #0066cc; color: white; padding: 20px; text-align: center; border-radius: 10px 10px 0 0; }" +
                "        .content { padding: 30px; background-color: #f9f9f9; }" +
                "        .password-box { background-color: #fff; padding: 15px; border: 2px solid #0066cc; border-radius: 5px; text-align: center; font-size: 24px; font-weight: bold; color: #0066cc; margin: 20px 0; }" +
                "        .footer { text-align: center; color: #999; font-size: 12px; margin-top: 20px; }" +
                "        .warning { color: #e74c3c; font-weight: bold; }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class='container'>" +
                "        <div class='header'>" +
                "            <h1>🔐 Khôi Phục Mật Khẩu</h1>" +
                "        </div>" +
                "        <div class='content'>" +
                "            <p>Xin chào <strong>" + tenNhanVien + "</strong>,</p>" +
                "            <p>Chúng tôi đã nhận được yêu cầu khôi phục mật khẩu của bạn.</p>" +
                "            <p>Mật khẩu mới của bạn là:</p>" +
                "            <div class='password-box'>" + newPassword + "</div>" +
                "            <p class='warning'>⚠️ Vui lòng đổi mật khẩu ngay sau khi đăng nhập!</p>" +
                "            <p>Nếu bạn không yêu cầu khôi phục mật khẩu, vui lòng liên hệ quản trị viên ngay lập tức.</p>" +
                "        </div>" +
                "        <div class='footer'>" +
                "            <p>© 2025 Hệ Thống Quản Lý Bán Vé Tàu</p>" +
                "            <p>Email này được gửi tự động, vui lòng không trả lời.</p>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";
            
            message.setContent(htmlContent, "text/html; charset=utf-8");
            
            Transport.send(message);
            
            return true;
            
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Tạo mật khẩu ngẫu nhiên
     */
    public static String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%";
        Random random = new Random();
        StringBuilder password = new StringBuilder();
        
        for (int i = 0; i < 10; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return password.toString();
    }
}