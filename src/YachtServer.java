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

            // 클라이언트를 저장할 리스트
            List<ClientHandler> connectedClients = new ArrayList<>();

            while (true) {
                // 클라이언트 연결 대기
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                // 클라이언트 핸들러 생성
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                connectedClients.add(clientHandler);
                clientHandler.start();

                // 두 명 연결되면 Room 생성
                if (connectedClients.size() == 2) {
                    System.out.println("Two clients connected. Creating a room...");
                    GameRoom room = new GameRoom(connectedClients.get(0), connectedClients.get(1));

                    // 다음 방을 위해 리스트 초기화
                    connectedClients.clear();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*public static synchronized int createRoom() {
        int roomId = nextRoomId++;
        GameRoom newRoom = new GameRoom(roomId);
        rooms.put(roomId, newRoom);
        System.out.println("Room " + roomId + " created.");
        return roomId;
    }*/

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