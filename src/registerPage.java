import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class registerPage extends JFrame implements ActionListener {
    private final JTextField lastNameField, firstNameField, middleNameField, emailField;
    private final JPasswordField passwordField, confirmPasswordField;
    private final JButton backButton;

    public registerPage() {
        setTitle("SKY RESERVE - Register");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(Color.decode("#0F149a"));
        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("SKY RESERVE - REGISTER");
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 40));
        titleLabel.setForeground(Color.WHITE);
        add(titleLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.decode("#0F149a"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font labelFont = new Font("Arial", Font.PLAIN, 30);
        Font fieldFont = new Font("Arial", Font.PLAIN, 28);

        JLabel lastNameLabel = new JLabel("Last Name: *");
        lastNameLabel.setFont(labelFont);
        lastNameLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(lastNameLabel, gbc);

        lastNameField = new JTextField(20);
        lastNameField.setFont(fieldFont);
        gbc.gridx = 1;
        formPanel.add(lastNameField, gbc);

        JLabel firstNameLabel = new JLabel("First Name: *");
        firstNameLabel.setFont(labelFont);
        firstNameLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(firstNameLabel, gbc);

        firstNameField = new JTextField(20);
        firstNameField.setFont(fieldFont);
        gbc.gridx = 1;
        formPanel.add(firstNameField, gbc);

        JLabel middleNameLabel = new JLabel("Middle Name:");
        middleNameLabel.setFont(labelFont);
        middleNameLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(middleNameLabel, gbc);

        middleNameField = new JTextField(20);
        middleNameField.setFont(fieldFont);
        gbc.gridx = 1;
        formPanel.add(middleNameField, gbc);

        JLabel emailLabel = new JLabel("Gmail Address: *");
        emailLabel.setFont(labelFont);
        emailLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(emailLabel, gbc);

        emailField = new JTextField(20);
        emailField.setFont(fieldFont);
        gbc.gridx = 1;
        formPanel.add(emailField, gbc);

        JLabel passwordLabel = new JLabel("Password: *");
        passwordLabel.setFont(labelFont);
        passwordLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(passwordLabel, gbc);

        passwordField = new JPasswordField(20);
        passwordField.setFont(fieldFont);
        gbc.gridx = 1;
        formPanel.add(passwordField, gbc);

        JLabel confirmPasswordLabel = new JLabel("Confirm Password: *");
        confirmPasswordLabel.setFont(labelFont);
        confirmPasswordLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 5;
        formPanel.add(confirmPasswordLabel, gbc);

        confirmPasswordField = new JPasswordField(20);
        confirmPasswordField.setFont(fieldFont);
        gbc.gridx = 1;
        formPanel.add(confirmPasswordField, gbc);

        JButton submitButton = new JButton("Submit");
        submitButton.setBackground(Color.decode("#fd9b4d"));
        submitButton.setFont(new Font("Arial", Font.BOLD, 30));
        submitButton.addActionListener(this);
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(submitButton, gbc);

        add(formPanel, BorderLayout.CENTER);

        backButton = new JButton("Go Back");
        backButton.setFont(new Font("Arial", Font.BOLD, 30));
        backButton.setBackground(Color.decode("#fd9b4d"));
        backButton.addActionListener(this);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(Color.decode("#0F149a"));
        bottomPanel.add(backButton);

        add(bottomPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String lastName = lastNameField.getText();
        String firstName = firstNameField.getText();
        String middleName = middleNameField.getText();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (e.getSource() == backButton) {
            dispose();
            new indexFrame();
            return;
        }

        if (lastName.isEmpty() || firstName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields.");
            return;
        }

        if (!email.matches("^[^@]+@gmail\\.com$")) {
            JOptionPane.showMessageDialog(this, "Invalid email. It must be a valid @gmail.com email with text before '@'.");
            return;
        }

        if (password.length() < 8 || password.length() > 16) {
            JOptionPane.showMessageDialog(this, "Password must be between 8 and 16 characters.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.");
            return;
        }

        try {
            Connection conn = DBConnector.getConnection();

            String checkEmailQuery = "SELECT COUNT(*) FROM users WHERE email = ?";
            PreparedStatement checkEmailStmt = conn.prepareStatement(checkEmailQuery);
            checkEmailStmt.setString(1, email);
            ResultSet rsEmail = checkEmailStmt.executeQuery();
            rsEmail.next();
            if (rsEmail.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, "This email is already registered.");
                return;
            }

            String sql = "INSERT INTO users (last_name, first_name, middle_name, email, password) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, lastName);
            pstmt.setString(2, firstName);
            pstmt.setString(3, middleName);
            pstmt.setString(4, email);
            pstmt.setString(5, password);
            pstmt.executeUpdate();

            conn.close();
            JOptionPane.showMessageDialog(this, "Registration successful!");

            dispose();
            new loginPage();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Registration failed.");
        }
    }

}
