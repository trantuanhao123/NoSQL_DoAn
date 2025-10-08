package DAO;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Utility class để tạo các biểu đồ và thành phần UI cho thống kê
 * @author HAO
 */
public class ChartUtils {

    /**
     * Tạo biểu đồ đường (Line Chart)
     */
    public static ChartPanel createLineChart(String title, String xAxisLabel, String yAxisLabel, Map<String, BigDecimal> data) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        for (Map.Entry<String, BigDecimal> entry : data.entrySet()) {
            dataset.addValue(entry.getValue(), "Doanh thu", entry.getKey());
        }
        
        JFreeChart chart = ChartFactory.createLineChart(
            title,
            xAxisLabel,
            yAxisLabel,
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        );
        
        // Tùy chỉnh màu sắc
        chart.setBackgroundPaint(Color.WHITE);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(new Color(245, 245, 245));
        plot.setDomainGridlinePaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.WHITE);
        
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(500, 300));
        return chartPanel;
    }

    /**
     * Tạo biểu đồ cột (Bar Chart)
     */
    public static ChartPanel createBarChart(String title, String xAxisLabel, String yAxisLabel, List<Map<String, Object>> data) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        for (Map<String, Object> item : data) {
            String name = (String) item.get("name");
            Number value = (Number) item.get("value");
            dataset.addValue(value, "Số lượng", name);
        }
        
        JFreeChart chart = ChartFactory.createBarChart(
            title,
            xAxisLabel,
            yAxisLabel,
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        );
        
        // Tùy chỉnh màu sắc
        chart.setBackgroundPaint(Color.WHITE);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(new Color(245, 245, 245));
        plot.setDomainGridlinePaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.WHITE);
        
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(350, 250));
        return chartPanel;
    }

    /**
     * Tạo biểu đồ cột cho doanh thu (Bar Chart for Revenue)
     */
    public static ChartPanel createRevenueBarChart(String title, String xAxisLabel, String yAxisLabel, Map<String, BigDecimal> data) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        for (Map.Entry<String, BigDecimal> entry : data.entrySet()) {
            dataset.addValue(entry.getValue(), "Doanh thu", entry.getKey());
        }
        
        JFreeChart chart = ChartFactory.createBarChart(
            title,
            xAxisLabel,
            yAxisLabel,
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        );
        
        // Tùy chỉnh màu sắc và hiển thị đẹp hơn
        chart.setBackgroundPaint(Color.WHITE);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(new Color(230, 230, 230));
        plot.setRangeGridlinePaint(new Color(230, 230, 230));
        plot.setOutlineVisible(false);
        
        // Màu gradient cho cột doanh thu
        plot.getRenderer().setSeriesPaint(0, new Color(39, 174, 96));
        
        // Format trục Y để hiển thị số thay vì ký hiệu khoa học
        org.jfree.chart.axis.NumberAxis rangeAxis = (org.jfree.chart.axis.NumberAxis) plot.getRangeAxis();
        java.text.DecimalFormat df = new java.text.DecimalFormat("#,###");
        rangeAxis.setNumberFormatOverride(df);
        
        // Hiển thị giá trị trên cột
        org.jfree.chart.renderer.category.BarRenderer renderer = 
            (org.jfree.chart.renderer.category.BarRenderer) plot.getRenderer();
        renderer.setDefaultItemLabelGenerator(new org.jfree.chart.labels.StandardCategoryItemLabelGenerator());
        renderer.setDefaultItemLabelsVisible(true);
        
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 400));
        return chartPanel;
    }

    /**
     * Tạo biểu đồ cột cho số liệu (Bar Chart for Count)
     */
    public static ChartPanel createCountBarChart(String title, String xAxisLabel, String yAxisLabel, Map<String, Long> data) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        for (Map.Entry<String, Long> entry : data.entrySet()) {
            dataset.addValue(entry.getValue(), "Số lượng", entry.getKey());
        }
        
        JFreeChart chart = ChartFactory.createBarChart(
            title,
            xAxisLabel,
            yAxisLabel,
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        );
        
        // Tùy chỉnh màu sắc
        chart.setBackgroundPaint(Color.WHITE);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(new Color(245, 245, 245));
        plot.setDomainGridlinePaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.WHITE);
        
        // Màu cho cột
        plot.getRenderer().setSeriesPaint(0, new Color(230, 126, 34));
        
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(500, 300));
        return chartPanel;
    }

    /**
     * Tạo biểu đồ tròn (Pie Chart)
     */
    public static ChartPanel createPieChart(String title, Map<String, Long> data) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        
        for (Map.Entry<String, Long> entry : data.entrySet()) {
            dataset.setValue(entry.getKey(), entry.getValue());
        }
        
        JFreeChart chart = ChartFactory.createPieChart(
            title,
            dataset,
            true,
            true,
            false
        );
        
        // Tùy chỉnh màu sắc và hiển thị đẹp hơn
        chart.setBackgroundPaint(Color.WHITE);
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlinePaint(Color.WHITE);
        plot.setSectionOutlinesVisible(true);
        
        // Màu sắc đẹp cho các slice
        plot.setSectionPaint("Pending", new Color(241, 196, 15));
        plot.setSectionPaint("Processing", new Color(52, 152, 219));
        plot.setSectionPaint("Completed", new Color(46, 204, 113));
        plot.setSectionPaint("Cancelled", new Color(231, 76, 60));
        plot.setSectionPaint("Regular", new Color(149, 165, 166));
        plot.setSectionPaint("Silver", new Color(189, 195, 199));
        plot.setSectionPaint("Gold", new Color(241, 196, 15));
        plot.setSectionPaint("Platinum", new Color(155, 89, 182));
        
        // Hiển thị % cho mỗi slice
        plot.setLabelGenerator(new org.jfree.chart.labels.StandardPieSectionLabelGenerator(
            "{0}: {2}", java.text.NumberFormat.getNumberInstance(), 
            java.text.NumberFormat.getPercentInstance()
        ));
        
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(400, 320));
        return chartPanel;
    }

    /**
     * Tạo thẻ thống kê (Stats Card)
     */
    public static JPanel createStatsCard(String title, String value, String subtitle, Color backgroundColor) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(backgroundColor);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        card.setPreferredSize(new Dimension(200, 100));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        titleLabel.setForeground(new Color(100, 100, 100));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 24));
        valueLabel.setForeground(new Color(50, 50, 50));
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        if (!subtitle.isEmpty()) {
            JLabel subtitleLabel = new JLabel(subtitle);
            subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 10));
            subtitleLabel.setForeground(new Color(150, 150, 150));
            subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            card.add(subtitleLabel);
        }

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(valueLabel);

        return card;
    }

    /**
     * Format số tiền VNĐ
     */
    public static String formatMoney(BigDecimal amount) {
        if (amount == null) return "0";
        
        // Chuyển đổi thành triệu
        double millions = amount.doubleValue() / 1000000;
        
        if (millions >= 1000) {
            // Nếu >= 1 tỷ, hiển thị tỷ
            double billions = millions / 1000;
            return String.format("%.1f tỷ", billions);
        } else if (millions >= 1) {
            // Nếu >= 1 triệu, hiển thị triệu
            return String.format("%.1f triệu", millions);
        } else if (amount.doubleValue() >= 1000) {
            // Dưới 1 triệu nhưng >= 1000, hiển thị nghìn
            double thousands = amount.doubleValue() / 1000;
            return String.format("%.0f nghìn", thousands);
        } else {
            // Dưới 1000, hiển thị đầy đủ
            return String.format("%.0f", amount.doubleValue());
        }
    }

    /**
     * Format số lượng
     */
    public static String formatNumber(Long number) {
        if (number == null) return "0";
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(number);
    }
}