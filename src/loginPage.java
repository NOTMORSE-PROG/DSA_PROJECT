import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class loginPage extends JFrame implements ActionListener {
    private final JButton loginButton;
    private final JButton goBackButton;
    private final JTextField emailField;
    private final JPasswordField passwordField;

    public loginPage() {
        setTitle("SKY REVERSE - LOGIN");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        getContentPane().setBackground(Color.decode("#0F149a"));

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;

        goBackButton = new JButton("Go Back");
        goBackButton.setFont(new Font("Arial", Font.BOLD, 24));
        goBackButton.setPreferredSize(new Dimension(150, 50));
        goBackButton.setForeground(Color.WHITE);
        goBackButton.setBackground(Color.decode("#fd9b4d"));
        goBackButton.setOpaque(true);
        goBackButton.setBorderPainted(false);
        goBackButton.addActionListener(this);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        add(goBackButton, gbc);

        JLabel titleLabel = new JLabel("SKY REVERSE - LOGIN");
        titleLabel.setFont(new Font("Serif", Font.BOLD, 50));
        titleLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 60, 0);
        add(titleLabel, gbc);

        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(new Font("Arial", Font.PLAIN, 24));
        emailLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        add(emailLabel, gbc);

        emailField = new JTextField(20);
        emailField.setFont(new Font("Arial", Font.PLAIN, 24));
        gbc.gridx = 1;
        gbc.gridy = 2;
        add(emailField, gbc);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 24));
        passwordLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 3;
        add(passwordLabel, gbc);

        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 24));
        gbc.gridx = 1;
        gbc.gridy = 3;
        add(passwordField, gbc);

        loginButton = new JButton("LOGIN");
        loginButton.setFont(new Font("Arial", Font.BOLD, 30));
        loginButton.setPreferredSize(new Dimension(200, 80));
        loginButton.setForeground(Color.WHITE);
        loginButton.setBackground(Color.decode("#fd9b4d"));
        loginButton.setOpaque(true);
        loginButton.setBorderPainted(false);
        loginButton.addActionListener(this);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        add(loginButton, gbc);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loginButton) {
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();

            if (!email.matches("^[^@]+@gmail\\.com$")) {
                JOptionPane.showMessageDialog(this, "Invalid email. It must be a Gmail address.");
                return;
            }

            try {
                Connection conn = DBConnector.getConnection();
                String sql = "SELECT * FROM users WHERE email = ? AND password = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, email);
                pstmt.setString(2, password);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    JOptionPane.showMessageDialog(this, "Login successful!");
                     new userDashboard(email);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid email or password. Please try again.");
                }
                conn.close();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "An error occurred. Please try again.");
            }
        } else if (e.getSource() == goBackButton) {
            new indexFrame();
            this.dispose();
        }
    }

}



