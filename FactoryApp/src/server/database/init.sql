-- Таблица пользователей
CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role ENUM('ADMIN', 'MANAGER', 'TECHNOLOGIST', 'WAREHOUSE_MANAGER', 'CUSTOMER') NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    address VARCHAR(200),
    active BOOLEAN DEFAULT TRUE,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Таблица продуктов
CREATE TABLE IF NOT EXISTS products (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    weight DECIMAL(10, 3),
    stock_quantity INT DEFAULT 0,
    description TEXT,
    image_url VARCHAR(200),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Таблица заказов
CREATE TABLE IF NOT EXISTS orders (
    id INT PRIMARY KEY AUTO_INCREMENT,
    client_name VARCHAR(100) NOT NULL,
    client_phone VARCHAR(20) NOT NULL,
    client_address VARCHAR(200),
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    delivery_date TIMESTAMP NULL,
    status ENUM('Новый', 'В обработке', 'Готов', 'Отгружен', 'Доставлен', 'Отменен') DEFAULT 'Новый',
    total_amount DECIMAL(10, 2) DEFAULT 0,
    notes TEXT,
    user_id INT,
    created_by INT,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (created_by) REFERENCES users(id)
);

-- Таблица позиций заказа
CREATE TABLE IF NOT EXISTS order_items (
    id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    product_name VARCHAR(100) NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    total DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Таблица сырья
CREATE TABLE IF NOT EXISTS raw_materials (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    unit VARCHAR(20) NOT NULL,
    current_stock DECIMAL(10, 3) DEFAULT 0,
    min_stock DECIMAL(10, 3) DEFAULT 10,
    max_stock DECIMAL(10, 3) DEFAULT 100,
    last_purchase_price DECIMAL(10, 2) DEFAULT 0,
    last_delivery_date TIMESTAMP NULL,
    supplier VARCHAR(100),
    storage_location VARCHAR(100),
    quality_grade ENUM('A', 'B', 'C') DEFAULT 'A',
    expiration_date TIMESTAMP NULL,
    barcode VARCHAR(50),
    notes TEXT
);

-- Вставка тестовых данных
INSERT INTO users (username, password, full_name, role, email, phone) VALUES
('admin', 'admin', 'Администратор Системы', 'ADMIN', 'admin@factory.ru', '+79991112233'),
('manager', 'manager123', 'Менеджер Отдела', 'MANAGER', 'manager@factory.ru', '+79992223344'),
('tech', 'tech123', 'Технолог Производства', 'TECHNOLOGIST', 'tech@factory.ru', '+79993334455'),
('warehouse', 'warehouse123', 'Кладовщик Склад', 'WAREHOUSE_MANAGER', 'warehouse@factory.ru', '+79994445566'),
('customer1', 'customer123', 'Иван Иванов', 'CUSTOMER', 'customer@mail.ru', '+79995556677');

INSERT INTO products (name, category, price, weight, stock_quantity, description) VALUES
('Торт Шоколадный', 'Торты', 1200.00, 1.5, 10, 'Шоколадный торт с кремом'),
('Торт Наполеон', 'Торты', 1500.00, 1.8, 8, 'Слоеный торт с заварным кремом'),
('Пирожное Эклер', 'Пирожные', 80.00, 0.1, 50, 'Эклеры с заварным кремом'),
('Печенье Овсяное', 'Печенье', 60.00, 0.08, 100, 'Овсяное печенье с изюмом'),
('Кекс Ванильный', 'Кексы', 90.00, 0.15, 30, 'Ванильный кекс с глазурью');

INSERT INTO raw_materials (name, category, unit, current_stock, min_stock, max_stock, last_purchase_price, supplier) VALUES
('Мука пшеничная', 'Мука', 'кг', 100.5, 20.0, 200.0, 50.00, 'ООО Зерно'),
('Сахар', 'Сахар', 'кг', 80.2, 10.0, 150.0, 45.00, 'Сахарный завод'),
('Какао порошок', 'Какао', 'кг', 25.0, 5.0, 50.0, 300.00, 'Какао компания'),
('Яйца', 'Яйца', 'шт', 500.0, 100.0, 1000.0, 8.00, 'Птицефабрика'),
('Масло сливочное', 'Масло', 'кг', 30.5, 10.0, 60.0, 250.00, 'Молочный комбинат');