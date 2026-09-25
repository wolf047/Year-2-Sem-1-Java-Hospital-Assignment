/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package HelperFunction;

/**
 *
 * @author Sascha
 */
import java.nio.file.*;
import java.util.*;
import java.io.IOException;
import java.io.UncheckedIOException;

public final class FileHandling {
    
// DECLARATION OF CONSTANT VARIABLES
    private static final Path DIRECTORY_PATH = Path.of("src/main/java/Database/");
    private static final Map<String, List<String>> FILENAME_HEADERS = new LinkedHashMap<>();
    static {
        FILENAME_HEADERS.put("Users.txt", List.of("user_id", "first_name", "last_name", "dob", "gender", "phone", "email", "password", "role", "deleted"));
        FILENAME_HEADERS.put("Doctors.txt", List.of("doctor_id", "department_id", "specialization", "practice_start_year", "off_day"));
        FILENAME_HEADERS.put("Patients.txt", List.of("patient_id", "blood_type", "allergies"));
        FILENAME_HEADERS.put("Departments.txt", List.of("department_id", "department_name", "description", "manager_id", "deleted"));
        FILENAME_HEADERS.put("Cases.txt", List.of("case_id", "patient_id", "doctor_in_charge", "open_date", "close_date", "category", "type", "case_summary", "deleted"));
        FILENAME_HEADERS.put("Consultations.txt", List.of("consultation_id", "case_id", "doctor_id", "complaint", "vital_signs", "notes", "consultation_status", "consult_room_id", "date", "start_time", "end_time", "deleted"));
        FILENAME_HEADERS.put("Admissions.txt", List.of("admission_id", "case_id", "bed_id", "admission_date", "discharge_date", "remarks", "deleted"));
        FILENAME_HEADERS.put("DrugCatalogue.txt", List.of("drug_id", "drug_name", "form", "price", "deleted"));
        FILENAME_HEADERS.put("Prescriptions.txt", List.of("prescription_id", "consultation_id", "deleted"));
        FILENAME_HEADERS.put("PrescriptionItems.txt", List.of("prescription_item_id", "prescription_id", "drug_id", "dosage", "frequency", "duration", "instructions", "deleted"));
        FILENAME_HEADERS.put("ConsultationRooms.txt", List.of("consult_room_id", "status", "deleted"));
        FILENAME_HEADERS.put("InpatientWards.txt", List.of("ward_id", "department_id", "gender", "capacity", "deleted"));
        FILENAME_HEADERS.put("InpatientBeds.txt", List.of("bed_id", "ward_id", "deleted"));
        FILENAME_HEADERS.put("Labs.txt", List.of("lab_id", "type", "status", "deleted"));
        FILENAME_HEADERS.put("ImagingRooms.txt", List.of("imaging_room_id", "type", "status", "deleted"));
        FILENAME_HEADERS.put("ImagingAppointments.txt", List.of("imaging_id", "request_id", "imaging_room_id", "date", "start_time", "end_time", "deleted"));
        FILENAME_HEADERS.put("DiagnosticServiceCatalogue.txt", List.of("service_id", "service_name", "category", "type", "price", "deleted"));
        FILENAME_HEADERS.put("DiagnosticServiceRequests.txt", List.of("request_id", "consultation_id", "service_id", "request_date", "request_remarks", "result_date", "results", "deleted"));
        FILENAME_HEADERS.put("AcceptedInsuranceNetworks.txt", List.of("insurance_id", "insurance_name", "deleted"));
        FILENAME_HEADERS.put("TierMultipliers.txt", List.of("tier_id", "tier_name", "minimum_years", "multiplier", "deleted"));
        FILENAME_HEADERS.put("ConsultationHospitalisationFees.txt", List.of("amount"));
        FILENAME_HEADERS.put("Invoices.txt", List.of("invoice_id", "case_id", "date_issued", "total_amount", "deleted"));
        FILENAME_HEADERS.put("InvoiceItems.txt", List.of("invoice_item_id", "invoice_id", "item_name", "quantity", "amount_charged", "deleted"));
        FILENAME_HEADERS.put("Receipts.txt", List.of("receipt_id", "invoice_id", "insurance_id", "payment_method", "amount_paid", "payment_date", "deleted"));
        FILENAME_HEADERS.put("Reviews.txt", List.of("review_id", "consultation_id", "rating", "comments", "date_reviewed", "deleted"));
        FILENAME_HEADERS.put("Shifts.txt", List.of("shift_id", "department_id", "date", "start_time", "end_time", "deleted"));
        FILENAME_HEADERS.put("ShiftDoctors.txt", List.of("assignment_id", "shift_id", "doctor_id", "deleted"));
    }

    private static final Set<String> WITHOUT_ID = Set.of("ConsultationHospitalisationFees.txt");
    private static final Set<String> WITHOUT_DELETE = Set.of("Doctors.txt", "Patients.txt", "ConsultationHospitalisationFees.txt");
    
 
// VALIDATION METHODS
    private static void fileExists(String filename) {
        if (filename == null) {
            throw new NullPointerException("Filename must not be null.");
        }
        if (!FILENAME_HEADERS.containsKey(filename)) {
            throw new IllegalArgumentException("Unknown file: " + filename);
        }
    }
    
    private static void methodSupported(String filename) {
        fileExists(filename);
        if (WITHOUT_ID.contains(filename)) {
            throw new UnsupportedOperationException("Operation not supported for file: " + filename);
        }
    }
    
    private static void deletedSupported(String filename) {
        fileExists(filename);
        if (WITHOUT_DELETE.contains(filename)) {
            throw new UnsupportedOperationException("Delete not supported for file: " + filename);
        }
    }
    
    private static void hasCorrectFieldCount(String filename, List<String> record) {
        fileExists(filename);
        List<String> header = FILENAME_HEADERS.get(filename);
        if (header == null || record == null || record.size() != header.size()) {
            throw new IllegalArgumentException("Number of attributes does not match fields for: " + filename);
        }
    }
        
 
// SETUP METHODS    
    private static void ensureDirectoryExists() {
        try {
            if (!Files.exists(DIRECTORY_PATH)) {
                Files.createDirectories(DIRECTORY_PATH);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to create directory: " + DIRECTORY_PATH, e);
        }
    }

    private static void ensureFileExists(String filename) {
        fileExists(filename);
        ensureDirectoryExists();
        Path filepath = DIRECTORY_PATH.resolve(filename);
        try {
            if (!Files.exists(filepath)) {
                Files.createFile(filepath);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to create file: " + filename, e);
        }
    }
    
// FORMATTING AND PARSING METHODS
    private static String formatAttribute(String attribute) {
        if (attribute == null) {
            throw new NullPointerException("Attribute must not be null.");
        } 
        String escapedAttribute = attribute.strip().replace("\\", "\\\\").replace("\r\n", "\\n").replace("\r", "\\n").replace("\n", "\\n");
        if (escapedAttribute.contains(",") || escapedAttribute.contains(" ")) {
            return "`" + escapedAttribute + "`";
        }
        return escapedAttribute;
    }
    
    private static ArrayList<String> parseRecordString(String record) {
        ArrayList<String> parsedRecord = new ArrayList<>();
        StringBuilder placeholder = new StringBuilder();
        boolean inQuotes = false;

        for (char character : record.toCharArray()) {
            if (character == '`') {
                inQuotes = !inQuotes;
            } else if (character == ',' && !inQuotes) {
                parsedRecord.add(placeholder.toString().strip());
                placeholder.setLength(0);
            } else {
                placeholder.append(character);
            }
        }
        parsedRecord.add(placeholder.toString().strip());
        return parsedRecord;
    }

    
// HELPER METHODS
    private static void addUpdatedRecords(Path filepath, TreeMap<Integer, ArrayList<String>> recordsMap) {
        ArrayList<String> updatedRecords = new ArrayList<>();
        for (Map.Entry<Integer, ArrayList<String>> record : recordsMap.entrySet()) {
            ArrayList<String> joinedRecord = new ArrayList<>(record.getValue());
            joinedRecord.add(0, String.valueOf(record.getKey()));
            joinedRecord.replaceAll(FileHandling::formatAttribute);
            updatedRecords.add(String.join(",", joinedRecord));
        }
        try {
            Files.write(filepath, updatedRecords, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write to: " + filepath, e);
        }
    }
    
    private static int deletedColIndex(String filename) {
        List<String> header = FILENAME_HEADERS.get(filename);
        int index = header.indexOf("deleted");
        if (index < 0) {
            throw new UnsupportedOperationException("File has no 'deleted' column: " + filename);
        }
        return index - 1;
    }
    
// USE HERE ===========================================================  
// PUBLIC METHODS
    public static Integer getNextID(String filename) {
        methodSupported(filename);
        ensureFileExists(filename);
        TreeMap<Integer, ArrayList<String>> recordsMap = readAllRecords(filename);
        if (recordsMap.isEmpty()) {
            return 1;
        }
        return recordsMap.lastKey() + 1;
    }

    public static void addRecord(String filename, ArrayList<String> record) {
        methodSupported(filename);
        if (record == null) {
            throw new NullPointerException("Record must not be null.");
        }
        ensureFileExists(filename);
        hasCorrectFieldCount(filename, record);
        
        ArrayList<String> formattedRecord = new ArrayList<>(record.size());
        for (String attribute : record) {
            formattedRecord.add(formatAttribute(attribute));
        }

        Path filepath = DIRECTORY_PATH.resolve(filename);
        try {
            Files.writeString(filepath, String.join(",", formattedRecord) + System.lineSeparator(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write: " + filename, e);        }
    }


    public static void editRecord(String filename, ArrayList<String> updated) {
        methodSupported(filename);
        if (updated == null) {
            throw new NullPointerException("Updated record must not be null.");
        }
        ensureFileExists(filename);  
        hasCorrectFieldCount(filename, updated);
        
        Path filepath = DIRECTORY_PATH.resolve(filename);
        TreeMap<Integer, ArrayList<String>> recordsMap = readAllRecords(filename);

        Integer key = Integer.valueOf(updated.get(0));
        if (!recordsMap.containsKey(key)) {
            throw new NoSuchElementException("Record does not exist: " + key);
        }

        ArrayList<String> value = new ArrayList<>(updated.subList(1, updated.size()));
        recordsMap.put(key, value);
        addUpdatedRecords(filepath, recordsMap);
    }
    
    
    public static void removeRecord(String filename, int recordID) {
        deletedSupported(filename);
        ensureFileExists(filename);
         
        Path filepath = DIRECTORY_PATH.resolve(filename);
        TreeMap<Integer, ArrayList<String>> recordsMap = readAllRecords(filename);

        if (!recordsMap.containsKey(recordID)) {
            throw new NoSuchElementException("Record does not exist: " + recordID);
        }
        
        int index = deletedColIndex(filename);
        ArrayList<String> value = new ArrayList<>(recordsMap.get(recordID));
        value.set(index, "1");
        recordsMap.put(recordID, value);
        addUpdatedRecords(filepath, recordsMap);
    }
    
    
    public static void updateConHosFees(ArrayList<Float> fees) {
        if (fees == null) {
            throw new NullPointerException("Fees must not be null.");
        }
        if (fees.size() != 2 || fees.get(0) == null || fees.get(1) == null) {
            throw new IllegalArgumentException("Fees must have exactly 2 non-null values.");
        }
        
        ensureFileExists("ConsultationHospitalisationFees.txt");
        
        ArrayList<String> formattedFees = new ArrayList<>();
        formattedFees.add(String.format(Locale.US, "%.2f", fees.get(0)));
        formattedFees.add(String.format(Locale.US, "%.2f", fees.get(1)));
        
        Path filepath = DIRECTORY_PATH.resolve("ConsultationHospitalisationFees.txt");
        
        try {
            Files.write(filepath, formattedFees, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write: " + "ConsultationHospitalisationFees.txt", e);        
        }
    }

    
    public static ArrayList<Float> readConHosFees() {
        ensureFileExists("ConsultationHospitalisationFees.txt");
        Path filepath = DIRECTORY_PATH.resolve("ConsultationHospitalisationFees.txt");

        ArrayList<Float> fees = new ArrayList<>();
        try {
            for (String row : Files.readAllLines(filepath)) {
                if (row.isBlank()) continue;
                fees.add(Float.parseFloat(row.strip()));
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read: " + "ConsultationHospitalisationFees.txt", e);        
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Corrupted file: contains non-numeric value.", e);
        }
        
        if (fees.size() < 2) {
            fees.clear();
            fees.add(0.00F);
            fees.add(0.00F);
        }
        return fees;
    }
    
    

    public static List<String> readHeader(String filename) {
        fileExists(filename);
        return FILENAME_HEADERS.get(filename);
    }
    

    public static TreeMap<Integer, ArrayList<String>> readAllRecords(String filename) {
        methodSupported(filename);
        ensureFileExists(filename);
        Path filepath = DIRECTORY_PATH.resolve(filename);
        TreeMap<Integer, ArrayList<String>> recordsMap = new TreeMap<>();

        List<String> records;
        try {
            records = Files.readAllLines(filepath);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read: " + filename, e);
        }
        
        for (String record : records) {
            if (record.isBlank()) continue;

            ArrayList<String> parsedRecord = parseRecordString(record);
            if (parsedRecord.isEmpty()) continue;

            Integer key;
            try {
                key = Integer.valueOf(parsedRecord.get(0));
            } catch (NumberFormatException e) {
                throw new IllegalStateException("Corrupted file: contains invalid ID.", e);
            }
            
            ArrayList<String> value = new ArrayList<>(parsedRecord.subList(1, parsedRecord.size()));
            recordsMap.put(key, value);
        }
        
        return recordsMap;
    }

    public static ArrayList<String> readSpecificRecord(String filename, int recordID) {
        methodSupported(filename);
        TreeMap<Integer, ArrayList<String>> recordsMap = readAllRecords(filename);
        ArrayList<String> specificRecord = recordsMap.get(recordID);
        
        if (specificRecord == null) {
            return null;
        }
        
        specificRecord.add(0, String.valueOf(recordID));
        return specificRecord;
    }
    
    public static TreeMap<Integer, ArrayList<String>> readActiveRecords(String filename) {
        deletedSupported(filename);
        TreeMap<Integer, ArrayList<String>> allRecordsMap = readAllRecords(filename);

        if (allRecordsMap.isEmpty()) {
            return allRecordsMap;
        }
        
        int index = deletedColIndex(filename);
        if (index < 0) {
            return allRecordsMap;
        }

        TreeMap<Integer, ArrayList<String>> activeRecordsMap = new TreeMap<>();
        for (Map.Entry<Integer, ArrayList<String>> entry : allRecordsMap.entrySet()) {
            Integer key = entry.getKey();
            ArrayList<String> value = entry.getValue();
            if (value.size() > index && value.get(index).equals("0")) {
                activeRecordsMap.put(key, value);
            }
        }
        return activeRecordsMap;
    }
}
