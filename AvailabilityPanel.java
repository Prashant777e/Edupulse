package userinterface;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;

public class AvailabilityPanel extends JPanel {
    private DefaultTableModel availabilityModel;
    private JSpinner dateSpinner;
    private JComboBox<String> startHour, endHour;
    private int facultyId;
    
    public AvailabilityPanel(int facultyId) {
        this.facultyId = facultyId;
        initialize();
        loadAvailability();
    }
    
    private void initialize() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Form Panel
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Add Availability Slot"));
        
        dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        
        startHour = new JComboBox<>();
        endHour = new JComboBox<>();
        for (int h = 0; h < 24; h++) {
            String hour = String.format("%02d:00", h);
            startHour.addItem(hour);
            endHour.addItem(hour);
        }
        
        JButton addBtn = new JButton("Add Slot");
        addBtn.setBackground(new Color(46, 204, 113));
        addBtn.setForeground(Color.WHITE);
        
        formPanel.add(new JLabel("Date:")); formPanel.add(dateSpinner);
        formPanel.add(new JLabel("Start Time:")); formPanel.add(startHour);
        formPanel.add(new JLabel("End Time:")); formPanel.add(endHour);
        formPanel.add(new JLabel("")); formPanel.add(addBtn);
        
        // Table Panel
        String[] columns = {"ID", "Date", "Start Time", "End Time"};
        availabilityModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable availabilityTable = new JTable(availabilityModel);
        availabilityTable.setRowHeight(25);
        
        JButton deleteBtn = new JButton("Delete Selected");
        deleteBtn.setBackground(new Color(231, 76, 60));
        deleteBtn.setForeground(Color.WHITE);
        
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("My Availability Slots"));
        tablePanel.add(new JScrollPane(availabilityTable), BorderLayout.CENTER);
        tablePanel.add(deleteBtn, BorderLayout.SOUTH);
        
        addBtn.addActionListener(e -> addAvailability());
        deleteBtn.addActionListener(e -> {
            int row = availabilityTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select a slot to delete");
                return;
            }
            deleteAvailability((int) availabilityModel.getValueAt(row, 0));
        });
        
        add(formPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
    }
    
    private void addAvailability() {
        java.util.Date selectedDate = (java.util.Date) dateSpinner.getValue();
        String start = (String) startHour.getSelectedItem();
        String end = (String) endHour.getSelectedItem();
        
        try {
            Connection conn = DatabaseConnection.getConnection();
            // Insert without is_booked and location columns
            String sql = "INSERT INTO faculty_availability (faculty_id, available_date, start_time, end_time) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, facultyId);
            stmt.setDate(2, new java.sql.Date(selectedDate.getTime()));
            stmt.setString(3, start);
            stmt.setString(4, end);
            stmt.executeUpdate();
            stmt.close();
            
            JOptionPane.showMessageDialog(this, "Availability slot added!");
            loadAvailability();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
    
    private void loadAvailability() {
        availabilityModel.setRowCount(0);
        try {
            Connection conn = DatabaseConnection.getConnection();
            // Select only columns that exist in your table
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT id, available_date, start_time, end_time FROM faculty_availability WHERE faculty_id = ? ORDER BY available_date, start_time");
            stmt.setInt(1, facultyId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                availabilityModel.addRow(new Object[]{
                    rs.getInt("id"), 
                    rs.getDate("available_date"), 
                    rs.getString("start_time"),
                    rs.getString("end_time")
                });
            }
            rs.close(); 
            stmt.close();
        } catch (SQLException e) { 
            e.printStackTrace();
        }
    }
    
    private void deleteAvailability(int slotId) {
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this availability slot?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement("DELETE FROM faculty_availability WHERE id = ? AND faculty_id = ?");
                stmt.setInt(1, slotId);
                stmt.setInt(2, facultyId);
                stmt.executeUpdate();
                stmt.close();
                loadAvailability();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
}