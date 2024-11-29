import java.io.*;
import java.net.*;
import java.util.*;

public class LobbyServer {
    private static final int PORT = 12345; // 서버 포트
    private static List<ClientHandler> clients = new ArrayList<>(); // 연결된 클라이언트 목록
    private static Map<Integer, GameRoom> rooms = new HashMap<>(); // 생성된 방 목록
    private static int nextRoomId = 1; // 다음 생성할 방의 ID

    public static void main(String[] args) {
        System.out.println("Lobby Server is running...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket); // 독립된 ClientHandler 사용
                clients.add(clientHandler); // 클라이언트 목록에 추가
                clientHandler.start(); // 클라이언트 스레드 실행
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 방 생성 메서드
    public static synchronized int createRoom(ClientHandler creator) {
        int roomId = nextRoomId++;
        GameRoom newRoom = new GameRoom(roomId); // 새로운 방 생성
        newRoom.addPlayer(creator); // 방 생성자를 첫 번째 플레이어로 등록
        creator.setCurrentRoom(newRoom); // 클라이언트에게 현재 방 정보 설정
        rooms.put(roomId, newRoom); // 방을 전체 방 목록에 추가
        System.out.println("Room " + roomId + " created by Player " + creator.getPlayerId());
        return roomId;
    }

    // 방 검색 메서드
    public static synchronized GameRoom getRoom(int roomId) {
        return rooms.get(roomId);
    }
}





