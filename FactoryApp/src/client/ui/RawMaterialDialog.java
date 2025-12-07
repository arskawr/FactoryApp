package client.ui;

import shared.models.RawMaterial;
import javax.swing.*;
import java.awt.*;
import java.util.Date;

public class RawMaterialDialog extends JDialog {
    private RawMaterial material;
    private boolean confirmed;
    
    private JTextField nameField;
    private JTextField categoryField;
    private JTextField currentStockField;
    private JTextField minStockField;
    private JTextField maxStockField;
    private JTextField priceField;
    private JTextField supplierField;
    private JTextArea notesArea;
    
    public RawMaterialDialog(JFrame parent, String title, RawMaterial material) {
        super(parent, title, true);
        this.material = material != null ? material : new RawMaterial();
        this.confirmed = false;
        
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        initComponents();
        pack();
        setLocationRelativeTo(parent);
        setSize(500, 600);
        setResizable(false);
    }
    
    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(new Color(240, 245, 250));
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(240, 245, 250));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Название
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Название:*"), gbc);
        gbc.gridx = 1;
        nameField = createStyledTextField(25);
        nameField.setText(material.getName());
        formPanel.add(nameField, gbc);
        
        // Категория
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Категория:*"), gbc);
        gbc.gridx = 1;
        categoryField = createStyledTextField(25);
        categoryField.setText(material.getCategory());
        formPanel.add(categoryField, gbc);
        
        // Единица измерения
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Единица:*"), gbc);
        gbc.gridx = 1;
        String[] units = {"кг", "г", "л", "мл", "шт", "уп", "бут"};
        JComboBox<String> unitCombo = new JComboBox<>(units);
        unitCombo.setEditable(true);
        if (material.getUnit() != null) {
            unitCombo.setSelectedItem(material.getUnit());
        }
        unitCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        unitCombo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        formPanel.add(unitCombo, gbc);
        
        // Текущий запас
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Текущий запас:*"), gbc);
        gbc.gridx = 1;
        currentStockField = createStyledTextField(25);
        currentStockField.setText(String.valueOf(material.getCurrentStock()));
        formPanel.add(currentStockField, gbc);
        
        // Минимальный запас
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Мин. запас:*"), gbc);
        gbc.gridx = 1;
        minStockField = createStyledTextField(25);
        minStockField.setText(String.valueOf(material.getMinStock()));
        formPanel.add(minStockField, gbc);
        
        // Максимальный запас
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("Макс. запас:*"), gbc);
        gbc.gridx = 1;
        maxStockField = createStyledTextField(25);
        maxStockField.setText(String.valueOf(material.getMaxStock()));
        formPanel.add(maxStockField, gbc);
        
        // Цена
        gbc.gridx = 0; gbc.gridy = 6;
        formPanel.add(new JLabel("Цена (руб):"), gbc);
        gbc.gridx = 1;
        priceField = createStyledTextField(25);
        priceField.setText(String.valueOf(material.getLastPurchasePrice()));
        formPanel.add(priceField, gbc);
        
        // Поставщик
        gbc.gridx = 0; gbc.gridy = 7;
        formPanel.add(new JLabel("Поставщик:"), gbc);
        gbc.gridx = 1;
        supplierField = createStyledTextField(25);
        supplierField.setText(material.getSupplier());
        formPanel.add(supplierField, gbc);
        
        // Примечания
        gbc.gridx = 0; gbc.gridy = 8;
        formPanel.add(new JLabel("Примечания:"), gbc);
        gbc.gridx = 1;
        notesArea = new JTextArea(4, 25);
        notesArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        notesArea.setText(material.getNotes());
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        JScrollPane scrollPane = new JScrollPane(notesArea);
        formPanel.add(scrollPane, gbc);
        
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        // Панель кнопок
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(new Color(240, 245, 250));
        
        JButton okButton = createStyledButton("Сохранить", new Color(46, 204, 113));
        JButton cancelButton = createStyledButton("Отмена", new Color(231, 76, 60));
        
        okButton.addActionListener(e -> {
            if (validateFields()) {
                saveMaterial(unitCombo);
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
                JOptionPane.showMessageDialog(this, "Введите название сырья", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            if (categoryField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Введите категорию сырья", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            double currentStock = Double.parseDouble(currentStockField.getText().trim());
            if (currentStock < 0) {
                JOptionPane.showMessageDialog(this, "Текущий запас не может быть отрицательным", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            double minStock = Double.parseDouble(minStockField.getText().trim());
            if (minStock < 0) {
                JOptionPane.showMessageDialog(this, "Минимальный запас не может быть отрицательным", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            double maxStock = Double.parseDouble(maxStockField.getText().trim());
            if (maxStock < 0) {
                JOptionPane.showMessageDialog(this, "Максимальный запас не может быть отрицательным", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            if (minStock > maxStock) {
                JOptionPane.showMessageDialog(this, "Минимальный запас не может быть больше максимального", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            return true;
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Проверьте правильность числовых значений", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    private void saveMaterial(JComboBox<String> unitCombo) {
        material.setName(nameField.getText().trim());
        material.setCategory(categoryField.getText().trim());
        material.setUnit(unitCombo.getSelectedItem().toString().trim());
        material.setCurrentStock(Double.parseDouble(currentStockField.getText().trim()));
        material.setMinStock(Double.parseDouble(minStockField.getText().trim()));
        material.setMaxStock(Double.parseDouble(maxStockField.getText().trim()));
        material.setLastPurchasePrice(Double.parseDouble(priceField.getText().trim()));
        material.setSupplier(supplierField.getText().trim());
        material.setNotes(notesArea.getText().trim());
        material.setLastDeliveryDate(new Date());
    }
    
    public RawMaterial getMaterial() {
        return material;
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }
}