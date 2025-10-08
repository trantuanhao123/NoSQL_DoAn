package DAO;

import MODELS.LoyaltyAccount;
import MODELS.OrderByCustomer;
import MODELS.OrderById;
import MODELS.OrderItem;
import com.datastax.oss.driver.api.core.CqlSession;

import java.util.UUID;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * OrderService ------------- Xử lý toàn bộ logic nghiệp vụ liên quan đến đơn
 * hàng: - Tạo đơn (ghi vào 2 bảng: orders_by_customer, orders_by_id) - Đọc đơn
 * (theo customer hoặc orderId) - Cập nhật trạng thái - Xóa đơn hàng - Cập nhật
 * điểm khách hàng thân thiết (Loyalty)
 */
public class OrderService {

    private final OrderByCustomerDAO orderByCustomerDAO;
    private final OrderByIdDAO orderByIdDAO;
    private final ProductDAO productDAO;
    private final LoyaltyAccountDAO loyaltyDAO;

    public OrderService(CqlSession session) {
        DaoMapper daoMapper = new DaoMapperBuilder(session).build();
        this.orderByCustomerDAO = daoMapper.orderByCustomerDAO();
        this.orderByIdDAO = daoMapper.orderByIdDAO();
        this.productDAO = daoMapper.productDAO();
        this.loyaltyDAO = daoMapper.loyaltyAccountDAO();
    }

    /**
     * ------------------------- CREATE -------------------------
     */
    public void createOrder(UUID customerId, List<OrderItem> items) {
        UUID orderId = UUID.randomUUID();
        Instant now = Instant.now();

        // Tính tổng tiền đơn hàng
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem item : items) {
            total = total.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        // Xác định khóa phân vùng cho bảng orders_by_customer
        String yearMonth = String.format("%d_%02d",
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH) + 1);

        // Ghi vào orders_by_customer
        OrderByCustomer orderByCustomer = new OrderByCustomer(
                customerId, yearMonth, now, orderId, total, items, "PENDING"
        );
        orderByCustomerDAO.save(orderByCustomer);

        // Ghi vào orders_by_id
        OrderById orderById = new OrderById(
                orderId, customerId, now, total, items, "PENDING"
        );
        orderByIdDAO.save(orderById);

        // Cập nhật tài khoản khách hàng thân thiết
        updateLoyaltyAccount(customerId, total);
    }

    /**
     * ------------------------- READ -------------------------
     */
    // Lấy tất cả đơn hàng của 1 khách hàng
    public List<OrderByCustomer> getOrdersByCustomer(UUID customerId) {
        List<OrderByCustomer> result = new ArrayList<>();
        orderByCustomerDAO.findByCustomer(customerId).forEach(result::add);
        return result;
    }

    // Lấy thông tin chi tiết 1 đơn hàng theo ID
    public OrderById getOrderById(UUID orderId) {
        return orderByIdDAO.findById(orderId);
    }

    /**
     * ------------------------- UPDATE -------------------------
     */
    public void updateOrderStatus(UUID orderId, String status) {
        OrderById order = orderByIdDAO.findById(orderId);
        if (order != null) {
            order.setStatus(status);
            orderByIdDAO.update(order);
        }
    }

    /**
     * ------------------------- DELETE -------------------------
     */
    public void deleteOrder(UUID orderId, UUID customerId) {
        try {
            // 1️⃣ Lấy order đầy đủ từ bảng orders_by_id (để biết ngày đặt hàng thật)
            OrderById order = orderByIdDAO.findById(orderId);
            if (order == null) {
                System.out.println("⚠️ Không tìm thấy order trong orders_by_id: " + orderId);
                return;
            }

            // 2️⃣ Xóa khỏi bảng orders_by_id
            orderByIdDAO.delete(order);
            System.out.println("✅ Đã xóa order khỏi orders_by_id: " + orderId);

            // 3️⃣ Tính đúng partition key `year_month` theo ngày đặt hàng gốc
            Instant orderDate = order.getOrderDate();
            ZonedDateTime zdt = orderDate.atZone(ZoneId.systemDefault());
            String yearMonth = String.format("%d_%02d", zdt.getYear(), zdt.getMonthValue());

            // 4️⃣ Tạo đối tượng với đúng khóa để xóa khỏi orders_by_customer
            OrderByCustomer orderByCustomer = new OrderByCustomer(
                    customerId,
                    yearMonth,
                    order.getOrderDate(),
                    orderId,
                     order.getTotal(),
                    order.getItems(),
                    order.getStatus()
            );

            orderByCustomerDAO.delete(orderByCustomer);
            System.out.println("✅ Đã xóa order khỏi orders_by_customer: " + orderId + " (" + yearMonth + ")");

        } catch (Exception e) {
            System.err.println("❌ Lỗi khi xóa đơn hàng: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * ------------------------- LOYALTY -------------------------
     */
    private void updateLoyaltyAccount(UUID customerId, BigDecimal orderTotal) {
        LoyaltyAccount acc = loyaltyDAO.findById(customerId);
        Instant now = Instant.now();

        if (acc == null) {
            // Nếu khách hàng chưa có tài khoản điểm, tạo mới
            acc = new LoyaltyAccount(
                    customerId,
                    orderTotal.longValue(), // điểm
                    "Bronze",
                    orderTotal, // lifetimeSpent
                    1, // số đơn hàng
                    now
            );
        } else {
            // Cập nhật tài khoản cũ
            acc.setOrderCount(acc.getOrderCount() + 1);
            acc.setLifetimeSpent(acc.getLifetimeSpent().add(orderTotal));
            acc.setPoints(acc.getPoints() + orderTotal.longValue());
            acc.setLastUpdated(now);

            // Cập nhật hạng thẻ theo tổng chi tiêu
            BigDecimal total = acc.getLifetimeSpent();
            String newTier;
            if (total.compareTo(BigDecimal.valueOf(10_000_000)) > 0) {
                newTier = "Gold";
            } else if (total.compareTo(BigDecimal.valueOf(5_000_000)) > 0) {
                newTier = "Silver";
            } else {
                newTier = "Bronze";
            }
            acc.setTier(newTier);
        }

        loyaltyDAO.save(acc);
    }
}
