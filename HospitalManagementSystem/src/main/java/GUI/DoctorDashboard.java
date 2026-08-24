package com.mycompany.guitesting;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DoctorDashboard extends JPanel {

    private final Color MAIN_BG = new Color(248, 250, 252);
    private final Color PANEL_BG = Color.WHITE;
    private final Color TEXT_BLACK = new Color(15, 23, 42);
    private final Color TEXT_MUTED = new Color(100, 116, 139);
    private final Color ACCENT_BLUE = new Color(14, 165, 233);
    private final Color BORDER_COLOR = new Color(226, 232, 240);
    private final Color HOVER_BG = new Color(241, 245, 249);

    public DoctorDashboard(HMSApplication app) {
        setLayout(new BorderLayout());
        setBackground(MAIN_BG);

        // ==========================================
        // 1. SETUP INTERNAL CARD LAYOUT
        // ==========================================
        CardLayout internalCard = new CardLayout();
        JPanel mainContent = new JPanel(internalCard);

        // ==========================================
        // 2. CLINICAL TOP HEADER BAR (2-Part Layout)
        // ==========================================
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setBackground(PANEL_BG);
        topHeader.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
            new EmptyBorder(15, 30, 15, 30)
        ));

        // Left: Status & Name
        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftHeader.setBackground(PANEL_BG);
        JLabel statusDot = new JLabel("🟢");
        statusDot.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        JLabel brandLabel = new JLabel("Dr. Smith's Workspace");
        brandLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        brandLabel.setForeground(TEXT_BLACK);
        leftHeader.add(statusDot);
        leftHeader.add(brandLabel);
        topHeader.add(leftHeader, BorderLayout.WEST);

        // Right: Navigation Links & Logout
        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 5));
        rightHeader.setBackground(PANEL_BG);

        JButton btnWorkspace = createMenuLink("Workspace");
        btnWorkspace.addActionListener(e -> internalCard.show(mainContent, "WORKSPACE"));

        JButton btnVitals = createMenuLink("Log Vitals");
        btnVitals.addActionListener(e -> internalCard.show(mainContent, "VITALS"));
        
        JButton btnPrescriptions = createMenuLink("Prescriptions"); // ADD THIS LINE
        btnPrescriptions.addActionListener(e -> internalCard.show(mainContent, "PRESCRIPTIONS"));

        JButton btnProfile = createMenuLink("👤 Profile");
        btnProfile.addActionListener(e -> internalCard.show(mainContent, "PROFILE"));

        JButton btnLogout = new JButton("Logout");
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnLogout.setForeground(new Color(220, 38, 38));
        btnLogout.setBackground(PANEL_BG);
        btnLogout.setBorder(BorderFactory.createLineBorder(new Color(220, 38, 38), 1));
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.setPreferredSize(new Dimension(80, 32));
        btnLogout.addActionListener(e -> app.navigateTo("LOGIN"));

        rightHeader.add(btnWorkspace);
        rightHeader.add(btnVitals);
        rightHeader.add(btnPrescriptions);
        
        JLabel separator = new JLabel("|");
        separator.setForeground(BORDER_COLOR);
        separator.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        rightHeader.add(separator);

        rightHeader.add(btnProfile);
        rightHeader.add(btnLogout);
        topHeader.add(rightHeader, BorderLayout.EAST);

        add(topHeader, BorderLayout.NORTH);

        // ==========================================
        // 3. BUILD WORKSPACE PANEL (Original Split Screen)
        // ==========================================
        JPanel workspacePanel = new JPanel(new BorderLayout(30, 0));
        workspacePanel.setBackground(MAIN_BG);
        workspacePanel.setBorder(new EmptyBorder(30, 40, 30, 40));

        // LEFT: Patient Queue
        JPanel queuePanel = new JPanel(new BorderLayout());
        queuePanel.setBackground(MAIN_BG);
        queuePanel.setPreferredSize(new Dimension(320, 0));

        JLabel queueTitle = new JLabel("Today's Schedule");
        queueTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        queueTitle.setForeground(TEXT_BLACK);
        queueTitle.setBorder(new EmptyBorder(0, 0, 15, 0));
        queuePanel.add(queueTitle, BorderLayout.NORTH);

        JPanel patientList = new JPanel(new GridLayout(5, 1, 0, 15));
        patientList.setBackground(MAIN_BG);
        patientList.add(new PatientQueueItem("09:00 AM", "Sarah Jenkins", "Routine Checkup", true));
        patientList.add(new PatientQueueItem("09:45 AM", "Michael Chang", "Lab Results Review", false));
        patientList.add(new PatientQueueItem("10:30 AM", "Emma Watson", "Fever & Cough", false));
        
        JPanel queueWrapper = new JPanel(new BorderLayout());
        queueWrapper.setBackground(MAIN_BG);
        queueWrapper.add(patientList, BorderLayout.NORTH);
        
        JScrollPane scrollQueue = new JScrollPane(queueWrapper);
        scrollQueue.setBorder(null);
        scrollQueue.setBackground(MAIN_BG);
        queuePanel.add(scrollQueue, BorderLayout.CENTER);
        workspacePanel.add(queuePanel, BorderLayout.WEST);

        // RIGHT: Action Hub
        JPanel actionPanel = new JPanel(new BorderLayout());
        actionPanel.setBackground(MAIN_BG);

        JLabel actionTitle = new JLabel("Clinical Actions");
        actionTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        actionTitle.setForeground(TEXT_BLACK);
        actionTitle.setBorder(new EmptyBorder(0, 0, 15, 0));
        actionPanel.add(actionTitle, BorderLayout.NORTH);

        JPanel toolGrid = new JPanel(new GridLayout(2, 2, 20, 20));
        toolGrid.setBackground(MAIN_BG);
        
        // Pass routing info so clicking the tile opens Log Vitals
        toolGrid.add(new ActionTile("🩺 Log Vitals & Notes", "Record vital signs and write consultation notes.", internalCard, mainContent, "VITALS", ACCENT_BLUE));
        toolGrid.add(new ActionTile("💊 Issue Prescription", "Issue digital medication prescriptions.", internalCard, mainContent, "PRESCRIPTIONS", new Color(16, 185, 129)));
        toolGrid.add(new ActionTile("🧪 Lab & X-Ray Requests", "Request lab tests or imaging.", internalCard, mainContent, "LAB_REQUESTS", new Color(139, 92, 246)));
        toolGrid.add(new ActionTile("📁 Patient History", "Review past medical assessments.", null, null, "", new Color(245, 158, 11)));

        actionPanel.add(toolGrid, BorderLayout.CENTER);
        workspacePanel.add(actionPanel, BorderLayout.CENTER);

        // ==========================================
        // 4. ADD SCREENS TO MAIN CONTENT
        // ==========================================
        mainContent.add(workspacePanel, "WORKSPACE");
        mainContent.add(new LogVitalsPanel(), "VITALS");
        mainContent.add(new IssuePrescriptionPanel(), "PRESCRIPTIONS");
        mainContent.add(new IssueLabRequestPanel(), "LAB_REQUESTS");
        mainContent.add(new EditProfilePanel("Doctor"), "PROFILE");

        add(mainContent, BorderLayout.CENTER);
    }

    // --- Helper Components ---
    private JButton createMenuLink(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(TEXT_BLACK);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setForeground(ACCENT_BLUE); }
            public void mouseExited(MouseEvent e) { btn.setForeground(TEXT_BLACK); }
        });
        return btn;
    }

    class PatientQueueItem extends JPanel {
        public PatientQueueItem(String time, String name, String reason, boolean isActive) {
            setLayout(new BorderLayout());
            setBackground(PANEL_BG);
            if (isActive) {
                setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(ACCENT_BLUE, 2), new EmptyBorder(15, 15, 15, 15)));
            } else {
                setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1), new EmptyBorder(15, 15, 15, 15)));
            }
            JLabel lblTime = new JLabel(time);
            lblTime.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblTime.setForeground(ACCENT_BLUE);
            JLabel lblName = new JLabel(name);
            lblName.setFont(new Font("Segoe UI", Font.BOLD, 16));
            lblName.setForeground(TEXT_BLACK);
            JLabel lblReason = new JLabel(reason);
            lblReason.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            lblReason.setForeground(TEXT_MUTED);
            
            JPanel centerInfo = new JPanel(new GridLayout(2, 1));
            centerInfo.setBackground(PANEL_BG);
            centerInfo.add(lblName);
            centerInfo.add(lblReason);
            
            add(lblTime, BorderLayout.NORTH);
            add(centerInfo, BorderLayout.CENTER);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { setBackground(HOVER_BG); centerInfo.setBackground(HOVER_BG); }
                public void mouseExited(MouseEvent e) { setBackground(PANEL_BG); centerInfo.setBackground(PANEL_BG); }
            });
        }
    }

    class ActionTile extends JPanel {
        public ActionTile(String title, String desc, CardLayout layout, JPanel parent, String targetScreen, Color accentColor) {
            setLayout(new BorderLayout());
            setBackground(PANEL_BG);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(4, 0, 0, 0, accentColor), BorderFactory.createLineBorder(BORDER_COLOR, 1)),
                new EmptyBorder(25, 25, 25, 25)
            ));

            JLabel lblTitle = new JLabel(title);
            lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
            lblTitle.setForeground(TEXT_BLACK);
            lblTitle.setBorder(new EmptyBorder(0, 0, 10, 0));

            JLabel lblDesc = new JLabel("<html><p style='width:200px; line-height:1.4;'>" + desc + "</p></html>");
            lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            lblDesc.setForeground(TEXT_MUTED);

            add(lblTitle, BorderLayout.NORTH);
            add(lblDesc, BorderLayout.CENTER);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { setBackground(HOVER_BG); }
                public void mouseExited(MouseEvent e) { setBackground(PANEL_BG); }
                public void mouseClicked(MouseEvent e) {
                    if (layout != null && parent != null && targetScreen != null && !targetScreen.isEmpty()) {
                        layout.show(parent, targetScreen);
                    }
                }
            });
        }
    }
}