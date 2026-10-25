package userinterface;


import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class NotificationHistoryPanel extends JPanel {
    private DefaultTableModel historyModel;
    
    public NotificationHistoryPanel() {
        initialize();
    }
    
    private void initialize() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        String[] columns = {"Date & Time", "Title", "Recipients", "Status"};
        historyModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        
        JTable historyTable = new JTable(historyModel);
        historyTable.setRowHeight(25);
        
        // Sample data (in real app, store in database)
        historyModel.addRow(new Object[]{"2024-01-15 10:30", "Exam Schedule", "All Students (120)", "Sent"});
        historyModel.addRow(new Object[]{"2024-01-14 15:20", "Assignment Deadline", "CS Students (45)", "Sent"});
        historyModel.addRow(new Object[]{"2024-01-13 09:15", "Holiday Notice", "All Students (120)", "Sent"});
        
        JLabel infoLabel = new JLabel("Recent Notifications Sent", SwingConstants.CENTER);
        infoLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        add(infoLabel, BorderLayout.NORTH);
        add(new JScrollPane(historyTable), BorderLayout.CENTER);
    }
    
    public void addToHistory(String datetime, String title, String recipients, String status) {
        historyModel.insertRow(0, new Object[]{datetime, title, recipients, status});
    }
}