package userinterface;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ViewStudentsPanel extends JPanel {
    
    private JTable studentTable;
    private DefaultTableModel tableModel;
    
    public ViewStudentsPanel() {
        initialize();
        loadStudents();
    }
    
    void initialize() {
        setLayout(new BorderLayout(10, 10));
        
        String[] columns = {"ID", "Name", "Roll No", "Course", "Semester", "Energy Points"};
        tableModel = new DefaultTableModel(columns, 0);
        studentTable = new JTable(tableModel);
        studentTable.setRowHeight(25);
        
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadStudents());
        
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.add(refreshBtn);
        
        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(studentTable), BorderLayout.CENTER);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }
    
    void loadStudents() {
        tableModel.setRowCount(0);
        
        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = "SELECT id, name, roll_number, course, semester, energy_points FROM students ORDER BY name";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("roll_number"),
                    rs.getString("course"),
                    rs.getInt("semester"),
                    rs.getInt("energy_points")
                });
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
}