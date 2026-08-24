package com.mycompany.guitesting;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class ManageUsersPanel extends JPanel {

    private final Color MAIN_BG = new Color(248, 250, 252);
    private final Color PANEL_BG = Color.WHITE;
    private final Color TEXT_BLACK = new Color(15, 23, 42);
    private final Color TEXT_MUTED = new Color(100, 116, 139);
    private final Color ACCENT_BLUE = new Color(14, 165, 233);
    private final Color BORDER_COLOR = new Color(226, 232, 240);

    // Form Components
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JComboBox<String> cbRole;
    private JComboBox<String> cbManager;
    private JLabel lblManager;
    private DefaultTableModel tableModel;
    private JTable userTable;

    public ManageUsersPanel() {
        setLayout(new BorderLayout(30, 0));
        setBackground(MAIN_BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(MAIN_BG);
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        JLabel title = new JLabel("Manage System Users");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(TEXT_BLACK);
        headerPanel.add(title, BorderLayout.WEST);
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

        txtUsername = createTextField();
        txtPassword = new JPasswordField();
        txtPassword.setPreferredSize(new Dimension(200, 35));
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR), new EmptyBorder(5, 10, 5, 10)
        ));

        cbRole = new JComboBox<>(new String[]{"Admin", "Manager", "Doctor", "Patient"});
        cbRole.setPreferredSize(new Dimension(200, 35));
        cbRole.setBackground(Color.WHITE);

        // Dynamic Manager Dropdown (Only for Doctors)
        cbManager = new JComboBox<>(new String[]{"None", "Dr. Alan (Cardiology)", "Dr. Sarah (ER)"});
        cbManager.setPreferredSize(new Dimension(200, 35));
        cbManager.setBackground(Color.WHITE);
        cbManager.setEnabled(false); // Disabled by default
        lblManager = new JLabel("Assign Manager (Doctors Only):");
        lblManager.setForeground(TEXT_MUTED);

        // Event Listener to enable Manager dropdown if Doctor is selected
        cbRole.addActionListener(e -> {
            boolean isDoctor = cbRole.getSelectedItem().equals("Doctor");
            cbManager.setEnabled(isDoctor);
        });

        // Add components to form
        formPanel.add(createFormLabel("Username:"), gbc); gbc.gridy++;
        formPanel.add(txtUsername, gbc); gbc.gridy++;
        formPanel.add(createFormLabel("Password:"), gbc); gbc.gridy++;
        formPanel.add(txtPassword, gbc); gbc.gridy++;
        formPanel.add(createFormLabel("System Role:"), gbc); gbc.gridy++;
        formPanel.add(cbRole, gbc); gbc.gridy++;
        formPanel.add(lblManager, gbc); gbc.gridy++;
        formPanel.add(cbManager, gbc); gbc.gridy++;

        // Buttons (All forced to use Color.BLACK for font)
        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        buttonPanel.setBackground(PANEL_BG);
        buttonPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        JButton btnAdd = styleButton(new JButton("Add User"), ACCENT_BLUE, Color.BLACK);
        JButton btnUpdate = styleButton(new JButton("Update"), new Color(16, 185, 129), Color.BLACK);
        JButton btnDelete = styleButton(new JButton("Delete"), new Color(239, 68, 68), Color.BLACK);
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

        String[] columns = {"ID", "Username", "Role", "Assigned Manager"};
        tableModel = new DefaultTableModel(columns, 0);
        userTable = new JTable(tableModel);
        
        // Modern Table Styling
        userTable.setRowHeight(35);
        userTable.setShowGrid(false);
        userTable.setShowHorizontalLines(true);
        userTable.setGridColor(BORDER_COLOR);
        userTable.setSelectionBackground(new Color(224, 242, 254));
        userTable.setSelectionForeground(TEXT_BLACK);
        userTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JTableHeader tableHeader = userTable.getTableHeader();
        tableHeader.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tableHeader.setBackground(MAIN_BG);
        tableHeader.setForeground(TEXT_MUTED);
        tableHeader.setPreferredSize(new Dimension(0, 40));
        tableHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));

        // Adding some dummy data for preview
        tableModel.addRow(new Object[]{"U001", "admin1", "Admin", "N/A"});
        tableModel.addRow(new Object[]{"U002", "dr_smith", "Doctor", "Dr. Sarah (ER)"});
        tableModel.addRow(new Object[]{"U003", "manager_alan", "Manager", "N/A"});

        JScrollPane scrollPane = new JScrollPane(userTable);
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

    private JButton styleButton(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg); // Forces the font color to black (or whatever color is passed)
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(bg.darker()));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(0, 35));
        return btn;
    }
}