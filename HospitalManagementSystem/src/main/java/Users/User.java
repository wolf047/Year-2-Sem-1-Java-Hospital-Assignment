package Users;


import java.time.LocalDate;

public class User {
    protected String user_id, first_name, last_name, phone, password;
    protected int gender;
    protected LocalDate dob;
    protected Role role;
    
    public User(String user_id, String first_name, String last_name, String phone,
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
    
    public String getUserID(){
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

    public void setUserID(String user_id) {
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
    
}
