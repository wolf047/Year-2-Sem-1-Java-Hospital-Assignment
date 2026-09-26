package Doctor;

import HelperFunction.FileHandling;
import Users.Role;
import Users.User;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;

public class Doctor extends User implements DoctorServices {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd-MM-yyyy");

// FIELDS DECLARATION   
    // DOCTOR DETAILS
    private String department_id, specialization, off_day;
    private int practice_start_year;

    // CONSULTATION DASH TBL
    private int weekOffset = 0;
    private LocalDate weekStart;
    private ArrayList<Object[]> weekRows = new ArrayList<>();
    private ArrayList<Integer> weekConsultIds = new ArrayList<>();
    
    // CONSULTATION DIALOG
    private int currentConsultId = -1;
    private String consultCaseId, consultDoctorId, consultComplaint, consultVitals, consultNotes,
            consultStatus, consultRoom, consultDate, consultStart, consultEnd;

    // CASE DASH TBL
    private ArrayList<Object[]> caseRows = new ArrayList<>();
    private ArrayList<Integer> caseIds = new ArrayList<>();

    // CASE DIALOG
    private int currentCaseId = -1;
    private String casePatientId, caseDoctorInCharge, caseOpenDate, caseCloseDate, caseCategory, caseType, caseSummary;
    private ArrayList<Object[]> caseConsultRows = new ArrayList<>();
    private ArrayList<Integer> caseConsultIds = new ArrayList<>();
    private ArrayList<Object[]> caseTestRows = new ArrayList<>();
    private ArrayList<String> caseTestDetails = new ArrayList<>();

    // PRESCRIPTION DIAGNOSTIC SEARCH RESULTS 
    private ArrayList<Integer> drugResultIds = new ArrayList<>();
    private ArrayList<Integer> serviceResultIds = new ArrayList<>();


// CONSTRUCTOR
    public Doctor(int user_id) {
        this.user_id = user_id;
        this.role = Role.Doctor;
        constructorLoadDetails();
    }

    // USERLOGIN
    public Doctor(int user_id, String first_name, String last_name, String phone, String email,
            String password, String gender, LocalDate dob, Role role) {
        super(user_id, first_name, last_name, phone, email, password, gender, dob, role);
        constructorLoadDetails();
    }

    private void constructorLoadDetails() {
        TreeMap<Integer, ArrayList<String>> users = FileHandling.readAllRecords("Users.txt");
        if (users == null || !users.containsKey(this.user_id)) {
            return;
        }
        
        // Users(user_id, first_name, last_name, dob, gender, phone, email, password, role, deleted)
        ArrayList<String> userValue = users.get(this.user_id);
        this.first_name = userValue.get(0);
        this.last_name = userValue.get(1);
        this.gender = userValue.get(3);
        this.phone = userValue.get(4);
        this.email = userValue.get(5);
        this.password = userValue.get(6);

        try {
            this.dob = LocalDate.parse(userValue.get(2), DATE);
        } catch (Exception e) {
            this.dob = null;
        }

        // Doctors(doctor_id, department_id, specialization, practice_start_year, off_day)
        this.department_id = "";
        this.specialization = "";
        this.practice_start_year = 0;
        this.off_day = "";
        TreeMap<Integer, ArrayList<String>> doctors = FileHandling.readAllRecords("Doctors.txt");
        if (doctors != null && doctors.containsKey(this.user_id)) {
            ArrayList<String> doctorValue = doctors.get(this.user_id);
            this.department_id = doctorValue.get(0);
            this.specialization = doctorValue.get(1);
            this.off_day = doctorValue.get(3);
            try {
                this.practice_start_year = Integer.parseInt(doctorValue.get(2));
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

    // prepend prefix
    public String getDoctorCode() {
        return String.format("DOC%03d", this.user_id);
    }

    public String getDepartmentName() {
        TreeMap<Integer, ArrayList<String>> departments = FileHandling.readAllRecords("Departments.txt");
        if (departments == null || this.department_id.isEmpty()) {
            return "";
        }
        // Departments(department_id, department_name, description, manager_id, deleted)
        ArrayList<String> departmentValue = departments.get(Integer.parseInt(this.department_id));
        if (departmentValue == null) {
            return "";
        }
        return departmentValue.get(0);
    }

    public String getSpecialization() {
        return this.specialization;
    }

    public String getOffDayText() {
        if (this.off_day == null || this.off_day.isEmpty()) {
            return "";
        }
        return capitalize(this.off_day);
    }

// PROFILE
    /* 
    successful save: null
    error: String error message
    */
    public String saveProfile(String phone, String newEmail, String current, String newPwd, String confirm) {
        if (!phone.matches("01\\d-\\d{7,8}")) {
            return "Phone must look like 012-3456789.";
        }
        if (!newEmail.matches("[A-Za-z0-9._-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.(com|net|org|edu|gov|my)")) {
            return "Please enter a valid email.";
        }

        TreeMap<Integer, ArrayList<String>> users = FileHandling.readAllRecords("Users.txt");
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

        ArrayList<String> userRecord = new ArrayList<>();
        userRecord.add(String.valueOf(this.user_id));
        userRecord.addAll(users.get(this.user_id));
        userRecord.set(5, phone);
        userRecord.set(6, newEmail);
        userRecord.set(7, newPassword);
        FileHandling.editRecord("Users.txt", userRecord);

        this.phone = phone;
        this.email = newEmail;
        this.password = newPassword;
        return null;
    }

// HELPER FUNCTIONS
    private String capitalize(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }

    public String today() {
        return LocalDate.now().format(DATE);
    }

    private String getDoctorName(String doctorId) {
        TreeMap<Integer, ArrayList<String>> users = FileHandling.readAllRecords("Users.txt");
        int id = Integer.parseInt(doctorId);
        if (users == null || !users.containsKey(id)) {
            return "Unknown";
        }
        return "Dr. " + users.get(id).get(0) + " " + users.get(id).get(1);
    }

    private String getPatientName(String patientId) {
        TreeMap<Integer, ArrayList<String>> users = FileHandling.readAllRecords("Users.txt");
        int id = Integer.parseInt(patientId);
        if (users == null || !users.containsKey(id)) {
            return "Unknown";
        }
        return users.get(id).get(0) + " " + users.get(id).get(1);
    }

    // case has close_date: true
    private boolean isCaseClosed(String caseId) {
        TreeMap<Integer, ArrayList<String>> cases = FileHandling.readAllRecords("Cases.txt");
        if (cases == null) {
            return false;
        }
        ArrayList<String> caseValue = cases.get(Integer.parseInt(caseId));
        if (caseValue == null) {
            return false;
        }
        return !caseValue.get(3).isEmpty();
    }

    // updates consultation status with reference to date and consultation details 
    private void autoFinalizeConsultation(int consultId, ArrayList<String> consultationValue) {
        // Consultations(consultation_id, case_id, doctor_id, complaint, vital_signs, notes, consultation_status, consult_room_id, date, start_time, end_time, deleted)
        String status = consultationValue.get(5); // consultation_status
        if (!status.equals("booked") && !status.equals("incomplete")) {
            return;
        }
        LocalDate consultDate;
        LocalTime slotEnd;
        try {
            consultDate = LocalDate.parse(consultationValue.get(7), DATE); // date
            slotEnd = LocalTime.parse(consultationValue.get(9)); // end_time
        } catch (Exception e) {
            return;
        }
        boolean hasDetails = !consultationValue.get(3).trim().isEmpty() || !consultationValue.get(4).trim().isEmpty(); // vital_signs, notes

        String newStatus;
        if (status.equals("incomplete")) {
            if (!LocalDate.now().isAfter(consultDate)) {
                return;
            }
            newStatus = "completed";
        } else {
            if (hasDetails || LocalDateTime.now().isBefore(LocalDateTime.of(consultDate, slotEnd))) {
                return;
            }
            newStatus = "cancelled";
        }
        consultationValue.set(5, newStatus);

        ArrayList<String> consultationRecord = new ArrayList<>();
        consultationRecord.add(String.valueOf(consultId));
        consultationRecord.addAll(consultationValue);
        FileHandling.editRecord("Consultations.txt", consultationRecord);
    }

    
    public void finalizeAllConsultations() {
        TreeMap<Integer, ArrayList<String>> consultations = FileHandling.readActiveRecords("Consultations.txt");
        if (consultations == null) {
            return;
        }
        for (Integer consultId : consultations.keySet()) {
            ArrayList<String> consultationValue = consultations.get(consultId);
            if (!consultationValue.get(1).equals(String.valueOf(this.user_id))) {
                continue;
            }
            autoFinalizeConsultation(consultId, consultationValue);
        }
    }


// SCHEDULE
    public ArrayList<Object[]> getSchedule() {
        ArrayList<Object[]> rows = new ArrayList<>();
        // ShiftDoctors(assignment_id, shift_id, doctor_id, deleted)
        // Shifts(shift_id, department_id, date, start_time, end_time, deleted)
        TreeMap<Integer, ArrayList<String>> assignments = FileHandling.readActiveRecords("ShiftDoctors.txt");
        TreeMap<Integer, ArrayList<String>> shifts = FileHandling.readActiveRecords("Shifts.txt");
        if (assignments == null || shifts == null) {
            return rows;
        }
        LocalDate today = LocalDate.now();
        for (ArrayList<String> assignmentValue : assignments.values()) {
            if (!assignmentValue.get(1).equals(String.valueOf(this.user_id))) {
                continue;
            }
            ArrayList<String> shiftValue = shifts.get(Integer.parseInt(assignmentValue.get(0)));
            if (shiftValue == null) {
                continue;
            }
            LocalDate shiftDate;
            try {
                shiftDate = LocalDate.parse(shiftValue.get(1), DATE);
            } catch (Exception e) {
                continue;
            }
            if (shiftDate.isBefore(today)) {
                continue;
            }
            rows.add(new Object[]{shiftValue.get(1), shiftValue.get(2), shiftValue.get(3)});
        }
        return rows;
    }

// CONSULTATION
    public void loadWeek(int weekOffset) {
        // weekOffset = -1 previous week, 0 stay on current week, +1 next week
        this.weekOffset += weekOffset;

        LocalDate today = LocalDate.now();
        LocalDate mondayOfThisWeek = today.minusDays(today.getDayOfWeek().getValue() - 1);
        this.weekStart = mondayOfThisWeek.plusWeeks(this.weekOffset);
        LocalDate weekEnd = this.weekStart.plusDays(6);

        weekRows.clear();
        weekConsultIds.clear();

        TreeMap<Integer, ArrayList<String>> consultations = FileHandling.readActiveRecords("Consultations.txt");
        if (consultations == null) {
            return;
        }
        for (Integer consultId : consultations.keySet()) {
            // Consultations(consultation_id, case_id, doctor_id, complaint, vital_signs, notes, consultation_status, consult_room_id, date, start_time, end_time, deleted)
            ArrayList<String> consultationValue = consultations.get(consultId);
            if (!consultationValue.get(1).equals(String.valueOf(this.user_id))) {
                continue;
            }
            LocalDate consultDate;
            try {
                consultDate = LocalDate.parse(consultationValue.get(7), DATE);
            } catch (Exception e) {
                continue;
            }
            if (consultDate.isBefore(this.weekStart) || consultDate.isAfter(weekEnd)) {
                continue;
            }
            autoFinalizeConsultation(consultId, consultationValue);
            weekRows.add(new Object[]{consultationValue.get(7), consultationValue.get(0), consultationValue.get(8) + " - " + consultationValue.get(9), consultationValue.get(2), consultationValue.get(6), capitalize(consultationValue.get(5))});
            weekConsultIds.add(consultId);
        }
    }

    public Object[][] getWeekRows() {
        return weekRows.toArray(new Object[0][]);
    }

    public String getWeekRange() {
        if (this.weekStart == null) {
            return "Week of —";
        }
        DateTimeFormatter label = DateTimeFormatter.ofPattern("dd MMM yyyy");
        return this.weekStart.format(label) + " - " + this.weekStart.plusDays(6).format(label);
    }

    public int getWeekConsultId(int row) {
        if (row < 0 || row >= weekConsultIds.size()) {
            return -1;
        }
        return weekConsultIds.get(row);
    }

// CASES
    public ArrayList<Object[]> getCases() {
        caseRows.clear();
        caseIds.clear();

        TreeMap<Integer, ArrayList<String>> cases = FileHandling.readActiveRecords("Cases.txt");
        TreeMap<Integer, ArrayList<String>> consultations = FileHandling.readActiveRecords("Consultations.txt");
        if (cases == null) {
            return caseRows;
        }

        TreeSet<Integer> involvedCaseIds = new TreeSet<>();
        if (consultations != null) {
            for (ArrayList<String> consultationValue : consultations.values()) { 
                // Consultations(consultation_id, case_id, doctor_id, complaint, vital_signs, notes, consultation_status, consult_room_id, date, start_time, end_time, deleted)
                if (consultationValue.get(1).equals(String.valueOf(this.user_id))) {
                    involvedCaseIds.add(Integer.parseInt(consultationValue.get(0)));
                }
            }
        }

        for (Integer caseId : cases.keySet()) {
            // Cases(case_id, patient_id, doctor_in_charge, open_date, close_date, category, type, case_summary, deleted)
            ArrayList<String> caseValue = cases.get(caseId);
            boolean inCharge = caseValue.get(1).equals(String.valueOf(this.user_id));
            if (!inCharge && !involvedCaseIds.contains(caseId)) {
                continue;
            }
            String status = "Open";
            if (!caseValue.get(3).isEmpty()) { // close_date
                status = "Closed";
            }
            caseRows.add(new Object[]{caseId, getPatientName(caseValue.get(0)), getDoctorName(caseValue.get(1)),
                capitalize(caseValue.get(4)), capitalize(caseValue.get(5)), caseValue.get(2), caseValue.get(3), status});
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
        
        // Cases(case_id, patient_id, doctor_in_charge, open_date, close_date, category, type, case_summary, deleted)
        ArrayList<String> caseValue = cases.get(caseId);
        if (caseValue.get(7).equals("1")) {
            return false;
        }
        this.currentCaseId = caseId;
        this.casePatientId = caseValue.get(0);
        this.caseDoctorInCharge = caseValue.get(1);
        this.caseOpenDate = caseValue.get(2);
        this.caseCloseDate = caseValue.get(3);
        this.caseCategory = caseValue.get(4);
        this.caseType = caseValue.get(5);
        this.caseSummary = caseValue.get(6);

        caseConsultRows.clear();
        caseConsultIds.clear();
        caseTestRows.clear();
        caseTestDetails.clear();

        TreeMap<Integer, ArrayList<String>> consultations = FileHandling.readActiveRecords("Consultations.txt");
        TreeMap<Integer, ArrayList<String>> requests = FileHandling.readActiveRecords("DiagnosticServiceRequests.txt");
        TreeMap<Integer, ArrayList<String>> services = FileHandling.readActiveRecords("DiagnosticServiceCatalogue.txt");

        if (consultations != null) {
            for (Integer consultId : consultations.keySet()) {
                // Consultations(consultation_id, case_id, doctor_id, complaint, vital_signs, notes, consultation_status, consult_room_id, date, start_time, end_time, deleted)
                ArrayList<String> consultationValue = consultations.get(consultId);
                if (!consultationValue.get(0).equals(String.valueOf(caseId))) {
                    continue;
                }
                autoFinalizeConsultation(consultId, consultationValue);
                caseConsultRows.add(new Object[]{consultationValue.get(7), getDoctorName(consultationValue.get(1)), consultationValue.get(2), capitalize(consultationValue.get(5))});
                caseConsultIds.add(consultId);

                if (requests != null && services != null) {
                    for (ArrayList<String> requestValue : requests.values()) {
                        // DiagnosticServiceRequests(request_id, consultation_id, service_id, request_date, request_remarks, result_date, results, deleted)
                        // DiagnosticServiceCatalogue(service_id, service_name, category, type, price, deleted)
                        if (!requestValue.get(0).equals(String.valueOf(consultId))) {
                            continue;
                        }
                        String serviceName = "Unknown";
                        ArrayList<String> serviceValue = services.get(Integer.parseInt(requestValue.get(1)));
                        if (serviceValue != null) {
                            serviceName = serviceValue.get(0);
                        }
                        String status = "Pending";
                        String results = "Not available yet";
                        if (!requestValue.get(4).isEmpty()) {
                            status = "Ready";
                            results = requestValue.get(5);
                        }
                        caseTestRows.add(new Object[]{serviceName, requestValue.get(2), status, requestValue.get(4)});
                        caseTestDetails.add(serviceName
                                + "\n\nRequested by: " + getDoctorName(consultationValue.get(1)) + " on " + requestValue.get(2)
                                + "\n\nRemarks: " + requestValue.get(3)
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
        return "Patient: " + getPatientName(this.casePatientId) + "  |  Opened: " + this.caseOpenDate;
    }

    public String getCaseRoleNote() {
        if (!isCaseOpen()) {
            return "Case is closed and cannot be edited.";
        }
        if (isCaseInCharge()) {
            return "Doctor-in-charge.";
        }
        return "View-only as contributing doctor. Only " + getDoctorName(this.caseDoctorInCharge) + " can edit this case.";
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
        int patientId = Integer.parseInt(this.casePatientId);

        String age = "-";
        String gender = "-";
        if (users != null && users.containsKey(patientId)) {
            ArrayList<String> userValue = users.get(patientId);
            gender = userValue.get(3);
            try {
                LocalDate dob = LocalDate.parse(userValue.get(2), DATE);
                age = String.valueOf(Period.between(dob, LocalDate.now()).getYears());
            } catch (Exception e) {
                age = "-";
            }
        }
        String bloodType = "-";
        String allergies = "-";
        if (patients != null && patients.containsKey(patientId)) {
            ArrayList<String> patientValue = patients.get(patientId);
            bloodType = patientValue.get(0);
            allergies = patientValue.get(1);
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

    /* 
    successful save: null
    error: String error message
    */
    public String saveCaseSummary(String summary) {
        if (!isCaseInCharge()) {
            return "Only doctor-in-charge can edit case summary.";
        }
        if (!isCaseOpen()) {
            return "Case is closed and cannot be edited.";
        }
        if (summary.contains("`")) {
            return "Summary cannot contain a backtick (`) character.";
        }

        ArrayList<String> caseRecord = new ArrayList<>();
        caseRecord.add(String.valueOf(this.currentCaseId));
        caseRecord.add(this.casePatientId);
        caseRecord.add(this.caseDoctorInCharge);
        caseRecord.add(this.caseOpenDate);
        caseRecord.add(this.caseCloseDate);
        caseRecord.add(this.caseCategory);
        caseRecord.add(this.caseType);
        caseRecord.add(summary.trim());
        caseRecord.add("0");
        FileHandling.editRecord("Cases.txt", caseRecord);

        this.caseSummary = summary.trim();
        return null;
    }

    /* 
    successful save: null
    error: String error message
    */
    public String closeCase() {
        if (!isCaseInCharge()) {
            return "Only doctor-in-charge can close case.";
        }
        if (!isCaseOpen()) {
            return "Case is already closed.";
        }
        if (this.caseSummary == null || this.caseSummary.trim().isEmpty()) {
            return "Please write a case summary before closing this case.";
        }

        ArrayList<String> caseRecord = new ArrayList<>();
        caseRecord.add(String.valueOf(this.currentCaseId));
        caseRecord.add(this.casePatientId);
        caseRecord.add(this.caseDoctorInCharge);
        caseRecord.add(this.caseOpenDate);
        caseRecord.add(today());
        caseRecord.add(this.caseCategory);
        caseRecord.add(this.caseType);
        caseRecord.add(this.caseSummary);
        caseRecord.add("0");
        FileHandling.editRecord("Cases.txt", caseRecord);

        this.caseCloseDate = today();
        return null;
    }

// CONSULTATION DIALOG
    public boolean loadConsultation(int consultId) {
        TreeMap<Integer, ArrayList<String>> consultations = FileHandling.readAllRecords("Consultations.txt");
        if (consultations == null || !consultations.containsKey(consultId)) {
            return false;
        }
        // Consultations(consultation_id, case_id, doctor_id, complaint, vital_signs, notes, consultation_status, consult_room_id, date, start_time, end_time, deleted)
        ArrayList<String> consultationValue = consultations.get(consultId);
        if (consultationValue.get(10).equals("1")) {
            return false;
        }
        autoFinalizeConsultation(consultId, consultationValue);
        this.currentConsultId = consultId;
        this.consultCaseId = consultationValue.get(0);
        this.consultDoctorId = consultationValue.get(1);
        this.consultComplaint = consultationValue.get(2);
        this.consultVitals = consultationValue.get(3);
        this.consultNotes = consultationValue.get(4);
        this.consultStatus = consultationValue.get(5);
        this.consultRoom = consultationValue.get(6);
        this.consultDate = consultationValue.get(7);
        this.consultStart = consultationValue.get(8);
        this.consultEnd = consultationValue.get(9);
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
            return "Consultation conducted by " + getDoctorName(this.consultDoctorId)
                    + ". View-only.";
        }
        if (!this.consultStatus.equals("booked") && !this.consultStatus.equals("incomplete")) {
            return "Consultation is " + this.consultStatus + " and cannot be edited.";
        }
        if (isCaseClosed(this.consultCaseId)) {
            return "Case is closed. Consultation details cannot be edited.";
        }
        LocalDate consultDateParsed = LocalDate.parse(this.consultDate, DATE);
        LocalDate today = LocalDate.now();
        if (consultDateParsed.isAfter(today)) {
            return "Consultation not scheduled on this day.";
        }
        if (consultDateParsed.equals(today) && LocalTime.now().isBefore(LocalTime.parse(this.consultStart))) {
            return "This consultation has not started yet. Details can be added once it starts.";
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
        if (this.consultStatus.equals("incomplete")) {
            return consultDateParsed.equals(LocalDate.now());
        }
        try {
            LocalDateTime slotStart = LocalDateTime.of(consultDateParsed, LocalTime.parse(this.consultStart));
            return !LocalDateTime.now().isBefore(slotStart);
        } catch (Exception e) {
            return false;
        }
    }

    /* 
    successful save: null
    error: String error message
    */
    private String saveConsultation(String vitals, String notes, String newStatus) {
        if (this.currentConsultId == -1) {
            return "No consultation loaded.";
        }
        if (!canEditConsultation()) {
            return "Consultation cannot be edited.";
        }
        if (vitals.contains("`") || notes.contains("`")) {
            return "Vital signs and notes cannot contain a backtick (`) character.";
        }

        ArrayList<String> consultationRecord = new ArrayList<>();
        consultationRecord.add(String.valueOf(this.currentConsultId));
        consultationRecord.add(this.consultCaseId);
        consultationRecord.add(this.consultDoctorId);
        consultationRecord.add(this.consultComplaint);
        consultationRecord.add(vitals.trim());
        consultationRecord.add(notes.trim());
        consultationRecord.add(newStatus);
        consultationRecord.add(this.consultRoom);
        consultationRecord.add(this.consultDate);
        consultationRecord.add(this.consultStart);
        consultationRecord.add(this.consultEnd);
        consultationRecord.add("0");
        FileHandling.editRecord("Consultations.txt", consultationRecord);

        this.consultVitals = vitals.trim();
        this.consultNotes = notes.trim();
        this.consultStatus = newStatus;
        return null;
    }


    public String saveConsultationProgress(String vitals, String notes) {
        return saveConsultation(vitals, notes, "incomplete");
    }

    public String completeConsultation(String vitals, String notes) {
        return saveConsultation(vitals, notes, "completed");
    }

// PRESCRIPTIONS
    public ArrayList<String> getDrugForms() {
        ArrayList<String> selectionForms = new ArrayList<>();
        selectionForms.add("All");
        TreeSet<String> catalogueForms = new TreeSet<>();
        TreeMap<Integer, ArrayList<String>> drugs = FileHandling.readActiveRecords("DrugCatalogue.txt");
        if (drugs != null) {
            for (ArrayList<String> drugValue : drugs.values()) {
                catalogueForms.add(drugValue.get(1));
            }
        }
        selectionForms.addAll(catalogueForms);
        return selectionForms;
    }

    public ArrayList<Object[]> searchDrugs(String searchForm, String searchText) {
        ArrayList<Object[]> rows = new ArrayList<>();
        drugResultIds.clear();

        // DrugCatalogue(drug_id, drug_name, form, price, deleted)
        TreeMap<Integer, ArrayList<String>> drugs = FileHandling.readActiveRecords("DrugCatalogue.txt");
        if (drugs == null) {
            return rows;
        }
        String search = "";
        if (searchText != null) {
            search = searchText.trim().toLowerCase();
        }
        for (Integer drugId : drugs.keySet()) {
            ArrayList<String> drugValue = drugs.get(drugId);
            if (searchForm != null && !searchForm.equalsIgnoreCase("All") && !drugValue.get(1).equalsIgnoreCase(searchForm)) {
                continue;
            }
            if (!search.isEmpty() && !drugValue.get(0).toLowerCase().contains(search)) {
                continue;
            }
            double price = 0.0;
            try {
                price = Double.parseDouble(drugValue.get(2));
            } catch (Exception e) {
                price = 0.0;
            }
            rows.add(new Object[]{drugValue.get(0), drugValue.get(1), String.format("%.2f", price)});
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
                return "Dosage (tab/cap)";
            case "syrup/solution":
                return "Dosage (mL)";
            case "powder":
                return "Dosage (g)";
            case "injection":
                return "Dosage (mg)";
            case "drops":
                return "Dosage (drops)";
            case "cream/ointment/gel":
                return "Dosage (mL)";
            default:
                return "Dosage";
        }
    }

    public ArrayList<String[]> getPrescriptionItems(int consultId) {
        ArrayList<String[]> rows = new ArrayList<>();
        TreeMap<Integer, ArrayList<String>> prescriptions = FileHandling.readActiveRecords("Prescriptions.txt");
        TreeMap<Integer, ArrayList<String>> items = FileHandling.readActiveRecords("PrescriptionItems.txt");
        TreeMap<Integer, ArrayList<String>> drugs = FileHandling.readActiveRecords("DrugCatalogue.txt");
        if (prescriptions == null || items == null) {
            return rows;
        }
        for (Integer prescriptionId : prescriptions.keySet()) {
            ArrayList<String> prescriptionValue = prescriptions.get(prescriptionId);
            if (!prescriptionValue.get(0).equals(String.valueOf(consultId))) {
                continue;
            }
            for (ArrayList<String> itemValue : items.values()) {
                if (!itemValue.get(0).equals(String.valueOf(prescriptionId))) {
                    continue;
                }
                String drugName = "Unknown";
                if (drugs != null) {
                    ArrayList<String> drugValue = drugs.get(Integer.parseInt(itemValue.get(1)));
                    if (drugValue != null) {
                        drugName = drugValue.get(0);
                    }
                }
                rows.add(new String[]{itemValue.get(1), drugName, itemValue.get(2), itemValue.get(3), itemValue.get(4), itemValue.get(5)});
            }
        }
        return rows;
    }

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

    public String savePrescription(int consultId, ArrayList<String[]> items) {
        if (!loadConsultation(consultId) || !canEditConsultation()) {
            return "Cannot write a prescription for this consultation.";
        }
        if (items == null || items.isEmpty()) {
            return "Please add at least one drug to the prescription.";
        }

        Integer prescriptionId = null;
        TreeMap<Integer, ArrayList<String>> prescriptions = FileHandling.readActiveRecords("Prescriptions.txt");
        if (prescriptions != null) {
            for (Integer id : prescriptions.keySet()) {
                ArrayList<String> prescriptionValue = prescriptions.get(id);
                if (prescriptionValue.get(0).equals(String.valueOf(consultId))) {
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
                    ArrayList<String> itemValue = existingItems.get(itemId);
                    if (itemValue.get(0).equals(String.valueOf(prescriptionId))) {
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

// DIAGNOSTIC REQUESTS
    public ArrayList<String> getServiceCategories() {
        ArrayList<String> selectionCategories = new ArrayList<>();
        selectionCategories.add("All");
        TreeSet<String> catalogueCategories = new TreeSet<>();
        TreeMap<Integer, ArrayList<String>> services = FileHandling.readActiveRecords("DiagnosticServiceCatalogue.txt");
        if (services != null) {
            for (ArrayList<String> serviceValue : services.values()) { 
                catalogueCategories.add(serviceValue.get(1));
            }
        }
        selectionCategories.addAll(catalogueCategories);
        return selectionCategories;
    }

    public ArrayList<String> getServiceTypes(String category) {
        ArrayList<String> selectionTypes = new ArrayList<>();
        selectionTypes.add("All");
        TreeSet<String> catalogueTypes = new TreeSet<>();
        TreeMap<Integer, ArrayList<String>> services = FileHandling.readActiveRecords("DiagnosticServiceCatalogue.txt");
        if (services != null) {
            for (ArrayList<String> serviceValue : services.values()) { 
                if (category != null && !category.equalsIgnoreCase("All") && !serviceValue.get(1).equalsIgnoreCase(category)) {
                    continue;
                }
                catalogueTypes.add(serviceValue.get(2));
            }
        }
        selectionTypes.addAll(catalogueTypes);
        return selectionTypes;
    }

    public ArrayList<Object[]> searchServices(String searchCategory, String searchType, String searchText) {
        ArrayList<Object[]> rows = new ArrayList<>();
        serviceResultIds.clear();

        // DiagnosticServiceCatalogue(service_id, service_name, category, type, price, deleted)
        TreeMap<Integer, ArrayList<String>> services = FileHandling.readActiveRecords("DiagnosticServiceCatalogue.txt");
        if (services == null) {
            return rows;
        }
        String search = "";
        if (searchText != null) {
            search = searchText.trim().toLowerCase();
        }
        for (Integer serviceId : services.keySet()) {
            ArrayList<String> serviceValue = services.get(serviceId); 
            if (searchCategory != null && !searchCategory.equalsIgnoreCase("All") && !serviceValue.get(1).equalsIgnoreCase(searchCategory)) {
                continue;
            }
            if (searchType != null && !searchType.equalsIgnoreCase("All") && !serviceValue.get(2).equalsIgnoreCase(searchType)) {
                continue;
            }
            if (!search.isEmpty() && !serviceValue.get(0).toLowerCase().contains(search)) {
                continue;
            }
            double price = 0.0;
            try {
                price = Double.parseDouble(serviceValue.get(3));
            } catch (Exception e) {
                price = 0.0;
            }
            rows.add(new Object[]{serviceValue.get(0), capitalize(serviceValue.get(1)), capitalize(serviceValue.get(2)), String.format("%.2f", price)});
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

    public String checkRemarkText(String text) {
        if (text != null && text.contains("`")) {
            return "Remarks cannot contain a backtick (`) character.";
        }
        return null;
    }

    public String submitDiagnosticRequests(int consultId, ArrayList<String[]> requests) {
        if (!loadConsultation(consultId) || !canEditConsultation()) {
            return "Cannot request diagnostic services for this consultation.";
        }
        if (requests == null || requests.isEmpty()) {
            return "Please add at least one service to the request list.";
        }

        for (String[] request : requests) {
            ArrayList<String> requestRecord = new ArrayList<>();
            requestRecord.add(String.valueOf(FileHandling.getNextID("DiagnosticServiceRequests.txt")));
            requestRecord.add(String.valueOf(consultId));
            requestRecord.add(request[0]);
            requestRecord.add(today());
            requestRecord.add(request[1]);
            requestRecord.add("");
            requestRecord.add("");
            requestRecord.add("0");
            FileHandling.addRecord("DiagnosticServiceRequests.txt", requestRecord);
        }
        return null;
    }

    public ArrayList<Object[]> getDiagnosticRequestItems(int consultId) {
        ArrayList<Object[]> rows = new ArrayList<>();
        TreeMap<Integer, ArrayList<String>> requests = FileHandling.readActiveRecords("DiagnosticServiceRequests.txt");
        TreeMap<Integer, ArrayList<String>> services = FileHandling.readActiveRecords("DiagnosticServiceCatalogue.txt");
        if (requests == null) {
            return rows;
        }
        for (Integer requestId : requests.keySet()) {
            ArrayList<String> requestValue = requests.get(requestId);
            if (!requestValue.get(0).equals(String.valueOf(consultId))) {
                continue;
            }
            String serviceName = "Unknown";
            if (services != null) {
                ArrayList<String> serviceValue = services.get(Integer.parseInt(requestValue.get(1)));
                if (serviceValue != null) {
                    serviceName = serviceValue.get(0);
                }
            }
            rows.add(new Object[]{requestId, requestValue.get(1), serviceName, requestValue.get(2), requestValue.get(3)});
        }
        return rows;
    }

    /* 
    successful save: null
    error: String error message
    */    
    public String deleteDiagnosticRequest(int requestId) {
        ArrayList<String> requestRecord = FileHandling.readSpecificRecord("DiagnosticServiceRequests.txt", requestId);
        if (requestRecord == null) {
            return "This diagnostic request could not be found.";
        }
        int consultId = Integer.parseInt(requestRecord.get(1)); 
        if (!loadConsultation(consultId) || !canEditConsultation()) {
            return "You cannot delete diagnostic requests for this consultation.";
        }
        FileHandling.removeRecord("DiagnosticServiceRequests.txt", requestId);
        return null;
    }

// REVIEWS
    public ArrayList<Object[]> getReviews() {
        ArrayList<Object[]> rows = new ArrayList<>();
        TreeMap<Integer, ArrayList<String>> reviews = FileHandling.readActiveRecords("Reviews.txt");
        TreeMap<Integer, ArrayList<String>> consultations = FileHandling.readAllRecords("Consultations.txt");
        TreeMap<Integer, ArrayList<String>> cases = FileHandling.readAllRecords("Cases.txt");
        if (reviews == null || consultations == null || cases == null) {
            return rows;
        }
        for (ArrayList<String> reviewValue : reviews.values()) { 
            int consultId = Integer.parseInt(reviewValue.get(0));
            ArrayList<String> consultationValue = consultations.get(consultId); 
            if (consultationValue == null || !consultationValue.get(1).equals(String.valueOf(this.user_id))) {
                continue;
            }
            String patientName = "Unknown";
            ArrayList<String> caseValue = cases.get(Integer.parseInt(consultationValue.get(0)));
            if (caseValue != null) {
                patientName = getPatientName(caseValue.get(0));
            }
            rows.add(new Object[]{consultId, patientName, reviewValue.get(1), reviewValue.get(3), reviewValue.get(2)});
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
        return String.format("Average rating: %.1f out of 5 (%d review%s).", average, reviews.size(), reviews.size() == 1 ? "" : "s");
    }
}
