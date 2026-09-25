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

    // rows: {userId, fullName, role, phone, email, status}
    ArrayList<Object[]> getUsers();
    String toggleUserStatus(int userId);

    // rows: {assignmentId, department, date, startTime, endTime, doctorName}
    ArrayList<Object[]> getShiftAssignments();
    // rows: {shiftId, department, date, startTime, endTime}
    ArrayList<Object[]> getShifts();
    // rows: {doctorId, fullName}
    ArrayList<Object[]> getActiveDoctors();
    String assignDoctor(int shiftId, int doctorId);
    String unassignDoctor(int assignmentId);

    ArrayList<String> getAssetCategories();
    // rows: {assetId, detail, status}
    ArrayList<Object[]> getAssets(String category);
    String toggleAssetStatus(String category, int assetId);

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
