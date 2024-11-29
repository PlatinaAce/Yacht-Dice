import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class LobbyClient {
    private JFrame frame;
    private JTextArea messageArea;
    private PrintWriter out;
    private BufferedReader in;
    private String playerRole; // P1 또는 P2 역할

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LobbyClient().createUI());
    }

    private void createUI() {
        frame = new JFrame("Yahtzee Lobby");
        frame.setSize(500, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // 제목 라벨
        JLabel titleLabel = new JLabel("Yahtzee Game Lobby", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        frame.add(titleLabel, BorderLayout.NORTH);

        // 메시지 영역
        messageArea = new JTextArea(10, 40);
        messageArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(messageArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        // 버튼 패널
        JPanel buttonPanel = new JPanel();
        JButton createRoomButton = new JButton("Create Room");
        JButton joinRoomButton = new JButton("Join Room");
        createRoomButton.addActionListener(e -> sendCreateRoomRequest());
        joinRoomButton.addActionListener(e -> sendJoinRoomRequest());
        buttonPanel.add(createRoomButton);
        buttonPanel.add(joinRoomButton);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        frame.setVisible(true);

        connectToServer();
    }

    private void connectToServer() {
        try {
            String serverAddress = JOptionPane.showInputDialog(frame, "Enter server address:", "localhost");
            if (serverAddress == null || serverAddress.isEmpty()) return;

            Socket socket = new Socket(serverAddress, 12345);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        handleServerMessage(message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            messageArea.append("Connected to server.\n");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Unable to connect to server.");
        }
    }

    private void sendCreateRoomRequest() {
        out.println("CREATE_ROOM");
    }

    private void sendJoinRoomRequest() {
        String roomId = JOptionPane.showInputDialog(frame, "Enter Room ID:");
        if (roomId != null && !roomId.trim().isEmpty()) {
            out.println("JOIN_ROOM " + roomId);
        }
    }

    private void handleServerMessage(String message) {
        messageArea.append("Server: " + message + "\n");

        if (message.startsWith("ROOM_CREATED")) {
            String roomId = message.split(" ")[1];
            JOptionPane.showMessageDialog(frame, "Room created. ID: " + roomId);
        } else if (message.startsWith("JOINED_ROOM")) {
            String roomId = message.split(" ")[1];
            JOptionPane.showMessageDialog(frame, "Joined room: " + roomId);
        } else if (message.startsWith("Game is starting")) {
            JOptionPane.showMessageDialog(frame, "Game is starting!");
            SwingUtilities.invokeLater(() -> new GameScreen(playerRole));
            frame.dispose();
        } else if (message.startsWith("ASSIGNED_ROLE")) {
            playerRole = message.split(" ")[1];
        }
    }
}
