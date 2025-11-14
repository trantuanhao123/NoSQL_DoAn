package SERVICE; 

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import MODELS.*;
import KetNoiCSDL.KetNoICSDL;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

public class ThongKeService {
    private CqlSession session;
    private final ExecutorService executor;
    private final CacheManager cacheManager;

    // SỬA: Định nghĩa các trạng thái chuẩn bằng Tiếng Việt
    private static final String STATUS_HOAN_THANH = "Hoàn thành";
    private static final String STATUS_CHO_XU_LY = "Chờ xử lý";
    private static final String STATUS_DA_HUY = "Đã hủy";
    private static final String STATUS_KHAC = "Khác";

    public ThongKeService() {
        this.session = KetNoICSDL.getSession();
        this.executor = Executors.newFixedThreadPool(4);
        this.cacheManager = new CacheManager();
    }

    // ===== CACHE MANAGER (Không thay đổi) =====
    private static class CacheManager {
        private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();
        private static final long CACHE_DURATION_MS = 5 * 60 * 1000; // 5 minutes

        private static class CacheEntry {
            final Object data;
            final long timestamp;

            CacheEntry(Object data) {
                this.data = data;
                this.timestamp = System.currentTimeMillis();
            }

            boolean isExpired() {
                return System.currentTimeMillis() - timestamp > CACHE_DURATION_MS;
            }
        }

        @SuppressWarnings("unchecked")
        <T> T get(String key, Class<T> type) {
            CacheEntry entry = cache.get(key);
            if (entry != null && !entry.isExpired()) {
                return (T) entry.data;
            }
            return null;
        }

        void put(String key, Object data) {
            cache.put(key, new CacheEntry(data));
        }

        void clear() {
            cache.clear();
        }
    }
    
    // 1. Thống kê tổng quan (Không thay đổi logic, chỉ phụ thuộc hàm con)
    public Map<String, Object> getTongQuan() {
        String cacheKey = "tong_quan";
        Map<String, Object> cached = cacheManager.get(cacheKey, Map.class);
        if (cached != null) return cached;

        Map<String, Object> stats = new HashMap<>();

        try {
            CompletableFuture<Long> customersFuture = CompletableFuture.supplyAsync(
                () -> countTable("customers"), executor);

            CompletableFuture<Long> productsFuture = CompletableFuture.supplyAsync(
                () -> countTable("products"), executor);

            CompletableFuture<Map<String, Object>> ordersFuture = CompletableFuture.supplyAsync(
                () -> getOrderStatsOptimized(), executor);

            Long totalCustomers = customersFuture.get(10, TimeUnit.SECONDS);
            Long totalProducts = productsFuture.get(10, TimeUnit.SECONDS);
            Map<String, Object> orderStats = ordersFuture.get(10, TimeUnit.SECONDS);

            stats.put("totalCustomers", totalCustomers);
            stats.put("totalProducts", totalProducts);
            stats.put("totalOrders", orderStats.get("totalOrders"));
            stats.put("totalRevenue", orderStats.get("totalRevenue"));

            cacheManager.put(cacheKey, stats);

        } catch (Exception e) {
            System.err.println("Lỗi trong getTongQuan(): " + e.getMessage());
            e.printStackTrace();
            stats.put("totalCustomers", 0L);
            stats.put("totalProducts", 0L);
            stats.put("totalOrders", 0L);
            stats.put("totalRevenue", BigDecimal.ZERO);
        }

        return stats;
    }

    // Helper: Count table efficiently (Không thay đổi)
    private long countTable(String tableName) {
        try {
            ResultSet rs = session.execute("SELECT * FROM " + tableName);
            long count = 0;
            for (Row row : rs) {
                count++;
            }
            return count;
        } catch (Exception e) {
            System.err.println("Lỗi đếm bảng " + tableName + ": " + e.getMessage());
            return 0L;
        }
    }

    // SỬA: Chuẩn hóa trạng thái về Tiếng Việt
    // Chấp nhận cả "Completed" (từ code cũ) và "Hoàn thành" (từ CSDL)
    private String normalizeStatus(String status) {
        if (status == null) return STATUS_KHAC;
        String s = status.trim();

        // Nhóm 1: Hoàn thành
        if (s.equalsIgnoreCase("completed") || s.equalsIgnoreCase(STATUS_HOAN_THANH)) {
            return STATUS_HOAN_THANH;
        }
        
        // Nhóm 2: Chờ xử lý (Bao gồm Pending và Processing)
        if (s.equalsIgnoreCase("pending") || s.equalsIgnoreCase(STATUS_CHO_XU_LY) || s.equalsIgnoreCase("processing")) {
            return STATUS_CHO_XU_LY;
        }
        
        // Nhóm 3: Hủy
        if (s.equalsIgnoreCase("cancelled") || s.equalsIgnoreCase("canceled") || s.equalsIgnoreCase(STATUS_DA_HUY)) {
            return STATUS_DA_HUY;
        }
        
        // Nhóm 4: Các trạng thái khác
        if (s.isEmpty()) return STATUS_KHAC;
        
        // Trả về trạng thái lạ (nếu có)
        if (s.length() > 1) return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
        return s.toUpperCase();
    }

    // Helper: Get order stats optimized
    private Map<String, Object> getOrderStatsOptimized() {
        Map<String, Object> result = new HashMap<>();
        long totalOrders = 0;
        BigDecimal totalRevenue = BigDecimal.ZERO;

        try {
            ResultSet rs = session.execute("SELECT status, total FROM orders_by_id");

            for (Row row : rs) {
                totalOrders++;
                String status = normalizeStatus(row.getString("status"));
                BigDecimal orderTotal = row.getBigDecimal("total");

                // SỬA: Kiểm tra bằng trạng thái Tiếng Việt chuẩn
                if (STATUS_HOAN_THANH.equals(status) && orderTotal != null) {
                    totalRevenue = totalRevenue.add(orderTotal);
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi lấy thống kê đơn hàng: " + e.getMessage());
        }

        result.put("totalOrders", totalOrders);
        result.put("totalRevenue", totalRevenue);
        return result;
    }
    
    // 2. Doanh thu theo tháng
    public Map<String, BigDecimal> getDoanhThuTheoThang() {
        String cacheKey = "doanh_thu_thang";
        Map<String, BigDecimal> cached = cacheManager.get(cacheKey, Map.class);
        if (cached != null) return cached;

        Map<String, BigDecimal> doanhThu = new LinkedHashMap<>();

        try {
            LocalDate baseDate = LocalDate.now().withDayOfMonth(1);
            List<LocalDate> months = new ArrayList<>();
            for (int i = 11; i >= 0; i--) {
                months.add(baseDate.minusMonths(i));
            }

            boolean sameYear = months.stream().map(LocalDate::getYear).distinct().count() == 1;
            DateTimeFormatter labelFormatter = sameYear
                    ? DateTimeFormatter.ofPattern("MM")
                    : DateTimeFormatter.ofPattern("MM/yyyy");

            for (LocalDate thang : months) {
                String thangStr = thang.format(labelFormatter);
                doanhThu.put(thangStr, BigDecimal.ZERO);
            }

            ResultSet rs = session.execute("SELECT order_date, total, status FROM orders_by_id");

            for (Row row : rs) {
                try {
                    java.time.Instant orderInstant = row.get("order_date", java.time.Instant.class);
                    BigDecimal total = row.getBigDecimal("total");
                    String status = normalizeStatus(row.getString("status"));

                    // SỬA: Kiểm tra bằng trạng thái Tiếng Việt chuẩn
                    if (orderInstant != null && total != null && STATUS_HOAN_THANH.equals(status)) {
                        LocalDate localDate = orderInstant
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate();

                        String monthKey = localDate.format(labelFormatter);

                        if (doanhThu.containsKey(monthKey)) {
                            doanhThu.computeIfPresent(monthKey, (k, v) -> v.add(total));
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Lỗi xử lý row trong getDoanhThuTheoThang(): " + e.getMessage());
                }
            }
            cacheManager.put(cacheKey, doanhThu);
        } catch (Exception e) {
            System.err.println("Lỗi trong getDoanhThuTheoThang(): " + e.getMessage());
            e.printStackTrace();
        }
        return doanhThu;
    }
    
    // 3. Top 10 sản phẩm bán chạy
    public List<Map<String, Object>> getTopSanPhamBanChay() {
        String cacheKey = "top_san_pham";
        List<Map<String, Object>> cached = cacheManager.get(cacheKey, List.class);
        if (cached != null) return cached;

        List<Map<String, Object>> topProducts = new ArrayList<>();
        Map<String, Integer> productSales = new HashMap<>();

        try {
            // SỬA: Lấy từ orders_by_id để giảm tải (nếu CSDL của bạn có index)
            // Nếu orders_by_customer tốt hơn cho bạn, hãy đổi lại
            ResultSet rs = session.execute("SELECT items, status FROM orders_by_id"); 
                                            // hoặc "SELECT items, status FROM orders_by_customer"

            for (Row row : rs) {
                try {
                    String status = normalizeStatus(row.getString("status"));
                    // SỬA: Kiểm tra bằng trạng thái Tiếng Việt chuẩn
                    if (!STATUS_HOAN_THANH.equals(status)) continue;

                    List<com.datastax.oss.driver.api.core.data.UdtValue> items = row.getList("items", com.datastax.oss.driver.api.core.data.UdtValue.class);

                    if (items != null) {
                        for (com.datastax.oss.driver.api.core.data.UdtValue item : items) {
                            String model = item.getString("model");
                            Integer qty = item.getInt("qty");

                            if (model != null && qty != null) {
                                productSales.merge(model, qty, Integer::sum);
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Lỗi xử lý row trong getTopSanPhamBanChay(): " + e.getMessage());
                }
            }

            productSales.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .forEach(entry -> {
                    Map<String, Object> product = new HashMap<>();
                    product.put("name", entry.getKey());
                    product.put("value", entry.getValue());
                    topProducts.add(product);
                });

            cacheManager.put(cacheKey, topProducts);

        } catch (Exception e) {
            System.err.println("Lỗi trong getTopSanPhamBanChay(): " + e.getMessage());
            e.printStackTrace();
        }
        return topProducts;
    }
    
    // 4. Thống kê khách hàng theo tier
    public Map<String, Long> getKhachHangTheoTier() {
        String cacheKey = "khach_hang_tier";
        Map<String, Long> cached = cacheManager.get(cacheKey, Map.class);
        if (cached != null) return cached;

        Map<String, Long> tierStats = new HashMap<>();
        // SỬA: Chuẩn hóa tên Tier (nếu bạn dùng tiếng Việt ở OrderService)
        // Ví dụ: "Đồng", "Bạc", "Vàng". 
        // Tạm thời giữ nguyên nếu Tier vẫn là Tiếng Anh
        tierStats.put("Bronze", 0L);
        tierStats.put("Silver", 0L);
        tierStats.put("Gold", 0L);
        tierStats.put("No Tier", 0L);

        try {
            ResultSet rs = session.execute("SELECT tier FROM loyalty_accounts");

            for (Row row : rs) {
                String tier = row.getString("tier");
                if (tier == null || tier.isEmpty()) {
                    tier = "No Tier";
                }
                tierStats.merge(tier, 1L, Long::sum);
            }
            cacheManager.put(cacheKey, tierStats);
        } catch (Exception e) {
            System.err.println("Lỗi trong getKhachHangTheoTier(): " + e.getMessage());
            e.printStackTrace();
        }
        return tierStats;
    }
    
    // 5. Thống kê đơn hàng theo trạng thái
    public Map<String, Long> getDonHangTheoTrangThai() {
        String cacheKey = "don_hang_trang_thai";
        Map<String, Long> cached = cacheManager.get(cacheKey, Map.class);
        if (cached != null) return cached;

        Map<String, Long> statusStats = new HashMap<>();
        // SỬA: Khởi tạo Map bằng các key Tiếng Việt chuẩn
        statusStats.put(STATUS_HOAN_THANH, 0L);
        statusStats.put(STATUS_CHO_XU_LY, 0L);
        statusStats.put(STATUS_DA_HUY, 0L);

        try {
            ResultSet rs = session.execute("SELECT status FROM orders_by_id");

            for (Row row : rs) {
                String status = normalizeStatus(row.getString("status"));
                // Tự động gộp các trạng thái (bao gồm cả STATUS_KHAC nếu có)
                statusStats.merge(status, 1L, Long::sum);
            }

            cacheManager.put(cacheKey, statusStats);

        } catch (Exception e) {
            System.err.println("Lỗi trong getDonHangTheoTrangThai(): " + e.getMessage());
            e.printStackTrace();
        }
        return statusStats;
    }
    
    // 6. Thống kê sản phẩm theo thương hiệu (Không thay đổi)
    public Map<String, Long> getSanPhamTheoBrand() {
        String cacheKey = "san_pham_brand";
        Map<String, Long> cached = cacheManager.get(cacheKey, Map.class);
        if (cached != null) return cached;

        Map<String, Long> brandStats = new HashMap<>();

        try {
            ResultSet rs = session.execute("SELECT brand FROM products");

            for (Row row : rs) {
                String brand = row.getString("brand");
                if (brand == null || brand.isEmpty()) {
                    brand = "Unknown";
                }
                brandStats.merge(brand, 1L, Long::sum);
            }
            cacheManager.put(cacheKey, brandStats);
        } catch (Exception e) {
            System.err.println("Lỗi trong getSanPhamTheoBrand(): " + e.getMessage());
            e.printStackTrace();
        }
        return brandStats;
    }
    
    // 7. Doanh thu theo ngày (30 ngày gần nhất)
    public Map<String, BigDecimal> getDoanhThuTheoNgay() {
        String cacheKey = "doanh_thu_ngay";
        Map<String, BigDecimal> cached = cacheManager.get(cacheKey, Map.class);
        if (cached != null) return cached;

        Map<String, BigDecimal> dailyRevenue = new LinkedHashMap<>();

        try {
            LocalDate now = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            for (int i = 29; i >= 0; i--) {
                LocalDate date = now.minusDays(i);
                String dateStr = date.format(formatter);
                dailyRevenue.put(dateStr, BigDecimal.ZERO);
            }

            ResultSet rs = session.execute("SELECT order_date, total, status FROM orders_by_id");

            for (Row row : rs) {
                try {
                    java.time.Instant orderInstant = row.get("order_date", java.time.Instant.class);
                    BigDecimal total = row.getBigDecimal("total");
                    String status = normalizeStatus(row.getString("status"));

                    // SỬA: Kiểm tra bằng trạng thái Tiếng Việt chuẩn
                    if (orderInstant != null && total != null && STATUS_HOAN_THANH.equals(status)) {
                        LocalDate localDate = orderInstant
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate();

                        String dateKey = localDate.format(formatter);

                        if (dailyRevenue.containsKey(dateKey)) {
                            dailyRevenue.computeIfPresent(dateKey, (k, v) -> v.add(total));
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Lỗi xử lý row trong getDoanhThuTheoNgay(): " + e.getMessage());
                }
            }
            cacheManager.put(cacheKey, dailyRevenue);
        } catch (Exception e) {
            System.err.println("Lỗi trong getDoanhThuTheoNgay(): " + e.getMessage());
            e.printStackTrace();
        }
        return dailyRevenue;
    }
    
    // 8. Thống kê đơn hàng theo tháng (6 tháng gần nhất)
    public Map<String, Long> getDonHangTheoThang() {
        String cacheKey = "don_hang_thang";
        Map<String, Long> cached = cacheManager.get(cacheKey, Map.class);
        if (cached != null) return cached;

        Map<String, Long> monthlyOrders = new LinkedHashMap<>();

        try {
            LocalDate now = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yyyy");

            for (int i = 5; i >= 0; i--) {
                LocalDate thang = now.minusMonths(i);
                String thangStr = thang.format(formatter);
                monthlyOrders.put(thangStr, 0L);
            }

            ResultSet rs = session.execute("SELECT order_date FROM orders_by_id");

            for (Row row : rs) {
                try {
                    java.time.Instant orderInstant = row.get("order_date", java.time.Instant.class);

                    if (orderInstant != null) {
                        LocalDate localDate = orderInstant
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate();

                        String monthKey = localDate.format(formatter);

                        if (monthlyOrders.containsKey(monthKey)) {
                            monthlyOrders.merge(monthKey, 1L, Long::sum);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Lỗi xử lý row trong getDonHangTheoThang(): " + e.getMessage());
                }
            }
            cacheManager.put(cacheKey, monthlyOrders);
        } catch (Exception e) {
            System.err.println("Lỗi trong getDonHangTheoThang(): " + e.getMessage());
            e.printStackTrace();
        }
        return monthlyOrders;
    }
    
    // 9. Thống kê điểm tích lũy khách hàng (Không thay đổi)
    public Map<String, Long> getDiemTichLuyKhachHang() {
        String cacheKey = "diem_tich_luy";
        Map<String, Long> cached = cacheManager.get(cacheKey, Map.class);
        if (cached != null) return cached;

        Map<String, Long> loyaltyStats = new HashMap<>();
        loyaltyStats.put("0-1000 điểm", 0L);
        loyaltyStats.put("1000-5000 điểm", 0L);
        loyaltyStats.put("5000-10000 điểm", 0L);
        loyaltyStats.put("Trên 10000 điểm", 0L);

        try {
            ResultSet rs = session.execute("SELECT points FROM loyalty_accounts");

            for (Row row : rs) {
                Long points = row.getLong("points");
                if (points == null) points = 0L;

                String range;
                if (points < 1000) {
                    range = "0-1000 điểm";
                } else if (points < 5000) {
                    range = "1000-5000 điểm";
                } else if (points < 10000) {
                    range = "5000-10000 điểm";
                } else {
                    range = "Trên 10000 điểm";
                }
                loyaltyStats.merge(range, 1L, Long::sum);
            }
            cacheManager.put(cacheKey, loyaltyStats);
        } catch (Exception e) {
            System.err.println("Lỗi trong getDiemTichLuyKhachHang(): " + e.getMessage());
            e.printStackTrace();
        }
        return loyaltyStats;
    }
    
    // ===== RESOURCE MANAGEMENT (Không thay đổi) =====
    public void shutdown() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    System.err.println("Executor did not terminate gracefully, forcing shutdown");
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                System.err.println("Interrupted while waiting for executor shutdown: " + e.getMessage());
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
    
    // ===== CACHE MANAGEMENT (Không thay đổi) =====
    public void clearCache() {
        cacheManager.clear();
    }
    
    public void clearCache(String key) {
        cacheManager.clear();
    }
}