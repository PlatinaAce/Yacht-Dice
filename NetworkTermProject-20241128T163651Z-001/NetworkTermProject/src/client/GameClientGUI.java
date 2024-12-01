/*
package client;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
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
    private JLabel totalScoreLabel;
    private JPanel scorePanel;
    private JCheckBox[] categoryCheckBoxes = new JCheckBox[13];  // 각 카테고리에 대한 체크박스
    private int currentPlayer = 1;  // 현재 플레이어 (1번 플레이어부터 시작)
    private final int totalPlayers = 2;  // 전체 플레이어 수 (예: 2명)
    private JPanel categoryPanel;  // 카테고리 선택 UI를 포함하는 패널
    private int[] categoryScores = new int[13];


    // JTable for scoreboard
    private JTable scoreTable;
    private DefaultTableModel tableModel;
    private JButton saveButton;  // 점수를 저장하는 버튼

    public GameClientGUI() {
        setTitle("Yacht Game - Client");
        setSize(1000, 600);  // Adjust window size
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        setupCategorySelection();  // 카테고리 선택 UI 설정
        setupSaveButton();         // 저장 버튼 설정

        // Dice panel
        JPanel dicePanel = new JPanel();
        dicePanel.setLayout(new GridLayout(1, 5));
        for (int i = 0; i < 5; i++) {
            diceLabels[i] = new JLabel();
            diceLabels[i].setHorizontalAlignment(SwingConstants.CENTER);
            diceLabels[i].setIcon(loadDiceImage(1)); // Initial value is 1
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

        // Scoreboard table
        setupScoreTable();

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
        add(new JScrollPane(scoreTable), BorderLayout.EAST);  // Add score table to the right
        add(bottomPanel, BorderLayout.SOUTH);


        connectToServer();

        setVisible(true);
    }
    
    private void handleScoreSelection() {
        for (int i = 0; i < 13; i++) {
            if (categoryCheckBoxes[i].isSelected()) {
                int score = calculateCategoryScore(i);  // 각 카테고리별 점수 계산
                tableModel.setValueAt(String.valueOf(score), i, 1);  // 테이블에 점수 반영
                categoryCheckBoxes[i].setEnabled(false);  // 선택된 카테고리는 더 이상 선택할 수 없게 함

                // 점수 저장 완료 메시지 출력
                logArea.append("You have selected category " + categoryCheckBoxes[i].getText() + " with score: " + score + "\n");
                sendMessageToOpponent("Player " + currentPlayer + " has saved their score for " + categoryCheckBoxes[i].getText() + " with " + score + " points.");
            }
        }
        // 선택이 끝난 후, 턴을 다른 플레이어에게 넘깁니다.
        switchToNextPlayer();
    }
    private void sendMessageToOpponent(String message) {
        if (out != null) {
            out.println(message);  // 상대방에게 메시지 전송
            out.flush();
        }
    }
    private void switchToNextPlayer() {
        currentPlayer = (currentPlayer % totalPlayers) + 1;  // 현재 플레이어를 순환
        logArea.append("Player " + currentPlayer + "'s turn.\n");  // 턴 변경 로그 출력
        resetForNextTurn();
    }

    private void resetForNextTurn() {
        rollCount = 0;  // 굴린 횟수 초기화
        Arrays.fill(diceKept, false);  // 모든 주사위 선택 초기화
        categoryPanel.setEnabled(true);  // 카테고리 선택 활성화
    }
    private void setupCategorySelection() {
        categoryPanel = new JPanel();  // 패널 초기화
        categoryPanel.setLayout(new GridLayout(13, 1));  // 13개의 카테고리
        String[] categories = {
            "Ones", "Twos", "Threes", "Fours", "Fives", "Sixes",
            "Full House", "Four of a Kind", "Small Straight", "Large Straight",
            "Yacht", "Chance", "Bonus"
        };

        for (int i = 0; i < 13; i++) {
            categoryCheckBoxes[i] = new JCheckBox(categories[i]);
            categoryPanel.add(categoryCheckBoxes[i]);
        }

        add(categoryPanel, BorderLayout.WEST);  // UI에 추가
    }

    private void setupScoreTable() {
        String[] columnNames = {"Category", "Score", "Opponent Score"}; // Add column for opponent score
        String[][] data = new String[14][3]; // 13 categories + 1 for opponent's total score

        String[] categories = {
            "Ones", "Twos", "Threes", "Fours", "Fives", "Sixes",
            "Full House", "Four of a Kind", "Small Straight", "Large Straight",
            "Yacht", "Chance", "Bonus", "Opponent Total Score"  // Last row is for opponent's total score
        };

        for (int i = 0; i < categories.length; i++) {
            data[i][0] = categories[i];
            data[i][1] = "0";  // Initialize own score to 0
            data[i][2] = "0";  // Initialize opponent's score to 0
        }

        tableModel = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Prevent editing
            }
        };

        scoreTable = new JTable(tableModel);
        scoreTable.getTableHeader().setReorderingAllowed(false);
        JScrollPane scrollPane = new JScrollPane(scoreTable);

        // Create a panel to hold the table and total score label
        scorePanel = new JPanel();
        scorePanel.setLayout(new BorderLayout());
        scorePanel.add(scrollPane, BorderLayout.CENTER);

        // Total score label at the bottom of the panel
        totalScoreLabel = new JLabel("Total Score: 0");
        totalScoreLabel.setHorizontalAlignment(SwingConstants.CENTER);
        scorePanel.add(totalScoreLabel, BorderLayout.SOUTH);
    }


    private void updateOpponentScore(String scoreData) {
        // "OPPONENT_TOTAL_SCORE"만 추출하여 상대 점수판에 업데이트
        String[] parts = scoreData.split(" ");
        if (parts.length == 2 && parts[0].equals("OPPONENT_TOTAL_SCORE")) {
            String totalScore = parts[1];  // totalScore만 추출

            // Update the "Opponent Score" column (3rd column, 13th row)
            tableModel.setValueAt(totalScore, 13, 2);  // 13번 행, 3번째 열 (상대 점수 칸)
        }
    }
    
    private void connectToServer() {
        try {
            Socket socket = new Socket("localhost", 12371); // Connect to server
            out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        if (message.startsWith("OPPONENT_TOTAL_SCORE")) {
                            updateOpponentScore(message);  // Handle opponent's total score
                        } else if (message.equals("REQUEST_OPPONENT_SCORE")) {
                            // Optionally, send back the opponent's total score upon request
                        } else {
                            logArea.append("Server: " + message + "\n");
                        }
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

    private void setupUI() {
        // UI 초기화 코드

        totalScoreLabel = new JLabel("Opponent Total Score: 0");
        totalScoreLabel.setHorizontalAlignment(SwingConstants.CENTER);
        totalScoreLabel.setFont(new Font("Arial", Font.BOLD, 14));  // Optional: make it more noticeable
        add(totalScoreLabel, BorderLayout.SOUTH);  // 화면 하단에 위치
    }


    private void handleRollDice(ActionEvent e) {
        if (rollCount >= 3) {
            logArea.append("You have already rolled 3 times!\n");
            calculateAndUpdateScore();
            return;
        }

        Random random = new Random();
        for (int i = 0; i < diceValues.length; i++) {
            if (!diceKept[i]) {
                diceValues[i] = random.nextInt(6) + 1;
            }
            diceLabels[i].setIcon(loadDiceImage(diceValues[i]));
        }

        if (out != null) {
            out.println("OPPONENT ROLL " + formatDiceValues());
            out.flush();
        }

        logArea.append("Rolled: " + formatDiceValues() + "\n");
        rollCount++;

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
        String path = "/resources/dice" + value + ".png";
        return new ImageIcon(getClass().getResource(path));
    }
    
    private int calculateCategoryScore(int categoryIndex) {
        int score = 0;

        // "Ones", "Twos", ..., "Sixes" 계산
        if (categoryIndex >= 0 && categoryIndex <= 5) {
            int target = categoryIndex + 1;  // Ones = 1, Twos = 2, ..., Sixes = 6
            for (int value : diceValues) {
                if (value == target) {
                    score += value;  // 카테고리에 맞는 값의 합산
                }
            }
        }

        // "Full House" 계산 (3개의 동일 값과 2개의 동일 값)
        else if (categoryIndex == 6) {
            if (isFullHouse()) {
                score = 25;
            }
        }

        // "Four of a Kind" 계산 (4개의 동일 값)
        else if (categoryIndex == 7) {
            if (isFourOfAKind()) {
                score = sumAllDice();
            }
        }

        // "Small Straight" 계산 (연속된 4개의 숫자)
        else if (categoryIndex == 8) {
            if (isSmallStraight()) {
                score = 30;
            }
        }

        // "Large Straight" 계산 (연속된 5개의 숫자)
        else if (categoryIndex == 9) {
            if (isLargeStraight()) {
                score = 40;
            }
        }

        // "Yacht" 계산 (5개의 동일 값)
        else if (categoryIndex == 10) {
            if (isYacht()) {
                score = 50;
            }
        }

        // "Chance" 계산 (모든 주사위의 합)
        else if (categoryIndex == 11) {
            score = sumAllDice();
        }

        // "Bonus" 계산 (One-Six 범위 점수 합이 63 이상일 때)
        else if (categoryIndex == 12) {
            int[] categoryScores = new int[13];
            if (sumOfOnesToSixes(categoryScores) >= 63) {
                score = 35;
            }
        }

        return score;  // 계산된 점수 반환
    }


    private void calculateAndUpdateScore() {
        // 점수 계산 로직
        // int[] categoryScores = new int[13];
        String[] categories = {
            "Ones", "Twos", "Threes", "Fours", "Fives", "Sixes",
            "Full House", "Four of a Kind", "Small Straight", "Large Straight",
            "Yacht", "Chance", "Bonus"
        };

        // Ones to Sixes
        for (int i = 0; i < 6; i++) {
            int score = 0;
            for (int value : diceValues) {
                if (value == i + 1) {
                    score += value;
                }
            }
            categoryScores[i] = score;
        }

        // Full House
        if (isFullHouse()) {
            categoryScores[6] = 25;
        }

        // Four of a Kind
        if (isFourOfAKind()) {
            categoryScores[7] = sumAllDice();
        }

        // Small Straight
        if (isSmallStraight()) {
            categoryScores[8] = 30;
        }

        // Large Straight
        if (isLargeStraight()) {
            categoryScores[9] = 40;
        }

        // Yacht
        if (isYacht()) {
            categoryScores[10] = 50;
        }

        // Chance
        categoryScores[11] = sumAllDice();

        // Bonus (if total of Ones-Sixes exceeds 63)
        if (sumOfOnesToSixes(categoryScores) >= 63) {
            categoryScores[12] = 35;
        }

        // JTable 업데이트
        for (int i = 0; i < categoryScores.length; i++) {
            tableModel.setValueAt(String.valueOf(categoryScores[i]), i, 1);
        }
        
        // Total Score 계산
        int totalScore = 0;
        for (int i = 0; i < 13; i++) {
            totalScore += Integer.parseInt((String) tableModel.getValueAt(i, 1));  // 카테고리 점수 합산
        }

        totalScoreLabel.setText("Total Score: " + totalScore);  // 자신의 토탈 점수 업데이트

        // 점수 데이터를 서버에 전송
        if (out != null) {
            out.println("TOTAL_SCORE " + totalScore);  // 서버로 총합만 전송
            out.flush();
        }

        logArea.append("Total score updated: " + totalScore + "\n");
        
        // 3rd roll: After 3 rolls, get opponent's score
        if (rollCount == 3) {
            if (out != null) {
                out.println("REQUEST_OPPONENT_SCORE");  // Request opponent's total score after 3 rolls
                out.flush();
            }
        }
        
        
    }

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

    private boolean isSmallStraight() {
        int[] sortedDice = Arrays.copyOf(diceValues, diceValues.length);
        Arrays.sort(sortedDice);
        String sequence = Arrays.toString(sortedDice);
        return sequence.contains("1, 2, 3, 4") || sequence.contains("2, 3, 4, 5") || sequence.contains("3, 4, 5, 6");
    }

    private boolean isLargeStraight() {
        int[] sortedDice = Arrays.copyOf(diceValues, diceValues.length);
        Arrays.sort(sortedDice);
        return Arrays.equals(sortedDice, new int[]{1, 2, 3, 4, 5}) || Arrays.equals(sortedDice, new int[]{2, 3, 4, 5, 6});
    }

    private boolean isYacht() {
        return Arrays.stream(diceValues).distinct().count() == 1;
    }

    private int sumAllDice() {
        return Arrays.stream(diceValues).sum();
    }

    private int sumOfOnesToSixes(int[] categoryScores) {
        return Arrays.stream(categoryScores, 0, 6).sum();
    }
    private void setupSaveButton() {
        saveButton = new JButton("Save Score");

        // 버튼에 액션 리스너 추가
        saveButton.addActionListener(e -> handleScoreSave());

        // BoxLayout 사용하여 버튼을 세로로 배치
        this.setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        this.add(saveButton);  // 버튼 추가

        this.revalidate();  // 레이아웃 갱신
        this.repaint();     // UI 갱신
    }

    private void handleScoreSave() {
        boolean scoreSaved = false;

        for (int i = 0; i < 13; i++) {
            if (categoryCheckBoxes[i].isSelected() && categoryCheckBoxes[i].isEnabled()) {
                int score = calculateCategoryScore(i);  // 선택된 카테고리 점수 계산
                categoryScores[i] = score;  // 점수를 categoryScores 배열에 저장
                tableModel.setValueAt(String.valueOf(score), i, 1);  // JTable에 점수 반영
                categoryCheckBoxes[i].setEnabled(false);  // 선택된 카테고리는 더 이상 선택할 수 없게 함

                // 점수 저장 완료 메시지 출력
                logArea.append("You have selected category " + categoryCheckBoxes[i].getText() + " with score: " + score + "\n");
                sendMessageToOpponent("Player " + currentPlayer + " has saved their score for " + categoryCheckBoxes[i].getText() + " with " + score + " points.");
                scoreSaved = true;
            }
        }
        if (scoreSaved) {
            // 턴을 다른 플레이어에게 넘깁니다.
            switchToNextPlayer();
        }
    }

    public static void main(String[] args) {
        new GameClientGUI();
    }
}
*/
package client;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
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
    private JButton saveButton;
    private JTextArea logArea;
    private PrintWriter out;
    private int rollCount = 0;  // Tracks the number of rolls
    private JLabel totalScoreLabel;
    private JTable scoreTable;
    private DefaultTableModel tableModel;
    private int selectedCategoryIndex = -1;
    private Boolean[] savedCategories = new Boolean[13]; // 카테고리 점수 저장 여부를 체크
    // 플레이어 턴을 나타내는 변수 (예: 0번 플레이어, 1번 플레이어 등)
    private int currentTurn = 0; // 0: Player 1, 1: Player 2 등
    private JLabel turnLabel; // 현재 턴을 표시하는 JLabel

    public GameClientGUI() {
        setTitle("Yacht Game - Client");
        setSize(800, 600);  // Adjust window size
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());  // Set BorderLayout for the frame
        
        // turnLabel 초기화
        turnLabel = new JLabel("Current Turn: Player 1"); // 초기 값은 Player 1로 설정
        turnLabel.setFont(new Font("Arial", Font.BOLD, 16));  // 폰트 스타일 설정
        turnLabel.setBounds(10, 10, 200, 30);  // 위치와 크기 설정
        add(turnLabel); // 화면에 추가

        // Dice panel
        JPanel dicePanel = new JPanel();
        dicePanel.setLayout(new GridLayout(1, 5));  // Arrange dice in a row
        for (int i = 0; i < 5; i++) {
            diceLabels[i] = new JLabel();
            diceLabels[i].setHorizontalAlignment(SwingConstants.CENTER);
            diceLabels[i].setIcon(loadDiceImage(1)); // Initial value is 1
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

        // Set up score table and button
        setupScoreTable();

        // Roll Dice button
        rollButton = new JButton("Roll Dice");
        rollButton.addActionListener(this::handleRollDice);

        // Save Score button
        saveButton = new JButton("Save Score");
        saveButton.addActionListener(this::handleSaveScore);

        // Log area
        logArea = new JTextArea(10, 20);
        logArea.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(logArea);

        // Button panel for Roll Dice and Save Score
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 2));  // Two buttons in a single row
        buttonPanel.add(rollButton);
        buttonPanel.add(saveButton);

        // Main bottom panel where buttons and log are added
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.add(buttonPanel, BorderLayout.NORTH);  // Add buttons to top
        bottomPanel.add(logScrollPane, BorderLayout.CENTER);  // Add log area to bottom

        // Add panels to the main JFrame
        add(dicePanel, BorderLayout.CENTER);  // Dice panel in the center
        add(bottomPanel, BorderLayout.SOUTH);  // Button and log panel at the bottom
        add(new JScrollPane(scoreTable), BorderLayout.EAST);  // Score table on the right

        connectToServer();
        setVisible(true);
    }

    private void setupScoreTable() {
        String[] columnNames = {"Category", "Score", "Opponent Score"};
        String[][] data = new String[14][3];  // 13 categories + 1 for opponent's total score
        String[] categories = {
            "Ones", "Twos", "Threes", "Fours", "Fives", "Sixes",
            "Full House", "Four of a Kind", "Small Straight", "Large Straight",
            "Yacht", "Chance", "Bonus", "Opponent Total Score"
        };

        for (int i = 0; i < categories.length; i++) {
            data[i][0] = categories[i];
            data[i][1] = "0";  // Initialize own score to 0
            data[i][2] = "0";  // Initialize opponent's score to 0
        }

        tableModel = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1; // Allow editing only in "Score" column
            }
        };

        scoreTable = new JTable(tableModel);
        scoreTable.getTableHeader().setReorderingAllowed(false);
        JScrollPane scrollPane = new JScrollPane(scoreTable);

        // Add mouse listener to handle category clicks for selection
        scoreTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = scoreTable.rowAtPoint(e.getPoint());
                int col = scoreTable.columnAtPoint(e.getPoint());

                // 'Score' 열(인덱스 1)만 선택되도록 처리
                if (col == 1) {
                    // 이미 저장된 카테고리는 선택할 수 없도록 체크
                    if (savedCategories[row]) {
                        logArea.append("This category has already been saved!\n");
                        return; // 이미 저장된 카테고리는 다시 선택할 수 없음
                    }

                    // 유효한 카테고리 인덱스 선택
                    selectedCategoryIndex = row;
                    logArea.append("Selected Category: " + tableModel.getValueAt(row, 0) + "\n");
                }
            }
        });



        // Total score label
        totalScoreLabel = new JLabel("Total Score: 0");
        totalScoreLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(totalScoreLabel, BorderLayout.SOUTH);  // Add to bottom of frame

    }
    // 턴을 표시할 메서드
    private void updateTurnDisplay() {
        String currentPlayer = currentTurn == 0 ? "Player 1" : "Player 2";
        turnLabel.setText("Current Turn: " + currentPlayer);
    }

    
    private void updateOpponentScore(String scoreData) {
        // "OPPONENT_TOTAL_SCORE"만 추출하여 상대 점수판에 업데이트
        String[] parts = scoreData.split(" ");
        if (parts.length == 2 && parts[0].equals("OPPONENT_TOTAL_SCORE")) {
            String totalScore = parts[1];  // totalScore만 추출
            // Update the "Opponent Score" column (3rd column, 13th row)
            tableModel.setValueAt(totalScore, 13, 2);  // 13번 행, 3번째 열 (상대 점수 칸)
        }
    }
   
    private void connectToServer() {
        try {
            Socket socket = new Socket("localhost", 12370); // Connect to server
            out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        if (message.startsWith("OPPONENT_TOTAL_SCORE")) {
                            updateOpponentScore(message);  // Handle opponent's total score
                        } else if (message.equals("REQUEST_OPPONENT_SCORE")) {
                            // Optionally, send back the opponent's total score upon request
                        } else {
                            logArea.append("Server: " + message + "\n");
                        }
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
            calculateAndUpdateScore();
            return;
        }
        Random random = new Random();
        for (int i = 0; i < diceValues.length; i++) {
            if (!diceKept[i]) {
                diceValues[i] = random.nextInt(6) + 1;
            }
            diceLabels[i].setIcon(loadDiceImage(diceValues[i]));
        }
        if (out != null) {
            out.println("OPPONENT ROLL " + formatDiceValues());
            out.flush();
        }
        logArea.append("Rolled: " + formatDiceValues() + "\n");
        rollCount++;
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
    private void calculateAndUpdateScore() {
        // 점수 계산 로직
        int[] categoryScores = new int[13];
        String[] categories = {
            "Ones", "Twos", "Threes", "Fours", "Fives", "Sixes",
            "Full House", "Four of a Kind", "Small Straight", "Large Straight",
            "Yacht", "Chance", "Bonus"
        };
        // Ones to Sixes
        for (int i = 0; i < 6; i++) {
            int score = 0;
            for (int value : diceValues) {
                if (value == i + 1) {
                    score += value;
                }
            }
            categoryScores[i] = score;
        }
        // Full House
        if (isFullHouse()) {
            categoryScores[6] = 25;
        }
        // Four of a Kind
        if (isFourOfAKind()) {
            categoryScores[7] = sumAllDice();
        }
        // Small Straight
        if (isSmallStraight()) {
            categoryScores[8] = 30;
        }
        // Large Straight
        if (isLargeStraight()) {
            categoryScores[9] = 40;
        }
        // Yacht
        if (isYacht()) {
            categoryScores[10] = 50;
        }
        // Chance
        categoryScores[11] = sumAllDice();
        // Bonus (if total of Ones-Sixes exceeds 63)
        if (sumOfOnesToSixes(categoryScores) >= 63) {
            categoryScores[12] = 35;
        }
        // JTable 업데이트
        for (int i = 0; i < categoryScores.length; i++) {
            tableModel.setValueAt(String.valueOf(categoryScores[i]), i, 1);
        }
    }

    // 점수를 저장하고 턴을 넘기는 메서드
    private void handleSaveScore(ActionEvent e) {
        // selectedCategoryIndex가 유효한 범위(0~12) 내에 있는지 확인
        if (selectedCategoryIndex < 0 || selectedCategoryIndex >= savedCategories.length) {
            logArea.append("Invalid category selection. Index: " + selectedCategoryIndex + "\n");
            return; // 유효하지 않은 인덱스인 경우 리턴
        }

        // 점수가 이미 저장된 카테고리인지 확인
        if (savedCategories[selectedCategoryIndex]) {
            logArea.append("Category already saved!\n");
            return; // 이미 저장된 카테고리는 다시 저장할 수 없음
        }

        // 점수 계산하여 저장
        int score = calculateCategoryScore(selectedCategoryIndex);
        tableModel.setValueAt(String.valueOf(score), selectedCategoryIndex, 1);

        // 저장된 카테고리 상태 업데이트
        savedCategories[selectedCategoryIndex] = Boolean.TRUE;
        logArea.append("Score saved for category: " + tableModel.getValueAt(selectedCategoryIndex, 0) + "\n");

        // 점수를 저장한 후, 턴을 넘김
        currentTurn = (currentTurn + 1) % 2; // 2명의 플레이어가 돌아가면서 턴을 진행 (0 -> 1 -> 0 -> 1...)
        updateTurnDisplay(); // 턴 표시 업데이트
        selectedCategoryIndex = -1; // 선택된 카테고리 인덱스 초기화

        // 게임 종료 체크: 모든 카테고리가 저장되었는지 확인
        if (Arrays.stream(savedCategories).allMatch(Boolean::booleanValue)) {
            logArea.append("Game Over! All categories have been scored.\n");
            JOptionPane.showMessageDialog(this, "Game Over! All categories have been scored.");
        }
    }


    private int calculateCategoryScore(int categoryIndex) {
        int score = 0;

        // "Ones", "Twos", ..., "Sixes" 계산
        if (categoryIndex >= 0 && categoryIndex <= 5) {
            int target = categoryIndex + 1;  // Ones = 1, Twos = 2, ..., Sixes = 6
            for (int value : diceValues) {
                if (value == target) {
                    score += value;  // 카테고리에 맞는 값의 합산
                }
            }
        }

        // "Full House" 계산 (3개의 동일 값과 2개의 동일 값)
        else if (categoryIndex == 6) {
            if (isFullHouse()) {
                score = 25;
            }
        }

        // "Four of a Kind" 계산 (4개의 동일 값)
        else if (categoryIndex == 7) {
            if (isFourOfAKind()) {
                score = sumAllDice();
            }
        }

        // "Small Straight" 계산 (연속된 4개의 숫자)
        else if (categoryIndex == 8) {
            if (isSmallStraight()) {
                score = 30;
            }
        }

        // "Large Straight" 계산 (연속된 5개의 숫자)
        else if (categoryIndex == 9) {
            if (isLargeStraight()) {
                score = 40;
            }
        }

        // "Yacht" 계산 (5개의 동일 값)
        else if (categoryIndex == 10) {
            if (isYacht()) {
                score = 50;
            }
        }

        // "Chance" 계산 (모든 주사위의 합)
        else if (categoryIndex == 11) {
            score = sumAllDice();
        }

        // "Bonus" 계산 (One-Six 범위 점수 합이 63 이상일 때)
        else if (categoryIndex == 12) {
            int[] categoryScores = new int[13];
            if (sumOfOnesToSixes(categoryScores) >= 63) {
                score = 35;
            }
        }

        return score;  // 계산된 점수 반환
    }
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

    private boolean isSmallStraight() {
        int[] sortedDice = Arrays.copyOf(diceValues, diceValues.length);
        Arrays.sort(sortedDice);
        String sequence = Arrays.toString(sortedDice);
        return sequence.contains("1, 2, 3, 4") || sequence.contains("2, 3, 4, 5") || sequence.contains("3, 4, 5, 6");
    }

    private boolean isLargeStraight() {
        int[] sortedDice = Arrays.copyOf(diceValues, diceValues.length);
        Arrays.sort(sortedDice);
        return Arrays.equals(sortedDice, new int[]{1, 2, 3, 4, 5}) || Arrays.equals(sortedDice, new int[]{2, 3, 4, 5, 6});
    }

    private boolean isYacht() {
        return Arrays.stream(diceValues).distinct().count() == 1;
    }

    private int sumAllDice() {
        return Arrays.stream(diceValues).sum();
    }

    private int sumOfOnesToSixes(int[] categoryScores) {
        return Arrays.stream(categoryScores, 0, 6).sum();
    }

    private void calculateTotalScore() {
        int totalScore = 0;
        for (int i = 0; i < scoreTable.getRowCount(); i++) {
            String scoreStr = (String) tableModel.getValueAt(i, 1);
            if (!scoreStr.equals("0")) {
                totalScore += Integer.parseInt(scoreStr);
            }
        }
        totalScoreLabel.setText("Total Score: " + totalScore); // 총점 라벨에 업데이트
    }

    private ImageIcon loadDiceImage(int value) {
        String path = "/resources/dice" + value + ".png";
        return new ImageIcon(getClass().getResource(path));
    }

    public static void main(String[] args) {
        new GameClientGUI();
    }
}
