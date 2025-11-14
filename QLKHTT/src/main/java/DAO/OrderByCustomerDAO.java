package DAO;

import KetNoiCSDL.KetNoICSDL;
import MODELS.OrderByCustomer;
import MODELS.OrderItem;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.data.UdtValue;
import com.datastax.oss.driver.api.core.cql.*;
import com.datastax.oss.driver.api.core.type.UserDefinedType;
import java.time.Instant;
import java.util.*;

public class OrderByCustomerDAO {

    private final CqlSession session;
    private final UserDefinedType orderItemType;

    public OrderByCustomerDAO(CqlSession session) {
        this.session = session;
        this.orderItemType = session.getMetadata()
                .getKeyspace(session.getKeyspace().get())
                .flatMap(ks -> ks.getUserDefinedType("order_item"))
                .orElseThrow(() -> new RuntimeException("⚠️ Không tìm thấy UDT 'order_item' trong keyspace!"));
    }

    private UdtValue toUdt(OrderItem item) {
        return orderItemType.newValue()
                .setString("product_id", item.getProductId())
                .setString("model", item.getModel())
                .setInt("qty", item.getQuantity())
                .setBigDecimal("price", item.getPrice());
    }

    private OrderItem fromUdt(UdtValue udt) {
        return new OrderItem(
                udt.getString("product_id"),
                udt.getString("model"),
                udt.getInt("qty"),
                udt.getBigDecimal("price")
        );
    }

    public void save(OrderByCustomer order) {
        String query = """
            INSERT INTO orders_by_customer (
                customer_id, yyyy_mm, order_date, order_id,
                total, items, status
            ) VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        List<UdtValue> udtItems = order.getItems() == null
                ? Collections.emptyList()
                : order.getItems().stream().map(this::toUdt).toList();

        PreparedStatement ps = session.prepare(query);
        BoundStatement bs = ps.bind(
                order.getCustomerId(),
                order.getYearMonth(),
                order.getOrderDate(),
                order.getOrderId(),
                order.getTotal(),
                udtItems,
                order.getStatus()
        );
        session.execute(bs);
    }

    public void update(OrderByCustomer order) {
        String query = """
            UPDATE orders_by_customer
            SET total = ?, items = ?, status = ?
            WHERE customer_id = ? AND yyyy_mm = ? AND order_date = ? AND order_id = ?
        """;

        List<UdtValue> udtItems = order.getItems() == null
                ? Collections.emptyList()
                : order.getItems().stream().map(this::toUdt).toList();

        PreparedStatement ps = session.prepare(query);
        BoundStatement bs = ps.bind(
                order.getTotal(),
                udtItems,
                order.getStatus(),
                order.getCustomerId(),
                order.getYearMonth(),
                order.getOrderDate(),
                order.getOrderId()
        );
        session.execute(bs);
    }

    public void delete(String customerId, String yyyyMM, Instant orderDate, String orderId) {
        String query = """
            DELETE FROM orders_by_customer
            WHERE customer_id = ? AND yyyy_mm = ? AND order_date = ? AND order_id = ?
        """;
        PreparedStatement ps = session.prepare(query);
        BoundStatement bs = ps.bind(customerId, yyyyMM, orderDate, orderId);
        session.execute(bs);
    }

    public List<OrderByCustomer> findByCustomer(String customerId) {
        String query = "SELECT * FROM orders_by_customer WHERE customer_id = ? ALLOW FILTERING";
        PreparedStatement ps = session.prepare(query);
        BoundStatement bs = ps.bind(customerId);
        ResultSet rs = session.execute(bs);
        return mapOrders(rs);
    }

    public List<OrderByCustomer> findByCustomerAndYearMonth(String customerId, String yyyyMM) {
        String query = "SELECT * FROM orders_by_customer WHERE customer_id = ? AND yyyy_mm = ?";
        PreparedStatement ps = session.prepare(query);
        BoundStatement bs = ps.bind(customerId, yyyyMM);
        ResultSet rs = session.execute(bs);
        return mapOrders(rs);
    }

    public List<OrderByCustomer> findAll() {
        String query = "SELECT * FROM orders_by_customer";
        ResultSet rs = session.execute(query);
        return mapOrders(rs);
    }

    public List<OrderByCustomer> findByStatus(String status) {
        String query = "SELECT * FROM orders_by_customer WHERE status = ? ALLOW FILTERING";
        PreparedStatement ps = session.prepare(query);
        BoundStatement bs = ps.bind(status);
        ResultSet rs = session.execute(bs);
        return mapOrders(rs);
    }

    private List<OrderByCustomer> mapOrders(ResultSet rs) {
        List<OrderByCustomer> result = new ArrayList<>();
        for (Row row : rs) {
            OrderByCustomer o = new OrderByCustomer();
            o.setCustomerId(row.getString("customer_id"));
            o.setYearMonth(row.getString("yyyy_mm"));
            o.setOrderDate(row.getInstant("order_date"));
            o.setOrderId(row.getString("order_id"));
            o.setTotal(row.getBigDecimal("total"));
            o.setStatus(row.getString("status"));
            List<UdtValue> udtList = row.getList("items", UdtValue.class);
            List<OrderItem> items = new ArrayList<>();
            if (udtList != null) {
                for (UdtValue u : udtList) {
                    items.add(fromUdt(u));
                }
            }
            o.setItems(items);

            result.add(o);
        }
        return result;
    }
}
