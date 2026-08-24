package com.mycompany.guitesting;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class PatientDashboard extends JPanel {

    private final Color MAIN_BG = new Color(248, 250, 252);
    private final Color HEADER_BG = Color.WHITE;
    private final Color TEXT_BLACK = new Color(15, 23, 42);
    private final Color TEXT_MUTED = new Color(100, 116, 139);
    private final Color ACCENT_TEAL = new Color(13, 148, 136);
    private final Color BORDER_COLOR = new Color(226, 232, 240);

    public PatientDashboard(HMSApplication app) {
        setLayout(new BorderLayout());
        setBackground(MAIN_BG);

        // Internal Card Layout for routing
        CardLayout internalCard = new CardLayout();
        JPanel mainContent = new JPanel(internalCard);

        // ==========================================
        // TOP HEADER BAR (Teal Accent Theme)
        // ==========================================
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setBackground(HEADER_BG);
        topHeader.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
            new EmptyBorder(15, 30, 15, 30)
        ));

        // Brand / Welcome
        JPanel titleGroup = new JPanel(new GridLayout(2, 1, 0, 2));
        titleGroup.setBackground(HEADER_BG);
        JLabel brandLabel = new JLabel("🏥 APU Medical Centre");
        brandLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        brandLabel.setForeground(TEXT_BLACK);
        
        JLabel subLabel = new JLabel("Patient Portal • Welcome, Sarah");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subLabel.setForeground(TEXT_MUTED);
        
        titleGroup.add(brandLabel);
        titleGroup.add(subLabel);
        topHeader.add(titleGroup, BorderLayout.WEST);

        // Right Navigation Links
        JPanel rightNav = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 5));
        rightNav.setBackground(HEADER_BG);
        
        JButton btnBookings = createMenuLink("Appointments");
        btnBookings.addActionListener(e -> internalCard.show(mainContent, "BOOKINGS"));

        JButton btnHistory = createMenuLink("Medical History");
        btnHistory.addActionListener(e -> internalCard.show(mainContent, "HISTORY"));

        JButton btnFeedback = createMenuLink("Give Feedback");
        btnFeedback.addActionListener(e -> internalCard.show(mainContent, "FEEDBACK"));

        JButton btnProfile = createMenuLink("👤 Profile");
        btnProfile.addActionListener(e -> internalCard.show(mainContent, "PROFILE"));

        JButton btnLogout = new JButton("Logout");
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnLogout.setForeground(new Color(220, 38, 38));
        btnLogout.setBackground(HEADER_BG);
        btnLogout.setBorder(BorderFactory.createLineBorder(new Color(220, 38, 38), 1));
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.setPreferredSize(new Dimension(80, 32));
        btnLogout.addActionListener(e -> app.navigateTo("LOGIN"));

        rightNav.add(btnBookings);
        rightNav.add(btnHistory);
        rightNav.add(btnFeedback);
        
        JLabel separator = new JLabel("|");
        separator.setForeground(BORDER_COLOR);
        rightNav.add(separator);

        rightNav.add(btnProfile);
        rightNav.add(btnLogout);
        
        topHeader.add(rightNav, BorderLayout.EAST);
        add(topHeader, BorderLayout.NORTH);

        // ==========================================
        // REGISTER ALL PATIENT SCREENS
        // ==========================================
        mainContent.add(new PatientBookingPanel(), "BOOKINGS");
        mainContent.add(new PatientMedicalHistoryPanel(), "HISTORY");
        mainContent.add(new PatientFeedbackPanel(), "FEEDBACK");
        mainContent.add(new PatientProfilePanel(), "PROFILE");

        add(mainContent, BorderLayout.CENTER);
    }

    private JButton createMenuLink(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(TEXT_BLACK);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setForeground(ACCENT_TEAL); }
            public void mouseExited(MouseEvent e) { btn.setForeground(TEXT_BLACK); }
        });
        return btn;
    }
}