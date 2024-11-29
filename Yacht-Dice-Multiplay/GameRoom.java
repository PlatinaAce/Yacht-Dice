// 하나의 게임 방을 관리하는 클래스
// 플레이어를 추가하고 ->  addPlayer
// 메세지를 방 안의 모든 플레이어에게 전송해줌 -> broadcast


//1. 턴관리 
//2. roll_dice 결과 브로드캐스트

import java.util.*;
import java.io.*;

public class GameRoom {
    private int roomId;
    private List<ClientHandler> players = new ArrayList<>();
    private Map<Integer, Integer> scores = new HashMap<>(); // 각 플레이어 점수
    private int currentTurn = 0; // 현재 턴 플레이어 ID (0부터 시작)

    public GameRoom(int roomId) {
        this.roomId = roomId;
    }

    // 방 ID 반환
    public int getRoomID() {
        return roomId;
    }

    // 플레이어 추가 및 ID 반환
    public synchronized int addPlayer(ClientHandler player) {
        int playerId = players.size() + 1; // 플레이어 ID는 1부터 시작
        players.add(player);
        scores.put(playerId, 0); // 점수 초기화
        broadcast("Player " + playerId + " joined the room.");
        if (players.size() == 2) { // 플레이어가 두 명인 경우 게임 시작
            notifyTurnChange();
        }
        return playerId;
    }

    // 모든 클라이언트에게 메시지 브로드캐스트
    public synchronized void broadcast(String message) {
        for (ClientHandler player : players) {
            player.sendMessage(message);
        }
    }

    // 턴 시작 및 변경 알림
    public synchronized void notifyTurnChange() {
        for (int i = 0; i < players.size(); i++) {
            if (i == currentTurn) {
                players.get(i).sendMessage("TURN_START"); // 현재 턴인 플레이어
            } else {
                players.get(i).sendMessage("TURN_WAIT"); // 대기 중인 플레이어
            }
        }
    }

    // 주사위 굴리기 처리
    public synchronized void rollDice(int playerId) {
        if (currentTurn + 1 != playerId) {
            players.get(playerId - 1).sendMessage("ERROR_NOT_YOUR_TURN");
            return;
        }

        // 주사위 결과 생성 및 브로드캐스트
        int[] diceResults = DiceRoller.rollDice(null); // DiceRoller에서 주사위 결과 생성
        broadcast("DICE_UPDATE Player " + playerId + ": " + Arrays.toString(diceResults));

        // 턴 변경
        nextTurn();
    }

    // 점수 업데이트 처리
    public synchronized void updateScore(int playerId, int score) {
        if (currentTurn + 1 != playerId) {
            players.get(playerId - 1).sendMessage("ERROR_NOT_YOUR_TURN");
            return;
        }

        // 점수 업데이트 및 브로드캐스트
        scores.put(playerId, score);
        broadcast("SCORE_UPDATE Player " + playerId + ": " + score);

        // 턴 변경
        nextTurn();
    }

    // 턴 변경
    public synchronized void nextTurn() {
        currentTurn = (currentTurn + 1) % players.size(); // 다음 플레이어로 턴 이동
        notifyTurnChange(); // 모든 클라이언트에게 턴 변경 알림
    }
}

