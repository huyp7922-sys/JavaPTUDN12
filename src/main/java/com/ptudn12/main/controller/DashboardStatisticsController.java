package com.ptudn12.main.controller;

import com.ptudn12.main.dao.DashboardDAO;
import com.ptudn12.main.entity.DailyRevenue;
import com.ptudn12.main.entity.ScheduleStatus;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class DashboardStatisticsController {

    
    @FXML private Label welcomeSubtitle;
    @FXML private Label totalRoutesLabel;
    @FXML private Label todaySchedulesLabel;
    @FXML private Label totalTrainsLabel;
    @FXML private Label revenueLabel;
    
    @FXML private BarChart<String, Number> revenueChart;
    @FXML private PieChart scheduleStatusChart;
    @FXML private ListView<String> activitiesList;

    private DashboardDAO dashboardDAO;

    @FXML
    public void initialize() {
        dashboardDAO = new DashboardDAO();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm, dd/MM/yyyy");
        welcomeSubtitle.setText("Dưới đây là tổng quan hệ thống - Cập nhật lúc " + 
                               LocalDateTime.now().format(formatter));

        loadStatistics();
        loadRevenueChart();
        loadScheduleStatusChart();
        loadRecentActivities();
    }

    private void loadStatistics() {
        try {
            int totalRoutes = dashboardDAO.getTotalRoutes();
            int todaySchedules = dashboardDAO.getTodaySchedules();
            int totalTrains = dashboardDAO.getTotalTrains();
            long monthlyRevenue = dashboardDAO.getMonthlyRevenue();

            totalRoutesLabel.setText(String.valueOf(totalRoutes));
            todaySchedulesLabel.setText(String.valueOf(todaySchedules));
            totalTrainsLabel.setText(String.valueOf(totalTrains));
            
            // Format revenue to display in billions or millions
            revenueLabel.setText(formatCurrency(monthlyRevenue));
        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi khi tải thống kê: " + e.getMessage());
        }
    }

    private void loadRevenueChart() {
        try {
            List<DailyRevenue> dailyRevenues = dashboardDAO.getDailyRevenueLastWeek();
            
            System.out.println("=== DEBUG: Doanh thu 7 ngày ===");
            System.out.println("Số ngày: " + dailyRevenues.size());
            
            revenueChart.getData().clear();
            
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Doanh thu (triệu đồng)");

            for (DailyRevenue dr : dailyRevenues) {
                // Format ngày dd-MM
                String dateLabel = dr.getNgay().substring(5); // Lấy MM-dd từ yyyy-MM-dd
                
                // Convert to millions for display
                double revenue = dr.getDoanhThu() / 1_000_000.0;
                System.out.println(dr.getNgay() + " (" + dateLabel + ") -> " + revenue + " triệu");
                
                XYChart.Data<String, Number> data = new XYChart.Data<>(dateLabel, revenue);
                series.getData().add(data);
            }

            revenueChart.getData().add(series);
            
            // Force chart to show all categories
            revenueChart.setAnimated(false);
            revenueChart.layout();
            
        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi khi tải biểu đồ doanh thu: " + e.getMessage());
        }
    }

    private void loadScheduleStatusChart() {
        try {
            List<ScheduleStatus> statuses = dashboardDAO.getScheduleStatus();
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
            
            for (ScheduleStatus status : statuses) {
                String viName = status.getTrangThaiVi();
                int count = status.getSoLuong();
                pieChartData.add(new PieChart.Data(viName + " (" + count + ")", count));
            }

            scheduleStatusChart.setData(pieChartData);
            scheduleStatusChart.setLegendVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi khi tải biểu đồ trạng thái: " + e.getMessage());
        }
    }

    private void loadRecentActivities() {
        try {
            List<String> activities = dashboardDAO.getRecentActivities();
            ObservableList<String> activityList = FXCollections.observableArrayList(activities);
            activitiesList.setItems(activityList);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi khi tải hoạt động gần đây: " + e.getMessage());
        }
    }

    private String formatCurrency(long amount) {
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        
        if (amount >= 1_000_000_000) {
            return String.format("%.1f tỷ", amount / 1_000_000_000.0);
        } else if (amount >= 1_000_000) {
            return String.format("%.1f tr", amount / 1_000_000.0);
        } else {
            return nf.format(amount);
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}