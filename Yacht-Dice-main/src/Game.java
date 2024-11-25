import java.util.Arrays;

class Game {
    private Dice[] dice; // 주사위 객체 배열
    private int rollsLeft; // 남은 주사위 굴리기 횟수
    private int[][] scoreCards; // 두 플레이어의 점수 카드 (멀티플레이어 지원)
    private boolean[][] lockedScores; // 점수 잠금 상태
    private int currentPlayer; // 현재 플레이어 (0: P1, 1: P2)
    private boolean scoreSubmitted; // 점수 제출 여부 (턴당 한 번만 가능)

    public Game() {
        dice = new Dice[5];
        for (int i = 0; i < 5; i++) {
            dice[i] = new Dice();
        }
        rollsLeft = 3;

        scoreCards = new int[2][15]; // 두 플레이어 점수 카드
        lockedScores = new boolean[2][15]; // 각 플레이어의 점수 잠금 상태

        Arrays.fill(scoreCards[0], 0); // P1 점수 초기화
        Arrays.fill(scoreCards[1], 0); // P2 점수 초기화
        currentPlayer = 0; // P1부터 시작
        scoreSubmitted = false;
    }

    public Dice[] getDice() {
        return dice;
    }

    public int[] getCurrentScoreCard() {
        return scoreCards[currentPlayer];
    }

    public boolean[] getCurrentLockedScores() {
        return lockedScores[currentPlayer];
    }

    public int getRollsLeft() {
        return rollsLeft;
    }

    public int getCurrentPlayer() {
        return currentPlayer + 1; // 1-based index 반환
    }

    public void rollDice(boolean[] keepFlags) {
        if (rollsLeft <= 0) {
            throw new IllegalStateException("No rolls left.");
        }

        for (int i = 0; i < 5; i++) {
            if (!keepFlags[i]) {
                dice[i].roll(); // 주사위 굴리기
            }
        }
        rollsLeft--;
        scoreSubmitted = false; // 주사위를 굴렸으므로 점수 제출 가능
    }

    public void calculateScores() {
        int[] counts = new int[6]; // 주사위 값 빈도수
        int sum = 0;

        for (Dice die : dice) {
            int value = die.getValue();
            counts[value - 1]++;
            sum += value;
        }

        int[] scoreCard = getCurrentScoreCard();

        // Aces ~ Sixes 점수 계산
        for (int i = 0; i < 6; i++) {
            scoreCard[i] = counts[i] * (i + 1);
        }

        // Choice 점수 계산
        scoreCard[8] = sum;

        // 4 of a Kind 점수 계산
        boolean hasFour = Arrays.stream(counts).anyMatch(count -> count >= 4);
        scoreCard[9] = hasFour ? sum : 0;

        // Full House 점수 계산
        boolean hasThree = Arrays.stream(counts).anyMatch(count -> count == 3);
        boolean hasTwo = Arrays.stream(counts).anyMatch(count -> count == 2);
        scoreCard[10] = (hasThree && hasTwo) ? sum : 0;

        // Small Straight 점수 계산
        scoreCard[11] = containsSequence(counts, 4) ? 15 : 0;

        // Large Straight 점수 계산
        scoreCard[12] = containsSequence(counts, 5) ? 30 : 0;

        // Yacht 점수 계산
        boolean hasYacht = Arrays.stream(counts).anyMatch(count -> count == 5);
        scoreCard[13] = hasYacht ? 50 : 0;
    }

    public void submitScore(int category) {
        if (lockedScores[currentPlayer][category]) {
            throw new IllegalStateException("This category is already locked.");
        }

        int[] scoreCard = getCurrentScoreCard();
        lockedScores[currentPlayer][category] = true;

        // 기록된 점수에 제출된 점수 반영
        scoreCards[currentPlayer][category] = scoreCard[category];

        // 점수 제출 후 Subtotal, Total 업데이트
        calculateTotals();

        scoreSubmitted = true; // 점수 제출 상태 업데이트
    }

    public void calculateTotals() {
        int[] scoreCard = getCurrentScoreCard();
        boolean[] locked = getCurrentLockedScores();

        // Subtotal 계산
        scoreCard[6] = 0;
        for (int i = 0; i < 6; i++) {
            if (locked[i]) {
                scoreCard[6] += scoreCard[i];
            }
        }

        // Bonus 계산
        scoreCard[7] = (scoreCard[6] >= 63) ? 35 : 0;

        // Total 계산
        scoreCard[14] = scoreCard[6] + scoreCard[7];
        for (int i = 8; i < 14; i++) {
            if (locked[i]) {
                scoreCard[14] += scoreCard[i];
            }
        }
    }

    private boolean containsSequence(int[] counts, int length) {
        int consecutive = 0;
        for (int count : counts) {
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
        rollsLeft = 3; // 주사위 횟수 초기화
        currentPlayer = (currentPlayer + 1) % 2; // 다음 플레이어로 변경
    }

    public boolean isScoreSubmitted() {
        return scoreSubmitted;
    }
}



