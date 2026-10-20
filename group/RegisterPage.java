import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.function.Consumer;

/** The register page: creates a new account in the MySQL users table. */
public class RegisterPage extends JPanel {

    private final JTextField     fullNameField = Theme.textField();
    private final JTextField     usernameField = Theme.textField();
    private final JPasswordField passwordField = Theme.passwordField();
    private final JPasswordField confirmField  = Theme.passwordField();
    private final JCheckBox      showPassword  = Theme.checkBox("Show passwords");
    private final JLabel         messageLabel  = Theme.messageLabel();
    private final JButton        registerButton = Theme.button("Create Account", Theme.GREEN);

    private final Consumer<String> onRegistered;      // gets the new username after a successful register

    public RegisterPage(Consumer<String> onRegistered, Runnable onBackToLogin) {
        this.onRegistered = onRegistered;

        setLayout(new BorderLayout());
        setBackground(Theme.BG);
        add(Theme.header("Create Account", "Register to use the inventory system"), BorderLayout.NORTH);
        add(buildForm(onBackToLogin), BorderLayout.CENTER);

        // Enter = jump to the next field (Enter in the last field presses "Create Account")
        fullNameField.addActionListener(e -> usernameField.requestFocusInWindow());
        usernameField.addActionListener(e -> passwordField.requestFocusInWindow());
        passwordField.addActionListener(e -> confirmField.requestFocusInWindow());
        registerButton.addActionListener(e -> doRegister());
        Theme.bindShowPassword(showPassword, passwordField, confirmField);
    }

    private JPanel buildForm(Runnable onBackToLogin) {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.BG);
        form.setBorder(new EmptyBorder(24, 40, 20, 40));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTH;

        int row = 0;
        c.gridy = row++; c.insets = new Insets(0, 0, 6, 0);  form.add(Theme.fieldLabel("Full name"), c);
        c.gridy = row++; c.insets = new Insets(0, 0, 14, 0); form.add(fullNameField, c);
        c.gridy = row++; c.insets = new Insets(0, 0, 6, 0);  form.add(Theme.fieldLabel("Username"), c);
        c.gridy = row++; c.insets = new Insets(0, 0, 14, 0); form.add(usernameField, c);
        c.gridy = row++; c.insets = new Insets(0, 0, 6, 0);  form.add(Theme.fieldLabel("Password (at least 6 characters)"), c);
        c.gridy = row++; c.insets = new Insets(0, 0, 14, 0); form.add(passwordField, c);
        c.gridy = row++; c.insets = new Insets(0, 0, 6, 0);  form.add(Theme.fieldLabel("Confirm password"), c);
        c.gridy = row++; c.insets = new Insets(0, 0, 8, 0);  form.add(confirmField, c);
        c.gridy = row++; c.insets = new Insets(0, 0, 8, 0);  form.add(showPassword, c);
        c.gridy = row++; c.insets = new Insets(0, 0, 12, 0); form.add(messageLabel, c);
        c.gridy = row++; c.insets = new Insets(0, 0, 18, 0); form.add(registerButton, c);
        c.gridy = row++; c.insets = new Insets(0, 0, 0, 0);  form.add(Theme.linkRow("Already have an account?", "Login", onBackToLogin), c);
        c.gridy = row;   c.weighty = 1;                      form.add(Box.createVerticalGlue(), c);
        return form;
    }

    // ------------------------------------------------------------------ the registration itself

    private void doRegister() {
        Theme.clear(messageLabel);
        String fullName = fullNameField.getText().trim();
        String username = usernameField.getText().trim();
        char[] password = passwordField.getPassword();
        char[] confirm  = confirmField.getPassword();
        try {
            String problem = check(fullName, username, password, confirm);
            if (problem != null) {
                Theme.error(messageLabel, problem);
                return;
            }
            if (Auth.register(fullName, username, password)) {
                clearForm();
                onRegistered.accept(username);                    // go back to the login page
            } else {
                Theme.error(messageLabel, "That username is already taken. Please choose another one.");
            }
        } catch (SQLException ex) {
            Theme.error(messageLabel, Auth.explain(ex));
        } finally {
            Arrays.fill(password, '\0');                          // wipe the passwords from memory
            Arrays.fill(confirm, '\0');
        }
    }

    /** Returns a message for the first problem found, or null when everything is fine. */
    private String check(String fullName, String username, char[] password, char[] confirm) {
        if (fullName.isEmpty())                        return "Please enter your full name.";
        if (fullName.length() > 100)                   return "The full name is too long (100 characters at most).";
        if (!username.matches("[A-Za-z0-9_.]{3,30}"))  return "Username: 3 to 30 characters, using letters, numbers, _ or . only.";
        if (password.length < 6)                       return "The password must have at least 6 characters.";
        if (!Arrays.equals(password, confirm))         return "The two passwords do not match.";
        return null;
    }

    private void clearForm() {
        fullNameField.setText("");
        usernameField.setText("");
        passwordField.setText("");
        confirmField.setText("");
        showPassword.setSelected(false);
        passwordField.setEchoChar('\u2022');
        confirmField.setEchoChar('\u2022');
        Theme.clear(messageLabel);
    }

    // ------------------------------------------------------------------ used by AuthFrame

    public JButton getDefaultButton() { return registerButton; }

    public void focusFirstField() {
        SwingUtilities.invokeLater(() -> fullNameField.requestFocusInWindow());
    }

    public void showError(String message) { Theme.error(messageLabel, message); }
}
