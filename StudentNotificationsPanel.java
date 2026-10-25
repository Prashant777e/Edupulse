package userinterface;


import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class StudentNotificationsPanel extends JPanel {
    private DefaultTableModel notificationModel;
    private int userId;
    private JLabel unreadCountLabel;
    
    public StudentNotificationsPanel(int userId) {
        this.userId = userId;
        initialize();
        loadNotifications();
    }
    
    private void initialize() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Header Panel
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBorder(BorderFactory.createTitledBorder("Notifications"));
        
        unreadCountLabel = new JLabel("Unread: 0");
        unreadCountLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        unreadCountLabel.setForeground(new Color(231, 76, 60));
        
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadNotifications());
        
        headerPanel.add(unreadCountLabel);
        headerPanel.add(refreshBtn);
        
        // Notifications Table
        String[] columns = {"Status", "Title", "Message", "Date Received"};
        notificationModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        
        JTable notificationTable = new JTable(notificationModel);
        notificationTable.setRowHeight(40);
        notificationTable.getColumnModel().getColumn(0).setMaxWidth(60);
        
        // Double-click to mark as read
        notificationTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = notificationTable.getSelectedRow();
                if (evt.getClickCount() == 2 && row >= 0) {
                    markAsRead(row);
                }
            }
        });
        
        add(headerPanel, BorderLayout.NORTH);
        add(new JScrollPane(notificationTable), BorderLayout.CENTER);
    }
    
    private void loadNotifications() {
        notificationModel.setRowCount(0);
        int unreadCount = 0;
        
        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = "SELECT id, title, message, is_read, created_at FROM notifications WHERE user_id = ? ORDER BY created_at DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                boolean isRead = rs.getBoolean("is_read");
                if (!isRead) unreadCount++;
                
                String status = isRead ? "✓ Read" : "● New";
                notificationModel.addRow(new Object[]{
                    status,
                    rs.getString("title"),
                    rs.getString("message"),
                    rs.getTimestamp("created_at")
                });
            }
            rs.close(); stmt.close();
            
            unreadCountLabel.setText("Unread: " + unreadCount);
            if (unreadCount > 0) {
                unreadCountLabel.setForeground(new Color(231, 76, 60));
            } else {
                unreadCountLabel.setForeground(new Color(46, 204, 113));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void markAsRead(int row) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = "UPDATE notifications SET is_read = 1 WHERE user_id = ? AND is_read = 0 ORDER BY created_at DESC LIMIT 1";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.executeUpdate();
            stmt.close();
            loadNotifications();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}