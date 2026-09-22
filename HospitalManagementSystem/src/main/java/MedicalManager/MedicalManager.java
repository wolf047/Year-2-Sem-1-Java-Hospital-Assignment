
package MedicalManager;

import Users.Role;
import Users.User;
import java.time.LocalDate;
import java.time.LocalTime;
import HelperFunction.FileHandling;
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
            details.set(0,String.valueOf(deptID));
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
        TreeMap<Integer, ArrayList<String>> shifts = FileHandling.readAllRecords("Shifts.txt");
        if (shifts != null && shifts.containsKey(shiftID)){
            ArrayList<String> record = shifts.get(shiftID);
            record.set(4, "1"); // set delete to true (1)
            
            record.add(0, String.valueOf(shiftID));
            FileHandling.editRecord("Shift.txt", record);
            return true;
        }
        return false;
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
    
    public void assignDoctorToShift(int shiftID, int doctorID){
        int newID = FileHandling.getNextID("ShiftDoctors");
        ArrayList<String> record = new ArrayList<>();
        record.add(String.valueOf(newID));
        record.add(String.valueOf(shiftID));
        record.add(String.valueOf(doctorID));
        record.add("0");
        
        FileHandling.addRecord("ShiftDoctors.txt", record);
    }
    
    public List<String[]> getDoctorsForShift(int targetShiftID) {
        List<String[]> assignedDoctors = new ArrayList<>();
        TreeMap<Integer, ArrayList<String>> assignments = FileHandling.readAllRecords("ShiftDoctors.txt");
        TreeMap<Integer, ArrayList<String>> users = FileHandling.readAllRecords("Users.txt");
        
        if(assignments != null) {
            
        }
        
        return assignedDoctors;
    }
    
    public void removeDoctorShift(int shiftID){
        FileHandling.removeRecord("ShiftDoctors", shiftID);
    }
    
    
    public double calculateTotalRevenue() {
        TreeMap<Integer, ArrayList<String>> invoices = FileHandling.readAllRecords("Invoices.txt");
        double total = 0.0;
        if(invoices != null) {
            for(ArrayList<String> invoice : invoices.values()) {
                if("0".equals(invoice.get(3))){
                    total += Double.parseDouble(invoice.get(2));
                }
            }
        }
        return total;
    }
    
    public int getUsedBeds() {
        TreeMap<Integer, ArrayList<String>> beds = FileHandling.readAllRecords("InpatientBeds.txt");
        int totalBeds = 0;
        if (beds != null) {
            for(ArrayList<String> bed : beds.values()){
                if("0".equals(bed.get(1))){
                    totalBeds++;
                }
            }
        }
        return totalBeds;
    }
    
    public int[] getCaseSummary(){
        TreeMap<Integer, ArrayList<String>> cases = FileHandling.readAllRecords("Cases.txt");
        return new int[]{1,2};
    }
      
    
}