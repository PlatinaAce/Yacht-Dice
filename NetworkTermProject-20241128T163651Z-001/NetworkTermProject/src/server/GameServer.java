/*
package server;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.ArrayList;
import java.util.List;


public class GameServer {
    private static final int PORT = 12346;
    private static final ExecutorService pool = Executors.newFixedThreadPool(2); // 두 플레이어 처리
    private static Socket[] playerSockets = new Socket[2];
    private static int connectedPlayers = 0;

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Server started. Waiting for players...");

        while (connectedPlayers < 2) {
            Socket clientSocket = serverSocket.accept();
            playerSockets[connectedPlayers] = clientSocket;
            connectedPlayers++;
            System.out.println("Player " + connectedPlayers + " connected.");
            pool.execute(new ClientHandler(clientSocket, connectedPlayers));
        }
        serverSocket.close();
    }
    
    public static synchronized void broadcast(String message, int sender) throws IOException {
        for (int i = 0; i < playerSockets.length; i++) {
            if (i + 1 != sender) {
                PrintWriter out = new PrintWriter(playerSockets[i].getOutputStream(), true);
                out.println(message);
            }
        }
    }
    
    private List<ClientHandler> clients = new ArrayList<>(); // 클라이언트 관리 리스트

    public void addClient(ClientHandler client) {
        clients.add(client);
    }

    public void removeClient(ClientHandler client) {
        clients.remove(client);
    }
    
}
*/

package server;

import java.io.*;
import java.net.*;
import java.util.*;

public class GameServer {
    private List<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) {
        new GameServer().startServer();
    }

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(12371)) {
            System.out.println("Server started on port 12371");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected");
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public synchronized void broadcast(String message, int senderId) {
        for (ClientHandler client : clients) {
            if (client.getId() != senderId) { // 자신 제외
                client.sendMessage(message);
            }
        }
    }
    
    public synchronized void broadcastScore(int totalScore, int senderId) {
        String message = "Player " + senderId + " has saved their score. Total Score: " + totalScore;
        for (ClientHandler client : clients) {
            if (client.getId() != senderId) {  // 자신에게는 전송하지 않음
                client.sendMessage(message);  // 상대방에게 전송
            }
        }
    }

    

    public synchronized void removeClient(ClientHandler client) {
        clients.remove(client);
    }
}
