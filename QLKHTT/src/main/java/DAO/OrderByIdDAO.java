package DAO;

import MODELS.OrderById;
import MODELS.OrderItem;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;
import com.datastax.oss.driver.api.core.data.UdtValue;
import com.datastax.oss.driver.api.core.type.UserDefinedType;
import java.util.*;

public class OrderByIdDAO {

    private final CqlSession session;

    public OrderByIdDAO(CqlSession session) {
        this.session = session;
    }

    public void save(OrderById order) {
        String query = "INSERT INTO orders_by_id (order_id, customer_id, order_date, total, items, status) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = session.prepare(query);

        List<UdtValue> itemValues = new ArrayList<>();
        UserDefinedType orderItemType = session.getMetadata()
                .getKeyspace(session.getKeyspace().get())
                .flatMap(ks -> ks.getUserDefinedType("order_item"))
                .orElseThrow(() -> new IllegalStateException("UDT order_item not found"));

        for (OrderItem item : order.getItems()) {
            UdtValue udt = orderItemType.newValue()
                    .setString("product_id", item.getProductId())
                    .setString("model", item.getModel())
                    .setInt("qty", item.getQuantity())
                    .setBigDecimal("price", item.getPrice());
            itemValues.add(udt);
        }

        session.execute(ps.bind(
                order.getOrderId(),
                order.getCustomerId(),
                order.getOrderDate(),
                order.getTotal(),
                itemValues,
                order.getStatus()
        ));
    }

    public OrderById findById(String orderId) {
        String query = "SELECT * FROM orders_by_id WHERE order_id = ?";
        PreparedStatement ps = session.prepare(query);
        Row row = session.execute(ps.bind(orderId)).one();
        if (row == null) {
            return null;
        }

        List<OrderItem> items = new ArrayList<>();
        List<UdtValue> udtItems = row.getList("items", UdtValue.class);
        if (udtItems != null) {
            for (UdtValue udt : udtItems) {
                OrderItem item = new OrderItem(
                        udt.getString("product_id"),
                        udt.getString("model"),
                        udt.getInt("qty"),
                        udt.getBigDecimal("price")
                );
                items.add(item);
            }
        }

        return new OrderById(
                // THAY ĐỔI: getUuid -> getString
                row.getString("order_id"),
                row.getString("customer_id"),
                row.getInstant("order_date"),
                row.getBigDecimal("total"),
                items,
                row.getString("status")
        );
    }

    public void update(OrderById order) {
        String query = "UPDATE orders_by_id SET customer_id=?, order_date=?, total=?, items=?, status=? WHERE order_id=?";
        PreparedStatement ps = session.prepare(query);

        List<UdtValue> itemValues = new ArrayList<>();
        UserDefinedType orderItemType = session.getMetadata()
                .getKeyspace(session.getKeyspace().get())
                .flatMap(ks -> ks.getUserDefinedType("order_item"))
                .orElseThrow(() -> new IllegalStateException("UDT order_item not found"));

        for (OrderItem item : order.getItems()) {
            UdtValue udt = orderItemType.newValue()
                    .setString("product_id", item.getProductId())
                    .setString("model", item.getModel())
                    .setInt("qty", item.getQuantity())
                    .setBigDecimal("price", item.getPrice());
            itemValues.add(udt);
        }

        session.execute(ps.bind(
                order.getCustomerId(),
                order.getOrderDate(),
                order.getTotal(),
                itemValues,
                order.getStatus(),
                order.getOrderId()
        ));
    }

    public void delete(String orderId) {
        String query = "DELETE FROM orders_by_id WHERE order_id = ?";
        PreparedStatement ps = session.prepare(query);
        session.execute(ps.bind(orderId));
    }

    public List<OrderById> findByStatus(String status) {
        String query = "SELECT * FROM orders_by_id WHERE status = ?";
        PreparedStatement ps = session.prepare(query);
        ResultSet rs = session.execute(ps.bind(status));

        List<OrderById> result = new ArrayList<>();
        for (Row row : rs) {
            List<OrderItem> items = new ArrayList<>();
            List<UdtValue> udtItems = row.getList("items", UdtValue.class);
            if (udtItems != null) {
                for (UdtValue udt : udtItems) {
                    items.add(new OrderItem(
                            udt.getString("product_id"),
                            udt.getString("model"),
                            udt.getInt("qty"),
                            udt.getBigDecimal("price")
                    ));
                }
            }

            result.add(new OrderById(
                    row.getString("order_id"),
                    row.getString("customer_id"),
                    row.getInstant("order_date"),
                    row.getBigDecimal("total"),
                    items,
                    row.getString("status")
            ));
        }
        return result;
    }
}
