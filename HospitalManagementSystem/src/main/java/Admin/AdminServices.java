/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Admin;

import java.util.ArrayList;

/**
 *
 * @author Sascha
 */
public interface AdminServices {

    String getFullName();

    // =====================================================================
    // MANAGE USERS
    // =====================================================================
    // rows: {userId, fullName, role, phone, email, status}
    ArrayList<Object[]> getUsers(String search, String roleFilter);
    // {firstName, lastName, dob, gender, phone, email, password, role}
    String[] getUserDetail(int userId);
    // {departmentId, specialization, practiceStartYear, offDay} or null
    String[] getDoctorDetail(int userId);
    // {bloodType, allergies} or null
    String[] getPatientDetail(int userId);
    // rows: {departmentId, departmentName}
    ArrayList<Object[]> getDepartments();
    String createUser(String firstName, String lastName, String dob, String gender, String phone,
            String email, String password, String role, String deptId, String specialization,
            String practiceStartYear, String offDay, String bloodType, String allergies);
    String updateUser(int userId, String firstName, String lastName, String dob, String gender,
            String phone, String email, String password, String deptId, String specialization,
            String practiceStartYear, String offDay, String bloodType, String allergies);
    String toggleUserStatus(int userId);

    // =====================================================================
    // ASSIGN DOCTORS TO MEDICAL MANAGERS
    // =====================================================================
    // rows: {doctorId, doctorName, departmentName, managerName}
    ArrayList<Object[]> getDoctorAssignments();
    // rows: {departmentId, "DeptName (Manager: X)"}
    ArrayList<Object[]> getAssignableDepartments();
    String assignDoctorToDepartment(int doctorId, int departmentId);

    // =====================================================================
    // HOSPITAL ASSETS
    // =====================================================================
    ArrayList<String> getAssetCategories();
    // rows: {assetId, detail, status}
    ArrayList<Object[]> getAssets(String category);
    ArrayList<String> getAssetTypes(String category);
    String addAsset(String category, String type);
    String toggleAssetStatus(String category, int assetId);

    // =====================================================================
    // WARDS & BEDS
    // =====================================================================
    // rows: {wardId, departmentName, gender, capacity, bedCount}
    ArrayList<Object[]> getWards();
    // {departmentId, gender, capacity} or null
    String[] getWardDetail(int wardId);
    String addWard(int departmentId, String gender, int capacity);
    String updateWard(int wardId, String gender, int capacity);
    // rows: {bedId, wardId}
    ArrayList<Object[]> getBeds(int wardId);
    String addBed(int wardId);
    String removeBed(int bedId);

    // =====================================================================
    // ADMISSIONS
    // =====================================================================
    // rows: {caseId, "Case #X - PatientName"}
    ArrayList<Object[]> getCasesForAdmission();
    // rows: {bedId, "Bed #X - Ward Y (gender)"}
    ArrayList<Object[]> getAvailableBeds(int caseId);
    // rows: {admissionId, patientName, bedLabel, admissionDate, dischargeDate, remarks, status}
    ArrayList<Object[]> getAdmissions();
    String createAdmission(int caseId, int bedId, String admissionDate, String remarks);
    String dischargePatient(int admissionId, String dischargeDate);

    // =====================================================================
    // DIAGNOSTICS: LAB RESULTS & IMAGING SCHEDULING
    // =====================================================================
    // rows: {requestId, serviceName, patientName, requestDate, requestRemarks}
    ArrayList<Object[]> getPendingLabRequests();
    String submitLabResult(int requestId, String results);
    // rows: {requestId, serviceName, patientName, requestDate}
    ArrayList<Object[]> getPendingImagingRequests();
    // rows: {imagingRoomId, "Room #X (type)"} matching the request's required type
    ArrayList<Object[]> getImagingRoomsForRequest(int requestId);
    String scheduleImaging(int requestId, int imagingRoomId, String date, String startTime, String endTime);

    // =====================================================================
    // CATALOGUES: DRUGS & DIAGNOSTIC SERVICES
    // =====================================================================
    // rows: {drugId, name, form, price, status}
    ArrayList<Object[]> getDrugCatalogue();
    ArrayList<String> getDrugForms();
    String addDrug(String name, String form, String price);
    String updateDrug(int drugId, String name, String form, String price);
    String toggleDrugStatus(int drugId);

    // rows: {serviceId, name, category, type, price, status}
    ArrayList<Object[]> getServiceCatalogue();
    ArrayList<String> getServiceTypes(String category);
    String addService(String name, String category, String type, String price);
    String updateService(int serviceId, String name, String category, String type, String price);
    String toggleServiceStatus(int serviceId);

    // =====================================================================
    // RATES & INSURANCE
    // =====================================================================
    float getConsultationFee();
    float getHospitalisationFee();
    String updateFees(float consultationFee, float hospitalisationFee);

    // rows: {tierId, tierName, minimumYears, multiplier}
    ArrayList<Object[]> getTiers();
    String updateTierMultiplier(int tierId, float multiplier);

    // rows: {insuranceId, insuranceName}
    ArrayList<Object[]> getInsuranceNetworks();
    String addInsuranceNetwork(String name);
    String removeInsuranceNetwork(int insuranceId);
}
