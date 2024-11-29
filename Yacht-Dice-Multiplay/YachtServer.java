//게임 서버의 메인 클래스
//클라이언트 연결을 관리하고, 게임 방을 생성 및 관리함
//서버가 여러 방을 관리하기 위해 사용하는 데이터 구조입니다.

import java.io.*;
import java.net.*;
import java.util.*;

public class YachtServer {
    private static final int PORT = 12345;
    private static ServerSocket serverSocket;
    private static Map<Integer, GameRoom> rooms = new HashMap<>();
    private static int nextRoomId = 1;

    public static void main(String[] args) {
        System.out.println("Yacht Server is starting...");
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Waiting for clients...");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clientHandler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized int createRoom(ClientHandler creator) {
        int roomId = nextRoomId++;
        GameRoom newRoom = new GameRoom(roomId);
        rooms.put(roomId, newRoom);
        newRoom.addPlayer(creator); // 방 생성자가 첫 번째 플레이어
        return roomId;
    }

    public static synchronized GameRoom getRoom(int roomId) {
        return rooms.get(roomId);
    }

    public static synchronized void shutdownServer() {
        try {
            System.out.println("Shutting down server...");
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private GameRoom currentRoom;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                String message;
                while ((message = in.readLine()) != null) {
                    handleClientMessage(message);
                }
            } catch (IOException e) {
                System.out.println("Client disconnected.");
            } finally {
                try {
                    if (currentRoom != null) {
                        currentRoom.removePlayer(this); // 클라이언트가 방에서 나갈 때 처리
                    }
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void handleClientMessage(String message) {
            try {
                if (message.startsWith("CREATE_ROOM")) {
                    int roomId = createRoom(this);
                    sendMessage("ROOM_CREATED " + roomId);
                    sendMessage("ASSIGNED_ROLE P1");
                } else if (message.startsWith("JOIN_ROOM")) {
                    int roomId = Integer.parseInt(message.split(" ")[1]);
                    GameRoom room = getRoom(roomId);
                    if (room != null && room.addPlayer(this)) {
                        currentRoom = room; // 현재 방 설정
                        sendMessage("JOINED_ROOM " + roomId);
                        sendMessage("ASSIGNED_ROLE P2");
                        room.broadcast("Game is starting");
                        room.notifyTurnChange(); // 게임 시작 후 턴 알림
                    } else {
                        sendMessage("ERROR Room is full or does not exist.");
                    }
                } else if (message.startsWith("ROLL_DICE")) {
                    if (currentRoom != null) {
                        currentRoom.handleRollDice(this);
                    }
                } else if (message.startsWith("UPDATE_SCORE")) {
                    if (currentRoom != null) {
                        currentRoom.handleUpdateScore(this, message);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void sendMessage(String message) {
            out.println(message);
        }
    }

    static class GameRoom {
        private int roomId;
        private List<ClientHandler> players = new ArrayList<>();
        private int currentTurn = 0;

        public GameRoom(int roomId) {
            this.roomId = roomId;
        }

        public synchronized boolean addPlayer(ClientHandler player) {
            if (players.size() < 2) {
                players.add(player);
                return true;
            }
            return false; // 방이 가득 찼음
        }

        public synchronized void removePlayer(ClientHandler player) {
            players.remove(player);
            broadcast("Player left the room.");
        }

        public synchronized void broadcast(String message) {
            for (ClientHandler player : players) {
                player.sendMessage(message);
            }
        }

        public synchronized void notifyTurnChange() {
            for (int i = 0; i < players.size(); i++) {
                if (i == currentTurn) {
                    players.get(i).sendMessage("TURN_START");
                } else {
                    players.get(i).sendMessage("TURN_WAIT");
                }
            }
        }

        public synchronized void handleRollDice(ClientHandler player) {
            if (players.indexOf(player) != currentTurn) {
                player.sendMessage("ERROR Not your turn.");
                return;
            }

            int[] diceResults = DiceRoller.rollDice(null);
            broadcast("DICE_UPDATE " + Arrays.toString(diceResults));
            advanceTurn();
        }

        public synchronized void handleUpdateScore(ClientHandler player, String message) {
            if (players.indexOf(player) != currentTurn) {
                player.sendMessage("ERROR Not your turn.");
                return;
            }

            String[] parts = message.split(" ");
            int score = Integer.parseInt(parts[1]);
            broadcast("SCORE_UPDATE Player " + (currentTurn + 1) + ": " + score);
            advanceTurn();
        }

        private synchronized void advanceTurn() {
            currentTurn = (currentTurn + 1) % players.size();
            notifyTurnChange();
        }
    }
}



