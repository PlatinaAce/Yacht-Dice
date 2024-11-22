import java.util.Random;

public class DiceRoller {
    private static final Random random = new Random();

    public static int[] rollDice(int[] keep) {
        int[] dice = new int[5];
        for (int i = 0; i < 5; i++) {
            if (keep == null || keep[i] == 0) { // 고정되지 않은 주사위만 굴림
                dice[i] = random.nextInt(6) + 1;
            } else {
                dice[i] = keep[i];
            }
        }
        return dice;
    }
}
