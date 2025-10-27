package com.ptudn12.main.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DashboardStatisticsController {

    
    @FXML private Label welcomeSubtitle;
    @FXML private Label totalRoutesLabel;
    @FXML private Label todaySchedulesLabel;
    @FXML private Label totalTrainsLabel;
    @FXML private Label revenueLabel;
    
    @FXML private BarChart<String, Number> revenueChart;
    @FXML private PieChart scheduleStatusChart;
    @FXML private ListView<String> activitiesList;

    @FXML
    public void initialize() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm, dd/MM/yyyy");
        welcomeSubtitle.setText("Dưới đây là tổng quan hệ thống - Cập nhật lúc " + 
                               LocalDateTime.now().format(formatter));

        loadStatistics();
        
        loadRevenueChart();
        
        loadScheduleStatusChart();
        
        loadRecentActivities();
    }

    private void loadStatistics() {
        totalRoutesLabel.setText("24");
        todaySchedulesLabel.setText("12");
        totalTrainsLabel.setText("45");
        revenueLabel.setText("2.5 tỷ");
    }

    private void loadRevenueChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Doanh thu");

        series.getData().add(new XYChart.Data<>("T2", 350));
        series.getData().add(new XYChart.Data<>("T3", 420));
        series.getData().add(new XYChart.Data<>("T4", 380));
        series.getData().add(new XYChart.Data<>("T5", 450));
        series.getData().add(new XYChart.Data<>("T6", 520));
        series.getData().add(new XYChart.Data<>("T7", 480));
        series.getData().add(new XYChart.Data<>("CN", 390));

        revenueChart.getData().add(series);
    }

    private void loadScheduleStatusChart() {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
            new PieChart.Data("Đang chạy (45%)", 45),
            new PieChart.Data("Chờ khởi hành (25%)", 25),
            new PieChart.Data("Đã hoàn thành (20%)", 20),
            new PieChart.Data("Tạm hoãn (10%)", 10)
        );

        scheduleStatusChart.setData(pieChartData);
        scheduleStatusChart.setLegendVisible(true);
    }

    private void loadRecentActivities() {
        ObservableList<String> activities = FXCollections.observableArrayList(
            "Tuyến đường TD024 vừa được thêm mới - 10 phút trước",
            "Lịch trình LT_HaNoi_SaiGon_001 đã khởi hành - 25 phút trước",
            "Tàu SE8 hoàn thành hành trình HN-SG - 1 giờ trước",
            "Lịch trình LT_DaNang_NhaTrang_005 bị trễ 15 phút - 2 giờ trước",
            "Nhân viên Nguyễn Văn A vừa cập nhật hồ sơ - 3 giờ trước",
            "Hóa đơn HD001234 đã được thanh toán - 4 giờ trước",
            "Tàu SE3 đang trong quá trình bảo trì - 5 giờ trước",
            "Báo cáo doanh thu tháng 9 đã được tạo - 1 ngày trước"
        );

        activitiesList.setItems(activities);
    }
}