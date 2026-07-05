package ui;

import dao.StudentDAO;
import model.DashboardStats;
import model.Student;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;

public class Dashboard extends javax.swing.JFrame {

    private final String adminUsername;
    private final StudentDAO studentDao = new StudentDAO();

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);

    private final NavButton dashboardNav = new NavButton(null, "Dashboard");
    private final NavButton addNav = new NavButton(null, "Add Student");
    private final NavButton viewNav = new NavButton(null, "View Students");
    private final NavButton deptNav = new NavButton(null, "Departments");
    private final NavButton aboutNav = new NavButton(null, "About");
    private final NavButton logoutNav = new NavButton(null, "Logout");

    private final StatCard totalCard = new StatCard("Total Students", "—", Theme.PRIMARY);
    private final StatCard maleCard = new StatCard("Male Students", "—", new java.awt.Color(0x0EA5E9));
    private final StatCard femaleCard = new StatCard("Female Students", "—", new java.awt.Color(0xEC4899));
    private final StatCard deptCard = new StatCard("Departments", "—", Theme.SUCCESS);

    private final DefaultTableModel recentTableModel =
        new DefaultTableModel(new Object[]{"ID", "Name", "Department", "Semester"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

    private AddStudent addStudentPanel;
    private ViewStudents viewStudentsPanel;
    private DepartmentManagement departmentPanel;

    public Dashboard(String adminUsername) {
        super("EduTrack by Md. Tarek");
        this.adminUsername = adminUsername;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1200, 760));
        setLocationRelativeTo(null);
        getContentPane().setBackground(Theme.BG);
        setLayout(new BorderLayout());

        add(buildTopBar(), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.add(buildSidebar(), BorderLayout.WEST);
        body.add(buildContent(), BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);

        showHome();
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Theme.CARD);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER),
            BorderFactory.createEmptyBorder(14, 24, 14, 24)));

        JLabel title = new JLabel("EduTrack");
        title.setFont(Theme.FONT_SECTION.deriveFont(18f));
        title.setForeground(Theme.PRIMARY);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        right.setOpaque(false);
        JLabel who = new JLabel(adminUsername);
        who.setFont(Theme.FONT_BODY_BOLD);
        who.setForeground(Theme.TEXT_PRIMARY);
        PillButton logout = new PillButton("Logout", PillButton.Style.GHOST);
        logout.addActionListener(e -> doLogout());
        right.add(who);
        right.add(logout);

        bar.add(title, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Theme.SIDEBAR);
        sidebar.setPreferredSize(new Dimension(230, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(16, 8, 16, 8));

        dashboardNav.addActionListener(e -> showHome());
        addNav.addActionListener(e -> showAdd(null));
        viewNav.addActionListener(e -> showView());
        deptNav.addActionListener(e -> showDepartments());
        aboutNav.addActionListener(e -> showCard(aboutNav, "about"));
        logoutNav.addActionListener(e -> doLogout());

        for (NavButton nav : new NavButton[]{dashboardNav, addNav, viewNav, deptNav, aboutNav}) {
            nav.setAlignmentX(LEFT_ALIGNMENT);
            sidebar.add(nav);
        }
        sidebar.add(Box.createVerticalGlue());
        logoutNav.setAlignmentX(LEFT_ALIGNMENT);
        sidebar.add(logoutNav);
        sidebar.add(Box.createVerticalStrut(12));
        sidebar.add(buildSidebarCredit());
        return sidebar;
    }

    private JLabel buildSidebarCredit() {
        JLabel credit = new JLabel("<html>Developed by <b>Md. Tarek</b></html>");
        credit.setFont(Theme.FONT_SUBTITLE);
        credit.setForeground(Theme.SIDEBAR_TEXT_MUTED);
        credit.setAlignmentX(LEFT_ALIGNMENT);
        credit.setBorder(BorderFactory.createEmptyBorder(0, 20, 4, 20));
        credit.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        credit.setToolTipText("https://tarekdev59.vercel.app/");
        credit.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                try {
                    java.awt.Desktop.getDesktop().browse(java.net.URI.create("https://tarekdev59.vercel.app/"));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(Dashboard.this,
                        "Visit: https://tarekdev59.vercel.app/", "Developer", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
        return credit;
    }

    private JPanel buildContent() {
        addStudentPanel = new AddStudent(this::showView);
        viewStudentsPanel = new ViewStudents(this::showAdd);
        departmentPanel = new DepartmentManagement();

        contentPanel.setOpaque(false);
        contentPanel.add(buildHomePanel(), "home");
        contentPanel.add(addStudentPanel, "add");
        contentPanel.add(viewStudentsPanel, "view");
        contentPanel.add(departmentPanel, "dept");
        contentPanel.add(new About(), "about");

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Theme.BG);
        wrapper.add(contentPanel, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildHomePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Dashboard");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);
        JLabel subtitle = new JLabel("Overview of your student records");
        subtitle.setFont(Theme.FONT_SUBTITLE);
        subtitle.setForeground(Theme.TEXT_MUTED);
        titles.add(title);
        titles.add(Box.createVerticalStrut(4));
        titles.add(subtitle);
        header.add(titles, BorderLayout.WEST);

        PillButton refresh = new PillButton("Refresh", PillButton.Style.GHOST);
        refresh.addActionListener(e -> refreshHome());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        actions.setOpaque(false);
        actions.add(refresh);
        header.add(actions, BorderLayout.EAST);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(BorderFactory.createEmptyBorder(24, 0, 0, 0));

        JPanel statsRow = new JPanel(new GridLayout(1, 4, 20, 0));
        statsRow.setOpaque(false);
        statsRow.add(totalCard);
        statsRow.add(maleCard);
        statsRow.add(femaleCard);
        statsRow.add(deptCard);
        body.add(statsRow);
        body.add(Box.createVerticalStrut(24));

        RoundedPanel recentCard = new RoundedPanel(14, Theme.CARD, Theme.BORDER);
        recentCard.setLayout(new BorderLayout());
        recentCard.setBorder(BorderFactory.createEmptyBorder(20, 22, 20, 22));
        JLabel recentTitle = new JLabel("Recently Added Students");
        recentTitle.setFont(Theme.FONT_SECTION);
        recentTitle.setForeground(Theme.TEXT_PRIMARY);
        recentCard.add(recentTitle, BorderLayout.NORTH);

        JTable recentTable = new JTable(recentTableModel);
        recentTable.setRowHeight(34);
        recentTable.getTableHeader().setReorderingAllowed(false);
        JScrollPane scroll = new JScrollPane(recentTable);
        scroll.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        scroll.setPreferredSize(new Dimension(10, 220));
        recentCard.add(scroll, BorderLayout.CENTER);
        body.add(recentCard);

        panel.add(header, BorderLayout.NORTH);
        panel.add(body, BorderLayout.CENTER);
        return panel;
    }

    private void refreshHome() {
        new SwingWorker<DashboardStats, Void>() {
            @Override
            protected DashboardStats doInBackground() throws Exception {
                return studentDao.getDashboardStats();
            }

            @Override
            protected void done() {
                try {
                    DashboardStats stats = get();
                    totalCard.setValue(String.valueOf(stats.getTotalStudents()));
                    maleCard.setValue(String.valueOf(stats.getMaleCount()));
                    femaleCard.setValue(String.valueOf(stats.getFemaleCount()));
                    deptCard.setValue(String.valueOf(stats.getTotalDepartments()));

                    recentTableModel.setRowCount(0);
                    for (Student s : stats.getRecentStudents()) {
                        recentTableModel.addRow(new Object[]{
                            s.getStudentCode(), s.getFullName(),
                            s.getDepartmentName() == null ? "Unassigned" : s.getDepartmentName(),
                            s.getSemester() == null ? "" : s.getSemester()
                        });
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(Dashboard.this,
                        "Could not load dashboard stats:\n" + rootMessage(ex),
                        "Neon connection error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void showHome() {
        cardLayout.show(contentPanel, "home");
        setActiveNav(dashboardNav);
        refreshHome();
    }

    private void showAdd(Student editing) {
        addStudentPanel.reloadDepartments();
        if (editing == null) {
            addStudentPanel.startAdd();
        } else {
            addStudentPanel.startEdit(editing);
        }
        cardLayout.show(contentPanel, "add");
        setActiveNav(addNav);
    }

    private void showView() {
        viewStudentsPanel.refresh();
        cardLayout.show(contentPanel, "view");
        setActiveNav(viewNav);
    }

    private void showDepartments() {
        departmentPanel.refresh();
        cardLayout.show(contentPanel, "dept");
        setActiveNav(deptNav);
    }

    private void showCard(NavButton nav, String card) {
        cardLayout.show(contentPanel, card);
        setActiveNav(nav);
    }

    private void setActiveNav(NavButton active) {
        for (NavButton nav : new NavButton[]{dashboardNav, addNav, viewNav, deptNav, aboutNav}) {
            nav.setActive(nav == active);
        }
    }

    private void doLogout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Log out of EduTrack?",
            "Confirm logout", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            new Login().setVisible(true);
        }
    }

    private static String rootMessage(Throwable ex) {
        Throwable cause = ex;
        while (cause.getCause() != null) cause = cause.getCause();
        return cause.getMessage() != null ? cause.getMessage() : cause.toString();
    }
}
