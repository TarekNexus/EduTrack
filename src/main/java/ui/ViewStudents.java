package ui;

import dao.StudentDAO;
import model.Student;
import utils.ImageHelper;

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
import javax.swing.Timer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;

public class ViewStudents extends JPanel {

    private final StudentDAO studentDao = new StudentDAO();
    private final StudentTableModel tableModel = new StudentTableModel();
    private final JTable table = new JTable(tableModel);
    private final JTextField searchField = new JTextField();
    private final JLabel countLabel = new JLabel();
    private final Timer searchDebounce;
    private final Consumer<Student> onEdit;

    public ViewStudents(Consumer<Student> onEdit) {
        this.onEdit = onEdit;
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(32, 36, 32, 36));

        searchDebounce = new Timer(300, e -> runSearch());
        searchDebounce.setRepeats(false);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Students");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);
        JLabel subtitle = new JLabel("Search and manage every enrolled student");
        subtitle.setFont(Theme.FONT_SUBTITLE);
        subtitle.setForeground(Theme.TEXT_MUTED);
        titles.add(title);
        titles.add(Box.createVerticalStrut(4));
        titles.add(subtitle);

        header.add(titles, BorderLayout.WEST);
        return header;
    }

    private RoundedPanel buildTableCard() {
        RoundedPanel card = new RoundedPanel(16, Theme.CARD, Theme.BORDER);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        searchField.putClientProperty("JTextField.placeholderText", "Search by Student ID, name, or department…");
        searchField.setPreferredSize(new Dimension(360, 36));
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { searchDebounce.restart(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { searchDebounce.restart(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { searchDebounce.restart(); }
        });

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);
        toolbar.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));
        toolbar.add(searchField, BorderLayout.CENTER);

        table.setRowHeight(48);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setReorderingAllowed(false);
        table.getColumnModel().getColumn(StudentTableModel.PHOTO_COLUMN).setCellRenderer(new PhotoRenderer());
        table.getColumnModel().getColumn(StudentTableModel.PHOTO_COLUMN).setPreferredWidth(56);
        table.getColumnModel().getColumn(StudentTableModel.PHOTO_COLUMN).setMaxWidth(56);
        table.getColumnModel().getColumn(StudentTableModel.ACTION_COLUMN).setCellRenderer(new ActionRenderer());
        table.getColumnModel().getColumn(StudentTableModel.ACTION_COLUMN).setCellEditor(new ActionEditor());
        table.getColumnModel().getColumn(StudentTableModel.ACTION_COLUMN).setPreferredWidth(190);
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.rowAtPoint(e.getPoint());
                    int col = table.columnAtPoint(e.getPoint());
                    if (row >= 0 && col != StudentTableModel.ACTION_COLUMN) {
                        showDetails(tableModel.getStudentAt(row));
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        countLabel.setFont(Theme.FONT_SUBTITLE);
        countLabel.setForeground(Theme.TEXT_MUTED);
        countLabel.setBorder(BorderFactory.createEmptyBorder(12, 2, 0, 0));

        card.add(toolbar, BorderLayout.NORTH);
        card.add(scrollPane, BorderLayout.CENTER);
        card.add(countLabel, BorderLayout.SOUTH);
        return card;
    }

    public void refresh() {
        searchField.setText("");
        load(studentDao::findAll);
    }

    private void runSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            load(studentDao::findAll);
        } else {
            load(() -> studentDao.search(keyword));
        }
    }

    private interface QueryCall {
        List<Student> run() throws SQLException;
    }

    private void load(QueryCall call) {
        new SwingWorker<List<Student>, Void>() {
            @Override
            protected List<Student> doInBackground() throws Exception {
                return call.run();
            }

            @Override
            protected void done() {
                try {
                    List<Student> students = get();
                    tableModel.setRows(students);
                    countLabel.setText(students.size() + (students.size() == 1 ? " student" : " students"));
                } catch (Exception ex) {
                    showError("Could not load students", ex);
                }
            }
        }.execute();
    }

    private void showDetails(Student student) {
        StudentDetails.show(topFrame(), student);
    }

    private void deleteStudent(Student student) {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete " + student.getFullName() + "? This cannot be undone.",
            "Confirm delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            studentDao.delete(student.getId());
            runSearch();
        } catch (Exception ex) {
            showError("Could not delete student", ex);
        }
    }

    private java.awt.Frame topFrame() {
        return (java.awt.Frame) javax.swing.SwingUtilities.getWindowAncestor(this);
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

    private static final class PhotoRenderer implements TableCellRenderer {
        private final JLabel label = new JLabel();

        PhotoRenderer() {
            label.setOpaque(true);
            label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                         boolean hasFocus, int row, int column) {
            Student s = (Student) value;
            var icon = ImageHelper.loadCircularIcon(s.getPhotoPath(), 32);
            label.setIcon(icon != null ? icon : ImageHelper.placeholderIcon(s.getFullName(), 32, Theme.PRIMARY));
            label.setBackground(row % 2 == 0 ? Theme.CARD : Theme.CARD_HOVER);
            return label;
        }
    }

    private final class ActionRenderer implements TableCellRenderer {
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 0));

        ActionRenderer() {
            panel.setOpaque(true);
            panel.add(actionButton("View", Theme.PRIMARY));
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
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 0));
        private int currentRow;

        ActionEditor() {
            panel.setOpaque(true);
            JButton view = actionButton("View", Theme.PRIMARY);
            JButton edit = actionButton("Edit", Theme.TEXT_PRIMARY);
            JButton delete = actionButton("Delete", Theme.DANGER);

            view.addActionListener(e -> {
                fireEditingStopped();
                showDetails(tableModel.getStudentAt(currentRow));
            });
            edit.addActionListener(e -> {
                fireEditingStopped();
                onEdit.accept(tableModel.getStudentAt(currentRow));
            });
            delete.addActionListener(e -> {
                fireEditingStopped();
                deleteStudent(tableModel.getStudentAt(currentRow));
            });

            panel.add(view);
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
