import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.text.NumberFormat;
import java.util.Locale;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class HotelManagement{
    // Color scheme
    private static final Color PRIMARY_COLOR = new Color(0, 102, 153); // Dark blue
    private static final Color SECONDARY_COLOR = new Color(220, 240, 255); // Light blue
    private static final Color ACCENT_COLOR = new Color(255, 153, 0); // Orange
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 245); // Light gray
    
    static final String DB_URL = "jdbc:mysql://localhost:3306/hotel_db";
    static final String DB_USER = "root";
    static final String DB_PASS = "dbms";

    public static void main(String[] args) {
        initializeDatabase();
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new LoginPage();
        });
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    private static void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // Create tables (same as before)
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "username VARCHAR(50) UNIQUE NOT NULL," +
                "password VARCHAR(50) NOT NULL," +
                "role VARCHAR(20) NOT NULL DEFAULT 'customer')");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS rooms (" +
                "room_id INT PRIMARY KEY," +
                "room_type VARCHAR(20) NOT NULL," +
                "price DECIMAL(10,2) NOT NULL," +
                "capacity INT NOT NULL DEFAULT 2," +
                "status VARCHAR(20) DEFAULT 'available')");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS bookings (" +
                "booking_id INT AUTO_INCREMENT PRIMARY KEY," +
                "user_id INT NOT NULL," +
                "room_id INT NOT NULL," +
                "booking_date DATE NOT NULL," +
                "status VARCHAR(20) DEFAULT 'confirmed'," +
                "check_in_date DATE," +
                "check_out_date DATE," +
                "number_of_persons INT," +
                "food_requested BOOLEAN DEFAULT FALSE," +
                "num_guests INT," +
                "FOREIGN KEY (user_id) REFERENCES users(id)," +
                "FOREIGN KEY (room_id) REFERENCES rooms(room_id))");

            // Insert sample data (same as before)
            if (!hasData(stmt, "users")) {
                stmt.executeUpdate("INSERT INTO users (username, password, role) VALUES " +
                    "('admin', 'admin123', 'admin')," +
                    "('guest1', 'guest123', 'customer')," +
                    "('guest2', 'guest123', 'customer')");
            }

            if (!hasData(stmt, "rooms")) {
                stmt.executeUpdate("INSERT INTO rooms (room_id, room_type, price, capacity) VALUES " +
                    "(101, 'Single', 1500.00, 1)," +
                    "(102, 'Single', 1500.00, 1)," +
                    "(201, 'Double', 2500.00, 2)," +
                    "(202, 'Double', 2500.00, 2)," +
                    "(301, 'Suite', 5000.00, 4)," +
                    "(302, 'Suite', 5500.00, 4)," +
                    "(401, 'Family', 3500.00, 6)," +
                    "(402, 'Family', 4000.00, 6)");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database initialization failed: " + e.getMessage());
        }
    }

    private static boolean hasData(Statement stmt, String table) throws SQLException {
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + table);
        rs.next();
        return rs.getInt(1) > 0;
    }

    static class LoginPage extends JFrame {
        private JTextField usernameField = new JTextField(15);
        private JPasswordField passwordField = new JPasswordField(15);
        private JComboBox<String> roleBox = new JComboBox<>(new String[]{"customer", "admin"});

        public LoginPage() {
            setTitle("Hotel Management System - Login");
            setSize(450, 350);
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            setLocationRelativeTo(null);
            getContentPane().setBackground(BACKGROUND_COLOR);
            
            // Header panel
            JPanel headerPanel = new JPanel();
            headerPanel.setBackground(PRIMARY_COLOR);
            headerPanel.setBorder(new EmptyBorder(15, 0, 15, 0));
            JLabel titleLabel = new JLabel("HOTEL MANAGEMENT SYSTEM");
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
            titleLabel.setForeground(TEXT_COLOR);
            headerPanel.add(titleLabel);

            // Form panel
            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBackground(BACKGROUND_COLOR);
            formPanel.setBorder(new EmptyBorder(20, 40, 20, 40));
            
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;

            // Username
            addFormField(formPanel, gbc, 0, "Username:", usernameField);
            
            // Password
            addFormField(formPanel, gbc, 1, "Password:", passwordField);
            
            // Role
            addFormField(formPanel, gbc, 2, "Role:", roleBox);
            
            // Buttons
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
            buttonPanel.setBackground(BACKGROUND_COLOR);
            
            JButton loginBtn = createStyledButton("Login", PRIMARY_COLOR);
            JButton registerBtn = createStyledButton("Register", ACCENT_COLOR);
            
            buttonPanel.add(loginBtn);
            buttonPanel.add(registerBtn);

            loginBtn.addActionListener(e -> authenticate());
            registerBtn.addActionListener(e -> {
                dispose();
                new RegistrationPage();
            });

            // Layout
            setLayout(new BorderLayout());
            add(headerPanel, BorderLayout.NORTH);
            add(formPanel, BorderLayout.CENTER);
            add(buttonPanel, BorderLayout.SOUTH);

            setVisible(true);
        }

        private void addFormField(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
            gbc.gridx = 0;
            gbc.gridy = row;
            JLabel jLabel = new JLabel(label);
            jLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            panel.add(jLabel, gbc);
            
            gbc.gridx = 1;
            field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            field.setPreferredSize(new Dimension(200, 30));
            panel.add(field, gbc);
        }

        private JButton createStyledButton(String text, Color bgColor) {
            JButton button = new JButton(text);
            button.setFont(new Font("Segoe UI", Font.BOLD, 14));
            button.setBackground(bgColor);
            button.setForeground(TEXT_COLOR);
            button.setFocusPainted(false);
            button.setBorder(new EmptyBorder(8, 20, 8, 20));
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            return button;
        }

        private void authenticate() {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            String role = roleBox.getSelectedItem().toString();

            try (Connection conn = getConnection()) {
                String sql = "SELECT * FROM users WHERE username = ? AND password = ? AND role = ?";
                PreparedStatement pst = conn.prepareStatement(sql);
                pst.setString(1, username);
                pst.setString(2, password);
                pst.setString(3, role);
                ResultSet rs = pst.executeQuery();

                if (rs.next()) {
                    dispose();
                    if (role.equals("customer")) {
                        new CustomerDashboard(rs.getInt("id"));
                    } else {
                        new AdminDashboard();
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid credentials!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    static class RegistrationPage extends JFrame {
        private JTextField usernameField = new JTextField(15);
        private JPasswordField passwordField = new JPasswordField(15);

        public RegistrationPage() {
            setTitle("Hotel Management System - Registration");
            setSize(450, 300);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setLocationRelativeTo(null);
            getContentPane().setBackground(BACKGROUND_COLOR);
            
            // Header panel
            JPanel headerPanel = new JPanel();
            headerPanel.setBackground(PRIMARY_COLOR);
            headerPanel.setBorder(new EmptyBorder(15, 0, 15, 0));
            JLabel titleLabel = new JLabel("CUSTOMER REGISTRATION");
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
            titleLabel.setForeground(TEXT_COLOR);
            headerPanel.add(titleLabel);

            // Form panel
            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBackground(BACKGROUND_COLOR);
            formPanel.setBorder(new EmptyBorder(20, 40, 20, 40));
            
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;

            // Username
            addFormField(formPanel, gbc, 0, "Username:", usernameField);
            
            // Password
            addFormField(formPanel, gbc, 1, "Password:", passwordField);
            
            // Register Button
            JButton registerBtn = createStyledButton("Register", ACCENT_COLOR);
            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.gridwidth = 2;
            gbc.anchor = GridBagConstraints.CENTER;
            formPanel.add(registerBtn, gbc);

            registerBtn.addActionListener(e -> registerCustomer());

            // Layout
            setLayout(new BorderLayout());
            add(headerPanel, BorderLayout.NORTH);
            add(formPanel, BorderLayout.CENTER);

            setVisible(true);
        }

        private void addFormField(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
            gbc.gridx = 0;
            gbc.gridy = row;
            JLabel jLabel = new JLabel(label);
            jLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            panel.add(jLabel, gbc);
            
            gbc.gridx = 1;
            field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            field.setPreferredSize(new Dimension(200, 30));
            panel.add(field, gbc);
        }

        private JButton createStyledButton(String text, Color bgColor) {
            JButton button = new JButton(text);
            button.setFont(new Font("Segoe UI", Font.BOLD, 14));
            button.setBackground(bgColor);
            button.setForeground(TEXT_COLOR);
            button.setFocusPainted(false);
            button.setBorder(new EmptyBorder(8, 25, 8, 25));
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            return button;
        }

        private void registerCustomer() {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Connection conn = getConnection()) {
                String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, 'customer')";
                PreparedStatement pst = conn.prepareStatement(sql);
                pst.setString(1, username);
                pst.setString(2, password);
                pst.executeUpdate();

                JOptionPane.showMessageDialog(this, "Registration successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
                new LoginPage();
            } catch (SQLIntegrityConstraintViolationException ex) {
                JOptionPane.showMessageDialog(this, "Username already exists!", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    static class CustomerDashboard extends JFrame {
        private int userId;
        private JTextArea roomListArea = new JTextArea();

        public CustomerDashboard(int userId) {
            this.userId = userId;
            setTitle("Hotel Management System - Customer Dashboard");
            setSize(900, 650);
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            setLocationRelativeTo(null);
            getContentPane().setBackground(BACKGROUND_COLOR);
            
            // Header panel
            JPanel headerPanel = new JPanel(new BorderLayout());
            headerPanel.setBackground(PRIMARY_COLOR);
            headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
            
            JLabel titleLabel = new JLabel("CUSTOMER DASHBOARD");
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
            titleLabel.setForeground(TEXT_COLOR);
            
            JLabel userLabel = new JLabel("User ID: " + userId);
            userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            userLabel.setForeground(TEXT_COLOR);
            
            headerPanel.add(titleLabel, BorderLayout.WEST);
            headerPanel.add(userLabel, BorderLayout.EAST);

            // Room List
            roomListArea.setEditable(false);
            roomListArea.setFont(new Font("Consolas", Font.PLAIN, 14));
            roomListArea.setBackground(SECONDARY_COLOR);
            roomListArea.setBorder(new EmptyBorder(10, 10, 10, 10));
            
            JScrollPane scrollPane = new JScrollPane(roomListArea);
            scrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
                "Available Rooms",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14),
                PRIMARY_COLOR));

            // Buttons
            JPanel buttonPanel = new JPanel();
            buttonPanel.setBackground(BACKGROUND_COLOR);
            buttonPanel.setBorder(new EmptyBorder(10, 0, 15, 0));
            
            JButton bookBtn = createStyledButton("Book Room", PRIMARY_COLOR);
            JButton refreshBtn = createStyledButton("Refresh", new Color(70, 130, 180)); // Steel blue
            JButton logoutBtn = createStyledButton("Logout", ACCENT_COLOR);
            
            buttonPanel.add(bookBtn);
            buttonPanel.add(refreshBtn);
            buttonPanel.add(logoutBtn);

            bookBtn.addActionListener(e -> new BookingPage(userId));
            refreshBtn.addActionListener(e -> loadAvailableRooms());
            logoutBtn.addActionListener(e -> {
                dispose();
                new LoginPage();
            });

            // Layout
            setLayout(new BorderLayout());
            add(headerPanel, BorderLayout.NORTH);
            add(scrollPane, BorderLayout.CENTER);
            add(buttonPanel, BorderLayout.SOUTH);

            loadAvailableRooms();
            setVisible(true);
        }

        private JButton createStyledButton(String text, Color bgColor) {
            JButton button = new JButton(text);
            button.setFont(new Font("Segoe UI", Font.BOLD, 14));
            button.setBackground(bgColor);
            button.setForeground(TEXT_COLOR);
            button.setFocusPainted(false);
            button.setBorder(new EmptyBorder(8, 20, 8, 20));
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            return button;
        }

        private void loadAvailableRooms() {
            try (Connection conn = getConnection()) {
                String sql = "SELECT room_id, room_type, price, capacity FROM rooms " +
                             "WHERE status='available' ORDER BY room_type, room_id";
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);

                StringBuilder sb = new StringBuilder();
                NumberFormat rupeeFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

                sb.append(String.format("%-50s\n", "========== AVAILABLE ROOMS =========="));
                sb.append(String.format("%-10s %-15s %-15s %-15s\n", 
                    "Room ID", "Type", "Price/Night", "Capacity"));
                sb.append(String.format("%-10s %-15s %-15s %-15s\n", 
                    "-------", "----", "------------", "--------"));

                boolean roomsAvailable = false;

                while (rs.next()) {
                    roomsAvailable = true;
                    sb.append(String.format("%-10d %-15s %-15s %-15d\n",
                        rs.getInt("room_id"),
                        rs.getString("room_type"),
                        rupeeFormat.format(rs.getDouble("price")),
                        rs.getInt("capacity")));
                }

                if (!roomsAvailable) {
                    sb.append("\nNo rooms currently available. Please check back later.");
                }

                roomListArea.setText(sb.toString());
            } catch (SQLException e) {
                roomListArea.setText("Error loading room information:\n" + e.getMessage());
            }
        }
    }

    static class BookingPage extends JFrame {
        private int userId;
        private JTextField roomIdField = new JTextField(10);
        private JSpinner numGuestsSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
        private JCheckBox foodServiceCheckBox = new JCheckBox("Include food service (₹500 per day)");
        private JSpinner checkInSpinner;
        private JSpinner checkOutSpinner;

        public BookingPage(int userId) {
            this.userId = userId;
            setTitle("Hotel Management System - Book a Room");
            setSize(550, 450);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setLocationRelativeTo(null);
            getContentPane().setBackground(BACKGROUND_COLOR);
            
            // Header panel
            JPanel headerPanel = new JPanel();
            headerPanel.setBackground(PRIMARY_COLOR);
            headerPanel.setBorder(new EmptyBorder(15, 0, 15, 0));
            JLabel titleLabel = new JLabel("BOOK A ROOM");
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
            titleLabel.setForeground(TEXT_COLOR);
            headerPanel.add(titleLabel);

            // Form panel
            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBackground(BACKGROUND_COLOR);
            formPanel.setBorder(new EmptyBorder(20, 40, 20, 40));
            
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;

            // Room ID
            addFormField(formPanel, gbc, 0, "Room ID:", roomIdField);
            
            // Check-in Date
            gbc.gridx = 0; gbc.gridy = 1;
            formPanel.add(new JLabel("Check-in Date:"), gbc);
            gbc.gridx = 1;
            checkInSpinner = new JSpinner(new SpinnerDateModel());
            JSpinner.DateEditor checkInEditor = new JSpinner.DateEditor(checkInSpinner, "dd/MM/yyyy");
            checkInSpinner.setEditor(checkInEditor);
            checkInSpinner.setPreferredSize(new Dimension(150, 30));
            formPanel.add(checkInSpinner, gbc);

            // Check-out Date
            gbc.gridx = 0; gbc.gridy = 2;
            formPanel.add(new JLabel("Check-out Date:"), gbc);
            gbc.gridx = 1;
            checkOutSpinner = new JSpinner(new SpinnerDateModel());
            JSpinner.DateEditor checkOutEditor = new JSpinner.DateEditor(checkOutSpinner, "dd/MM/yyyy");
            checkOutSpinner.setEditor(checkOutEditor);
            checkOutSpinner.setPreferredSize(new Dimension(150, 30));
            formPanel.add(checkOutSpinner, gbc);

            // Number of Guests
            addFormField(formPanel, gbc, 3, "Number of Guests:", numGuestsSpinner);
            
            // Food Service
            foodServiceCheckBox.setBackground(BACKGROUND_COLOR);
            foodServiceCheckBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            foodServiceCheckBox.setForeground(PRIMARY_COLOR);
            gbc.gridx = 0; gbc.gridy = 4;
            gbc.gridwidth = 2;
            formPanel.add(foodServiceCheckBox, gbc);

            // Buttons
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
            buttonPanel.setBackground(BACKGROUND_COLOR);
            
            JButton confirmBtn = createStyledButton("Confirm Booking", PRIMARY_COLOR);
            JButton cancelBtn = createStyledButton("Cancel", new Color(192, 57, 43)); // Dark red
            
            buttonPanel.add(confirmBtn);
            buttonPanel.add(cancelBtn);

            confirmBtn.addActionListener(e -> bookRoom());
            cancelBtn.addActionListener(e -> dispose());

            // Layout
            setLayout(new BorderLayout());
            add(headerPanel, BorderLayout.NORTH);
            add(formPanel, BorderLayout.CENTER);
            add(buttonPanel, BorderLayout.SOUTH);

            setVisible(true);
        }

        private void addFormField(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
            gbc.gridx = 0;
            gbc.gridy = row;
            JLabel jLabel = new JLabel(label);
            jLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            panel.add(jLabel, gbc);
            
            gbc.gridx = 1;
            field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            field.setPreferredSize(new Dimension(200, 30));
            panel.add(field, gbc);
        }

        private JButton createStyledButton(String text, Color bgColor) {
            JButton button = new JButton(text);
            button.setFont(new Font("Segoe UI", Font.BOLD, 14));
            button.setBackground(bgColor);
            button.setForeground(TEXT_COLOR);
            button.setFocusPainted(false);
            button.setBorder(new EmptyBorder(8, 20, 8, 20));
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            return button;
        }

        private void bookRoom() {
            try {
                int roomId = Integer.parseInt(roomIdField.getText());
                LocalDate checkIn = ((java.util.Date) checkInSpinner.getValue()).toInstant()
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                LocalDate checkOut = ((java.util.Date) checkOutSpinner.getValue()).toInstant()
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                int numGuests = (Integer) numGuestsSpinner.getValue();
                boolean foodService = foodServiceCheckBox.isSelected();

                // Validate dates
                if (checkOut.isBefore(checkIn.plusDays(1))) {
                    JOptionPane.showMessageDialog(this, "Minimum stay is 1 night", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try (Connection conn = getConnection()) {
                    // Check room availability and capacity
                    PreparedStatement checkStmt = conn.prepareStatement(
                        "SELECT room_type, price, capacity FROM rooms WHERE room_id=? AND status='available'");
                    checkStmt.setInt(1, roomId);
                    ResultSet rs = checkStmt.executeQuery();

                    if (rs.next()) {
                        String roomType = rs.getString("room_type");
                        double price = rs.getDouble("price");
                        int capacity = rs.getInt("capacity");

                        if (numGuests > capacity) {
                            JOptionPane.showMessageDialog(this, 
                                "This room can only accommodate " + capacity + " guests", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        // Calculate total price
                        long days = ChronoUnit.DAYS.between(checkIn, checkOut);
                        double totalPrice = price * days;
                        if (foodService) {
                            totalPrice += 500 * days;
                        }

                        NumberFormat rupeeFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

                        // Show confirmation dialog
                        String message = String.format(
                            "<html><div style='width:300px;'><h3>Booking Details</h3>" +
                            "<p><b>Room:</b> %s (%d)<br>" +
                            "<b>Type:</b> %s<br>" +
                            "<b>Check-in:</b> %s<br>" +
                            "<b>Check-out:</b> %s<br>" +
                            "<b>Nights:</b> %d<br>" +
                            "<b>Guests:</b> %d<br>" +
                            "<b>Food Service:</b> %s<br><br>" +
                            "<b>Total Price:</b> <span style='color:#006699;font-weight:bold;'>%s</span></p></div></html>",
                            roomType, roomId, roomType, checkIn, checkOut, days, numGuests,
                            foodService ? "Yes (+₹" + (500 * days) + ")" : "No",
                            rupeeFormat.format(totalPrice));

                        int confirm = JOptionPane.showConfirmDialog(this, message, 
                            "Confirm Booking", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

                        if (confirm == JOptionPane.YES_OPTION) {
                            // Create booking with correct column names
                            PreparedStatement bookStmt = conn.prepareStatement(
                                "INSERT INTO bookings (user_id, room_id, check_in_date, check_out_date, " +
                                "num_guests, food_requested, booking_date) VALUES (?, ?, ?, ?, ?, ?, ?)");
                            bookStmt.setInt(1, userId);
                            bookStmt.setInt(2, roomId);
                            bookStmt.setDate(3, Date.valueOf(checkIn));
                            bookStmt.setDate(4, Date.valueOf(checkOut));
                            bookStmt.setInt(5, numGuests);
                            bookStmt.setBoolean(6, foodService);
                            bookStmt.setDate(7, Date.valueOf(LocalDate.now()));
                            bookStmt.executeUpdate();

                            // Update room status
                            PreparedStatement updateStmt = conn.prepareStatement(
                                "UPDATE rooms SET status='booked' WHERE room_id=?");
                            updateStmt.setInt(1, roomId);
                            updateStmt.executeUpdate();

                            JOptionPane.showMessageDialog(this, "Booking confirmed!", "Success", JOptionPane.INFORMATION_MESSAGE);
                            dispose();
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, "Room not available or invalid Room ID", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid Room ID", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    static class AdminDashboard extends JFrame {
        private JTextArea dashboardArea = new JTextArea();

        public AdminDashboard() {
            setTitle("Hotel Management System - Admin Dashboard");
            setSize(1100, 750);
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            setLocationRelativeTo(null);
            getContentPane().setBackground(BACKGROUND_COLOR);
            
            // Header panel
            JPanel headerPanel = new JPanel();
            headerPanel.setBackground(PRIMARY_COLOR);
            headerPanel.setBorder(new EmptyBorder(15, 0, 15, 0));
            JLabel titleLabel = new JLabel("ADMIN DASHBOARD");
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
            titleLabel.setForeground(TEXT_COLOR);
            headerPanel.add(titleLabel);

            // Dashboard Content
            dashboardArea.setEditable(false);
            dashboardArea.setFont(new Font("Consolas", Font.PLAIN, 14));
            dashboardArea.setBackground(SECONDARY_COLOR);
            dashboardArea.setBorder(new EmptyBorder(10, 10, 10, 10));
            
            JScrollPane scrollPane = new JScrollPane(dashboardArea);
            scrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
                "Hotel Management Overview",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14),
                PRIMARY_COLOR));

            // Buttons
            JPanel buttonPanel = new JPanel();
            buttonPanel.setBackground(BACKGROUND_COLOR);
            buttonPanel.setBorder(new EmptyBorder(10, 0, 15, 0));
            
            JButton refreshBtn = createStyledButton("Refresh", new Color(70, 130, 180)); // Steel blue
            JButton logoutBtn = createStyledButton("Logout", ACCENT_COLOR);
            
            buttonPanel.add(refreshBtn);
            buttonPanel.add(logoutBtn);

            refreshBtn.addActionListener(e -> loadDashboardData());
            logoutBtn.addActionListener(e -> {
                dispose();
                new LoginPage();
            });

            // Layout
            setLayout(new BorderLayout());
            add(headerPanel, BorderLayout.NORTH);
            add(scrollPane, BorderLayout.CENTER);
            add(buttonPanel, BorderLayout.SOUTH);

            loadDashboardData();
            setVisible(true);
        }

        private JButton createStyledButton(String text, Color bgColor) {
            JButton button = new JButton(text);
            button.setFont(new Font("Segoe UI", Font.BOLD, 14));
            button.setBackground(bgColor);
            button.setForeground(TEXT_COLOR);
            button.setFocusPainted(false);
            button.setBorder(new EmptyBorder(8, 25, 8, 25));
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            return button;
        }

        private void loadDashboardData() {
            try (Connection conn = getConnection()) {
                StringBuilder sb = new StringBuilder();
                NumberFormat rupeeFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

                // Room Inventory Summary
                sb.append(String.format("%-60s\n", "========== HOTEL MANAGEMENT SYSTEM - ADMIN DASHBOARD =========="));
                sb.append("\n=== ROOM INVENTORY ===\n\n");
                sb.append(String.format("%-10s %-10s %-15s\n", "Room Type", "Total", "Available"));
                sb.append(String.format("%-10s %-10s %-15s\n", "---------", "-----", "---------"));

                String roomSummary = "SELECT room_type, COUNT(*) as total, " +
                    "SUM(CASE WHEN status='available' THEN 1 ELSE 0 END) as available " +
                    "FROM rooms GROUP BY room_type";
                
                ResultSet rs = conn.createStatement().executeQuery(roomSummary);
                while (rs.next()) {
                    sb.append(String.format("%-10s %-10d %-15d\n",
                        rs.getString("room_type"),
                        rs.getInt("total"),
                        rs.getInt("available")));
                }

                // Current Bookings - with null checks
                sb.append("\n=== CURRENT BOOKINGS ===\n\n");
                sb.append(String.format("%-10s %-10s %-10s %-12s %-12s %-6s %-8s %-12s %-10s %-10s\n",
                    "BookingID", "User", "Room", "Type", "Check-in", "Nights", "Guests", "Food", "Total", "Status"));
                sb.append(String.format("%-10s %-10s %-10s %-12s %-12s %-6s %-8s %-12s %-10s %-10s\n",
                    "---------", "----", "----", "----", "--------", "------", "------", "----", "-----", "------"));

                String bookingsQuery = "SELECT b.booking_id, u.username, r.room_id, r.room_type, " +
                    "b.check_in_date, b.check_out_date, b.num_guests, b.food_requested, " +
                    "b.booking_date, b.status, r.price " +
                    "FROM bookings b JOIN users u ON b.user_id = u.id " +
                    "JOIN rooms r ON b.room_id = r.room_id " +
                    "ORDER BY b.check_in_date DESC";
                
                rs = conn.createStatement().executeQuery(bookingsQuery);
                while (rs.next()) {
                    Date checkInDate = rs.getDate("check_in_date");
                    Date checkOutDate = rs.getDate("check_out_date");
                    
                    // Handle null dates
                    String checkInStr = "N/A";
                    String checkOutStr = "N/A";
                    String nightsStr = "N/A";
                    double totalPrice = 0;
                    
                    if (checkInDate != null && checkOutDate != null) {
                        LocalDate checkIn = checkInDate.toLocalDate();
                        LocalDate checkOut = checkOutDate.toLocalDate();
                        checkInStr = checkIn.toString();
                        checkOutStr = checkOut.toString();
                        
                        long days = ChronoUnit.DAYS.between(checkIn, checkOut);
                        nightsStr = String.valueOf(days);
                        
                        double basePrice = rs.getDouble("price") * days;
                        double foodPrice = rs.getBoolean("food_requested") ? 500 * days : 0;
                        totalPrice = basePrice + foodPrice;
                    } else if (checkInDate != null) {
                        checkInStr = checkInDate.toLocalDate().toString();
                    } else if (checkOutDate != null) {
                        checkOutStr = checkOutDate.toLocalDate().toString();
                    }

                    sb.append(String.format("%-10d %-10s %-10d %-12s %-12s %-6s %-8d %-12s %-10s %-10s\n",
                        rs.getInt("booking_id"),
                        rs.getString("username"),
                        rs.getInt("room_id"),
                        rs.getString("room_type"),
                        checkInStr,
                        nightsStr,
                        rs.getInt("num_guests"),
                        rs.getBoolean("food_requested") ? "Yes" : "No",
                        checkInDate != null && checkOutDate != null ? rupeeFormat.format(totalPrice) : "N/A",
                        rs.getString("status")));
                }

                dashboardArea.setText(sb.toString());
            } catch (SQLException e) {
                dashboardArea.setText("Error loading dashboard data:\n" + e.getMessage());
            }
        }
    }
}