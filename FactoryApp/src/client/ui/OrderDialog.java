package client.ui;

import shared.models.Order;
import shared.models.Product;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OrderDialog extends JDialog {
    private Order order;
    private boolean confirmed;
    
    private JTextField clientNameField;
    private JTextField clientPhoneField;
    private JTextField clientAddressField;
    private JTextArea notesArea;
    private JComboBox<String> statusCombo;
    private JTable itemsTable;
    private DefaultTableModel itemsModel;
    private JLabel totalLabel;
    
    // Временный список продуктов (в реальном приложении будет из БД)
    private List<Product> availableProducts = new ArrayList<>();
    
    public OrderDialog(JFrame parent, String title, Order order) {
        super(parent, title, true);
        this.order = order != null ? order : new Order();
        this.confirmed = false;
        
        // Заполняем список продуктов для демонстрации
        initDemoProducts();
        
        initComponents();
        pack();
        setLocationRelativeTo(parent);
        setSize(800, 600);
    }
    
    private void initDemoProducts() {
        availableProducts.add(new shared.models.Product(1, "Шоколадный торт", "Торты", 1200.0, 1.5, 10));
        availableProducts.add(new shared.models.Product(2, "Кекс ванильный", "Кексы", 150.0, 0.2, 50));
        availableProducts.add(new shared.models.Product(3, "Печенье овсяное", "Печенье", 80.0, 0.1, 100));
        availableProducts.add(new shared.models.Product(4, "Эклеры", "Пирожные", 60.0, 0.05, 200));
        availableProducts.add(new shared.models.Product(5, "Тирамису", "Десерты", 450.0, 0.3, 25));
    }
    
    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Панель информации о клиенте
        JPanel clientPanel = new JPanel(new GridBagLayout());
        clientPanel.setBorder(BorderFactory.createTitledBorder("Информация о клиенте"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Клиент
        gbc.gridx = 0; gbc.gridy = 0;
        clientPanel.add(new JLabel("Имя клиента:"), gbc);
        gbc.gridx = 1;
        clientNameField = new JTextField(20);
        clientNameField.setText(order.getClientName());
        clientPanel.add(clientNameField, gbc);
        
        // Телефон
        gbc.gridx = 0; gbc.gridy = 1;
        clientPanel.add(new JLabel("Телефон:"), gbc);
        gbc.gridx = 1;
        clientPhoneField = new JTextField(20);
        clientPhoneField.setText(order.getClientPhone());
        clientPanel.add(clientPhoneField, gbc);
        
        // Адрес
        gbc.gridx = 0; gbc.gridy = 2;
        clientPanel.add(new JLabel("Адрес доставки:"), gbc);
        gbc.gridx = 1;
        clientAddressField = new JTextField(20);
        clientAddressField.setText(order.getClientAddress());
        clientPanel.add(clientAddressField, gbc);
        
        // Статус
        gbc.gridx = 0; gbc.gridy = 3;
        clientPanel.add(new JLabel("Статус:"), gbc);
        gbc.gridx = 1;
        String[] statuses = {"Новый", "В производстве", "Готов", "Отгружен", "Выполнен", "Отменен"};
        statusCombo = new JComboBox<>(statuses);
        statusCombo.setSelectedItem(order.getStatus());
        clientPanel.add(statusCombo, gbc);
        
        // Дата заказа
        gbc.gridx = 0; gbc.gridy = 4;
        clientPanel.add(new JLabel("Дата заказа:"), gbc);
        gbc.gridx = 1;
        JTextField orderDateField = new JTextField(new SimpleDateFormat("dd.MM.yyyy HH:mm").format(
            order.getOrderDate() != null ? order.getOrderDate() : new Date()));
        orderDateField.setEditable(false);
        clientPanel.add(orderDateField, gbc);
        
        mainPanel.add(clientPanel, BorderLayout.NORTH);
        
        // Панель позиций заказа
        JPanel itemsPanel = new JPanel(new BorderLayout());
        itemsPanel.setBorder(BorderFactory.createTitledBorder("Позиции заказа"));
        
        // Таблица позиций
        String[] columns = {"Продукт", "Количество", "Цена", "Сумма"};
        itemsModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 3; // Сумма не редактируется
            }
        };
        
        itemsTable = new JTable(itemsModel);
        itemsTable.getColumnModel().getColumn(0).setPreferredWidth(200);
        itemsTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        itemsTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        itemsTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        
        // Заполняем существующие позиции
        if (order.getItems() != null) {
            for (Order.OrderItem item : order.getItems()) {
                itemsModel.addRow(new Object[]{
                    item.getProductName(),
                    item.getQuantity(),
                    item.getPrice(),
                    item.getTotal()
                });
            }
        }
        
        JScrollPane scrollPane = new JScrollPane(itemsTable);
        itemsPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Панель управления позициями
        JPanel itemsControlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addItemBtn = new JButton("Добавить позицию");
        JButton removeItemBtn = new JButton("Удалить позицию");
        
        addItemBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAddItemDialog();
            }
        });
        
        removeItemBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = itemsTable.getSelectedRow();
                if (selectedRow >= 0) {
                    itemsModel.removeRow(selectedRow);
                    updateTotal();
                }
            }
        });
        
        itemsControlPanel.add(addItemBtn);
        itemsControlPanel.add(removeItemBtn);
        
        itemsPanel.add(itemsControlPanel, BorderLayout.SOUTH);
        
        mainPanel.add(itemsPanel, BorderLayout.CENTER);
        
        // Панель заметок и итогов
        JPanel bottomPanel = new JPanel(new BorderLayout());
        
        // Заметки
        JPanel notesPanel = new JPanel(new BorderLayout());
        notesPanel.setBorder(BorderFactory.createTitledBorder("Примечания"));
        notesArea = new JTextArea(3, 20);
        notesArea.setText(order.getNotes());
        notesArea.setLineWrap(true);
        notesPanel.add(new JScrollPane(notesArea), BorderLayout.CENTER);
        
        // Итоговая сумма
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        totalPanel.add(new JLabel("Итоговая сумма:"));
        totalLabel = new JLabel(calculateTotal() + " руб.");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 14));
        totalPanel.add(totalLabel);
        
        bottomPanel.add(notesPanel, BorderLayout.CENTER);
        bottomPanel.add(totalPanel, BorderLayout.SOUTH);
        
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        // Панель кнопок
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okButton = new JButton("Сохранить");
        JButton cancelButton = new JButton("Отмена");
        
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (validateFields()) {
                    saveOrder();
                    confirmed = true;
                    dispose();
                }
            }
        });
        
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                confirmed = false;
                dispose();
            }
        });
        
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        
        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void showAddItemDialog() {
        JDialog itemDialog = new JDialog(this, "Добавить позицию", true);
        itemDialog.setLayout(new BorderLayout());
        itemDialog.setSize(400, 200);
        itemDialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Выбор продукта
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Продукт:"), gbc);
        gbc.gridx = 1;
        String[] productNames = availableProducts.stream()
            .map(p -> p.getId() + ". " + p.getName() + " - " + p.getPrice() + " руб.")
            .toArray(String[]::new);
        JComboBox<String> productCombo = new JComboBox<>(productNames);
        panel.add(productCombo, gbc);
        
        // Количество
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Количество:"), gbc);
        gbc.gridx = 1;
        JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
        panel.add(quantitySpinner, gbc);
        
        itemDialog.add(panel, BorderLayout.CENTER);
        
        // Кнопки
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Добавить");
        JButton cancelButton = new JButton("Отмена");
        
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = productCombo.getSelectedIndex();
                if (selectedIndex >= 0) {
                    Product product = availableProducts.get(selectedIndex);
                    int quantity = (int) quantitySpinner.getValue();
                    
                    itemsModel.addRow(new Object[]{
                        product.getName(),
                        quantity,
                        product.getPrice(),
                        product.getPrice() * quantity
                    });
                    
                    updateTotal();
                    itemDialog.dispose();
                }
            }
        });
        
        cancelButton.addActionListener(e -> itemDialog.dispose());
        
        buttonPanel.add(addButton);
        buttonPanel.add(cancelButton);
        itemDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        itemDialog.setVisible(true);
    }
    
    private void updateTotal() {
        double total = 0;
        for (int i = 0; i < itemsModel.getRowCount(); i++) {
            total += (double) itemsModel.getValueAt(i, 3);
        }
        totalLabel.setText(total + " руб.");
    }
    
    private double calculateTotal() {
        double total = 0;
        for (int i = 0; i < itemsModel.getRowCount(); i++) {
            total += (double) itemsModel.getValueAt(i, 3);
        }
        return total;
    }
    
    private boolean validateFields() {
        if (clientNameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Введите имя клиента", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (clientPhoneField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Введите телефон клиента", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (itemsModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Добавьте хотя бы одну позицию в заказ", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }
    
    private void saveOrder() {
        order.setClientName(clientNameField.getText().trim());
        order.setClientPhone(clientPhoneField.getText().trim());
        order.setClientAddress(clientAddressField.getText().trim());
        order.setStatus((String) statusCombo.getSelectedItem());
        order.setNotes(notesArea.getText().trim());
        order.setTotalAmount(calculateTotal());
        
        // Сохраняем позиции
        List<Order.OrderItem> items = new ArrayList<>();
        for (int i = 0; i < itemsModel.getRowCount(); i++) {
            String productName = (String) itemsModel.getValueAt(i, 0);
            int quantity = (int) itemsModel.getValueAt(i, 1);
            double price = (double) itemsModel.getValueAt(i, 2);
            
            // Находим продукт по имени
            Product product = availableProducts.stream()
                .filter(p -> p.getName().equals(productName))
                .findFirst()
                .orElse(null);
            
            if (product != null) {
                items.add(new Order.OrderItem(product.getId(), productName, quantity, price));
            }
        }
        
        order.setItems(items);
    }
    
    public Order getOrder() {
        return order;
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }
}