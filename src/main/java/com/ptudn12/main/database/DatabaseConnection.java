package com.ptudn12.main.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author Huy
 */
public class DatabaseConnection {
    private static final String SERVER = "localhost";
   // private static final String PORT = "1433"; // Sửa lại từ "root" thành "1433"
  private static final String PORT = "3306";
    private static final String DATABASE = "HeThongVeTau";
    private static final String USER = "sa";
    private static final String PASSWORD = "123456";
    
   private static final String URL = String.format(
        // "jdbc:sqlserver://%s:%s;databaseName=%s;encrypt=true;trustServerCertificate=true;characterEncoding=UTF-8;sendStringParametersAsUnicode=true"
         "jdbc:mysql://%s:%s;databaseName=%s;encrypt=true;trustServerCertificate=true;characterEncoding=UTF-8;sendStringParametersAsUnicode=true",
        SERVER, PORT, DATABASE
    );
    
    public static Connection getConnection() throws SQLException {
        try {
        //    Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            Class.forName("com.microsoft.mysql.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQL Server JDBC Driver not found", e);
        }
    }
    
    // Test connection
    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            System.out.println("Kết nối thành công đến database: " + DATABASE);
        } catch (SQLException e) {
            System.err.println("Lỗi kết nối: " + e.getMessage());
            e.printStackTrace();
        }
    }
}