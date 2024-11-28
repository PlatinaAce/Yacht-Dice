import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;

public class LobbyClient {
    private static JTextArea messageArea;
    private static PrintWriter out;

    public static void main(String[] args) {
        // 기본 프레임 설정
        JFrame frame = new JFrame("Yahtzee Lobby");
        frame.setSize(500, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // 상단 영역 (제목)
        JLabel titleLabel = new JLabel("Yahtzee Game Lobby", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setOpaque(true);
        titleLabel.setBackground(new Color(60, 63, 65)); // 다크 테마 배경색
        titleLabel.setForeground(Color.WHITE); // 텍스트 색상
        titleLabel.setPreferredSize(new Dimension(500, 50));
        frame.add(titleLabel, BorderLayout.NORTH);

        // 중앙 영역 (유저 입력과 메시지)
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        frame.add(centerPanel, BorderLayout.CENTER);

        // 유저네임 입력 영역
        JPanel usernamePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        usernamePanel.setOpaque(false);
        JLabel usernameLabel = new JLabel("Enter Username:");
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        JTextField usernameField = new JTextField(20);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 14));
        usernamePanel.add(usernameLabel);
        usernamePanel.add(usernameField);
        centerPanel.add(usernamePanel);

        // 메시지 표시 영역
        messageArea = new JTextArea(8, 40);
        messageArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        messageArea.setEditable(false);
        messageArea.setBackground(new Color(230, 230, 230));
        messageArea.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        JScrollPane messageScrollPane = new JScrollPane(messageArea);
        centerPanel.add(messageScrollPane);

        // 하단 영역 (버튼)
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JButton connectButton = new JButton("Connect");
        connectButton.setFont(new Font("Arial", Font.BOLD, 16));
        connectButton.setBackground(new Color(0, 123, 255));
        connectButton.setForeground(Color.WHITE);
        connectButton.setFocusPainted(false);
        buttonPanel.add(connectButton);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        frame.setVisible(true);

        // 버튼 클릭 시 서버 연결 및 유저네임 전송
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText().trim();
                if (username.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Please enter a username.");
                    return;
                }
                connectToServer(username);
            }
        });
    }

    private static void connectToServer(String username) {
        try {
            Socket socket = new Socket("localhost", 12345); // 서버 연결 (로컬 호스트)
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // 유저네임 서버로 전송
            out.println(username);

            // 메시지 수신 스레드
            new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        messageArea.append(message + "\n");
                        if (message.contains("Game is starting")) {
                            JOptionPane.showMessageDialog(null, "The game is starting!");
                            break;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            messageArea.append("Connected to server. Waiting for another player...\n");
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to connect to the server.");
        }
    }
}