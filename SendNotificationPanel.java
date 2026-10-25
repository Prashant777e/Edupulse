package userinterface;


import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class SendNotificationPanel extends JPanel {
    private JComboBox<String> recipientType;
    private JComboBox<String> courseCombo;
    private JComboBox<Integer> semesterCombo;
    private JTextField titleField;
    private JTextArea messageArea;
    private JLabel statusLabel;
    
    public SendNotificationPanel() {
        initialize();
    }
    
    private void initialize() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Recipient Panel
        JPanel recipientPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        recipientPanel.setBorder(BorderFactory.createTitledBorder("Select Recipients"));
        
        recipientType = new JComboBox<>(new String[]{"All Students", "Specific Course", "Specific Semester"});
        courseCombo = new JComboBox<>();
        courseCombo.setEnabled(false);
        courseCombo.addItem("Select Course");
        
        semesterCombo = new JComboBox<>();
        semesterCombo.setEnabled(false);
        for (int i = 1; i <= 8; i++) semesterCombo.addItem(i);
        
        recipientType.addActionListener(e -> {
            String selected = (String) recipientType.getSelectedItem();
            courseCombo.setEnabled("Specific Course".equals(selected));
            semesterCombo.setEnabled("Specific Semester".equals(selected));
        });
        
        recipientPanel.add(new JLabel("Send to:"));
        recipientPanel.add(recipientType);
        recipientPanel.add(courseCombo);
        recipientPanel.add(semesterCombo);
        
        // Title Panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBorder(BorderFactory.createTitledBorder("Notification Title"));
        titleField = new JTextField();
        titlePanel.add(titleField, BorderLayout.CENTER);
        
        // Message Panel
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.setBorder(BorderFactory.createTitledBorder("Notification Message"));
        messageArea = new JTextArea(8, 50);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messagePanel.add(new JScrollPane(messageArea), BorderLayout.CENTER);
        
        // Button Panel
        JPanel buttonPanel = new JPanel(new BorderLayout());
        JButton sendBtn = new JButton("Send Notification");
        sendBtn.setBackground(new Color(41, 128, 185));
        sendBtn.setForeground(Color.WHITE);
        sendBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        statusLabel = new JLabel("Ready to send", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        
        buttonPanel.add(sendBtn, BorderLayout.CENTER);
        buttonPanel.add(statusLabel, BorderLayout.SOUTH);
        
        sendBtn.addActionListener(e -> sendNotification());
        
        // Assemble
        add(recipientPanel, BorderLayout.NORTH);
        add(titlePanel, BorderLayout.CENTER);
        add(messagePanel, BorderLayout.SOUTH);
        add(buttonPanel, BorderLayout.EAST);
        
        loadCourses();
    }
    
    private void loadCourses() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT DISTINCT course FROM students WHERE course IS NOT NULL");
            while (rs.next()) {
                courseCombo.addItem(rs.getString("course"));
            }
            rs.close(); stmt.close();
        } catch (SQLException e) { e.printStackTrace(); }
    }
    
    private void sendNotification() {
        String title = titleField.getText().trim();
        String message = messageArea.getText().trim();
        
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a title");
            return;
        }
        if (message.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a message");
            return;
        }
        
        String type = (String) recipientType.getSelectedItem();
        String course = null;
        Integer semester = null;
        
        if ("Specific Course".equals(type)) {
            course = (String) courseCombo.getSelectedItem();
            if (course == null || "Select Course".equals(course)) {
                JOptionPane.showMessageDialog(this, "Please select a course");
                return;
            }
        } else if ("Specific Semester".equals(type)) {
            semester = (Integer) semesterCombo.getSelectedItem();
        }
        
        int sentCount = sendToDatabase(title, message, type, course, semester);
        
        if (sentCount > 0) {
            JOptionPane.showMessageDialog(this, "Notification sent to " + sentCount + " students!");
            titleField.setText("");
            messageArea.setText("");
            statusLabel.setText("Sent to " + sentCount + " students on " + new java.util.Date());
        } else {
            JOptionPane.showMessageDialog(this, "No students found");
        }
    }
    
    private int sendToDatabase(String title, String message, String type, String course, Integer semester) {
        int count = 0;
        try {
            Connection conn = DatabaseConnection.getConnection();
            StringBuilder sql = new StringBuilder("SELECT user_id FROM students WHERE 1=1");
            
            if ("Specific Course".equals(type) && course != null) {
                sql.append(" AND course = ?");
            } else if ("Specific Semester".equals(type) && semester != null) {
                sql.append(" AND semester = ?");
            }
            
            PreparedStatement stmt = conn.prepareStatement(sql.toString());
            int idx = 1;
            if ("Specific Course".equals(type) && course != null) {
                stmt.setString(idx++, course);
            } else if ("Specific Semester".equals(type) && semester != null) {
                stmt.setInt(idx++, semester);
            }
            
            ResultSet rs = stmt.executeQuery();
            
            String insertSql = "INSERT INTO notifications (user_id, title, message, is_read, created_at) VALUES (?, ?, ?, 0, NOW())";
            PreparedStatement insertStmt = conn.prepareStatement(insertSql);
            
            while (rs.next()) {
                insertStmt.setInt(1, rs.getInt("user_id"));
                insertStmt.setString(2, title);
                insertStmt.setString(3, message);
                insertStmt.addBatch();
                count++;
            }
            insertStmt.executeBatch();
            
            rs.close(); stmt.close(); insertStmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
        return count;
    }
}