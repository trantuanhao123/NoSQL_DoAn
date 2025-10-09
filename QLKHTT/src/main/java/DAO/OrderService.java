package DAO;

import KetNoiCSDL.KetNoICSDL;
import MODELS.*;
import com.datastax.oss.driver.api.core.CqlSession;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * OrderService xử lý logic nghiệp vụ:
 * - Tạo đơn hàng (ghi vào cả orders_by_id & orders_by_customer)
 * - Cập nhật trạng thái đơn
 * - Xóa đơn hàng
 * - Cập nhật điểm khách hàng thân thiết
 */
public class OrderService {

    private final OrderByCustomerDAO orderByCustomerDAO;
    private final OrderByIdDAO orderByIdDAO;
    private final LoyaltyAccountDAO loyaltyDAO;

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
                customerId, yearMonth, now, orderId, total, items, "PENDING"
        );
        orderByCustomerDAO.save(orderByCustomer);

        // 4️⃣ Ghi vào orders_by_id
        OrderById orderById = new OrderById(orderId, customerId, now, total, items, "PENDING");
        orderByIdDAO.save(orderById);

        // 5️⃣ Cập nhật điểm tích lũy
        updateLoyaltyAccount(customerId, total);

        System.out.println("✅ Tạo đơn hàng thành công: " + orderId);
    }

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
            // 1️⃣ Lấy đơn hàng từ bảng orders_by_id
            OrderById order = orderByIdDAO.findById(orderId);
            if (order == null) {
                System.out.println("⚠️ Không tìm thấy order: " + orderId);
                return;
            }

            // 2️⃣ Xóa khỏi orders_by_id
            orderByIdDAO.delete(orderId);

            // 3️⃣ Tính lại partition key `yyyy_mm`
            Instant orderDate = order.getOrderDate();
            ZonedDateTime zdt = orderDate.atZone(ZoneId.systemDefault());
            String yearMonth = String.format("%d_%02d", zdt.getYear(), zdt.getMonthValue());

            // 4️⃣ Xóa khỏi orders_by_customer
            orderByCustomerDAO.delete(
                    order.getCustomerId(),
                    yearMonth,
                    order.getOrderDate(),
                    orderId
            );

            System.out.println("✅ Đã xóa order " + orderId + " khỏi cả hai bảng.");

        } catch (Exception e) {
            System.err.println("❌ Lỗi khi xóa order: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * ------------------------- LOYALTY -------------------------
     */
    private void updateLoyaltyAccount(UUID customerId, BigDecimal orderTotal) {
        LoyaltyAccount acc = loyaltyDAO.findByCustomer(customerId);
        Instant now = Instant.now();

        if (acc == null) {
            // Tạo mới nếu chưa có
            acc = new LoyaltyAccount(
                    customerId,
                    orderTotal.longValue(),
                    "Bronze",
                    orderTotal,
                    1,
                    now
            );
        } else {
            // Cập nhật thông tin hiện có
            acc.setOrderCount(acc.getOrderCount() + 1);
            acc.setLifetimeSpent(acc.getLifetimeSpent().add(orderTotal));
            acc.setPoints(acc.getPoints() + orderTotal.longValue());
            acc.setLastUpdated(now);

            // Xét hạng thẻ
            BigDecimal total = acc.getLifetimeSpent();
            String newTier;
            if (total.compareTo(BigDecimal.valueOf(10_000_000)) >= 0) {
                newTier = "Gold";
            } else if (total.compareTo(BigDecimal.valueOf(5_000_000)) >= 0) {
                newTier = "Silver";
            } else {
                newTier = "Bronze";
            }
            acc.setTier(newTier);
        }

        loyaltyDAO.save(acc);
        System.out.println("💳 Cập nhật LoyaltyAccount cho khách " + customerId);
    }

    /**
     * ------------------------- TEST -------------------------
     */
    public static void main(String[] args) {
        try (CqlSession session = KetNoICSDL.getSession()) {
            OrderService service = new OrderService(session);

            UUID customerId = UUID.randomUUID();
            List<OrderItem> items = List.of(
                    new OrderItem("P001", "MacBook Pro 14", 1, new BigDecimal("42000000")),
                    new OrderItem("P002", "Magic Mouse", 2, new BigDecimal("2500000"))
            );

            service.createOrder(customerId, items);
        }
    }
}
