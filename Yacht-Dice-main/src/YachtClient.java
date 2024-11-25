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

            System.out.println("Connected to server!");
            sendMessage("YachtClient joined the server.");
            startMultiPlay();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to connect to the server.");
        }
    }

    private void startMultiPlay() {
        try {
            String serverMessage;
            while ((serverMessage = readMessage()) != null) {
                System.out.println("Server: " + serverMessage);
                if (serverMessage.contains("Game is starting")) {
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "Game is starting!"));
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
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


