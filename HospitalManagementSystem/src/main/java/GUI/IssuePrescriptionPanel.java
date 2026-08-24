package com.mycompany.guitesting;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class IssuePrescriptionPanel extends JPanel {

    private final Color MAIN_BG = new Color(248, 250, 252);
    private final Color PANEL_BG = Color.WHITE;
    private final Color TEXT_BLACK = new Color(15, 23, 42);
    private final Color TEXT_MUTED = new Color(100, 116, 139);
    private final Color ACCENT_BLUE = new Color(14, 165, 233);
    private final Color BORDER_COLOR = new Color(226, 232, 240);

    // Form Components
    private JComboBox<String> cbPatient;
    private JTextField txtMedication;
    private JTextField txtDosage;
    private JComboBox<String> cbFrequency;
    private JTextField txtDuration;
    private JTextArea txtInstructions;
    private DefaultTableModel tableModel;
    private JTable prescriptionTable;

    public IssuePrescriptionPanel() {
        setLayout(new BorderLayout(30, 0));
        setBackground(MAIN_BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(MAIN_BG);
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        JLabel title = new JLabel("Issue Digital Prescriptions");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(TEXT_BLACK);
        
        JLabel subtitle = new JLabel("Create and assign secure electronic medication records directly to patients.");
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
        gbc.insets = new Insets(0, 0, 10, 0);
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;

        cbPatient = new JComboBox<>(new String[]{"Sarah Jenkins (P001)", "Michael Chang (P002)", "Emma Watson (P003)"});
        styleComboBox(cbPatient);

        txtMedication = createTextField();
        txtMedication.setToolTipText("e.g., Amoxicillin 500mg");

        txtDosage = createTextField();
        txtDosage.setToolTipText("e.g., 1 Capsule");

        cbFrequency = new JComboBox<>(new String[]{"Once Daily (OD)", "Twice Daily (BD)", "Three Times Daily (TDS)", "Four Times Daily (QDS)", "As Needed (PRN)"});
        styleComboBox(cbFrequency);

        txtDuration = createTextField();
        txtDuration.setToolTipText("e.g., 7 Days");

        txtInstructions = new JTextArea(3, 20);
        txtInstructions.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtInstructions.setLineWrap(true);
        txtInstructions.setWrapStyleWord(true);
        txtInstructions.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        JScrollPane scrollInst = new JScrollPane(txtInstructions);

        // Add components to form
        formPanel.add(createFormLabel("Select Patient:"), gbc); gbc.gridy++;
        formPanel.add(cbPatient, gbc); gbc.gridy++;
        formPanel.add(createFormLabel("Medication Name:"), gbc); gbc.gridy++;
        formPanel.add(txtMedication, gbc); gbc.gridy++;
        formPanel.add(createFormLabel("Dosage:"), gbc); gbc.gridy++;
        formPanel.add(txtDosage, gbc); gbc.gridy++;
        formPanel.add(createFormLabel("Frequency:"), gbc); gbc.gridy++;
        formPanel.add(cbFrequency, gbc); gbc.gridy++;
        formPanel.add(createFormLabel("Duration:"), gbc); gbc.gridy++;
        formPanel.add(txtDuration, gbc); gbc.gridy++;
        formPanel.add(createFormLabel("Special Instructions:"), gbc); gbc.gridy++;
        formPanel.add(scrollInst, gbc); gbc.gridy++;

        // Buttons (All black fonts)
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.setBackground(PANEL_BG);
        buttonPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        JButton btnIssue = styleButton(new JButton("Issue Prescription"), ACCENT_BLUE, Color.BLACK);
        JButton btnClear = styleButton(new JButton("Clear"), new Color(226, 232, 240), Color.BLACK);

        buttonPanel.add(btnClear);
        buttonPanel.add(btnIssue);
        
        gbc.insets = new Insets(10, 0, 0, 0);
        formPanel.add(buttonPanel, gbc);

        add(formPanel, BorderLayout.WEST);

        // ==========================================
        // RIGHT SIDE: DATA TABLE (History)
        // ==========================================
        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setBackground(PANEL_BG);
        tableContainer.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));

        JLabel tableTitle = new JLabel("Issued Prescriptions Log");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tableTitle.setBorder(new EmptyBorder(15, 15, 15, 15));
        tableContainer.add(tableTitle, BorderLayout.NORTH);

        String[] columns = {"Rx ID", "Patient Name", "Medication", "Dosage", "Frequency", "Duration"};
        tableModel = new DefaultTableModel(columns, 0);
        prescriptionTable = new JTable(tableModel);
        
        prescriptionTable.setRowHeight(35);
        prescriptionTable.setShowGrid(false);
        prescriptionTable.setShowHorizontalLines(true);
        prescriptionTable.setGridColor(BORDER_COLOR);
        prescriptionTable.setSelectionBackground(new Color(224, 242, 254));
        prescriptionTable.setSelectionForeground(TEXT_BLACK);
        prescriptionTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JTableHeader tableHeader = prescriptionTable.getTableHeader();
        tableHeader.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tableHeader.setBackground(MAIN_BG);
        tableHeader.setForeground(TEXT_MUTED);
        tableHeader.setPreferredSize(new Dimension(0, 40));
        tableHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));

        // Dummy Data
        tableModel.addRow(new Object[]{"RX-901", "Sarah Jenkins", "Paracetamol 500mg", "1 Tablet", "TDS", "5 Days"});
        tableModel.addRow(new Object[]{"RX-902", "Michael Chang", "Amoxicillin 250mg", "1 Capsule", "BD", "7 Days"});

        JScrollPane scrollPane = new JScrollPane(prescriptionTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(PANEL_BG);
        
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

    private JTextField createTextField() {
        JTextField tf = new JTextField();
        tf.setPreferredSize(new Dimension(200, 30));
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR), new EmptyBorder(5, 10, 5, 10)
        ));
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return tf;
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