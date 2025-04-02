CREATE TABLE IF NOT EXISTS category (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    parent_category VARCHAR(255),
    active BOOLEAN DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_category_name ON category(name);
CREATE INDEX IF NOT EXISTS idx_parent_category ON category(parent_category);

CREATE TABLE IF NOT EXISTS product (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    available BOOLEAN DEFAULT TRUE,
    category_id BIGINT,
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES category(id)
); 

CREATE INDEX IF NOT EXISTS idx_product_category ON product(category_id);

CREATE TABLE IF NOT EXISTS app_user (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    role VARCHAR(20) NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP,
    CONSTRAINT chk_role CHECK (role IN ('ADMIN', 'USER', 'MANAGER'))
);

CREATE INDEX IF NOT EXISTS idx_username ON app_user(username);
CREATE INDEX IF NOT EXISTS idx_email ON app_user(email);

