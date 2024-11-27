//서버에 연결하여 싱글플레이 또는 멀티 플레이 모드를 선택하고 실행

import javax.swing.*;
import java.io.*;
import java.net.*;

public class YachtClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public YachtClient() {
        selectGameMode();
    }

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

    private void startSinglePlay() {
        System.out.println("Starting SinglePlay mode...");
        SwingUtilities.invokeLater(GameGUI::new); // 로컬 게임 GUI 실행
    }

    private void connectToServer() {
        try {
            String serverAddress = JOptionPane.showInputDialog(null, "Enter server address:", "localhost");
            if (serverAddress == null || serverAddress.isEmpty()) return;

            socket = new Socket(serverAddress, 12345);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            sendMessage("YachtClient joined the server."); // 여기까진 서버에게 전달 됐dma.
            System.out.println("Connected to server!");
            String serverMessage1 = readMessage();
            int handlernum = Integer.parseInt(serverMessage1.split(" ")[1]);
            if(handlernum==1){
                sendMessage("CREATE_ROOM");
            }
            String serverMessage2 = readMessage();
            int roomId = Integer.parseInt(serverMessage2.split(" ")[1]);
            startMultiPlay(roomId);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to connect to the server.");
        }
    }

    private void startMultiPlay(int roomId) {
        sendMessage("JOIN_ROOM " + roomId); 
        try {
            String serverMessage;
            while ((serverMessage = readMessage()) != null) {
                System.out.println("Server: " + serverMessage);
                if (serverMessage.startsWith("ROLL_DICE_RESULT")) {
                    // 주사위 결과를 GUI에 업데이트
                    String[] parts = serverMessage.split(" ");
                    int playerId = Integer.parseInt(parts[2]);
                    String diceResults = serverMessage.substring(serverMessage.indexOf("["));
                    SwingUtilities.invokeLater(() -> updateDiceInGUI(playerId, diceResults));
                } else if (serverMessage.startsWith("UPDATE_SCORE")) {
                    // 점수판을 GUI에 업데이트
                    String[] parts = serverMessage.split(" ");
                    int playerId = Integer.parseInt(parts[2]);
                    int score = Integer.parseInt(parts[3]);
                    SwingUtilities.invokeLater(() -> updateScoreInGUI(playerId, score));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // GUI에서 주사위 결과 업데이트
    private void updateDiceInGUI(int playerId, String diceResults) {
        System.out.println("Player " + playerId + " rolled dice: " + diceResults);
        // GUI에 주사위 결과 반영 로직 추가
    }

    // GUI에서 점수판 업데이트
    private void updateScoreInGUI(int playerId, int score) {
        System.out.println("Player " + playerId + " scored: " + score);
        // GUI에 점수 업데이트 로직 추가
    }



    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    public String readMessage() throws IOException {
        if (in != null) {
            return in.readLine();
        }
        return null;
    }

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


