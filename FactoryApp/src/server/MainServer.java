package server;

import shared.Protocol;
import javax.swing.*;
import server.database.DatabaseManager;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class MainServer {
    private ServerGUI gui;
    private ExecutorService threadPool;
    private List<ClientHandler> clients;
    private Properties config;
    private ServerSocket serverSocket;
    private boolean isRunning;
    private DatabaseManager databaseManager;
    
    public MainServer() {
        this.clients = new CopyOnWriteArrayList<>();
        this.threadPool = Executors.newCachedThreadPool();
        this.isRunning = false;
        this.config = new Properties();
        this.databaseManager = DatabaseManager.getInstance();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainServer server = new MainServer();
            server.initialize();
        });
    }
    
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    
    public void initialize() {
        loadConfig();
        gui = new ServerGUI(this);
        gui.setVisible(true);
        updateGUIWithConfig();
        gui.log("Система автоматизации кондитерской фабрики инициализирована");
    }
    
    private void loadConfig() {
        try {
            File configFile = new File("config/server-config.properties");
            if (!configFile.exists()) {
                createDefaultConfig();
                if (gui != null) {
                    gui.log("Конфигурационный файл не найден, создан файл по умолчанию");
                }
                return;
            }
            
            try (InputStream input = new FileInputStream(configFile)) {
                config.load(input);
                if (gui != null) {
                    gui.log("Конфигурация загружена успешно");
                }
            }
        } catch (IOException e) {
            if (gui == null) {
                gui = new ServerGUI(this);
            }
            gui.log("Ошибка загрузки конфигурации: " + e.getMessage());
            setDefaultConfig();
        }
    }
    
    private void createDefaultConfig() {
        try {
            File configDir = new File("config");
            if (!configDir.exists()) {
                configDir.mkdirs();
            }
            
            File configFile = new File("config/server-config.properties");
            try (OutputStream output = new FileOutputStream(configFile)) {
                String defaultConfig = "# Server Configuration\n" +
                        "server.port=5555\n" +
                        "server.max_clients=10\n" +
                        "server.socket_type=blocking\n" +
                        "server.ssl_enabled=false\n\n" +
                        "# Database Configuration\n" +
                        "database.url=jdbc:sqlite:database/factory.db\n" +
                        "database.driver=org.sqlite.JDBC\n\n" +
                        "# Application Settings\n" +
                        "app.name=Confectionery Factory Automation System\n" +
                        "app.version=1.0\n" +
                        "app.debug=true\n\n" +
                        "# Business Settings\n" +
                        "business.currency=RUB\n" +
                        "business.vat_rate=20.0\n";
                output.write(defaultConfig.getBytes());
            }
            
            try (InputStream input = new FileInputStream(configFile)) {
                config.load(input);
            }
        } catch (IOException e) {
            System.err.println("Не удалось создать конфигурационный файл: " + e.getMessage());
            setDefaultConfig();
        }
    }
    
    private void setDefaultConfig() {
        config.setProperty("server.port", "5555");
        config.setProperty("server.max_clients", "10");
        config.setProperty("server.ssl_enabled", "false");
        config.setProperty("database.url", "jdbc:sqlite:database/factory.db");
        config.setProperty("app.debug", "true");
    }
    
    private void updateGUIWithConfig() {
        if (gui != null) {
            String port = config.getProperty("server.port", "5555");
            String maxClients = config.getProperty("server.max_clients", "10");
            gui.updateServerInfo(port, maxClients, "Нет");
        }
    }
    
    public void startServer() {
        if (isRunning) {
            gui.log("Сервер уже запущен");
            return;
        }
        
        try {
            int port = Integer.parseInt(config.getProperty("server.port", "5555"));
            
            serverSocket = new ServerSocket(port);
            isRunning = true;
            
            gui.updateStatus("Запущен");
            gui.updateServerInfo(String.valueOf(port), 
                config.getProperty("server.max_clients", "10"), 
                "Да");
            gui.log("Сервер запущен на порту " + port);
            
            startAcceptingConnections();
            
        } catch (Exception e) {
            gui.log("Ошибка запуска сервера: " + e.getMessage());
            JOptionPane.showMessageDialog(gui, 
                "Не удалось запустить сервер: " + e.getMessage(), 
                "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void startAcceptingConnections() {
        threadPool.submit(() -> {
            gui.log("Ожидание подключений...");
            while (!Thread.currentThread().isInterrupted() && isRunning && serverSocket != null) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    handleNewConnection(clientSocket);
                    
                } catch (IOException e) {
                    if (isRunning) {
                        gui.log("Ошибка при принятии подключения: " + e.getMessage());
                    }
                }
            }
        });
    }
    
    private void handleNewConnection(Socket clientSocket) {
        ClientHandler handler = new ClientHandler(clientSocket, this, gui);
        clients.add(handler);
        threadPool.submit(handler);
        
        gui.updateClientCount(clients.size());
        gui.log("Новый клиент подключен: " + clientSocket.getInetAddress().getHostAddress());
    }
    
    public void broadcastMessage(Protocol.Message message, ClientHandler exclude) {
        for (ClientHandler client : clients) {
            if (client != exclude && client.isAuthenticated()) {
                client.sendMessage(message);
            }
        }
    }
    
    public void removeClient(ClientHandler client) {
        clients.remove(client);
        gui.updateClientCount(clients.size());
        gui.log("Клиент отключен: " + client.getClientInfo());
        gui.removeClientFromTable(client.getClientInfo());
    }
    
    public void stopServer() {
        if (!isRunning) {
            gui.log("Сервер уже остановлен");
            return;
        }
        
        isRunning = false;
        
        for (ClientHandler client : clients) {
            client.disconnect();
        }
        clients.clear();
        
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                serverSocket = null;
            }
        } catch (IOException e) {
            gui.log("Ошибка при закрытии сокета: " + e.getMessage());
        }
        
        gui.updateStatus("Остановлен");
        gui.updateClientCount(0);
        gui.updateServerInfo(
            config.getProperty("server.port", "5555"),
            config.getProperty("server.max_clients", "10"),
            "Нет"
        );
        gui.log("Сервер остановлен");
    }
    
    public void shutdown() {
        stopServer();
        System.exit(0);
    }
    
    public List<ClientHandler> getConnectedClients() {
        return new ArrayList<>(clients);
    }
    
    public Properties getConfig() {
        return config;
    }
    
    public boolean isRunning() {
        return isRunning;
    }
}