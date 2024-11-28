package game;

import java.io.*;
import java.util.*;

public class ScoreSheet {
    private Map<String, Integer> scores = new HashMap<>();

    public void recordScore(int[] dice, BufferedReader in, PrintWriter out) throws IOException {
        out.println("Choose the category to record the score: Ones, Twos, ... Yacht");
        String category = in.readLine();
        int score = calculateScore(dice, category); // 수정된 메서드 호출
        scores.put(category, score);
        out.println(score + " recorded on " + category);
    }

    // calculateScore 메서드 수정
    private int calculateScore(int[] dice, String category) {
        switch (category) {
            case "Ones":
                return calculateSum(dice, 1);
            case "Twos":
                return calculateSum(dice, 2);
            case "Threes":
                return calculateSum(dice, 3);
            case "Fours":
                return calculateSum(dice, 4);
            case "Fives":
                return calculateSum(dice, 5);
            case "Sixes":
                return calculateSum(dice, 6);
            case "Yacht":
                return isYacht(dice) ? 50 : 0;
            default:
                return 0;
        }
    }

    // 특정 숫자의 합 계산
    private int calculateSum(int[] dice, int number) {
        int sum = 0;
        for (int die : dice) {
            if (die == number) sum += die;
        }
        return sum;
    }

    // Yacht 조건 확인 (5개의 주사위가 같은 숫자인지)
    private boolean isYacht(int[] dice) {
        int first = dice[0];
        for (int die : dice) {
            if (die != first) return false;
        }
        return true;
    }
}
