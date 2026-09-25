
package MedicalManager;

import Users.Role;
import Users.User;
import java.time.LocalDate;
import java.time.LocalTime;
import HelperFunction.FileHandling;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class MedicalManager extends User{
    public MedicalManager(int user_id, String first_name, String last_name, String phone,
            String email, String password, String gender, LocalDate dob, Role role){
        super(user_id, first_name, last_name, phone, email, password, gender, dob, role);
    }
    
    public MedicalManager(){
        this.user_id = 0;
        this.first_name = "";
        this.last_name = "";
        this.phone = "";
        this.password = "";
        this.gender = "";
    }
    
    public List<ArrayList<String>> viewAllActiveDepartments(){
        List<ArrayList<String>> result = new ArrayList<>(); // create list to store lists of records
        TreeMap<Integer, ArrayList<String>> departments = FileHandling.readActiveRecords("Departments.txt");
        if(departments != null){
            for(Map.Entry<Integer, ArrayList<String>> department : departments.entrySet()){
                ArrayList<String> record = new ArrayList<>();
                record.add(String.valueOf(department.getKey()));
                record.addAll(department.getValue());
                result.add(record);
            }
        }
        return result;
    }

    public List<ArrayList<String>> viewManagingDepartments(){
            List<ArrayList<String>> result = new ArrayList<>(); // create list to store lists of records
            TreeMap<Integer, ArrayList<String>> departments = FileHandling.readAllRecords("Departments.txt");
            if(departments != null){
                for(Map.Entry<Integer, ArrayList<String>> department : departments.entrySet()){
                    int deptID = department.getKey();
                    ArrayList<String> details = department.getValue();
                    
                    // if is managed by current user and not deleted
                    if(Integer.parseInt(details.get(2)) == this.user_id && "0".equals(details.get(3))){
                        ArrayList<String> record = new ArrayList<>();
                        record.add(String.valueOf(deptID));
                        record.addAll(details);
                        
                        result.add(record);
                    }
                }
            }
            return result;
    }
    
    public void createDepartment(String deptName, String desc){
        int newID = FileHandling.getNextID("Departments.txt");
        
            ArrayList<String> record = new ArrayList<>();
            record.add(String.valueOf(newID));
            record.add(deptName);
            record.add(desc);
            record.add(String.valueOf(this.user_id));
            record.add("0"); // deleted flag, by default 0 = active
        
            FileHandling.addRecord("Departments.txt", record);
    }
    
    public boolean updateDepartment(int deptID, String deptName, String desc){
        // read all records
        // compare id
        // edit values of selected id
            TreeMap<Integer, ArrayList<String>> departments = FileHandling.readAllRecords("Departments.txt");
            if (departments != null && departments.containsKey(deptID)){
                ArrayList<String> details = departments.get(deptID);
                details.set(0, deptName);
                details.set(1, desc);
                
                // add ID to front because editRecord() uses Integer.valueOf(record.get(0)) as key for the given arraylist
                details.add(0, String.valueOf(deptID)); // add back ID because
                FileHandling.editRecord("Departments.txt", details);
                return true; // if true, show update successful
            }else{
                return false; // if false, show unable to update
            }
    }
    
    public boolean isDepartmentAlreadyExist(String deptName, int excludeSelfID){
        TreeMap<Integer, ArrayList<String>> departments = FileHandling.readActiveRecords("Departments.txt");
        if(departments != null){
            for(Map.Entry<Integer, ArrayList<String>> entry : departments.entrySet()){
                int deptID = entry.getKey();
                String existingDeptName = entry.getValue().get(0);
                if(deptID != excludeSelfID && existingDeptName.equalsIgnoreCase(deptName.trim())) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public boolean deleteDepartment(int deptID){
        try {
            FileHandling.removeRecord("Departments.txt", deptID);
            return true;
        }catch (Exception e){
            return false;
        }
    }
   
    public void createShift(int deptID, String date, String startTime,
           String endTime){
        int newID = FileHandling.getNextID("Shifts.txt");
        
        ArrayList<String> details = new ArrayList<>();
        details.add(String.valueOf(newID));
        details.add(String.valueOf(deptID));
        details.add(date);
        details.add(startTime);
        details.add(endTime);
        details.add("0");
        
        FileHandling.addRecord("Shifts.txt", details);
        
    }
    
    public boolean updateShift(int shiftID, int deptID, String date, String startTime, String endTime){
        TreeMap<Integer, ArrayList<String>> shifts = FileHandling.readAllRecords("Shifts.txt");
        if(shifts != null && shifts.containsKey(shiftID)){
            ArrayList<String> details = shifts.get(shiftID);
            details.set(0, String.valueOf(deptID));
            details.set(1, date);
            details.set(2, startTime);
            details.set(3, endTime);
            
            details.add(0, String.valueOf(shiftID));
            FileHandling.editRecord("Shifts.txt", details);
            return true;
        }
        return false;
    }
    
    public boolean deleteShift(int shiftID) {
        try {
            FileHandling.removeRecord("Shifts.txt", shiftID);
            TreeMap<Integer, ArrayList<String>> assignments = FileHandling.readActiveRecords("ShiftDoctors.txt");
            if (assignments != null) {
                for (Map.Entry<Integer, ArrayList<String>> entry : assignments.entrySet()) {
                    int assignID = entry.getKey();
                    int assignedShiftID = Integer.parseInt(entry.getValue().get(0)); 
                    
                    if (assignedShiftID == shiftID) {
                        removeDoctorShift(assignID);
                    }
                }
            }
            return true;
        } catch(Exception e) {
            return false;
        }
    }
    
    public List<ArrayList<String>> viewAllShifts() {
        TreeMap<Integer, ArrayList<String>> shifts = FileHandling.readAllRecords("Shifts.txt");
        List<ArrayList<String>> results = new ArrayList<>(); // create list to store list of records
        if(shifts != null) {
            for(Map.Entry<Integer, ArrayList<String>> entry : shifts.entrySet()){
                Integer shiftID = entry.getKey();
                ArrayList<String> details = entry.getValue();
                if("0".equals(details.get(4))) { // TreeMap -> { shiftID = [deptID, date, start_time, end_time, deleted] }
                    ArrayList<String> record = new ArrayList<>();
                    record.add(String.valueOf(shiftID));
                    record.addAll(details);
                    results.add(record);
                }
            }
        }
        return results;
    }
    
    public boolean isShiftDuplicate(int deptID, String date, String startTime,
            String endTime, int excludeShiftID){
        TreeMap<Integer, ArrayList<String>> shifts = FileHandling.readActiveRecords("Shifts.txt");
        if (shifts != null) {
            for (Map.Entry<Integer, ArrayList<String>> entry : shifts.entrySet()) {
                int shiftID = entry.getKey();
                ArrayList<String> details = entry.getValue();
                if (shiftID != excludeShiftID) {
                    int existingDeptID = Integer.parseInt(details.get(0));
                    String existingDate = details.get(1);
                    String existingStart = details.get(2);
                    String existingEnd = details.get(3);
                    if (existingDeptID == deptID && existingDate.equals(date) 
                            && existingStart.equals(startTime) && existingEnd.equals(endTime)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    public void assignDoctorToShift(int shiftID, int doctorID){
        int newID = FileHandling.getNextID("ShiftDoctors.txt");
        ArrayList<String> record = new ArrayList<>();
        record.add(String.valueOf(newID));
        record.add(String.valueOf(shiftID));
        record.add(String.valueOf(doctorID));
        record.add("0");
        
        FileHandling.addRecord("ShiftDoctors.txt", record);
    }
    
    public boolean removeDoctorShift(int assignmentID){
        try {
            FileHandling.removeRecord("ShiftDoctors.txt", assignmentID);
            return true;
        } catch(Exception e) {
            return false;
        }
    }
    
    
    public int getAssignedDoctorCount(int shiftID){
        TreeMap<Integer, ArrayList<String>> assignments = FileHandling.readActiveRecords("ShiftDoctors.txt");
        int count = 0;
        if(assignments != null){
            for(ArrayList<String> assignment : assignments.values()){
                if(Integer.parseInt(assignment.get(0)) == shiftID){
                    count++;
                }
            }
        }
        return count;
    }
    
    public List<String[]> getAssignedDoctors(int shiftID){
        List<String[]> results = new ArrayList<>();
        TreeMap<Integer, ArrayList<String>> assignments = FileHandling.readActiveRecords("ShiftDoctors.txt");
        TreeMap<Integer, ArrayList<String>> users = FileHandling.readActiveRecords("Users.txt");
        
        if(assignments != null){
            for(Map.Entry<Integer, ArrayList<String>> entry : assignments.entrySet()){
                int assignID = entry.getKey();
                int assignedShiftID = Integer.parseInt(entry.getValue().get(0));
                int doctorID = Integer.parseInt(entry.getValue().get(1));
                
                if(assignedShiftID == shiftID){
                    String doctorName = "";
                    if(users !=null && users.containsKey(doctorID)){
                        doctorName = users.get(doctorID).get(0) + users.get(doctorID).get(1);
                    }
                    results.add(new String[]{
                        String.valueOf(assignID),
                        String.valueOf(doctorID),
                        String.valueOf(doctorName),
                    });
                }
            }
        }
        return results;
    }
    
    public boolean hasTimeCollision(int doctorID, String dateStr, String startStr, String endStr){
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        // convert time strings to LocalDate objects
        LocalDate date = LocalDate.parse(dateStr, dateFormatter);
        LocalTime startTime = LocalTime.parse(startStr, timeFormatter);
        LocalTime endTime = LocalTime.parse(endStr, timeFormatter);
        // use LocalDateTime to glue time to date, to compare overnight shifts
        LocalDateTime selectedStart = date.atTime(startTime);
        LocalDateTime selectedEnd = date.atTime(endTime);
        // handle overnight shifts
        if (endTime.isBefore(startTime)) { // compare time (without date)
            selectedEnd = selectedEnd.plusDays(1); // add one day to end so end > start (time + date)
        }
        TreeMap<Integer, ArrayList<String>> assignments = FileHandling.readActiveRecords("ShiftDoctors.txt");
        TreeMap<Integer, ArrayList<String>> shifts = FileHandling.readActiveRecords("Shifts.txt");
        if(assignments != null && shifts != null){
            for(ArrayList<String> assignment : assignments.values()){
                int assignedDoctorID = Integer.parseInt(assignment.get(1));
                if(assignedDoctorID == doctorID){ // check shifts for selected doctor
                    int assignedShiftID = Integer.parseInt(assignment.get(0));
                    ArrayList<String> shiftDetails = shifts.get(assignedShiftID); // get shift details
                    if(shiftDetails != null){
                        // get the shift's date and time to be compared on selected shift
                        // if overlap with current shift, return true (has collision)
                        String assignedDateStr = shiftDetails.get(1);
                        String assignedStartStr = shiftDetails.get(2);
                        String assignedEndStr = shiftDetails.get(3);
                        
                        LocalDate assignedDate = LocalDate.parse(assignedDateStr, dateFormatter);
                        LocalTime assignedStartTime = LocalTime.parse(assignedStartStr, timeFormatter);
                        LocalTime assignedEndTime = LocalTime.parse(assignedEndStr, timeFormatter);
                        LocalDateTime assignedStart = assignedDate.atTime(assignedStartTime);
                        LocalDateTime assignedEnd = assignedDate.atTime(assignedEndTime);
                        if(assignedEndTime.isBefore(assignedStartTime)){ // compare start and end time (without date)
                            assignedEnd = assignedEnd.plusDays(1);
                        }
                        // check if doctor already have shift at selected shift time
                        // check for shiftStart < dsEnd AND shiftEnd > dsStart
                        if(selectedStart.isBefore(assignedEnd) && selectedEnd.isAfter(assignedStart)){
                            return true; // collision found
                        }
                    }
                }
            }
        }
        return false;
    }
    
    public List<String[]> getEligibleDoctors(int shiftID, int deptID){
        List<String[]> eligible = new ArrayList<>();
        
        TreeMap<Integer, ArrayList<String>> shifts = FileHandling.readActiveRecords("Shifts.txt");
        if(shifts == null || !shifts.containsKey(shiftID)){ return eligible; }
        String date = shifts.get(shiftID).get(1);
        String startTime = shifts.get(shiftID).get(2);
        String endTime = shifts.get(shiftID).get(3);
        
        LocalDate shiftDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        String shiftDay = shiftDate.getDayOfWeek().name();
        
        // get assigned doctors
        ArrayList<Integer> assignedDoctorIDs = new ArrayList<>(); // IDs of all doctors assigned to this shift
        List<String[]> assigned = getAssignedDoctors(shiftID);
        for(String[] assignment : assigned){
            assignedDoctorIDs.add(Integer.parseInt(assignment[1]));
        }
        TreeMap<Integer, ArrayList<String>> doctors = FileHandling.readAllRecords("Doctors.txt");
        TreeMap<Integer, ArrayList<String>> users = FileHandling.readActiveRecords("Users.txt");
        if(doctors != null){
            for(Map.Entry<Integer, ArrayList<String>> entry : doctors.entrySet()){
                int doctorID = entry.getKey();
                int doctorDeptID = Integer.parseInt(entry.getValue().get(0));
                String offDay = entry.getValue().get(3);
                // if doctor belongs in the department, not off day and not in that same shift
                if(doctorDeptID == deptID && !shiftDay.equalsIgnoreCase(offDay) &&
                        !assignedDoctorIDs.contains(doctorID)){
                    // check time collisions with other shifts
                    if(!hasTimeCollision(doctorID, date, startTime, endTime)){
                        String doctorName = "";
                        if(users != null && users.containsKey(doctorID)){
                            doctorName = users.get(doctorID).get(0) + " " + users.get(doctorID).get(1);
                        }
                        eligible.add(new String[]{String.valueOf(doctorID), doctorName, offDay});
                    }
                
                }
            }
        }
        
        return eligible;
    }
    
    public double[] getRevenueMetrics(){
        TreeMap<Integer, ArrayList<String>> invoices = FileHandling.readActiveRecords("Invoices.txt");
        TreeMap<Integer, ArrayList<String>> receipts = FileHandling.readActiveRecords("Receipts.txt");
        double totalInvoiced = 0.0, collected = 0.0;
        
        if(invoices != null) {
            for(ArrayList<String> invoice : invoices.values()) {
                totalInvoiced += Double.parseDouble(invoice.get(2)); 
            }
        }
        if(receipts != null) {
            for(ArrayList<String> receipt : receipts.values()) {
                collected += Double.parseDouble(receipt.get(3)); 
            }
        }
        double outstanding = totalInvoiced - collected;
        return new double[]{totalInvoiced, collected, outstanding};
    }
    
    public int[] getOccupancyMetrics(){
        TreeMap<Integer, ArrayList<String>> beds = FileHandling.readActiveRecords("InpatientBeds.txt");
        TreeMap<Integer, ArrayList<String>> admissions = FileHandling.readActiveRecords("Admissions.txt");
        int total = 0, occupied = 0, available = 0;
        
        if (beds != null) {
            total = beds.size();
        }
        if (admissions != null) {
            for(ArrayList<String> admission : admissions.values()){
                String dischargeDate = admission.size() > 3 ? admission.get(3) : "";
                // If there is no discharge date, the patient is still in the bed
                if(dischargeDate == null || dischargeDate.trim().isEmpty() || dischargeDate.equalsIgnoreCase("null")) {
                    occupied++; 
                }
            }
        }
        
        available = Math.max(0, total - occupied); // Prevent negative numbers just in case
        return new int[]{total, occupied, available};
    }
    
    public double[] getReviewMetrics() {
        TreeMap<Integer, ArrayList<String>> reviews = FileHandling.readActiveRecords("Reviews.txt");
        int totalReviews = 0;
        double sumRating = 0.0;
        
        if(reviews != null) {
            for(ArrayList<String> review : reviews.values()){
                totalReviews++;
                sumRating += Double.parseDouble(review.get(1)); 
            }
        }
        double avgRating = totalReviews == 0 ? 0.0 : (sumRating / totalReviews);
        return new double[]{avgRating, totalReviews};
    }
    
    public int[] getNumberCases(){
        TreeMap<Integer, ArrayList<String>> cases = FileHandling.readAllRecords("Cases.txt");
        int open = 0, closed = 0, total = 0;
        for (ArrayList<String> c: cases.values()){
            if("0".equals(c.get(7))){ // if not deleted
                total++;
                String closeDate = c.get(3);
                // check if there is no close date, means still open
                if(closeDate == null || closeDate.trim().isEmpty() || closeDate.equalsIgnoreCase("null")) { 
                    open++;
                } else {
                    closed++;
                }
            }
            
        }
        return new int[]{open, closed, total};
    }
    
    public List<String[]> getRevenueTableData(){
        List<String[]> data = new ArrayList<>();
        TreeMap<Integer, ArrayList<String>> receipts = FileHandling.readActiveRecords("Receipts.txt");
        if (receipts != null) {
            for (Map.Entry<Integer, ArrayList<String>> entry : receipts.entrySet()) {
                String rcptId = String.format("RCPT%03d", entry.getKey());
                String invId = String.format("INV%03d", Integer.parseInt(entry.getValue().get(0)));
                String paymentMethod = entry.getValue().get(2).toUpperCase().replace("_", " "); 
                String amount = String.format("RM %.2f", Double.parseDouble(entry.getValue().get(3)));
                String date = entry.getValue().get(4);
                
                data.add(new String[]{rcptId, invId, paymentMethod, amount, date});
            }
        }
        return data;
    }
    
    public List<String[]> getCasesTableData(){
        List<String[]> data = new ArrayList<>();
        TreeMap<Integer, ArrayList<String>> cases = FileHandling.readActiveRecords("Cases.txt");
        if (cases != null) {
            for (Map.Entry<Integer, ArrayList<String>> entry : cases.entrySet()) {
                String caseId = String.format("CASE%03d", entry.getKey());
                String patientId = String.format("USER%03d", Integer.parseInt(entry.getValue().get(0)));
                String category = entry.getValue().get(4).toUpperCase();
                String type = entry.getValue().get(5).toUpperCase();
                
                String closeDate = entry.getValue().get(3);
                String status = (closeDate == null || closeDate.trim().isEmpty() || closeDate.equalsIgnoreCase("null")) ? "OPEN" : "CLOSED";
                
                data.add(new String[]{caseId, patientId, category, type, status});
            }
        }
        return data;
    }
    
    public List<String[]> getConsultationsTableData(){
        List<String[]> data = new ArrayList<>();
        TreeMap<Integer, ArrayList<String>> consults = FileHandling.readActiveRecords("Consultations.txt");
        if (consults != null) {
            for (Map.Entry<Integer, ArrayList<String>> entry : consults.entrySet()) {
                String consId = String.format("CONS%03d", entry.getKey());
                String caseId = String.format("CASE%03d", Integer.parseInt(entry.getValue().get(0)));
                String docId = String.format("USER%03d", Integer.parseInt(entry.getValue().get(1)));
                String status = entry.getValue().get(5).toUpperCase(); // consultation_status
                String date = entry.getValue().get(7);
                
                data.add(new String[]{consId, caseId, docId, date, status});
            }
        }
        return data;
    }
      
    public List<String[]> getWardTableData(){
        List<String[]> data = new ArrayList<>();
        TreeMap<Integer, ArrayList<String>> wards = FileHandling.readActiveRecords("InpatientWards.txt");
        TreeMap<Integer, ArrayList<String>> beds = FileHandling.readActiveRecords("InpatientBeds.txt");
        TreeMap<Integer, ArrayList<String>> admissions = FileHandling.readActiveRecords("Admissions.txt");

        if (wards != null) {
            for (Map.Entry<Integer, ArrayList<String>> entry : wards.entrySet()) {
                int wardId = entry.getKey();
                String wardIdStr = String.format("WARD%02d", wardId);
                String deptId = String.format("DEP%03d", Integer.parseInt(entry.getValue().get(0)));
                int capacity = Integer.parseInt(entry.getValue().get(2));
                
                int occupied = 0;
                // Count occupied beds physically located in this specific ward
                if (beds != null && admissions != null) {
                    for (Map.Entry<Integer, ArrayList<String>> bed : beds.entrySet()) {
                        if (Integer.parseInt(bed.getValue().get(0)) == wardId) {
                            int currentBedId = bed.getKey();
                            // Check if this bed is currently assigned to an active admission
                            for (ArrayList<String> adm : admissions.values()) {
                                if (Integer.parseInt(adm.get(1)) == currentBedId) {
                                    String disDate = adm.size() > 3 ? adm.get(3) : "";
                                    if (disDate == null || disDate.trim().isEmpty() || disDate.equalsIgnoreCase("null")) {
                                        occupied++;
                                        break; 
                                    }
                                }
                            }
                        }
                    }
                }
                int available = Math.max(0, capacity - occupied);
                data.add(new String[]{wardIdStr, deptId, String.valueOf(capacity), String.valueOf(occupied), String.valueOf(available)});
            }
        }
        return data;
    }
    
    public List<String[]> getReviewsTableData(){
        List<String[]> data = new ArrayList<>();
        TreeMap<Integer, ArrayList<String>> reviews = FileHandling.readActiveRecords("Reviews.txt");
        if (reviews != null) {
            for (Map.Entry<Integer, ArrayList<String>> entry : reviews.entrySet()) {
                String revId = String.format("REV%03d", entry.getKey());
                String consId = String.format("CONS%03d", Integer.parseInt(entry.getValue().get(0)));
                String rating = entry.getValue().get(1) + " / 5";
                
                // Remove the backticks (`) from the text file strings
                String comment = entry.getValue().get(2).replace("`", ""); 
                
                data.add(new String[]{revId, consId, rating, comment});
            }
        }
        return data;
    }
    
}