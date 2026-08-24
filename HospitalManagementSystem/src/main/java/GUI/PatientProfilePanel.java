package com.mycompany.guitesting;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class PatientProfilePanel extends JPanel {

    private final Color MAIN_BG = new Color(248, 250, 252);
    private final Color PANEL_BG = Color.WHITE;
    private final Color TEXT_BLACK = new Color(15, 23, 42);
    private final Color TEXT_MUTED = new Color(100, 116, 139);
    private final Color ACCENT_TEAL = new Color(13, 148, 136);
    private final Color BORDER_COLOR = new Color(226, 232, 240);

    public PatientProfilePanel() {
        setLayout(new BorderLayout(0, 30));
        setBackground(MAIN_BG);
        setBorder(new EmptyBorder(30, 60, 30, 60));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(MAIN_BG);
        JLabel title = new JLabel("Patient Account Settings");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(TEXT_BLACK);
        JLabel subtitle = new JLabel("Manage your personal contact details, medical history notes, and insurance information.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(TEXT_MUTED);
        headerPanel.add(title, BorderLayout.NORTH);
        headerPanel.add(subtitle, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // Centered Card
        JPanel centerWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        centerWrapper.setBackground(MAIN_BG);

        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBackground(PANEL_BG);
        formCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(4, 0, 0, 0, ACCENT_TEAL),
                BorderFactory.createLineBorder(BORDER_COLOR, 1)
            ),
            new EmptyBorder(30, 40, 30, 40)
        ));
        formCard.setPreferredSize(new Dimension(500, 520));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 12, 0);
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;

        JTextField txtName = createTextField("Sarah Jenkins");
        JTextField txtPhone = createTextField("+60 12-345 6789");
        JTextField txtInsurance = createTextField("AIA Health Platinum");
        JPasswordField txtPass = new JPasswordField();
        stylePasswordField(txtPass);

        formCard.add(createFormLabel("Full Name:"), gbc); gbc.gridy++;
        formCard.add(txtName, gbc); gbc.gridy++;
        formCard.add(createFormLabel("Contact Number:"), gbc); gbc.gridy++;
        formCard.add(txtPhone, gbc); gbc.gridy++;
        formCard.add(createFormLabel("Insurance Provider:"), gbc); gbc.gridy++;
        formCard.add(txtInsurance, gbc); gbc.gridy++;
        formCard.add(createFormLabel("New Password:"), gbc); gbc.gridy++;
        formCard.add(txtPass, gbc); gbc.gridy++;

        JButton btnSave = new JButton("Save Changes");
        btnSave.setBackground(ACCENT_TEAL);
        btnSave.setForeground(Color.BLACK);
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSave.setFocusPainted(false);
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSave.setPreferredSize(new Dimension(140, 40));

        JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 15));
        btnWrapper.setBackground(PANEL_BG);
        btnWrapper.add(btnSave);

        formCard.add(btnWrapper, gbc);
        centerWrapper.add(formCard);
        add(centerWrapper, BorderLayout.CENTER);
    }

    private JLabel createFormLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(TEXT_MUTED);
        return lbl;
    }

    private JTextField createTextField(String val) {
        JTextField tf = new JTextField(val);
        tf.setPreferredSize(new Dimension(400, 35));
        tf.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER_COLOR), new EmptyBorder(5, 10, 5, 10)));
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return tf;
    }

    private void stylePasswordField(JPasswordField pf) {
        pf.setPreferredSize(new Dimension(400, 35));
        pf.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER_COLOR), new EmptyBorder(5, 10, 5, 10)));
        pf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    }
}