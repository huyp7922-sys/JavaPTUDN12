package com.ptudn12.main.dao;

import com.ptudn12.main.database.DatabaseConnection;
import com.ptudn12.main.entity.ThongKe;
import java.sql.*;
import java.util.*;

public class ThongKeDAO {

    public List<ThongKe> getAllStatistics() throws Exception {
        Connection con = DatabaseConnection.getConnection();
        String sql =
                "SELECT t.maTuyen, t.tenTuyen, " +
                "SUM(ct.soVe) AS tongVe, " +
                "AVG(ct.tyLeLapDay) AS tile, " +
                "COUNT(*) AS soChuyen, " +
                "SUM(ct.doanhThu) AS doanhThu " +
                "FROM ChiTietLichTrinh ct " +
                "JOIN TuyenDuong t ON ct.maTuyen = t.maTuyen " +
                "GROUP BY t.maTuyen, t.tenTuyen";

        PreparedStatement ps = con.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        List<ThongKe> list = new ArrayList<>();
        while (rs.next()) {
            list.add(new ThongKe(
                    rs.getString("maTuyen"),
                    rs.getString("tenTuyen"),
                    rs.getInt("tongVe"),
                    rs.getDouble("tile"),
                    rs.getInt("soChuyen"),
                    rs.getLong("doanhThu")
            ));
        }

        return list;
    }
}
