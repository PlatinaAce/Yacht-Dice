import java.io.*;
import java.net.*;
import java.util.*;

public class LobbyServer {
    private static final int PORT = 12345; // 서버 포트
    private static List<ClientHandler> clients = new ArrayList<>(); // 클라이언트 목록

    public static void main(String[] args) {
        System.out.println("Lobby Server is starting...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept(); // 클라이언트 연결 대기
                ClientHandler clientHandler = new ClientHandler(clientSocket); // ClientHandler 사용
                clients.add(clientHandler);
                clientHandler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 모든 클라이언트에게 메시지 브로드캐스트
    static synchronized void broadcast(String message, ClientHandler excludeClient) {
        for (ClientHandler client : clients) {
            if (client != excludeClient) {
                client.sendMessage(message);
            }
        }
    }

    // 클라이언트 제거
    static synchronized void removeClient(ClientHandler client) {
        clients.remove(client);
    }
}