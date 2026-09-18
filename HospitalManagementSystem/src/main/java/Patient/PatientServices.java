package Patient;

import java.util.ArrayList;

// What the Patient screens are allowed to ask for.
public interface PatientServices {

    // details to show
    int getUserID();

    String getFirst();

    String getLast();

    String getGender();

    String getPhone();

    String getEmail();

    String getBloodType();

    String getAllergies();

    String getFullName();

    String getDobText();

    // profile
    void reload();

    String saveProfile(String phone, String email, String current, String newPwd, String confirm);

    // ratings
    ArrayList<Object[]> getPendingRatings();

    ArrayList<Object[]> getPastRatings();

    String submitRating(String consultId, int rating, String comment);

    // medical history
    void loadHistory();

    ArrayList<Object[]> getVisitRows();

    ArrayList<String> getVisitDetails();

    ArrayList<Object[]> getPrescriptionRows();

    ArrayList<String> getPrescriptionDetails();

    ArrayList<Object[]> getTestRows();

    ArrayList<String> getTestDetails();

    // bookings
    String getDoctorName(String doctorId);

    ArrayList<String[]> getDepartments();

    ArrayList<String[]> getDoctors(String deptId);

    void loadSlots(String deptId, String doctorFilter, String date);

    ArrayList<Object[]> getSlotRows();

    ArrayList<String[]> getSlotInfo();

    String bookSlot(String[] info, String category, String reason);

    String getLastBookedRoom();

    ArrayList<String> getFreeTimes(String doctorId, String date, int ignoreId);

    String rescheduleBooking(int consultId, String doctorId, String date, String start, String end);

    String cancelBooking(int consultId);
}
