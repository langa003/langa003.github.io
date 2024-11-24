import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;
import java.util.List;

class User {
    String name;
    String surname;
    String email;
    String address;
    String meterNumber;

    public User(String name, String surname, String email, String address, String meterNumber) {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.address = address;
        this.meterNumber = meterNumber;
    }

    public String getMeterNumber() {
        return meterNumber;
    }

    @Override
    public String toString() {
        return name + "," + surname + "," + email + "," + address + "," + meterNumber;
    }
}

class Token {
    private String meterNumber;
    private int kilowatts;
    private static int lastTokenNumber = 10654327; // Static variable to keep track of last token
    private int tokenNumber;

    public Token(String meterNumber, int kilowatts) {
        this.meterNumber = meterNumber;
        this.kilowatts = kilowatts;
        this.tokenNumber = generateToken(); // Generate token when a new Token is created
    }

    // Incremental token generation method
    private int generateToken() {
        lastTokenNumber++; // Increment the token number
        return lastTokenNumber; // Return the new unique token number
    }

    public int getTokenNumber() {
        return tokenNumber;
    }

    public int getKilowatts() {
        return kilowatts;
    }
}

class UserRegistrationException extends Exception {
    public UserRegistrationException(String message) {
        super(message);
    }
}

public class ElectricityBillingSystem {
    private List<User> users = new ArrayList<>();
    private JFrame frame;
    private JTextArea outputArea; // Area for displaying token info

    public ElectricityBillingSystem() {
        loadUsers();
        createAndShowGUI();
    }

    private void loadUsers() {
        File userFile = new File("users.csv");
        if (userFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(userFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] details = line.split(",");
                    if (details.length == 5) {
                        users.add(new User(details[0], details[1], details[2], details[3], details[4]));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void createAndShowGUI() {
        frame = new JFrame("Electricity Billing System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new FlowLayout());
        frame.getContentPane().setBackground(Color.YELLOW); // Set yellow background

        JButton registerButton = new JButton("Register");
        JButton signInButton = new JButton("Sign In");

        registerButton.addActionListener(new RegisterAction());
        signInButton.addActionListener(new SignInAction());

        outputArea = new JTextArea(10, 30); // Text area to display token information
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea); // Add a scroll pane for better usability
        scrollPane.setPreferredSize(new Dimension(300, 200));

        frame.add(registerButton);
        frame.add(signInButton);
        frame.add(scrollPane); // Add the output area to the frame
        frame.setSize(400, 300);
        frame.setVisible(true);
    }

    private class RegisterAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JTextField nameField = new JTextField(10);
            JTextField surnameField = new JTextField(10);
            JTextField emailField = new JTextField(10);
            JTextField addressField = new JTextField(10);
            JTextField meterField = new JTextField(10);

            JPanel panel = new JPanel();
            panel.setLayout(new GridLayout(6, 2));
            panel.add(new JLabel("Name:"));
            panel.add(nameField);
            panel.add(new JLabel("Surname:"));
            panel.add(surnameField);
            panel.add(new JLabel("Email:"));
            panel.add(emailField);
            panel.add(new JLabel("Address:"));
            panel.add(addressField);
            panel.add(new JLabel("Meter Number:"));
            panel.add(meterField);

            int result = JOptionPane.showConfirmDialog(frame, panel, "Register", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                try {
                    registerUser(nameField.getText(), surnameField.getText(), emailField.getText(),
                            addressField.getText(), meterField.getText());
                } catch (UserRegistrationException ex) {
                    JOptionPane.showMessageDialog(frame, ex.getMessage());
                }
            }
        }
    }

    private class SignInAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JTextField meterField = new JTextField(10);
            JPanel panel = new JPanel();
            panel.add(new JLabel("Meter Number:"));
            panel.add(meterField);
            int result = JOptionPane.showConfirmDialog(frame, panel, "Sign In", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                String meterNumber = meterField.getText();
                User user = findUserByMeterNumber(meterNumber);
                if (user != null) {
                    buyTokens(user);
                } else {
                    JOptionPane.showMessageDialog(frame, "User not found.");
                }
            }
        }
    }

    private void registerUser(String name, String surname, String email, String address, String meterNumber) throws UserRegistrationException {
        for (User user : users) {
            if (user.getMeterNumber().equals(meterNumber)) {
                throw new UserRegistrationException("Meter number already registered.");
            }
        }
        User newUser = new User(name, surname, email, address, meterNumber);
        users.add(newUser);
        saveUser(newUser);
        JOptionPane.showMessageDialog(frame, "Registered successfully.");
    }

    private void saveUser(User user) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("users.csv", true))) {
            bw.write(user.toString());
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private User findUserByMeterNumber(String meterNumber) {
        for (User user : users) {
            if (user.getMeterNumber().equals(meterNumber)) {
                return user;
            }
        }
        return null;
    }

    private void buyTokens(User user) {
        JTextField amountField = new JTextField(10);
        JPanel panel = new JPanel();
        panel.add(new JLabel("Amount in $:"));
        panel.add(amountField);
        int result = JOptionPane.showConfirmDialog(frame, panel, "Buy Tokens", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try {
                int amount = Integer.parseInt(amountField.getText());
                int kilowatts = (int) (amount * 5); // Assuming 1 dollar buys 5 kWh
                Token token = new Token(user.getMeterNumber(), kilowatts);
                // Display the token number and kilowatts in the output area
                outputArea.append("Token Number: " + token.getTokenNumber() + " | Kilowatts: " + kilowatts + "\n");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Invalid amount entered.");
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ElectricityBillingSystem::new);
    }
}