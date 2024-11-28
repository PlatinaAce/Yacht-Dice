// 하나의 게임 방을 관리하는 클래스
// 플레이어를 추가하고 ->  addPlayer
// 메세지를 방 안의 모든 플레이어에게 전송해줌 -> broadcast

import java.util.*;

public class GameRoom {
    private int roomId;
    private List<ClientHandler> players = new ArrayList<>();
    private Map<Integer, Integer> scores = new HashMap<>(); // 각 플레이어 점수
    private int currentTurn = 0;

    public GameRoom(int roomId) {
        this.roomId = roomId;
    }

    public int getRoomID(){
        return roomId;
    }

    public synchronized int addPlayer(ClientHandler player) {
        int playerId = players.size() + 1; // 플레이어 ID 부여
        players.add(player);
        scores.put(playerId, 0); // 점수 초기화
        broadcast("Player "+playerId+" joined the room.");
        broadcast("Game is starting");
        return playerId;
    }

    public synchronized void broadcast(String message) {
        for (ClientHandler player : players) {
            player.sendMessage(message);
        }
    }

    public synchronized void rollDice(int playerId) {
        if (currentTurn + 1 != playerId) {
            broadcast("It's not Player " + playerId + "'s turn.");
            return;
        }

        int[] dice = DiceRoller.rollDice(null);
        broadcast("ROLL_DICE_RESULT Player " + playerId + " rolled " + Arrays.toString(dice));
        nextTurn();
    }

    public synchronized void updateScore(int playerId, int score) {
        scores.put(playerId, score);
        broadcast("Player " + playerId + " updated score to " + score);
        nextTurn();
    }

    public synchronized void nextTurn() {
        currentTurn = (currentTurn + 1) % players.size(); // 다음 플레이어로 턴 이동
        broadcast("NEXT_TURN " + (currentTurn + 1));
    }
}
