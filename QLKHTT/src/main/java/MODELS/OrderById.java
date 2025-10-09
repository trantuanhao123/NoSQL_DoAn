package MODELS;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.io.Serializable;

/**
 * Lớp OrderById mô tả đơn hàng được truy cập theo order_id.
 * Không sử dụng annotation Cassandra mapper.
 */
public class OrderById implements Serializable {

    private UUID orderId;
    private UUID customerId;
    private Instant orderDate;
    private BigDecimal total;
    private List<OrderItem> items;
    private String status;

    // ✅ Constructor mặc định (Cassandra driver cần khi deserialize)
    public OrderById() {}

    // ✅ Constructor đầy đủ
    public OrderById(UUID orderId, UUID customerId, Instant orderDate, BigDecimal total, List<OrderItem> items, String status) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.orderDate = orderDate;
        this.total = total;
        this.items = items;
        this.status = status;
    }

    // ✅ Getters / Setters
    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    public Instant getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Instant orderDate) {
        this.orderDate = orderDate;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // ✅ toString() để debug/log
    @Override
    public String toString() {
        return "OrderById{" +
                "orderId=" + orderId +
                ", customerId=" + customerId +
                ", orderDate=" + orderDate +
                ", total=" + total +
                ", items=" + items +
                ", status='" + status + '\'' +
                '}';
    }
}
