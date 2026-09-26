/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Doctor;

/**
 *
 * @author Sascha
 */

import java.util.ArrayList;

public interface DoctorServices {

    String getFullName();
    String getFirst();
    String getLast();
    String getPhone();
    String getEmail();
    String getDoctorCode();
    String getDepartmentName();
    String getSpecialization();
    String getOffDayText();
    String saveProfile(String phone, String email, String currentPwd, String newPwd, String confirmPwd);

    ArrayList<Object[]> getSchedule();

    void finalizeAllConsultations();
    void loadWeek(int weekOffset);
    Object[][] getWeekRows();
    String[] getWeekHeaders();
    String getWeekRange();
    int getWeekConsultId(int row, int col);

    ArrayList<Object[]> getCases();
    int getCaseId(int row);

    boolean loadCase(int caseId);
    String getCaseTitle();
    String getCaseMeta();
    String getCaseRoleNote();
    boolean isCaseOpen();
    boolean isCaseInCharge();
    String[] getCasePatient();
    String getCaseSummary();
    ArrayList<Object[]> getCaseConsultRows();
    int getCaseConsultId(int row);
    ArrayList<Object[]> getCaseTestRows();
    String getCaseTestDetail(int row);
    String saveCaseSummary(String summary);
    String closeCase();

    boolean loadConsultation(int consultId);
    int getConsultCaseId();
    String getConsultTitle();
    String getConsultMeta();
    String getConsultStatus();
    String getConsultRoleNote();
    String getComplaint();
    String getVitalSigns();
    String getNotes();
    boolean canEditConsultation();
    String saveConsultationProgress(String vitals, String notes);
    String completeConsultation(String vitals, String notes);

    ArrayList<String> getDrugForms();
    ArrayList<Object[]> searchDrugs(String form, String text);
    int getDrugResultId(int row);
    String getDosageUnit(String form);
    ArrayList<String[]> getPrescriptionItems(int consultId);
    String checkPrescriptionItem(String unit, String dosage, String frequency, String instructions);
    String savePrescription(int consultId, ArrayList<String[]> items);

    ArrayList<String> getServiceCategories();
    ArrayList<String> getServiceTypes(String category);
    ArrayList<Object[]> searchServices(String category, String type, String text);
    int getServiceResultId(int row);
    String checkText(String text);
    // requests: {serviceId, remarks}
    String submitDiagnosticRequests(int consultId, ArrayList<String[]> requests);
    // rows: {requestId, serviceId, serviceName, requestDate, remarks}
    ArrayList<Object[]> getDiagnosticRequestItems(int consultId);
    String deleteDiagnosticRequest(int requestId);
    String today();

    ArrayList<Object[]> getReviews();
    String getRatingSummary();
}