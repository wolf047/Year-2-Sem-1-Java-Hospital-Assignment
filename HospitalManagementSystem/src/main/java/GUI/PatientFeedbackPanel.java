package com.mycompany.guitesting;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class PatientFeedbackPanel extends JPanel {

    private final Color MAIN_BG = new Color(248, 250, 252);
    private final Color PANEL_BG = Color.WHITE;
    private final Color TEXT_BLACK = new Color(15, 23, 42);
    private final Color TEXT_MUTED = new Color(100, 116, 139);
    private final Color ACCENT_TEAL = new Color(13, 148, 136);
    private final Color BORDER_COLOR = new Color(226, 232, 240);

    public PatientFeedbackPanel() {
        setLayout(new BorderLayout(0, 30));
        setBackground(MAIN_BG);
        setBorder(new EmptyBorder(30, 60, 30, 60));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(MAIN_BG);
        JLabel title = new JLabel("Submit Visit Feedback & Ratings");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(TEXT_BLACK);
        JLabel subtitle = new JLabel("Rate your consultation experience and share comments to help us improve care quality.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(TEXT_MUTED);
        headerPanel.add(title, BorderLayout.NORTH);
        headerPanel.add(subtitle, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // Centered Feedback Card
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
        formCard.setPreferredSize(new Dimension(500, 480));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 12, 0);
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;

        JComboBox<String> cbDoctor = new JComboBox<>(new String[]{"Dr. Smith (General Practice)", "Dr. Alan (Cardiology)", "Dr. Sarah (Emergency/GP)", "Dr. Emily (Pediatrics)"});
        styleComboBox(cbDoctor);

        JComboBox<String> cbRating = new JComboBox<>(new String[]{"⭐⭐⭐⭐⭐ (5 - Excellent)", "⭐⭐⭐⭐ (4 - Very Good)", "⭐⭐⭐ (3 - Good)", "⭐⭐ (2 - Fair)", "⭐ (1 - Poor)"});
        styleComboBox(cbRating);

        JTextArea txtComments = new JTextArea(4, 20);
        txtComments.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtComments.setLineWrap(true);
        txtComments.setWrapStyleWord(true);
        txtComments.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        JScrollPane scrollComments = new JScrollPane(txtComments);

        formCard.add(createFormLabel("Select Doctor / Visit:"), gbc); gbc.gridy++;
        formCard.add(cbDoctor, gbc); gbc.gridy++;
        formCard.add(createFormLabel("Satisfaction Rating:"), gbc); gbc.gridy++;
        formCard.add(cbRating, gbc); gbc.gridy++;
        formCard.add(createFormLabel("Comments & Review:"), gbc); gbc.gridy++;
        formCard.add(scrollComments, gbc); gbc.gridy++;

        JButton btnSubmit = new JButton("Submit Review");
        btnSubmit.setBackground(ACCENT_TEAL);
        btnSubmit.setForeground(Color.BLACK); // Black font applied
        btnSubmit.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSubmit.setFocusPainted(false);
        btnSubmit.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSubmit.setPreferredSize(new Dimension(140, 40));
        btnSubmit.addActionListener(e -> JOptionPane.showMessageDialog(this, "Thank you! Your feedback has been submitted successfully."));

        JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 15));
        btnWrapper.setBackground(PANEL_BG);
        btnWrapper.add(btnSubmit);

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

    private void styleComboBox(JComboBox<String> cb) {
        cb.setPreferredSize(new Dimension(400, 35));
        cb.setBackground(Color.WHITE);
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    }
}