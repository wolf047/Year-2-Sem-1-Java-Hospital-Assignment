package com.mycompany.guitesting;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class AdminDashboard extends JPanel {

    private final Color MAIN_BG = new Color(248, 250, 252);
    private final Color SIDEBAR_BG = Color.WHITE;
    private final Color HOVER_BG = new Color(241, 245, 249);
    private final Color TEXT_BLACK = new Color(15, 23, 42);
    private final Color TEXT_MUTED = new Color(100, 116, 139);
    private final Color ACCENT_BLUE = new Color(14, 165, 233);
    private final Color BORDER_COLOR = new Color(226, 232, 240);

    public AdminDashboard(HMSApplication app) {
        setLayout(new BorderLayout());
        setBackground(MAIN_BG);

        // ==========================================
        // 1. SETUP INTERNAL CARD LAYOUT
        // ==========================================
        CardLayout internalCard = new CardLayout();
        JPanel mainContent = new JPanel(internalCard);

        // ==========================================
        // 2. SIDEBAR NAVIGATION
        // ==========================================
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(250, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR));

        JPanel profilePanel = new JPanel(new GridLayout(2, 1));
        profilePanel.setBackground(SIDEBAR_BG);
        profilePanel.setBorder(new EmptyBorder(40, 20, 30, 20));

        JLabel iconLabel = new JLabel("⚙️", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 50));
        JLabel nameLabel = new JLabel("Admin Portal", SwingConstants.CENTER);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        nameLabel.setForeground(TEXT_BLACK);

        profilePanel.add(iconLabel);
        profilePanel.add(nameLabel);
        sidebar.add(profilePanel, BorderLayout.NORTH);

        JPanel navPanel = new JPanel(new GridLayout(6, 1, 0, 8));
        navPanel.setBackground(SIDEBAR_BG);
        navPanel.setBorder(new EmptyBorder(10, 15, 0, 15));

        // Create Navigation Buttons
        JButton btnOverview = createNavButton("📊   Dashboard Overview");
        JButton btnManageUsers = createNavButton("👥   Manage Users");
        JButton btnManageAssets = createNavButton("🏥   Hospital Assets");
        JButton btnConfigRates = createNavButton("💲   Billing Rates");

        // Wire up the routing for Overview and Manage Users
        btnOverview.addActionListener(e -> internalCard.show(mainContent, "OVERVIEW"));
        btnManageUsers.addActionListener(e -> internalCard.show(mainContent, "MANAGE_USERS"));
        btnManageAssets.addActionListener(e -> internalCard.show(mainContent, "MANAGE_ASSETS"));
        btnConfigRates.addActionListener(e -> internalCard.show(mainContent, "CONFIG_RATES"));

        navPanel.add(btnOverview);
        navPanel.add(btnManageUsers);
        navPanel.add(btnManageAssets);
        navPanel.add(btnConfigRates);

        sidebar.add(navPanel, BorderLayout.CENTER);

        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(SIDEBAR_BG);
        footerPanel.setBorder(new EmptyBorder(20, 15, 30, 15));
        JButton btnLogout = createNavButton("🚪   Logout");
        btnLogout.setForeground(new Color(220, 38, 38));
        btnLogout.addActionListener(e -> app.navigateTo("LOGIN"));
        footerPanel.add(btnLogout, BorderLayout.SOUTH);
        sidebar.add(footerPanel, BorderLayout.SOUTH);
        
        

        // ==========================================
        // 3. BUILD OVERVIEW PANEL
        // ==========================================
        JPanel overviewPanel = new JPanel(new BorderLayout());
        overviewPanel.setBackground(MAIN_BG);
        overviewPanel.setBorder(new EmptyBorder(40, 50, 40, 50));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(MAIN_BG);
        headerPanel.setBorder(new EmptyBorder(0, 0, 40, 0));

        JLabel welcomeLabel = new JLabel("System Administration");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        welcomeLabel.setForeground(TEXT_BLACK);
        JLabel subtitleLabel = new JLabel("Manage hospital resources, user access, and configurations.");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtitleLabel.setForeground(TEXT_MUTED);

        headerPanel.add(welcomeLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        overviewPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 30, 0));
        cardsPanel.setBackground(MAIN_BG);
        cardsPanel.add(new CardPanel("TOTAL USERS", "142", "12 new this month", ACCENT_BLUE));
        cardsPanel.add(new CardPanel("ACTIVE WARDS", "24", "85% capacity", new Color(139, 92, 246))); 
        cardsPanel.add(new CardPanel("SYSTEM STATUS", "Online", "All services running", new Color(16, 185, 129))); 

        JPanel centerWrapper = new JPanel(new BorderLayout(0, 40));
        centerWrapper.setBackground(MAIN_BG);
        centerWrapper.add(cardsPanel, BorderLayout.NORTH);

        JPanel activityPanel = new JPanel(new BorderLayout());
        activityPanel.setBackground(Color.WHITE);
        activityPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(25, 25, 25, 25)
        ));
        JLabel activityTitle = new JLabel("Recent System Logs");
        activityTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        activityTitle.setForeground(TEXT_BLACK);
        activityPanel.add(activityTitle, BorderLayout.NORTH);

        centerWrapper.add(activityPanel, BorderLayout.CENTER);
        overviewPanel.add(centerWrapper, BorderLayout.CENTER);

        // ==========================================
        // 4. ADD PANELS TO MAIN CONTENT (CARD LAYOUT)
        // ==========================================
        mainContent.add(overviewPanel, "OVERVIEW");
        mainContent.add(new ManageUsersPanel(), "MANAGE_USERS");
        mainContent.add(new ManageAssetsPanel(), "MANAGE_ASSETS");
        mainContent.add(new ConfigureRatesPanel(), "CONFIG_RATES");

        // Assemble Dashboard
        add(sidebar, BorderLayout.WEST);
        add(mainContent, BorderLayout.CENTER);
    }

    private JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(TEXT_BLACK);
        btn.setBackground(SIDEBAR_BG);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(15, 20, 15, 20));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(HOVER_BG);
                btn.setForeground(ACCENT_BLUE);
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(SIDEBAR_BG);
                if(!text.contains("Logout")) btn.setForeground(TEXT_BLACK);
                else btn.setForeground(new Color(220, 38, 38));
            }
        });
        return btn;
    }

    class CardPanel extends JPanel {
        public CardPanel(String title, String mainValue, String subText, Color accentColor) {
            setLayout(new BorderLayout());
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(4, 0, 0, 0, accentColor),
                    BorderFactory.createMatteBorder(0, 1, 1, 1, BORDER_COLOR)
                ),
                new EmptyBorder(25, 25, 25, 25)
            ));

            JLabel lblTitle = new JLabel(title);
            lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblTitle.setForeground(TEXT_MUTED);
            JLabel lblValue = new JLabel(mainValue);
            lblValue.setFont(new Font("Segoe UI", Font.BOLD, 42));
            lblValue.setForeground(TEXT_BLACK);
            lblValue.setBorder(new EmptyBorder(10, 0, 10, 0));
            JLabel lblSub = new JLabel(subText);
            lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            lblSub.setForeground(TEXT_MUTED);

            add(lblTitle, BorderLayout.NORTH);
            add(lblValue, BorderLayout.CENTER);
            add(lblSub, BorderLayout.SOUTH);
        }
    }
}