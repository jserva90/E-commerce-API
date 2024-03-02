-- Create Product table
CREATE TABLE products (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price NUMERIC(10, 2) NOT NULL
);

-- Create Order table
CREATE TABLE orders (
    id UUID PRIMARY KEY,
    status VARCHAR(50) NOT NULL,
    discount NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    paid NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    returns NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    total NUMERIC(10, 2) NOT NULL DEFAULT 0.00
);

-- Create OrderItem table
CREATE TABLE order_items (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    replaced_with_id UUID NULL,
    is_replaced BOOLEAN NOT NULL DEFAULT false,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (product_id) REFERENCES products(id),
    FOREIGN KEY (replaced_with_id) REFERENCES order_items(id)
);

-- Populate Product table
INSERT INTO products (id, name, price) VALUES
(123, 'Ketchup', 0.45),
(456, 'Beer', 2.33),
(879, 'Õllesnäkk', 0.42),
(999, '75" OLED TV', 1333.37);