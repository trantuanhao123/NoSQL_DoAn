package MODELS;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class OrderByCustomer {
    private UUID customerId;
    private String yearMonth;
    private Instant orderDate;
    private UUID orderId;
    private BigDecimal total;
    private List<OrderItem> items;
    private String status;

    // Constructors
    public OrderByCustomer() {}

    public OrderByCustomer(UUID customerId, String yearMonth, Instant orderDate, UUID orderId, BigDecimal total, List<OrderItem> items, String status) {
        this.customerId = customerId;
        this.yearMonth = yearMonth;
        this.orderDate = orderDate;
        this.orderId = orderId;
        this.total = total;
        this.items = items;
        this.status = status;
    }

    // Getters and Setters
    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    public String getYearMonth() {
        return yearMonth;
    }

    public void setYearMonth(String yearMonth) {
        this.yearMonth = yearMonth;
    }

    public Instant getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Instant orderDate) {
        this.orderDate = orderDate;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
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
}
