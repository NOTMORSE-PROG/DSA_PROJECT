import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Setting extends JFrame implements ActionListener {
    private String userEmail;
    private final JPasswordField newPasswordField, confirmPasswordField;
    private final JTextField emailField;
    private final JButton updateAccountButton, deleteAccountButton, goBackButton;

    public Setting(String userEmail) {
        this.userEmail = userEmail;
        setTitle("Profile Settings");
        setResizable(false);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        getContentPane().setBackground(Color.decode("#0F149a"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel titleLabel = new JLabel("PROFILE SETTINGS", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 48));
        titleLabel.setForeground(Color.WHITE);
        add(titleLabel, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.decode("#0F149a"));
        add(mainPanel, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        String fullName = "";
        try (Connection conn = DBConnector.getConnection()) {
            String sql = "SELECT CONCAT(first_name, ' ', last_name) AS full_name FROM users WHERE email = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userEmail);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                fullName = rs.getString("full_name");
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading user data: " + e.getMessage());
        }

        JLabel fullNameLabel = new JLabel("Full Name:");
        fullNameLabel.setFont(new Font("Arial", Font.BOLD, 28));
        fullNameLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(fullNameLabel, gbc);

        JLabel fullNameValue = new JLabel(fullName);
        fullNameValue.setFont(new Font("Arial", Font.PLAIN, 28));
        fullNameValue.setForeground(Color.WHITE);
        gbc.gridx = 1;
        mainPanel.add(fullNameValue, gbc);

        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(new Font("Arial", Font.BOLD, 28));
        emailLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(emailLabel, gbc);

        emailField = new JTextField(userEmail);
        emailField.setFont(new Font("Arial", Font.PLAIN, 28));
        gbc.gridx = 1;
        mainPanel.add(emailField, gbc);

        JLabel newPasswordLabel = new JLabel("New Password:");
        newPasswordLabel.setFont(new Font("Arial", Font.BOLD, 28));
        newPasswordLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(newPasswordLabel, gbc);

        newPasswordField = new JPasswordField();
        newPasswordField.setFont(new Font("Arial", Font.PLAIN, 28));
        gbc.gridx = 1;
        mainPanel.add(newPasswordField, gbc);

        JLabel confirmPasswordLabel = new JLabel("Confirm Password:");
        confirmPasswordLabel.setFont(new Font("Arial", Font.BOLD, 28));
        confirmPasswordLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 3;
        mainPanel.add(confirmPasswordLabel, gbc);

        confirmPasswordField = new JPasswordField();
        confirmPasswordField.setFont(new Font("Arial", Font.PLAIN, 28));
        gbc.gridx = 1;
        mainPanel.add(confirmPasswordField, gbc);

        updateAccountButton = new JButton("Update Account");
        updateAccountButton.setFont(new Font("Arial", Font.BOLD, 28));
        updateAccountButton.setForeground(Color.WHITE);
        updateAccountButton.setBackground(Color.decode("#fd9b4d"));
        updateAccountButton.addActionListener(this);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        mainPanel.add(updateAccountButton, gbc);

        deleteAccountButton = new JButton("Delete Account");
        deleteAccountButton.setFont(new Font("Arial", Font.BOLD, 28));
        deleteAccountButton.setForeground(Color.WHITE);
        deleteAccountButton.setBackground(Color.RED);
        deleteAccountButton.addActionListener(this);
        gbc.gridy = 5;
        mainPanel.add(deleteAccountButton, gbc);

        goBackButton = new JButton("Go Back");
        goBackButton.setFont(new Font("Arial", Font.BOLD, 28));
        goBackButton.setForeground(Color.WHITE);
        goBackButton.setBackground(Color.decode("#fd9b4d"));
        goBackButton.addActionListener(this);
        gbc.gridy = 6;
        mainPanel.add(goBackButton, gbc);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == updateAccountButton) {
            String newPassword = new String(newPasswordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());
            String newEmail = emailField.getText().trim();
            if (newEmail.equals(userEmail) && newPassword.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No changes to update.");
                return;
            }
            if (!newEmail.equals(userEmail)) {
                if (newEmail.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Email cannot be empty.");
                    return;
                }

                Pattern emailPattern = Pattern.compile("^[a-zA-Z0-9._%+-]+@gmail\\.com$");
                Matcher matcher = emailPattern.matcher(newEmail);
                if (!matcher.matches()) {
                    JOptionPane.showMessageDialog(this, "Please enter a valid Gmail address (e.g., username@gmail.com).");
                    return;
                }
                try (Connection conn = DBConnector.getConnection()) {
                    String checkEmailSQL = "SELECT COUNT(*) FROM users WHERE email = ?";
                    PreparedStatement checkEmailStmt = conn.prepareStatement(checkEmailSQL);
                    checkEmailStmt.setString(1, newEmail);
                    ResultSet rs = checkEmailStmt.executeQuery();

                    if (rs.next() && rs.getInt(1) > 0) {
                        JOptionPane.showMessageDialog(this, "There is already a user associated with this email.");
                        return;
                    }
                } catch (SQLException | ClassNotFoundException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error checking email availability: " + ex.getMessage());
                    return;
                }
            }
            if (!newPassword.isEmpty()) {
                int minPasswordLength = 8;
                int maxPasswordLength = 16;

                if (newPassword.length() < minPasswordLength || newPassword.length() > maxPasswordLength) {
                    JOptionPane.showMessageDialog(this, "Password must be between " + minPasswordLength + " and " + maxPasswordLength + " characters.");
                    return;
                }

                if (!newPassword.equals(confirmPassword)) {
                    JOptionPane.showMessageDialog(this, "Passwords do not match.");
                    return;
                }
            }
            try (Connection conn = DBConnector.getConnection()) {
                String updateSQL;
                if (newPassword.isEmpty()) {
                    updateSQL = "UPDATE users SET email = ? WHERE email = ?";
                } else {
                    updateSQL = "UPDATE users SET email = ?, password = ? WHERE email = ?";
                }

                PreparedStatement pstmt = conn.prepareStatement(updateSQL);
                pstmt.setString(1, newEmail);

                if (!newPassword.isEmpty()) {
                    pstmt.setString(2, newPassword);
                    pstmt.setString(3, userEmail);
                } else {
                    pstmt.setString(2, userEmail);
                }

                pstmt.executeUpdate();

                JOptionPane.showMessageDialog(this, "Account updated successfully.");
                userEmail = newEmail;
                emailField.setText(userEmail);
                newPasswordField.setText("");
                confirmPasswordField.setText("");
            } catch (SQLException | ClassNotFoundException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error updating account: " + ex.getMessage());
            }
        } else if (e.getSource() == deleteAccountButton) {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to delete your account? This action cannot be undone.",
                    "Confirm Deletion",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection conn = DBConnector.getConnection()) {
                    String deleteUserSQL = "DELETE FROM users WHERE email = ?";
                    try (PreparedStatement pstmt = conn.prepareStatement(deleteUserSQL)) {
                        pstmt.setString(1, userEmail);
                        pstmt.executeUpdate();
                    }
                    JOptionPane.showMessageDialog(this, "Account deleted successfully.");
                    this.dispose();
                    new indexFrame();
                } catch (SQLException | ClassNotFoundException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Unable to delete account: " + ex.getMessage());
                }
            }
        } else if (e.getSource() == goBackButton) {
            this.dispose();
            new userDashboard(userEmail);
        }
    }
}
