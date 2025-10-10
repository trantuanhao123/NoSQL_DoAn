package DAO;

import KetNoiCSDL.KetNoICSDL;
import MODELS.*;
import com.datastax.oss.driver.api.core.CqlSession;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

public class OrderService {

    private final OrderByCustomerDAO orderByCustomerDAO;
    private final OrderByIdDAO orderByIdDAO;
    private final LoyaltyAccountDAO loyaltyDAO;

    // Định nghĩa ngưỡng xét hạng (Đặt ở đây hoặc Config file là tốt nhất)
    private static final BigDecimal SILVER_THRESHOLD_SPENT = BigDecimal.valueOf(5_000_000);
    private static final int SILVER_THRESHOLD_ORDERS = 3;
    
    private static final BigDecimal GOLD_THRESHOLD_SPENT = BigDecimal.valueOf(15_000_000);
    private static final int GOLD_THRESHOLD_ORDERS = 10;

    public OrderService(CqlSession session) {
        this.orderByCustomerDAO = new OrderByCustomerDAO(session);
        this.orderByIdDAO = new OrderByIdDAO(session);
        this.loyaltyDAO = new LoyaltyAccountDAO(session);
    }

    /**
     * ------------------------- CREATE -------------------------
     */
    public void createOrder(UUID customerId, List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Danh sách sản phẩm không được rỗng.");
        }

        UUID orderId = UUID.randomUUID();
        Instant now = Instant.now();

        // 1️⃣ Tính tổng tiền
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem item : items) {
            total = total.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        // 2️⃣ Xác định khóa phân vùng "yyyy_MM"
        ZonedDateTime zdt = now.atZone(ZoneId.systemDefault());
        String yearMonth = String.format("%d_%02d", zdt.getYear(), zdt.getMonthValue());

        // 3️⃣ Ghi vào orders_by_customer
        OrderByCustomer orderByCustomer = new OrderByCustomer(
                customerId, yearMonth, now, orderId, total, items, "COMPLETED"
        );
        orderByCustomerDAO.save(orderByCustomer);

        // 4️⃣ Ghi vào orders_by_id
        OrderById orderById = new OrderById(orderId, customerId, now, total, items, "COMPLETED");
        orderByIdDAO.save(orderById);

        // 5️⃣ Cập nhật điểm tích lũy
        updateLoyaltyAccount(customerId, total);

        System.out.println("✅ Tạo đơn hàng thành công: " + orderId);
    }
    
    // ... (Các phương thức READ và UPDATE không thay đổi) ...
    
    /**
     * ------------------------- READ -------------------------
     */
    public List<OrderByCustomer> getOrdersByCustomer(UUID customerId) {
        return orderByCustomerDAO.findByCustomer(customerId);
    }

    public List<OrderByCustomer> getOrdersByCustomerAndMonth(UUID customerId, String yyyyMM) {
        return orderByCustomerDAO.findByCustomerAndYearMonth(customerId, yyyyMM);
    }

    public OrderById getOrderById(UUID orderId) {
        return orderByIdDAO.findById(orderId);
    }

    /**
     * ------------------------- UPDATE -------------------------
     */
    public void updateOrderStatus(UUID orderId, String status) {
        OrderById order = orderByIdDAO.findById(orderId);
        if (order == null) {
            System.out.println("⚠️ Không tìm thấy đơn hàng: " + orderId);
            return;
        }

        // Xử lý logic hoàn tiền/hủy đơn hàng: Nếu trạng thái mới là 'CANCELLED'
        // và trạng thái cũ là 'COMPLETED', ta cần rollback Loyalty.
        if (status.equalsIgnoreCase("CANCELLED") && order.getStatus().equalsIgnoreCase("COMPLETED")) {
            rollbackLoyaltyAccount(order.getCustomerId(), order.getTotal());
            System.out.println("🔄 Đã hoàn tác Loyalty do hủy đơn hàng " + orderId);
        }
        // *Chú ý: Logic phức tạp hơn cần xét đến các trạng thái khác*

        // Cập nhật trong orders_by_id
        order.setStatus(status);
        orderByIdDAO.update(order);

        // Cập nhật trong orders_by_customer
        Instant orderDate = order.getOrderDate();
        ZonedDateTime zdt = orderDate.atZone(ZoneId.systemDefault());
        String yearMonth = String.format("%d_%02d", zdt.getYear(), zdt.getMonthValue());

        OrderByCustomer orderByCustomer = new OrderByCustomer(
                order.getCustomerId(),
                yearMonth,
                orderDate,
                orderId,
                order.getTotal(),
                order.getItems(),
                status
        );
        orderByCustomerDAO.update(orderByCustomer);

        System.out.println("✅ Đã cập nhật trạng thái đơn hàng " + orderId + " → " + status);
    }

    /**
     * ------------------------- DELETE -------------------------
     */
    public void deleteOrder(UUID orderId) {
        try {
            // 1️⃣ Lấy đơn hàng từ orders_by_id để lấy thông tin cần thiết
            OrderById order = orderByIdDAO.findById(orderId);
            if (order == null) {
                System.out.println("⚠️ Không tìm thấy order: " + orderId);
                return;
            }
            
            // Lấy thông tin cần thiết để xóa và hoàn tác
            UUID customerId = order.getCustomerId();
            BigDecimal orderTotal = order.getTotal();
            Instant orderDate = order.getOrderDate();

            // 2️⃣ Xóa khỏi orders_by_id
            orderByIdDAO.delete(orderId);

            // 3️⃣ Tính lại partition key `yyyy_mm`
            ZonedDateTime zdt = orderDate.atZone(ZoneId.systemDefault());
            String yearMonth = String.format("%d_%02d", zdt.getYear(), zdt.getMonthValue());

            // 4️⃣ Xóa khỏi orders_by_customer
            orderByCustomerDAO.delete(
                    customerId,
                    yearMonth,
                    orderDate,
                    orderId
            );

            // 5️⃣ HOÀN TÁC LOYALTY (Chỉ hoàn tác nếu đơn hàng đã được tính điểm)
            // Giả sử đơn hàng có trạng thái "COMPLETED" mới được tính điểm/chi tiêu
            // Vì OrderService tạo đơn hàng với status "COMPLETED", ta mặc định hoàn tác.
            if (order.getStatus().equalsIgnoreCase("COMPLETED")) {
                 rollbackLoyaltyAccount(customerId, orderTotal);
            }

            System.out.println("✅ Đã xóa order " + orderId + " khỏi cả hai bảng.");

        } catch (Exception e) {
            System.err.println("❌ Lỗi khi xóa order: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * ------------------------- LOYALTY LOGIC -------------------------
     */

    /**
     * Cập nhật LoyaltyAccount khi có đơn hàng mới (Tăng điểm, chi tiêu, count).
     */
    private void updateLoyaltyAccount(UUID customerId, BigDecimal orderTotal) {
        LoyaltyAccount acc = loyaltyDAO.findByCustomer(customerId);
        Instant now = Instant.now();
        
        // 1. Tính toán giá trị mới
        long newPoints = orderTotal.longValue();
        BigDecimal newLifetimeSpent = orderTotal;
        int newOrderCount = 1;

        if (acc != null) {
            newOrderCount = acc.getOrderCount() + 1;
            newLifetimeSpent = acc.getLifetimeSpent().add(orderTotal);
            newPoints = acc.getPoints() + orderTotal.longValue();
        }

        // 2. Xét hạng thẻ
        String newTier = determineTier(newLifetimeSpent, newOrderCount);
        
        // 3. Chuẩn bị đối tượng và lưu
        LoyaltyAccount accountToSave = (acc == null) 
            ? new LoyaltyAccount(customerId, newPoints, newTier, newLifetimeSpent, newOrderCount, now)
            : acc;
        
        if (acc != null) {
            accountToSave.setPoints(newPoints);
            accountToSave.setLifetimeSpent(newLifetimeSpent);
            accountToSave.setOrderCount(newOrderCount);
            accountToSave.setTier(newTier);
            accountToSave.setLastUpdated(now);
        }
        
        loyaltyDAO.save(accountToSave);
        System.out.println("💳 Cập nhật LoyaltyAccount cho khách " + customerId + " → Hạng: " + newTier);
    }
    
    /**
     * HOÀN TÁC LoyaltyAccount khi hủy hoặc xóa đơn hàng (Giảm điểm, chi tiêu, count).
     */
    private void rollbackLoyaltyAccount(UUID customerId, BigDecimal orderTotal) {
        LoyaltyAccount acc = loyaltyDAO.findByCustomer(customerId);

        if (acc == null) {
            System.err.println("❌ Lỗi rollback: Không tìm thấy Loyalty Account cho khách hàng " + customerId);
            return;
        }
        
        // 1. Tính toán giá trị mới (Giảm đi)
        long newPoints = acc.getPoints() - orderTotal.longValue();
        BigDecimal newLifetimeSpent = acc.getLifetimeSpent().subtract(orderTotal);
        int newOrderCount = acc.getOrderCount() - 1;
        
        // Đảm bảo không có giá trị âm
        newPoints = Math.max(0, newPoints);
        newLifetimeSpent = newLifetimeSpent.max(BigDecimal.ZERO);
        newOrderCount = Math.max(0, newOrderCount);
        
        // 2. Xét hạng thẻ mới (sau khi rollback)
        String newTier = determineTier(newLifetimeSpent, newOrderCount);

        // 3. Cập nhật và lưu
        acc.setPoints(newPoints);
        acc.setLifetimeSpent(newLifetimeSpent);
        acc.setOrderCount(newOrderCount);
        acc.setTier(newTier);
        acc.setLastUpdated(Instant.now());
        
        loyaltyDAO.save(acc);
        System.out.println("➖ Đã hoàn tác Loyalty cho khách " + customerId + " → Hạng mới: " + newTier);
    }

    /**
     * Logic xét hạng thẻ chung.
     */
    private String determineTier(BigDecimal totalSpent, int orderCount) {
        String newTier;
        
        // 1. Xét Hạng Vàng (Gold)
        if (totalSpent.compareTo(GOLD_THRESHOLD_SPENT) >= 0 && orderCount >= GOLD_THRESHOLD_ORDERS) {
            newTier = "Gold";
        } 
        // 2. Xét Hạng Bạc (Silver)
        else if (totalSpent.compareTo(SILVER_THRESHOLD_SPENT) >= 0 && orderCount >= SILVER_THRESHOLD_ORDERS) {
            newTier = "Silver";
        } 
        // 3. Hạng Đồng (Bronze)
        else {
            newTier = "Bronze";
        }
        return newTier;
    }
}