package client;

import client.ui.ProductDialog;
import client.ui.OrderDialog;
import client.ui.RawMaterialDialog;
import shared.models.Product;
import shared.models.Order;
import shared.models.RawMaterial;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class ClientGUI extends JFrame {
    private String userRole;
    private NetworkClient networkClient;
    private JTabbedPane tabbedPane;
    private JLabel statusLabel;
    private JLabel connectionLabel;
    
    // Для хранения данных из сервера
    private List<Product> serverProducts = new ArrayList<>();
    private List<Order> serverOrders = new ArrayList<>();
    private List<RawMaterial> serverRawMaterials = new ArrayList<>();
    
    public ClientGUI(String role) {
        this.userRole = role;
        this.networkClient = new NetworkClient("localhost", 5555);
        initComponents();
        setTitle("Кондитерская фабрика - Клиент [" + role + "]");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setResizable(true);
    }
    
    private void initComponents() {
        // Меню
        JMenuBar menuBar = new JMenuBar();
        
        // Меню Файл
        JMenu fileMenu = new JMenu("Файл");
        JMenuItem exitItem = new JMenuItem("Выход");
        exitItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                disconnectFromServer();
                System.exit(0);
            }
        });
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);
        
        // Меню Подключение
        JMenu connectionMenu = new JMenu("Подключение");
        JMenuItem connectItem = new JMenuItem("Подключиться к серверу");
        JMenuItem disconnectItem = new JMenuItem("Отключиться от сервера");
        
        connectItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connectToServer();
            }
        });
        
        disconnectItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                disconnectFromServer();
            }
        });
        
        connectionMenu.add(connectItem);
        connectionMenu.add(disconnectItem);
        menuBar.add(connectionMenu);
        
        // Меню Данные
        JMenu dataMenu = new JMenu("Данные");
        JMenuItem refreshDataItem = new JMenuItem("Обновить все данные");
        refreshDataItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshAllData();
            }
        });
        dataMenu.add(refreshDataItem);
        menuBar.add(dataMenu);
        
        setJMenuBar(menuBar);
        
        // Панель вкладок
        tabbedPane = new JTabbedPane();
        createTabs();
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // Статус бар
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusLabel = new JLabel(" Статус: " + networkClient.getStatus());
        connectionLabel = new JLabel("Сервер: не подключено ");
        
        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(connectionLabel, BorderLayout.EAST);
        
        add(statusPanel, BorderLayout.SOUTH);
    }
    
    private void createTabs() {
        tabbedPane.removeAll();
        
        tabbedPane.addTab("Главная", createHomePanel());
        
        // Добавляем вкладки в зависимости от роли
        switch (userRole) {
            case "Администратор":
                tabbedPane.addTab("Продукция", createProductsPanel());
                tabbedPane.addTab("Заказы", createOrdersPanel());
                tabbedPane.addTab("Сырье", createRawMaterialsPanel());
                tabbedPane.addTab("Пользователи", createUsersPanel());
                tabbedPane.addTab("Отчеты", createReportsPanel());
                break;
            case "Менеджер":
                tabbedPane.addTab("Продукция", createProductsPanel());
                tabbedPane.addTab("Заказы", createOrdersPanel());
                tabbedPane.addTab("Клиенты", createClientsPanel());
                tabbedPane.addTab("Отчеты", createReportsPanel());
                break;
            case "Технолог":
                tabbedPane.addTab("Продукция", createProductsPanel());
                tabbedPane.addTab("Сырье", createRawMaterialsPanel());
                tabbedPane.addTab("Рецепты", createRecipesPanel());
                break;
            case "Кладовщик":
                tabbedPane.addTab("Сырье", createRawMaterialsPanel());
                tabbedPane.addTab("Продукция", createProductsPanel());
                tabbedPane.addTab("Отчеты", createReportsPanel());
                break;
            default:
                tabbedPane.addTab("Продукция", createProductsPanel());
                tabbedPane.addTab("Заказы", createOrdersPanel());
                break;
        }
    }
    
    private JPanel createHomePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        String connectionStatus = networkClient.isConnected() ? 
            "Подключено (" + networkClient.getConnectionInfo() + ")" : 
            "Не подключено";
        
        JLabel welcomeLabel = new JLabel(
            "<html><center><h1>Добро пожаловать в систему автоматизации кондитерской фабрики!</h1>" +
            "<h3>Роль: " + userRole + "</h3>" +
            "<p>Статус подключения: <b>" + connectionStatus + "</b></p>" +
            "<p>Используйте меню выше для навигации по системе</p>" +
            "<p>Для начала работы подключитесь к серверу</p></center></html>",
            JLabel.CENTER
        );
        
        panel.add(welcomeLabel, BorderLayout.CENTER);
        
        // Панель быстрого доступа
        JPanel quickAccessPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        quickAccessPanel.setBorder(BorderFactory.createTitledBorder("Быстрый доступ"));
        quickAccessPanel.setPreferredSize(new Dimension(0, 200));
        
        JButton connectBtn = new JButton("Подключиться к серверу");
        JButton viewProductsBtn = new JButton("Просмотр продукции");
        JButton createOrderBtn = new JButton("Создать заказ");
        JButton viewMaterialsBtn = new JButton("Просмотр сырья");
        JButton viewReportsBtn = new JButton("Отчеты");
        JButton refreshDataBtn = new JButton("Обновить данные");
        
        connectBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connectToServer();
            }
        });
        
        viewProductsBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!networkClient.isConnected()) {
                    JOptionPane.showMessageDialog(ClientGUI.this,
                        "Для просмотра продукции необходимо подключиться к серверу",
                        "Требуется подключение", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                tabbedPane.setSelectedIndex(tabbedPane.indexOfTab("Продукция"));
            }
        });
        
        createOrderBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!networkClient.isConnected()) {
                    JOptionPane.showMessageDialog(ClientGUI.this,
                        "Для создания заказа необходимо подключиться к серверу",
                        "Требуется подключение", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (tabbedPane.indexOfTab("Заказы") != -1) {
                    tabbedPane.setSelectedIndex(tabbedPane.indexOfTab("Заказы"));
                }
            }
        });
        
        viewMaterialsBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!networkClient.isConnected()) {
                    JOptionPane.showMessageDialog(ClientGUI.this,
                        "Для просмотра сырья необходимо подключиться к серверу",
                        "Требуется подключение", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (tabbedPane.indexOfTab("Сырье") != -1) {
                    tabbedPane.setSelectedIndex(tabbedPane.indexOfTab("Сырье"));
                }
            }
        });
        
        viewReportsBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!networkClient.isConnected()) {
                    JOptionPane.showMessageDialog(ClientGUI.this,
                        "Для просмотра отчетов необходимо подключиться к серверу",
                        "Требуется подключение", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (tabbedPane.indexOfTab("Отчеты") != -1) {
                    tabbedPane.setSelectedIndex(tabbedPane.indexOfTab("Отчеты"));
                }
            }
        });
        
        refreshDataBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshAllData();
            }
        });
        
        quickAccessPanel.add(connectBtn);
        quickAccessPanel.add(viewProductsBtn);
        quickAccessPanel.add(createOrderBtn);
        quickAccessPanel.add(viewMaterialsBtn);
        quickAccessPanel.add(viewReportsBtn);
        quickAccessPanel.add(refreshDataBtn);
        
        panel.add(quickAccessPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    

// Добавьте поле и методы в ClientGUI


public void setNetworkClient(NetworkClient networkClient) {
    
    this.networkClient = networkClient; // или замените существующий networkClient
    connectToServer(); // Автоматическое подключение
}



    private JPanel createProductsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        if (!networkClient.isConnected()) {
            JPanel messagePanel = new JPanel(new BorderLayout());
            JLabel messageLabel = new JLabel("<html><center><h3>Не подключено к серверу</h3>" +
                "<p>Для работы с продукцией необходимо подключиться к серверу</p>" +
                "<p>Нажмите кнопку ниже для подключения</p></center></html>", 
                JLabel.CENTER);
            
            JButton connectButton = new JButton("Подключиться сейчас");
            connectButton.addActionListener(e -> connectToServer());
            
            messagePanel.add(messageLabel, BorderLayout.CENTER);
            messagePanel.add(connectButton, BorderLayout.SOUTH);
            
            panel.add(messagePanel, BorderLayout.CENTER);
            return panel;
        }
        
        // Модель таблицы
        String[] columns = {"ID", "Название", "Категория", "Цена (руб)", "Вес (кг)", "На складе", "Описание"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(80);
        table.getColumnModel().getColumn(4).setPreferredWidth(80);
        table.getColumnModel().getColumn(5).setPreferredWidth(80);
        table.getColumnModel().getColumn(6).setPreferredWidth(200);
        
        JScrollPane scrollPane = new JScrollPane(table);
        
        // Панель кнопок
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn = new JButton("Добавить");
        JButton editBtn = new JButton("Редактировать");
        JButton deleteBtn = new JButton("Удалить");
        JButton refreshBtn = new JButton("Обновить");
        JButton stockReportBtn = new JButton("Отчет по остаткам");
        
        refreshBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadProductsFromServer(model);
            }
        });
        
        addBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ProductDialog dialog = new ProductDialog(ClientGUI.this, "Добавить продукт", null);
                dialog.setVisible(true);
                if (dialog.isConfirmed()) {
                    Product newProduct = dialog.getProduct();
                    // Отправляем продукт на сервер
                    boolean success = networkClient.addProduct(newProduct);
                    if (success) {
                        JOptionPane.showMessageDialog(ClientGUI.this, 
                            "Продукт успешно добавлен", "Успех", JOptionPane.INFORMATION_MESSAGE);
                        loadProductsFromServer(model);
                    } else {
                        JOptionPane.showMessageDialog(ClientGUI.this, 
                            "Ошибка при добавлении продукта", "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        
        editBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow >= 0) {
                    int id = (int) model.getValueAt(selectedRow, 0);
                    String name = (String) model.getValueAt(selectedRow, 1);
                    String category = (String) model.getValueAt(selectedRow, 2);
                    double price = (double) model.getValueAt(selectedRow, 3);
                    double weight = (double) model.getValueAt(selectedRow, 4);
                    int stock = (int) model.getValueAt(selectedRow, 5);
                    String description = (String) model.getValueAt(selectedRow, 6);
                    
                    Product product = new Product();
                    product.setId(id);
                    product.setName(name);
                    product.setCategory(category);
                    product.setPrice(price);
                    product.setWeight(weight);
                    product.setStockQuantity(stock);
                    product.setDescription(description);
                    
                    ProductDialog dialog = new ProductDialog(ClientGUI.this, "Редактировать продукт", product);
                    dialog.setVisible(true);
                    if (dialog.isConfirmed()) {
                        Product updatedProduct = dialog.getProduct();
                        // Отправляем изменения на сервер
                        boolean success = networkClient.updateProduct(updatedProduct);
                        if (success) {
                            JOptionPane.showMessageDialog(ClientGUI.this, 
                                "Продукт успешно обновлен", "Успех", JOptionPane.INFORMATION_MESSAGE);
                            loadProductsFromServer(model);
                        } else {
                            JOptionPane.showMessageDialog(ClientGUI.this, 
                                "Ошибка при обновлении продукта", "Ошибка", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(ClientGUI.this, 
                        "Выберите продукт для редактирования", "Внимание", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        
        deleteBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow >= 0) {
                    int id = (int) model.getValueAt(selectedRow, 0);
                    String name = (String) model.getValueAt(selectedRow, 1);
                    
                    int confirm = JOptionPane.showConfirmDialog(ClientGUI.this, 
                        "Удалить продукт: " + name + "?\nЭто действие нельзя отменить.", 
                        "Подтверждение удаления", JOptionPane.YES_NO_OPTION);
                    
                    if (confirm == JOptionPane.YES_OPTION) {
                        // Отправляем запрос на удаление
                        boolean success = networkClient.deleteProduct(id);
                        if (success) {
                            JOptionPane.showMessageDialog(ClientGUI.this, 
                                "Продукт успешно удален", "Успех", JOptionPane.INFORMATION_MESSAGE);
                            loadProductsFromServer(model);
                        } else {
                            JOptionPane.showMessageDialog(ClientGUI.this, 
                                "Ошибка при удалении продукта", "Ошибка", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(ClientGUI.this, 
                        "Выберите продукт для удаления", "Внимание", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        
        stockReportBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int lowStockCount = 0;
                int totalProducts = model.getRowCount();
                double totalValue = 0;
                
                for (int i = 0; i < model.getRowCount(); i++) {
                    int stock = (int) model.getValueAt(i, 5);
                    double price = (double) model.getValueAt(i, 3);
                    totalValue += stock * price;
                    
                    if (stock < 10) {
                        lowStockCount++;
                    }
                }
                
                JOptionPane.showMessageDialog(ClientGUI.this, 
                    "<html><b>Отчет по остаткам продукции</b><br>" +
                    "Всего позиций: " + totalProducts + "<br>" +
                    "Требуют пополнения (<10): " + lowStockCount + "<br>" +
                    "Общая стоимость запасов: " + String.format("%.2f", totalValue) + " руб.<br>" +
                    "Средний остаток: " + (totalProducts > 0 ? 
                        String.format("%.1f", (double) totalValue / totalProducts) : "0") + " руб. на позицию</html>", 
                    "Отчет по остаткам", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        buttonPanel.add(addBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(refreshBtn);
        buttonPanel.add(stockReportBtn);
        
        // Панель поиска
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Поиск:"));
        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("Найти");
        
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String searchText = searchField.getText().toLowerCase().trim();
                if (searchText.isEmpty()) {
                    loadProductsFromServer(model);
                    return;
                }
                
                // Фильтруем локально уже загруженные данные
                model.setRowCount(0);
                for (Product product : serverProducts) {
                    if (product.getName().toLowerCase().contains(searchText) ||
                        product.getCategory().toLowerCase().contains(searchText) ||
                        (product.getDescription() != null && product.getDescription().toLowerCase().contains(searchText))) {
                        
                        model.addRow(new Object[]{
                            product.getId(),
                            product.getName(),
                            product.getCategory(),
                            product.getPrice(),
                            product.getWeight(),
                            product.getStockQuantity(),
                            product.getDescription()
                        });
                    }
                }
            }
        });
        
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(searchPanel, BorderLayout.WEST);
        topPanel.add(new JLabel(" "), BorderLayout.CENTER);
        
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Загружаем данные при создании панели
        loadProductsFromServer(model);
        
        return panel;
    }
    
    private void loadProductsFromServer(DefaultTableModel model) {
        new Thread(() -> {
            try {
                List<Product> products = networkClient.getProducts();
                SwingUtilities.invokeLater(() -> {
                    model.setRowCount(0);
                    if (products == null || products.isEmpty()) {
                        model.addRow(new Object[]{"Нет данных", "", "", "", "", "", ""});
                        serverProducts.clear();
                    } else {
                        serverProducts = products;
                        for (Product product : products) {
                            model.addRow(new Object[]{
                                product.getId(),
                                product.getName(),
                                product.getCategory(),
                                product.getPrice(),
                                product.getWeight(),
                                product.getStockQuantity(),
                                product.getDescription()
                            });
                        }
                    }
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(ClientGUI.this, 
                        "Ошибка загрузки продуктов: " + e.getMessage(), 
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }
    
    private JPanel createOrdersPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        if (!networkClient.isConnected()) {
            JPanel messagePanel = new JPanel(new BorderLayout());
            JLabel messageLabel = new JLabel("<html><center><h3>Не подключено к серверу</h3>" +
                "<p>Для работы с заказами необходимо подключиться к серверу</p>" +
                "<p>Нажмите кнопку ниже для подключения</p></center></html>", 
                JLabel.CENTER);
            
            JButton connectButton = new JButton("Подключиться сейчас");
            connectButton.addActionListener(e -> connectToServer());
            
            messagePanel.add(messageLabel, BorderLayout.CENTER);
            messagePanel.add(connectButton, BorderLayout.SOUTH);
            
            panel.add(messagePanel, BorderLayout.CENTER);
            return panel;
        }
        
        // Модель таблицы
        String[] columns = {"ID", "Клиент", "Телефон", "Дата заказа", "Статус", "Сумма", "Примечания"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(100);
        table.getColumnModel().getColumn(6).setPreferredWidth(200);
        
        JScrollPane scrollPane = new JScrollPane(table);
        
        // Панель кнопок
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton createBtn = new JButton("Создать заказ");
        JButton viewBtn = new JButton("Просмотреть");
        JButton editBtn = new JButton("Изменить статус");
        JButton deleteBtn = new JButton("Удалить");
        JButton refreshBtn = new JButton("Обновить");
        JButton printBtn = new JButton("Печать");
        
        refreshBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadOrdersFromServer(model);
            }
        });
        
        createBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OrderDialog dialog = new OrderDialog(ClientGUI.this, "Создание заказа", null);
                dialog.setVisible(true);
                if (dialog.isConfirmed()) {
                    Order newOrder = dialog.getOrder();
                    newOrder.setOrderDate(new Date());
                    
                    // Отправляем заказ на сервер
                    boolean success = networkClient.addOrder(newOrder);
                    if (success) {
                        JOptionPane.showMessageDialog(ClientGUI.this, 
                            "Заказ успешно создан", "Успех", JOptionPane.INFORMATION_MESSAGE);
                        loadOrdersFromServer(model);
                    } else {
                        JOptionPane.showMessageDialog(ClientGUI.this, 
                            "Ошибка при создании заказа", "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        
        viewBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow >= 0) {
                    int orderId = (int) model.getValueAt(selectedRow, 0);
                    // Находим заказ в загруженных данных
                    for (Order order : serverOrders) {
                        if (order.getId() == orderId) {
                            OrderDialog dialog = new OrderDialog(ClientGUI.this, "Просмотр заказа #" + orderId, order);
                            dialog.setVisible(true);
                            break;
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(ClientGUI.this, 
                        "Выберите заказ для просмотра", "Внимание", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        
        editBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow >= 0) {
                    String[] statuses = {"Новый", "В производстве", "Готов", "Отгружен", "Выполнен", "Отменен"};
                    String currentStatus = (String) model.getValueAt(selectedRow, 4);
                    
                    String newStatus = (String) JOptionPane.showInputDialog(
                        ClientGUI.this,
                        "Выберите новый статус заказа:",
                        "Изменение статуса",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        statuses,
                        currentStatus
                    );
                    
                    if (newStatus != null && !newStatus.equals(currentStatus)) {
                        // Отправляем изменение статуса на сервер
                        Map<String, Object> statusData = new HashMap<>();
                        statusData.put("orderId", model.getValueAt(selectedRow, 0));
                        statusData.put("status", newStatus);
                        
                        boolean success = networkClient.updateOrderStatus(statusData);
                        if (success) {
                            JOptionPane.showMessageDialog(ClientGUI.this, 
                                "Статус заказа успешно изменен", "Успех", JOptionPane.INFORMATION_MESSAGE);
                            loadOrdersFromServer(model);
                        } else {
                            JOptionPane.showMessageDialog(ClientGUI.this, 
                                "Ошибка при изменении статуса заказа", "Ошибка", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(ClientGUI.this, 
                        "Выберите заказ для изменения статуса", "Внимание", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        
        deleteBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow >= 0) {
                    int orderId = (int) model.getValueAt(selectedRow, 0);
                    String clientName = (String) model.getValueAt(selectedRow, 1);
                    
                    int confirm = JOptionPane.showConfirmDialog(ClientGUI.this, 
                        "Удалить заказ #" + orderId + " от " + clientName + "?\nЭто действие нельзя отменить.", 
                        "Подтверждение удаления", JOptionPane.YES_NO_OPTION);
                    
                    if (confirm == JOptionPane.YES_OPTION) {
                        // Отправляем запрос на удаление
                        boolean success = networkClient.deleteOrder(orderId);
                        if (success) {
                            JOptionPane.showMessageDialog(ClientGUI.this, 
                                "Заказ успешно удален", "Успех", JOptionPane.INFORMATION_MESSAGE);
                            loadOrdersFromServer(model);
                        } else {
                            JOptionPane.showMessageDialog(ClientGUI.this, 
                                "Ошибка при удалении заказа", "Ошибка", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(ClientGUI.this, 
                        "Выберите заказ для удаления", "Внимание", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        
        printBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(ClientGUI.this, 
                    "Функция печати будет реализована в будущих версиях", 
                    "Информация", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        buttonPanel.add(createBtn);
        buttonPanel.add(viewBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(refreshBtn);
        buttonPanel.add(printBtn);
        
        // Фильтры
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Статус:"));
        JComboBox<String> statusFilter = new JComboBox<>(new String[]{"Все", "Новый", "В производстве", "Готов", "Отгружен", "Выполнен", "Отменен"});
        filterPanel.add(statusFilter);
        
        filterPanel.add(new JLabel("Период:"));
        JComboBox<String> periodFilter = new JComboBox<>(new String[]{"За все время", "Сегодня", "Неделя", "Месяц", "Квартал"});
        filterPanel.add(periodFilter);
        
        JButton filterBtn = new JButton("Применить фильтр");
        filterPanel.add(filterBtn);
        
        filterBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedStatus = (String) statusFilter.getSelectedItem();
                String selectedPeriod = (String) periodFilter.getSelectedItem();
                
                // Фильтруем локально
                model.setRowCount(0);
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                
                Calendar calendar = Calendar.getInstance();
                Date filterDate = null;
                
                if ("Сегодня".equals(selectedPeriod)) {
                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    filterDate = calendar.getTime();
                } else if ("Неделя".equals(selectedPeriod)) {
                    calendar.add(Calendar.DAY_OF_YEAR, -7);
                    filterDate = calendar.getTime();
                } else if ("Месяц".equals(selectedPeriod)) {
                    calendar.add(Calendar.MONTH, -1);
                    filterDate = calendar.getTime();
                } else if ("Квартал".equals(selectedPeriod)) {
                    calendar.add(Calendar.MONTH, -3);
                    filterDate = calendar.getTime();
                }
                
                for (Order order : serverOrders) {
                    boolean statusMatch = "Все".equals(selectedStatus) || order.getStatus().equals(selectedStatus);
                    boolean periodMatch = filterDate == null || order.getOrderDate().after(filterDate);
                    
                    if (statusMatch && periodMatch) {
                        model.addRow(new Object[]{
                            order.getId(),
                            order.getClientName(),
                            order.getClientPhone(),
                            sdf.format(order.getOrderDate()),
                            order.getStatus(),
                            order.getTotalAmount() + " руб.",
                            order.getNotes()
                        });
                    }
                }
            }
        });
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(filterPanel, BorderLayout.WEST);
        
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Загружаем данные при создании панели
        loadOrdersFromServer(model);
        
        return panel;
    }
    
    private void loadOrdersFromServer(DefaultTableModel model) {
        new Thread(() -> {
            try {
                List<Order> orders = networkClient.getOrders();
                SwingUtilities.invokeLater(() -> {
                    model.setRowCount(0);
                    if (orders == null || orders.isEmpty()) {
                        model.addRow(new Object[]{"Нет данных", "", "", "", "", "", ""});
                        serverOrders.clear();
                    } else {
                        serverOrders = orders;
                        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                        for (Order order : orders) {
                            model.addRow(new Object[]{
                                order.getId(),
                                order.getClientName(),
                                order.getClientPhone(),
                                sdf.format(order.getOrderDate()),
                                order.getStatus(),
                                order.getTotalAmount() + " руб.",
                                order.getNotes()
                            });
                        }
                    }
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(ClientGUI.this, 
                        "Ошибка загрузки заказов: " + e.getMessage(), 
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }
    
    private JPanel createRawMaterialsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        if (!networkClient.isConnected()) {
            JPanel messagePanel = new JPanel(new BorderLayout());
            JLabel messageLabel = new JLabel("<html><center><h3>Не подключено к серверу</h3>" +
                "<p>Для работы с сырьем необходимо подключиться к серверу</p>" +
                "<p>Нажмите кнопку ниже для подключения</p></center></html>", 
                JLabel.CENTER);
            
            JButton connectButton = new JButton("Подключиться сейчас");
            connectButton.addActionListener(e -> connectToServer());
            
            messagePanel.add(messageLabel, BorderLayout.CENTER);
            messagePanel.add(connectButton, BorderLayout.SOUTH);
            
            panel.add(messagePanel, BorderLayout.CENTER);
            return panel;
        }
        
        // Модель таблицы
        String[] columns = {"ID", "Название", "Категория", "Единица", "Текущий запас", "Мин. запас", "Макс. запас", "Статус", "Поставщик"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(80);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(80);
        table.getColumnModel().getColumn(6).setPreferredWidth(80);
        table.getColumnModel().getColumn(7).setPreferredWidth(100);
        table.getColumnModel().getColumn(8).setPreferredWidth(150);
        
        JScrollPane scrollPane = new JScrollPane(table);
        
        // Панель кнопок
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn = new JButton("Добавить");
        JButton editBtn = new JButton("Редактировать");
        JButton deleteBtn = new JButton("Удалить");
        JButton refreshBtn = new JButton("Обновить");
        JButton lowStockBtn = new JButton("Низкий запас");
        
        refreshBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadRawMaterialsFromServer(model);
            }
        });
        
        addBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RawMaterialDialog dialog = new RawMaterialDialog(ClientGUI.this, "Добавить сырье", null);
                dialog.setVisible(true);
                if (dialog.isConfirmed()) {
                    RawMaterial newMaterial = dialog.getMaterial();
                    // Отправляем сырье на сервер
                    boolean success = networkClient.addRawMaterial(newMaterial);
                    if (success) {
                        JOptionPane.showMessageDialog(ClientGUI.this, 
                            "Сырье успешно добавлено", "Успех", JOptionPane.INFORMATION_MESSAGE);
                        loadRawMaterialsFromServer(model);
                    } else {
                        JOptionPane.showMessageDialog(ClientGUI.this, 
                            "Ошибка при добавлении сырья", "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        
        editBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow >= 0) {
                    int id = (int) model.getValueAt(selectedRow, 0);
                    // Находим сырье в загруженных данных
                    RawMaterial material = findRawMaterialById(id);
                    if (material != null) {
                        RawMaterialDialog dialog = new RawMaterialDialog(ClientGUI.this, "Редактировать сырье", material);
                        dialog.setVisible(true);
                        if (dialog.isConfirmed()) {
                            RawMaterial updatedMaterial = dialog.getMaterial();
                            // Отправляем изменения на сервер
                            boolean success = networkClient.updateRawMaterial(updatedMaterial);
                            if (success) {
                                JOptionPane.showMessageDialog(ClientGUI.this, 
                                    "Сырье успешно обновлено", "Успех", JOptionPane.INFORMATION_MESSAGE);
                                loadRawMaterialsFromServer(model);
                            } else {
                                JOptionPane.showMessageDialog(ClientGUI.this, 
                                    "Ошибка при обновлении сырья", "Ошибка", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(ClientGUI.this, 
                        "Выберите сырье для редактирования", "Внимание", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        
        deleteBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow >= 0) {
                    int id = (int) model.getValueAt(selectedRow, 0);
                    String name = (String) model.getValueAt(selectedRow, 1);
                    
                    int confirm = JOptionPane.showConfirmDialog(ClientGUI.this, 
                        "Удалить сырье: " + name + "?\nЭто действие нельзя отменить.", 
                        "Подтверждение удаления", JOptionPane.YES_NO_OPTION);
                    
                    if (confirm == JOptionPane.YES_OPTION) {
                        // Отправляем запрос на удаление
                        boolean success = networkClient.deleteRawMaterial(id);
                        if (success) {
                            JOptionPane.showMessageDialog(ClientGUI.this, 
                                "Сырье успешно удалено", "Успех", JOptionPane.INFORMATION_MESSAGE);
                            loadRawMaterialsFromServer(model);
                        } else {
                            JOptionPane.showMessageDialog(ClientGUI.this, 
                                "Ошибка при удалении сырья", "Ошибка", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(ClientGUI.this, 
                        "Выберите сырье для удаления", "Внимание", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        
        lowStockBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int lowStockCount = 0;
                StringBuilder lowStockList = new StringBuilder();
                
                for (int i = 0; i < model.getRowCount(); i++) {
                    double currentStock = (double) model.getValueAt(i, 4);
                    double minStock = (double) model.getValueAt(i, 5);
                    
                    if (currentStock <= minStock) {
                        lowStockCount++;
                        lowStockList.append("- ").append(model.getValueAt(i, 1))
                                   .append(" (остаток: ").append(currentStock)
                                   .append(" ").append(model.getValueAt(i, 3))
                                   .append(")\n");
                    }
                }
                
                if (lowStockCount == 0) {
                    JOptionPane.showMessageDialog(ClientGUI.this, 
                        "Все позиции сырья в норме", 
                        "Информация", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(ClientGUI.this, 
                        "<html><b>Сырье с низким запасом:</b><br><br>" + 
                        lowStockList.toString() + 
                        "<br>Всего позиций: " + lowStockCount + "</html>", 
                        "Низкий запас", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        
        buttonPanel.add(addBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(refreshBtn);
        buttonPanel.add(lowStockBtn);
        
        // Панель поиска
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Поиск:"));
        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("Найти");
        
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String searchText = searchField.getText().toLowerCase().trim();
                if (searchText.isEmpty()) {
                    loadRawMaterialsFromServer(model);
                    return;
                }
                
                // Фильтруем локально уже загруженные данные
                model.setRowCount(0);
                for (RawMaterial material : serverRawMaterials) {
                    if (material.getName().toLowerCase().contains(searchText) ||
                        material.getCategory().toLowerCase().contains(searchText) ||
                        (material.getSupplier() != null && material.getSupplier().toLowerCase().contains(searchText))) {
                        
                        String status;
                        if (material.getCurrentStock() <= 0) {
                            status = "Нет в наличии";
                        } else if (material.getCurrentStock() <= material.getMinStock()) {
                            status = "Требует пополнения";
                        } else if (material.getCurrentStock() >= material.getMaxStock() * 0.9) {
                            status = "Переизбыток";
                        } else {
                            status = "Норма";
                        }
                        
                        model.addRow(new Object[]{
                            material.getId(),
                            material.getName(),
                            material.getCategory(),
                            material.getUnit(),
                            material.getCurrentStock(),
                            material.getMinStock(),
                            material.getMaxStock(),
                            status,
                            material.getSupplier()
                        });
                    }
                }
            }
        });
        
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(searchPanel, BorderLayout.WEST);
        topPanel.add(new JLabel(" "), BorderLayout.CENTER);
        
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Загружаем данные при создании панели
        loadRawMaterialsFromServer(model);
        
        return panel;
    }
    
    // Вспомогательный метод для поиска сырья по ID
    private RawMaterial findRawMaterialById(int id) {
        for (RawMaterial material : serverRawMaterials) {
            if (material.getId() == id) {
                return material;
            }
        }
        return null;
    }
    
    private void loadRawMaterialsFromServer(DefaultTableModel model) {
        new Thread(() -> {
            try {
                List<RawMaterial> materials = networkClient.getRawMaterials();
                SwingUtilities.invokeLater(() -> {
                    model.setRowCount(0);
                    if (materials == null || materials.isEmpty()) {
                        model.addRow(new Object[]{"Нет данных", "", "", "", "", "", "", "", ""});
                        serverRawMaterials.clear();
                    } else {
                        serverRawMaterials = materials;
                        for (RawMaterial material : materials) {
                            String status;
                            if (material.getCurrentStock() <= 0) {
                                status = "Нет в наличии";
                            } else if (material.getCurrentStock() <= material.getMinStock()) {
                                status = "Требует пополнения";
                            } else if (material.getCurrentStock() >= material.getMaxStock() * 0.9) {
                                status = "Переизбыток";
                            } else {
                                status = "Норма";
                            }
                            
                            model.addRow(new Object[]{
                                material.getId(),
                                material.getName(),
                                material.getCategory(),
                                material.getUnit(),
                                material.getCurrentStock(),
                                material.getMinStock(),
                                material.getMaxStock(),
                                status,
                                material.getSupplier()
                            });
                        }
                    }
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(ClientGUI.this, 
                        "Ошибка загрузки сырья: " + e.getMessage(), 
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }
    
    private JPanel createUsersPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("<html><center><h2>Управление пользователями</h2>" +
            "<p>В этом разделе администратор может управлять пользователями системы</p></center></html>", 
            JLabel.CENTER);
        
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Модель таблицы
        String[] columns = {"ID", "Логин", "ФИО", "Роль", "Email", "Телефон", "Статус"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // Демо-данные пользователей
        Object[][] demoUsers = {
            {1, "admin", "Иванов Иван Иванович", "Администратор", "admin@factory.com", "+7 (999) 111-11-11", "Активен"},
            {2, "manager", "Петрова Анна Сергеевна", "Менеджер", "manager@factory.com", "+7 (999) 222-22-22", "Активен"},
            {3, "technologist", "Сидоров Алексей Викторович", "Технолог", "tech@factory.com", "+7 (999) 333-33-33", "Активен"},
            {4, "warehouse", "Кузнецова Ольга Дмитриевна", "Кладовщик", "warehouse@factory.com", "+7 (999) 444-44-44", "Активен"},
            {5, "accountant", "Смирнова Елена Викторовна", "Бухгалтер", "accountant@factory.com", "+7 (999) 555-55-55", "Активен"}
        };
        
        for (Object[] user : demoUsers) {
            model.addRow(user);
        }
        
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        
        // Панель кнопок
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn = new JButton("Добавить пользователя");
        JButton editBtn = new JButton("Редактировать");
        JButton deleteBtn = new JButton("Удалить");
        JButton blockBtn = new JButton("Блокировать");
        JButton resetPassBtn = new JButton("Сбросить пароль");
        
        // Добавим обработчики для кнопок
        addBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                "Добавление пользователей будет реализовано в следующей версии",
                "Информация", JOptionPane.INFORMATION_MESSAGE);
        });
        
        editBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                String username = (String) model.getValueAt(selectedRow, 1);
                JOptionPane.showMessageDialog(this,
                    "Редактирование пользователя: " + username + "\nБудет реализовано в следующей версии",
                    "Информация", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Выберите пользователя для редактирования",
                    "Внимание", JOptionPane.WARNING_MESSAGE);
            }
        });
        
        deleteBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                String username = (String) model.getValueAt(selectedRow, 1);
                int confirm = JOptionPane.showConfirmDialog(this,
                    "Удалить пользователя: " + username + "?\nЭто действие нельзя отменить.",
                    "Подтверждение удаления", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    model.removeRow(selectedRow);
                }
            } else {
                JOptionPane.showMessageDialog(this,
                    "Выберите пользователя для удаления",
                    "Внимание", JOptionPane.WARNING_MESSAGE);
            }
        });
        
        buttonPanel.add(addBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(blockBtn);
        buttonPanel.add(resetPassBtn);
        
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder("Информация"));
        JTextArea infoArea = new JTextArea(
            "Функционал управления пользователями:\n\n" +
            "1. Добавление новых пользователей\n" +
            "2. Редактирование информации о пользователях\n" +
            "3. Назначение ролей и прав доступа\n" +
            "4. Блокировка/разблокировка учетных записей\n" +
            "5. Сброс паролей пользователей\n" +
            "6. Просмотр истории действий\n\n" +
            "Для работы с реальными данными необходимо подключение к серверу."
        );
        infoArea.setEditable(false);
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        infoArea.setRows(8);
        
        infoPanel.add(new JScrollPane(infoArea), BorderLayout.CENTER);
        
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        centerPanel.add(infoPanel, BorderLayout.SOUTH);
        
        panel.add(centerPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("<html><center><h2>Отчеты и аналитика</h2>" +
            "<p>Генерация отчетов по различным аспектам работы фабрики</p></center></html>", 
            JLabel.CENTER);
        
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Панель выбора отчета
        JPanel selectionPanel = new JPanel(new GridBagLayout());
        selectionPanel.setBorder(BorderFactory.createTitledBorder("Параметры отчета"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0; gbc.gridy = 0;
        selectionPanel.add(new JLabel("Тип отчета:"), gbc);
        gbc.gridx = 1;
        String[] reportTypes = {"По продажам", "По производству", "По остаткам", "Финансовый", "По заказам", "По продукции", "По сырью"};
        JComboBox<String> reportTypeCombo = new JComboBox<>(reportTypes);
        selectionPanel.add(reportTypeCombo, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        selectionPanel.add(new JLabel("Период с:"), gbc);
        gbc.gridx = 1;
        JTextField fromDateField = new JTextField(new SimpleDateFormat("dd.MM.yyyy").format(new Date()));
        fromDateField.setPreferredSize(new Dimension(150, 25));
        selectionPanel.add(fromDateField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        selectionPanel.add(new JLabel("Период по:"), gbc);
        gbc.gridx = 1;
        JTextField toDateField = new JTextField(new SimpleDateFormat("dd.MM.yyyy").format(new Date()));
        toDateField.setPreferredSize(new Dimension(150, 25));
        selectionPanel.add(toDateField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        selectionPanel.add(new JLabel("Формат:"), gbc);
        gbc.gridx = 1;
        JComboBox<String> formatCombo = new JComboBox<>(new String[]{"Текстовый", "CSV", "PDF", "Excel"});
        selectionPanel.add(formatCombo, gbc);
        
        panel.add(selectionPanel, BorderLayout.CENTER);
        
        // Кнопки генерации
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton generateBtn = new JButton("Сгенерировать отчет");
        JButton previewBtn = new JButton("Предпросмотр");
        JButton saveBtn = new JButton("Сохранить отчет");
        JButton printBtn = new JButton("Печать");
        
        generateBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String reportType = (String) reportTypeCombo.getSelectedItem();
                String fromDate = fromDateField.getText();
                String toDate = toDateField.getText();
                String format = (String) formatCombo.getSelectedItem();
                
                if (!networkClient.isConnected()) {
                    JOptionPane.showMessageDialog(ClientGUI.this,
                        "Для генерации отчетов необходимо подключение к серверу",
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                JOptionPane.showMessageDialog(ClientGUI.this, 
                    "<html><b>Генерация отчета</b><br><br>" +
                    "Тип: " + reportType + "<br>" +
                    "Период: " + fromDate + " - " + toDate + "<br>" +
                    "Формат: " + format + "<br><br>" +
                    "Отчет будет сгенерирован на основе данных из базы данных.<br>" +
                    "Генерация может занять несколько секунд.</html>",
                    "Генерация отчета", JOptionPane.INFORMATION_MESSAGE);
                
                // В реальном приложении здесь был бы вызов networkClient.generateReport()
                new Thread(() -> {
                    try {
                        Thread.sleep(2000); // Имитация генерации отчета
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(ClientGUI.this,
                                "Отчет успешно сгенерирован!\n" +
                                "Файл сохранен: reports/" + reportType.toLowerCase().replace(" ", "_") + 
                                "_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".txt",
                                "Успех", JOptionPane.INFORMATION_MESSAGE);
                        });
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }).start();
            }
        });
        
        previewBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String reportType = (String) reportTypeCombo.getSelectedItem();
                String fromDate = fromDateField.getText();
                String toDate = toDateField.getText();
                
                // Демо-предпросмотр отчета
                StringBuilder report = new StringBuilder();
                report.append("ОТЧЕТ ПО ").append(reportType.toUpperCase()).append("\n");
                report.append("========================================\n");
                report.append("Период: ").append(fromDate).append(" - ").append(toDate).append("\n");
                report.append("Дата формирования: ").append(new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date())).append("\n");
                report.append("Сформировал: ").append(userRole).append("\n\n");
                
                if (reportType.equals("По продажам")) {
                    report.append("ОБЩАЯ СТАТИСТИКА:\n");
                    report.append("----------------------------------------\n");
                    report.append("Всего продаж: 156\n");
                    report.append("Общая выручка: 245,800 руб.\n");
                    report.append("Средний чек: 1,575 руб.\n");
                    report.append("Количество клиентов: 89\n\n");
                    
                    report.append("ТОП-5 ПРОДУКТОВ:\n");
                    report.append("----------------------------------------\n");
                    report.append("1. Шоколадный торт - 45 продаж (108,000 руб.)\n");
                    report.append("2. Кекс ванильный - 38 продаж (5,700 руб.)\n");
                    report.append("3. Эклеры - 32 продажи (3,840 руб.)\n");
                    report.append("4. Печенье овсяное - 28 продаж (2,240 руб.)\n");
                    report.append("5. Тирамису - 13 продаж (5,850 руб.)\n\n");
                    
                    report.append("АНАЛИЗ ПРОДАЖ:\n");
                    report.append("----------------------------------------\n");
                    report.append("Наибольшие продажи: Понедельник, Среда\n");
                    report.append("Наименьшие продажи: Воскресенье\n");
                    report.append("Среднее количество заказов в день: 22\n");
                    report.append("Самый популярный период: 14:00-16:00\n");
                } else if (reportType.equals("По сырью")) {
                    report.append("СТАТИСТИКА ПО СЫРЬЮ:\n");
                    report.append("----------------------------------------\n");
                    report.append("Всего позиций сырья: 15\n");
                    report.append("Требует пополнения: 3 позиции\n");
                    report.append("Общая стоимость запасов: 85,200 руб.\n");
                    report.append("Средний запас на позицию: 65 кг\n\n");
                    
                    report.append("ПОЗИЦИИ С НИЗКИМ ЗАПАСОМ:\n");
                    report.append("----------------------------------------\n");
                    report.append("1. Мука пшеничная - 12.5 кг (мин: 20 кг)\n");
                    report.append("2. Сахар - 8.2 кг (мин: 10 кг)\n");
                    report.append("3. Какао порошок - 2.5 кг (мин: 5 кг)\n");
                } else {
                    report.append("ДЕМО-ДАННЫЕ ДЛЯ ОТЧЕТА:\n");
                    report.append("----------------------------------------\n");
                    report.append("Это демонстрационный отчет.\n");
                    report.append("В реальном приложении здесь будут данные из базы.\n\n");
                    report.append("Всего записей: 156\n");
                    report.append("Среднее значение: 1,250 руб.\n");
                    report.append("Минимальное значение: 80 руб.\n");
                    report.append("Максимальное значение: 2,500 руб.\n");
                }
                
                JTextArea textArea = new JTextArea(report.toString(), 25, 60);
                textArea.setEditable(false);
                textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
                JScrollPane scroll = new JScrollPane(textArea);
                JOptionPane.showMessageDialog(ClientGUI.this, scroll, "Предпросмотр отчета: " + reportType, JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        saveBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                "Функция сохранения отчетов будет реализована в следующей версии",
                "Информация", JOptionPane.INFORMATION_MESSAGE);
        });
        
        printBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                "Функция печати будет реализована в следующей версии",
                "Информация", JOptionPane.INFORMATION_MESSAGE);
        });
        
        buttonPanel.add(generateBtn);
        buttonPanel.add(previewBtn);
        buttonPanel.add(saveBtn);
        buttonPanel.add(printBtn);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createClientsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("<html><center><h2>Управление клиентами</h2>" +
            "<p>База данных клиентов и управление заказами</p></center></html>", 
            JLabel.CENTER);
        
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Модель таблицы
        String[] columns = {"ID", "Название", "Тип", "Контактное лицо", "Телефон", "Email", "Всего заказов", "Сумма заказов"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // Демо-данные клиентов
        Object[][] demoClients = {
            {1, "ООО 'Вкусняшки'", "Юридическое лицо", "Иванов И.И.", "+7 (999) 111-22-33", "vkus@mail.ru", 15, "125,400 руб."},
            {2, "Кафе 'Уют'", "ИП", "Петрова А.С.", "+7 (999) 444-55-66", "cafe-uyut@mail.ru", 8, "68,200 руб."},
            {3, "Ресторан 'Гурман'", "Юридическое лицо", "Сидоров А.В.", "+7 (999) 777-88-99", "gurman@mail.ru", 22, "189,500 руб."},
            {4, "Кофейня 'Арабика'", "ИП", "Кузнецова О.Д.", "+7 (999) 222-33-44", "arabica@mail.ru", 5, "42,300 руб."},
            {5, "Магазин 'Сладости'", "Юридическое лицо", "Смирнов Д.К.", "+7 (999) 555-66-77", "sladosti@mail.ru", 12, "96,800 руб."}
        };
        
        for (Object[] client : demoClients) {
            model.addRow(client);
        }
        
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        
        // Панель кнопок
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn = new JButton("Добавить клиента");
        JButton editBtn = new JButton("Редактировать");
        JButton deleteBtn = new JButton("Удалить");
        JButton historyBtn = new JButton("История заказов");
        JButton notifyBtn = new JButton("Отправить уведомление");
        
        buttonPanel.add(addBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(historyBtn);
        buttonPanel.add(notifyBtn);
        
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder("Статистика"));
        
        JTextArea statsArea = new JTextArea(
            "Общая статистика по клиентам:\n\n" +
            "Всего клиентов: 89\n" +
            "Активных клиентов: 67\n" +
            "Средний чек: 1,575 руб.\n" +
            "Среднее количество заказов на клиента: 4.2\n" +
            "Общая выручка от клиентов: 1,245,800 руб.\n" +
            "Клиентов с бонусной программой: 45\n\n" +
            "Топ-5 клиентов по сумме заказов:\n" +
            "1. Ресторан 'Гурман' - 189,500 руб.\n" +
            "2. ООО 'Вкусняшки' - 125,400 руб.\n" +
            "3. Сеть кафе 'Весна' - 112,300 руб.\n" +
            "4. Магазин 'Сладости' - 96,800 руб.\n" +
            "5. Кофейня 'Амадеус' - 89,200 руб."
        );
        statsArea.setEditable(false);
        statsArea.setLineWrap(true);
        statsArea.setWrapStyleWord(true);
        statsArea.setRows(12);
        
        infoPanel.add(new JScrollPane(statsArea), BorderLayout.CENTER);
        
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        centerPanel.add(infoPanel, BorderLayout.SOUTH);
        
        panel.add(centerPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createRecipesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("<html><center><h2>Управление рецептами</h2>" +
            "<p>База рецептов и технологических карт</p></center></html>", 
            JLabel.CENTER);
        
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Модель таблицы
        String[] columns = {"ID", "Название рецепта", "Продукт", "Выход (кг)", "Время приготовления", "Сложность", "Стоимость"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // Демо-данные рецептов
        Object[][] demoRecipes = {
            {1, "Шоколадный торт", "Торт шоколадный", 2.0, "3 часа", "Средняя", "850 руб."},
            {2, "Наполеон", "Торт наполеон", 1.8, "4 часа", "Сложная", "920 руб."},
            {3, "Эклеры", "Пирожное эклер", 0.5, "2 часа", "Средняя", "180 руб."},
            {4, "Овсяное печенье", "Печенье овсяное", 1.0, "1.5 часа", "Легкая", "120 руб."},
            {5, "Ванильный кекс", "Кекс ванильный", 0.8, "2 часа", "Легкая", "95 руб."}
        };
        
        for (Object[] recipe : demoRecipes) {
            model.addRow(recipe);
        }
        
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        
        // Панель кнопок
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn = new JButton("Добавить рецепт");
        JButton editBtn = new JButton("Редактировать");
        JButton deleteBtn = new JButton("Удалить");
        JButton viewBtn = new JButton("Просмотреть");
        JButton calcBtn = new JButton("Рассчитать стоимость");
        
        buttonPanel.add(addBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(viewBtn);
        buttonPanel.add(calcBtn);
        
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder("Информация о рецепте"));
        
        JTextArea recipeInfo = new JTextArea(
            "Шоколадный торт\n" +
            "----------------------------------------\n" +
            "Ингредиенты:\n" +
            "- Мука пшеничная: 500 г\n" +
            "- Сахар: 300 г\n" +
            "- Какао-порошок: 100 г\n" +
            "- Яйца: 6 шт.\n" +
            "- Масло сливочное: 200 г\n" +
            "- Сливки 33%: 400 мл\n" +
            "- Шоколад темный: 150 г\n\n" +
            "Технология приготовления:\n" +
            "1. Просеять муку с какао\n" +
            "2. Взбить яйца с сахаром\n" +
            "3. Добавить растопленное масло\n" +
            "4. Постепенно ввести муку\n" +
            "5. Выпекать 40 мин при 180°C\n" +
            "6. Приготовить крем из сливок и шоколада\n" +
            "7. Собрать торт"
        );
        recipeInfo.setEditable(false);
        recipeInfo.setLineWrap(true);
        recipeInfo.setWrapStyleWord(true);
        recipeInfo.setRows(15);
        
        infoPanel.add(new JScrollPane(recipeInfo), BorderLayout.CENTER);
        
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        centerPanel.add(infoPanel, BorderLayout.SOUTH);
        
        panel.add(centerPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void connectToServer() {
        if (networkClient.isConnected()) {
            JOptionPane.showMessageDialog(this, 
                "Уже подключено к серверу", 
                "Информация", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        JDialog connectDialog = new JDialog(this, "Подключение к серверу", true);
        connectDialog.setLayout(new BorderLayout());
        connectDialog.setSize(300, 150);
        connectDialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel messageLabel = new JLabel("Подключение к серверу...", JLabel.CENTER);
        panel.add(messageLabel, BorderLayout.CENTER);
        
        connectDialog.add(panel);
        
        Thread connectionThread = new Thread(() -> {
            boolean connected = false;
            boolean authSuccess = false;
            
            try {
                connected = networkClient.connect();
                
                if (connected) {
                    // Пробуем разные комбинации для демо
                    authSuccess = networkClient.sendLoginRequest("admin", "admin", userRole);
                    if (!authSuccess) {
                        authSuccess = networkClient.sendLoginRequest("manager", "manager123", userRole);
                    }
                    if (!authSuccess) {
                        authSuccess = networkClient.sendLoginRequest("tech", "tech123", userRole);
                    }
                    if (!authSuccess) {
                        authSuccess = networkClient.sendLoginRequest("warehouse", "warehouse123", userRole);
                    }
                }
            } catch (Exception e) {
                System.err.println("Ошибка в потоке подключения: " + e.getMessage());
            }
            
            final boolean finalConnected = connected;
            final boolean finalAuthSuccess = authSuccess;
            
            SwingUtilities.invokeLater(() -> {
                connectDialog.dispose();
                
                if (finalConnected && finalAuthSuccess) {
                    statusLabel.setText(" Статус: " + networkClient.getStatus());
                    connectionLabel.setText("Сервер: " + networkClient.getConnectionInfo());
                    
                    JOptionPane.showMessageDialog(ClientGUI.this, 
                        "<html><b>Успешно подключено к серверу!</b><br><br>" +
                        "Сервер: " + networkClient.getConnectionInfo() + "<br>" +
                        "Пользователь: " + networkClient.getCurrentUser() + "<br>" +
                        "Роль: " + userRole + "</html>", 
                        "Успех", JOptionPane.INFORMATION_MESSAGE);
                    
                    // Обновляем вкладки
                    createTabs();
                    
                    // Обновляем главную страницу
                    tabbedPane.setComponentAt(0, createHomePanel());
                    
                } else {
                    statusLabel.setText(" Статус: " + networkClient.getStatus());
                    connectionLabel.setText("Сервер: не подключено");
                    
                    if (!finalConnected) {
                        JOptionPane.showMessageDialog(ClientGUI.this, 
                            "<html><b>Не удалось подключиться к серверу</b><br><br>" +
                            "Проверьте:<br>" +
                            "1. Запущен ли сервер<br>" +
                            "2. Правильность настроек подключения<br>" +
                            "3. Брандмауэр (порт 5555)<br><br>" +
                            "<i>Для демонстрации работы используйте демо-данные</i></html>", 
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(ClientGUI.this, 
                            "<html><b>Подключено, но ошибка аутентификации</b><br><br>" +
                            "Попробуйте следующие учетные данные:<br>" +
                            "- Логин: admin, Пароль: admin<br>" +
                            "- Логин: manager, Пароль: manager123<br>" +
                            "- Логин: tech, Пароль: tech123<br>" +
                            "- Логин: warehouse, Пароль: warehouse123<br><br>" +
                            "<i>Для демонстрации работы используйте демо-данные</i></html>", 
                            "Ошибка", JOptionPane.WARNING_MESSAGE);
                    }
                }
            });
        });
        
        connectionThread.start();
        connectDialog.setVisible(true);
    }
    
    private void disconnectFromServer() {
        if (networkClient.isConnected()) {
            networkClient.disconnect();
            statusLabel.setText(" Статус: " + networkClient.getStatus());
            connectionLabel.setText("Сервер: не подключено ");
            JOptionPane.showMessageDialog(this, 
                "Отключено от сервера", 
                "Информация", JOptionPane.INFORMATION_MESSAGE);
            
            createTabs();
            tabbedPane.setComponentAt(0, createHomePanel());
        }
    }
    
    private void refreshAllData() {
        if (!networkClient.isConnected()) {
            JOptionPane.showMessageDialog(this, 
                "Нет подключения к серверу", 
                "Ошибка", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Обновляем текущую вкладку
        int selectedIndex = tabbedPane.getSelectedIndex();
        String selectedTitle = tabbedPane.getTitleAt(selectedIndex);
        
        // Обновляем данные на текущей вкладке
        switch (selectedTitle) {
            case "Продукция":
                tabbedPane.setComponentAt(selectedIndex, createProductsPanel());
                break;
            case "Заказы":
                tabbedPane.setComponentAt(selectedIndex, createOrdersPanel());
                break;
            case "Сырье":
                tabbedPane.setComponentAt(selectedIndex, createRawMaterialsPanel());
                break;
            case "Отчеты":
                tabbedPane.setComponentAt(selectedIndex, createReportsPanel());
                break;
            default:
                // Для других вкладок просто пересоздаем их
                createTabs();
        }
        
        JOptionPane.showMessageDialog(this, 
            "Данные обновлены для вкладки: " + selectedTitle, 
            "Информация", JOptionPane.INFORMATION_MESSAGE);
    }
    
    // Вспомогательные методы для демо-данных
    
}