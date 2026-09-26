
package MedicalManager;
import java.util.List;

public interface ReportGenerator {
    double[] getRevenueMetrics();
    int[] getOccupancyMetrics();
    int[] getNumberCases();
    List<String[]> getRevenueTableData();
}
