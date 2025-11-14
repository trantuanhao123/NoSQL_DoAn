package MODELS;

import java.math.BigDecimal;
import java.time.Instant;

public class Product {

    private String productId;
    private String brand;
    private String model;
    private String cpu;
    private int ram;
    private String storage;
    private BigDecimal price;
    private boolean available;
    private String image;
    private Instant createdAt;

    public Product() {
    }

    public Product(
            String productId,
            String brand,
            String model,
            String cpu,
            int ram,
            String storage,
            BigDecimal price,
            boolean available,
            String image,
            Instant createdAt
    ) {
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

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getCpu() {
        return cpu;
    }

    public void setCpu(String cpu) {
        this.cpu = cpu;
    }

    public int getRam() {
        return ram;
    }

    public void setRam(int ram) {
        this.ram = ram;
    }

    public String getStorage() {
        return storage;
    }

    public void setStorage(String storage) {
        this.storage = storage;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Product{"
                + "productId='" + productId + '\''
                + ", brand='" + brand + '\''
                + ", model='" + model + '\''
                + ", cpu='" + cpu + '\''
                + ", ram=" + ram
                + ", storage='" + storage + '\''
                + ", price=" + price
                + ", available=" + available
                + ", image='" + image + '\''
                + ", createdAt=" + createdAt
                + '}';
    }
}
