
package Doctor;

import Users.OffDay;
import Users.Role;
import Users.User;
import java.time.LocalDate;

public class Doctor extends User{
    private String department_id, specialization;
    private int years_experience;
    private OffDay off_day;
    
    public Doctor(int user_id, String first_name, String last_name, String phone,
            String password, String gender, LocalDate dob, Role role, String department_id,
            String specialization, int years_experience, OffDay off_day){
        super(user_id, first_name, last_name, phone, password, gender, dob, role);
        this.department_id = department_id;
        this.specialization = specialization;
        this.years_experience = years_experience;
        this.off_day = off_day;
    }
    
    public String getDepartmentID(){
        return this.department_id;
    }
    
    public String getSpecialization(){
        return this.specialization;
    }
    
    public int getYearsExperience(){
        return this.years_experience;
    }
    
    public OffDay getOffDay(){
        return this.off_day;
    }
    
    public void setDepartmentID(String department_id){
        this.department_id = department_id;
    }
    
    public void setSpecialization(String specialization){
        this.specialization = specialization;
    }
    
    public void setYearsExperience(int years_experience){
        this.years_experience = years_experience;
    }
    
    public void setOffDay(OffDay off_day){
        this.off_day = off_day;
    }
}
