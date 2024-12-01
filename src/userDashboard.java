import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class userDashboard extends JFrame implements ActionListener {
    private final JButton bookFlightButton, checkTicketsButton, profileSettingsButton, logoutButton;
    private final String userEmail;

    public userDashboard(String userEmail) {
        this.userEmail = userEmail;

        setTitle("USER Dashboard");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(Color.decode("#0F149a"));
        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("SKY COMMERCE", JLabel.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 50));
        titleLabel.setForeground(Color.WHITE);
        add(titleLabel, BorderLayout.NORTH);

        String userFullName = fetchUserName(userEmail);
        JLabel userNameLabel = new JLabel("Hello, " + userFullName, JLabel.CENTER);
        userNameLabel.setFont(new Font("Arial", Font.PLAIN, 40));
        userNameLabel.setForeground(Color.WHITE);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridBagLayout());
        buttonPanel.setBackground(Color.decode("#0F149a"));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10);
        buttonPanel.add(userNameLabel, gbc);

        gbc.gridy = 1;
        bookFlightButton = createButton("Book a Flight");
        buttonPanel.add(bookFlightButton, gbc);

        gbc.gridy = 2;
        checkTicketsButton = createButton("Check Tickets");
        buttonPanel.add(checkTicketsButton, gbc);

        gbc.gridy = 3;
        profileSettingsButton = createButton("Profile Settings");
        buttonPanel.add(profileSettingsButton, gbc);

        add(buttonPanel, BorderLayout.CENTER);

        logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Arial", Font.PLAIN, 30));
        logoutButton.setPreferredSize(new Dimension(200, 50));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setBackground(Color.RED);
        logoutButton.setOpaque(true);
        logoutButton.setBorderPainted(false);
        logoutButton.addActionListener(this);

        JPanel logoutPanel = new JPanel();
        logoutPanel.setBackground(Color.decode("#0F149a"));
        logoutPanel.add(logoutButton);
        add(logoutPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 35));
        button.setPreferredSize(new Dimension(500, 60));
        button.setForeground(Color.WHITE);
        button.setBackground(Color.decode("#fd9b4d"));
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.addActionListener(this);
        return button;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == bookFlightButton) {
            this.dispose();
            new bookFlight(userEmail);
        } else if (e.getSource() == checkTicketsButton) {
            JOptionPane.showMessageDialog(this, "Navigating to 'Check Tickets'...");
        } else if (e.getSource() == profileSettingsButton) {
            JOptionPane.showMessageDialog(this, "Navigating to 'Profile Settings'...");
        } else if (e.getSource() == logoutButton) {
            JOptionPane.showMessageDialog(this, "Logging out...");
            this.dispose();
            new loginPage();
        }
    }

    private String fetchUserName(String userEmail) {
        String fullName = "";
        try {
            Connection conn = DBConnector.getConnection();
            String sql = "SELECT first_name, middle_name, last_name FROM users WHERE email = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userEmail);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String firstName = rs.getString("first_name");
                String middleName = rs.getString("middle_name");
                String lastName = rs.getString("last_name");

                String middleInitial = (middleName != null && !middleName.isEmpty())
                        ? middleName.substring(0, 1).toUpperCase() + "."
                        : "";
                fullName = firstName.substring(0, 1).toUpperCase() + firstName.substring(1) + " " +
                        middleInitial + " " +
                        lastName.substring(0, 1).toUpperCase() + lastName.substring(1);
            }
            conn.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return fullName;
    }
}
