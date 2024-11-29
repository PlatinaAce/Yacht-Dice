//서버로부터 턴 신호를 받아 자신의 턴일때만 roll 버튼 활성화

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import javax.swing.table.DefaultTableModel;


class GameScreen {
    private JFrame frame;
    private JLabel[] diceLabels; // 주사위를 표시하는 라벨 배열
    private JButton rollButton, submitScoreButton;
    private JCheckBox[] keepCheckboxes; // 주사위 보관 여부를 표시하는 체크박스 배열
    private JLabel turnLabel; // 현재 턴을 표시하는 라벨
    private JTable scoreTable; // 점수판
    private DefaultTableModel tableModel; // 점수판의 데이터 모델
    private boolean isMyTurn; // 현재 플레이어의 턴 여부
    private String playerRole; // "P1" 또는 "P2"로 구분
    private String opponentRole; // 상대방의 역할

    public GameScreen(String playerRole) {
        this.playerRole = playerRole;
        this.opponentRole = playerRole.equals("P1") ? "P2" : "P1";
        this.isMyTurn = false; // 초기에는 자신의 턴이 아님
        initializeUI();
    }

    private void initializeUI() {
        frame = new JFrame("Yacht Dice - " + playerRole);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 600);
        frame.setLayout(new BorderLayout());

        // 상단 패널: 현재 턴 표시
        JPanel topPanel = new JPanel();
        turnLabel = new JLabel("Waiting for opponent...");
        turnLabel.setFont(new Font("Arial", Font.BOLD, 18));
        topPanel.add(turnLabel);
        frame.add(topPanel, BorderLayout.NORTH);

        // 중앙 패널: 주사위 및 점수판
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridLayout(2, 1));

        // 주사위 패널
        JPanel dicePanel = new JPanel();
        dicePanel.setLayout(new GridLayout(1, 5));
        diceLabels = new JLabel[5];
        keepCheckboxes = new JCheckBox[5];
        for (int i = 0; i < 5; i++) {
            JPanel diceContainer = new JPanel(new BorderLayout());
            diceLabels[i] = new JLabel("⚀", SwingConstants.CENTER);
            diceLabels[i].setFont(new Font("Arial", Font.BOLD, 36));
            keepCheckboxes[i] = new JCheckBox("Keep");
            diceContainer.add(diceLabels[i], BorderLayout.CENTER);
            diceContainer.add(keepCheckboxes[i], BorderLayout.SOUTH);
            dicePanel.add(diceContainer);
        }
        centerPanel.add(dicePanel);

        // 점수판 패널
        String[] columnNames = {"Category", playerRole, opponentRole};
        String[][] data = new String[15][3];
        String[] categories = {"Aces", "Deuces", "Threes", "Fours", "Fives", "Sixes", "Subtotal", "+35 Bonus",
                               "Choice", "Four of a Kind", "Full House", "Small Straight", "Large Straight", "Yacht", "Total"};
        for (int i = 0; i < 15; i++) {
            data[i][0] = categories[i];
            data[i][1] = "";
            data[i][2] = "";
        }
        tableModel = new DefaultTableModel(data, columnNames);
        scoreTable = new JTable(tableModel);
        centerPanel.add(new JScrollPane(scoreTable));
        frame.add(centerPanel, BorderLayout.CENTER);

        // 하단 패널: 버튼 및 남은 굴리기 횟수
        JPanel bottomPanel = new JPanel();
        rollButton = new JButton("Roll Dice");
        submitScoreButton = new JButton("Submit Score");
        rollButton.setEnabled(false); // 초기에는 비활성화
        submitScoreButton.setEnabled(false); // 초기에는 비활성화
        bottomPanel.add(rollButton);
        bottomPanel.add(submitScoreButton);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    public void updateTurn(boolean isMyTurn) {
        this.isMyTurn = isMyTurn;
        rollButton.setEnabled(isMyTurn);
        submitScoreButton.setEnabled(isMyTurn);
        turnLabel.setText(isMyTurn ? "Your turn (" + playerRole + ")" : "Opponent's turn (" + opponentRole + ")");
    }

    public void updateDice(int[] diceResults) {
        for (int i = 0; i < diceLabels.length; i++) {
            diceLabels[i].setText(getDiceSymbol(diceResults[i]));
        }
    }

    public void updateScore(String player, int row, int score) {
        int column = player.equals(playerRole) ? 1 : 2;
        tableModel.setValueAt(String.valueOf(score), row, column);
    }

    private String getDiceSymbol(int value) {
        switch (value) {
            case 1: return "⚀";
            case 2: return "⚁";
            case 3: return "⚂";
            case 4: return "⚃";
            case 5: return "⚄";
            case 6: return "⚅";
            default: return "?";
        }
    }
}

