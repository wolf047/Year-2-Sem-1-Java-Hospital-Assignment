
package Admin;

import HelperFunction.FileHandling;
import Users.Role;
import Users.User;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.TreeMap;

// One admin: their details and everything they can do.
// The screens talk to this class through the AdminServices interface.
public class Admin extends User implements AdminServices {

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

    // =====================================================================
    // USERS PAGE
    // =====================================================================
    @Override
    public ArrayList<Object[]> getUsers() {
        ArrayList<Object[]> rows = new ArrayList<>();
        TreeMap<Integer, ArrayList<String>> users = FileHandling.readAllRecords("Users.txt");
        if (users == null) {
            return rows;
        }
        for (Integer id : users.keySet()) {
            // u: 0 first, 1 last, 2 dob, 3 gender, 4 phone, 5 email, 6 password, 7 role, 8 deleted
            ArrayList<String> u = users.get(id);
            String fullName = u.get(0) + " " + u.get(1);
            String status = u.get(8).equals("0") ? "Active" : "Deactivated";
            rows.add(new Object[]{id, fullName, readableRole(u.get(7)), u.get(4), u.get(5), status});
        }
        return rows;
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
    // DOCTOR ASSIGNMENT PAGE
    // =====================================================================
    @Override
    public ArrayList<Object[]> getShiftAssignments() {
        ArrayList<Object[]> rows = new ArrayList<>();
        TreeMap<Integer, ArrayList<String>> assignments = FileHandling.readAllRecords("ShiftDoctors.txt");
        TreeMap<Integer, ArrayList<String>> shifts = FileHandling.readAllRecords("Shifts.txt");
        if (assignments == null || shifts == null) {
            return rows;
        }
        for (Integer id : assignments.keySet()) {
            // a: 0 shift_id, 1 doctor_id, 2 deleted (may be missing)
            ArrayList<String> a = assignments.get(id);
            if (a.size() > 2 && a.get(2).equals("1")) {
                continue;
            }
            ArrayList<String> s = shifts.get(Integer.parseInt(a.get(0))); // 0 department_id, 1 date, 2 start, 3 end
            if (s == null) {
                continue;
            }
            rows.add(new Object[]{id, getDepartmentName(s.get(0)), s.get(1), s.get(2), s.get(3), getUserName(a.get(1))});
        }
        return rows;
    }

    @Override
    public ArrayList<Object[]> getShifts() {
        ArrayList<Object[]> rows = new ArrayList<>();
        TreeMap<Integer, ArrayList<String>> shifts = FileHandling.readActiveRecords("Shifts.txt");
        if (shifts == null) {
            return rows;
        }
        for (Integer id : shifts.keySet()) {
            ArrayList<String> s = shifts.get(id); // 0 department_id, 1 date, 2 start, 3 end
            rows.add(new Object[]{id, getDepartmentName(s.get(0)), s.get(1), s.get(2), s.get(3)});
        }
        return rows;
    }

    @Override
    public ArrayList<Object[]> getActiveDoctors() {
        ArrayList<Object[]> rows = new ArrayList<>();
        TreeMap<Integer, ArrayList<String>> users = FileHandling.readActiveRecords("Users.txt");
        if (users == null) {
            return rows;
        }
        for (Integer id : users.keySet()) {
            ArrayList<String> u = users.get(id); // 7 role
            if (!u.get(7).equals("Doctor")) {
                continue;
            }
            rows.add(new Object[]{id, u.get(0) + " " + u.get(1)});
        }
        return rows;
    }

    @Override
    public String assignDoctor(int shiftId, int doctorId) {
        TreeMap<Integer, ArrayList<String>> shifts = FileHandling.readActiveRecords("Shifts.txt");
        if (shifts == null || !shifts.containsKey(shiftId)) {
            return "Please select a shift.";
        }
        TreeMap<Integer, ArrayList<String>> users = FileHandling.readActiveRecords("Users.txt");
        if (users == null || !users.containsKey(doctorId) || !users.get(doctorId).get(7).equals("Doctor")) {
            return "Please select a doctor.";
        }
        TreeMap<Integer, ArrayList<String>> assignments = FileHandling.readAllRecords("ShiftDoctors.txt");
        if (assignments != null) {
            for (ArrayList<String> a : assignments.values()) { // 0 shift_id, 1 doctor_id, 2 deleted (may be missing)
                if (a.size() > 2 && a.get(2).equals("1")) {
                    continue;
                }
                if (a.get(0).equals(String.valueOf(shiftId)) && a.get(1).equals(String.valueOf(doctorId))) {
                    return "This doctor is already assigned to that shift.";
                }
            }
        }

        ArrayList<String> record = new ArrayList<>();
        record.add(String.valueOf(FileHandling.getNextID("ShiftDoctors.txt")));
        record.add(String.valueOf(shiftId));
        record.add(String.valueOf(doctorId));
        record.add("0");
        FileHandling.addRecord("ShiftDoctors.txt", record);
        return null;
    }

    @Override
    public String unassignDoctor(int assignmentId) {
        TreeMap<Integer, ArrayList<String>> assignments = FileHandling.readAllRecords("ShiftDoctors.txt");
        if (assignments == null || !assignments.containsKey(assignmentId)) {
            return "That assignment could not be found.";
        }
        ArrayList<String> a = assignments.get(assignmentId); // 0 shift_id, 1 doctor_id, 2 deleted (may be missing)

        ArrayList<String> record = new ArrayList<>();
        record.add(String.valueOf(assignmentId));
        record.add(a.get(0));
        record.add(a.get(1));
        record.add("1");
        FileHandling.editRecord("ShiftDoctors.txt", record);
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
        t.set(2, String.format(java.util.Locale.US, "%.2f", multiplier));

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
}
