package com.mycompany.guitesting;

import javax.swing.*;
import java.awt.*;

public class HMSApplication extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainContainer;

    public HMSApplication() {
        setTitle("APU Medical Centre - HMS");
        setSize(1024, 768);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        // Load all system screens
        mainContainer.add(new LoginPanel(this), "LOGIN");
        mainContainer.add(new AdminDashboard(this), "ADMIN");
        mainContainer.add(new ManagerDashboard(this), "MANAGER");
        mainContainer.add(new DoctorDashboard(this), "DOCTOR");
        mainContainer.add(new PatientDashboard(this), "PATIENT");

        add(mainContainer);
    }

    public void navigateTo(String screenName) {
        cardLayout.show(mainContainer, screenName);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            new HMSApplication().setVisible(true);
        });
    }
}