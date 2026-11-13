//The main Swing/AWT desktop application with Register and Login forms, direct JDBC queries, and a live user preview table.
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class auth_app extends JFrame {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/demo_db?allowPublicKeyRetrieval=true&useSSL=false";
    private static final String DB_USER = "root";
    private static final String DB_PASS = ""; // Adjust if local MySQL requires a password

    // UI Fields - Register
    private JTextField regUserField;
    private JPasswordField regPassField;
    private JComboBox<String> regRoleBox;

    // UI Fields - Login
    private JTextField loginUserField;
    private JPasswordField loginPassField;

    // Live Database Table
    private DefaultTableModel tableModel;
    private JTable userTable;

    public auth_app() {
        setTitle("AuthPortal - Java Swing & MySQL Demo");
        setSize(850, 480);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Top Header
        JLabel headerLabel = new JLabel("University AuthPortal (JDBC & Swing Demo)", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(12, 10, 5, 10));
        add(headerLabel, BorderLayout.NORTH);

        // Center Panels: Left (Forms), Right (Live Table)
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 15, 10));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));

        // Left Container for Register & Login boxes
        JPanel leftFormsPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        leftFormsPanel.add(buildRegisterPanel());
        leftFormsPanel.add(buildLoginPanel());
        centerPanel.add(leftFormsPanel);

        // Right Container for Database Record Viewer
        centerPanel.add(buildTablePanel());
        add(centerPanel, BorderLayout.CENTER);

        // Initial fetch from MySQL
        refreshTableData();
    }

    private JPanel buildRegisterPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Register New User (INSERT Query)"));

        regUserField = new JTextField();
        regPassField = new JPasswordField();
        regRoleBox = new JComboBox<>(new String[]{"student", "faculty", "admin"});
        JButton regBtn = new JButton("Register Account");

        panel.add(new JLabel("Username:"));
        panel.add(regUserField);
        panel.add(new JLabel("Password:"));
        panel.add(regPassField);
        panel.add(new JLabel("Role:"));
        panel.add(regRoleBox);
        panel.add(new JLabel("")); // Spacer
        panel.add(regBtn);

        regBtn.addActionListener(e -> handleRegister());
        return panel;
    }

    private JPanel buildLoginPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Login Verification (SELECT Query)"));

        loginUserField = new JTextField();
        loginPassField = new JPasswordField();
        JButton loginBtn = new JButton("Verify Credentials");

        panel.add(new JLabel("Username:"));
        panel.add(loginUserField);
        panel.add(new JLabel("Password:"));
        panel.add(loginPassField);
        panel.add(new JLabel("")); // Spacer
        panel.add(loginBtn);

        loginBtn.addActionListener(e -> handleLogin());
        return panel;
    }

    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Live MySQL Records (demo_db.users)"));

        String[] columns = {"ID", "Username", "Role", "Created At"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // read-only view
            }
        };

        userTable = new JTable(tableModel);
        userTable.setFillsViewportHeight(true);
        panel.add(new JScrollPane(userTable), BorderLayout.CENTER);

        JButton refreshBtn = new JButton("Refresh Table");
        refreshBtn.addActionListener(e -> refreshTableData());
        panel.add(refreshBtn, BorderLayout.SOUTH);

        return panel;
    }

    // ----------------- JDBC Operations -----------------

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    private void handleRegister() {
        String username = regUserField.getText().trim();
        String password = new String(regPassField.getPassword()).trim();
        String role = (String) regRoleBox.getSelectedItem();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all registration fields.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, role);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Registration Successful for user: " + username, "Success", JOptionPane.INFORMATION_MESSAGE);
                regUserField.setText("");
                regPassField.setText("");
                refreshTableData();
            }
        } catch (SQLIntegrityConstraintViolationException ex) {
            JOptionPane.showMessageDialog(this, "Username already exists in MySQL!", "Registration Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void handleLogin() {
        String username = loginUserField.getText().trim();
        String password = new String(loginPassField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "SELECT role FROM users WHERE username = ? AND password = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String role = rs.getString("role");
                    JOptionPane.showMessageDialog(this, "Login Successful!\nWelcome " + username + " (Role: " + role + ")", "Access Granted", JOptionPane.INFORMATION_MESSAGE);
                    loginUserField.setText("");
                    loginPassField.setText("");
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid username or password.", "Authentication Failed", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void refreshTableData() {
        tableModel.setRowCount(0);
        String sql = "SELECT id, username, role, created_at FROM users ORDER BY id ASC";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("role"),
                        rs.getString("created_at")
                });
            }
        } catch (SQLException ex) {
            System.err.println("Could not load table rows: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            auth_app app = new auth_app();
            app.setVisible(true);
        });
    }
}
