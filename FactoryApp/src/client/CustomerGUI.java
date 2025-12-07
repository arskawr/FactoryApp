package client;

import shared.models.User;
import shared.models.Order;
import shared.models.Product;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.*;

public class CustomerGUI extends JFrame {
    private NetworkClient networkClient;
    private User currentUser;
    private JTabbedPane tabbedPane;
    private JLabel statusLabel;
    private JLabel welcomeLabel;
    
    // Корзина
    private Map<Integer, CartItem> cart = new HashMap<>();
    
    class CartItem {
        Product product;
        int quantity;
        
        CartItem(Product product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }
        
        double getTotal() {
            return product.getPrice() * quantity;
        }
    }
    
    public CustomerGUI(User user, NetworkClient networkClient) {
        this.currentUser = user;
        this.networkClient = networkClient;
        initComponents();
        setTitle("Кондитерская фабрика - Покупатель [" + user.getFullName() + "]");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setResizable(true);
    }
    
    private void initComponents() {
        // Меню
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("Файл");
        JMenuItem exitItem = new JMenuItem("Выход");
        exitItem.addActionListener(e -> {
            networkClient.logout();
            System.exit(0);
        });
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);
        
        JMenu cartMenu = new JMenu("Корзина");
        JMenuItem viewCartItem = new JMenuItem("Просмотреть корзину");
        JMenuItem clearCartItem = new JMenuItem("Очистить корзину");
        viewCartItem.addActionListener(e -> tabbedPane.setSelectedIndex(1));
        clearCartItem.addActionListener(e -> clearCart());
        cartMenu.add(viewCartItem);
        cartMenu.add(clearCartItem);
        menuBar.add(cartMenu);
        
        setJMenuBar(menuBar);
        
        // Вкладки
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Каталог", createCatalogPanel());
        tabbedPane.addTab("Корзина", createCartPanel());
        tabbedPane.addTab("Мои заказы", createMyOrdersPanel());
        tabbedPane.addTab("Профиль", createProfilePanel());
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // Статус бар
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusLabel = new JLabel(" Статус: " + networkClient.getStatus());
        welcomeLabel = new JLabel("Добро пожаловать, " + currentUser.getFullName() + "! ");
        
        // Иконка корзины
        JPanel cartPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JLabel cartIcon = new JLabel("🛒");
        cartIcon.setFont(new Font("Arial", Font.PLAIN, 16));
        cartPanel.add(cartIcon);
        JLabel cartCountLabel = new JLabel("0");
        cartCountLabel.setFont(new Font("Arial", Font.BOLD, 12));
        cartPanel.add(cartCountLabel);
        
        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(welcomeLabel, BorderLayout.CENTER);
        statusPanel.add(cartPanel, BorderLayout.EAST);
        
        add(statusPanel, BorderLayout.SOUTH);
        
        // Обновляем счетчик корзины
        updateCartCount(cartCountLabel);
    }
    
    private JPanel createCatalogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        if (!networkClient.isConnected()) {
            panel.add(createConnectionMessage(), BorderLayout.CENTER);
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
        JScrollPane scrollPane = new JScrollPane(table);
        
        // Панель кнопок
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addToCartBtn = new JButton("Добавить в корзину");
        JButton viewDetailsBtn = new JButton("Просмотреть детали");
        JButton refreshBtn = new JButton("Обновить");
        
        refreshBtn.addActionListener(e -> loadProducts(model));
        
        addToCartBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow >= 0) {
                    int id = (int) model.getValueAt(selectedRow, 0);
                    String name = (String) model.getValueAt(selectedRow, 1);
                    double price = (double) model.getValueAt(selectedRow, 3);
                    int stock = (int) model.getValueAt(selectedRow, 5);
                    
                    // Диалог выбора количества
                    String input = JOptionPane.showInputDialog(CustomerGUI.this,
                        "Количество товара '" + name + "':", "1");
                    
                    if (input != null) {
                        try {
                            int quantity = Integer.parseInt(input);
                            if (quantity > 0 && quantity <= stock) {
                                Product product = new Product();
                                product.setId(id);
                                product.setName(name);
                                product.setPrice(price);
                                
                                addToCart(product, quantity);
                                JOptionPane.showMessageDialog(CustomerGUI.this,
                                    "Товар добавлен в корзину",
                                    "Успех", JOptionPane.INFORMATION_MESSAGE);
                            } else {
                                JOptionPane.showMessageDialog(CustomerGUI.this,
                                    "Неверное количество. Доступно: " + stock,
                                    "Ошибка", JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(CustomerGUI.this,
                                "Введите корректное число",
                                "Ошибка", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(CustomerGUI.this,
                        "Выберите товар для добавления в корзину",
                        "Ошибка", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        
        viewDetailsBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                String details = "Название: " + model.getValueAt(selectedRow, 1) + "\n" +
                               "Категория: " + model.getValueAt(selectedRow, 2) + "\n" +
                               "Цена: " + model.getValueAt(selectedRow, 3) + " руб.\n" +
                               "Вес: " + model.getValueAt(selectedRow, 4) + " кг\n" +
                               "На складе: " + model.getValueAt(selectedRow, 5) + " шт.\n" +
                               "Описание: " + model.getValueAt(selectedRow, 6);
                
                JOptionPane.showMessageDialog(CustomerGUI.this, details,
                    "Информация о товаре", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        buttonPanel.add(addToCartBtn);
        buttonPanel.add(viewDetailsBtn);
        buttonPanel.add(refreshBtn);
        
        // Панель поиска
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Поиск:"));
        JTextField searchField = new JTextField(20);
        JButton searchBtn = new JButton("Найти");
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(searchPanel, BorderLayout.WEST);
        topPanel.add(buttonPanel, BorderLayout.EAST);
        
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Загрузка товаров
        loadProducts(model);
        
        return panel;
    }
    
    private JPanel createCartPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Модель таблицы корзины
        String[] columns = {"ID", "Название", "Цена (руб)", "Количество", "Сумма"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3; // Только количество можно редактировать
            }
        };
        
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(table);
        
        // Обновление суммы при изменении количества
        table.getModel().addTableModelListener(e -> {
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                int column = e.getColumn();
                if (column == 3) { // Колонка количества
                    try {
                        int productId = (int) model.getValueAt(row, 0);
                        int newQuantity = Integer.parseInt(model.getValueAt(row, 3).toString());
                        
                        if (newQuantity > 0) {
                            CartItem item = cart.get(productId);
                            if (item != null && newQuantity <= item.product.getStockQuantity()) {
                                item.quantity = newQuantity;
                                double total = item.getTotal();
                                model.setValueAt(total, row, 4);
                                updateCartTotal(panel);
                            }
                        }
                    } catch (NumberFormatException ex) {
                        // Обработка ошибки
                    }
                }
            }
        });
        
        // Панель кнопок
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton updateBtn = new JButton("Обновить");
        JButton removeBtn = new JButton("Удалить");
        JButton clearBtn = new JButton("Очистить корзину");
        JButton checkoutBtn = new JButton("Оформить заказ");
        
        updateBtn.addActionListener(e -> updateCartTable(model, panel));
        removeBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                int productId = (int) model.getValueAt(selectedRow, 0);
                cart.remove(productId);
                updateCartTable(model, panel);
            }
        });
        clearBtn.addActionListener(e -> clearCart());
        checkoutBtn.addActionListener(e -> checkoutOrder());
        
        buttonPanel.add(updateBtn);
        buttonPanel.add(removeBtn);
        buttonPanel.add(clearBtn);
        buttonPanel.add(checkoutBtn);
        
        // Итоговая сумма
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        totalPanel.add(new JLabel("Итого:"));
        JLabel totalLabel = new JLabel("0 руб.");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 14));
        totalPanel.add(totalLabel);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(totalPanel, BorderLayout.SOUTH);
        
        // Сохраняем ссылку на label для обновления
        panel.putClientProperty("totalLabel", totalLabel);
        
        // Обновляем таблицу
        updateCartTable(model, panel);
        
        return panel;
    }
    
    private JPanel createMyOrdersPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        if (!networkClient.isConnected()) {
            panel.add(createConnectionMessage(), BorderLayout.CENTER);
            return panel;
        }
        
        // Модель таблицы заказов
        String[] columns = {"ID", "Дата заказа", "Статус", "Сумма", "Примечания"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        
        // Панель кнопок
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshBtn = new JButton("Обновить");
        JButton viewDetailsBtn = new JButton("Просмотреть детали");
        
        refreshBtn.addActionListener(e -> loadMyOrders(model));
        viewDetailsBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                int orderId = (int) model.getValueAt(selectedRow, 0);
                viewOrderDetails(orderId);
            }
        });
        
        buttonPanel.add(refreshBtn);
        buttonPanel.add(viewDetailsBtn);
        
        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Загрузка заказов
        loadMyOrders(model);
        
        return panel;
    }
    
    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Информация о пользователе
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("ФИО:"), gbc);
        gbc.gridx = 1;
        JTextField nameField = new JTextField(currentUser.getFullName(), 20);
        nameField.setEditable(false);
        formPanel.add(nameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Логин:"), gbc);
        gbc.gridx = 1;
        JTextField loginField = new JTextField(currentUser.getUsername(), 20);
        loginField.setEditable(false);
        formPanel.add(loginField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        JTextField emailField = new JTextField(currentUser.getEmail(), 20);
        emailField.setEditable(false);
        formPanel.add(emailField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Телефон:"), gbc);
        gbc.gridx = 1;
        JTextField phoneField = new JTextField(currentUser.getPhone(), 20);
        phoneField.setEditable(false);
        formPanel.add(phoneField, gbc);
        
        panel.add(formPanel, BorderLayout.CENTER);
        
        // Кнопка выхода
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton logoutButton = new JButton("Выйти");
        logoutButton.addActionListener(e -> {
            networkClient.logout();
            dispose();
            new AuthWindow().setVisible(true);
        });
        bottomPanel.add(logoutButton);
        
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    // Вспомогательные методы
    private void loadProducts(DefaultTableModel model) {
        new Thread(() -> {
            try {
                java.util.List<Product> products = networkClient.getProducts();
                SwingUtilities.invokeLater(() -> {
                    model.setRowCount(0);
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
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    private void addToCart(Product product, int quantity) {
        if (cart.containsKey(product.getId())) {
            CartItem item = cart.get(product.getId());
            item.quantity += quantity;
        } else {
            cart.put(product.getId(), new CartItem(product, quantity));
        }
        
        // Обновляем счетчик корзины
        Component southPanel = getContentPane().getComponent(1);
        if (southPanel instanceof JPanel) {
            JPanel cartPanel = (JPanel) ((JPanel) southPanel).getComponent(2);
            JLabel cartCount = (JLabel) cartPanel.getComponent(1);
            updateCartCount(cartCount);
        }
    }
    
    private void updateCartCount(JLabel cartCountLabel) {
        int totalItems = cart.values().stream().mapToInt(item -> item.quantity).sum();
        cartCountLabel.setText(String.valueOf(totalItems));
    }
    
    private void updateCartTable(DefaultTableModel model, JPanel panel) {
        model.setRowCount(0);
        double total = 0;
        
        for (CartItem item : cart.values()) {
            double itemTotal = item.getTotal();
            total += itemTotal;
            model.addRow(new Object[]{
                item.product.getId(),
                item.product.getName(),
                item.product.getPrice(),
                item.quantity,
                itemTotal
            });
        }
        
        JLabel totalLabel = (JLabel) panel.getClientProperty("totalLabel");
        if (totalLabel != null) {
            totalLabel.setText(String.format("%.2f руб.", total));
        }
    }
    
    private void updateCartTotal(JPanel panel) {
        double total = cart.values().stream().mapToDouble(CartItem::getTotal).sum();
        JLabel totalLabel = (JLabel) panel.getClientProperty("totalLabel");
        if (totalLabel != null) {
            totalLabel.setText(String.format("%.2f руб.", total));
        }
    }
    
    private void clearCart() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Очистить корзину?",
            "Подтверждение",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            cart.clear();
            tabbedPane.setComponentAt(1, createCartPanel());
            
            // Обновляем счетчик
            Component southPanel = getContentPane().getComponent(1);
            if (southPanel instanceof JPanel) {
                JPanel cartPanel = (JPanel) ((JPanel) southPanel).getComponent(2);
                JLabel cartCount = (JLabel) cartPanel.getComponent(1);
                cartCount.setText("0");
            }
        }
    }
    
    private void checkoutOrder() {
        if (cart.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Корзина пуста",
                "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Диалог оформления заказа
        JTextField nameField = new JTextField(currentUser.getFullName());
        JTextField phoneField = new JTextField(currentUser.getPhone());
        JTextField addressField = new JTextField();
        JTextArea notesArea = new JTextArea(3, 20);
        
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.add(new JLabel("Имя:"));
        panel.add(nameField);
        panel.add(new JLabel("Телефон:"));
        panel.add(phoneField);
        panel.add(new JLabel("Адрес доставки:"));
        panel.add(addressField);
        panel.add(new JLabel("Примечания:"));
        panel.add(new JScrollPane(notesArea));
        
        int result = JOptionPane.showConfirmDialog(this, panel, 
            "Оформление заказа", JOptionPane.OK_CANCEL_OPTION);
        
        if (result == JOptionPane.OK_OPTION) {
            if (nameField.getText().trim().isEmpty() || phoneField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Заполните обязательные поля",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Создаем заказ
            Order order = new Order();
            order.setClientName(nameField.getText().trim());
            order.setClientPhone(phoneField.getText().trim());
            order.setClientAddress(addressField.getText().trim());
            order.setNotes(notesArea.getText().trim());
            order.setStatus("Новый");
            
            // Рассчитываем сумму
            double total = cart.values().stream().mapToDouble(CartItem::getTotal).sum();
            order.setTotalAmount(total);
            
            // Отправляем заказ
            new Thread(() -> {
                boolean success = networkClient.addOrder(order);
                SwingUtilities.invokeLater(() -> {
                    if (success) {
                        JOptionPane.showMessageDialog(CustomerGUI.this,
                            "Заказ успешно оформлен!",
                            "Успех", JOptionPane.INFORMATION_MESSAGE);
                        cart.clear();
                        tabbedPane.setComponentAt(1, createCartPanel());
                        tabbedPane.setComponentAt(2, createMyOrdersPanel());
                    } else {
                        JOptionPane.showMessageDialog(CustomerGUI.this,
                            "Ошибка при оформлении заказа",
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                });
            }).start();
        }
    }
    
    private void loadMyOrders(DefaultTableModel model) {
        new Thread(() -> {
            try {
                java.util.List<Order> orders = networkClient.getOrders();
                SwingUtilities.invokeLater(() -> {
                    model.setRowCount(0);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                    for (Order order : orders) {
                        model.addRow(new Object[]{
                            order.getId(),
                            sdf.format(order.getOrderDate()),
                            order.getStatus(),
                            String.format("%.2f руб.", order.getTotalAmount()),
                            order.getNotes()
                        });
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    private void viewOrderDetails(int orderId) {
        // Здесь можно реализовать детальный просмотр заказа
        JOptionPane.showMessageDialog(this,
            "Детали заказа #" + orderId + "\nФункция в разработке",
            "Информация о заказе",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private JPanel createConnectionMessage() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel("<html><center><h3>Нет подключения к серверу</h3>" +
            "<p>Для работы необходимо подключиться к серверу</p></center></html>", 
            JLabel.CENTER);
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }
}