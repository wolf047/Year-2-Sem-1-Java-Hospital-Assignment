package com.mycompany.guitesting;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class LogVitalsPanel extends JPanel {

    private final Color MAIN_BG = new Color(248, 250, 252);
    private final Color PANEL_BG = Color.WHITE;
    private final Color TEXT_BLACK = new Color(15, 23, 42);
    private final Color TEXT_MUTED = new Color(100, 116, 139);
    private final Color ACCENT_BLUE = new Color(14, 165, 233);
    private final Color BORDER_COLOR = new Color(226, 232, 240);

    // Form Components
    private JComboBox<String> cbPatient;
    private JTextField txtTemp;
    private JTextField txtBP;
    private JTextField txtPulse;
    private JTextArea txtNotes;
    private DefaultTableModel tableModel;
    private JTable vitalsTable;

    public LogVitalsPanel() {
        setLayout(new BorderLayout(30, 0));
        setBackground(MAIN_BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(MAIN_BG);
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        JLabel title = new JLabel("Log Patient Vitals & Consultation Notes");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(TEXT_BLACK);
        
        JLabel subtitle = new JLabel("Record vital signs and write clinical observations for active patients.");
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

        txtTemp = createTextField();
        txtTemp.setToolTipText("e.g., 37.2 °C");

        txtBP = createTextField();
        txtBP.setToolTipText("e.g., 120/80 mmHg");

        txtPulse = createTextField();
        txtPulse.setToolTipText("e.g., 75 bpm");

        txtNotes = new JTextArea(4, 20);
        txtNotes.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtNotes.setLineWrap(true);
        txtNotes.setWrapStyleWord(true);
        txtNotes.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        JScrollPane scrollNotes = new JScrollPane(txtNotes);

        // Add components to form
        formPanel.add(createFormLabel("Select Patient:"), gbc); gbc.gridy++;
        formPanel.add(cbPatient, gbc); gbc.gridy++;
        formPanel.add(createFormLabel("Temperature (°C):"), gbc); gbc.gridy++;
        formPanel.add(txtTemp, gbc); gbc.gridy++;
        formPanel.add(createFormLabel("Blood Pressure (mmHg):"), gbc); gbc.gridy++;
        formPanel.add(txtBP, gbc); gbc.gridy++;
        formPanel.add(createFormLabel("Pulse Rate (bpm):"), gbc); gbc.gridy++;
        formPanel.add(txtPulse, gbc); gbc.gridy++;
        formPanel.add(createFormLabel("Consultation Notes:"), gbc); gbc.gridy++;
        formPanel.add(scrollNotes, gbc); gbc.gridy++;

        // Buttons (All black fonts)
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.setBackground(PANEL_BG);
        buttonPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        JButton btnSave = styleButton(new JButton("Save Record"), ACCENT_BLUE, Color.BLACK);
        JButton btnClear = styleButton(new JButton("Clear"), new Color(226, 232, 240), Color.BLACK);

        buttonPanel.add(btnClear);
        buttonPanel.add(btnSave);
        
        gbc.insets = new Insets(10, 0, 0, 0);
        formPanel.add(buttonPanel, gbc);

        add(formPanel, BorderLayout.WEST);

        // ==========================================
        // RIGHT SIDE: DATA TABLE (History)
        // ==========================================
        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setBackground(PANEL_BG);
        tableContainer.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));

        JLabel tableTitle = new JLabel("Recent Patient Records");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tableTitle.setBorder(new EmptyBorder(15, 15, 15, 15));
        tableContainer.add(tableTitle, BorderLayout.NORTH);

        String[] columns = {"Record ID", "Patient Name", "Temp", "BP", "Pulse", "Notes"};
        tableModel = new DefaultTableModel(columns, 0);
        vitalsTable = new JTable(tableModel);
        
        vitalsTable.setRowHeight(35);
        vitalsTable.setShowGrid(false);
        vitalsTable.setShowHorizontalLines(true);
        vitalsTable.setGridColor(BORDER_COLOR);
        vitalsTable.setSelectionBackground(new Color(224, 242, 254));
        vitalsTable.setSelectionForeground(TEXT_BLACK);
        vitalsTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JTableHeader tableHeader = vitalsTable.getTableHeader();
        tableHeader.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tableHeader.setBackground(MAIN_BG);
        tableHeader.setForeground(TEXT_MUTED);
        tableHeader.setPreferredSize(new Dimension(0, 40));
        tableHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));

        // Dummy Data
        tableModel.addRow(new Object[]{"V-501", "Sarah Jenkins", "36.8 °C", "120/80", "72", "Patient reports mild fatigue. Advised rest."});
        tableModel.addRow(new Object[]{"V-502", "Michael Chang", "37.5 °C", "130/85", "80", "Slight fever observed. Prescribed paracetamol."});

        JScrollPane scrollPane = new JScrollPane(vitalsTable);
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
        tf.setPreferredSize(new Dimension(200, 32));
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR), new EmptyBorder(5, 10, 5, 10)
        ));
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return tf;
    }
    
    private void styleComboBox(JComboBox<String> cb) {
        cb.setPreferredSize(new Dimension(200, 32));
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