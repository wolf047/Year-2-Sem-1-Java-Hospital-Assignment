/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package HelperFunction;

/**
 *
 * @author Sascha
 */
import java.io.IOException;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/*
+ ensureDirectoryExists(): void
+ ensureFileExists(String filename): void
+ getNextID(String filename): int
+ formatAttribute(String attribute): String
+ addRecord(String filename, ArrayList<String> record): vois
+ editRecord(String filename, ArrayList<String> record): void
+ removeRecord(String filename, int recordID): void
+ readHeader(String filename): ArrayList<String>
+ readAllRecords(String filename): Map<Integer, ArrayList<String>>
+ readSpecificRecord(String filename, int recordID): ArrayList<String>

*/


public final class FileHandling {
    private static final Path DIRECTORY_PATH = Path.of("src/main/java/Database/");
    private static final Map<String, List<String>> FILENAME_HEADERS = new LinkedHashMap<>();
    static {
        FILENAME_HEADERS.put("AcceptedInsuranceNetworks.txt", List.of("insurance_id", "insurance_name", "deleted"));
        FILENAME_HEADERS.put("Admissions.txt", List.of("admission_id", "case_id", "bed_id", "admission_date", "discharge_date", "remarks", "deleted"));
        FILENAME_HEADERS.put("BaseConsultationFees.txt", List.of("base_fee"));
        FILENAME_HEADERS.put("Cases.txt", List.of("case_id", "patient_id", "doctor_in_charge", "open_date", "close_date", "category", "type", "case_summary", "deleted"));
        FILENAME_HEADERS.put("ConsultationRooms.txt", List.of("consult_room_id", "status", "deleted"));
        FILENAME_HEADERS.put("Consultations.txt", List.of("consultation_id", "case_id", "doctor_id", "complaint", "vital_signs", "notes", "booking_status", "consult_room_id", "date", "start_time", "end_time", "deleted"));
        FILENAME_HEADERS.put("CustomerReviews.txt", List.of("review_id", "consultation_id", "rating", "comments", "deleted"));
        FILENAME_HEADERS.put("Departments.txt", List.of("department_id", "department_name", "description", "manager_id", "deleted"));
        FILENAME_HEADERS.put("DiagnosticServicePrices.txt", List.of("service_id", "service_name", "category", "type", "base_price", "deleted"));
        FILENAME_HEADERS.put("DiagnosticServiceRequests.txt", List.of("request_id", "consultation_id", "service_id", "request_date", "request_remarks", "result_date", "results", "deleted"));
        FILENAME_HEADERS.put("Doctors.txt", List.of("doctor_id", "department_id", "specialization", "years_experience", "off_day"));
        FILENAME_HEADERS.put("ImagingRooms.txt", List.of("imaging_room_id", "type", "status", "deleted"));
        FILENAME_HEADERS.put("ImagingSlots.txt", List.of("imaging_id", "request_id", "imaging_room_id", "date", "start_time", "end_time", "deleted"));
        FILENAME_HEADERS.put("InpatientBeds.txt", List.of("bed_id", "ward_id", "deleted"));
        FILENAME_HEADERS.put("InpatientWards.txt", List.of("ward_id", "department_id", "gender", "capacity", "deleted"));
        FILENAME_HEADERS.put("InvoiceItemizations.txt", List.of("item_id", "invoice_id", "item_name", "quantity", "amount_charged", "deleted"));
        FILENAME_HEADERS.put("Invoices.txt", List.of("invoice_id", "case_id", "date_issued", "total_amount", "deleted"));
        FILENAME_HEADERS.put("Labs.txt", List.of("lab_id", "type", "status", "deleted"));
        FILENAME_HEADERS.put("Patients.txt", List.of("patient_id", "blood_type", "allergies"));
        FILENAME_HEADERS.put("Prescriptions.txt", List.of("prescription_id", "consultation_id", "medication_name", "dosage", "frequency", "duration", "instructions", "deleted"));
        FILENAME_HEADERS.put("Receipts.txt", List.of("receipt_id", "invoice_id", "insurance_id", "payment_method", "amount_paid", "payment_date", "deleted"));
        FILENAME_HEADERS.put("ShiftDoctors.txt", List.of("assignment_id", "shift_id", "doctor_id"));
        FILENAME_HEADERS.put("Shifts.txt", List.of("shift_id", "department_id", "duty", "date", "start_time", "end_time", "deleted"));
        FILENAME_HEADERS.put("TierMultipliers.txt", List.of("tier_id", "tier_name", "minimum_years", "multiplier", "deleted"));
        FILENAME_HEADERS.put("Users.txt", List.of("user_id", "first_name", "last_name", "dob", "gender", "phone", "email", "password", "role", "deleted"));
    }
    
    
    public static void ensureDirectoryExists(){
        try {
            if (!Files.exists(DIRECTORY_PATH)){
                Files.createDirectories(DIRECTORY_PATH);
            }
        }
        catch (Exception e){
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    
    public static void ensureFileExists(String filename){
        try {
            ensureDirectoryExists();
            if (FILENAME_HEADERS.containsKey(filename)){
                Path filepath = DIRECTORY_PATH.resolve(filename);
                if (!Files.exists(filepath)){
                    Files.createFile(filepath);
                }
            }
            else {
                System.out.println("Error: File does not exist.");
            }
        }
        catch (Exception e){
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    
    public static Integer getNextID(String filename){
        ensureDirectoryExists();
        ensureFileExists(filename);
        
        TreeMap<Integer, ArrayList<String>> recordsMap = readAllRecords(filename);
        Integer lastID = 0;
        if (recordsMap != null && !recordsMap.isEmpty()){
            lastID = recordsMap.lastKey();
        }
        
        return lastID + 1;
    }
    
    
    public static String formatAttribute(String attribute){
        if (attribute.contains(",") || attribute.contains(" ")){
            return "\"" + attribute.strip() + "\"";
        }
        else {
            return attribute.strip();
        }
    }
    
    
    public static void addRecord(String filename, ArrayList<String> record){
        try {
            ensureDirectoryExists();
            ensureFileExists(filename);
            Path filepath = DIRECTORY_PATH.resolve(filename);
            for (int i = 0; i < record.size(); i++){
                String formattedAttribute = formatAttribute(record.get(i));
                record.set(i, formattedAttribute);
            }
            Files.writeString(filepath, String.join(",", record) + System.lineSeparator(), StandardOpenOption.APPEND);
        }
        catch (Exception e){
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    
    public static void editRecord(String filename, ArrayList<String> record){
        try {
            ensureDirectoryExists();
            ensureFileExists(filename);
    
            Path filepath = DIRECTORY_PATH.resolve(filename);
            TreeMap<Integer, ArrayList<String>> recordsMap = readAllRecords(filename);
            
            if (recordsMap == null){
                System.out.println("Error: No records available.");
            }
            else {
                Integer key = Integer.valueOf(record.get(0));
                ArrayList<String> value = new ArrayList<>(record.subList(1, record.size()));
                recordsMap.put(key, value);

                ArrayList<String> updatedRecords = new ArrayList<>();

                for (Map.Entry<Integer, ArrayList<String>> line : recordsMap.entrySet()){
                    ArrayList<String> completeLine = new ArrayList<>(line.getValue());
                    completeLine.add(0, String.valueOf(line.getKey()));
                    updatedRecords.add(String.join(",", completeLine));            
                }

                Files.write(filepath, updatedRecords, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
            }
        }
        catch (Exception e){
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    
    public static void removeRecord(String filename, int recordID){
        try {
            ensureDirectoryExists();
            ensureFileExists(filename);
            
            Path filepath = DIRECTORY_PATH.resolve(filename);
            TreeMap<Integer, ArrayList<String>> recordsMap = readAllRecords(filename);
            
            if (recordsMap == null){
                System.out.println("Error: No records available.");
            }
            else {
                
                if (recordsMap.containsKey(recordID)){
                    ArrayList<String> removedRecord = recordsMap.remove(recordID);        
                    removedRecord.add(0, String.valueOf(recordID));

                    ArrayList<String> updatedRecords = new ArrayList<>();

                    for (Map.Entry<Integer, ArrayList<String>> line : recordsMap.entrySet()){
                        ArrayList<String> completeLine = new ArrayList<>(line.getValue());
                        completeLine.add(0, String.valueOf(line.getKey()));
                        updatedRecords.add(String.join(",", completeLine));            
                    }

                    Files.write(filepath, updatedRecords, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
                }
                else {
                    System.out.println("Error: Record does not exist.");
                }
                
            }
        }
        catch (Exception e){
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    
    public static List<String> readHeader(String filename){
        return FILENAME_HEADERS.get(filename);
    }
    
    
    public static ArrayList<String> parseRecordString(String record){
        ArrayList<String> parsedRecord = new ArrayList<>();
        String placeholder = "";
        boolean inQuotes = false;
        
        for (char character : record.toCharArray()){
            if (character == '"'){
                inQuotes = !inQuotes;
            }
            else if (character == ',' && !inQuotes){
                parsedRecord.add(placeholder.strip());
                placeholder = "";
            }
            else {
                placeholder += character;
            }
        }
        if (!placeholder.isEmpty()){
            parsedRecord.add(placeholder.strip()); 
        }
        
        return parsedRecord;   
    }
    
    
    public static TreeMap<Integer, ArrayList<String>> readAllRecords(String filename){
        TreeMap<Integer, ArrayList<String>> recordsMap = new TreeMap<>();
        
        try {
            ensureDirectoryExists();
            ensureFileExists(filename);
            
            Path filepath = DIRECTORY_PATH.resolve(filename);
            ArrayList<String> records = new ArrayList<>(Files.readAllLines(filepath));
            
            if (records.isEmpty()){
                return null;
            }
            else {
                for (String record : records){
                    ArrayList<String> parsedRecord = parseRecordString(record);

                    Integer key = Integer.valueOf(parsedRecord.get(0));
                    ArrayList<String> value = new ArrayList<>(parsedRecord.subList(1, parsedRecord.size()));

                    recordsMap.put(key, value);
                }
                return recordsMap;
            }
        }
        catch (Exception e){
            System.out.println("Error: " + e.getMessage());
            return null;
        } 
    }
    
    
    public static ArrayList<String> readSpecificRecord(String filename, int recordID){
        TreeMap<Integer, ArrayList<String>> recordsMap = readAllRecords(filename);
        
        if (recordsMap == null){
            System.out.println("Error: No records available.");
            return null;
        }
        else {
            ArrayList<String> specificRecord = recordsMap.get(recordID);
            specificRecord.add(0, String.valueOf(recordID));
            return specificRecord;
        }
    }
}
