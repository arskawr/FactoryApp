package server;

import shared.Protocol;
import shared.models.*;
import server.database.DatabaseManager;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private MainServer server;
    private ServerGUI gui;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private boolean authenticated;
    private User currentUser;
    private Protocol.UserRole role;
    private Date connectTime;
    private String clientInfo;
    private DatabaseManager dbManager;
    
    public ClientHandler(Socket socket, MainServer server, ServerGUI gui) {
        this.clientSocket = socket;
        this.server = server;
        this.gui = gui;
        this.authenticated = false;
        this.connectTime = new Date();
        this.clientInfo = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
        this.dbManager = DatabaseManager.getInstance();
    }
    
    @Override
    public void run() {
        try {
            gui.log("Обработка клиента " + getClientInfo() + " начата");
            
            // Создаем потоки
            output = new ObjectOutputStream(clientSocket.getOutputStream());
            output.flush();
            input = new ObjectInputStream(clientSocket.getInputStream());
            
            gui.log("Потоки созданы для клиента " + getClientInfo());
            
            // Добавляем клиента в таблицу
            gui.addClientToTable(clientInfo, clientSocket.getPort(), 
                "Подключен", "Не аутентифицирован", connectTime.toString());
            
            // Обработка сообщений
            while (!clientSocket.isClosed() && server.isRunning()) {
                try {
                    Object obj = input.readObject();
                    
                    if (obj instanceof Protocol.Message) {
                        Protocol.Message message = (Protocol.Message) obj;
                        gui.log("Получено сообщение от " + getClientInfo() + 
                               ": " + message.getType());
                        processMessage(message);
                    }
                    
                } catch (ClassNotFoundException e) {
                    gui.log("Ошибка чтения сообщения от " + getClientInfo() + ": " + e.getMessage());
                    sendError("Неизвестный формат сообщения");
                } catch (EOFException e) {
                    gui.log("Клиент " + getClientInfo() + " отключился (EOF)");
                    break;
                } catch (SocketException e) {
                    gui.log("Соединение с клиентом " + getClientInfo() + " разорвано");
                    break;
                }
            }
            
        } catch (IOException e) {
            gui.log("Ошибка соединения с " + getClientInfo() + ": " + e.getMessage());
        } finally {
            disconnect();
        }
    }
    
    private void processMessage(Protocol.Message message) {
        try {
            switch (message.getType()) {
                case LOGIN_REQUEST:
                    handleLogin(message);
                    break;
                case LOGOUT:
                    handleLogout();
                    break;
                    
                case GET_PRODUCTS:
                    handleGetProducts();
                    break;
                case ADD_PRODUCT:
                    handleAddProduct(message);
                    break;
                case UPDATE_PRODUCT:
                    handleUpdateProduct(message);
                    break;
                case DELETE_PRODUCT:
                    handleDeleteProduct(message);
                    break;
                    
                case GET_ORDERS:
                    handleGetOrders();
                    break;
                case ADD_ORDER:
                    handleAddOrder(message);
                    break;
                case UPDATE_ORDER:
                    handleUpdateOrder(message);
                    break;
                case DELETE_ORDER:
                    handleDeleteOrder(message);
                    break;
                case UPDATE_ORDER_STATUS:
                    handleUpdateOrderStatus(message);
                    break;
                    
                case GET_RAW_MATERIALS:
                    handleGetRawMaterials();
                    break;
                case ADD_RAW_MATERIAL:
                    handleAddRawMaterial(message);
                    break;
                case UPDATE_RAW_MATERIAL:
                    handleUpdateRawMaterial(message);
                    break;
                    
                case GET_RECIPES:
                    handleGetRecipes();
                    break;
                case ADD_RECIPE:
                    handleAddRecipe(message);
                    break;
                    
                case EXECUTE_COMMAND:
                    handleExecuteCommand(message);
                    break;
                case BACKUP_DATABASE:
                    handleBackupDatabase();
                    break;
                case EXPORT_DATA:
                    handleExportData(message);
                    break;
                    
                case PING:
                    sendMessage(new Protocol.Message(Protocol.MessageType.PING, "pong"));
                    break;
                    
                default:
                    sendError("Неизвестная команда: " + message.getType());
                    break;
            }
        } catch (Exception e) {
            gui.log("Ошибка обработки сообщения от " + getClientInfo() + ": " + e.getMessage());
            sendError("Ошибка сервера: " + e.getMessage());
        }
    }
    
private void handleLogin(Protocol.Message message) {
    try {
        Object data = message.getData();
        if (data instanceof String) {
            String authData = (String) data;
            String[] parts = authData.split(":");
            
            if (parts.length >= 2) {
                String username = parts[0];
                String password = parts[1];
                
                // Аутентификация через БД
                User user = dbManager.authenticateUser(username, password);
                
                if (user != null) {
                    authenticated = true;
                    currentUser = user;
                    role = user.getRole();
                    
                    gui.log("Клиент " + getClientInfo() + " аутентифицирован как " + 
                           username + " (роль: " + role + ")");
                    gui.updateClientInTable(clientInfo, "Аутентифицирован", role.toString());
                    
                    // Отправляем успешный ответ с данными пользователя
                    Protocol.Message response = new Protocol.Message(
                        Protocol.MessageType.LOGIN_RESPONSE, // ТЕПЕРЬ ПРАВИЛЬНО
                        user,
                        true,
                        null
                    );
                    sendMessage(response);
                } else {
                    Protocol.Message response = new Protocol.Message(
                        Protocol.MessageType.ERROR,
                        "Неверные логин или пароль",
                        false,
                        "Ошибка аутентификации"
                    );
                    sendMessage(response);
                    gui.log("Неудачная попытка входа от " + getClientInfo() + 
                           " (логин: " + username + ")");
                }
            } else {
                sendError("Неверный формат данных для входа");
            }
        } else {
            sendError("Неверный тип данных для входа");
        }
    } catch (SQLException e) {
        gui.log("Ошибка аутентификации: " + e.getMessage());
        sendError("Ошибка базы данных при аутентификации");
    }

}


    private void handleLogout() {
        authenticated = false;
        currentUser = null;
        gui.updateClientInTable(clientInfo, "Отключен", "Не аутентифицирован");
        sendMessage(new Protocol.Message(Protocol.MessageType.LOGOUT, "Вы успешно вышли из системы", true, null));
    }
    
    private void handleGetProducts() throws SQLException {
        if (!checkAuthentication()) return;
        
        List<Product> products = dbManager.getAllProducts();
        sendMessage(new Protocol.Message(Protocol.MessageType.GET_PRODUCTS, products, true, null));
        gui.log("Отправлен список продуктов клиенту " + getClientInfo());
    }
    
    private void handleAddProduct(Protocol.Message message) throws SQLException {
        if (!checkAuthentication()) return;
        if (!checkRole(Protocol.UserRole.ADMIN, Protocol.UserRole.MANAGER, Protocol.UserRole.TECHNOLOGIST)) return;
        
        if (message.getData() instanceof Product) {
            Product product = (Product) message.getData();
            boolean success = dbManager.addProduct(product);
            
            if (success) {
                sendMessage(new Protocol.Message(Protocol.MessageType.ADD_PRODUCT, 
                    "Продукт добавлен успешно", true, null));
                gui.log("Клиент " + getClientInfo() + " добавил продукт: " + product.getName());
            } else {
                sendError("Не удалось добавить продукт");
            }
        } else {
            sendError("Неверный формат данных продукта");
        }
    }
    
    private void handleUpdateProduct(Protocol.Message message) throws SQLException {
        if (!checkAuthentication()) return;
        if (!checkRole(Protocol.UserRole.ADMIN, Protocol.UserRole.MANAGER, Protocol.UserRole.TECHNOLOGIST)) return;
        
        if (message.getData() instanceof Product) {
            Product product = (Product) message.getData();
            boolean success = dbManager.updateProduct(product);
            
            if (success) {
                sendMessage(new Protocol.Message(Protocol.MessageType.UPDATE_PRODUCT, 
                    "Продукт обновлен успешно", true, null));
                gui.log("Клиент " + getClientInfo() + " обновил продукт: " + product.getName());
            } else {
                sendError("Не удалось обновить продукт");
            }
        } else {
            sendError("Неверный формат данных продукта");
        }
    }
    
    private void handleDeleteProduct(Protocol.Message message) throws SQLException {
        if (!checkAuthentication()) return;
        if (!checkRole(Protocol.UserRole.ADMIN)) return;
        
        if (message.getData() instanceof Integer) {
            int productId = (Integer) message.getData();
            boolean success = dbManager.deleteProduct(productId);
            
            if (success) {
                sendMessage(new Protocol.Message(Protocol.MessageType.DELETE_PRODUCT, 
                    "Продукт удален успешно", true, null));
                gui.log("Клиент " + getClientInfo() + " удалил продукт ID: " + productId);
            } else {
                sendError("Не удалось удалить продукт");
            }
        } else {
            sendError("Неверный формат ID продукта");
        }
    }
    
    private void handleGetOrders() throws SQLException {
        if (!checkAuthentication()) return;
        
        List<Order> orders = dbManager.getAllOrders();
        sendMessage(new Protocol.Message(Protocol.MessageType.GET_ORDERS, orders, true, null));
        gui.log("Отправлен список заказов клиенту " + getClientInfo());
    }
    
    private void handleAddOrder(Protocol.Message message) throws SQLException {
        if (!checkAuthentication()) return;
        if (!checkRole(Protocol.UserRole.ADMIN, Protocol.UserRole.MANAGER)) return;
        
        if (message.getData() instanceof Order) {
            Order order = (Order) message.getData();
            int orderId = dbManager.addOrder(order, currentUser.getId());
            
            if (orderId > 0) {
                // Добавляем позиции заказа
                for (Order.OrderItem item : order.getItems()) {
                    dbManager.addOrderItem(orderId, item);
                }
                
                sendMessage(new Protocol.Message(Protocol.MessageType.ADD_ORDER, 
                    orderId, true, null));
                gui.log("Клиент " + getClientInfo() + " создал заказ ID: " + orderId);
            } else {
                sendError("Не удалось создать заказ");
            }
        } else {
            sendError("Неверный формат данных заказа");
        }
    }
    
    
    
    private void handleGetRawMaterials() throws SQLException {
        if (!checkAuthentication()) return;
        if (!checkRole(Protocol.UserRole.ADMIN, Protocol.UserRole.WAREHOUSE_MANAGER, Protocol.UserRole.TECHNOLOGIST)) return;
        
        List<RawMaterial> materials = dbManager.getAllRawMaterials();
        sendMessage(new Protocol.Message(Protocol.MessageType.GET_RAW_MATERIALS, materials, true, null));
        gui.log("Отправлен список сырья клиенту " + getClientInfo());
    }
    

    
    private void handleGetRecipes() {
        // Временная реализация
        sendMessage(new Protocol.Message(Protocol.MessageType.GET_RECIPES, 
            new java.util.ArrayList<Recipe>(), true, null));
    }
    
    private void handleAddRecipe(Protocol.Message message) {
        // Временная реализация
        sendMessage(new Protocol.Message(Protocol.MessageType.ADD_RECIPE, 
            "Добавление рецепта пока не реализовано", false, "Функция в разработке"));
    }
    
    private void handleExecuteCommand(Protocol.Message message) {
        if (!checkAuthentication()) return;
        if (!checkRole(Protocol.UserRole.ADMIN)) return;
        
        Object data = message.getData();
        if (data instanceof String) {
            String command = (String) data;
            
            try {
                // Выполняем команду операционной системы
                Process process = Runtime.getRuntime().exec(new String[]{"cmd.exe", "/c", command});
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                StringBuilder output = new StringBuilder();
                String line;
                
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
                
                int exitCode = process.waitFor();
                
                if (exitCode == 0) {
                    sendMessage(new Protocol.Message(Protocol.MessageType.EXECUTE_COMMAND, 
                        output.toString(), true, null));
                    gui.log("Клиент " + getClientInfo() + " выполнил команду: " + command);
                } else {
                    sendError("Команда завершилась с ошибкой, код: " + exitCode);
                }
            } catch (Exception e) {
                sendError("Ошибка выполнения команды: " + e.getMessage());
            }
        } else {
            sendError("Неверный формат команды");
        }
    }
    
    private void handleBackupDatabase() {
        if (!checkAuthentication()) return;
        if (!checkRole(Protocol.UserRole.ADMIN)) return;
        
        try {
            // Создаем резервную копию базы данных
            String backupDir = "backup";
            new File(backupDir).mkdirs();
            
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String backupFile = backupDir + "/factory_backup_" + timestamp + ".db";
            
            Files.copy(Paths.get("database/factory.db"), Paths.get(backupFile), 
                      StandardCopyOption.REPLACE_EXISTING);
            
            sendMessage(new Protocol.Message(Protocol.MessageType.BACKUP_DATABASE, 
                "Резервная копия создана: " + backupFile, true, null));
            gui.log("Клиент " + getClientInfo() + " создал резервную копию БД");
        } catch (Exception e) {
            sendError("Ошибка создания резервной копии: " + e.getMessage());
        }
    }
    
    private void handleExportData(Protocol.Message message) {
        if (!checkAuthentication()) return;
        if (!checkRole(Protocol.UserRole.ADMIN, Protocol.UserRole.MANAGER)) return;
        
        // Временная реализация
        sendMessage(new Protocol.Message(Protocol.MessageType.EXPORT_DATA, 
            "Экспорт данных пока не реализован", false, "Функция в разработке"));
    }
    
    private boolean checkAuthentication() {
        if (!authenticated) {
            sendError("Требуется аутентификация");
            return false;
        }
        return true;
    }
    
    private boolean checkRole(Protocol.UserRole... allowedRoles) {
        for (Protocol.UserRole allowedRole : allowedRoles) {
            if (role == allowedRole) {
                return true;
            }
        }
        sendError("Недостаточно прав. Требуется одна из ролей: " + 
                 Arrays.toString(allowedRoles));
        return false;
    }
    
    private void sendError(String errorMessage) {
        sendMessage(new Protocol.Message(Protocol.MessageType.ERROR, 
            null, false, errorMessage));
    }
    

private void handleDeleteRawMaterial(Protocol.Message message) throws SQLException {
    if (!checkAuthentication()) return;
    if (!checkRole(Protocol.UserRole.ADMIN, Protocol.UserRole.WAREHOUSE_MANAGER)) return;
    
    if (message.getData() instanceof Integer) {
        int materialId = (Integer) message.getData();
        boolean success = dbManager.deleteRawMaterial(materialId);
        
        if (success) {
            sendMessage(new Protocol.Message(Protocol.MessageType.DELETE_RAW_MATERIAL, 
                "Сырье удалено успешно", true, null));
            gui.log("Клиент " + getClientInfo() + " удалил сырье ID: " + materialId);
        } else {
            sendError("Не удалось удалить сырье");
        }
    } else {
        sendError("Неверный формат ID сырья");
    }
}







    public void sendMessage(Protocol.Message message) {
        try {
            if (output != null) {
                output.writeObject(message);
                output.flush();
                gui.log("Отправлено сообщение клиенту " + getClientInfo() + 
                       ": " + message.getType());
            }
        } catch (IOException e) {
            gui.log("Ошибка отправки сообщения клиенту " + getClientInfo() + ": " + e.getMessage());
        }
    }
    
private void handleUpdateOrder(Protocol.Message message) throws SQLException {
    if (!checkAuthentication()) return;
    if (!checkRole(Protocol.UserRole.ADMIN, Protocol.UserRole.MANAGER)) return;
    
    if (message.getData() instanceof Order) {
        Order order = (Order) message.getData();
        boolean success = dbManager.updateOrderStatus(order.getId(), order.getStatus());
        
        if (success) {
            sendMessage(new Protocol.Message(Protocol.MessageType.UPDATE_ORDER, 
                "Заказ обновлен успешно", true, null));
            gui.log("Клиент " + getClientInfo() + " обновил заказ ID: " + order.getId());
        } else {
            sendError("Не удалось обновить заказ");
        }
    } else {
        sendError("Неверный формат данных заказа");
    }
}

private void handleDeleteOrder(Protocol.Message message) throws SQLException {
    if (!checkAuthentication()) return;
    if (!checkRole(Protocol.UserRole.ADMIN)) return;
    
    if (message.getData() instanceof Integer) {
        int orderId = (Integer) message.getData();
        boolean success = dbManager.deleteOrder(orderId);
        
        if (success) {
            sendMessage(new Protocol.Message(Protocol.MessageType.DELETE_ORDER, 
                "Заказ удален успешно", true, null));
            gui.log("Клиент " + getClientInfo() + " удалил заказ ID: " + orderId);
        } else {
            sendError("Не удалось удалить заказ");
        }
    } else {
        sendError("Неверный формат ID заказа");
    }
}

private void handleUpdateOrderStatus(Protocol.Message message) throws SQLException {
    if (!checkAuthentication()) return;
    if (!checkRole(Protocol.UserRole.ADMIN, Protocol.UserRole.MANAGER)) return;
    
    if (message.getData() instanceof Map) {
        @SuppressWarnings("unchecked")
        Map<String, Object> statusData = (Map<String, Object>) message.getData();
        int orderId = (Integer) statusData.get("orderId");
        String status = (String) statusData.get("status");
        
        boolean success = dbManager.updateOrderStatus(orderId, status);
        
        if (success) {
            sendMessage(new Protocol.Message(Protocol.MessageType.UPDATE_ORDER_STATUS, 
                "Статус заказа обновлен", true, null));
            gui.log("Клиент " + getClientInfo() + " обновил статус заказа ID: " + orderId + " на " + status);
        } else {
            sendError("Не удалось обновить статус заказа");
        }
    } else {
        sendError("Неверный формат данных для изменения статуса");
    }
}

private void handleAddRawMaterial(Protocol.Message message) throws SQLException {
    if (!checkAuthentication()) return;
    if (!checkRole(Protocol.UserRole.ADMIN, Protocol.UserRole.WAREHOUSE_MANAGER)) return;
    
    if (message.getData() instanceof RawMaterial) {
        RawMaterial material = (RawMaterial) message.getData();
        boolean success = dbManager.addRawMaterial(material);
        
        if (success) {
            sendMessage(new Protocol.Message(Protocol.MessageType.ADD_RAW_MATERIAL, 
                "Сырье добавлено успешно", true, null));
            gui.log("Клиент " + getClientInfo() + " добавил сырье: " + material.getName());
        } else {
            sendError("Не удалось добавить сырье");
        }
    } else {
        sendError("Неверный формат данных сырья");
    }
}

private void handleUpdateRawMaterial(Protocol.Message message) throws SQLException {
    if (!checkAuthentication()) return;
    if (!checkRole(Protocol.UserRole.ADMIN, Protocol.UserRole.WAREHOUSE_MANAGER)) return;
    
    if (message.getData() instanceof RawMaterial) {
        RawMaterial material = (RawMaterial) message.getData();
        boolean success = dbManager.updateRawMaterial(material);
        
        if (success) {
            sendMessage(new Protocol.Message(Protocol.MessageType.UPDATE_RAW_MATERIAL, 
                "Сырье обновлено успешно", true, null));
            gui.log("Клиент " + getClientInfo() + " обновил сырье: " + material.getName());
        } else {
            sendError("Не удалось обновить сырье");
        }
    } else {
        sendError("Неверный формат данных сырья");
    }
}

    public void disconnect() {
        try {
            if (input != null) input.close();
            if (output != null) output.close();
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            gui.log("Ошибка при отключении клиента: " + e.getMessage());
        } finally {
            server.removeClient(this);
            gui.log("Клиент " + getClientInfo() + " отключен");
        }
    }
    
    public boolean isAuthenticated() { 
        return authenticated; 
    }
    
    public String getClientInfo() { 
        return clientInfo; 
    }
    
    public String getUsername() { 
        return currentUser != null ? currentUser.getUsername() : "Не аутентифицирован"; 
    }
    
    public Protocol.UserRole getRole() { 
        return role; 
    }
    
    public Date getConnectTime() {
        return connectTime;
    }
}