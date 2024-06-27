import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.nio.channels.SocketChannel;

public class Db {

    private JSONObject db;
    private JSONArray userList;
    private File file;

    /*-------------------- GESTAO DB --------------------*/
    @SuppressWarnings("unchecked")
    public Db(String filename) {
        this.file = new File(filename);
        try {
            if (file.exists()) {
                JSONParser parser = new JSONParser();
                db = (JSONObject) parser.parse(new FileReader(file));
                userList = (JSONArray) db.get("database");
            } else {
                userList = new JSONArray();
                db = new JSONObject();
                db.put("database", userList);
                saveDb();
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
            userList = new JSONArray();
            db = new JSONObject();
            db.put("database", userList);
        }
    }

    private void saveDb() {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(db.toJSONString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private JSONObject findUser(String username) {
        for (Object obj : userList) {
            JSONObject user = (JSONObject) obj;
            if (user.get("username").equals(username)) {
                return user;
            }
        }
        return null;
    }

    /*-------------------- CONEXOES --------------------*/
    public Client login(String username, String password, SocketChannel socket) {
        JSONObject userDetails = findUser(username);
        if (userDetails != null && userDetails.get("password").equals(password)) {
            return new Client(username, password, (String) userDetails.get("token"), ((Long) userDetails.get("elo")).intValue(), socket);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public Client register(String username, String password, SocketChannel socket) {
        if (findUser(username) == null) {
            JSONObject userDetails = new JSONObject();
            userDetails.put("username", username);
            userDetails.put("password", password);  
            userDetails.put("token", generateToken()); 
            userDetails.put("elo", 0);
            userList.add(userDetails);
            saveDb();
            return new Client(username, password, (String) userDetails.get("token"), 0, socket);
        }
        return null;
    }

    public Client reconnect(String token, SocketChannel socket) {
        for (Object obj : userList) {
            JSONObject userDetails = (JSONObject) obj;
            if (userDetails.get("token").equals(token)) {
                String username = (String) userDetails.get("username");
                String password = (String) userDetails.get("password");
                int elo = ((Long) userDetails.get("elo")).intValue();
                return new Client(username, password, token, elo, socket);
            }
        }
        return null;
    }

    /*-------------------- TOKENS --------------------*/
    private String generateToken() {
        return Long.toHexString(Double.doubleToLongBits(Math.random()));
    }

    public boolean validateToken(String username, String token) {
        JSONObject userDetails = findUser(username);
        return userDetails != null && userDetails.get("token").equals(token);
    }

    @SuppressWarnings("unchecked")
    public void renewToken(String username) {
        JSONObject userDetails = findUser(username);
        if (userDetails != null) {
            userDetails.put("token", generateToken());
            saveDb();
        }
    }

    public void renewAllTokens() {
        for (Object obj : userList) {
            JSONObject user = (JSONObject) obj;
            renewToken((String) user.get("username"));
        }
    }
    
    public void cancelToken(String username) {
        JSONObject userDetails = findUser(username);
        if (userDetails != null) {
            userDetails.remove("token");
            saveDb();
        }
    }

    /*-------------------- UPDATES JOGADOR --------------------*/
    @SuppressWarnings("unchecked")
    public boolean updatePlayer(String username, int updatedElo) {
        JSONObject userDetails = findUser(username);
        if (userDetails != null) {
            userDetails.put("elo", updatedElo);
            saveDb();
            return true;
        }
        return false;
    }
}
