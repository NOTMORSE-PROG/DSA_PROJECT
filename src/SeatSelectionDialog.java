import javax.swing.*;
import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.ArrayList;
import java.util.List;

public class SeatSelectionDialog extends JDialog {
    private Flight selectedFlight;
    private List<JToggleButton> seatButtons;
    private List<Integer> selectedSeats;

    public SeatSelectionDialog(JFrame parent, Flight flight) {
        super(parent, "Select Seats", true);
        this.selectedFlight = flight;
        this.selectedSeats = new ArrayList<>();
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        JPanel seatGridPanel = new JPanel(new GridLayout(10, 6, 5, 5));
        seatButtons = new ArrayList<>();

        List<Integer> bookedSeats = selectedFlight.getBookedSeats();
        for (int i = 1; i <= 60; i++) {
            JToggleButton seatButton = getJToggleButton(i, bookedSeats);

            seatButtons.add(seatButton);
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
            seatButton.addActionListener(e -> {
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

        proceedButton.addActionListener(e -> {
            if (selectedSeats.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select at least one seat.");
                return;
            }
            selectedFlight.bookSeats(selectedSeats);
            openPaymentDialog();
        });

        cancelButton.addActionListener(e -> dispose());

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
            openEWalletPayment();
        }
    }

    private void openEWalletPayment() {
        String ewalletNumber = JOptionPane.showInputDialog(this, "Enter 11-digit E-Wallet Number (starting with 09):");

        if (ewalletNumber != null && ewalletNumber.matches("^09\\d{9}$")) {
            processPayment();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid E-Wallet number. Must start with 09 and be 11 digits.");
        }
    }

    private boolean validateCreditCard(String cardNumber, String expiry, String cvv) {
        return cardNumber.replaceAll("\\s", "").length() >= 13 &&
                expiry.matches("^(0[1-9]|1[0-2])/\\d{2}$") &&
                cvv.matches("\\d{3}");
    }

    private void processPayment() {
        double totalPrice = selectedFlight.getPrice() * selectedSeats.size();

        StringBuilder receipt = new StringBuilder();
        receipt.append("Flight Booking Receipt\n\n");
        receipt.append("Flight: ").append(selectedFlight.getFlightName()).append("\n");
        receipt.append("From: ").append(selectedFlight.getOrigin()).append("\n");
        receipt.append("To: ").append(selectedFlight.getDestination()).append("\n");
        receipt.append("Departure: ").append(selectedFlight.getDepartureTime()).append("\n");
        receipt.append("Seats Selected: ").append(selectedSeats).append("\n");
        receipt.append("Total Price: ₱").append(String.format("%.2f", totalPrice)).append("\n");

        selectedFlight.bookSeats(selectedSeats.size());

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
                "Would you like to print the receipt or save it?",
                "Booking Complete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirmAction == JOptionPane.YES_OPTION) {
            printReceipt(receipt.toString());
        }
        dispose();
    }

    private void printReceipt(String receiptContent) {
        try {
            PrinterJob printerJob = getPrinterJob(receiptContent);

            if (printerJob.printDialog()) {
                printerJob.print();
                JOptionPane.showMessageDialog(
                        this,
                        "Receipt printed successfully!",
                        "Print Confirmation",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        } catch (PrinterException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Error printing receipt: " + e.getMessage(),
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

    private boolean validateCreditCardDetails(String cardNumber, String expiry, String cvv) {
        String cleanCardNumber = cardNumber.replaceAll("\\D", "");

        boolean isValidNumber = validateLuhnAlgorithm(cleanCardNumber);

        boolean isValidExpiry = expiry.matches("^(0[1-9]|1[0-2])/\\d{2}$");

        boolean isValidCVV = cvv.matches("\\d{3,4}");

        return isValidNumber && isValidExpiry && isValidCVV;
    }

    private void openCreditCardPayment() {
        JPanel panel = new JPanel(new GridLayout(4, 2));
        JTextField cardNumberField = new JTextField();
        JTextField expiryField = new JTextField();
        JTextField cvvField = new JTextField();
        JTextField nameField = new JTextField();

        panel.add(new JLabel("Card Number:"));
        panel.add(cardNumberField);
        panel.add(new JLabel("Expiry Date (MM/YY):"));
        panel.add(expiryField);
        panel.add(new JLabel("CVV:"));
        panel.add(cvvField);
        panel.add(new JLabel("Cardholder Name:"));
        panel.add(nameField);

        int result = JOptionPane.showConfirmDialog(
                this, panel, "Credit Card Payment",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String formattedCardNumber = formatCreditCardNumber(cardNumberField.getText());
            if (validateCreditCardDetails(formattedCardNumber, expiryField.getText(), cvvField.getText())) {
                processPayment();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid card details. Please try again.");
            }
        }
    }

    private boolean validateLuhnAlgorithm(String cardNumber) {
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
        String digitsOnly = cardNumber.replaceAll("\\D", "");

        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < digitsOnly.length(); i++) {
            if (i > 0 && i % 4 == 0) {
                formatted.append(" ");
            }
            formatted.append(digitsOnly.charAt(i));
        }

        return formatted.toString();
    }
}