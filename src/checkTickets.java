import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.sql.*;

public class checkTickets extends JFrame implements ActionListener {
    private final JPanel ticketPanel;
    private final String userEmail;
    private final JButton backButton;

    public checkTickets(String userEmail) {
        this.userEmail = userEmail;
        setTitle("Check Tickets");
        setResizable(false);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        getContentPane().setBackground(Color.decode("#0F149a"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel titleLabel = new JLabel("Tickets", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 48));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setPreferredSize(new Dimension(getWidth(), 80));
        add(titleLabel, BorderLayout.NORTH);

        ticketPanel = new JPanel(new GridBagLayout());
        ticketPanel.setBackground(Color.decode("#0F149a"));
        ticketPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JScrollPane scrollPane = new JScrollPane(ticketPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.decode("#0F149a"));
        add(scrollPane, BorderLayout.CENTER);

        backButton = new JButton("Go Back");
        backButton.setFont(new Font("Arial", Font.BOLD, 36));
        backButton.setForeground(Color.WHITE);
        backButton.setBackground(Color.decode("#fd9b4d"));
        backButton.setFocusPainted(false);
        backButton.addActionListener(this);
        add(backButton, BorderLayout.SOUTH);

        loadTickets();

        setVisible(true);
    }

    private void loadTickets() {
        ticketPanel.removeAll();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        try (Connection conn = DBConnector.getConnection()) {
            String sql = """
                SELECT user_fullname, booking_id, flight, origin, destination, departure, seats_selected, price
                FROM tickets
                WHERE user_id = (SELECT id FROM users WHERE email = ?);
            """;

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userEmail);
            ResultSet rs = pstmt.executeQuery();

            int row = 0;
            while (rs.next()) {
                String fullname = rs.getString("user_fullname");
                int bookingId = rs.getInt("booking_id");
                String flight = rs.getString("flight");
                String origin = rs.getString("origin");
                String destination = rs.getString("destination");
                String departure = rs.getString("departure");
                String seats = rs.getString("seats_selected");
                double price = rs.getDouble("price");

                String[] seatArray = seats.split(",");
                StringBuilder formattedSeats = new StringBuilder();
                if (seatArray.length >= 5) {
                    for (int i = 0; i < seatArray.length; i++) {
                        formattedSeats.append(seatArray[i]);
                        if ((i + 1) % 5 == 0) {
                            formattedSeats.append("<br>");
                        } else {
                            formattedSeats.append(", ");
                        }
                    }
                } else {
                    formattedSeats.append(seats);
                }

                String ticketText = String.format(
                        """
                        Full Name: %s<br>
                        Booking ID: %d<br>
                        Flight: %s<br>
                        Origin: %s<br>
                        Destination: %s<br>
                        Departure: %s<br>
                        Seats Selected: %s<br>
                        Price: $%.2f
                        """,
                        fullname, bookingId, flight, origin, destination, departure, formattedSeats.toString(), price
                );

                if (row > 0) {
                    JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
                    separator.setForeground(Color.WHITE);
                    gbc.gridx = 0;
                    gbc.gridy = row * 2 - 1;
                    gbc.gridwidth = 3;
                    ticketPanel.add(separator, gbc);
                }

                gbc.gridx = 0;
                gbc.gridy = row * 2;
                gbc.gridwidth = 1;

                JLabel ticketLabel = new JLabel("<html>" + ticketText + "</html>");
                ticketLabel.setFont(new Font("Arial", Font.PLAIN, 28));
                ticketLabel.setForeground(Color.WHITE);
                ticketPanel.add(ticketLabel, gbc);

                JButton printButton = new JButton("Print");
                printButton.setFont(new Font("Arial", Font.BOLD, 28));
                printButton.setForeground(Color.WHITE);
                printButton.setBackground(Color.decode("#fd9b4d"));
                printButton.setFocusPainted(false);
                printButton.putClientProperty("ticketDetails", ticketText);
                printButton.addActionListener(this);
                gbc.gridx = 1;
                ticketPanel.add(printButton, gbc);

                JButton removeButton = new JButton("Remove");
                removeButton.setFont(new Font("Arial", Font.BOLD, 28));
                removeButton.setForeground(Color.WHITE);
                removeButton.setBackground(Color.decode("#e74c3c"));
                removeButton.setFocusPainted(false);
                removeButton.putClientProperty("bookingId", bookingId);
                removeButton.addActionListener(this);
                gbc.gridx = 2;
                ticketPanel.add(removeButton, gbc);

                row++;
            }

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading tickets: " + e.getMessage());
        }

        revalidate();
        repaint();
    }




    @Override
    public void actionPerformed(ActionEvent e) {
        JButton sourceButton = (JButton) e.getSource();

        if (sourceButton.equals(backButton)) {
            this.dispose();
            new userDashboard(userEmail);
        } else if (sourceButton.getText().equals("Print")) {
            String ticketDetails = (String) sourceButton.getClientProperty("ticketDetails");
            System.out.println("Printing Ticket:\n" + ticketDetails.replace("<br>", "\n"));
            printTicket(ticketDetails.replace("<br>", "\n"));
            JOptionPane.showMessageDialog(this, "Ticket sent to the printer!");
        } else if (sourceButton.getText().equals("Remove")) {
            int bookingId = (int) sourceButton.getClientProperty("bookingId");
            removeTicketFromDatabase(bookingId);
            loadTickets();
        }
    }


    private void printTicket(String ticketDetails) {
        PrinterJob printerJob = PrinterJob.getPrinterJob();
        printerJob.setJobName("Ticket Printing");

        printerJob.setPrintable((graphics, pageFormat, pageIndex) -> {
            if (pageIndex > 0) {
                return Printable.NO_SUCH_PAGE;
            }

            Graphics2D g2d = (Graphics2D) graphics;
            g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
            g2d.setFont(new Font("Monospaced", Font.PLAIN, 12));

            int y = 20;
            for (String line : ticketDetails.split("\n")) {
                g2d.drawString(line, 10, y);
                y += 15;
            }

            return Printable.PAGE_EXISTS;
        });

        boolean doPrint = printerJob.printDialog();
        if (doPrint) {
            try {
                printerJob.print();
            } catch (PrinterException e) {
                JOptionPane.showMessageDialog(this, "Printing failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void removeTicketFromDatabase(int bookingId) {
        try (Connection conn = DBConnector.getConnection()) {
            String sql = "DELETE FROM tickets WHERE booking_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, bookingId);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Ticket removed successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to remove the ticket.");
            }

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error removing ticket: " + e.getMessage());
        }
    }

}
