
// Main.java - Applicant Tracking System (ATS)
// Single file Java Swing application with SQLite database
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.URI;
import java.sql.*;
import java.util.ArrayList;

public class Main extends JFrame {

    // Database connection URL (ats.db will be in current directory)
   private static final String DB_URL = "jdbc:sqlite:ats.db";

    // Swing components for Job section
    private JTextField txtJobTitle = new JTextField(12);
    private JTextField txtJobSkills = new JTextField(15);
    private JTextField txtJobMinExp = new JTextField(4);
    private JButton btnAddJob = new JButton("Add Job");

    // Swing components for Candidate section
    private JTextField txtCandName = new JTextField(10);
    private JTextField txtCandEmail = new JTextField(12);
    private JTextField txtCandSkills = new JTextField(15);
    private JTextField txtCandExp = new JTextField(4);
    private JComboBox<JobItem> cmbJobs = new JComboBox<>();
    private JButton btnAddCandidate = new JButton("Add Candidate");

    // Table components
    private DefaultTableModel tableModel;
    private JTable tblCandidates;

    // Status update components
    private JComboBox<String> cmbStatus = new JComboBox<>(new String[] {
            "Applied", "Shortlisted", "Interviewed", "Selected", "Rejected"
    });
    private JButton btnUpdateStatus = new JButton("Update Status");
    private JButton btnRefresh = new JButton("Refresh");
    private JButton btnOpenWeb = new JButton("Open Web Dashboard");

    // Simple helper class to store job in JComboBox
    static class JobItem {
        int id;
        String title;
        String requiredSkills;
        int minExp;

        public JobItem(int id, String title, String requiredSkills, int minExp) {
            this.id = id;
            this.title = title;
            this.requiredSkills = requiredSkills;
            this.minExp = minExp;
        }

        @Override
        public String toString() {
            return id + ": " + title;
        }
    }

    public Main() {
        setTitle("Applicant Tracking System (ATS) - Java Swing");
        setSize(950, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        

        // 2. Top panel: Forms to Add Job and Add Candidate
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        // Job Form Panel
        JPanel jobPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        jobPanel.setBorder(BorderFactory.createTitledBorder("1. Add Job"));
        jobPanel.add(new JLabel("Title:"));
        jobPanel.add(txtJobTitle);
        jobPanel.add(new JLabel("Required Skills (comma-separated):"));
        jobPanel.add(txtJobSkills);
        jobPanel.add(new JLabel("Min Exp (yrs):"));
        jobPanel.add(txtJobMinExp);
        jobPanel.add(btnAddJob);
        topPanel.add(jobPanel);

        // Candidate Form Panel
        JPanel candPanel = new JPanel(new GridLayout(2, 1));
        candPanel.setBorder(BorderFactory.createTitledBorder("2. Add Candidate"));

        JPanel candRow1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        candRow1.add(new JLabel("Name:"));
        candRow1.add(txtCandName);
        candRow1.add(new JLabel("Email:"));
        candRow1.add(txtCandEmail);
        candRow1.add(new JLabel("Skills (comma-separated):"));
        candRow1.add(txtCandSkills);
        candRow1.add(new JLabel("Exp (yrs):"));
        candRow1.add(txtCandExp);

        JPanel candRow2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        candRow2.add(new JLabel("Select Job:"));
        candRow2.add(cmbJobs);
        candRow2.add(btnAddCandidate);

        candPanel.add(candRow1);
        candPanel.add(candRow2);
        topPanel.add(candPanel);

        add(topPanel, BorderLayout.NORTH);

        // 3. Center: JTable showing candidates sorted by match score
        String[] columns = { "ID", "Name", "Job", "Skills", "Exp (yrs)", "Match Score (%)", "Status" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // read-only cells
            }
        };
        tblCandidates = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(tblCandidates);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Candidates (Sorted by Highest Match Score)"));
        add(scrollPane, BorderLayout.CENTER);

        // 4. Bottom panel: Actions (Status Update, Refresh, Open Web Dashboard)
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        bottomPanel.setBorder(BorderFactory.createTitledBorder("Actions"));
        bottomPanel.add(new JLabel("Change Selected Status to:"));
        bottomPanel.add(cmbStatus);
        bottomPanel.add(btnUpdateStatus);
        bottomPanel.add(btnRefresh);
        bottomPanel.add(btnOpenWeb);
        add(bottomPanel, BorderLayout.SOUTH);

        // 5. Button Listeners
        btnAddJob.addActionListener(e -> handleAddJob());
        btnAddCandidate.addActionListener(e -> handleAddCandidate());
        btnUpdateStatus.addActionListener(e -> handleUpdateStatus());
        btnRefresh.addActionListener(e -> loadData());
        btnOpenWeb.addActionListener(e -> openWebDashboard());

        // Load initial data into combo box and table
        loadData();
    }

    
   

    // Add a new job
    private void handleAddJob() {
        String title = txtJobTitle.getText().trim();
        String skills = txtJobSkills.getText().trim();
        String expStr = txtJobMinExp.getText().trim();

        if (title.isEmpty() || skills.isEmpty() || expStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all job fields!");
            return;
        }

        try {
            int minExp = Integer.parseInt(expStr);
            String sql = "INSERT INTO jobs (title, required_skills, min_experience) VALUES (?, ?, ?)";
            try (Connection conn = DriverManager.getConnection(DB_URL);
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, title);
                pstmt.setString(2, skills);
                pstmt.setInt(3, minExp);
                pstmt.executeUpdate();
            }

            // Clear inputs and refresh
            txtJobTitle.setText("");
            txtJobSkills.setText("");
            txtJobMinExp.setText("");
            JOptionPane.showMessageDialog(this, "Job added successfully!");
            loadData();

        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Min Experience must be a valid number!");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage());
        }
    }

    // Add candidate with Match Score calculation
    private void handleAddCandidate() {
        JobItem selectedJob = (JobItem) cmbJobs.getSelectedItem();
        if (selectedJob == null) {
            JOptionPane.showMessageDialog(this, "Please select a job first (add a job if none exists).");
            return;
        }

        String name = txtCandName.getText().trim();
        String email = txtCandEmail.getText().trim();
        String skills = txtCandSkills.getText().trim();
        String expStr = txtCandExp.getText().trim();

        if (name.isEmpty() || email.isEmpty() || skills.isEmpty() || expStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all candidate fields!");
            return;
        }

        try {
            int candExp = Integer.parseInt(expStr);

            // Calculate Match Score:
            // 80% Skill Match + 20% Experience Match
            double matchScore = calculateMatchScore(selectedJob.requiredSkills, skills, selectedJob.minExp, candExp);

            String sql = "INSERT INTO candidates (name, email, skills, experience, job_id, match_score, status) "
                    + "VALUES (?, ?, ?, ?, ?, ?, 'Applied')";
            try (Connection conn = DriverManager.getConnection(DB_URL);
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, name);
                pstmt.setString(2, email);
                pstmt.setString(3, skills);
                pstmt.setInt(4, candExp);
                pstmt.setInt(5, selectedJob.id);
                pstmt.setDouble(6, Math.round(matchScore * 10.0) / 10.0); // round to 1 decimal
                pstmt.executeUpdate();
            }

            // Clear inputs and refresh
            txtCandName.setText("");
            txtCandEmail.setText("");
            txtCandSkills.setText("");
            txtCandExp.setText("");
            JOptionPane.showMessageDialog(this,
                    "Candidate added! Match Score: " + (Math.round(matchScore * 10.0) / 10.0) + "%");
            loadData();

        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Experience must be a valid number!");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage());
        }
    }

    // Match Score Formula: 80% skill match + 20% experience match
    private double calculateMatchScore(String jobSkillsStr, String candSkillsStr, int minExp, int candExp) {
        // 1. Skill Match = (matched skills / total required skills) * 100
        String[] jobSkills = jobSkillsStr.split(",");
        String[] candSkills = candSkillsStr.split(",");

        int matchedCount = 0;
        int totalRequired = 0;

        for (String js : jobSkills) {
            String trimmedJs = js.trim().toLowerCase();
            if (trimmedJs.isEmpty())
                continue;
            totalRequired++;

            for (String cs : candSkills) {
                if (trimmedJs.equalsIgnoreCase(cs.trim())) {
                    matchedCount++;
                    break;
                }
            }
        }

        double skillMatch = (totalRequired == 0) ? 100.0 : ((double) matchedCount / totalRequired) * 100.0;

        // 2. Experience Match = min(100, candExp / minExp * 100), or 100 if minExp == 0
        double expMatch;
        if (minExp <= 0) {
            expMatch = 100.0;
        } else {
            expMatch = Math.min(100.0, ((double) candExp / minExp) * 100.0);
        }

        return (0.80 * skillMatch) + (0.20 * expMatch);
    }

    // Update status of selected candidate in JTable
    private void handleUpdateStatus() {
        int selectedRow = tblCandidates.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a candidate row from the table first!");
            return;
        }

        int candidateId = (int) tableModel.getValueAt(selectedRow, 0);
        String newStatus = (String) cmbStatus.getSelectedItem();

        String sql = "UPDATE candidates SET status = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newStatus);
            pstmt.setInt(2, candidateId);
            pstmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Status updated to '" + newStatus + "'!");
            loadData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage());
        }
    }

    // Load jobs into JComboBox and candidates into JTable sorted by match_score
    // DESC
    private void loadData() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {

            // 1. Reload Jobs in JComboBox
            cmbJobs.removeAllItems();
            String sqlJobs = "SELECT id, title, required_skills, min_experience FROM jobs";
            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(sqlJobs)) {
                while (rs.next()) {
                    cmbJobs.addItem(new JobItem(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("required_skills"),
                            rs.getInt("min_experience")));
                }
            }

            // 2. Reload Candidates in JTable (sorted by match_score DESC)
            tableModel.setRowCount(0);
            String sqlCandidates = "SELECT c.id, c.name, j.title, c.skills, c.experience, c.match_score, c.status "
                    + "FROM candidates c "
                    + "LEFT JOIN jobs j ON c.job_id = j.id "
                    + "ORDER BY c.match_score DESC";

            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(sqlCandidates)) {
                while (rs.next()) {
                    tableModel.addRow(new Object[] {
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("title"),
                            rs.getString("skills"),
                            rs.getInt("experience"),
                            rs.getDouble("match_score"),
                            rs.getString("status")
                    });
                }
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage());
        }
    }

    // Open Web Dashboard in browser
    private void openWebDashboard() {
        try {
            Desktop.getDesktop().browse(new URI("http://127.0.0.1:8000/"));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not open browser: " + ex.getMessage()
                    + "\nMake sure Django server is running at http://127.0.0.1:8000/");
        }
    }

    public static void main(String[] args) {
        // Run Swing app on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            new Main().setVisible(true);
        });
    }
}
