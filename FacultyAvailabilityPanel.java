package userinterface;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class FacultyAvailabilityPanel extends JPanel {
    private DefaultTableModel availabilityModel;
    private JComboBox<String> facultyFilter;
    
    public FacultyAvailabilityPanel() {
        initialize();
        loadAvailability();
    }
    
    private void initialize() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Filter Panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filter by Faculty"));
        
        facultyFilter = new JComboBox<>();
        facultyFilter.addItem("All Faculty");
        
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadAvailability());
        
        filterPanel.add(new JLabel("Select Faculty:"));
        filterPanel.add(facultyFilter);
        filterPanel.add(refreshBtn);
        
        // Availability Table
        String[] columns = {"Faculty", "Department", "Date", "Time"};
        availabilityModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        
        JTable availabilityTable = new JTable(availabilityModel);
        availabilityTable.setRowHeight(25);
        
        add(filterPanel, BorderLayout.NORTH);
        add(new JScrollPane(availabilityTable), BorderLayout.CENTER);
        
        loadFaculties();
    }
    
    private void loadFaculties() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = "SELECT f.id, f.name, f.department FROM faculty f JOIN users u ON u.id = f.user_id";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                facultyFilter.addItem(rs.getString("name") + " (" + rs.getString("department") + ")");
            }
            rs.close(); 
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void loadAvailability() {
        availabilityModel.setRowCount(0);
        String selectedFaculty = (String) facultyFilter.getSelectedItem();
        
        try {
            Connection conn = DatabaseConnection.getConnection();
            StringBuilder sql = new StringBuilder(
                "SELECT f.name as faculty_name, f.department, fa.available_date, fa.start_time, fa.end_time " +
                "FROM faculty_availability fa JOIN faculty f ON f.id = fa.faculty_id " +
                "WHERE fa.available_date >= CURDATE() ");
            
            if (selectedFaculty != null && !selectedFaculty.equals("All Faculty")) {
                String facultyName = selectedFaculty.split(" \\(")[0];
                sql.append(" AND f.name = ? ");
            }
            sql.append(" ORDER BY fa.available_date, fa.start_time");
            
            PreparedStatement stmt = conn.prepareStatement(sql.toString());
            if (selectedFaculty != null && !selectedFaculty.equals("All Faculty")) {
                String facultyName = selectedFaculty.split(" \\(")[0];
                stmt.setString(1, facultyName);
            }
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                availabilityModel.addRow(new Object[]{
                    rs.getString("faculty_name"),
                    rs.getString("department"),
                    rs.getDate("available_date"),
                    rs.getString("start_time") + " - " + rs.getString("end_time")
                });
            }
            rs.close(); 
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}