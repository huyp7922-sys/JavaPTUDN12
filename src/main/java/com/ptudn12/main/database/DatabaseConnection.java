/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ptudn12.main.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author Huy
 */
public class DatabaseConnection {
    private static final String SERVER = "localhost";
    private static final String PORT = "root";
    private static final String DATABASE = "HeThongVeTau";
    private static final String USER = "sa"; // hoặc username của bạn
    private static final String PASSWORD = "123456";
    
    private static final String URL = String.format(
        "jdbc:sqlserver://%s:%s;databaseName=%s;encrypt=true;trustServerCertificate=true",
        SERVER, PORT, DATABASE
    );
    
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}