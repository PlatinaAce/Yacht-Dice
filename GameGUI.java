import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GameGUI {
    private JFrame frame;
    private JLabel[] diceLabels;
    private JButton rollButton, submitScoreButton;
    private JCheckBox[] keepCheckboxes;
    private Game game;
    private JLabel rollsLeftLabel;
    private JTable scoreCardTable; // 점수표 테이블
    private DefaultTableModel tableModel; // 테이블 모델
    private boolean[] scoresLocked; // 점수가 기록되었는지 추적

    public GameGUI() {
        game = new Game();
        scoresLocked = new boolean[12]; // 각 점수 항목의 상태를 저장
        initializeUI();
    }

    private void initializeUI() {
        frame = new JFrame("Yacht Dice Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 600);
        frame.setLayout(new BorderLayout());

        // 주사위 패널
        JPanel dicePanel = new JPanel();
        dicePanel.setLayout(new GridLayout(1, 5));
        diceLabels = new JLabel[5];
        keepCheckboxes = new JCheckBox[5];

        for (int i = 0; i < 5; i++) {
            JPanel diceContainer = new JPanel();
            diceContainer.setLayout(new BorderLayout());

            diceLabels[i] = new JLabel();
            diceLabels[i].setHorizontalAlignment(SwingConstants.CENTER);
            keepCheckboxes[i] = new JCheckBox("Keep");
            keepCheckboxes[i].setHorizontalAlignment(SwingConstants.CENTER);

            diceContainer.add(diceLabels[i], BorderLayout.CENTER);
            diceContainer.add(keepCheckboxes[i], BorderLayout.SOUTH);
            dicePanel.add(diceContainer);
        }

        // 버튼 패널
        JPanel controlPanel = new JPanel();
        rollButton = new JButton("Roll Dice");
        submitScoreButton = new JButton("Submit Score");
        rollsLeftLabel = new JLabel("Rolls Left: " + game.getRollsLeft());
        controlPanel.add(rollButton);
        controlPanel.add(submitScoreButton);
        controlPanel.add(rollsLeftLabel);

        // 점수표 패널
        JPanel scorePanel = new JPanel();
        scorePanel.setLayout(new BorderLayout());
        JLabel scoreTitle = new JLabel("Scorecard:");
        scoreTitle.setHorizontalAlignment(SwingConstants.CENTER);

        String[] columnNames = {"Category", "Score"};
        String[] scoreNames = {
                "Ones", "Twos", "Threes", "Fours", "Fives", "Sixes",
                "Choice", "Four of a Kind", "Full House" ,
                "Small Straight", "Large Straight", "Yacht"
        };
        Object[][] data = new Object[12][2];
        for (int i = 0; i < scoreNames.length; i++) {
            data[i][0] = scoreNames[i];
            data[i][1] = ""; // 초기 점수는 비어 있음
        }

        tableModel = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1 && !scoresLocked[row]; // 점수가 기록되지 않은 경우만 수정 가능
            }
        };

        scoreCardTable = new JTable(tableModel);
        JScrollPane scoreScrollPane = new JScrollPane(scoreCardTable);
        scorePanel.add(scoreTitle, BorderLayout.NORTH);
        scorePanel.add(scoreScrollPane, BorderLayout.CENTER);

        // 버튼 액션 리스너 추가
        rollButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rollDice();
            }
        });

        submitScoreButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                submitScore();
            }
        });

        // 메인 프레임에 컴포넌트 추가
        frame.add(dicePanel, BorderLayout.CENTER);
        frame.add(controlPanel, BorderLayout.SOUTH);
        frame.add(scorePanel, BorderLayout.EAST);

        // 초기 주사위 이미지로 dice0.png 표시
        updateDiceImages(true);

        frame.setVisible(true);
    }

    private void updateDiceImages() {
        updateDiceImages(false); // 주사위 던지기 후 실제 값으로 업데이트
    }

    private void updateDiceImages(boolean isInitial) {
        Dice[] dice = game.getDice();
        String imagePath;

        for (int i = 0; i < dice.length; i++) {
            // 게임 시작 시 또는 턴 시작 시 dice0 이미지로 초기화
            if (isInitial) {
                imagePath = "/resources/dice0.png"; // dice0 이미지로 초기화
            } else {
                imagePath = "/resources/dice" + dice[i].getValue() + ".png"; // 실제 값으로 이미지 업데이트
            }

            try {
                ImageIcon icon = new ImageIcon(getClass().getResource(imagePath));
                diceLabels[i].setIcon(icon);
            } catch (Exception e) {
                diceLabels[i].setText("Error");
            }
        }
        rollsLeftLabel.setText("Rolls Left: " + game.getRollsLeft());
    }

    private void rollDice() {
        if (game.getRollsLeft() <= 0) {
            JOptionPane.showMessageDialog(frame, "No rolls left! Submit your score or reset the turn.");
            return;
        }
        boolean[] keepFlags = new boolean[5];
        for (int i = 0; i < 5; i++) {
            keepFlags[i] = keepCheckboxes[i].isSelected();
        }
        game.rollDice(keepFlags);
        game.calculateScores();
        updateDiceImages(false); // 실제 주사위 값으로 이미지 업데이트
        updateScoreTable();
    }

    private void updateScoreTable() {
        int[] scoreCard = game.getScoreCard();
        for (int i = 0; i < scoreCard.length; i++) {
            if (!scoresLocked[i]) { // 점수가 기록되지 않은 경우에만 업데이트
                tableModel.setValueAt(scoreCard[i], i, 1);
            }
        }
    }

    private void submitScore() {
        int selectedRow = scoreCardTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, "Please select a category to submit your score.");
            return;
        }
        if (scoresLocked[selectedRow]) {
            JOptionPane.showMessageDialog(frame, "This category is already locked.");
            return;
        }
        Object scoreValue = tableModel.getValueAt(selectedRow, 1);
        if (scoreValue == null || scoreValue.toString().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No score available for this category.");
            return;
        }

        scoresLocked[selectedRow] = true; // 점수 잠금
        JOptionPane.showMessageDialog(frame, "Score submitted for " + tableModel.getValueAt(selectedRow, 0) + ".");
        game.resetTurn(); // 턴 리셋
        updateDiceImages(true); // dice0 이미지로 초기화
    }

    public static void main(String[] args) {
        new GameGUI();
    }
}
