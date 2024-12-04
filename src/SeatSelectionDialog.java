import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class SeatSelectionDialog extends JDialog implements ActionListener {
    private final Flight selectedFlight;
    private final List<Integer> selectedSeats;
    private final String userEmail;

    public SeatSelectionDialog(JFrame parent, Flight flight, String userEmail) {
        super(parent, "Select Seats", true);
        this.selectedFlight = flight;
        this.selectedSeats = new ArrayList<>();
        this.userEmail = userEmail;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        JPanel seatGridPanel = new JPanel(new GridLayout(10, 6, 5, 5));

        List<Integer> bookedSeats = selectedFlight.getBookedSeats();
        for (int i = 1; i <= 60; i++) {
            JToggleButton seatButton = getJToggleButton(i, bookedSeats);
            seatGridPanel.add(seatButton);
        }


        JPanel buttonPanel = getJPanel();
        add(new JLabel("Select Your Seats"), BorderLayout.NORTH);
        add(seatGridPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JToggleButton getJToggleButton(int i, List<Integer> bookedSeats) {
        JToggleButton seatButton = new JToggleButton(String.valueOf(i));
        seatButton.setPreferredSize(new Dimension(50, 50));

        if (bookedSeats.contains(i)) {
            seatButton.setBackground(Color.RED);
            seatButton.setEnabled(false);
        } else {
            seatButton.setBackground(Color.GREEN);
            seatButton.addActionListener(_ -> {
                if (seatButton.isSelected()) {
                    selectedSeats.add(Integer.parseInt(seatButton.getText()));
                } else {
                    selectedSeats.remove(Integer.valueOf(seatButton.getText()));
                }
            });
        }
        return seatButton;
    }

    private JPanel getJPanel() {
        JPanel buttonPanel = new JPanel();
        JButton proceedButton = new JButton("Proceed to Payment");
        JButton cancelButton = new JButton("Cancel");

        proceedButton.addActionListener(this);
        cancelButton.addActionListener(this);

        buttonPanel.add(proceedButton);
        buttonPanel.add(cancelButton);
        return buttonPanel;
    }

    private void openPaymentDialog() {
        String[] paymentMethods = {"Credit Card", "E-Wallet"};
        int choice = JOptionPane.showOptionDialog(
                this,
                "Select Payment Method",
                "Payment",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                paymentMethods,
                paymentMethods[0]
        );

        if (choice == 0) {
            openCreditCardPayment();
        } else if (choice == 1) {
            openEWalletProviderSelection();
        }
    }

    private void openEWalletProviderSelection() {
        String[] eWalletProviders = {"Maya", "GCash"};
        int providerChoice = JOptionPane.showOptionDialog(
                this,
                "Select E-Wallet Provider",
                "E-Wallet Payment",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                eWalletProviders,
                eWalletProviders[0]
        );

        if (providerChoice == JOptionPane.CLOSED_OPTION) {
            JOptionPane.showMessageDialog(this, "Payment canceled.");
            return;
        }

        String selectedProvider = eWalletProviders[providerChoice];
        processEWalletPayment(selectedProvider);
    }

    private void processEWalletPayment(String provider) {
        while (true) {
            String ewalletNumber = JOptionPane.showInputDialog(
                    this,
                    "Enter 11-digit " + provider + " Number (starting with 09):"
            );

            if (ewalletNumber == null) {
                JOptionPane.showMessageDialog(this, "Payment canceled.");
                return;
            }

            if (!ewalletNumber.matches("\\d+")) {
                JOptionPane.showMessageDialog(this, "E-Wallet number must contain only numbers.");
                continue;
            }

            if (ewalletNumber.matches("^09\\d{9}$")) {
                JOptionPane.showMessageDialog(this, "Payment successful via " + provider + ".");
                processPayment(getUserEmail());
                break;
            } else {
                JOptionPane.showMessageDialog(this, "Invalid E-Wallet number. Must start with 09 and be 11 digits.");
            }
        }
    }


    private void processPayment(String userEmail) {
        int randomId = 100000 + new Random().nextInt(900000);
        double totalPrice = selectedFlight.getPrice() * selectedSeats.size();
        StringBuilder receipt = new StringBuilder();
        receipt.append("Flight Booking Receipt\n\n");
        receipt.append("Booking ID: ").append(randomId).append("\n");
        receipt.append("Flight: ").append(selectedFlight.getFlightName()).append("\n");
        receipt.append("From: ").append(selectedFlight.getOrigin()).append("\n");
        receipt.append("To: ").append(selectedFlight.getDestination()).append("\n");

        LocalDateTime departureLocalDateTime = selectedFlight.getDepartureTime();
        Date departureTime = Date.from(departureLocalDateTime.atZone(ZoneId.systemDefault()).toInstant());

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
        String formattedDepartureTime = dateFormat.format(departureTime);

        receipt.append("Departure: ").append(formattedDepartureTime).append("\n");

        String seatsString = selectedSeats.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        receipt.append("Seats Selected: ").append(seatsString).append("\n");
        receipt.append("Total Price: ₱").append(String.format("%.2f", totalPrice)).append("\n");

        selectedFlight.bookSeats(selectedSeats);

        String userFullName = "";
        try (Connection connection = DBConnector.getConnection()) {
            String getUserFullNameQuery = "SELECT CONCAT(first_name, ' ', last_name) AS full_name FROM users WHERE email = ?";
            PreparedStatement userStatement = connection.prepareStatement(getUserFullNameQuery);
            userStatement.setString(1, userEmail);
            ResultSet resultSet = userStatement.executeQuery();
            if (resultSet.next()) {
                userFullName = resultSet.getString("full_name");
            }

            String sql = """
            INSERT INTO tickets (user_id, user_fullname, booking_id, flight, origin, destination, departure, seats_selected, price)
            VALUES ((SELECT id FROM users WHERE email = ?), ?, ?, ?, ?, ?, ?, ?, ?);
        """;
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, userEmail);
            statement.setString(2, userFullName);
            statement.setInt(3, randomId);
            statement.setString(4, selectedFlight.getFlightName());
            statement.setString(5, selectedFlight.getOrigin());
            statement.setString(6, selectedFlight.getDestination());
            statement.setTimestamp(7, new java.sql.Timestamp(departureTime.getTime()));
            statement.setString(8, seatsString);
            statement.setDouble(9, totalPrice);
            statement.executeUpdate();

            String transactionSql = """
            INSERT INTO transaction_history (user_id, transaction_type, flight_name, seats_selected)
            VALUES ((SELECT id FROM users WHERE email = ?), 'BOOKED', ?, ?);
        """;
            PreparedStatement transactionStatement = connection.prepareStatement(transactionSql);
            transactionStatement.setString(1, userEmail);
            transactionStatement.setString(2, selectedFlight.getFlightName());
            transactionStatement.setString(3, seatsString);
            transactionStatement.executeUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        JTextArea receiptArea = new JTextArea(receipt.toString());
        receiptArea.setEditable(false);
        JOptionPane.showMessageDialog(
                this,
                new JScrollPane(receiptArea),
                "Booking Confirmation",
                JOptionPane.INFORMATION_MESSAGE
        );

        int confirmAction = JOptionPane.showConfirmDialog(
                this,
                "Would you like to print the Ticket?",
                "Booking Complete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirmAction == JOptionPane.YES_OPTION) {
            printTicket(receipt.toString());
        }
        dispose();
    }

    private void printTicket(String ticketContent) {
        try {
            PrinterJob printerJob = getPrinterJob(ticketContent);

            if (printerJob.printDialog()) {
                printerJob.print();
                JOptionPane.showMessageDialog(
                        this,
                        "Ticket printed successfully!",
                        "Print Confirmation",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        } catch (PrinterException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Error printing Ticket: " + e.getMessage(),
                    "Print Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private static PrinterJob getPrinterJob(String receiptContent) {
        PrinterJob printerJob = PrinterJob.getPrinterJob();

        printerJob.setPrintable((graphics, pageFormat, pageIndex) -> {
            if (pageIndex > 0) {
                return Printable.NO_SUCH_PAGE;
            }

            Graphics2D g2d = (Graphics2D) graphics;
            g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

            String[] lines = receiptContent.split("\n");
            Font receiptFont = new Font("Monospaced", Font.PLAIN, 10);
            g2d.setFont(receiptFont);

            int y = 50;
            for (String line : lines) {
                g2d.drawString(line, 50, y);
                y += 15;
            }

            return Printable.PAGE_EXISTS;
        });
        return printerJob;
    }

    private void openCreditCardPayment() {
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
                    this, panel, "Credit Card Payment",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
            );

            if (result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION) {
                return;
            }

            if (result == JOptionPane.OK_OPTION) {
                if (cardNumberField.getText().trim().isEmpty() ||
                        expiryField.getText().trim().isEmpty() ||
                        cvvField.getText().trim().isEmpty() ||
                        nameField.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "All fields must be filled.");
                    continue;
                }

                if (isNumeric(cardNumberField.getText())) {
                    JOptionPane.showMessageDialog(this, "Card Number must contain only numbers.");
                    continue;
                }

                if (isNumeric(cvvField.getText())) {
                    JOptionPane.showMessageDialog(this, "CVV must contain only numbers.");
                    continue;
                }

                if (!isValidName(nameField.getText())) {
                    JOptionPane.showMessageDialog(this, "Cardholder Name must contain only letters and spaces.");
                    continue;
                }

                String formattedCardNumber = formatCreditCardNumber(cardNumberField.getText());
                String expiryDate = expiryField.getText();

                if (!isExpiryDateValid(expiryDate)) {
                    JOptionPane.showMessageDialog(this, "Credit card has expired or is invalid (expires December 2024 or earlier).");
                    continue;
                }

                if (validateCreditCardDetails(formattedCardNumber, expiryDate, cvvField.getText())) {
                    processPayment(getUserEmail());
                    break;
                } else {
                    int choice = JOptionPane.showConfirmDialog(
                            this,
                            "Invalid card details. Would you like to try again?",
                            "Payment Error",
                            JOptionPane.YES_NO_OPTION
                    );

                    if (choice == JOptionPane.NO_OPTION) {
                        return;
                    }
                }
            }
        }
    }

    private boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return true;
        }
        return !str.matches("\\d+");
    }

    private boolean isValidName(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        return str.matches("^[a-zA-Z ]+$");
    }


    private boolean validateCreditCardDetails(String cardNumber, String expiry, String cvv) {
        String cleanCardNumber = cardNumber.replaceAll("\\D", "");

        boolean isValidNumber = isValidCreditCardNumber(cleanCardNumber);

        boolean isValidExpiry = expiry.matches("^(0[1-9]|1[0-2])/\\d{2}$");

        boolean isValidCVV = cvv.matches("\\d{3,4}");

        return isValidNumber && isValidExpiry && isValidCVV;
    }

    private boolean isExpiryDateValid(String expiryDate) {
        try {
            java.time.LocalDate currentDate = java.time.LocalDate.now();
            int month = Integer.parseInt(expiryDate.substring(0, 2));
            int year = Integer.parseInt("20" + expiryDate.substring(3, 5));

            java.time.LocalDate expiry = java.time.LocalDate.of(year, month, 1);

            if (expiry.isBefore(currentDate)) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private boolean isValidCreditCardNumber(String cardNumber) {
        int sum = 0;
        boolean alternate = false;

        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cardNumber.charAt(i));

            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit = (digit % 10) + 1;
                }
            }

            sum += digit;
            alternate = !alternate;
        }

        return (sum % 10 == 0);
    }

    private String formatCreditCardNumber(String cardNumber) {
        return cardNumber.replaceAll("[^0-9]", "");
    }

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source instanceof JButton button) {
            if (button.getText().equals("Proceed to Payment")) {
                if (selectedSeats.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please select at least one seat.");
                    return;
                }
                openPaymentDialog();
            } else if (button.getText().equals("Cancel")) {
                dispose();
            }
        }
    }

    public String getUserEmail() {
        return userEmail;
    }
}
