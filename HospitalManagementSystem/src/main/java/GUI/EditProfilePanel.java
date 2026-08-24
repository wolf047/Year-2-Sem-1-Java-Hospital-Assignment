package com.mycompany.guitesting;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class EditProfilePanel extends JPanel {

    private final Color MAIN_BG = new Color(248, 250, 252);
    private final Color PANEL_BG = Color.WHITE;
    private final Color TEXT_BLACK = new Color(15, 23, 42);
    private final Color TEXT_MUTED = new Color(100, 116, 139);
    private final Color ACCENT_BLUE = new Color(14, 165, 233);
    private final Color BORDER_COLOR = new Color(226, 232, 240);

    // Form Components
    private JTextField txtFullName;
    private JTextField txtEmail;
    private JTextField txtPhone;
    private JPasswordField txtPassword;
    private JPasswordField txtConfirmPassword;

    public EditProfilePanel(String roleName) {
        setLayout(new BorderLayout(0, 30));
        setBackground(MAIN_BG);
        setBorder(new EmptyBorder(40, 60, 40, 60));

        // ==========================================
        // 1. HEADER SECTION
        // ==========================================
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(MAIN_BG);
        
        JLabel title = new JLabel("My Profile Settings");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(TEXT_BLACK);
        
        JLabel subtitle = new JLabel("Update your personal information and account credentials for your " + roleName + " account.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtitle.setForeground(TEXT_MUTED);
        
        headerPanel.add(title, BorderLayout.NORTH);
        headerPanel.add(subtitle, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // ==========================================
        // 2. CENTERED FORM CARD
        // ==========================================
        JPanel centerWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        centerWrapper.setBackground(MAIN_BG);

        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBackground(PANEL_BG);
        
        // Corrected Dual-Tone Border
        formCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(4, 0, 0, 0, ACCENT_BLUE),
                BorderFactory.createMatteBorder(0, 1, 1, 1, BORDER_COLOR)
            ),
            new EmptyBorder(40, 50, 40, 50)
        ));
        formCard.setPreferredSize(new Dimension(500, 500));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 15, 0);
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;

        // Initialize fields
        txtFullName = createTextField();
        txtEmail = createTextField();
        txtPhone = createTextField();
        
        txtPassword = new JPasswordField();
        stylePasswordField(txtPassword);
        
        txtConfirmPassword = new JPasswordField();
        stylePasswordField(txtConfirmPassword);

        // Add fields to card
        formCard.add(createSectionTitle("Personal Details"), gbc); gbc.gridy++;
        formCard.add(createFormLabel("Full Name:"), gbc); gbc.gridy++;
        formCard.add(txtFullName, gbc); gbc.gridy++;
        
        formCard.add(createFormLabel("Email Address:"), gbc); gbc.gridy++;
        formCard.add(txtEmail, gbc); gbc.gridy++;
        
        formCard.add(createFormLabel("Contact Number:"), gbc); gbc.gridy++;
        formCard.add(txtPhone, gbc); gbc.gridy++;
        
        gbc.insets = new Insets(20, 0, 15, 0); // Extra space before security section
        formCard.add(createSectionTitle("Security & Credentials"), gbc); gbc.gridy++;
        
        gbc.insets = new Insets(0, 0, 15, 0);
        formCard.add(createFormLabel("New Password:"), gbc); gbc.gridy++;
        formCard.add(txtPassword, gbc); gbc.gridy++;
        
        formCard.add(createFormLabel("Confirm Password:"), gbc); gbc.gridy++;
        formCard.add(txtConfirmPassword, gbc); gbc.gridy++;

        // Action Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        buttonPanel.setBackground(PANEL_BG);
        buttonPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        JButton btnSave = styleButton(new JButton("Save Changes"), ACCENT_BLUE, Color.BLACK);
        JButton btnCancel = styleButton(new JButton("Cancel"), new Color(226, 232, 240), Color.BLACK);
        
        buttonPanel.add(btnCancel);
        buttonPanel.add(btnSave);
        
        formCard.add(buttonPanel, gbc);
        centerWrapper.add(formCard);
        
        add(centerWrapper, BorderLayout.CENTER);
    }

    // --- Helper Methods ---
    private JLabel createSectionTitle(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lbl.setForeground(TEXT_BLACK);
        lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        return lbl;
    }

    private JLabel createFormLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(TEXT_MUTED); 
        return lbl;
    }

    private JTextField createTextField() {
        JTextField tf = new JTextField();
        tf.setPreferredSize(new Dimension(400, 40));
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR), new EmptyBorder(5, 10, 5, 10)
        ));
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        tf.setBackground(MAIN_BG); 
        return tf;
    }

    private void stylePasswordField(JPasswordField pf) {
        pf.setPreferredSize(new Dimension(400, 40));
        pf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR), new EmptyBorder(5, 10, 5, 10)
        ));
        pf.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        pf.setBackground(MAIN_BG);
    }

    private JButton styleButton(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(bg.darker()));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(140, 40));
        return btn;
    }
}