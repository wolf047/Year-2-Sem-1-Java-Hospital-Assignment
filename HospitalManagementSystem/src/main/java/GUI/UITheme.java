package com.mycompany.guitesting;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class UITheme {
    // 1. Centralized Color Palette
    public static final Color PRIMARY_COLOR = new Color(41, 128, 185); 
    public static final Color HOVER_COLOR = new Color(52, 152, 219);
    public static final Color SIDEBAR_COLOR = new Color(44, 62, 80);   
    public static final Color BACKGROUND_COLOR = new Color(245, 246, 250); 
    public static final Color TEXT_COLOR = new Color(255, 255, 255);

    // 2. Centralized Fonts
    public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 28);
    public static final Font STANDARD_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 14);

    // 3. Reusable Button Styling Method (OOP Concept: Abstraction)
    public static void styleButton(JButton button) {
        button.setFont(BUTTON_FONT);
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(TEXT_COLOR);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 15, 10, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Adds the interactive hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(HOVER_COLOR);
            }
            @Override
            public void mouseExited(MouseEvent evt) {
                button.setBackground(PRIMARY_COLOR);
            }
        });
    }
}