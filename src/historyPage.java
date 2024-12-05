import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class historyPage extends JFrame implements ActionListener {

    private final String userEmail;
    private final JPanel historyPanel;

    public historyPage(String userEmail) {
        this.userEmail = userEmail;

        setTitle("Transaction History");
        setResizable(false);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        getContentPane().setBackground(Color.decode("#0F149a"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel titleLabel = new JLabel("Transaction History", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 48));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setPreferredSize(new Dimension(getWidth(), 80));
        add(titleLabel, BorderLayout.NORTH);

        historyPanel = new JPanel(new GridBagLayout());
        historyPanel.setBackground(Color.decode("#0F149a"));
        historyPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JScrollPane scrollPane = new JScrollPane(historyPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.decode("#0F149a"));
        add(scrollPane, BorderLayout.CENTER);

        JButton backButton = new JButton("Go Back");
        backButton.setFont(new Font("Arial", Font.BOLD, 36));
        backButton.setForeground(Color.WHITE);
        backButton.setBackground(Color.decode("#fd9b4d"));
        backButton.setFocusPainted(false);
        backButton.addActionListener(this);
        add(backButton, BorderLayout.SOUTH);

        loadTransactionHistory();

        setVisible(true);
    }

    private void loadTransactionHistory() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        try (Connection conn = DBConnector.getConnection()) {
            int userId = getUserId(conn);

            if (userId == -1) {
                JOptionPane.showMessageDialog(this, "User not found!");
                return;
            }

            String sql = """
                SELECT id, transaction_type, flight_name, seats_selected, transaction_date
                FROM transaction_history
                WHERE user_id = ?
                ORDER BY transaction_date DESC;
                """;

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            int row = 0;
            while (rs.next()) {
                int transactionId = rs.getInt("id");
                String transactionType = rs.getString("transaction_type");
                String flightName = rs.getString("flight_name");
                String seatsSelected = rs.getString("seats_selected");
                String transactionDate = rs.getString("transaction_date");

                StringBuilder formattedSeats = getFormattedSeats(seatsSelected);

                String historyText = String.format(
                        "Transaction Type: %s<br>Flight: %s<br>Seats: %s<br>Date: %s",
                        transactionType, flightName, formattedSeats, transactionDate
                );

                gbc.gridx = 0;
                gbc.gridy = row * 2;
                gbc.gridwidth = 2;

                JLabel historyLabel = new JLabel("<html>" + historyText + "</html>");
                historyLabel.setFont(new Font("Arial", Font.PLAIN, 32));
                historyLabel.setForeground(Color.WHITE);
                historyPanel.add(historyLabel, gbc);

                JButton removeButton = new JButton("Remove");
                removeButton.setFont(new Font("Arial", Font.BOLD, 28));
                removeButton.setForeground(Color.WHITE);
                removeButton.setBackground(Color.RED);
                removeButton.setFocusPainted(false);
                removeButton.putClientProperty("transactionId", transactionId);
                removeButton.addActionListener(this);

                gbc.gridx = 2;
                gbc.gridwidth = 1;
                historyPanel.add(removeButton, gbc);

                row++;
            }

            if (row == 0) {
                JLabel noHistoryLabel = new JLabel("No transaction history found!", JLabel.CENTER);
                noHistoryLabel.setFont(new Font("Arial", Font.BOLD, 36));
                noHistoryLabel.setForeground(Color.WHITE);
                gbc.gridx = 0;
                gbc.gridy = 0;
                gbc.gridwidth = 3;
                historyPanel.add(noHistoryLabel, gbc);
            }

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading transaction history: " + e.getMessage());
        }

        revalidate();
        repaint();
    }

    private static StringBuilder getFormattedSeats(String seats) {
        String[] seatArray = seats.split(",");
        StringBuilder formattedSeats = new StringBuilder();

        for (int i = 0; i < seatArray.length; i++) {
            formattedSeats.append(seatArray[i].trim());
            if ((i + 1) % 5 == 0 && i != seatArray.length - 1) {
                formattedSeats.append("<br>");
            } else {
                formattedSeats.append(", ");
            }
        }

        return formattedSeats;
    }


    private int getUserId(Connection conn) throws SQLException {
        String sql = "SELECT id FROM users WHERE email = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, userEmail);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            return rs.getInt("id");
        }
        return -1;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JButton sourceButton = (JButton) e.getSource();
        if (sourceButton.getText().equals("Go Back")) {
            this.dispose();
            new userDashboard(userEmail);
        } else if (sourceButton.getText().equals("Remove")) {
            int transactionId = (int) sourceButton.getClientProperty("transactionId");
            removeTransaction(transactionId);
        }
    }

    private void removeTransaction(int transactionId) {
        try (Connection conn = DBConnector.getConnection()) {
            String sql = "DELETE FROM transaction_history WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, transactionId);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Transaction removed successfully!");
                this.dispose();
                new historyPage(userEmail);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to remove transaction. It may have already been removed.");
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error removing transaction: " + e.getMessage());
        }
    }
}
