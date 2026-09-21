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

public final class FileHandling {

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
        FILENAME_HEADERS.put("Reviews.txt", List.of("review_id", "consultation_id", "rating", "comments", "deleted"));
        FILENAME_HEADERS.put("Shifts.txt", List.of("shift_id", "department_id", "date", "start_time", "end_time", "deleted"));
        FILENAME_HEADERS.put("ShiftDoctors.txt", List.of("assignment_id", "shift_id", "doctor_id", "deleted"));
    }

    private static void ensureDirectoryExists() {
        try {
            if (!Files.exists(DIRECTORY_PATH)) {
                Files.createDirectories(DIRECTORY_PATH);
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void ensureFileExists(String filename) {
        try {
            ensureDirectoryExists();
            if (FILENAME_HEADERS.containsKey(filename)) {
                Path filepath = DIRECTORY_PATH.resolve(filename);
                if (!Files.exists(filepath)) {
                    Files.createFile(filepath);
                }
            } else {
                throw new IllegalArgumentException("File does not exist.");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static Integer getNextID(String filename) {
        try {
            ensureDirectoryExists();
            ensureFileExists(filename);
            if (!filename.equals("ConsultationHospitalisationFees.txt")) {
                TreeMap<Integer, ArrayList<String>> recordsMap = readAllRecords(filename);
                Integer lastID = 0;
                if (recordsMap == null) {
                    throw new IllegalStateException("Error: Cannot read file.");
                }
                if (!recordsMap.isEmpty()) {
                    lastID = recordsMap.lastKey();
                }
                return lastID + 1;
            } else {
                throw new IllegalArgumentException("File does not support ID numbering.");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }
    }

    private static String formatAttribute(String attribute) {
        if (attribute == null) {
            return "";
        } else if (attribute.contains(",") || attribute.contains(" ")) {
            return "`" + attribute.strip() + "`";
        } else {
            return attribute.strip();
        }
    }
    
    private static boolean hasCorrectFieldCount(String filename, List<String> record) {
        List<String> header = FILENAME_HEADERS.get(filename);
        if (header == null || record == null || record.size() != header.size()) {
            System.out.println("Error: Number of attributes do not match number of fields in file.");
            return false;
        }
        return true;
    }

    public static void addRecord(String filename, ArrayList<String> record) {
        try {
            ensureDirectoryExists();
            ensureFileExists(filename);
            if (!hasCorrectFieldCount(filename, record)) return;
            if (!filename.equals("ConsultationHospitalisationFees.txt")) {
                Path filepath = DIRECTORY_PATH.resolve(filename);
                for (int i = 0; i < record.size(); i++) {
                    String formattedAttribute = formatAttribute(record.get(i));
                    record.set(i, formattedAttribute);
                }
                Files.writeString(filepath, String.join(",", record) + System.lineSeparator(), StandardOpenOption.APPEND);
            } else {
                throw new IllegalArgumentException("File does not support usage of this method.");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    private static void addUpdatedRecords(Path filepath, TreeMap<Integer, ArrayList<String>> recordsMap) {
        try {
            ArrayList<String> updatedRecords = new ArrayList<>();

            for (Map.Entry<Integer, ArrayList<String>> record : recordsMap.entrySet()) {
                ArrayList<String> joinedRecord = new ArrayList<>(record.getValue());
                joinedRecord.add(0, String.valueOf(record.getKey()));
                joinedRecord.replaceAll(FileHandling::formatAttribute);
                updatedRecords.add(String.join(",", joinedRecord));
            }
            Files.write(filepath, updatedRecords, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void editRecord(String filename, ArrayList<String> updated) {
        try {
            ensureDirectoryExists();
            ensureFileExists(filename);  
            if (!hasCorrectFieldCount(filename, updated)) return;
            if (!filename.equals("ConsultationHospitalisationFees.txt")) {
                Path filepath = DIRECTORY_PATH.resolve(filename);
                TreeMap<Integer, ArrayList<String>> recordsMap = readAllRecords(filename);

                if (recordsMap == null) {
                    System.out.println("Error: No records available.");
                } else {
                    Integer key = Integer.valueOf(updated.get(0));
                    ArrayList<String> specificRecord = readSpecificRecord(filename, key);
                    if (specificRecord != null) {
                        ArrayList<String> value = new ArrayList<>(updated.subList(1, updated.size()));
                        recordsMap.put(key, value);
                        addUpdatedRecords(filepath, recordsMap);
                    } else {
                        System.out.println("Error: Record does not exist.");
                    }
                }
            } else {
                throw new IllegalArgumentException("File does not support usage of this method.");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    public static void updateConHosFees(ArrayList<Float> fees) {
        try {
            ensureDirectoryExists();
            ensureFileExists("ConsultationHospitalisationFees.txt");
            if (fees != null && fees.size() == 2 && fees.get(0) != null && fees.get(1) != null) {
                Path filepath = DIRECTORY_PATH.resolve("ConsultationHospitalisationFees.txt");
                ArrayList<String> formattedFees = new ArrayList<>();
                formattedFees.add(String.format(Locale.US, "%.2f", fees.get(0)));
                formattedFees.add(String.format(Locale.US, "%.2f", fees.get(1)));
                Files.write(filepath, formattedFees, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
            } else {
                throw new IllegalArgumentException("Error: Need 2 non-null values.");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    
    public static ArrayList<Float> readConHosFees() {
        try {
            ensureDirectoryExists();
            ensureFileExists("ConsultationHospitalisationFees.txt");
            Path filepath = DIRECTORY_PATH.resolve("ConsultationHospitalisationFees.txt");
            ArrayList<Float> fees = new ArrayList<>();
            for (String row : Files.readAllLines(filepath)) {
                if (row.isBlank()) continue;
                fees.add(Float.parseFloat(row.strip()));
            }
            if (fees.size() < 2) {
                fees.clear();
                fees.add(0.00F);
                fees.add(0.00F);
            }
            return fees;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }
    }
    
    public static void removeRecord(String filename, int recordID) {
        try {
            ensureDirectoryExists();
            ensureFileExists(filename);       
            if (!filename.equals("Patients.txt") && !filename.equals("Doctors.txt") && !filename.equals("ConsultationHospitalisationFees.txt")) {
                Path filepath = DIRECTORY_PATH.resolve(filename);
                TreeMap<Integer, ArrayList<String>> recordsMap = readAllRecords(filename);

                if (recordsMap == null) {
                    System.out.println("Error: No records available.");
                } else {
                    ArrayList<String> specificRecord = readSpecificRecord(filename, recordID);
                    if (specificRecord != null) {
                        ArrayList<String> value = new ArrayList<>(specificRecord.subList(1, specificRecord.size()));
                        value.set(value.size() - 1, "1");
                        recordsMap.put(recordID, value);
                        addUpdatedRecords(filepath, recordsMap);
                    } else {
                        System.out.println("Error: Record does not exist.");
                    }
                }
            } else {
                throw new IllegalArgumentException("File does not support usage of this method.");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static List<String> readHeader(String filename) {
        return FILENAME_HEADERS.get(filename);
    }

    private static ArrayList<String> parseRecordString(String record) {
        ArrayList<String> parsedRecord = new ArrayList<>();
        String placeholder = "";
        boolean inQuotes = false;

        for (char character : record.toCharArray()) {
            if (character == '`') {
                inQuotes = !inQuotes;
            } else if (character == ',' && !inQuotes) {
                parsedRecord.add(placeholder.strip());
                placeholder = "";
            } else {
                placeholder += character;
            }
        }
            parsedRecord.add(placeholder.toString().strip());

        return parsedRecord;
    }

    public static TreeMap<Integer, ArrayList<String>> readAllRecords(String filename) {
        try {
            TreeMap<Integer, ArrayList<String>> recordsMap = new TreeMap<>();

            ensureDirectoryExists();
            ensureFileExists(filename);
            if (!filename.equals("ConsultationHospitalisationFees.txt")) {
                Path filepath = DIRECTORY_PATH.resolve(filename);
                ArrayList<String> records = new ArrayList<>(Files.readAllLines(filepath));
                for (String record : records) {
                    if (record.isBlank()) continue;

                    ArrayList<String> parsedRecord = parseRecordString(record);

                    Integer key = Integer.valueOf(parsedRecord.get(0));
                    ArrayList<String> value = new ArrayList<>(parsedRecord.subList(1, parsedRecord.size()));

                    recordsMap.put(key, value);
                }
                return recordsMap;
            }
            else {
                throw new IllegalArgumentException("File does not support usage of this method.");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }
    }

    public static ArrayList<String> readSpecificRecord(String filename, int recordID) {
        try{
            TreeMap<Integer, ArrayList<String>> recordsMap = readAllRecords(filename);

            if (recordsMap == null || recordsMap.isEmpty()) {
                System.out.println("Error: No records available.");
                return null;
            } else {
                ArrayList<String> specificRecord = recordsMap.get(recordID);
                if (specificRecord != null) {
                    specificRecord.add(0, String.valueOf(recordID));
                    return specificRecord;
                } else {
                    System.out.println("Error: Record does not exist.");
                    return null;
                }
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return null; 
        }
    }
    
    public static TreeMap<Integer, ArrayList<String>> readActiveRecords(String filename) {
        try{
            if (!filename.equals("Patients.txt") && !filename.equals("Doctors.txt") && !filename.equals("ConsultationHospitalisationFees.txt")) {
                TreeMap<Integer, ArrayList<String>> allRecordsMap = readAllRecords(filename);

                if (allRecordsMap == null || allRecordsMap.isEmpty()) {
                    System.out.println("Error: No records available.");
                    return null;
                } else {
                    int index = FILENAME_HEADERS.get(filename).indexOf("deleted") - 1;
                    TreeMap<Integer, ArrayList<String>> activeRecordsMap = new TreeMap<>();

                    for (Map.Entry<Integer, ArrayList<String>> entry : allRecordsMap.entrySet()) {
                        Integer key = entry.getKey();
                        ArrayList<String> value = entry.getValue();

                        if (value.size() > index && value.get(index).equals("1")) {
                            activeRecordsMap.put(key, value);
                        }
                    }
                    return activeRecordsMap;
                }
            } else {
                throw new IllegalArgumentException("File does not support usage of this method.");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return null; 
        }
    }
}
