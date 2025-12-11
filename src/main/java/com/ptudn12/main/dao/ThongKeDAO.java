package com.ptudn12.main.dao;

import com.ptudn12.main.database.DatabaseConnection;
import com.ptudn12.main.entity.ThongKe;
import java.sql.*;
import java.util.*;

public class ThongKeDAO {

    public List<ThongKe> getAllStatistics() throws Exception {
        Connection con = DatabaseConnection.getConnection();
        String sql =
                "SELECT " +
                "  td.maTuyen, " +
                "  CONCAT(g1.viTriGa, ' → ', g2.viTriGa) AS tenTuyen, " +
                "  COUNT(ctlt.maChiTietLichTrinh) AS tongVe, " +
                "  ISNULL(CAST(100.0 * COUNT(CASE WHEN vt.trangThai IN ('DaBan', 'DaSuDung') THEN 1 END) / " +
                "           NULLIF(COUNT(ctlt.maChiTietLichTrinh), 0) AS FLOAT), 0) AS tyLe, " +
                "  COUNT(DISTINCT l.maLichTrinh) AS soChuyen, " +
                "  ISNULL(SUM(CASE WHEN vt.trangThai IN ('DaBan', 'DaSuDung') THEN ctlt.giaChoNgoi ELSE 0 END), 0) AS doanhThu " +
                "FROM TuyenDuong td " +
                "LEFT JOIN Ga g1 ON td.diemDi = g1.maGa " +
                "LEFT JOIN Ga g2 ON td.diemDen = g2.maGa " +
                "LEFT JOIN LichTrinh l ON td.maTuyen = l.maTuyenDuong " +
                "LEFT JOIN ChiTietLichTrinh ctlt ON l.maLichTrinh = ctlt.maLichTrinh " +
                "LEFT JOIN VeTau vt ON ctlt.maChiTietLichTrinh = vt.chiTietLichTrinhId " +
                "WHERE l.maLichTrinh IS NOT NULL " +
                "GROUP BY td.maTuyen, g1.viTriGa, g2.viTriGa " +
                "ORDER BY td.maTuyen";

        PreparedStatement ps = con.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        List<ThongKe> list = new ArrayList<>();
        while (rs.next()) {
            list.add(new ThongKe(
                    String.valueOf(rs.getInt("maTuyen")),
                    rs.getString("tenTuyen"),
                    rs.getInt("tongVe"),
                    rs.getDouble("tyLe"),
                    rs.getInt("soChuyen"),
                    rs.getLong("doanhThu")
            ));
        }

        return list;
    }
}
