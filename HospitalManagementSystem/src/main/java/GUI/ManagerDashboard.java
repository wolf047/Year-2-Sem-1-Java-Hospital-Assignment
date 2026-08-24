package com.mycompany.guitesting;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ManagerDashboard extends JPanel {

    private final Color MAIN_BG = new Color(248, 250, 252);
    private final Color HEADER_BG = Color.WHITE;
    private final Color TEXT_BLACK = new Color(15, 23, 42);
    private final Color TEXT_MUTED = new Color(100, 116, 139);
    private final Color ACCENT_BLUE = new Color(14, 165, 233);
    private final Color BORDER_COLOR = new Color(226, 232, 240);
    private final Color HOVER_BG = new Color(241, 245, 249);

    public ManagerDashboard(HMSApplication app) {
        setLayout(new BorderLayout());
        setBackground(MAIN_BG);

        // ==========================================
        // 1. SETUP INTERNAL CARD LAYOUT
        // ==========================================
        CardLayout internalCard = new CardLayout();
        JPanel mainContent = new JPanel(internalCard);

        // ==========================================
        // 2. EXECUTIVE TOP HEADER BAR (2-PART LAYOUT)
        // ==========================================
        JPanel topHeader = new JPanel(new BorderLayout()); 
        topHeader.setBackground(HEADER_BG);
        topHeader.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
            new EmptyBorder(15, 30, 15, 30)
        ));

        // LEFT (WEST): Title & Role info
        JPanel titleGroup = new JPanel(new GridLayout(2, 1, 0, 2));
        titleGroup.setBackground(HEADER_BG);
        JLabel brandLabel = new JLabel("🏥 APU Medical Centre");
        brandLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        brandLabel.setForeground(TEXT_BLACK);
        
        JLabel subLabel = new JLabel("Manager Portal");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subLabel.setForeground(TEXT_MUTED);
        
        titleGroup.add(brandLabel);
        titleGroup.add(subLabel);
        topHeader.add(titleGroup, BorderLayout.WEST);

        // RIGHT (EAST): All Navigation & Actions
        JPanel rightNav = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 5));
        rightNav.setBackground(HEADER_BG);
        
        // Create Nav Links
        JButton btnOverview = createMenuLink("Overview");
        btnOverview.addActionListener(e -> internalCard.show(mainContent, "OVERVIEW"));
        
        JButton btnDept = createMenuLink("Departments");
        btnDept.addActionListener(e -> internalCard.show(mainContent, "DEPARTMENTS"));

        JButton btnRosters = createMenuLink("Rosters");
        btnRosters.addActionListener(e -> internalCard.show(mainContent, "ROSTERS"));
        
        JButton btnReports = createMenuLink("Reports"); // ADD THIS LINE
        btnReports.addActionListener(e -> internalCard.show(mainContent, "REPORTS"));

        JButton btnProfile = createMenuLink("👤 Profile");
        btnProfile.addActionListener(e -> internalCard.show(mainContent, "PROFILE"));

        // Logout Button
        JButton btnLogout = new JButton("Logout");
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnLogout.setForeground(new Color(220, 38, 38));
        btnLogout.setBackground(HEADER_BG);
        btnLogout.setBorder(BorderFactory.createLineBorder(new Color(220, 38, 38), 1));
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.setPreferredSize(new Dimension(80, 32));
        btnLogout.addActionListener(e -> app.navigateTo("LOGIN"));

        // Add them all to the right side
        rightNav.add(btnOverview);
        rightNav.add(btnDept);
        rightNav.add(btnRosters);
        rightNav.add(btnReports);
        
        // Add a subtle visual divider line between nav and profile
        JLabel separator = new JLabel("|");
        separator.setForeground(BORDER_COLOR);
        separator.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        rightNav.add(separator);
        
        rightNav.add(btnProfile);
        rightNav.add(btnLogout);
        
        topHeader.add(rightNav, BorderLayout.EAST);
        add(topHeader, BorderLayout.NORTH);

        // ==========================================
        // 3. BUILD OVERVIEW PANEL
        // ==========================================
        JPanel overviewPanel = new JPanel(new BorderLayout(0, 30));
        overviewPanel.setBackground(MAIN_BG);
        overviewPanel.setBorder(new EmptyBorder(30, 40, 30, 40));

        // Top Row: KPI Metrics Ticker
        JPanel metricsPanel = new JPanel(new GridLayout(1, 3, 25, 0));
        metricsPanel.setBackground(MAIN_BG);
        metricsPanel.add(new MetricCard("MONTHLY REVENUE", "$124,500", "+12% from last month", ACCENT_BLUE));
        metricsPanel.add(new MetricCard("ACTIVE DEPARTMENTS", "8 Specialized", "Cardiology, ER, Pediatrics...", new Color(139, 92, 246)));
        metricsPanel.add(new MetricCard("TOTAL SHIFTS COVERED", "98%", "Optimal staffing levels", new Color(16, 185, 129)));

        // Bottom Row: Management Action Hub
        JPanel actionHub = new JPanel(new GridLayout(1, 3, 25, 0));
        actionHub.setBackground(MAIN_BG);
        
        actionHub.add(createActionCard("🏢 Departments", "Create or update specialized clinical departments.", internalCard, mainContent, "DEPARTMENTS"));
        actionHub.add(createActionCard("📋 Shift Rosters", "Design and modify operational shift rosters for doctors.", internalCard, mainContent, "ROSTERS")); 
        actionHub.add(createActionCard("📈 Reports", "View reports on hospital metrics and revenue summaries.", internalCard, mainContent, "REPORTS")); 

        JPanel dashboardStack = new JPanel(new GridLayout(2, 1, 0, 30));
        dashboardStack.setBackground(MAIN_BG);
        dashboardStack.add(metricsPanel);
        dashboardStack.add(actionHub);

        overviewPanel.add(dashboardStack, BorderLayout.CENTER);

        // ==========================================
        // 4. ADD SCREENS TO MAIN CONTENT
        // ==========================================
        mainContent.add(overviewPanel, "OVERVIEW");
        mainContent.add(new ManageDepartmentsPanel(), "DEPARTMENTS");
        mainContent.add(new ManageRostersPanel(), "ROSTERS"); 
        mainContent.add(new ViewReportsPanel(), "REPORTS");
        mainContent.add(new EditProfilePanel("Manager"), "PROFILE");

        add(mainContent, BorderLayout.CENTER);
    }

    // ==========================================
    // 5. UI HELPER COMPONENTS
    // ==========================================

    private JButton createMenuLink(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(TEXT_BLACK); 
        btn.setContentAreaFilled(false); 
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setForeground(ACCENT_BLUE); 
            }
            public void mouseExited(MouseEvent e) {
                btn.setForeground(TEXT_BLACK);
            }
        });
        return btn;
    }

    class MetricCard extends JPanel {
        public MetricCard(String title, String value, String subtext, Color accentColor) {
            setLayout(new BorderLayout());
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(4, 0, 0, 0, accentColor),
                    BorderFactory.createMatteBorder(0, 1, 1, 1, BORDER_COLOR)
                ),
                new EmptyBorder(20, 20, 20, 20)
            ));

            JLabel lblTitle = new JLabel(title);
            lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblTitle.setForeground(TEXT_MUTED);

            JLabel lblValue = new JLabel(value);
            lblValue.setFont(new Font("Segoe UI", Font.BOLD, 32));
            lblValue.setForeground(TEXT_BLACK);
            lblValue.setBorder(new EmptyBorder(8, 0, 8, 0));

            JLabel lblSub = new JLabel(subtext);
            lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblSub.setForeground(TEXT_MUTED);

            add(lblTitle, BorderLayout.NORTH);
            add(lblValue, BorderLayout.CENTER);
            add(lblSub, BorderLayout.SOUTH);
        }
    }

    private JPanel createActionCard(String title, String desc, CardLayout layout, JPanel parent, String targetScreen) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(25, 25, 25, 25)
        ));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(TEXT_BLACK);
        lblTitle.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel lblDesc = new JLabel("<html><p style='width:200px'>" + desc + "</p></html>");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblDesc.setForeground(TEXT_MUTED);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblDesc, BorderLayout.CENTER);

        card.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                card.setBackground(HOVER_BG);
            }
            public void mouseExited(MouseEvent e) {
                card.setBackground(Color.WHITE);
            }
            public void mouseClicked(MouseEvent e) {
                if (layout != null && parent != null && targetScreen != null && !targetScreen.isEmpty()) {
                    layout.show(parent, targetScreen);
                }
            }
        });

        return card;
    }
}