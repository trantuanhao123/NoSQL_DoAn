package SERVICE; 

import DAO.LoyaltyAccountDAO;
import DAO.OrderByIdDAO;
import DAO.OrderByCustomerDAO;
import MODELS.OrderByCustomer; 
import com.datastax.oss.driver.api.core.CqlSession;
import java.util.List;

public class LoyaltyAccountService {

    private final LoyaltyAccountDAO loyaltyDAO;
    private final OrderByIdDAO orderByIdDAO;
    private final OrderByCustomerDAO orderByCustomerDAO;

    public LoyaltyAccountService(CqlSession session) {
        this.loyaltyDAO = new LoyaltyAccountDAO(session);
        this.orderByIdDAO = new OrderByIdDAO(session);
        this.orderByCustomerDAO = new OrderByCustomerDAO(session);
    }
    public void deleteLoyaltyAndOrders(String customerId) {
        System.out.println("Bat dau xoa du lieu cho khach hang: " + customerId);

        try {
            // Bước 1: Tìm tất cả đơn hàng của khách hàng này
            List<OrderByCustomer> allOrders = orderByCustomerDAO.findByCustomer(customerId);

            if (allOrders != null && !allOrders.isEmpty()) {
                System.out.println("Tim thay " + allOrders.size() + " don hang. Bat dau xoa...");
                
                // Bước 2: Lặp qua từng đơn hàng và xóa
                for (OrderByCustomer order : allOrders) {
                    
                    // Xóa khỏi bảng 'order_by_id'
                    orderByIdDAO.delete(order.getOrderId());
                    
                    // Xóa khỏi bảng 'order_by_customer'
                    // (Cần đủ các khóa chính để xóa)
                    orderByCustomerDAO.delete(
                            order.getCustomerId(),
                            order.getYearMonth(),
                            order.getOrderDate(),
                            order.getOrderId()
                    );
                }
                System.out.println("Da xoa xong " + allOrders.size() + " don hang.");
            } else {
                System.out.println("Khach hang " + customerId + " khong co don hang nao.");
            }

            // Bước 3: Sau khi đã xóa hết đơn hàng, xóa tài khoản loyalty
            loyaltyDAO.delete(customerId);
            System.out.println("Da xoa LoyaltyAccount cho khach hang: " + customerId);
            System.out.println("Hoan tat.");

        } catch (Exception e) {
            System.err.println("Loi nghiem trong khi xoa du lieu loyalty/order: " + e.getMessage());
            e.printStackTrace();
            // Bạn có thể throw exception ở đây để báo cho Panel biết là đã thất bại
            throw new RuntimeException("Xoa that bai: " + e.getMessage(), e);
        }
    }
}