package com.ptudn12.main.controller;

import com.ptudn12.main.dao.DashboardDAO;
import com.ptudn12.main.entity.DailyRevenue;
import com.ptudn12.main.entity.ScheduleStatus;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;

import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;

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

  
    @FXML
    public void initialize() {
        refreshAllData();
    }

    @FXML
    private void handleRefresh() {
        refreshAllData();
    }

    private void refreshAllData() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss - dd/MM/yyyy");
        welcomeSubtitle.setText("Cập nhật lần cuối: " + LocalDateTime.now().format(dtf));
        loadStatistics();
        loadRevenueChart();
        loadScheduleStatusChart();
        loadRecentActivities();
    }


    private void loadStatistics() {
        try {
            totalRoutesLabel.setText(String.valueOf(dashboardDAO.getTotalRoutes()));
            todaySchedulesLabel.setText(String.valueOf(dashboardDAO.getTodaySchedules()));
            totalTrainsLabel.setText(String.valueOf(dashboardDAO.getTotalTrains()));
            revenueLabel.setText(formatCurrency(dashboardDAO.getMonthlyRevenue()));
        } catch (Exception e) {
            e.printStackTrace();
            showError("Không thể tải thống kê tổng quan");
        }
    }


    private void loadRevenueChart() {
        try {
            revenueChart.getData().clear();

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Doanh thu (triệu đồng)");

            List<DailyRevenue> dailyRevenues = dashboardDAO.getDailyRevenueLastWeek();

            for (DailyRevenue dr : dailyRevenues) {
                String dateLabel =
                        dr.getNgay().substring(8) + "/" + dr.getNgay().substring(5, 7);

                double displayValue = dr.getDoanhThu() / 1_000_000.0;

                XYChart.Data<String, Number> data =
                        new XYChart.Data<>(dateLabel, displayValue);

                series.getData().add(data);
            }

            revenueChart.getData().add(series);

            for (XYChart.Data<String, Number> data : series.getData()) {
                Node node = data.getNode();

                long originalValue =
                        (long) (data.getYValue().doubleValue() * 1_000_000);

                String exactMoney =
                        NumberFormat.getCurrencyInstance(new Locale("vi", "VN"))
                                .format(originalValue);

                Tooltip tooltip = new Tooltip("Doanh thu: " + exactMoney);
                tooltip.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
                Tooltip.install(node, tooltip);

                node.setOnMouseEntered(e ->
                        node.setStyle("-fx-bar-fill: #2980b9; -fx-cursor: hand;"));
                node.setOnMouseExited(e ->
                        node.setStyle(""));
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Không thể tải biểu đồ doanh thu");
        }
    }

    private void loadScheduleStatusChart() {
        try {
            List<ScheduleStatus> statuses = dashboardDAO.getScheduleStatus();

            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

            for (ScheduleStatus s : statuses) {
                pieData.add(new PieChart.Data(
                        s.getTrangThaiVi() + " (" + s.getSoLuong() + ")",
                        s.getSoLuong()
                ));
            }

            scheduleStatusChart.setData(pieData);
            scheduleStatusChart.setLegendVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
            showError("Không thể tải biểu đồ trạng thái");
        }
    }

 
    private void loadRecentActivities() {
        try {

            activitiesList.setItems(
                    FXCollections.observableArrayList(
                            dashboardDAO.getRecentActivities()
                    )
            );
        } catch (Exception e) {
            e.printStackTrace();
            showError("Không thể tải hoạt động gần đây");
        }
    }

  
    private String formatCurrency(long amount) {
        if (amount >= 1_000_000_000)
            return String.format("%.2f Tỷ", amount / 1_000_000_000.0);
        if (amount >= 1_000_000)
            return String.format("%.1f Tr", amount / 1_000_000.0);
        return NumberFormat.getInstance(new Locale("vi", "VN"))
                .format(amount) + " đ";
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
