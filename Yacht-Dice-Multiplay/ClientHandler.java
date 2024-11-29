// 서버는 각 클라이언트를 별도의 스레드로 관리함
//방 내에 발생한 이벤트(주사위 결과 등)은 해당 방에 연결된 모든 클라이언트에게 전송됨
//각 스레드는 독립적으로 클라이언트와 통신하므로 여러 플레이어가 동시에 요청 보낼수있음 -> 멀티스레드
//클라이언트와 서버 간 데이터를 주고받는 스레드,클라이언트가 보낸 요청을 처리하고, 서버의 응답을 클라이언트에 전송

//해당 턴의 클라이언트에만 roll_dice와 update_score명령을 허용해야함
//모든 플레이어가 동일한 주사위 결과를 실시간으로 볼수 있도록 각 클라이언트에 브로드캐스트 메시지 추가해야함

//1. turn 부여 메시지 전송
//2. rolldice 버튼과 update_score 버튼 제한 (턴이 아닌 플레이어에게-무시)
//3. broadcast 주사위 결과(roll_dice 후): 주사위 결과를 클라이언트에게 동시에 전달 (동기화)

import java.io.*;
import java.net.*;
import java.util.Arrays;

public class ClientHandler extends Thread {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private int playerId; // 현재 플레이어 ID
    private GameRoom currentRoom; // 현재 참여 중인 방

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
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
                handleClientMessage(message); // 클라이언트 메시지 처리
            }
        } catch (IOException e) {
            System.out.println("Client disconnected.");
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 클라이언트 메시지 처리
    private void handleClientMessage(String message) {
        if (message.startsWith("CREATE_ROOM")) {
            int roomId = LobbyServer.createRoom(this);
            sendMessage("ROOM_CREATED " + roomId);
            sendMessage("ASSIGNED_ROLE P1");
        } else if (message.startsWith("JOIN_ROOM")) {
            int roomId = Integer.parseInt(message.split(" ")[1]);
            GameRoom room = LobbyServer.getRoom(roomId);
            if (room != null) { 
                int playerId = room.addPlayer(this); // addPlayer 메서드가 플레이어 ID를 반환
                if (playerId > 0) { // 플레이어 ID가 유효하면
                    this.setPlayerId(playerId); // 플레이어 ID 설정
                    sendMessage("JOINED_ROOM " + room.getRoomID());
                    sendMessage("ASSIGNED_ROLE P" + playerId);
                    room.broadcast("Player " + playerId + " joined the room.");
                } else {
                    sendMessage("ERROR Room is full.");
                }
            } else {
                sendMessage("ERROR Room not found.");
            }

    }
}

    // 클라이언트에게 메시지 전송
    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    // 현재 방 설정
    public void setCurrentRoom(GameRoom room) {
        this.currentRoom = room;
    }

    // 현재 방 반환
    public GameRoom getCurrentRoom() {
        return currentRoom;
    }

    // 플레이어 ID 설정
    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    // 플레이어 ID 반환
    public int getPlayerId() {
        return playerId;
    }
}



