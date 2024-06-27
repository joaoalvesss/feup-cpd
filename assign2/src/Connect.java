import java.io.*;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Connect {

    private final int port;
    private final String host;
    private SocketChannel socket;
    private String token_dir = "/";
    private Gui gui;

    public Connect(int port, String host) {
        this.port = port;
        this.host = host;
    }

    public void open() throws IOException {
        this.socket = SocketChannel.open();
        this.socket.connect(new InetSocketAddress(this.host, this.port));
    }

    public void close() throws IOException {
        this.socket.close();
    }

    public static void send_message(SocketChannel socket, String message) throws IOException {
        System.out.println("Send message: " + message);
        ByteBuffer buffer = ByteBuffer.allocate(512);
        buffer.clear();
        buffer.put(message.getBytes());
        buffer.flip();
        while (buffer.hasRemaining()) {
            socket.write(buffer);
        }
    }

    public static String receive_message(SocketChannel socket) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(512);
        buffer.clear();  // Clear the buffer before reading
        int bytesRead = socket.read(buffer);
        if (bytesRead == -1) {
            throw new IOException("Connection closed by server");
        }
        buffer.flip();  // Flip the buffer for reading
        byte[] bytes = new byte[bytesRead];
        buffer.get(bytes);  // Read the bytes from the buffer
        String message = new String(bytes);
        //System.out.println(message);
        return message;
    }

    public void writeToken(String filename, String fileContent) throws IOException {
        File currentDir;
        try {
            currentDir = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile();
            token_dir = currentDir.getAbsolutePath() + File.separator;
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("TOKEN DIR: " + token_dir);
        File file = new File(token_dir + filename);
        if (!file.exists()) {
            file.createNewFile();
        }
        try (BufferedWriter bW = new BufferedWriter(new FileWriter(file.getAbsoluteFile()))) {
            bW.write(fileContent);
        }
    }

    public String read_token(String filename) throws IOException {
        
        File currentDir;
        try {
            currentDir = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile();
            token_dir = currentDir.getAbsolutePath() + File.separator;
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println(filename);
        System.out.println(token_dir);
        if (filename == null || filename.equals("")) {
            return null;
        }
        File file = new File(token_dir + filename);
        if (!file.exists()) {
            return null;
        }
        StringBuilder fileContent = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                fileContent.append(line);
            }
        }
        return fileContent.toString();
    }

    public boolean setConnection() throws IOException { // player to server
        boolean isAuthenticated = false;
        String requestType = "";
        String[] response;
        String[] credentials = new String[3];
        String status = "MENU";
        String loginStatus = "none";
        String registerStatus = "none";
        String reconnectStatus = "none";
        while (!requestType.equals("AUTH") && !requestType.equals("FIN")) {

            if (!requestType.equals("")) System.out.println(requestType);
            System.out.println(status);
            switch (status) {
                case "MENU":
                    String userAction = menuGUI();
                    if (!(userAction == null)){
                        switch (userAction) {
                            case "login":
                                status = "LOGIN";
                                loginStatus = "none";
                                break;
                            case "register":
                                status = "REGISTER";
                                registerStatus = "none";
                                break;
                            case "reconnect":
                                status = "RECONNECT";
                                reconnectStatus = "none";
                                break;
                            case "exit":
                                send_message(this.socket, "exit");
                                response = receive_message(this.socket).split("\n");
                                requestType = response[0];
                                break;
                            default:
                                System.out.println("Wrong userAction received from GUI");
                                break;
                    }}
                    break;

                case "LOGIN": // USER, acho eu
                    // Get credentials from the GUI
                    System.out.println(loginStatus);
                    if (loginStatus.equals("none")) {
                        System.out.println("printed login");
                        credentials = loginGUI(false);
                        loginStatus = "click";
                    }
                    if (credentials == null) continue;
                    else if (credentials[0].equals("BACK_BUTTON")) {
                        status = "MENU";
                        loginStatus = "none";
                    } else {
                        if (loginStatus.equals("click")) {
                            send_message(this.socket, "login");
                            loginStatus = "sentLogin";
                        }
                        else if (loginStatus.equals("sentLogin")) {
                            response = receive_message(this.socket).split("\n");
                            requestType = response[0];
                            if (requestType.equals("REQ1")) { // resposta do server ta certa?
                                send_message(this.socket, credentials[0]);
                                System.out.println(credentials[0]);
                                loginStatus = "sentUsername";
                            }
                            else loginStatus = "click";
                        }
                        else if (loginStatus.equals("sentUsername")) {
                            response = receive_message(this.socket).split("\n");
                            requestType = response[0];
                            if (requestType.equals("REQ2")) { // resposta do server ta certa?
                                send_message(this.socket, credentials[1]);
                                System.out.println(credentials[1]);
                                loginStatus = "sentPassword";
                            }
                            else loginStatus = "sentLogin";
                        }
                        else if (loginStatus.equals("sentPassword")) {
                            response = receive_message(this.socket).split("\n");
                            requestType = response[0];
                            if (requestType.equals("AUTH")) { // resposta do server ta certa?
                                isAuthenticated = true;
                                status = "END";
                                System.out.println("Logged into" + credentials[0]);
                                writeToken("token_file", response[1]);
                            }
                            else loginStatus = "none";
                        }
                    }
                    break;

                case "RECONNECT":
                    // Get token from the GUI
                    String token = read_token("token_file");
                    if (reconnectStatus.equals("none")) {
                        send_message(this.socket, "reconnect\n" + token);
                        reconnectStatus = "sent";
                    }
                    else if (reconnectStatus.equals("sent")) {
                            response = receive_message(this.socket).split("\n");
                            requestType = response[0];
                            if (requestType.equals("AUTH")) { // resposta do server ta certa?
                                isAuthenticated = true;
                                status = "END";
                                System.out.println("Logged into" + token);
                            }
                            else {
                                reconnectStatus = "none";
                                status = "MENU";
                            }
                    }
                    break;

                case "REGISTER":
                    if (registerStatus.equals("none")) {
                        credentials = registerGUI(false);
                        registerStatus = "click";
                    }
                    if (credentials[0].equals("BACK_BUTTON")) {
                        status = "MENU";
                        registerStatus = "none";
                    } else {
                        if (registerStatus.equals("click")) {
                            send_message(this.socket, "register");
                            registerStatus = "sentRegister";
                        }
                        else if (registerStatus.equals("sentRegister")) {
                            response = receive_message(this.socket).split("\n");
                            requestType = response[0];
                            if (requestType.equals("REQ1")) { // resposta do server ta certa?
                                send_message(this.socket, credentials[0]);
                                registerStatus = "sentUsername";
                            }
                            else registerStatus = "click";
                        }
                        else if (registerStatus.equals("sentUsername")) {
                            response = receive_message(this.socket).split("\n");
                            requestType = response[0];
                            if (requestType.equals("REQ2")) { // resposta do server ta certa?
                                send_message(this.socket, credentials[1]);
                                registerStatus = "sentPassword";
                            }
                            else registerStatus = "sentRegister";
                        }
                        else if (registerStatus.equals("sentPassword")) {
                            response = receive_message(this.socket).split("\n");
                            requestType = response[0];
                            if (requestType.equals("AUTH")) { // resposta do server ta certa?
                                isAuthenticated = true;
                                status = "END";
                                System.out.println("Logged into" + credentials[0]);

                                writeToken("token_file", response[1]);
                            }
                            else registerStatus = "none";
                        }
                    }
                    break;
            }
        }
        if (isAuthenticated) loginGUI(true);
        return isAuthenticated;
    }

    public void GUIManager() throws IOException {
        String serverRequest = "";
        while (!serverRequest.equals("FIN")) {
            serverRequest = receive_message(this.socket);
            System.out.println("Received from server: " + serverRequest);
            String[] requestParts = serverRequest.split("\n");
            String type = requestParts[0].toUpperCase();
    
            switch (type) {
                case "GO_QUEUE":
                    Connect.send_message(this.socket, "ACK");
                    queueGUI();
                    break;
                    
                case "GAME_SCREEN":
                    int input = gameGUI();
                    String msg = "INPUT\n" + input;
                    System.out.println("Sending message: " + msg);
                    Connect.send_message(this.socket, msg);
                    break;
                
                case "FIN":
                    //close();
                    break;
    
                case "GAME_OVER":
                    this.gui.drawInQueue(false, true);
                    Connect.send_message(this.socket, "FIN");
                    String[] message = {requestParts[1], requestParts[2], requestParts[3]};
                    gameOverGUI(message);
                    break;
    
                default:
                    System.out.println("Unknown server request of type: " + type);
                    break;
            }
        }
    }
    

    private void startGUI() {
        this.gui = new Gui();
    }

    private String menuGUI() {
        return this.gui.drawMenu(false);
    }

    private String[] loginGUI(boolean queue) {
        return this.gui.drawLogin(false, queue);
    }

    private String[] registerGUI(boolean queue) {
        return this.gui.drawRegister(false, queue);
    }
    
    private void queueGUI() {
        this.gui.drawInQueue(false, false);      
    }

    private int gameGUI() {
        return this.gui.drawGameScreen(false);
    }

    private void gameOverGUI(String[] game_info) {
        this.gui.drawGameOver(game_info);
    }

    private String[] reconnectGUI() {
        return this.gui.drawReconnect(false); 
    }


    public static void main(String[] args) {
        int port = 12345;
        String host = "localhost";

        Connect connect = new Connect(port, host);

        try {
            connect.open();
            System.out.println("Connected to server at " + host + ":" + port);
            
            connect.startGUI();

            // Authenticate
            boolean isAuthenticated = connect.setConnection(); // if yes, go manage gui
            if (isAuthenticated) {
                System.out.println("Authentication successful!");
                
                connect.GUIManager();
                connect.close();
            } else {
                System.out.println("Authentication failed or process ended.");
                connect.close();
            }
        } 
        catch (IOException e) {
            e.printStackTrace();
        }
    }

}
