
package Patient;

import Users.Role;
import Users.User;
import java.time.LocalDate;

public class Patient extends User{
     private String blood_type, allergies;
     
     public Patient(int user_id, String first_name, String last_name, String phone,
            String password, String gender, LocalDate dob, Role role, String blood_type, String allergies){
         super(user_id, first_name, last_name, phone, password, gender, dob, role);
         this.blood_type = blood_type;
         this.allergies = allergies;
     }
     
     public String getBloodType(){
         return this.blood_type;
     }
     
     public String getAllergies(){
         return this.allergies;
     }
     
     public void setBloodType(String blood_type){
         this.blood_type = blood_type;
     }
     
     public void setAllergies(String allergies){
         this.allergies = allergies;
     }
}
