//게임 서버의 메인 클래스
//클라이언트 연결을 관리하고, 게임 방을 생성 및 관리함
//서버가 여러 방을 관리하기 위해 사용하는 데이터 구조입니다.

import java.net.*;
import java.io.*;
import java.util.*;

public class YachtServer {
    private static final int PORT = 12345; // 서버 포트 번호
    private static ServerSocket serverSocket;
    public static Map<Integer, GameRoom> rooms = new HashMap<>();
    private static int nextRoomId = 1;

    public static void main(String[] args) {
        System.out.println("Server is starting...");
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Waiting for clients...");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress()); // 여기까지 됨.
                ClientHandler c = new ClientHandler(clientSocket);
                c.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized int createRoom() {
        int roomId = nextRoomId++;
        GameRoom newRoom = new GameRoom(roomId);
        rooms.put(roomId, newRoom);
        System.out.println("Room " + roomId + " created.");
        return roomId;
    }

    public static synchronized GameRoom getRoom(int roomId) {
        return rooms.get(roomId);
    }

    public static synchronized void shutdownServer() {
        try {
            System.out.println("Shutting down server...");
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

