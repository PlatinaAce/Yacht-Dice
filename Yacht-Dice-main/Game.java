import java.util.Arrays;

class Game {
    private Dice[] dice;
    private int rollsLeft;
    private int[] scoreCard;

    public Game() {
        dice = new Dice[5];
        for (int i = 0; i < 5; i++) {
            dice[i] = new Dice();
        }
        rollsLeft = 3;
        scoreCard = new int[12]; // 12 categories
    }

    public Dice[] getDice() {
        return dice;
    }

    public int[] getScoreCard() {
        return scoreCard;
    }

    public int getRollsLeft() {
        return rollsLeft;
    }

    public void rollDice(boolean[] keepFlags) {
        for (int i = 0; i < 5; i++) {
            if (!keepFlags[i]) {
                dice[i].roll();
            }
        }
        rollsLeft--;
    }

    public void calculateScores() {
        int[] diceValues = new int[6];
        for (Dice die : dice) {
            diceValues[die.getValue() - 1]++;
        }

        // Ones, Twos, ..., Sixes
        for (int i = 0; i < 6; i++) {
            scoreCard[i] = diceValues[i] * (i + 1);
        }

        // Choice
        scoreCard[6] = Arrays.stream(dice).mapToInt(Dice::getValue).sum();

        // Four of a Kind
        boolean hasFour = Arrays.stream(diceValues).anyMatch(count -> count >= 4);
        scoreCard[7] = hasFour ? Arrays.stream(dice).mapToInt(Dice::getValue).sum() : 0;

        // Full House
        boolean hasThree = false, hasTwo = false;
        for (int count : diceValues) {
            if (count == 3) hasThree = true;
            if (count == 2) hasTwo = true;
        }
        scoreCard[8] = (hasThree && hasTwo) ? Arrays.stream(dice).mapToInt(Dice::getValue).sum() : 0;

        // Small Straight (1-2-3-4 or 2-3-4-5 or 3-4-5-6)
        scoreCard[9] = (containsSequence(diceValues, 4)) ? 30 : 0;

        // Large Straight (1-2-3-4-5 or 2-3-4-5-6)
        scoreCard[10] = (containsSequence(diceValues, 5)) ? 40 : 0;

        // Yacht
        boolean hasYacht = Arrays.stream(diceValues).anyMatch(count -> count == 5);
        scoreCard[11] = hasYacht ? 50 : 0;
    }

    private boolean containsSequence(int[] diceValues, int length) {
        int consecutive = 0;
        for (int count : diceValues) {
            if (count > 0) {
                consecutive++;
                if (consecutive >= length) return true;
            } else {
                consecutive = 0;
            }
        }
        return false;
    }

    public void resetTurn() {
        rollsLeft = 3;
    }
}