import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Colors, fonts and ready-made styled components, so the login and register pages
 * look the same as Main.java.
 */
public class Theme {

    // Same palette as Main.java
    public static final Color PRIMARY    = new Color(41, 128, 185);
    public static final Color BG         = new Color(245, 247, 250);
    public static final Color TEXT       = new Color(50, 50, 50);
    public static final Color GREEN      = new Color(46, 204, 113);
    public static final Color RED_TEXT   = new Color(192, 57, 43);
    public static final Color GREEN_TEXT = new Color(30, 132, 73);

    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font FONT_BOLD  = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_PLAIN = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 13);

    private Theme() { }   // this class only holds static helpers

    // ------------------------------------------------------------------ layout pieces

    /** The blue band at the top of a page. */
    public static JPanel header(String title, String subtitle) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(PRIMARY);
        panel.setBorder(new EmptyBorder(26, 40, 26, 40));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FONT_TITLE);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(FONT_PLAIN);
        subtitleLabel.setForeground(new Color(214, 234, 248));
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 4)));
        panel.add(subtitleLabel);
        return panel;
    }

    public static JLabel fieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_BOLD);
        label.setForeground(TEXT);
        return label;
    }

    public static JTextField textField() {
        JTextField field = new JTextField();
        styleField(field);
        return field;
    }

    public static JPasswordField passwordField() {
        JPasswordField field = new JPasswordField();
        styleField(field);
        return field;
    }

    private static void styleField(JTextField field) {
        field.setFont(FONT_PLAIN);
        field.setPreferredSize(new Dimension(100, 40));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(5, 10, 5, 10)));
    }

    public static JCheckBox checkBox(String text) {
        JCheckBox box = new JCheckBox(text);
        box.setFont(FONT_SMALL);
        box.setForeground(TEXT);
        box.setOpaque(false);
        box.setFocusPainted(false);
        return box;
    }

    /** Lets a check box show / hide what is typed in one or more password fields. */
    public static void bindShowPassword(JCheckBox box, JPasswordField... fields) {
        char hidden = fields[0].getEchoChar();
        box.addActionListener(e -> {
            for (JPasswordField field : fields) {
                field.setEchoChar(box.isSelected() ? (char) 0 : hidden);
            }
        });
    }

    // ------------------------------------------------------------------ buttons

    /** A big colored button (same style as the buttons in Main.java). */
    public static JButton button(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(FONT_BOLD);
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setContentAreaFilled(false);      // turn off the system button look, so our color shows on Windows / Mac too
        button.setOpaque(true);                  // (must come AFTER the line above, which switches opaque off)
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 16, 10, 16));
        button.setPreferredSize(new Dimension(100, 44));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { button.setBackground(color.darker()); }
            @Override public void mouseExited(MouseEvent e)  { button.setBackground(color); }
        });
        return button;
    }

    /** A button that looks like a blue text link. */
    public static JButton link(String text, Runnable action) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(PRIMARY);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(2, 2, 2, 2));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addActionListener(e -> action.run());
        return button;
    }

    /** A centered line such as:  Don't have an account?  Register */
    public static JPanel linkRow(String text, String linkText, Runnable action) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        row.setOpaque(false);
        JLabel label = new JLabel(text);
        label.setFont(FONT_SMALL);
        label.setForeground(TEXT);
        row.add(label);
        row.add(link(linkText, action));
        return row;
    }

    // ------------------------------------------------------------------ messages

    /** The label under the form where errors / success messages appear. */
    public static JLabel messageLabel() {
        JLabel label = new JLabel(" ");
        label.setFont(FONT_SMALL);
        label.setVerticalAlignment(SwingConstants.TOP);
        label.setPreferredSize(new Dimension(100, 40));
        return label;
    }

    public static void error(JLabel label, String message)   { show(label, message, RED_TEXT); }

    public static void success(JLabel label, String message) { show(label, message, GREEN_TEXT); }

    public static void clear(JLabel label) { label.setText(" "); }

    private static void show(JLabel label, String message, Color color) {
        label.setForeground(color);
        // HTML lets long messages wrap onto a second line
        label.setText("<html><div style='width:330px'>" + escape(message) + "</div></html>");
    }

    private static String escape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
