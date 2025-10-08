/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package MODELS;

import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import java.math.BigDecimal;

/**
 *
 * @author HAO
 */
@Entity
@CqlName("order_item")
public class OrderItem {
    @CqlName("product_id")
    private String productId;

    @CqlName("model")
    private String model;

    @CqlName("qty")
    private int quantity;

    @CqlName("price")
    private BigDecimal price;

    // Constructors
    public OrderItem() {}

    public OrderItem(String productId, String model, int quantity, BigDecimal price) {
        this.productId = productId;
        this.model = model;
        this.quantity = quantity;
        this.price = price;
    }

    // Getters and Setters
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
}