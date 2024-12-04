import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.sql.*;
import java.time.LocalDate;

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

                StringBuilder formattedSeats = getStringBuilder(seats);

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
                        fullname, bookingId, flight, origin, destination, departure, formattedSeats, price
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

                JButton removeButton = new JButton("Cancel Flight");
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

    private static StringBuilder getStringBuilder(String seats) {
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
        return formattedSeats;
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
        } else if (sourceButton.getText().equals("Cancel Flight")) {
            int bookingId = (int) sourceButton.getClientProperty("bookingId");
            cancelFlight(bookingId, userEmail);
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

    private void cancelFlight(int bookingId, String userEmail) {
        String flightName;
        String seatsSelected;
        double ticketPrice;

        try (Connection conn = DBConnector.getConnection()) {
            String getFlightDetailsSQL = """
            SELECT tickets.flight, tickets.seats_selected, tickets.price
            FROM tickets
            JOIN users ON tickets.user_id = users.id
            WHERE tickets.booking_id = ? AND users.email = ?;
        """;
            PreparedStatement getFlightDetailsStmt = conn.prepareStatement(getFlightDetailsSQL);
            getFlightDetailsStmt.setInt(1, bookingId);
            getFlightDetailsStmt.setString(2, userEmail);
            ResultSet rs = getFlightDetailsStmt.executeQuery();

            if (rs.next()) {
                flightName = rs.getString("flight");
                seatsSelected = rs.getString("seats_selected");
                ticketPrice = rs.getDouble("price");

                String reason;
                while (true) {
                    reason = JOptionPane.showInputDialog(this, "Please provide a reason for canceling the flight:");
                    if (reason == null) {
                        JOptionPane.showMessageDialog(this, "Flight Cancellation aborted.");
                        return;
                    }
                    if (reason.trim().isEmpty()) {
                        JOptionPane.showMessageDialog(this, "This field cannot be empty. Please provide a reason.");
                    } else {
                        break;
                    }
                }

                int numberOfSeats = seatsSelected.split(",").length;
                double refundAmount = ticketPrice - (numberOfSeats * 200);

                String[] refundMethods = {"Credit Card", "E-Wallet"};
                int methodChoice = JOptionPane.showOptionDialog(
                        this,
                        "Select Refund Method",
                        "Refund",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        refundMethods,
                        refundMethods[0]
                );

                if (methodChoice == JOptionPane.CLOSED_OPTION) {
                    JOptionPane.showMessageDialog(this, "Flight Cancellation aborted.");
                    return;
                }

                boolean refundSuccess;
                if (methodChoice == 0) {
                    refundSuccess = openCreditCardRefund();
                } else {
                    refundSuccess = openEWalletRefundWithProvider();
                }

                if (!refundSuccess) {
                    JOptionPane.showMessageDialog(this, "Flight Cancellation aborted.");
                    return;
                }

                String deleteTicketSQL = "DELETE FROM tickets WHERE booking_id = ?";
                PreparedStatement deleteTicketStmt = conn.prepareStatement(deleteTicketSQL);
                deleteTicketStmt.setInt(1, bookingId);
                deleteTicketStmt.executeUpdate();

                String updateBookedSeatsSQL = """
                UPDATE flights
                SET booked_seats = TRIM(BOTH ',' FROM REPLACE(
                    CONCAT(',', booked_seats, ','), CONCAT(',', ?, ','), ',')),
                    available_seats = available_seats + ?
                WHERE flight_name = ?;
            """;
                PreparedStatement updateBookedSeatsStmt = conn.prepareStatement(updateBookedSeatsSQL);
                updateBookedSeatsStmt.setString(1, seatsSelected);
                updateBookedSeatsStmt.setInt(2, numberOfSeats);
                updateBookedSeatsStmt.setString(3, flightName);
                updateBookedSeatsStmt.executeUpdate();

                String insertTransactionSQL = """
                INSERT INTO transaction_history (user_id, transaction_type, flight_name, seats_selected)
                VALUES ((SELECT id FROM users WHERE email = ?), 'CANCELLED', ?, ?);
            """;
                PreparedStatement insertTransactionStmt = conn.prepareStatement(insertTransactionSQL);
                insertTransactionStmt.setString(1, userEmail);
                insertTransactionStmt.setString(2, flightName);
                insertTransactionStmt.setString(3, seatsSelected);
                insertTransactionStmt.executeUpdate();

                JOptionPane.showMessageDialog(this, "Flight canceled successfully for the following reason:\n" + reason +
                        "\nInconvenience Fee (₱200 per seat)" +
                        "\nRefund Amount: ₱" + String.format("%.2f", refundAmount));
            } else {
                JOptionPane.showMessageDialog(this, "Error: Booking ID not found for the provided email.");
            }
        } catch (SQLException | ClassNotFoundException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error canceling flight: " + ex.getMessage());
        }
    }


    private boolean openEWalletRefundWithProvider() {
        String[] eWalletProviders = {"Maya", "GCash"};
        int providerChoice = JOptionPane.showOptionDialog(
                this,
                "Select E-Wallet Provider",
                "E-Wallet Refund",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                eWalletProviders,
                eWalletProviders[0]
        );

        if (providerChoice == JOptionPane.CLOSED_OPTION) {
            JOptionPane.showMessageDialog(this, "Refund canceled.");
            return false;
        }

        String selectedProvider = eWalletProviders[providerChoice];
        while (true) {
            String ewalletNumber = JOptionPane.showInputDialog(
                    this,
                    "Enter 11-digit " + selectedProvider + " Number (starting with 09):"
            );
            if (ewalletNumber == null) {
                JOptionPane.showMessageDialog(this, "Refund canceled.");
                return false;
            }
            if (!ewalletNumber.matches("\\d+")) {
                JOptionPane.showMessageDialog(this, "E-Wallet number must contain only numbers.");
                continue;
            }
            if (ewalletNumber.matches("^09\\d{9}$")) {
                int confirmation = JOptionPane.showConfirmDialog(
                        this,
                        "Confirm Refund using " + selectedProvider + " number: " + ewalletNumber,
                        "Refund Confirmation",
                        JOptionPane.OK_CANCEL_OPTION
                );
                if (confirmation == JOptionPane.OK_OPTION) {
                    JOptionPane.showMessageDialog(this, "Refund successful via " + selectedProvider + ".");
                    return true;
                } else {
                    JOptionPane.showMessageDialog(this, "Refund canceled.");
                    return false;
                }
            } else {
                JOptionPane.showMessageDialog(this, "Invalid E-Wallet number. Must start with 09 and be 11 digits.");
            }
        }
    }
    private boolean openCreditCardRefund() {
        while (true) {
            JPanel panel = new JPanel(new GridLayout(4, 2));
            JTextField cardNumberField = new JTextField();
            JTextField expiryField = new JTextField();
            JTextField cvvField = new JTextField();
            JTextField nameField = new JTextField();

            panel.add(new JLabel("Card Number: *"));
            panel.add(cardNumberField);
            panel.add(new JLabel("Expiry Date (MM/YY): *"));
            panel.add(expiryField);
            panel.add(new JLabel("CVV: *"));
            panel.add(cvvField);
            panel.add(new JLabel("Cardholder Name: *"));
            panel.add(nameField);

            int result = JOptionPane.showConfirmDialog(
                    this, panel, "Credit Card Refund",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
            );

            if (result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION) {
                return false;
            }

            if (result == JOptionPane.OK_OPTION) {
                try {
                    if (cardNumberField.getText().trim().isEmpty() ||
                            expiryField.getText().trim().isEmpty() ||
                            cvvField.getText().trim().isEmpty() ||
                            nameField.getText().trim().isEmpty()) {
                        throw new IllegalArgumentException("All fields must be filled.");
                    }

                    String cardNumber = cardNumberField.getText().trim();
                    if (cardNumber.startsWith("-")) {
                        throw new IllegalArgumentException("Card number cannot be negative.");
                    }

                    String formattedCardNumber = formatCreditCardNumber(cardNumber);
                    if (!isValidCreditCardNumber(formattedCardNumber)) {
                        throw new IllegalArgumentException("Invalid Card Number.");
                    }

                    String expiryDate = expiryField.getText();
                    if (!isExpiryDateValid(expiryDate)) {
                        throw new IllegalArgumentException("Credit card has expired or is invalid (expires December 2024 or earlier).");
                    }

                    if (!isNumeric(cvvField.getText())) {
                        throw new IllegalArgumentException("CVV must contain only numbers.");
                    }

                    if (!isValidName(nameField.getText())) {
                        throw new IllegalArgumentException("Cardholder Name must contain only letters and spaces.");
                    }

                    if (!expiryField.getText().matches("^(0[1-9]|1[0-2])/\\d{2}$")) {
                        throw new IllegalArgumentException("Invalid Expiry Date format (MM/YY).");
                    }

                    if (!cvvField.getText().matches("\\d{3,4}")) {
                        throw new IllegalArgumentException("Invalid CVV.");
                    }

                    int confirmation = JOptionPane.showConfirmDialog(
                            this,
                            "Confirm Refund to Credit Card?",
                            "Refund Confirmation",
                            JOptionPane.OK_CANCEL_OPTION
                    );

                    if (confirmation == JOptionPane.OK_OPTION) {
                        JOptionPane.showMessageDialog(this, "Refund successful.");
                        return true;
                    } else {
                        return false;
                    }

                } catch (IllegalArgumentException e) {
                    JOptionPane.showMessageDialog(this, e.getMessage());
                }
            }
        }
    }

    private boolean isValidCreditCardNumber(String cardNumber) {
        cardNumber = cardNumber.replaceAll("[^0-9]", "");
        if (cardNumber.length() != 16) {
            return false;
        }
        int sum = 0;
        boolean alternate = false;
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cardNumber.charAt(i));

            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            alternate = !alternate;
        }

        return (sum % 10 == 0);
    }

    private boolean isExpiryDateValid(String expiryDate) {
        try {
            LocalDate currentDate = LocalDate.now();
            int month = Integer.parseInt(expiryDate.substring(0, 2));
            int year = Integer.parseInt("20" + expiryDate.substring(3, 5));

            LocalDate expiry = LocalDate.of(year, month, 1);

            return !expiry.isBefore(currentDate);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isNumeric(String str) {
        return str.matches("\\d+");
    }

    private boolean isValidName(String name) {
        return name.matches("[a-zA-Z\\s]+");
    }

    private String formatCreditCardNumber(String cardNumber) {
        return cardNumber.replaceAll("[^0-9]", "");
    }

}
