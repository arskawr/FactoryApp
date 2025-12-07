package server;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Date;


public class ServerGUI extends JFrame {
    private MainServer server;
    private JTextArea logArea;
    private JLabel statusLabel;
    private JLabel clientCountLabel;
    private DefaultTableModel clientsModel;
    
    // Добавляем поля для информации о сервере
    private JLabel portLabel;
    private JLabel maxClientsLabel;
    private JLabel runningLabel;
    
    public ServerGUI(MainServer server) {
        this.server = server;
        initComponents();
        setTitle("Кондитерская фабрика - Сервер управления");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                shutdown();
            }
        });
    }
    
    private void initComponents() {
        // Главная панель с меню и вкладками
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Меню
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("Файл");
        JMenuItem startItem = new JMenuItem("Запустить сервер");
        JMenuItem stopItem = new JMenuItem("Остановить сервер");
        JMenuItem exitItem = new JMenuItem("Выход");
        
        startItem.addActionListener(e -> server.startServer());
        stopItem.addActionListener(e -> server.stopServer());
        exitItem.addActionListener(e -> shutdown());
        
        fileMenu.add(startItem);
        fileMenu.add(stopItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);
        
        setJMenuBar(menuBar);
        
        // Панель вкладок
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Вкладка 1: Мониторинг
        tabbedPane.addTab("Мониторинг", createMonitoringPanel());
        
        // Вкладка 2: Подключенные клиенты
        tabbedPane.addTab("Клиенты", createClientsPanel());
        
        // Вкладка 3: Логи
        tabbedPane.addTab("Логи", createLogPanel());
        
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        // Статус бар
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusLabel = new JLabel(" Статус: Остановлен");
        clientCountLabel = new JLabel("Клиентов: 0 ");
        
        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(clientCountLabel, BorderLayout.EAST);
        
        mainPanel.add(statusPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JPanel createMonitoringPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Панель информации о сервере
        JPanel infoPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Информация о сервере"));
        
        infoPanel.add(new JLabel("Порт:"));
        portLabel = new JLabel("5555");
        infoPanel.add(portLabel);
        
        infoPanel.add(new JLabel("Макс. клиентов:"));
        maxClientsLabel = new JLabel("10");
        infoPanel.add(maxClientsLabel);
        
        infoPanel.add(new JLabel("Запущен:"));
        runningLabel = new JLabel("Нет");
        infoPanel.add(runningLabel);
        
        infoPanel.add(new JLabel("Режим сокета:"));
        infoPanel.add(new JLabel("Блокирующий"));
        
        panel.add(infoPanel, BorderLayout.NORTH);
        
        // Кнопки управления
        JPanel controlPanel = new JPanel(new FlowLayout());
        JButton startBtn = new JButton("Запуск сервера");
        JButton stopBtn = new JButton("Остановка");
        JButton restartBtn = new JButton("Перезапуск");
        
        startBtn.addActionListener(e -> server.startServer());
        stopBtn.addActionListener(e -> server.stopServer());
        restartBtn.addActionListener(e -> {
            server.stopServer();
            server.startServer();
        });
        
        controlPanel.add(startBtn);
        controlPanel.add(stopBtn);
        controlPanel.add(restartBtn);
        
        panel.add(controlPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createClientsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Таблица клиентов
        String[] columns = {"IP адрес", "Порт", "Статус", "Роль", "Время подключения"};
        clientsModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Запрещаем редактирование
            }
        };
        JTable clientsTable = new JTable(clientsModel);
        
        JScrollPane scrollPane = new JScrollPane(clientsTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Журнал событий"));
        
        // Панель управления логом
        JPanel logControlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton clearBtn = new JButton("Очистить");
        JCheckBox autoScroll = new JCheckBox("Автопрокрутка", true);
        
        clearBtn.addActionListener(e -> logArea.setText(""));
        autoScroll.addActionListener(e -> {
            if (autoScroll.isSelected()) {
                logArea.setCaretPosition(logArea.getDocument().getLength());
            }
        });
        
        logControlPanel.add(clearBtn);
        logControlPanel.add(autoScroll);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(logControlPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void shutdown() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Вы уверены, что хотите выключить сервер?",
            "Подтверждение выхода",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            server.shutdown();
        }
    }
    
    public void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(String.format("[%tT] %s\n", new Date(), message));
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
    
    public void updateStatus(String status) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(" Статус: " + status);
        });
    }
    
    public void updateClientCount(int count) {
        SwingUtilities.invokeLater(() -> {
            clientCountLabel.setText("Клиентов: " + count + " ");
        });
    }
    
    // Метод для обновления информации о сервере
    public void updateServerInfo(String port, String maxClients, String isRunning) {
        SwingUtilities.invokeLater(() -> {
            portLabel.setText(port);
            maxClientsLabel.setText(maxClients);
            runningLabel.setText(isRunning);
        });
    }
    
    // Методы для работы с таблицей клиентов
    public void addClientToTable(String clientInfo, int port, String status, String role, String connectTime) {
        SwingUtilities.invokeLater(() -> {
            clientsModel.addRow(new Object[]{clientInfo, String.valueOf(port), status, role, connectTime});
        });
    }
    
    public void updateClientInTable(String clientInfo, String status, String role) {
        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < clientsModel.getRowCount(); i++) {
                if (clientsModel.getValueAt(i, 0).equals(clientInfo)) {
                    clientsModel.setValueAt(status, i, 2);
                    clientsModel.setValueAt(role, i, 3);
                    break;
                }
            }
        });
    }
    
    public void removeClientFromTable(String clientInfo) {
        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < clientsModel.getRowCount(); i++) {
                if (clientsModel.getValueAt(i, 0).equals(clientInfo)) {
                    clientsModel.removeRow(i);
                    break;
                }
            }
        });
    }
}