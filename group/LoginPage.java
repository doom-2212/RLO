import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.util.Arrays;

/** The login page: checks the username and password against the MySQL users table. */
public class LoginPage extends JPanel {

    private final JTextField     usernameField = Theme.textField();
    private final JPasswordField passwordField = Theme.passwordField();
    private final JCheckBox      showPassword  = Theme.checkBox("Show password");
    private final JLabel         messageLabel  = Theme.messageLabel();
    private final JButton        loginButton   = Theme.button("Login", Theme.PRIMARY);

    private final Runnable onSuccess;            // runs after a correct login (opens the inventory)

    public LoginPage(Runnable onSuccess, Runnable onRegister) {
        this.onSuccess = onSuccess;

        setLayout(new BorderLayout());
        setBackground(Theme.BG);
        add(Theme.header("ICF Inventory System", "Please log in to continue"), BorderLayout.NORTH);
        add(buildForm(onRegister), BorderLayout.CENTER);

        usernameField.addActionListener(e -> passwordField.requestFocusInWindow());   // Enter = next field
        loginButton.addActionListener(e -> doLogin());
        Theme.bindShowPassword(showPassword, passwordField);
    }

    private JPanel buildForm(Runnable onRegister) {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.BG);
        form.setBorder(new EmptyBorder(28, 40, 20, 40));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTH;

        int row = 0;
        c.gridy = row++; c.insets = new Insets(0, 0, 6, 0);  form.add(Theme.fieldLabel("Username"), c);
        c.gridy = row++; c.insets = new Insets(0, 0, 16, 0); form.add(usernameField, c);
        c.gridy = row++; c.insets = new Insets(0, 0, 6, 0);  form.add(Theme.fieldLabel("Password"), c);
        c.gridy = row++; c.insets = new Insets(0, 0, 8, 0);  form.add(passwordField, c);
        c.gridy = row++; c.insets = new Insets(0, 0, 8, 0);  form.add(showPassword, c);
        c.gridy = row++; c.insets = new Insets(0, 0, 12, 0); form.add(messageLabel, c);
        c.gridy = row++; c.insets = new Insets(0, 0, 18, 0); form.add(loginButton, c);
        c.gridy = row++; c.insets = new Insets(0, 0, 0, 0);  form.add(Theme.linkRow("Don't have an account?", "Register", onRegister), c);
        c.gridy = row;   c.weighty = 1;                      form.add(Box.createVerticalGlue(), c);   // pushes everything to the top
        return form;
    }

    // ------------------------------------------------------------------ the login itself

    private void doLogin() {
        Theme.clear(messageLabel);
        String username = usernameField.getText().trim();
        char[] password = passwordField.getPassword();
        try {
            if (username.isEmpty() || password.length == 0) {
                Theme.error(messageLabel, "Please enter your username and password.");
                return;
            }
            if (Auth.login(username, password)) {
                passwordField.setText("");
                onSuccess.run();                                  // open the inventory
            } else {
                Theme.error(messageLabel, "Wrong username or password.");
                passwordField.setText("");
                passwordField.requestFocusInWindow();
            }
        } catch (SQLException ex) {
            Theme.error(messageLabel, Auth.explain(ex));
        } finally {
            Arrays.fill(password, '\0');                          // wipe the password from memory
        }
    }

    // ------------------------------------------------------------------ used by AuthFrame

    public JButton getDefaultButton() { return loginButton; }

    public void focusFirstField() {
        SwingUtilities.invokeLater(() -> usernameField.requestFocusInWindow());
    }

    /** After registering: fill in the new username and move the cursor to the password. */
    public void prefill(String username) {
        usernameField.setText(username);
        passwordField.setText("");
        SwingUtilities.invokeLater(() -> passwordField.requestFocusInWindow());
    }

    public void showError(String message)   { Theme.error(messageLabel, message); }

    public void showSuccess(String message) { Theme.success(messageLabel, message); }
}
