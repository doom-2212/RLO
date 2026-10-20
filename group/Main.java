import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.net.URI;
import java.sql.*;

public class Main extends JFrame {

    private DefaultTableModel invTableModel;
    private JTable invTable;
    private static int invoiceNo = 1000;

    // --- DATABASE CREDENTIALS ---
    private static final String DB_URL = "jdbc:mysql://localhost:3306/icf_inventory?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String DB_USER = "root"; 
    private static final String DB_PASS = "Doom2212"; 

    // Used by the login/register code (Auth.java) so the database settings are written in one place only
    static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    // --- UI COLORS ---
    private final Color PRIMARY_COLOR = new Color(41, 128, 185); 
    private final Color BG_COLOR = new Color(245, 247, 250);     
    private final Color TEXT_COLOR = new Color(50, 50, 50);

    public Main() {
        setTitle("ICF Inventory & Billing System");
        setSize(1000, 650);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_COLOR);


        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("ICF Inventory System");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JLabel userLabel = new JLabel("👤 " + Auth.getCurrentUser());
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        userLabel.setForeground(Color.WHITE);
        headerPanel.add(userLabel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);


        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(BG_COLOR);
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

   
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBackground(BG_COLOR);
        controlPanel.setPreferredSize(new Dimension(200, 0));

        JButton btnAdd = createStyledButton("➕ Add Product", PRIMARY_COLOR);
        JButton btnDel = createStyledButton("🗑 Delete Product", new Color(231, 76, 60));
        JButton btnBill = createStyledButton("🛒 Bill / Checkout", new Color(46, 204, 113));
        JButton btnRate = createStyledButton("⭐ Rate Us", new Color(241, 196, 15));
        JButton btnLogout = createStyledButton("🔒 Logout", new Color(52, 73, 94));
        JButton btnExit = createStyledButton("❌ Exit", new Color(149, 165, 166));

        controlPanel.add(btnAdd);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        controlPanel.add(btnDel);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        controlPanel.add(btnBill);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        controlPanel.add(btnRate);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        controlPanel.add(btnLogout);
        controlPanel.add(Box.createVerticalGlue()); 
        controlPanel.add(btnExit);
        
        mainPanel.add(controlPanel, BorderLayout.WEST);


        JPanel dataPanel = new JPanel(new BorderLayout(0, 10));
        dataPanel.setBackground(BG_COLOR);

        // Search Bar
        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setBackground(BG_COLOR);
        JLabel searchIcon = new JLabel("🔍 Search:");
        searchIcon.setFont(new Font("Segoe UI", Font.BOLD, 14));
        searchIcon.setForeground(TEXT_COLOR);
        
        JTextField searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(5, 10, 5, 10)
        ));
        
        searchPanel.add(searchIcon, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        dataPanel.add(searchPanel, BorderLayout.NORTH);

        // Table
        invTableModel = new DefaultTableModel(new String[]{"Product Name", "Quantity", "Price (₱)"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        invTable = new JTable(invTableModel);
        styleTable(invTable);
        
        JScrollPane scrollPane = new JScrollPane(invTable);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        dataPanel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(dataPanel, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);

       
        btnAdd.addActionListener(e -> addProduct());
        btnDel.addActionListener(e -> deleteProduct());
        btnBill.addActionListener(e -> openBilling());
        btnExit.addActionListener(e -> System.exit(0));
        btnLogout.addActionListener(e -> {
            Auth.logout();
            new AuthFrame();   // back to the login page
            dispose();         // close the inventory window
        });
        
        btnRate.addActionListener(e -> {
            String url = "https://u2l.ai/po96G0";
            JLabel link = new JLabel("<html>Thanks for Shopping!<br><br>Please rate us here:<br><a href=''>" + url + "</a></html>");
            link.setCursor(new Cursor(Cursor.HAND_CURSOR));
            link.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent ev) {
                    try { Desktop.getDesktop().browse(new URI(url)); } 
                    catch (Exception ex) { JOptionPane.showMessageDialog(null, "Could not open link."); }
                }
            });
            JOptionPane.showMessageDialog(this, link, "Rate Us here :)", JOptionPane.INFORMATION_MESSAGE);
        });

        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                String query = searchField.getText().toLowerCase();
                for (int i = 0; i < invTableModel.getRowCount(); i++) {
                    if (invTableModel.getValueAt(i, 0).toString().toLowerCase().contains(query)) {
                        invTable.setRowSelectionInterval(i, i);
                        invTable.scrollRectToVisible(invTable.getCellRect(i, 0, true));
                        return;
                    }
                }
            }
        });

        loadProductsFromDB();
        setVisible(true);
    }



    private JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bgColor);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Hover effect
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(bgColor.darker()); }
            public void mouseExited(MouseEvent e) { btn.setBackground(bgColor); }
        });
        return btn;
    }

        private void styleTable(JTable table) {
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setSelectionBackground(new Color(189, 214, 238));
        table.setSelectionForeground(TEXT_COLOR);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(230, 230, 230));

        // Header styling
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(236, 240, 241));
        header.setForeground(TEXT_COLOR);
        header.setPreferredSize(new Dimension(100, 35));
        
        // --- THIS MAKES COLUMNS UNMOVABLE ---
        header.setReorderingAllowed(false); 

        // Center align Quantity and Price columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        
        // Lock the width of the columns so they look clean
        if (table.getColumnCount() == 3) {
            // Inventory Table
            table.getColumnModel().getColumn(0).setPreferredWidth(300); // Product Name
            table.getColumnModel().getColumn(1).setPreferredWidth(100); // Quantity
            table.getColumnModel().getColumn(2).setPreferredWidth(100); // Price
        } else if (table.getColumnCount() == 4) {
            // Billing Cart Table
            table.getColumnModel().getColumn(0).setPreferredWidth(250); // Product
            table.getColumnModel().getColumn(1).setPreferredWidth(80);  // Qty
            table.getColumnModel().getColumn(2).setPreferredWidth(100); // Price
            table.getColumnModel().getColumn(3).setPreferredWidth(100); // Subtotal
        }
    }

    // --- DATABASE METHODS ---

    private void loadProductsFromDB() {
        invTableModel.setRowCount(0);
        String sql = "SELECT name, quantity, price FROM products";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
             
            while (rs.next()) {
                invTableModel.addRow(new Object[]{
                    rs.getString("name"),
                    rs.getInt("quantity"),
                    String.format("%.2f", rs.getDouble("price"))
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage());
        }
    }

    private void addProduct() {
        JTextField nameFld = new JTextField();
        JTextField qtyFld = new JTextField();
        JTextField priceFld = new JTextField();

        Object[] form = {"Product Name:", nameFld, "Quantity:", qtyFld, "Price:", priceFld};
        int opt = JOptionPane.showConfirmDialog(this, form, "Add Product", JOptionPane.OK_CANCEL_OPTION);
        if (opt != JOptionPane.OK_OPTION) return;

        try {
            String name = nameFld.getText().trim();
            int qty = Integer.parseInt(qtyFld.getText().trim());
            double price = Double.parseDouble(priceFld.getText().trim());
            
            if (name.isEmpty() || qty < 0 || price < 0) {
                JOptionPane.showMessageDialog(this, "Please enter valid details.");
                return;
            }

            String sql = "INSERT INTO products (name, quantity, price) VALUES (?, ?, ?)";
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, name);
                pstmt.setInt(2, qty);
                pstmt.setDouble(3, price);
                pstmt.executeUpdate();
            }
            loadProductsFromDB();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Quantity/Price must be numbers.");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to save:\n" + ex.getMessage());
        }
    }

    private void deleteProduct() {
        int row = invTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a product first.");
            return;
        }
        
        String name = invTableModel.getValueAt(row, 0).toString();
        String sql = "DELETE FROM products WHERE name = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
            loadProductsFromDB(); 
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to delete:\n" + ex.getMessage());
        }
    }

    private void updateStockInDB(String name, int newQty) {
        String sql = "UPDATE products SET quantity = ? WHERE name = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, newQty);
            pstmt.setString(2, name);
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // --- BILLING TERMINAL ---

    private void openBilling() {
        if (invTableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Inventory is empty.");
            return;
        }

        JFrame billFrame = new JFrame("Billing Terminal");
        billFrame.setSize(700, 550);
        billFrame.setLocationRelativeTo(this);
        billFrame.setLayout(new BorderLayout());
        billFrame.getContentPane().setBackground(BG_COLOR);

        // 1. North Panel (Header + Product Selector)
        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
        northPanel.setBackground(BG_COLOR);

        // Header
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.setBackground(PRIMARY_COLOR);
        JLabel title = new JLabel("Point of Sale");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        header.add(title);
        northPanel.add(header);

        // Top Controls (Product Selector)
        JPanel posTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        posTop.setBackground(BG_COLOR);
        
        JComboBox<String> prodDrop = new JComboBox<>();
        prodDrop.setPreferredSize(new Dimension(200, 30));
        for (int i = 0; i < invTableModel.getRowCount(); i++) {
            prodDrop.addItem(invTableModel.getValueAt(i, 0).toString());
        }
        
        JTextField qtyInput = new JTextField(5);
        qtyInput.setPreferredSize(new Dimension(50, 30));
        
        JButton addCartBtn = createStyledButton("Add to Cart", PRIMARY_COLOR);
        addCartBtn.setMaximumSize(new Dimension(120, 30));
        addCartBtn.setPreferredSize(new Dimension(120, 30));

        posTop.add(new JLabel("Select Product:"));
        posTop.add(prodDrop); 
        posTop.add(new JLabel("Qty:")); 
        posTop.add(qtyInput); 
        posTop.add(addCartBtn);
        
        northPanel.add(posTop);
        billFrame.add(northPanel, BorderLayout.NORTH);

        // 2. Center Panel (Cart Table)
        DefaultTableModel cartModel = new DefaultTableModel(new String[]{"Product", "Qty", "Price", "Subtotal"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable cartTable = new JTable(cartModel);
        styleTable(cartTable);
        
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(new EmptyBorder(10, 15, 10, 15));
        tablePanel.setBackground(BG_COLOR);
        tablePanel.add(new JScrollPane(cartTable), BorderLayout.CENTER);
        billFrame.add(tablePanel, BorderLayout.CENTER);

        // 3. Bottom Panel (Totals and Actions)
        JPanel posBottom = new JPanel(new BorderLayout());
        posBottom.setBackground(BG_COLOR);
        posBottom.setBorder(new EmptyBorder(0, 15, 15, 15));

        JLabel totalLbl = new JLabel("TOTAL: ₱0.00");
        totalLbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        totalLbl.setForeground(new Color(231, 76, 60)); 
        
        JPanel actionBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionBtns.setBackground(BG_COLOR);
        
        JButton removeBtn = createStyledButton("Remove", new Color(149, 165, 166));
        removeBtn.setPreferredSize(new Dimension(100, 40));
        
        JButton checkoutBtn = createStyledButton("Checkout", new Color(46, 204, 113));
        checkoutBtn.setPreferredSize(new Dimension(120, 40));

        actionBtns.add(removeBtn);
        actionBtns.add(checkoutBtn);
        
        posBottom.add(totalLbl, BorderLayout.WEST);
        posBottom.add(actionBtns, BorderLayout.EAST);
        billFrame.add(posBottom, BorderLayout.SOUTH);

        // Events
        addCartBtn.addActionListener(e -> {
            String sel = (String) prodDrop.getSelectedItem();
            if (sel == null) return;

            int qty;
            try {
                qty = Integer.parseInt(qtyInput.getText().trim());
                if (qty <= 0) throw new NumberFormatException();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(billFrame, "Enter a valid quantity.");
                return;
            }

            int invRow = findRow(sel);
            if (invRow == -1) {
                JOptionPane.showMessageDialog(billFrame, "Product not found in table.");
                return;
            }

            int stock = Integer.parseInt(invTableModel.getValueAt(invRow, 1).toString());
            if (qty > stock) {
                JOptionPane.showMessageDialog(billFrame, "Not enough stock! Only " + stock + " left.");
                return;
            }

            double price = Double.parseDouble(invTableModel.getValueAt(invRow, 2).toString());
            cartModel.addRow(new Object[]{sel, qty, String.format("%.2f", price), String.format("%.2f", qty * price)});
            qtyInput.setText("");
            updateTotal(cartModel, totalLbl);
        });

        removeBtn.addActionListener(e -> {
            int row = cartTable.getSelectedRow();
            if (row >= 0) {
                cartModel.removeRow(row);
                updateTotal(cartModel, totalLbl);
            }
        });

        checkoutBtn.addActionListener(e -> {
            if (cartModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(billFrame, "Cart is empty.");
                return;
            }

            for (int i = 0; i < cartModel.getRowCount(); i++) {
                String name = cartModel.getValueAt(i, 0).toString();
                int qty = Integer.parseInt(cartModel.getValueAt(i, 1).toString());
                int invRow = findRow(name);
                int currentStock = Integer.parseInt(invTableModel.getValueAt(invRow, 1).toString());
                updateStockInDB(name, currentStock - qty);
            }
            
            loadProductsFromDB();

            // Receipt
            String receipt = "================================\n" +
                             "        ICF SCHOOL SUPPLIES     \n" +
                             "================================\n" +
                             "Invoice #: INV-" + (++invoiceNo) + "\n" +
                             "--------------------------------\n";
                             
            for (int i = 0; i < cartModel.getRowCount(); i++) {
                receipt += String.format("%-15s x%-3s = ₱%s\n", 
                    cartModel.getValueAt(i, 0).toString(),
                    cartModel.getValueAt(i, 1).toString(),
                    cartModel.getValueAt(i, 3).toString());
            }

            receipt += "--------------------------------\n" +
                       "TOTAL: " + totalLbl.getText().replace("TOTAL: ", "") + "\n" +
                       "================================\n" +
                       "      Thank you, come again!    \n" +
                       "================================\n";

            JTextArea area = new JTextArea(receipt);
            area.setFont(new Font("Monospaced", Font.PLAIN, 12));
            area.setEditable(false);
            
            JOptionPane.showMessageDialog(billFrame, new JScrollPane(area), "Receipt", JOptionPane.INFORMATION_MESSAGE);
            billFrame.dispose();
        });

        billFrame.setVisible(true);
    }

    private int findRow(String name) {
        for (int i = 0; i < invTableModel.getRowCount(); i++) {
            if (invTableModel.getValueAt(i, 0).toString().equalsIgnoreCase(name)) return i;
        }
        return -1;
    }

    private void updateTotal(DefaultTableModel cartModel, JLabel lbl) {
        double sum = 0;
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            sum += Double.parseDouble(cartModel.getValueAt(i, 3).toString());
        }
        lbl.setText("TOTAL: ₱" + String.format("%.2f", sum));
    }

    public static void main(String[] args) {
        // Use System Look and Feel for native window borders
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } 
        catch (Exception ignored) {}
        
        SwingUtilities.invokeLater(() -> new AuthFrame());   // start at the login page
    }
}