/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package Doctor;

import java.util.ArrayList;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Sascha
 */
public class ConsultationsDialog extends javax.swing.JDialog {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(ConsultationsDialog.class.getName());

    private DoctorServices doctor;
    private int consultId = -1;

    // A diagnostic request drafted (or already on file) for this consultation, pending the
    // doctor's "Save Progress"/"Complete Consultation" click. requestId is -1 for a brand new
    // draft that has not been submitted yet.
    private static class PendingDiagRequest {
        int requestId;
        String serviceId;
        String serviceName;
        String requestDate;
        String remarks;

        PendingDiagRequest(int requestId, String serviceId, String serviceName, String requestDate, String remarks) {
            this.requestId = requestId;
            this.serviceId = serviceId;
            this.serviceName = serviceName;
            this.requestDate = requestDate;
            this.remarks = remarks;
        }
    }

    // Nothing from the Prescription/Diagnostic Request dialogs is written to file until the
    // doctor clicks "Save Progress" or "Complete Consultation" here. Cancel discards all of it.
    private ArrayList<String[]> pendingPrescriptionItems = new ArrayList<>();
    private ArrayList<PendingDiagRequest> pendingDiagRequests = new ArrayList<>();
    private ArrayList<Integer> originalDiagRequestIds = new ArrayList<>();

    /**
     * Creates new form ConsultationDialog
     */
    public ConsultationsDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
    }

    // Used by the doctor dashboard to open one specific consultation
    public ConsultationsDialog(java.awt.Frame parent, boolean modal, DoctorServices doctor, int consultId) {
        super(parent, modal);
        initComponents();
        this.doctor = doctor;
        this.consultId = consultId;

        if (!doctor.loadConsultation(consultId)) {
            JOptionPane.showMessageDialog(this, "This consultation could not be found.");
            dispose();
            return;
        }
        txtComplaint.setText(doctor.getComplaint());
        txtComplaint.setEditable(false);
        txtVitalSigns.setText(doctor.getVitalSigns());
        txtNotes.setText(doctor.getNotes());
        loadPendingState();
        refresh();
    }

    // Seeds the pending prescription/diagnostic-request drafts from what is currently on file
    private void loadPendingState() {
        pendingPrescriptionItems = doctor.getPrescriptionItems(consultId);

        pendingDiagRequests = new ArrayList<>();
        originalDiagRequestIds = new ArrayList<>();
        for (Object[] row : doctor.getDiagnosticRequestItems(consultId)) {
            int requestId = (Integer) row[0];
            pendingDiagRequests.add(new PendingDiagRequest(requestId, (String) row[1], (String) row[2], (String) row[3], (String) row[4]));
            originalDiagRequestIds.add(requestId);
        }
    }

    // Redraws every field from the doctor object's and the pending drafts' current data.
    // Deliberately never touches txtVitalSigns/txtNotes so in-progress typing survives a
    // round trip through the Prescription/Diagnostic Request dialogs.
    private void refresh() {
        lblConsultHeader.setText(doctor.getConsultTitle());
        lblConsultMeta.setText(doctor.getConsultMeta());
        lblConsultStatusBadge.setText(doctor.getConsultStatus());
        lblConsultRoleNote.setText("<html>" + doctor.getConsultRoleNote() + "</html>");

        boolean canEdit = doctor.canEditConsultation();
        txtVitalSigns.setEditable(canEdit);
        txtNotes.setEditable(canEdit);
        btnSaveProgress.setEnabled(canEdit);
        btnCompleteConsultation.setEnabled(canEdit);
        btnAddPrescription.setEnabled(canEdit);
        btnAddDiagRequest.setEnabled(canEdit);

        DefaultTableModel prescriptionModel = (DefaultTableModel) tblPrescriptionItemsView.getModel();
        prescriptionModel.setRowCount(0);
        for (String[] item : pendingPrescriptionItems) {
            prescriptionModel.addRow(new Object[]{item[1], item[2], item[3], item[4]});
        }
        btnAddPrescription.setText(pendingPrescriptionItems.isEmpty() ? "Add Prescription" : "Edit Prescription");

        DefaultTableModel diagnosticModel = (DefaultTableModel) tblDiagnosticRequestsView.getModel();
        diagnosticModel.setRowCount(0);
        for (PendingDiagRequest req : pendingDiagRequests) {
            diagnosticModel.addRow(new Object[]{req.serviceName, req.requestDate, req.remarks});
        }
        btnDeleteDiagRequest.setEnabled(canEdit && !pendingDiagRequests.isEmpty());
    }

    // Saves vitals/notes, the drafted prescription, and the drafted diagnostic requests
    // (new ones added, existing ones removed) all together, marking the consultation
    // "completed" or "incomplete" depending on markComplete. Returns false and shows the
    // error if any step fails, leaving the dialog open with the drafts untouched.
    private boolean saveEverything(boolean markComplete) {
        String result = markComplete
                ? doctor.completeConsultation(txtVitalSigns.getText(), txtNotes.getText())
                : doctor.saveConsultationProgress(txtVitalSigns.getText(), txtNotes.getText());
        if (result != null) {
            JOptionPane.showMessageDialog(this, result);
            return false;
        }

        if (!pendingPrescriptionItems.isEmpty()) {
            result = doctor.savePrescription(consultId, pendingPrescriptionItems);
            if (result != null) {
                JOptionPane.showMessageDialog(this, result);
                return false;
            }
        }

        for (Integer originalId : originalDiagRequestIds) {
            boolean stillPending = false;
            for (PendingDiagRequest req : pendingDiagRequests) {
                if (req.requestId == originalId) {
                    stillPending = true;
                    break;
                }
            }
            if (!stillPending) {
                doctor.deleteDiagnosticRequest(originalId);
            }
        }

        ArrayList<String[]> newRequests = new ArrayList<>();
        for (PendingDiagRequest req : pendingDiagRequests) {
            if (req.requestId == -1) {
                newRequests.add(new String[]{req.serviceId, req.remarks});
            }
        }
        if (!newRequests.isEmpty()) {
            result = doctor.submitDiagnosticRequests(consultId, newRequests);
            if (result != null) {
                JOptionPane.showMessageDialog(this, result);
                return false;
            }
        }

        doctor.loadConsultation(consultId);
        loadPendingState();
        refresh();
        return true;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblConsultHeader = new javax.swing.JLabel();
        lblConsultStatusBadge = new javax.swing.JLabel();
        lblConsultMeta = new javax.swing.JLabel();
        lblConsultRoleNote = new javax.swing.JLabel();
        btnViewCase = new javax.swing.JButton();
        lblComplaintHeader = new javax.swing.JLabel();
        scrComplaint = new javax.swing.JScrollPane();
        txtComplaint = new javax.swing.JTextArea();
        lblVitalsHeader = new javax.swing.JLabel();
        scrVitalSigns = new javax.swing.JScrollPane();
        txtVitalSigns = new javax.swing.JTextArea();
        lblNotesHeader = new javax.swing.JLabel();
        scrNotes = new javax.swing.JScrollPane();
        txtNotes = new javax.swing.JTextArea();
        lblPrescriptionHeader = new javax.swing.JLabel();
        btnAddPrescription = new javax.swing.JButton();
        scrPrescriptionItemsView = new javax.swing.JScrollPane();
        tblPrescriptionItemsView = new javax.swing.JTable();
        lblDiagnosticHeader = new javax.swing.JLabel();
        btnAddDiagRequest = new javax.swing.JButton();
        btnDeleteDiagRequest = new javax.swing.JButton();
        scrDiagnosticRequestsView = new javax.swing.JScrollPane();
        tblDiagnosticRequestsView = new javax.swing.JTable();
        btnSaveProgress = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        btnCompleteConsultation = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Consultation Notes");
        setModal(true);
        setName("consultations"); // NOI18N
        setPreferredSize(new java.awt.Dimension(560, 910));
        setResizable(false);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblConsultHeader.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblConsultHeader.setText("lblConsultHeader");
        getContentPane().add(lblConsultHeader, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, 400, 26));

        lblConsultStatusBadge.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblConsultStatusBadge.setText("lblConsultStatusBadge");
        getContentPane().add(lblConsultStatusBadge, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 20, 100, 18));

        lblConsultMeta.setForeground(new java.awt.Color(102, 102, 102));
        lblConsultMeta.setText("lblConsultMeta");
        getContentPane().add(lblConsultMeta, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 48, 520, 18));

        lblConsultRoleNote.setFont(new java.awt.Font("Segoe UI", 2, 12)); // NOI18N
        lblConsultRoleNote.setForeground(new java.awt.Color(102, 102, 102));
        lblConsultRoleNote.setText("lblConsultRoleNote");
        lblConsultRoleNote.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        getContentPane().add(lblConsultRoleNote, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 68, 350, 40));

        btnViewCase.setBackground(new java.awt.Color(38, 117, 154));
        btnViewCase.setForeground(new java.awt.Color(255, 255, 255));
        btnViewCase.setText("View Case");
        btnViewCase.setFocusPainted(false);
        btnViewCase.addActionListener(this::btnViewCase);
        getContentPane().add(btnViewCase, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 68, 130, 26));

        lblComplaintHeader.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblComplaintHeader.setText("Complaint:");
        getContentPane().add(lblComplaintHeader, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 114, 300, 18));

        txtComplaint.setColumns(20);
        txtComplaint.setLineWrap(true);
        txtComplaint.setRows(2);
        txtComplaint.setWrapStyleWord(true);
        scrComplaint.setViewportView(txtComplaint);

        getContentPane().add(scrComplaint, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 134, 520, 50));

        lblVitalsHeader.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblVitalsHeader.setText("Vital Signs: ");
        getContentPane().add(lblVitalsHeader, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 194, 300, 18));

        txtVitalSigns.setColumns(20);
        txtVitalSigns.setLineWrap(true);
        txtVitalSigns.setRows(2);
        txtVitalSigns.setWrapStyleWord(true);
        scrVitalSigns.setViewportView(txtVitalSigns);

        getContentPane().add(scrVitalSigns, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 214, 520, 50));

        lblNotesHeader.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblNotesHeader.setText("Notes: ");
        getContentPane().add(lblNotesHeader, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 274, 300, 18));

        txtNotes.setColumns(20);
        txtNotes.setLineWrap(true);
        txtNotes.setRows(5);
        txtNotes.setWrapStyleWord(true);
        scrNotes.setViewportView(txtNotes);

        getContentPane().add(scrNotes, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 294, 520, 110));

        lblPrescriptionHeader.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblPrescriptionHeader.setText("Prescription:");
        getContentPane().add(lblPrescriptionHeader, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 416, 140, 22));

        btnAddPrescription.setBackground(new java.awt.Color(38, 117, 154));
        btnAddPrescription.setForeground(new java.awt.Color(255, 255, 255));
        btnAddPrescription.setText("Add Prescription");
        btnAddPrescription.setFocusPainted(false);
        btnAddPrescription.addActionListener(this::btnAddPrescription);
        getContentPane().add(btnAddPrescription, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 540, 200, 26));

        tblPrescriptionItemsView.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Drug", "Dosage", "Frequency", "Days"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblPrescriptionItemsView.getTableHeader().setReorderingAllowed(false);
        scrPrescriptionItemsView.setViewportView(tblPrescriptionItemsView);

        getContentPane().add(scrPrescriptionItemsView, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 444, 520, 86));

        lblDiagnosticHeader.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblDiagnosticHeader.setText("Diagnostic Requests:");
        getContentPane().add(lblDiagnosticHeader, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 576, 170, 22));

        btnAddDiagRequest.setBackground(new java.awt.Color(38, 117, 154));
        btnAddDiagRequest.setForeground(new java.awt.Color(255, 255, 255));
        btnAddDiagRequest.setText("Add Request");
        btnAddDiagRequest.setFocusPainted(false);
        btnAddDiagRequest.addActionListener(this::btnAddDiagRequest);
        getContentPane().add(btnAddDiagRequest, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 728, 200, 26));

        btnDeleteDiagRequest.setText("Delete Selected");
        btnDeleteDiagRequest.setFocusPainted(false);
        btnDeleteDiagRequest.addActionListener(this::btnDeleteDiagRequest);
        getContentPane().add(btnDeleteDiagRequest, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 728, 200, 26));

        tblDiagnosticRequestsView.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Service", "Requested Date", "Remarks"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblDiagnosticRequestsView.getTableHeader().setReorderingAllowed(false);
        scrDiagnosticRequestsView.setViewportView(tblDiagnosticRequestsView);

        getContentPane().add(scrDiagnosticRequestsView, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 608, 520, 110));

        btnSaveProgress.setBackground(new java.awt.Color(38, 117, 154));
        btnSaveProgress.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnSaveProgress.setForeground(new java.awt.Color(255, 255, 255));
        btnSaveProgress.setText("Save Progress");
        btnSaveProgress.addActionListener(this::btnSaveProgress);
        getContentPane().add(btnSaveProgress, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 780, 250, 36));

        btnCancel.setBackground(new java.awt.Color(38, 117, 154));
        btnCancel.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnCancel.setForeground(new java.awt.Color(255, 255, 255));
        btnCancel.setText("Cancel");
        btnCancel.addActionListener(this::btnCancel);
        getContentPane().add(btnCancel, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 780, 250, 36));

        btnCompleteConsultation.setBackground(new java.awt.Color(38, 117, 154));
        btnCompleteConsultation.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnCompleteConsultation.setForeground(new java.awt.Color(255, 255, 255));
        btnCompleteConsultation.setText("Complete Consultation");
        btnCompleteConsultation.addActionListener(this::btnCompleteConsultation);
        getContentPane().add(btnCompleteConsultation, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 830, 520, 36));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnSaveProgress(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveProgress
        if (!saveEverything(false)) {
            return;
        }
        JOptionPane.showMessageDialog(this, "Progress saved.");
    }//GEN-LAST:event_btnSaveProgress

    private void btnAddPrescription(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddPrescription
        if (!doctor.canEditConsultation()) {
            JOptionPane.showMessageDialog(this, "You cannot write a prescription for this consultation.");
            return;
        }
        PrescriptionDialog dialog = new PrescriptionDialog((java.awt.Frame) getOwner(), true, doctor, consultId,
                new ArrayList<>(pendingPrescriptionItems));
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            pendingPrescriptionItems = dialog.getResultItems();
        }
        refresh();
    }//GEN-LAST:event_btnAddPrescription

    private void btnAddDiagRequest(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddDiagRequest
        if (!doctor.canEditConsultation()) {
            JOptionPane.showMessageDialog(this, "You cannot request diagnostic services for this consultation.");
            return;
        }
        DiagnosticRequestDialog dialog = new DiagnosticRequestDialog((java.awt.Frame) getOwner(), true, doctor, consultId);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            for (String[] request : dialog.getResultRequests()) {
                pendingDiagRequests.add(new PendingDiagRequest(-1, request[0], request[1], doctor.today(), request[2]));
            }
        }
        refresh();
    }//GEN-LAST:event_btnAddDiagRequest

    private void btnDeleteDiagRequest(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteDiagRequest
        int row = tblDiagnosticRequestsView.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a diagnostic request to delete.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Remove this diagnostic request?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        pendingDiagRequests.remove(row);
        refresh();
    }//GEN-LAST:event_btnDeleteDiagRequest

    private void btnViewCase(java.awt.event.ActionEvent evt) {
        int caseId = doctor.getConsultCaseId();
        if (caseId < 0) {
            return;
        }
        CasesDialog dialog = new CasesDialog((java.awt.Frame) getOwner(), true, doctor, caseId);
        dialog.setVisible(true);
        doctor.loadConsultation(consultId);
        refresh();
    }

    private void btnCancel(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancel
        dispose();
    }//GEN-LAST:event_btnCancel

    private void btnCompleteConsultation(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCompleteConsultation
        if (!saveEverything(true)) {
            return;
        }
        JOptionPane.showMessageDialog(this, "Consultation completed.");
        dispose();
    }//GEN-LAST:event_btnCompleteConsultation


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

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                ConsultationsDialog dialog = new ConsultationsDialog(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddDiagRequest;
    private javax.swing.JButton btnAddPrescription;
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnCompleteConsultation;
    private javax.swing.JButton btnDeleteDiagRequest;
    private javax.swing.JButton btnSaveProgress;
    private javax.swing.JButton btnViewCase;
    private javax.swing.JLabel lblComplaintHeader;
    private javax.swing.JLabel lblConsultHeader;
    private javax.swing.JLabel lblConsultMeta;
    private javax.swing.JLabel lblConsultRoleNote;
    private javax.swing.JLabel lblConsultStatusBadge;
    private javax.swing.JLabel lblDiagnosticHeader;
    private javax.swing.JLabel lblNotesHeader;
    private javax.swing.JLabel lblPrescriptionHeader;
    private javax.swing.JLabel lblVitalsHeader;
    private javax.swing.JScrollPane scrComplaint;
    private javax.swing.JScrollPane scrDiagnosticRequestsView;
    private javax.swing.JScrollPane scrNotes;
    private javax.swing.JScrollPane scrPrescriptionItemsView;
    private javax.swing.JScrollPane scrVitalSigns;
    private javax.swing.JTable tblDiagnosticRequestsView;
    private javax.swing.JTable tblPrescriptionItemsView;
    private javax.swing.JTextArea txtComplaint;
    private javax.swing.JTextArea txtNotes;
    private javax.swing.JTextArea txtVitalSigns;
    // End of variables declaration//GEN-END:variables
}
