/*
package server;

import java.io.*;
import java.net.*;
import java.util.Random;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private BufferedReader in;
    private BufferedWriter out;
    private static int player1Score = 0;
    private static int player2Score = 0;

    // Dice 클래스를 추가하여 주사위 굴리기
    public class Dice {
        private Random rand = new Random();

        // 주사위 3번 굴리기
        public int[] rollDice() {
            int[] rolls = new int[3];
            for (int i = 0; i < 3; i++) {
                rolls[i] = rand.nextInt(6) + 1;  // 1~6 사이의 숫자 생성
            }
            return rolls;
        }
    }

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        try {
            this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            this.out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            // 클라이언트와의 상호작용 시작
            out.write("Welcome to the game!\n");
            out.flush();

            // 클라이언트로부터 플레이어 명 받기 (player1 또는 player2)
            String player = in.readLine();

            // 게임 시작
            if ("player1".equals(player)) {
                handlePlayerMove("player1", in, out, null, null);
            } else if ("player2".equals(player)) {
                handlePlayerMove("player2", in, out, null, null);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 플레이어의 이동 처리
    public void handlePlayerMove(String player, BufferedReader inPlayer, BufferedWriter outPlayer,
                                 BufferedReader inOpponent, BufferedWriter outOpponent) throws IOException {
        Dice dice = new Dice();
        int[] diceRolls;
        int score = 0;
        String move;

        if ("player1".equals(player)) {
            // 플레이어 1의 다이스 굴리기
            move = inPlayer.readLine();  // 클라이언트가 'roll' 명령을 보냈다고 가정
            if ("roll".equals(move)) {
                diceRolls = dice.rollDice();  // 다이스 굴리기
                StringBuilder rollResult = new StringBuilder("You rolled: ");
                for (int roll : diceRolls) {
                    rollResult.append(roll).append(" ");  // 결과 출력 (예: "You rolled: 3 5 2 ")
                    score += roll;  // 점수 계산
                }
                player1Score += score;  // 플레이어 1의 점수에 더하기

                // 플레이어 1에게 다이스 결과 및 점수 전송
                outPlayer.write(rollResult.toString() + "\n");
                outPlayer.write("Your total score: " + player1Score + "\n");
                outPlayer.flush();
            }

            // 플레이어 2에게도 다이스 결과 전송
            String opponentRollResult = "Player 1 rolled and scored: " + score + "\n";
            opponentRollResult += "Opponent's total score: " + player2Score + "\n";
            outOpponent.write(opponentRollResult);
            outOpponent.flush();

        } else if ("player2".equals(player)) {
            // 플레이어 2의 다이스 굴리기
            move = inPlayer.readLine();  // 클라이언트가 'roll' 명령을 보냈다고 가정
            if ("roll".equals(move)) {
                diceRolls = dice.rollDice();  // 다이스 굴리기
                StringBuilder rollResult = new StringBuilder("You rolled: ");
                for (int roll : diceRolls) {
                    rollResult.append(roll).append(" ");  // 결과 출력 (예: "You rolled: 3 5 2 ")
                    score += roll;  // 점수 계산
                }
                player2Score += score;  // 플레이어 2의 점수에 더하기

                // 플레이어 2에게 다이스 결과 및 점수 전송
                outPlayer.write(rollResult.toString() + "\n");
                outPlayer.write("Your total score: " + player2Score + "\n");
                outPlayer.flush();
            }

            // 플레이어 1에게도 다이스 결과 전송
            String opponentRollResult = "Player 2 rolled and scored: " + score + "\n";
            opponentRollResult += "Opponent's total score: " + player1Score + "\n";
            outOpponent.write(opponentRollResult);
            outOpponent.flush();
        }
    }
}
*/

package server;

import java.io.*;
import java.net.*;

public class ClientHandler implements Runnable {
    private Socket socket;
    private int playerId;

    public ClientHandler(Socket socket, int playerId) {
        this.socket = socket;
        this.playerId = playerId;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Player " + playerId + ": " + message);
                GameServer.broadcast(message, playerId);
            }
        } catch (IOException e) {
            System.err.println("Player " + playerId + " disconnected.");
        }
    }
}

