package PANELS;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.Map;
import java.util.List;

public class ChartUtils {
    
    // 1. Tạo biểu đồ đường (Line Chart) - Doanh thu theo thời gian
    public static ChartPanel createLineChart(String title, String xLabel, String yLabel, Map<String, BigDecimal> data) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        for (Map.Entry<String, BigDecimal> entry : data.entrySet()) {
            dataset.addValue(entry.getValue(), "Doanh thu", entry.getKey());
        }
        
        JFreeChart chart = ChartFactory.createLineChart(
            title,
            xLabel,
            yLabel,
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        );
        
        // Tùy chỉnh màu sắc
        chart.setBackgroundPaint(Color.WHITE);
        chart.getPlot().setBackgroundPaint(new Color(240, 248, 255));
        
        return new ChartPanel(chart);
    }
    
    // 2. Tạo biểu đồ cột (Bar Chart) - Top sản phẩm
    public static ChartPanel createBarChart(String title, String xLabel, String yLabel, List<Map<String, Object>> data) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        for (Map<String, Object> item : data) {
            String productName = (String) item.get("productName");
            Integer soLuong = (Integer) item.get("soLuongBan");
            
            // Rút gọn tên sản phẩm nếu quá dài
            String shortName = productName != null && productName.length() > 15 ? 
                              productName.substring(0, 15) + "..." : productName;
            
            dataset.addValue(soLuong != null ? soLuong : 0, "Số lượng bán", shortName);
        }
        
        JFreeChart chart = ChartFactory.createBarChart(
            title,
            xLabel,
            yLabel,
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        );
        
        // Tùy chỉnh màu sắc
        chart.setBackgroundPaint(Color.WHITE);
        chart.getPlot().setBackgroundPaint(new Color(248, 255, 240));
        
        return new ChartPanel(chart);
    }
    
    // 3. Tạo biểu đồ tròn (Pie Chart) - Tỷ lệ
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
        
        // Tùy chỉnh màu sắc
        chart.setBackgroundPaint(Color.WHITE);
        
        return new ChartPanel(chart);
    }
    
    // 4. Tạo biểu đồ cột ngang (Horizontal Bar Chart)
    public static ChartPanel createHorizontalBarChart(String title, String xLabel, String yLabel, Map<String, Long> data) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        for (Map.Entry<String, Long> entry : data.entrySet()) {
            dataset.addValue(entry.getValue(), "Số lượng", entry.getKey());
        }
        
        JFreeChart chart = ChartFactory.createBarChart(
            title,
            yLabel, // Đổi chỗ vì horizontal
            xLabel,
            dataset,
            PlotOrientation.HORIZONTAL, // Horizontal
            true,
            true,
            false
        );
        
        // Tùy chỉnh màu sắc  
        chart.setBackgroundPaint(Color.WHITE);
        chart.getPlot().setBackgroundPaint(new Color(255, 248, 240));
        
        return new ChartPanel(chart);
    }
    
    // 5. Tạo panel thống kê số (Dashboard cards)
    public static JPanel createStatsCard(String title, String value, String unit, Color bgColor) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createRaisedBevelBorder(),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        card.setBackground(bgColor);
        card.setPreferredSize(new Dimension(200, 100));
        
        // Tiêu đề
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 12));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setForeground(Color.DARK_GRAY);
        
        // Giá trị
        JLabel valueLabel = new JLabel(value + " " + unit);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 20));
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        valueLabel.setForeground(new Color(51, 51, 51));
        
        card.add(Box.createVerticalGlue());
        card.add(titleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 5)));
        card.add(valueLabel);
        card.add(Box.createVerticalGlue());
        
        return card;
    }
    
    // 6. Format số tiền VND
    public static String formatMoney(BigDecimal amount) {
        if (amount == null) return "0 VNĐ";
        
        long value = amount.longValue();
        if (value >= 1_000_000_000) {
            return String.format("%.1f tỷ", value / 1_000_000_000.0);
        } else if (value >= 1_000_000) {
            return String.format("%.1f triệu", value / 1_000_000.0);
        } else if (value >= 1_000) {
            return String.format("%.0f nghìn", value / 1_000.0);
        } else {
            return String.format("%,d VNĐ", value);
        }
    }
    
    // 7. Format số lượng
    public static String formatNumber(long number) {
        if (number >= 1_000_000) {
            return String.format("%.1fM", number / 1_000_000.0);
        } else if (number >= 1_000) {
            return String.format("%.1fK", number / 1_000.0);
        } else {
            return String.valueOf(number);
        }
    }
}