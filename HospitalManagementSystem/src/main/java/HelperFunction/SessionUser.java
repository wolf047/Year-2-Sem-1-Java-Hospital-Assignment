
package HelperFunction;
import Users.User;

/**
 *
 * @author lmao
 */

public class SessionUser {
    private static User currentUser = null;
    
    public static void login(User user){
        currentUser = user;
    }
    
    public static User getCurrentUser(){
        return currentUser;
    }
    
    public static void logout(){
        currentUser = null;
    }
    
}
