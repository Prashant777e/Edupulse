package userinterface;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class LoginPage extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleCombo;
    
    public LoginPage() {
        
        // Basic function for frame 
        setTitle("EduPulse - Login Window");
        setSize(450, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Title Label 
        JLabel titleLabel = new JLabel("EduPulse");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        mainPanel.add(titleLabel, gbc);
        
        // Username
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        mainPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        usernameField = new JTextField(15);
        mainPanel.add(usernameField, gbc);
        
        // Password
        gbc.gridy = 2;
        gbc.gridx = 0;
        mainPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        mainPanel.add(passwordField, gbc);
        
        // Role
        gbc.gridy = 3;
        gbc.gridx = 0;
        mainPanel.add(new JLabel("Login as:"), gbc);
        gbc.gridx = 1;
        String[] s = {"Student", "Faculty", "Admin"};
        roleCombo = new JComboBox<>(s);
        mainPanel.add(roleCombo, gbc);
        
        // Login button
        JButton loginButton = new JButton("Login");
        loginButton.setBackground(new Color(41, 128, 185));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        gbc.gridy = 4;
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(loginButton, gbc);
        
        // Register button
        JButton registerButton = new JButton("Register");
        registerButton.setBackground(new Color(46, 204, 113));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFocusPainted(false);
        gbc.gridy = 5;
        mainPanel.add(registerButton, gbc);
        
        loginButton.addActionListener(e -> login());
        registerButton.addActionListener(e -> openRegistration());
        
        add(mainPanel);
        
        // Test database connection on startup
        testConnection();
    }
    
    private void testConnection() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn != null) {
                System.out.println("Database connection test: SUCCESS");
            }
        } catch (Exception e) {
            System.err.println("Database connection test: FAILED");
        }
    }
    
    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String role = ((String) roleCombo.getSelectedItem()).toLowerCase();
        
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter username and password");
            return;
        }
        
        try {
            String sql = "SELECT id, role FROM users WHERE username = ? AND password = ? AND role = ?";
            PreparedStatement stmt = DatabaseConnection.prepare(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, role);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                int userId = rs.getInt("id");
                String userRole = rs.getString("role");
                dispose(); // Close login window
                
                // Open appropriate dashboard based on role
                if (userRole.equals("student")) {
                    // Get student name for dashboard title
                    String studentName = getStudentName(userId);
                    SwingUtilities.invokeLater(() -> {
                        new StudentDashboard(userId).setVisible(true);
                    });
                } else if (userRole.equals("faculty")) {
                    // Get faculty name for dashboard title
                    String facultyName = getFacultyName(userId);
                    SwingUtilities.invokeLater(() -> {
                        new FacultyDashboard(userId, facultyName).setVisible(true);
                    });
                } else if (userRole.equals("admin")) {
                    JOptionPane.showMessageDialog(this, "Admin dashboard coming soon!");
                    // new AdminDashboard().setVisible(true);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username, password, or role");
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Login error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private String getStudentName(int userId) {
        String name = "Student";
        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = "SELECT name FROM students WHERE user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                name = rs.getString("name");
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return name;
    }
    
    private String getFacultyName(int userId) {
        String name = "Faculty";
        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = "SELECT name FROM faculty WHERE user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                name = rs.getString("name");
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return name;
    }
    
    private void openRegistration() {
        new RegistrationPage().setVisible(true);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginPage().setVisible(true);
        });
    }
}