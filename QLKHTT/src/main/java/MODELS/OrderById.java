package MODELS;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.io.Serializable;

public class OrderById implements Serializable {

    private String orderId;
    private String customerId;

    private Instant orderDate;
    private BigDecimal total;
    private List<OrderItem> items;
    private String status;

    public OrderById() {
    }

    public OrderById(String orderId, String customerId, Instant orderDate, BigDecimal total, List<OrderItem> items, String status) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.orderDate = orderDate;
        this.total = total;
        this.items = items;
        this.status = status;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
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

    @Override
    public String toString() {
        return "OrderById{"
                + "orderId=" + orderId
                + ", customerId=" + customerId
                + ", orderDate=" + orderDate
                + ", total=" + total
                + ", items=" + items
                + ", status='" + status + '\''
                + '}';
    }
}
