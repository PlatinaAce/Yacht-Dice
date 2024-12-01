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
    }

    public int getRoomID(){
        return roomId;
    }

   /* public synchronized int addPlayer(ClientHandler player) {
        int playerId = players.size() + 1; // 플레이어 ID 부여
        players.add(player);
        scores.put(playerId, 0); // 점수 초기화
        broadcast("Player "+playerId+" joined the room.");
        broadcast("Game is starting");  // 이게 clienthandler에 가고,,
        return playerId;
    }*/

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

    /*public synchronized void nextTurn() {
        currentTurn = (currentTurn + 1) % 2; // 다음 플레이어로 턴 이동
        broadcast("NEXT_TURN " + (currentTurn + 1));
    }*/
    // 플레이어에게 메시지를 전송하는 함수
    public synchronized void nextTurn() {
        currentTurn = (currentTurn + 1) % 2; // 턴을 P1 <-> P2로 전환
        // 플레이어에게 턴을 알려줌
        if (currentTurn == 0) {
            clientHandler1.sendMessage("NEXT_TURN 1"); // P1의 차례
            clientHandler2.sendMessage("NEXT_TURN 2"); // P2는 기다림
            mgui1.myGameUpdate();
            mgui1.updateButtonState();
            mgui2.myGameUpdate();
            mgui2.updateButtonState();
        } else {
            clientHandler1.sendMessage("NEXT_TURN 2"); // P1은 기다림
            clientHandler2.sendMessage("NEXT_TURN 1"); // P2의 차례
            mgui1.myGameUpdate();
            mgui1.updateButtonState();
            mgui2.myGameUpdate();
            mgui2.updateButtonState();
        }
    }

    public ClientHandler getClientHandler1(){
        return this.clientHandler1;
    }
    public ClientHandler getClientHandler2(){
        return this.clientHandler2;
    }
    public void setClientHandler1(ClientHandler clientHandler){
        this.clientHandler1 = clientHandler;
    }
    public void setClientHandler2(ClientHandler clientHandler){
        this.clientHandler2 = clientHandler;
    }

    /*public void updateTurn(int turn) {  //결국 여기가문제
        if (turn == 0) {
            //mgui1.updateTurnIndicator();
            mgui1.updateButtonState();  // P1의 주사위 버튼 활성화
           // mgui2.updateTurnStatus("Waiting for Player 1...");
            mgui2.updateButtonState(); // P2의 주사위 버튼 비활성화
        } else if (turn == 1) {
            //mgui1.updateTurnStatus("Waiting for Player 2...");
            mgui1.updateButtonState(); // P1의 주사위 버튼 비활성화
            //mgui2.updateTurnStatus("It's your turn, Player 2!");
            mgui2.updateButtonState();  // P2의 주사위 버튼 활성화
        }
    }*/
    public void updateTurn(){
        mgui1.updateButtonState();
        mgui2.updateButtonState();

    }
}