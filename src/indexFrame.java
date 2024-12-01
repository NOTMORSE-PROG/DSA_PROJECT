import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class indexFrame extends JFrame implements ActionListener {
    private final JButton registerButton;
    private final JButton loginButton;

    public indexFrame() {
        setTitle("SKY RESERVE");
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

        JLabel titleLabel = new JLabel("SKY RESERVE");
        titleLabel.setFont(new Font("Serif", Font.BOLD, 50));
        titleLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 60, 0);
        add(titleLabel, gbc);

        loginButton = new JButton("LOGIN");
        loginButton.setFont(new Font("Arial", Font.BOLD, 30));
        loginButton.setPreferredSize(new Dimension(300, 80));
        loginButton.setForeground(Color.WHITE);
        loginButton.setBackground(Color.decode("#fd9b4d"));
        loginButton.setOpaque(true);
        loginButton.setBorderPainted(false);
        loginButton.addActionListener(this);
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(loginButton, gbc);

        registerButton = new JButton("REGISTER");
        registerButton.setFont(new Font("Arial", Font.BOLD, 30));
        registerButton.setPreferredSize(new Dimension(300, 80));
        registerButton.setForeground(Color.WHITE);
        registerButton.setBackground(Color.decode("#fd9b4d"));
        registerButton.setOpaque(true);
        registerButton.setBorderPainted(false);
        registerButton.addActionListener(this);
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(registerButton, gbc);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == registerButton) {
            new registerPage();
            dispose();
        } else if (e.getSource() == loginButton) {
            new loginPage();
            dispose();
        }
    }

}
