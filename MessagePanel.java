package userinterface;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class MessagePanel extends JPanel
{
	int studentId;  // This is actually the user ID from users table
	JComboBox<String> messagerecipient;//combobox storing all student information
	JTextArea message;
	JButton send;
	DefaultTableModel mssg;
	
	MessagePanel(int studentId)  // This receives user ID from users table
	{
		this.studentId = studentId;
		initialize();
	}
	
	void initialize()
	{
		setLayout(new BorderLayout(10,10));
		
		JPanel compose = new JPanel(new BorderLayout(5,5));
		compose.setBorder(BorderFactory.createTitledBorder("Compose Message"));
		
		messagerecipient = new JComboBox<>();
		loadUsers();  // add users in recipient combobox
		
		//composing area message text Ares
		message = new JTextArea(5,30);
		message.setLineWrap(true);// long line of text goes to next line
		message.setWrapStyleWord(true);//Wraps at word boundaries (complete words stay together)
		
		//message button
		send = new JButton("Send Message");
		send.setBackground(new Color(41, 128, 185));
		send.setFocusPainted(false);
		send.addActionListener(e -> sendMessage());
		JPanel messagePanel = new JPanel(new BorderLayout(10,10));
		messagePanel.add(new JScrollPane(message), BorderLayout.CENTER);
		messagePanel.add(send, BorderLayout.SOUTH);
		
		compose.add(new JLabel("To:"), BorderLayout.NORTH);
		compose.add(messagerecipient, BorderLayout.CENTER);
		compose.add(messagePanel, BorderLayout.SOUTH);
		
		JPanel inbox = new JPanel(new BorderLayout(5,5));
		inbox.setBorder(BorderFactory.createTitledBorder("Inbox"));
		
		String[] arr = {"From", "Message", "Date", "Status"};
		mssg = new DefaultTableModel(arr, 0);
		JTable msgTable = new JTable(mssg);
		msgTable.setRowHeight(25);
		
		loadMessages();
		
		inbox.add(new JScrollPane(msgTable), BorderLayout.CENTER);
		add(compose, BorderLayout.WEST);
		add(inbox, BorderLayout.CENTER);
	}
	
	void loadUsers()  // Changed from loadStudents to loadUsers
	{
		messagerecipient.removeAllItems();
		try
		{
			Connection conn = DatabaseConnection.getConnection();
			if (conn == null) return;
			
			// Load from users table instead of students
			String sql = "SELECT id, username FROM users WHERE id != ? AND role = 'student' ORDER BY username";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setInt(1, studentId);
			ResultSet rs = stmt.executeQuery();
			
			while(rs.next())
			{
				messagerecipient.addItem(rs.getInt("id") + "|" + rs.getString("username"));
			}
			rs.close();
			stmt.close();
		}
		catch(SQLException e)
		{
			System.err.println("Error loading users: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	void loadMessages()
	{
		mssg.setRowCount(0);
		try {
			Connection conn = DatabaseConnection.getConnection();
			if (conn == null) return;
			
			// Join with users table instead of students
			String sql = "SELECT m.id, u.username, m.message, m.sent_at, m.is_read " +
						"FROM messages m " +
						"JOIN users u ON u.id = m.sender_id " +
						"WHERE m.receiver_id = ? " +
						"ORDER BY m.sent_at DESC";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setInt(1, studentId);
			ResultSet rs = stmt.executeQuery();
			
			while (rs.next()) {
				String status = rs.getBoolean("is_read") ? "Read" : "New";
				String messageText = rs.getString("message");
				// Skip empty messages
				if (messageText != null && !messageText.trim().isEmpty()) {
					mssg.addRow(new Object[]{
						rs.getString("username"), 
						messageText,
						rs.getString("sent_at"), 
						status
					});
				}
			}
			rs.close();
			stmt.close();
			
			// Mark messages as read
			String updateSql = "UPDATE messages SET is_read = 1 WHERE receiver_id = ? AND is_read = 0";
			PreparedStatement updateStmt = conn.prepareStatement(updateSql);
			updateStmt.setInt(1, studentId);
			updateStmt.executeUpdate();
			updateStmt.close();
			
		} catch (SQLException e) {
			System.err.println("Error loading messages: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	private void sendMessage() 
	{
		if (messagerecipient.getItemCount() == 0) {
			JOptionPane.showMessageDialog(this, "No other users available to message!");
			return;
		}
		
		String selected = (String) messagerecipient.getSelectedItem();
		String messageText = message.getText().trim();
		
		if (messageText.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Please enter a message!");
			return;
		}
		
		int receiverId = Integer.parseInt(selected.split("\\|")[0]);
		
		// Prevent sending message to self
		if (receiverId == studentId) {
			JOptionPane.showMessageDialog(this, "You cannot send a message to yourself!");
			return;
		}
		
		try {
			Connection conn = DatabaseConnection.getConnection();
			if (conn == null) return;
			
			String sql = "INSERT INTO messages (sender_id, receiver_id, message) VALUES (?, ?, ?)";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setInt(1, studentId);
			stmt.setInt(2, receiverId);
			stmt.setString(3, messageText);
			int rows = stmt.executeUpdate();
			stmt.close();
			
			if (rows > 0) {
				message.setText("");
				loadMessages();
				JOptionPane.showMessageDialog(this, "Message sent successfully!");
			}
		} catch (SQLException e) 
		{
			System.err.println("Error sending message: " + e.getMessage());
			JOptionPane.showMessageDialog(this, "Failed to send message: " + e.getMessage());
			e.printStackTrace();
		}
	}
}