package userinterface;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class StudentManagementPanel extends JPanel {
    private DefaultTableModel studentTableModel;
    private JComboBox<String> courseFilter;
    private JComboBox<Integer> semesterFilter;
    
    public StudentManagementPanel() {
        initialize();
        loadStudents(null, 0);
    }
    
    private void initialize() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Filter Panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filter Students"));
        
        courseFilter = new JComboBox<>();
        courseFilter.addItem("All Courses");
        
        semesterFilter = new JComboBox<>();
        semesterFilter.addItem(0);
        for (int i = 1; i <= 8; i++) semesterFilter.addItem(i);
        
        JButton refreshBtn = new JButton("Refresh");
        
        filterPanel.add(new JLabel("Course:"));
        filterPanel.add(courseFilter);
        filterPanel.add(new JLabel("Semester:"));
        filterPanel.add(semesterFilter);
        filterPanel.add(refreshBtn);
        
        // Student Table
        String[] columns = {"ID", "Roll No", "Name", "Course", "Semester", "Email", "Energy Pts"};
        studentTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable studentTable = new JTable(studentTableModel);
        studentTable.setRowHeight(25);
        
        refreshBtn.addActionListener(e -> {
            String course = courseFilter.getSelectedItem().equals("All Courses") ? null : (String) courseFilter.getSelectedItem();
            int semester = (int) semesterFilter.getSelectedItem();
            if (semester == 0) semester = 0;
            loadStudents(course, semester);
        });
        
        add(filterPanel, BorderLayout.NORTH);
        add(new JScrollPane(studentTable), BorderLayout.CENTER);
        
        loadCourses();
    }
    
    private void loadStudents(String course, int semester) {
        studentTableModel.setRowCount(0);
        try {
            Connection conn = DatabaseConnection.getConnection();
            StringBuilder sql = new StringBuilder(
                "SELECT id, roll_number, name, course, semester, email, energy_points FROM students WHERE 1=1");
            
            if (course != null) sql.append(" AND course = ?");
            if (semester > 0) sql.append(" AND semester = ?");
            sql.append(" ORDER BY semester, name");
            
            PreparedStatement stmt = conn.prepareStatement(sql.toString());
            int idx = 1;
            if (course != null) stmt.setString(idx++, course);
            if (semester > 0) stmt.setInt(idx++, semester);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                studentTableModel.addRow(new Object[]{
                    rs.getInt("id"), rs.getString("roll_number"), rs.getString("name"),
                    rs.getString("course"), rs.getInt("semester"), rs.getString("email"),
                    rs.getInt("energy_points")
                });
            }
            rs.close(); stmt.close();
        } catch (SQLException e) { e.printStackTrace(); }
    }
    
    private void loadCourses() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT DISTINCT course FROM students WHERE course IS NOT NULL");
            while (rs.next()) {
                courseFilter.addItem(rs.getString("course"));
            }
            rs.close(); stmt.close();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}