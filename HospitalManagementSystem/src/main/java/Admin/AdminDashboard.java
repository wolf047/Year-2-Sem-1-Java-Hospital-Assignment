/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package Admin;

import HelperFunction.SessionUser;
import Users.UserLogin;
import java.awt.CardLayout;
import java.awt.Color;
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

    private void showPage(String cardName, JButton activeButton) {
        CardLayout cardLayout = (CardLayout) pnlContent.getLayout();
        cardLayout.show(pnlContent, cardName);

        JButton[] menu = {btnNavUsers, btnNavAssign, btnNavAssets, btnNavWards, btnNavAdmissions,
            btnNavDiagnostics, btnNavCatalogues, btnNavBilling};
        for (JButton b : menu) {
            b.setBackground(NAV_BG);
            b.setForeground(Color.WHITE);
        }
        activeButton.setBackground(Color.WHITE);
        activeButton.setForeground(BLUE);
    }

    private void showDiagSubPage(String cardName, JButton activeButton) {
        CardLayout cardLayout = (CardLayout) pnlDiagContent.getLayout();
        cardLayout.show(pnlDiagContent, cardName);
        JButton[] tabs = {btnDiagSubLab, btnDiagSubImaging};
        for (JButton b : tabs) {
            b.setBackground(NAV_BG);
            b.setForeground(Color.WHITE);
        }
        activeButton.setBackground(Color.WHITE);
        activeButton.setForeground(BLUE);
    }

    private void showCatSubPage(String cardName, JButton activeButton) {
        CardLayout cardLayout = (CardLayout) pnlCatContent.getLayout();
        cardLayout.show(pnlCatContent, cardName);
        JButton[] tabs = {btnCatSubDrugs, btnCatSubServices};
        for (JButton b : tabs) {
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

        cmbRoleFilter.setModel(new DefaultComboBoxModel<>(new String[]{"All", "Admin", "Medical Manager", "Doctor", "Patient"}));

        loadUsers();
        loadAssignPage();
        loadAssets();
        loadWardsPage();
        loadAdmissionsPage();
        loadDiagnosticsPage();
        loadCataloguesPage();
        loadBilling();

        showPage("users", btnNavUsers);
        showDiagSubPage("lab", btnDiagSubLab);
        showCatSubPage("drugs", btnCatSubDrugs);
    }

    // =====================================================================
    // MANAGE USERS PAGE
    // =====================================================================
    private void loadUsers() {
        DefaultTableModel model = (DefaultTableModel) tblUsers.getModel();
        model.setRowCount(0);
        String search = txtUserSearch.getText();
        String roleFilter = (String) cmbRoleFilter.getSelectedItem();
        for (Object[] row : admin.getUsers(search, roleFilter)) {
            model.addRow(row);
        }
    }

    private void btnSearchUsersActionPerformed(java.awt.event.ActionEvent evt) {
        loadUsers();
    }

    private void btnNewUserActionPerformed(java.awt.event.ActionEvent evt) {
        UserEditDialog dialog = new UserEditDialog(this, admin, null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadUsers();
            loadAssignPage();
        }
    }

    private void btnEditUserActionPerformed(java.awt.event.ActionEvent evt) {
        int row = tblUsers.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a user from the list.");
            return;
        }
        int userId = (Integer) tblUsers.getValueAt(row, 0);
        UserEditDialog dialog = new UserEditDialog(this, admin, userId);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadUsers();
            loadAssignPage();
            lblWelcome.setText("Welcome, " + admin.getFullName());
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
    // ASSIGN DOCTORS TO MEDICAL MANAGERS PAGE
    // =====================================================================
    private void loadAssignPage() {
        DefaultTableModel model = (DefaultTableModel) tblAssignments.getModel();
        model.setRowCount(0);
        for (Object[] row : admin.getDoctorAssignments()) {
            model.addRow(row);
        }
    }

    private void btnReassignDeptActionPerformed(java.awt.event.ActionEvent evt) {
        int row = tblAssignments.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a doctor from the list.");
            return;
        }
        int doctorId = (Integer) tblAssignments.getValueAt(row, 0);
        String doctorName = (String) tblAssignments.getValueAt(row, 1);
        String currentDept = (String) tblAssignments.getValueAt(row, 2);
        DepartmentAssignDialog dialog = new DepartmentAssignDialog(this, admin, doctorId, doctorName, currentDept);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadAssignPage();
        }
    }

    // =====================================================================
    // HOSPITAL ASSETS PAGE
    // =====================================================================
    private void loadAssets() {
        cmbAssetCategory.setModel(new DefaultComboBoxModel<>(admin.getAssetCategories().toArray(new String[0])));
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

    private void btnAddAssetActionPerformed(java.awt.event.ActionEvent evt) {
        String category = (String) cmbAssetCategory.getSelectedItem();
        AddAssetDialog dialog = new AddAssetDialog(this, admin, category);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            refreshAssetsTable();
        }
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
    // WARDS & BEDS PAGE
    // =====================================================================
    private void loadWardsPage() {
        DefaultTableModel model = (DefaultTableModel) tblWards.getModel();
        model.setRowCount(0);
        for (Object[] row : admin.getWards()) {
            model.addRow(row);
        }
    }

    private void btnAddWardActionPerformed(java.awt.event.ActionEvent evt) {
        WardEditDialog dialog = new WardEditDialog(this, admin, null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadWardsPage();
        }
    }

    private void btnEditWardActionPerformed(java.awt.event.ActionEvent evt) {
        int row = tblWards.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a ward from the list.");
            return;
        }
        int wardId = (Integer) tblWards.getValueAt(row, 0);
        WardEditDialog dialog = new WardEditDialog(this, admin, wardId);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadWardsPage();
        }
    }

    private void btnManageBedsActionPerformed(java.awt.event.ActionEvent evt) {
        int row = tblWards.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a ward from the list.");
            return;
        }
        int wardId = (Integer) tblWards.getValueAt(row, 0);
        String wardLabel = tblWards.getValueAt(row, 1) + " Ward #" + wardId + " (" + tblWards.getValueAt(row, 2) + ")";
        BedsDialog dialog = new BedsDialog(this, admin, wardId, wardLabel);
        dialog.setVisible(true);
        loadWardsPage();
    }

    // =====================================================================
    // ADMISSIONS PAGE
    // =====================================================================
    private void loadAdmissionsPage() {
        DefaultTableModel model = (DefaultTableModel) tblAdmissions.getModel();
        model.setRowCount(0);
        for (Object[] row : admin.getAdmissions()) {
            model.addRow(row);
        }
    }

    private void btnNewAdmissionActionPerformed(java.awt.event.ActionEvent evt) {
        AdmissionDialog dialog = new AdmissionDialog(this, admin);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadAdmissionsPage();
        }
    }

    private void btnDischargeActionPerformed(java.awt.event.ActionEvent evt) {
        int row = tblAdmissions.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an admission from the list.");
            return;
        }
        int admissionId = (Integer) tblAdmissions.getValueAt(row, 0);
        String input = JOptionPane.showInputDialog(this, "Discharge date (dd-MM-yyyy):",
                java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        if (input == null) {
            return;
        }
        String result = admin.dischargePatient(admissionId, input);
        if (result != null) {
            JOptionPane.showMessageDialog(this, result);
            return;
        }
        loadAdmissionsPage();
    }

    // =====================================================================
    // DIAGNOSTICS: LAB RESULTS & IMAGING SCHEDULING PAGE
    // =====================================================================
    private void loadDiagnosticsPage() {
        loadLabRequests();
        loadImagingRequests();
    }

    private void loadLabRequests() {
        DefaultTableModel model = (DefaultTableModel) tblLabRequests.getModel();
        model.setRowCount(0);
        for (Object[] row : admin.getPendingLabRequests()) {
            model.addRow(row);
        }
    }

    private void btnEnterLabResultActionPerformed(java.awt.event.ActionEvent evt) {
        int row = tblLabRequests.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a pending lab request.");
            return;
        }
        int requestId = (Integer) tblLabRequests.getValueAt(row, 0);
        String serviceName = (String) tblLabRequests.getValueAt(row, 1);
        String patientName = (String) tblLabRequests.getValueAt(row, 2);
        String requestDate = (String) tblLabRequests.getValueAt(row, 3);
        String remarks = (String) tblLabRequests.getValueAt(row, 4);
        LabResultDialog dialog = new LabResultDialog(this, admin, requestId, serviceName, patientName, requestDate, remarks);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadLabRequests();
        }
    }

    private void loadImagingRequests() {
        DefaultTableModel model = (DefaultTableModel) tblImagingRequests.getModel();
        model.setRowCount(0);
        for (Object[] row : admin.getPendingImagingRequests()) {
            model.addRow(row);
        }
    }

    private void btnScheduleSelectedActionPerformed(java.awt.event.ActionEvent evt) {
        int row = tblImagingRequests.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a pending imaging request.");
            return;
        }
        int requestId = (Integer) tblImagingRequests.getValueAt(row, 0);
        String serviceName = (String) tblImagingRequests.getValueAt(row, 1);
        String patientName = (String) tblImagingRequests.getValueAt(row, 2);
        String requestDate = (String) tblImagingRequests.getValueAt(row, 3);
        ImagingScheduleDialog dialog = new ImagingScheduleDialog(this, admin, requestId, serviceName, patientName, requestDate);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadImagingRequests();
        }
    }

    private void btnDiagSubLabActionPerformed(java.awt.event.ActionEvent evt) {
        showDiagSubPage("lab", btnDiagSubLab);
    }

    private void btnDiagSubImagingActionPerformed(java.awt.event.ActionEvent evt) {
        showDiagSubPage("imaging", btnDiagSubImaging);
    }

    // =====================================================================
    // CATALOGUES: DRUGS & DIAGNOSTIC SERVICES PAGE
    // =====================================================================
    private void loadCataloguesPage() {
        loadDrugs();
        loadServices();
    }

    private void loadDrugs() {
        DefaultTableModel model = (DefaultTableModel) tblDrugs.getModel();
        model.setRowCount(0);
        for (Object[] row : admin.getDrugCatalogue()) {
            model.addRow(row);
        }
    }

    private void btnAddDrugActionPerformed(java.awt.event.ActionEvent evt) {
        CatalogueItemDialog dialog = new CatalogueItemDialog(this, admin, "drug", null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadDrugs();
        }
    }

    private void btnEditDrugActionPerformed(java.awt.event.ActionEvent evt) {
        int row = tblDrugs.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a drug from the list.");
            return;
        }
        Object[] existing = new Object[]{
            tblDrugs.getValueAt(row, 0), tblDrugs.getValueAt(row, 1),
            tblDrugs.getValueAt(row, 2), tblDrugs.getValueAt(row, 3)
        };
        CatalogueItemDialog dialog = new CatalogueItemDialog(this, admin, "drug", existing);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadDrugs();
        }
    }

    private void btnToggleDrugActionPerformed(java.awt.event.ActionEvent evt) {
        int row = tblDrugs.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a drug from the list.");
            return;
        }
        int drugId = (Integer) tblDrugs.getValueAt(row, 0);
        String result = admin.toggleDrugStatus(drugId);
        if (result != null) {
            JOptionPane.showMessageDialog(this, result);
            return;
        }
        loadDrugs();
    }

    private void loadServices() {
        DefaultTableModel model = (DefaultTableModel) tblServices.getModel();
        model.setRowCount(0);
        for (Object[] row : admin.getServiceCatalogue()) {
            model.addRow(row);
        }
    }

    private void btnAddServiceActionPerformed(java.awt.event.ActionEvent evt) {
        CatalogueItemDialog dialog = new CatalogueItemDialog(this, admin, "service", null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadServices();
        }
    }

    private void btnEditServiceActionPerformed(java.awt.event.ActionEvent evt) {
        int row = tblServices.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a service from the list.");
            return;
        }
        Object[] existing = new Object[]{
            tblServices.getValueAt(row, 0), tblServices.getValueAt(row, 1),
            tblServices.getValueAt(row, 2), tblServices.getValueAt(row, 3), tblServices.getValueAt(row, 4)
        };
        CatalogueItemDialog dialog = new CatalogueItemDialog(this, admin, "service", existing);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadServices();
        }
    }

    private void btnToggleServiceActionPerformed(java.awt.event.ActionEvent evt) {
        int row = tblServices.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a service from the list.");
            return;
        }
        int serviceId = (Integer) tblServices.getValueAt(row, 0);
        String result = admin.toggleServiceStatus(serviceId);
        if (result != null) {
            JOptionPane.showMessageDialog(this, result);
            return;
        }
        loadServices();
    }

    private void btnCatSubDrugsActionPerformed(java.awt.event.ActionEvent evt) {
        showCatSubPage("drugs", btnCatSubDrugs);
    }

    private void btnCatSubServicesActionPerformed(java.awt.event.ActionEvent evt) {
        showCatSubPage("services", btnCatSubServices);
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
        btnNavWards = new javax.swing.JButton();
        btnNavAdmissions = new javax.swing.JButton();
        btnNavDiagnostics = new javax.swing.JButton();
        btnNavCatalogues = new javax.swing.JButton();
        btnNavBilling = new javax.swing.JButton();
        btnLogout = new javax.swing.JButton();
        pnlContent = new javax.swing.JPanel();

        pnlUsers = new javax.swing.JPanel();
        lblUsersTitle = new javax.swing.JLabel();
        lblUserSearch = new javax.swing.JLabel();
        txtUserSearch = new javax.swing.JTextField();
        lblRoleFilter = new javax.swing.JLabel();
        cmbRoleFilter = new javax.swing.JComboBox<>();
        btnSearchUsers = new javax.swing.JButton();
        scrUsers = new javax.swing.JScrollPane();
        tblUsers = new javax.swing.JTable();
        btnNewUser = new javax.swing.JButton();
        btnEditUser = new javax.swing.JButton();
        btnToggleUserStatus = new javax.swing.JButton();

        pnlAssign = new javax.swing.JPanel();
        lblAssignTitle = new javax.swing.JLabel();
        scrAssignments = new javax.swing.JScrollPane();
        tblAssignments = new javax.swing.JTable();
        btnReassignDept = new javax.swing.JButton();

        pnlAssets = new javax.swing.JPanel();
        lblAssetsTitle = new javax.swing.JLabel();
        lblAssetCategory = new javax.swing.JLabel();
        cmbAssetCategory = new javax.swing.JComboBox<>();
        scrAssets = new javax.swing.JScrollPane();
        tblAssets = new javax.swing.JTable();
        btnAddAsset = new javax.swing.JButton();
        btnToggleAssetStatus = new javax.swing.JButton();

        pnlWards = new javax.swing.JPanel();
        lblWardsTitle = new javax.swing.JLabel();
        scrWards = new javax.swing.JScrollPane();
        tblWards = new javax.swing.JTable();
        btnAddWard = new javax.swing.JButton();
        btnEditWard = new javax.swing.JButton();
        btnManageBeds = new javax.swing.JButton();

        pnlAdmissions = new javax.swing.JPanel();
        lblAdmissionsTitle = new javax.swing.JLabel();
        scrAdmissions = new javax.swing.JScrollPane();
        tblAdmissions = new javax.swing.JTable();
        btnNewAdmission = new javax.swing.JButton();
        btnDischarge = new javax.swing.JButton();

        pnlDiagnostics = new javax.swing.JPanel();
        lblDiagTitle = new javax.swing.JLabel();
        btnDiagSubLab = new javax.swing.JButton();
        btnDiagSubImaging = new javax.swing.JButton();
        pnlDiagContent = new javax.swing.JPanel();
        pnlDiagLab = new javax.swing.JPanel();
        scrLabRequests = new javax.swing.JScrollPane();
        tblLabRequests = new javax.swing.JTable();
        btnEnterLabResult = new javax.swing.JButton();
        pnlDiagImaging = new javax.swing.JPanel();
        scrImagingRequests = new javax.swing.JScrollPane();
        tblImagingRequests = new javax.swing.JTable();
        btnScheduleSelected = new javax.swing.JButton();

        pnlCatalogues = new javax.swing.JPanel();
        lblCatTitle = new javax.swing.JLabel();
        btnCatSubDrugs = new javax.swing.JButton();
        btnCatSubServices = new javax.swing.JButton();
        pnlCatContent = new javax.swing.JPanel();
        pnlCatDrugs = new javax.swing.JPanel();
        scrDrugs = new javax.swing.JScrollPane();
        tblDrugs = new javax.swing.JTable();
        btnAddDrug = new javax.swing.JButton();
        btnEditDrug = new javax.swing.JButton();
        btnToggleDrug = new javax.swing.JButton();
        pnlCatServices = new javax.swing.JPanel();
        scrServices = new javax.swing.JScrollPane();
        tblServices = new javax.swing.JTable();
        btnAddService = new javax.swing.JButton();
        btnEditService = new javax.swing.JButton();
        btnToggleService = new javax.swing.JButton();

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

        btnNavUsers.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnNavUsers.setForeground(new java.awt.Color(38, 117, 154));
        btnNavUsers.setText("Manage Users");
        btnNavUsers.setContentAreaFilled(false);
        btnNavUsers.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnNavUsers.setFocusPainted(false);
        btnNavUsers.setRolloverEnabled(false);
        btnNavUsers.addActionListener(this::btnNavUsersActionPerformed);
        pnlNav.add(btnNavUsers);
        btnNavUsers.setBounds(10, 20, 160, 36);

        btnNavAssign.setBackground(new java.awt.Color(30, 95, 125));
        btnNavAssign.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnNavAssign.setForeground(new java.awt.Color(255, 255, 255));
        btnNavAssign.setText("Assign Doctors");
        btnNavAssign.setContentAreaFilled(false);
        btnNavAssign.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnNavAssign.setFocusPainted(false);
        btnNavAssign.setRolloverEnabled(false);
        btnNavAssign.addActionListener(this::btnNavAssignActionPerformed);
        pnlNav.add(btnNavAssign);
        btnNavAssign.setBounds(10, 62, 160, 36);

        btnNavAssets.setBackground(new java.awt.Color(30, 95, 125));
        btnNavAssets.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnNavAssets.setForeground(new java.awt.Color(255, 255, 255));
        btnNavAssets.setText("Hospital Assets");
        btnNavAssets.setContentAreaFilled(false);
        btnNavAssets.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnNavAssets.setFocusPainted(false);
        btnNavAssets.setRolloverEnabled(false);
        btnNavAssets.addActionListener(this::btnNavAssetsActionPerformed);
        pnlNav.add(btnNavAssets);
        btnNavAssets.setBounds(10, 104, 160, 36);

        btnNavWards.setBackground(new java.awt.Color(30, 95, 125));
        btnNavWards.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnNavWards.setForeground(new java.awt.Color(255, 255, 255));
        btnNavWards.setText("Wards & Beds");
        btnNavWards.setContentAreaFilled(false);
        btnNavWards.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnNavWards.setFocusPainted(false);
        btnNavWards.setRolloverEnabled(false);
        btnNavWards.addActionListener(this::btnNavWardsActionPerformed);
        pnlNav.add(btnNavWards);
        btnNavWards.setBounds(10, 146, 160, 36);

        btnNavAdmissions.setBackground(new java.awt.Color(30, 95, 125));
        btnNavAdmissions.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnNavAdmissions.setForeground(new java.awt.Color(255, 255, 255));
        btnNavAdmissions.setText("Admissions");
        btnNavAdmissions.setContentAreaFilled(false);
        btnNavAdmissions.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnNavAdmissions.setFocusPainted(false);
        btnNavAdmissions.setRolloverEnabled(false);
        btnNavAdmissions.addActionListener(this::btnNavAdmissionsActionPerformed);
        pnlNav.add(btnNavAdmissions);
        btnNavAdmissions.setBounds(10, 188, 160, 36);

        btnNavDiagnostics.setBackground(new java.awt.Color(30, 95, 125));
        btnNavDiagnostics.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnNavDiagnostics.setForeground(new java.awt.Color(255, 255, 255));
        btnNavDiagnostics.setText("Diagnostics");
        btnNavDiagnostics.setContentAreaFilled(false);
        btnNavDiagnostics.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnNavDiagnostics.setFocusPainted(false);
        btnNavDiagnostics.setRolloverEnabled(false);
        btnNavDiagnostics.addActionListener(this::btnNavDiagnosticsActionPerformed);
        pnlNav.add(btnNavDiagnostics);
        btnNavDiagnostics.setBounds(10, 230, 160, 36);

        btnNavCatalogues.setBackground(new java.awt.Color(30, 95, 125));
        btnNavCatalogues.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnNavCatalogues.setForeground(new java.awt.Color(255, 255, 255));
        btnNavCatalogues.setText("Catalogues");
        btnNavCatalogues.setContentAreaFilled(false);
        btnNavCatalogues.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnNavCatalogues.setFocusPainted(false);
        btnNavCatalogues.setRolloverEnabled(false);
        btnNavCatalogues.addActionListener(this::btnNavCataloguesActionPerformed);
        pnlNav.add(btnNavCatalogues);
        btnNavCatalogues.setBounds(10, 272, 160, 36);

        btnNavBilling.setBackground(new java.awt.Color(30, 95, 125));
        btnNavBilling.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnNavBilling.setForeground(new java.awt.Color(255, 255, 255));
        btnNavBilling.setText("Rates & Insurance");
        btnNavBilling.setContentAreaFilled(false);
        btnNavBilling.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnNavBilling.setFocusPainted(false);
        btnNavBilling.setRolloverEnabled(false);
        btnNavBilling.addActionListener(this::btnNavBillingActionPerformed);
        pnlNav.add(btnNavBilling);
        btnNavBilling.setBounds(10, 314, 160, 36);

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
        btnLogout.setBounds(10, 480, 160, 40);

        getContentPane().add(pnlNav);
        pnlNav.setBounds(0, 50, 180, 550);

        pnlContent.setBackground(new java.awt.Color(255, 255, 255));
        pnlContent.setLayout(new java.awt.CardLayout());

        // ---------------- Manage Users ----------------
        pnlUsers.setBackground(new java.awt.Color(255, 255, 255));
        pnlUsers.setLayout(null);

        lblUsersTitle.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblUsersTitle.setText("Manage End Users");
        pnlUsers.add(lblUsersTitle);
        lblUsersTitle.setBounds(20, 15, 400, 30);

        lblUserSearch.setText("Search (ID or Name):");
        pnlUsers.add(lblUserSearch);
        lblUserSearch.setBounds(20, 55, 150, 20);

        pnlUsers.add(txtUserSearch);
        txtUserSearch.setBounds(180, 53, 180, 26);

        lblRoleFilter.setText("Role:");
        pnlUsers.add(lblRoleFilter);
        lblRoleFilter.setBounds(370, 55, 40, 20);

        cmbRoleFilter.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] {}));
        pnlUsers.add(cmbRoleFilter);
        cmbRoleFilter.setBounds(410, 53, 130, 26);

        btnSearchUsers.setBackground(new java.awt.Color(38, 117, 154));
        btnSearchUsers.setForeground(new java.awt.Color(255, 255, 255));
        btnSearchUsers.setText("Search");
        btnSearchUsers.addActionListener(this::btnSearchUsersActionPerformed);
        pnlUsers.add(btnSearchUsers);
        btnSearchUsers.setBounds(550, 53, 50, 26);

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
        scrUsers.setBounds(20, 90, 580, 330);

        btnNewUser.setBackground(new java.awt.Color(38, 117, 154));
        btnNewUser.setForeground(new java.awt.Color(255, 255, 255));
        btnNewUser.setText("New User");
        btnNewUser.addActionListener(this::btnNewUserActionPerformed);
        pnlUsers.add(btnNewUser);
        btnNewUser.setBounds(20, 430, 120, 36);

        btnEditUser.setBackground(new java.awt.Color(38, 117, 154));
        btnEditUser.setForeground(new java.awt.Color(255, 255, 255));
        btnEditUser.setText("Edit Selected");
        btnEditUser.addActionListener(this::btnEditUserActionPerformed);
        pnlUsers.add(btnEditUser);
        btnEditUser.setBounds(150, 430, 140, 36);

        btnToggleUserStatus.setBackground(new java.awt.Color(38, 117, 154));
        btnToggleUserStatus.setForeground(new java.awt.Color(255, 255, 255));
        btnToggleUserStatus.setText("Activate / Deactivate Selected");
        btnToggleUserStatus.addActionListener(this::btnToggleUserStatusActionPerformed);
        pnlUsers.add(btnToggleUserStatus);
        btnToggleUserStatus.setBounds(300, 430, 280, 36);

        pnlContent.add(pnlUsers, "users");

        // ---------------- Assign Doctors ----------------
        pnlAssign.setBackground(new java.awt.Color(255, 255, 255));
        pnlAssign.setLayout(null);

        lblAssignTitle.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblAssignTitle.setText("Assign Doctors to Medical Managers");
        pnlAssign.add(lblAssignTitle);
        lblAssignTitle.setBounds(20, 15, 500, 30);

        tblAssignments.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new String [] {
                "Doctor ID", "Doctor Name", "Department", "Medical Manager"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
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
        scrAssignments.setBounds(20, 60, 580, 420);

        btnReassignDept.setBackground(new java.awt.Color(38, 117, 154));
        btnReassignDept.setForeground(new java.awt.Color(255, 255, 255));
        btnReassignDept.setText("Reassign Selected");
        btnReassignDept.addActionListener(this::btnReassignDeptActionPerformed);
        pnlAssign.add(btnReassignDept);
        btnReassignDept.setBounds(20, 490, 200, 36);

        pnlContent.add(pnlAssign, "assign");

        // ---------------- Hospital Assets ----------------
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
        scrAssets.setBounds(20, 96, 580, 384);

        btnAddAsset.setBackground(new java.awt.Color(38, 117, 154));
        btnAddAsset.setForeground(new java.awt.Color(255, 255, 255));
        btnAddAsset.setText("Add New");
        btnAddAsset.addActionListener(this::btnAddAssetActionPerformed);
        pnlAssets.add(btnAddAsset);
        btnAddAsset.setBounds(20, 490, 120, 36);

        btnToggleAssetStatus.setBackground(new java.awt.Color(38, 117, 154));
        btnToggleAssetStatus.setForeground(new java.awt.Color(255, 255, 255));
        btnToggleAssetStatus.setText("Toggle Status (OK / Maintenance)");
        btnToggleAssetStatus.addActionListener(this::btnToggleAssetStatusActionPerformed);
        pnlAssets.add(btnToggleAssetStatus);
        btnToggleAssetStatus.setBounds(150, 490, 280, 36);

        pnlContent.add(pnlAssets, "assets");

        // ---------------- Wards & Beds ----------------
        pnlWards.setBackground(new java.awt.Color(255, 255, 255));
        pnlWards.setLayout(null);

        lblWardsTitle.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblWardsTitle.setText("Wards & Beds");
        pnlWards.add(lblWardsTitle);
        lblWardsTitle.setBounds(20, 15, 400, 30);

        tblWards.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new String [] {
                "Ward ID", "Department", "Gender", "Capacity", "Beds"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblWards.getTableHeader().setReorderingAllowed(false);
        tblWards.setSelectionBackground(new java.awt.Color(38, 117, 154));
        tblWards.setSelectionForeground(new java.awt.Color(255, 255, 255));
        scrWards.setViewportView(tblWards);
        pnlWards.add(scrWards);
        scrWards.setBounds(20, 60, 580, 420);

        btnAddWard.setBackground(new java.awt.Color(38, 117, 154));
        btnAddWard.setForeground(new java.awt.Color(255, 255, 255));
        btnAddWard.setText("Add Ward");
        btnAddWard.addActionListener(this::btnAddWardActionPerformed);
        pnlWards.add(btnAddWard);
        btnAddWard.setBounds(20, 490, 110, 36);

        btnEditWard.setBackground(new java.awt.Color(38, 117, 154));
        btnEditWard.setForeground(new java.awt.Color(255, 255, 255));
        btnEditWard.setText("Edit Selected Ward");
        btnEditWard.addActionListener(this::btnEditWardActionPerformed);
        pnlWards.add(btnEditWard);
        btnEditWard.setBounds(140, 490, 170, 36);

        btnManageBeds.setBackground(new java.awt.Color(38, 117, 154));
        btnManageBeds.setForeground(new java.awt.Color(255, 255, 255));
        btnManageBeds.setText("Manage Beds");
        btnManageBeds.addActionListener(this::btnManageBedsActionPerformed);
        pnlWards.add(btnManageBeds);
        btnManageBeds.setBounds(320, 490, 150, 36);

        pnlContent.add(pnlWards, "wards");

        // ---------------- Admissions ----------------
        pnlAdmissions.setBackground(new java.awt.Color(255, 255, 255));
        pnlAdmissions.setLayout(null);

        lblAdmissionsTitle.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblAdmissionsTitle.setText("Admissions");
        pnlAdmissions.add(lblAdmissionsTitle);
        lblAdmissionsTitle.setBounds(20, 15, 400, 30);

        tblAdmissions.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new String [] {
                "ID", "Patient", "Bed", "Admitted", "Discharged", "Remarks", "Status"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblAdmissions.getTableHeader().setReorderingAllowed(false);
        tblAdmissions.setSelectionBackground(new java.awt.Color(38, 117, 154));
        tblAdmissions.setSelectionForeground(new java.awt.Color(255, 255, 255));
        scrAdmissions.setViewportView(tblAdmissions);
        pnlAdmissions.add(scrAdmissions);
        scrAdmissions.setBounds(20, 60, 580, 420);

        btnNewAdmission.setBackground(new java.awt.Color(38, 117, 154));
        btnNewAdmission.setForeground(new java.awt.Color(255, 255, 255));
        btnNewAdmission.setText("New Admission");
        btnNewAdmission.addActionListener(this::btnNewAdmissionActionPerformed);
        pnlAdmissions.add(btnNewAdmission);
        btnNewAdmission.setBounds(20, 490, 150, 36);

        btnDischarge.setBackground(new java.awt.Color(38, 117, 154));
        btnDischarge.setForeground(new java.awt.Color(255, 255, 255));
        btnDischarge.setText("Discharge Selected");
        btnDischarge.addActionListener(this::btnDischargeActionPerformed);
        pnlAdmissions.add(btnDischarge);
        btnDischarge.setBounds(180, 490, 200, 36);

        pnlContent.add(pnlAdmissions, "admissions");

        // ---------------- Diagnostics ----------------
        pnlDiagnostics.setBackground(new java.awt.Color(255, 255, 255));
        pnlDiagnostics.setLayout(null);

        lblDiagTitle.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblDiagTitle.setText("Diagnostics");
        pnlDiagnostics.add(lblDiagTitle);
        lblDiagTitle.setBounds(20, 15, 400, 30);

        btnDiagSubLab.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnDiagSubLab.setText("Lab Results");
        btnDiagSubLab.addActionListener(this::btnDiagSubLabActionPerformed);
        pnlDiagnostics.add(btnDiagSubLab);
        btnDiagSubLab.setBounds(20, 50, 140, 30);

        btnDiagSubImaging.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnDiagSubImaging.setText("Schedule Imaging");
        btnDiagSubImaging.addActionListener(this::btnDiagSubImagingActionPerformed);
        pnlDiagnostics.add(btnDiagSubImaging);
        btnDiagSubImaging.setBounds(170, 50, 170, 30);

        pnlDiagContent.setLayout(new java.awt.CardLayout());

        pnlDiagLab.setLayout(null);

        tblLabRequests.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new String [] {
                "Request ID", "Service", "Patient", "Requested", "Remarks"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblLabRequests.getTableHeader().setReorderingAllowed(false);
        tblLabRequests.setSelectionBackground(new java.awt.Color(38, 117, 154));
        tblLabRequests.setSelectionForeground(new java.awt.Color(255, 255, 255));
        scrLabRequests.setViewportView(tblLabRequests);
        pnlDiagLab.add(scrLabRequests);
        scrLabRequests.setBounds(0, 0, 580, 390);

        btnEnterLabResult.setBackground(new java.awt.Color(38, 117, 154));
        btnEnterLabResult.setForeground(new java.awt.Color(255, 255, 255));
        btnEnterLabResult.setText("Enter Result");
        btnEnterLabResult.addActionListener(this::btnEnterLabResultActionPerformed);
        pnlDiagLab.add(btnEnterLabResult);
        btnEnterLabResult.setBounds(0, 400, 150, 36);

        pnlDiagContent.add(pnlDiagLab, "lab");

        pnlDiagImaging.setLayout(null);

        tblImagingRequests.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new String [] {
                "Request ID", "Service", "Patient", "Requested"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblImagingRequests.getTableHeader().setReorderingAllowed(false);
        tblImagingRequests.setSelectionBackground(new java.awt.Color(38, 117, 154));
        tblImagingRequests.setSelectionForeground(new java.awt.Color(255, 255, 255));
        scrImagingRequests.setViewportView(tblImagingRequests);
        pnlDiagImaging.add(scrImagingRequests);
        scrImagingRequests.setBounds(0, 0, 580, 390);

        btnScheduleSelected.setBackground(new java.awt.Color(38, 117, 154));
        btnScheduleSelected.setForeground(new java.awt.Color(255, 255, 255));
        btnScheduleSelected.setText("Schedule Selected");
        btnScheduleSelected.addActionListener(this::btnScheduleSelectedActionPerformed);
        pnlDiagImaging.add(btnScheduleSelected);
        btnScheduleSelected.setBounds(0, 400, 170, 36);

        pnlDiagContent.add(pnlDiagImaging, "imaging");

        pnlDiagnostics.add(pnlDiagContent);
        pnlDiagContent.setBounds(20, 90, 580, 440);

        pnlContent.add(pnlDiagnostics, "diagnostics");

        // ---------------- Catalogues ----------------
        pnlCatalogues.setBackground(new java.awt.Color(255, 255, 255));
        pnlCatalogues.setLayout(null);

        lblCatTitle.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblCatTitle.setText("Catalogues");
        pnlCatalogues.add(lblCatTitle);
        lblCatTitle.setBounds(20, 15, 400, 30);

        btnCatSubDrugs.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnCatSubDrugs.setText("Drug Catalogue");
        btnCatSubDrugs.addActionListener(this::btnCatSubDrugsActionPerformed);
        pnlCatalogues.add(btnCatSubDrugs);
        btnCatSubDrugs.setBounds(20, 50, 150, 30);

        btnCatSubServices.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnCatSubServices.setText("Diagnostic Services");
        btnCatSubServices.addActionListener(this::btnCatSubServicesActionPerformed);
        pnlCatalogues.add(btnCatSubServices);
        btnCatSubServices.setBounds(180, 50, 180, 30);

        pnlCatContent.setLayout(new java.awt.CardLayout());

        pnlCatDrugs.setLayout(null);

        tblDrugs.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new String [] {
                "ID", "Name", "Form", "Price", "Status"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblDrugs.getTableHeader().setReorderingAllowed(false);
        tblDrugs.setSelectionBackground(new java.awt.Color(38, 117, 154));
        tblDrugs.setSelectionForeground(new java.awt.Color(255, 255, 255));
        scrDrugs.setViewportView(tblDrugs);
        pnlCatDrugs.add(scrDrugs);
        scrDrugs.setBounds(0, 0, 580, 390);

        btnAddDrug.setBackground(new java.awt.Color(38, 117, 154));
        btnAddDrug.setForeground(new java.awt.Color(255, 255, 255));
        btnAddDrug.setText("Add New");
        btnAddDrug.addActionListener(this::btnAddDrugActionPerformed);
        pnlCatDrugs.add(btnAddDrug);
        btnAddDrug.setBounds(0, 400, 100, 36);

        btnEditDrug.setBackground(new java.awt.Color(38, 117, 154));
        btnEditDrug.setForeground(new java.awt.Color(255, 255, 255));
        btnEditDrug.setText("Edit Selected");
        btnEditDrug.addActionListener(this::btnEditDrugActionPerformed);
        pnlCatDrugs.add(btnEditDrug);
        btnEditDrug.setBounds(110, 400, 140, 36);

        btnToggleDrug.setBackground(new java.awt.Color(38, 117, 154));
        btnToggleDrug.setForeground(new java.awt.Color(255, 255, 255));
        btnToggleDrug.setText("Toggle Active / Inactive");
        btnToggleDrug.addActionListener(this::btnToggleDrugActionPerformed);
        pnlCatDrugs.add(btnToggleDrug);
        btnToggleDrug.setBounds(260, 400, 220, 36);

        pnlCatContent.add(pnlCatDrugs, "drugs");

        pnlCatServices.setLayout(null);

        tblServices.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new String [] {
                "ID", "Name", "Category", "Type", "Price", "Status"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblServices.getTableHeader().setReorderingAllowed(false);
        tblServices.setSelectionBackground(new java.awt.Color(38, 117, 154));
        tblServices.setSelectionForeground(new java.awt.Color(255, 255, 255));
        scrServices.setViewportView(tblServices);
        pnlCatServices.add(scrServices);
        scrServices.setBounds(0, 0, 580, 390);

        btnAddService.setBackground(new java.awt.Color(38, 117, 154));
        btnAddService.setForeground(new java.awt.Color(255, 255, 255));
        btnAddService.setText("Add New");
        btnAddService.addActionListener(this::btnAddServiceActionPerformed);
        pnlCatServices.add(btnAddService);
        btnAddService.setBounds(0, 400, 100, 36);

        btnEditService.setBackground(new java.awt.Color(38, 117, 154));
        btnEditService.setForeground(new java.awt.Color(255, 255, 255));
        btnEditService.setText("Edit Selected");
        btnEditService.addActionListener(this::btnEditServiceActionPerformed);
        pnlCatServices.add(btnEditService);
        btnEditService.setBounds(110, 400, 140, 36);

        btnToggleService.setBackground(new java.awt.Color(38, 117, 154));
        btnToggleService.setForeground(new java.awt.Color(255, 255, 255));
        btnToggleService.setText("Toggle Active / Inactive");
        btnToggleService.addActionListener(this::btnToggleServiceActionPerformed);
        pnlCatServices.add(btnToggleService);
        btnToggleService.setBounds(260, 400, 220, 36);

        pnlCatContent.add(pnlCatServices, "services");

        pnlCatalogues.add(pnlCatContent);
        pnlCatContent.setBounds(20, 90, 580, 440);

        pnlContent.add(pnlCatalogues, "catalogues");

        // ---------------- Rates & Insurance ----------------
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

    private void btnNavWardsActionPerformed(java.awt.event.ActionEvent evt) {
        showPage("wards", btnNavWards);
    }

    private void btnNavAdmissionsActionPerformed(java.awt.event.ActionEvent evt) {
        showPage("admissions", btnNavAdmissions);
    }

    private void btnNavDiagnosticsActionPerformed(java.awt.event.ActionEvent evt) {
        showPage("diagnostics", btnNavDiagnostics);
    }

    private void btnNavCataloguesActionPerformed(java.awt.event.ActionEvent evt) {
        showPage("catalogues", btnNavCatalogues);
    }

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
    private javax.swing.JButton btnAddAsset;
    private javax.swing.JButton btnAddDrug;
    private javax.swing.JButton btnAddInsurance;
    private javax.swing.JButton btnAddService;
    private javax.swing.JButton btnAddWard;
    private javax.swing.JButton btnCatSubDrugs;
    private javax.swing.JButton btnCatSubServices;
    private javax.swing.JButton btnDiagSubImaging;
    private javax.swing.JButton btnDiagSubLab;
    private javax.swing.JButton btnDischarge;
    private javax.swing.JButton btnEditDrug;
    private javax.swing.JButton btnEditService;
    private javax.swing.JButton btnEditUser;
    private javax.swing.JButton btnEditWard;
    private javax.swing.JButton btnEnterLabResult;
    private javax.swing.JButton btnLogout;
    private javax.swing.JButton btnManageBeds;
    private javax.swing.JButton btnNavAdmissions;
    private javax.swing.JButton btnNavAssets;
    private javax.swing.JButton btnNavAssign;
    private javax.swing.JButton btnNavBilling;
    private javax.swing.JButton btnNavCatalogues;
    private javax.swing.JButton btnNavDiagnostics;
    private javax.swing.JButton btnNavUsers;
    private javax.swing.JButton btnNavWards;
    private javax.swing.JButton btnNewAdmission;
    private javax.swing.JButton btnNewUser;
    private javax.swing.JButton btnReassignDept;
    private javax.swing.JButton btnRemoveInsurance;
    private javax.swing.JButton btnSaveFees;
    private javax.swing.JButton btnScheduleSelected;
    private javax.swing.JButton btnSearchUsers;
    private javax.swing.JButton btnToggleAssetStatus;
    private javax.swing.JButton btnToggleDrug;
    private javax.swing.JButton btnToggleService;
    private javax.swing.JButton btnToggleUserStatus;
    private javax.swing.JButton btnUpdateMultiplier;
    private javax.swing.JComboBox<String> cmbAssetCategory;
    private javax.swing.JComboBox<String> cmbRoleFilter;
    private javax.swing.JLabel lblAdmissionsTitle;
    private javax.swing.JLabel lblAssetCategory;
    private javax.swing.JLabel lblAssetsTitle;
    private javax.swing.JLabel lblAssignTitle;
    private javax.swing.JLabel lblBillingTitle;
    private javax.swing.JLabel lblCatTitle;
    private javax.swing.JLabel lblConsultFee;
    private javax.swing.JLabel lblDiagTitle;
    private javax.swing.JLabel lblFeesHeader;
    private javax.swing.JLabel lblHospFee;
    private javax.swing.JLabel lblInsuranceHeader;
    private javax.swing.JLabel lblNewMultiplier;
    private javax.swing.JLabel lblPortalTitle;
    private javax.swing.JLabel lblRoleFilter;
    private javax.swing.JLabel lblTiersHeader;
    private javax.swing.JLabel lblUserSearch;
    private javax.swing.JLabel lblUsersTitle;
    private javax.swing.JLabel lblWardsTitle;
    private javax.swing.JLabel lblWelcome;
    private javax.swing.JPanel pnlAdmissions;
    private javax.swing.JPanel pnlAssets;
    private javax.swing.JPanel pnlAssign;
    private javax.swing.JPanel pnlBilling;
    private javax.swing.JPanel pnlCatContent;
    private javax.swing.JPanel pnlCatDrugs;
    private javax.swing.JPanel pnlCatServices;
    private javax.swing.JPanel pnlCatalogues;
    private javax.swing.JPanel pnlContent;
    private javax.swing.JPanel pnlDiagContent;
    private javax.swing.JPanel pnlDiagImaging;
    private javax.swing.JPanel pnlDiagLab;
    private javax.swing.JPanel pnlDiagnostics;
    private javax.swing.JPanel pnlHeader;
    private javax.swing.JPanel pnlNav;
    private javax.swing.JPanel pnlUsers;
    private javax.swing.JPanel pnlWards;
    private javax.swing.JScrollPane scrAdmissions;
    private javax.swing.JScrollPane scrAssets;
    private javax.swing.JScrollPane scrAssignments;
    private javax.swing.JScrollPane scrDrugs;
    private javax.swing.JScrollPane scrImagingRequests;
    private javax.swing.JScrollPane scrInsurance;
    private javax.swing.JScrollPane scrLabRequests;
    private javax.swing.JScrollPane scrServices;
    private javax.swing.JScrollPane scrTiers;
    private javax.swing.JScrollPane scrUsers;
    private javax.swing.JScrollPane scrWards;
    private javax.swing.JTable tblAdmissions;
    private javax.swing.JTable tblAssets;
    private javax.swing.JTable tblAssignments;
    private javax.swing.JTable tblDrugs;
    private javax.swing.JTable tblImagingRequests;
    private javax.swing.JTable tblInsurance;
    private javax.swing.JTable tblLabRequests;
    private javax.swing.JTable tblServices;
    private javax.swing.JTable tblTiers;
    private javax.swing.JTable tblUsers;
    private javax.swing.JTable tblWards;
    private javax.swing.JTextField txtConsultFee;
    private javax.swing.JTextField txtHospFee;
    private javax.swing.JTextField txtInsuranceName;
    private javax.swing.JTextField txtMultiplier;
    private javax.swing.JTextField txtUserSearch;
    // End of variables declaration//GEN-END:variables
}
