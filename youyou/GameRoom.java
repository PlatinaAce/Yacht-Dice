// 하나의 게임 방을 관리하는 클래스
// 플레이어를 추가하고 ->  addPlayer
// 메세지를 방 안의 모든 플레이어에게 전송해줌 -> broadcast

import java.util.*;

public class GameRoom {
    private int roomId;
    private List<ClientHandler> players = new ArrayList<>();
    private ClientHandler clientHandler1;
    private ClientHandler clientHandler2;
    private MultiGameGUI mgui1;
    private MultiGameGUI mgui2;
    private Map<Integer, Integer> scores = new HashMap<>(); // 각 플레이어 점수
    private int currentTurn = 0;
    public int counthihi=0;

    public GameRoom(ClientHandler clientHandler1, ClientHandler clientHandler2) {
        this.clientHandler1 = clientHandler1;
        this.clientHandler2 = clientHandler2;
        players.add(clientHandler1); // 첫 번째 플레이어 추가
        players.add(clientHandler2); // 두 번째 플레이어 추가
        clientHandler1.setGameRoom(this);
        clientHandler2.setGameRoom(this);

        // 클라이언트에 맞게 GUI 생성
        this.mgui1 = new MultiGameGUI(this, clientHandler1);
        counthihi=1;
        clientHandler1.setGUI(mgui1);
        this.mgui2 = new MultiGameGUI(this, clientHandler2);
        clientHandler1.setGUI(mgui2);
        broadcast("Game Start!");
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
        currentTurn = (currentTurn + 1) % 2; // 턴을 P1 <-> P2로 전환
        mgui1.myGameTurnUpdate();
        mgui1.updateButtonState();
        mgui2.myGameTurnUpdate();
        mgui2.updateButtonState();

        // 플레이어에게 턴을 알려줌
        if (currentTurn == 0) {
            clientHandler1.sendMessage("NEXT_TURN 1"); // P1의 차례
            clientHandler2.sendMessage("NEXT_TURN 2"); // P2는 기다림
        } else {
            clientHandler1.sendMessage("NEXT_TURN 2"); // P1은 기다림
            clientHandler2.sendMessage("NEXT_TURN 1"); // P2의 차례
        }
    }
    public synchronized void scoreUpdate(){
        mgui1.myGameScoreUpdate();
        mgui2.myGameScoreUpdate();
    }
}