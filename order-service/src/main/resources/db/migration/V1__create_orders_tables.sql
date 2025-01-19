CREATE TABLE orders
(
    id           BIGSERIAL PRIMARY KEY,
    order_number UUID           NOT NULL UNIQUE,
    user_id      BIGINT        NOT NULL,
    status       VARCHAR(20)    NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    version      BIGINT                   DEFAULT 0,
    created_at   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT status_check CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'DELIVERED'))
);

CREATE TABLE order_items
(
    id          BIGSERIAL PRIMARY KEY,
    order_id    BIGINT         NOT NULL REFERENCES orders (id),
    product_id  BIGINT         NOT NULL,
    quantity    INTEGER        NOT NULL,
    unit_price  DECIMAL(10, 2) NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    version     BIGINT                   DEFAULT 0,
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT quantity_check CHECK (quantity > 0)
);

CREATE INDEX idx_orders_order_number ON orders (order_number);
CREATE INDEX idx_orders_user_id ON orders (user_id);
CREATE INDEX idx_order_items_order_id ON order_items (order_id);
CREATE INDEX idx_order_items_product_id ON order_items (product_id); 