package client;

import javax.swing.*;

public class MainClient {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AuthWindow authWindow = new AuthWindow();
            authWindow.setVisible(true);
        });
    }
}

