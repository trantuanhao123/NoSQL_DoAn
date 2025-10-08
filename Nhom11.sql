CREATE KEYSPACE IF NOT EXISTS DoAnSQL
WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};

USE DoAnSQL;


---------- 1) CUSTOMERS
CREATE TABLE IF NOT EXISTS customers (
  customer_id uuid,
  full_name text,
  email text,
  phone text,
  dob date,
  gender text,
  address text,
  created_at timestamp,
  status text,
  PRIMARY KEY (customer_id)
);

INSERT INTO customers (customer_id, full_name, email, phone, dob, gender, address, created_at, status)
VALUES (11111111-1111-1111-1111-111111111111, 'Nguyễn Văn An', 'nguyenvanan@gmail.com', '0901234567',
        '1990-03-15', 'Nam', '123 Đường Láng, P. Láng Thượng, Q. Đống Đa, Hà Nội', '2024-01-15 10:30:00', 'Active');

INSERT INTO customers (customer_id, full_name, email, phone, dob, gender, address, created_at, status)
VALUES (22222222-2222-2222-2222-222222222222, 'Trần Thị Bình', 'tranthibinh@outlook.com', '0912345678',
        '1995-07-22', 'Nữ', '456 Nguyễn Trãi, P. 8, Q. 5, TP. Hồ Chí Minh', '2024-02-10 14:20:00', 'Active');

INSERT INTO customers (customer_id, full_name, email, phone, dob, gender, address, created_at, status)
VALUES (33333333-3333-3333-3333-333333333333, 'Lê Minh Cường', 'leminhcuong@yahoo.com', '0987654321',
        '1988-11-30', 'Nam', '789 Hùng Vương, Q. Hải Châu, TP. Đà Nẵng', '2024-03-05 09:15:00', 'Active');

INSERT INTO customers (customer_id, full_name, email, phone, dob, gender, address, created_at, status)
VALUES (44444444-4444-4444-4444-444444444444, 'Phạm Thị Diễm', 'phamthidiem@gmail.com', '0923456789',
        '1992-12-08', 'Nữ', '321 Lê Lợi, Q. 1, TP. Hồ Chí Minh', '2024-04-12 16:45:00', 'Active');

INSERT INTO customers (customer_id, full_name, email, phone, dob, gender, address, created_at, status)
VALUES (55555555-5555-5555-5555-555555555555, 'Hoàng Văn Em', 'hoangvanem@hotmail.com', '0934567890',
        '1985-05-18', 'Nam', '654 Trần Phú, Q. Hà Đông, Hà Nội', '2024-05-20 11:10:00', 'Active');

-- Index để tìm kiếm nhanh hơn
CREATE INDEX IF NOT EXISTS idx_customers_email ON customers (email);
CREATE INDEX IF NOT EXISTS idx_customers_phone ON customers (phone);


---------- 2) PRODUCTS
CREATE TABLE IF NOT EXISTS products (
  product_id text,
  brand text,
  model text,
  cpu text,
  ram int,
  storage text,
  price decimal,
  available boolean,
  image text,
  created_at timestamp,
  PRIMARY KEY (product_id)
);

INSERT INTO products (product_id, brand, model, cpu, ram, storage, price, available, image, created_at)
VALUES ('LP001', 'Apple', 'MacBook Air 13 M2 256GB', 'Apple M2 8-core', 8, '256GB SSD', 27990000, true, 'macbook_air_m2_256gb.jpg', '2024-01-01 08:00:00');

INSERT INTO products (product_id, brand, model, cpu, ram, storage, price, available, image, created_at)
VALUES ('LP002', 'Apple', 'MacBook Pro 14 M3 512GB', 'Apple M3 8-core', 16, '512GB SSD', 49990000, true, 'macbook_pro_14_m3_512gb.jpg', '2024-01-01 08:00:00');

INSERT INTO products (product_id, brand, model, cpu, ram, storage, price, available, image, created_at)
VALUES ('LP003', 'Dell', 'XPS 13 Plus i7', 'Intel Core i7-1360P', 16, '512GB SSD', 35990000, true, 'dell_xps_13_plus_i7.jpg', '2024-01-01 08:00:00');

INSERT INTO products (product_id, brand, model, cpu, ram, storage, price, available, image, created_at)
VALUES ('LP004', 'HP', 'Spectre x360 14', 'Intel Core i7-1355U', 16, '1TB SSD', 38990000, true, 'hp_spectre_x360_14.jpg', '2024-01-01 08:00:00');

INSERT INTO products (product_id, brand, model, cpu, ram, storage, price, available, image, created_at)
VALUES ('LP005', 'Lenovo', 'ThinkPad X1 Carbon Gen11', 'Intel Core i7-1365U', 16, '512GB SSD', 42990000, true, 'lenovo_thinkpad_x1_carbon_gen11.jpg', '2024-01-01 08:00:00');

INSERT INTO products (product_id, brand, model, cpu, ram, storage, price, available, image, created_at)
VALUES ('LP006', 'Asus', 'ZenBook 14 OLED', 'Intel Core i5-1340P', 16, '512GB SSD', 23990000, true, 'asus_zenbook_14_oled.jpg', '2024-01-01 08:00:00');

INSERT INTO products (product_id, brand, model, cpu, ram, storage, price, available, image, created_at)
VALUES ('LP007', 'MSI', 'Modern 14 C13M', 'Intel Core i5-1335U', 8, '512GB SSD', 16990000, true, 'msi_modern_14_c13m.jpg', '2024-01-01 08:00:00');

INSERT INTO products (product_id, brand, model, cpu, ram, storage, price, available, image, created_at)
VALUES ('LP008', 'Acer', 'Swift 3 SF314', 'AMD Ryzen 5 7530U', 8, '256GB SSD', 14990000, true, 'acer_swift_3_sf314.jpg', '2024-01-01 08:00:00');

INSERT INTO products (product_id, brand, model, cpu, ram, storage, price, available, image, created_at)
VALUES ('LP009', 'Asus', 'TUF Gaming A15', 'AMD Ryzen 7 7735HS', 16, '512GB SSD', 24990000, true, 'asus_tuf_gaming_a15.jpg', '2024-01-01 08:00:00');

INSERT INTO products (product_id, brand, model, cpu, ram, storage, price, available, image, created_at)
VALUES ('LP010', 'LG', 'Gram 17 2024', 'Intel Core i7-1360P', 16, '512GB SSD', 31990000, false, 'lg_gram_17_2024.jpg', '2024-01-01 08:00:00');

-- Index để tìm kiếm sản phẩm nhanh hơn
CREATE INDEX IF NOT EXISTS idx_products_brand ON products (brand);
CREATE INDEX IF NOT EXISTS idx_products_available ON products (available);


--------- 3) ORDERS BY CUSTOMER (và UDT)
CREATE TYPE IF NOT EXISTS order_item (
  product_id text,
  model text,
  qty int,
  price decimal
);

CREATE TABLE IF NOT EXISTS orders_by_customer (
  customer_id uuid,
  yyyy_mm text,               -- YYYY-MM
  order_date timestamp,
  order_id uuid,
  total decimal,
  items list<frozen<order_item>>,
  status text,
  PRIMARY KEY ((customer_id, yyyy_mm), order_date, order_id)
) WITH CLUSTERING ORDER BY (order_date DESC, order_id ASC);

------- 4) ORDERS BY ID
CREATE TABLE IF NOT EXISTS orders_by_id (
  order_id uuid,
  customer_id uuid,
  order_date timestamp,
  total decimal,
  items list<frozen<order_item>>,
  status text,
  PRIMARY KEY (order_id)
);


-- Index để query orders theo status
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders_by_id (status);


----------- 5) LOYALTY ACCOUNTS
CREATE TABLE IF NOT EXISTS loyalty_accounts (
  customer_id uuid,
  points bigint,
  tier text,
  lifetime_spent decimal,
  order_count int,
  last_updated timestamp,
  PRIMARY KEY (customer_id)
);


-- Index để query loyalty theo tier
CREATE INDEX IF NOT EXISTS idx_loyalty_tier ON loyalty_accounts (tier);

-- =========================
-- SAMPLE QUERIES  - Có thể bỏ comment để test
-- =========================

-- Q1: tìm theo email/phone (có index, không dùng ALLOW FILTERING)
-- SELECT * FROM customers WHERE email='nguyenvanan@gmail.com';
-- SELECT * FROM customers WHERE phone='0901234567';

-- Q2: xem chi tiết KH theo primary key
-- SELECT * FROM customers WHERE customer_id=11111111-1111-1111-1111-111111111111;

-- Q3: tìm sản phẩm theo product_id
-- SELECT * FROM products WHERE product_id='LP001';

-- Q4: liệt kê theo brand và available (có index)
-- SELECT * FROM products WHERE brand='Apple';
-- SELECT * FROM products WHERE available=true;

-- Q5: lịch sử đơn hàng theo tháng (query pattern chính)
-- SELECT * FROM orders_by_customer
-- WHERE customer_id=11111111-1111-1111-1111-111111111111 AND yyyy_mm='2024-10';

-- Q6: tra cứu đơn hàng theo order_id
-- SELECT * FROM orders_by_id WHERE order_id=aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1;

-- Q7: xem loyalty theo customer_id
-- SELECT points, tier, lifetime_spent, order_count FROM loyalty_accounts
-- WHERE customer_id=11111111-1111-1111-1111-111111111111;

-- Q8: xem theo tier (có index)
-- SELECT customer_id, tier, lifetime_spent FROM loyalty_accounts WHERE tier='Gold';

-- Q9: xem orders theo status (có index)
-- SELECT order_id, customer_id, total, status FROM orders_by_id WHERE status='Pending';

