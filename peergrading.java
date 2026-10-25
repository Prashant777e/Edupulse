package userinterface;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class peergrading extends JPanel 
{
	int studentId;
	String studentName;
	DefaultTableModel peerhistory;
	JComboBox<String> peer;
	JTextField topic;
	JButton awardbtn;
	
	public peergrading(int studentId, String studentName)
	{
		this.studentId = studentId;
		this.studentName = studentName;
		initialize();
	}
	
	void initialize()
	{
		setLayout(new BorderLayout(10,10));
		
		// Award Panel (Right side)
		JPanel award = new JPanel(new BorderLayout(10,10));
		award.setBorder(BorderFactory.createTitledBorder("Award Peer Points"));
		
		peer = new JComboBox<String>();
		topic = new JTextField();
		awardbtn = new JButton("Award 10 Points");  // ← Initialize FIRST
		awardbtn.setBackground(new Color(46, 204, 113));
		awardbtn.setForeground(Color.WHITE);
		
		// Top: Select student
		JPanel topPanel = new JPanel(new BorderLayout(5,5));
		topPanel.add(new JLabel("Award To:"), BorderLayout.NORTH);
		topPanel.add(peer, BorderLayout.CENTER);
		award.add(topPanel, BorderLayout.NORTH);
		
		// Bottom: Topic and button
		JPanel description = new JPanel(new BorderLayout(10,10));
		description.setBorder(BorderFactory.createTitledBorder("Topic / Reason"));
		description.add(topic, BorderLayout.CENTER);
		description.add(awardbtn, BorderLayout.SOUTH);
		award.add(description, BorderLayout.CENTER);
		
		// History Panel (Center)
		JPanel history = new JPanel(new BorderLayout(10,10));
		history.setBorder(BorderFactory.createTitledBorder("Peer Teaching History"));
		
		String[] column = {"Type", "Peer", "Topic", "Points", "Date"};
		peerhistory = new DefaultTableModel(column, 0);
		JTable his = new JTable(peerhistory);
		his.setRowHeight(25);
		history.add(new JScrollPane(his), BorderLayout.CENTER);
		
		
		// Button action
		awardbtn.addActionListener(e -> awardPoints());
		
		// Load students AFTER all components are ready
		loadStudents(peer);
		loadPeerHistory();
		
		// Add panels to main panel
		add(history, BorderLayout.CENTER);
		add(award, BorderLayout.EAST);
	}
	
	private void loadStudents(JComboBox<String> combo)
	{
		combo.removeAllItems();
		try {
			String sql = "SELECT id, name, roll_number FROM students WHERE id != ? ORDER BY name";
			PreparedStatement stmt = DatabaseConnection.prepare(sql);
			stmt.setInt(1, studentId);
			ResultSet rs = stmt.executeQuery();
			
			while (rs.next()) {
				combo.addItem(rs.getInt("id") + "|" + rs.getString("name") + " (" + rs.getString("roll_number") + ")");
			}
			rs.close();
			stmt.close();
			
			if (combo.getItemCount() == 0) {
				combo.addItem("0|No other students available");
				if (awardbtn != null) {  // Null check for safety
					awardbtn.setEnabled(false);
				}
			} else {
				if (awardbtn != null) {  //Null check for safety
					awardbtn.setEnabled(true);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error loading students: " + e.getMessage());
		}
	}
	
	void awardPoints()
	{
		// Validation
		if (peer.getItemCount() == 0 || peer.getSelectedItem() == null) {
			JOptionPane.showMessageDialog(this, "No students available to award points!");
			return;
		}
		
		String selected = (String) peer.getSelectedItem();
		if (selected.startsWith("0|")) {
			JOptionPane.showMessageDialog(this, "No other students available!");
			return;
		}
		
		String[] parts = selected.split("\\|");
		if (parts.length < 2) {
			JOptionPane.showMessageDialog(this, "Invalid selection!");
			return;
		}
		
		int receiverId = Integer.parseInt(parts[0]);
		String receiverName = parts[1];
		String topicText = topic.getText().trim();
		
		// Prevent self-awarding
		if (receiverId == studentId) {
			JOptionPane.showMessageDialog(this, "You cannot award points to yourself!");
			return;
		}
		
		// Confirm before awarding
		int confirm = JOptionPane.showConfirmDialog(this,
			"Award 10 points to " + receiverName + "?\nTopic: " + (topicText.isEmpty() ? "General Help" : topicText),
			"Confirm Award",
			JOptionPane.YES_NO_OPTION);
		
		if (confirm != JOptionPane.YES_OPTION) {
			return;
		}
		
		try {
			Connection conn = DatabaseConnection.getConnection();
			conn.setAutoCommit(false);
			
			// Insert into peer_teaching table
			String sql = "INSERT INTO peer_teaching (giver_student_id, receiver_student_id, points, topic) VALUES (?, ?, 10, ?)";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setInt(1, studentId);
			stmt.setInt(2, receiverId);
			stmt.setString(3, topicText.isEmpty() ? "General Help" : topicText);
			int rowsInserted = stmt.executeUpdate();
			stmt.close();
			
			if (rowsInserted > 0) {
				// Update energy points
				String updateSql = "UPDATE students SET energy_points = COALESCE(energy_points, 0) + 10 WHERE id = ?";
				PreparedStatement updateStmt = conn.prepareStatement(updateSql);
				updateStmt.setInt(1, receiverId);
				int rowsUpdated = updateStmt.executeUpdate();
				updateStmt.close();
				
				if (rowsUpdated > 0) {
					conn.commit();
					
					// Refresh UI
					topic.setText("");
					loadPeerHistory();
					
					JOptionPane.showMessageDialog(this, 
						"Successfully awarded 10 points to " + receiverName + "!\n" +
						"Topic: " + (topicText.isEmpty() ? "General Help" : topicText),
						"Success",
						JOptionPane.INFORMATION_MESSAGE);
				} else {
					conn.rollback();
					JOptionPane.showMessageDialog(this, "Failed to update energy points!");
				}
			} else {
				conn.rollback();
				JOptionPane.showMessageDialog(this, "Failed to record peer teaching entry!");
			}
			
		} catch (SQLException e) {
			try {
				DatabaseConnection.getConnection().rollback();
			} catch (SQLException ex) {}
			System.err.println("Error awarding points: " + e.getMessage());
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error awarding points: " + e.getMessage());
		}
	}
	
	private void loadPeerHistory()
	{
		peerhistory.setRowCount(0);
		
		// Points GIVEN by this student
		try {
			String sql = "SELECT 'Given To', s.name, pt.topic, pt.points, pt.awarded_at " +
						"FROM peer_teaching pt " +
						"JOIN students s ON s.id = pt.receiver_student_id " +
						"WHERE pt.giver_student_id = ? " +
						"ORDER BY pt.awarded_at DESC";
			PreparedStatement stmt = DatabaseConnection.prepare(sql);
			stmt.setInt(1, studentId);
			ResultSet rs = stmt.executeQuery();
			
			while (rs.next()) {
				peerhistory.addRow(new Object[]{
					rs.getString(1), 
					rs.getString(2), 
					rs.getString(3),
					"+" + rs.getInt(4), 
					rs.getString(5)
				});
			}
			rs.close();
			stmt.close();
		} catch (SQLException e) {
			System.err.println("Error loading given points: " + e.getMessage());
			e.printStackTrace();
		}
		
		// Points RECEIVED by this student
		try {
			String sql = "SELECT 'Received From', s.name, pt.topic, pt.points, pt.awarded_at " +
						"FROM peer_teaching pt " +
						"JOIN students s ON s.id = pt.giver_student_id " +
						"WHERE pt.receiver_student_id = ? " +
						"ORDER BY pt.awarded_at DESC";
			PreparedStatement stmt = DatabaseConnection.prepare(sql);
			stmt.setInt(1, studentId);
			ResultSet rs = stmt.executeQuery();
			
			while (rs.next()) {
				peerhistory.addRow(new Object[]{
					rs.getString(1), 
					rs.getString(2), 
					rs.getString(3),
					"+" + rs.getInt(4), 
					rs.getString(5)
				});
			}
			rs.close();
			stmt.close();
		} catch (SQLException e) {
			System.err.println("Error loading received points: " + e.getMessage());
			e.printStackTrace();
		}
	}
}