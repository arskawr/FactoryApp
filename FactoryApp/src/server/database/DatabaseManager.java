package server.database;

import shared.models.*;
import shared.Protocol;
import java.sql.*;
import java.util.*;

public class DatabaseManager {
    private Connection connection;
    private static DatabaseManager instance;
    
    // ↓↓↓ ВАШИ ДАННЫЕ БД ЗДЕСЬ ↓↓↓
    private static final String DB_URL = "jdbc:mysql://localhost:3306/factory_db";
    private static final String DB_USER = "factory_user";
    private static final String DB_PASSWORD = "31214858"; // ИЗМЕНИТЕ НА СВОЙ ПАРОЛЬ!
    // ↑↑↑ ИЗМЕНИТЕ ПАРОЛЬ НА ВАШ ↑↑↑
    
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
            // Загружаем драйвер MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Подключаемся к базе данных
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            connection.setAutoCommit(true);
            
            System.out.println("Успешное подключение к MySQL!");
            
            // Проверяем таблицы и создаем демо-данные
            checkAndCreateTables();
            
        } catch (Exception e) {
            System.err.println("ОШИБКА ПОДКЛЮЧЕНИЯ К БАЗЕ ДАННЫХ: " + e.getMessage());
            System.err.println("URL: " + DB_URL);
            System.err.println("User: " + DB_USER);
            e.printStackTrace();
            
            // Если не удалось подключиться, используем временную память
            try {
                Class.forName("org.h2.Driver");
                connection = DriverManager.getConnection("jdbc:h2:mem:factory;DB_CLOSE_DELAY=-1", "sa", "");
                System.out.println("Используем H2 базу данных в памяти");
                checkAndCreateTables();
            } catch (Exception ex) {
                System.err.println("Не удалось создать резервную базу данных: " + ex.getMessage());
            }
        }
    }
    
    private void checkAndCreateTables() throws SQLException {
        // Проверяем, есть ли таблицы
        DatabaseMetaData meta = connection.getMetaData();
        ResultSet tables = meta.getTables(null, null, "users", null);
        
        if (!tables.next()) {
            createTables();
            insertDemoData();
            System.out.println("Таблицы и демо-данные созданы");
        } else {
            System.out.println("Таблицы уже существуют");
        }
        tables.close();
    }
    
    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Таблица пользователей
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "username VARCHAR(50) UNIQUE NOT NULL," +
                    "password VARCHAR(100) NOT NULL," +
                    "full_name VARCHAR(100) NOT NULL," +
                    "role VARCHAR(20) NOT NULL," +
                    "email VARCHAR(100)," +
                    "phone VARCHAR(20)," +
                    "active BOOLEAN DEFAULT TRUE," +
                    "created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");
            
            // Таблица продуктов
            stmt.execute("CREATE TABLE IF NOT EXISTS products (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "name VARCHAR(100) NOT NULL," +
                    "category VARCHAR(50) NOT NULL," +
                    "price DECIMAL(10,2) NOT NULL," +
                    "weight DECIMAL(10,3)," +
                    "stock_quantity INT DEFAULT 0," +
                    "description TEXT," +
                    "created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                    ")");
            
            // Таблица заказов
            stmt.execute("CREATE TABLE IF NOT EXISTS orders (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "client_name VARCHAR(100) NOT NULL," +
                    "client_phone VARCHAR(20) NOT NULL," +
                    "client_address VARCHAR(200)," +
                    "order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "delivery_date TIMESTAMP NULL," +
                    "status VARCHAR(20) DEFAULT 'Новый'," +
                    "total_amount DECIMAL(10,2) DEFAULT 0," +
                    "notes TEXT," +
                    "created_by INT," +
                    "FOREIGN KEY (created_by) REFERENCES users(id)" +
                    ")");
            
            // Таблица позиций заказа
            stmt.execute("CREATE TABLE IF NOT EXISTS order_items (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "order_id INT NOT NULL," +
                    "product_id INT NOT NULL," +
                    "product_name VARCHAR(100) NOT NULL," +
                    "quantity INT NOT NULL," +
                    "price DECIMAL(10,2) NOT NULL," +
                    "total DECIMAL(10,2) NOT NULL," +
                    "FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE," +
                    "FOREIGN KEY (product_id) REFERENCES products(id)" +
                    ")");
            
            // Таблица сырья (ДОБАВЛЯЕМ)
            stmt.execute("CREATE TABLE IF NOT EXISTS raw_materials (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "name VARCHAR(100) NOT NULL," +
                    "category VARCHAR(50) NOT NULL," +
                    "unit VARCHAR(20) NOT NULL," +
                    "current_stock DECIMAL(10,3) DEFAULT 0," +
                    "min_stock DECIMAL(10,3) DEFAULT 10," +
                    "max_stock DECIMAL(10,3) DEFAULT 100," +
                    "last_purchase_price DECIMAL(10,2) DEFAULT 0," +
                    "last_delivery_date TIMESTAMP NULL," +
                    "supplier VARCHAR(100)," +
                    "storage_location VARCHAR(100)," +
                    "quality_grade VARCHAR(10) DEFAULT 'A'," +
                    "expiration_date TIMESTAMP NULL," +
                    "barcode VARCHAR(50)," +
                    "notes TEXT" +
                    ")");
            
            System.out.println("Все таблицы созданы успешно");
        }
    }
    
    private void insertDemoData() throws SQLException {
        // Проверяем, есть ли уже пользователи
        String checkSql = "SELECT COUNT(*) FROM users";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(checkSql)) {
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("Демо-данные уже существуют");
                return;
            }
        }
        
        System.out.println("Добавление демо-данных...");
        
        // Добавляем пользователей
        String userSql = "INSERT INTO users (username, password, full_name, role, email, phone) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(userSql)) {
            String[][] users = {
                {"admin", "admin", "Администратор Системы", "ADMIN", "admin@factory.ru", "+79991112233"},
                {"manager", "manager123", "Менеджер Отдела", "MANAGER", "manager@factory.ru", "+79992223344"},
                {"tech", "tech123", "Технолог Производства", "TECHNOLOGIST", "tech@factory.ru", "+79993334455"},
                {"warehouse", "warehouse123", "Кладовщик Склад", "WAREHOUSE_MANAGER", "warehouse@factory.ru", "+79994445566"}
            };
            
            for (String[] user : users) {
                pstmt.setString(1, user[0]);
                pstmt.setString(2, user[1]);
                pstmt.setString(3, user[2]);
                pstmt.setString(4, user[3]);
                pstmt.setString(5, user[4]);
                pstmt.setString(6, user[5]);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
        
        // Добавляем продукты
        String productSql = "INSERT INTO products (name, category, price, weight, stock_quantity, description) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(productSql)) {
            Object[][] products = {
                {"Торт Шоколадный", "Торты", 1200.0, 1.5, 10, "Шоколадный торт с кремом"},
                {"Торт Наполеон", "Торты", 1500.0, 1.8, 8, "Слоеный торт с заварным кремом"},
                {"Пирожное Эклер", "Пирожные", 80.0, 0.1, 50, "Эклеры с заварным кремом"},
                {"Печенье Овсяное", "Печенье", 60.0, 0.08, 100, "Овсяное печенье с изюмом"},
                {"Кекс Ванильный", "Кексы", 90.0, 0.15, 30, "Ванильный кекс с глазурью"},
                {"Чизкейк Классический", "Десерты", 800.0, 0.9, 15, "Классический чизкейк на бисквите"}
            };
            
            for (Object[] product : products) {
                pstmt.setString(1, (String) product[0]);
                pstmt.setString(2, (String) product[1]);
                pstmt.setDouble(3, (Double) product[2]);
                pstmt.setDouble(4, (Double) product[3]);
                pstmt.setInt(5, (Integer) product[4]);
                pstmt.setString(6, (String) product[5]);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
        
        // Добавляем сырье (ДОБАВЛЯЕМ)
        String rawMaterialSql = "INSERT INTO raw_materials (name, category, unit, current_stock, min_stock, max_stock, last_purchase_price, supplier) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(rawMaterialSql)) {
            Object[][] materials = {
                {"Мука пшеничная", "Мука", "кг", 100.5, 20.0, 200.0, 50.0, "ООО Зерно"},
                {"Сахар", "Сахар", "кг", 80.2, 10.0, 150.0, 45.0, "Сахарный завод"},
                {"Какао порошок", "Какао", "кг", 25.0, 5.0, 50.0, 300.0, "Какао компания"},
                {"Яйца", "Яйца", "шт", 500.0, 100.0, 1000.0, 8.0, "Птицефабрика"},
                {"Масло сливочное", "Масло", "кг", 30.5, 10.0, 60.0, 250.0, "Молочный комбинат"},
                {"Сливки", "Молочные", "л", 40.0, 15.0, 80.0, 120.0, "Молочный комбинат"}
            };
            
            for (Object[] material : materials) {
                pstmt.setString(1, (String) material[0]);
                pstmt.setString(2, (String) material[1]);
                pstmt.setString(3, (String) material[2]);
                pstmt.setDouble(4, (Double) material[3]);
                pstmt.setDouble(5, (Double) material[4]);
                pstmt.setDouble(6, (Double) material[5]);
                pstmt.setDouble(7, (Double) material[6]);
                pstmt.setString(8, (String) material[7]);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
        
        System.out.println("Демо-данные добавлены успешно");
    }
    
    // === МЕТОДЫ ДЛЯ РАБОТЫ С ПОЛЬЗОВАТЕЛЯМИ ===
    
    public User authenticateUser(String username, String password) throws SQLException {
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
                    user.setRole(Protocol.UserRole.valueOf(rs.getString("role")));
                    user.setEmail(rs.getString("email"));
                    user.setPhone(rs.getString("phone"));
                    user.setActive(rs.getBoolean("active"));
                    return user;
                }
            }
        }
        return null;
    }
    
    // === МЕТОДЫ ДЛЯ РАБОТЫ С ПРОДУКТАМИ ===
    
    public List<Product> getAllProducts() throws SQLException {
        List<Product> products = new ArrayList<>();
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
        return products;
    }
    
    public boolean addProduct(Product product) throws SQLException {
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
    }
    
    public boolean updateProduct(Product product) throws SQLException {
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
    }
    
    public boolean deleteProduct(int productId) throws SQLException {
        String sql = "DELETE FROM products WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            return pstmt.executeUpdate() > 0;
        }
    }
    
    // === МЕТОДЫ ДЛЯ РАБОТЫ С ЗАКАЗАМИ ===
    
    public List<Order> getAllOrders() throws SQLException {
        List<Order> orders = new ArrayList<>();
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
                order.setDeliveryDate(rs.getTimestamp("delivery_date"));
                order.setStatus(rs.getString("status"));
                order.setTotalAmount(rs.getDouble("total_amount"));
                order.setNotes(rs.getString("notes"));
                orders.add(order);
            }
        }
        return orders;
    }
    
    public int addOrder(Order order, int userId) throws SQLException {
        String sql = "INSERT INTO orders (client_name, client_phone, client_address, status, total_amount, notes, created_by) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, order.getClientName());
            pstmt.setString(2, order.getClientPhone());
            pstmt.setString(3, order.getClientAddress());
            pstmt.setString(4, order.getStatus());
            pstmt.setDouble(5, order.getTotalAmount());
            pstmt.setString(6, order.getNotes());
            pstmt.setInt(7, userId);
            
            pstmt.executeUpdate();
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        }
        return -1;
    }
    
    // ДОБАВЛЯЕМ ЭТОТ МЕТОД (отсутствовал)
    public boolean addOrderItem(int orderId, Order.OrderItem item) throws SQLException {
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
    }
    
    // ДОБАВЛЯЕМ ЭТОТ МЕТОД (отсутствовал)
    public List<RawMaterial> getAllRawMaterials() throws SQLException {
        List<RawMaterial> materials = new ArrayList<>();
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
                material.setLastDeliveryDate(rs.getTimestamp("last_delivery_date"));
                material.setSupplier(rs.getString("supplier"));
                materials.add(material);
            }
        } catch (SQLException e) {
            // Если таблицы нет, возвращаем пустой список
            System.err.println("Ошибка при получении сырья: " + e.getMessage());
        }
        return materials;
    }
    






// === МЕТОДЫ ДЛЯ РАБОТЫ С РЕЦЕПТАМИ ===

public List<Recipe> getAllRecipes() throws SQLException {
    List<Recipe> recipes = new ArrayList<>();
    String sql = "SELECT * FROM recipes ORDER BY name";
    
    try (Statement stmt = connection.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {
        
        while (rs.next()) {
            Recipe recipe = new Recipe();
            recipe.setId(rs.getInt("id"));
            recipe.setName(rs.getString("name"));
            recipe.setDescription(rs.getString("description"));
            recipe.setProductId(rs.getInt("product_id"));
            recipe.setProductName(rs.getString("product_name"));
            recipe.setOutputQuantity(rs.getDouble("output_quantity"));
            recipe.setUnit(rs.getString("unit"));
            recipe.setProductionTime(rs.getDouble("production_time"));
            recipe.setDifficulty(rs.getString("difficulty"));
            
            // Загружаем ингредиенты рецепта
            recipe.setIngredients(getRecipeIngredients(recipe.getId()));
            
            recipes.add(recipe);
        }
    }
    return recipes;
}

private List<Recipe.RecipeIngredient> getRecipeIngredients(int recipeId) throws SQLException {
    List<Recipe.RecipeIngredient> ingredients = new ArrayList<>();
    String sql = "SELECT * FROM recipe_ingredients WHERE recipe_id = ?";
    
    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
        pstmt.setInt(1, recipeId);
        try (ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Recipe.RecipeIngredient ingredient = new Recipe.RecipeIngredient();
                ingredient.setRawMaterialId(rs.getInt("raw_material_id"));
                ingredient.setRawMaterialName(rs.getString("raw_material_name"));
                ingredient.setQuantity(rs.getDouble("quantity"));
                ingredient.setUnit(rs.getString("unit"));
                ingredient.setNotes(rs.getString("notes"));
                ingredients.add(ingredient);
            }
        }
    }
    return ingredients;
}

public boolean addRecipe(Recipe recipe) throws SQLException {
    String sql = "INSERT INTO recipes (name, description, product_id, product_name, output_quantity, unit, production_time, difficulty) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    
    try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        pstmt.setString(1, recipe.getName());
        pstmt.setString(2, recipe.getDescription());
        pstmt.setInt(3, recipe.getProductId());
        pstmt.setString(4, recipe.getProductName());
        pstmt.setDouble(5, recipe.getOutputQuantity());
        pstmt.setString(6, recipe.getUnit());
        pstmt.setDouble(7, recipe.getProductionTime());
        pstmt.setString(8, recipe.getDifficulty());
        
        int affectedRows = pstmt.executeUpdate();
        if (affectedRows > 0) {
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int recipeId = generatedKeys.getInt(1);
                    
                    // Добавляем ингредиенты
                    for (Recipe.RecipeIngredient ingredient : recipe.getIngredients()) {
                        addRecipeIngredient(recipeId, ingredient);
                    }
                    return true;
                }
            }
        }
        return false;
    }
}

public boolean deleteOrder(int orderId) throws SQLException {
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
        e.printStackTrace();
        return false;
    }
}

public boolean updateOrderStatus(int orderId, String status) throws SQLException {
    String sql = "UPDATE orders SET status = ? WHERE id = ?";
    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
        pstmt.setString(1, status);
        pstmt.setInt(2, orderId);
        return pstmt.executeUpdate() > 0;
    }
}

public boolean addRawMaterial(RawMaterial material) throws SQLException {
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
}

public boolean updateRawMaterial(RawMaterial material) throws SQLException {
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
}

public boolean deleteRawMaterial(int materialId) throws SQLException {
    String sql = "DELETE FROM raw_materials WHERE id = ?";
    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
        pstmt.setInt(1, materialId);
        return pstmt.executeUpdate() > 0;
    }
}

// Метод для регистрации покупателя
public boolean registerCustomer(String username, String password, String fullName, String email, String phone, String address) throws SQLException {
    String sql = "INSERT INTO users (username, password, full_name, role, email, phone, address) VALUES (?, ?, ?, 'CUSTOMER', ?, ?, ?)";
    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
        pstmt.setString(1, username);
        pstmt.setString(2, password);
        pstmt.setString(3, fullName);
        pstmt.setString(4, email);
        pstmt.setString(5, phone);
        pstmt.setString(6, address);
        return pstmt.executeUpdate() > 0;
    }
}

// Получение заказов конкретного пользователя
public List<Order> getOrdersByUserId(int userId) throws SQLException {
    List<Order> orders = new ArrayList<>();
    String sql = "SELECT * FROM orders WHERE created_by = ? ORDER BY order_date DESC";
    
    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
        pstmt.setInt(1, userId);
        try (ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Order order = new Order();
                order.setId(rs.getInt("id"));
                order.setClientName(rs.getString("client_name"));
                order.setClientPhone(rs.getString("client_phone"));
                order.setClientAddress(rs.getString("client_address"));
                order.setOrderDate(rs.getTimestamp("order_date"));
                order.setDeliveryDate(rs.getTimestamp("delivery_date"));
                order.setStatus(rs.getString("status"));
                order.setTotalAmount(rs.getDouble("total_amount"));
                order.setNotes(rs.getString("notes"));
                
                // Получаем позиции заказа
                List<Order.OrderItem> items = getOrderItems(order.getId());
                order.setItems(items);
                
                orders.add(order);
            }
        }
    }
    return orders;
}

private List<Order.OrderItem> getOrderItems(int orderId) throws SQLException {
    List<Order.OrderItem> items = new ArrayList<>();
    String sql = "SELECT * FROM order_items WHERE order_id = ?";
    
    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
        pstmt.setInt(1, orderId);
        try (ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Order.OrderItem item = new Order.OrderItem();
                item.setProductId(rs.getInt("product_id"));
                item.setProductName(rs.getString("product_name"));
                item.setQuantity(rs.getInt("quantity"));
                item.setPrice(rs.getDouble("price"));
                items.add(item);
            }
        }
    }
    return items;
}


private boolean addRecipeIngredient(int recipeId, Recipe.RecipeIngredient ingredient) throws SQLException {
    String sql = "INSERT INTO recipe_ingredients (recipe_id, raw_material_id, raw_material_name, quantity, unit, notes) VALUES (?, ?, ?, ?, ?, ?)";
    
    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
        pstmt.setInt(1, recipeId);
        pstmt.setInt(2, ingredient.getRawMaterialId());
        pstmt.setString(3, ingredient.getRawMaterialName());
        pstmt.setDouble(4, ingredient.getQuantity());
        pstmt.setString(5, ingredient.getUnit());
        pstmt.setString(6, ingredient.getNotes());
        
        return pstmt.executeUpdate() > 0;
    }
}

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Соединение с базой данных закрыто");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при закрытии соединения с БД: " + e.getMessage());
        }
    }
}


