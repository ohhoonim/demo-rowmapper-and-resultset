-- 장바구니 테이블
CREATE TABLE IF NOT EXISTS tb_cart (
    cart_id uuid,
    customer_id uuid NOT NULL,
    tag text,
    created_at TIMESTAMPTZ(6) NOT NULL DEFAULT NOW(),
    created_by TEXT NOT NULL,
    modified_at TIMESTAMPTZ(6) NOT NULL DEFAULT NOW(),
    modified_by TEXT NOT NULL,
    CONSTRAINT pk_tb_cart PRIMARY KEY (cart_id),
    CONSTRAINT uk_tb_cart_customer_id UNIQUE (customer_id)
);

-- 장바구니 항목 테이블
CREATE TABLE IF NOT EXISTS tb_cart_item (
    cart_item_id uuid,
    cart_id uuid NOT NULL,
    product_id uuid NOT NULL,
    product_name TEXT NOT NULL,
    product_base_price DECIMAL(19, 4) NOT NULL,
    product_image_url TEXT,
    option_id BIGINT NOT NULL,
    option_name TEXT NOT NULL,
    option_additional_price DECIMAL(19, 4) NOT NULL,
    quantity INTEGER NOT NULL,
    CONSTRAINT pk_tb_cart_item PRIMARY KEY (cart_item_id),
    CONSTRAINT fk_tb_cart_item_cart FOREIGN KEY (cart_id) REFERENCES tb_cart(cart_id) ON DELETE CASCADE
);

-- 장바구니 행동 로그 테이블
CREATE TABLE IF NOT EXISTS tb_cart_behavior_log (
    log_id bigint generated always as identity,
    customer_id uuid NOT NULL,
    behavior_type VARCHAR(50) NOT NULL,
    product_id uuid,
    created_at TIMESTAMPTZ(6) NOT NULL DEFAULT NOW(),
    created_by TEXT NOT NULL,
    CONSTRAINT pk_tb_cart_behavior_log PRIMARY KEY (log_id)
);

COMMENT ON TABLE tb_cart IS '장바구니 마스터';
COMMENT ON TABLE tb_cart_item IS '장바구니 상세 항목';
COMMENT ON TABLE tb_cart_behavior_log IS '장바구니 활동 로그';
