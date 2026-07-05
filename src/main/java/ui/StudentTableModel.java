package ui;

import model.Student;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class StudentTableModel extends AbstractTableModel {

    private static final String[] COLUMNS = {
        "Photo", "ID", "Name", "Department", "Semester", "Phone", "Action"
    };

    public static final int PHOTO_COLUMN = 0;
    public static final int ACTION_COLUMN = 6;

    private List<Student> rows = new ArrayList<>();

    public void setRows(List<Student> rows) {
        this.rows = rows;
        fireTableDataChanged();
    }

    public Student getStudentAt(int rowIndex) {
        return rows.get(rowIndex);
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Student s = rows.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> s;
            case 1 -> s.getStudentCode();
            case 2 -> s.getFullName();
            case 3 -> s.getDepartmentName() == null ? "Unassigned" : s.getDepartmentName();
            case 4 -> s.getSemester() == null ? "" : s.getSemester();
            case 5 -> s.getPhone() == null ? "" : s.getPhone();
            default -> "";
        };
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == ACTION_COLUMN;
    }
}
