package com.student;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

public class StudentApp extends JFrame {
	private JTextField nameField, emailField, courseField, gradeField, attendanceField, scheduleField;
	private JButton addButton, loadButton, deleteButton, updateButton, gradeReportButton, scheduleReportButton;
	private JTable studentTable;
	private DefaultTableModel tableModel;

	public StudentApp() {
		setTitle("Student Information Management System");
		setSize(900, 600);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setLayout(new BorderLayout());

		// TOP PANEL (Input Form)
		JPanel inputPanel = new JPanel(new GridLayout(7, 2, 10, 10));
		inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		inputPanel.add(new JLabel("Full Name:"));
		nameField = new JTextField();
		inputPanel.add(nameField);

		inputPanel.add(new JLabel("Email Address:"));
		emailField = new JTextField();
		inputPanel.add(emailField);

		inputPanel.add(new JLabel("Course:"));
		courseField = new JTextField();
		inputPanel.add(courseField);

		inputPanel.add(new JLabel("Grade:"));
		gradeField = new JTextField();
		inputPanel.add(gradeField);

		inputPanel.add(new JLabel("Attendance (%):"));
		attendanceField = new JTextField();
		inputPanel.add(attendanceField);

		inputPanel.add(new JLabel("Class Schedule (e.g., Mon 10AM):"));
		scheduleField = new JTextField();
		inputPanel.add(scheduleField);

		addButton = new JButton("Add Student");
		loadButton = new JButton("Refresh Records");
		inputPanel.add(addButton);
		inputPanel.add(loadButton);

		add(inputPanel, BorderLayout.NORTH);

		// CENTER PANEL (Data Table)
		tableModel = new DefaultTableModel(
				new String[] { "ID", "Name", "Email", "Course", "Grade", "Attendance %", "Schedule" }, 0);
		studentTable = new JTable(tableModel);
		add(new JScrollPane(studentTable), BorderLayout.CENTER);

		// BOTTOM PANEL (Reports & CRUD)
		JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
		deleteButton = new JButton("Delete Selected Student");
		updateButton = new JButton("Update Selected Student");
		gradeReportButton = new JButton("Grade Performance Report");
		scheduleReportButton = new JButton("Class Schedule Report");

		bottomPanel.add(deleteButton);
		bottomPanel.add(updateButton);
		bottomPanel.add(gradeReportButton);
		bottomPanel.add(scheduleReportButton);
		add(bottomPanel, BorderLayout.SOUTH);

		// BUTTON ACTIONS

		addButton.addActionListener(e -> addStudent());
		loadButton.addActionListener(e -> loadStudents());
		deleteButton.addActionListener(e -> deleteStudent());
		updateButton.addActionListener(e -> updateStudent());
		gradeReportButton.addActionListener(e -> generateGradeReport());
		scheduleReportButton.addActionListener(e -> generateScheduleReport());

		// Load data on startup
		loadStudents();
	}

	protected void updateStudent() {
		int selectedRow = studentTable.getSelectedRow();
		if (selectedRow == -1) {
			JOptionPane.showMessageDialog(this, "Please select a student from the table to update.");
			return;
		}

		String id = (String) tableModel.getValueAt(selectedRow, 0);
		String name = nameField.getText();
		String email = emailField.getText();
		String course = courseField.getText();
		String grade = gradeField.getText();
		String attendance = attendanceField.getText();
		String schedule = scheduleField.getText();

		if (name.isEmpty() || email.isEmpty() || course.isEmpty() || attendance.isEmpty() || grade.isEmpty()
				|| schedule.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Please fill in all required fields before updating.");
			return;
		}

		String sql = "UPDATE students SET name = ?, email = ?, course = ?, grade = ?, attendance = ?, class_schedule = ? WHERE id = ?";

		try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, name);
			pstmt.setString(2, email);
			pstmt.setString(3, course);
			pstmt.setString(4, grade);
			pstmt.setInt(5, Integer.parseInt(attendance));
			pstmt.setString(6, schedule);
			pstmt.setInt(7, Integer.parseInt(id));

			pstmt.executeUpdate();
			JOptionPane.showMessageDialog(this, "Student Details Updated Successfully!");

			// Clear fields and refresh
			nameField.setText("");
			emailField.setText("");
			courseField.setText("");
			gradeField.setText("");
			attendanceField.setText("");
			scheduleField.setText("");
			loadStudents();

		} catch (SQLException | NumberFormatException ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error updating student record.");
		}

	}

	// Read Operation
	protected void loadStudents() {
		tableModel.setRowCount(0);
		String sql = "SELECT * FROM students";

		try (Connection conn = DBConnection.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next()) {
				Vector<String> row = new Vector<>();
				row.add(String.valueOf(rs.getInt("id")));
				row.add(rs.getString("name"));
				row.add(rs.getString("email"));
				row.add(rs.getString("course"));
				row.add(rs.getString("grade"));
				row.add(String.valueOf(rs.getInt("attendance")));
				row.add(rs.getString("class_schedule"));
				tableModel.addRow(row);
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error loading student data.");
		}
	}

	// Create Operation
	protected void addStudent() {
		String name = nameField.getText();
		String email = emailField.getText();
		String course = courseField.getText();
		String grade = gradeField.getText();
		String attendance = attendanceField.getText();
		String schedule = scheduleField.getText();

		if (name.isEmpty() || email.isEmpty() || course.isEmpty() || attendance.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Please fill in all required fields.");
			return;
		}

		String sql = "INSERT INTO students(name, email, course, grade, attendance, class_schedule) VALUES(?, ?, ?, ?, ?, ?)";

		try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, name);
			pstmt.setString(2, email);
			pstmt.setString(3, course);
			pstmt.setString(4, grade);
			pstmt.setInt(5, Integer.parseInt(attendance));
			pstmt.setString(6, schedule);
			pstmt.executeUpdate();

			JOptionPane.showMessageDialog(this, "Student Added Successfully!");

			nameField.setText("");
			emailField.setText("");
			courseField.setText("");
			gradeField.setText("");
			attendanceField.setText("");
			scheduleField.setText("");
			loadStudents();

		} catch (SQLException | NumberFormatException ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error adding student. Ensure attendance is a number.");
		}
	}

	// Delete Operation (Fulfills CRUD requirement)
	protected void deleteStudent() {
		int selectedRow = studentTable.getSelectedRow();
		if (selectedRow == -1) {
			JOptionPane.showMessageDialog(this, "Please select a student from the table to delete.");
			return;
		}

		String id = (String) tableModel.getValueAt(selectedRow, 0);
		String sql = "DELETE FROM students WHERE id = ?";

		try (Connection conn = DBConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, Integer.parseInt(id));
			pstmt.executeUpdate();
			JOptionPane.showMessageDialog(this, "Student Deleted Successfully!");
			loadStudents();

		} catch (SQLException ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error deleting student.");
		}
	}

	// Fulfills "Instant reports on grade performance"
	protected void generateGradeReport() {
		String sql = "SELECT grade, COUNT(*) as total_students, AVG(attendance) as avg_attendance FROM students GROUP BY grade ORDER BY grade";
		StringBuilder report = new StringBuilder("Grade Performance Report:\n\n");

		try (Connection conn = DBConnection.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next()) {
				String grade = rs.getString("grade");
				int count = rs.getInt("total_students");
				double avgAtt = rs.getDouble("avg_attendance");
				report.append("Grade ").append(grade != null ? grade : "N/A").append(": ").append(count)
						.append(" Students (Avg Attendance: ").append(String.format("%.1f", avgAtt)).append("%)\n");
			}
			JOptionPane.showMessageDialog(this, report.toString(), "Grade Report", JOptionPane.INFORMATION_MESSAGE);

		} catch (SQLException ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error generating report.");
		}
	}

	// Fulfills "Instant reports on class schedules"
	protected void generateScheduleReport() {
		String sql = "SELECT class_schedule, COUNT(*) as total_students FROM students GROUP BY class_schedule ORDER BY class_schedule";
		StringBuilder report = new StringBuilder("Class Schedule Report:\n\n");

		try (Connection conn = DBConnection.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next()) {
				String schedule = rs.getString("class_schedule");
				int count = rs.getInt("total_students");
				report.append("Schedule [").append(schedule != null && !schedule.isEmpty() ? schedule : "Unassigned")
						.append("]: ").append(count).append(" Students\n");
			}
			JOptionPane.showMessageDialog(this, report.toString(), "Schedule Report", JOptionPane.INFORMATION_MESSAGE);

		} catch (SQLException ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error generating report.");
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			new StudentApp().setVisible(true);
		});
	}
}