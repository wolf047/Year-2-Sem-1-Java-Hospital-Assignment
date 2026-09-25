/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package Admin;

import HelperFunction.SessionUser;
import Users.UserLogin;
import java.awt.CardLayout;
import java.awt.Color;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private final Color NAV_BG = new Color(30, 95, 125);
    private final Color BLUE = new Color(38, 117, 154);

    private AdminServices admin;

    // combo selections are matched back to ids by list index
    private final ArrayList<Integer> deptAssignIds = new ArrayList<>();
    private final ArrayList<Integer> wardDeptIds = new ArrayList<>();
    private final ArrayList<Integer> caseIds = new ArrayList<>();
    private final ArrayList<Integer> bedIds = new ArrayList<>();
    private final ArrayList<Integer> imagingRoomIds = new ArrayList<>();

    private void populateCombo(javax.swing.JComboBox<String> combo, ArrayList<Integer> idsOut, ArrayList<Object[]> rows) {
        idsOut.clear();
        ArrayList<String> labels = new ArrayList<>();
        for (Object[] row : rows) {
            idsOut.add((Integer) row[0]);
            labels.add((String) row[1]);
        }
        combo.setModel(new DefaultComboBoxModel<>(labels.toArray(new String[0])));
    }

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
        cmbWardGender.setModel(new DefaultComboBoxModel<>(new String[]{"male", "female"}));
        cmbServiceCategory.setModel(new DefaultComboBoxModel<>(new String[]{"laboratory", "imaging"}));
        cmbDrugForm.setModel(new DefaultComboBoxModel<>(admin.getDrugForms().toArray(new String[0])));

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

        tblWards.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                refreshBedsTable();
            }
        });
        tblImagingRequests.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                refreshImagingRoomCombo();
            }
        });
        tblDrugs.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                fillDrugFieldsFromSelection();
            }
        });
        tblServices.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                fillServiceFieldsFromSelection();
            }
        });
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
        populateCombo(cmbDeptAssign, deptAssignIds, admin.getAssignableDepartments());
    }

    private void btnAssignDeptActionPerformed(java.awt.event.ActionEvent evt) {
        int row = tblAssignments.getSelectedRow();
        int deptIndex = cmbDeptAssign.getSelectedIndex();
        if (row < 0 || deptIndex < 0) {
            JOptionPane.showMessageDialog(this, "Please select a doctor and a department.");
            return;
        }
        int doctorId = (Integer) tblAssignments.getValueAt(row, 0);
        String result = admin.assignDoctorToDepartment(doctorId, deptAssignIds.get(deptIndex));
        if (result != null) {
            JOptionPane.showMessageDialog(this, result);
            return;
        }
        loadAssignPage();
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
        ArrayList<String> types = admin.getAssetTypes(category);
        cmbNewAssetType.setModel(new DefaultComboBoxModel<>(types.toArray(new String[0])));
        cmbNewAssetType.setEnabled(!types.isEmpty());
    }

    private void cmbAssetCategoryActionPerformed(java.awt.event.ActionEvent evt) {
        refreshAssetsTable();
    }

    private void btnAddAssetActionPerformed(java.awt.event.ActionEvent evt) {
        String category = (String) cmbAssetCategory.getSelectedItem();
        String type = (String) cmbNewAssetType.getSelectedItem();
        String result = admin.addAsset(category, type);
        if (result != null) {
            JOptionPane.showMessageDialog(this, result);
            return;
        }
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
    // WARDS & BEDS PAGE
    // =====================================================================
    private void loadWardsPage() {
        DefaultTableModel model = (DefaultTableModel) tblWards.getModel();
        model.setRowCount(0);
        for (Object[] row : admin.getWards()) {
            model.addRow(row);
        }
        populateCombo(cmbWardDept, wardDeptIds, admin.getDepartments());
        refreshBedsTable();
    }

    private void refreshBedsTable() {
        DefaultTableModel model = (DefaultTableModel) tblBeds.getModel();
        model.setRowCount(0);
        int row = tblWards.getSelectedRow();
        if (row < 0) {
            return;
        }
        int wardId = (Integer) tblWards.getValueAt(row, 0);
        for (Object[] r : admin.getBeds(wardId)) {
            model.addRow(r);
        }
    }

    private void btnAddWardActionPerformed(java.awt.event.ActionEvent evt) {
        int deptIndex = cmbWardDept.getSelectedIndex();
        if (deptIndex < 0) {
            JOptionPane.showMessageDialog(this, "Please select a department.");
            return;
        }
        String gender = (String) cmbWardGender.getSelectedItem();
        int capacity;
        try {
            capacity = Integer.parseInt(txtWardCapacity.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid capacity.");
            return;
        }
        String result = admin.addWard(wardDeptIds.get(deptIndex), gender, capacity);
        if (result != null) {
            JOptionPane.showMessageDialog(this, result);
            return;
        }
        txtWardCapacity.setText("");
        loadWardsPage();
    }

    private void btnUpdateWardActionPerformed(java.awt.event.ActionEvent evt) {
        int row = tblWards.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a ward from the list.");
            return;
        }
        String gender = (String) cmbWardGender.getSelectedItem();
        int capacity;
        try {
            capacity = Integer.parseInt(txtWardCapacity.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid capacity.");
            return;
        }
        int wardId = (Integer) tblWards.getValueAt(row, 0);
        String result = admin.updateWard(wardId, gender, capacity);
        if (result != null) {
            JOptionPane.showMessageDialog(this, result);
            return;
        }
        txtWardCapacity.setText("");
        loadWardsPage();
    }

    private void btnAddBedActionPerformed(java.awt.event.ActionEvent evt) {
        int row = tblWards.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a ward from the list.");
            return;
        }
        int wardId = (Integer) tblWards.getValueAt(row, 0);
        String result = admin.addBed(wardId);
        if (result != null) {
            JOptionPane.showMessageDialog(this, result);
            return;
        }
        loadWardsPage();
    }

    private void btnRemoveBedActionPerformed(java.awt.event.ActionEvent evt) {
        int row = tblBeds.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a bed from the list.");
            return;
        }
        int bedId = (Integer) tblBeds.getValueAt(row, 0);
        String result = admin.removeBed(bedId);
        if (result != null) {
            JOptionPane.showMessageDialog(this, result);
            return;
        }
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
        populateCombo(cmbCase, caseIds, admin.getCasesForAdmission());
        refreshAvailableBedsCombo();
        txtAdmissionDate.setText(LocalDate.now().format(DATE_FORMAT));
        txtAdmissionRemarks.setText("");
    }

    private void refreshAvailableBedsCombo() {
        int caseIndex = cmbCase.getSelectedIndex();
        if (caseIndex < 0) {
            cmbBed.setModel(new DefaultComboBoxModel<>(new String[]{}));
            bedIds.clear();
            return;
        }
        populateCombo(cmbBed, bedIds, admin.getAvailableBeds(caseIds.get(caseIndex)));
    }

    private void cmbCaseActionPerformed(java.awt.event.ActionEvent evt) {
        refreshAvailableBedsCombo();
    }

    private void btnAdmitActionPerformed(java.awt.event.ActionEvent evt) {
        int caseIndex = cmbCase.getSelectedIndex();
        int bedIndex = cmbBed.getSelectedIndex();
        if (caseIndex < 0 || bedIndex < 0) {
            JOptionPane.showMessageDialog(this, "Please select a case and an available bed.");
            return;
        }
        String result = admin.createAdmission(caseIds.get(caseIndex), bedIds.get(bedIndex),
                txtAdmissionDate.getText(), txtAdmissionRemarks.getText());
        if (result != null) {
            JOptionPane.showMessageDialog(this, result);
            return;
        }
        loadAdmissionsPage();
    }

    private void btnDischargeActionPerformed(java.awt.event.ActionEvent evt) {
        int row = tblAdmissions.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an admission from the list.");
            return;
        }
        int admissionId = (Integer) tblAdmissions.getValueAt(row, 0);
        String input = JOptionPane.showInputDialog(this, "Discharge date (dd-MM-yyyy):",
                LocalDate.now().format(DATE_FORMAT));
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
        txtLabResult.setText("");
    }

    private void btnSubmitLabResultActionPerformed(java.awt.event.ActionEvent evt) {
        int row = tblLabRequests.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a pending lab request.");
            return;
        }
        int requestId = (Integer) tblLabRequests.getValueAt(row, 0);
        String result = admin.submitLabResult(requestId, txtLabResult.getText());
        if (result != null) {
            JOptionPane.showMessageDialog(this, result);
            return;
        }
        loadLabRequests();
    }

    private void loadImagingRequests() {
        DefaultTableModel model = (DefaultTableModel) tblImagingRequests.getModel();
        model.setRowCount(0);
        for (Object[] row : admin.getPendingImagingRequests()) {
            model.addRow(row);
        }
        refreshImagingRoomCombo();
        txtImagingDate.setText(LocalDate.now().format(DATE_FORMAT));
        txtImagingStart.setText("");
        txtImagingEnd.setText("");
    }

    private void refreshImagingRoomCombo() {
        int row = tblImagingRequests.getSelectedRow();
        if (row < 0) {
            cmbImagingRoom.setModel(new DefaultComboBoxModel<>(new String[]{}));
            imagingRoomIds.clear();
            return;
        }
        int requestId = (Integer) tblImagingRequests.getValueAt(row, 0);
        populateCombo(cmbImagingRoom, imagingRoomIds, admin.getImagingRoomsForRequest(requestId));
    }

    private void btnScheduleImagingActionPerformed(java.awt.event.ActionEvent evt) {
        int row = tblImagingRequests.getSelectedRow();
        int roomIndex = cmbImagingRoom.getSelectedIndex();
        if (row < 0 || roomIndex < 0) {
            JOptionPane.showMessageDialog(this, "Please select a pending request and an imaging room.");
            return;
        }
        int requestId = (Integer) tblImagingRequests.getValueAt(row, 0);
        String result = admin.scheduleImaging(requestId, imagingRoomIds.get(roomIndex),
                txtImagingDate.getText(), txtImagingStart.getText(), txtImagingEnd.getText());
        if (result != null) {
            JOptionPane.showMessageDialog(this, result);
            return;
        }
        loadImagingRequests();
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
        cmbServiceCategory.setSelectedIndex(0);
        refreshServiceTypeCombo();
        loadServices();
    }

    private void loadDrugs() {
        DefaultTableModel model = (DefaultTableModel) tblDrugs.getModel();
        model.setRowCount(0);
        for (Object[] row : admin.getDrugCatalogue()) {
            model.addRow(row);
        }
    }

    private void fillDrugFieldsFromSelection() {
        int row = tblDrugs.getSelectedRow();
        if (row < 0) {
            return;
        }
        txtDrugName.setText((String) tblDrugs.getValueAt(row, 1));
        cmbDrugForm.setSelectedItem(tblDrugs.getValueAt(row, 2));
        txtDrugPrice.setText(String.valueOf(tblDrugs.getValueAt(row, 3)));
    }

    private void btnAddDrugActionPerformed(java.awt.event.ActionEvent evt) {
        String result = admin.addDrug(txtDrugName.getText(), (String) cmbDrugForm.getSelectedItem(), txtDrugPrice.getText());
        if (result != null) {
            JOptionPane.showMessageDialog(this, result);
            return;
        }
        txtDrugName.setText("");
        txtDrugPrice.setText("");
        loadDrugs();
    }

    private void btnUpdateDrugActionPerformed(java.awt.event.ActionEvent evt) {
        int row = tblDrugs.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a drug from the list.");
            return;
        }
        int drugId = (Integer) tblDrugs.getValueAt(row, 0);
        String result = admin.updateDrug(drugId, txtDrugName.getText(), (String) cmbDrugForm.getSelectedItem(), txtDrugPrice.getText());
        if (result != null) {
            JOptionPane.showMessageDialog(this, result);
            return;
        }
        loadDrugs();
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

    private void refreshServiceTypeCombo() {
        String category = (String) cmbServiceCategory.getSelectedItem();
        ArrayList<String> types = admin.getServiceTypes(category);
        cmbServiceType.setModel(new DefaultComboBoxModel<>(types.toArray(new String[0])));
    }

    private void cmbServiceCategoryActionPerformed(java.awt.event.ActionEvent evt) {
        refreshServiceTypeCombo();
    }

    private void loadServices() {
        DefaultTableModel model = (DefaultTableModel) tblServices.getModel();
        model.setRowCount(0);
        for (Object[] row : admin.getServiceCatalogue()) {
            model.addRow(row);
        }
    }

    private void fillServiceFieldsFromSelection() {
        int row = tblServices.getSelectedRow();
        if (row < 0) {
            return;
        }
        txtServiceName.setText((String) tblServices.getValueAt(row, 1));
        cmbServiceCategory.setSelectedItem(tblServices.getValueAt(row, 2));
        cmbServiceType.setSelectedItem(tblServices.getValueAt(row, 3));
        txtServicePrice.setText(String.valueOf(tblServices.getValueAt(row, 4)));
    }

    private void btnAddServiceActionPerformed(java.awt.event.ActionEvent evt) {
        String result = admin.addService(txtServiceName.getText(), (String) cmbServiceCategory.getSelectedItem(),
                (String) cmbServiceType.getSelectedItem(), txtServicePrice.getText());
        if (result != null) {
            JOptionPane.showMessageDialog(this, result);
            return;
        }
        txtServiceName.setText("");
        txtServicePrice.setText("");
        loadServices();
    }

    private void btnUpdateServiceActionPerformed(java.awt.event.ActionEvent evt) {
        int row = tblServices.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a service from the list.");
            return;
        }
        int serviceId = (Integer) tblServices.getValueAt(row, 0);
        String result = admin.updateService(serviceId, txtServiceName.getText(), (String) cmbServiceCategory.getSelectedItem(),
                (String) cmbServiceType.getSelectedItem(), txtServicePrice.getText());
        if (result != null) {
            JOptionPane.showMessageDialog(this, result);
            return;
        }
        loadServices();
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
        lblAssignNew = new javax.swing.JLabel();
        cmbDeptAssign = new javax.swing.JComboBox<>();
        btnAssignDept = new javax.swing.JButton();

        pnlAssets = new javax.swing.JPanel();
        lblAssetsTitle = new javax.swing.JLabel();
        lblAssetCategory = new javax.swing.JLabel();
        cmbAssetCategory = new javax.swing.JComboBox<>();
        scrAssets = new javax.swing.JScrollPane();
        tblAssets = new javax.swing.JTable();
        lblNewAssetType = new javax.swing.JLabel();
        cmbNewAssetType = new javax.swing.JComboBox<>();
        btnAddAsset = new javax.swing.JButton();
        btnToggleAssetStatus = new javax.swing.JButton();

        pnlWards = new javax.swing.JPanel();
        lblWardsTitle = new javax.swing.JLabel();
        scrWards = new javax.swing.JScrollPane();
        tblWards = new javax.swing.JTable();
        lblNewWard = new javax.swing.JLabel();
        cmbWardDept = new javax.swing.JComboBox<>();
        cmbWardGender = new javax.swing.JComboBox<>();
        txtWardCapacity = new javax.swing.JTextField();
        btnAddWard = new javax.swing.JButton();
        btnUpdateWard = new javax.swing.JButton();
        lblBedsHeader = new javax.swing.JLabel();
        scrBeds = new javax.swing.JScrollPane();
        tblBeds = new javax.swing.JTable();
        btnAddBed = new javax.swing.JButton();
        btnRemoveBed = new javax.swing.JButton();

        pnlAdmissions = new javax.swing.JPanel();
        lblAdmissionsTitle = new javax.swing.JLabel();
        scrAdmissions = new javax.swing.JScrollPane();
        tblAdmissions = new javax.swing.JTable();
        lblNewAdmission = new javax.swing.JLabel();
        cmbCase = new javax.swing.JComboBox<>();
        cmbBed = new javax.swing.JComboBox<>();
        lblAdmDate = new javax.swing.JLabel();
        txtAdmissionDate = new javax.swing.JTextField();
        lblAdmRemarks = new javax.swing.JLabel();
        txtAdmissionRemarks = new javax.swing.JTextField();
        btnAdmit = new javax.swing.JButton();
        btnDischarge = new javax.swing.JButton();

        pnlDiagnostics = new javax.swing.JPanel();
        lblDiagTitle = new javax.swing.JLabel();
        btnDiagSubLab = new javax.swing.JButton();
        btnDiagSubImaging = new javax.swing.JButton();
        pnlDiagContent = new javax.swing.JPanel();
        pnlDiagLab = new javax.swing.JPanel();
        scrLabRequests = new javax.swing.JScrollPane();
        tblLabRequests = new javax.swing.JTable();
        lblLabResult = new javax.swing.JLabel();
        scrLabResult = new javax.swing.JScrollPane();
        txtLabResult = new javax.swing.JTextArea();
        btnSubmitLabResult = new javax.swing.JButton();
        pnlDiagImaging = new javax.swing.JPanel();
        scrImagingRequests = new javax.swing.JScrollPane();
        tblImagingRequests = new javax.swing.JTable();
        lblScheduleImaging = new javax.swing.JLabel();
        cmbImagingRoom = new javax.swing.JComboBox<>();
        txtImagingDate = new javax.swing.JTextField();
        txtImagingStart = new javax.swing.JTextField();
        txtImagingEnd = new javax.swing.JTextField();
        btnScheduleImaging = new javax.swing.JButton();
        lblImagingHint = new javax.swing.JLabel();

        pnlCatalogues = new javax.swing.JPanel();
        lblCatTitle = new javax.swing.JLabel();
        btnCatSubDrugs = new javax.swing.JButton();
        btnCatSubServices = new javax.swing.JButton();
        pnlCatContent = new javax.swing.JPanel();
        pnlCatDrugs = new javax.swing.JPanel();
        scrDrugs = new javax.swing.JScrollPane();
        tblDrugs = new javax.swing.JTable();
        txtDrugName = new javax.swing.JTextField();
        cmbDrugForm = new javax.swing.JComboBox<>();
        txtDrugPrice = new javax.swing.JTextField();
        btnAddDrug = new javax.swing.JButton();
        btnUpdateDrug = new javax.swing.JButton();
        btnToggleDrug = new javax.swing.JButton();
        pnlCatServices = new javax.swing.JPanel();
        scrServices = new javax.swing.JScrollPane();
        tblServices = new javax.swing.JTable();
        txtServiceName = new javax.swing.JTextField();
        cmbServiceCategory = new javax.swing.JComboBox<>();
        cmbServiceType = new javax.swing.JComboBox<>();
        txtServicePrice = new javax.swing.JTextField();
        btnAddService = new javax.swing.JButton();
        btnUpdateService = new javax.swing.JButton();
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
        scrAssignments.setBounds(20, 60, 580, 290);

        lblAssignNew.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblAssignNew.setText("Reassign selected doctor to department:");
        pnlAssign.add(lblAssignNew);
        lblAssignNew.setBounds(20, 365, 400, 20);

        cmbDeptAssign.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] {}));
        pnlAssign.add(cmbDeptAssign);
        cmbDeptAssign.setBounds(20, 390, 400, 26);

        btnAssignDept.setBackground(new java.awt.Color(38, 117, 154));
        btnAssignDept.setForeground(new java.awt.Color(255, 255, 255));
        btnAssignDept.setText("Assign");
        btnAssignDept.addActionListener(this::btnAssignDeptActionPerformed);
        pnlAssign.add(btnAssignDept);
        btnAssignDept.setBounds(430, 390, 150, 30);

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
        scrAssets.setBounds(20, 100, 580, 270);

        lblNewAssetType.setText("Type (if applicable):");
        pnlAssets.add(lblNewAssetType);
        lblNewAssetType.setBounds(20, 385, 160, 26);

        cmbNewAssetType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] {}));
        pnlAssets.add(cmbNewAssetType);
        cmbNewAssetType.setBounds(190, 385, 200, 26);

        btnAddAsset.setBackground(new java.awt.Color(38, 117, 154));
        btnAddAsset.setForeground(new java.awt.Color(255, 255, 255));
        btnAddAsset.setText("Add New");
        btnAddAsset.addActionListener(this::btnAddAssetActionPerformed);
        pnlAssets.add(btnAddAsset);
        btnAddAsset.setBounds(400, 385, 120, 30);

        btnToggleAssetStatus.setBackground(new java.awt.Color(38, 117, 154));
        btnToggleAssetStatus.setForeground(new java.awt.Color(255, 255, 255));
        btnToggleAssetStatus.setText("Toggle Status (OK / Maintenance)");
        btnToggleAssetStatus.addActionListener(this::btnToggleAssetStatusActionPerformed);
        pnlAssets.add(btnToggleAssetStatus);
        btnToggleAssetStatus.setBounds(20, 430, 280, 36);

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
        scrWards.setBounds(20, 55, 580, 150);

        lblNewWard.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblNewWard.setText("Add / Update Ward:");
        pnlWards.add(lblNewWard);
        lblNewWard.setBounds(20, 215, 200, 20);

        cmbWardDept.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] {}));
        pnlWards.add(cmbWardDept);
        cmbWardDept.setBounds(20, 238, 200, 26);

        cmbWardGender.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] {}));
        pnlWards.add(cmbWardGender);
        cmbWardGender.setBounds(230, 238, 80, 26);

        txtWardCapacity.setToolTipText("Capacity");
        pnlWards.add(txtWardCapacity);
        txtWardCapacity.setBounds(320, 238, 70, 26);

        btnAddWard.setBackground(new java.awt.Color(38, 117, 154));
        btnAddWard.setForeground(new java.awt.Color(255, 255, 255));
        btnAddWard.setText("Add");
        btnAddWard.addActionListener(this::btnAddWardActionPerformed);
        pnlWards.add(btnAddWard);
        btnAddWard.setBounds(400, 238, 70, 26);

        btnUpdateWard.setBackground(new java.awt.Color(38, 117, 154));
        btnUpdateWard.setForeground(new java.awt.Color(255, 255, 255));
        btnUpdateWard.setText("Update");
        btnUpdateWard.addActionListener(this::btnUpdateWardActionPerformed);
        pnlWards.add(btnUpdateWard);
        btnUpdateWard.setBounds(480, 238, 110, 26);

        lblBedsHeader.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblBedsHeader.setText("Beds in Selected Ward:");
        pnlWards.add(lblBedsHeader);
        lblBedsHeader.setBounds(20, 280, 250, 20);

        tblBeds.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new String [] {
                "Bed ID", "Ward ID"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false
            };
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblBeds.getTableHeader().setReorderingAllowed(false);
        tblBeds.setSelectionBackground(new java.awt.Color(38, 117, 154));
        tblBeds.setSelectionForeground(new java.awt.Color(255, 255, 255));
        scrBeds.setViewportView(tblBeds);
        pnlWards.add(scrBeds);
        scrBeds.setBounds(20, 305, 350, 155);

        btnAddBed.setBackground(new java.awt.Color(38, 117, 154));
        btnAddBed.setForeground(new java.awt.Color(255, 255, 255));
        btnAddBed.setText("Add Bed to Selected Ward");
        btnAddBed.addActionListener(this::btnAddBedActionPerformed);
        pnlWards.add(btnAddBed);
        btnAddBed.setBounds(375, 305, 220, 30);

        btnRemoveBed.setBackground(new java.awt.Color(38, 117, 154));
        btnRemoveBed.setForeground(new java.awt.Color(255, 255, 255));
        btnRemoveBed.setText("Remove Selected Bed");
        btnRemoveBed.addActionListener(this::btnRemoveBedActionPerformed);
        pnlWards.add(btnRemoveBed);
        btnRemoveBed.setBounds(375, 345, 220, 30);

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
        scrAdmissions.setBounds(20, 55, 580, 260);

        lblNewAdmission.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblNewAdmission.setText("New Admission:");
        pnlAdmissions.add(lblNewAdmission);
        lblNewAdmission.setBounds(20, 325, 200, 20);

        cmbCase.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] {}));
        cmbCase.addActionListener(this::cmbCaseActionPerformed);
        pnlAdmissions.add(cmbCase);
        cmbCase.setBounds(20, 348, 280, 26);

        cmbBed.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] {}));
        pnlAdmissions.add(cmbBed);
        cmbBed.setBounds(310, 348, 180, 26);

        lblAdmDate.setText("Date (dd-MM-yyyy):");
        pnlAdmissions.add(lblAdmDate);
        lblAdmDate.setBounds(20, 380, 130, 20);

        pnlAdmissions.add(txtAdmissionDate);
        txtAdmissionDate.setBounds(150, 378, 100, 26);

        lblAdmRemarks.setText("Remarks:");
        pnlAdmissions.add(lblAdmRemarks);
        lblAdmRemarks.setBounds(260, 380, 70, 20);

        pnlAdmissions.add(txtAdmissionRemarks);
        txtAdmissionRemarks.setBounds(330, 378, 170, 26);

        btnAdmit.setBackground(new java.awt.Color(38, 117, 154));
        btnAdmit.setForeground(new java.awt.Color(255, 255, 255));
        btnAdmit.setText("Admit");
        btnAdmit.addActionListener(this::btnAdmitActionPerformed);
        pnlAdmissions.add(btnAdmit);
        btnAdmit.setBounds(510, 378, 90, 26);

        btnDischarge.setBackground(new java.awt.Color(38, 117, 154));
        btnDischarge.setForeground(new java.awt.Color(255, 255, 255));
        btnDischarge.setText("Discharge Selected");
        btnDischarge.addActionListener(this::btnDischargeActionPerformed);
        pnlAdmissions.add(btnDischarge);
        btnDischarge.setBounds(20, 420, 200, 30);

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
        scrLabRequests.setBounds(0, 0, 580, 260);

        lblLabResult.setText("Result:");
        pnlDiagLab.add(lblLabResult);
        lblLabResult.setBounds(0, 270, 60, 20);

        txtLabResult.setLineWrap(true);
        txtLabResult.setWrapStyleWord(true);
        txtLabResult.setRows(3);
        scrLabResult.setViewportView(txtLabResult);
        pnlDiagLab.add(scrLabResult);
        scrLabResult.setBounds(0, 292, 580, 70);

        btnSubmitLabResult.setBackground(new java.awt.Color(38, 117, 154));
        btnSubmitLabResult.setForeground(new java.awt.Color(255, 255, 255));
        btnSubmitLabResult.setText("Submit Result");
        btnSubmitLabResult.addActionListener(this::btnSubmitLabResultActionPerformed);
        pnlDiagLab.add(btnSubmitLabResult);
        btnSubmitLabResult.setBounds(0, 372, 150, 30);

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
        scrImagingRequests.setBounds(0, 0, 580, 200);

        lblScheduleImaging.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblScheduleImaging.setText("Schedule Appointment:");
        pnlDiagImaging.add(lblScheduleImaging);
        lblScheduleImaging.setBounds(0, 210, 250, 20);

        cmbImagingRoom.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] {}));
        pnlDiagImaging.add(cmbImagingRoom);
        cmbImagingRoom.setBounds(0, 235, 190, 26);

        txtImagingDate.setToolTipText("dd-MM-yyyy");
        pnlDiagImaging.add(txtImagingDate);
        txtImagingDate.setBounds(200, 235, 90, 26);

        txtImagingStart.setToolTipText("HH:mm");
        pnlDiagImaging.add(txtImagingStart);
        txtImagingStart.setBounds(300, 235, 70, 26);

        txtImagingEnd.setToolTipText("HH:mm");
        pnlDiagImaging.add(txtImagingEnd);
        txtImagingEnd.setBounds(380, 235, 70, 26);

        btnScheduleImaging.setBackground(new java.awt.Color(38, 117, 154));
        btnScheduleImaging.setForeground(new java.awt.Color(255, 255, 255));
        btnScheduleImaging.setText("Schedule");
        btnScheduleImaging.addActionListener(this::btnScheduleImagingActionPerformed);
        pnlDiagImaging.add(btnScheduleImaging);
        btnScheduleImaging.setBounds(460, 235, 110, 26);

        lblImagingHint.setFont(new java.awt.Font("Segoe UI", 2, 11)); // NOI18N
        lblImagingHint.setText("Room / Date dd-MM-yyyy / Start HH:mm / End HH:mm (30-min increments)");
        pnlDiagImaging.add(lblImagingHint);
        lblImagingHint.setBounds(0, 268, 580, 20);

        pnlDiagContent.add(pnlDiagImaging, "imaging");

        pnlDiagnostics.add(pnlDiagContent);
        pnlDiagContent.setBounds(20, 90, 580, 420);

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
        scrDrugs.setBounds(0, 0, 580, 260);

        txtDrugName.setToolTipText("Drug name");
        pnlCatDrugs.add(txtDrugName);
        txtDrugName.setBounds(0, 270, 230, 26);

        cmbDrugForm.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] {}));
        pnlCatDrugs.add(cmbDrugForm);
        cmbDrugForm.setBounds(240, 270, 160, 26);

        txtDrugPrice.setToolTipText("Price");
        pnlCatDrugs.add(txtDrugPrice);
        txtDrugPrice.setBounds(410, 270, 80, 26);

        btnAddDrug.setBackground(new java.awt.Color(38, 117, 154));
        btnAddDrug.setForeground(new java.awt.Color(255, 255, 255));
        btnAddDrug.setText("Add New");
        btnAddDrug.addActionListener(this::btnAddDrugActionPerformed);
        pnlCatDrugs.add(btnAddDrug);
        btnAddDrug.setBounds(0, 305, 100, 30);

        btnUpdateDrug.setBackground(new java.awt.Color(38, 117, 154));
        btnUpdateDrug.setForeground(new java.awt.Color(255, 255, 255));
        btnUpdateDrug.setText("Update Selected");
        btnUpdateDrug.addActionListener(this::btnUpdateDrugActionPerformed);
        pnlCatDrugs.add(btnUpdateDrug);
        btnUpdateDrug.setBounds(105, 305, 165, 30);

        btnToggleDrug.setBackground(new java.awt.Color(38, 117, 154));
        btnToggleDrug.setForeground(new java.awt.Color(255, 255, 255));
        btnToggleDrug.setText("Toggle Active / Inactive");
        btnToggleDrug.addActionListener(this::btnToggleDrugActionPerformed);
        pnlCatDrugs.add(btnToggleDrug);
        btnToggleDrug.setBounds(280, 305, 220, 30);

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
        scrServices.setBounds(0, 0, 580, 260);

        txtServiceName.setToolTipText("Service name");
        pnlCatServices.add(txtServiceName);
        txtServiceName.setBounds(0, 270, 190, 26);

        cmbServiceCategory.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] {}));
        cmbServiceCategory.addActionListener(this::cmbServiceCategoryActionPerformed);
        pnlCatServices.add(cmbServiceCategory);
        cmbServiceCategory.setBounds(200, 270, 110, 26);

        cmbServiceType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] {}));
        pnlCatServices.add(cmbServiceType);
        cmbServiceType.setBounds(320, 270, 140, 26);

        txtServicePrice.setToolTipText("Price");
        pnlCatServices.add(txtServicePrice);
        txtServicePrice.setBounds(470, 270, 80, 26);

        btnAddService.setBackground(new java.awt.Color(38, 117, 154));
        btnAddService.setForeground(new java.awt.Color(255, 255, 255));
        btnAddService.setText("Add New");
        btnAddService.addActionListener(this::btnAddServiceActionPerformed);
        pnlCatServices.add(btnAddService);
        btnAddService.setBounds(0, 305, 100, 30);

        btnUpdateService.setBackground(new java.awt.Color(38, 117, 154));
        btnUpdateService.setForeground(new java.awt.Color(255, 255, 255));
        btnUpdateService.setText("Update Selected");
        btnUpdateService.addActionListener(this::btnUpdateServiceActionPerformed);
        pnlCatServices.add(btnUpdateService);
        btnUpdateService.setBounds(105, 305, 165, 30);

        btnToggleService.setBackground(new java.awt.Color(38, 117, 154));
        btnToggleService.setForeground(new java.awt.Color(255, 255, 255));
        btnToggleService.setText("Toggle Active / Inactive");
        btnToggleService.addActionListener(this::btnToggleServiceActionPerformed);
        pnlCatServices.add(btnToggleService);
        btnToggleService.setBounds(280, 305, 220, 30);

        pnlCatContent.add(pnlCatServices, "services");

        pnlCatalogues.add(pnlCatContent);
        pnlCatContent.setBounds(20, 90, 580, 420);

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
    private javax.swing.JButton btnAddBed;
    private javax.swing.JButton btnAddDrug;
    private javax.swing.JButton btnAddInsurance;
    private javax.swing.JButton btnAddService;
    private javax.swing.JButton btnAddWard;
    private javax.swing.JButton btnAdmit;
    private javax.swing.JButton btnAssignDept;
    private javax.swing.JButton btnCatSubDrugs;
    private javax.swing.JButton btnCatSubServices;
    private javax.swing.JButton btnDiagSubImaging;
    private javax.swing.JButton btnDiagSubLab;
    private javax.swing.JButton btnDischarge;
    private javax.swing.JButton btnEditUser;
    private javax.swing.JButton btnLogout;
    private javax.swing.JButton btnNavAdmissions;
    private javax.swing.JButton btnNavAssets;
    private javax.swing.JButton btnNavAssign;
    private javax.swing.JButton btnNavBilling;
    private javax.swing.JButton btnNavCatalogues;
    private javax.swing.JButton btnNavDiagnostics;
    private javax.swing.JButton btnNavUsers;
    private javax.swing.JButton btnNavWards;
    private javax.swing.JButton btnNewUser;
    private javax.swing.JButton btnRemoveBed;
    private javax.swing.JButton btnRemoveInsurance;
    private javax.swing.JButton btnSaveFees;
    private javax.swing.JButton btnScheduleImaging;
    private javax.swing.JButton btnSearchUsers;
    private javax.swing.JButton btnSubmitLabResult;
    private javax.swing.JButton btnToggleAssetStatus;
    private javax.swing.JButton btnToggleDrug;
    private javax.swing.JButton btnToggleService;
    private javax.swing.JButton btnToggleUserStatus;
    private javax.swing.JButton btnUpdateDrug;
    private javax.swing.JButton btnUpdateMultiplier;
    private javax.swing.JButton btnUpdateService;
    private javax.swing.JButton btnUpdateWard;
    private javax.swing.JComboBox<String> cmbAssetCategory;
    private javax.swing.JComboBox<String> cmbBed;
    private javax.swing.JComboBox<String> cmbCase;
    private javax.swing.JComboBox<String> cmbDeptAssign;
    private javax.swing.JComboBox<String> cmbDrugForm;
    private javax.swing.JComboBox<String> cmbImagingRoom;
    private javax.swing.JComboBox<String> cmbNewAssetType;
    private javax.swing.JComboBox<String> cmbRoleFilter;
    private javax.swing.JComboBox<String> cmbServiceCategory;
    private javax.swing.JComboBox<String> cmbServiceType;
    private javax.swing.JComboBox<String> cmbWardDept;
    private javax.swing.JComboBox<String> cmbWardGender;
    private javax.swing.JLabel lblAdmDate;
    private javax.swing.JLabel lblAdmRemarks;
    private javax.swing.JLabel lblAdmissionsTitle;
    private javax.swing.JLabel lblAssetCategory;
    private javax.swing.JLabel lblAssetsTitle;
    private javax.swing.JLabel lblAssignNew;
    private javax.swing.JLabel lblAssignTitle;
    private javax.swing.JLabel lblBedsHeader;
    private javax.swing.JLabel lblBillingTitle;
    private javax.swing.JLabel lblCatTitle;
    private javax.swing.JLabel lblConsultFee;
    private javax.swing.JLabel lblDiagTitle;
    private javax.swing.JLabel lblFeesHeader;
    private javax.swing.JLabel lblHospFee;
    private javax.swing.JLabel lblImagingHint;
    private javax.swing.JLabel lblInsuranceHeader;
    private javax.swing.JLabel lblLabResult;
    private javax.swing.JLabel lblNewAdmission;
    private javax.swing.JLabel lblNewAssetType;
    private javax.swing.JLabel lblNewMultiplier;
    private javax.swing.JLabel lblNewWard;
    private javax.swing.JLabel lblPortalTitle;
    private javax.swing.JLabel lblRoleFilter;
    private javax.swing.JLabel lblScheduleImaging;
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
    private javax.swing.JScrollPane scrBeds;
    private javax.swing.JScrollPane scrDrugs;
    private javax.swing.JScrollPane scrImagingRequests;
    private javax.swing.JScrollPane scrInsurance;
    private javax.swing.JScrollPane scrLabRequests;
    private javax.swing.JScrollPane scrLabResult;
    private javax.swing.JScrollPane scrServices;
    private javax.swing.JScrollPane scrTiers;
    private javax.swing.JScrollPane scrUsers;
    private javax.swing.JScrollPane scrWards;
    private javax.swing.JTable tblAdmissions;
    private javax.swing.JTable tblAssets;
    private javax.swing.JTable tblAssignments;
    private javax.swing.JTable tblBeds;
    private javax.swing.JTable tblDrugs;
    private javax.swing.JTable tblImagingRequests;
    private javax.swing.JTable tblInsurance;
    private javax.swing.JTable tblLabRequests;
    private javax.swing.JTable tblServices;
    private javax.swing.JTable tblTiers;
    private javax.swing.JTable tblUsers;
    private javax.swing.JTable tblWards;
    private javax.swing.JTextArea txtLabResult;
    private javax.swing.JTextField txtAdmissionDate;
    private javax.swing.JTextField txtAdmissionRemarks;
    private javax.swing.JTextField txtConsultFee;
    private javax.swing.JTextField txtDrugName;
    private javax.swing.JTextField txtDrugPrice;
    private javax.swing.JTextField txtHospFee;
    private javax.swing.JTextField txtImagingDate;
    private javax.swing.JTextField txtImagingEnd;
    private javax.swing.JTextField txtImagingStart;
    private javax.swing.JTextField txtInsuranceName;
    private javax.swing.JTextField txtMultiplier;
    private javax.swing.JTextField txtServiceName;
    private javax.swing.JTextField txtServicePrice;
    private javax.swing.JTextField txtUserSearch;
    private javax.swing.JTextField txtWardCapacity;
    // End of variables declaration//GEN-END:variables
}
