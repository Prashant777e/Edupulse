package userinterface;

import java.awt.*;
import java.sql.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class DashboardPanel extends JPanel 
{
	
	int studentId;//stores student id
	String name;//name of user to display on title
	int energy;//energy pts
	
	JLabel energylabel;
	
	JTable leaderboard;
	
	public DashboardPanel(int studentId,String name,int energy)
	{
		this.studentId=studentId;
		this.name=name;
		this.energy=energy;
		initialize();
	}
	
	
	void initialize()
	{
		setLayout(new BorderLayout(10,10));
		
		JLabel welcome = new JLabel("Welcome " + name);//sets 
		welcome.setFont(new Font("Segoe UI", Font.BOLD, 18));
		
		JPanel energypanel = createEnergyPanel();
		createLeaderboardTable(); //creates energy panel add progress bar 
		
		JPanel topPanel = new JPanel(new BorderLayout(10,10));
		topPanel.add(welcome, BorderLayout.NORTH);
		topPanel.add(energypanel, BorderLayout.CENTER);
		
		JPanel leaderboardPanel = new JPanel(new BorderLayout());
		leaderboardPanel.setBorder(BorderFactory.createTitledBorder("Leaderboard - Top 20 Students"));
		leaderboardPanel.add(new JScrollPane(leaderboard), BorderLayout.CENTER);
		
		add(topPanel, BorderLayout.NORTH);
		add(leaderboardPanel, BorderLayout.CENTER);
	}
	
	JPanel createEnergyPanel()
	{
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder("ENERGY METER"));//creates border layout 
		energylabel = new JLabel(energy + "/1000 Energy points");
		energylabel.setHorizontalAlignment(SwingConstants.CENTER);
		energylabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
		
		//adding progress bar ranging from 0 to 1000
		JProgressBar eb = new JProgressBar(0,1000);
		eb.setValue(energy);//sets energy value
		eb.setStringPainted(true);//allowing painting of progressbar
		eb.setForeground(new Color(96,204,113));//sets vibrant green color in it
		eb.setPreferredSize(new Dimension(300,30));//sets diamention of progressbar
		
		panel.add(energylabel, BorderLayout.NORTH);//adding energy label to panel
		panel.add(eb, BorderLayout.CENTER);
		return panel;
	}
	
	
	void createLeaderboardTable()
	{
		String[] column = {"Rank", "Name", "Roll No", "Energy Points"};//array of string storing headings of tables
		
		DefaultTableModel m = new DefaultTableModel(column, 0);
		
		try
		{
			Connection conn = DatabaseConnection.getConnection();
			
			if (conn == null) {
				System.err.println("Database connection failed!");
				return;
			}
			
			String sql = "SELECT name, roll_number, energy_points FROM students ORDER BY energy_points DESC LIMIT 20";
			Statement stmt = conn.createStatement();
			
			ResultSet rs = stmt.executeQuery(sql);
			
			int rank = 1;
			while(rs.next())
			{
				m.addRow(new Object[]{
					rank++, 
					rs.getString("name"), 
					rs.getString("roll_number"), 
					rs.getInt("energy_points")
				});
			}
			rs.close();
			stmt.close();
		}
		catch(SQLException e)
		{
			System.err.println("Error loading leaderboard: " + e.getMessage());
			e.printStackTrace();
		}
		leaderboard = new JTable(m);
		leaderboard.setRowHeight(30);
		leaderboard.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
	}
}