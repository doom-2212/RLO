import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

/**
 * The window that holds the login page and the register page (it flips between them).
 * After a correct login it opens the inventory window (Main), which loads the products from MySQL.
 */
public class AuthFrame extends JFrame {

    private static final String LOGIN = "login";
    private static final String REGISTER = "register";
    private static final int WIDTH = 460;

    private final CardLayout cards = new CardLayout();
    private final JPanel deck = new JPanel(cards);
    private final LoginPage loginPage;
    private final RegisterPage registerPage;

    public AuthFrame() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        loginPage = new LoginPage(this::openInventory, this::showRegister);
        registerPage = new RegisterPage(this::accountCreated, this::showLogin);
        deck.add(loginPage, LOGIN);
        deck.add(registerPage, REGISTER);
        setContentPane(deck);

        showLogin();
        setVisible(true);

        // Check MySQL right away, so a problem (server not running, ...) is shown before anyone types
        try {
            Auth.ensureUsersTable();
        } catch (SQLException ex) {
            loginPage.showError(Auth.explain(ex));
        }
    }

    private void showLogin() {
        showPage(LOGIN, "Login", 520, loginPage.getDefaultButton());
        loginPage.focusFirstField();
    }

    private void showRegister() {
        showPage(REGISTER, "Register", 660, registerPage.getDefaultButton());
        registerPage.focusFirstField();
    }

    private void showPage(String name, String title, int height, JButton enterButton) {
        cards.show(deck, name);
        deck.setPreferredSize(new Dimension(WIDTH, height));
        setTitle("ICF Inventory & Billing System - " + title);
        getRootPane().setDefaultButton(enterButton);      // the Enter key presses this button
        pack();
        setLocationRelativeTo(null);
    }

    /** Called by the register page after a new account was saved. */
    private void accountCreated(String username) {
        showLogin();
        loginPage.prefill(username);
        loginPage.showSuccess("Account created! Please log in.");
    }

    /** Called by the login page after a correct login. */
    private void openInventory() {
        new Main();     // the inventory window: it loads the products from MySQL by itself
        dispose();      // close the login window
    }
}
