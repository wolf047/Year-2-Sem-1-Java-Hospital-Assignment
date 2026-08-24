package com.mycompany.guitesting;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class IssueLabRequestPanel extends JPanel {

    private final Color MAIN_BG = new Color(248, 250, 252);
    private final Color PANEL_BG = Color.WHITE;
    private final Color TEXT_BLACK = new Color(15, 23, 42);
    private final Color TEXT_MUTED = new Color(100, 116, 139);
    private final Color ACCENT_BLUE = new Color(14, 165, 233);
    private final Color BORDER_COLOR = new Color(226, 232, 240);

    // Form Components
    private JComboBox<String> cbPatient;
    private JComboBox<String> cbRequestType;
    private JComboBox<String> cbUrgency;
    private JTextArea txtClinicalNotes;
    private DefaultTableModel tableModel;
    private JTable requestTable;

    public IssueLabRequestPanel() {
        setLayout(new BorderLayout(30, 0));
        setBackground(MAIN_BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(MAIN_BG);
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        JLabel title = new JLabel("Lab & Imaging Requests");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(TEXT_BLACK);
        
        JLabel subtitle = new JLabel("Submit requisitions for specialized lab tests, X-rays, or imaging to hospital administration.");
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
            new EmptyBorder(20, 20, 20, 20)
        ));
        formPanel.setPreferredSize(new Dimension(380, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 12, 0);
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;

        cbPatient = new JComboBox<>(new String[]{"Sarah Jenkins (P001)", "Michael Chang (P002)", "Emma Watson (P003)"});
        styleComboBox(cbPatient);

        // Types based on the user requirement (Lab tests, X-rays, specialized imaging)
        cbRequestType = new JComboBox<>(new String[]{
            "Full Blood Count (FBC) Lab", 
            "Lipid & Metabolic Panel Lab", 
            "Chest X-Ray", 
            "MRI Brain Scan (Imaging)", 
            "CT Scan Abdomen"
        });
        styleComboBox(cbRequestType);

        cbUrgency = new JComboBox<>(new String[]{"Routine", "Urgent / Stat", "Emergency"});
        styleComboBox(cbUrgency);

        txtClinicalNotes = new JTextArea(4, 20);
        txtClinicalNotes.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtClinicalNotes.setLineWrap(true);
        txtClinicalNotes.setWrapStyleWord(true);
        txtClinicalNotes.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        JScrollPane scrollNotes = new JScrollPane(txtClinicalNotes);

        // Add components to form
        formPanel.add(createFormLabel("Select Patient:"), gbc); gbc.gridy++;
        formPanel.add(cbPatient, gbc); gbc.gridy++;
        formPanel.add(createFormLabel("Requisition Type:"), gbc); gbc.gridy++;
        formPanel.add(cbRequestType, gbc); gbc.gridy++;
        formPanel.add(createFormLabel("Urgency Level:"), gbc); gbc.gridy++;
        formPanel.add(cbUrgency, gbc); gbc.gridy++;
        formPanel.add(createFormLabel("Clinical Justification / Notes:"), gbc); gbc.gridy++;
        formPanel.add(scrollNotes, gbc); gbc.gridy++;

        // Buttons (All black fonts)
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.setBackground(PANEL_BG);
        buttonPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        JButton btnSubmit = styleButton(new JButton("Submit Request"), ACCENT_BLUE, Color.BLACK);
        JButton btnClear = styleButton(new JButton("Clear"), new Color(226, 232, 240), Color.BLACK);

        buttonPanel.add(btnClear);
        buttonPanel.add(btnSubmit);
        
        gbc.insets = new Insets(10, 0, 0, 0);
        formPanel.add(buttonPanel, gbc);

        add(formPanel, BorderLayout.WEST);

        // ==========================================
        // RIGHT SIDE: DATA TABLE (Requests Log)
        // ==========================================
        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setBackground(PANEL_BG);
        tableContainer.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));

        JLabel tableTitle = new JLabel("Submitted Requisitions Tracker");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tableTitle.setBorder(new EmptyBorder(15, 15, 15, 15));
        tableContainer.add(tableTitle, BorderLayout.NORTH);

        String[] columns = {"Req ID", "Patient Name", "Test / Imaging Type", "Urgency", "Status"};
        tableModel = new DefaultTableModel(columns, 0);
        requestTable = new JTable(tableModel);
        
        requestTable.setRowHeight(35);
        requestTable.setShowGrid(false);
        requestTable.setShowHorizontalLines(true);
        requestTable.setGridColor(BORDER_COLOR);
        requestTable.setSelectionBackground(new Color(224, 242, 254));
        requestTable.setSelectionForeground(TEXT_BLACK);
        requestTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JTableHeader tableHeader = requestTable.getTableHeader();
        tableHeader.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tableHeader.setBackground(MAIN_BG);
        tableHeader.setForeground(TEXT_MUTED);
        tableHeader.setPreferredSize(new Dimension(0, 40));
        tableHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));

        // Dummy Data
        tableModel.addRow(new Object[]{"REQ-301", "Sarah Jenkins", "Chest X-Ray", "Routine", "Pending Asset Assignment"});
        tableModel.addRow(new Object[]{"REQ-302", "Michael Chang", "Full Blood Count (FBC) Lab", "Urgent / Stat", "In Progress"});

        JScrollPane scrollPane = new JScrollPane(requestTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(MAIN_BG);
        
        tableContainer.add(scrollPane, BorderLayout.CENTER);
        add(tableContainer, BorderLayout.CENTER);
    }

    // --- Helper Methods ---
    private JLabel createFormLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(TEXT_BLACK);
        return lbl;
    }
    
    private void styleComboBox(JComboBox<String> cb) {
        cb.setPreferredSize(new Dimension(200, 30));
        cb.setBackground(Color.WHITE);
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
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