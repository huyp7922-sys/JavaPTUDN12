package com.ptudn12.main.controller;

import com.ptudn12.main.dao.DashboardDAO;
import com.ptudn12.main.entity.DailyRevenue;
import com.ptudn12.main.entity.ScheduleStatus;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;

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


    private final DashboardDAO dashboardDAO = new DashboardDAO();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

  
    @FXML
    public void initialize() {
        revenueChart.setAnimated(false); 
        refreshAllData();
    }

    @FXML
    private void handleRefresh() {
        refreshAllData();
    }

    private void refreshAllData() {

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy");
        welcomeSubtitle.setText("Cập nhật lúc: " + LocalDateTime.now().format(dtf));

        loadOverviewStatistics();
        loadRevenueChart();
        loadScheduleStatusChart();
        loadRecentActivities();
    }


    private void loadOverviewStatistics() {
        try {
            totalRoutesLabel.setText(String.valueOf(dashboardDAO.getTotalRoutes()));
            todaySchedulesLabel.setText(String.valueOf(dashboardDAO.getTodaySchedules()));
            totalTrainsLabel.setText(String.valueOf(dashboardDAO.getTotalTrains()));

            
            long monthlyRev = dashboardDAO.getMonthlyRevenue();
            revenueLabel.setText(formatCurrencyShort(monthlyRev));
            
        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi tải số liệu tổng quan!");
        }
    }


    private void loadRevenueChart() {
        try {

            revenueChart.getData().clear(); 

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Doanh thu ngày");

            List<DailyRevenue> dataList = dashboardDAO.getDailyRevenueLastWeek();

            for (DailyRevenue item : dataList) {
                String dateStr = item.getNgay(); 
                String label = dateStr.substring(8, 10) + "/" + dateStr.substring(5, 7);
                
                series.getData().add(new XYChart.Data<>(label, item.getDoanhThu()));
            }

            revenueChart.getData().add(series);

            for (XYChart.Data<String, Number> data : series.getData()) {
                Node node = data.getNode();

                double value = data.getYValue().doubleValue();
                
                String textMoney = currencyFormat.format(value);

                Tooltip tooltip = new Tooltip("Doanh thu: " + textMoney);
                tooltip.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-color: #2d3748; -fx-text-fill: white;");
                tooltip.setShowDelay(javafx.util.Duration.millis(100)); 
                Tooltip.install(node, tooltip);

                node.setOnMouseEntered(e -> {
                    node.setStyle("-fx-bar-fill: #2b6cb0; -fx-cursor: hand;"); 
                });
                
                node.setOnMouseExited(e -> {
                    node.setStyle(""); 
                });

            }

        } catch (Exception e) {
            e.printStackTrace();

            showError("Lỗi tải biểu đồ doanh thu!");

        }
    }

    private void loadScheduleStatusChart() {
        try {

            List<ScheduleStatus> list = dashboardDAO.getScheduleStatus();
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

            for (ScheduleStatus s : list) {
                pieData.add(new PieChart.Data(s.getTrangThaiVi() + " (" + s.getSoLuong() + ")", s.getSoLuong()));
            }

            scheduleStatusChart.setData(pieData);
            
            for (PieChart.Data data : scheduleStatusChart.getData()) {
                Node node = data.getNode();
                Tooltip tooltip = new Tooltip(data.getName() + ": " + (int)data.getPieValue());
                tooltip.setStyle("-fx-font-size: 13px;");
                Tooltip.install(node, tooltip);
                
                node.setOnMouseEntered(e -> node.setStyle("-fx-cursor: hand;"));
            }

        } catch (Exception e) {
            e.printStackTrace();

        }
    }

 
    private void loadRecentActivities() {
        try {

            List<String> acts = dashboardDAO.getRecentActivities();
            activitiesList.setItems(FXCollections.observableArrayList(acts));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private String formatCurrencyShort(long amount) {
        if (amount >= 1_000_000_000) {
            return String.format("%.2f Tỷ", amount / 1_000_000_000.0);
        } else if (amount >= 1_000_000) {
            return String.format("%.1f Tr", amount / 1_000_000.0);
        } else {
            return currencyFormat.format(amount);
        }
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi Hệ Thống");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
