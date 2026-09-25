/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package Admin;

import java.awt.Frame;
import java.util.ArrayList;
import javax.swing.JOptionPane;

/**
 *
 * @author Sascha
 */
public class UserEditDialog extends javax.swing.JDialog {

    private static final String[] ROLE_INTERNAL = {"Admin", "MedicalManager", "Doctor", "Patient"};
    private static final String[] ROLE_READABLE = {"Admin", "Medical Manager", "Doctor", "Patient"};

    private final AdminServices admin;
    private final Integer editingUserId;
    private final ArrayList<Integer> deptIds = new ArrayList<>();
    private boolean saved = false;

    /**
     * Creates new form UserEditDialog. Pass editingUserId = null to create a
     * brand new user, or an existing user's id to edit their details.
     */
    public UserEditDialog(Frame parent, AdminServices admin, Integer editingUserId) {
        super(parent, true);
        this.admin = admin;
        this.editingUserId = editingUserId;
        initComponents();
        setLocationRelativeTo(parent);

        cmbGender.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"male", "female"}));
        cmbRole.setModel(new javax.swing.DefaultComboBoxModel<>(ROLE_READABLE));
        cmbOffDay.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{
            "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"
        }));
        cmbBloodType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{
            "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"
        }));

        deptIds.clear();
        ArrayList<String> deptLabels = new ArrayList<>();
        for (Object[] row : admin.getDepartments()) {
            deptIds.add((Integer) row[0]);
            deptLabels.add((String) row[1]);
        }
        cmbDept.setModel(new javax.swing.DefaultComboBoxModel<>(deptLabels.toArray(new String[0])));

        if (editingUserId == null) {
            setTitle("Create New User");
            cmbRole.setSelectedIndex(0);
            updateExtraPanels(ROLE_INTERNAL[0]);
        } else {
            setTitle("Edit User");
            loadForEdit();
        }
    }

    private int indexOfInternal(String role) {
        for (int i = 0; i < ROLE_INTERNAL.length; i++) {
            if (ROLE_INTERNAL[i].equals(role)) {
                return i;
            }
        }
        return 0;
    }

    private void updateExtraPanels(String role) {
        pnlDoctorExtra.setVisible("Doctor".equals(role));
        pnlPatientExtra.setVisible("Patient".equals(role));
    }

    private void loadForEdit() {
        String[] detail = admin.getUserDetail(editingUserId);
        if (detail == null) {
            return;
        }
        txtFirstName.setText(detail[0]);
        txtLastName.setText(detail[1]);
        txtDob.setText(detail[2]);
        cmbGender.setSelectedItem(detail[3]);
        txtPhone.setText(detail[4]);
        txtEmail.setText(detail[5]);
        txtPassword.setText(detail[6]);
        String role = detail[7];
        cmbRole.setSelectedIndex(indexOfInternal(role));
        cmbRole.setEnabled(false);
        updateExtraPanels(role);

        if (role.equals("Doctor")) {
            String[] doctorDetail = admin.getDoctorDetail(editingUserId);
            if (doctorDetail != null) {
                int deptIdVal = Integer.parseInt(doctorDetail[0]);
                int deptIndex = deptIds.indexOf(deptIdVal);
                if (deptIndex >= 0) {
                    cmbDept.setSelectedIndex(deptIndex);
                }
                txtSpecialization.setText(doctorDetail[1]);
                txtPracticeYear.setText(doctorDetail[2]);
                cmbOffDay.setSelectedItem(doctorDetail[3]);
            }
        } else if (role.equals("Patient")) {
            String[] patientDetail = admin.getPatientDetail(editingUserId);
            if (patientDetail != null) {
                cmbBloodType.setSelectedItem(patientDetail[0]);
                txtAllergies.setText(patientDetail[1]);
            }
        }
    }

    public boolean isSaved() {
        return saved;
    }

    private void cmbRoleActionPerformed(java.awt.event.ActionEvent evt) {
        if (cmbRole.isEnabled()) {
            updateExtraPanels(ROLE_INTERNAL[cmbRole.getSelectedIndex()]);
        }
    }

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {
        String firstName = txtFirstName.getText();
        String lastName = txtLastName.getText();
        String dob = txtDob.getText();
        String gender = (String) cmbGender.getSelectedItem();
        String phone = txtPhone.getText();
        String email = txtEmail.getText();
        String password = txtPassword.getText();
        String role = ROLE_INTERNAL[cmbRole.getSelectedIndex()];

        String deptId = null;
        String specialization = null;
        String practiceStartYear = null;
        String offDay = null;
        String bloodType = null;
        String allergies = null;

        if ("Doctor".equals(role)) {
            int deptIndex = cmbDept.getSelectedIndex();
            deptId = (deptIndex >= 0 && deptIndex < deptIds.size()) ? String.valueOf(deptIds.get(deptIndex)) : "";
            specialization = txtSpecialization.getText();
            practiceStartYear = txtPracticeYear.getText();
            offDay = (String) cmbOffDay.getSelectedItem();
        } else if ("Patient".equals(role)) {
            bloodType = (String) cmbBloodType.getSelectedItem();
            allergies = txtAllergies.getText();
        }

        String result;
        if (editingUserId == null) {
            result = admin.createUser(firstName, lastName, dob, gender, phone, email, password, role,
                    deptId, specialization, practiceStartYear, offDay, bloodType, allergies);
        } else {
            result = admin.updateUser(editingUserId, firstName, lastName, dob, gender, phone, email, password,
                    deptId, specialization, practiceStartYear, offDay, bloodType, allergies);
        }
        if (result != null) {
            JOptionPane.showMessageDialog(this, result);
            return;
        }
        saved = true;
        dispose();
    }

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {
        dispose();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblFirstName = new javax.swing.JLabel();
        txtFirstName = new javax.swing.JTextField();
        lblLastName = new javax.swing.JLabel();
        txtLastName = new javax.swing.JTextField();
        lblDob = new javax.swing.JLabel();
        txtDob = new javax.swing.JTextField();
        lblGender = new javax.swing.JLabel();
        cmbGender = new javax.swing.JComboBox<>();
        lblPhone = new javax.swing.JLabel();
        txtPhone = new javax.swing.JTextField();
        lblEmail = new javax.swing.JLabel();
        txtEmail = new javax.swing.JTextField();
        lblPassword = new javax.swing.JLabel();
        txtPassword = new javax.swing.JTextField();
        lblRole = new javax.swing.JLabel();
        cmbRole = new javax.swing.JComboBox<>();
        pnlDoctorExtra = new javax.swing.JPanel();
        lblDept = new javax.swing.JLabel();
        cmbDept = new javax.swing.JComboBox<>();
        lblSpecialization = new javax.swing.JLabel();
        txtSpecialization = new javax.swing.JTextField();
        lblPracticeYear = new javax.swing.JLabel();
        txtPracticeYear = new javax.swing.JTextField();
        lblOffDay = new javax.swing.JLabel();
        cmbOffDay = new javax.swing.JComboBox<>();
        pnlPatientExtra = new javax.swing.JPanel();
        lblBloodType = new javax.swing.JLabel();
        cmbBloodType = new javax.swing.JComboBox<>();
        lblAllergies = new javax.swing.JLabel();
        txtAllergies = new javax.swing.JTextField();
        btnSave = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("User Details");
        setResizable(false);
        getContentPane().setLayout(null);

        lblFirstName.setText("First Name:");
        getContentPane().add(lblFirstName);
        lblFirstName.setBounds(20, 20, 140, 20);
        getContentPane().add(txtFirstName);
        txtFirstName.setBounds(170, 18, 270, 26);

        lblLastName.setText("Last Name:");
        getContentPane().add(lblLastName);
        lblLastName.setBounds(20, 54, 140, 20);
        getContentPane().add(txtLastName);
        txtLastName.setBounds(170, 52, 270, 26);

        lblDob.setText("Date of Birth (dd-MM-yyyy):");
        getContentPane().add(lblDob);
        lblDob.setBounds(20, 88, 150, 20);
        getContentPane().add(txtDob);
        txtDob.setBounds(170, 86, 270, 26);

        lblGender.setText("Gender:");
        getContentPane().add(lblGender);
        lblGender.setBounds(20, 122, 140, 20);
        cmbGender.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] {}));
        getContentPane().add(cmbGender);
        cmbGender.setBounds(170, 120, 270, 26);

        lblPhone.setText("Phone:");
        getContentPane().add(lblPhone);
        lblPhone.setBounds(20, 156, 140, 20);
        getContentPane().add(txtPhone);
        txtPhone.setBounds(170, 154, 270, 26);

        lblEmail.setText("Email:");
        getContentPane().add(lblEmail);
        lblEmail.setBounds(20, 190, 140, 20);
        getContentPane().add(txtEmail);
        txtEmail.setBounds(170, 188, 270, 26);

        lblPassword.setText("Password:");
        getContentPane().add(lblPassword);
        lblPassword.setBounds(20, 224, 140, 20);
        getContentPane().add(txtPassword);
        txtPassword.setBounds(170, 222, 270, 26);

        lblRole.setText("Role:");
        getContentPane().add(lblRole);
        lblRole.setBounds(20, 258, 140, 20);
        cmbRole.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] {}));
        cmbRole.addActionListener(this::cmbRoleActionPerformed);
        getContentPane().add(cmbRole);
        cmbRole.setBounds(170, 256, 270, 26);

        pnlDoctorExtra.setLayout(null);

        lblDept.setText("Department:");
        pnlDoctorExtra.add(lblDept);
        lblDept.setBounds(0, 0, 140, 20);

        pnlDoctorExtra.add(cmbDept);
        cmbDept.setBounds(150, -2, 270, 26);

        lblSpecialization.setText("Specialization:");
        pnlDoctorExtra.add(lblSpecialization);
        lblSpecialization.setBounds(0, 36, 140, 20);

        pnlDoctorExtra.add(txtSpecialization);
        txtSpecialization.setBounds(150, 34, 270, 26);

        lblPracticeYear.setText("Practice Start Year:");
        pnlDoctorExtra.add(lblPracticeYear);
        lblPracticeYear.setBounds(0, 72, 140, 20);

        pnlDoctorExtra.add(txtPracticeYear);
        txtPracticeYear.setBounds(150, 70, 270, 26);

        lblOffDay.setText("Off Day:");
        pnlDoctorExtra.add(lblOffDay);
        lblOffDay.setBounds(0, 108, 140, 20);

        pnlDoctorExtra.add(cmbOffDay);
        cmbOffDay.setBounds(150, 106, 270, 26);

        getContentPane().add(pnlDoctorExtra);
        pnlDoctorExtra.setBounds(20, 292, 420, 150);

        pnlPatientExtra.setLayout(null);

        lblBloodType.setText("Blood Type:");
        pnlPatientExtra.add(lblBloodType);
        lblBloodType.setBounds(0, 0, 140, 20);

        pnlPatientExtra.add(cmbBloodType);
        cmbBloodType.setBounds(150, -2, 270, 26);

        lblAllergies.setText("Allergies:");
        pnlPatientExtra.add(lblAllergies);
        lblAllergies.setBounds(0, 36, 140, 20);

        pnlPatientExtra.add(txtAllergies);
        txtAllergies.setBounds(150, 34, 270, 26);

        getContentPane().add(pnlPatientExtra);
        pnlPatientExtra.setBounds(20, 292, 420, 150);

        btnSave.setBackground(new java.awt.Color(38, 117, 154));
        btnSave.setForeground(new java.awt.Color(255, 255, 255));
        btnSave.setText("Save");
        btnSave.addActionListener(this::btnSaveActionPerformed);
        getContentPane().add(btnSave);
        btnSave.setBounds(170, 470, 100, 36);

        btnCancel.setBackground(new java.awt.Color(150, 150, 150));
        btnCancel.setForeground(new java.awt.Color(255, 255, 255));
        btnCancel.setText("Cancel");
        btnCancel.addActionListener(this::btnCancelActionPerformed);
        getContentPane().add(btnCancel);
        btnCancel.setBounds(290, 470, 100, 36);

        setBounds(0, 0, 480, 570);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnSave;
    private javax.swing.JComboBox<String> cmbBloodType;
    private javax.swing.JComboBox<String> cmbDept;
    private javax.swing.JComboBox<String> cmbGender;
    private javax.swing.JComboBox<String> cmbOffDay;
    private javax.swing.JComboBox<String> cmbRole;
    private javax.swing.JLabel lblAllergies;
    private javax.swing.JLabel lblBloodType;
    private javax.swing.JLabel lblDept;
    private javax.swing.JLabel lblDob;
    private javax.swing.JLabel lblEmail;
    private javax.swing.JLabel lblFirstName;
    private javax.swing.JLabel lblGender;
    private javax.swing.JLabel lblLastName;
    private javax.swing.JLabel lblOffDay;
    private javax.swing.JLabel lblPassword;
    private javax.swing.JLabel lblPhone;
    private javax.swing.JLabel lblPracticeYear;
    private javax.swing.JLabel lblRole;
    private javax.swing.JLabel lblSpecialization;
    private javax.swing.JPanel pnlDoctorExtra;
    private javax.swing.JPanel pnlPatientExtra;
    private javax.swing.JTextField txtAllergies;
    private javax.swing.JTextField txtDob;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtFirstName;
    private javax.swing.JTextField txtLastName;
    private javax.swing.JTextField txtPassword;
    private javax.swing.JTextField txtPhone;
    private javax.swing.JTextField txtPracticeYear;
    private javax.swing.JTextField txtSpecialization;
    // End of variables declaration//GEN-END:variables
}
