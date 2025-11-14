CREATE KEYSPACE IF NOT EXISTS QLKHTT
WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};

USE QLKHTT;

===========================================
1) CUSTOMERS  (customer_id = text)
===========================================
CREATE TABLE IF NOT EXISTS customers (
  customer_id text,
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

-- Index
CREATE INDEX IF NOT EXISTS idx_customers_email ON customers (email);
CREATE INDEX IF NOT EXISTS idx_customers_phone ON customers (phone);


===========================================
2) PRODUCTS  (product_id = text)
===========================================

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

-- Index
CREATE INDEX IF NOT EXISTS idx_products_brand ON products (brand);
CREATE INDEX IF NOT EXISTS idx_products_available ON products (available);

-- Insert
INSERT INTO products (product_id, brand, model, cpu, ram, storage, price, available, image, created_at)
VALUES ('SP001', 'Apple', 'MacBook Air 13 M2 256GB', 'Apple M2 8-core', 8, '256GB SSD', 27990000, true, 'macbook_air_m2_256gb.jpg', '2024-01-01 08:00:00');

INSERT INTO products (product_id, brand, model, cpu, ram, storage, price, available, image, created_at)
VALUES ('SP002', 'Apple', 'MacBook Pro 14 M3 512GB', 'Apple M3 8-core', 16, '512GB SSD', 49990000, true, 'macbook_pro_14_m3_512gb.jpg', '2024-01-01 08:00:00');

INSERT INTO products (product_id, brand, model, cpu, ram, storage, price, available, image, created_at)
VALUES ('SP003', 'Dell', 'XPS 13 Plus i7', 'Intel Core i7-1360P', 16, '512GB SSD', 35990000, true, 'dell_xps_13_plus_i7.jpg', '2024-01-01 08:00:00');

INSERT INTO products (product_id, brand, model, cpu, ram, storage, price, available, image, created_at)
VALUES ('SP004', 'HP', 'Spectre x360 14', 'Intel Core i7-1355U', 16, '1TB SSD', 38990000, true, 'hp_spectre_x360_14.jpg', '2024-01-01 08:00:00');

INSERT INTO products (product_id, brand, model, cpu, ram, storage, price, available, image, created_at)
VALUES ('SP005', 'Lenovo', 'ThinkPad X1 Carbon Gen11', 'Intel Core i7-1365U', 16, '512GB SSD', 42990000, true, 'lenovo_thinkpad_x1_carbon_gen11.jpg', '2024-01-01 08:00:00');

INSERT INTO products (product_id, brand, model, cpu, ram, storage, price, available, image, created_at)
VALUES ('SP006', 'Asus', 'ZenBook 14 OLED', 'Intel Core i5-1340P', 16, '512GB SSD', 23990000, true, 'asus_zenbook_14_oled.jpg', '2024-01-01 08:00:00');

INSERT INTO products (product_id, brand, model, cpu, ram, storage, price, available, image, created_at)
VALUES ('SP007', 'MSI', 'Modern 14 C13M', 'Intel Core i5-1335U', 8, '512GB SSD', 16990000, true, 'msi_modern_14_c13m.jpg', '2024-01-01 08:00:00');

INSERT INTO products (product_id, brand, model, cpu, ram, storage, price, available, image, created_at)
VALUES ('SP008', 'Acer', 'Swift 3 SF314', 'AMD Ryzen 5 7530U', 8, '256GB SSD', 14990000, true, 'acer_swift_3_sf314.jpg', '2024-01-01 08:00:00');

INSERT INTO products (product_id, brand, model, cpu, ram, storage, price, available, image, created_at)
VALUES ('SP009', 'Asus', 'TUF Gaming A15', 'AMD Ryzen 7 7735HS', 16, '512GB SSD', 24990000, true, 'asus_tuf_gaming_a15.jpg', '2024-01-01 08:00:00');

INSERT INTO products (product_id, brand, model, cpu, ram, storage, price, available, image, created_at)
VALUES ('SP010', 'LG', 'Gram 17 2024', 'Intel Core i7-1360P', 16, '512GB SSD', 31990000, false, 'lg_gram_17_2024.jpg', '2024-01-01 08:00:00');


===========================================
3) ORDERS + UDT  (order_id = text)
===========================================

CREATE TYPE IF NOT EXISTS order_item (
  product_id text,
  model text,
  qty int,
  price decimal
);

-- Orders by Customer (Phân vùng theo customer + tháng)
CREATE TABLE IF NOT EXISTS orders_by_customer (
  customer_id text,
  yyyy_mm text,                -- YYYY-MM
  order_date timestamp,
  order_id text,
  total decimal,
  items list<frozen<order_item>>,
  status text,
  PRIMARY KEY ((customer_id, yyyy_mm), order_date, order_id)
) WITH CLUSTERING ORDER BY (order_date DESC, order_id ASC);

-- Orders by ID
CREATE TABLE IF NOT EXISTS orders_by_id (
  order_id text,
  customer_id text,
  order_date timestamp,
  total decimal,
  items list<frozen<order_item>>,
  status text,
  PRIMARY KEY (order_id)
);

-- Index order status
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders_by_id (status);
select * from orders_by_id

===========================================
4) LOYALTY ACCOUNTS  (customer_id = text)
===========================================

CREATE TABLE IF NOT EXISTS loyalty_accounts (
  customer_id text,
  points bigint,
  tier text,
  lifetime_spent decimal,
  order_count int,
  last_updated timestamp,
  PRIMARY KEY (customer_id)
);

-- Index
CREATE INDEX IF NOT EXISTS idx_loyalty_tier ON loyalty_accounts (tier);
