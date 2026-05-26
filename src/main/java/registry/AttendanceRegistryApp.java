package registry;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.BasicStroke;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// Author: Group 3
// Part: Main Swing attendance registry application.
public class AttendanceRegistryApp extends JFrame {
    // Author: Group 3
    // Part: File paths, credentials, table columns, date formats, and color palette.
    private static final Path DATA_FILE = Path.of("data", "students.tsv");
    private static final Path DELETED_DATA_FILE = Path.of("data", "deleted_students.tsv");
    private static final Path ATTENDANCE_FILE = Path.of("data", "attendance_records.tsv");
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";
    private static final String[] COLUMNS = {
            "No.", "ID", "Name", "Course", "Section", "Birthday", "Department", "Attendance Status", "Time In", "Time Out"
    };
    private static final String[] ATTENDANCE_COLUMNS = {
            "Date", "ID", "Name", "Course", "Section", "Department", "Status", "Time In", "Time Out"
    };
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm:ss a");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
    private static final DateTimeFormatter SESSION_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM d, yyyy");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy");
    private static final LocalTime LATE_TIME = LocalTime.of(9, 0);

    // Author: Group 3
    // Part: Light and dark theme colors used by the full interface.
    private static final Color INK = new Color(30, 41, 59);
    private static final Color MUTED = new Color(100, 116, 139);
    private static final Color SURFACE = new Color(248, 250, 252);
    private static final Color PANEL = Color.WHITE;
    private static final Color ACCENT = new Color(22, 163, 74);
    private static final Color ACCENT_DARK = new Color(21, 128, 61);
    private static final Color DANGER = new Color(220, 38, 38);
    private static final Color DARK_INK = new Color(226, 232, 240);
    private static final Color DARK_MUTED = new Color(148, 163, 184);
    private static final Color DARK_SURFACE = new Color(15, 23, 42);
    private static final Color DARK_PANEL = new Color(30, 41, 59);

    // Author: Group 3
    // Part: Shared layouts, table models, in-memory records, and timer state.
    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);
    private final CardLayout dashboardCards = new CardLayout();
    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable registryTable = new JTable(tableModel);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
    private final DefaultTableModel attendanceTableModel = new DefaultTableModel(ATTENDANCE_COLUMNS, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable attendanceHistoryTable = new JTable(attendanceTableModel);
    private final DefaultTableModel calendarDayTableModel = new DefaultTableModel(ATTENDANCE_COLUMNS, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable calendarDayTable = new JTable(calendarDayTableModel);
    private final List<StudentRecord> records = new ArrayList<>();
    private final List<StudentRecord> deletedRecords = new ArrayList<>();
    private final List<AttendanceRecord> attendanceRecords = new ArrayList<>();
    private final Timer clockTimer = new Timer(1000, e -> updateClockLabel());

    // Author: Group 3
    // Part: Runtime UI component references used by refresh and event handlers.
    private JPanel dashboardPage;
    private JPanel sidebarPanel;
    private FadePanel dashboardContent;
    private JTextField loginIdField;
    private JPasswordField loginPasswordField;
    private JTextField usernameField;
    private JPasswordField registerPasswordField;
    private JPasswordField confirmPasswordField;
    private JTextField idField;
    private JTextField nameField;
    private JTextField courseField;
    private JTextField sectionField;
    private JComboBox<Integer> birthYearBox;
    private JComboBox<Integer> birthDayBox;
    private JComboBox<String> birthMonthBox;
    private JComboBox<String> departmentBox;
    private JTextField searchField;
    private JLabel recordsHeading;
    private JLabel dashboardSubtitle;
    private JLabel clockLabel;
    private JLabel totalUsersLabel;
    private JLabel presentUsersLabel;
    private JLabel absentUsersLabel;
    private JLabel lateUsersLabel;
    private JLabel activeSessionLabel;
    private JLabel profileNameLabel;
    private JLabel profileIdLabel;
    private JLabel profileCourseLabel;
    private JLabel profileSectionLabel;
    private JLabel profileDepartmentLabel;
    private JLabel profileAttendanceLabel;
    private JButton timeInSelectedButton;
    private JButton timeOutSelectedButton;
    private JButton addUserButton;
    private JButton editSelectedButton;
    private JButton editProfileButton;
    private JButton deleteSelectedButton;
    private JButton recycleBinButton;
    private JButton restoreSelectedButton;
    private JButton activeRecordsButton;
    private JButton navDashboardButton;
    private JButton navAttendanceButton;
    private JButton navHistoryButton;
    private JButton navProfileButton;
    private JButton darkModeButton;
    private JPanel calendarDaysPanel;
    private JLabel calendarMonthLabel;
    private JLabel selectedDateLabel;
    private JLabel selectedDateStatsLabel;
    private JTextField historySearchField;
    private JTextField historyDateField;
    private JComboBox<String> historyStatusFilter;
    private YearMonth visibleCalendarMonth = YearMonth.now();
    private LocalDate selectedCalendarDate = LocalDate.now();
    private String currentUsername;
    private boolean currentUserIsAdmin;
    private boolean showingRecycleBin;
    private boolean darkMode;
    private String activeDashboardSection = "dashboard";
    private LocalDate activeAttendanceDate = LocalDate.now();

    // Author: Group 3
    // Part: Program entry point.
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Swing's default look is fine if the OS look and feel is unavailable.
            }
            new AttendanceRegistryApp().setVisible(true);
        });
    }

    // Author: Group 3
    // Part: Window setup, view registration, saved-data loading, and clock startup.
    public AttendanceRegistryApp() {
        setTitle("Attendance Registry System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1040, 900));
        setLocationRelativeTo(null);

        root.add(buildLoginView(), "login");
        root.add(buildCreateView(), "create");
        root.add(buildDashboardView(), "dashboard");
        add(root);

        loadRecords();
        updateClockLabel();
        clockTimer.start();
        cards.show(root, "login");
    }

    // Author: Group 3
    // Part: Login screen UI.
    private JPanel buildLoginView() {
        JPanel page = pageShell();
        JPanel card = authCard("Attendance Registry", "Sign in to manage organization registry records.");

        loginIdField = field("Username");
        loginPasswordField = passwordField();

        card.add(labeled("Username", loginIdField));
        card.add(Box.createVerticalStrut(12));
        card.add(labeled("Password", loginPasswordField));
        card.add(Box.createVerticalStrut(22));

        JButton login = primaryButton("Log in", ActionIcon.LOG_IN);
        login.addActionListener(this::handleLogin);
        card.add(login);
        card.add(Box.createVerticalStrut(12));

        JButton create = ghostButton("Register account", ActionIcon.USER_PLUS);
        create.addActionListener(e -> cards.show(root, "create"));
        card.add(create);
        card.add(Box.createVerticalStrut(12));

        JLabel adminHint = new JLabel("<html>Admin: username <b>admin</b>, password <b>admin123</b></html>");
        adminHint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        adminHint.setForeground(MUTED);
        adminHint.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(adminHint);

        page.add(card, centerConstraints());
        return page;
    }

    // Author: Group 3
    // Part: Account registration screen UI.
    private JPanel buildCreateView() {
        JPanel page = pageShell();
        JPanel card = authCard("Register Account", "Create a member profile, then continue to the registry.");

        usernameField = field("username");
        registerPasswordField = passwordField();
        confirmPasswordField = passwordField();
        idField = field("2026-0001");
        nameField = field("Student name");
        courseField = field("BSIT");
        sectionField = field("A");
        birthYearBox = new JComboBox<>(yearOptions());
        birthDayBox = new JComboBox<>(dayOptions());
        birthMonthBox = new JComboBox<>(monthOptions());
        styleComboBox(birthYearBox);
        styleComboBox(birthDayBox);
        styleComboBox(birthMonthBox);
        departmentBox = new JComboBox<>(departmentOptions());
        styleComboBox(departmentBox);

        card.add(labeled("Username", usernameField));
        card.add(Box.createVerticalStrut(8));
        card.add(labeled("Password", registerPasswordField));
        card.add(Box.createVerticalStrut(8));
        card.add(labeled("Confirm Password", confirmPasswordField));
        card.add(Box.createVerticalStrut(8));
        card.add(labeled("Student ID", idField));
        card.add(Box.createVerticalStrut(10));
        card.add(labeled("Student Name", nameField));
        card.add(Box.createVerticalStrut(10));
        card.add(labeled("Course", courseField));
        card.add(Box.createVerticalStrut(10));
        card.add(labeled("Section", sectionField));
        card.add(Box.createVerticalStrut(10));
        card.add(labeled("Birthday", birthdayPicker()));
        card.add(Box.createVerticalStrut(10));
        card.add(labeled("Organization / Department", departmentBox));
        card.add(Box.createVerticalStrut(16));

        JButton create = primaryButton("Register account", ActionIcon.CHECK);
        create.addActionListener(e -> {
            if (addRecordFromForm()) {
                clearCreateForm();
                cards.show(root, "dashboard");
            }
        });
        card.add(create);
        card.add(Box.createVerticalStrut(12));

        JButton back = ghostButton("Back to login", ActionIcon.BACK);
        back.addActionListener(e -> cards.show(root, "login"));
        card.add(back);
        card.add(Box.createVerticalStrut(18));

        page.add(card, centerConstraints());
        return scrollPageShell(page);
    }

    // Author: Group 3
    // Part: Main dashboard shell with sidebar, header, and content cards.
    private JPanel buildDashboardView() {
        dashboardPage = new JPanel(new BorderLayout());
        dashboardPage.setBackground(backgroundColor());

        sidebarPanel = buildSidebar();
        dashboardPage.add(sidebarPanel, BorderLayout.WEST);

        JPanel main = new JPanel(new BorderLayout(18, 18));
        main.setBackground(backgroundColor());
        main.setBorder(new EmptyBorder(22, 24, 24, 24));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Attendance Registry System");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(textColor());
        dashboardSubtitle = new JLabel("Log in to view today's attendance.");
        dashboardSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dashboardSubtitle.setForeground(mutedColor());

        JPanel titleStack = new JPanel();
        titleStack.setOpaque(false);
        titleStack.setLayout(new BoxLayout(titleStack, BoxLayout.Y_AXIS));
        titleStack.add(title);
        titleStack.add(dashboardSubtitle);

        clockLabel = new JLabel();
        clockLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        clockLabel.setForeground(textColor());
        clockLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        darkModeButton = ghostButton("Dark mode", ActionIcon.MOON);
        darkModeButton.addActionListener(e -> toggleDarkMode());
        JButton logout = dangerButton("Log out", ActionIcon.LOG_OUT);
        logout.addActionListener(e -> {
            currentUsername = null;
            currentUserIsAdmin = false;
            showingRecycleBin = false;
            clearTable();
            cards.show(root, "login");
        });

        JPanel headerActions = new JPanel();
        headerActions.setOpaque(false);
        headerActions.setLayout(new BoxLayout(headerActions, BoxLayout.Y_AXIS));
        clockLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        darkModeButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
        logout.setAlignmentX(Component.RIGHT_ALIGNMENT);
        headerActions.add(clockLabel);
        headerActions.add(Box.createVerticalStrut(8));
        headerActions.add(darkModeButton);
        headerActions.add(Box.createVerticalStrut(8));
        headerActions.add(logout);

        header.add(titleStack, BorderLayout.WEST);
        header.add(headerActions, BorderLayout.EAST);
        main.add(header, BorderLayout.NORTH);

        dashboardContent = new FadePanel(dashboardCards);
        dashboardContent.setOpaque(false);
        dashboardContent.add(buildDashboardPanel(), "dashboard");
        dashboardContent.add(buildTablePanel(), "attendance");
        dashboardContent.add(buildFullHistoryPanel(), "history");
        dashboardContent.add(buildProfilePanel(), "profile");
        main.add(dashboardContent, BorderLayout.CENTER);

        dashboardPage.add(main, BorderLayout.CENTER);
        return dashboardPage;
    }

    // Author: Group 3
    // Part: Sidebar navigation.
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(230, 10));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(24, 18, 24, 18));
        sidebar.setBackground(panelColor());

        JLabel brand = new JLabel("<html><b>GROUP 3</b><br>Registry</html>");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 18));
        brand.setForeground(textColor());
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(brand);
        sidebar.add(Box.createVerticalStrut(28));

        navDashboardButton = navButton("Dashboard", ActionIcon.DASHBOARD, "dashboard");
        navAttendanceButton = navButton("Attendance", ActionIcon.TABLE, "attendance");
        navHistoryButton = navButton("History", ActionIcon.SEARCH, "history");
        navProfileButton = navButton("Profile", ActionIcon.USER_PLUS, "profile");
        sidebar.add(navDashboardButton);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(navAttendanceButton);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(navHistoryButton);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(navProfileButton);
        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }

    private JButton navButton(String text, ActionIcon icon, String section) {
        JButton button = ghostButton(text, icon);
        button.addActionListener(e -> showDashboardSection(section));
        return button;
    }

    // Author: Group 3
    // Part: Dashboard overview cards and summary panels.
    private JPanel buildDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(18, 18));
        panel.setOpaque(false);

        JPanel metrics = new JPanel(new GridLayout(1, 5, 14, 0));
        metrics.setOpaque(false);
        totalUsersLabel = metricValueLabel("0");
        presentUsersLabel = metricValueLabel("0");
        absentUsersLabel = metricValueLabel("0");
        lateUsersLabel = metricValueLabel("0");
        activeSessionLabel = sessionValueLabel(formatSessionDate(LocalDate.now().toString()));
        metrics.add(metricCard("Total Users", totalUsersLabel, ACCENT));
        metrics.add(metricCard("Present Today", presentUsersLabel, new Color(37, 99, 235)));
        metrics.add(metricCard("Absent Today", absentUsersLabel, DANGER));
        metrics.add(metricCard("Late Today", lateUsersLabel, new Color(202, 138, 4)));
        metrics.add(metricCard("Current Active Session", activeSessionLabel, ACCENT_DARK));
        panel.add(metrics, BorderLayout.NORTH);

        JPanel lower = new JPanel(new GridLayout(1, 2, 18, 0));
        lower.setOpaque(false);
        lower.add(buildProfileSummaryPanel());
        lower.add(buildQuickAttendancePanel());
        panel.add(lower, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildProfileSummaryPanel() {
        JPanel panel = panelCard();
        panel.setLayout(new BorderLayout(0, 12));
        JLabel heading = new JLabel("Profile Management");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 20));
        heading.setForeground(textColor());
        JLabel body = new JLabel("<html>Open the Profile section to update account details. Admin users can add, edit, search, delete, and restore users from Attendance.</html>");
        body.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        body.setForeground(mutedColor());
        panel.add(heading, BorderLayout.NORTH);
        panel.add(body, BorderLayout.CENTER);
        return panel;
    }

    // Author: Group 3
    // Part: Dashboard attendance overview, calendar, and history table.
    private JPanel buildQuickAttendancePanel() {
        JPanel panel = panelCard();
        panel.setLayout(new BorderLayout(0, 12));
        JLabel heading = new JLabel("Today's Attendance Overview");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 20));
        heading.setForeground(textColor());
        JLabel body = new JLabel("<html>Attendance status updates automatically from today's time-in records. Duplicate time-in and time-out actions are blocked.</html>");
        body.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        body.setForeground(mutedColor());
        JButton openTable = primaryButton("Open attendance history", ActionIcon.SEARCH);
        openTable.addActionListener(e -> showDashboardSection("history"));
        JButton manageToday = ghostButton("Manage today", ActionIcon.CHECK);
        manageToday.addActionListener(e -> {
            selectedCalendarDate = LocalDate.now();
            visibleCalendarMonth = YearMonth.now();
            refreshAttendanceCalendar();
            showDashboardSection("attendance");
        });

        JPanel overviewContent = new JPanel(new BorderLayout(0, 12));
        overviewContent.setOpaque(false);
        overviewContent.add(body, BorderLayout.NORTH);
        overviewContent.add(buildAttendanceCalendarPanel(), BorderLayout.CENTER);

        JPanel actions = new JPanel(new GridLayout(1, 2, 8, 0));
        actions.setOpaque(false);
        actions.add(manageToday);
        actions.add(openTable);

        panel.add(heading, BorderLayout.NORTH);
        panel.add(overviewContent, BorderLayout.CENTER);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildFullHistoryPanel() {
        JPanel panel = panelCard();
        panel.setLayout(new BorderLayout(0, 14));
        panel.add(buildAttendanceHistoryFilters(), BorderLayout.NORTH);
        panel.add(buildAttendanceHistoryPanel(), BorderLayout.CENTER);
        return panel;
    }

    // Author: Group 3
    // Part: Attendance history search and filter controls.
    private JPanel buildAttendanceHistoryFilters() {
        JPanel filters = new JPanel(new BorderLayout(12, 0));
        filters.setOpaque(false);

        JLabel heading = new JLabel("Attendance History Viewer");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 20));
        heading.setForeground(textColor());

        historySearchField = field("Search name, ID, course...");
        historyDateField = field("Date yyyy-mm-dd or May 24, 2026");
        historyStatusFilter = new JComboBox<>(new String[]{"All statuses", "Present", "Late", "Absent", "Completed", "Timed in"});
        styleComboBox(historyStatusFilter);

        JButton applyFilters = primaryButton("Filter", ActionIcon.SEARCH);
        applyFilters.addActionListener(e -> refreshAttendanceHistory());
        JButton clearFilters = ghostButton("Clear", ActionIcon.BACK);
        clearFilters.addActionListener(e -> {
            historySearchField.setText("");
            historyDateField.setText("");
            historyStatusFilter.setSelectedIndex(0);
            refreshAttendanceHistory();
        });
        historySearchField.addActionListener(e -> refreshAttendanceHistory());
        historyDateField.addActionListener(e -> refreshAttendanceHistory());
        historyStatusFilter.addActionListener(e -> refreshAttendanceHistory());

        JPanel controls = new JPanel(new GridLayout(1, 5, 8, 0));
        controls.setOpaque(false);
        controls.add(historySearchField);
        controls.add(historyDateField);
        controls.add(historyStatusFilter);
        controls.add(applyFilters);
        controls.add(clearFilters);

        filters.add(heading, BorderLayout.WEST);
        filters.add(controls, BorderLayout.CENTER);
        return filters;
    }

    // Author: Group 3
    // Part: Calendar view for attendance sessions by day.
    private JPanel buildAttendanceCalendarPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        JPanel calendarHeader = new JPanel(new BorderLayout(8, 0));
        calendarHeader.setOpaque(false);
        JButton previousMonth = ghostButton("<", ActionIcon.BACK);
        previousMonth.addActionListener(e -> {
            visibleCalendarMonth = visibleCalendarMonth.minusMonths(1);
            refreshAttendanceCalendar();
        });
        JButton nextMonth = ghostButton(">", ActionIcon.PLUS);
        nextMonth.addActionListener(e -> {
            visibleCalendarMonth = visibleCalendarMonth.plusMonths(1);
            refreshAttendanceCalendar();
        });
        JButton todayButton = primaryButton("Today", ActionIcon.CHECK);
        todayButton.addActionListener(e -> {
            selectedCalendarDate = LocalDate.now();
            visibleCalendarMonth = YearMonth.now();
            refreshAttendanceCalendar();
        });
        calendarMonthLabel = new JLabel();
        calendarMonthLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        calendarMonthLabel.setForeground(textColor());
        calendarMonthLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel calendarActions = new JPanel(new GridLayout(1, 2, 6, 0));
        calendarActions.setOpaque(false);
        calendarActions.add(todayButton);
        calendarActions.add(nextMonth);

        calendarHeader.add(previousMonth, BorderLayout.WEST);
        calendarHeader.add(calendarMonthLabel, BorderLayout.CENTER);
        calendarHeader.add(calendarActions, BorderLayout.EAST);

        JPanel calendarShell = new JPanel(new BorderLayout(0, 6));
        calendarShell.setOpaque(false);
        JPanel weekdayHeader = new JPanel(new GridLayout(1, 7, 4, 0));
        weekdayHeader.setOpaque(false);
        for (String day : new String[]{"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"}) {
            JLabel dayLabel = new JLabel(day, SwingConstants.CENTER);
            dayLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
            dayLabel.setForeground(mutedColor());
            weekdayHeader.add(dayLabel);
        }
        calendarDaysPanel = new JPanel(new GridLayout(0, 7, 4, 4));
        calendarDaysPanel.setOpaque(false);
        calendarShell.add(weekdayHeader, BorderLayout.NORTH);
        calendarShell.add(calendarDaysPanel, BorderLayout.CENTER);

        JPanel detailPanel = new JPanel(new BorderLayout(0, 8));
        detailPanel.setOpaque(false);
        selectedDateLabel = new JLabel();
        selectedDateLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        selectedDateLabel.setForeground(textColor());
        selectedDateStatsLabel = new JLabel();
        selectedDateStatsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        selectedDateStatsLabel.setForeground(mutedColor());

        JPanel selectedSummary = new JPanel();
        selectedSummary.setOpaque(false);
        selectedSummary.setLayout(new BoxLayout(selectedSummary, BoxLayout.Y_AXIS));
        selectedSummary.add(selectedDateLabel);
        selectedSummary.add(selectedDateStatsLabel);

        styleCalendarDayTable();
        JScrollPane selectedScroll = new JScrollPane(calendarDayTable);
        selectedScroll.setBorder(BorderFactory.createLineBorder(borderColor()));
        selectedScroll.setPreferredSize(new Dimension(0, 132));
        detailPanel.add(selectedSummary, BorderLayout.NORTH);
        detailPanel.add(selectedScroll, BorderLayout.CENTER);

        panel.add(calendarHeader, BorderLayout.NORTH);
        panel.add(calendarShell, BorderLayout.CENTER);
        panel.add(detailPanel, BorderLayout.SOUTH);
        refreshAttendanceCalendar();
        return panel;
    }

    private JPanel metricCard(String title, JLabel valueLabel, Color accent) {
        JPanel card = panelCard();
        card.setLayout(new BorderLayout(0, 8));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLabel.setForeground(mutedColor());
        valueLabel.setForeground(accent);
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private JLabel metricValueLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 34));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JLabel sessionValueLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 18));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    // Author: Group 3
    // Part: Profile details panel.
    private JPanel buildProfilePanel() {
        JPanel panel = panelCard();
        panel.setPreferredSize(new Dimension(280, 10));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel heading = new JLabel("Profile");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 22));
        heading.setForeground(textColor());
        panel.add(heading);
        panel.add(Box.createVerticalStrut(18));

        profileNameLabel = detailLabel("Name: -");
        profileIdLabel = detailLabel("Student ID: -");
        profileCourseLabel = detailLabel("Course: -");
        profileSectionLabel = detailLabel("Section: -");
        profileDepartmentLabel = detailLabel("Department: -");
        profileAttendanceLabel = detailLabel("Today: -");

        panel.add(profileNameLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(profileIdLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(profileCourseLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(profileSectionLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(profileDepartmentLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(profileAttendanceLabel);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    // Author: Group 3
    // Part: Active records table and admin attendance controls.
    private JPanel buildTablePanel() {
        JPanel panel = panelCard();
        panel.setLayout(new BorderLayout(0, 14));

        JPanel tools = new JPanel(new BorderLayout(12, 0));
        tools.setOpaque(false);
        recordsHeading = new JLabel("Records");
        recordsHeading.setFont(new Font("Segoe UI", Font.BOLD, 22));
        recordsHeading.setForeground(INK);

        searchField = field("Search ID, name, course...");
        searchField.addActionListener(e -> applySearch());
        JButton search = primaryButton("Search", ActionIcon.SEARCH);
        search.addActionListener(e -> applySearch());

        JPanel searchGroup = new JPanel(new BorderLayout(8, 0));
        searchGroup.setOpaque(false);
        searchGroup.setPreferredSize(new Dimension(360, 40));
        searchGroup.add(searchField, BorderLayout.CENTER);
        searchGroup.add(search, BorderLayout.EAST);

        timeInSelectedButton = primaryButton("Time in selected", ActionIcon.CHECK);
        timeInSelectedButton.addActionListener(e -> timeInSelectedRecord());
        timeOutSelectedButton = primaryButton("Time out selected", ActionIcon.CHECK);
        timeOutSelectedButton.addActionListener(e -> timeOutSelectedRecord());
        addUserButton = primaryButton("Add user", ActionIcon.USER_PLUS);
        addUserButton.addActionListener(e -> addUserFromDashboard());
        editSelectedButton = ghostButton("Edit selected", ActionIcon.EDIT);
        editSelectedButton.addActionListener(e -> editSelectedRecord());
        editProfileButton = ghostButton("Edit profile", ActionIcon.EDIT);
        editProfileButton.addActionListener(e -> editCurrentProfile());
        deleteSelectedButton = dangerButton("Delete selected", ActionIcon.TRASH);
        deleteSelectedButton.addActionListener(e -> deleteSelectedRecord());
        recycleBinButton = ghostButton("Recycle bin", ActionIcon.TRASH);
        recycleBinButton.addActionListener(e -> {
            showingRecycleBin = true;
            refreshDashboard();
        });
        restoreSelectedButton = primaryButton("Restore selected", ActionIcon.BACK);
        restoreSelectedButton.addActionListener(e -> restoreSelectedRecord());
        activeRecordsButton = ghostButton("Active records", ActionIcon.BACK);
        activeRecordsButton.addActionListener(e -> {
            showingRecycleBin = false;
            refreshDashboard();
        });

        JPanel adminActions = new JPanel(new GridLayout(0, 3, 8, 8));
        adminActions.setOpaque(false);
        adminActions.add(addUserButton);
        adminActions.add(editSelectedButton);
        adminActions.add(editProfileButton);
        adminActions.add(timeInSelectedButton);
        adminActions.add(timeOutSelectedButton);
        adminActions.add(deleteSelectedButton);
        adminActions.add(recycleBinButton);
        adminActions.add(restoreSelectedButton);
        adminActions.add(activeRecordsButton);

        JPanel rightTools = new JPanel(new BorderLayout(0, 8));
        rightTools.setOpaque(false);
        rightTools.add(searchGroup, BorderLayout.NORTH);
        rightTools.add(adminActions, BorderLayout.SOUTH);

        tools.add(recordsHeading, BorderLayout.WEST);
        tools.add(rightTools, BorderLayout.EAST);
        panel.add(tools, BorderLayout.NORTH);

        registryTable.setRowSorter(sorter);
        registryTable.setFillsViewportHeight(true);
        registryTable.setRowHeight(36);
        registryTable.setShowGrid(false);
        registryTable.setIntercellSpacing(new Dimension(0, 0));
        registryTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        registryTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        registryTable.getTableHeader().setBackground(new Color(226, 232, 240));
        registryTable.getTableHeader().setForeground(INK);
        registryTable.setSelectionBackground(new Color(187, 247, 208));
        registryTable.setSelectionForeground(INK);
        registryTable.setDefaultRenderer(Object.class, new ZebraRenderer());

        JScrollPane scroll = new JScrollPane(registryTable);
        scroll.setBorder(BorderFactory.createLineBorder(borderColor()));

        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // Author: Group 3
    // Part: Attendance records table component.
    private JPanel buildAttendanceHistoryPanel() {
        JPanel historyPanel = new JPanel(new BorderLayout(0, 10));
        historyPanel.setOpaque(false);

        JLabel historyHeading = new JLabel("Attendance Records by Date");
        historyHeading.setFont(new Font("Segoe UI", Font.BOLD, 16));
        historyHeading.setForeground(textColor());

        attendanceHistoryTable.setFillsViewportHeight(true);
        attendanceHistoryTable.setRowHeight(32);
        attendanceHistoryTable.setShowGrid(false);
        attendanceHistoryTable.setIntercellSpacing(new Dimension(0, 0));
        attendanceHistoryTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        attendanceHistoryTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        attendanceHistoryTable.getTableHeader().setBackground(new Color(226, 232, 240));
        attendanceHistoryTable.getTableHeader().setForeground(INK);
        attendanceHistoryTable.setDefaultRenderer(Object.class, new ZebraRenderer());

        JScrollPane historyScroll = new JScrollPane(attendanceHistoryTable);
        historyScroll.setBorder(BorderFactory.createLineBorder(borderColor()));
        historyScroll.setPreferredSize(new Dimension(0, 420));

        historyPanel.add(historyHeading, BorderLayout.NORTH);
        historyPanel.add(historyScroll, BorderLayout.CENTER);
        return historyPanel;
    }

    private void styleCalendarDayTable() {
        calendarDayTable.setFillsViewportHeight(true);
        calendarDayTable.setRowHeight(28);
        calendarDayTable.setShowGrid(false);
        calendarDayTable.setIntercellSpacing(new Dimension(0, 0));
        calendarDayTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        calendarDayTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        calendarDayTable.getTableHeader().setBackground(new Color(226, 232, 240));
        calendarDayTable.getTableHeader().setForeground(INK);
        calendarDayTable.setDefaultRenderer(Object.class, new ZebraRenderer());
    }

    // Author: Group 3
    // Part: Calendar rendering and selected-day summary refresh.
    private void refreshAttendanceCalendar() {
        if (calendarDaysPanel == null || calendarMonthLabel == null) {
            return;
        }
        calendarMonthLabel.setText(visibleCalendarMonth.format(MONTH_FORMATTER));
        calendarDaysPanel.removeAll();

        LocalDate firstDay = visibleCalendarMonth.atDay(1);
        int leadingBlankDays = firstDay.getDayOfWeek().getValue() % 7;
        int totalCells = leadingBlankDays + visibleCalendarMonth.lengthOfMonth();
        int paddedCells = totalCells <= 35 ? 35 : 42;

        for (int i = 0; i < leadingBlankDays; i++) {
            calendarDaysPanel.add(blankCalendarCell());
        }
        for (int day = 1; day <= visibleCalendarMonth.lengthOfMonth(); day++) {
            LocalDate date = visibleCalendarMonth.atDay(day);
            calendarDaysPanel.add(calendarDateButton(date));
        }
        for (int i = totalCells; i < paddedCells; i++) {
            calendarDaysPanel.add(blankCalendarCell());
        }

        refreshSelectedCalendarDate();
        calendarDaysPanel.revalidate();
        calendarDaysPanel.repaint();
    }

    private Component blankCalendarCell() {
        JPanel blank = new JPanel();
        blank.setOpaque(false);
        blank.setPreferredSize(new Dimension(44, 38));
        return blank;
    }

    private JButton calendarDateButton(LocalDate date) {
        DayStats stats = dayStats(date);
        JButton button = new FlatButton("<html><center>" + date.getDayOfMonth()
                + "<br><span style='font-size:9px'>" + stats.present() + "</span></center></html>");
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setFont(new Font("Segoe UI", Font.BOLD, 11));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(date.equals(selectedCalendarDate) ? INK : borderColor()),
                new EmptyBorder(4, 4, 4, 4)
        ));
        button.setBackground(calendarDateColor(date, stats));
        button.setForeground(INK);
        button.setToolTipText(formatDisplayDate(date.toString()) + ": "
                + stats.present() + " present, " + stats.late() + " late, " + stats.absent() + " absent");
        button.addActionListener(e -> {
            selectedCalendarDate = date;
            refreshAttendanceCalendar();
        });
        return button;
    }

    private Color calendarDateColor(LocalDate date, DayStats stats) {
        if (date.isAfter(LocalDate.now())) {
            return panelColor();
        }
        if (stats.absent() > 0) {
            return new Color(254, 202, 202);
        }
        if (stats.late() > 0) {
            return new Color(254, 240, 138);
        }
        if (stats.present() > 0 || stats.total() > 0) {
            return new Color(187, 247, 208);
        }
        return panelColor();
    }

    private void refreshSelectedCalendarDate() {
        if (selectedDateLabel == null || selectedDateStatsLabel == null) {
            return;
        }
        DayStats stats = dayStats(selectedCalendarDate);
        selectedDateLabel.setText(formatDisplayDate(selectedCalendarDate.toString()) + " Attendance");
        selectedDateStatsLabel.setText("Present: " + stats.present()
                + " | Late: " + stats.late()
                + " | Absent: " + stats.absent()
                + " | Total: " + stats.total());

        calendarDayTableModel.setRowCount(0);
        for (StudentRecord student : visibleStudentsForCalendar()) {
            AttendanceRecord attendance = findAttendanceRecord(student.username(), selectedCalendarDate.toString());
            String timeIn = attendance == null ? "" : attendance.timeIn();
            String timeOut = attendance == null ? "" : attendance.timeOut();
            calendarDayTableModel.addRow(new Object[]{
                    formatDisplayDate(selectedCalendarDate.toString()),
                    student.id(),
                    student.name(),
                    student.course(),
                    student.section(),
                    student.department(),
                    selectedDateStatus(attendance),
                    displayValue(timeIn),
                    displayValue(timeOut)
            });
        }
    }

    private String selectedDateStatus(AttendanceRecord attendance) {
        if (attendance == null || attendance.timeIn().isEmpty()) {
            return selectedCalendarDate.isAfter(LocalDate.now()) ? "Not started" : "Absent";
        }
        if (isLate(attendance.timeIn())) {
            return attendance.timeOut().isEmpty() ? "Late - timed in" : "Late - completed";
        }
        return attendance.timeOut().isEmpty() ? "Timed in" : "Completed";
    }

    private DayStats dayStats(LocalDate date) {
        List<StudentRecord> visibleStudents = visibleStudentsForCalendar();
        if (date.isAfter(LocalDate.now())) {
            return new DayStats(visibleStudents.size(), 0, 0, 0);
        }
        int present = 0;
        int late = 0;
        for (StudentRecord student : visibleStudents) {
            AttendanceRecord attendance = findAttendanceRecord(student.username(), date.toString());
            if (attendance != null && !attendance.timeIn().isEmpty()) {
                present++;
                if (isLate(attendance.timeIn())) {
                    late++;
                }
            }
        }
        return new DayStats(visibleStudents.size(), present, late, Math.max(0, visibleStudents.size() - present));
    }

    private List<StudentRecord> visibleStudentsForCalendar() {
        if (currentUserIsAdmin || currentUsername == null) {
            return records;
        }
        StudentRecord currentRecord = findRecordByUsername(currentUsername);
        if (currentRecord == null) {
            return List.of();
        }
        return List.of(currentRecord);
    }

    private AttendanceRecord findAttendanceRecord(String username, String date) {
        for (AttendanceRecord record : attendanceRecords) {
            if (record.username().equals(username) && record.date().equals(date)) {
                return record;
            }
        }
        return null;
    }

    private boolean isLate(String timeText) {
        try {
            return LocalTime.parse(timeText, TIME_FORMATTER).isAfter(LATE_TIME);
        } catch (DateTimeException ex) {
            return false;
        }
    }

    // Author: Group 3
    // Part: Login validation and dashboard entry.
    private void handleLogin(ActionEvent event) {
        String username = loginIdField.getText().trim();
        String password = new String(loginPasswordField.getPassword());
        if (username.isEmpty() || password.isEmpty()) {
            showMessage("Enter your username and password.");
            return;
        }
        if (ADMIN_USERNAME.equals(username) && ADMIN_PASSWORD.equals(password)) {
            currentUsername = ADMIN_USERNAME;
            currentUserIsAdmin = true;
            showingRecycleBin = false;
            loginPasswordField.setText("");
            refreshDashboard();
            cards.show(root, "dashboard");
            return;
        }
        StudentRecord record = findRecordByLogin(username, password);
        if (record == null) {
            showMessage("Account not found. Check your username and password.");
            return;
        }
        currentUsername = record.username();
        currentUserIsAdmin = false;
        showingRecycleBin = false;
        refreshDashboard();
        loginPasswordField.setText("");
        cards.show(root, "dashboard");
    }

    // Author: Group 3
    // Part: Registration form validation and user creation.
    private boolean addRecordFromForm() {
        String username = usernameField.getText().trim();
        String password = new String(registerPasswordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showMessage("Please enter a username and password.");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            showMessage("Passwords do not match.");
            return false;
        }
        if (ADMIN_USERNAME.equalsIgnoreCase(username)) {
            showMessage("The admin username is reserved.");
            return false;
        }
        if (findRecordByUsername(username) != null) {
            showMessage("Username is already registered.");
            return false;
        }
        StudentRecord record;
        try {
            record = new StudentRecord(
                    username,
                    password,
                    idField.getText().trim(),
                    nameField.getText().trim(),
                    courseField.getText().trim(),
                    sectionField.getText().trim(),
                    selectedBirthday(),
                    String.valueOf(departmentBox.getSelectedItem()),
                    "",
                    "",
                    ""
            );
        } catch (DateTimeException ex) {
            showMessage("Please choose a valid birthday.");
            return false;
        }
        if (!validateRecord(record, true)) {
            return false;
        }
        records.add(record);
        ensureAttendanceRecordForToday(record);
        currentUsername = record.username();
        currentUserIsAdmin = false;
        showingRecycleBin = false;
        refreshDashboard();
        saveRecords();
        return true;
    }

    private boolean validateRecord(StudentRecord record, boolean requireDate) {
        if (record.id().isEmpty() || record.name().isEmpty() || record.course().isEmpty()
                || record.section().isEmpty() || record.birthday().isEmpty()) {
            showMessage("Please complete all fields.");
            return false;
        }
        if (requireDate) {
            try {
                selectedBirthdayDate();
            } catch (DateTimeException ex) {
                showMessage("Please choose a valid birthday.");
                return false;
            }
        }
        return true;
    }

    // Author: Group 3
    // Part: Add/edit/profile management dialog.
    private StudentRecord showUserForm(String title, StudentRecord existing) {
        JTextField formUsername = field("username");
        JPasswordField formPassword = passwordField();
        JTextField formId = field("2026-0001");
        JTextField formName = field("Student name");
        JTextField formCourse = field("BSIT");
        JTextField formSection = field("A");
        JComboBox<Integer> formYear = new JComboBox<>(yearOptions());
        JComboBox<Integer> formDay = new JComboBox<>(dayOptions());
        JComboBox<String> formMonth = new JComboBox<>(monthOptions());
        JComboBox<String> formDepartment = new JComboBox<>(departmentOptions());
        styleComboBox(formYear);
        styleComboBox(formDay);
        styleComboBox(formMonth);
        styleComboBox(formDepartment);

        if (existing != null) {
            formUsername.setText(existing.username());
            formPassword.setText(existing.password());
            formId.setText(existing.id());
            formName.setText(existing.name());
            formCourse.setText(existing.course());
            formSection.setText(existing.section());
            selectBirthday(existing.birthday(), formYear, formDay, formMonth);
            formDepartment.setSelectedItem(existing.department());
        }

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(8, 8, 8, 8));
        form.add(labeled("Username", formUsername));
        form.add(Box.createVerticalStrut(8));
        form.add(labeled("Password", formPassword));
        form.add(Box.createVerticalStrut(8));
        form.add(labeled("Student ID", formId));
        form.add(Box.createVerticalStrut(8));
        form.add(labeled("Student Name", formName));
        form.add(Box.createVerticalStrut(8));
        form.add(labeled("Course", formCourse));
        form.add(Box.createVerticalStrut(8));
        form.add(labeled("Section", formSection));
        form.add(Box.createVerticalStrut(8));
        form.add(labeled("Birthday", birthdayPicker(formYear, formDay, formMonth)));
        form.add(Box.createVerticalStrut(8));
        form.add(labeled("Organization / Department", formDepartment));

        while (true) {
            int result = JOptionPane.showConfirmDialog(
                    this,
                    form,
                    title,
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );
            if (result != JOptionPane.OK_OPTION) {
                return null;
            }

            String username = formUsername.getText().trim();
            String password = new String(formPassword.getPassword());
            if (username.isEmpty() || password.isEmpty()) {
                showMessage("Please enter a username and password.");
                continue;
            }
            if (ADMIN_USERNAME.equalsIgnoreCase(username)) {
                showMessage("The admin username is reserved.");
                continue;
            }

            StudentRecord record;
            try {
                record = new StudentRecord(
                        username,
                        password,
                        formId.getText().trim(),
                        formName.getText().trim(),
                        formCourse.getText().trim(),
                        formSection.getText().trim(),
                        selectedBirthday(formYear, formDay, formMonth),
                        String.valueOf(formDepartment.getSelectedItem()),
                        existing == null ? "" : existing.lastAttendanceDate(),
                        existing == null ? "" : existing.timeIn(),
                        existing == null ? "" : existing.timeOut()
                );
            } catch (DateTimeException ex) {
                showMessage("Please choose a valid birthday.");
                continue;
            }

            if (record.id().isEmpty() || record.name().isEmpty() || record.course().isEmpty()
                    || record.section().isEmpty() || record.birthday().isEmpty()) {
                showMessage("Please complete all fields.");
                continue;
            }
            return record;
        }
    }

    private void selectBirthday(String birthday, JComboBox<Integer> yearBox, JComboBox<Integer> dayBox,
                                JComboBox<String> monthBox) {
        try {
            LocalDate date = LocalDate.parse(birthday);
            yearBox.setSelectedItem(date.getYear());
            dayBox.setSelectedItem(date.getDayOfMonth());
            monthBox.setSelectedIndex(date.getMonthValue() - 1);
        } catch (DateTimeException ex) {
            yearBox.setSelectedItem(LocalDate.now().getYear());
            dayBox.setSelectedItem(1);
            monthBox.setSelectedIndex(0);
        }
    }

    private boolean isUsernameAvailable(String username, String currentUsernameToIgnore) {
        if (ADMIN_USERNAME.equalsIgnoreCase(username)) {
            return false;
        }
        for (StudentRecord record : records) {
            if (record.username().equals(username) && !record.username().equals(currentUsernameToIgnore)) {
                return false;
            }
        }
        return true;
    }

    // Author: Group 3
    // Part: Active student table row rendering.
    private void appendRecord(StudentRecord record) {
        tableModel.addRow(new Object[]{
                tableModel.getRowCount() + 1,
                record.id(),
                record.name(),
                record.course(),
                record.section(),
                record.birthday(),
                record.department(),
                attendanceText(record),
                displayValue(todayTimeIn(record)),
                displayValue(todayTimeOut(record))
        });
    }

    // Author: Group 3
    // Part: User management actions for delete, add, edit, and restore.
    private void deleteSelectedRecord() {
        if (!currentUserIsAdmin) {
            showMessage("Only the admin can delete records.");
            return;
        }
        if (showingRecycleBin) {
            showMessage("Open active records to move a student to the recycle bin.");
            return;
        }
        int viewRow = registryTable.getSelectedRow();
        if (viewRow < 0) {
            showMessage("Select a record first.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete the selected record?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        int modelRow = registryTable.convertRowIndexToModel(viewRow);
        if (modelRow < 0 || modelRow >= records.size()) {
            showMessage("Selected record was not found.");
            return;
        }
        StudentRecord deletedRecord = records.remove(modelRow);
        deletedRecords.add(deletedRecord);
        saveRecords();
        refreshDashboard();
    }

    private void addUserFromDashboard() {
        if (!currentUserIsAdmin) {
            showMessage("Only the admin can add users.");
            return;
        }
        StudentRecord record = showUserForm("Add User", null);
        if (record == null) {
            return;
        }
        if (!isUsernameAvailable(record.username(), null)) {
            showMessage("Username is already registered.");
            return;
        }
        records.add(record);
        ensureAttendanceRecordForToday(record);
        saveRecords();
        refreshDashboard();
    }

    private void editSelectedRecord() {
        if (!currentUserIsAdmin) {
            showMessage("Only the admin can edit selected records.");
            return;
        }
        if (showingRecycleBin) {
            showMessage("Restore the student before editing their profile.");
            return;
        }
        int viewRow = registryTable.getSelectedRow();
        if (viewRow < 0) {
            showMessage("Select a record first.");
            return;
        }
        int modelRow = registryTable.convertRowIndexToModel(viewRow);
        if (modelRow < 0 || modelRow >= records.size()) {
            showMessage("Selected record was not found.");
            return;
        }
        StudentRecord existing = records.get(modelRow);
        StudentRecord updated = showUserForm("Edit User", existing);
        if (updated == null) {
            return;
        }
        if (!isUsernameAvailable(updated.username(), existing.username())) {
            showMessage("Username is already registered.");
            return;
        }
        records.set(modelRow, updated);
        if (existing.username().equals(currentUsername)) {
            currentUsername = updated.username();
        }
        saveRecords();
        refreshDashboard();
    }

    private void editCurrentProfile() {
        if (currentUserIsAdmin) {
            return;
        }
        StudentRecord existing = findRecordByUsername(currentUsername);
        if (existing == null) {
            showMessage("Profile was not found.");
            return;
        }
        StudentRecord updated = showUserForm("Edit Profile", existing);
        if (updated == null) {
            return;
        }
        if (!isUsernameAvailable(updated.username(), existing.username())) {
            showMessage("Username is already registered.");
            return;
        }
        int recordIndex = records.indexOf(existing);
        if (recordIndex < 0) {
            showMessage("Profile was not found.");
            return;
        }
        records.set(recordIndex, updated);
        currentUsername = updated.username();
        saveRecords();
        refreshDashboard();
    }

    // Author: Group 3
    // Part: Attendance time-in action with duplicate prevention.
    private void timeInSelectedRecord() {
        if (!currentUserIsAdmin) {
            showMessage("Only the admin can update selected records.");
            return;
        }
        if (showingRecycleBin) {
            showMessage("Restore the student before marking attendance.");
            return;
        }
        int viewRow = registryTable.getSelectedRow();
        if (viewRow < 0) {
            showMessage("Select a record first.");
            return;
        }
        int modelRow = registryTable.convertRowIndexToModel(viewRow);
        if (modelRow < 0 || modelRow >= records.size()) {
            showMessage("Selected record was not found.");
            return;
        }
        StudentRecord record = records.get(modelRow);
        if (isPresentToday(record) && !record.timeIn().isEmpty()) {
            showMessage(record.name() + " is already timed in today.");
            return;
        }
        StudentRecord updatedRecord = record.withTimeIn(LocalDate.now().toString(), currentTimeText());
        records.set(modelRow, updatedRecord);
        recordAttendanceTimeIn(updatedRecord);
        saveRecords();
        refreshDashboard();
        showDashboardSection("attendance");
        selectRecordInTable(updatedRecord.username());
    }

    // Author: Group 3
    // Part: Attendance time-out action with same-day validation.
    private void timeOutSelectedRecord() {
        if (!currentUserIsAdmin) {
            showMessage("Only the admin can update selected records.");
            return;
        }
        if (showingRecycleBin) {
            showMessage("Restore the student before recording time out.");
            return;
        }
        int viewRow = registryTable.getSelectedRow();
        if (viewRow < 0) {
            showMessage("Select a record first.");
            return;
        }
        int modelRow = registryTable.convertRowIndexToModel(viewRow);
        if (modelRow < 0 || modelRow >= records.size()) {
            showMessage("Selected record was not found.");
            return;
        }
        StudentRecord record = records.get(modelRow);
        if (!LocalDate.now().toString().equals(record.lastAttendanceDate()) || record.timeIn().isEmpty()) {
            showMessage("Record time in first before recording time out.");
            return;
        }
        if (!record.timeOut().isEmpty()) {
            showMessage(record.name() + " is already timed out today.");
            return;
        }
        StudentRecord updatedRecord = record.withTimeOut(currentTimeText());
        records.set(modelRow, updatedRecord);
        recordAttendanceTimeOut(updatedRecord);
        saveRecords();
        refreshDashboard();
        showDashboardSection("attendance");
        selectRecordInTable(updatedRecord.username());
    }

    private void selectRecordInTable(String username) {
        List<StudentRecord> visibleRecords = showingRecycleBin ? deletedRecords : records;
        for (int modelRow = 0; modelRow < visibleRecords.size() && modelRow < tableModel.getRowCount(); modelRow++) {
            if (visibleRecords.get(modelRow).username().equals(username)) {
                int viewRow = registryTable.convertRowIndexToView(modelRow);
                if (viewRow >= 0) {
                    registryTable.setRowSelectionInterval(viewRow, viewRow);
                    registryTable.scrollRectToVisible(registryTable.getCellRect(viewRow, 0, true));
                }
                return;
            }
        }
    }

    private void restoreSelectedRecord() {
        if (!currentUserIsAdmin) {
            showMessage("Only the admin can restore records.");
            return;
        }
        if (!showingRecycleBin) {
            showMessage("Open the recycle bin to restore a student.");
            return;
        }
        int viewRow = registryTable.getSelectedRow();
        if (viewRow < 0) {
            showMessage("Select a record first.");
            return;
        }
        int modelRow = registryTable.convertRowIndexToModel(viewRow);
        if (modelRow < 0 || modelRow >= deletedRecords.size()) {
            showMessage("Selected record was not found.");
            return;
        }
        StudentRecord restoredRecord = deletedRecords.get(modelRow);
        if (findRecordByUsername(restoredRecord.username()) != null) {
            showMessage("An active student already uses this username.");
            return;
        }
        deletedRecords.remove(modelRow);
        records.add(restoredRecord);
        ensureAttendanceRecordForToday(restoredRecord);
        saveRecords();
        refreshDashboard();
    }

    // Author: Group 3
    // Part: Search/filter behavior for active records.
    private void applySearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            sorter.setRowFilter(null);
            return;
        }
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(query)));
    }

    // Author: Group 3
    // Part: Student dashboard refresh.
    private void refreshDashboard() {
        synchronizeDailyAttendanceSession();
        if (currentUserIsAdmin) {
            refreshAdminDashboard();
            return;
        }
        StudentRecord record = findRecordByUsername(currentUsername);
        clearTable();
        if (record == null) {
            return;
        }
        appendRecord(record);
        updateDashboardStats();
        showDashboardSection("dashboard");
        if (dashboardSubtitle != null) {
            dashboardSubtitle.setText("Today's attendance for " + record.name() + " - " + LocalDate.now());
        }
        if (profileNameLabel != null) {
            profileNameLabel.setText("Name: " + record.name());
            profileIdLabel.setText("Student ID: " + record.id());
            profileCourseLabel.setText("Course: " + record.course());
            profileSectionLabel.setText("Section: " + record.section());
            profileDepartmentLabel.setText("Department: " + record.department());
            profileAttendanceLabel.setText("Today: " + attendanceSummary(record));
        }
        if (timeInSelectedButton != null) {
            timeInSelectedButton.setVisible(false);
        }
        if (timeOutSelectedButton != null) {
            timeOutSelectedButton.setVisible(false);
        }
        if (addUserButton != null) {
            addUserButton.setVisible(false);
        }
        if (editSelectedButton != null) {
            editSelectedButton.setVisible(false);
        }
        if (editProfileButton != null) {
            editProfileButton.setVisible(true);
        }
        if (deleteSelectedButton != null) {
            deleteSelectedButton.setVisible(false);
        }
        if (recycleBinButton != null) {
            recycleBinButton.setVisible(false);
        }
        if (restoreSelectedButton != null) {
            restoreSelectedButton.setVisible(false);
        }
        if (activeRecordsButton != null) {
            activeRecordsButton.setVisible(false);
        }
        if (recordsHeading != null) {
            recordsHeading.setText("Records");
        }
        sorter.setRowFilter(null);
        if (searchField != null) {
            searchField.setText("");
        }
    }

    // Author: Group 3
    // Part: Admin dashboard refresh.
    private void refreshAdminDashboard() {
        synchronizeDailyAttendanceSession();
        clearTable();
        List<StudentRecord> visibleRecords = showingRecycleBin ? deletedRecords : records;
        for (StudentRecord record : visibleRecords) {
            appendRecord(record);
        }
        updateDashboardStats();
        showDashboardSection(showingRecycleBin ? "attendance" : "dashboard");
        if (recordsHeading != null) {
            recordsHeading.setText(showingRecycleBin ? "Recycle Bin" : "Records");
        }
        if (dashboardSubtitle != null) {
            String viewName = showingRecycleBin ? "deleted records" : "active records";
            dashboardSubtitle.setText("Admin control panel - " + viewName + " - " + LocalDate.now());
        }
        if (profileNameLabel != null) {
            profileNameLabel.setText("Name: Administrator");
            profileIdLabel.setText("Account: admin");
            profileCourseLabel.setText("Access: Full system control");
            profileSectionLabel.setText("Active: " + records.size() + " | Recycle Bin: " + deletedRecords.size());
            profileDepartmentLabel.setText("Department: System Admin");
            profileAttendanceLabel.setText(showingRecycleBin
                    ? "Recycle Bin: Restore returned students"
                    : "Today: Manage time in and time out for all users");
        }
        if (timeInSelectedButton != null) {
            timeInSelectedButton.setVisible(!showingRecycleBin);
        }
        if (timeOutSelectedButton != null) {
            timeOutSelectedButton.setVisible(!showingRecycleBin);
        }
        if (addUserButton != null) {
            addUserButton.setVisible(!showingRecycleBin);
        }
        if (editSelectedButton != null) {
            editSelectedButton.setVisible(!showingRecycleBin);
        }
        if (editProfileButton != null) {
            editProfileButton.setVisible(false);
        }
        if (deleteSelectedButton != null) {
            deleteSelectedButton.setVisible(!showingRecycleBin);
        }
        if (recycleBinButton != null) {
            recycleBinButton.setVisible(!showingRecycleBin);
        }
        if (restoreSelectedButton != null) {
            restoreSelectedButton.setVisible(showingRecycleBin);
        }
        if (activeRecordsButton != null) {
            activeRecordsButton.setVisible(showingRecycleBin);
        }
        sorter.setRowFilter(null);
        if (searchField != null) {
            searchField.setText("");
        }
    }

    // Author: Group 3
    // Part: Sidebar navigation and fade transitions.
    private void showDashboardSection(String section) {
        if (dashboardContent == null) {
            return;
        }
        activeDashboardSection = section;
        dashboardCards.show(dashboardContent, section);
        dashboardContent.fadeIn();
        updateNavigationState(section);
    }

    private void updateNavigationState(String activeSection) {
        styleNavButton(navDashboardButton, "dashboard".equals(activeSection));
        styleNavButton(navAttendanceButton, "attendance".equals(activeSection));
        styleNavButton(navHistoryButton, "history".equals(activeSection));
        styleNavButton(navProfileButton, "profile".equals(activeSection));
    }

    private void styleNavButton(JButton button, boolean active) {
        if (button == null) {
            return;
        }
        button.setBackground(active ? ACCENT : panelColor());
        button.setForeground(active ? Color.WHITE : textColor());
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(active ? ACCENT : borderColor()),
                new EmptyBorder(10, 14, 10, 14)
        ));
        Icon icon = button.getIcon();
        if (icon instanceof LineIcon lineIcon) {
            lineIcon.setColor(button.getForeground());
        }
        button.repaint();
    }

    // Author: Group 3
    // Part: Dark mode toggle and theme application.
    private void toggleDarkMode() {
        darkMode = !darkMode;
        if (darkModeButton != null) {
            darkModeButton.setText(darkMode ? "Light mode" : "Dark mode");
            darkModeButton.setIcon(new LineIcon(darkMode ? ActionIcon.SUN : ActionIcon.MOON, darkModeButton.getForeground()));
        }
        applyTheme();
    }

    private void applyTheme() {
        if (dashboardPage != null) {
            applyTheme(dashboardPage);
        }
        if (registryTable != null) {
            styleTable(registryTable);
        }
        if (attendanceHistoryTable != null) {
            styleTable(attendanceHistoryTable);
        }
        if (calendarDayTable != null) {
            styleTable(calendarDayTable);
        }
        updateNavigationState(activeDashboardSection);
        SwingUtilities.updateComponentTreeUI(root);
        root.repaint();
    }

    private void styleTable(JTable table) {
        table.setBackground(panelColor());
        table.setForeground(textColor());
        table.getTableHeader().setBackground(darkMode ? new Color(51, 65, 85) : new Color(226, 232, 240));
        table.getTableHeader().setForeground(textColor());
        table.setSelectionBackground(darkMode ? new Color(20, 83, 45) : new Color(187, 247, 208));
        table.setSelectionForeground(textColor());
    }

    private void applyTheme(Component component) {
        if (component instanceof JPanel panel) {
            panel.setBackground(Boolean.TRUE.equals(panel.getClientProperty("cardPanel")) || panel == sidebarPanel
                    ? panelColor()
                    : backgroundColor());
        }
        if (component instanceof JLabel label) {
            label.setForeground(textColor());
        }
        if (component instanceof JTextField textField) {
            textField.setBackground(panelColor());
            textField.setForeground(textColor());
            textField.setCaretColor(textColor());
            textField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(borderColor()),
                    new EmptyBorder(9, 10, 9, 10)
            ));
        }
        if (component instanceof JPasswordField passwordField) {
            passwordField.setBackground(panelColor());
            passwordField.setForeground(textColor());
            passwordField.setCaretColor(textColor());
        }
        if (component instanceof JComboBox<?> comboBox) {
            comboBox.setBackground(panelColor());
            comboBox.setForeground(textColor());
        }
        if (component instanceof JScrollPane scrollPane) {
            scrollPane.getViewport().setBackground(panelColor());
            scrollPane.setBorder(BorderFactory.createLineBorder(borderColor()));
        }
        if (component instanceof java.awt.Container container) {
            for (Component child : container.getComponents()) {
                applyTheme(child);
            }
        }
    }

    private Color backgroundColor() {
        return darkMode ? DARK_SURFACE : SURFACE;
    }

    private Color panelColor() {
        return darkMode ? DARK_PANEL : PANEL;
    }

    private Color textColor() {
        return darkMode ? DARK_INK : INK;
    }

    private Color mutedColor() {
        return darkMode ? DARK_MUTED : MUTED;
    }

    private Color borderColor() {
        return darkMode ? new Color(71, 85, 105) : new Color(226, 232, 240);
    }

    // Author: Group 3
    // Part: Table refresh coordination for records, history, and calendar.
    private void clearTable() {
        tableModel.setRowCount(0);
        refreshAttendanceHistory();
        refreshAttendanceCalendar();
    }

    // Author: Group 3
    // Part: Attendance history filtering and grouped display by date.
    private void refreshAttendanceHistory() {
        attendanceTableModel.setRowCount(0);

        List<AttendanceRecord> visibleAttendanceRecords = new ArrayList<>();
        for (AttendanceRecord record : attendanceRecords) {
            if (!currentUserIsAdmin && !record.username().equals(currentUsername)) {
                continue;
            }
            if (!matchesHistoryFilters(record)) {
                continue;
            }
            visibleAttendanceRecords.add(record);
        }
        visibleAttendanceRecords.sort(Comparator
                .comparing(AttendanceRecord::date)
                .reversed()
                .thenComparing(AttendanceRecord::name));

        String currentSessionDate = null;
        boolean todaySessionShown = false;
        String today = LocalDate.now().toString();
        for (AttendanceRecord record : visibleAttendanceRecords) {
            if (!record.date().equals(currentSessionDate)) {
                addAttendanceSessionRow(record.date());
                currentSessionDate = record.date();
                if (today.equals(record.date())) {
                    todaySessionShown = true;
                }
            }
            addAttendanceRecordRow(record);
        }
        if (!todaySessionShown && !historyFiltersActive()) {
            attendanceTableModel.insertRow(0, attendanceSessionRow(today));
        }
    }

    private boolean historyFiltersActive() {
        boolean hasSearch = historySearchField != null && !historySearchField.getText().trim().isEmpty();
        boolean hasDate = historyDateField != null && !historyDateField.getText().trim().isEmpty();
        boolean hasStatus = historyStatusFilter != null && historyStatusFilter.getSelectedIndex() > 0;
        return hasSearch || hasDate || hasStatus;
    }

    private boolean matchesHistoryFilters(AttendanceRecord record) {
        String query = historySearchField == null ? "" : historySearchField.getText().trim().toLowerCase();
        if (!query.isEmpty() && !(record.id().toLowerCase().contains(query)
                || record.name().toLowerCase().contains(query)
                || record.course().toLowerCase().contains(query)
                || record.section().toLowerCase().contains(query)
                || record.department().toLowerCase().contains(query)
                || record.date().toLowerCase().contains(query)
                || formatDisplayDate(record.date()).toLowerCase().contains(query))) {
            return false;
        }

        String dateQuery = historyDateField == null ? "" : historyDateField.getText().trim().toLowerCase();
        if (!dateQuery.isEmpty() && !(record.date().toLowerCase().contains(dateQuery)
                || formatDisplayDate(record.date()).toLowerCase().contains(dateQuery)
                || formatSessionDate(record.date()).toLowerCase().contains(dateQuery))) {
            return false;
        }

        String statusFilter = historyStatusFilter == null ? "All statuses" : String.valueOf(historyStatusFilter.getSelectedItem());
        if (!"All statuses".equals(statusFilter)) {
            String status = attendanceRecordStatus(record).toLowerCase();
            return switch (statusFilter) {
                case "Present" -> !record.timeIn().isEmpty() && !isLate(record.timeIn());
                case "Late" -> !record.timeIn().isEmpty() && isLate(record.timeIn());
                case "Absent" -> record.timeIn().isEmpty();
                default -> status.contains(statusFilter.toLowerCase());
            };
        }
        return true;
    }

    private void addAttendanceSessionRow(String date) {
        attendanceTableModel.addRow(attendanceSessionRow(date));
    }

    private Object[] attendanceSessionRow(String date) {
        return new Object[]{
                formatSessionDate(date) + " Attendance",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                ""
        };
    }

    private void addAttendanceRecordRow(AttendanceRecord record) {
        attendanceTableModel.addRow(new Object[]{
                formatDisplayDate(record.date()),
                record.id(),
                record.name(),
                record.course(),
                record.section(),
                record.department(),
                attendanceRecordStatus(record),
                displayValue(record.timeIn()),
                displayValue(record.timeOut())
        });
    }

    private String formatSessionDate(String date) {
        try {
            return LocalDate.parse(date).format(SESSION_DATE_FORMATTER);
        } catch (DateTimeException ex) {
            return date;
        }
    }

    private String formatDisplayDate(String date) {
        try {
            return LocalDate.parse(date).format(SESSION_DATE_FORMATTER);
        } catch (DateTimeException ex) {
            return date;
        }
    }

    // Author: Group 3
    // Part: Daily attendance session reset and session setup.
    private void resetActiveRecordsForToday() {
        String today = LocalDate.now().toString();
        for (int i = 0; i < records.size(); i++) {
            StudentRecord record = records.get(i);
            if (!record.lastAttendanceDate().isEmpty() && !today.equals(record.lastAttendanceDate())) {
                records.set(i, record.withAttendanceReset());
            }
        }
    }

    private void ensureTodayAttendanceSession() {
        for (StudentRecord record : records) {
            ensureAttendanceRecordForToday(record);
        }
    }

    private void ensureAttendanceRecordForToday(StudentRecord student) {
        String today = LocalDate.now().toString();
        if (findAttendanceRecordIndex(student.username(), today) >= 0) {
            return;
        }
        attendanceRecords.add(new AttendanceRecord(
                today,
                student.username(),
                student.id(),
                student.name(),
                student.course(),
                student.section(),
                student.department(),
                "",
                ""
        ));
    }

    private StudentRecord findRecordByLogin(String username, String password) {
        for (StudentRecord record : records) {
            if (record.username().equals(username) && record.password().equals(password)) {
                return record;
            }
        }
        return null;
    }

    private StudentRecord findRecordByUsername(String username) {
        for (StudentRecord record : records) {
            if (record.username().equals(username)) {
                return record;
            }
        }
        return null;
    }

    private String attendanceText(StudentRecord record) {
        if (isPresentToday(record)) {
            String presentText = isLate(record.timeIn()) ? "Late" : "Present";
            return record.timeOut().isEmpty() ? presentText + " - timed in" : presentText + " - completed";
        }
        return "Absent today";
    }

    private String attendanceSummary(StudentRecord record) {
        return attendanceText(record) + " | In: " + displayValue(todayTimeIn(record))
                + " | Out: " + displayValue(todayTimeOut(record));
    }

    private boolean isPresentToday(StudentRecord record) {
        return LocalDate.now().toString().equals(record.lastAttendanceDate()) && !record.timeIn().isEmpty();
    }

    private String todayTimeIn(StudentRecord record) {
        return isPresentToday(record) ? record.timeIn() : "";
    }

    private String todayTimeOut(StudentRecord record) {
        return isPresentToday(record) ? record.timeOut() : "";
    }

    private String attendanceRecordStatus(AttendanceRecord record) {
        if (record.timeIn().isEmpty()) {
            return "Absent";
        }
        if (isLate(record.timeIn())) {
            return record.timeOut().isEmpty() ? "Late - timed in" : "Late - completed";
        }
        return record.timeOut().isEmpty() ? "Timed in" : "Completed";
    }

    // Author: Group 3
    // Part: Attendance history write-through for time-in/time-out.
    private void recordAttendanceTimeIn(StudentRecord record) {
        String today = LocalDate.now().toString();
        int index = findAttendanceRecordIndex(record.username(), today);
        AttendanceRecord attendanceRecord = new AttendanceRecord(
                today,
                record.username(),
                record.id(),
                record.name(),
                record.course(),
                record.section(),
                record.department(),
                record.timeIn(),
                ""
        );
        if (index >= 0) {
            attendanceRecords.set(index, attendanceRecord);
        } else {
            attendanceRecords.add(attendanceRecord);
        }
    }

    private void recordAttendanceTimeOut(StudentRecord record) {
        String today = LocalDate.now().toString();
        int index = findAttendanceRecordIndex(record.username(), today);
        if (index >= 0) {
            AttendanceRecord existing = attendanceRecords.get(index);
            attendanceRecords.set(index, new AttendanceRecord(
                    existing.date(),
                    existing.username(),
                    existing.id(),
                    existing.name(),
                    existing.course(),
                    existing.section(),
                    existing.department(),
                    existing.timeIn(),
                    record.timeOut()
            ));
            return;
        }
        attendanceRecords.add(new AttendanceRecord(
                today,
                record.username(),
                record.id(),
                record.name(),
                record.course(),
                record.section(),
                record.department(),
                record.timeIn(),
                record.timeOut()
        ));
    }

    private int findAttendanceRecordIndex(String username, String date) {
        for (int i = 0; i < attendanceRecords.size(); i++) {
            AttendanceRecord record = attendanceRecords.get(i);
            if (record.username().equals(username) && record.date().equals(date)) {
                return i;
            }
        }
        return -1;
    }

    private int presentUsersCount() {
        int count = 0;
        for (StudentRecord record : records) {
            if (isPresentToday(record)) {
                count++;
            }
        }
        return count;
    }

    private int lateUsersCount() {
        int count = 0;
        for (StudentRecord record : records) {
            if (isPresentToday(record) && isLate(record.timeIn())) {
                count++;
            }
        }
        return count;
    }

    // Author: Group 3
    // Part: Dashboard statistics and live clock.
    private void updateDashboardStats() {
        int present = presentUsersCount();
        int late = lateUsersCount();
        int total = records.size();
        int absent = Math.max(0, total - present);
        if (totalUsersLabel != null) {
            totalUsersLabel.setText(String.valueOf(total));
            presentUsersLabel.setText(String.valueOf(present));
            absentUsersLabel.setText(String.valueOf(absent));
            lateUsersLabel.setText(String.valueOf(late));
            activeSessionLabel.setText(formatSessionDate(LocalDate.now().toString()));
        }
    }

    private void updateClockLabel() {
        if (!LocalDate.now().equals(activeAttendanceDate)) {
            startNewDailyAttendanceSession();
        }
        if (clockLabel != null) {
            clockLabel.setText(LocalDate.now().format(DATE_FORMATTER) + " | " + currentTimeText());
        }
    }

    // Author: Group 3
    // Part: Automatic day-change detection for a fresh attendance session.
    private void synchronizeDailyAttendanceSession() {
        if (!LocalDate.now().equals(activeAttendanceDate)) {
            activeAttendanceDate = LocalDate.now();
        }
        resetActiveRecordsForToday();
        ensureTodayAttendanceSession();
        saveRecords();
    }

    private void startNewDailyAttendanceSession() {
        synchronizeDailyAttendanceSession();
        if (currentUsername != null || currentUserIsAdmin) {
            refreshDashboard();
        }
    }

    private String currentTimeText() {
        return LocalTime.now().format(TIME_FORMATTER);
    }

    private String displayValue(String value) {
        return value == null || value.isEmpty() ? "-" : value;
    }

    // Author: Group 3
    // Part: Data loading from TSV files.
    private void loadRecords() {
        readRecords(DATA_FILE, records);
        readRecords(DELETED_DATA_FILE, deletedRecords);
        readAttendanceRecords();
        migrateExistingAttendanceRecords();
        resetActiveRecordsForToday();
        ensureTodayAttendanceSession();
        saveRecords();
    }

    private void readRecords(Path file, List<StudentRecord> targetRecords) {
        if (!Files.exists(file)) {
            return;
        }
        try {
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            for (String line : lines) {
                String[] parts = line.split("\t", -1);
                if (parts.length == 11) {
                    targetRecords.add(new StudentRecord(
                            parts[0], parts[1], parts[2], parts[3], parts[4],
                            parts[5], parts[6], parts[7], parts[8], parts[9], parts[10]
                    ));
                } else if (parts.length == 9) {
                    targetRecords.add(new StudentRecord(
                            parts[0], parts[1], parts[2], parts[3], parts[4],
                            parts[5], parts[6], parts[7], parts[8], "", ""
                    ));
                } else if (parts.length == 6) {
                    targetRecords.add(new StudentRecord(
                            parts[0], "", parts[0], parts[1], parts[2],
                            parts[3], parts[4], parts[5], "", "", ""
                    ));
                }
            }
        } catch (IOException ex) {
            showMessage("Could not load saved records.");
        }
    }

    // Author: Group 3
    // Part: Data saving to TSV files.
    private void saveRecords() {
        writeRecords(DATA_FILE, records);
        writeRecords(DELETED_DATA_FILE, deletedRecords);
        writeAttendanceRecords();
    }

    private void writeRecords(Path file, List<StudentRecord> sourceRecords) {
        try {
            Files.createDirectories(file.getParent());
            List<String> lines = new ArrayList<>();
            for (StudentRecord record : sourceRecords) {
                lines.add(String.join("\t",
                        record.username(),
                        record.password(),
                        record.id(),
                        record.name(),
                        record.course(),
                        record.section(),
                        record.birthday(),
                        record.department(),
                        record.lastAttendanceDate(),
                        record.timeIn(),
                        record.timeOut()
                ));
            }
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            showMessage("Could not save records.");
        }
    }

    private void readAttendanceRecords() {
        if (!Files.exists(ATTENDANCE_FILE)) {
            return;
        }
        try {
            List<String> lines = Files.readAllLines(ATTENDANCE_FILE, StandardCharsets.UTF_8);
            for (String line : lines) {
                String[] parts = line.split("\t", -1);
                if (parts.length == 9) {
                    attendanceRecords.add(new AttendanceRecord(
                            parts[0], parts[1], parts[2], parts[3], parts[4],
                            parts[5], parts[6], parts[7], parts[8]
                    ));
                }
            }
        } catch (IOException ex) {
            showMessage("Could not load attendance records.");
        }
    }

    private void migrateExistingAttendanceRecords() {
        for (StudentRecord record : records) {
            if (!record.lastAttendanceDate().isEmpty() && !record.timeIn().isEmpty()
                    && findAttendanceRecordIndex(record.username(), record.lastAttendanceDate()) < 0) {
                attendanceRecords.add(new AttendanceRecord(
                        record.lastAttendanceDate(),
                        record.username(),
                        record.id(),
                        record.name(),
                        record.course(),
                        record.section(),
                        record.department(),
                        record.timeIn(),
                        record.timeOut()
                ));
            }
        }
    }

    private void writeAttendanceRecords() {
        try {
            Files.createDirectories(ATTENDANCE_FILE.getParent());
            List<String> lines = new ArrayList<>();
            for (AttendanceRecord record : attendanceRecords) {
                lines.add(String.join("\t",
                        record.date(),
                        record.username(),
                        record.id(),
                        record.name(),
                        record.course(),
                        record.section(),
                        record.department(),
                        record.timeIn(),
                        record.timeOut()
                ));
            }
            Files.write(ATTENDANCE_FILE, lines, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            showMessage("Could not save attendance records.");
        }
    }

    private void renumberRows() {
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            tableModel.setValueAt(row + 1, row, 0);
        }
    }

    private String valueAt(int row, int column) {
        Object value = tableModel.getValueAt(row, column);
        return value == null ? "" : value.toString();
    }

    // Author: Group 3
    // Part: Form reset and reusable UI builders.
    private void clearCreateForm() {
        usernameField.setText("");
        registerPasswordField.setText("");
        confirmPasswordField.setText("");
        idField.setText("");
        nameField.setText("");
        courseField.setText("");
        sectionField.setText("");
        birthYearBox.setSelectedItem(LocalDate.now().getYear());
        birthDayBox.setSelectedItem(1);
        birthMonthBox.setSelectedIndex(0);
        departmentBox.setSelectedIndex(0);
    }

    private JPanel pageShell() {
        JPanel page = new JPanel(new GridBagLayout());
        page.setBackground(SURFACE);
        page.setBorder(new EmptyBorder(24, 32, 24, 32));
        return page;
    }

    private JPanel scrollPageShell(JPanel content) {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(SURFACE);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        scroll.getViewport().setBackground(SURFACE);

        outer.add(scroll, BorderLayout.CENTER);
        return outer;
    }

    private GridBagConstraints centerConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        return gbc;
    }

    private JPanel authCard(String titleText, String subtitleText) {
        JPanel card = panelCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(430, titleText.contains("Account") ? 960 : 390));

        JLabel badge = new JLabel("GROUP 3");
        badge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        badge.setForeground(ACCENT_DARK);
        badge.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel(titleText);
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        title.setForeground(INK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("<html>" + subtitleText + "</html>");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(badge);
        card.add(Box.createVerticalStrut(8));
        card.add(title);
        card.add(Box.createVerticalStrut(6));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(28));
        return card;
    }

    private JPanel panelCard() {
        JPanel panel = new JPanel();
        panel.putClientProperty("cardPanel", Boolean.TRUE);
        panel.setBackground(panelColor());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor()),
                new EmptyBorder(24, 24, 24, 24)
        ));
        return panel;
    }

    private JPanel labeled(String label, Component input) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel text = new JLabel(label);
        text.setFont(new Font("Segoe UI", Font.BOLD, 13));
        text.setForeground(textColor());
        panel.add(text, BorderLayout.NORTH);
        panel.add(input, BorderLayout.CENTER);
        return panel;
    }

    private JLabel detailLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setForeground(textColor());
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JLabel metricLabel(String text) {
        JLabel label = detailLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        return label;
    }

    private JTextField field(String placeholder) {
        JTextField field = new JTextField();
        field.setToolTipText(placeholder);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBackground(panelColor());
        field.setForeground(textColor());
        field.setCaretColor(textColor());
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor()),
                new EmptyBorder(9, 10, 9, 10)
        ));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        return field;
    }

    private JPanel birthdayPicker() {
        return birthdayPicker(birthYearBox, birthDayBox, birthMonthBox);
    }

    private JPanel birthdayPicker(JComboBox<Integer> yearBox, JComboBox<Integer> dayBox, JComboBox<String> monthBox) {
        JPanel picker = new JPanel(new GridBagLayout());
        picker.setOpaque(false);
        picker.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 8);

        gbc.gridx = 0;
        gbc.weightx = 1.1;
        picker.add(yearBox, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.8;
        picker.add(dayBox, gbc);

        gbc.gridx = 2;
        gbc.weightx = 1.1;
        gbc.insets = new Insets(0, 0, 0, 0);
        picker.add(monthBox, gbc);

        return picker;
    }

    private Integer[] yearOptions() {
        int currentYear = LocalDate.now().getYear();
        Integer[] years = new Integer[80];
        for (int i = 0; i < years.length; i++) {
            years[i] = currentYear - i;
        }
        return years;
    }

    private Integer[] dayOptions() {
        Integer[] days = new Integer[31];
        for (int i = 0; i < days.length; i++) {
            days[i] = i + 1;
        }
        return days;
    }

    private String[] monthOptions() {
        return new String[]{
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        };
    }

    private void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        comboBox.setBackground(panelColor());
        comboBox.setForeground(textColor());
        comboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
    }

    private String selectedBirthday() {
        return selectedBirthday(birthYearBox, birthDayBox, birthMonthBox);
    }

    private LocalDate selectedBirthdayDate() {
        return selectedBirthdayDate(birthYearBox, birthDayBox, birthMonthBox);
    }

    private String selectedBirthday(JComboBox<Integer> yearBox, JComboBox<Integer> dayBox, JComboBox<String> monthBox) {
        return selectedBirthdayDate(yearBox, dayBox, monthBox).toString();
    }

    private LocalDate selectedBirthdayDate(JComboBox<Integer> yearBox, JComboBox<Integer> dayBox,
                                           JComboBox<String> monthBox) {
        int year = (Integer) yearBox.getSelectedItem();
        int day = (Integer) dayBox.getSelectedItem();
        int month = monthBox.getSelectedIndex() + 1;
        return LocalDate.of(year, month, day);
    }

    private JPasswordField passwordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBackground(panelColor());
        field.setForeground(textColor());
        field.setCaretColor(textColor());
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor()),
                new EmptyBorder(9, 10, 9, 10)
        ));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        return field;
    }

    // Author: Group 3
    // Part: Button styles and message helpers.
    private JButton primaryButton(String text, ActionIcon icon) {
        JButton button = button(text, ACCENT, Color.WHITE, icon);
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(ACCENT_DARK);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(ACCENT);
            }
        });
        return button;
    }

    private JButton dangerButton(String text, ActionIcon icon) {
        return button(text, DANGER, Color.WHITE, icon);
    }

    private JButton ghostButton(String text, ActionIcon icon) {
        JButton button = button(text, Color.WHITE, ACCENT_DARK, icon);
        button.setBorder(BorderFactory.createLineBorder(new Color(187, 247, 208)));
        return button;
    }

    private JButton button(String text, Color background, Color foreground, ActionIcon icon) {
        JButton button = new FlatButton(text);
        button.setIcon(new LineIcon(icon, foreground));
        button.setIconTextGap(9);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBorder(new EmptyBorder(10, 14, 10, 14));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        return button;
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Attendance Registry", JOptionPane.INFORMATION_MESSAGE);
    }

    private String[] departmentOptions() {
        return new String[]{"IT Department", "Registrar", "Accounting", "Student Council", "Faculty", "Other"};
    }

    // Author: Group 3
    // Part: Data models for students, attendance, and day statistics.
    private record StudentRecord(String username, String password, String id, String name, String course,
                                 String section, String birthday, String department, String lastAttendanceDate,
                                 String timeIn, String timeOut) {
        private StudentRecord withTimeIn(String attendanceDate, String newTimeIn) {
            return new StudentRecord(username, password, id, name, course, section, birthday, department,
                    attendanceDate, newTimeIn, "");
        }

        private StudentRecord withTimeOut(String newTimeOut) {
            return new StudentRecord(username, password, id, name, course, section, birthday, department,
                    lastAttendanceDate, timeIn, newTimeOut);
        }

        private StudentRecord withAttendanceReset() {
            return new StudentRecord(username, password, id, name, course, section, birthday, department,
                    "", "", "");
        }
    }

    private record AttendanceRecord(String date, String username, String id, String name, String course,
                                    String section, String department, String timeIn, String timeOut) {
    }

    private record DayStats(int total, int present, int late, int absent) {
    }

    // Author: Group 3
    // Part: Built-in line icon types.
    private enum ActionIcon {
        LOG_IN,
        USER_PLUS,
        DASHBOARD,
        TABLE,
        CHECK,
        BACK,
        LOG_OUT,
        PLUS,
        EDIT,
        MOON,
        SUN,
        TRASH,
        SEARCH
    }

    // Author: Group 3
    // Part: Custom lightweight icon painter.
    private static class LineIcon implements Icon {
        private final ActionIcon type;
        private Color color;

        LineIcon(ActionIcon type, Color color) {
            this.type = type;
            this.color = color;
        }

        void setColor(Color color) {
            this.color = color;
        }

        @Override
        public int getIconWidth() {
            return 18;
        }

        @Override
        public int getIconHeight() {
            return 18;
        }

        @Override
        public void paintIcon(Component component, Graphics graphics, int x, int y) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(color);
            g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            int left = x + 2;
            int top = y + 2;
            int right = x + 16;
            int bottom = y + 16;
            int midX = x + 9;
            int midY = y + 9;

            switch (type) {
                case LOG_IN -> {
                    g.drawLine(left, midY, right - 4, midY);
                    g.drawLine(right - 7, midY - 4, right - 3, midY);
                    g.drawLine(right - 7, midY + 4, right - 3, midY);
                    g.drawLine(right, top, right, bottom);
                }
                case LOG_OUT -> {
                    g.drawLine(left + 4, midY, right, midY);
                    g.drawLine(right - 4, midY - 4, right, midY);
                    g.drawLine(right - 4, midY + 4, right, midY);
                    g.drawLine(left, top, left, bottom);
                }
                case USER_PLUS -> {
                    g.drawOval(left + 2, top, 6, 6);
                    g.drawArc(left, top + 8, 10, 8, 0, 180);
                    g.drawLine(right - 4, top + 3, right - 4, top + 11);
                    g.drawLine(right - 8, top + 7, right, top + 7);
                }
                case DASHBOARD -> {
                    g.drawRect(left + 1, top + 1, 5, 5);
                    g.drawRect(right - 6, top + 1, 5, 5);
                    g.drawRect(left + 1, bottom - 6, 5, 5);
                    g.drawRect(right - 6, bottom - 6, 5, 5);
                }
                case TABLE -> {
                    g.drawRect(left + 1, top + 2, 12, 12);
                    g.drawLine(left + 1, top + 6, right - 1, top + 6);
                    g.drawLine(left + 5, top + 2, left + 5, bottom - 2);
                    g.drawLine(left + 9, top + 2, left + 9, bottom - 2);
                }
                case CHECK -> {
                    g.drawLine(left + 1, midY, midX - 1, bottom - 3);
                    g.drawLine(midX - 1, bottom - 3, right - 1, top + 3);
                }
                case BACK -> {
                    g.drawLine(right, midY, left + 1, midY);
                    g.drawLine(left + 5, midY - 4, left + 1, midY);
                    g.drawLine(left + 5, midY + 4, left + 1, midY);
                }
                case PLUS -> {
                    g.drawLine(midX, top + 2, midX, bottom - 2);
                    g.drawLine(left + 2, midY, right - 2, midY);
                }
                case EDIT -> {
                    g.drawLine(left + 3, bottom - 2, right - 2, top + 3);
                    g.drawLine(right - 5, top + 2, right - 1, top + 6);
                    g.drawLine(left + 2, bottom - 1, left + 6, bottom - 2);
                    g.drawLine(left + 2, bottom - 1, left + 3, bottom - 5);
                }
                case MOON -> {
                    g.drawArc(left + 2, top + 1, 11, 13, 80, 250);
                    g.drawArc(left + 6, top + 1, 8, 13, 90, -245);
                }
                case SUN -> {
                    g.drawOval(left + 4, top + 4, 6, 6);
                    g.drawLine(midX, top, midX, top + 2);
                    g.drawLine(midX, bottom - 2, midX, bottom);
                    g.drawLine(left, midY, left + 2, midY);
                    g.drawLine(right - 2, midY, right, midY);
                }
                case TRASH -> {
                    g.drawLine(left + 3, top + 4, right - 3, top + 4);
                    g.drawLine(left + 6, top + 1, right - 6, top + 1);
                    g.drawRect(left + 4, top + 6, 8, 9);
                    g.drawLine(left + 7, top + 8, left + 7, bottom - 2);
                    g.drawLine(right - 7, top + 8, right - 7, bottom - 2);
                }
                case SEARCH -> {
                    g.drawOval(left + 1, top + 1, 9, 9);
                    g.drawLine(left + 9, top + 9, right - 1, bottom - 1);
                }
            }
            g.dispose();
        }
    }

    // Author: Group 3
    // Part: Table row renderer and animation/button helpers.
    private static class ZebraRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                                                       boolean focused, int row, int column) {
            Component component = super.getTableCellRendererComponent(table, value, selected, focused, row, column);
            setHorizontalAlignment(column == 0 ? SwingConstants.CENTER : SwingConstants.LEFT);
            boolean sessionRow = isSessionRow(table, row);
            setFont(table.getFont().deriveFont(sessionRow ? Font.BOLD : Font.PLAIN));
            if (!selected) {
                Color base = table.getBackground();
                component.setBackground(sessionRow ? sessionBackground(base) : row % 2 == 0 ? base : slightlyDifferent(base));
                component.setForeground(table.getForeground());
            }
            setBorder(new EmptyBorder(0, 10, 0, 10));
            return component;
        }

        private boolean isSessionRow(JTable table, int row) {
            Object idValue = table.getValueAt(row, 1);
            Object dateValue = table.getValueAt(row, 0);
            return (idValue == null || idValue.toString().isEmpty())
                    && dateValue != null
                    && dateValue.toString().endsWith(" Attendance");
        }

        private Color sessionBackground(Color color) {
            int delta = color.getRed() < 80 ? 28 : -14;
            return new Color(
                    Math.max(0, Math.min(255, color.getRed() + delta)),
                    Math.max(0, Math.min(255, color.getGreen() + delta)),
                    Math.max(0, Math.min(255, color.getBlue() + delta))
            );
        }

        private Color slightlyDifferent(Color color) {
            int delta = color.getRed() < 80 ? 14 : -6;
            return new Color(
                    Math.max(0, Math.min(255, color.getRed() + delta)),
                    Math.max(0, Math.min(255, color.getGreen() + delta)),
                    Math.max(0, Math.min(255, color.getBlue() + delta))
            );
        }
    }

    private static class FadePanel extends JPanel {
        private float alpha = 1f;
        private Timer fadeTimer;

        FadePanel(CardLayout layout) {
            super(layout);
        }

        void fadeIn() {
            alpha = 0.35f;
            if (fadeTimer != null && fadeTimer.isRunning()) {
                fadeTimer.stop();
            }
            fadeTimer = new Timer(18, e -> {
                alpha = Math.min(1f, alpha + 0.08f);
                repaint();
                if (alpha >= 1f) {
                    ((Timer) e.getSource()).stop();
                }
            });
            fadeTimer.start();
        }

        @Override
        protected void paintChildren(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, alpha));
            super.paintChildren(g);
            g.dispose();
        }
    }

    private static class FlatButton extends JButton {
        FlatButton(String text) {
            super(text);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
            g.dispose();
            super.paintComponent(graphics);
        }
    }
}
