import java.nio.channels.SocketChannel;

public class Client {
     private String username;
     private String password;
     private String token;
     private int elo;
     private SocketChannel socket;


     public Client(String username, String password, String token, int elo, SocketChannel socket) {
          this.username = username;
          this.password = password;
          this.token = token; 
          this.elo = elo;
          this.socket = socket;
     }

     public String getUsername() { return username; }
     public String getPassword() { return password; }
     public String getToken() { return token; }
     public int getElo() { return elo; }
     public SocketChannel getSocket() { return socket; } 

     public void setUsername(String username) { this.username = username; }
     public void setPassword(String password) { this.password = password; }
     public void setToken(String token) { this.token = token; }
     public void setElo(int elo) { this.elo = elo; }
     public void setSocket(SocketChannel socket) { this.socket = socket; } 

     public void updateRank(int elo) { this.elo += elo; }
     
     public boolean authenticate(String password) { return this.password.equals(password); }

     @Override
     public String toString() {
          return "User{" +
                    "username='" + username + '\'' +
                    ", token='" + token + '\'' +
                    ", elo=" + elo +
                    '}';
     }
}
