import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.sql.Connection;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReentrantLock;
import org.json.simple.parser.ParseException;

public class Server {
    // Constants
    private final int MAX_GAMES = 5;
    private final int MAX_CLIENTS = 10;
    private static final int GAME_PLAYERS = 2;

    // Server fields
    private List<Client> queue;
    private String filename;
    private int mode;
    private int port;
    private ServerSocketChannel serverChannel;
    private Selector selector;
    private Db db;
    private ExecutorService threadPoolGames;
    private ExecutorService threadPoolClients;

    // Locks
    private ReentrantLock db_lock;
    private ReentrantLock queue_lock;
    private ReentrantLock token_lock;

    private List<String> logged_usernames;

    private int maxEloDiff;

    private long time;
    private boolean ranked;

    // Constructor
    public Server(int mode, int port, String filename, boolean ranked) throws IOException, ParseException {
        this.mode = mode;
        this.port = port;
        this.filename = filename;
        this.db = new Db(this.filename);
        this.threadPoolClients = Executors.newFixedThreadPool(this.MAX_CLIENTS);
        this.threadPoolGames = Executors.newFixedThreadPool(this.MAX_GAMES);
        this.db_lock = new ReentrantLock();
        this.queue_lock = new ReentrantLock();
        this.token_lock = new ReentrantLock();
        this.queue = new ArrayList<>();
        this.logged_usernames = new ArrayList<>();
        this.maxEloDiff = 3;
        this.time = System.currentTimeMillis();
        this.ranked = ranked;
    }

    // Start the server
    public void start() throws IOException {
        this.serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(this.port));
        serverChannel.configureBlocking(false);
        this.selector = Selector.open();
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        if (this.ranked) System.out.println("Server is listening on port " + this.port + " with Ranked mode.");
        else System.out.println("Server is listening on port " + this.port + " with Simple mode.");
    }

    // Run the server
    public void run() {
        this.db_lock.lock();
        try {
            this.db.renewAllTokens();
        } finally {
            this.db_lock.unlock();
        }

        Thread simpleGameThread = Thread.ofVirtual().unstarted(() -> {
            while (true) {
                if (this.ranked) createRankedGame();
                else createSimpleGame();
            }
        });

        Thread authThread = Thread.ofVirtual().unstarted(this::createAuth);

        simpleGameThread.start();
        authThread.start();

        try {
            simpleGameThread.join();
            authThread.join();
        } catch (InterruptedException e) {
            System.out.println("Server interrupted: " + e.getMessage());
        }
    }

    public void createSimpleGame() {
        queue_lock.lock();
        try {
            if (queue.size() >= GAME_PLAYERS) {
                List<Client> gamePlayers = new ArrayList<>();
                for (int i = 0; i < GAME_PLAYERS; i++) {
                    Client c = queue.remove(0);
                    gamePlayers.add(c);
                    this.logged_usernames.remove(c.getUsername());
                }

                System.out.println("There were enough players into the queue, so a game was created!");
                Game game = new Game(gamePlayers, this.queue, this.queue_lock, false, db, db_lock);
                threadPoolGames.submit(game);
                System.out.println("New Simple game mode has started.");
            } else {
                //System.out.println("Not enough players to start a new game.");
            }
        } catch(Exception e) {
            System.out.println(e.getMessage() + " exception happened in createSimpleGame function in Server.");
        } finally {
            queue_lock.unlock();
        }
    }

    public boolean rankedGamePossible() {
        int player1rank = queue.get(0).getElo();
        int num_players = 1;
        for (int i = 1; i < queue.size(); i++) {
            if (Math.abs(queue.get(i).getElo() - player1rank) < this.maxEloDiff) num_players++;
        }
        if (num_players >= GAME_PLAYERS) return true;
        return false;
    }

    public List<Client> rankedGamePlayers() {
        int player1rank = queue.get(0).getElo();
        List<Client> players = new ArrayList<>();
        List<Client> toRemove = new ArrayList<>();  // Temporary list to store players to be removed
    
        for (Client c : queue) {
            System.out.println(c.getElo());
            System.out.println(players.size());
            System.out.println(queue.size());
            if (Math.abs(c.getElo() - player1rank) < this.maxEloDiff && GAME_PLAYERS > players.size()) {
                players.add(c);
                toRemove.add(c);  // Add to the temporary list instead of removing directly
                this.logged_usernames.remove(c.getUsername());
            }
        }
    
        // Remove the players after the iteration is complete
        queue.removeAll(toRemove);
        
        return players;
    }
    

    public void createRankedGame() {
        queue_lock.lock();
        // System.out.println(this.maxEloDiff);
        try {
            if (queue.size() >= GAME_PLAYERS) {
                if (rankedGamePossible()) {
                    List<Client> gamePlayers = rankedGamePlayers();
                    for (Client c : gamePlayers) System.out.println(c.getUsername());
                    System.out.println("There were enough players into the queue, so a game was created!");
                    Game game = new Game(gamePlayers, this.queue, this.queue_lock, true, db, db_lock);
                    threadPoolGames.submit(game);
                    System.out.println("New Ranked game mode has started.");
                    this.maxEloDiff = 3;
                }
            } else {

                if (System.currentTimeMillis() - this.time > 3000) {
                    this.time = System.currentTimeMillis();
                    if (queue.size() > 0) this.maxEloDiff += 1;
                }
                //System.out.println("Not enough players to start a new game.");
            }
        } catch(Exception e) {
            System.out.println(e.getMessage() + " exception happened in createSimpleGame function in Server.");
        } finally {
            queue_lock.unlock();
        }
    }

    public void createAuth() {
        while (true) {
            try {
                selector.select();  // Block until an I/O event occurs
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectedKeys.iterator();

                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();

                    if (key.isAcceptable()) {
                        handleAccept(key);
                    } else if (key.isReadable()) {
                        handleRead(key);
                    }
                }
            } catch (Exception e) {
                System.out.println("Error in createAuth: " + e.getMessage());
            }
        }
    }

    private void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverChannel.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);
        System.out.println("Accepted new client connection.");
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        try {
            handleConnection(socketChannel);
        } catch (Exception e) {
            System.out.println("Error handling client connection: " + e.getMessage());
            socketChannel.close();
        }
    }

    private void handleConnection(SocketChannel socket) throws IOException {
        Client client = null;
        String option, user, pass, token;
        var time1 = System.currentTimeMillis();

        while (client == null) {
            if (System.currentTimeMillis() - time1 >= 50000) {
                    System.out.println("Server line 166");
                Server.server_request_client("Slow client connection avoided", "FIN", socket);
                return;
            }
            String[] message = Connect.receive_message(socket).split("\n");
            option = message[0];

            switch (option) {
                case "login": // Login
                    Server.server_request_client("Enter username:", "REQ1", socket);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        System.out.println("Error in handling connection into login username: " + e.getMessage());
                    }
                    user = Connect.receive_message(socket);
                    System.out.println("USER: "+user);
                    Server.server_request_client("Enter password:", "REQ2", socket);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        System.out.println("Error in handling connection into login password: " + e.getMessage());
                    }
                    pass = Connect.receive_message(socket);
                    System.out.println("PASS: "+pass);
                    client = login(user, pass, socket);
                    if (client == null) {
                        Server.server_request_client("Login failed", "NACK", socket);
                    } else {
                        this.logged_usernames.add(client.getUsername());
                        Server.server_request_client(client.getToken(), "AUTH", socket);
                    }
                    break;

                case "register": // Register
                    Server.server_request_client("Enter username:", "REQ1", socket);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        System.out.println("Error in handling connection into register username: " + e.getMessage());
                    }
                    user = Connect.receive_message(socket).split("\n")[0];
                    Server.server_request_client("Enter password:", "REQ2", socket);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        System.out.println("Error in handling connection into register password: " + e.getMessage());
                    }
                    pass = Connect.receive_message(socket).split("\n")[0];
                    client = register(user, pass, socket);
                    if (client == null) {
                        Server.server_request_client("Registration failed", "NACK", socket);
                    } else {
                        this.logged_usernames.add(client.getUsername());
                        Server.server_request_client("client.getToken()", "AUTH", socket);
                    }
                    break;

                case "reconnect": // Reconnect
                    token = message[1];
                    client = reconnect(token, socket);
                    System.out.println(token);
                    if (client == null) {
                        Server.server_request_client("Reconnection failed", "NACK", socket);
                    } else {
                        Server.server_request_client("Reconnection successful", "AUTH", socket);
                    }
                    break;

                case "exit": // Exit
                    System.out.println("Server line 233");
                    Server.server_request_client("Connection ended", "FIN", socket);
                    socket.close();
                    return;

                default:
                    //Server.server_request_client("Invalid option selected", "NACK", socket);
                    continue;
            }

            if (client != null) {
                try {
                    this.queue_lock.lock();
                    if (clientInQueue(client.getUsername())) {
                        // Client already in the queue, update socket
                        client.setSocket(socket);
                        System.out.println("UPDATED SOCKET");
                        // Server.server_request_client("You were already at queue, go again to queue", "GO_QUEUE", client.getSocket());
                        // Connect.receive_message(client.getSocket());
                    } else {
                        queue.add(client);
                        System.out.println("NOME DO CLIENTE É: " + client.getUsername());
                        // Server.server_request_client("You got added to the queue", "GO_QUEUE", client.getSocket());
                        // Connect.receive_message(client.getSocket());
                    }
                } catch (Exception e) {
                    System.out.println("Error inserting client in queue: " + e.getMessage() + " in handleConnection.");
                } finally {
                    this.queue_lock.unlock();
                }
            }
        }
    }

    public boolean clientInQueue(String username) {
        for (Client c : this.queue) if (c.getUsername().equals(username)) return true;
        return false;
    }

    public void updateElo(String username, int elo) {
        try {
            this.db_lock.lock();
            db.updatePlayer(username, elo);
            this.db_lock.unlock();
        } catch (Exception e) {
            System.out.println("Exception " + e + " detected.");
        }
    }

    // Register a new user
    public Client register(String username, String password, SocketChannel userSocket) throws IOException {
        try {
            this.db_lock.lock();
            Client client = db.register(username, password, userSocket);
            this.db_lock.unlock();
            return client;
        } catch (Exception e) {
            System.out.println("Exception " + e + " detected.");
        }
        return null;
    }

    public boolean clientLoggedIn(String username) {
        for (int i = 0; i < this.logged_usernames.size(); i++) {
            if (this.logged_usernames.get(i).equals(username)) return true;
        }
        return false;
    }

    // Login a user
    public Client login(String username, String password, SocketChannel userSocket) throws IOException {
        System.out.println("USERNAME: " + username);
        System.out.println(this.logged_usernames);
        if (!clientLoggedIn(username)) {
            try {
                this.db_lock.lock();
                Client client = db.login(username, password, userSocket);
                this.db_lock.unlock();
                return client;
            } catch (Exception e) {
                System.out.println("Exception " + e + " detected.");
            }
        }
        return null;
    }

    // Reconnect a user
    public Client reconnect(String usertoken, SocketChannel userSocket) {
        this.db_lock.lock();
        try {
            Client client = db.reconnect(usertoken, userSocket);
            return client;
        } finally {
            this.db_lock.unlock();
        }
    }

    public static void server_request_client(String displayMessage, String type, SocketChannel clientSocket) {
        try {
            Connect.send_message(clientSocket, type + "\n" + displayMessage);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Exception " + e + " caught into server_request_client");
        }
    }

    // Main method
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java Server");
            System.out.println("Missing argument for ranked! 0 | 1");
            return;
        }
        boolean ranked = true;
        if (args[0].equals("0")) ranked = false;

        int mode = 0;
        int port = 12345;
        String filename = "db.json";

        try {
            Server server = new Server(mode, port, filename, ranked);
            server.start();
            server.run();
        } catch (Exception e) {
            System.out.println("Server error occurred: " + e.getMessage());
        }
    }
}
