/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package MODELS;

import com.datastax.oss.driver.api.mapper.annotations.ClusteringColumn;
import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author HAO
 */
@Entity
@CqlName("orders_by_customer")
public class OrderByCustomer {
    @PartitionKey(0)
    @CqlName("customer_id")
    private UUID customerId;

    @PartitionKey(1)
    @CqlName("yyyy_mm")
    private String yearMonth;

    @ClusteringColumn(0)
    @CqlName("order_date")
    private LocalDateTime orderDate;

    @ClusteringColumn(1)
    @CqlName("order_id")
    private UUID orderId;

    @CqlName("total")
    private BigDecimal total;

    @CqlName("items")
    private List<OrderItem> items;

    @CqlName("status")
    private String status;

    // Constructors
    public OrderByCustomer() {}

    public OrderByCustomer(UUID customerId, String yearMonth, LocalDateTime orderDate, UUID orderId, BigDecimal total, List<OrderItem> items, String status) {
        this.customerId = customerId;
        this.yearMonth = yearMonth;
        this.orderDate = orderDate;
        this.orderId = orderId;
        this.total = total;
        this.items = items;
        this.status = status;
    }

    // Getters and Setters
    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }
    public String getYearMonth() { return yearMonth; }
    public void setYearMonth(String yearMonth) { this.yearMonth = yearMonth; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
