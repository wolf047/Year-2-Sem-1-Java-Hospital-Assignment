package Users;


import java.time.LocalDate;
import java.util.*;
import HelperFunction.FileHandling;

public class User {
    protected String first_name, last_name, phone, password;
    protected int user_id, gender;
    protected LocalDate dob;
    protected Role role;
    
    public User(int user_id, String first_name, String last_name, String phone,
            String password, int gender, LocalDate dob, Role role){
        this.user_id = user_id;
        this.first_name = first_name;
        this.last_name = last_name;
        this.phone = phone;
        this.password = password;
        this.gender = gender;
        this.dob = dob;
        this.role = role;
    }
    
    public int getUserID(){
        return this.user_id;
    }
    
    public String getFirst(){
        return this.first_name;
    }
    
    public String getLast(){
        return this.last_name;
    }
    
    public String getPhone(){
        return this.phone;
    }

    public String getPassword() {
        return this.password;
    }

    public int getGender() {
        return this.gender;
    }
    
    public LocalDate getDob(){
        return this.dob;
    }

    public Role getRole() {
        return this.role;
    }

    public void setUserID(int user_id) {
        this.user_id = user_id;
    }

    public void setFirst(String first_name) {
        this.first_name = first_name;
    }

    public void setLast(String last_name) {
        this.last_name = last_name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public void setRole(Role role) {
        this.role = role;
    }
    
    
    // update profile method that updates Users.txt directly
    // return type boolean, true = success; false = error message
    public boolean updateProfile(String firstName, String lastName, String phone, String email,
            String password, int gender, LocalDate dob){
        TreeMap<Integer, ArrayList<String>> usersMap = FileHandling.readAllRecords("Users.txt");
        int userID = this.user_id;
        
        // if records not empty and current id exists in record
        // update to Users.txt using FileHandling editRecord method
        if (usersMap != null && usersMap.containsKey(userID)) {
            ArrayList<String> values = usersMap.get(userID);
            values.set(0, firstName);
            values.set(1, lastName);
            values.set(2, dob.toString());
            values.set(3, String.valueOf(gender));
            values.set(4, phone);
            values.set(5, email);
            values.set(6, password);
            
            FileHandling.editRecord("Users.txt", values);
            
            // update current class memory of user properties
            this.first_name = firstName;
            this.last_name = lastName;
            this.dob = dob;
            this.phone = phone;
            this.password = password;
            this.gender = gender;
            
            return true;
        }
        
        return false;
    }
}
