//MultiGameGUI에서 현재 턴 관리 및 버튼 활성화 로직은 잘 설계되었으나, 
//서버와의 통신을 통한 동기화 처리가 더 필요.

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.io.IOException;



public class MultiGameGUI {
    private JFrame frame;
    private JLabel[] diceLabels;
    private JButton rollButton, submitScoreButton;
    private JCheckBox[] keepCheckboxes;
    private Game game; // 게임 객체
    private JLabel rollsLeftLabel; // 주사위 굴리기 횟수 레이블
    private JTable scoreCardTable;
    private DefaultTableModel tableModel;
    private boolean[] scoresLocked;
    private JLabel playerNameLabel, opponentNameLabel; // 이름을 표시할 레이블
    private String playerName = "";
    private String opponentName = "";
    private boolean isMyTurn = false;  // 내 차례인지 여부를 나타내는 변수
    private String playerRole = ""; // 현재 유저가 P1인지 P2인지 구분할 변수
    private PrintWriter out;
    private BufferedReader in;
    
    
    
    public MultiGameGUI(BufferedReader in , PrintWriter out) {
        this.in = in;
        this.out = out;
        game = new Game();
        scoresLocked = new boolean[15]; // 점수 잠금 배열
        scoresLocked[6] = true;
        scoresLocked[7] = true;
        scoresLocked[14] = true;
        initializeUI();
        startMessageListener();
    }
    
    
    
    private void handleServerMessage(String message) {
        if (message.startsWith("TURN_START")) {
            isMyTurn = true;
            updateButtonState();
            JOptionPane.showMessageDialog(frame, "It's your turn!");
        } else if (message.startsWith("TURN_WAIT")) {
            isMyTurn = false;
            updateButtonState();
            JOptionPane.showMessageDialog(frame, "Waiting for the other player...");
        } else if (message.startsWith("DICE_UPDATE")) {
            // 예: "DICE_UPDATE [1, 2, 3, 4, 5]"
            String diceValues = message.substring("DICE_UPDATE ".length()).trim();
            updateDiceLabels(diceValues);
        } else if (message.startsWith("SCORE_UPDATE")) {
            // 예: "SCORE_UPDATE Player 1: 50"
            String scoreUpdate = message.substring("SCORE_UPDATE ".length()).trim();
            JOptionPane.showMessageDialog(frame, scoreUpdate);
        } else if (message.startsWith("GAME_OVER")) {
            // 서버에서 최종 결과를 전달
            String result = message.substring("GAME_OVER ".length()).trim();
            announceWinner();
        }
    }
    
    private void updateDiceLabels(String diceValues) {
        diceValues = diceValues.replace("[", "").replace("]", ""); // 대괄호 제거
        String[] values = diceValues.split(", "); // 쉼표로 분리
        for (int i = 0; i < diceLabels.length && i < values.length; i++) {
            diceLabels[i].setText(values[i]);
        }
    }




    private void initializeUI() {
        frame = new JFrame("Yacht Dice");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 450);
        frame.setLayout(new BorderLayout());

        // 상대방 이름 표시(이름이 잘 나오는 지 테스트용으로)
        opponentNameLabel = new JLabel("Opponent: " + opponentName, SwingConstants.CENTER);
        opponentNameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        frame.add(opponentNameLabel, BorderLayout.NORTH); // 상단에 상대방 이름 표시(잘 나오는 지 테스트용으로)

        // 내 이름 표시
        playerNameLabel = new JLabel("ME: " + "YourName", SwingConstants.CENTER);
        playerNameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        frame.add(playerNameLabel, BorderLayout.SOUTH); // 하단에 내 이름 표시

        // Control Panel (Roll Dice, Submit Score, Rolls Left)
        JPanel controlPanel = createControlPanel();

        // Dice Panel
        JPanel dicePanel = createDicePanel();

        // Scorecard Panel
        JPanel scorePanel = createScorePanel();

        rollButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (canPerformAction()) {
                    rollDice();
                } else {
                    JOptionPane.showMessageDialog(frame, "It's not your turn!");
                }
            }
        });

        submitScoreButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (canPerformAction()) {
                    submitScore();
                } else {
                    JOptionPane.showMessageDialog(frame, "It's not your turn!");
                }
            }
        });

        frame.add(dicePanel, BorderLayout.CENTER);
        frame.add(controlPanel, BorderLayout.SOUTH);
        frame.add(scorePanel, BorderLayout.EAST);

        // 초기 주사위 이미지를 dice0.png로 표시
        updateDiceImages(true);
        updateTurnIndicator();
        updateButtonState();

        frame.setVisible(true); // 프레임을 화면에 표시
    }
    
    private void startMessageListener() {
        new Thread(() -> {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Server: " + message); // 서버 메시지 디버그 출력
                    handleServerMessage(message);
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(frame, "Disconnected from the server.", "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }).start();
    }

    private boolean canPerformAction() {
        return (game.getCurrentTurn() == 0 && playerRole.equals("P1")) ||
                (game.getCurrentTurn() == 1 && playerRole.equals("P2"));
    }

    // 버튼 패널 설정
    private JPanel createControlPanel() {     //okokokokokokokokokok
        JPanel controlPanel = new JPanel();
        rollButton = new JButton("Roll Dice"); // 주사위 굴리기 버튼
        submitScoreButton = new JButton("Submit Score"); // 점수 제출 버튼
        rollsLeftLabel = new JLabel("Rolls Left: " + game.getRollsLeft()); // 남은 주사위 횟수 레이블
        controlPanel.add(rollButton); // 버튼 패널에 버튼 추가
        controlPanel.add(submitScoreButton);
        controlPanel.add(rollsLeftLabel);

        return controlPanel;
    }

    private void updateTurnIndicator() {
        if (game.getCurrentTurn() == 0) {
            playerNameLabel.setForeground(Color.RED);
            opponentNameLabel.setForeground(Color.BLACK);
        } else {
            playerNameLabel.setForeground(Color.BLACK);
            opponentNameLabel.setForeground(Color.RED);
        }

        String currentPlayer = (game.getCurrentTurn() == 0) ? "P1" : "P2";
        frame.setTitle("Yacht Dice - " + currentPlayer + "'s Turn");
    }

    private void updateButtonState() {
        // 현재 턴이 내 턴인지 확인
        boolean isMyTurn = (game.getCurrentTurn() == 0 && playerRole.equals("P1")) ||
                (game.getCurrentTurn() == 1 && playerRole.equals("P2"));

        // 내 턴일 때만 버튼 활성화
        rollButton.setEnabled(isMyTurn && game.getRollsLeft() > 0);
        submitScoreButton.setEnabled(isMyTurn && !game.isScoreSubmitted());

        // 체크박스도 턴에 따라 활성화/비활성화
        for (JCheckBox checkbox : keepCheckboxes) {
            checkbox.setEnabled(isMyTurn);
        }

        // 턴 표시 업데이트
        updateTurnIndicator();
    }

    // 주사위 패널 설정
    private JPanel createDicePanel() { //okokokokokok
        JPanel dicePanel = new JPanel();
        dicePanel.setLayout(new GridLayout(1, 5));

        diceLabels = new JLabel[5]; // 주사위 값 레이블 배열
        keepCheckboxes = new JCheckBox[5]; // 체크박스 배열

        // 각 주사위에 대한 레이블과 체크박스를 추가
        for (int i = 0; i < 5; i++) {
            JPanel diceContainer = new JPanel();
            diceContainer.setLayout(new BorderLayout());

            diceLabels[i] = new JLabel(); // 주사위 값 표시 레이블
            diceLabels[i].setHorizontalAlignment(SwingConstants.CENTER);
            keepCheckboxes[i] = new JCheckBox("Keep"); // "Keep" 체크박스
            keepCheckboxes[i].setHorizontalAlignment(SwingConstants.CENTER);

            diceContainer.add(diceLabels[i], BorderLayout.CENTER); // 레이블을 중앙에 배치
            diceContainer.add(keepCheckboxes[i], BorderLayout.SOUTH); // 체크박스를 하단에 배치
            dicePanel.add(diceContainer); // 주사위 패널에 추가
        }

        return dicePanel;
    }

    private JPanel createScorePanel() {
        JPanel scorePanel = new JPanel();
        scorePanel.setLayout(new BorderLayout());

        JLabel scoreTitle = new JLabel("Scorecard");
        scoreTitle.setHorizontalAlignment(SwingConstants.CENTER);

        String[] columnNames = {"Categories", "P1", "P2"};
        Object[][] data = {
                {"Aces", "", ""},
                {"Deuces", "", ""},
                {"Threes", "", ""},
                {"Fours", "", ""},
                {"Fives", "", ""},
                {"Sixes", "", ""},
                {"Subtotal", "0/63", "0/63"},
                {"+35 Bonus", "", ""},
                {"Choice", "", ""},
                {"4 of a Kind", "", ""},
                {"Full House", "", ""},
                {"S. Straight", "", ""},
                {"L. Straight", "", ""},
                {"Yacht", "", ""},
                {"Total", "0", "0"}
        };

        tableModel = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // P1과 P2의 점수 입력 가능 여부 제어
                if (column == 1 || column == 2) {
                    // Subtotal, Bonus, Total은 수정 불가
                    return row != 6 && row != 7 && row != 15;
                }
                return false;
            }
        };

        scoreCardTable = new JTable(tableModel);
        scoreCardTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                // P1 칸(column == 1)에서만 색상 처리
                if (column == 1) {
                    // Subtotal, +35 Bonus, Total 칸은 항상 흰색
                    if (row == 6 || row == 7 || row == 14) {
                        cell.setBackground(Color.WHITE);
                    } else if (row < scoresLocked.length && scoresLocked[row]) {
                        cell.setBackground(Color.LIGHT_GRAY);
                    } else {
                        cell.setBackground(Color.WHITE);
                    }
                } else {
                    cell.setBackground(Color.WHITE); // P2 또는 다른 열은 흰색
                }
                return cell;
            }
        });

        JScrollPane scoreScrollPane = new JScrollPane(scoreCardTable);
        scorePanel.add(scoreTitle, BorderLayout.NORTH);
        scorePanel.add(scoreScrollPane, BorderLayout.CENTER);

        return scorePanel;
    }

    // 주사위 이미지를 업데이트하는 함수 (초기 상태와 실제 값에 따른 이미지 업데이트)
    private void updateDiceImages ( boolean isInitial){
        Dice[] dice = game.getDice(); // 게임에서 주사위 값 가져오기
        String imagePath;

        for (int i = 0; i < dice.length; i++) {
            // 게임 시작 시 또는 턴 시작 시 dice0 이미지로 초기화
            if (isInitial) {
                imagePath = "/resources/dice0.png"; // 초기 상태는 dice0 이미지
            } else {
                imagePath = "/resources/dice" + dice[i].getValue() + ".png"; // 실제 주사위 값으로 이미지 업데이트
            }

            try {
                ImageIcon icon = new ImageIcon(getClass().getResource(imagePath));
                diceLabels[i].setIcon(icon); // 주사위 이미지 설정
            } catch (Exception e) {
                diceLabels[i].setText("Error"); // 이미지 로딩 실패 시 텍스트 표시
            }
        }
        rollsLeftLabel.setText("Rolls Left: " + game.getRollsLeft()); // 남은 주사위 횟수 갱신
    }

    // 주사위 굴리기 함수
    private void rollDice () {
        if (!canPerformAction()) {
            JOptionPane.showMessageDialog(frame, "It's not your turn!");
            return;
        }

        if (game.getRollsLeft() <= 0) {
            JOptionPane.showMessageDialog(frame, "No rolls left! Submit your score or reset the turn.");
            return; // 남은 주사위가 없으면 경고 메시지 출력
        }
        boolean[] keepFlags = new boolean[5];
        for (int i = 0; i < 5; i++) {
            keepFlags[i] = keepCheckboxes[i].isSelected(); // 체크박스를 기반으로 주사위 유지 여부 결정
        }
        game.rollDice(keepFlags); // 주사위 굴리기
        game.calculateScores(); // 점수 계산
        updateDiceImages(false); // 실제 주사위 값으로 이미지 업데이트
        updateScoreTable(); // 점수표 업데이트
    }

    private void updateScoreTable() {
        int[] scoreCard = game.getScoreCard();
        int[] recordCard = game.getRecordCard();

        for (int i = 0; i < 6; i++) {
            if (!scoresLocked[i]) {
                tableModel.setValueAt(scoreCard[i], i, 1); // Aces ~ Sixes
            } else {
                tableModel.setValueAt(recordCard[i], i, 1); // 확정된 점수 표시
            }
        }

        for (int i = 8; i < 14; i++) {
            if (!scoresLocked[i]) {
                tableModel.setValueAt(scoreCard[i], i, 1); // Choice ~ Yacht
            } else {
                tableModel.setValueAt(recordCard[i], i, 1); // 확정된 점수 표시
            }
        }
    }

    private void updateTotalTable() {
        int[] recordCard = game.getRecordCard();

        // Subtotal, Bonus, Total 업데이트
        tableModel.setValueAt(recordCard[6] + "/63", 6, 1);
        tableModel.setValueAt(recordCard[7] > 0 ? "35" : "", 7, 1);
        tableModel.setValueAt(recordCard[14], 14, 1);
    }

    // 점수 제출 함수
    private void submitScore() {
        if (!canPerformAction()) {
            JOptionPane.showMessageDialog(frame, "It's not your turn!");
            return;
        }

        if (game.isScoreSubmitted()) { // 점수를 제출했는데 또 제출하려는지 확인
            JOptionPane.showMessageDialog(frame, "You already submitted the score.");
            return;
        }

        int selectedRow = scoreCardTable.getSelectedRow(); // 선택된 행
        int selectedColumn = scoreCardTable.getSelectedColumn(); // 선택된 열

        int expectedColumn = (game.getCurrentTurn() == 0) ? 1 : 2;
        if (selectedColumn != expectedColumn) {
            JOptionPane.showMessageDialog(frame,
                    "Please select your own column (P" + (game.getCurrentTurn() + 1) + ")");
            return;
        }

        if (selectedRow == -1 || (selectedColumn != 1 && selectedColumn != 2)) {
            JOptionPane.showMessageDialog(frame, "Please select a valid category.");
            return;
        }

        if (scoresLocked[selectedRow]) {
            JOptionPane.showMessageDialog(frame, "You cannot submit here!");
            return;
        }

        Object scoreValue = tableModel.getValueAt(selectedRow, selectedColumn);
        if (scoreValue == null || scoreValue.toString().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "You cannot submit here!");
            return;
        }

        scoresLocked[selectedRow] = true; // 선택된 점수 잠금

        // 업데이트: recordCard에 점수 저장
        game.getRecordCard()[selectedRow] = game.getScoreCard()[selectedRow];

        // Game 클래스의 Subtotal 및 Total 업데이트
        game.calculateTotal(scoresLocked);
        updateTotalTable(); // 점수표 갱신

        // 점수 입력 후 해당 셀을 회색으로 처리
        scoreCardTable.repaint();

        // Keep 해제
        for (JCheckBox keepCheckBox : keepCheckboxes) {
            keepCheckBox.setSelected(false); // Keep 체크 해제
        }

        String player = (selectedColumn == 1) ? "P1" : "P2";
        JOptionPane.showMessageDialog(frame, "Score submitted for " + player + ": " + tableModel.getValueAt(selectedRow, 0));

        // 선택되지 않은 점수를 초기화
        int[] scoreCard = game.getScoreCard();
        for (int i = 0; i < scoreCard.length; i++) {
            if (!scoresLocked[i]) {
                scoreCard[i] = 0; // 점수 초기화
            }
        }
        updateScoreTable(); // 초기화된 점수표 갱신

        game.setScoreSubmitted(true); // 점수 제출 상태를 true로 설정

        // 모든 칸이 채워졌는지 확인
        if (isGameOver()) {
            announceWinner(); // 승자 확인 및 표시
        }
        else{
            game.nextTurn(); // 다음 플레이어로 턴 전환

            updateTurnIndicator(); // 턴 전환 시 UI 업데이트
            updateButtonState();

            updateDiceImages(true); // dice0 이미지로 초기화

            // 턴 전환 메시지 전송 (네트워크)
            sendMessage("TURN_CHANGE:" + game.getCurrentTurn());

            String message = "Turn changed to Player " + (game.getCurrentTurn() == 0 ? "1" : "2");
            JOptionPane.showMessageDialog(frame, message);
        }
    }

    // 모든 점수가 채워졌는지 확인
    private boolean isGameOver() {
        for (boolean locked : scoresLocked) {
            if (!locked) {
                return false; // 채워지지 않은 칸이 있으면 게임 계속
            }
        }
        return true; // 모든 칸이 채워짐
    }

    // 승자 확인 및 팝업창 띄우기
    private void announceWinner() {
        int p1Score = Integer.parseInt(tableModel.getValueAt(14, 1).toString()); // P1 Total 점수
        int p2Score = Integer.parseInt(tableModel.getValueAt(14, 2).toString()); // P2 Total 점수

        String message;
        if (p1Score > p2Score) {
            message = "P1 Wins!\nScore: " + p1Score;
        } else if (p2Score > p1Score) {
            message = "P2 Wins!\nScore: " + p2Score;
        } else {
            message = "It's a Tie!\nScore: " + p1Score;
        }

        // 게임 종료 메시지
        JOptionPane.showMessageDialog(frame, message);

        // 게임 종료 후 버튼 비활성화
        rollButton.setEnabled(false);
        submitScoreButton.setEnabled(false);
    }

    public void setPlayerRole(String role) {
        this.playerRole = role;

        // Update name labels based on player role
        if (role.equals("P1")) {
            playerNameLabel.setText("P1: " + playerName);
            opponentNameLabel.setText("P2: " + opponentName);
        } else if (role.equals("P2")) {
            playerNameLabel.setText("P2: " + playerName);
            opponentNameLabel.setText("P1: " + opponentName);
        }

        updateButtonState();
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }
    
    public void enableTurnControls() {
        rollButton.setEnabled(true);
        submitScoreButton.setEnabled(true);
    }

    public void disableTurnControls() {
        rollButton.setEnabled(false);
        submitScoreButton.setEnabled(false);
    }

    public void updateDiceDisplay(int[] diceResults) {
        for (int i = 0; i < diceResults.length; i++) {
            diceLabels[i].setText(String.valueOf(diceResults[i])); // 주사위 값을 레이블에 표시
        }
    }

    public void updateScore(String player, int score) {
        int column = player.equals("P1") ? 1 : 2; // P1은 첫 번째, P2는 두 번째 열
        tableModel.setValueAt(score, 14, column); // Total 점수 위치에 업데이트
    }

    public void announceWinner(String result) {
        JOptionPane.showMessageDialog(frame, result);
        rollButton.setEnabled(false);
        submitScoreButton.setEnabled(false);
    }

}