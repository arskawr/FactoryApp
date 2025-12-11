package server;

import shared.Protocol;
import shared.models.*;
import server.database.DatabaseManager;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
            
            output = new ObjectOutputStream(clientSocket.getOutputStream());
            output.flush();
            input = new ObjectInputStream(clientSocket.getInputStream());
            
            gui.log("Потоки созданы для клиента " + getClientInfo());
            
            gui.addClientToTable(clientInfo, clientSocket.getPort(), 
                "Подключен", "Не аутентифицирован", connectTime.toString());
            
            while (!clientSocket.isClosed() && server.isRunning()) {
                try {
                    Object obj = input.readObject();
                    
                    if (obj instanceof Protocol.Message) {
                        Protocol.Message message = (Protocol.Message) obj;
                        gui.log("Получено сообщение от " + getClientInfo() + ": " + message.getType());
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
                    
                case REGISTER_REQUEST:
                    @SuppressWarnings("unchecked")
                    Map<String, String> regData = (Map<String, String>) message.getData();
                    String regUsername = regData.get("username");
                    String regPassword = regData.get("password");
                    String fullName = regData.get("fullName");
                    String email = regData.get("email");
                    String phone = regData.get("phone");
                    String address = regData.get("address");

                    boolean regSuccess = dbManager.registerUser(regUsername, regPassword, fullName,
                            "CUSTOMER", email, phone, address);

                    sendMessage(new Protocol.Message(Protocol.MessageType.REGISTER_RESPONSE,
                            null, regSuccess, regSuccess ? null : "Логин уже существует"));

                    if (regSuccess) {
                        gui.log("Зарегистрирован новый покупатель: " + regUsername);
                    }
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
                case UPDATE_ORDER_STATUS:
                    handleUpdateOrderStatus(message);
                    break;
                case DELETE_ORDER:
                    handleDeleteOrder(message);
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
                case DELETE_RAW_MATERIAL:
                    handleDeleteRawMaterial(message);
                    break;
                case PING:
                    sendMessage(new Protocol.Message(Protocol.MessageType.PING, "pong"));
                    break;
                default:
                    sendError("Неизвестная команда: " + message.getType());
                    break;
            }
        } catch (Exception e) {
            gui.log("Ошибка обработки сообщения от " + getClientInfo() + ": " + e.toString());
            e.printStackTrace();
            sendError("Ошибка сервера");
        }
    }
    
    private void handleLogin(Protocol.Message message) {
        Object data = message.getData();
        if (!(data instanceof String)) {
            sendError("Неверный формат данных для входа");
            return;
        }

        String[] parts = ((String) data).split(":");
        if (parts.length < 2) {
            sendError("Неверный формат данных для входа");
            return;
        }

        String username = parts[0];
        String password = parts[1];

        User user = dbManager.authenticateUser(username, password);

        if (user != null) {
            authenticated = true;
            currentUser = user;
            role = user.getRole();

            gui.log("Клиент " + getClientInfo() + " аутентифицирован как " + username + " (" + role + ")");
            gui.updateClientInTable(clientInfo, "Аутентифицирован", role.getDisplayName());

            sendMessage(new Protocol.Message(Protocol.MessageType.LOGIN_RESPONSE, user, true, null));
        } else {
            sendMessage(new Protocol.Message(Protocol.MessageType.ERROR, null, false, "Неверные логин или пароль"));
            gui.log("Неудачная попытка входа от " + getClientInfo());
        }
    }

    private void handleLogout() {
        authenticated = false;
        currentUser = null;
        role = null;
        gui.updateClientInTable(clientInfo, "Подключен", "Не аутентифицирован");
        sendMessage(new Protocol.Message(Protocol.MessageType.LOGOUT, "Выход выполнен", true, null));
    }

    private void handleGetProducts() {
        if (!checkAuthentication()) return;
        List<Product> products = dbManager.getAllProducts();
        sendMessage(new Protocol.Message(Protocol.MessageType.GET_PRODUCTS, products, true, null));
    }

    private void handleAddProduct(Protocol.Message message) {
        if (!checkAuthentication()) return;
        if (message.getData() instanceof Product product) {
            boolean success = dbManager.addProduct(product);
            sendMessage(new Protocol.Message(Protocol.MessageType.ADD_PRODUCT, null, success, success ? null : "Ошибка добавления"));
            if (success) gui.log("Добавлен продукт: " + product.getName());
        } else {
            sendError("Неверный формат продукта");
        }
    }

    private void handleUpdateProduct(Protocol.Message message) {
        if (!checkAuthentication()) return;
        if (message.getData() instanceof Product product) {
            boolean success = dbManager.updateProduct(product);
            sendMessage(new Protocol.Message(Protocol.MessageType.UPDATE_PRODUCT, null, success, success ? null : "Ошибка обновления"));
            if (success) gui.log("Обновлен продукт: " + product.getName());
        } else {
            sendError("Неверный формат продукта");
        }
    }

    private void handleDeleteProduct(Protocol.Message message) {
        if (!checkAuthentication()) return;
        if (message.getData() instanceof Integer id) {
            boolean success = dbManager.deleteProduct(id);
            sendMessage(new Protocol.Message(Protocol.MessageType.DELETE_PRODUCT, null, success, success ? null : "Ошибка удаления"));
            if (success) gui.log("Удален продукт ID: " + id);
        } else {
            sendError("Неверный ID продукта");
        }
    }

    private void handleGetOrders() {
        if (!checkAuthentication()) return;
        List<Order> orders = dbManager.getAllOrders();
        sendMessage(new Protocol.Message(Protocol.MessageType.GET_ORDERS, orders, true, null));
    }

    private void handleAddOrder(Protocol.Message message) {
        if (!checkAuthentication()) return;
        if (message.getData() instanceof Order order) {
            int orderId = dbManager.addOrder(order, currentUser.getId());
            boolean success = orderId > 0;
            sendMessage(new Protocol.Message(Protocol.MessageType.ADD_ORDER, success ? orderId : null, success, success ? null : "Ошибка создания заказа"));
            if (success) gui.log("Создан заказ ID: " + orderId + " пользователем " + currentUser.getUsername());
        } else {
            sendError("Неверный формат заказа");
        }
    }

    private void handleUpdateOrderStatus(Protocol.Message message) {
        if (!checkAuthentication()) return;
        if (message.getData() instanceof Map<?, ?> rawMap) {
            @SuppressWarnings("unchecked")
            Map<String, Object> statusData = (Map<String, Object>) rawMap;
            int orderId = (Integer) statusData.get("orderId");
            String status = (String) statusData.get("status");
            boolean success = dbManager.updateOrderStatus(orderId, status);
            sendMessage(new Protocol.Message(Protocol.MessageType.UPDATE_ORDER_STATUS, null, success, success ? null : "Ошибка"));
            if (success) gui.log("Обновлен статус заказа " + orderId + " на " + status);
        } else {
            sendError("Неверный формат данных статуса");
        }
    }

    private void handleDeleteOrder(Protocol.Message message) {
        if (!checkAuthentication()) return;
        if (message.getData() instanceof Integer id) {
            boolean success = dbManager.deleteOrder(id);
            sendMessage(new Protocol.Message(Protocol.MessageType.DELETE_ORDER, null, success, success ? null : "Ошибка"));
            if (success) gui.log("Удален заказ ID: " + id);
        } else {
            sendError("Неверный ID заказа");
        }
    }

    private void handleGetRawMaterials() {
        if (!checkAuthentication()) return;
        List<RawMaterial> materials = dbManager.getAllRawMaterials();
        sendMessage(new Protocol.Message(Protocol.MessageType.GET_RAW_MATERIALS, materials, true, null));
    }

    private void handleAddRawMaterial(Protocol.Message message) {
        if (!checkAuthentication()) return;
        if (message.getData() instanceof RawMaterial material) {
            boolean success = dbManager.addRawMaterial(material);
            sendMessage(new Protocol.Message(Protocol.MessageType.ADD_RAW_MATERIAL, null, success, success ? null : "Ошибка"));
            if (success) gui.log("Добавлено сырье: " + material.getName());
        } else {
            sendError("Неверный формат сырья");
        }
    }

    private void handleUpdateRawMaterial(Protocol.Message message) {
        if (!checkAuthentication()) return;
        if (message.getData() instanceof RawMaterial material) {
            boolean success = dbManager.updateRawMaterial(material);
            sendMessage(new Protocol.Message(Protocol.MessageType.UPDATE_RAW_MATERIAL, null, success, success ? null : "Ошибка"));
            if (success) gui.log("Обновлено сырье: " + material.getName());
        } else {
            sendError("Неверный формат сырья");
        }
    }

    private void handleDeleteRawMaterial(Protocol.Message message) {
        if (!checkAuthentication()) return;
        if (message.getData() instanceof Integer id) {
            boolean success = dbManager.deleteRawMaterial(id);
            sendMessage(new Protocol.Message(Protocol.MessageType.DELETE_RAW_MATERIAL, null, success, success ? null : "Ошибка"));
            if (success) gui.log("Удалено сырье ID: " + id);
        } else {
            sendError("Неверный ID сырья");
        }
    }

    private boolean checkAuthentication() {
        if (!authenticated) {
            sendError("Требуется аутентификация");
            return false;
        }
        return true;
    }
    
    private void sendError(String errorMessage) {
        sendMessage(new Protocol.Message(Protocol.MessageType.ERROR, null, false, errorMessage));
    }
    
    public void sendMessage(Protocol.Message message) {
        try {
            if (output != null) {
                output.writeObject(message);
                output.flush();
            }
        } catch (IOException e) {
            gui.log("Ошибка отправки клиенту " + getClientInfo() + ": " + e.getMessage());
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
            gui.log("Ошибка закрытия сокета: " + e.getMessage());
        } finally {
            server.removeClient(this);
            gui.removeClientFromTable(clientInfo);
            gui.log("Клиент отключён: " + clientInfo);
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