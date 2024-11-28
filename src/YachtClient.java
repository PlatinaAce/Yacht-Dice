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
      //  SwingUtilities.invokeLater(GameGUI::new); // 로컬 게임 GUI 실행
        // single 만들어두신거 연결하기
    }

    private void connectToServer() { //broadcast듣기.. readmessage로.
        try {
            String serverAddress = JOptionPane.showInputDialog(null, "Enter server address:", "localhost");
            if (serverAddress == null || serverAddress.isEmpty()) return;

            socket = new Socket(serverAddress, 12345);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            sendMessage("YachtClient joined the server."); // 여기까진 서버에게 전달 됐dma.
            System.out.println("Connected to server!");
            // 여기 방 구현 이상함
            String serverMessage1 = readMessage();
            int handlernum = Integer.parseInt(serverMessage1.split(" ")[1]);
            if(handlernum==1){
                sendMessage("CREATE_ROOM");
            }
            //여긴원래코드
            String serverMessage2 = readMessage();
            int roomId = Integer.parseInt(serverMessage2.split(" ")[1]);
            startMultiPlay(roomId);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to connect to the server.");
        }
    }

    private void startMultiPlay(int roomId) {//여기가문제
        sendMessage("JOIN_ROOM "+roomId);; // 방에 참여하기
        try {
            String serverMessage;
            while ((serverMessage = readMessage()) != null) {
                System.out.println("Server: " + serverMessage); //여기가 안뜸
                if (serverMessage.contains("Game is starting")) {
                    SwingUtilities.invokeLater(() -> new GameGUI(in, out));
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

