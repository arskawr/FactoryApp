package client.ui;

import shared.models.Product;
import javax.swing.*;
import java.awt.*;

public class ProductDialog extends JDialog {
    private Product product;
    private boolean confirmed;
    
    private JTextField nameField;
    private JTextField priceField;
    private JTextField weightField;
    private JTextField stockField;
    private JTextArea descriptionArea;
    private JComboBox<String> categoryCombo;
    
    public ProductDialog(JFrame parent, String title, Product product) {
        super(parent, title, true);
        this.product = product != null ? product : new Product();
        this.confirmed = false;
        
        // Устанавливаем современный стиль
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        initComponents();
        pack();
        setLocationRelativeTo(parent);
        setSize(550, 500); // Увеличиваем размер
        setResizable(false);
    }
    
    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(new Color(240, 245, 250));
        
        // Заголовок
        JLabel titleLabel = new JLabel(getTitle(), JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(44, 62, 80));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(240, 245, 250));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Название
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel nameLabel = new JLabel("Название:*");
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        formPanel.add(nameLabel, gbc);
        gbc.gridx = 1;
        nameField = createStyledTextField(30);
        nameField.setText(product.getName());
        formPanel.add(nameField, gbc);
        
        // Категория
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel categoryLabel = new JLabel("Категория:*");
        categoryLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        formPanel.add(categoryLabel, gbc);
        gbc.gridx = 1;
        String[] categories = {"Торты", "Пирожные", "Печенье", "Кексы", "Десерты", "Шоколад", "Конфеты"};
        categoryCombo = new JComboBox<>(categories);
        categoryCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        categoryCombo.setEditable(true);
        if (product.getCategory() != null) {
            categoryCombo.setSelectedItem(product.getCategory());
        }
        categoryCombo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        formPanel.add(categoryCombo, gbc);
        
        // Цена
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel priceLabel = new JLabel("Цена (руб):*");
        priceLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        formPanel.add(priceLabel, gbc);
        gbc.gridx = 1;
        priceField = createStyledTextField(30);
        priceField.setText(String.valueOf(product.getPrice()));
        formPanel.add(priceField, gbc);
        
        // Вес
        gbc.gridx = 0; gbc.gridy = 3;
        JLabel weightLabel = new JLabel("Вес (кг):*");
        weightLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        formPanel.add(weightLabel, gbc);
        gbc.gridx = 1;
        weightField = createStyledTextField(30);
        weightField.setText(String.valueOf(product.getWeight()));
        formPanel.add(weightField, gbc);
        
        // Количество на складе
        gbc.gridx = 0; gbc.gridy = 4;
        JLabel stockLabel = new JLabel("На складе:*");
        stockLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        formPanel.add(stockLabel, gbc);
        gbc.gridx = 1;
        stockField = createStyledTextField(30);
        stockField.setText(String.valueOf(product.getStockQuantity()));
        formPanel.add(stockField, gbc);
        
        // Описание
        gbc.gridx = 0; gbc.gridy = 5;
        JLabel descLabel = new JLabel("Описание:");
        descLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        formPanel.add(descLabel, gbc);
        gbc.gridx = 1;
        descriptionArea = new JTextArea(4, 30);
        descriptionArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descriptionArea.setText(product.getDescription() != null ? product.getDescription() : "");
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        JScrollPane scrollPane = new JScrollPane(descriptionArea);
        formPanel.add(scrollPane, gbc);
        
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        // Панель кнопок
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(new Color(240, 245, 250));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        JButton okButton = createStyledButton("Сохранить", new Color(46, 204, 113));
        JButton cancelButton = createStyledButton("Отмена", new Color(231, 76, 60));
        
        okButton.addActionListener(e -> {
            if (validateFields()) {
                saveProduct();
                confirmed = true;
                dispose();
            }
        });
        
        cancelButton.addActionListener(e -> {
            confirmed = false;
            dispose();
        });
        
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JTextField createStyledTextField(int columns) {
        JTextField field = new JTextField(columns);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        return field;
    }
    
    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });
        
        return button;
    }
    
    private boolean validateFields() {
        try {
            if (nameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Введите название продукта", 
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            if (categoryCombo.getSelectedItem() == null || 
                categoryCombo.getSelectedItem().toString().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Введите или выберите категорию продукта", 
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            double price = Double.parseDouble(priceField.getText().trim());
            if (price <= 0) {
                JOptionPane.showMessageDialog(this, 
                    "Цена должна быть больше 0", 
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            double weight = Double.parseDouble(weightField.getText().trim());
            if (weight <= 0) {
                JOptionPane.showMessageDialog(this, 
                    "Вес должен быть больше 0", 
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            int stock = Integer.parseInt(stockField.getText().trim());
            if (stock < 0) {
                JOptionPane.showMessageDialog(this, 
                    "Количество не может быть отрицательным", 
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            return true;
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, 
                "Проверьте правильность числовых значений", 
                "Ошибка", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    private void saveProduct() {
        product.setName(nameField.getText().trim());
        product.setCategory(categoryCombo.getSelectedItem().toString().trim());
        product.setPrice(Double.parseDouble(priceField.getText().trim()));
        product.setWeight(Double.parseDouble(weightField.getText().trim()));
        product.setStockQuantity(Integer.parseInt(stockField.getText().trim()));
        product.setDescription(descriptionArea.getText().trim());
    }
    
    public Product getProduct() {
        return product;
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }
}