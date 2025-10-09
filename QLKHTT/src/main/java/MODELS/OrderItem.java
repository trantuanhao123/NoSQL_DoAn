package MODELS;

import java.math.BigDecimal;
import java.io.Serializable;

/**
 * Lớp OrderItem mô tả 1 sản phẩm trong đơn hàng.
 * Không sử dụng annotation của Cassandra mapper.
 */
public class OrderItem implements Serializable {

    private String productId;
    private String model;
    private int quantity;
    private BigDecimal price;

    // ✅ Constructor mặc định (Cassandra driver cần để deserialize)
    public OrderItem() {}

    // ✅ Constructor đầy đủ
    public OrderItem(String productId, String model, int quantity, BigDecimal price) {
        this.productId = productId;
        this.model = model;
        this.quantity = quantity;
        this.price = price;
    }

    // ✅ Getters / Setters
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    // ✅ Optional: hỗ trợ debug hoặc log
    @Override
    public String toString() {
        return "OrderItem{" +
                "productId='" + productId + '\'' +
                ", model='" + model + '\'' +
                ", quantity=" + quantity +
                ", price=" + price +
                '}';
    }
}
