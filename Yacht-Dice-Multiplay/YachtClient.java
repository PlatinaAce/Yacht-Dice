//주사위 결과 업데이트(DICE_UPDATE).
//턴 시작/종료 업데이트(TURN n, TURN_END n).

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.Arrays;

public class YachtClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private MultiGameGUI gameGUI; // MultiGameGUI 인스턴스

    public YachtClient() {
        selectGameMode();
    }

    // 게임 모드 선택 (싱글플레이 또는 멀티플레이)
    private void selectGameMode() {
        JFrame frame = new JFrame("Select Game Mode");
        frame.setSize(300, 150);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));

        JLabel label = new JLabel("Select your game mode:");
        label.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        frame.add(label);

        JButton singlePlayButton = new JButton("SinglePlay");
        singlePlayButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
        singlePlayButton.addActionListener(e -> {
            frame.dispose();
            startSinglePlay();
        });
        frame.add(singlePlayButton);

        JButton multiPlayButton = new JButton("MultiPlay");
        multiPlayButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
        multiPlayButton.addActionListener(e -> {
            frame.dispose();
            connectToServer();
        });
        frame.add(multiPlayButton);

        frame.setVisible(true);
    }

    // 싱글플레이 모드 시작
    private void startSinglePlay() {
        System.out.println("Starting SinglePlay mode...");
        SwingUtilities.invokeLater(SingleGameGUI::new); // 로컬 게임 GUI 실행
    }

    // 서버 연결 및 멀티플레이 모드 시작
    private void connectToServer() {
        try {
            String serverAddress = JOptionPane.showInputDialog(null, "Enter server address:", "localhost");
            if (serverAddress == null || serverAddress.isEmpty()) return;

            socket = new Socket(serverAddress, 12345);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            sendMessage("YachtClient joined the server."); // 서버 연결 확인
            System.out.println("Connected to server!");

            handleRoomSelection(); // 방 생성 또는 참여
            startMessageListener(); // 서버 메시지 수신 스레드 시작
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to connect to the server.");
        }
    }

    // 방 생성 또는 참여
    private void handleRoomSelection() throws IOException {
        String[] options = {"Create Room", "Join Room"};
        int choice = JOptionPane.showOptionDialog(
                null,
                "Do you want to create a new room or join an existing room?",
                "Room Selection",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == 0) { // 방 생성
            sendMessage("CREATE_ROOM");
            String serverMessage = in.readLine();
            int roomId = Integer.parseInt(serverMessage.split(" ")[1]);
            JOptionPane.showMessageDialog(null, "Room created! Room ID: " + roomId);
            startMultiPlay(roomId, "P1");
        } else if (choice == 1) { // 방 참여
            String roomIdInput = JOptionPane.showInputDialog("Enter Room ID:");
            sendMessage("JOIN_ROOM " + roomIdInput);
            String serverMessage = in.readLine();
            if (serverMessage.startsWith("JOINED_ROOM")) {
                JOptionPane.showMessageDialog(null, "Joined room successfully!");
                startMultiPlay(Integer.parseInt(roomIdInput), "P2");
            } else {
                JOptionPane.showMessageDialog(null, "Failed to join room. Try again.");
            }
        }
    }

    // 멀티플레이 시작
    private void startMultiPlay(int roomId, String role) {
        SwingUtilities.invokeLater(() -> {
            gameGUI = new MultiGameGUI(in, out); // MultiGameGUI 생성
            gameGUI.setPlayerRole(role); // 역할 설정
        });
    }

    // 서버 메시지 수신 스레드 시작
    private void startMessageListener() {
        new Thread(() -> {
            try {
                String message = "Static message"; // 안전한 코드
                System.out.println(message);
            } catch (Exception e) { // 모든 예외 처리
                e.printStackTrace();
            }
        }).start();
    }

    // 서버 메시지 처리
    private void handleServerMessage(String message) {
        if (gameGUI == null) return; // GUI가 초기화되지 않은 경우 처리하지 않음

        if (message.startsWith("TURN_START")) {
            SwingUtilities.invokeLater(() -> {
                gameGUI.enableTurnControls(); // 사용자의 턴을 활성화
                JOptionPane.showMessageDialog(null, "Your turn!");
            });
        } else if (message.startsWith("TURN_WAIT")) {
            SwingUtilities.invokeLater(() -> {
                gameGUI.disableTurnControls(); // 다른 사용자의 턴, 컨트롤 비활성화
                JOptionPane.showMessageDialog(null, "Waiting for the other player...");
            });
        } else if (message.startsWith("DICE_UPDATE")) {
            String diceData = message.substring("DICE_UPDATE ".length()).trim();
            String[] diceValues = diceData.replace("[", "").replace("]", "").split(", ");
            int[] diceResults = Arrays.stream(diceValues).mapToInt(Integer::parseInt).toArray();

            SwingUtilities.invokeLater(() -> gameGUI.updateDiceDisplay(diceResults)); // 주사위 결과를 UI에 반영
        } else if (message.startsWith("SCORE_UPDATE")) {
            String[] parts = message.split(" ");
            String player = parts[1];
            int score = Integer.parseInt(parts[2]);

            SwingUtilities.invokeLater(() -> gameGUI.updateScore(player, score)); // 점수판 업데이트
        } else if (message.startsWith("GAME_END")) {
            String result = message.substring("GAME_END ".length()).trim(); // 결과 문자열
            SwingUtilities.invokeLater(() -> gameGUI.announceWinner(result)); // 승자 표시
        } else {
            System.out.println("Unknown message from server: " + message);
        }
    }

    // 서버 메시지 읽기
    private String readMessage() {
        try {
            if (in != null) {
                return in.readLine(); // 서버에서 한 줄 읽기
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null; // 읽을 메시지가 없거나 오류 발생 시
    }

    // 메시지 전송
    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    // 연결 종료
    public void closeConnection() {
        try {
            if (socket != null) socket.close();
            if (out != null) out.close();
            if (in != null) in.close();
            System.out.println("Disconnected from server.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new YachtClient();
    }
}




