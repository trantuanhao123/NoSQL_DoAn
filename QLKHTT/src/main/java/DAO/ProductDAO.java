package DAO;

import MODELS.Product;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProductDAO {
    private final CqlSession session;

    public ProductDAO(CqlSession session) {
        this.session = session;
    }

    // 🟢 Thêm sản phẩm mới
    public void insertProduct(Product product) {
        String query = "INSERT INTO products (product_id, brand, model, cpu, ram, storage, price, available, image, created_at) "
                     + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement stmt = session.prepare(query);
        session.execute(stmt.bind(
            product.getProductId(),
            product.getBrand(),
            product.getModel(),
            product.getCpu(),
            product.getRam(),
            product.getStorage(),
            product.getPrice(),
            product.isAvailable(),
            product.getImage(),
            product.getCreatedAt()
        ));
    }

    // 🟡 Cập nhật thông tin sản phẩm
    public void updateProduct(Product product) {
        String query = "UPDATE products SET brand = ?, model = ?, cpu = ?, ram = ?, storage = ?, "
                     + "price = ?, available = ?, image = ? WHERE product_id = ?";
        PreparedStatement stmt = session.prepare(query);
        session.execute(stmt.bind(
            product.getBrand(),
            product.getModel(),
            product.getCpu(),
            product.getRam(),
            product.getStorage(),
            product.getPrice(),
            product.isAvailable(),
            product.getImage(),
            product.getProductId()
        ));
    }

    // 🔴 Xóa sản phẩm theo ID
    public void deleteProductById(String productId) {
        String query = "DELETE FROM products WHERE product_id = ?";
        PreparedStatement stmt = session.prepare(query);
        session.execute(stmt.bind(productId));
    }

    // 🔍 Tìm sản phẩm theo ID
    public Product findProductById(String productId) {
        String query = "SELECT * FROM products WHERE product_id = ?";
        PreparedStatement stmt = session.prepare(query);
        ResultSet rs = session.execute(stmt.bind(productId));
        Row row = rs.one();

        if (row == null) return null;
        return mapRowToProduct(row);
    }

    // 📋 Lấy toàn bộ danh sách sản phẩm
    public List<Product> findAllProducts() {
        String query = "SELECT * FROM products";
        ResultSet rs = session.execute(query);
        List<Product> products = new ArrayList<>();
        for (Row row : rs) {
            products.add(mapRowToProduct(row));
        }
        return products;
    }

    // 🧩 Hàm helper map dữ liệu từ Row → Product
    private Product mapRowToProduct(Row row) {
        Product product = new Product();
        product.setProductId(row.getString("product_id"));
        product.setBrand(row.getString("brand"));
        product.setModel(row.getString("model"));
        product.setCpu(row.getString("cpu"));
        product.setRam(row.getInt("ram"));
        product.setStorage(row.getString("storage"));
        product.setPrice(row.getBigDecimal("price"));
        product.setAvailable(row.getBoolean("available"));
        product.setImage(row.getString("image"));
        product.setCreatedAt(row.getInstant("created_at"));
        return product;
    }

    // 🧠 Tạo sản phẩm mẫu (nếu cần test nhanh)
    public static Product createSampleProduct() {
        return new Product(
            UUID.randomUUID().toString(),
            "Dell",
            "Inspiron 15",
            "Intel Core i5",
            16,
            "512GB SSD",
            new BigDecimal("15990000"),
            true,
            "https://example.com/image.jpg",
            Instant.now()
        );
    }
}
