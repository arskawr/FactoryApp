package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RegistrationWindow extends JDialog {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField fullNameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextField addressField;
    private JButton registerButton;
    private JButton cancelButton;
    private boolean registered = false;
    
    public RegistrationWindow(JFrame parent) {
        super(parent, "Регистрация покупателя", true);
        initComponents();
        setSize(500, 450);
        setLocationRelativeTo(parent);
        setResizable(false);
    }
    
    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(240, 245, 250));
        
        // Заголовок
        JLabel titleLabel = new JLabel("Регистрация нового покупателя", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(44, 62, 80));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Форма
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(240, 245, 250));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Имя пользователя
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Логин:*"), gbc);
        gbc.gridx = 1;
        usernameField = createStyledTextField(20);
        formPanel.add(usernameField, gbc);
        
        // Пароль
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Пароль:*"), gbc);
        gbc.gridx = 1;
        passwordField = createStyledPasswordField(20);
        formPanel.add(passwordField, gbc);
        
        // Подтверждение пароля
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Подтверждение:*"), gbc);
        gbc.gridx = 1;
        confirmPasswordField = createStyledPasswordField(20);
        formPanel.add(confirmPasswordField, gbc);
        
        // ФИО
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("ФИО:*"), gbc);
        gbc.gridx = 1;
        fullNameField = createStyledTextField(20);
        formPanel.add(fullNameField, gbc);
        
        // Email
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Email:*"), gbc);
        gbc.gridx = 1;
        emailField = createStyledTextField(20);
        formPanel.add(emailField, gbc);
        
        // Телефон
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("Телефон:*"), gbc);
        gbc.gridx = 1;
        phoneField = createStyledTextField(20);
        formPanel.add(phoneField, gbc);
        
        // Адрес
        gbc.gridx = 0; gbc.gridy = 6;
        formPanel.add(new JLabel("Адрес:"), gbc);
        gbc.gridx = 1;
        addressField = createStyledTextField(20);
        formPanel.add(addressField, gbc);
        
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        // Кнопки
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(new Color(240, 245, 250));
        
        registerButton = createStyledButton("Зарегистрироваться", new Color(52, 152, 219));
        cancelButton = createStyledButton("Отмена", new Color(231, 76, 60));
        
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (validateFields()) {
                    registered = true;
                    dispose();
                }
            }
        });
        
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registered = false;
                dispose();
            }
        });
        
        buttonPanel.add(registerButton);
        buttonPanel.add(cancelButton);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Информация
        JLabel infoLabel = new JLabel("* - обязательные поля", JLabel.CENTER);
        infoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        infoLabel.setForeground(new Color(127, 140, 141));
        mainPanel.add(infoLabel, BorderLayout.PAGE_END);
        
        add(mainPanel);
    }
    
    private JTextField createStyledTextField(int columns) {
        JTextField field = new JTextField(columns);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        return field;
    }
    
    private JPasswordField createStyledPasswordField(int columns) {
        JPasswordField field = new JPasswordField(columns);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        return field;
    }
    
    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
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
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirm = new String(confirmPasswordField.getPassword());
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        
        if (username.isEmpty() || password.isEmpty() || fullName.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Заполните все обязательные поля", 
                "Ошибка", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (!password.equals(confirm)) {
            JOptionPane.showMessageDialog(this, 
                "Пароли не совпадают", 
                "Ошибка", JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
            confirmPasswordField.setText("");
            return false;
        }
        
        if (password.length() < 6) {
            JOptionPane.showMessageDialog(this, 
                "Пароль должен содержать минимум 6 символов", 
                "Ошибка", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }
    
    public boolean isRegistered() {
        return registered;
    }
    
    public String getUsername() {
        return usernameField.getText().trim();
    }
    
    public String getPassword() {
        return new String(passwordField.getPassword());
    }
    
    public String getFullName() {
        return fullNameField.getText().trim();
    }
    
    public String getEmail() {
        return emailField.getText().trim();
    }
    
    public String getPhone() {
        return phoneField.getText().trim();
    }
    
    public String getAddress() {
        return addressField.getText().trim();
    }
}