package userinterface;

import javax.swing.*;
import java.sql.*;

public class StudentDashboard extends JFrame {
    private String studentName;
    private int userId;
    private int studentId;
    private int energyPoints;
    
    public StudentDashboard(int userId) {
        this.userId = userId;
        loadStudentInfo();
        
        setTitle("EduPulse - " + studentName);
        setSize(1000, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("📊 Dashboard", new DashboardPanel(studentId, studentName, energyPoints));
        tabs.addTab("💬 Messages", new MessagePanel(userId));
        tabs.addTab("⭐ Peer Grading", new peergrading(studentId, studentName));
        tabs.addTab("📢 Notifications", new StudentNotificationsPanel(userId));
        tabs.addTab("👨‍🏫 Faculty Availability", new FacultyAvailabilityPanel());
        
        add(tabs);
        setVisible(true);
    }
    
    private void loadStudentInfo() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = "SELECT id, name, energy_points FROM students WHERE user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                studentId = rs.getInt("id");
                studentName = rs.getString("name");
                energyPoints = rs.getInt("energy_points");
            }
            rs.close(); stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}