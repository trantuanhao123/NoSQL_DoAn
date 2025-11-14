package PANELS;

import DAO.ChartUtils;
import SERVICE.ThongKeService;
import org.jfree.chart.ChartPanel;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.Map;

public class ThongKe extends javax.swing.JPanel {
    
    private ThongKeService thongKeService;
    private JTabbedPane tabbedPane;
    private JPanel dashboardPanel;
    private JButton btnRefresh;

    public ThongKe() {
        initComponents();
        initCustomComponents();
        loadData();
    }
    
    private void initCustomComponents() {
        this.thongKeService = new ThongKeService();
        this.setLayout(new BorderLayout());
        
        // Header panel với nút refresh
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        headerPanel.setBackground(new Color(52, 73, 94));
        
        btnRefresh = new JButton("Làm mới");
        btnRefresh.setFont(new Font("Arial", Font.BOLD, 12));
        btnRefresh.setBackground(new Color(52, 152, 219));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.addActionListener(e -> loadData());
        
        headerPanel.add(btnRefresh);
        this.add(headerPanel, BorderLayout.NORTH);
        
        // Tabbed Pane chính
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 12));
        
        // Tab 1: Dashboard tổng quan
        createDashboardTab();
        
        // Tab 2: Biểu đồ doanh thu
        createRevenueChartTab();
        
        // Tab 3: Biểu đồ đơn hàng
        createOrderChartTab();
        
        // Tab 4: Biểu đồ khách hàng
        createCustomerChartTab();
        
        this.add(tabbedPane, BorderLayout.CENTER);
    }
    
    private void createDashboardTab() {
        dashboardPanel = new JPanel(new GridBagLayout());
        dashboardPanel.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Tiêu đề
        JLabel titleLabel = new JLabel("TỔNG QUAN HỆ THỐNG", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(52, 73, 94));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 4;
        dashboardPanel.add(titleLabel, gbc);
        
        tabbedPane.addTab("Tổng quan", dashboardPanel);
    }
    
    private void createRevenueChartTab() {
        JPanel revenuePanel = new JPanel(new BorderLayout(10, 10));
        revenuePanel.setBackground(new Color(248, 249, 250));
        revenuePanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Header panel với gradient
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(39, 174, 96));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("BIỂU ĐỒ DOANH THU", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        
        revenuePanel.add(headerPanel, BorderLayout.NORTH);
        
        tabbedPane.addTab("Doanh thu", revenuePanel);
    }
    
    private void createOrderChartTab() {
        JPanel orderPanel = new JPanel(new BorderLayout(10, 10));
        orderPanel.setBackground(new Color(248, 249, 250));
        orderPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Header panel với màu cam
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(230, 126, 34));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("THỐNG KÊ ĐƠN HÀNG", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        
        orderPanel.add(headerPanel, BorderLayout.NORTH);
        
        tabbedPane.addTab("Đơn hàng", orderPanel);
    }
    
    private void createCustomerChartTab() {
        JPanel customerPanel = new JPanel(new BorderLayout(10, 10));
        customerPanel.setBackground(new Color(248, 249, 250));
        customerPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Header panel với màu tím
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(155, 89, 182));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("THỐNG KÊ KHÁCH HÀNG", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        
        customerPanel.add(headerPanel, BorderLayout.NORTH);
        
        tabbedPane.addTab("Khách hàng", customerPanel);
    }
    
    private void loadData() {
        thongKeService.clearCache();
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                loadDashboardData();
                loadRevenueChart();
                loadOrderCharts();
                loadCustomerCharts();
                return null;
            }
            
            @Override
            protected void done() {
                if (btnRefresh != null) {
                    btnRefresh.setText("Làm mới");
                    btnRefresh.setEnabled(true);
                }
            }
        };
        
        if (btnRefresh != null) {
            btnRefresh.setText("⏳ Đang tải...");
            btnRefresh.setEnabled(false);
        }
        worker.execute();
    }
    
    private void loadDashboardData() {
        SwingUtilities.invokeLater(() -> {
            try {
                Map<String, Object> stats = thongKeService.getTongQuan();
                
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.insets = new Insets(10, 10, 10, 10);
                gbc.gridy = 2;
                
                // Clear existing cards (keep title)
                Component[] components = dashboardPanel.getComponents();
                for (int i = components.length - 1; i >= 1; i--) {
                    dashboardPanel.remove(components[i]);
                }
                
                // Tạo các thẻ thống kê
                JPanel card1 = ChartUtils.createStatsCard("Tổng khách hàng", 
                    ChartUtils.formatNumber((Long) stats.get("totalCustomers")), "", 
                    new Color(52, 152, 219, 30));
                gbc.gridx = 0;
                dashboardPanel.add(card1, gbc);
                
                JPanel card2 = ChartUtils.createStatsCard("Tổng sản phẩm", 
                    ChartUtils.formatNumber((Long) stats.get("totalProducts")), "", 
                    new Color(46, 204, 113, 30));
                gbc.gridx = 1;
                dashboardPanel.add(card2, gbc);
                
                JPanel card3 = ChartUtils.createStatsCard("Tổng đơn hàng", 
                    ChartUtils.formatNumber((Long) stats.get("totalOrders")), "", 
                    new Color(155, 89, 182, 30));
                gbc.gridx = 2;
                dashboardPanel.add(card3, gbc);
                
                JPanel card4 = ChartUtils.createStatsCard("Tổng doanh thu", 
                    ChartUtils.formatMoney((BigDecimal) stats.get("totalRevenue")), "", 
                    new Color(230, 126, 34, 30));
                gbc.gridx = 3;
                dashboardPanel.add(card4, gbc);
                
                dashboardPanel.revalidate();
                dashboardPanel.repaint();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi khi tải dữ liệu tổng quan: " + e.getMessage());
            }
        });
    }
    
    private void loadRevenueChart() {
        SwingUtilities.invokeLater(() -> {
            try {
                Map<String, BigDecimal> revenueData = thongKeService.getDoanhThuTheoThang();
                
                // Sử dụng bar chart thay vì line chart để đẹp hơn
                ChartPanel barChart = ChartUtils.createRevenueBarChart(
                    "Doanh thu theo tháng", 
                    "Tháng", 
                    "Doanh thu (VNĐ)", 
                    revenueData
                );
                
                JPanel revenuePanel = (JPanel) tabbedPane.getComponentAt(1);
                revenuePanel.removeAll();
                
                // Tạo lại header panel
                JPanel headerPanel = new JPanel();
                headerPanel.setBackground(new Color(39, 174, 96));
                headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                
                JLabel titleLabel = new JLabel("BIỂU ĐỒ DOANH THU", JLabel.CENTER);
                titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
                titleLabel.setForeground(Color.WHITE);
                headerPanel.add(titleLabel);
                
                revenuePanel.add(headerPanel, BorderLayout.NORTH);
                
                // Tạo panel cho chart với padding đẹp
                JPanel chartContainer = new JPanel(new BorderLayout());
                chartContainer.setBackground(Color.WHITE);
                chartContainer.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                    BorderFactory.createEmptyBorder(15, 15, 15, 15)
                ));
                chartContainer.add(barChart, BorderLayout.CENTER);
                
                revenuePanel.add(chartContainer, BorderLayout.CENTER);
                revenuePanel.revalidate();
                revenuePanel.repaint();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    
    private void loadOrderCharts() {
        SwingUtilities.invokeLater(() -> {
            try {
                Map<String, Long> orderStatusStats = thongKeService.getDonHangTheoTrangThai();
                Map<String, Long> monthlyOrderStats = thongKeService.getDonHangTheoThang();
                
                JPanel orderPanel = (JPanel) tabbedPane.getComponentAt(2);
                orderPanel.removeAll();
                
                // Tạo lại header panel
                JPanel headerPanel = new JPanel();
                headerPanel.setBackground(new Color(230, 126, 34));
                headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                
                JLabel titleLabel = new JLabel("THỐNG KÊ ĐƠN HÀNG", JLabel.CENTER);
                titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
                titleLabel.setForeground(Color.WHITE);
                headerPanel.add(titleLabel);
                
                orderPanel.add(headerPanel, BorderLayout.NORTH);
                
                // Tạo panel chính với 2 biểu đồ tròn
                JPanel chartsPanel = new JPanel(new GridLayout(1, 2, 20, 0));
                chartsPanel.setBackground(new Color(248, 249, 250));
                chartsPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
                
                // Biểu đồ tròn trạng thái đơn hàng
                ChartPanel statusChart = ChartUtils.createPieChart(
                    "Trạng thái đơn hàng",
                    orderStatusStats
                );
                JPanel statusContainer = new JPanel(new BorderLayout());
                statusContainer.setBackground(Color.WHITE);
                statusContainer.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                    BorderFactory.createEmptyBorder(15, 15, 15, 15)
                ));
                statusContainer.add(statusChart, BorderLayout.CENTER);
                
                // Biểu đồ tròn theo tháng (chuyển từ bar chart sang pie chart)
                ChartPanel monthlyChart = ChartUtils.createPieChart(
                    "Đơn hàng theo tháng",
                    monthlyOrderStats
                );
                JPanel monthlyContainer = new JPanel(new BorderLayout());
                monthlyContainer.setBackground(Color.WHITE);
                monthlyContainer.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                    BorderFactory.createEmptyBorder(15, 15, 15, 15)
                ));
                monthlyContainer.add(monthlyChart, BorderLayout.CENTER);
                
                chartsPanel.add(statusContainer);
                chartsPanel.add(monthlyContainer);
                
                orderPanel.add(chartsPanel, BorderLayout.CENTER);
                orderPanel.revalidate();
                orderPanel.repaint();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    
    private void loadCustomerCharts() {
        SwingUtilities.invokeLater(() -> {
            try {
                Map<String, Long> tierStats = thongKeService.getKhachHangTheoTier();
                Map<String, Long> orderStats = thongKeService.getDonHangTheoTrangThai();
                
                JPanel customerPanel = (JPanel) tabbedPane.getComponentAt(3);
                customerPanel.removeAll();
                
                // Tạo lại header panel
                JPanel headerPanel = new JPanel();
                headerPanel.setBackground(new Color(155, 89, 182));
                headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                
                JLabel titleLabel = new JLabel("THỐNG KÊ KHÁCH HÀNG", JLabel.CENTER);
                titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
                titleLabel.setForeground(Color.WHITE);
                headerPanel.add(titleLabel);
                
                customerPanel.add(headerPanel, BorderLayout.NORTH);
                
                // Tạo panel chính với 2 biểu đồ tròn
                JPanel chartsPanel = new JPanel(new GridLayout(1, 2, 20, 0));
                chartsPanel.setBackground(new Color(248, 249, 250));
                chartsPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
                
                // Biểu đồ tròn phân loại khách hàng
                ChartPanel tierChart = ChartUtils.createPieChart(
                    "Phân loại khách hàng",
                    tierStats
                );
                JPanel tierContainer = new JPanel(new BorderLayout());
                tierContainer.setBackground(Color.WHITE);
                tierContainer.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                    BorderFactory.createEmptyBorder(15, 15, 15, 15)
                ));
                tierContainer.add(tierChart, BorderLayout.CENTER);
                
                // Biểu đồ tròn trạng thái đơn hàng
                ChartPanel orderChart = ChartUtils.createPieChart(
                    "Trạng thái đơn hàng",
                    orderStats
                );
                JPanel orderContainer = new JPanel(new BorderLayout());
                orderContainer.setBackground(Color.WHITE);
                orderContainer.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                    BorderFactory.createEmptyBorder(15, 15, 15, 15)
                ));
                orderContainer.add(orderChart, BorderLayout.CENTER);
                
                chartsPanel.add(tierContainer);
                chartsPanel.add(orderContainer);
                
                customerPanel.add(chartsPanel, BorderLayout.CENTER);
                customerPanel.revalidate();
                customerPanel.repaint();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        // Keep empty - using custom components
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}