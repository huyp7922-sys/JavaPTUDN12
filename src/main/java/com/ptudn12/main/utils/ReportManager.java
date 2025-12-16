package com.ptudn12.main. utils;

import com.ptudn12.main.entity.VeTau;
import net.sf.jasperreports. engine.*;
import net.sf. jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports. engine.xml.JRXmlLoader;
import net. sf.jasperreports.view.JasperViewer;

import java.io. InputStream;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class ReportManager {
    private static final String REPORT_TEMPLATE_PATH = "/views/ticket_template.xml";
    private static final DecimalFormat moneyFormatter = new DecimalFormat("#,##0 'VNĐ'");
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    public static void printVeTau(VeTau ve) {
        try {
            // 1. Load Template
            InputStream reportStream = ReportManager.class. getResourceAsStream(REPORT_TEMPLATE_PATH);
            if (reportStream == null) {
                System.err.println("Không tìm thấy file report template!");
                return;
            }
            JasperDesign jasperDesign = JRXmlLoader.load(reportStream);
            JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);

            // 2. Map dữ liệu
            Map<String, Object> parameters = new HashMap<>();
            
            parameters.put("maVe", ve.getMaVe());
            
            // Thông tin tàu & Lịch trình
            String ngayKhoiHanh = "";
            if (ve.getChiTietLichTrinh() != null && ve.getChiTietLichTrinh().getLichTrinh() != null) {
                var lt = ve.getChiTietLichTrinh().getLichTrinh();
                parameters.put("macTau", lt.getTau().getMacTau());
                parameters.put("ngayDi", lt. getNgayGioKhoiHanh().format(dateFormatter));
                parameters. put("gioDi", lt.getNgayGioKhoiHanh().format(timeFormatter));
                ngayKhoiHanh = lt.getNgayGioKhoiHanh().toString();
                
                if (lt.getTuyenDuong() != null) {
                    parameters.put("gaDi", lt.getTuyenDuong().getDiemDi().getViTriGa());
                    parameters.put("gaDen", lt.getTuyenDuong().getDiemDen().getViTriGa());
                }
            }
            
            // Thông tin Chỗ
            if (ve.getChiTietLichTrinh() != null && ve.getChiTietLichTrinh().getCho() != null) {
                var cho = ve.getChiTietLichTrinh().getCho();
                parameters.put("toa", String.valueOf(cho.getToa().getMaToa()));
                parameters.put("cho", String.valueOf(cho. getSoThuTu()));
                parameters.put("loaiCho", cho.getLoaiCho().getDescription());
            }
            
            // Thông tin khác
            parameters.put("loaiVe", ve.getLoaiVe() != null ? ve.getLoaiVe().toString() : "Thường");
            
            // Khách hàng
            if (ve.getKhachHang() != null) {
                parameters.put("hoTen", ve.getKhachHang().getTenKhachHang());
                String id = ve.getKhachHang().getSoCCCD();
                if (id == null) id = ve.getKhachHang().getHoChieu();
                parameters.put("giayTo", id);
            }
            
            // Giá vé
            double gia = 0;
            if (ve. getChiTietLichTrinh() != null) gia = ve.getChiTietLichTrinh().getGiaChoNgoi();
            parameters.put("giaVe", moneyFormatter. format(gia));
            
            String qrUrl = "https://huyp7922-sys.github.io/railway-ticket-verify/?maVe=" + ve.getMaVe() + "&ngay=" + ngayKhoiHanh;
            parameters.put("qrCodeData", qrUrl);

            // 3. Fill Report
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource());

            // 4. Hiển thị
            JasperViewer.viewReport(jasperPrint, false); 

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Lỗi khi tạo report: " + e.getMessage());
        }
    }
}