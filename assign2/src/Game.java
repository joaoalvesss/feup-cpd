import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public class Game implements Runnable {
     private final List<Client> clients;
     private final List<Client> queue;
     private final ReentrantLock queueLock;
     private final int ROUND_NUM = 3; // MUDAR AQUI DEPOIS
     List<Integer> scores = new ArrayList<Integer>();
     private Random rand = new Random();
     private boolean ranked;
     private ReentrantLock db_Lock;
     private Db db;

    public Game(List<Client> clients, List<Client> queue, ReentrantLock queueLock, boolean ranked, Db db, ReentrantLock db_lock) {
        this.clients = clients;
        this.queue = queue;
        this.queueLock = queueLock;
        for (int i = 0; i < clients.size(); i++) scores.add(0);
        this.ranked = ranked;
        this.db_Lock = db_lock;
        this.db = db;
    }

     @Override
     public void run() {
          try {
               String winner = this.playGame();
               for (Client c : this.clients) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        System.out.println("Error in handling connection into login username: " + e.getMessage());
                    }
                    String[] message = Connect.receive_message(c.getSocket()).split("\n");
                    Server.server_request_client("Finish", "FIN", c.getSocket());
               }
          } 
          catch (Exception e) {
               System.out.println(e.getMessage() + " exception happenned in run function in Game.");
          }
     }

     public String playGame() {
          try {

               String winner = "";

               for (int i = 0; i < ROUND_NUM; i++) {
                    int randInt = this.rand.nextInt(100) + 1;
                    System.out.println("Computers number is: " + randInt);
                    for (int j = 0; j < this.clients.size(); j++) {
                         int score = this.getInputFromClient(j) - randInt;
                         this.scores.set(j, this.scores.get(j) + Math.abs(score));
                    }
               }

               int min = Collections.min(this.scores);
               int minIndex = this.scores.indexOf(Collections.min(this.scores));

               int n = 0;
               for (int i = 0; i < this.scores.size(); i++) {
                    if (this.scores.get(i) == min) n++;
               }
               if (n > 1) winner = "tie";
               else winner = this.clients.get(minIndex).getUsername();
               for(int i = 0; i < this.clients.size(); i++){
                    Server.server_request_client(winner+"\n"+min+"\n"+this.scores.get(i), "GAME_OVER", this.clients.get(i).getSocket());
                    Connect.receive_message(this.clients.get(i).getSocket());
               }
               if (this.ranked) {
                    for (Client c : this.clients) {
                         if (c.getUsername().equals(winner)) c.updateRank(1);
                         else c.updateRank(-1);
                         try {
                             this.db_Lock.lock();
                             db.updatePlayer(c.getUsername(), c.getElo());
                             this.db_Lock.unlock();
                         } catch (Exception e) {
                             System.out.println("Exception " + e + " detected.");
                         }
                    }
               }
               return winner;

          } catch (Exception e) {
               System.out.println(e.getMessage() + " exception happenned in playGame function in Game.");
          }
          return null;
     }

     public int getInputFromClient(int i) {
          /*Client c = this.clients.get(i);
          try {
              String message = Connect.receive_message(c.getSocket());
              System.out.println("Received message from client: " + message); // Debug statement
              String[] input = message.split("\n");
              if (input[0].equals("INPUT")) {
                  return Integer.parseInt(input[1]);
              } else {
                  return -1;
              }
          } catch (IOException e) {
              e.printStackTrace();
          }*/
          return this.rand.nextInt(100) + 1; // TO DO ta aqui
      }
      
}
