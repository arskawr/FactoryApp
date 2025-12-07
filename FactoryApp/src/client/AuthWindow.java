package client;

import javax.swing.*;
import shared.models.User;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AuthWindow extends JFrame {
    private JTextField loginField;
    private JPasswordField passwordField;
    private JComboBox<String> roleCombo;
    private JButton loginButton;
    private JButton exitButton;
    private boolean loginInProgress = false;
    
    public AuthWindow() {
        initComponents();
        setTitle("Авторизация - Кондитерская фабрика");
        setSize(450, 350); // Увеличиваем размер
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
    }
    
    private void initComponents() {
        // Устанавливаем современный стиль
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        mainPanel.setBackground(new Color(240, 245, 250));
        
        // Заголовок
        JLabel titleLabel = new JLabel("Система автоматизации кондитерской фабрики", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(44, 62, 80));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Центральная панель с полями
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(new Color(240, 245, 250));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Логин
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel loginLabel = new JLabel("Логин:");
        loginLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        centerPanel.add(loginLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = 0;
        loginField = new JTextField(18);
        loginField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        loginField.setText("admin");
        loginField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        centerPanel.add(loginField, gbc);
        
        // Пароль
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel passwordLabel = new JLabel("Пароль:");
        passwordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        centerPanel.add(passwordLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = 1;
        passwordField = new JPasswordField(18);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setText("admin");
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        centerPanel.add(passwordField, gbc);
        
        // Роль
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel roleLabel = new JLabel("Роль:");
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        centerPanel.add(roleLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = 2;
        String[] roles = {"Администратор", "Менеджер", "Технолог", "Кладовщик","Покупатель"};
        roleCombo = new JComboBox<>(roles);
        roleCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        roleCombo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        centerPanel.add(roleCombo, gbc);
        
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        // Панель кнопок
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(new Color(240, 245, 250));
        
        loginButton = createStyledButton("Вход", new Color(46, 204, 113));
        exitButton = createStyledButton("Выход", new Color(231, 76, 60));
        
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!loginInProgress) {
                    loginInProgress = true;
                    performLogin();
                }
            }
        });
        
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        
// В initComponents() после создания exitButton добавьте:
JButton registerButton = createStyledButton("Регистрация", new Color(52, 152, 219));



registerButton.addActionListener(new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) {
        RegistrationWindow regWindow = new RegistrationWindow(AuthWindow.this);
        regWindow.setVisible(true);
        
        if (regWindow.isRegistered()) {
            // Показываем сообщение об успешной регистрации
            JOptionPane.showMessageDialog(AuthWindow.this,
                "Регистрация успешна! Теперь вы можете войти.",
                "Успех", JOptionPane.INFORMATION_MESSAGE);
            
            // Заполняем поля логина
            loginField.setText(regWindow.getUsername());
            passwordField.setText(regWindow.getPassword());
        }
    }
});

// Добавьте кнопку на панель
buttonPanel.add(registerButton);

        
        buttonPanel.add(loginButton);
        buttonPanel.add(exitButton);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Добавляем Enter для быстрого входа
        getRootPane().setDefaultButton(loginButton);
        
        // Информационная панель
        JLabel infoLabel = new JLabel("Для демо-входа используйте: admin / admin", JLabel.CENTER);
        infoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        infoLabel.setForeground(new Color(127, 140, 141));
        mainPanel.add(infoLabel, BorderLayout.PAGE_END);
        
        add(mainPanel);
    }
    
    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Эффект при наведении
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
    
    private void performLogin() {
        String login = loginField.getText().trim();
        String password = new String(passwordField.getPassword());
        String role = (String) roleCombo.getSelectedItem();
        
        if (login.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Заполните все поля", 
                "Ошибка", JOptionPane.WARNING_MESSAGE);
            loginInProgress = false;
            return;
        }
        
        // Открываем соединение
        NetworkClient networkClient = new NetworkClient("localhost", 5555);
        if (!networkClient.connect()) {
            JOptionPane.showMessageDialog(this,
                "Не удалось подключиться к серверу",
                "Ошибка", JOptionPane.ERROR_MESSAGE);
            loginInProgress = false;
            return;
        }
        
        // Аутентификация
        User user = networkClient.login(login, password, role);
        
        if (user != null) {
            this.dispose();
            
            SwingUtilities.invokeLater(() -> {
                if ("Покупатель".equals(role)) {
                    CustomerGUI customerGUI = new CustomerGUI(user, networkClient);
                    customerGUI.setVisible(true);
                } else {
                    ClientGUI clientGUI = new ClientGUI(role);
                    clientGUI.setVisible(true);
                    // Передаем networkClient в ClientGUI
                    clientGUI.setNetworkClient(networkClient);
                }
            });
        } else {
            JOptionPane.showMessageDialog(this, 
                "Неверный логин или пароль", 
                "Ошибка", JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
            networkClient.disconnect();
        }
    
    loginInProgress = false;
}
}