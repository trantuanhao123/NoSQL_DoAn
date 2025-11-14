package MODELS;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class OrderByCustomer {

    private String customerId;
    private String yearMonth;
    private Instant orderDate;

    private String orderId;

    private BigDecimal total;
    private List<OrderItem> items;
    private String status;

    public OrderByCustomer() {
    }

    public OrderByCustomer(String customerId, String yearMonth, Instant orderDate, String orderId, BigDecimal total, List<OrderItem> items, String status) {
        this.customerId = customerId;
        this.yearMonth = yearMonth;
        this.orderDate = orderDate;
        this.orderId = orderId;
        this.total = total;
        this.items = items;
        this.status = status;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
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

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
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
