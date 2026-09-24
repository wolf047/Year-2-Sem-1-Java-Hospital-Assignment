/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package HelperFunction;

/**
 *
 * @author Sascha
 */

import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.function.Function;

public final class TableLoader {
    private TableLoader() {}
    
    public static void fill(DefaultTableModel model, TreeMap<Integer, ArrayList<String>> records, Function<Row, Object[]> mapper) {
        model.setRowCount(0);
        if (records == null) {
            return;
        }
        for (var v : records.entrySet()) {
            model.addRow(mapper.apply(new Row(v.getKey(), v.getValue())));
        }
    }
    
    public static final class Row {
        public final int id;
        public final ArrayList<String> fields;
        public Row(int id, ArrayList<String> fields) {
            this.id = id;
            this.fields = fields;
        }
    }
}
