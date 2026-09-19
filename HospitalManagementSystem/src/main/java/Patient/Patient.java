package Patient;

import HelperFunction.FileHandling;
import Users.Role;
import Users.User;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.TreeMap;

// One patient: their details and everything they can do.
// The screens talk to this class through the PatientServices interface.
public class Patient extends User implements PatientServices {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    // =====================================================================
    // FIELDS
    // =====================================================================
    private String blood_type, allergies;

    // Medical History: rows for the 3 tables + one details text per row (same order)
    private ArrayList<Object[]> visitRows = new ArrayList<>();
    private ArrayList<Object[]> rxRows = new ArrayList<>();
    private ArrayList<Object[]> testRows = new ArrayList<>();
    private ArrayList<String> visitDetails = new ArrayList<>();
    private ArrayList<String> rxDetails = new ArrayList<>();
    private ArrayList<String> testDetails = new ArrayList<>();

    // Bookings
    private ArrayList<Object[]> slotRows = new ArrayList<>();
    private ArrayList<String[]> slotInfo = new ArrayList<>();
    private String lastBookedRoom = "";

    // =====================================================================
    // CONSTRUCTORS
    // =====================================================================
    // Original constructor (used by the rest of the group)
    public Patient(int user_id, String first_name, String last_name, String phone, String email,
            String password, String gender, LocalDate dob, Role role, String blood_type, String allergies) {
        super(user_id, first_name, last_name, phone, email, password, gender, dob, role);
        this.blood_type = blood_type;
        this.allergies = allergies;
    }

    // Builds a patient straight from the files (used after login)
    public Patient(int user_id) {
        this.user_id = user_id;
        this.role = Role.PATIENT;
        reload();
    }

    // =====================================================================
    // GETTERS AND SETTERS
    // =====================================================================
    public String getBloodType() {
        return this.blood_type;
    }

    public String getAllergies() {
        return this.allergies;
    }

    public void setBloodType(String blood_type) {
        this.blood_type = blood_type;
    }

    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }

    // "Farah Hidayah binti Hassan"
    public String getFullName() {
        return this.first_name + " " + this.last_name;
    }

    // date of birth as text, e.g. "05-03-2012"
    public String getDobText() {
        if (this.dob == null) {
            return "";
        }
        return this.dob.format(DATE);
    }

    // =====================================================================
    // PROFILE
    // =====================================================================
    // Reads this patient's details from Users.txt and Patients.txt
    public final void reload() {
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
        setEmail(u.get(5));
        this.password = u.get(6);

        try {
            this.dob = LocalDate.parse(u.get(2), DATE);
        } catch (Exception e) {
            this.dob = null;
            System.out.println("Bad date of birth for user " + this.user_id);
        }

        this.blood_type = "";
        this.allergies = "";
        TreeMap<Integer, ArrayList<String>> patients = FileHandling.readAllRecords("Patients.txt");
        if (patients != null && patients.containsKey(this.user_id)) {
            this.blood_type = patients.get(this.user_id).get(0);
            this.allergies = patients.get(this.user_id).get(1);
        }
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
            if (newPwd.contains(",") || newPwd.contains("\"")) {
                return "Password cannot contain commas or quotes.";
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
        setEmail(newEmail);
        this.password = newPassword;
        return null;   // null means success
    }

    // =====================================================================
    // SHARED LITTLE HELPERS
    // =====================================================================
    // "9" -> "Dr. Kavitha a/p Rajendran"
    public String getDoctorName(String doctorId) {
        TreeMap<Integer, ArrayList<String>> users = FileHandling.readAllRecords("Users.txt");
        int id = Integer.parseInt(doctorId);
        if (users == null || !users.containsKey(id)) {
            return "Unknown";
        }
        return "Dr. " + users.get(id).get(0) + " " + users.get(id).get(1);
    }

    // true if this consultation is my own, completed and not deleted
    private boolean isMyCompletedVisit(TreeMap<Integer, ArrayList<String>> cases, ArrayList<String> c) {
        ArrayList<String> k = cases.get(Integer.parseInt(c.get(0)));
        if (k == null || !k.get(0).equals(String.valueOf(this.user_id))) {
            return false;
        }
        return c.get(5).equals("completed") && !c.get(10).equals("1");
    }

    // the review of one visit, or null if it has none
    private ArrayList<String> findReview(TreeMap<Integer, ArrayList<String>> reviews, int consultId) {
        if (reviews == null) {
            return null;
        }
        for (ArrayList<String> r : reviews.values()) { // 0 consultation_id, 1 rating, 2 comments, 3 deleted
            if (r.get(0).equals(String.valueOf(consultId)) && r.get(3).equals("0")) {
                return r;
            }
        }
        return null;
    }

    private int toMinutes(String time) {                 // "09:30" -> 570
        return Integer.parseInt(time.substring(0, 2)) * 60 + Integer.parseInt(time.substring(3, 5));
    }

    private String toTime(int minutes) {                 // 570 -> "09:30"
        return String.format("%02d:%02d", minutes / 60, minutes % 60);
    }

    private String sortKey(String date, String time) {   // -> "202609220930"
        return date.substring(6, 10) + date.substring(3, 5) + date.substring(0, 2) + time.replace(":", "");
    }

    private String now() {
        return new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
    }

    private String getSpecialization(TreeMap<Integer, ArrayList<String>> doctors, String doctorId) {
        ArrayList<String> d = doctors.get(Integer.parseInt(doctorId)); // 0 department_id, 1 specialization
        if (d == null) {
            return "-";
        }
        return d.get(1);
    }

    // department ids patients may book (no Emergency, no deleted)
    private ArrayList<String> bookableDeptIds() {
        ArrayList<String> ids = new ArrayList<>();
        TreeMap<Integer, ArrayList<String>> depts = FileHandling.readAllRecords("Departments.txt");
        if (depts == null) {
            return ids;
        }
        for (Integer id : depts.keySet()) {
            ArrayList<String> d = depts.get(id); // 0 name, 3 deleted
            if (d.get(3).equals("1") || d.get(0).equals("Emergency")) {
                continue;
            }
            ids.add(String.valueOf(id));
        }
        return ids;
    }

    // =====================================================================
    // RATINGS
    // =====================================================================
    // completed visits that I have NOT rated yet
    public ArrayList<Object[]> getPendingRatings() {
        ArrayList<Object[]> rows = new ArrayList<>();
        TreeMap<Integer, ArrayList<String>> cases = FileHandling.readAllRecords("Cases.txt");
        TreeMap<Integer, ArrayList<String>> consults = FileHandling.readAllRecords("Consultations.txt");
        TreeMap<Integer, ArrayList<String>> reviews = FileHandling.readAllRecords("Reviews.txt");
        if (cases == null || consults == null) {
            return rows;
        }
        for (Integer consultId : consults.keySet()) {
            ArrayList<String> c = consults.get(consultId);
            if (!isMyCompletedVisit(cases, c)) {
                continue;
            }
            if (findReview(reviews, consultId) != null) {
                continue; // already rated
            }
            rows.add(new Object[]{consultId, c.get(7), getDoctorName(c.get(1)), c.get(2)});
        }
        return rows;
    }

    // visits I have already rated
    public ArrayList<Object[]> getPastRatings() {
        ArrayList<Object[]> rows = new ArrayList<>();
        TreeMap<Integer, ArrayList<String>> cases = FileHandling.readAllRecords("Cases.txt");
        TreeMap<Integer, ArrayList<String>> consults = FileHandling.readAllRecords("Consultations.txt");
        TreeMap<Integer, ArrayList<String>> reviews = FileHandling.readAllRecords("Reviews.txt");
        if (cases == null || consults == null) {
            return rows;
        }
        for (Integer consultId : consults.keySet()) {
            ArrayList<String> c = consults.get(consultId);
            if (!isMyCompletedVisit(cases, c)) {
                continue;
            }
            ArrayList<String> r = findReview(reviews, consultId);
            if (r == null) {
                continue;
            }
            rows.add(new Object[]{c.get(7), getDoctorName(c.get(1)), r.get(1), r.get(2)});
        }
        return rows;
    }

    // Saves a rating. Returns null if saved, or the error message.
    public String submitRating(String consultId, int rating, String comment) {
        if (consultId == null || consultId.isEmpty()) {
            return "Please select a visit to rate.";
        }
        if (rating < 1 || rating > 5) {
            return "Please choose a rating from 1 to 5.";
        }
        if (comment.contains("\"")) {
            return "Comment cannot contain double quotes.";
        }
        comment = comment.trim().replace("\n", " "); // a new line would break the file

        ArrayList<String> record = new ArrayList<>();
        record.add(String.valueOf(FileHandling.getNextID("Reviews.txt")));
        record.add(consultId);
        record.add(String.valueOf(rating));
        record.add(comment);
        record.add("0");
        FileHandling.addRecord("Reviews.txt", record);
        return null;
    }

    // =====================================================================
    // MEDICAL HISTORY
    // =====================================================================
    // Reads the files once and builds the rows + details for all 3 tabs
    public void loadHistory() {
        visitRows.clear();
        rxRows.clear();
        testRows.clear();
        visitDetails.clear();
        rxDetails.clear();
        testDetails.clear();

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
            // c: 0 case_id, 1 doctor, 2 complaint, 3 vital_signs, 4 notes, 5 status, 6 room, 7 date, 10 deleted
            ArrayList<String> c = consults.get(consultId);
            if (!isMyCompletedVisit(cases, c)) {
                continue;
            }
            // k: 0 patient_id, 4 category, 6 case_summary
            ArrayList<String> k = cases.get(Integer.parseInt(c.get(0)));
            String date = c.get(7);
            String doctor = getDoctorName(c.get(1));

            // ---- Visits tab ----
            visitRows.add(new Object[]{date, doctor, k.get(4), c.get(2)});
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
                        // 0 prescription_id, 1 drug_id, 2 dosage, 3 frequency, 4 duration, 5 instructions, 6 deleted
                        if (!it.get(0).equals(String.valueOf(rxId)) || it.get(6).equals("1")) {
                            continue;
                        }
                        String medicine = "Unknown";
                        ArrayList<String> d = drugs.get(Integer.parseInt(it.get(1)));
                        if (d != null) {
                            medicine = d.get(0);
                        }
                        rxRows.add(new Object[]{date, medicine, it.get(2), it.get(3), it.get(4) + " days"});
                        rxDetails.add(medicine
                                + "\n\nInstructions: " + it.get(5)
                                + "\n\nPrescribed by " + doctor + " on " + date);
                    }
                }
            }

            // ---- Test Results tab ----
            if (requests != null && services != null) {
                for (ArrayList<String> r : requests.values()) {
                    // 0 consultation_id, 1 service_id, 2 request_date, 3 remarks, 4 result_date, 5 results, 6 deleted
                    if (!r.get(0).equals(String.valueOf(consultId)) || r.get(6).equals("1")) {
                        continue;
                    }
                    String testName = "Unknown";
                    String category = "-";
                    ArrayList<String> s = services.get(Integer.parseInt(r.get(1)));
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
                    testRows.add(new Object[]{r.get(2), testName, category, r.get(4), status});
                    testDetails.add(testName
                            + "\n\nRequest remarks: " + r.get(3)
                            + "\n\nResults: " + results);
                }
            }
        }
    }

    public ArrayList<Object[]> getVisitRows() {
        return visitRows;
    }

    public ArrayList<String> getVisitDetails() {
        return visitDetails;
    }

    public ArrayList<Object[]> getPrescriptionRows() {
        return rxRows;
    }

    public ArrayList<String> getPrescriptionDetails() {
        return rxDetails;
    }

    public ArrayList<Object[]> getTestRows() {
        return testRows;
    }

    public ArrayList<String> getTestDetails() {
        return testDetails;
    }

    // =====================================================================
    // BOOKINGS - filters
    // =====================================================================
    public ArrayList<String[]> getDepartments() {
        ArrayList<String[]> list = new ArrayList<>();
        list.add(new String[]{"all", "All"});
        TreeMap<Integer, ArrayList<String>> depts = FileHandling.readAllRecords("Departments.txt");
        if (depts == null) {
            return list;
        }
        for (Integer id : depts.keySet()) {
            ArrayList<String> d = depts.get(id); // 0 name, 3 deleted
            if (d.get(3).equals("1") || d.get(0).equals("Emergency")) {
                continue;
            }
            list.add(new String[]{String.valueOf(id), d.get(0)});
        }
        return list;
    }

    public ArrayList<String[]> getDoctors(String deptId) {
        ArrayList<String[]> list = new ArrayList<>();
        list.add(new String[]{"any", "Any"});
        TreeMap<Integer, ArrayList<String>> doctors = FileHandling.readAllRecords("Doctors.txt");
        TreeMap<Integer, ArrayList<String>> users = FileHandling.readAllRecords("Users.txt");
        if (doctors == null || users == null) {
            return list;
        }
        ArrayList<String> bookable = bookableDeptIds();
        for (Integer id : doctors.keySet()) {
            ArrayList<String> d = doctors.get(id); // 0 department_id
            ArrayList<String> u = users.get(id);   // 0 first, 1 last, 8 deleted
            if (u == null || u.get(8).equals("1")) {
                continue;
            }
            if (!bookable.contains(d.get(0))) {
                continue;
            }
            if (!deptId.equals("all") && !d.get(0).equals(deptId)) {
                continue;
            }
            list.add(new String[]{String.valueOf(id), "Dr. " + u.get(0) + " " + u.get(1)});
        }
        return list;
    }

    // =====================================================================
    // BOOKINGS - clash checks and rooms
    // =====================================================================
    // true if the doctor AND this patient are both free
    private boolean isFree(TreeMap<Integer, ArrayList<String>> consults, TreeMap<Integer, ArrayList<String>> cases,
            String doctorId, String date, String start, String end, int ignoreId) {
        for (Integer id : consults.keySet()) {
            if (id == ignoreId) {
                continue;
            }
            ArrayList<String> c = consults.get(id); // 0 case, 1 doctor, 5 status, 7 date, 8 start, 9 end, 10 deleted
            if (c.get(10).equals("1") || c.get(5).equals("cancelled") || !c.get(7).equals(date)) {
                continue;
            }
            boolean overlap = toMinutes(start) < toMinutes(c.get(9)) && toMinutes(c.get(8)) < toMinutes(end);
            if (!overlap) {
                continue;
            }
            if (c.get(1).equals(doctorId)) {
                return false;   // doctor busy
            }
            ArrayList<String> k = cases.get(Integer.parseInt(c.get(0)));
            if (k != null && k.get(0).equals(String.valueOf(this.user_id))) {
                return false;   // I am busy
            }
        }
        return true;
    }

    private boolean isRoomUsable(TreeMap<Integer, ArrayList<String>> rooms, String roomId) {
        ArrayList<String> r = rooms.get(Integer.parseInt(roomId)); // 0 status, 1 deleted
        return r != null && r.get(0).equals("ok") && r.get(1).equals("0");
    }

    private boolean isRoomFree(TreeMap<Integer, ArrayList<String>> consults, String roomId,
            String date, String start, String end, int ignoreId) {
        for (Integer id : consults.keySet()) {
            if (id == ignoreId) {
                continue;
            }
            ArrayList<String> c = consults.get(id);
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

    // the doctor's room that day if free, else the first working free room, else ""
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
        return "";
    }

    // =====================================================================
    // BOOKINGS - the slot list
    // =====================================================================
    public void loadSlots(String deptId, String doctorFilter, String date) {
        slotRows.clear();
        slotInfo.clear();

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
        String now = now();
        ArrayList<String> bookable = bookableDeptIds();

        // 1) my upcoming bookings (any date)
        for (Integer consultId : consults.keySet()) {
            ArrayList<String> c = consults.get(consultId);
            ArrayList<String> k = cases.get(Integer.parseInt(c.get(0)));
            if (k == null || !k.get(0).equals(String.valueOf(this.user_id))) {
                continue;
            }
            if (!c.get(5).equals("booked") || c.get(10).equals("1")) {
                continue;
            }
            if (sortKey(c.get(7), c.get(8)).compareTo(now) <= 0) {
                continue;   // already passed
            }
            slotRows.add(new Object[]{c.get(7), c.get(8) + " - " + c.get(9), getDoctorName(c.get(1)),
                getSpecialization(doctors, c.get(1)), "Room " + c.get(6), "Booked (You)"});
            slotInfo.add(new String[]{"booked", String.valueOf(consultId), c.get(1)});
        }

        // 2) free 30-minute slots on the chosen date
        for (Integer shiftId : shifts.keySet()) {
            ArrayList<String> s = shifts.get(shiftId); // 0 department_id, 1 date, 2 start, 3 end, 4 deleted
            if (!s.get(1).equals(date) || s.get(4).equals("1")) {
                continue;
            }
            if (!bookable.contains(s.get(0))) {
                continue;
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

                for (int m = toMinutes(s.get(2)); m + 30 <= toMinutes(s.get(3)); m += 30) {
                    String start = toTime(m);
                    String end = toTime(m + 30);
                    if (sortKey(date, start).compareTo(now) <= 0) {
                        continue;
                    }
                    if (!isFree(consults, cases, doctorId, date, start, end, -1)) {
                        continue;
                    }
                    slotRows.add(new Object[]{date, start + " - " + end, doctorName, spec, "-", "Available"});
                    slotInfo.add(new String[]{"available", doctorId, date, start, end});
                }
            }
        }
    }

    public ArrayList<Object[]> getSlotRows() {
        return slotRows;
    }

    public ArrayList<String[]> getSlotInfo() {
        return slotInfo;
    }

    public String getLastBookedRoom() {
        return lastBookedRoom;
    }

    // =====================================================================
    // BOOKINGS - book, reschedule, cancel
    // =====================================================================
    // info = {"available", doctorId, date, start, end}. Returns null if booked.
    public String bookSlot(String[] info, String category, String reason) {
        if (info == null || !info[0].equals("available")) {
            return "Please select a slot marked Available.";
        }
        String doctorId = info[1];
        String date = info[2];
        String start = info[3];
        String end = info[4];

        reason = reason.trim().replace("\n", " ");
        if (reason.isEmpty()) {
            return "Please enter the reason for your visit.";
        }
        if (reason.contains("\"")) {
            return "Reason cannot contain double quotes.";
        }

        TreeMap<Integer, ArrayList<String>> cases = FileHandling.readAllRecords("Cases.txt");
        TreeMap<Integer, ArrayList<String>> consults = FileHandling.readAllRecords("Consultations.txt");
        if (cases == null) {
            cases = new TreeMap<>();
        }
        if (consults == null) {
            consults = new TreeMap<>();
        }
        if (!isFree(consults, cases, doctorId, date, start, end, -1)) {
            return "Sorry, this slot is no longer available.";
        }
        String room = pickRoom(consults, doctorId, date, start, end, -1);
        if (room.isEmpty()) {
            return "No consultation room is free at this time. Please choose another slot.";
        }

        // a new Case for this booking
        String today = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
        int caseId = FileHandling.getNextID("Cases.txt");

        ArrayList<String> newCase = new ArrayList<>();
        newCase.add(String.valueOf(caseId));
        newCase.add(String.valueOf(this.user_id));
        newCase.add(doctorId);       // doctor_in_charge
        newCase.add(today);          // open_date
        newCase.add("");             // close_date
        newCase.add(category.toLowerCase());
        newCase.add("scheduled");
        newCase.add("");             // case_summary
        newCase.add("0");
        FileHandling.addRecord("Cases.txt", newCase);

        // the Consultation linked to it
        ArrayList<String> consult = new ArrayList<>();
        consult.add(String.valueOf(FileHandling.getNextID("Consultations.txt")));
        consult.add(String.valueOf(caseId));
        consult.add(doctorId);
        consult.add(reason);         // complaint
        consult.add("");             // vital_signs
        consult.add("");             // notes
        consult.add("booked");
        consult.add(room);
        consult.add(date);
        consult.add(start);
        consult.add(end);
        consult.add("0");
        FileHandling.addRecord("Consultations.txt", consult);

        lastBookedRoom = room;
        return null;
    }

    // free times of one doctor on one date, e.g. "10:00 - 10:30"
    public ArrayList<String> getFreeTimes(String doctorId, String date, int ignoreId) {
        ArrayList<String> options = new ArrayList<>();
        TreeMap<Integer, ArrayList<String>> cases = FileHandling.readAllRecords("Cases.txt");
        TreeMap<Integer, ArrayList<String>> consults = FileHandling.readAllRecords("Consultations.txt");
        TreeMap<Integer, ArrayList<String>> shifts = FileHandling.readAllRecords("Shifts.txt");
        TreeMap<Integer, ArrayList<String>> shiftDocs = FileHandling.readAllRecords("ShiftDoctors.txt");
        if (cases == null || consults == null || shifts == null || shiftDocs == null) {
            return options;
        }
        String now = now();
        for (Integer shiftId : shifts.keySet()) {
            ArrayList<String> s = shifts.get(shiftId);
            if (!s.get(1).equals(date) || s.get(4).equals("1")) {
                continue;
            }
            boolean doctorOnShift = false;
            for (ArrayList<String> a : shiftDocs.values()) {
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
                if (isFree(consults, cases, doctorId, date, start, end, ignoreId)) {
                    options.add(start + " - " + end);
                }
            }
        }
        return options;
    }

    // Moves a booking. Returns null if done.
    public String rescheduleBooking(int consultId, String doctorId, String date, String start, String end) {
        TreeMap<Integer, ArrayList<String>> consults = FileHandling.readAllRecords("Consultations.txt");
        if (consults == null) {
            return "No bookings found.";
        }
        String room = pickRoom(consults, doctorId, date, start, end, consultId);
        if (room.isEmpty()) {
            return "No consultation room is free at that time.";
        }
        // with the id: 7 room, 8 date, 9 start, 10 end
        ArrayList<String> record = FileHandling.readSpecificRecord("Consultations.txt", consultId);
        record.set(7, room);
        record.set(8, date);
        record.set(9, start);
        record.set(10, end);
        FileHandling.editRecord("Consultations.txt", record);

        lastBookedRoom = room;
        return null;
    }

    // Cancels a booking (soft delete). Returns null if done.
    public String cancelBooking(int consultId) {
        // with the id: 1 case_id, 6 status, 11 deleted
        ArrayList<String> consult = FileHandling.readSpecificRecord("Consultations.txt", consultId);
        if (consult == null) {
            return "That booking could not be found.";
        }
        String caseId = consult.get(1);
        consult.set(6, "cancelled");
        consult.set(11, "1");
        FileHandling.editRecord("Consultations.txt", consult);

        // delete the case only if no other visit uses it (keeps old history safe)
        TreeMap<Integer, ArrayList<String>> consults = FileHandling.readAllRecords("Consultations.txt");
        boolean caseStillUsed = false;
        if (consults != null) {
            for (Integer id : consults.keySet()) {
                ArrayList<String> c = consults.get(id); // 0 case_id, 10 deleted
                if (id != consultId && c.get(0).equals(caseId) && c.get(10).equals("0")) {
                    caseStillUsed = true;
                }
            }
        }
        if (!caseStillUsed) {
            ArrayList<String> k = FileHandling.readSpecificRecord("Cases.txt", Integer.parseInt(caseId)); // 8 deleted
            if (k != null) {
                k.set(8, "1");
                FileHandling.editRecord("Cases.txt", k);
            }
        }
        return null;
    }

    // =====================================================================
    // LOGIN
    // =====================================================================
    // Returns the patient's id if the email and password match an active patient, or -1
    public static int login(String email, String password) {
        TreeMap<Integer, ArrayList<String>> users = FileHandling.readAllRecords("Users.txt");
        if (users == null) {
            return -1;
        }
        for (Integer id : users.keySet()) {
            // u: 5 email, 6 password, 7 role, 8 deleted
            ArrayList<String> u = users.get(id);
            if (u.get(5).equalsIgnoreCase(email) && u.get(6).equals(password)
                    && u.get(7).equals("Patient") && u.get(8).equals("0")) {
                return id;
            }
        }
        return -1;
    }
}
