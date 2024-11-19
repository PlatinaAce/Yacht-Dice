import java.io.*;
import java.net.*;

public class LobbyServer {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            System.out.println("Server is running...");

            // 첫 번째 클라이언트 연결
            Socket client1 = serverSocket.accept();
            PrintWriter out1 = new PrintWriter(client1.getOutputStream(), true);
            BufferedReader in1 = new BufferedReader(new InputStreamReader(client1.getInputStream()));
            String username1 = in1.readLine();
            System.out.println("Client 1 connected: " + username1);
            out1.println("Waiting for another player...");

            // 두 번째 클라이언트 연결
            Socket client2 = serverSocket.accept();
            PrintWriter out2 = new PrintWriter(client2.getOutputStream(), true);
            BufferedReader in2 = new BufferedReader(new InputStreamReader(client2.getInputStream()));
            String username2 = in2.readLine();
            System.out.println("Client 2 connected: " + username2);
            out2.println("Waiting for another player...");

            // 게임 시작 메시지 전송
            out1.println("Game is starting! You are now playing with " + username2);
            out2.println("Game is starting! You are now playing with " + username1);

            // 소켓 닫기 (테스트용)
            client1.close();
            client2.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}