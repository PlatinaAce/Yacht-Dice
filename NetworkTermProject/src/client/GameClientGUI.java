
package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Random;

public class GameClientGUI extends JFrame {
    private int[] diceValues = new int[5];
    private boolean[] diceKept = new boolean[5];  // Tracks which dice are kept
    private JLabel[] diceLabels = new JLabel[5];
    private JButton rollButton;
    private JTextArea logArea;
    private PrintWriter out;
    private int rollCount = 0;  // Tracks the number of rolls

    // Scoreboard related variables
    private JLabel scoreLabel;
    private int totalScore = 0;
    private int[] categoryScores = new int[13];  // 13 categories
    private JLabel[] categoryLabels = new JLabel[13];
    private boolean[] categoryFilled = new boolean[13];  // Tracks which categories are filled

    public GameClientGUI() {
        setTitle("Yacht Game - Client");
        setSize(600, 600);  // Adjust window size
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Dice panel
        JPanel dicePanel = new JPanel();
        dicePanel.setLayout(new GridLayout(1, 5));
        for (int i = 0; i < 5; i++) {
            diceLabels[i] = new JLabel();
            diceLabels[i].setHorizontalAlignment(SwingConstants.CENTER);
            diceLabels[i].setIcon(loadDiceImage(1)); // Initial value is 1
            // Add mouse listener for keeping dice
            final int index = i;
            diceLabels[i].addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    diceKept[index] = !diceKept[index];  // Toggle keep status
                    diceLabels[index].setBorder(diceKept[index] ? BorderFactory.createLineBorder(Color.GREEN, 3) : BorderFactory.createLineBorder(Color.BLACK));
                }
            });
            dicePanel.add(diceLabels[i]);
        }

        // Scoreboard panel
        JPanel scorePanel = new JPanel();
        scorePanel.setLayout(new GridLayout(14, 2));  // 13 categories + total score
        scoreLabel = new JLabel("Total Score: 0");
        scorePanel.add(scoreLabel);
        
        // Category labels
        String[] categories = {
            "Ones", "Twos", "Threes", "Fours", "Fives", "Sixes",
            "Full House", "Four of a Kind", "Small Straight", "Large Straight", "Yacht", "Chance", "Bonus"
        };
        for (int i = 0; i < categories.length; i++) {
            categoryLabels[i] = new JLabel(categories[i] + ": 0");
            scorePanel.add(categoryLabels[i]);
        }

        // Button and log
        rollButton = new JButton("Roll Dice");
        logArea = new JTextArea(10, 20);
        logArea.setEditable(false);

        rollButton.addActionListener(this::handleRollDice);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.add(rollButton, BorderLayout.NORTH);
        bottomPanel.add(new JScrollPane(logArea), BorderLayout.CENTER);

        add(dicePanel, BorderLayout.CENTER);
        add(scorePanel, BorderLayout.WEST);  // Add score panel to the left
        add(bottomPanel, BorderLayout.SOUTH);

        connectToServer();

        setVisible(true);
    }
    
    private void connectToServer() {
        try {
            Socket socket = new Socket("localhost", 12345); // 서버와 같은 포트 번호
            out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Receive messages from the server
            new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        logArea.append("Server: " + message + "\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
            logArea.append("Server connection failed\n");
        }
    }

    

    private void handleRollDice(ActionEvent e) {
        if (rollCount >= 3) {
            logArea.append("You have already rolled 3 times!\n");
            calculateAndUpdateScore();  // Update score after 3 rolls
            return;  // Stop rolling after 3 attempts
        }

        Random random = new Random();
        int rollSum = 0;

        // Roll dice logic
        for (int i = 0; i < diceValues.length; i++) {
            if (!diceKept[i]) {  // Only roll dice that are not kept
                diceValues[i] = random.nextInt(6) + 1;
            }
            diceLabels[i].setIcon(loadDiceImage(diceValues[i]));  // Update dice image
            rollSum += diceValues[i]; // Sum up the dice values
        }

        // Send roll data to server
        if (out != null) {
            out.println("OPPONENT ROLL " + formatDiceValues());
            out.flush();
        }

        logArea.append("Rolled: " + formatDiceValues() + "\n");

        rollCount++;  // Increment roll count

        // Update score after the third roll
        if (rollCount == 3) {
            calculateAndUpdateScore();
        }
    }

    private String formatDiceValues() {
        StringBuilder sb = new StringBuilder();
        for (int value : diceValues) {
            sb.append(value).append(" ");
        }
        return sb.toString().trim();
    }

    private ImageIcon loadDiceImage(int value) {
        // Dice images should be saved as dice1.png, dice2.png, etc. in the src/resources/ directory
        String path = "/resources/dice" + value + ".png";
        return new ImageIcon(getClass().getResource(path));
    }

    // Update score method
    private void updateScore(int rollSum) {
        totalScore = 0;  // Reset the total score
        for (int score : categoryScores) {
            totalScore += score;  // Sum the category scores
        }
        scoreLabel.setText("Total Score: " + totalScore);  // Update total score on the UI
    }

    // Category score calculation method
    private void calculateCategoryScores() {
        // "Ones" category calculation (sum of all dice showing 1)
        int onesScore = 0;
        for (int value : diceValues) {
            if (value == 1) onesScore += value;
        }
        categoryScores[0] = onesScore;  // Store score for Ones category
        categoryLabels[0].setText("Ones: " + onesScore);  // Update UI

        // "Twos" category calculation (sum of all dice showing 2)
        int twosScore = 0;
        for (int value : diceValues) {
            if (value == 2) twosScore += value;
        }
        categoryScores[1] = twosScore;  // Store score for Twos category
        categoryLabels[1].setText("Twos: " + twosScore);  // Update UI for Twos

        // "Threes" category calculation (sum of all dice showing 3)
        int threesScore = 0;
        for (int value : diceValues) {
            if (value == 3) threesScore += value;
        }
        categoryScores[2] = threesScore;  // Store score for Threes category
        categoryLabels[2].setText("Threes: " + threesScore);  // Update UI for Threes

        // "Fours" category calculation (sum of all dice showing 4)
        int foursScore = 0;
        for (int value : diceValues) {
            if (value == 4) foursScore += value;
        }
        categoryScores[3] = foursScore;  // Store score for Fours category
        categoryLabels[3].setText("Fours: " + foursScore);  // Update UI for Fours

        // "Fives" category calculation (sum of all dice showing 5)
        int fivesScore = 0;
        for (int value : diceValues) {
            if (value == 5) fivesScore += value;
        }
        categoryScores[4] = fivesScore;  // Store score for Fives category
        categoryLabels[4].setText("Fives: " + fivesScore);  // Update UI for Fives

        // "Sixes" category calculation (sum of all dice showing 6)
        int sixesScore = 0;
        for (int value : diceValues) {
            if (value == 6) sixesScore += value;
        }
        categoryScores[5] = sixesScore;  // Store score for Sixes category
        categoryLabels[5].setText("Sixes: " + sixesScore);  // Update UI for Sixes

        // "Full House" (3 of a kind and a pair) - Score 25 if valid
        int fullHouseScore = 0;
        if (isFullHouse()) {
            fullHouseScore = 25;
        }
        categoryScores[6] = fullHouseScore;  // Store score for Full House category
        categoryLabels[6].setText("Full House: " + fullHouseScore);  // Update UI for Full House

        // "Four of a Kind" (at least 4 dice showing the same face)
        int fourOfAKindScore = 0;
        if (isFourOfAKind()) {
            fourOfAKindScore = sumAllDice();
        }
        categoryScores[7] = fourOfAKindScore;  // Store score for Four of a Kind category
        categoryLabels[7].setText("Four of a Kind: " + fourOfAKindScore);  // Update UI for Four of a Kind

        // "Small Straight" (4 consecutive numbers) - Score 30 if valid
        int smallStraightScore = 0;
        if (isSmallStraight()) {
            smallStraightScore = 30;
        }
        categoryScores[8] = smallStraightScore;  // Store score for Small Straight category
        categoryLabels[8].setText("Small Straight: " + smallStraightScore);  // Update UI for Small Straight

        // "Large Straight" (5 consecutive numbers) - Score 40 if valid
        int largeStraightScore = 0;
        if (isLargeStraight()) {
            largeStraightScore = 40;
        }
        categoryScores[9] = largeStraightScore;  // Store score for Large Straight category
        categoryLabels[9].setText("Large Straight: " + largeStraightScore);  // Update UI for Large Straight

        // "Yacht" (all five dice are the same number) - Score 50 if valid
        int yachtScore = 0;
        if (isYacht()) {
            yachtScore = 50;
        }
        categoryScores[10] = yachtScore;  // Store score for Yacht category
        categoryLabels[10].setText("Yacht: " + yachtScore);  // Update UI for Yacht

        // "Chance" (sum of all dice)
        int chanceScore = sumAllDice();
        categoryScores[11] = chanceScore;  // Store score for Chance category
        categoryLabels[11].setText("Chance: " + chanceScore);  // Update UI for Chance

        // "Bonus" (if the total of the Ones-Sixes categories is greater than 63)
        int bonusScore = 0;
        if (sumOfOnesToSixes() > 63) {
            bonusScore = 35;
        }
        categoryScores[12] = bonusScore;  // Store score for Bonus category
        categoryLabels[12].setText("Bonus: " + bonusScore);  // Update UI for Bonus
    }

 	// Full House: 3 of one number and 2 of another
    private boolean isFullHouse() {
        int[] counts = new int[6];
        for (int value : diceValues) {
            counts[value - 1]++;
        }
        boolean hasThreeOfOne = false, hasTwoOfOne = false;
        for (int count : counts) {
            if (count == 3) hasThreeOfOne = true;
            if (count == 2) hasTwoOfOne = true;
        }
        return hasThreeOfOne && hasTwoOfOne;
    }

    // Four of a Kind: At least 4 dice showing the same number
    private boolean isFourOfAKind() {
        int[] counts = new int[6];
        for (int value : diceValues) {
            counts[value - 1]++;
        }
        for (int count : counts) {
            if (count >= 4) return true;
        }
        return false;
    }

    // Small Straight: 4 consecutive numbers
    private boolean isSmallStraight() {
        Arrays.sort(diceValues);
        for (int i = 0; i < 2; i++) {
            if (diceValues[i] + 1 == diceValues[i + 1] &&
                diceValues[i + 1] + 1 == diceValues[i + 2] &&
                diceValues[i + 2] + 1 == diceValues[i + 3]) {
                return true;
            }
        }
        return false;
    }

    // Large Straight: 5 consecutive numbers
    private boolean isLargeStraight() {
        Arrays.sort(diceValues);
        for (int i = 0; i < 1; i++) {
            if (diceValues[i] + 1 == diceValues[i + 1] &&
                diceValues[i + 1] + 1 == diceValues[i + 2] &&
                diceValues[i + 2] + 1 == diceValues[i + 3] &&
                diceValues[i + 3] + 1 == diceValues[i + 4]) {
                return true;
            }
        }
        return false;
    }

    // Yacht: All five dice show the same number
    private boolean isYacht() {
        return diceValues[0] == diceValues[1] && diceValues[0] == diceValues[2] &&
               diceValues[0] == diceValues[3] && diceValues[0] == diceValues[4];
    }

    // Sum of all dice (used for categories like "Chance")
    private int sumAllDice() {
        int sum = 0;
        for (int value : diceValues) {
            sum += value;
        }
        return sum;
    }

    // Sum of categories "Ones" to "Sixes" (for calculating the Bonus)
    private int sumOfOnesToSixes() {
        int sum = 0;
        for (int i = 0; i < 6; i++) {
            sum += categoryScores[i];  // Sum the scores from Ones to Sixes categories
        }
        return sum;
    }


    private void calculateAndUpdateScore() {
        // Calculate and update all categories after 3 rolls
        calculateCategoryScores();

        // Update the total score after filling in the categories
        totalScore = 0;  // Reset total score
        for (int score : categoryScores) {
            totalScore += score;  // Add scores for each category
        }

        // Update the displayed total score
        scoreLabel.setText("Total Score: " + totalScore);

        // Log the updated score
        logArea.append("Score has been updated.\n");
    }

    public static void main(String[] args) {
        new GameClientGUI();
    }
}


