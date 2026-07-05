package ui;

import dao.DepartmentDAO;
import model.Department;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

public class DepartmentManagement extends JPanel {

    private final DepartmentDAO departmentDao = new DepartmentDAO();
    private final DeptTableModel tableModel = new DeptTableModel();
    private final JTable table = new JTable(tableModel);
    private final JTextField nameField = new JTextField();

    public DepartmentManagement() {
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(32, 36, 32, 36));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Departments");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setAlignmentX(LEFT_ALIGNMENT);
        JLabel subtitle = new JLabel("Add, rename, or remove departments");
        subtitle.setFont(Theme.FONT_SUBTITLE);
        subtitle.setForeground(Theme.TEXT_MUTED);
        subtitle.setAlignmentX(LEFT_ALIGNMENT);
        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitle);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        return header;
    }

    private RoundedPanel buildBody() {
        RoundedPanel card = new RoundedPanel(16, Theme.CARD, Theme.BORDER);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(22, 24, 22, 24));

        JPanel addRow = new JPanel(new BorderLayout(10, 0));
        addRow.setOpaque(false);
        addRow.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        nameField.putClientProperty("JTextField.placeholderText", "Department name, e.g. CSt");
        nameField.setPreferredSize(new Dimension(10, 36));
        nameField.addActionListener(e -> onAdd());
        PillButton addButton = new PillButton("+ Add Department", PillButton.Style.PRIMARY);
        addButton.addActionListener(e -> onAdd());
        addRow.add(nameField, BorderLayout.CENTER);
        addRow.add(addButton, BorderLayout.EAST);

        table.setRowHeight(42);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setReorderingAllowed(false);
        table.getColumnModel().getColumn(2).setCellRenderer(new ActionRenderer());
        table.getColumnModel().getColumn(2).setCellEditor(new ActionEditor());
        table.getColumnModel().getColumn(2).setPreferredWidth(130);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        card.add(addRow, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    public void refresh() {
        new SwingWorker<List<Department>, Void>() {
            @Override
            protected List<Department> doInBackground() throws Exception {
                return departmentDao.findAll();
            }

            @Override
            protected void done() {
                try {
                    tableModel.setRows(get());
                } catch (Exception ex) {
                    showError("Could not load departments", ex);
                }
            }
        }.execute();
    }

    private void onAdd() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Department name is required.",
                "Check your input", JOptionPane.WARNING_MESSAGE);
            return;
        }
        runWrite(
            () -> departmentDao.insert(name),
            () -> {
                nameField.setText("");
                JOptionPane.showMessageDialog(this, "Department added successfully.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                refresh();
            },
            "Could not add department (it may already exist)");
    }

    private void onEdit(Department department) {
        String newName = JOptionPane.showInputDialog(this, "Department name",
            department.getName());
        if (newName == null) return;
        String trimmedName = newName.trim();
        if (trimmedName.isEmpty() || trimmedName.equals(department.getName())) return;
        runWrite(
            () -> departmentDao.update(department.getId(), trimmedName),
            () -> {
                JOptionPane.showMessageDialog(this, "Department updated successfully.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                refresh();
            },
            "Could not rename department");
    }

    private void onDelete(Department department) {
        String message = department.getStudentCount() > 0
            ? "Delete \"" + department.getName() + "\"? " + department.getStudentCount() +
              " student(s) will become Unassigned."
            : "Delete \"" + department.getName() + "\"?";
        int confirm = JOptionPane.showConfirmDialog(this, message,
            "Confirm delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        runWrite(() -> departmentDao.delete(department.getId()), this::refresh, "Could not delete department");
    }

    private interface ThrowingAction {
        void run() throws Exception;
    }

    private void runWrite(ThrowingAction action, Runnable onSuccess, String errorTitle) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                action.run();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    onSuccess.run();
                } catch (Exception ex) {
                    showError(errorTitle, ex);
                }
            }
        }.execute();
    }

    private void showError(String title, Exception ex) {
        Throwable cause = ex;
        while (cause.getCause() != null) cause = cause.getCause();
        JOptionPane.showMessageDialog(this, title + ":\n" + cause.getMessage(),
            "Neon connection error", JOptionPane.ERROR_MESSAGE);
    }

    private JButton actionButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(color);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(52, 26));
        return button;
    }

    private static final class DeptTableModel extends AbstractTableModel {
        private final String[] columns = { "Department", "Students", "Action" };
        private List<Department> rows = new ArrayList<>();

        void setRows(List<Department> rows) {
            this.rows = rows;
            fireTableDataChanged();
        }

        Department getDepartmentAt(int row) {
            return rows.get(row);
        }

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Department d = rows.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> d.getName();
                case 1 -> d.getStudentCount();
                default -> "";
            };
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 2;
        }
    }

    private final class ActionRenderer implements TableCellRenderer {
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));

        ActionRenderer() {
            panel.setOpaque(true);
            panel.add(actionButton("Edit", Theme.TEXT_PRIMARY));
            panel.add(actionButton("Delete", Theme.DANGER));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                         boolean hasFocus, int row, int column) {
            panel.setBackground(row % 2 == 0 ? Theme.CARD : Theme.CARD_HOVER);
            return panel;
        }
    }

    private final class ActionEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        private int currentRow;

        ActionEditor() {
            panel.setOpaque(true);
            JButton edit = actionButton("Edit", Theme.TEXT_PRIMARY);
            JButton delete = actionButton("Delete", Theme.DANGER);
            edit.addActionListener(e -> {
                fireEditingStopped();
                onEdit(tableModel.getDepartmentAt(currentRow));
            });
            delete.addActionListener(e -> {
                fireEditingStopped();
                onDelete(tableModel.getDepartmentAt(currentRow));
            });
            panel.add(edit);
            panel.add(delete);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            currentRow = row;
            panel.setBackground(table.getSelectionBackground());
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return null;
        }
    }
}
