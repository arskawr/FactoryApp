package client;

import shared.Protocol;
import shared.models.*;
import java.io.*;
import java.net.*;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

public class NetworkClient {
    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private String host;
    private int port;
    private boolean connected;
    private String status;
    private User currentUser;
    
    public NetworkClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.connected = false;
        this.status = "Отключено";
        this.currentUser = null;
    }
    
    public boolean connect() {
        try {
            System.out.println("Попытка подключения к " + host + ":" + port);
            socket = new Socket(host, port);
            socket.setSoTimeout(5000);
            
            output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();
            
            input = new ObjectInputStream(socket.getInputStream());
            
            connected = true;
            status = "Подключено";
            System.out.println("Успешно подключено к серверу " + host + ":" + port);
            return true;
            
        } catch (ConnectException e) {
            System.err.println("Сервер не отвечает. Убедитесь, что сервер запущен.");
            status = "Сервер недоступен";
            return false;
        } catch (IOException e) {
            System.err.println("Ошибка подключения к серверу " + host + ":" + port + ": " + e.getMessage());
            status = "Ошибка подключения";
            return false;
        }
    }
    
    public User login(String username, String password) {
        if (!connected) {
            System.err.println("Не подключен к серверу, невозможно отправить запрос");
            return null;
        }
        
        try {
            String authData = username + ":" + password;
            Protocol.Message message = new Protocol.Message(Protocol.MessageType.LOGIN_REQUEST, authData);
            
            System.out.println("Отправка запроса аутентификации...");
            output.writeObject(message);
            output.flush();
            
            socket.setSoTimeout(10000);
            Protocol.Message response = (Protocol.Message) input.readObject();
            socket.setSoTimeout(0);
            
            if (response.isSuccess() && response.getData() instanceof User) {
                this.currentUser = (User) response.getData();
                System.out.println("Успешный вход как " + currentUser.getUsername());
                return currentUser;
            } else {
                System.err.println("Ошибка аутентификации: " + response.getErrorMessage());
                return null;
            }
            
        } catch (SocketTimeoutException e) {
            System.err.println("Таймаут ожидания ответа от сервера");
            disconnect();
            return null;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Ошибка при отправке запроса: " + e.getMessage());
            disconnect();
            return null;
        }
    }
    
        public boolean register(String username, String password, String fullName,
                            String email, String phone, String address) {
        if (!connected) return false;

        Map<String, String> data = new HashMap<>();
        data.put("username", username);
        data.put("password", password);
        data.put("fullName", fullName);
        data.put("email", email);
        data.put("phone", phone);
        data.put("address", address);

        Protocol.Message response = sendRequest(Protocol.MessageType.REGISTER_REQUEST, data);
        return response.isSuccess();
    }
    
    public Protocol.Message sendRequest(Protocol.MessageType type, Object data) {
        if (!connected) {
            return new Protocol.Message(Protocol.MessageType.ERROR, 
                "Не подключен к серверу", false, "Нет соединения");
        }
        
        try {
            Protocol.Message message = new Protocol.Message(type, data);
            output.writeObject(message);
            output.flush();
            
            socket.setSoTimeout(30000);
            Protocol.Message response = (Protocol.Message) input.readObject();
            socket.setSoTimeout(0);
            
            return response;
        } catch (SocketTimeoutException e) {
            System.err.println("Таймаут ожидания ответа");
            disconnect();
            return new Protocol.Message(Protocol.MessageType.ERROR, 
                null, false, "Таймаут ожидания ответа");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Ошибка отправки сообщения: " + e.getMessage());
            disconnect();
            return new Protocol.Message(Protocol.MessageType.ERROR, 
                null, false, "Ошибка связи: " + e.getMessage());
        }
    }
    
    public void disconnect() {
        try {
            if (output != null) {
                output.close();
            }
            if (input != null) {
                input.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Ошибка при отключении: " + e.getMessage());
        } finally {
            connected = false;
            status = "Отключено";
            currentUser = null;
        }
    }
    
    // Методы для конкретных запросов
    
    @SuppressWarnings("unchecked")
    public List<Product> getProducts() {
        Protocol.Message response = sendRequest(Protocol.MessageType.GET_PRODUCTS, null);
        if (response.isSuccess() && response.getData() instanceof List) {
            return (List<Product>) response.getData();
        }
        return new ArrayList<>();
    }
    
    @SuppressWarnings("unchecked")
    public List<Order> getOrders() {
        Protocol.Message response = sendRequest(Protocol.MessageType.GET_ORDERS, null);
        if (response.isSuccess() && response.getData() instanceof List) {
            return (List<Order>) response.getData();
        }
        return new ArrayList<>();
    }
    
    @SuppressWarnings("unchecked")
    public List<RawMaterial> getRawMaterials() {
        Protocol.Message response = sendRequest(Protocol.MessageType.GET_RAW_MATERIALS, null);
        if (response.isSuccess() && response.getData() instanceof List) {
            return (List<RawMaterial>) response.getData();
        }
        return new ArrayList<>();
    }
    
    public boolean addProduct(Product product) {
        Protocol.Message response = sendRequest(Protocol.MessageType.ADD_PRODUCT, product);
        return response.isSuccess();
    }
    
    public boolean updateProduct(Product product) {
        Protocol.Message response = sendRequest(Protocol.MessageType.UPDATE_PRODUCT, product);
        return response.isSuccess();
    }
    
    public boolean deleteProduct(int productId) {
        Protocol.Message response = sendRequest(Protocol.MessageType.DELETE_PRODUCT, productId);
        return response.isSuccess();
    }
    
    public boolean addOrder(Order order) {
        Protocol.Message response = sendRequest(Protocol.MessageType.ADD_ORDER, order);
        return response.isSuccess();
    }
    
    public boolean updateOrderStatus(Map<String, Object> statusData) {
        Protocol.Message response = sendRequest(Protocol.MessageType.UPDATE_ORDER_STATUS, statusData);
        return response.isSuccess();
    }
    
    public boolean deleteOrder(int orderId) {
        Protocol.Message response = sendRequest(Protocol.MessageType.DELETE_ORDER, orderId);
        return response.isSuccess();
    }
    
    public boolean addRawMaterial(RawMaterial material) {
        Protocol.Message response = sendRequest(Protocol.MessageType.ADD_RAW_MATERIAL, material);
        return response.isSuccess();
    }
    
    public boolean updateRawMaterial(RawMaterial material) {
        Protocol.Message response = sendRequest(Protocol.MessageType.UPDATE_RAW_MATERIAL, material);
        return response.isSuccess();
    }
    
    public boolean deleteRawMaterial(int materialId) {
        Protocol.Message response = sendRequest(Protocol.MessageType.DELETE_RAW_MATERIAL, materialId);
        return response.isSuccess();
    }
    
    // Геттеры
    public boolean isConnected() {
        return connected && socket != null && !socket.isClosed() && socket.isConnected();
    }
    
    public String getStatus() {
        return status;
    }
    
    public String getConnectionInfo() {
        if (socket != null && isConnected()) {
            return socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
        }
        return "Нет подключения";
    }
    
    public User getCurrentUser() {
        return currentUser;
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
    public void logout() {
    if (connected && currentUser != null) {
        try {
            Protocol.Message message = new Protocol.Message(Protocol.MessageType.LOGOUT, "logout");
            output.writeObject(message);
            output.flush();
        } catch (IOException e) {
            System.err.println("Ошибка при выходе: " + e.getMessage());
        }
    }
    currentUser = null;
    disconnect();
}

}