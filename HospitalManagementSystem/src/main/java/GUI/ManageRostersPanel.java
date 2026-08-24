package com.mycompany.guitesting;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class ManageRostersPanel extends JPanel {

    private final Color MAIN_BG = new Color(248, 250, 252);
    private final Color PANEL_BG = Color.WHITE;
    private final Color TEXT_BLACK = new Color(15, 23, 42);
    private final Color TEXT_MUTED = new Color(100, 116, 139);
    private final Color ACCENT_BLUE = new Color(14, 165, 233);
    private final Color BORDER_COLOR = new Color(226, 232, 240);

    // Form Components
    private JComboBox<String> cbDoctor;
    private JTextField txtDate;
    private JComboBox<String> cbShift;
    private JComboBox<String> cbDepartment;
    private DefaultTableModel tableModel;
    private JTable rosterTable;

    public ManageRostersPanel() {
        setLayout(new BorderLayout(30, 0));
        setBackground(MAIN_BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(MAIN_BG);
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        JLabel title = new JLabel("Doctor Shift Rosters");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(TEXT_BLACK);
        
        JLabel subtitle = new JLabel("Design and modify operational shift schedules for clinical staff.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(TEXT_MUTED);
        
        headerPanel.add(title, BorderLayout.NORTH);
        headerPanel.add(subtitle, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // ==========================================
        // LEFT SIDE: INPUT FORM
        // ==========================================
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(PANEL_BG);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(25, 25, 25, 25)
        ));
        formPanel.setPreferredSize(new Dimension(350, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 15, 0);
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;

        // Initialize fields
        cbDoctor = new JComboBox<>(new String[]{"Dr. Smith", "Dr. Sarah", "Dr. Alan", "Dr. Emily"});
        styleComboBox(cbDoctor);

        txtDate = createTextField();
        txtDate.setToolTipText("YYYY-MM-DD");
        txtDate.setText("2026-08-25"); // Default example date

        cbShift = new JComboBox<>(new String[]{
            "Morning (08:00 AM - 04:00 PM)", 
            "Evening (04:00 PM - 12:00 AM)", 
            "Night (12:00 AM - 08:00 AM)", 
            "Off-Duty"
        });
        styleComboBox(cbShift);

        cbDepartment = new JComboBox<>(new String[]{"General Practice", "Cardiology", "Emergency (ER)", "Pediatrics"});
        styleComboBox(cbDepartment);

        // Add components to form
        formPanel.add(createFormLabel("Select Doctor:"), gbc); gbc.gridy++;
        formPanel.add(cbDoctor, gbc); gbc.gridy++;
        formPanel.add(createFormLabel("Shift Date (YYYY-MM-DD):"), gbc); gbc.gridy++;
        formPanel.add(txtDate, gbc); gbc.gridy++;
        formPanel.add(createFormLabel("Shift Timing:"), gbc); gbc.gridy++;
        formPanel.add(cbShift, gbc); gbc.gridy++;
        formPanel.add(createFormLabel("Assigned Department/Ward:"), gbc); gbc.gridy++;
        formPanel.add(cbDepartment, gbc); gbc.gridy++;

        // Buttons (All black fonts)
        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        buttonPanel.setBackground(PANEL_BG);
        buttonPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        JButton btnAdd = styleButton(new JButton("Assign Shift"), ACCENT_BLUE, Color.BLACK);
        JButton btnUpdate = styleButton(new JButton("Update"), new Color(16, 185, 129), Color.BLACK);
        JButton btnDelete = styleButton(new JButton("Remove"), new Color(239, 68, 68), Color.BLACK);
        JButton btnClear = styleButton(new JButton("Clear"), new Color(226, 232, 240), Color.BLACK);

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClear);
        
        gbc.insets = new Insets(15, 0, 0, 0);
        formPanel.add(buttonPanel, gbc);

        add(formPanel, BorderLayout.WEST);

        // ==========================================
        // RIGHT SIDE: DATA TABLE
        // ==========================================
        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setBackground(PANEL_BG);
        tableContainer.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));

        String[] columns = {"Roster ID", "Doctor", "Date", "Shift", "Department"};
        tableModel = new DefaultTableModel(columns, 0);
        rosterTable = new JTable(tableModel);
        
        // Modern Table Styling
        rosterTable.setRowHeight(35);
        rosterTable.setShowGrid(false);
        rosterTable.setShowHorizontalLines(true);
        rosterTable.setGridColor(BORDER_COLOR);
        rosterTable.setSelectionBackground(new Color(224, 242, 254));
        rosterTable.setSelectionForeground(TEXT_BLACK);
        rosterTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JTableHeader tableHeader = rosterTable.getTableHeader();
        tableHeader.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tableHeader.setBackground(MAIN_BG);
        tableHeader.setForeground(TEXT_MUTED);
        tableHeader.setPreferredSize(new Dimension(0, 40));
        tableHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));

        // Adding some dummy data for preview
        tableModel.addRow(new Object[]{"R-1001", "Dr. Smith", "2026-08-25", "Morning", "General Practice"});
        tableModel.addRow(new Object[]{"R-1002", "Dr. Sarah", "2026-08-25", "Evening", "Emergency (ER)"});
        tableModel.addRow(new Object[]{"R-1003", "Dr. Alan", "2026-08-26", "Night", "Cardiology"});

        JScrollPane scrollPane = new JScrollPane(rosterTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(PANEL_BG);
        
        tableContainer.add(scrollPane, BorderLayout.CENTER);
        add(tableContainer, BorderLayout.CENTER);
    }

    // --- Helper Methods ---
    private JLabel createFormLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(TEXT_BLACK);
        return lbl;
    }

    private JTextField createTextField() {
        JTextField tf = new JTextField();
        tf.setPreferredSize(new Dimension(200, 35));
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR), new EmptyBorder(5, 10, 5, 10)
        ));
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return tf;
    }
    
    private void styleComboBox(JComboBox<String> cb) {
        cb.setPreferredSize(new Dimension(200, 35));
        cb.setBackground(Color.WHITE);
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    }

    private JButton styleButton(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(bg.darker()));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(0, 35));
        return btn;
    }
}