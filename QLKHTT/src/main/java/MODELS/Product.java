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

/**
 *
 * @author HAO
 */
@Entity
@CqlName("products")
public class Product {
    @PartitionKey
    @CqlName("product_id")
    private String productId;

    @CqlName("brand")
    private String brand;

    @CqlName("model")
    private String model;

    @CqlName("cpu")
    private String cpu;

    @CqlName("ram")
    private int ram;

    @CqlName("storage")
    private String storage;

    @CqlName("price")
    private BigDecimal price;

    @CqlName("available")
    private boolean available;

    @CqlName("image")
    private String image;

    @CqlName("created_at")
    private Instant createdAt;

    // Constructors
    public Product() {}

    public Product(String productId, String brand, String model, String cpu, int ram, String storage, BigDecimal price, boolean available, String image, Instant createdAt) {
        this.productId = productId;
        this.brand = brand;
        this.model = model;
        this.cpu = cpu;
        this.ram = ram;
        this.storage = storage;
        this.price = price;
        this.available = available;
        this.image = image;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getCpu() { return cpu; }
    public void setCpu(String cpu) { this.cpu = cpu; }
    public int getRam() { return ram; }
    public void setRam(int ram) { this.ram = ram; }
    public String getStorage() { return storage; }
    public void setStorage(String storage) { this.storage = storage; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public Instant getCreatedAt() { return createdAt; } 
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
