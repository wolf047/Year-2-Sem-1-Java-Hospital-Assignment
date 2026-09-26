
package Admin;

import HelperFunction.FileHandling;
import Users.Role;
import Users.User;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

// One admin: their details and everything they can do.
// The screens talk to this class through the AdminServices interface.
public class Admin extends User implements AdminServices {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    public Admin(int user_id, String first_name, String last_name, String phone, String email,
            String password, String gender, LocalDate dob, Role role){
        super(user_id, first_name, last_name, phone, email, password, gender, dob, role);
    }

    @Override
    public String getFullName() {
        return this.first_name + " " + this.last_name;
    }

    // "MedicalManager" -> "Medical Manager"
    private String readableRole(String role) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < role.length(); i++) {
            char c = role.charAt(i);
            if (i > 0 && Character.isUpperCase(c)) {
                result.append(' ');
            }
            result.append(c);
        }
        return result.toString();
    }

    private String getUserName(String userId) {
        TreeMap<Integer, ArrayList<String>> users = FileHandling.readAllRecords("Users.txt");
        int id = Integer.parseInt(userId);
        if (users == null || !users.containsKey(id)) {
            return "Unknown";
        }
        return users.get(id).get(0) + " " + users.get(id).get(1);
    }

    private String getDepartmentName(String departmentId) {
        TreeMap<Integer, ArrayList<String>> departments = FileHandling.readAllRecords("Departments.txt");
        int id = Integer.parseInt(departmentId);
        if (departments == null || !departments.containsKey(id)) {
            return "Unknown";
        }
        return departments.get(id).get(0);
    }

    // requestRow value list: consultation_id(0), service_id(1), ...
    private String getRequestPatientName(ArrayList<String> requestRow) {
        try {
            int consultId = Integer.parseInt(requestRow.get(0));
            TreeMap<Integer, ArrayList<String>> consultations = FileHandling.readAllRecords("Consultations.txt");
            ArrayList<String> consult = consultations == null ? null : consultations.get(consultId);
            if (consult == null) {
                return "Unknown";
            }
            int caseId = Integer.parseInt(consult.get(0));
            TreeMap<Integer, ArrayList<String>> cases = FileHandling.readAllRecords("Cases.txt");
            ArrayList<String> c = cases == null ? null : cases.get(caseId);
            if (c == null) {
                return "Unknown";
            }
            return getUserName(c.get(0));
        } catch (Exception e) {
            return "Unknown";
        }
    }

    // =====================================================================
    // MANAGE USERS PAGE
    // =====================================================================
    @Override
    public ArrayList<Object[]> getUsers(String search, String roleFilter) {
        ArrayList<Object[]> rows = new ArrayList<>();
        TreeMap<Integer, ArrayList<String>> users = FileHandling.readAllRecords("Users.txt");
        if (users == null) {
            return rows;
        }
        String needle = search == null ? "" : search.trim().toLowerCase();
        for (Integer id : users.keySet()) {
            // u: 0 first, 1 last, 2 dob, 3 gender, 4 phone, 5 email, 6 password, 7 role, 8 deleted
            ArrayList<String> u = users.get(id);
            if (roleFilter != null && !roleFilter.equals("All") && !readableRole(u.get(7)).equals(roleFilter)) {
                continue;
            }
            String fullName = u.get(0) + " " + u.get(1);
            if (!needle.isEmpty() && !String.valueOf(id).equals(needle) && !fullName.toLowerCase().contains(needle)) {
                continue;
            }
            String status = u.get(8).equals("0") ? "Active" : "Deactivated";
            rows.add(new Object[]{id, fullName, readableRole(u.get(7)), u.get(4), u.get(5), status});
        }
        return rows;
    }

    @Override
    public String[] getUserDetail(int userId) {
        TreeMap<Integer, ArrayList<String>> users = FileHandling.readAllRecords("Users.txt");
        if (users == null || !users.containsKey(userId)) {
            return null;
        }
        ArrayList<String> u = users.get(userId);
        return new String[]{u.get(0), u.get(1), u.get(2), u.get(3), u.get(4), u.get(5), u.get(6), u.get(7)};
    }

    @Override
    public String[] getDoctorDetail(int userId) {
        TreeMap<Integer, ArrayList<String>> doctors = FileHandling.readAllRecords("Doctors.txt");
        if (doctors == null || !doctors.containsKey(userId)) {
            return null;
        }
        ArrayList<String> d = doctors.get(userId);
        return new String[]{d.get(0), d.get(1), d.get(2), d.get(3)};
    }

    @Override
    public String[] getPatientDetail(int userId) {
        TreeMap<Integer, ArrayList<String>> patients = FileHandling.readAllRecords("Patients.txt");
        if (patients == null || !patients.containsKey(userId)) {
            return null;
        }
        ArrayList<String> p = patients.get(userId);
        return new String[]{p.get(0), p.get(1)};
    }

    @Override
    public ArrayList<Object[]> getDepartments() {
        ArrayList<Object[]> rows = new ArrayList<>();
        TreeMap<Integer, ArrayList<String>> departments = FileHandling.readActiveRecords("Departments.txt");
        if (departments == null) {
            return rows;
        }
        for (Integer id : departments.keySet()) {
            rows.add(new Object[]{id, departments.get(id).get(0)});
        }
        return rows;
    }

    @Override
    public String createUser(String firstName, String lastName, String dob, String gender, String phone,
            String email, String password, String role, String deptId, String specialization,
            String practiceStartYear, String offDay, String bloodType, String allergies) {
        firstName = firstName == null ? "" : firstName.trim();
        lastName = lastName == null ? "" : lastName.trim();
        phone = phone == null ? "" : phone.trim();
        email = email == null ? "" : email.trim();
        password = password == null ? "" : password.trim();

        if (firstName.isEmpty() || lastName.isEmpty() || phone.isEmpty() || email.isEmpty() || password.isEmpty()) {
            return "Please fill in all required fields.";
        }
        if (gender == null || (!gender.equals("male") && !gender.equals("female"))) {
            return "Please select a gender.";
        }
        LocalDate dobParsed;
        try {
            dobParsed = LocalDate.parse(dob.trim(), DATE_FORMAT);
        } catch (Exception e) {
            return "Please enter a valid date of birth (dd-MM-yyyy).";
        }
        if (dobParsed.isAfter(LocalDate.now())) {
            return "Date of birth cannot be in the future.";
        }
        if (role == null || (!role.equals("Admin") && !role.equals("MedicalManager")
                && !role.equals("Doctor") && !role.equals("Patient"))) {
            return "Please select a role.";
        }
        TreeMap<Integer, ArrayList<String>> users = FileHandling.readAllRecords("Users.txt");
        if (users != null) {
            for (ArrayList<String> u : users.values()) {
                if (u.get(5).equalsIgnoreCase(email)) {
                    return "That email address is already in use.";
                }
            }
        }

        int deptIdInt = -1;
        if (role.equals("Doctor")) {
            specialization = specialization == null ? "" : specialization.trim();
            if (specialization.isEmpty()) {
                return "Please enter a specialization.";
            }
            try {
                deptIdInt = Integer.parseInt(deptId);
            } catch (Exception e) {
                return "Please select a department.";
            }
            TreeMap<Integer, ArrayList<String>> departments = FileHandling.readActiveRecords("Departments.txt");
            if (departments == null || !departments.containsKey(deptIdInt)) {
                return "Please select a valid department.";
            }
            try {
                int year = Integer.parseInt(practiceStartYear.trim());
                if (year < 1950 || year > LocalDate.now().getYear()) {
                    return "Please enter a valid practice start year.";
                }
            } catch (Exception e) {
                return "Please enter a valid practice start year.";
            }
            if (offDay == null || offDay.isBlank()) {
                return "Please select an off day.";
            }
        } else if (role.equals("Patient")) {
            bloodType = bloodType == null ? "" : bloodType.trim();
            allergies = allergies == null ? "" : allergies.trim();
            if (bloodType.isEmpty()) {
                return "Please select a blood type.";
            }
            if (allergies.isEmpty()) {
                return "Please enter allergy information (or \"None known\").";
            }
        }

        int newId = FileHandling.getNextID("Users.txt");
        ArrayList<String> userRecord = new ArrayList<>();
        userRecord.add(String.valueOf(newId));
        userRecord.add(firstName);
        userRecord.add(lastName);
        userRecord.add(dob.trim());
        userRecord.add(gender);
        userRecord.add(phone);
        userRecord.add(email);
        userRecord.add(password);
        userRecord.add(role);
        userRecord.add("0");
        FileHandling.addRecord("Users.txt", userRecord);

        if (role.equals("Doctor")) {
            ArrayList<String> doctorRecord = new ArrayList<>();
            doctorRecord.add(String.valueOf(newId));
            doctorRecord.add(String.valueOf(deptIdInt));
            doctorRecord.add(specialization);
            doctorRecord.add(practiceStartYear.trim());
            doctorRecord.add(offDay);
            FileHandling.addRecord("Doctors.txt", doctorRecord);
        } else if (role.equals("Patient")) {
            ArrayList<String> patientRecord = new ArrayList<>();
            patientRecord.add(String.valueOf(newId));
            patientRecord.add(bloodType);
            patientRecord.add(allergies);
            FileHandling.addRecord("Patients.txt", patientRecord);
        }
        return null;
    }

    @Override
    public String updateUser(int userId, String firstName, String lastName, String dob, String gender,
            String phone, String email, String password, String deptId, String specialization,
            String practiceStartYear, String offDay, String bloodType, String allergies) {
        firstName = firstName == null ? "" : firstName.trim();
        lastName = lastName == null ? "" : lastName.trim();
        phone = phone == null ? "" : phone.trim();
        email = email == null ? "" : email.trim();
        password = password == null ? "" : password.trim();

        if (firstName.isEmpty() || lastName.isEmpty() || phone.isEmpty() || email.isEmpty() || password.isEmpty()) {
            return "Please fill in all required fields.";
        }
        if (gender == null || (!gender.equals("male") && !gender.equals("female"))) {
            return "Please select a gender.";
        }
        LocalDate dobParsed;
        try {
            dobParsed = LocalDate.parse(dob.trim(), DATE_FORMAT);
        } catch (Exception e) {
            return "Please enter a valid date of birth (dd-MM-yyyy).";
        }
        if (dobParsed.isAfter(LocalDate.now())) {
            return "Date of birth cannot be in the future.";
        }

        TreeMap<Integer, ArrayList<String>> users = FileHandling.readAllRecords("Users.txt");
        if (users == null || !users.containsKey(userId)) {
            return "That user could not be found.";
        }
        for (Map.Entry<Integer, ArrayList<String>> entry : users.entrySet()) {
            if (entry.getKey() == userId) {
                continue;
            }
            if (entry.getValue().get(5).equalsIgnoreCase(email)) {
                return "That email address is already in use.";
            }
        }
        ArrayList<String> u = users.get(userId);
        String role = u.get(7);

        int deptIdInt = -1;
        if (role.equals("Doctor")) {
            specialization = specialization == null ? "" : specialization.trim();
            if (specialization.isEmpty()) {
                return "Please enter a specialization.";
            }
            try {
                deptIdInt = Integer.parseInt(deptId);
            } catch (Exception e) {
                return "Please select a department.";
            }
            TreeMap<Integer, ArrayList<String>> departments = FileHandling.readActiveRecords("Departments.txt");
            if (departments == null || !departments.containsKey(deptIdInt)) {
                return "Please select a valid department.";
            }
            try {
                int year = Integer.parseInt(practiceStartYear.trim());
                if (year < 1950 || year > LocalDate.now().getYear()) {
                    return "Please enter a valid practice start year.";
                }
            } catch (Exception e) {
                return "Please enter a valid practice start year.";
            }
            if (offDay == null || offDay.isBlank()) {
                return "Please select an off day.";
            }
        } else if (role.equals("Patient")) {
            bloodType = bloodType == null ? "" : bloodType.trim();
            allergies = allergies == null ? "" : allergies.trim();
            if (bloodType.isEmpty()) {
                return "Please select a blood type.";
            }
            if (allergies.isEmpty()) {
                return "Please enter allergy information (or \"None known\").";
            }
        }

        ArrayList<String> record = new ArrayList<>();
        record.add(String.valueOf(userId));
        record.add(firstName);
        record.add(lastName);
        record.add(dob.trim());
        record.add(gender);
        record.add(phone);
        record.add(email);
        record.add(password);
        record.add(role);
        record.add(u.get(8));
        FileHandling.editRecord("Users.txt", record);

        if (role.equals("Doctor")) {
            ArrayList<String> doctorRecord = new ArrayList<>();
            doctorRecord.add(String.valueOf(userId));
            doctorRecord.add(String.valueOf(deptIdInt));
            doctorRecord.add(specialization);
            doctorRecord.add(practiceStartYear.trim());
            doctorRecord.add(offDay);
            FileHandling.editRecord("Doctors.txt", doctorRecord);
        } else if (role.equals("Patient")) {
            ArrayList<String> patientRecord = new ArrayList<>();
            patientRecord.add(String.valueOf(userId));
            patientRecord.add(bloodType);
            patientRecord.add(allergies);
            FileHandling.editRecord("Patients.txt", patientRecord);
        }

        if (userId == this.user_id) {
            this.first_name = firstName;
            this.last_name = lastName;
        }
        return null;
    }

    @Override
    public String toggleUserStatus(int userId) {
        if (userId == this.user_id) {
            return "You cannot deactivate your own account.";
        }
        TreeMap<Integer, ArrayList<String>> users = FileHandling.readAllRecords("Users.txt");
        if (users == null || !users.containsKey(userId)) {
            return "That user could not be found.";
        }
        ArrayList<String> u = users.get(userId);
        String newStatus = u.get(8).equals("0") ? "1" : "0";

        ArrayList<String> record = new ArrayList<>();
        record.add(String.valueOf(userId));
        record.addAll(u.subList(0, 8));
        record.add(newStatus);
        FileHandling.editRecord("Users.txt", record);
        return null;
    }

    // =====================================================================
    // ASSIGN DOCTORS TO MEDICAL MANAGERS PAGE
    // =====================================================================
    @Override
    public ArrayList<Object[]> getDoctorAssignments() {
        ArrayList<Object[]> rows = new ArrayList<>();
        TreeMap<Integer, ArrayList<String>> users = FileHandling.readActiveRecords("Users.txt");
        TreeMap<Integer, ArrayList<String>> doctors = FileHandling.readAllRecords("Doctors.txt");
        TreeMap<Integer, ArrayList<String>> departments = FileHandling.readAllRecords("Departments.txt");
        if (users == null || doctors == null || departments == null) {
            return rows;
        }
        for (Integer id : users.keySet()) {
            ArrayList<String> u = users.get(id);
            if (!u.get(7).equals("Doctor")) {
                continue;
            }
            ArrayList<String> d = doctors.get(id);
            if (d == null) {
                continue;
            }
            String deptName = "Unassigned";
            String managerName = "-";
            ArrayList<String> dept = departments.get(Integer.parseInt(d.get(0)));
            if (dept != null) {
                deptName = dept.get(0);
                managerName = getUserName(dept.get(2));
            }
            rows.add(new Object[]{id, u.get(0) + " " + u.get(1), deptName, managerName});
        }
        return rows;
    }

    @Override
    public ArrayList<Object[]> getAssignableDepartments() {
        ArrayList<Object[]> rows = new ArrayList<>();
        TreeMap<Integer, ArrayList<String>> departments = FileHandling.readActiveRecords("Departments.txt");
        if (departments == null) {
            return rows;
        }
        for (Integer id : departments.keySet()) {
            ArrayList<String> dept = departments.get(id);
            String managerName = getUserName(dept.get(2));
            rows.add(new Object[]{id, dept.get(0) + " (Manager: " + managerName + ")"});
        }
        return rows;
    }

    @Override
    public String assignDoctorToDepartment(int doctorId, int departmentId) {
        TreeMap<Integer, ArrayList<String>> users = FileHandling.readActiveRecords("Users.txt");
        if (users == null || !users.containsKey(doctorId) || !users.get(doctorId).get(7).equals("Doctor")) {
            return "Please select a doctor.";
        }
        TreeMap<Integer, ArrayList<String>> departments = FileHandling.readActiveRecords("Departments.txt");
        if (departments == null || !departments.containsKey(departmentId)) {
            return "Please select a department.";
        }
        TreeMap<Integer, ArrayList<String>> doctors = FileHandling.readAllRecords("Doctors.txt");
        if (doctors == null || !doctors.containsKey(doctorId)) {
            return "That doctor's record could not be found.";
        }
        ArrayList<String> d = doctors.get(doctorId);
        ArrayList<String> record = new ArrayList<>();
        record.add(String.valueOf(doctorId));
        record.add(String.valueOf(departmentId));
        record.add(d.get(1));
        record.add(d.get(2));
        record.add(d.get(3));
        FileHandling.editRecord("Doctors.txt", record);
        return null;
    }

    // =====================================================================
    // HOSPITAL ASSETS PAGE
    // =====================================================================
    @Override
    public ArrayList<String> getAssetCategories() {
        ArrayList<String> categories = new ArrayList<>();
        categories.add("Consultation Rooms");
        categories.add("Labs");
        categories.add("Imaging Rooms");
        return categories;
    }

    // File and column layout for each asset category: {filename, hasDetailColumn}
    private String assetFile(String category) {
        switch (category) {
            case "Labs":
                return "Labs.txt";
            case "Imaging Rooms":
                return "ImagingRooms.txt";
            default:
                return "ConsultationRooms.txt";
        }
    }

    @Override
    public ArrayList<Object[]> getAssets(String category) {
        ArrayList<Object[]> rows = new ArrayList<>();
        TreeMap<Integer, ArrayList<String>> assets = FileHandling.readActiveRecords(assetFile(category));
        if (assets == null) {
            return rows;
        }
        boolean hasDetail = !category.equals("Consultation Rooms");
        for (Integer id : assets.keySet()) {
            ArrayList<String> row = assets.get(id);
            if (hasDetail) {
                // 0 type, 1 status, 2 deleted
                rows.add(new Object[]{id, row.get(0), row.get(1)});
            } else {
                // 0 status, 1 deleted
                rows.add(new Object[]{id, "-", row.get(0)});
            }
        }
        return rows;
    }

    @Override
    public ArrayList<String> getAssetTypes(String category) {
        ArrayList<String> types = new ArrayList<>();
        switch (category) {
            case "Labs":
                types.add("hematology");
                types.add("chemistry");
                types.add("specimen test");
                break;
            case "Imaging Rooms":
                types.add("x-ray");
                types.add("ultrasound");
                types.add("CT scan");
                break;
            default:
                break;
        }
        return types;
    }

    @Override
    public String addAsset(String category, String type) {
        String filename = assetFile(category);
        boolean hasDetail = !category.equals("Consultation Rooms");
        if (hasDetail && (type == null || type.isBlank())) {
            return "Please select a type.";
        }
        int newId = FileHandling.getNextID(filename);
        ArrayList<String> record = new ArrayList<>();
        record.add(String.valueOf(newId));
        if (hasDetail) {
            record.add(type);
        }
        record.add("ok");
        record.add("0");
        FileHandling.addRecord(filename, record);
        return null;
    }

    @Override
    public String toggleAssetStatus(String category, int assetId) {
        String filename = assetFile(category);
        TreeMap<Integer, ArrayList<String>> assets = FileHandling.readAllRecords(filename);
        if (assets == null || !assets.containsKey(assetId)) {
            return "That asset could not be found.";
        }
        boolean hasDetail = !category.equals("Consultation Rooms");
        ArrayList<String> row = assets.get(assetId);
        int statusIndex = hasDetail ? 1 : 0;
        String newStatus = row.get(statusIndex).equals("ok") ? "maintenance" : "ok";
        row.set(statusIndex, newStatus);

        ArrayList<String> record = new ArrayList<>();
        record.add(String.valueOf(assetId));
        record.addAll(row);
        FileHandling.editRecord(filename, record);
        return null;
    }

    // =====================================================================
    // WARDS & BEDS PAGE
    // =====================================================================
    @Override
    public ArrayList<Object[]> getWards() {
        ArrayList<Object[]> rows = new ArrayList<>();
        TreeMap<Integer, ArrayList<String>> wards = FileHandling.readActiveRecords("InpatientWards.txt");
        TreeMap<Integer, ArrayList<String>> beds = FileHandling.readActiveRecords("InpatientBeds.txt");
        if (wards == null) {
            return rows;
        }
        for (Integer id : wards.keySet()) {
            ArrayList<String> w = wards.get(id);
            int bedCount = 0;
            if (beds != null) {
                for (ArrayList<String> b : beds.values()) {
                    if (b.get(0).equals(String.valueOf(id))) {
                        bedCount++;
                    }
                }
            }
            rows.add(new Object[]{id, getDepartmentName(w.get(0)), w.get(1), w.get(2), bedCount});
        }
        return rows;
    }

    @Override
    public String[] getWardDetail(int wardId) {
        TreeMap<Integer, ArrayList<String>> wards = FileHandling.readAllRecords("InpatientWards.txt");
        if (wards == null || !wards.containsKey(wardId)) {
            return null;
        }
        ArrayList<String> w = wards.get(wardId);
        return new String[]{w.get(0), w.get(1), w.get(2)};
    }

    @Override
    public String addWard(int departmentId, String gender, int capacity) {
        if (gender == null || (!gender.equals("male") && !gender.equals("female"))) {
            return "Please select a gender.";
        }
        if (capacity <= 0) {
            return "Capacity must be greater than zero.";
        }
        TreeMap<Integer, ArrayList<String>> departments = FileHandling.readActiveRecords("Departments.txt");
        if (departments == null || !departments.containsKey(departmentId)) {
            return "Please select a valid department.";
        }
        ArrayList<String> record = new ArrayList<>();
        record.add(String.valueOf(FileHandling.getNextID("InpatientWards.txt")));
        record.add(String.valueOf(departmentId));
        record.add(gender);
        record.add(String.valueOf(capacity));
        record.add("0");
        FileHandling.addRecord("InpatientWards.txt", record);
        return null;
    }

    @Override
    public String updateWard(int wardId, String gender, int capacity) {
        if (gender == null || (!gender.equals("male") && !gender.equals("female"))) {
            return "Please select a gender.";
        }
        if (capacity <= 0) {
            return "Capacity must be greater than zero.";
        }
        TreeMap<Integer, ArrayList<String>> wards = FileHandling.readAllRecords("InpatientWards.txt");
        if (wards == null || !wards.containsKey(wardId)) {
            return "That ward could not be found.";
        }
        ArrayList<String> w = wards.get(wardId);
        ArrayList<String> record = new ArrayList<>();
        record.add(String.valueOf(wardId));
        record.add(w.get(0));
        record.add(gender);
        record.add(String.valueOf(capacity));
        record.add(w.get(3));
        FileHandling.editRecord("InpatientWards.txt", record);
        return null;
    }

    @Override
    public ArrayList<Object[]> getBeds(int wardId) {
        ArrayList<Object[]> rows = new ArrayList<>();
        TreeMap<Integer, ArrayList<String>> beds = FileHandling.readActiveRecords("InpatientBeds.txt");
        if (beds == null) {
            return rows;
        }
        for (Integer id : beds.keySet()) {
            ArrayList<String> b = beds.get(id);
            if (Integer.parseInt(b.get(0)) == wardId) {
                rows.add(new Object[]{id, wardId});
            }
        }
        return rows;
    }

    @Override
    public String addBed(int wardId) {
        TreeMap<Integer, ArrayList<String>> wards = FileHandling.readActiveRecords("InpatientWards.txt");
        if (wards == null || !wards.containsKey(wardId)) {
            return "Please select a valid ward.";
        }
        ArrayList<String> record = new ArrayList<>();
        record.add(String.valueOf(FileHandling.getNextID("InpatientBeds.txt")));
        record.add(String.valueOf(wardId));
        record.add("0");
        FileHandling.addRecord("InpatientBeds.txt", record);
        return null;
    }

    @Override
    public String removeBed(int bedId) {
        TreeMap<Integer, ArrayList<String>> beds = FileHandling.readAllRecords("InpatientBeds.txt");
        if (beds == null || !beds.containsKey(bedId)) {
            return "That bed could not be found.";
        }
        TreeMap<Integer, ArrayList<String>> admissions = FileHandling.readActiveRecords("Admissions.txt");
        if (admissions != null) {
            for (ArrayList<String> a : admissions.values()) {
                if (a.get(1).equals(String.valueOf(bedId)) && a.get(3).isBlank()) {
                    return "This bed currently has an active admission and cannot be removed.";
                }
            }
        }
        FileHandling.removeRecord("InpatientBeds.txt", bedId);
        return null;
    }

    // =====================================================================
    // ADMISSIONS PAGE
    // =====================================================================
    @Override
    public ArrayList<Object[]> getCasesForAdmission() {
        ArrayList<Object[]> rows = new ArrayList<>();
        TreeMap<Integer, ArrayList<String>> cases = FileHandling.readActiveRecords("Cases.txt");
        TreeMap<Integer, ArrayList<String>> admissions = FileHandling.readActiveRecords("Admissions.txt");
        if (cases == null) {
            return rows;
        }
        for (Integer id : cases.keySet()) {
            ArrayList<String> c = cases.get(id);
            boolean alreadyAdmitted = false;
            if (admissions != null) {
                for (ArrayList<String> a : admissions.values()) {
                    if (a.get(0).equals(String.valueOf(id)) && a.get(3).isBlank()) {
                        alreadyAdmitted = true;
                        break;
                    }
                }
            }
            if (alreadyAdmitted) {
                continue;
            }
            rows.add(new Object[]{id, "Case #" + id + " - " + getUserName(c.get(0))});
        }
        return rows;
    }

    @Override
    public ArrayList<Object[]> getAvailableBeds(int caseId) {
        ArrayList<Object[]> rows = new ArrayList<>();
        TreeMap<Integer, ArrayList<String>> cases = FileHandling.readAllRecords("Cases.txt");
        if (cases == null || !cases.containsKey(caseId)) {
            return rows;
        }
        int patientId = Integer.parseInt(cases.get(caseId).get(0));
        TreeMap<Integer, ArrayList<String>> allUsers = FileHandling.readAllRecords("Users.txt");
        if (allUsers == null || !allUsers.containsKey(patientId)) {
            return rows;
        }
        String patientGender = allUsers.get(patientId).get(3);

        TreeMap<Integer, ArrayList<String>> beds = FileHandling.readActiveRecords("InpatientBeds.txt");
        TreeMap<Integer, ArrayList<String>> wards = FileHandling.readActiveRecords("InpatientWards.txt");
        TreeMap<Integer, ArrayList<String>> admissions = FileHandling.readActiveRecords("Admissions.txt");
        if (beds == null || wards == null) {
            return rows;
        }

        for (Integer bedId : beds.keySet()) {
            ArrayList<String> b = beds.get(bedId);
            int wardId = Integer.parseInt(b.get(0));
            ArrayList<String> w = wards.get(wardId);
            if (w == null || !w.get(1).equals(patientGender)) {
                continue;
            }

            boolean occupied = false;
            if (admissions != null) {
                for (ArrayList<String> a : admissions.values()) {
                    if (a.get(1).equals(String.valueOf(bedId)) && a.get(3).isBlank()) {
                        occupied = true;
                        break;
                    }
                }
            }
            if (occupied) {
                continue;
            }
            rows.add(new Object[]{bedId, "Bed #" + bedId + " - " + getDepartmentName(w.get(0)) + " Ward (" + w.get(1) + ")"});
        }
        return rows;
    }

    @Override
    public ArrayList<Object[]> getAdmissions() {
        ArrayList<Object[]> rows = new ArrayList<>();
        TreeMap<Integer, ArrayList<String>> admissions = FileHandling.readActiveRecords("Admissions.txt");
        TreeMap<Integer, ArrayList<String>> cases = FileHandling.readAllRecords("Cases.txt");
        TreeMap<Integer, ArrayList<String>> beds = FileHandling.readAllRecords("InpatientBeds.txt");
        TreeMap<Integer, ArrayList<String>> wards = FileHandling.readAllRecords("InpatientWards.txt");
        if (admissions == null) {
            return rows;
        }
        for (Integer id : admissions.keySet()) {
            ArrayList<String> a = admissions.get(id);
            int caseId = Integer.parseInt(a.get(0));
            int bedId = Integer.parseInt(a.get(1));
            String patientName = "Unknown";
            if (cases != null && cases.containsKey(caseId)) {
                patientName = getUserName(cases.get(caseId).get(0));
            }
            String bedLabel = "Bed #" + bedId;
            if (beds != null && beds.containsKey(bedId) && wards != null) {
                ArrayList<String> b = beds.get(bedId);
                int wardId = Integer.parseInt(b.get(0));
                ArrayList<String> w = wards.get(wardId);
                if (w != null) {
                    bedLabel += " (" + getDepartmentName(w.get(0)) + " Ward)";
                }
            }
            String discharge = a.get(3).isBlank() ? "-" : a.get(3);
            String status = a.get(3).isBlank() ? "Active" : "Discharged";
            rows.add(new Object[]{id, patientName, bedLabel, a.get(2), discharge, a.get(4), status});
        }
        return rows;
    }

    @Override
    public String createAdmission(int caseId, int bedId, String admissionDate, String remarks) {
        LocalDate parsedDate;
        try {
            parsedDate = LocalDate.parse(admissionDate.trim(), DATE_FORMAT);
        } catch (Exception e) {
            return "Please enter a valid admission date (dd-MM-yyyy).";
        }
        remarks = remarks == null ? "" : remarks.trim();
        if (remarks.isEmpty()) {
            remarks = "-";
        }
        TreeMap<Integer, ArrayList<String>> cases = FileHandling.readActiveRecords("Cases.txt");
        if (cases == null || !cases.containsKey(caseId)) {
            return "Please select a valid case.";
        }
        TreeMap<Integer, ArrayList<String>> beds = FileHandling.readActiveRecords("InpatientBeds.txt");
        if (beds == null || !beds.containsKey(bedId)) {
            return "Please select a valid bed.";
        }
        TreeMap<Integer, ArrayList<String>> admissions = FileHandling.readActiveRecords("Admissions.txt");
        if (admissions != null) {
            for (ArrayList<String> a : admissions.values()) {
                if (a.get(1).equals(String.valueOf(bedId)) && a.get(3).isBlank()) {
                    return "That bed is already occupied.";
                }
                if (a.get(0).equals(String.valueOf(caseId)) && a.get(3).isBlank()) {
                    return "This case already has an active admission.";
                }
            }
        }
        ArrayList<String> record = new ArrayList<>();
        record.add(String.valueOf(FileHandling.getNextID("Admissions.txt")));
        record.add(String.valueOf(caseId));
        record.add(String.valueOf(bedId));
        record.add(admissionDate.trim());
        record.add("");
        record.add(remarks);
        record.add("0");
        FileHandling.addRecord("Admissions.txt", record);
        return null;
    }

    @Override
    public String dischargePatient(int admissionId, String dischargeDate) {
        TreeMap<Integer, ArrayList<String>> admissions = FileHandling.readAllRecords("Admissions.txt");
        if (admissions == null || !admissions.containsKey(admissionId)) {
            return "That admission could not be found.";
        }
        ArrayList<String> a = admissions.get(admissionId);
        if (!a.get(3).isBlank()) {
            return "This admission has already been discharged.";
        }
        LocalDate admissionDate;
        LocalDate parsedDischarge;
        try {
            admissionDate = LocalDate.parse(a.get(2), DATE_FORMAT);
            parsedDischarge = LocalDate.parse(dischargeDate.trim(), DATE_FORMAT);
        } catch (Exception e) {
            return "Please enter a valid discharge date (dd-MM-yyyy).";
        }
        if (parsedDischarge.isBefore(admissionDate)) {
            return "Discharge date cannot be before the admission date.";
        }
        ArrayList<String> record = new ArrayList<>();
        record.add(String.valueOf(admissionId));
        record.add(a.get(0));
        record.add(a.get(1));
        record.add(a.get(2));
        record.add(dischargeDate.trim());
        record.add(a.get(4));
        record.add(a.get(5));
        FileHandling.editRecord("Admissions.txt", record);
        return null;
    }

    // =====================================================================
    // DIAGNOSTICS: LAB RESULTS & IMAGING SCHEDULING PAGE
    // =====================================================================
    @Override
    public ArrayList<Object[]> getPendingLabRequests() {
        ArrayList<Object[]> rows = new ArrayList<>();
        TreeMap<Integer, ArrayList<String>> requests = FileHandling.readActiveRecords("DiagnosticServiceRequests.txt");
        TreeMap<Integer, ArrayList<String>> services = FileHandling.readAllRecords("DiagnosticServiceCatalogue.txt");
        if (requests == null || services == null) {
            return rows;
        }
        for (Integer id : requests.keySet()) {
            ArrayList<String> r = requests.get(id);
            if (!r.get(4).isBlank()) {
                continue;
            }
            int serviceId = Integer.parseInt(r.get(1));
            ArrayList<String> service = services.get(serviceId);
            if (service == null || !service.get(1).equals("laboratory")) {
                continue;
            }
            rows.add(new Object[]{id, service.get(0), getRequestPatientName(r), r.get(2), r.get(3)});
        }
        return rows;
    }

    @Override
    public String submitLabResult(int requestId, String results) {
        results = results == null ? "" : results.trim();
        if (results.isEmpty()) {
            return "Please enter the result.";
        }
        TreeMap<Integer, ArrayList<String>> requests = FileHandling.readAllRecords("DiagnosticServiceRequests.txt");
        if (requests == null || !requests.containsKey(requestId)) {
            return "That request could not be found.";
        }
        ArrayList<String> r = requests.get(requestId);
        if (!r.get(4).isBlank()) {
            return "This request already has a result.";
        }
        ArrayList<String> record = new ArrayList<>();
        record.add(String.valueOf(requestId));
        record.add(r.get(0));
        record.add(r.get(1));
        record.add(r.get(2));
        record.add(r.get(3));
        record.add(LocalDate.now().format(DATE_FORMAT));
        record.add(results);
        record.add(r.get(6));
        FileHandling.editRecord("DiagnosticServiceRequests.txt", record);
        return null;
    }

    @Override
    public ArrayList<Object[]> getPendingImagingRequests() {
        ArrayList<Object[]> rows = new ArrayList<>();
        TreeMap<Integer, ArrayList<String>> requests = FileHandling.readActiveRecords("DiagnosticServiceRequests.txt");
        TreeMap<Integer, ArrayList<String>> services = FileHandling.readAllRecords("DiagnosticServiceCatalogue.txt");
        TreeMap<Integer, ArrayList<String>> appointments = FileHandling.readActiveRecords("ImagingAppointments.txt");
        if (requests == null || services == null) {
            return rows;
        }
        for (Integer id : requests.keySet()) {
            ArrayList<String> r = requests.get(id);
            int serviceId = Integer.parseInt(r.get(1));
            ArrayList<String> service = services.get(serviceId);
            if (service == null || !service.get(1).equals("imaging")) {
                continue;
            }
            boolean scheduled = false;
            if (appointments != null) {
                for (ArrayList<String> appt : appointments.values()) {
                    if (appt.get(0).equals(String.valueOf(id))) {
                        scheduled = true;
                        break;
                    }
                }
            }
            if (scheduled) {
                continue;
            }
            rows.add(new Object[]{id, service.get(0), getRequestPatientName(r), r.get(2)});
        }
        return rows;
    }

    @Override
    public ArrayList<Object[]> getImagingRoomsForRequest(int requestId) {
        ArrayList<Object[]> rows = new ArrayList<>();
        TreeMap<Integer, ArrayList<String>> requests = FileHandling.readAllRecords("DiagnosticServiceRequests.txt");
        TreeMap<Integer, ArrayList<String>> services = FileHandling.readAllRecords("DiagnosticServiceCatalogue.txt");
        if (requests == null || !requests.containsKey(requestId) || services == null) {
            return rows;
        }
        int serviceId = Integer.parseInt(requests.get(requestId).get(1));
        ArrayList<String> service = services.get(serviceId);
        if (service == null) {
            return rows;
        }
        String requiredType = service.get(2);

        TreeMap<Integer, ArrayList<String>> rooms = FileHandling.readActiveRecords("ImagingRooms.txt");
        if (rooms == null) {
            return rows;
        }
        for (Integer id : rooms.keySet()) {
            ArrayList<String> room = rooms.get(id);
            if (!room.get(0).equals(requiredType) || !room.get(1).equals("ok")) {
                continue;
            }
            rows.add(new Object[]{id, "Room #" + id + " (" + room.get(0) + ")"});
        }
        return rows;
    }

    @Override
    public String scheduleImaging(int requestId, int imagingRoomId, String date, String startTime, String endTime) {
        TreeMap<Integer, ArrayList<String>> requests = FileHandling.readActiveRecords("DiagnosticServiceRequests.txt");
        if (requests == null || !requests.containsKey(requestId)) {
            return "Please select a valid request.";
        }
        TreeMap<Integer, ArrayList<String>> rooms = FileHandling.readActiveRecords("ImagingRooms.txt");
        if (rooms == null || !rooms.containsKey(imagingRoomId)) {
            return "Please select a valid imaging room.";
        }
        LocalDate parsedDate;
        LocalTime start;
        LocalTime end;
        try {
            parsedDate = LocalDate.parse(date.trim(), DATE_FORMAT);
            start = LocalTime.parse(startTime.trim(), TIME_FORMAT);
            end = LocalTime.parse(endTime.trim(), TIME_FORMAT);
        } catch (Exception e) {
            return "Please enter a valid date (dd-MM-yyyy) and times (HH:mm).";
        }
        if (!end.isAfter(start)) {
            return "End time must be after start time.";
        }
        if (start.getMinute() % 30 != 0 || end.getMinute() % 30 != 0) {
            return "Appointment times must be in 30-minute increments.";
        }

        TreeMap<Integer, ArrayList<String>> appointments = FileHandling.readActiveRecords("ImagingAppointments.txt");
        if (appointments != null) {
            for (ArrayList<String> appt : appointments.values()) {
                if (appt.get(0).equals(String.valueOf(requestId))) {
                    return "This request already has an imaging appointment.";
                }
                if (!appt.get(1).equals(String.valueOf(imagingRoomId))) {
                    continue;
                }
                if (!appt.get(2).equals(date.trim())) {
                    continue;
                }
                LocalTime existingStart = LocalTime.parse(appt.get(3), TIME_FORMAT);
                LocalTime existingEnd = LocalTime.parse(appt.get(4), TIME_FORMAT);
                if (start.isBefore(existingEnd) && existingStart.isBefore(end)) {
                    return "That imaging room is already booked for an overlapping time.";
                }
            }
        }

        ArrayList<String> record = new ArrayList<>();
        record.add(String.valueOf(FileHandling.getNextID("ImagingAppointments.txt")));
        record.add(String.valueOf(requestId));
        record.add(String.valueOf(imagingRoomId));
        record.add(date.trim());
        record.add(startTime.trim());
        record.add(endTime.trim());
        record.add("0");
        FileHandling.addRecord("ImagingAppointments.txt", record);
        return null;
    }

    // =====================================================================
    // CATALOGUES: DRUGS & DIAGNOSTIC SERVICES PAGE
    // =====================================================================
    @Override
    public ArrayList<Object[]> getDrugCatalogue() {
        ArrayList<Object[]> rows = new ArrayList<>();
        TreeMap<Integer, ArrayList<String>> drugs = FileHandling.readAllRecords("DrugCatalogue.txt");
        if (drugs == null) {
            return rows;
        }
        for (Integer id : drugs.keySet()) {
            ArrayList<String> d = drugs.get(id);
            String status = d.get(3).equals("0") ? "Active" : "Inactive";
            rows.add(new Object[]{id, d.get(0), d.get(1), d.get(2), status});
        }
        return rows;
    }

    @Override
    public ArrayList<String> getDrugForms() {
        ArrayList<String> forms = new ArrayList<>();
        forms.add("tablet/capsule");
        forms.add("syrup/solution");
        forms.add("powder");
        forms.add("injection");
        forms.add("drops");
        forms.add("cream/ointment/gel");
        return forms;
    }

    @Override
    public String addDrug(String name, String form, String price) {
        name = name == null ? "" : name.trim();
        if (name.isEmpty()) {
            return "Please enter a drug name.";
        }
        if (!getDrugForms().contains(form)) {
            return "Please select a valid form.";
        }
        float priceValue;
        try {
            priceValue = Float.parseFloat(price.trim());
        } catch (Exception e) {
            return "Please enter a valid price.";
        }
        if (priceValue < 0) {
            return "Price cannot be negative.";
        }
        ArrayList<String> record = new ArrayList<>();
        record.add(String.valueOf(FileHandling.getNextID("DrugCatalogue.txt")));
        record.add(name);
        record.add(form);
        record.add(String.format(Locale.US, "%.2f", priceValue));
        record.add("0");
        FileHandling.addRecord("DrugCatalogue.txt", record);
        return null;
    }

    @Override
    public String updateDrug(int drugId, String name, String form, String price) {
        name = name == null ? "" : name.trim();
        if (name.isEmpty()) {
            return "Please enter a drug name.";
        }
        if (!getDrugForms().contains(form)) {
            return "Please select a valid form.";
        }
        float priceValue;
        try {
            priceValue = Float.parseFloat(price.trim());
        } catch (Exception e) {
            return "Please enter a valid price.";
        }
        if (priceValue < 0) {
            return "Price cannot be negative.";
        }
        TreeMap<Integer, ArrayList<String>> drugs = FileHandling.readAllRecords("DrugCatalogue.txt");
        if (drugs == null || !drugs.containsKey(drugId)) {
            return "That drug could not be found.";
        }
        ArrayList<String> record = new ArrayList<>();
        record.add(String.valueOf(drugId));
        record.add(name);
        record.add(form);
        record.add(String.format(Locale.US, "%.2f", priceValue));
        record.add(drugs.get(drugId).get(3));
        FileHandling.editRecord("DrugCatalogue.txt", record);
        return null;
    }

    @Override
    public String toggleDrugStatus(int drugId) {
        TreeMap<Integer, ArrayList<String>> drugs = FileHandling.readAllRecords("DrugCatalogue.txt");
        if (drugs == null || !drugs.containsKey(drugId)) {
            return "That drug could not be found.";
        }
        ArrayList<String> d = drugs.get(drugId);
        String newStatus = d.get(3).equals("0") ? "1" : "0";
        ArrayList<String> record = new ArrayList<>();
        record.add(String.valueOf(drugId));
        record.add(d.get(0));
        record.add(d.get(1));
        record.add(d.get(2));
        record.add(newStatus);
        FileHandling.editRecord("DrugCatalogue.txt", record);
        return null;
    }

    @Override
    public ArrayList<Object[]> getServiceCatalogue() {
        ArrayList<Object[]> rows = new ArrayList<>();
        TreeMap<Integer, ArrayList<String>> services = FileHandling.readAllRecords("DiagnosticServiceCatalogue.txt");
        if (services == null) {
            return rows;
        }
        for (Integer id : services.keySet()) {
            ArrayList<String> s = services.get(id);
            String status = s.get(4).equals("0") ? "Active" : "Inactive";
            rows.add(new Object[]{id, s.get(0), s.get(1), s.get(2), s.get(3), status});
        }
        return rows;
    }

    @Override
    public ArrayList<String> getServiceTypes(String category) {
        ArrayList<String> types = new ArrayList<>();
        if ("laboratory".equals(category)) {
            types.add("hematology");
            types.add("chemistry");
            types.add("specimen test");
        } else if ("imaging".equals(category)) {
            types.add("x-ray");
            types.add("ultrasound");
            types.add("CT scan");
        }
        return types;
    }

    @Override
    public String addService(String name, String category, String type, String price) {
        name = name == null ? "" : name.trim();
        if (name.isEmpty()) {
            return "Please enter a service name.";
        }
        if (category == null || (!category.equals("laboratory") && !category.equals("imaging"))) {
            return "Please select a valid category.";
        }
        if (!getServiceTypes(category).contains(type)) {
            return "Please select a valid type for that category.";
        }
        float priceValue;
        try {
            priceValue = Float.parseFloat(price.trim());
        } catch (Exception e) {
            return "Please enter a valid price.";
        }
        if (priceValue < 0) {
            return "Price cannot be negative.";
        }
        ArrayList<String> record = new ArrayList<>();
        record.add(String.valueOf(FileHandling.getNextID("DiagnosticServiceCatalogue.txt")));
        record.add(name);
        record.add(category);
        record.add(type);
        record.add(String.format(Locale.US, "%.2f", priceValue));
        record.add("0");
        FileHandling.addRecord("DiagnosticServiceCatalogue.txt", record);
        return null;
    }

    @Override
    public String updateService(int serviceId, String name, String category, String type, String price) {
        name = name == null ? "" : name.trim();
        if (name.isEmpty()) {
            return "Please enter a service name.";
        }
        if (category == null || (!category.equals("laboratory") && !category.equals("imaging"))) {
            return "Please select a valid category.";
        }
        if (!getServiceTypes(category).contains(type)) {
            return "Please select a valid type for that category.";
        }
        float priceValue;
        try {
            priceValue = Float.parseFloat(price.trim());
        } catch (Exception e) {
            return "Please enter a valid price.";
        }
        if (priceValue < 0) {
            return "Price cannot be negative.";
        }
        TreeMap<Integer, ArrayList<String>> services = FileHandling.readAllRecords("DiagnosticServiceCatalogue.txt");
        if (services == null || !services.containsKey(serviceId)) {
            return "That service could not be found.";
        }
        ArrayList<String> record = new ArrayList<>();
        record.add(String.valueOf(serviceId));
        record.add(name);
        record.add(category);
        record.add(type);
        record.add(String.format(Locale.US, "%.2f", priceValue));
        record.add(services.get(serviceId).get(4));
        FileHandling.editRecord("DiagnosticServiceCatalogue.txt", record);
        return null;
    }

    @Override
    public String toggleServiceStatus(int serviceId) {
        TreeMap<Integer, ArrayList<String>> services = FileHandling.readAllRecords("DiagnosticServiceCatalogue.txt");
        if (services == null || !services.containsKey(serviceId)) {
            return "That service could not be found.";
        }
        ArrayList<String> s = services.get(serviceId);
        String newStatus = s.get(4).equals("0") ? "1" : "0";
        ArrayList<String> record = new ArrayList<>();
        record.add(String.valueOf(serviceId));
        record.add(s.get(0));
        record.add(s.get(1));
        record.add(s.get(2));
        record.add(s.get(3));
        record.add(newStatus);
        FileHandling.editRecord("DiagnosticServiceCatalogue.txt", record);
        return null;
    }

    // =====================================================================
    // RATES & INSURANCE PAGE
    // =====================================================================
    @Override
    public float getConsultationFee() {
        ArrayList<Float> fees = FileHandling.readConHosFees();
        return fees.isEmpty() ? 0 : fees.get(0);
    }

    @Override
    public float getHospitalisationFee() {
        ArrayList<Float> fees = FileHandling.readConHosFees();
        return fees.size() < 2 ? 0 : fees.get(1);
    }

    @Override
    public String updateFees(float consultationFee, float hospitalisationFee) {
        if (consultationFee < 0 || hospitalisationFee < 0) {
            return "Fees cannot be negative.";
        }
        ArrayList<Float> fees = new ArrayList<>();
        fees.add(consultationFee);
        fees.add(hospitalisationFee);
        FileHandling.updateConHosFees(fees);
        return null;
    }

    @Override
    public ArrayList<Object[]> getTiers() {
        ArrayList<Object[]> rows = new ArrayList<>();
        TreeMap<Integer, ArrayList<String>> tiers = FileHandling.readActiveRecords("TierMultipliers.txt");
        if (tiers == null) {
            return rows;
        }
        for (Integer id : tiers.keySet()) {
            ArrayList<String> t = tiers.get(id); // 0 tier_name, 1 minimum_years, 2 multiplier, 3 deleted
            rows.add(new Object[]{id, t.get(0), t.get(1), t.get(2)});
        }
        return rows;
    }

    @Override
    public String updateTierMultiplier(int tierId, float multiplier) {
        if (multiplier <= 0) {
            return "Multiplier must be greater than zero.";
        }
        TreeMap<Integer, ArrayList<String>> tiers = FileHandling.readAllRecords("TierMultipliers.txt");
        if (tiers == null || !tiers.containsKey(tierId)) {
            return "That tier could not be found.";
        }
        ArrayList<String> t = tiers.get(tierId);
        t.set(2, String.format(Locale.US, "%.2f", multiplier));

        ArrayList<String> record = new ArrayList<>();
        record.add(String.valueOf(tierId));
        record.addAll(t);
        FileHandling.editRecord("TierMultipliers.txt", record);
        return null;
    }

    @Override
    public ArrayList<Object[]> getInsuranceNetworks() {
        ArrayList<Object[]> rows = new ArrayList<>();
        TreeMap<Integer, ArrayList<String>> networks = FileHandling.readActiveRecords("AcceptedInsuranceNetworks.txt");
        if (networks == null) {
            return rows;
        }
        for (Integer id : networks.keySet()) {
            rows.add(new Object[]{id, networks.get(id).get(0)});
        }
        return rows;
    }

    @Override
    public String addInsuranceNetwork(String name) {
        name = name.trim();
        if (name.isEmpty()) {
            return "Please enter an insurance provider name.";
        }
        if (name.contains("`")) {
            return "Name cannot contain a backtick (`) character.";
        }
        ArrayList<String> record = new ArrayList<>();
        record.add(String.valueOf(FileHandling.getNextID("AcceptedInsuranceNetworks.txt")));
        record.add(name);
        record.add("0");
        FileHandling.addRecord("AcceptedInsuranceNetworks.txt", record);
        return null;
    }

    @Override
    public String removeInsuranceNetwork(int insuranceId) {
        TreeMap<Integer, ArrayList<String>> networks = FileHandling.readAllRecords("AcceptedInsuranceNetworks.txt");
        if (networks == null || !networks.containsKey(insuranceId)) {
            return "That insurance provider could not be found.";
        }
        FileHandling.removeRecord("AcceptedInsuranceNetworks.txt", insuranceId);
        return null;
    }

    // =====================================================================
    // INVOICES & PAYMENTS PAGE
    // =====================================================================
    // A doctor's consultation fee on a given date: the base consultation fee times the
    // multiplier of the highest tier their years of experience (as of that date) qualifies for.
    private float getDoctorFeeOnDate(String doctorId, LocalDate consultDate) {
        float baseFee = getConsultationFee();
        TreeMap<Integer, ArrayList<String>> doctors = FileHandling.readAllRecords("Doctors.txt");
        ArrayList<String> d = doctors == null ? null : doctors.get(Integer.parseInt(doctorId));
        if (d == null) {
            return baseFee;
        }
        int yearsExperience;
        try {
            yearsExperience = consultDate.getYear() - Integer.parseInt(d.get(2));
        } catch (Exception e) {
            return baseFee;
        }

        TreeMap<Integer, ArrayList<String>> tiers = FileHandling.readActiveRecords("TierMultipliers.txt");
        float bestMultiplier = 1;
        int bestMinYears = -1;
        if (tiers != null) {
            for (ArrayList<String> t : tiers.values()) { // 0 tier_name, 1 minimum_years, 2 multiplier
                try {
                    int minYears = Integer.parseInt(t.get(1));
                    if (yearsExperience >= minYears && minYears > bestMinYears) {
                        bestMinYears = minYears;
                        bestMultiplier = Float.parseFloat(t.get(2));
                    }
                } catch (Exception e) {
                    // skip a corrupted tier row
                }
            }
        }
        return baseFee * bestMultiplier;
    }

    @Override
    public ArrayList<Object[]> getClosedCasesForInvoicing() {
        ArrayList<Object[]> rows = new ArrayList<>();
        TreeMap<Integer, ArrayList<String>> cases = FileHandling.readActiveRecords("Cases.txt");
        TreeMap<Integer, ArrayList<String>> invoices = FileHandling.readActiveRecords("Invoices.txt");
        if (cases == null) {
            return rows;
        }
        for (Integer caseId : cases.keySet()) {
            ArrayList<String> c = cases.get(caseId); // 3 close_date
            if (c.get(3).isBlank()) {
                continue;
            }
            boolean alreadyInvoiced = false;
            if (invoices != null) {
                for (ArrayList<String> inv : invoices.values()) {
                    if (inv.get(0).equals(String.valueOf(caseId))) {
                        alreadyInvoiced = true;
                        break;
                    }
                }
            }
            if (alreadyInvoiced) {
                continue;
            }
            rows.add(new Object[]{caseId, "Case #" + caseId + " - " + getUserName(c.get(0)), c.get(3)});
        }
        return rows;
    }

    @Override
    public ArrayList<Object[]> previewInvoiceItems(int caseId) {
        ArrayList<Object[]> items = new ArrayList<>();
        TreeMap<Integer, ArrayList<String>> cases = FileHandling.readAllRecords("Cases.txt");
        if (cases == null || !cases.containsKey(caseId)) {
            return items;
        }

        TreeMap<Integer, ArrayList<String>> consultations = FileHandling.readAllRecords("Consultations.txt");
        TreeMap<Integer, ArrayList<String>> prescriptions = FileHandling.readAllRecords("Prescriptions.txt");
        TreeMap<Integer, ArrayList<String>> prescriptionItems = FileHandling.readAllRecords("PrescriptionItems.txt");
        TreeMap<Integer, ArrayList<String>> drugs = FileHandling.readAllRecords("DrugCatalogue.txt");
        TreeMap<Integer, ArrayList<String>> requests = FileHandling.readAllRecords("DiagnosticServiceRequests.txt");
        TreeMap<Integer, ArrayList<String>> services = FileHandling.readAllRecords("DiagnosticServiceCatalogue.txt");

        if (consultations != null) {
            for (Integer consultId : consultations.keySet()) {
                // c: 0 case_id, 1 doctor_id, 5 status, 7 date
                ArrayList<String> c = consultations.get(consultId);
                if (!c.get(0).equals(String.valueOf(caseId)) || !c.get(5).equals("completed")) {
                    continue;
                }
                LocalDate consultDate;
                try {
                    consultDate = LocalDate.parse(c.get(7), DATE_FORMAT);
                } catch (Exception e) {
                    continue;
                }
                float fee = getDoctorFeeOnDate(c.get(1), consultDate);
                items.add(new Object[]{"Consultation with " + getUserName(c.get(1)) + " on " + c.get(7), 1,
                    String.format(Locale.US, "%.2f", fee)});

                if (prescriptions != null && prescriptionItems != null) {
                    for (Map.Entry<Integer, ArrayList<String>> pEntry : prescriptions.entrySet()) {
                        if (!pEntry.getValue().get(0).equals(String.valueOf(consultId))) {
                            continue;
                        }
                        String prescriptionId = String.valueOf(pEntry.getKey());
                        for (ArrayList<String> item : prescriptionItems.values()) {
                            // item: 0 prescription_id, 1 drug_id
                            if (!item.get(0).equals(prescriptionId)) {
                                continue;
                            }
                            String drugName = "Unknown drug";
                            float drugPrice = 0;
                            if (drugs != null) {
                                ArrayList<String> drug = drugs.get(Integer.parseInt(item.get(1)));
                                if (drug != null) {
                                    drugName = drug.get(0);
                                    try {
                                        drugPrice = Float.parseFloat(drug.get(2));
                                    } catch (Exception e) {
                                        drugPrice = 0;
                                    }
                                }
                            }
                            items.add(new Object[]{drugName, 1, String.format(Locale.US, "%.2f", drugPrice)});
                        }
                    }
                }

                if (requests != null && services != null) {
                    for (ArrayList<String> r : requests.values()) {
                        // r: 0 consultation_id, 1 service_id, 4 result_date
                        if (!r.get(0).equals(String.valueOf(consultId)) || r.get(4).isBlank()) {
                            continue;
                        }
                        ArrayList<String> service = services.get(Integer.parseInt(r.get(1)));
                        if (service == null) {
                            continue;
                        }
                        float servicePrice;
                        try {
                            servicePrice = Float.parseFloat(service.get(3));
                        } catch (Exception e) {
                            servicePrice = 0;
                        }
                        items.add(new Object[]{service.get(0), 1, String.format(Locale.US, "%.2f", servicePrice)});
                    }
                }
            }
        }

        TreeMap<Integer, ArrayList<String>> admissions = FileHandling.readAllRecords("Admissions.txt");
        if (admissions != null) {
            for (ArrayList<String> a : admissions.values()) {
                // a: 0 case_id, 2 admission_date, 3 discharge_date
                if (!a.get(0).equals(String.valueOf(caseId)) || a.get(3).isBlank()) {
                    continue;
                }
                try {
                    LocalDate admissionDate = LocalDate.parse(a.get(2), DATE_FORMAT);
                    LocalDate dischargeDate = LocalDate.parse(a.get(3), DATE_FORMAT);
                    long days = dischargeDate.toEpochDay() - admissionDate.toEpochDay();
                    if (days < 1) {
                        days = 1;
                    }
                    float total = getHospitalisationFee() * days;
                    items.add(new Object[]{"Hospitalisation (" + days + (days == 1 ? " day)" : " days)"),
                        days, String.format(Locale.US, "%.2f", total)});
                } catch (Exception e) {
                    // skip a malformed admission record
                }
            }
        }
        return items;
    }

    @Override
    public String generateInvoice(int caseId) {
        TreeMap<Integer, ArrayList<String>> cases = FileHandling.readActiveRecords("Cases.txt");
        if (cases == null || !cases.containsKey(caseId)) {
            return "Please select a valid case.";
        }
        if (cases.get(caseId).get(3).isBlank()) {
            return "This case has not been closed yet.";
        }
        TreeMap<Integer, ArrayList<String>> invoices = FileHandling.readActiveRecords("Invoices.txt");
        if (invoices != null) {
            for (ArrayList<String> inv : invoices.values()) {
                if (inv.get(0).equals(String.valueOf(caseId))) {
                    return "This case already has an invoice.";
                }
            }
        }
        ArrayList<Object[]> items = previewInvoiceItems(caseId);
        if (items.isEmpty()) {
            return "This case has nothing billable yet (no completed consultations, medications, "
                    + "diagnostic services or hospitalisation).";
        }
        float total = 0;
        for (Object[] item : items) {
            total += Float.parseFloat((String) item[2]);
        }

        int invoiceId = FileHandling.getNextID("Invoices.txt");
        ArrayList<String> invoiceRecord = new ArrayList<>();
        invoiceRecord.add(String.valueOf(invoiceId));
        invoiceRecord.add(String.valueOf(caseId));
        invoiceRecord.add(LocalDate.now().format(DATE_FORMAT));
        invoiceRecord.add(String.format(Locale.US, "%.2f", total));
        invoiceRecord.add("0");
        FileHandling.addRecord("Invoices.txt", invoiceRecord);

        for (Object[] item : items) {
            ArrayList<String> itemRecord = new ArrayList<>();
            itemRecord.add(String.valueOf(FileHandling.getNextID("InvoiceItems.txt")));
            itemRecord.add(String.valueOf(invoiceId));
            itemRecord.add((String) item[0]);
            itemRecord.add(String.valueOf(item[1]));
            itemRecord.add((String) item[2]);
            itemRecord.add("0");
            FileHandling.addRecord("InvoiceItems.txt", itemRecord);
        }
        return null;
    }

    @Override
    public ArrayList<Object[]> getInvoices() {
        ArrayList<Object[]> rows = new ArrayList<>();
        TreeMap<Integer, ArrayList<String>> invoices = FileHandling.readActiveRecords("Invoices.txt");
        TreeMap<Integer, ArrayList<String>> cases = FileHandling.readAllRecords("Cases.txt");
        TreeMap<Integer, ArrayList<String>> receipts = FileHandling.readActiveRecords("Receipts.txt");
        if (invoices == null) {
            return rows;
        }
        for (Integer invoiceId : invoices.keySet()) {
            ArrayList<String> inv = invoices.get(invoiceId); // 0 case_id, 1 date_issued, 2 total_amount
            int caseId = Integer.parseInt(inv.get(0));
            String caseLabel = "Case #" + caseId;
            if (cases != null && cases.containsKey(caseId)) {
                caseLabel += " - " + getUserName(cases.get(caseId).get(0));
            }
            boolean paid = false;
            if (receipts != null) {
                for (ArrayList<String> r : receipts.values()) {
                    if (r.get(0).equals(String.valueOf(invoiceId))) {
                        paid = true;
                        break;
                    }
                }
            }
            rows.add(new Object[]{invoiceId, caseLabel, inv.get(1), inv.get(2), paid ? "Paid" : "Unpaid"});
        }
        return rows;
    }

    @Override
    public ArrayList<Object[]> getInvoiceItems(int invoiceId) {
        ArrayList<Object[]> rows = new ArrayList<>();
        TreeMap<Integer, ArrayList<String>> items = FileHandling.readActiveRecords("InvoiceItems.txt");
        if (items == null) {
            return rows;
        }
        for (ArrayList<String> item : items.values()) {
            // item: 0 invoice_id, 1 item_name, 2 quantity, 3 amount_charged
            if (!item.get(0).equals(String.valueOf(invoiceId))) {
                continue;
            }
            rows.add(new Object[]{item.get(1), item.get(2), item.get(3)});
        }
        return rows;
    }

    @Override
    public String recordPayment(int invoiceId, String paymentMethod, Integer insuranceId) {
        TreeMap<Integer, ArrayList<String>> invoices = FileHandling.readActiveRecords("Invoices.txt");
        if (invoices == null || !invoices.containsKey(invoiceId)) {
            return "Please select a valid invoice.";
        }
        TreeMap<Integer, ArrayList<String>> receipts = FileHandling.readActiveRecords("Receipts.txt");
        if (receipts != null) {
            for (ArrayList<String> r : receipts.values()) {
                if (r.get(0).equals(String.valueOf(invoiceId))) {
                    return "This invoice has already been paid.";
                }
            }
        }
        if (paymentMethod == null || (!paymentMethod.equals("cash") && !paymentMethod.equals("card")
                && !paymentMethod.equals("online_transfer") && !paymentMethod.equals("insurance"))) {
            return "Please select a payment method.";
        }
        if (paymentMethod.equals("insurance")) {
            if (insuranceId == null) {
                return "Please select an insurance provider.";
            }
            TreeMap<Integer, ArrayList<String>> networks = FileHandling.readActiveRecords("AcceptedInsuranceNetworks.txt");
            if (networks == null || !networks.containsKey(insuranceId)) {
                return "Please select a valid insurance provider.";
            }
        } else {
            insuranceId = null;
        }

        ArrayList<String> inv = invoices.get(invoiceId);
        ArrayList<String> record = new ArrayList<>();
        record.add(String.valueOf(FileHandling.getNextID("Receipts.txt")));
        record.add(String.valueOf(invoiceId));
        record.add(insuranceId == null ? "" : String.valueOf(insuranceId));
        record.add(paymentMethod);
        record.add(inv.get(2));
        record.add(LocalDate.now().format(DATE_FORMAT));
        record.add("0");
        FileHandling.addRecord("Receipts.txt", record);
        return null;
    }
}
