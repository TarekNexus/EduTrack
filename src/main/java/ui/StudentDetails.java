package ui;

import model.Student;
import utils.ImageHelper;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;

public class StudentDetails extends JDialog {

    private StudentDetails(Frame owner, Student student) {
        super(owner, "Student Details", true);
        getContentPane().setBackground(Theme.CARD);
        setLayout(new BorderLayout());
        setResizable(false);

        add(buildContent(student), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        pack();
        setSize(420, getHeight());
        setLocationRelativeTo(owner);
    }

    private RoundedPanel buildContent(Student s) {
        RoundedPanel panel = new RoundedPanel(0, Theme.CARD);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(28, 32, 12, 32));

        var icon = ImageHelper.loadCircularIcon(s.getPhotoPath(), 120);
        JLabel photo = new JLabel(icon != null ? icon : ImageHelper.placeholderIcon(s.getFullName(), 120, Theme.PRIMARY));
        photo.setAlignmentX(CENTER_ALIGNMENT);
        panel.add(photo);
        panel.add(Box.createVerticalStrut(10));

        JLabel name = new JLabel(s.getFullName(), SwingConstants.CENTER);
        name.setFont(Theme.FONT_SECTION);
        name.setForeground(Theme.TEXT_PRIMARY);
        name.setAlignmentX(CENTER_ALIGNMENT);
        panel.add(name);
        panel.add(Box.createVerticalStrut(18));

        panel.add(detailRow("Student ID", s.getStudentCode()));
        panel.add(detailRow("Department", s.getDepartmentName() == null ? "Unassigned" : s.getDepartmentName()));
        panel.add(detailRow("Semester", s.getSemester()));
        panel.add(detailRow("Email", s.getEmail()));
        panel.add(detailRow("Phone", s.getPhone()));
        panel.add(detailRow("Address", s.getAddress()));
        panel.add(detailRow("Blood Group", s.getBloodGroup()));
        panel.add(detailRow("CGPA", s.getCgpa() == null ? "—" : s.getCgpa().toString()));
        panel.add(detailRow("Gender", s.getGender()));

        return panel;
    }

    private java.awt.Component detailRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
        row.setAlignmentX(CENTER_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));

        JLabel l = new JLabel(label);
        l.setFont(Theme.FONT_SUBTITLE);
        l.setForeground(Theme.TEXT_MUTED);

        JLabel v = new JLabel(value == null || value.isBlank() ? "—" : value);
        v.setFont(Theme.FONT_BODY_BOLD);
        v.setForeground(Theme.TEXT_PRIMARY);
        v.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(l, BorderLayout.WEST);
        row.add(v, BorderLayout.EAST);
        return row;
    }

    private java.awt.Component buildFooter() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 16));
        bar.setBackground(Theme.CARD);
        PillButton close = new PillButton("Close", PillButton.Style.GHOST);
        close.addActionListener(e -> dispose());
        bar.add(close);
        return bar;
    }

    public static void show(Frame owner, Student student) {
        new StudentDetails(owner, student).setVisible(true);
    }
}
