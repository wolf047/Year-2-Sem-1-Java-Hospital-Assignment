package com.mycompany.guitesting;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class PatientMedicalHistoryPanel extends JPanel {

    private final Color MAIN_BG = new Color(248, 250, 252);
    private final Color PANEL_BG = Color.WHITE;
    private final Color TEXT_BLACK = new Color(15, 23, 42);
    private final Color TEXT_MUTED = new Color(100, 116, 139);
    private final Color ACCENT_TEAL = new Color(13, 148, 136);
    private final Color BORDER_COLOR = new Color(226, 232, 240);

    public PatientMedicalHistoryPanel() {
        setLayout(new BorderLayout(0, 25));
        setBackground(MAIN_BG);
        setBorder(new EmptyBorder(30, 40, 30, 40));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(MAIN_BG);
        JLabel title = new JLabel("My Medical History & Prescriptions");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(TEXT_BLACK);
        JLabel subtitle = new JLabel("Review past clinical assessments, vital sign records, and active digital prescriptions.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(TEXT_MUTED);
        headerPanel.add(title, BorderLayout.NORTH);
        headerPanel.add(subtitle, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // Split Layout: Top is Medical History, Bottom is Prescriptions Log
        JPanel container = new JPanel(new GridLayout(2, 1, 0, 20));
        container.setBackground(MAIN_BG);

        // --- SECTION 1: Medical History Table ---
        JPanel historyContainer = new JPanel(new BorderLayout());
        historyContainer.setBackground(PANEL_BG);
        historyContainer.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));

        JLabel lblHistTitle = new JLabel("Past Consultation & Vitals Records");
        lblHistTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblHistTitle.setBorder(new EmptyBorder(15, 15, 10, 15));
        historyContainer.add(lblHistTitle, BorderLayout.NORTH);

        String[] histCols = {"Date", "Doctor", "Department", "Diagnosis / Notes", "Temp / BP"};
        DefaultTableModel histModel = new DefaultTableModel(histCols, 0);
        JTable histTable = new JTable(histModel);
        styleTable(histTable);

        histModel.addRow(new Object[]{"2026-08-10", "Dr. Smith", "General Practice", "Mild fatigue and upper respiratory cold.", "36.8°C / 120/80"});
        histModel.addRow(new Object[]{"2026-07-15", "Dr. Alan", "Cardiology", "Routine cardiac checkup. Normal sinus rhythm.", "36.5°C / 118/76"});

        JScrollPane scrollHist = new JScrollPane(histTable);
        scrollHist.setBorder(BorderFactory.createEmptyBorder());
        scrollHist.getViewport().setBackground(PANEL_BG);
        historyContainer.add(scrollHist, BorderLayout.CENTER);

        // --- SECTION 2: Active Prescriptions Table ---
        JPanel rxContainer = new JPanel(new BorderLayout());
        rxContainer.setBackground(PANEL_BG);
        rxContainer.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));

        JLabel lblRxTitle = new JLabel("Issued Digital Prescriptions");
        lblRxTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblRxTitle.setBorder(new EmptyBorder(15, 15, 10, 15));
        rxContainer.add(lblRxTitle, BorderLayout.NORTH);

        String[] rxCols = {"Rx ID", "Medication Name", "Dosage", "Frequency", "Duration", "Prescribing Doctor"};
        DefaultTableModel rxModel = new DefaultTableModel(rxCols, 0);
        JTable rxTable = new JTable(rxModel);
        styleTable(rxTable);

        rxModel.addRow(new Object[]{"RX-901", "Paracetamol 500mg", "1 Tablet", "TDS (3x Daily)", "5 Days", "Dr. Smith"});
        rxModel.addRow(new Object[]{"RX-884", "Amoxicillin 250mg", "1 Capsule", "BD (2x Daily)", "7 Days", "Dr. Sarah"});

        JScrollPane scrollRx = new JScrollPane(rxTable);
        scrollRx.setBorder(BorderFactory.createEmptyBorder());
        scrollRx.getViewport().setBackground(PANEL_BG);
        rxContainer.add(scrollRx, BorderLayout.CENTER);

        container.add(historyContainer);
        container.add(rxContainer);
        add(container, BorderLayout.CENTER);
    }

    private void styleTable(JTable table) {
        table.setRowHeight(32);
        table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(BORDER_COLOR);
        table.setSelectionBackground(new Color(204, 251, 241)); // Light teal selection
        table.setSelectionForeground(TEXT_BLACK);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(MAIN_BG);
        header.setForeground(TEXT_MUTED);
        header.setPreferredSize(new Dimension(0, 35));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
    }
}