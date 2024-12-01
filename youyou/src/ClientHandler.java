// 서버는 각 클라이언트를 별도의 스레드로 관리함
//방 내에 발생한 이벤트(주사위 결과 등)은 해당 방에 연결된 모든 클라이언트에게 전송됨
//각 스레드는 독립적으로 클라이언트와 통신하므로 여러 플레이어가 동시에 요청 보낼수있음 -> 멀티스레드
//클라이언트와 서버 간 데이터를 주고받는 스레드,클라이언트가 보낸 요청을 처리하고, 서버의 응답을 클라이언트에 전송


import java.io.*;
import java.net.*;

public class ClientHandler extends Thread {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private int playerId;
    private int chCount=0;
    private GameRoom gameRoom;
    private MultiGameGUI mgui;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
        chCount++;
        try {
            this.out = new PrintWriter(clientSocket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Received: " + message); // 여기까지 전달됐음. Received: YachtClient joined the server.
                out.println("Handler_num: " + chCount);
                if(message.equals("NEXT_TURN")){
                    String[] parts = message.split(" ");
                    int turn = Integer.parseInt(parts[1]);
                    mgui.updateButtonState();
                    //gameRoom.updateTurn( ); //그래야 여기가 되는데.. gameRoom 이 전달안받아짐-->됨! 아 이게 안되네 ㅋㅋㅋㅋ여기네
                } else if (message.equals("CREATE_ROOM")) { // 여기서 각자만들면 안됨..
                    //int roomId = YachtServer.createRoom();
                   // out.println("ROOM_CREATED " + roomId);
                } else if (message.startsWith("JOIN_ROOM")) {
                    int roomId = Integer.parseInt(message.split(" ")[1]);
                    GameRoom room = YachtServer.getRoom(roomId);
                    if (room != null) {
                       // playerId = room.addPlayer(this); // 플레이어 ID 할당
                        out.println("JOINED_ROOM " + roomId);
                        room.broadcast("Player " + playerId + " joined the room.");
                    } else {
                        out.println("ERROR Room not found");
                    }
                } else if (message.startsWith("ROLL_DICE")) {
                    int roomId = Integer.parseInt(message.split(" ")[1]);
                    GameRoom room = YachtServer.getRoom(roomId);
                    if (room != null) {
                        room.rollDice(playerId);
                    }
                } else if (message.startsWith("UPDATE_SCORE")) {
                    String[] parts = message.split(" ");
                    int roomId = Integer.parseInt(parts[1]);
                    int score = Integer.parseInt(parts[2]);
                    GameRoom room = YachtServer.getRoom(roomId);
                    if (room != null) {
                        room.updateScore(playerId, score);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Client disconnected: " + clientSocket.getInetAddress());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    public void setGameRoom(GameRoom gameRoom){ // 이게 잘 셋이 안되나바 ㅇㅇ여기임
        this.gameRoom = gameRoom;
    }
    public void setGUI(MultiGameGUI mgui){
        this.mgui = mgui;
    }
}