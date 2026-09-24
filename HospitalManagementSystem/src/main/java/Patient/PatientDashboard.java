package Patient;

import HelperFunction.FileHandling;
import java.awt.CardLayout;
import java.awt.Color;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import java.util.TreeMap;
import javax.swing.table.DefaultTableModel;
import java.text.SimpleDateFormat;
import java.util.Date;
import HelperFunction.SessionUser;
import Users.UserLogin;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
/**
 *
 * @author User
 */
public class PatientDashboard extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(PatientDashboard.class.getName());

    /**
     * Patient dashboard: Bookings, Medical History, Ratings and Profile pages
     */
    // =====================================================================
    // 1. FIELDS
    // =====================================================================                                // logged-in patient (from PatientLogin)
    private PatientServices patient;                      // the patient object, seen through the interface
    private final Color NAV_BG = new Color(30, 95, 125);  // menu background
    private final Color BLUE = new Color(38, 117, 154);   // theme blue

    // Bookings: the combos show names, these lists hold the matching ids (same order)
    private ArrayList<String> deptIds = new ArrayList<>();
    private ArrayList<String> doctorIds = new ArrayList<>();
    // Bookings: hidden info per table row
    //   {"booked", consultId, doctorId}  or  {"available", doctorId, date, start, end}

    // =====================================================================
    // 2. CONSTRUCTOR
    // =====================================================================
    public PatientDashboard() {
        initComponents();

        // make every table read-only and consistent
        javax.swing.JTable[] tables = {tblSlots, tblVisits, tblPrescriptions, tblTests, tblPending, tblMyReviews};
        for (javax.swing.JTable t : tables) {
            t.setDefaultEditor(Object.class, null);
            t.getTableHeader().setReorderingAllowed(false);
            t.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
            t.setRowHeight(25);
            t.setSelectionBackground(BLUE);
            t.setSelectionForeground(Color.WHITE);
        }

        this.patient = (Patient) SessionUser.getCurrentUser();

        setLocationRelativeTo(null);
        loadWelcomeName();
        loadProfile();
        loadHistory();
        loadRatings();
        spnDate.setEditor(new javax.swing.JSpinner.DateEditor(spnDate, "dd-MM-yyyy"));
        loadDepartments();
        loadDoctorCombo();
        loadSlots();
        showPage("bookings", btnNavBookings);
    }

    // =====================================================================
    // 3. HEADER & NAVIGATION
    // =====================================================================
    private void loadWelcomeName() {
        lblWelcome.setText("Welcome, " + patient.getFullName());
    }

    private void showPage(String cardName, JButton activeButton) {
        CardLayout cardLayout = (CardLayout) pnlContent.getLayout();
        cardLayout.show(pnlContent, cardName);

        JButton[] menu = {btnNavBookings, btnNavHistory, btnNavRatings, btnNavProfile};
        for (JButton b : menu) {
            b.setBackground(NAV_BG);
            b.setForeground(Color.WHITE);
        }
        activeButton.setBackground(Color.WHITE);
        activeButton.setForeground(BLUE);

        if (cardName.equals("profile")) {
            getRootPane().setDefaultButton(btnSaveProfile);
        } else {
            getRootPane().setDefaultButton(null);
        }
    }

    // =====================================================================
    // 4. BOOKINGS PAGE (screen only - the Patient does the work)
    // =====================================================================
    private void loadDepartments() {
        cmbDepartment.removeAllItems();
        deptIds.clear();
        for (String[] d : patient.getDepartments()) {   // {id, name}
            deptIds.add(d[0]);                          // id FIRST: addItem fires the combo's event
            cmbDepartment.addItem(d[1]);
        }
    }

    private void loadDoctorCombo() {
        int index = cmbDepartment.getSelectedIndex();
        if (index < 0) {
            return;
        }
        cmbDoctor.removeAllItems();
        doctorIds.clear();
        for (String[] d : patient.getDoctors(deptIds.get(index))) {
            doctorIds.add(d[0]);
            cmbDoctor.addItem(d[1]);
        }
    }

    private void loadSlots() {
        DefaultTableModel model = (DefaultTableModel) tblSlots.getModel();
        model.setRowCount(0);

        int deptIndex = cmbDepartment.getSelectedIndex();
        int docIndex = cmbDoctor.getSelectedIndex();
        if (deptIndex < 0 || docIndex < 0) {
            return;
        }
        String date = new SimpleDateFormat("dd-MM-yyyy").format((Date) spnDate.getValue());
        patient.loadSlots(deptIds.get(deptIndex), doctorIds.get(docIndex), date);

        for (Object[] row : patient.getSlotRows()) {
            model.addRow(row);
        }
    }

    // =====================================================================
    // 6. MEDICAL HISTORY PAGE
    // =====================================================================
    private void loadHistory() {
        patient.loadHistory();   // the Patient reads the files and builds the rows

        DefaultTableModel visits = (DefaultTableModel) tblVisits.getModel();
        DefaultTableModel rx = (DefaultTableModel) tblPrescriptions.getModel();
        DefaultTableModel tests = (DefaultTableModel) tblTests.getModel();

        visits.setRowCount(0);
        rx.setRowCount(0);
        tests.setRowCount(0);
        txtVisitDetails.setText("");
        txtRxDetails.setText("");
        txtTestDetails.setText("");

        for (Object[] row : patient.getVisitRows()) {
            visits.addRow(row);
        }
        for (Object[] row : patient.getPrescriptionRows()) {
            rx.addRow(row);
        }
        for (Object[] row : patient.getTestRows()) {
            tests.addRow(row);
        }
    }

    // =====================================================================
    // 7. SUBMIT RATINGS PAGE
    // =====================================================================
    private void loadRatings() {
        DefaultTableModel pending = (DefaultTableModel) tblPending.getModel();
        DefaultTableModel past = (DefaultTableModel) tblMyReviews.getModel();
        pending.setRowCount(0);
        past.setRowCount(0);

        for (Object[] row : patient.getPendingRatings()) {
            pending.addRow(row);
        }
        for (Object[] row : patient.getPastRatings()) {
            past.addRow(row);
        }
    }

    private void clearRatingForm() {
        grpRating.clearSelection();
        txtComment.setText("");
        tblPending.clearSelection();
    }

    // =====================================================================
    // 8. MY PROFILE PAGE
    // =====================================================================
    private void loadProfile() {
        txtPatientID.setText(String.valueOf(patient.getUserID()));
        txtFirstName.setText(patient.getFirst());
        txtLastName.setText(patient.getLast());
        txtDOB.setText(patient.getDobText());
        txtGender.setText(patient.getGender());
        txtPhone.setText(patient.getPhone());
        txtEmail.setText(patient.getEmail());
        txtBloodType.setText(patient.getBloodType());
        txtAllergies.setText(patient.getAllergies());

        pwdCurrent.setText("");
        pwdNew.setText("");
        pwdConfirm.setText("");
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message);
        loadProfile();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        grpRating = new javax.swing.ButtonGroup();
        pnlHeader = new javax.swing.JPanel();
        lblPortalTitle = new javax.swing.JLabel();
        lblWelcome = new javax.swing.JLabel();
        pnlNav = new javax.swing.JPanel();
        btnNavBookings = new javax.swing.JButton();
        btnNavHistory = new javax.swing.JButton();
        btnNavRatings = new javax.swing.JButton();
        btnNavProfile = new javax.swing.JButton();
        btnLogout = new javax.swing.JButton();
        pnlContent = new javax.swing.JPanel();
        pnlBookings = new javax.swing.JPanel();
        lblPageTitle = new javax.swing.JLabel();
        lblDepartment = new javax.swing.JLabel();
        cmbDepartment = new javax.swing.JComboBox<>();
        lblDoctor = new javax.swing.JLabel();
        cmbDoctor = new javax.swing.JComboBox<>();
        lblDate = new javax.swing.JLabel();
        spnDate = new javax.swing.JSpinner();
        btnSearch = new javax.swing.JButton();
        lblCategory = new javax.swing.JLabel();
        cmbCategory = new javax.swing.JComboBox<>();
        lblReason = new javax.swing.JLabel();
        btnBook = new javax.swing.JButton();
        btnReschedule = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        scrReason = new javax.swing.JScrollPane();
        txtReason = new javax.swing.JTextArea();
        scrSlots = new javax.swing.JScrollPane();
        tblSlots = new javax.swing.JTable();
        pnlHistory = new javax.swing.JPanel();
        lblHistoryTitle = new javax.swing.JLabel();
        tabHistory = new javax.swing.JTabbedPane();
        pnlTabVisits = new javax.swing.JPanel();
        scrVisits = new javax.swing.JScrollPane();
        tblVisits = new javax.swing.JTable();
        lblVisitDetails = new javax.swing.JLabel();
        scrVisitDetails = new javax.swing.JScrollPane();
        txtVisitDetails = new javax.swing.JTextArea();
        pnlTabPrescription = new javax.swing.JPanel();
        scrPrescriptions = new javax.swing.JScrollPane();
        tblPrescriptions = new javax.swing.JTable();
        lblRxDetails = new javax.swing.JLabel();
        scrRxDetails = new javax.swing.JScrollPane();
        txtRxDetails = new javax.swing.JTextArea();
        pnlTabTests = new javax.swing.JPanel();
        scrTests = new javax.swing.JScrollPane();
        tblTests = new javax.swing.JTable();
        lblTestDetails = new javax.swing.JLabel();
        scrTestDetails = new javax.swing.JScrollPane();
        txtTestDetails = new javax.swing.JTextArea();
        pnlRatings = new javax.swing.JPanel();
        lblRatingsTitle = new javax.swing.JLabel();
        lblPending = new javax.swing.JLabel();
        scrPending = new javax.swing.JScrollPane();
        tblPending = new javax.swing.JTable();
        lblRating = new javax.swing.JLabel();
        rdoRating1 = new javax.swing.JRadioButton();
        rdoRating2 = new javax.swing.JRadioButton();
        rdoRating3 = new javax.swing.JRadioButton();
        rdoRating4 = new javax.swing.JRadioButton();
        rdoRating5 = new javax.swing.JRadioButton();
        lblRatingHint = new javax.swing.JLabel();
        lblComment = new javax.swing.JLabel();
        scrComment = new javax.swing.JScrollPane();
        txtComment = new javax.swing.JTextArea();
        btnSubmitRating = new javax.swing.JButton();
        btnClearRating = new javax.swing.JButton();
        lblMyReviews = new javax.swing.JLabel();
        scrMyReviews = new javax.swing.JScrollPane();
        tblMyReviews = new javax.swing.JTable();
        pnlProfile = new javax.swing.JPanel();
        lblProfileTitle = new javax.swing.JLabel();
        lblPersonalHeader = new javax.swing.JLabel();
        lblContactHeader = new javax.swing.JLabel();
        lblPasswordHeader = new javax.swing.JLabel();
        lblPatientID = new javax.swing.JLabel();
        lblFirstName = new javax.swing.JLabel();
        lblLastName = new javax.swing.JLabel();
        lblDOB = new javax.swing.JLabel();
        lblGender = new javax.swing.JLabel();
        lblBloodType = new javax.swing.JLabel();
        lblAllergies = new javax.swing.JLabel();
        txtPatientID = new javax.swing.JTextField();
        txtFirstName = new javax.swing.JTextField();
        txtLastName = new javax.swing.JTextField();
        txtDOB = new javax.swing.JTextField();
        txtGender = new javax.swing.JTextField();
        txtBloodType = new javax.swing.JTextField();
        scrAllergies = new javax.swing.JScrollPane();
        txtAllergies = new javax.swing.JTextArea();
        lblPhone = new javax.swing.JLabel();
        lblEmail = new javax.swing.JLabel();
        lblCurrentPwd = new javax.swing.JLabel();
        lblNewPwd = new javax.swing.JLabel();
        lblComfirmPwd = new javax.swing.JLabel();
        txtPhone = new javax.swing.JTextField();
        txtEmail = new javax.swing.JTextField();
        pwdCurrent = new javax.swing.JPasswordField();
        pwdNew = new javax.swing.JPasswordField();
        pwdConfirm = new javax.swing.JPasswordField();
        lblPwdHint = new javax.swing.JLabel();
        btnSaveProfile = new javax.swing.JButton();
        btnResetProfile = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("HMS Patient Portal");
        setResizable(false);
        getContentPane().setLayout(null);

        pnlHeader.setBackground(new java.awt.Color(38, 117, 154));
        pnlHeader.setLayout(null);

        lblPortalTitle.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblPortalTitle.setForeground(new java.awt.Color(255, 255, 255));
        lblPortalTitle.setText("APU Medical Centre - Patient Portal");
        pnlHeader.add(lblPortalTitle);
        lblPortalTitle.setBounds(20, 10, 450, 30);

        lblWelcome.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lblWelcome.setForeground(new java.awt.Color(255, 255, 255));
        lblWelcome.setText("Welcome, Patient");
        lblWelcome.setToolTipText("");
        pnlHeader.add(lblWelcome);
        lblWelcome.setBounds(470, 10, 300, 30);

        getContentPane().add(pnlHeader);
        pnlHeader.setBounds(0, 0, 800, 50);

        pnlNav.setBackground(new java.awt.Color(30, 95, 125));
        pnlNav.setLayout(null);

        btnNavBookings.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnNavBookings.setForeground(new java.awt.Color(38, 117, 154));
        btnNavBookings.setText("Bookings and Slots");
        btnNavBookings.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 15, 0, 0));
        btnNavBookings.setFocusPainted(false);
        btnNavBookings.setFocusable(false);
        btnNavBookings.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnNavBookings.setRolloverEnabled(false);
        btnNavBookings.addActionListener(this::btnNavBookingsActionPerformed);
        pnlNav.add(btnNavBookings);
        btnNavBookings.setBounds(10, 20, 160, 40);

        btnNavHistory.setBackground(new java.awt.Color(30, 95, 125));
        btnNavHistory.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnNavHistory.setForeground(new java.awt.Color(255, 255, 255));
        btnNavHistory.setText("Medical History");
        btnNavHistory.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 15, 0, 0));
        btnNavHistory.setFocusPainted(false);
        btnNavHistory.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnNavHistory.setRolloverEnabled(false);
        btnNavHistory.addActionListener(this::btnNavHistoryActionPerformed);
        pnlNav.add(btnNavHistory);
        btnNavHistory.setBounds(10, 70, 160, 40);

        btnNavRatings.setBackground(new java.awt.Color(30, 95, 125));
        btnNavRatings.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnNavRatings.setForeground(new java.awt.Color(255, 255, 255));
        btnNavRatings.setText("Submit Ratings");
        btnNavRatings.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 15, 0, 0));
        btnNavRatings.setFocusPainted(false);
        btnNavRatings.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnNavRatings.setRolloverEnabled(false);
        btnNavRatings.addActionListener(this::btnNavRatingsActionPerformed);
        pnlNav.add(btnNavRatings);
        btnNavRatings.setBounds(10, 120, 160, 40);

        btnNavProfile.setBackground(new java.awt.Color(30, 95, 125));
        btnNavProfile.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnNavProfile.setForeground(new java.awt.Color(255, 255, 255));
        btnNavProfile.setText("My Profile");
        btnNavProfile.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 15, 0, 0));
        btnNavProfile.setFocusPainted(false);
        btnNavProfile.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnNavProfile.setRolloverEnabled(false);
        btnNavProfile.addActionListener(this::btnNavProfileActionPerformed);
        pnlNav.add(btnNavProfile);
        btnNavProfile.setBounds(10, 170, 160, 40);

        btnLogout.setBackground(new java.awt.Color(30, 95, 125));
        btnLogout.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnLogout.setText("LOGOUT");
        btnLogout.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        btnLogout.setFocusPainted(false);
        btnLogout.setRolloverEnabled(false);
        btnLogout.addActionListener(this::btnLogoutActionPerformed);
        pnlNav.add(btnLogout);
        btnLogout.setBounds(10, 450, 160, 40);

        getContentPane().add(pnlNav);
        pnlNav.setBounds(0, 50, 180, 550);

        pnlContent.setBackground(new java.awt.Color(255, 255, 255));
        pnlContent.setLayout(new java.awt.CardLayout());

        pnlBookings.setBackground(new java.awt.Color(255, 255, 255));
        pnlBookings.setLayout(null);

        lblPageTitle.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        lblPageTitle.setForeground(new java.awt.Color(17, 17, 17));
        lblPageTitle.setText("Browse Consultations");
        pnlBookings.add(lblPageTitle);
        lblPageTitle.setBounds(20, 15, 400, 30);

        lblDepartment.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblDepartment.setText("Department");
        pnlBookings.add(lblDepartment);
        lblDepartment.setBounds(20, 60, 85, 28);

        cmbDepartment.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbDepartment.addActionListener(this::cmbDepartmentActionPerformed);
        pnlBookings.add(cmbDepartment);
        cmbDepartment.setBounds(105, 60, 175, 28);

        lblDoctor.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblDoctor.setText("Doctor");
        pnlBookings.add(lblDoctor);
        lblDoctor.setBounds(300, 60, 55, 28);

        cmbDoctor.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        pnlBookings.add(cmbDoctor);
        cmbDoctor.setBounds(355, 60, 230, 28);

        lblDate.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblDate.setText("Date:");
        pnlBookings.add(lblDate);
        lblDate.setBounds(20, 98, 85, 28);

        spnDate.setModel(new javax.swing.SpinnerDateModel());
        pnlBookings.add(spnDate);
        spnDate.setBounds(105, 98, 175, 28);

        btnSearch.setBackground(new java.awt.Color(38, 117, 154));
        btnSearch.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnSearch.setForeground(new java.awt.Color(255, 255, 255));
        btnSearch.setText("Search");
        btnSearch.setFocusPainted(false);
        btnSearch.setRolloverEnabled(false);
        btnSearch.addActionListener(this::btnSearchActionPerformed);
        pnlBookings.add(btnSearch);
        btnSearch.setBounds(300, 98, 100, 28);

        lblCategory.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblCategory.setText("Category:");
        pnlBookings.add(lblCategory);
        lblCategory.setBounds(20, 380, 70, 28);

        cmbCategory.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Diagnosis", "Healthcheck", "Immunization" }));
        pnlBookings.add(cmbCategory);
        cmbCategory.setBounds(90, 380, 160, 28);

        lblReason.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblReason.setText("Reason:");
        pnlBookings.add(lblReason);
        lblReason.setBounds(265, 380, 60, 28);

        btnBook.setBackground(new java.awt.Color(38, 117, 154));
        btnBook.setForeground(new java.awt.Color(255, 255, 255));
        btnBook.setText("BOOK SELECETED");
        btnBook.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(38, 117, 154), 1, true));
        btnBook.setFocusPainted(false);
        btnBook.setRolloverEnabled(false);
        btnBook.addActionListener(this::btnBookActionPerformed);
        pnlBookings.add(btnBook);
        btnBook.setBounds(20, 450, 182, 36);

        btnReschedule.setForeground(new java.awt.Color(38, 117, 154));
        btnReschedule.setText("RESCHEDULE");
        btnReschedule.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(38, 117, 154), 2));
        btnReschedule.setFocusPainted(false);
        btnReschedule.setRolloverEnabled(false);
        btnReschedule.addActionListener(this::btnRescheduleActionPerformed);
        pnlBookings.add(btnReschedule);
        btnReschedule.setBounds(212, 450, 182, 36);

        btnCancel.setForeground(new java.awt.Color(192, 57, 43));
        btnCancel.setText("CANCEL");
        btnCancel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(192, 57, 43), 2));
        btnCancel.setFocusPainted(false);
        btnCancel.setRolloverEnabled(false);
        btnCancel.addActionListener(this::btnCancelActionPerformed);
        pnlBookings.add(btnCancel);
        btnCancel.setBounds(404, 450, 182, 36);

        txtReason.setColumns(20);
        txtReason.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtReason.setLineWrap(true);
        txtReason.setRows(5);
        txtReason.setWrapStyleWord(true);
        scrReason.setViewportView(txtReason);

        pnlBookings.add(scrReason);
        scrReason.setBounds(325, 380, 261, 55);

        tblSlots.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        tblSlots.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Date", "Time", "Doctor", "Specialization", "Room", "Status"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblSlots.setRowHeight(25);
        tblSlots.setSelectionBackground(new java.awt.Color(38, 117, 154));
        tblSlots.setSelectionForeground(new java.awt.Color(255, 255, 255));
        tblSlots.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        scrSlots.setViewportView(tblSlots);

        pnlBookings.add(scrSlots);
        scrSlots.setBounds(20, 140, 566, 230);

        pnlContent.add(pnlBookings, "bookings");

        pnlHistory.setBackground(new java.awt.Color(255, 255, 255));
        pnlHistory.setLayout(null);

        lblHistoryTitle.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        lblHistoryTitle.setForeground(new java.awt.Color(17, 17, 17));
        lblHistoryTitle.setText("Medical History");
        pnlHistory.add(lblHistoryTitle);
        lblHistoryTitle.setBounds(20, 15, 400, 30);

        tabHistory.setForeground(new java.awt.Color(38, 117, 154));
        tabHistory.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        pnlTabVisits.setLayout(null);

        tblVisits.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        tblVisits.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Date", "Doctor", "Category", "Complaint"
            }
        ));
        tblVisits.setRowHeight(25);
        tblVisits.setSelectionBackground(new java.awt.Color(38, 117, 154));
        tblVisits.setSelectionForeground(new java.awt.Color(255, 255, 255));
        tblVisits.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tblVisits.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblVisitsMouseClicked(evt);
            }
        });
        scrVisits.setViewportView(tblVisits);

        pnlTabVisits.add(scrVisits);
        scrVisits.setBounds(10, 10, 540, 170);

        lblVisitDetails.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblVisitDetails.setForeground(new java.awt.Color(38, 117, 154));
        lblVisitDetails.setText("Visit Details");
        pnlTabVisits.add(lblVisitDetails);
        lblVisitDetails.setBounds(10, 190, 300, 22);

        txtVisitDetails.setEditable(false);
        txtVisitDetails.setBackground(new java.awt.Color(245, 248, 250));
        txtVisitDetails.setColumns(20);
        txtVisitDetails.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtVisitDetails.setLineWrap(true);
        txtVisitDetails.setRows(5);
        txtVisitDetails.setWrapStyleWord(true);
        scrVisitDetails.setViewportView(txtVisitDetails);

        pnlTabVisits.add(scrVisitDetails);
        scrVisitDetails.setBounds(10, 215, 540, 180);

        tabHistory.addTab("Visits", pnlTabVisits);

        pnlTabPrescription.setLayout(null);

        tblPrescriptions.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        tblPrescriptions.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Date", "Medicine", "Dosage", "Frequency", "Duration"
            }
        ));
        tblPrescriptions.setRowHeight(25);
        tblPrescriptions.setSelectionBackground(new java.awt.Color(38, 117, 154));
        tblPrescriptions.setSelectionForeground(new java.awt.Color(255, 255, 255));
        tblPrescriptions.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tblPrescriptions.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblPrescriptionsMouseClicked(evt);
            }
        });
        scrPrescriptions.setViewportView(tblPrescriptions);

        pnlTabPrescription.add(scrPrescriptions);
        scrPrescriptions.setBounds(10, 10, 540, 170);

        lblRxDetails.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblRxDetails.setForeground(new java.awt.Color(38, 117, 154));
        lblRxDetails.setText("Instructions");
        pnlTabPrescription.add(lblRxDetails);
        lblRxDetails.setBounds(10, 190, 300, 22);

        txtRxDetails.setEditable(false);
        txtRxDetails.setBackground(new java.awt.Color(245, 248, 250));
        txtRxDetails.setColumns(20);
        txtRxDetails.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtRxDetails.setLineWrap(true);
        txtRxDetails.setRows(5);
        txtRxDetails.setWrapStyleWord(true);
        scrRxDetails.setViewportView(txtRxDetails);

        pnlTabPrescription.add(scrRxDetails);
        scrRxDetails.setBounds(10, 215, 540, 180);

        tabHistory.addTab("Prescription", pnlTabPrescription);

        pnlTabTests.setLayout(null);

        tblTests.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        tblTests.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Request Date", "Test", "Category", "Result Date", "Status"
            }
        ));
        tblTests.setRowHeight(25);
        tblTests.setSelectionBackground(new java.awt.Color(38, 117, 154));
        tblTests.setSelectionForeground(new java.awt.Color(255, 255, 255));
        tblTests.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tblTests.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblTestsMouseClicked(evt);
            }
        });
        scrTests.setViewportView(tblTests);

        pnlTabTests.add(scrTests);
        scrTests.setBounds(10, 10, 540, 170);

        lblTestDetails.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblTestDetails.setForeground(new java.awt.Color(38, 117, 154));
        lblTestDetails.setText("Test Result");
        pnlTabTests.add(lblTestDetails);
        lblTestDetails.setBounds(10, 190, 300, 22);

        txtTestDetails.setEditable(false);
        txtTestDetails.setBackground(new java.awt.Color(245, 248, 250));
        txtTestDetails.setColumns(20);
        txtTestDetails.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtTestDetails.setLineWrap(true);
        txtTestDetails.setRows(5);
        txtTestDetails.setWrapStyleWord(true);
        scrTestDetails.setViewportView(txtTestDetails);

        pnlTabTests.add(scrTestDetails);
        scrTestDetails.setBounds(10, 215, 540, 180);

        tabHistory.addTab("Test Results", pnlTabTests);

        pnlHistory.add(tabHistory);
        tabHistory.setBounds(20, 55, 566, 440);

        pnlContent.add(pnlHistory, "history");

        pnlRatings.setBackground(new java.awt.Color(255, 255, 255));
        pnlRatings.setLayout(null);

        lblRatingsTitle.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        lblRatingsTitle.setForeground(new java.awt.Color(17, 17, 17));
        lblRatingsTitle.setText("Submit Ratings");
        pnlRatings.add(lblRatingsTitle);
        lblRatingsTitle.setBounds(20, 15, 400, 30);

        lblPending.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblPending.setForeground(new java.awt.Color(17, 17, 17));
        lblPending.setText("Visits waiting for your rating");
        pnlRatings.add(lblPending);
        lblPending.setBounds(20, 52, 300, 22);

        tblPending.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Visit ID", "Date", "Doctor", "Complaint"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        scrPending.setViewportView(tblPending);

        pnlRatings.add(scrPending);
        scrPending.setBounds(20, 76, 566, 110);

        lblRating.setText("Rating:");
        pnlRatings.add(lblRating);
        lblRating.setBounds(20, 196, 60, 28);

        grpRating.add(rdoRating1);
        rdoRating1.setText("1");
        rdoRating1.setFocusPainted(false);
        pnlRatings.add(rdoRating1);
        rdoRating1.setBounds(85, 196, 45, 28);

        grpRating.add(rdoRating2);
        rdoRating2.setText("2");
        rdoRating2.setFocusPainted(false);
        pnlRatings.add(rdoRating2);
        rdoRating2.setBounds(135, 196, 45, 28);

        grpRating.add(rdoRating3);
        rdoRating3.setText("3");
        rdoRating3.setFocusPainted(false);
        pnlRatings.add(rdoRating3);
        rdoRating3.setBounds(185, 196, 45, 28);

        grpRating.add(rdoRating4);
        rdoRating4.setText("4");
        rdoRating4.setFocusPainted(false);
        pnlRatings.add(rdoRating4);
        rdoRating4.setBounds(235, 196, 45, 28);

        grpRating.add(rdoRating5);
        rdoRating5.setText("5");
        rdoRating5.setFocusPainted(false);
        pnlRatings.add(rdoRating5);
        rdoRating5.setBounds(285, 196, 45, 28);

        lblRatingHint.setText("(1 = Poor, 5 = Excellent)");
        pnlRatings.add(lblRatingHint);
        lblRatingHint.setBounds(340, 196, 246, 28);

        lblComment.setText("Comment:");
        pnlRatings.add(lblComment);
        lblComment.setBounds(20, 230, 80, 22);

        txtComment.setColumns(20);
        txtComment.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtComment.setLineWrap(true);
        txtComment.setRows(5);
        txtComment.setWrapStyleWord(true);
        scrComment.setViewportView(txtComment);

        pnlRatings.add(scrComment);
        scrComment.setBounds(20, 254, 566, 50);

        btnSubmitRating.setBackground(new java.awt.Color(38, 117, 154));
        btnSubmitRating.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnSubmitRating.setForeground(new java.awt.Color(255, 255, 255));
        btnSubmitRating.setText("Submit Rating");
        btnSubmitRating.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(38, 117, 154)));
        btnSubmitRating.setFocusPainted(false);
        btnSubmitRating.setRolloverEnabled(false);
        btnSubmitRating.addActionListener(this::btnSubmitRatingActionPerformed);
        pnlRatings.add(btnSubmitRating);
        btnSubmitRating.setBounds(20, 314, 182, 34);

        btnClearRating.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnClearRating.setForeground(new java.awt.Color(38, 117, 154));
        btnClearRating.setText("Clear");
        btnClearRating.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(38, 117, 154), 2, true));
        btnClearRating.setFocusPainted(false);
        btnClearRating.setRolloverEnabled(false);
        btnClearRating.addActionListener(this::btnClearRatingActionPerformed);
        pnlRatings.add(btnClearRating);
        btnClearRating.setBounds(212, 314, 182, 34);

        lblMyReviews.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblMyReviews.setForeground(new java.awt.Color(38, 117, 154));
        lblMyReviews.setText("Your Past Ratings:");
        pnlRatings.add(lblMyReviews);
        lblMyReviews.setBounds(20, 360, 300, 22);

        tblMyReviews.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Date", "Doctor", "Rating", "Comment"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        scrMyReviews.setViewportView(tblMyReviews);

        pnlRatings.add(scrMyReviews);
        scrMyReviews.setBounds(20, 384, 566, 115);

        pnlContent.add(pnlRatings, "ratings");

        pnlProfile.setBackground(new java.awt.Color(255, 255, 255));
        pnlProfile.setLayout(null);

        lblProfileTitle.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        lblProfileTitle.setForeground(new java.awt.Color(17, 17, 17));
        lblProfileTitle.setText("My Profile");
        pnlProfile.add(lblProfileTitle);
        lblProfileTitle.setBounds(20, 15, 400, 30);

        lblPersonalHeader.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblPersonalHeader.setForeground(new java.awt.Color(38, 117, 154));
        lblPersonalHeader.setText("Personal Details");
        pnlProfile.add(lblPersonalHeader);
        lblPersonalHeader.setBounds(20, 55, 275, 22);

        lblContactHeader.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblContactHeader.setForeground(new java.awt.Color(38, 117, 154));
        lblContactHeader.setText("Contact Details");
        pnlProfile.add(lblContactHeader);
        lblContactHeader.setBounds(311, 55, 275, 22);

        lblPasswordHeader.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblPasswordHeader.setForeground(new java.awt.Color(38, 117, 154));
        lblPasswordHeader.setText("Change Password");
        pnlProfile.add(lblPasswordHeader);
        lblPasswordHeader.setBounds(311, 209, 275, 22);

        lblPatientID.setText("PatientID");
        pnlProfile.add(lblPatientID);
        lblPatientID.setBounds(20, 85, 48, 16);

        lblFirstName.setText("First Name");
        pnlProfile.add(lblFirstName);
        lblFirstName.setBounds(20, 141, 57, 16);

        lblLastName.setText("Last Name");
        pnlProfile.add(lblLastName);
        lblLastName.setBounds(20, 197, 55, 16);

        lblDOB.setText("Date Of Birth");
        pnlProfile.add(lblDOB);
        lblDOB.setBounds(20, 253, 69, 16);

        lblGender.setText("Gender");
        pnlProfile.add(lblGender);
        lblGender.setBounds(20, 309, 38, 16);

        lblBloodType.setText("Blood Type");
        pnlProfile.add(lblBloodType);
        lblBloodType.setBounds(20, 365, 60, 16);

        lblAllergies.setText("Allergies");
        pnlProfile.add(lblAllergies);
        lblAllergies.setBounds(20, 421, 45, 16);

        txtPatientID.setEditable(false);
        pnlProfile.add(txtPatientID);
        txtPatientID.setBounds(20, 105, 275, 28);

        txtFirstName.setEditable(false);
        pnlProfile.add(txtFirstName);
        txtFirstName.setBounds(20, 161, 275, 28);

        txtLastName.setEditable(false);
        pnlProfile.add(txtLastName);
        txtLastName.setBounds(20, 217, 275, 28);

        txtDOB.setEditable(false);
        pnlProfile.add(txtDOB);
        txtDOB.setBounds(20, 273, 275, 28);

        txtGender.setEditable(false);
        pnlProfile.add(txtGender);
        txtGender.setBounds(20, 329, 275, 28);

        txtBloodType.setEditable(false);
        pnlProfile.add(txtBloodType);
        txtBloodType.setBounds(20, 385, 275, 28);

        txtAllergies.setEditable(false);
        txtAllergies.setColumns(20);
        txtAllergies.setRows(5);
        scrAllergies.setViewportView(txtAllergies);

        pnlProfile.add(scrAllergies);
        scrAllergies.setBounds(20, 441, 280, 50);

        lblPhone.setText("Phone");
        pnlProfile.add(lblPhone);
        lblPhone.setBounds(311, 85, 275, 18);

        lblEmail.setText("Email");
        pnlProfile.add(lblEmail);
        lblEmail.setBounds(311, 141, 275, 18);

        lblCurrentPwd.setText("Current Password");
        pnlProfile.add(lblCurrentPwd);
        lblCurrentPwd.setBounds(311, 239, 275, 18);

        lblNewPwd.setText("New Password");
        pnlProfile.add(lblNewPwd);
        lblNewPwd.setBounds(311, 295, 275, 18);

        lblComfirmPwd.setText("Confirm New Password");
        pnlProfile.add(lblComfirmPwd);
        lblComfirmPwd.setBounds(311, 351, 275, 18);
        pnlProfile.add(txtPhone);
        txtPhone.setBounds(311, 105, 275, 28);
        pnlProfile.add(txtEmail);
        txtEmail.setBounds(311, 161, 275, 28);
        pnlProfile.add(pwdCurrent);
        pwdCurrent.setBounds(311, 259, 275, 28);
        pnlProfile.add(pwdNew);
        pwdNew.setBounds(311, 315, 275, 28);
        pnlProfile.add(pwdConfirm);
        pwdConfirm.setBounds(311, 371, 275, 28);

        lblPwdHint.setText("Leave blank to keep current password");
        pnlProfile.add(lblPwdHint);
        lblPwdHint.setBounds(311, 403, 275, 22);

        btnSaveProfile.setBackground(new java.awt.Color(38, 117, 154));
        btnSaveProfile.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnSaveProfile.setForeground(new java.awt.Color(255, 255, 255));
        btnSaveProfile.setText("Save");
        btnSaveProfile.addActionListener(this::btnSaveProfileActionPerformed);
        pnlProfile.add(btnSaveProfile);
        btnSaveProfile.setBounds(311, 441, 132, 36);

        btnResetProfile.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnResetProfile.setForeground(new java.awt.Color(38, 117, 154));
        btnResetProfile.setText("Reset");
        btnResetProfile.setFocusPainted(false);
        btnResetProfile.setRequestFocusEnabled(false);
        btnResetProfile.addActionListener(this::btnResetProfileActionPerformed);
        pnlProfile.add(btnResetProfile);
        btnResetProfile.setBounds(454, 441, 132, 36);

        pnlContent.add(pnlProfile, "profile");

        getContentPane().add(pnlContent);
        pnlContent.setBounds(180, 50, 620, 550);

        setBounds(0, 0, 816, 609);
    }// </editor-fold>//GEN-END:initComponents

// ===== EVENTS: NAVIGATION & LOGOUT =====
    private void btnNavBookingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNavBookingsActionPerformed
        loadSlots();
        showPage("bookings", btnNavBookings);
    }//GEN-LAST:event_btnNavBookingsActionPerformed

    private void btnNavHistoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNavHistoryActionPerformed

        loadHistory();
        showPage("history", btnNavHistory);
    }//GEN-LAST:event_btnNavHistoryActionPerformed

    private void btnNavRatingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNavRatingsActionPerformed

        loadRatings();
        showPage("ratings", btnNavRatings);
    }//GEN-LAST:event_btnNavRatingsActionPerformed
// ===== EVENTS: MY PROFILE =====
    private void btnNavProfileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNavProfileActionPerformed

        showPage("profile", btnNavProfile);
    }//GEN-LAST:event_btnNavProfileActionPerformed

    private void btnLogoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogoutActionPerformed

        int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to log out?", "Logout", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            SessionUser.logout();
            new UserLogin().setVisible(true);
            dispose();
        }
    }//GEN-LAST:event_btnLogoutActionPerformed

    private void btnSaveProfileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveProfileActionPerformed

        String result = patient.saveProfile(
                txtPhone.getText().trim(),
                txtEmail.getText().trim(),
                new String(pwdCurrent.getPassword()),
                new String(pwdNew.getPassword()),
                new String(pwdConfirm.getPassword()));

        if (result != null) {
            showError(result);
            return;
        }

        JOptionPane.showMessageDialog(this, "Profile updated.");
        loadProfile();
    }//GEN-LAST:event_btnSaveProfileActionPerformed

    private void btnResetProfileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResetProfileActionPerformed

        patient.reload();
        loadProfile();
    }//GEN-LAST:event_btnResetProfileActionPerformed
// ===== EVENTS: MEDICAL HISTORY =====
    private void tblVisitsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblVisitsMouseClicked

        int row = tblVisits.getSelectedRow();
        if (row >= 0) {
            txtVisitDetails.setText(patient.getVisitDetails().get(row));
            txtVisitDetails.setCaretPosition(0);
        }
    }//GEN-LAST:event_tblVisitsMouseClicked

    private void tblPrescriptionsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblPrescriptionsMouseClicked

        int row = tblPrescriptions.getSelectedRow();
        if (row >= 0) {
            txtRxDetails.setText(patient.getPrescriptionDetails().get(row));
            txtRxDetails.setCaretPosition(0);
        }
    }//GEN-LAST:event_tblPrescriptionsMouseClicked

    private void tblTestsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblTestsMouseClicked

        int row = tblTests.getSelectedRow();
        if (row >= 0) {
            txtTestDetails.setText(patient.getTestDetails().get(row));
            txtTestDetails.setCaretPosition(0);
        }
    }//GEN-LAST:event_tblTestsMouseClicked
// ===== EVENTS: SUBMIT RATINGS =====
    private void btnSubmitRatingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSubmitRatingActionPerformed

        int row = tblPending.getSelectedRow();
        String consultId = "";
        if (row >= 0) {
            consultId = tblPending.getValueAt(row, 0).toString();
        }

        int rating = 0;
        if (rdoRating1.isSelected()) {
            rating = 1;
        }
        if (rdoRating2.isSelected()) {
            rating = 2;
        }
        if (rdoRating3.isSelected()) {
            rating = 3;
        }
        if (rdoRating4.isSelected()) {
            rating = 4;
        }
        if (rdoRating5.isSelected()) {
            rating = 5;
        }

        String result = patient.submitRating(consultId, rating, txtComment.getText());
        if (result != null) {
            JOptionPane.showMessageDialog(this, result);
            return;
        }

        JOptionPane.showMessageDialog(this, "Thank you! Your rating has been submitted.");
        clearRatingForm();
        loadRatings();
    }//GEN-LAST:event_btnSubmitRatingActionPerformed

    private void btnClearRatingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearRatingActionPerformed

        clearRatingForm();
    }//GEN-LAST:event_btnClearRatingActionPerformed

    private void cmbDepartmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbDepartmentActionPerformed
        loadDoctorCombo();
    }//GEN-LAST:event_cmbDepartmentActionPerformed

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        loadSlots();
    }//GEN-LAST:event_btnSearchActionPerformed

    private void btnBookActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBookActionPerformed
        int row = tblSlots.getSelectedRow();
        String[] info = null;
        if (row >= 0) {
            info = patient.getSlotInfo().get(row);
        }
        if (info == null || !info[0].equals("available")) {
            JOptionPane.showMessageDialog(this, "Please select a slot marked Available.");
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
                "Book " + patient.getDoctorName(info[1]) + " on " + info[2] + " at " + info[3] + "?",
                "Confirm Booking", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        String result = patient.bookSlot(info, cmbCategory.getSelectedItem().toString(), txtReason.getText());
        if (result != null) {
            JOptionPane.showMessageDialog(this, result);
            loadSlots();
            return;
        }

        JOptionPane.showMessageDialog(this, "Booked! Please go to Room " + patient.getLastBookedRoom() + ".");
        txtReason.setText("");
        loadSlots();
    }//GEN-LAST:event_btnBookActionPerformed

    private void btnRescheduleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRescheduleActionPerformed
        int row = tblSlots.getSelectedRow();
        if (row < 0 || !patient.getSlotInfo().get(row)[0].equals("booked")) {
            JOptionPane.showMessageDialog(this, "Please select one of your bookings (Booked (You)).");
            return;
        }
        int consultId = Integer.parseInt(patient.getSlotInfo().get(row)[1]);
        String doctorId = patient.getSlotInfo().get(row)[2];
        String date = new SimpleDateFormat("dd-MM-yyyy").format((Date) spnDate.getValue());

        ArrayList<String> options = patient.getFreeTimes(doctorId, date, consultId);
        if (options.isEmpty()) {
            JOptionPane.showMessageDialog(this, patient.getDoctorName(doctorId) + " has no free slots on " + date
                    + ".\nChange the Date above and try again.");
            return;
        }

        Object picked = JOptionPane.showInputDialog(this,
                "Choose a new time with " + patient.getDoctorName(doctorId) + " on " + date
                + "\n(Change the Date above to see other days)",
                "Reschedule", JOptionPane.QUESTION_MESSAGE, null, options.toArray(), options.get(0));
        if (picked == null) {
            return;
        }
        String start = picked.toString().substring(0, 5);
        String end = picked.toString().substring(8, 13);

        String result = patient.rescheduleBooking(consultId, doctorId, date, start, end);
        if (result != null) {
            JOptionPane.showMessageDialog(this, result);
            return;
        }

        JOptionPane.showMessageDialog(this, "Rescheduled to " + date + " at " + start
                + " (Room " + patient.getLastBookedRoom() + ").");
        loadSlots();
    }//GEN-LAST:event_btnRescheduleActionPerformed

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        int row = tblSlots.getSelectedRow();
        if (row < 0 || !patient.getSlotInfo().get(row)[0].equals("booked")) {
            JOptionPane.showMessageDialog(this, "Please select one of your bookings (Booked (You)).");
            return;
        }
        int consultId = Integer.parseInt(patient.getSlotInfo().get(row)[1]);

        int choice = JOptionPane.showConfirmDialog(this, "Cancel this booking? This cannot be undone.",
                "Cancel Booking", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        String result = patient.cancelBooking(consultId);
        if (result != null) {
            JOptionPane.showMessageDialog(this, result);
            return;
        }

        JOptionPane.showMessageDialog(this, "Booking cancelled.");
        loadSlots();
    }//GEN-LAST:event_btnCancelActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        // Hide the focus box on buttons and radio buttons (also in pop-up dialogs)
        javax.swing.UIManager.put("Button.focus", new java.awt.Color(0, 0, 0, 0));
        javax.swing.UIManager.put("RadioButton.focus", new java.awt.Color(0, 0, 0, 0));

        // Use the Metal look and feel so our button colours show
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Metal".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        // Test run: open the dashboard for patient 40java.awt.Event
        java.awt.EventQueue.invokeLater(() -> new Users.UserLogin().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBook;
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnClearRating;
    private javax.swing.JButton btnLogout;
    private javax.swing.JButton btnNavBookings;
    private javax.swing.JButton btnNavHistory;
    private javax.swing.JButton btnNavProfile;
    private javax.swing.JButton btnNavRatings;
    private javax.swing.JButton btnReschedule;
    private javax.swing.JButton btnResetProfile;
    private javax.swing.JButton btnSaveProfile;
    private javax.swing.JButton btnSearch;
    private javax.swing.JButton btnSubmitRating;
    private javax.swing.JComboBox<String> cmbCategory;
    private javax.swing.JComboBox<String> cmbDepartment;
    private javax.swing.JComboBox<String> cmbDoctor;
    private javax.swing.ButtonGroup grpRating;
    private javax.swing.JLabel lblAllergies;
    private javax.swing.JLabel lblBloodType;
    private javax.swing.JLabel lblCategory;
    private javax.swing.JLabel lblComfirmPwd;
    private javax.swing.JLabel lblComment;
    private javax.swing.JLabel lblContactHeader;
    private javax.swing.JLabel lblCurrentPwd;
    private javax.swing.JLabel lblDOB;
    private javax.swing.JLabel lblDate;
    private javax.swing.JLabel lblDepartment;
    private javax.swing.JLabel lblDoctor;
    private javax.swing.JLabel lblEmail;
    private javax.swing.JLabel lblFirstName;
    private javax.swing.JLabel lblGender;
    private javax.swing.JLabel lblHistoryTitle;
    private javax.swing.JLabel lblLastName;
    private javax.swing.JLabel lblMyReviews;
    private javax.swing.JLabel lblNewPwd;
    private javax.swing.JLabel lblPageTitle;
    private javax.swing.JLabel lblPasswordHeader;
    private javax.swing.JLabel lblPatientID;
    private javax.swing.JLabel lblPending;
    private javax.swing.JLabel lblPersonalHeader;
    private javax.swing.JLabel lblPhone;
    private javax.swing.JLabel lblPortalTitle;
    private javax.swing.JLabel lblProfileTitle;
    private javax.swing.JLabel lblPwdHint;
    private javax.swing.JLabel lblRating;
    private javax.swing.JLabel lblRatingHint;
    private javax.swing.JLabel lblRatingsTitle;
    private javax.swing.JLabel lblReason;
    private javax.swing.JLabel lblRxDetails;
    private javax.swing.JLabel lblTestDetails;
    private javax.swing.JLabel lblVisitDetails;
    private javax.swing.JLabel lblWelcome;
    private javax.swing.JPanel pnlBookings;
    private javax.swing.JPanel pnlContent;
    private javax.swing.JPanel pnlHeader;
    private javax.swing.JPanel pnlHistory;
    private javax.swing.JPanel pnlNav;
    private javax.swing.JPanel pnlProfile;
    private javax.swing.JPanel pnlRatings;
    private javax.swing.JPanel pnlTabPrescription;
    private javax.swing.JPanel pnlTabTests;
    private javax.swing.JPanel pnlTabVisits;
    private javax.swing.JPasswordField pwdConfirm;
    private javax.swing.JPasswordField pwdCurrent;
    private javax.swing.JPasswordField pwdNew;
    private javax.swing.JRadioButton rdoRating1;
    private javax.swing.JRadioButton rdoRating2;
    private javax.swing.JRadioButton rdoRating3;
    private javax.swing.JRadioButton rdoRating4;
    private javax.swing.JRadioButton rdoRating5;
    private javax.swing.JScrollPane scrAllergies;
    private javax.swing.JScrollPane scrComment;
    private javax.swing.JScrollPane scrMyReviews;
    private javax.swing.JScrollPane scrPending;
    private javax.swing.JScrollPane scrPrescriptions;
    private javax.swing.JScrollPane scrReason;
    private javax.swing.JScrollPane scrRxDetails;
    private javax.swing.JScrollPane scrSlots;
    private javax.swing.JScrollPane scrTestDetails;
    private javax.swing.JScrollPane scrTests;
    private javax.swing.JScrollPane scrVisitDetails;
    private javax.swing.JScrollPane scrVisits;
    private javax.swing.JSpinner spnDate;
    private javax.swing.JTabbedPane tabHistory;
    private javax.swing.JTable tblMyReviews;
    private javax.swing.JTable tblPending;
    private javax.swing.JTable tblPrescriptions;
    private javax.swing.JTable tblSlots;
    private javax.swing.JTable tblTests;
    private javax.swing.JTable tblVisits;
    private javax.swing.JTextArea txtAllergies;
    private javax.swing.JTextField txtBloodType;
    private javax.swing.JTextArea txtComment;
    private javax.swing.JTextField txtDOB;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtFirstName;
    private javax.swing.JTextField txtGender;
    private javax.swing.JTextField txtLastName;
    private javax.swing.JTextField txtPatientID;
    private javax.swing.JTextField txtPhone;
    private javax.swing.JTextArea txtReason;
    private javax.swing.JTextArea txtRxDetails;
    private javax.swing.JTextArea txtTestDetails;
    private javax.swing.JTextArea txtVisitDetails;
    // End of variables declaration//GEN-END:variables
}
