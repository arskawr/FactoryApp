package client;

import javax.swing.*;
import java.awt.*;

public class RegistrationWindow extends JDialog {
    private JTextField usernameField = new JTextField();
    private JPasswordField passwordField = new JPasswordField();
    private JPasswordField confirmField = new JPasswordField();
    private JTextField fullNameField = new JTextField();
    private JTextField emailField = new JTextField();
    private JTextField phoneField = new JTextField();
    private JTextField addressField = new JTextField();
    private boolean registered = false;
    private NetworkClient networkClient;

    public RegistrationWindow(JFrame parent, NetworkClient client) {
        super(parent, "Регистрация покупателя", true);
        this.networkClient = client;

        initUI();

        setSize(580, 740);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initUI() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));
        panel.setBackground(new Color(240, 248, 255));

        JLabel title = new JLabel("Регистрация нового покупателя");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setAlignmentX(CENTER_ALIGNMENT);
        title.setForeground(new Color(20, 40, 90));
        panel.add(title);
        panel.add(Box.createVerticalStrut(35));

        addField(panel, "Логин *", usernameField);
        addField(panel, "Пароль *", passwordField);
        addField(panel, "Повторите пароль *", confirmField);
        addField(panel, "ФИО *", fullNameField);
        addField(panel, "Email *", emailField);
        addField(panel, "Телефон *", phoneField);
        addField(panel, "Адрес доставки", addressField);

        panel.add(Box.createVerticalStrut(40));

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 0));
        btns.setBackground(new Color(240, 248, 255));

        JButton reg = new JButton("Зарегистрироваться");
        JButton cancel = new JButton("Отмена");

        styleButton(reg, new Color(46, 204, 113));
        styleButton(cancel, new Color(231, 76, 60));

        btns.add(reg);
        btns.add(cancel);

        panel.add(btns);

        reg.addActionListener(e -> registerUser());
        cancel.addActionListener(e -> dispose());

        add(panel);
    }

    private void addField(JPanel panel, String text, JComponent field) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 17));
        label.setAlignmentX(CENTER_ALIGNMENT);
        panel.add(label);
        panel.add(Box.createVerticalStrut(8));

        field.setFont(new Font("Segoe UI", Font.PLAIN, 19));
        field.setMaximumSize(new Dimension(420, 52));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(70, 130, 200), 2, true),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)  // ИСПРАВЛЕНО ЗДЕСЬ!
        ));
        panel.add(field);
        panel.add(Box.createVerticalStrut(20));
    }

    private void styleButton(JButton btn, Color color) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btn.setForeground(Color.BLACK);
        btn.setBackground(color);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(220, 55));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(color.darker()); }
            public void mouseExited(java.awt.event.MouseEvent e) { btn.setBackground(color); }
        });
    }

    private void registerUser() {
        String login = usernameField.getText().trim();
        String pass1 = new String(passwordField.getPassword());
        String pass2 = new String(confirmField.getPassword());
        String name = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();

        if (login.isEmpty() || pass1.isEmpty() || name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Заполните все поля с *");
            return;
        }
        if (!pass1.equals(pass2)) {
            JOptionPane.showMessageDialog(this, "Пароли не совпадают");
            return;
        }
        if (pass1.length() < 6) {
            JOptionPane.showMessageDialog(this, "Пароль слишком короткий");
            return;
        }

        boolean ok = networkClient.register(login, pass1, name, email, phone, addressField.getText().trim());
        if (ok) {
            registered = true;
            JOptionPane.showMessageDialog(this, "Регистрация успешна!\nТеперь войдите в систему", "Успех", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Логин уже занят", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isRegistered() { return registered; }
    public String getUsername() { return usernameField.getText().trim(); }
    public String getPassword() { return new String(passwordField.getPassword()); }
    public String getFullName() { return fullNameField.getText().trim(); }
    public String getEmail() { return emailField.getText().trim(); }
    public String getPhone() { return phoneField.getText().trim(); }
    public String getAddress() { return addressField.getText().trim(); }
}