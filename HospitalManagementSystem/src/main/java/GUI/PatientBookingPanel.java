package com.mycompany.guitesting;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class PatientBookingPanel extends JPanel {

    private final Color MAIN_BG = new Color(248, 250, 252);
    private final Color CARD_BG = Color.WHITE;
    private final Color TEXT_BLACK = new Color(15, 23, 42);
    private final Color TEXT_MUTED = new Color(100, 116, 139);
    private final Color ACCENT_TEAL = new Color(13, 148, 136); // Unique teal theme for patients
    private final Color BORDER_COLOR = new Color(226, 232, 240);

    public PatientBookingPanel() {
        setLayout(new BorderLayout(0, 30));
        setBackground(MAIN_BG);
        setBorder(new EmptyBorder(30, 40, 30, 40));

        // ==========================================
        // 1. UNIQUE PATIENT HEADER
        // ==========================================
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(MAIN_BG);
        
        JLabel title = new JLabel("Find & Book Consultations");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(TEXT_BLACK);
        
        JLabel subtitle = new JLabel("Browse available specialist schedules, secure your appointment slot, or manage existing bookings.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(TEXT_MUTED);
        
        headerPanel.add(title, BorderLayout.NORTH);
        headerPanel.add(subtitle, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // ==========================================
        // 2. MAIN CONTENT: TABS (Browse Slots vs My Bookings)
        // ==========================================
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.setBackground(MAIN_BG);

        // TAB 1: Browse & Book Slots
        JPanel browsePanel = new JPanel(new GridLayout(2, 2, 20, 20));
        browsePanel.setBackground(MAIN_BG);
        browsePanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        browsePanel.add(new DoctorSlotCard("Dr. Alan (Cardiology)", "Mon, Aug 24 • 10:00 AM", "Room C-101", ACCENT_TEAL));
        browsePanel.add(new DoctorSlotCard("Dr. Sarah (Emergency/GP)", "Mon, Aug 24 • 02:00 PM", "Room E-202", ACCENT_TEAL));
        browsePanel.add(new DoctorSlotCard("Dr. Emily (Pediatrics)", "Tue, Aug 25 • 09:30 AM", "Room P-104", ACCENT_TEAL));
        browsePanel.add(new DoctorSlotCard("Dr. Smith (General Practice)", "Tue, Aug 25 • 11:00 AM", "Room G-105", ACCENT_TEAL));

        tabbedPane.addTab("🩺 Available Slots", browsePanel);

        // TAB 2: Manage Existing Bookings (Reschedule / Cancel)
        JPanel managePanel = new JPanel(new GridLayout(2, 1, 0, 15));
        managePanel.setBackground(MAIN_BG);
        managePanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        managePanel.add(new ActiveBookingCard("Appointment #BK-801", "Dr. Alan (Cardiology)", "Aug 22, 2026 at 10:00 AM", "Confirmed"));
        managePanel.add(new ActiveBookingCard("Appointment #BK-804", "Dr. Sarah (Emergency/GP)", "Aug 26, 2026 at 03:30 PM", "Pending Confirmation"));

        tabbedPane.addTab("📅 My Bookings & Reschedule", managePanel);

        add(tabbedPane, BorderLayout.CENTER);
    }

    // --- Custom Unique Card for Available Doctor Slots ---
    class DoctorSlotCard extends JPanel {
        public DoctorSlotCard(String doctorName, String timeSlot, String location, Color accentColor) {
            setLayout(new BorderLayout());
            setBackground(CARD_BG);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 5, 0, 0, accentColor), // Unique left color accent bar
                    BorderFactory.createLineBorder(BORDER_COLOR, 1)
                ),
                new EmptyBorder(20, 20, 20, 20)
            ));

            JPanel infoPanel = new JPanel(new GridLayout(3, 1, 0, 5));
            infoPanel.setBackground(CARD_BG);

            JLabel lblDoc = new JLabel(doctorName);
            lblDoc.setFont(new Font("Segoe UI", Font.BOLD, 18));
            lblDoc.setForeground(TEXT_BLACK);

            JLabel lblTime = new JLabel("🕒 " + timeSlot);
            lblTime.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            lblTime.setForeground(ACCENT_TEAL);

            JLabel lblLoc = new JLabel("📍 Location: " + location);
            lblLoc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            lblLoc.setForeground(TEXT_MUTED);

            infoPanel.add(lblDoc);
            infoPanel.add(lblTime);
            infoPanel.add(lblLoc);

            JButton btnBook = new JButton("Book Slot");
            btnBook.setBackground(accentColor);
            btnBook.setForeground(Color.BLACK);
            btnBook.setFont(new Font("Segoe UI", Font.BOLD, 13));
            btnBook.setFocusPainted(false);
            btnBook.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnBook.setPreferredSize(new Dimension(120, 35));
            btnBook.addActionListener(e -> JOptionPane.showMessageDialog(this, "Successfully booked slot with " + doctorName + "!"));

            JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 10));
            btnWrapper.setBackground(CARD_BG);
            btnWrapper.add(btnBook);

            add(infoPanel, BorderLayout.CENTER);
            add(btnWrapper, BorderLayout.SOUTH);
        }
    }

    // --- Custom Card for Managing Bookings (Reschedule / Cancel) ---
    class ActiveBookingCard extends JPanel {
        public ActiveBookingCard(String bkId, String doctorName, String dateTime, String status) {
            setLayout(new BorderLayout());
            setBackground(CARD_BG);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(20, 20, 20, 20)
            ));

            JPanel infoPanel = new JPanel(new GridLayout(3, 1, 0, 5));
            infoPanel.setBackground(CARD_BG);

            JLabel lblId = new JLabel(bkId + " — Status: " + status);
            lblId.setFont(new Font("Segoe UI", Font.BOLD, 15));
            lblId.setForeground(status.equals("Confirmed") ? new Color(16, 185, 129) : new Color(217, 119, 6));

            JLabel lblDoc = new JLabel("Doctor: " + doctorName);
            lblDoc.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblDoc.setForeground(TEXT_BLACK);

            JLabel lblTime = new JLabel("Scheduled For: " + dateTime);
            lblTime.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            lblTime.setForeground(TEXT_MUTED);

            infoPanel.add(lblId);
            infoPanel.add(lblDoc);
            infoPanel.add(lblTime);

            // Action buttons (Reschedule & Cancel)
            JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
            actionPanel.setBackground(CARD_BG);

            JButton btnReschedule = new JButton("Reschedule");
            styleActionButton(btnReschedule, new Color(226, 232, 240));
            btnReschedule.addActionListener(e -> JOptionPane.showMessageDialog(this, "Select a new date/time to reschedule " + bkId));

            JButton btnCancel = new JButton("Cancel Booking");
            styleActionButton(btnCancel, new Color(254, 226, 226));
            btnCancel.setForeground(new Color(220, 38, 38));
            btnCancel.addActionListener(e -> JOptionPane.showMessageDialog(this, "Booking " + bkId + " has been cancelled."));

            actionPanel.add(btnReschedule);
            actionPanel.add(btnCancel);

            add(infoPanel, BorderLayout.CENTER);
            add(actionPanel, BorderLayout.EAST);
        }

        private void styleActionButton(JButton btn, Color bg) {
            btn.setBackground(bg);
            btn.setForeground(TEXT_BLACK);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setPreferredSize(new Dimension(130, 32));
        }
    }
}