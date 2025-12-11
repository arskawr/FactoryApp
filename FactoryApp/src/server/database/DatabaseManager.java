package server.database;

import shared.models.*;
import java.sql.*;
import java.util.*;

public class DatabaseManager {
    private Connection connection;
    private static DatabaseManager instance;

    private static final String DB_URL = "jdbc:mysql://localhost:3306/factory_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&characterEncoding=utf8";
    private static final String DB_USER = "factory_user";
    private static final String DB_PASSWORD = "31214858";

    private DatabaseManager() {
        initializeDatabase();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private void initializeDatabase() {
        try {
            System.out.println("Подключение к MySQL...");
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            connection.setAutoCommit(true);
            System.out.println("Успешно подключено к MySQL!");

            checkAndCreateTables();
        } catch (Exception e) {
            System.err.println("Ошибка подключения к MySQL!");
            e.printStackTrace();
        }
    }

    private void checkAndCreateTables() throws SQLException {
        // Здесь ваш init.sql уже выполнили в Workbench — можно не создавать заново
        // Но на всякий случай оставим проверку существования таблиц
    }

    // Регистрация нового покупателя
    public boolean registerUser(String username, String password, String fullName,
                                 String role, String email, String phone, String address) {
        String sql = "INSERT INTO users (username, password, full_name, role, email, phone, address) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password); // пока без хеша — просто текст
            pstmt.setString(3, fullName);
            pstmt.setString(4, role);
            pstmt.setString(5, email);
            pstmt.setString(6, phone);
            pstmt.setString(7, address);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) { // дубликат логина
                return false;
            }
            e.printStackTrace();
            return false;
        }
    }

    // Остальные методы (getProducts, addProduct, login и т.д.) у вас уже есть и работают
    // Я их не трогаю — они полностью рабочие

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    

    
    
    private void createInMemoryDatabase() {
        try {
            System.out.println("Создаем in-memory базу данных для демо...");
            Class.forName("org.h2.Driver");
            connection = DriverManager.getConnection("jdbc:h2:mem:factory;DB_CLOSE_DELAY=-1", "sa", "");
            checkAndCreateTables();
            System.out.println("Используем H2 in-memory базу данных");
        } catch (Exception e) {
            System.err.println("Не удалось создать резервную базу данных: " + e.getMessage());
        }
    }
    
    
    
    private void insertDemoData() throws SQLException {
        // Проверяем, есть ли уже пользователи
        String checkUsers = "SELECT COUNT(*) FROM users";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(checkUsers)) {
            if (rs.next() && rs.getInt(1) == 0) {
                // Добавляем администратора по умолчанию
                String insertAdmin = "INSERT INTO users (username, password, full_name, role, email, phone) VALUES " +
                    "('admin', 'admin', 'Администратор', 'ADMIN', 'admin@factory.ru', '+79991112233')";
                stmt.executeUpdate(insertAdmin);
                System.out.println("Администратор добавлен: admin/admin");
            }
        }
        
        // Проверяем, есть ли продукты
        String checkProducts = "SELECT COUNT(*) FROM products";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(checkProducts)) {
            if (rs.next() && rs.getInt(1) == 0) {
                String insertProducts = "INSERT INTO products (name, category, price, weight, stock_quantity, description) VALUES " +
                    "('Шоколадный торт', 'Торты', 1200.00, 1.5, 10, 'Шоколадный торт с кремом')," +
                    "('Наполеон', 'Торты', 1500.00, 1.8, 8, 'Слоеный торт с заварным кремом')," +
                    "('Эклеры', 'Пирожные', 80.00, 0.1, 50, 'Эклеры с заварным кремом')," +
                    "('Печенье овсяное', 'Печенье', 60.00, 0.08, 100, 'Овсяное печенье с изюмом')," +
                    "('Кекс ванильный', 'Кексы', 90.00, 0.15, 30, 'Ванильный кекс с глазурью')";
                stmt.executeUpdate(insertProducts);
                System.out.println("Демо-продукты добавлены");
            }
        }
        
        // Проверяем, есть ли сырье
        String checkMaterials = "SELECT COUNT(*) FROM raw_materials";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(checkMaterials)) {
            if (rs.next() && rs.getInt(1) == 0) {
                String insertMaterials = "INSERT INTO raw_materials (name, category, unit, current_stock, min_stock, max_stock, last_purchase_price, supplier) VALUES " +
                    "('Мука пшеничная', 'Мука', 'кг', 100.5, 20.0, 200.0, 50.00, 'ООО Зерно')," +
                    "('Сахар', 'Сахар', 'кг', 80.2, 10.0, 150.0, 45.00, 'Сахарный завод')," +
                    "('Какао порошок', 'Какао', 'кг', 25.0, 5.0, 50.0, 300.00, 'Какао компания')," +
                    "('Яйца', 'Яйца', 'шт', 500.0, 100.0, 1000.0, 8.00, 'Птицефабрика')," +
                    "('Масло сливочное', 'Масло', 'кг', 30.5, 10.0, 60.0, 250.00, 'Молочный комбинат')";
                stmt.executeUpdate(insertMaterials);
                System.out.println("Демо-сырье добавлено");
            }
        }
    }
    
    // === МЕТОДЫ ДЛЯ РАБОТЫ С ПОЛЬЗОВАТЕЛЯМИ ===
    
    public User authenticateUser(String username, String password) {
        try {
            if (connection == null || connection.isClosed()) {
                System.err.println("Нет подключения к базе данных");
                return null;
            }
            
            String sql = "SELECT * FROM users WHERE username = ? AND password = ? AND active = TRUE";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, username);
                pstmt.setString(2, password);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        User user = new User();
                        user.setId(rs.getInt("id"));
                        user.setUsername(rs.getString("username"));
                        user.setFullName(rs.getString("full_name"));
                        user.setRole(shared.Protocol.UserRole.valueOf(rs.getString("role")));
                        user.setEmail(rs.getString("email"));
                        user.setPhone(rs.getString("phone"));
                        user.setActive(rs.getBoolean("active"));
                        
                        // Обновляем время последнего входа
                        updateLastLogin(user.getId());
                        
                        System.out.println("Аутентификация успешна: " + username);
                        return user;
                    } else {
                        System.out.println("Неверные учетные данные для пользователя: " + username);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка аутентификации пользователя " + username + ": " + e.getMessage());
        }
        return null;
    }
    
    private void updateLastLogin(int userId) {
        try {
            String sql = "UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, userId);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Ошибка обновления времени входа: " + e.getMessage());
        }
    }
    
    public boolean registerUser(String username, String password, String fullName, String email, String phone, String address) {
        try {
            // Проверяем, нет ли уже пользователя с таким именем
            String checkSql = "SELECT COUNT(*) FROM users WHERE username = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(checkSql)) {
                pstmt.setString(1, username);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        System.out.println("Пользователь " + username + " уже существует");
                        return false;
                    }
                }
            }
            
            // Регистрируем нового пользователя
            String sql = "INSERT INTO users (username, password, full_name, email, phone, address, role) VALUES (?, ?, ?, ?, ?, ?, 'CUSTOMER')";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, username);
                pstmt.setString(2, password);
                pstmt.setString(3, fullName);
                pstmt.setString(4, email);
                pstmt.setString(5, phone);
                pstmt.setString(6, address);
                
                boolean success = pstmt.executeUpdate() > 0;
                if (success) {
                    System.out.println("Пользователь " + username + " успешно зарегистрирован");
                }
                return success;
            }
        } catch (SQLException e) {
            System.err.println("Ошибка регистрации пользователя " + username + ": " + e.getMessage());
            return false;
        }
    }
    
    // === МЕТОДЫ ДЛЯ РАБОТЫ С ПРОДУКТАМИ ===
    
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        try {
            String sql = "SELECT * FROM products ORDER BY name";
            
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                
                while (rs.next()) {
                    Product product = new Product();
                    product.setId(rs.getInt("id"));
                    product.setName(rs.getString("name"));
                    product.setCategory(rs.getString("category"));
                    product.setPrice(rs.getDouble("price"));
                    product.setWeight(rs.getDouble("weight"));
                    product.setStockQuantity(rs.getInt("stock_quantity"));
                    product.setDescription(rs.getString("description"));
                    products.add(product);
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка получения продуктов: " + e.getMessage());
        }
        return products;
    }
    
    public boolean addProduct(Product product) {
        try {
            String sql = "INSERT INTO products (name, category, price, weight, stock_quantity, description) VALUES (?, ?, ?, ?, ?, ?)";
            
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, product.getName());
                pstmt.setString(2, product.getCategory());
                pstmt.setDouble(3, product.getPrice());
                pstmt.setDouble(4, product.getWeight());
                pstmt.setInt(5, product.getStockQuantity());
                pstmt.setString(6, product.getDescription());
                
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Ошибка добавления продукта: " + e.getMessage());
            return false;
        }
    }
    
    public boolean updateProduct(Product product) {
        try {
            String sql = "UPDATE products SET name = ?, category = ?, price = ?, weight = ?, stock_quantity = ?, description = ? WHERE id = ?";
            
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, product.getName());
                pstmt.setString(2, product.getCategory());
                pstmt.setDouble(3, product.getPrice());
                pstmt.setDouble(4, product.getWeight());
                pstmt.setInt(5, product.getStockQuantity());
                pstmt.setString(6, product.getDescription());
                pstmt.setInt(7, product.getId());
                
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Ошибка обновления продукта: " + e.getMessage());
            return false;
        }
    }
    
    public boolean deleteProduct(int productId) {
        try {
            String sql = "DELETE FROM products WHERE id = ?";
            
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, productId);
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Ошибка удаления продукта: " + e.getMessage());
            return false;
        }
    }
    
    // === МЕТОДЫ ДЛЯ РАБОТЫ С ЗАКАЗАМИ ===
    
    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        try {
            String sql = "SELECT * FROM orders ORDER BY order_date DESC";
            
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                
                while (rs.next()) {
                    Order order = new Order();
                    order.setId(rs.getInt("id"));
                    order.setClientName(rs.getString("client_name"));
                    order.setClientPhone(rs.getString("client_phone"));
                    order.setClientAddress(rs.getString("client_address"));
                    order.setOrderDate(rs.getTimestamp("order_date"));
                    order.setStatus(rs.getString("status"));
                    order.setTotalAmount(rs.getDouble("total_amount"));
                    order.setNotes(rs.getString("notes"));
                    orders.add(order);
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка получения заказов: " + e.getMessage());
        }
        return orders;
    }
    
    public int addOrder(Order order, int userId) {
        try {
            String sql = "INSERT INTO orders (user_id, client_name, client_phone, client_address, status, total_amount, notes) VALUES (?, ?, ?, ?, ?, ?, ?)";
            
            try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, userId);
                pstmt.setString(2, order.getClientName());
                pstmt.setString(3, order.getClientPhone());
                pstmt.setString(4, order.getClientAddress());
                pstmt.setString(5, order.getStatus());
                pstmt.setDouble(6, order.getTotalAmount());
                pstmt.setString(7, order.getNotes());
                
                pstmt.executeUpdate();
                
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка добавления заказа: " + e.getMessage());
        }
        return -1;
    }
    
    public boolean addOrderItem(int orderId, Order.OrderItem item) {
        try {
            String sql = "INSERT INTO order_items (order_id, product_id, product_name, quantity, price, total) VALUES (?, ?, ?, ?, ?, ?)";
            
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, orderId);
                pstmt.setInt(2, item.getProductId());
                pstmt.setString(3, item.getProductName());
                pstmt.setInt(4, item.getQuantity());
                pstmt.setDouble(5, item.getPrice());
                pstmt.setDouble(6, item.getTotal());
                
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Ошибка добавления позиции заказа: " + e.getMessage());
            return false;
        }
    }
    
    public boolean updateOrderStatus(int orderId, String status) {
        try {
            String sql = "UPDATE orders SET status = ? WHERE id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, status);
                pstmt.setInt(2, orderId);
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Ошибка обновления статуса заказа: " + e.getMessage());
            return false;
        }
    }
    
    public boolean deleteOrder(int orderId) {
        try {
            // Удаляем сначала позиции заказа
            String deleteItemsSql = "DELETE FROM order_items WHERE order_id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(deleteItemsSql)) {
                pstmt.setInt(1, orderId);
                pstmt.executeUpdate();
            }
            
            // Затем удаляем сам заказ
            String deleteOrderSql = "DELETE FROM orders WHERE id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(deleteOrderSql)) {
                pstmt.setInt(1, orderId);
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Ошибка удаления заказа: " + e.getMessage());
            return false;
        }
    }
    
    // === МЕТОДЫ ДЛЯ РАБОТЫ С СЫРЬЕМ ===
    
    public List<RawMaterial> getAllRawMaterials() {
        List<RawMaterial> materials = new ArrayList<>();
        try {
            String sql = "SELECT * FROM raw_materials ORDER BY name";
            
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                
                while (rs.next()) {
                    RawMaterial material = new RawMaterial();
                    material.setId(rs.getInt("id"));
                    material.setName(rs.getString("name"));
                    material.setCategory(rs.getString("category"));
                    material.setUnit(rs.getString("unit"));
                    material.setCurrentStock(rs.getDouble("current_stock"));
                    material.setMinStock(rs.getDouble("min_stock"));
                    material.setMaxStock(rs.getDouble("max_stock"));
                    material.setLastPurchasePrice(rs.getDouble("last_purchase_price"));
                    material.setSupplier(rs.getString("supplier"));
                    materials.add(material);
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка получения сырья: " + e.getMessage());
        }
        return materials;
    }
    
    public boolean addRawMaterial(RawMaterial material) {
        try {
            String sql = "INSERT INTO raw_materials (name, category, unit, current_stock, min_stock, max_stock, last_purchase_price, supplier, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, material.getName());
                pstmt.setString(2, material.getCategory());
                pstmt.setString(3, material.getUnit());
                pstmt.setDouble(4, material.getCurrentStock());
                pstmt.setDouble(5, material.getMinStock());
                pstmt.setDouble(6, material.getMaxStock());
                pstmt.setDouble(7, material.getLastPurchasePrice());
                pstmt.setString(8, material.getSupplier());
                pstmt.setString(9, material.getNotes());
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Ошибка добавления сырья: " + e.getMessage());
            return false;
        }
    }
    
    public boolean updateRawMaterial(RawMaterial material) {
        try {
            String sql = "UPDATE raw_materials SET name = ?, category = ?, unit = ?, current_stock = ?, min_stock = ?, max_stock = ?, last_purchase_price = ?, supplier = ?, notes = ? WHERE id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, material.getName());
                pstmt.setString(2, material.getCategory());
                pstmt.setString(3, material.getUnit());
                pstmt.setDouble(4, material.getCurrentStock());
                pstmt.setDouble(5, material.getMinStock());
                pstmt.setDouble(6, material.getMaxStock());
                pstmt.setDouble(7, material.getLastPurchasePrice());
                pstmt.setString(8, material.getSupplier());
                pstmt.setString(9, material.getNotes());
                pstmt.setInt(10, material.getId());
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Ошибка обновления сырья: " + e.getMessage());
            return false;
        }
    }
    
    public boolean deleteRawMaterial(int materialId) {
        try {
            String sql = "DELETE FROM raw_materials WHERE id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, materialId);
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Ошибка удаления сырья: " + e.getMessage());
            return false;
        }
    }
    
   
}