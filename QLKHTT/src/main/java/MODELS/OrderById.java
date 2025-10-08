/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package MODELS;

import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author HAO
 */
@Entity
@CqlName("orders_by_id")
public class OrderById {
    @PartitionKey
    @CqlName("order_id")
    private UUID orderId;

    @CqlName("customer_id")
    private UUID customerId;

    @CqlName("order_date")
    private Instant orderDate;

    @CqlName("total")
    private BigDecimal total;

    @CqlName("items")
    private List<OrderItem> items;

    @CqlName("status")
    private String status;

    // Constructors
    public OrderById() {}

    public OrderById(UUID orderId, UUID customerId, Instant orderDate, BigDecimal total, List<OrderItem> items, String status) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.orderDate = orderDate;
        this.total = total;
        this.items = items;
        this.status = status;
    }

    // Getters and Setters
    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }
    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }
    public Instant getOrderDate() { return orderDate; }
    public void setOrderDate(Instant orderDate) { this.orderDate = orderDate; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}