package Doctor;

import HelperFunction.FileHandling;
import Users.Role;
import Users.User;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;

// One doctor: their details and everything they can do.
// The screens talk to this class through the DoctorServices interface.
public class Doctor extends User implements DoctorServices {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    // =====================================================================
    // FIELDS
    // =====================================================================
    private String department_id, specialization, off_day;
    private int practice_start_year;

    // Consultations calendar (Consultations page)
    private int weekOffset = 0;
    private LocalDate weekStart;
    private ArrayList<Object[]> weekRows = new ArrayList<>();
    private ArrayList<Integer> weekConsultIds = new ArrayList<>();

    // Cases list (Cases page)
    private ArrayList<Object[]> caseRows = new ArrayList<>();
    private ArrayList<Integer> caseIds = new ArrayList<>();

    // The currently opened case (Case dialog)
    private int currentCaseId = -1;
    private String caseCatientId, caseDoctorInCharge, caseOpenDate, caseCloseDate, caseCategory, caseType, caseSummary;
    private ArrayList<Object[]> caseConsultRows = new ArrayList<>();
    private ArrayList<Integer> caseConsultIds = new ArrayList<>();
    private ArrayList<Object[]> caseTestRows = new ArrayList<>();
    private ArrayList<String> caseTestDetails = new ArrayList<>();

    // The currently opened consultation (Consultation dialog)
    private int currentConsultId = -1;
    private String consultCaseId, consultDoctorId, consultComplaint, consultVitals, consultNotes,
            consultStatus, consultRoom, consultDate, consultStart, consultEnd;

    // Search results (Prescription and Diagnostic Request dialogs)
    private ArrayList<Integer> drugResultIds = new ArrayList<>();
    private ArrayList<Integer> serviceResultIds = new ArrayList<>();

    // =====================================================================
    // CONSTRUCTORS
    // =====================================================================
    // Builds a doctor straight from the files (used after login)
    public Doctor(int user_id) {
        this.user_id = user_id;
        this.role = Role.Doctor;
        reload();
    }

    // Used by UserLogin: the user details come from Users.txt,
    // department, specialization and off day are read from Doctors.txt
    public Doctor(int user_id, String first_name, String last_name, String phone, String email,
            String password, String gender, LocalDate dob, Role role) {
        super(user_id, first_name, last_name, phone, email, password, gender, dob, role);
        reload();
    }

    // =====================================================================
    // PROFILE
    // =====================================================================
    // Reads this doctor's details from Users.txt and Doctors.txt
    private void reload() {
        TreeMap<Integer, ArrayList<String>> users = FileHandling.readAllRecords("Users.txt");
        if (users == null || !users.containsKey(this.user_id)) {
            return;
        }
        // u: 0 first, 1 last, 2 dob, 3 gender, 4 phone, 5 email, 6 password, 7 role, 8 deleted
        ArrayList<String> u = users.get(this.user_id);
        this.first_name = u.get(0);
        this.last_name = u.get(1);
        this.gender = u.get(3);
        this.phone = u.get(4);
        this.email = u.get(5);
        this.password = u.get(6);

        try {
            this.dob = LocalDate.parse(u.get(2), DATE);
        } catch (Exception e) {
            this.dob = null;
        }

        this.department_id = "";
        this.specialization = "";
        this.off_day = "";
        this.practice_start_year = 0;
        TreeMap<Integer, ArrayList<String>> doctors = FileHandling.readAllRecords("Doctors.txt");
        if (doctors != null && doctors.containsKey(this.user_id)) {
            // d: 0 department_id, 1 specialization, 2 practice_start_year, 3 off_day
            ArrayList<String> d = doctors.get(this.user_id);
            this.department_id = d.get(0);
            this.specialization = d.get(1);
            this.off_day = d.get(3);
            try {
                this.practice_start_year = Integer.parseInt(d.get(2));
            } catch (Exception e) {
                this.practice_start_year = 0;
            }
        }
    }

    public String getFullName() {
        return this.first_name + " " + this.last_name;
    }

    public String getFirst() {
        return this.first_name;
    }

    public String getLast() {
        return this.last_name;
    }

    public String getPhone() {
        return this.phone;
    }

    public String getEmail() {
        return this.email;
    }

    // "8" -> "DOC008"
    public String getDoctorCode() {
        return String.format("DOC%03d", this.user_id);
    }

    public String getDepartmentName() {
        TreeMap<Integer, ArrayList<String>> depts = FileHandling.readAllRecords("Departments.txt");
        if (depts == null || this.department_id.isEmpty()) {
            return "";
        }
        ArrayList<String> d = depts.get(Integer.parseInt(this.department_id));
        if (d == null) {
            return "";
        }
        return d.get(0);
    }

    public String getSpecialization() {
        return this.specialization;
    }

    // "MONDAY" -> "Monday"
    public String getOffDayText() {
        if (this.off_day == null || this.off_day.isEmpty()) {
            return "";
        }
        return capitalize(this.off_day);
    }

    // Checks and saves the profile. Returns null if saved, or the error message.
    public String saveProfile(String phone, String newEmail, String current, String newPwd, String confirm) {
        if (!phone.matches("01\\d-\\d{7,8}")) {
            return "Phone must look like 012-3456789.";
        }
        if (!newEmail.matches("[A-Za-z0-9._-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.(com|net|org|edu|gov|my)")) {
            return "Please enter a valid email.";
        }

        TreeMap<Integer, ArrayList<String>> users = FileHandling.readAllRecords("Users.txt");
        if (users == null || !users.containsKey(this.user_id)) {
            return "Your account could not be found.";
        }
        for (Integer id : users.keySet()) {
            if (id != this.user_id && users.get(id).get(5).equalsIgnoreCase(newEmail)) {
                return "This email is already used by another account.";
            }
        }

        String newPassword = users.get(this.user_id).get(6);
        if (!current.isEmpty() || !newPwd.isEmpty() || !confirm.isEmpty()) {
            if (!current.equals(newPassword)) {
                return "Current password is incorrect.";
            }
            if (newPwd.length() < 8) {
                return "New password must be at least 8 characters long.";
            }
            if (newPwd.contains("`")) {
                return "Password cannot contain a backtick (`) character.";
            }
            if (!newPwd.equals(confirm)) {
                return "New passwords do not match.";
            }
            newPassword = newPwd;
        }

        // editRecord needs the id at position 0, so every field moves one place right
        ArrayList<String> record = new ArrayList<>();
        record.add(String.valueOf(this.user_id));
        record.addAll(users.get(this.user_id));
        record.set(5, phone);
        record.set(6, newEmail);
        record.set(7, newPassword);
        FileHandling.editRecord("Users.txt", record);

        this.phone = phone;
        this.email = newEmail;
        this.password = newPassword;
        return null;   // null means success
    }

    // =====================================================================
    // SHARED LITTLE HELPERS
    // =====================================================================
    private String capitalize(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }

    public String today() {
        return LocalDate.now().format(DATE);
    }

    // "9" -> "Dr. Kavitha a/p Rajendran"
    private String getDoctorName(String doctorId) {
        TreeMap<Integer, ArrayList<String>> users = FileHandling.readAllRecords("Users.txt");
        int id = Integer.parseInt(doctorId);
        if (users == null || !users.containsKey(id)) {
            return "Unknown";
        }
        return "Dr. " + users.get(id).get(0) + " " + users.get(id).get(1);
    }

    // "40" -> "Farah Hidayah binti Hassan"
    private String getPatientName(String patientId) {
        TreeMap<Integer, ArrayList<String>> users = FileHandling.readAllRecords("Users.txt");
        int id = Integer.parseInt(patientId);
        if (users == null || !users.containsKey(id)) {
            return "Unknown";
        }
        return users.get(id).get(0) + " " + users.get(id).get(1);
    }

    // true if the case has already been closed (has a close date)
    private boolean isCaseClosed(String caseId) {
        TreeMap<Integer, ArrayList<String>> cases = FileHandling.readAllRecords("Cases.txt");
        if (cases == null) {
            return false;
        }
        ArrayList<String> k = cases.get(Integer.parseInt(caseId)); // 3 close_date
        if (k == null) {
            return false;
        }
        return !k.get(3).isEmpty();
    }

    // A consultation stays "booked" or "incomplete" until the day it happened is over. Once
    // that day has passed: a still-"booked" consultation (the doctor never saved anything)
    // becomes "completed" if details were somehow entered or "cancelled" if not; an
    // "incomplete" one (details saved but not manually marked complete) becomes "completed".
    // c is the consultation's field list (without its id).
    private void autoFinalizeConsultation(int consultId, ArrayList<String> c) {
        if (!c.get(5).equals("booked") && !c.get(5).equals("incomplete")) {
            return;
        }
        LocalDate consultDate;
        try {
            consultDate = LocalDate.parse(c.get(7), DATE);
        } catch (Exception e) {
            return;
        }
        if (!LocalDate.now().isAfter(consultDate)) {
            return;
        }
        String newStatus;
        if (c.get(5).equals("incomplete")) {
            newStatus = "completed";
        } else {
            newStatus = (!c.get(3).trim().isEmpty() || !c.get(4).trim().isEmpty()) ? "completed" : "cancelled";
        }
        c.set(5, newStatus);

        ArrayList<String> record = new ArrayList<>();
        record.add(String.valueOf(consultId));
        record.addAll(c);
        FileHandling.editRecord("Consultations.txt", record);
    }

    // =====================================================================
    // SCHEDULE (doctor's own shift assignments)
    // =====================================================================
    public ArrayList<Object[]> getSchedule() {
        ArrayList<Object[]> rows = new ArrayList<>();
        // Some ShiftDoctors.txt rows are missing the trailing "deleted" column,
        // so this reads every record and checks the flag only when it is present.
        TreeMap<Integer, ArrayList<String>> assignments = FileHandling.readAllRecords("ShiftDoctors.txt");
        TreeMap<Integer, ArrayList<String>> shifts = FileHandling.readActiveRecords("Shifts.txt");
        if (assignments == null || shifts == null) {
            return rows;
        }
        for (ArrayList<String> a : assignments.values()) { // 0 shift_id, 1 doctor_id, 2 deleted (may be missing)
            if (a.size() > 2 && a.get(2).equals("1")) {
                continue;
            }
            if (!a.get(1).equals(String.valueOf(this.user_id))) {
                continue;
            }
            ArrayList<String> s = shifts.get(Integer.parseInt(a.get(0))); // 1 date, 2 start, 3 end
            if (s == null) {
                continue;
            }
            rows.add(new Object[]{s.get(1), s.get(2), s.get(3)});
        }
        return rows;
    }

    // =====================================================================
    // CONSULTATIONS PAGE - weekly calendar
    // =====================================================================
    // delta = -1 previous week, 0 stay on current week, +1 next week
    public void loadWeek(int weekOffset) {
        this.weekOffset += weekOffset;

        LocalDate today = LocalDate.now();
        LocalDate mondayOfThisWeek = today.minusDays(today.getDayOfWeek().getValue() - 1);
        this.weekStart = mondayOfThisWeek.plusWeeks(this.weekOffset);
        LocalDate weekEnd = this.weekStart.plusDays(6);

        weekRows.clear();
        weekConsultIds.clear();

        TreeMap<Integer, ArrayList<String>> consults = FileHandling.readActiveRecords("Consultations.txt");
        if (consults == null) {
            return;
        }
        for (Integer consultId : consults.keySet()) {
            // c: 0 case_id, 1 doctor_id, 2 complaint, 6 room, 7 date, 8 start, 9 end, 5 status
            ArrayList<String> c = consults.get(consultId);
            if (!c.get(1).equals(String.valueOf(this.user_id))) {
                continue;
            }
            LocalDate consultDate;
            try {
                consultDate = LocalDate.parse(c.get(7), DATE);
            } catch (Exception e) {
                continue;
            }
            if (consultDate.isBefore(this.weekStart) || consultDate.isAfter(weekEnd)) {
                continue;
            }
            autoFinalizeConsultation(consultId, c);
            weekRows.add(new Object[]{c.get(7), c.get(0), c.get(8) + " - " + c.get(9), c.get(2), c.get(6), capitalize(c.get(5))});
            weekConsultIds.add(consultId);
        }
    }

    public Object[][] getWeekRows() {
        return weekRows.toArray(new Object[0][]);
    }

    public String[] getWeekHeaders() {
        return new String[]{"Date", "Case ID", "Time Slot", "Complaint", "Room ID", "Status"};
    }

    public String getWeekRange() {
        if (this.weekStart == null) {
            return "Week of —";
        }
        DateTimeFormatter label = DateTimeFormatter.ofPattern("dd MMM yyyy");
        return this.weekStart.format(label) + " - " + this.weekStart.plusDays(6).format(label);
    }

    public int getWeekConsultId(int row, int col) {
        if (row < 0 || row >= weekConsultIds.size()) {
            return -1;
        }
        return weekConsultIds.get(row);
    }

    // =====================================================================
    // CASES PAGE
    // =====================================================================
    public ArrayList<Object[]> getCases() {
        caseRows.clear();
        caseIds.clear();

        TreeMap<Integer, ArrayList<String>> cases = FileHandling.readActiveRecords("Cases.txt");
        TreeMap<Integer, ArrayList<String>> consults = FileHandling.readActiveRecords("Consultations.txt");
        if (cases == null) {
            return caseRows;
        }

        // case ids where this doctor has conducted at least one consultation
        TreeSet<Integer> involvedCaseIds = new TreeSet<>();
        if (consults != null) {
            for (ArrayList<String> c : consults.values()) { // 0 case_id, 1 doctor_id
                if (c.get(1).equals(String.valueOf(this.user_id))) {
                    involvedCaseIds.add(Integer.parseInt(c.get(0)));
                }
            }
        }

        for (Integer caseId : cases.keySet()) {
            // k: 0 patient_id, 1 doctor_in_charge, 2 open_date, 3 close_date, 4 category, 5 type, 6 summary
            ArrayList<String> k = cases.get(caseId);
            boolean inCharge = k.get(1).equals(String.valueOf(this.user_id));
            if (!inCharge && !involvedCaseIds.contains(caseId)) {
                continue;
            }
            String status = "Open";
            if (!k.get(3).isEmpty()) {
                status = "Closed";
            }
            caseRows.add(new Object[]{caseId, getPatientName(k.get(0)), getDoctorName(k.get(1)),
                capitalize(k.get(4)), capitalize(k.get(5)), k.get(2), k.get(3), status});
            caseIds.add(caseId);
        }
        return caseRows;
    }

    public int getCaseId(int row) {
        if (row < 0 || row >= caseIds.size()) {
            return -1;
        }
        return caseIds.get(row);
    }

    public boolean loadCase(int caseId) {
        TreeMap<Integer, ArrayList<String>> cases = FileHandling.readAllRecords("Cases.txt");
        if (cases == null || !cases.containsKey(caseId)) {
            return false;
        }
        // k: 0 patient_id, 1 doctor_in_charge, 2 open_date, 3 close_date, 4 category, 5 type, 6 summary, 7 deleted
        ArrayList<String> k = cases.get(caseId);
        if (k.get(7).equals("1")) {
            return false;
        }
        this.currentCaseId = caseId;
        this.caseCatientId = k.get(0);
        this.caseDoctorInCharge = k.get(1);
        this.caseOpenDate = k.get(2);
        this.caseCloseDate = k.get(3);
        this.caseCategory = k.get(4);
        this.caseType = k.get(5);
        this.caseSummary = k.get(6);

        caseConsultRows.clear();
        caseConsultIds.clear();
        caseTestRows.clear();
        caseTestDetails.clear();

        TreeMap<Integer, ArrayList<String>> consults = FileHandling.readActiveRecords("Consultations.txt");
        TreeMap<Integer, ArrayList<String>> requests = FileHandling.readActiveRecords("DiagnosticServiceRequests.txt");
        TreeMap<Integer, ArrayList<String>> services = FileHandling.readActiveRecords("DiagnosticServiceCatalogue.txt");

        if (consults != null) {
            for (Integer consultId : consults.keySet()) {
                // c: 0 case_id, 1 doctor_id, 2 complaint, 5 status, 7 date
                ArrayList<String> c = consults.get(consultId);
                if (!c.get(0).equals(String.valueOf(caseId))) {
                    continue;
                }
                autoFinalizeConsultation(consultId, c);
                caseConsultRows.add(new Object[]{c.get(7), getDoctorName(c.get(1)), c.get(2), capitalize(c.get(5))});
                caseConsultIds.add(consultId);

                if (requests != null && services != null) {
                    for (ArrayList<String> r : requests.values()) {
                        // r: 0 consultation_id, 1 service_id, 2 request_date, 3 remarks, 4 result_date, 5 results
                        if (!r.get(0).equals(String.valueOf(consultId))) {
                            continue;
                        }
                        String serviceName = "Unknown";
                        ArrayList<String> s = services.get(Integer.parseInt(r.get(1)));
                        if (s != null) {
                            serviceName = s.get(0);
                        }
                        String status = "Pending";
                        String results = "Not available yet";
                        if (!r.get(4).isEmpty()) {
                            status = "Ready";
                            results = r.get(5);
                        }
                        caseTestRows.add(new Object[]{serviceName, r.get(2), status, r.get(4)});
                        caseTestDetails.add(serviceName
                                + "\n\nRequested by: " + getDoctorName(c.get(1)) + " on " + r.get(2)
                                + "\n\nRemarks: " + r.get(3)
                                + "\n\nResults: " + results);
                    }
                }
            }
        }
        return true;
    }

    public String getCaseTitle() {
        return "Case #" + this.currentCaseId + " - " + capitalize(this.caseCategory) + " (" + capitalize(this.caseType) + ")";
    }

    public String getCaseMeta() {
        return "Patient: " + getPatientName(this.caseCatientId) + "  |  Opened: " + this.caseOpenDate;
    }

    public String getCaseRoleNote() {
        if (!isCaseOpen()) {
            return "This case is closed and can no longer be edited.";
        }
        if (isCaseInCharge()) {
            return "You are the doctor in charge of this case.";
        }
        return "You are viewing this case as a contributing doctor. Only " + getDoctorName(this.caseDoctorInCharge)
                + " can edit the summary or close this case.";
    }

    public boolean isCaseOpen() {
        return this.caseCloseDate == null || this.caseCloseDate.isEmpty();
    }

    public boolean isCaseInCharge() {
        return this.caseDoctorInCharge != null && this.caseDoctorInCharge.equals(String.valueOf(this.user_id));
    }

    public String[] getCasePatient() {
        TreeMap<Integer, ArrayList<String>> users = FileHandling.readAllRecords("Users.txt");
        TreeMap<Integer, ArrayList<String>> patients = FileHandling.readAllRecords("Patients.txt");
        int patientId = Integer.parseInt(this.caseCatientId);

        String age = "-";
        String gender = "-";
        if (users != null && users.containsKey(patientId)) {
            ArrayList<String> u = users.get(patientId); // 2 dob, 3 gender
            gender = u.get(3);
            try {
                LocalDate dob = LocalDate.parse(u.get(2), DATE);
                age = String.valueOf(Period.between(dob, LocalDate.now()).getYears());
            } catch (Exception e) {
                age = "-";
            }
        }
        String bloodType = "-";
        String allergies = "-";
        if (patients != null && patients.containsKey(patientId)) {
            ArrayList<String> p = patients.get(patientId); // 0 blood_type, 1 allergies
            bloodType = p.get(0);
            allergies = p.get(1);
        }
        return new String[]{age, gender, bloodType, allergies};
    }

    public String getCaseSummary() {
        return this.caseSummary;
    }

    public ArrayList<Object[]> getCaseConsultRows() {
        return caseConsultRows;
    }

    public int getCaseConsultId(int row) {
        if (row < 0 || row >= caseConsultIds.size()) {
            return -1;
        }
        return caseConsultIds.get(row);
    }

    public ArrayList<Object[]> getCaseTestRows() {
        return caseTestRows;
    }

    public String getCaseTestDetail(int row) {
        if (row < 0 || row >= caseTestDetails.size()) {
            return "";
        }
        return caseTestDetails.get(row);
    }

    // Saves the case summary. Returns null if saved, or the error message.
    public String saveCaseSummary(String summary) {
        if (this.currentCaseId == -1) {
            return "No case loaded.";
        }
        if (!isCaseInCharge()) {
            return "Only the doctor in charge can edit the case summary.";
        }
        if (!isCaseOpen()) {
            return "This case is closed and can no longer be edited.";
        }
        if (summary.contains("`")) {
            return "Summary cannot contain a backtick (`) character.";
        }

        ArrayList<String> record = new ArrayList<>();
        record.add(String.valueOf(this.currentCaseId));
        record.add(this.caseCatientId);
        record.add(this.caseDoctorInCharge);
        record.add(this.caseOpenDate);
        record.add(this.caseCloseDate);
        record.add(this.caseCategory);
        record.add(this.caseType);
        record.add(summary.trim());
        record.add("0");
        FileHandling.editRecord("Cases.txt", record);

        this.caseSummary = summary.trim();
        return null;
    }

    // Closes the case. Returns null if closed, or the error message.
    public String closeCase() {
        if (this.currentCaseId == -1) {
            return "No case loaded.";
        }
        if (!isCaseInCharge()) {
            return "Only the doctor in charge can close this case.";
        }
        if (!isCaseOpen()) {
            return "This case is already closed.";
        }
        if (this.caseSummary == null || this.caseSummary.trim().isEmpty()) {
            return "Please write a case summary before closing this case.";
        }

        ArrayList<String> record = new ArrayList<>();
        record.add(String.valueOf(this.currentCaseId));
        record.add(this.caseCatientId);
        record.add(this.caseDoctorInCharge);
        record.add(this.caseOpenDate);
        record.add(today());
        record.add(this.caseCategory);
        record.add(this.caseType);
        record.add(this.caseSummary);
        record.add("0");
        FileHandling.editRecord("Cases.txt", record);

        this.caseCloseDate = today();
        return null;
    }

    // =====================================================================
    // CONSULTATION DIALOG
    // =====================================================================
    public boolean loadConsultation(int consultId) {
        TreeMap<Integer, ArrayList<String>> consults = FileHandling.readAllRecords("Consultations.txt");
        if (consults == null || !consults.containsKey(consultId)) {
            return false;
        }
        // c: 0 case_id, 1 doctor_id, 2 complaint, 3 vitals, 4 notes, 5 status, 6 room, 7 date, 8 start, 9 end, 10 deleted
        ArrayList<String> c = consults.get(consultId);
        if (c.get(10).equals("1")) {
            return false;
        }
        autoFinalizeConsultation(consultId, c);
        this.currentConsultId = consultId;
        this.consultCaseId = c.get(0);
        this.consultDoctorId = c.get(1);
        this.consultComplaint = c.get(2);
        this.consultVitals = c.get(3);
        this.consultNotes = c.get(4);
        this.consultStatus = c.get(5);
        this.consultRoom = c.get(6);
        this.consultDate = c.get(7);
        this.consultStart = c.get(8);
        this.consultEnd = c.get(9);
        return true;
    }

    public int getConsultCaseId() {
        if (this.consultCaseId == null) {
            return -1;
        }
        return Integer.parseInt(this.consultCaseId);
    }

    public String getConsultTitle() {
        return "Consultation on " + this.consultDate + " (" + this.consultStart + " - " + this.consultEnd + ")";
    }

    public String getConsultMeta() {
        TreeMap<Integer, ArrayList<String>> cases = FileHandling.readAllRecords("Cases.txt");
        String patientName = "Unknown";
        if (cases != null && cases.containsKey(Integer.parseInt(this.consultCaseId))) {
            patientName = getPatientName(cases.get(Integer.parseInt(this.consultCaseId)).get(0));
        }
        return "Patient: " + patientName + "  |  Case #" + this.consultCaseId + "  |  Room " + this.consultRoom;
    }

    public String getConsultStatus() {
        return capitalize(this.consultStatus);
    }

    public String getConsultRoleNote() {
        if (!this.consultDoctorId.equals(String.valueOf(this.user_id))) {
            return "This consultation was conducted by " + getDoctorName(this.consultDoctorId)
                    + ". You may view it but cannot make changes.";
        }
        if (!this.consultStatus.equals("booked") && !this.consultStatus.equals("incomplete")) {
            return "This consultation is " + this.consultStatus + " and can no longer be edited.";
        }
        if (isCaseClosed(this.consultCaseId)) {
            return "This case is closed. Consultation details can no longer be edited.";
        }
        LocalDate consultDateParsed;
        try {
            consultDateParsed = LocalDate.parse(this.consultDate, DATE);
        } catch (Exception e) {
            return "";
        }
        LocalDate today = LocalDate.now();
        if (consultDateParsed.isAfter(today)) {
            return "This consultation has not taken place yet. Details can be added once it starts, on the day of the consultation.";
        }
        if (consultDateParsed.equals(today)) {
            try {
                if (LocalTime.now().isBefore(LocalTime.parse(this.consultStart))) {
                    return "This consultation has not started yet. Details can be added once it starts.";
                }
            } catch (Exception e) {
                // ignore
            }
        }
        return "";
    }

    public String getComplaint() {
        return this.consultComplaint;
    }

    public String getVitalSigns() {
        return this.consultVitals;
    }

    public String getNotes() {
        return this.consultNotes;
    }

    // Own, same-day, case-not-closed consultation that is either "booked" with its start time
    // already passed, or "incomplete" (which by definition only exists on the day it started).
    public boolean canEditConsultation() {
        if (this.currentConsultId == -1) {
            return false;
        }
        if (!this.consultDoctorId.equals(String.valueOf(this.user_id))) {
            return false;
        }
        if (!this.consultStatus.equals("booked") && !this.consultStatus.equals("incomplete")) {
            return false;
        }
        if (isCaseClosed(this.consultCaseId)) {
            return false;
        }
        LocalDate consultDateParsed;
        try {
            consultDateParsed = LocalDate.parse(this.consultDate, DATE);
        } catch (Exception e) {
            return false;
        }
        if (!consultDateParsed.equals(LocalDate.now())) {
            return false;
        }
        if (this.consultStatus.equals("incomplete")) {
            return true;
        }
        try {
            return !LocalTime.now().isBefore(LocalTime.parse(this.consultStart));
        } catch (Exception e) {
            return false;
        }
    }

    // Saves the consultation's vitals, notes, and status. Returns null if saved, or the error message.
    private String saveConsultation(String vitals, String notes, String newStatus) {
        if (this.currentConsultId == -1) {
            return "No consultation loaded.";
        }
        if (!canEditConsultation()) {
            return "You cannot edit this consultation.";
        }
        if (vitals.contains("`") || notes.contains("`")) {
            return "Vital signs and notes cannot contain a backtick (`) character.";
        }

        ArrayList<String> record = new ArrayList<>();
        record.add(String.valueOf(this.currentConsultId));
        record.add(this.consultCaseId);
        record.add(this.consultDoctorId);
        record.add(this.consultComplaint);
        record.add(vitals.trim());
        record.add(notes.trim());
        record.add(newStatus);
        record.add(this.consultRoom);
        record.add(this.consultDate);
        record.add(this.consultStart);
        record.add(this.consultEnd);
        record.add("0");
        FileHandling.editRecord("Consultations.txt", record);

        this.consultVitals = vitals.trim();
        this.consultNotes = notes.trim();
        this.consultStatus = newStatus;
        return null;
    }

    // Saves vitals/notes and marks the consultation "incomplete": details saved but not yet
    // marked complete, still editable for the rest of the day.
    public String saveConsultationProgress(String vitals, String notes) {
        return saveConsultation(vitals, notes, "incomplete");
    }

    // Saves vitals/notes and marks the consultation "completed": no longer editable.
    public String completeConsultation(String vitals, String notes) {
        return saveConsultation(vitals, notes, "completed");
    }

    // =====================================================================
    // PRESCRIPTIONS
    // =====================================================================
    public ArrayList<String> getDrugForms() {
        ArrayList<String> forms = new ArrayList<>();
        forms.add("All");
        TreeSet<String> distinctForms = new TreeSet<>();
        TreeMap<Integer, ArrayList<String>> drugs = FileHandling.readActiveRecords("DrugCatalogue.txt");
        if (drugs != null) {
            for (ArrayList<String> d : drugs.values()) { // 1 form
                distinctForms.add(d.get(1));
            }
        }
        forms.addAll(distinctForms);
        return forms;
    }

    public ArrayList<Object[]> searchDrugs(String form, String text) {
        ArrayList<Object[]> rows = new ArrayList<>();
        drugResultIds.clear();

        TreeMap<Integer, ArrayList<String>> drugs = FileHandling.readActiveRecords("DrugCatalogue.txt");
        if (drugs == null) {
            return rows;
        }
        String search = "";
        if (text != null) {
            search = text.trim().toLowerCase();
        }
        for (Integer drugId : drugs.keySet()) {
            ArrayList<String> d = drugs.get(drugId); // 0 name, 1 form, 2 price
            if (form != null && !form.equalsIgnoreCase("All") && !d.get(1).equalsIgnoreCase(form)) {
                continue;
            }
            if (!search.isEmpty() && !d.get(0).toLowerCase().contains(search)) {
                continue;
            }
            double price = 0.0;
            try {
                price = Double.parseDouble(d.get(2));
            } catch (Exception e) {
                price = 0.0;
            }
            rows.add(new Object[]{d.get(0), d.get(1), String.format("%.2f", price)});
            drugResultIds.add(drugId);
        }
        return rows;
    }

    public int getDrugResultId(int row) {
        if (row < 0 || row >= drugResultIds.size()) {
            return -1;
        }
        return drugResultIds.get(row);
    }

    public String getDosageUnit(String form) {
        if (form == null) {
            return "Dosage";
        }
        switch (form.toLowerCase()) {
            case "tablet/capsule":
                return "Dosage (mg)";
            case "syrup/solution":
                return "Dosage (mL)";
            case "powder":
                return "Dosage (g)";
            case "injection":
                return "Dosage (mg)";
            case "drops":
                return "Dosage (drops)";
            case "cream/ointment/gel":
                return "Dosage (application)";
            default:
                return "Dosage";
        }
    }

    // Existing items of a consultation's prescription, if any: {drugId, drugName, dosage, frequency, duration, instructions}
    public ArrayList<String[]> getPrescriptionItems(int consultId) {
        ArrayList<String[]> rows = new ArrayList<>();
        TreeMap<Integer, ArrayList<String>> prescriptions = FileHandling.readActiveRecords("Prescriptions.txt");
        TreeMap<Integer, ArrayList<String>> items = FileHandling.readActiveRecords("PrescriptionItems.txt");
        TreeMap<Integer, ArrayList<String>> drugs = FileHandling.readActiveRecords("DrugCatalogue.txt");
        if (prescriptions == null || items == null) {
            return rows;
        }
        for (Integer prescriptionId : prescriptions.keySet()) {
            ArrayList<String> p = prescriptions.get(prescriptionId); // 0 consultation_id
            if (!p.get(0).equals(String.valueOf(consultId))) {
                continue;
            }
            for (ArrayList<String> it : items.values()) {
                // it: 0 prescription_id, 1 drug_id, 2 dosage, 3 frequency, 4 duration, 5 instructions
                if (!it.get(0).equals(String.valueOf(prescriptionId))) {
                    continue;
                }
                String drugName = "Unknown";
                if (drugs != null) {
                    ArrayList<String> d = drugs.get(Integer.parseInt(it.get(1)));
                    if (d != null) {
                        drugName = d.get(0);
                    }
                }
                rows.add(new String[]{it.get(1), drugName, it.get(2), it.get(3), it.get(4), it.get(5)});
            }
        }
        return rows;
    }

    // Checks one prescription item before it is added to the pending list. Returns null if valid.
    public String checkPrescriptionItem(String unit, String dosage, String frequency, String instructions) {
        double dosageValue;
        try {
            dosageValue = Double.parseDouble(dosage.trim());
        } catch (Exception e) {
            return "Please enter a valid " + (unit == null ? "dosage" : unit.toLowerCase()) + ".";
        }
        if (dosageValue <= 0) {
            return "Dosage must be greater than zero.";
        }
        if (frequency == null || frequency.trim().isEmpty()) {
            return "Please enter the frequency.";
        }
        if (instructions != null && instructions.contains("`")) {
            return "Instructions cannot contain a backtick (`) character.";
        }
        return null;
    }

    // Creates the prescription if none exists yet, or replaces its items if one already does.
    // items: {drugId, drugName, dosage, frequency, duration, instructions}
    public String savePrescription(int consultId, ArrayList<String[]> items) {
        if (!loadConsultation(consultId) || !canEditConsultation()) {
            return "You cannot write a prescription for this consultation.";
        }
        if (items == null || items.isEmpty()) {
            return "Please add at least one drug to the prescription.";
        }

        Integer prescriptionId = null;
        TreeMap<Integer, ArrayList<String>> prescriptions = FileHandling.readActiveRecords("Prescriptions.txt");
        if (prescriptions != null) {
            for (Integer id : prescriptions.keySet()) {
                ArrayList<String> p = prescriptions.get(id); // 0 consultation_id
                if (p.get(0).equals(String.valueOf(consultId))) {
                    prescriptionId = id;
                    break;
                }
            }
        }

        if (prescriptionId == null) {
            prescriptionId = FileHandling.getNextID("Prescriptions.txt");
            ArrayList<String> prescriptionRecord = new ArrayList<>();
            prescriptionRecord.add(String.valueOf(prescriptionId));
            prescriptionRecord.add(String.valueOf(consultId));
            prescriptionRecord.add("0");
            FileHandling.addRecord("Prescriptions.txt", prescriptionRecord);
        } else {
            TreeMap<Integer, ArrayList<String>> existingItems = FileHandling.readActiveRecords("PrescriptionItems.txt");
            if (existingItems != null) {
                for (Integer itemId : existingItems.keySet()) {
                    ArrayList<String> it = existingItems.get(itemId); // 0 prescription_id
                    if (it.get(0).equals(String.valueOf(prescriptionId))) {
                        FileHandling.removeRecord("PrescriptionItems.txt", itemId);
                    }
                }
            }
        }

        for (String[] item : items) {
            ArrayList<String> itemRecord = new ArrayList<>();
            itemRecord.add(String.valueOf(FileHandling.getNextID("PrescriptionItems.txt")));
            itemRecord.add(String.valueOf(prescriptionId));
            itemRecord.add(item[0]);
            itemRecord.add(item[2]);
            itemRecord.add(item[3]);
            itemRecord.add(item[4]);
            itemRecord.add(item[5]);
            itemRecord.add("0");
            FileHandling.addRecord("PrescriptionItems.txt", itemRecord);
        }
        return null;
    }

    // =====================================================================
    // DIAGNOSTIC REQUESTS
    // =====================================================================
    public ArrayList<String> getServiceCategories() {
        ArrayList<String> categories = new ArrayList<>();
        categories.add("All");
        TreeSet<String> distinctCategories = new TreeSet<>();
        TreeMap<Integer, ArrayList<String>> services = FileHandling.readActiveRecords("DiagnosticServiceCatalogue.txt");
        if (services != null) {
            for (ArrayList<String> s : services.values()) { // 1 category
                distinctCategories.add(capitalize(s.get(1)));
            }
        }
        categories.addAll(distinctCategories);
        return categories;
    }

    public ArrayList<String> getServiceTypes(String category) {
        ArrayList<String> types = new ArrayList<>();
        types.add("All");
        TreeSet<String> distinctTypes = new TreeSet<>();
        TreeMap<Integer, ArrayList<String>> services = FileHandling.readActiveRecords("DiagnosticServiceCatalogue.txt");
        if (services != null) {
            for (ArrayList<String> s : services.values()) { // 1 category, 2 type
                if (category != null && !category.equalsIgnoreCase("All") && !s.get(1).equalsIgnoreCase(category)) {
                    continue;
                }
                distinctTypes.add(capitalize(s.get(2)));
            }
        }
        types.addAll(distinctTypes);
        return types;
    }

    public ArrayList<Object[]> searchServices(String category, String type, String text) {
        ArrayList<Object[]> rows = new ArrayList<>();
        serviceResultIds.clear();

        TreeMap<Integer, ArrayList<String>> services = FileHandling.readActiveRecords("DiagnosticServiceCatalogue.txt");
        if (services == null) {
            return rows;
        }
        String search = "";
        if (text != null) {
            search = text.trim().toLowerCase();
        }
        for (Integer serviceId : services.keySet()) {
            ArrayList<String> s = services.get(serviceId); // 0 name, 1 category, 2 type, 3 price
            if (category != null && !category.equalsIgnoreCase("All") && !s.get(1).equalsIgnoreCase(category)) {
                continue;
            }
            if (type != null && !type.equalsIgnoreCase("All") && !s.get(2).equalsIgnoreCase(type)) {
                continue;
            }
            if (!search.isEmpty() && !s.get(0).toLowerCase().contains(search)) {
                continue;
            }
            double price = 0.0;
            try {
                price = Double.parseDouble(s.get(3));
            } catch (Exception e) {
                price = 0.0;
            }
            rows.add(new Object[]{s.get(0), capitalize(s.get(1)), capitalize(s.get(2)), String.format("%.2f", price)});
            serviceResultIds.add(serviceId);
        }
        return rows;
    }

    public int getServiceResultId(int row) {
        if (row < 0 || row >= serviceResultIds.size()) {
            return -1;
        }
        return serviceResultIds.get(row);
    }

    // Checks a free text field (request remarks). Returns null if valid.
    public String checkText(String text) {
        if (text != null && text.contains("`")) {
            return "This field cannot contain a backtick (`) character.";
        }
        return null;
    }

    // requests: {serviceId, remarks}
    public String submitDiagnosticRequests(int consultId, ArrayList<String[]> requests) {
        if (!loadConsultation(consultId) || !canEditConsultation()) {
            return "You cannot request diagnostic services for this consultation.";
        }
        if (requests == null || requests.isEmpty()) {
            return "Please add at least one service to the request list.";
        }

        for (String[] request : requests) {
            ArrayList<String> record = new ArrayList<>();
            record.add(String.valueOf(FileHandling.getNextID("DiagnosticServiceRequests.txt")));
            record.add(String.valueOf(consultId));
            record.add(request[0]);
            record.add(today());
            record.add(request[1]);
            record.add("");
            record.add("");
            record.add("0");
            FileHandling.addRecord("DiagnosticServiceRequests.txt", record);
        }
        return null;
    }

    // The diagnostic requests already on file for one consultation, for editing on the Consultation dialog:
    // {requestId, serviceId, serviceName, requestDate, remarks}
    public ArrayList<Object[]> getDiagnosticRequestItems(int consultId) {
        ArrayList<Object[]> rows = new ArrayList<>();
        TreeMap<Integer, ArrayList<String>> requests = FileHandling.readActiveRecords("DiagnosticServiceRequests.txt");
        TreeMap<Integer, ArrayList<String>> services = FileHandling.readActiveRecords("DiagnosticServiceCatalogue.txt");
        if (requests == null) {
            return rows;
        }
        for (Integer requestId : requests.keySet()) {
            // r: 0 consultation_id, 1 service_id, 2 request_date, 3 remarks, 4 result_date, 5 results
            ArrayList<String> r = requests.get(requestId);
            if (!r.get(0).equals(String.valueOf(consultId))) {
                continue;
            }
            String serviceName = "Unknown";
            if (services != null) {
                ArrayList<String> s = services.get(Integer.parseInt(r.get(1)));
                if (s != null) {
                    serviceName = s.get(0);
                }
            }
            rows.add(new Object[]{requestId, r.get(1), serviceName, r.get(2), r.get(3)});
        }
        return rows;
    }

    // Deletes one diagnostic request. Returns null if deleted, or the error message.
    public String deleteDiagnosticRequest(int requestId) {
        ArrayList<String> record = FileHandling.readSpecificRecord("DiagnosticServiceRequests.txt", requestId);
        if (record == null) {
            return "This diagnostic request could not be found.";
        }
        int consultId = Integer.parseInt(record.get(1)); // 1 consultation_id
        if (!loadConsultation(consultId) || !canEditConsultation()) {
            return "You cannot delete diagnostic requests for this consultation.";
        }
        FileHandling.removeRecord("DiagnosticServiceRequests.txt", requestId);
        return null;
    }

    // =====================================================================
    // REVIEWS
    // =====================================================================
    // rows: {consultationId, patientName, rating, dateReviewed, comments}
    public ArrayList<Object[]> getReviews() {
        ArrayList<Object[]> rows = new ArrayList<>();
        TreeMap<Integer, ArrayList<String>> reviews = FileHandling.readActiveRecords("Reviews.txt");
        TreeMap<Integer, ArrayList<String>> consults = FileHandling.readAllRecords("Consultations.txt");
        TreeMap<Integer, ArrayList<String>> cases = FileHandling.readAllRecords("Cases.txt");
        if (reviews == null || consults == null || cases == null) {
            return rows;
        }
        for (ArrayList<String> r : reviews.values()) { // 0 consultation_id, 1 rating, 2 comments, 3 date_reviewed
            int consultId = Integer.parseInt(r.get(0));
            ArrayList<String> c = consults.get(consultId); // 0 case_id, 1 doctor_id
            if (c == null || !c.get(1).equals(String.valueOf(this.user_id))) {
                continue;
            }
            String patientName = "Unknown";
            ArrayList<String> k = cases.get(Integer.parseInt(c.get(0))); // 0 patient_id
            if (k != null) {
                patientName = getPatientName(k.get(0));
            }
            rows.add(new Object[]{consultId, patientName, r.get(1), r.get(3), r.get(2)});
        }
        return rows;
    }

    public String getRatingSummary() {
        ArrayList<Object[]> reviews = getReviews();
        if (reviews.isEmpty()) {
            return "No reviews yet.";
        }
        int total = 0;
        for (Object[] row : reviews) {
            total += Integer.parseInt((String) row[2]);
        }
        double average = (double) total / reviews.size();
        return String.format("Average rating: %.1f out of 5 (%d review%s).",
                average, reviews.size(), reviews.size() == 1 ? "" : "s");
    }
}
