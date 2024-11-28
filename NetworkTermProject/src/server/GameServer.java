package server;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class GameServer {
    private static final int PORT = 12345;
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
}
