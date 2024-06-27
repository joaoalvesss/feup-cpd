import java.awt.*;
import java.util.TimerTask;
import java.util.Timer;
import java.util.concurrent.CountDownLatch;
import javax.swing.*;

public class Gui extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel statusLabel;
    private JLabel roundLabel;
    private JLabel scoreLabel;
    private JTextField tokenField;

    private String[] credentials; 
    private String token; 
    private String userAction;
    private int inputNumber;
    private final Object lock = new Object();


    String username = "";
    String password = "";

    public Gui() {
        setTitle("Lucky Numbers");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        add(mainPanel);

        drawMenu(true);
        drawLogin(true, false);
        drawRegister(true, false);
        drawInQueue(true, false);
        drawGameScreen(true);
        drawReconnect(true);

        setVisible(true);
    }

    public String drawMenu(boolean constructor) {
        JPanel menuPanel = new JPanel(new GridLayout(4, 1)); 
        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");
        JButton reconnectButton = new JButton("Reconnect"); 
        JButton exitButton = new JButton("Exit");

        loginButton.addActionListener(e -> {
            userAction = "login";
            cardLayout.show(mainPanel, "Login");
            unblock();
        });
        registerButton.addActionListener(e -> {
            userAction = "register";
            cardLayout.show(mainPanel, "Register");
            unblock();
        });
        reconnectButton.addActionListener(e -> {
            userAction = "reconnect";
            cardLayout.show(mainPanel, "Reconnect");
            unblock();
        }); 
        exitButton.addActionListener(e -> {
            userAction = "exit";
            System.exit(0);
        });

        menuPanel.add(loginButton);
        menuPanel.add(registerButton);
        menuPanel.add(reconnectButton); 
        menuPanel.add(exitButton);

        mainPanel.add(menuPanel, "Menu");

        if (!constructor) block();
        return userAction;
    }

    public String[] drawLogin(boolean constructor, boolean queue) {
        if (queue) { 
            cardLayout.show(mainPanel, "InQueue");
            return new String[]{};
        }
        JPanel loginPanel = new JPanel(new GridLayout(4, 1));
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JButton loginButton = new JButton("Login");
        JButton backButton = new JButton("Back");
        statusLabel = new JLabel("", JLabel.CENTER);

        loginButton.addActionListener(e -> {
            username = usernameField.getText();
            password = new String(passwordField.getPassword());
            credentials = new String[]{username, password};
            System.out.println("GUI Login: credentials are " + credentials[0] + " and " + credentials[1]);
            unblock();
        });

        backButton.addActionListener(e -> {
            credentials = new String[]{"BACK_BUTTON", "BACK_BUTTON"};
            cardLayout.show(mainPanel, "Menu");
            unblock();
        });

        loginPanel.add(new JLabel("Username:", JLabel.CENTER));
        loginPanel.add(usernameField);
        loginPanel.add(new JLabel("Password:", JLabel.CENTER));
        loginPanel.add(passwordField);
        loginPanel.add(loginButton);
        loginPanel.add(backButton);
        loginPanel.add(statusLabel);

        mainPanel.add(loginPanel, "Login");

        System.out.println("GUI Login: constructor " + constructor);
        if (!constructor) block();
        return credentials;
    }

    public String[] drawRegister(boolean constructor, boolean queue) {
        if (queue) { 
            cardLayout.show(mainPanel, "InQueue");
            return new String[]{};
        }
        JPanel registerPanel = new JPanel(new GridLayout(4, 1));
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JButton registerButton = new JButton("Register");
        JButton backButton = new JButton("Back");
        statusLabel = new JLabel("", JLabel.CENTER);

        registerButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            credentials = new String[]{username, password};
            unblock();
        });

        backButton.addActionListener(e -> {
            credentials = new String[]{"BACK_BUTTON", "BACK_BUTTON"};
            cardLayout.show(mainPanel, "Menu");
            unblock();
        });

        registerPanel.add(new JLabel("Username:", JLabel.CENTER));
        registerPanel.add(usernameField);
        registerPanel.add(new JLabel("Password:", JLabel.CENTER));
        registerPanel.add(passwordField);
        registerPanel.add(registerButton);
        registerPanel.add(backButton);
        registerPanel.add(statusLabel);

        mainPanel.add(registerPanel, "Register");

        if (!constructor) block();
        return credentials;
    }

    public int drawGameScreen(boolean constructor) {
        cardLayout.show(mainPanel, "GameScreen");
        JPanel gamePanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
    
        JLabel instructionLabel = new JLabel("Enter a number between 1 and 100:", JLabel.CENTER);
        JTextField inputField = new JTextField(10);
        JButton submitButton = new JButton("Submit");
    
        submitButton.addActionListener(e -> {
            String input = inputField.getText();
            int a;
            try {
                a = Integer.parseInt(input);
            } catch (NumberFormatException o) {
                a = -1;
            }
            if (a < 101 && a > 0) {
                inputNumber = a;
                unblock();
            }
        });
    
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.anchor = GridBagConstraints.CENTER;
        gamePanel.add(instructionLabel, gbc);
    
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 10, 0);
        gamePanel.add(inputField, gbc);
    
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(0, 10, 10, 0);
        gamePanel.add(submitButton, gbc);
    
        mainPanel.add(gamePanel, "GameScreen");
        if (!constructor) block();
        return inputNumber;
    }    
    

    public void drawGameOver(String[] message) {
        JPanel gameOverPanel = new JPanel(new GridLayout(2, 1));
        String winner = message[0] + " with score " + message[1];
        String yourScore = message[2];
        JLabel messageLabel = new JLabel("Game Over.\n Winner is " + winner + "!",  JLabel.CENTER);
        JLabel scoreLabel = new JLabel("Your Score was " + yourScore, JLabel.CENTER);
    
        gameOverPanel.add(messageLabel, BorderLayout.CENTER);
        gameOverPanel.add(scoreLabel, BorderLayout.CENTER);
    
        Component[] components = mainPanel.getComponents();
        for (Component component : components) {
            if (component instanceof JPanel && ((JPanel) component).getLayout() instanceof GridLayout) {
                mainPanel.remove(component);
            }
        }

        mainPanel.add(gameOverPanel, "GameOver");
        cardLayout.show(mainPanel, "GameOver");

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.exit(0);
            }
        }, 10000); 
    }

    public void drawInQueue(boolean constructor, boolean gameOver) {
        
        if (gameOver) cardLayout.show(mainPanel, "GameOver");

        cardLayout.show(mainPanel, "InQueue");
        JPanel inQueuePanel = new JPanel(new BorderLayout());
        JLabel queueLabel = new JLabel("Waiting for other players...", JLabel.CENTER);


        inQueuePanel.add(queueLabel, BorderLayout.CENTER);

        mainPanel.add(inQueuePanel, "InQueue");
    }

    public String[] drawReconnect(boolean constructor) {
        JPanel reconnectPanel = new JPanel(new GridLayout(4, 1));
        tokenField = new JTextField();
        JButton reconnectButton = new JButton("Reconnect");
        JButton backButton = new JButton("Back");
        statusLabel = new JLabel("", JLabel.CENTER);

        reconnectButton.addActionListener(e -> {
            token = tokenField.getText();
            unblock();
        });

        backButton.addActionListener(e -> {
            token = "BACK_BUTTON";
            cardLayout.show(mainPanel, "Menu");
            unblock();
        });

        reconnectPanel.add(new JLabel("Token:", JLabel.CENTER));
        reconnectPanel.add(tokenField);
        reconnectPanel.add(reconnectButton);
        reconnectPanel.add(backButton);
        reconnectPanel.add(statusLabel);

        mainPanel.add(reconnectPanel, "Reconnect");

        if (!constructor) block();
        return new String[]{token};
    }

    public void updateRound(int round, int score1, int score2) {
        roundLabel.setText("Round: " + round);
        scoreLabel.setText("Scores - Player 1: " + score1 + ", Player 2: " + score2);
    }

    private void block() {
        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void unblock() {
        synchronized (lock) {
            lock.notify();
        }
    }
}
