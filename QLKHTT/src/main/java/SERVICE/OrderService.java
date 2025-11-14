package SERVICE;

import DAO.LoyaltyAccountDAO;
import DAO.OrderByCustomerDAO;
import DAO.OrderByIdDAO;
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

    private static final BigDecimal SILVER_THRESHOLD_SPENT = BigDecimal.valueOf(5_000_000);
    private static final int SILVER_THRESHOLD_ORDERS = 3;
    
    private static final BigDecimal GOLD_THRESHOLD_SPENT = BigDecimal.valueOf(15_000_000);
    private static final int GOLD_THRESHOLD_ORDERS = 10;

    public OrderService(CqlSession session) {
        this.orderByCustomerDAO = new OrderByCustomerDAO(session);
        this.orderByIdDAO = new OrderByIdDAO(session);
        this.loyaltyDAO = new LoyaltyAccountDAO(session);
    }

    public void createOrder(String orderId, String customerId, List<OrderItem> items, String status) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Danh sach san pham khong duoc rong.");
        }

        Instant now = Instant.now();

        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem item : items) {
            total = total.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        ZonedDateTime zdt = now.atZone(ZoneId.systemDefault());
        String yearMonth = String.format("%d_%02d", zdt.getYear(), zdt.getMonthValue());

        OrderByCustomer orderByCustomer = new OrderByCustomer(
                customerId, yearMonth, now, orderId, total, items, status
        );
        orderByCustomerDAO.save(orderByCustomer);

        OrderById orderById = new OrderById(orderId, customerId, now, total, items, status
        );
        orderByIdDAO.save(orderById);

        if (status.equalsIgnoreCase("Hoàn Thành")) {
            updateLoyaltyAccount(customerId, total);
        }

        System.out.println("Tao don hang thanh cong: " + orderId);
    }
     
    public List<OrderByCustomer> getOrdersByCustomer(String customerId) {
        if (customerId == null) {
            return orderByCustomerDAO.findAll();
        }
        return orderByCustomerDAO.findByCustomer(customerId);
    }

    public List<OrderByCustomer> getOrdersByCustomerAndMonth(String customerId, String yyyyMM) {
        return orderByCustomerDAO.findByCustomerAndYearMonth(customerId, yyyyMM);
    }

    public OrderById getOrderById(String orderId) {
        return orderByIdDAO.findById(orderId);
    }

    public void updateOrderStatus(String orderId, String newStatus) {
        OrderById order = orderByIdDAO.findById(orderId);
        if (order == null) {
            System.out.println("Khong tim thay don hang: " + orderId);
            return;
        }

        String oldStatus = order.getStatus();
        if (oldStatus.equalsIgnoreCase(newStatus)) {
            return;
        }

        String customerId = order.getCustomerId();
        BigDecimal total = order.getTotal();

        if (oldStatus.equalsIgnoreCase("Hoàn Thành") && !newStatus.equalsIgnoreCase("Hoàn Thành")) {
            rollbackLoyaltyAccount(customerId, total);
            System.out.println("Da hoan tac Loyalty do huy don hang " + orderId);
        }
        else if (!oldStatus.equalsIgnoreCase("Hoàn Thành") && newStatus.equalsIgnoreCase("Hoàn Thành")) {
            updateLoyaltyAccount(customerId, total);
            System.out.println("Da cap nhat Loyalty do hoan thanh don hang " + orderId);
        }

        order.setStatus(newStatus);
        orderByIdDAO.update(order);

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
                newStatus
        );
        orderByCustomerDAO.update(orderByCustomer);

        System.out.println("Da cap nhat trang thai don hang " + orderId + " thanh " + newStatus);
    }

    public void deleteOrder(String orderId) {
        try {
            OrderById order = orderByIdDAO.findById(orderId);
            if (order == null) {
                System.out.println("Khong tim thay order: " + orderId);
                return;
            }
            
            String customerId = order.getCustomerId();
            BigDecimal orderTotal = order.getTotal();
            Instant orderDate = order.getOrderDate();

            // 1. Xóa khỏi order_by_id
            orderByIdDAO.delete(orderId);

            // Lấy yearMonth để xóa khỏi order_by_customer
            ZonedDateTime zdt = orderDate.atZone(ZoneId.systemDefault());
            String yearMonth = String.format("%d_%02d", zdt.getYear(), zdt.getMonthValue());

            // 2. Xóa khỏi order_by_customer
            orderByCustomerDAO.delete(
                    customerId,
                    yearMonth,
                    orderDate,
                    orderId
            );

            System.out.println("Da xoa order " + orderId + " khoi ca hai bang.");

            // --- BẮT ĐẦU LOGIC CẬP NHẬT LOYALTY ---

            // 3. Cập nhật (Hoàn tác) điểm nếu đơn hàng đã COMPLETED
            if (order.getStatus().equalsIgnoreCase("Hoàn Thành")) {
                 rollbackLoyaltyAccount(customerId, orderTotal);
                 System.out.println("Da hoan tac Loyalty do xoa don hang " + orderId);
            }

            // 4. Cập nhật (Kiểm tra xóa) nếu đây là đơn cuối cùng
            // (Chúng ta gọi hàm này SAU KHI đã xóa đơn hàng ở bước 1 & 2)
            List<OrderByCustomer> remainingOrders = orderByCustomerDAO.findByCustomer(customerId);
            
            if (remainingOrders == null || remainingOrders.isEmpty()) {
                // Nếu không còn đơn hàng nào, xóa luôn tài khoản loyalty
                loyaltyDAO.delete(customerId);
                System.out.println("Khach hang " + customerId + " khong con don hang nao. Da xoa LoyaltyAccount.");
            }
            // --- KẾT THÚC LOGIC CẬP NHẬT LOYALTY ---

        } catch (Exception e) {
            System.err.println("Loi khi xoa order: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateLoyaltyAccount(String customerId, BigDecimal orderTotal) {
        LoyaltyAccount acc = loyaltyDAO.findByCustomer(customerId); 
        Instant now = Instant.now();
        
        long newPoints = orderTotal.longValue();
        BigDecimal newLifetimeSpent = orderTotal;
        int newOrderCount = 1;

        if (acc != null) {
            newOrderCount = acc.getOrderCount() + 1;
            newLifetimeSpent = acc.getLifetimeSpent().add(orderTotal);
            newPoints = acc.getPoints() + orderTotal.longValue();
        }

        String newTier = determineTier(newLifetimeSpent, newOrderCount);
        
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
        System.out.println("Cap nhat LoyaltyAccount cho khach " + customerId + " hang: " + newTier);
    }
    
    private void rollbackLoyaltyAccount(String customerId, BigDecimal orderTotal) {
        LoyaltyAccount acc = loyaltyDAO.findByCustomer(customerId); 

        if (acc == null) {
            System.err.println("Loi rollback: Khong tim thay Loyalty Account cho khach hang " + customerId);
            return;
        }
        
        long newPoints = acc.getPoints() - orderTotal.longValue();
        BigDecimal newLifetimeSpent = acc.getLifetimeSpent().subtract(orderTotal);
        int newOrderCount = acc.getOrderCount() - 1;
        
        newPoints = Math.max(0, newPoints);
        newLifetimeSpent = newLifetimeSpent.max(BigDecimal.ZERO);
        newOrderCount = Math.max(0, newOrderCount);
        
        String newTier = determineTier(newLifetimeSpent, newOrderCount);

        acc.setPoints(newPoints);
        acc.setLifetimeSpent(newLifetimeSpent);
        acc.setOrderCount(newOrderCount);
        acc.setTier(newTier);
        acc.setLastUpdated(Instant.now());
        
        loyaltyDAO.save(acc);
        System.out.println("Da hoan tac Loyalty cho khach " + customerId + " hang moi: " + newTier);
    }

    private String determineTier(BigDecimal totalSpent, int orderCount) {
        String newTier;
        
        if (totalSpent.compareTo(GOLD_THRESHOLD_SPENT) >= 0 && orderCount >= GOLD_THRESHOLD_ORDERS) {
            newTier = "Vàng";
        } 
        else if (totalSpent.compareTo(SILVER_THRESHOLD_SPENT) >= 0 && orderCount >= SILVER_THRESHOLD_ORDERS) {
            newTier = "Bạc";
        } 
        else {
            newTier = "Đồng";
        }
        return newTier;
    }
}