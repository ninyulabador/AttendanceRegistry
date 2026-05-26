import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AttendanceSystemFlow {

    public static void main(String[] args) {
        // Start the application flow
        showLoginScreen();
    }

    // ==========================================
    // NODE: Login Window -> Auth
    // ==========================================
    public static void showLoginScreen() {
        JFrame loginFrame = new JFrame("Admin Login");
        loginFrame.setSize(300, 150);
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setLayout(new FlowLayout());

        JLabel lblUser = new JLabel("Admin Name: (type 'admin')");
        JTextField txtUser = new JTextField(15);
        JButton btnLogin = new JButton("Login");

        loginFrame.add(lblUser);
        loginFrame.add(txtUser);
        loginFrame.add(btnLogin);

        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String user = txtUser.getText();
                // NODE: Auth {Check DB/List}
                if (user.equalsIgnoreCase("admin")) {
                    loginFrame.dispose(); // Close login
                    showMainApplication(); // Move to next screen
                } else {
                    JOptionPane.showMessageDialog(loginFrame, "Invalid Credentials!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        loginFrame.setLocationRelativeTo(null);
        loginFrame.setVisible(true);
    }

    // ==========================================
    // NODE: Main Application Window -> NavAtt
    // ==========================================
    public static void showMainApplication() {
        JFrame mainFrame = new JFrame("Attendance Registry System - Group 3");
        mainFrame.setSize(800, 500);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLayout(new BorderLayout());

        // Sidebar Panel (Navigation)
        JPanel sidebar = new JPanel(new GridLayout(4, 1, 10, 10));
        sidebar.setBackground(new Color(0, 104, 56)); // SJIT Green
        JButton btnDash = new JButton("Dashboard");
        JButton btnAtt = new JButton("Attendance");
        JButton btnHist = new JButton("History");
        
        sidebar.add(btnDash);
        sidebar.add(btnAtt);
        sidebar.add(btnHist);

        // Center Panel (Action Area)
        JPanel centerPanel = new JPanel();
        centerPanel.add(new JLabel("Active Records Table Placeholder..."));
        
        // Top Panel (Action Buttons - Add, Time In)
        JPanel topActions = new JPanel();
        JButton btnAddUser = new JButton("Add User");
        JButton btnTimeIn = new JButton("Time In Selected");

        // NODE: ActAdd -> PopAdd
        btnAddUser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Triggers JOptionPane as mapped in the flowchart
                String newStudent = JOptionPane.showInputDialog(mainFrame, "Enter New Student Name:");
                if (newStudent != null && !newStudent.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(mainFrame, newStudent + " added to ArrayList!");
                }
            }
        });

        // NODE: ActTime -> PopConfirm
        btnTimeIn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Triggers JOptionPane confirmation
                int confirm = JOptionPane.showConfirmDialog(mainFrame, "Mark selected student as Present?", "Confirm Time In", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    JOptionPane.showMessageDialog(mainFrame, "Timestamp Recorded!");
                }
            }
        });

        topActions.add(btnAddUser);
        topActions.add(btnTimeIn);

        // Add everything to the main frame
        mainFrame.add(sidebar, BorderLayout.WEST);
        mainFrame.add(topActions, BorderLayout.NORTH);
        mainFrame.add(centerPanel, BorderLayout.CENTER);

        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
    }
}