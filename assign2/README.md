## Simple Multiplayer Game Client-Server Application

### Description

This project is a simple multiplayer game application where multiple clients connect to a server to play a number-guessing game. Computer generates numbers between 1 and 100, and the server determines the winner based on whose guess is closest to a randomly generated number in a better of 3 number generations (3 rounds), wins the player with least different to the computer numbers at the end. The game has simple and ranked modes, runs in virtual threads, has locks to protect from various threads using the same things at the same time and has timeouts for slow clients. Game run at localhost with port 12345. A user can reconnect but this feature has bugs as: only the first connect user can reconnect, and with a reconnect player the game doesnt start due to socket problems. 

### Components

#### Client

- **Connect.java:** Manages server connection, handles messaging, and coordinates the GUI.
- **GUI.java:** Provides the graphical interface for client operations.

#### Server

- **Server.java:** Manages client connections, authentication, and game sessions.
- **Game.java:** Contains the game logic and manages player inputs.

### How to Run

#### Prerequisites

- Java 21 Development Kit (JDK) installed.
- Open the project in vscode and place the json.jar folder into the **Referenced Libraries** (left bottom) that exists into **Java Projects** to avoid any kind of errors related with that library.

#### Steps

- Inside of src dir:

1. **Compile the Java Files:**

   ```sh
   javac -cp libs/json.jar *.java
   ```

2. **Open Server:**

   Simple mode:
   ```sh
   java -classpath ".;libs/json.jar" Server 0
   ```
   Ranked mode:
   ```sh
   java -classpath ".;libs/json.jar" Server 1
   ```

3. **Open Client Connection:**

   ```sh
   java -classpath ".;libs/json.jar" Connect
   ```
### Already created users

- Here are some login credentials for testing:
```
username: joao
password: joao
```

```
username: 321
password: 321
```

### Usage

1. **Starting the Server:**
   - Run the server application to listen for client connections.

2. **Client Operations:**
   - **Login:** Enter your username and password.
   - **Register:** Create a new account.
   - **Reconnect:** Use a token to reconnect.
   - **Exit:** exit the game.

---
