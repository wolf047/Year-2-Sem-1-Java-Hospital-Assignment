/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package Admin;

import HelperFunction.SessionUser;
import Users.UserLogin;
import java.awt.CardLayout;
import java.awt.Color;
import java.util.ArrayList;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author User
 */
public class AdminDashboard extends javax.swing.JFrame {

    private final Color NAV_BG = new Color(30, 95, 125);
    private final Color BLUE = new Color(38, 117, 154);

    private AdminServices admin;

    // cmbShift/cmbDoctor selections are matched back to ids by list index
    private ArrayList<Integer> shiftIds = new ArrayList<>();
    private ArrayList<Integer> doctorIds = new ArrayList<>();

    private void showPage(String cardName, JButton activeButton) {
        CardLayout cardLayout = (CardLayout) pnlContent.getLayout();
        cardLayout.show(pnlContent, cardName);

        JButton[] menu = {btnNavUsers, btnNavAssign, btnNavAssets, btnNavBilling};
        for (JButton b : menu) {
            b.setBackground(NAV_BG);
            b.setForeground(Color.WHITE);
        }
        activeButton.setBackground(Color.WHITE);
        activeButton.setForeground(BLUE);
    }

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(AdminDashboard.class.getName());

    /**
     * Creates new form AdminDashboard
     */
    public AdminDashboard() {
        initComponents();

        this.admin = (Admin) SessionUser.getCurrentUser();
        lblWelcome.setText("Welcome, " + admin.getFullName());

        loadUsers();
        loadAssignmentsPage();
        loadAssets();
        loadBilling();
        showPage("users", btnNavUsers);
    }

    // =====================================================================
    // MANAGE USERS PAGE
    // =====================================================================
    private void loadUsers() {
        DefaultTableModel model = (DefaultTableModel) tblUsers.getModel();
        model.setRowCount(0);
        for (Object[] row : admin.getUsers()) {
            model.addRow(row);
        }
    }

    private void btnToggleUserStatusActionPerformed(java.awt.event.ActionEvent evt) {
        int row = tblUsers.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a user from the list.");
            return;
        }
        int userId = (Integer) tblUsers.getValueAt(row, 0);
        String result = admin.toggleUserStatus(userId);
        if (result != null) {
            JOptionPane.showMessageDialog(this, result);
            return;
        }
        loadUsers();
    }

    // =====================================================================
    // DOCTOR ASSIGNMENT PAGE
    // =====================================================================
    private void loadAssignmentsPage() {
        DefaultTableModel model = (DefaultTableModel) tblAssignments.getModel();
        model.setRowCount(0);
        for (Object[] row : admin.getShiftAssignments()) {
            model.addRow(row);
        }

        shiftIds.clear();
        ArrayList<String> shiftLabels = new ArrayList<>();
        for (Object[] row : admin.getShifts()) {
            shiftIds.add((Integer) row[0]);
            shiftLabels.add(row[1] + " - " + row[2] + " " + row[3] + "-" + row[4]);
        }
        cmbShift.setModel(new DefaultComboBoxModel<>(shiftLabels.toArray(new String[0])));

        doctorIds.clear();
        ArrayList<String> doctorLabels = new ArrayList<>();
        for (Object[] row : admin.getActiveDoctors()) {
            doctorIds.add((Integer) row[0]);
            doctorLabels.add((String) row[1]);
        }
        cmbDoctor.setModel(new DefaultComboBoxModel<>(doctorLabels.toArray(new String[0])));
    }

    private void btnAssignDoctorActionPerformed(java.awt.event.ActionEvent evt) {
        int shiftIndex = cmbShift.getSelectedIndex();
        int doctorIndex = cmbDoctor.getSelectedIndex();
        if (shiftIndex < 0 || doctorIndex < 0) {
            JOptionPane.showMessageDialog(this, "Please select both a shift and a doctor.");
            return;
        }
        String result = admin.assignDoctor(shiftIds.get(shiftIndex), doctorIds.get(doctorIndex));
        if (result != null) {
            JOptionPane.showMessageDialog(this, result);
            return;
        }
        loadAssignmentsPage();
    }

    private void btnUnassignDoctorActionPerformed(java.awt.event.ActionEvent evt) {
        int row = tblAssignments.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an assignment from the list.");
            return;
        }
        int assignmentId = (Integer) tblAssignments.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Remove this doctor from the shift?",
                "Confirm Unassign", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        String result = admin.unassignDoctor(assignmentId);
        if (result != null) {
            JOptionPane.showMessageDialog(this, result);
            return;
        }
        loadAssignmentsPage();
    }

    // =====================================================================
    // HOSPITAL ASSETS PAGE
    // =====================================================================
    private void loadAssets() {
        ArrayList<String> categories = admin.getAssetCategories();
        cmbAssetCategory.setModel(new DefaultComboBoxModel<>(categories.toArray(new String[0])));
        refreshAssetsTable();
    }

    private void refreshAssetsTable() {
        String category = (String) cmbAssetCategory.getSelectedItem();
        if (category == null) {
            return;
        }
        DefaultTableModel model = (DefaultTableModel) tblAssets.getModel();
        model.setRowCount(0);
        for (Object[] row : admin.getAssets(category)) {
            model.addRow(row);
        }
    }

    private void cmbAssetCategoryActionPerformed(java.awt.event.ActionEvent evt) {
        refreshAssetsTable();
    }

    private void btnToggleAssetStatusActionPerformed(java.awt.event.ActionEvent evt) {
        int row = tblAssets.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an asset from the list.");
            return;
        }
        String category = (String) cmbAssetCategory.getSelectedItem();
        int assetId = (Integer) tblAssets.getValueAt(row, 0);
        String result = admin.toggleAssetStatus(category, assetId);
        if (result != null) {
            JOptionPane.showMessageDialog(this, result);
            return;
        }
        refreshAssetsTable();
    }

    // =====================================================================
    // RATES & INSURANCE PAGE
    // =====================================================================
    private void loadBilling() {
        txtConsultFee.setText(String.valueOf(admin.getConsultationFee()));
        txtHospFee.setText(String.valueOf(admin.getHospitalisationFee()));

        DefaultTableModel tierModel = (DefaultTableModel) tblTiers.getModel();
        tierModel.setRowCount(0);
        for (Object[] row : admin.getTiers()) {
            tierModel.addRow(row);
        }

        DefaultTableModel insuranceModel = (DefaultTableModel) tblInsurance.getModel();
        insuranceModel.setRowCount(0);
        for (Object[] row : admin.getInsuranceNetworks()) {
            insuranceModel.addRow(row);
        }
    }

    private void btnSaveFeesActionPerformed(java.awt.event.ActionEvent evt) {
        float consultFee, hospFee;
        try {
            consultFee = Float.parseFloat(txtConsultFee.getText().trim());
            hospFee = Float.parseFloat(txtHospFee.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid amounts for both fees.");
            return;
        }
        String result = admin.updateFees(consultFee, hospFee);
        if (result != null) {
            JOptionPane.showMessageDialog(this, result);
            return;
        }
        JOptionPane.showMessageDialog(this, "Fees updated.");
        loadBilling();
    }

    private void btnUpdateMultiplierActionPerformed(java.awt.event.ActionEvent evt) {
        int row = tblTiers.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a tier from the list.");
            return;
        }
        float multiplier;
        try {
            multiplier = Float.parseFloat(txtMultiplier.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid multiplier.");
            return;
        }
        int tierId = (Integer) tblTiers.getValueAt(row, 0);
        String result = admin.updateTierMultiplier(tierId, multiplier);
        if (result != null) {
            JOptionPane.showMessageDialog(this, result);
            return;
        }
        txtMultiplier.setText("");
        loadBilling();
    }

    private void btnAddInsuranceActionPerformed(java.awt.event.ActionEvent evt) {
        String result = admin.addInsuranceNetwork(txtInsuranceName.getText());
        if (result != null) {
            JOptionPane.showMessageDialog(this, result);
            return;
        }
        txtInsuranceName.setText("");
        loadBilling();
    }

    private void btnRemoveInsuranceActionPerformed(java.awt.event.ActionEvent evt) {
        int row = tblInsurance.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an insurance provider from the list.");
            return;
        }
        int insuranceId = (Integer) tblInsurance.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Remove this insurance provider?",
                "Confirm Remove", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        String result = admin.removeInsuranceNetwork(insuranceId);
        if (result != null) {
            JOptionPane.showMessageDialog(this, result);
            return;
        }
        loadBilling();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlHeader = new javax.swing.JPanel();
        lblPortalTitle = new javax.swing.JLabel();
        lblWelcome = new javax.swing.JLabel();
        pnlNav = new javax.swing.JPanel();
        btnNavUsers = new javax.swing.JButton();
        btnNavAssign = new javax.swing.JButton();
        btnNavAssets = new javax.swing.JButton();
        btnNavBilling = new javax.swing.JButton();
        btnLogout = new javax.swing.JButton();
        pnlContent = new javax.swing.JPanel();
        pnlUsers = new javax.swing.JPanel();
        lblUsersTitle = new javax.swing.JLabel();
        scrUsers = new javax.swing.JScrollPane();
        tblUsers = new javax.swing.JTable();
        btnToggleUserStatus = new javax.swing.JButton();
        pnlAssign = new javax.swing.JPanel();
        lblAssignTitle = new javax.swing.JLabel();
        scrAssignments = new javax.swing.JScrollPane();
        tblAssignments = new javax.swing.JTable();
        lblAssignNew = new javax.swing.JLabel();
        cmbShift = new javax.swing.JComboBox<>();
        cmbDoctor = new javax.swing.JComboBox<>();
        btnAssignDoctor = new javax.swing.JButton();
        btnUnassignDoctor = new javax.swing.JButton();
        pnlAssets = new javax.swing.JPanel();
        lblAssetsTitle = new javax.swing.JLabel();
        lblAssetCategory = new javax.swing.JLabel();
        cmbAssetCategory = new javax.swing.JComboBox<>();
        scrAssets = new javax.swing.JScrollPane();
        tblAssets = new javax.swing.JTable();
        btnToggleAssetStatus = new javax.swing.JButton();
        pnlBilling = new javax.swing.JPanel();
        lblBillingTitle = new javax.swing.JLabel();
        lblFeesHeader = new javax.swing.JLabel();
        lblConsultFee = new javax.swing.JLabel();
        txtConsultFee = new javax.swing.JTextField();
        lblHospFee = new javax.swing.JLabel();
        txtHospFee = new javax.swing.JTextField();
        btnSaveFees = new javax.swing.JButton();
        lblTiersHeader = new javax.swing.JLabel();
        scrTiers = new javax.swing.JScrollPane();
        tblTiers = new javax.swing.JTable();
        lblNewMultiplier = new javax.swing.JLabel();
        txtMultiplier = new javax.swing.JTextField();
        btnUpdateMultiplier = new javax.swing.JButton();
        lblInsuranceHeader = new javax.swing.JLabel();
        scrInsurance = new javax.swing.JScrollPane();
        tblInsurance = new javax.swing.JTable();
        txtInsuranceName = new javax.swing.JTextField();
        btnAddInsurance = new javax.swing.JButton();
        btnRemoveInsurance = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("HMS Admin Portal");
        setPreferredSize(new java.awt.Dimension(800, 600));
        setResizable(false);
        getContentPane().setLayout(null);

        pnlHeader.setBackground(new java.awt.Color(38, 117, 154));
        pnlHeader.setLayout(null);

        lblPortalTitle.setBackground(new java.awt.Color(255, 255, 255));
        lblPortalTitle.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblPortalTitle.setText("APU Medical Centre - Admin Portal");
        pnlHeader.add(lblPortalTitle);
        lblPortalTitle.setBounds(20, 10, 450, 30);

        lblWelcome.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblWelcome.setText("Welcome Admin");
        pnlHeader.add(lblWelcome);
        lblWelcome.setBounds(470, 10, 300, 30);

        getContentPane().add(pnlHeader);
        pnlHeader.setBounds(0, 0, 800, 50);

        pnlNav.setBackground(new java.awt.Color(30, 95, 125));
        pnlNav.setLayout(null);

        btnNavUsers.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnNavUsers.setForeground(new java.awt.Color(38, 117, 154));
        btnNavUsers.setText("Manage Users");
        btnNavUsers.setContentAreaFilled(false);
        btnNavUsers.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnNavUsers.setFocusPainted(false);
        btnNavUsers.setRolloverEnabled(false);
        btnNavUsers.addActionListener(this::btnNavUsersActionPerformed);
        pnlNav.add(btnNavUsers);
        btnNavUsers.setBounds(10, 20, 160, 40);

        btnNavAssign.setBackground(new java.awt.Color(30, 95, 125));
        btnNavAssign.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnNavAssign.setForeground(new java.awt.Color(255, 255, 255));
        btnNavAssign.setText("Doctor Assignment");
        btnNavAssign.setContentAreaFilled(false);
        btnNavAssign.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnNavAssign.setFocusPainted(false);
        btnNavAssign.setRolloverEnabled(false);
        btnNavAssign.addActionListener(this::btnNavAssignActionPerformed);
        pnlNav.add(btnNavAssign);
        btnNavAssign.setBounds(10, 70, 160, 40);

        btnNavAssets.setBackground(new java.awt.Color(30, 95, 125));
        btnNavAssets.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnNavAssets.setForeground(new java.awt.Color(255, 255, 255));
        btnNavAssets.setText("Hospital Assets");
        btnNavAssets.setContentAreaFilled(false);
        btnNavAssets.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnNavAssets.setFocusPainted(false);
        btnNavAssets.setRolloverEnabled(false);
        btnNavAssets.addActionListener(this::btnNavAssetsActionPerformed);
        pnlNav.add(btnNavAssets);
        btnNavAssets.setBounds(10, 120, 160, 40);

        btnNavBilling.setBackground(new java.awt.Color(30, 95, 125));
        btnNavBilling.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnNavBilling.setForeground(new java.awt.Color(255, 255, 255));
        btnNavBilling.setText("Rates & Insurance");
        btnNavBilling.setContentAreaFilled(false);
        btnNavBilling.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnNavBilling.setFocusPainted(false);
        btnNavBilling.setRolloverEnabled(false);
        btnNavBilling.addActionListener(this::btnNavBillingActionPerformed);
        pnlNav.add(btnNavBilling);
        btnNavBilling.setBounds(10, 170, 160, 40);

        btnLogout.setBackground(new java.awt.Color(30, 95, 125));
        btnLogout.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnLogout.setForeground(new java.awt.Color(255, 255, 255));
        btnLogout.setText("LOGOUT");
        btnLogout.setContentAreaFilled(false);
        btnLogout.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnLogout.setFocusPainted(false);
        btnLogout.setRolloverEnabled(false);
        btnLogout.addActionListener(this::btnLogoutActionPerformed);
        pnlNav.add(btnLogout);
        btnLogout.setBounds(10, 450, 160, 40);

        getContentPane().add(pnlNav);
        pnlNav.setBounds(0, 50, 180, 550);

        pnlContent.setBackground(new java.awt.Color(255, 255, 255));
        pnlContent.setLayout(new java.awt.CardLayout());

        pnlUsers.setBackground(new java.awt.Color(255, 255, 255));
        pnlUsers.setLayout(null);

        lblUsersTitle.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblUsersTitle.setText("Manage End Users");
        pnlUsers.add(lblUsersTitle);
        lblUsersTitle.setBounds(20, 15, 400, 30);

        tblUsers.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new String [] {
                "ID", "Name", "Role", "Phone", "Email", "Status"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblUsers.getTableHeader().setReorderingAllowed(false);
        tblUsers.setSelectionBackground(new java.awt.Color(38, 117, 154));
        tblUsers.setSelectionForeground(new java.awt.Color(255, 255, 255));
        scrUsers.setViewportView(tblUsers);
        pnlUsers.add(scrUsers);
        scrUsers.setBounds(20, 60, 580, 400);

        btnToggleUserStatus.setBackground(new java.awt.Color(38, 117, 154));
        btnToggleUserStatus.setForeground(new java.awt.Color(255, 255, 255));
        btnToggleUserStatus.setText("Activate / Deactivate Selected");
        btnToggleUserStatus.addActionListener(this::btnToggleUserStatusActionPerformed);
        pnlUsers.add(btnToggleUserStatus);
        btnToggleUserStatus.setBounds(20, 470, 250, 36);

        pnlContent.add(pnlUsers, "users");

        pnlAssign.setBackground(new java.awt.Color(255, 255, 255));
        pnlAssign.setLayout(null);

        lblAssignTitle.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblAssignTitle.setText("Doctor Assignment");
        pnlAssign.add(lblAssignTitle);
        lblAssignTitle.setBounds(20, 15, 400, 30);

        tblAssignments.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new String [] {
                "Assignment ID", "Department", "Date", "Start", "End", "Doctor"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblAssignments.getTableHeader().setReorderingAllowed(false);
        tblAssignments.setSelectionBackground(new java.awt.Color(38, 117, 154));
        tblAssignments.setSelectionForeground(new java.awt.Color(255, 255, 255));
        scrAssignments.setViewportView(tblAssignments);
        pnlAssign.add(scrAssignments);
        scrAssignments.setBounds(20, 60, 580, 280);

        lblAssignNew.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblAssignNew.setText("Assign a doctor to a shift:");
        pnlAssign.add(lblAssignNew);
        lblAssignNew.setBounds(20, 355, 300, 20);

        cmbShift.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] {}));
        pnlAssign.add(cmbShift);
        cmbShift.setBounds(20, 380, 280, 26);

        cmbDoctor.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] {}));
        pnlAssign.add(cmbDoctor);
        cmbDoctor.setBounds(310, 380, 190, 26);

        btnAssignDoctor.setBackground(new java.awt.Color(38, 117, 154));
        btnAssignDoctor.setForeground(new java.awt.Color(255, 255, 255));
        btnAssignDoctor.setText("Assign");
        btnAssignDoctor.addActionListener(this::btnAssignDoctorActionPerformed);
        pnlAssign.add(btnAssignDoctor);
        btnAssignDoctor.setBounds(510, 380, 90, 26);

        btnUnassignDoctor.setBackground(new java.awt.Color(38, 117, 154));
        btnUnassignDoctor.setForeground(new java.awt.Color(255, 255, 255));
        btnUnassignDoctor.setText("Unassign Selected");
        btnUnassignDoctor.addActionListener(this::btnUnassignDoctorActionPerformed);
        pnlAssign.add(btnUnassignDoctor);
        btnUnassignDoctor.setBounds(20, 425, 250, 36);

        pnlContent.add(pnlAssign, "assign");

        pnlAssets.setBackground(new java.awt.Color(255, 255, 255));
        pnlAssets.setLayout(null);

        lblAssetsTitle.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblAssetsTitle.setText("Hospital Assets");
        pnlAssets.add(lblAssetsTitle);
        lblAssetsTitle.setBounds(20, 15, 400, 30);

        lblAssetCategory.setText("Category:");
        pnlAssets.add(lblAssetCategory);
        lblAssetCategory.setBounds(20, 60, 80, 26);

        cmbAssetCategory.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] {}));
        cmbAssetCategory.addActionListener(this::cmbAssetCategoryActionPerformed);
        pnlAssets.add(cmbAssetCategory);
        cmbAssetCategory.setBounds(110, 60, 220, 26);

        tblAssets.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new String [] {
                "ID", "Detail", "Status"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblAssets.getTableHeader().setReorderingAllowed(false);
        tblAssets.setSelectionBackground(new java.awt.Color(38, 117, 154));
        tblAssets.setSelectionForeground(new java.awt.Color(255, 255, 255));
        scrAssets.setViewportView(tblAssets);
        pnlAssets.add(scrAssets);
        scrAssets.setBounds(20, 100, 580, 350);

        btnToggleAssetStatus.setBackground(new java.awt.Color(38, 117, 154));
        btnToggleAssetStatus.setForeground(new java.awt.Color(255, 255, 255));
        btnToggleAssetStatus.setText("Toggle Status (OK / Maintenance)");
        btnToggleAssetStatus.addActionListener(this::btnToggleAssetStatusActionPerformed);
        pnlAssets.add(btnToggleAssetStatus);
        btnToggleAssetStatus.setBounds(20, 460, 260, 36);

        pnlContent.add(pnlAssets, "assets");

        pnlBilling.setBackground(new java.awt.Color(255, 255, 255));
        pnlBilling.setLayout(null);

        lblBillingTitle.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblBillingTitle.setText("Rates & Insurance");
        pnlBilling.add(lblBillingTitle);
        lblBillingTitle.setBounds(20, 15, 400, 30);

        lblFeesHeader.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblFeesHeader.setText("Consultation & Hospitalisation Fees");
        pnlBilling.add(lblFeesHeader);
        lblFeesHeader.setBounds(20, 55, 400, 20);

        lblConsultFee.setText("Consultation Fee (RM):");
        pnlBilling.add(lblConsultFee);
        lblConsultFee.setBounds(20, 80, 160, 26);

        pnlBilling.add(txtConsultFee);
        txtConsultFee.setBounds(180, 78, 100, 26);

        lblHospFee.setText("Hospitalisation Fee (RM/day):");
        pnlBilling.add(lblHospFee);
        lblHospFee.setBounds(300, 80, 190, 26);

        pnlBilling.add(txtHospFee);
        txtHospFee.setBounds(490, 78, 100, 26);

        btnSaveFees.setBackground(new java.awt.Color(38, 117, 154));
        btnSaveFees.setForeground(new java.awt.Color(255, 255, 255));
        btnSaveFees.setText("Save Fees");
        btnSaveFees.addActionListener(this::btnSaveFeesActionPerformed);
        pnlBilling.add(btnSaveFees);
        btnSaveFees.setBounds(20, 112, 150, 30);

        lblTiersHeader.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblTiersHeader.setText("Doctor Tier Multipliers");
        pnlBilling.add(lblTiersHeader);
        lblTiersHeader.setBounds(20, 155, 400, 20);

        tblTiers.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new String [] {
                "ID", "Tier", "Min Years", "Multiplier"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblTiers.getTableHeader().setReorderingAllowed(false);
        tblTiers.setSelectionBackground(new java.awt.Color(38, 117, 154));
        tblTiers.setSelectionForeground(new java.awt.Color(255, 255, 255));
        scrTiers.setViewportView(tblTiers);
        pnlBilling.add(scrTiers);
        scrTiers.setBounds(20, 180, 580, 110);

        lblNewMultiplier.setText("New Multiplier:");
        pnlBilling.add(lblNewMultiplier);
        lblNewMultiplier.setBounds(20, 300, 110, 26);

        pnlBilling.add(txtMultiplier);
        txtMultiplier.setBounds(140, 298, 80, 26);

        btnUpdateMultiplier.setBackground(new java.awt.Color(38, 117, 154));
        btnUpdateMultiplier.setForeground(new java.awt.Color(255, 255, 255));
        btnUpdateMultiplier.setText("Update Selected Tier");
        btnUpdateMultiplier.addActionListener(this::btnUpdateMultiplierActionPerformed);
        pnlBilling.add(btnUpdateMultiplier);
        btnUpdateMultiplier.setBounds(230, 298, 180, 26);

        lblInsuranceHeader.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblInsuranceHeader.setText("Accepted Insurance Networks");
        pnlBilling.add(lblInsuranceHeader);
        lblInsuranceHeader.setBounds(20, 340, 400, 20);

        tblInsurance.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new String [] {
                "ID", "Insurance Provider"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false
            };
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblInsurance.getTableHeader().setReorderingAllowed(false);
        tblInsurance.setSelectionBackground(new java.awt.Color(38, 117, 154));
        tblInsurance.setSelectionForeground(new java.awt.Color(255, 255, 255));
        scrInsurance.setViewportView(tblInsurance);
        pnlBilling.add(scrInsurance);
        scrInsurance.setBounds(20, 365, 580, 100);

        pnlBilling.add(txtInsuranceName);
        txtInsuranceName.setBounds(20, 475, 300, 26);

        btnAddInsurance.setBackground(new java.awt.Color(38, 117, 154));
        btnAddInsurance.setForeground(new java.awt.Color(255, 255, 255));
        btnAddInsurance.setText("Add");
        btnAddInsurance.addActionListener(this::btnAddInsuranceActionPerformed);
        pnlBilling.add(btnAddInsurance);
        btnAddInsurance.setBounds(330, 475, 100, 26);

        btnRemoveInsurance.setBackground(new java.awt.Color(38, 117, 154));
        btnRemoveInsurance.setForeground(new java.awt.Color(255, 255, 255));
        btnRemoveInsurance.setText("Remove Selected");
        btnRemoveInsurance.addActionListener(this::btnRemoveInsuranceActionPerformed);
        pnlBilling.add(btnRemoveInsurance);
        btnRemoveInsurance.setBounds(440, 475, 160, 26);

        pnlContent.add(pnlBilling, "billing");

        getContentPane().add(pnlContent);
        pnlContent.setBounds(180, 50, 620, 550);

        setBounds(0, 0, 814, 608);
    }// </editor-fold>//GEN-END:initComponents

    private void btnNavUsersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNavUsersActionPerformed
        showPage("users", btnNavUsers);
    }//GEN-LAST:event_btnNavUsersActionPerformed

    private void btnNavAssignActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNavAssignActionPerformed
        showPage("assign", btnNavAssign);
    }//GEN-LAST:event_btnNavAssignActionPerformed

    private void btnNavAssetsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNavAssetsActionPerformed
        showPage("assets", btnNavAssets);
    }//GEN-LAST:event_btnNavAssetsActionPerformed

    private void btnNavBillingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNavBillingActionPerformed
        showPage("billing", btnNavBilling);
    }//GEN-LAST:event_btnNavBillingActionPerformed

    private void btnLogoutActionPerformed(java.awt.event.ActionEvent evt) {
        int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to log out?", "Logout", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            SessionUser.logout();
            new UserLogin().setVisible(true);
            dispose();
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new AdminDashboard().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddInsurance;
    private javax.swing.JButton btnAssignDoctor;
    private javax.swing.JButton btnLogout;
    private javax.swing.JButton btnNavAssets;
    private javax.swing.JButton btnNavAssign;
    private javax.swing.JButton btnNavBilling;
    private javax.swing.JButton btnNavUsers;
    private javax.swing.JButton btnRemoveInsurance;
    private javax.swing.JButton btnSaveFees;
    private javax.swing.JButton btnToggleAssetStatus;
    private javax.swing.JButton btnToggleUserStatus;
    private javax.swing.JButton btnUnassignDoctor;
    private javax.swing.JButton btnUpdateMultiplier;
    private javax.swing.JComboBox<String> cmbAssetCategory;
    private javax.swing.JComboBox<String> cmbDoctor;
    private javax.swing.JComboBox<String> cmbShift;
    private javax.swing.JLabel lblAssetCategory;
    private javax.swing.JLabel lblAssetsTitle;
    private javax.swing.JLabel lblAssignNew;
    private javax.swing.JLabel lblAssignTitle;
    private javax.swing.JLabel lblBillingTitle;
    private javax.swing.JLabel lblConsultFee;
    private javax.swing.JLabel lblFeesHeader;
    private javax.swing.JLabel lblHospFee;
    private javax.swing.JLabel lblInsuranceHeader;
    private javax.swing.JLabel lblNewMultiplier;
    private javax.swing.JLabel lblPortalTitle;
    private javax.swing.JLabel lblTiersHeader;
    private javax.swing.JLabel lblUsersTitle;
    private javax.swing.JLabel lblWelcome;
    private javax.swing.JPanel pnlAssets;
    private javax.swing.JPanel pnlAssign;
    private javax.swing.JPanel pnlBilling;
    private javax.swing.JPanel pnlContent;
    private javax.swing.JPanel pnlHeader;
    private javax.swing.JPanel pnlNav;
    private javax.swing.JPanel pnlUsers;
    private javax.swing.JScrollPane scrAssets;
    private javax.swing.JScrollPane scrAssignments;
    private javax.swing.JScrollPane scrInsurance;
    private javax.swing.JScrollPane scrTiers;
    private javax.swing.JScrollPane scrUsers;
    private javax.swing.JTable tblAssets;
    private javax.swing.JTable tblAssignments;
    private javax.swing.JTable tblInsurance;
    private javax.swing.JTable tblTiers;
    private javax.swing.JTable tblUsers;
    private javax.swing.JTextField txtConsultFee;
    private javax.swing.JTextField txtHospFee;
    private javax.swing.JTextField txtInsuranceName;
    private javax.swing.JTextField txtMultiplier;
    // End of variables declaration//GEN-END:variables
}
