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
    // =====================================================================
    private int patientId;                                // logged-in patient (from PatientLogin)
    private final Color NAV_BG = new Color(30, 95, 125);  // menu background
    private final Color BLUE = new Color(38, 117, 154);   // theme blue

    // Medical History: one details text per table row (same order as the rows)
    private ArrayList<String> visitDetails = new ArrayList<>();
    private ArrayList<String> rxDetails = new ArrayList<>();
    private ArrayList<String> testDetails = new ArrayList<>();

    // Bookings: the combos show names, these lists hold the matching ids (same order)
    private ArrayList<String> deptIds = new ArrayList<>();
    private ArrayList<String> doctorIds = new ArrayList<>();
    // Bookings: hidden info for each table row
    //   {"booked", consultId, doctorId}  or  {"available", doctorId, date, start, end}
    private ArrayList<String[]> slotRows = new ArrayList<>();

    // =====================================================================
    // 2. CONSTRUCTOR - opens the dashboard for one patient and loads every page
    // =====================================================================
    public PatientDashboard(int patientId) {
        initComponents();
        this.patientId = patientId;
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
    // 3. SHARED HELPERS - used by more than one page
    // =====================================================================
    // Returns "Dr. First Last" for a doctor id, or "Unknown"
    private String getDoctorName(String doctorId) {
        TreeMap<Integer, ArrayList<String>> users = FileHandling.readAllRecords("Users.txt");
        int id = Integer.parseInt(doctorId);
        if (users == null || !users.containsKey(id)) {
            return "Unknown";
        }
        // no id: 0 first name, 1 last name
        return "Dr. " + users.get(id).get(0) + " " + users.get(id).get(1);
    }

    // =====================================================================
    // 4. HEADER & NAVIGATION
    // =====================================================================
    // Shows "Welcome, First Last" in the header
    private void loadWelcomeName() {
        ArrayList<String> user = FileHandling.readSpecificRecord("Users.txt", patientId);
        if (user != null) {
            lblWelcome.setText("Welcome, " + user.get(1) + " " + user.get(2));
        }
    }

    // Switches the card and highlights the active menu button
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

        // Enter = Save, only on the Profile page
        if (cardName.equals("profile")) {
            getRootPane().setDefaultButton(btnSaveProfile);
        } else {
            getRootPane().setDefaultButton(null);
        }
    }

    // =====================================================================
    // 5. BOOKINGS PAGE
    // =====================================================================
    // "09:30" -> 570
    private int toMinutes(String time) {
        return Integer.parseInt(time.substring(0, 2)) * 60 + Integer.parseInt(time.substring(3, 5));
    }

// 570 -> "09:30"
    private String toTime(int minutes) {
        return String.format("%02d:%02d", minutes / 60, minutes % 60);
    }

// "22-09-2026", "09:30" -> "202609220930" (year first, so it can be compared as text)
    private String sortKey(String date, String time) {
        return date.substring(6, 10) + date.substring(3, 5) + date.substring(0, 2) + time.replace(":", "");
    }

// Doctors.txt (no id): 0 department_id, 1 specialization
    private String getSpecialization(TreeMap<Integer, ArrayList<String>> doctors, String doctorId) {
        ArrayList<String> d = doctors.get(Integer.parseInt(doctorId));
        if (d == null) {
            return "-";
        }
        return d.get(1);
    }

    // Department combo: "All" + departments (no Emergency, no deleted)
    private void loadDepartments() {
        cmbDepartment.removeAllItems();
        deptIds.clear();
        deptIds.add("all");            // add the id FIRST: addItem fires the combo's event
        cmbDepartment.addItem("All");

        TreeMap<Integer, ArrayList<String>> depts = FileHandling.readAllRecords("Departments.txt");
        if (depts == null) {
            return;
        }
        for (Integer id : depts.keySet()) {
            ArrayList<String> d = depts.get(id); // 0 name, 1 description, 2 manager_id, 3 deleted
            if (d.get(3).equals("1") || d.get(0).equals("Emergency")) {
                continue; // patients don't book emergency
            }
            deptIds.add(String.valueOf(id));
            cmbDepartment.addItem(d.get(0));
        }
    }

// Doctor combo: "Any" + doctors in the chosen department
    private void loadDoctorCombo() {
        int index = cmbDepartment.getSelectedIndex();
        if (index < 0) {
            return;
        }
        String deptId = deptIds.get(index);

        cmbDoctor.removeAllItems();
        doctorIds.clear();
        doctorIds.add("any");
        cmbDoctor.addItem("Any");

        TreeMap<Integer, ArrayList<String>> doctors = FileHandling.readAllRecords("Doctors.txt");
        TreeMap<Integer, ArrayList<String>> users = FileHandling.readAllRecords("Users.txt");
        if (doctors == null || users == null) {
            return;
        }
        for (Integer id : doctors.keySet()) {
            ArrayList<String> d = doctors.get(id); // 0 department_id
            ArrayList<String> u = users.get(id);   // 0 first, 1 last, 8 deleted
            if (u == null || u.get(8).equals("1")) {
                continue;
            }
            if (!deptIds.contains(d.get(0))) {
                continue; // hides Emergency doctors
            }
            if (!deptId.equals("all") && !d.get(0).equals(deptId)) {
                continue;
            }
            doctorIds.add(String.valueOf(id));
            cmbDoctor.addItem("Dr. " + u.get(0) + " " + u.get(1));
        }
    }

    // true if the doctor AND this patient are both free for that time
    private boolean isFree(TreeMap<Integer, ArrayList<String>> consults, TreeMap<Integer, ArrayList<String>> cases,
            String doctorId, String date, String start, String end, int ignoreId) {
        for (Integer id : consults.keySet()) {
            if (id == ignoreId) {
                continue; // Reschedule: ignore the booking being moved
            }
            ArrayList<String> c = consults.get(id); // 0 case, 1 doctor, 5 status, 7 date, 8 start, 9 end, 10 deleted
            if (c.get(10).equals("1") || c.get(5).equals("cancelled") || !c.get(7).equals(date)) {
                continue;
            }
            // two times overlap if A starts before B ends AND B starts before A ends
            boolean overlap = toMinutes(start) < toMinutes(c.get(9)) && toMinutes(c.get(8)) < toMinutes(end);
            if (!overlap) {
                continue;
            }
            if (c.get(1).equals(doctorId)) {
                return false; // doctor is busy
            }
            ArrayList<String> k = cases.get(Integer.parseInt(c.get(0)));
            if (k != null && k.get(0).equals(String.valueOf(patientId))) {
                return false; // I already have something at this time
            }
        }
        return true;
    }

    // Table = my upcoming bookings + free 30-minute slots on the chosen date
    private void loadSlots() {
        DefaultTableModel model = (DefaultTableModel) tblSlots.getModel();
        model.setRowCount(0);
        slotRows.clear();

        int deptIndex = cmbDepartment.getSelectedIndex();
        int docIndex = cmbDoctor.getSelectedIndex();
        if (deptIndex < 0 || docIndex < 0) {
            return;
        }
        String deptId = deptIds.get(deptIndex);
        String doctorFilter = doctorIds.get(docIndex);
        String date = new SimpleDateFormat("dd-MM-yyyy").format((Date) spnDate.getValue());
        String now = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());

        TreeMap<Integer, ArrayList<String>> cases = FileHandling.readAllRecords("Cases.txt");
        TreeMap<Integer, ArrayList<String>> consults = FileHandling.readAllRecords("Consultations.txt");
        TreeMap<Integer, ArrayList<String>> shifts = FileHandling.readAllRecords("Shifts.txt");
        TreeMap<Integer, ArrayList<String>> shiftDocs = FileHandling.readAllRecords("ShiftDoctors.txt");
        TreeMap<Integer, ArrayList<String>> doctors = FileHandling.readAllRecords("Doctors.txt");
        if (cases == null) {
            cases = new TreeMap<>();
        }
        if (consults == null) {
            consults = new TreeMap<>();
        }
        if (shifts == null || shiftDocs == null || doctors == null) {
            return;
        }

        // 1) My upcoming bookings (any date)
        for (Integer consultId : consults.keySet()) {
            ArrayList<String> c = consults.get(consultId);
            ArrayList<String> k = cases.get(Integer.parseInt(c.get(0)));
            if (k == null || !k.get(0).equals(String.valueOf(patientId))) {
                continue;
            }
            if (!c.get(5).equals("booked") || c.get(10).equals("1")) {
                continue;
            }
            if (sortKey(c.get(7), c.get(8)).compareTo(now) <= 0) {
                continue; // already passed
            }
            model.addRow(new Object[]{c.get(7), c.get(8) + " - " + c.get(9),
                getDoctorName(c.get(1)), getSpecialization(doctors, c.get(1)), "Room " + c.get(6), "Booked (You)"});
            slotRows.add(new String[]{"booked", String.valueOf(consultId), c.get(1)});
        }

        // 2) Free 30-minute slots on the chosen date
        for (Integer shiftId : shifts.keySet()) {
            ArrayList<String> s = shifts.get(shiftId); // 0 department_id, 1 date, 2 start, 3 end, 4 deleted
            if (!s.get(1).equals(date) || s.get(4).equals("1")) {
                continue;
            }
            if (!deptIds.contains(s.get(0))) {
                continue; // Emergency or deleted department
            }
            if (!deptId.equals("all") && !s.get(0).equals(deptId)) {
                continue;
            }

            for (ArrayList<String> a : shiftDocs.values()) { // 0 shift_id, 1 doctor_id
                if (!a.get(0).equals(String.valueOf(shiftId))) {
                    continue;
                }
                String doctorId = a.get(1);
                if (!doctorFilter.equals("any") && !doctorFilter.equals(doctorId)) {
                    continue;
                }
                String doctorName = getDoctorName(doctorId);
                String spec = getSpecialization(doctors, doctorId);

                // step through the shift in 30-minute jumps
                for (int m = toMinutes(s.get(2)); m + 30 <= toMinutes(s.get(3)); m += 30) {
                    String start = toTime(m);
                    String end = toTime(m + 30);
                    if (sortKey(date, start).compareTo(now) <= 0) {
                        continue; // time already passed
                    }
                    if (!isFree(consults, cases, doctorId, date, start, end, -1)) {
                        continue;
                    }
                    model.addRow(new Object[]{date, start + " - " + end, doctorName, spec, "-", "Available"});
                    slotRows.add(new String[]{"available", doctorId, date, start, end});
                }
            }
        }
    }

    // Room can be used: status "ok" and not deleted
    private boolean isRoomUsable(TreeMap<Integer, ArrayList<String>> rooms, String roomId) {
        ArrayList<String> r = rooms.get(Integer.parseInt(roomId)); // 0 status, 1 deleted
        return r != null && r.get(0).equals("ok") && r.get(1).equals("0");
    }

// Room has no other booking overlapping this time
    private boolean isRoomFree(TreeMap<Integer, ArrayList<String>> consults, String roomId,
            String date, String start, String end, int ignoreId) {
        for (Integer id : consults.keySet()) {
            if (id == ignoreId) {
                continue;
            }
            ArrayList<String> c = consults.get(id); // 5 status, 6 room, 7 date, 8 start, 9 end, 10 deleted
            if (c.get(10).equals("1") || c.get(5).equals("cancelled")) {
                continue;
            }
            if (!c.get(6).equals(roomId) || !c.get(7).equals(date)) {
                continue;
            }
            if (toMinutes(start) < toMinutes(c.get(9)) && toMinutes(c.get(8)) < toMinutes(end)) {
                return false;
            }
        }
        return true;
    }

// 1st choice: the room this doctor already uses that day. 2nd choice: first free working room
    private String pickRoom(TreeMap<Integer, ArrayList<String>> consults, String doctorId,
            String date, String start, String end, int ignoreId) {
        TreeMap<Integer, ArrayList<String>> rooms = FileHandling.readAllRecords("ConsultationRooms.txt");
        if (rooms == null) {
            return "";
        }
        for (Integer id : consults.keySet()) {
            ArrayList<String> c = consults.get(id);
            if (id == ignoreId || c.get(10).equals("1") || c.get(5).equals("cancelled")) {
                continue;
            }
            if (c.get(1).equals(doctorId) && c.get(7).equals(date)) {
                String room = c.get(6);
                if (isRoomUsable(rooms, room) && isRoomFree(consults, room, date, start, end, ignoreId)) {
                    return room;
                }
            }
        }
        for (Integer id : rooms.keySet()) {
            String room = String.valueOf(id);
            if (isRoomUsable(rooms, room) && isRoomFree(consults, room, date, start, end, ignoreId)) {
                return room;
            }
        }
        return ""; // no room free
    }

    // =====================================================================
    // 6. MEDICAL HISTORY PAGE
    // =====================================================================
    // Fills the Visits, Prescriptions and Test Results tabs
    private void loadHistory() {
        DefaultTableModel visits = (DefaultTableModel) tblVisits.getModel();
        DefaultTableModel rx = (DefaultTableModel) tblPrescriptions.getModel();
        DefaultTableModel tests = (DefaultTableModel) tblTests.getModel();

        // start clean
        visits.setRowCount(0);
        rx.setRowCount(0);
        tests.setRowCount(0);
        visitDetails.clear();
        rxDetails.clear();
        testDetails.clear();
        txtVisitDetails.setText("");
        txtRxDetails.setText("");
        txtTestDetails.setText("");

        TreeMap<Integer, ArrayList<String>> cases = FileHandling.readAllRecords("Cases.txt");
        TreeMap<Integer, ArrayList<String>> consults = FileHandling.readAllRecords("Consultations.txt");
        TreeMap<Integer, ArrayList<String>> prescriptions = FileHandling.readAllRecords("Prescriptions.txt");
        TreeMap<Integer, ArrayList<String>> items = FileHandling.readAllRecords("PrescriptionItems.txt");
        TreeMap<Integer, ArrayList<String>> drugs = FileHandling.readAllRecords("DrugCatalogue.txt");
        TreeMap<Integer, ArrayList<String>> requests = FileHandling.readAllRecords("DiagnosticServiceRequests.txt");
        TreeMap<Integer, ArrayList<String>> services = FileHandling.readAllRecords("DiagnosticServiceCatalogue.txt");

        if (cases == null || consults == null) {
            return;
        }

        for (Integer consultId : consults.keySet()) {
            // c: 0 case_id, 1 doctor_id, 2 complaint, 3 vital_signs, 4 notes, 5 status, 6 room, 7 date, 8 start, 9 end, 10 deleted
            ArrayList<String> c = consults.get(consultId);
            // k: 0 patient_id, 1 doctor_in_charge, 2 open_date, 3 close_date, 4 category, 5 type, 6 case_summary, 7 deleted
            ArrayList<String> k = cases.get(Integer.parseInt(c.get(0)));

            // skip visits that are not mine, not completed, or deleted
            if (k == null || !k.get(0).equals(String.valueOf(patientId))) {
                continue;
            }
            if (!c.get(5).equals("completed") || c.get(10).equals("1")) {
                continue;
            }

            String date = c.get(7);
            String doctor = getDoctorName(c.get(1));

            // ---- Visits tab ----
            visits.addRow(new Object[]{date, doctor, k.get(4), c.get(2)});
            visitDetails.add("Vital signs: " + c.get(3)
                    + "\n\nDoctor's notes: " + c.get(4)
                    + "\n\nCase summary: " + k.get(6));

            // ---- Prescriptions tab ----
            if (prescriptions != null && items != null && drugs != null) {
                for (Integer rxId : prescriptions.keySet()) {
                    ArrayList<String> p = prescriptions.get(rxId); // 0 consultation_id, 1 deleted
                    if (!p.get(0).equals(String.valueOf(consultId)) || p.get(1).equals("1")) {
                        continue;
                    }
                    for (ArrayList<String> it : items.values()) {
                        // it: 0 prescription_id, 1 drug_id, 2 dosage, 3 frequency, 4 duration, 5 instructions, 6 deleted
                        if (!it.get(0).equals(String.valueOf(rxId)) || it.get(6).equals("1")) {
                            continue;
                        }
                        String medicine = "Unknown";
                        ArrayList<String> d = drugs.get(Integer.parseInt(it.get(1))); // 0 drug_name
                        if (d != null) {
                            medicine = d.get(0);
                        }
                        rx.addRow(new Object[]{date, medicine, it.get(2), it.get(3), it.get(4) + " days"});
                        rxDetails.add(medicine
                                + "\n\nInstructions: " + it.get(5)
                                + "\n\nPrescribed by " + doctor + " on " + date);
                    }
                }
            }

            // ---- Test Results tab ----
            if (requests != null && services != null) {
                for (ArrayList<String> r : requests.values()) {
                    // r: 0 consultation_id, 1 service_id, 2 request_date, 3 remarks, 4 result_date, 5 results, 6 deleted
                    if (!r.get(0).equals(String.valueOf(consultId)) || r.get(6).equals("1")) {
                        continue;
                    }
                    String testName = "Unknown";
                    String category = "-";
                    ArrayList<String> s = services.get(Integer.parseInt(r.get(1))); // 0 service_name, 1 category
                    if (s != null) {
                        testName = s.get(0);
                        category = s.get(1);
                    }
                    String status = "Ready";
                    String results = r.get(5);
                    if (r.get(4).isEmpty()) {
                        status = "Pending";
                        results = "Not available yet";
                    }
                    tests.addRow(new Object[]{r.get(2), testName, category, r.get(4), status});
                    testDetails.add(testName
                            + "\n\nRequest remarks: " + r.get(3)
                            + "\n\nResults: " + results);
                }
            }
        }
    }

    // =====================================================================
    // 7. SUBMIT RATINGS PAGE
    // =====================================================================
    // Splits my completed visits into "waiting for rating" and "already rated"
    private void loadRatings() {
        DefaultTableModel pending = (DefaultTableModel) tblPending.getModel();
        DefaultTableModel past = (DefaultTableModel) tblMyReviews.getModel();
        pending.setRowCount(0);
        past.setRowCount(0);

        TreeMap<Integer, ArrayList<String>> cases = FileHandling.readAllRecords("Cases.txt");
        TreeMap<Integer, ArrayList<String>> consults = FileHandling.readAllRecords("Consultations.txt");
        TreeMap<Integer, ArrayList<String>> reviews = FileHandling.readAllRecords("Reviews.txt");

        if (cases == null || consults == null) {
            return;
        }

        for (Integer consultId : consults.keySet()) {
            ArrayList<String> c = consults.get(consultId);
            ArrayList<String> k = cases.get(Integer.parseInt(c.get(0)));

            // same filter as History: mine, completed, not deleted
            if (k == null || !k.get(0).equals(String.valueOf(patientId))) {
                continue;
            }
            if (!c.get(5).equals("completed") || c.get(10).equals("1")) {
                continue;
            }

            // look for a review of this visit
            ArrayList<String> myReview = null;
            if (reviews != null) {
                for (ArrayList<String> r : reviews.values()) {
                    // r: 0 consultation_id, 1 rating, 2 comments, 3 deleted
                    if (r.get(0).equals(String.valueOf(consultId)) && r.get(3).equals("0")) {
                        myReview = r;
                    }
                }
            }

            String doctor = getDoctorName(c.get(1));
            if (myReview == null) {
                pending.addRow(new Object[]{consultId, c.get(7), doctor, c.get(2)});
            } else {
                past.addRow(new Object[]{c.get(7), doctor, myReview.get(1), myReview.get(2)});
            }
        }
    }

    // Resets the rating form
    private void clearRatingForm() {
        grpRating.clearSelection();   // un-tick all 5 radio buttons
        txtComment.setText("");
        tblPending.clearSelection();
    }

    // =====================================================================
    // 8. MY PROFILE PAGE
    // =====================================================================
    // Fills the profile page from Users.txt and Patients.txt
    private void loadProfile() {
        // readSpecificRecord keeps the id: 0 id, 1 first, 2 last, 3 dob, 4 gender, 5 phone, 6 email
        ArrayList<String> user = FileHandling.readSpecificRecord("Users.txt", patientId);
        if (user == null) {
            return;
        }
        txtPatientID.setText(String.valueOf(patientId));
        txtFirstName.setText(user.get(1));
        txtLastName.setText(user.get(2));
        txtDOB.setText(user.get(3));
        txtGender.setText(user.get(4));
        txtPhone.setText(user.get(5));
        txtEmail.setText(user.get(6));

        // Patients.txt (no id): 0 blood type, 1 allergies
        TreeMap<Integer, ArrayList<String>> patients = FileHandling.readAllRecords("Patients.txt");
        if (patients != null && patients.containsKey(patientId)) {
            txtBloodType.setText(patients.get(patientId).get(0));
            txtAllergies.setText(patients.get(patientId).get(1));
        }

        pwdCurrent.setText("");
        pwdNew.setText("");
        pwdConfirm.setText("");
    }

    // Shows an error, then puts back the saved (valid) values
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
        btnLogout.setForeground(new java.awt.Color(204, 204, 204));
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
        lblLastName.setBounds(20, 197, 56, 16);

        lblDOB.setText("Date Of Birth");
        pnlProfile.add(lblDOB);
        lblDOB.setBounds(20, 253, 68, 16);

        lblGender.setText("Gender");
        pnlProfile.add(lblGender);
        lblGender.setBounds(20, 309, 38, 16);

        lblBloodType.setText("Blood Type");
        pnlProfile.add(lblBloodType);
        lblBloodType.setBounds(20, 365, 60, 16);

        lblAllergies.setText("Allegergies");
        pnlProfile.add(lblAllergies);
        lblAllergies.setBounds(20, 421, 58, 16);

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
            new PatientLogin().setVisible(true);
            dispose();
        }
    }//GEN-LAST:event_btnLogoutActionPerformed

    private void btnSaveProfileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveProfileActionPerformed

        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();
        String current = new String(pwdCurrent.getPassword());
        String newPwd = new String(pwdNew.getPassword());
        String confirm = new String(pwdConfirm.getPassword());

        //1.For Phone and Email
        if (!phone.matches("01\\d-\\d{7,8}")) {
            showError("Phone must look like 012-3456789.");
            return;
        }
        //if (!email.contains("@") || !email.contains(".")) { can be used but can accept any as long as they are @ and .
        if (!email.matches("[A-Za-z0-9._-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.(com|net|org|edu|gov|my)")) {
            showError("Please enter a valid email");
            return;
        }

        TreeMap<Integer, ArrayList<String>> users = FileHandling.readAllRecords("Users.txt");
        for (Integer id : users.keySet()) {
            if (id != patientId && users.get(id).get(5).equalsIgnoreCase(email)) {
                showError("This email is already used by another account.");
                return;
            }
        }
        // 3. Password (only if any password box is filled)
        ArrayList<String> me = users.get(patientId); // no id: 5 email, 6 password
        String password = me.get(6);

        if (!current.isEmpty() || !newPwd.isEmpty() || !confirm.isEmpty()) {
            if (!current.equals(password)) {
                showError("Current password is incorrect.");
                return;
            }
            if (newPwd.length() < 8) {
                showError("New password must be at least 8 characters long.");
                return;
            }
            if (newPwd.contains(",") || newPwd.contains("\"")) {
                showError("Password cannot contain commas or quotes.");
                return;
            }
            if (!newPwd.equals(confirm)) {
                showError("New passwords do not match.");
                return;
            }
            password = newPwd;
        }

        // 4. Build the full row (editRecord needs the id at position 0) and save
        ArrayList<String> record = new ArrayList<>();
        record.add(String.valueOf(patientId));
        record.addAll(me);
        record.set(5, phone);
        record.set(6, email);
        record.set(7, password);
        FileHandling.editRecord("Users.txt", record);

        JOptionPane.showMessageDialog(this, "Profile updated.");
        loadProfile();
    }//GEN-LAST:event_btnSaveProfileActionPerformed

    private void btnResetProfileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResetProfileActionPerformed

        loadProfile();
    }//GEN-LAST:event_btnResetProfileActionPerformed
// ===== EVENTS: MEDICAL HISTORY =====
    private void tblVisitsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblVisitsMouseClicked

        int row = tblVisits.getSelectedRow();
        if (row >= 0) {
            txtVisitDetails.setText(visitDetails.get(row));
            txtVisitDetails.setCaretPosition(0); // scroll the box back to the top
        }
    }//GEN-LAST:event_tblVisitsMouseClicked

    private void tblPrescriptionsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblPrescriptionsMouseClicked

        int row = tblPrescriptions.getSelectedRow();
        if (row >= 0) {
            txtRxDetails.setText(rxDetails.get(row));
            txtRxDetails.setCaretPosition(0); // scroll the box back to the top
        }
    }//GEN-LAST:event_tblPrescriptionsMouseClicked

    private void tblTestsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblTestsMouseClicked

        int row = tblTests.getSelectedRow();
        if (row >= 0) {
            txtTestDetails.setText(testDetails.get(row));
            txtTestDetails.setCaretPosition(0); // scroll the box back to the top
        }
    }//GEN-LAST:event_tblTestsMouseClicked
// ===== EVENTS: SUBMIT RATINGS =====
    private void btnSubmitRatingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSubmitRatingActionPerformed

        // 1. A visit must be selected
        int row = tblPending.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a visit to rate.");
            return;
        }

// 2. A rating must be chosen
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
        if (rating == 0) {
            JOptionPane.showMessageDialog(this, "Please choose a rating from 1 to 5.");
            return;
        }

// 3. Comment is optional, but must be safe for the file
        String comment = txtComment.getText().trim();
        if (comment.contains("\"")) {
            JOptionPane.showMessageDialog(this, "Comment cannot contain double quotes.");
            return;
        }
        comment = comment.replace("\n", " "); // a new line would break the file

// 4. Save to Reviews.txt
        String consultId = tblPending.getValueAt(row, 0).toString();

        ArrayList<String> record = new ArrayList<>();
        record.add(String.valueOf(FileHandling.getNextID("Reviews.txt")));
        record.add(consultId);
        record.add(String.valueOf(rating));
        record.add(comment);
        record.add("0");
        FileHandling.addRecord("Reviews.txt", record);

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
        // 1. Must pick an Available row
        int row = tblSlots.getSelectedRow();
        if (row < 0 || !slotRows.get(row)[0].equals("available")) {
            JOptionPane.showMessageDialog(this, "Please select a slot marked Available.");
            return;
        }
        String[] info = slotRows.get(row); // {"available", doctorId, date, start, end}
        String doctorId = info[1];
        String date = info[2];
        String start = info[3];
        String end = info[4];

// 2. Reason is required and must be safe for the file
        String reason = txtReason.getText().trim().replace("\n", " ");
        if (reason.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the reason for your visit.");
            return;
        }
        if (reason.contains("\"")) {
            JOptionPane.showMessageDialog(this, "Reason cannot contain double quotes.");
            return;
        }
        String category = cmbCategory.getSelectedItem().toString().toLowerCase();

// 3. Check again that the slot is still free, then pick a room
        TreeMap<Integer, ArrayList<String>> cases = FileHandling.readAllRecords("Cases.txt");
        TreeMap<Integer, ArrayList<String>> consults = FileHandling.readAllRecords("Consultations.txt");
        if (cases == null) {
            cases = new TreeMap<>();
        }
        if (consults == null) {
            consults = new TreeMap<>();
        }
        if (!isFree(consults, cases, doctorId, date, start, end, -1)) {
            JOptionPane.showMessageDialog(this, "Sorry, this slot is no longer available.");
            loadSlots();
            return;
        }
        String room = pickRoom(consults, doctorId, date, start, end, -1);
        if (room.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No consultation room is free at this time. Please choose another slot.");
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
                "Book " + getDoctorName(doctorId) + " on " + date + " at " + start + "?",
                "Confirm Booking", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

// 4. Save a new Case (every booking gets its own case)
        String today = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
        int caseId = FileHandling.getNextID("Cases.txt");

        ArrayList<String> newCase = new ArrayList<>();
        newCase.add(String.valueOf(caseId));
        newCase.add(String.valueOf(patientId));
        newCase.add(doctorId);      // doctor_in_charge
        newCase.add(today);         // open_date
        newCase.add("");            // close_date (doctor closes it later)
        newCase.add(category);
        newCase.add("scheduled");
        newCase.add("");            // case_summary (doctor writes it later)
        newCase.add("0");
        FileHandling.addRecord("Cases.txt", newCase);

// 5. Save the Consultation linked to that case
        ArrayList<String> consult = new ArrayList<>();
        consult.add(String.valueOf(FileHandling.getNextID("Consultations.txt")));
        consult.add(String.valueOf(caseId));
        consult.add(doctorId);
        consult.add(reason);        // complaint
        consult.add("");            // vital_signs (doctor fills in)
        consult.add("");            // notes (doctor fills in)
        consult.add("booked");
        consult.add(room);
        consult.add(date);
        consult.add(start);
        consult.add(end);
        consult.add("0");
        FileHandling.addRecord("Consultations.txt", consult);

        JOptionPane.showMessageDialog(this, "Booked! Please go to Room " + room + ".");
        txtReason.setText("");
        loadSlots();
    }//GEN-LAST:event_btnBookActionPerformed

    private void btnRescheduleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRescheduleActionPerformed
        // 1. Must pick one of my bookings
        int row = tblSlots.getSelectedRow();
        if (row < 0 || !slotRows.get(row)[0].equals("booked")) {
            JOptionPane.showMessageDialog(this, "Please select one of your bookings (Booked (You)).");
            return;
        }
        int consultId = Integer.parseInt(slotRows.get(row)[1]);
        String doctorId = slotRows.get(row)[2];
        String date = new SimpleDateFormat("dd-MM-yyyy").format((Date) spnDate.getValue());
        String now = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());

        TreeMap<Integer, ArrayList<String>> cases = FileHandling.readAllRecords("Cases.txt");
        TreeMap<Integer, ArrayList<String>> consults = FileHandling.readAllRecords("Consultations.txt");
        TreeMap<Integer, ArrayList<String>> shifts = FileHandling.readAllRecords("Shifts.txt");
        TreeMap<Integer, ArrayList<String>> shiftDocs = FileHandling.readAllRecords("ShiftDoctors.txt");
        if (cases == null || consults == null || shifts == null || shiftDocs == null) {
            return;
        }

// 2. Collect the same doctor's free slots on the chosen date
        ArrayList<String> options = new ArrayList<>();
        for (Integer shiftId : shifts.keySet()) {
            ArrayList<String> s = shifts.get(shiftId); // 1 date, 2 start, 3 end, 4 deleted
            if (!s.get(1).equals(date) || s.get(4).equals("1")) {
                continue;
            }
            boolean doctorOnShift = false;
            for (ArrayList<String> a : shiftDocs.values()) { // 0 shift_id, 1 doctor_id
                if (a.get(0).equals(String.valueOf(shiftId)) && a.get(1).equals(doctorId)) {
                    doctorOnShift = true;
                }
            }
            if (!doctorOnShift) {
                continue;
            }
            for (int m = toMinutes(s.get(2)); m + 30 <= toMinutes(s.get(3)); m += 30) {
                String start = toTime(m);
                String end = toTime(m + 30);
                if (sortKey(date, start).compareTo(now) <= 0) {
                    continue;
                }
                if (isFree(consults, cases, doctorId, date, start, end, consultId)) { // ignore my old booking
                    options.add(start + " - " + end);
                }
            }
        }
        if (options.isEmpty()) {
            JOptionPane.showMessageDialog(this, getDoctorName(doctorId) + " has no free slots on " + date
                    + ".\nChange the Date above and try again.");
            return;
        }

// 3. Let the patient choose from a drop-down
        Object picked = JOptionPane.showInputDialog(this,
                "Choose a new time with " + getDoctorName(doctorId) + " on " + date
                + "\n(Change the Date above to see other days)",
                "Reschedule", JOptionPane.QUESTION_MESSAGE, null, options.toArray(), options.get(0));
        if (picked == null) {
            return; // pressed Cancel
        }
        String start = picked.toString().substring(0, 5);  // "10:00 - 10:30" -> "10:00"
        String end = picked.toString().substring(8, 13);   //                 -> "10:30"

        String room = pickRoom(consults, doctorId, date, start, end, consultId);
        if (room.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No consultation room is free at that time.");
            return;
        }

// 4. Update the same consultation row (with id: 7 room, 8 date, 9 start, 10 end)
        ArrayList<String> record = FileHandling.readSpecificRecord("Consultations.txt", consultId);
        record.set(7, room);
        record.set(8, date);
        record.set(9, start);
        record.set(10, end);
        FileHandling.editRecord("Consultations.txt", record);

        JOptionPane.showMessageDialog(this, "Rescheduled to " + date + " at " + start + " (Room " + room + ").");
        loadSlots();
    }//GEN-LAST:event_btnRescheduleActionPerformed

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        // 1. Must pick one of my bookings
        int row = tblSlots.getSelectedRow();
        if (row < 0 || !slotRows.get(row)[0].equals("booked")) {
            JOptionPane.showMessageDialog(this, "Please select one of your bookings (Booked (You)).");
            return;
        }
        int consultId = Integer.parseInt(slotRows.get(row)[1]);

        int choice = JOptionPane.showConfirmDialog(this, "Cancel this booking? This cannot be undone.",
                "Cancel Booking", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

// 2. Soft-delete the consultation (with id: 1 case_id, 6 status, 11 deleted)
        ArrayList<String> consult = FileHandling.readSpecificRecord("Consultations.txt", consultId);
        String caseId = consult.get(1);
        consult.set(6, "cancelled");
        consult.set(11, "1");
        FileHandling.editRecord("Consultations.txt", consult);

// 3. Delete the case too, ONLY if no other visit uses it (keeps old history safe)
        TreeMap<Integer, ArrayList<String>> consults = FileHandling.readAllRecords("Consultations.txt");
        boolean caseStillUsed = false;
        for (Integer id : consults.keySet()) {
            ArrayList<String> c = consults.get(id); // 0 case_id, 10 deleted
            if (id != consultId && c.get(0).equals(caseId) && c.get(10).equals("0")) {
                caseStillUsed = true;
            }
        }
        if (!caseStillUsed) {
            ArrayList<String> k = FileHandling.readSpecificRecord("Cases.txt", Integer.parseInt(caseId)); // 8 deleted
            k.set(8, "1");
            FileHandling.editRecord("Cases.txt", k);
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

        // Test run: open the dashboard for patient 40
        java.awt.EventQueue.invokeLater(() -> new PatientDashboard(40).setVisible(true));
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
