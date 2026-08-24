package com.mycompany.guitesting;

import javax.swing.*;
import java.awt.*;

public class LoginPanel extends JPanel {
    private HMSApplication app;

    public LoginPanel(HMSApplication app) {
        this.app = app;
        setLayout(new GridBagLayout());
        setBackground(UITheme.BACKGROUND_COLOR);
        
        JPanel loginBox = new JPanel(new GridLayout(4, 1, 10, 15));
        loginBox.setBackground(UITheme.BACKGROUND_COLOR);
        loginBox.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("HMS Login", SwingConstants.CENTER);
        titleLabel.setFont(UITheme.TITLE_FONT);
        
        JTextField usernameField = new JTextField(15);
        usernameField.setFont(UITheme.STANDARD_FONT);
        JPasswordField passwordField = new JPasswordField(15);
        
        JButton loginButton = new JButton("Login");
        UITheme.styleButton(loginButton);

        loginButton.addActionListener(e -> {
            String user = usernameField.getText().toLowerCase();
            if (user.equals("admin")) app.navigateTo("ADMIN");
            else if (user.equals("doctor")) app.navigateTo("DOCTOR");
            else if (user.equals("patient")) app.navigateTo("PATIENT");
            else if (user.equals("manager")) app.navigateTo("MANAGER");
            else JOptionPane.showMessageDialog(this, "Type admin, doctor, patient, or manager");
        });

        loginBox.add(titleLabel);
        loginBox.add(usernameField);
        loginBox.add(passwordField);
        loginBox.add(loginButton);
        
        add(loginBox);
    }
}