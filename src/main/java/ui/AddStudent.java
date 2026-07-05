package ui;

import dao.DepartmentDAO;
import dao.StudentDAO;
import model.Department;
import model.Student;
import utils.ImageHelper;
import utils.Validation;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class AddStudent extends JPanel {

    private static final String[] SEMESTERS = {
        "1st", "2nd", "3rd", "4th", "5th", "6th", "7th", "8th"
    };
    private static final String[] BLOOD_GROUPS = {
        "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"
    };
    private static final String[] GENDERS = { "Male", "Female", "Other" };

    private final StudentDAO studentDao = new StudentDAO();
    private final DepartmentDAO departmentDao = new DepartmentDAO();
    private final Runnable onFinished;

    private final JLabel headerTitle = new JLabel();
    private final JLabel headerSubtitle = new JLabel();

    private final JTextField studentCodeField = new JTextField();
    private final JTextField nameField = new JTextField();
    private final JTextField emailField = new JTextField();
    private final JTextField phoneField = new JTextField();
    private final JComboBox<Department> departmentCombo = new JComboBox<>();
    private final JComboBox<String> semesterCombo = new JComboBox<>(SEMESTERS);
    private final JSpinner cgpaSpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 4.0, 0.01));
    private final JComboBox<String> bloodGroupCombo = new JComboBox<>(BLOOD_GROUPS);
    private final JComboBox<String> genderCombo = new JComboBox<>(GENDERS);
    private final JTextArea addressArea = new JTextArea(3, 18);

    private final JLabel photoPreview = new JLabel();
    private final PillButton saveButton = new PillButton("Save Student", PillButton.Style.PRIMARY);

    private Integer editingId;
    private File pendingPhotoFile;
    private String existingPhotoPath;

    public AddStudent(Runnable onFinished) {
        this.onFinished = onFinished;
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(32, 36, 32, 36));

        studentCodeField.putClientProperty("JTextField.placeholderText", "e.g. 2024001");
        nameField.putClientProperty("JTextField.placeholderText", "e.g. John Doe");
        emailField.putClientProperty("JTextField.placeholderText", "e.g. john@example.com");
        phoneField.putClientProperty("JTextField.placeholderText", "e.g. 01700000000");
        addressArea.putClientProperty("JTextArea.placeholderText", "Street, city, ZIP");

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);

        startAdd();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        headerTitle.setFont(Theme.FONT_TITLE);
        headerTitle.setForeground(Theme.TEXT_PRIMARY);
        headerTitle.setAlignmentX(LEFT_ALIGNMENT);
        headerSubtitle.setFont(Theme.FONT_SUBTITLE);
        headerSubtitle.setForeground(Theme.TEXT_MUTED);
        headerSubtitle.setAlignmentX(LEFT_ALIGNMENT);
        header.add(headerTitle);
        header.add(Box.createVerticalStrut(4));
        header.add(headerSubtitle);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        return header;
    }

    private JPanel buildBody() {
        RoundedPanel card = new RoundedPanel(16, Theme.CARD, Theme.BORDER);
        card.setLayout(new BorderLayout(24, 0));
        card.setBorder(BorderFactory.createEmptyBorder(26, 28, 26, 28));

        card.add(buildForm(), BorderLayout.CENTER);
        card.add(buildPhotoPanel(), BorderLayout.EAST);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(card, BorderLayout.CENTER);
        wrapper.add(buildButtonBar(), BorderLayout.SOUTH);
        return wrapper;
    }

    private JPanel buildForm() {
        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        JPanel personal = new JPanel(new GridBagLayout());
        personal.setOpaque(false);
        GridBagConstraints pgc = new GridBagConstraints();
        pgc.fill = GridBagConstraints.HORIZONTAL;
        pgc.insets = new Insets(6, 8, 6, 8);
        int prow = 0;
        addField(personal, pgc, prow, 0, "Student ID", studentCodeField);
        addField(personal, pgc, prow++, 1, "Full Name", nameField);
        addField(personal, pgc, prow, 0, "Email", emailField);
        addField(personal, pgc, prow++, 1, "Phone", phoneField);
        addField(personal, pgc, prow++, 0, "Gender", genderCombo);

        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);
        JScrollPane addressScroll = new JScrollPane(addressArea);
        pgc.gridx = 0;
        pgc.gridy = prow;
        pgc.gridwidth = 2;
        pgc.insets = new Insets(6, 8, 6, 8);
        JPanel addressWrap = new JPanel(new BorderLayout());
        addressWrap.setOpaque(false);
        JLabel addrLabel = new JLabel("Address");
        addrLabel.setFont(Theme.FONT_SUBTITLE);
        addrLabel.setForeground(Theme.TEXT_MUTED);
        addrLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        addressWrap.add(addrLabel, BorderLayout.NORTH);
        addressWrap.add(addressScroll, BorderLayout.CENTER);
        personal.add(addressWrap, pgc);

        JPanel academic = new JPanel(new GridBagLayout());
        academic.setOpaque(false);
        GridBagConstraints agc = new GridBagConstraints();
        agc.fill = GridBagConstraints.HORIZONTAL;
        agc.insets = new Insets(6, 8, 6, 8);
        int arow = 0;
        addField(academic, agc, arow, 0, "Department", departmentCombo);
        addField(academic, agc, arow++, 1, "Semester", semesterCombo);
        addField(academic, agc, arow, 0, "CGPA (0.0 - 4.0)", cgpaSpinner);
        addField(academic, agc, arow++, 1, "Blood Group", bloodGroupCombo);

        form.add(sectionLabel("Personal Information"));
        form.add(Box.createVerticalStrut(14));
        form.add(personal);
        form.add(Box.createVerticalStrut(22));
        form.add(sectionLabel("Academic Information"));
        form.add(Box.createVerticalStrut(14));
        form.add(academic);

        return form;
    }

    private JPanel sectionLabel(String text) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

        JPanel bar = new JPanel();
        bar.setBackground(Theme.PRIMARY);
        bar.setPreferredSize(new Dimension(4, 16));

        JLabel label = new JLabel(text);
        label.setFont(Theme.FONT_SECTION);
        label.setForeground(Theme.TEXT_PRIMARY);

        row.add(bar, BorderLayout.WEST);
        row.add(label, BorderLayout.CENTER);
        return row;
    }

    private void addField(JPanel form, GridBagConstraints gc, int row, int col, String label, java.awt.Component field) {
        JLabel l = new JLabel(label);
        l.setFont(Theme.FONT_SUBTITLE);
        l.setForeground(Theme.TEXT_MUTED);

        JPanel wrap = new JPanel(new BorderLayout(0, 4));
        wrap.setOpaque(false);
        wrap.add(l, BorderLayout.NORTH);
        field.setFont(Theme.FONT_BODY);
        wrap.add(field, BorderLayout.CENTER);

        gc.gridx = col;
        gc.gridy = row;
        gc.gridwidth = 1;
        gc.weightx = 1;
        form.add(wrap, gc);
    }

    private JPanel buildPhotoPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(210, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 0));

        JLabel photoCaption = new JLabel("Student Photo");
        photoCaption.setFont(Theme.FONT_SUBTITLE);
        photoCaption.setForeground(Theme.TEXT_MUTED);
        photoCaption.setAlignmentX(CENTER_ALIGNMENT);

        RoundedPanel photoFrame = new RoundedPanel(90, Theme.BG, Theme.BORDER);
        photoFrame.setLayout(new BorderLayout());
        photoFrame.setPreferredSize(new Dimension(172, 172));
        photoFrame.setMaximumSize(new Dimension(172, 172));
        photoFrame.setAlignmentX(CENTER_ALIGNMENT);
        photoFrame.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        photoPreview.setHorizontalAlignment(SwingConstants.CENTER);
        photoPreview.setVerticalAlignment(SwingConstants.CENTER);
        photoFrame.add(photoPreview, BorderLayout.CENTER);

        PillButton chooseButton = new PillButton("Choose Image", PillButton.Style.GHOST);
        chooseButton.setAlignmentX(CENTER_ALIGNMENT);
        chooseButton.addActionListener(e -> onChooseImage());

        panel.add(Box.createVerticalGlue());
        panel.add(photoCaption);
        panel.add(Box.createVerticalStrut(12));
        panel.add(photoFrame);
        panel.add(Box.createVerticalStrut(16));
        panel.add(chooseButton);
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private JPanel buildButtonBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 16));
        bar.setOpaque(false);
        PillButton cancel = new PillButton("Cancel", PillButton.Style.GHOST);
        cancel.addActionListener(e -> onFinished.run());
        saveButton.addActionListener(e -> onSave());
        bar.add(cancel);
        bar.add(saveButton);
        return bar;
    }

    public void reloadDepartments() {
        Department selected = (Department) departmentCombo.getSelectedItem();
        departmentCombo.removeAllItems();
        try {
            List<Department> departments = departmentDao.findAll();
            for (Department d : departments) {
                departmentCombo.addItem(d);
            }
            if (selected != null) {
                for (int i = 0; i < departmentCombo.getItemCount(); i++) {
                    if (departmentCombo.getItemAt(i).getId().equals(selected.getId())) {
                        departmentCombo.setSelectedIndex(i);
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not load departments:\n" + rootMessage(ex),
                "Neon connection error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void startAdd() {
        editingId = null;
        pendingPhotoFile = null;
        existingPhotoPath = null;
        headerTitle.setText("Add Student");
        headerSubtitle.setText("Fill in the details to enroll a new student");
        saveButton.setText("Save Student");

        studentCodeField.setText("");
        nameField.setText("");
        emailField.setText("");
        phoneField.setText("");
        semesterCombo.setSelectedIndex(0);
        cgpaSpinner.setValue(0.0);
        bloodGroupCombo.setSelectedIndex(0);
        genderCombo.setSelectedIndex(0);
        addressArea.setText("");
        photoPreview.setIcon(ImageHelper.placeholderIcon("?", 160, Theme.PRIMARY));
    }

    public void startEdit(Student s) {
        editingId = s.getId();
        pendingPhotoFile = null;
        existingPhotoPath = s.getPhotoPath();
        headerTitle.setText("Edit Student");
        headerSubtitle.setText("Update " + s.getFullName() + "'s record");
        saveButton.setText("Update Student");

        studentCodeField.setText(s.getStudentCode());
        nameField.setText(s.getFullName());
        emailField.setText(s.getEmail());
        phoneField.setText(s.getPhone());
        selectDepartment(s.getDepartmentId());
        semesterCombo.setSelectedItem(s.getSemester());
        cgpaSpinner.setValue(s.getCgpa() == null ? 0.0 : s.getCgpa().doubleValue());
        if (s.getBloodGroup() != null) bloodGroupCombo.setSelectedItem(s.getBloodGroup());
        if (s.getGender() != null) genderCombo.setSelectedItem(s.getGender());
        addressArea.setText(s.getAddress());

        var icon = ImageHelper.loadCircularIcon(s.getPhotoPath(), 160);
        photoPreview.setIcon(icon != null ? icon : ImageHelper.placeholderIcon(s.getFullName(), 160, Theme.PRIMARY));
    }

    private void selectDepartment(Integer departmentId) {
        if (departmentId == null) return;
        for (int i = 0; i < departmentCombo.getItemCount(); i++) {
            if (departmentCombo.getItemAt(i).getId().equals(departmentId)) {
                departmentCombo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void onChooseImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Image files", "jpg", "jpeg", "png", "gif"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            pendingPhotoFile = chooser.getSelectedFile();
            var icon = ImageHelper.loadCircularIcon(pendingPhotoFile.getAbsolutePath(), 160);
            if (icon != null) {
                photoPreview.setIcon(icon);
            } else {
                JOptionPane.showMessageDialog(this, "Could not read that image file.",
                    "Invalid image", JOptionPane.WARNING_MESSAGE);
                pendingPhotoFile = null;
            }
        }
    }

    private void onSave() {
        String code = studentCodeField.getText().trim();
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();

        if (Validation.isBlank(code)) {
            warn("Student ID is required.");
            return;
        }
        if (Validation.isBlank(name)) {
            warn("Full name is required.");
            return;
        }
        if (!Validation.isValidEmail(email)) {
            warn("Please enter a valid email address.");
            return;
        }
        if (!Validation.isValidPhone(phone)) {
            warn("Please enter a valid phone number.");
            return;
        }

        Student s = new Student();
        s.setStudentCode(code);
        s.setFullName(name);
        s.setEmail(email);
        s.setPhone(blankToNull(phone));
        s.setAddress(blankToNull(addressArea.getText()));
        s.setGender((String) genderCombo.getSelectedItem());
        Department dept = (Department) departmentCombo.getSelectedItem();
        s.setDepartmentId(dept == null ? null : dept.getId());
        s.setSemester((String) semesterCombo.getSelectedItem());
        s.setCgpa(BigDecimal.valueOf((Double) cgpaSpinner.getValue()).setScale(2, RoundingMode.HALF_UP));
        s.setBloodGroup((String) bloodGroupCombo.getSelectedItem());

        String photoPath = existingPhotoPath;
        if (pendingPhotoFile != null) {
            photoPath = ImageHelper.copyToAssets(pendingPhotoFile, code);
        }
        s.setPhotoPath(photoPath);

        boolean isNew = editingId == null;
        if (!isNew) {
            s.setId(editingId);
        }

        saveButton.setEnabled(false);
        saveButton.setText(isNew ? "Saving..." : "Updating...");

        new javax.swing.SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                if (isNew) {
                    studentDao.insert(s);
                } else {
                    studentDao.update(s);
                }
                return null;
            }

            @Override
            protected void done() {
                saveButton.setEnabled(true);
                saveButton.setText(isNew ? "Save Student" : "Update Student");
                try {
                    get();
                    JOptionPane.showMessageDialog(AddStudent.this,
                        isNew ? "Student added successfully." : "Student updated successfully.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                    onFinished.run();
                } catch (Exception ex) {
                    warn("Could not save student:\n" + rootMessage(ex));
                }
            }
        }.execute();
    }

    private String blankToNull(String s) {
        String trimmed = s == null ? "" : s.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void warn(String message) {
        JOptionPane.showMessageDialog(this, message, "Check your input", JOptionPane.WARNING_MESSAGE);
    }

    private static String rootMessage(Throwable ex) {
        Throwable cause = ex;
        while (cause.getCause() != null) cause = cause.getCause();
        return cause.getMessage() != null ? cause.getMessage() : cause.toString();
    }
}
