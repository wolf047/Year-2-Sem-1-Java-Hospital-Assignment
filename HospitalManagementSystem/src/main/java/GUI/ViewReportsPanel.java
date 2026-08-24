package com.mycompany.guitesting;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class ViewReportsPanel extends JPanel {

    private final Color MAIN_BG = new Color(248, 250, 252);
    private final Color PANEL_BG = Color.WHITE;
    private final Color TEXT_BLACK = new Color(15, 23, 42);
    private final Color TEXT_MUTED = new Color(100, 116, 139);
    private final Color ACCENT_BLUE = new Color(14, 165, 233);
    private final Color BORDER_COLOR = new Color(226, 232, 240);

    private DefaultTableModel tableModel;
    private JTable reportTable;

    public ViewReportsPanel() {
        setLayout(new BorderLayout(0, 25));
        setBackground(MAIN_BG);
        setBorder(new EmptyBorder(20, 40, 20, 40));

        // ==========================================
        // 1. HEADER SECTION
        // ==========================================
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(MAIN_BG);
        
        JLabel title = new JLabel("Metrics & Revenue Reports");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(TEXT_BLACK);
        
        JLabel subtitle = new JLabel("Generate and view analytical summaries of hospital performance and finances.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(TEXT_MUTED);
        
        headerPanel.add(title, BorderLayout.NORTH);
        headerPanel.add(subtitle, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // ==========================================
        // 2. MAIN CONTENT (Filters + Cards + Table)
        // ==========================================
        JPanel contentContainer = new JPanel(new BorderLayout(0, 25));
        contentContainer.setBackground(MAIN_BG);

        // --- A. FILTERS SECTION ---
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        filterPanel.setBackground(PANEL_BG);
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(5, 10, 5, 10)
        ));

        JComboBox<String> cbMonth = new JComboBox<>(new String[]{"August 2026", "July 2026", "June 2026", "Q3 2026", "Year-to-Date"});
        cbMonth.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbMonth.setBackground(Color.WHITE);
        cbMonth.setPreferredSize(new Dimension(150, 35));

        JComboBox<String> cbDept = new JComboBox<>(new String[]{"All Departments", "Cardiology", "Emergency", "Pediatrics"});
        cbDept.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbDept.setBackground(Color.WHITE);
        cbDept.setPreferredSize(new Dimension(180, 35));

        JButton btnGenerate = new JButton("Generate Report");
        btnGenerate.setBackground(ACCENT_BLUE);
        btnGenerate.setForeground(Color.BLACK); // Requested black font
        btnGenerate.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnGenerate.setFocusPainted(false);
        btnGenerate.setBorder(BorderFactory.createLineBorder(ACCENT_BLUE.darker()));
        btnGenerate.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGenerate.setPreferredSize(new Dimension(140, 35));

        JLabel lblFilter = new JLabel("Report Period: ");
        lblFilter.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        filterPanel.add(lblFilter);
        filterPanel.add(cbMonth);
        filterPanel.add(new JLabel("   Filter By: "));
        filterPanel.add(cbDept);
        filterPanel.add(btnGenerate);

        contentContainer.add(filterPanel, BorderLayout.NORTH);

        // --- B. SUMMARY CARDS SECTION ---
        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 25, 0));
        cardsPanel.setBackground(MAIN_BG);
        
        cardsPanel.add(new ReportCard("TOTAL REVENUE", "$124,500.00", "Includes consults & labs", ACCENT_BLUE));
        cardsPanel.add(new ReportCard("PATIENT VISITS", "1,842", "Across all wards", new Color(139, 92, 246))); // Purple
        cardsPanel.add(new ReportCard("AVG. CONSULT TIME", "18 Mins", "Optimal efficiency", new Color(16, 185, 129))); // Green
        
        // Wrap cards so they don't stretch too tall
        JPanel centerStack = new JPanel(new BorderLayout(0, 25));
        centerStack.setBackground(MAIN_BG);
        centerStack.add(cardsPanel, BorderLayout.NORTH);

        // --- C. DETAILED DATA TABLE ---
        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setBackground(PANEL_BG);
        tableContainer.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));

        JLabel tableTitle = new JLabel("Revenue Breakdown by Department");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tableTitle.setBorder(new EmptyBorder(15, 15, 15, 15));
        tableContainer.add(tableTitle, BorderLayout.NORTH);

        String[] columns = {"Department", "Total Consultations", "Lab Tests Ordered", "Revenue Generated", "Status"};
        tableModel = new DefaultTableModel(columns, 0);
        reportTable = new JTable(tableModel);
        
        // Modern Table Styling
        reportTable.setRowHeight(35);
        reportTable.setShowGrid(false);
        reportTable.setShowHorizontalLines(true);
        reportTable.setGridColor(BORDER_COLOR);
        reportTable.setSelectionBackground(new Color(224, 242, 254));
        reportTable.setSelectionForeground(TEXT_BLACK);
        reportTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JTableHeader tableHeader = reportTable.getTableHeader();
        tableHeader.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tableHeader.setBackground(MAIN_BG);
        tableHeader.setForeground(TEXT_MUTED);
        tableHeader.setPreferredSize(new Dimension(0, 40));
        tableHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));

        // Dummy report data
        tableModel.addRow(new Object[]{"Cardiology", "340", "120", "$42,500.00", "Above Target"});
        tableModel.addRow(new Object[]{"Emergency (ER)", "850", "300", "$55,000.00", "On Target"});
        tableModel.addRow(new Object[]{"Pediatrics", "410", "45", "$18,000.00", "Below Target"});
        tableModel.addRow(new Object[]{"General Practice", "242", "80", "$9,000.00", "On Target"});

        JScrollPane scrollPane = new JScrollPane(reportTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(PANEL_BG);
        
        tableContainer.add(scrollPane, BorderLayout.CENTER);
        centerStack.add(tableContainer, BorderLayout.CENTER);

        contentContainer.add(centerStack, BorderLayout.CENTER);
        add(contentContainer, BorderLayout.CENTER);
    }

    // --- Inner Class for Report Summary Cards ---
    class ReportCard extends JPanel {
        public ReportCard(String title, String mainValue, String subText, Color accentColor) {
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
            
            JLabel lblValue = new JLabel(mainValue);
            lblValue.setFont(new Font("Segoe UI", Font.BOLD, 32));
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