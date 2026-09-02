
package MedicalManager;

import Users.Role;
import Users.User;
import java.time.LocalDate;
import HelperFunction.FileHandling;
import java.util.*;

public class MedicalManager extends User{
    public MedicalManager(int user_id, String first_name, String last_name, String phone,
            String password, int gender, LocalDate dob, Role role){
        super(user_id, first_name, last_name, phone, password, gender, dob, role);
    }
    
    public void createDepartment(String deptName, String desc){
        try{
            int newID = FileHandling.getNextID("Departments.txt");
        
        ArrayList<String> record = new ArrayList<>();
        record.add(String.valueOf(newID));
        record.add(deptName);
        record.add(desc);
        record.add(String.valueOf(this.user_id));
        record.add("0"); // deleted flag, by default 0 = active
        
        FileHandling.addRecord("Departments.txt", record);
        }catch(Exception e){
            System.out.println(e);
        }
    }
    
    public void updateDepartment(int deptID, String deptName, String desc){
        // read all records
        // compare id
        // edit values of selected id
    }
}
