import java.io.*;
import java.net.*;

public class YachtClient {
    private static final String SERVER_IP = "127.0.0.1"; // 서버 IP
    private static final int SERVER_PORT = 12345;       // 서버 포트
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public YachtClient() {
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            System.out.println("Connected to server!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createRoom() {
        out.println("CREATE_ROOM");
        try {
            String response = in.readLine();
            if (response.startsWith("ROOM_CREATED")) {
                System.out.println("Room created with ID: " + response.split(" ")[1]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void joinRoom(int roomId) {
        out.println("JOIN_ROOM " + roomId);
        try {
            String response = in.readLine();
            if (response.startsWith("JOINED_ROOM")) {
                System.out.println("Joined room with ID: " + roomId);
            } else {
                System.out.println(response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void rollDice(int roomId) {
        out.println("ROLL_DICE " + roomId);
    }

    public static void main(String[] args) {
        YachtClient client = new YachtClient();
        client.createRoom(); // 방 생성
        client.joinRoom(1);  // 방 참가 (방 ID는 테스트용으로 1)
        client.rollDice(1);  // 주사위 굴리기
    }
    
    public void disconnect() {
        try {
            if (socket != null) {
                socket.close(); // 소켓 종료
                System.out.println("Disconnected from server.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
