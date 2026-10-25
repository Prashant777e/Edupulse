package userinterface;

import java.sql.*;

import javax.swing.*;

public class FacultyDashboard extends JFrame {
    private int userId;
    private int facultyId;
    private String facultyName;
    
    public FacultyDashboard(int userId, String facultyName) {
        this.userId = userId;
        this.facultyName = facultyName;
        loadFacultyId();
        initialize();
    }
    
    private void loadFacultyId() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT id FROM faculty WHERE user_id = ?");
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                facultyId = rs.getInt("id");
            }
            rs.close(); stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void initialize() {
        setTitle("EduPulse - Faculty: " + facultyName);
        setSize(1200, 800);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("👥 Students", new StudentManagementPanel());
       // tabs.addTab("📝 Grades", new GradeEntryPanel(facultyId));
        tabs.addTab("📢 Send Notification", new SendNotificationPanel());
        tabs.addTab("⏰ My Availability", new AvailabilityPanel(facultyId));
        tabs.addTab("📜 History", new NotificationHistoryPanel());
        
        add(tabs);
        setVisible(true);
    }
}