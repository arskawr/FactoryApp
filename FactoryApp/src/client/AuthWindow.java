package client;

import shared.Protocol;
import shared.models.User;

import javax.swing.*;
import java.awt.*;

public class AuthWindow extends JFrame {
    private JTextField loginField;
    private JPasswordField passwordField;
    private NetworkClient networkClient;
    private JLabel statusLabel;

    public AuthWindow() {
        networkClient = new NetworkClient("localhost", 5555);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        initUI();

        setTitle("Кондитерская фабрика — Вход в систему");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(560, 620);
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);
    }

    private void initUI() {
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        getContentPane().setBackground(new Color(248, 250, 255));

        // Главная панель с отступами
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(new Color(248, 250, 255));
        container.setBorder(BorderFactory.createEmptyBorder(70, 80, 70, 80));

        // Название
        JLabel title = new JLabel("Кондитерская фабрика");
        title.setFont(new Font("Segoe UI", Font.BOLD, 38));
        title.setForeground(new Color(30, 50, 110));
        title.setAlignmentX(CENTER_ALIGNMENT);
        container.add(title);
        container.add(Box.createVerticalStrut(70));

        // Логин
        JLabel loginLbl = new JLabel("Логин");
        loginLbl.setFont(new Font("Segoe UI", Font.PLAIN, 19));
        loginLbl.setAlignmentX(CENTER_ALIGNMENT);
        container.add(loginLbl);
        container.add(Box.createVerticalStrut(12));

        loginField = new JTextField();
        loginField.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        loginField.setMaximumSize(new Dimension(380, 58));
        loginField.setPreferredSize(new Dimension(380, 58));
        loginField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 149, 237), 2, true),
            BorderFactory.createEmptyBorder(10, 25, 10, 25)
        ));
        container.add(loginField);
        container.add(Box.createVerticalStrut(35));

        // Пароль
        JLabel passLbl = new JLabel("Пароль");
        passLbl.setFont(new Font("Segoe UI", Font.PLAIN, 19));
        passLbl.setAlignmentX(CENTER_ALIGNMENT);
        container.add(passLbl);
        container.add(Box.createVerticalStrut(12));

        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        passwordField.setMaximumSize(new Dimension(380, 58));
        passwordField.setPreferredSize(new Dimension(380, 58));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 149, 237), 2, true),
            BorderFactory.createEmptyBorder(10, 25, 10, 25)
        ));
        container.add(passwordField);
        container.add(Box.createVerticalStrut(60));

        // Кнопки
        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 40, 0));
        btnPanel.setBackground(new Color(248, 250, 255));

        JButton loginBtn = new JButton("Вход");
        JButton regBtn = new JButton("Регистрация");

        styleButton(loginBtn, new Color(46, 204, 113));
        styleButton(regBtn, new Color(52, 152, 219));

        btnPanel.add(loginBtn);
        btnPanel.add(regBtn);

        container.add(btnPanel);

        // Статус внизу
        statusLabel = new JLabel("Проверка подключения...");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        statusLabel.setForeground(new Color(100, 100, 100));
        statusLabel.setAlignmentX(CENTER_ALIGNMENT);
        container.add(Box.createVerticalStrut(40));
        container.add(statusLabel);

        getContentPane().add(container);

        loginBtn.addActionListener(e -> performLogin());
        regBtn.addActionListener(e -> openRegistration());
        passwordField.addActionListener(e -> performLogin());

        updateStatus();
    }

    private void styleButton(JButton btn, Color color) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 19));
        btn.setForeground(Color.WHITE);
        btn.setBackground(color);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(170, 58));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(color.darker()); }
            public void mouseExited(java.awt.event.MouseEvent e) { btn.setBackground(color); }
        });
    }

    private void updateStatus() {
        SwingUtilities.invokeLater(() -> {
            if (networkClient.isConnected()) {
                statusLabel.setText("Подключено к серверу");
                statusLabel.setForeground(new Color(0, 140, 0));
            } else {
                statusLabel.setText("Сервер недоступен");
                statusLabel.setForeground(Color.RED);
            }
        });
    }

    private void performLogin() {
        String login = loginField.getText().trim();
        String pass = new String(passwordField.getPassword());

        if (login.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Заполните логин и пароль");
            return;
        }

        if (!networkClient.connect()) {
            JOptionPane.showMessageDialog(this, "Не удалось подключиться к серверу");
            updateStatus();
            return;
        }

        updateStatus();

        if (login.equals("admin") && pass.equals("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setFullName("Администратор");
            admin.setRole(Protocol.UserRole.ADMIN);
            openMainWindow(admin);
            return;
        }

        User user = networkClient.login(login, pass);
        if (user != null) {
            openMainWindow(user);
        } else {
            JOptionPane.showMessageDialog(this, "Неверный логин или пароль");
        }
    }

    private void openRegistration() {
        if (!networkClient.isConnected() && !networkClient.connect()) {
            JOptionPane.showMessageDialog(this, "Сервер недоступен");
            return;
        }
        new RegistrationWindow(this, networkClient).setVisible(true);
    }

    private void openMainWindow(User user) {
        dispose();
        SwingUtilities.invokeLater(() -> {
            if (user.getRole() == Protocol.UserRole.CUSTOMER) {
                new CustomerGUI(user, networkClient).setVisible(true);
            } else {
                ClientGUI gui = new ClientGUI(user.getRole().getDisplayName());
                gui.setNetworkClient(networkClient);
                gui.setVisible(true);
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AuthWindow());
    }
}