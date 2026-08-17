
package MedicalManager;

import Users.Role;
import Users.User;
import java.time.LocalDate;

public class MedicalManager extends User{
    public MedicalManager(String user_id, String first_name, String last_name, String phone,
            String password, int gender, LocalDate dob, Role role){
        super(user_id, first_name, last_name, phone, password, gender, dob, role);
    }
    
}
