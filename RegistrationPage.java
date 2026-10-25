package userinterface;


import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class RegistrationPage extends JFrame {
    private JTextField usernameField, nameField, emailField, rollField, courseField;
    private JPasswordField passwordField, confirmField;
    private JComboBox<Integer> semesterCombo;
    
    public RegistrationPage() {
        setTitle("EduPulse - Student Registration");
        setSize(500, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        
        int row = 0;
        
        // Title
        JLabel title = new JLabel("Student Registration");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(41, 128, 185));
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        panel.add(title, gbc);
        
        // Username
        gbc.gridwidth = 1;
        gbc.gridy = row++;
        gbc.gridx = 0;
        panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        usernameField = new JTextField(20);
        panel.add(usernameField, gbc);
        
        // Password
        gbc.gridy = row++;
        gbc.gridx = 0;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        passwordField = new JPasswordField(20);
        panel.add(passwordField, gbc);
        
        // Confirm Password
        gbc.gridy = row++;
        gbc.gridx = 0;
        panel.add(new JLabel("Confirm Password:"), gbc);
        gbc.gridx = 1;
        confirmField = new JPasswordField(20);
        panel.add(confirmField, gbc);
        
        // Full Name
        gbc.gridy = row++;
        gbc.gridx = 0;
        panel.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1;
        nameField = new JTextField(20);
        panel.add(nameField, gbc);
        
        // Email
        gbc.gridy = row++;
        gbc.gridx = 0;
        panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        emailField = new JTextField(20);
        panel.add(emailField, gbc);
        
        // Roll Number
        gbc.gridy = row++;
        gbc.gridx = 0;
        panel.add(new JLabel("Roll Number:"), gbc);
        gbc.gridx = 1;
        rollField = new JTextField(20);
        panel.add(rollField, gbc);
        
        // Course
        gbc.gridy = row++;
        gbc.gridx = 0;
        panel.add(new JLabel("Course:"), gbc);
        gbc.gridx = 1;
        courseField = new JTextField(20);
        panel.add(courseField, gbc);
        
        // Semester
        gbc.gridy = row++;
        gbc.gridx = 0;
        panel.add(new JLabel("Semester:"), gbc);
        gbc.gridx = 1;
        semesterCombo = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5, 6, 7, 8});
        panel.add(semesterCombo, gbc);
        
        // Register button
        JButton registerBtn = new JButton("Register");
        registerBtn.setBackground(new Color(46, 204, 113));
        registerBtn.setForeground(Color.WHITE);
        registerBtn.setFocusPainted(false);
        gbc.gridy = row++;
        gbc.gridx = 1;
        panel.add(registerBtn, gbc);
        
        registerBtn.addActionListener(e -> registerStudent());
        
        add(panel);
    }
    
    private void registerStudent() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirm = new String(confirmField.getPassword());
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String rollNumber = rollField.getText().trim();
        String course = courseField.getText().trim();
        int semester = (Integer) semesterCombo.getSelectedItem();
        
        // Validation
        if (username.isEmpty() || password.isEmpty() || name.isEmpty() || rollNumber.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all required fields");
            return;
        }
        
        if (!password.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match");
            return;
        }
        
        if (password.length() < 4) {
            JOptionPane.showMessageDialog(this, "Password must be at least 4 characters");
            return;
        }
        
        Connection conn = null;
        try {
            // Get connection
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            // Insert into users table with RETURN_GENERATED_KEYS
            String userSql = "INSERT INTO users (username, password, role) VALUES (?, ?, 'student')";
            PreparedStatement userStmt = conn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS);
            userStmt.setString(1, username);
            userStmt.setString(2, password);
            int userRows = userStmt.executeUpdate();
            
            if (userRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }
            
            // Get the generated user_id
            ResultSet generatedKeys = userStmt.getGeneratedKeys();
            int userId = -1;
            if (generatedKeys.next()) {
                userId = generatedKeys.getInt(1);
            } else {
                throw new SQLException("Creating user failed, no ID obtained.");
            }
            generatedKeys.close();
            userStmt.close();
            
            // Insert into students table
            String studentSql = "INSERT INTO students (user_id, name, email, roll_number, course, semester, energy_points) " +
                               "VALUES (?, ?, ?, ?, ?, ?, 0)";
            PreparedStatement studentStmt = conn.prepareStatement(studentSql);
            studentStmt.setInt(1, userId);
            studentStmt.setString(2, name);
            studentStmt.setString(3, email.isEmpty() ? null : email);
            studentStmt.setString(4, rollNumber);
            studentStmt.setString(5, course.isEmpty() ? null : course);
            studentStmt.setInt(6, semester);
            studentStmt.executeUpdate();
            studentStmt.close();
            
            // Commit transaction
            conn.commit();
            
            JOptionPane.showMessageDialog(this, "Registration successful!\nYou can now login.");
            dispose();
            
        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            
            // Check for duplicate entry
            if (e.getMessage().contains("Duplicate entry")) {
                if (e.getMessage().contains("username")) {
                    JOptionPane.showMessageDialog(this, "Username already exists. Please choose another.");
                } else if (e.getMessage().contains("roll_number")) {
                    JOptionPane.showMessageDialog(this, "Roll number already exists. Please check.");
                } else {
                    JOptionPane.showMessageDialog(this, "Duplicate entry. Please check your details.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Registration failed: " + e.getMessage());
            }
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}