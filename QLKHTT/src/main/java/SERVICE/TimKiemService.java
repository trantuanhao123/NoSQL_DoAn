package SERVICE;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import KetNoiCSDL.KetNoICSDL;
import java.math.BigDecimal;
import java.util.*;

public class TimKiemService {
    private CqlSession session;
    private PreparedStatement psFindCustomerByEmail;
    private PreparedStatement psFindCustomerByPhone;
    private PreparedStatement psGetCustomerById;
    private PreparedStatement psGetProductById;
    private PreparedStatement psGetAvailableProductsByBrand;
    private PreparedStatement psGetOrdersByCustomerMonth;
    private PreparedStatement psGetOrderById;
    private PreparedStatement psGetLoyaltyByCustomer;
    private PreparedStatement psUpdateLoyalty;

    public TimKiemService() {
        this.session = KetNoICSDL.getSession();
        prepareStatements();
    }

    private void prepareStatements() {
        psFindCustomerByEmail = session.prepare("SELECT * FROM customers WHERE email = ? ALLOW FILTERING");
        psFindCustomerByPhone = session.prepare("SELECT * FROM customers WHERE phone = ? ALLOW FILTERING");
        psGetCustomerById = session.prepare("SELECT * FROM customers WHERE customer_id = ?");
        psGetProductById = session.prepare("SELECT * FROM products WHERE product_id = ?");
        psGetAvailableProductsByBrand = session.prepare("SELECT * FROM products WHERE brand = ? AND available = true ALLOW FILTERING");
        psGetOrdersByCustomerMonth = session.prepare("SELECT * FROM orders_by_customer WHERE customer_id = ? AND yyyy_mm = ?");
        psGetOrderById = session.prepare("SELECT * FROM orders_by_id WHERE order_id = ?");
        psGetLoyaltyByCustomer = session.prepare("SELECT points, tier, lifetime_spent, order_count FROM loyalty_accounts WHERE customer_id = ?");
        psUpdateLoyalty = session.prepare("UPDATE loyalty_accounts SET points = ?, lifetime_spent = ?, order_count = ?, last_updated = toTimestamp(now()) WHERE customer_id = ?");
    }

    private Map<String, Object> rowToMap(Row row, List<String> columns) {
        Map<String, Object> map = new HashMap<>();
        for (String col : columns) {
            try {
                Object value = row.getObject(col);
                map.put(col, value);
            } catch (Exception e) {
            }
        }
        return map;
    }

    public List<Map<String, Object>> findCustomersByEmailOrPhone(String email, String phone) {
        List<Map<String, Object>> customers = new ArrayList<>();
        List<String> columns = Arrays.asList("customer_id", "full_name", "email", "phone", "dob", "gender", "address", "created_at", "status");
        try {
            if (email != null && !email.isEmpty()) {
                BoundStatement bs = psFindCustomerByEmail.bind(email);
                ResultSet rs = session.execute(bs);
                for (Row row : rs) {
                    customers.add(rowToMap(row, columns));
                }
            }
            if (phone != null && !phone.isEmpty()) {
                BoundStatement bs = psFindCustomerByPhone.bind(phone);
                ResultSet rs = session.execute(bs);
                for (Row row : rs) {
                    final String currentCustomerId = row.getString("customer_id"); 
                    boolean exists = customers.stream().anyMatch(c -> c.get("customer_id").equals(currentCustomerId));
                    if (!exists) {
                        customers.add(rowToMap(row, columns));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Lỗi khi tìm khách hàng: " + e.getMessage());
        }
        return customers;
    }

    public Map<String, Object> getCustomerDetails(String customerId) {
        Map<String, Object> customer = new HashMap<>();
        try {
            BoundStatement bs = psGetCustomerById.bind(customerId);
            ResultSet rs = session.execute(bs);
            Row row = rs.one();
            if (row != null) {
                List<String> columns = Arrays.asList("customer_id", "full_name", "email", "phone", "dob", "gender", "address", "created_at", "status");
                customer = rowToMap(row, columns);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Lỗi khi lấy chi tiết khách hàng: " + e.getMessage());
        }
        return customer;
    }

    public Map<String, Object> findProductById(String productId) {
        Map<String, Object> product = new HashMap<>();
        try {
            BoundStatement bs = psGetProductById.bind(productId);
            ResultSet rs = session.execute(bs);
            Row row = rs.one();
            if (row != null) {
                List<String> columns = Arrays.asList("product_id", "brand", "model", "cpu", "ram", "storage", "price", "available", "image", "created_at");
                product = rowToMap(row, columns);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Lỗi khi tìm sản phẩm: " + e.getMessage());
        }
        return product;
    }

    public List<Map<String, Object>> listAvailableProductsByBrand(String brand) {
        List<Map<String, Object>> products = new ArrayList<>();
        try {
            BoundStatement bs = psGetAvailableProductsByBrand.bind(brand);
            ResultSet rs = session.execute(bs);
            List<String> columns = Arrays.asList("product_id", "brand", "model", "cpu", "ram", "storage", "price", "available", "image", "created_at");
            for (Row row : rs) {
                products.add(rowToMap(row, columns));
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Lỗi khi liệt kê sản phẩm: " + e.getMessage());
        }
        return products;
    }

    public List<Map<String, Object>> getCustomerOrderHistory(String customerId, String yyyyMm) {
        List<Map<String, Object>> orders = new ArrayList<>();
        try {
            BoundStatement bs = psGetOrdersByCustomerMonth.bind(customerId, yyyyMm);
            ResultSet rs = session.execute(bs);
            List<String> columns = Arrays.asList("customer_id", "yyyy_mm", "order_date", "order_id", "total", "items", "status");
            for (Row row : rs) {
                orders.add(rowToMap(row, columns));
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Lỗi khi lấy lịch sử đơn hàng: " + e.getMessage());
        }
        return orders;
    }

    public Map<String, Object> getOrderById(String orderId) {
        Map<String, Object> order = new HashMap<>();
        try {
            BoundStatement bs = psGetOrderById.bind(orderId);
            ResultSet rs = session.execute(bs);
            Row row = rs.one();
            if (row != null) {
                List<String> columns = Arrays.asList("order_id", "customer_id", "order_date", "total", "items", "status");
                order = rowToMap(row, columns);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Lỗi khi tra cứu đơn hàng: " + e.getMessage());
        }
        return order;
    }

    public Map<String, Object> getCustomerLoyalty(String customerId) {
        Map<String, Object> loyalty = new HashMap<>();
        try {
            BoundStatement bs = psGetLoyaltyByCustomer.bind(customerId);
            ResultSet rs = session.execute(bs);
            Row row = rs.one();
            if (row != null) {
                loyalty.put("points", row.getLong("points"));
                loyalty.put("tier", row.getString("tier"));
                loyalty.put("lifetime_spent", row.getBigDecimal("lifetime_spent"));
                loyalty.put("order_count", row.getInt("order_count"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Lỗi khi lấy thông tin loyalty: " + e.getMessage());
        }
        return loyalty;
    }

    public boolean updateCustomerLoyalty(String customerId, long pointsDelta, BigDecimal spentDelta) {
        try {
            BoundStatement bsSelect = psGetLoyaltyByCustomer.bind(customerId);
            ResultSet rs = session.execute(bsSelect);
            Row row = rs.one();
            if (row == null) {
                return false;
            }
            long currentPoints = row.getLong("points");
            BigDecimal currentSpent = row.getBigDecimal("lifetime_spent");
            int currentOrderCount = row.getInt("order_count");

            long newPoints = currentPoints + pointsDelta;
            BigDecimal newSpent = currentSpent.add(spentDelta);
            int newOrderCount = currentOrderCount + 1;

            BoundStatement bsUpdate = psUpdateLoyalty.bind(newPoints, newSpent, newOrderCount, customerId);
            session.execute(bsUpdate);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Lỗi khi cập nhật loyalty: " + e.getMessage());
            return false;
        }
    }
}