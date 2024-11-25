import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;

public class GameGUI {
    private JFrame frame;
    private JLabel[] diceLabels;
    private JButton rollButton, submitScoreButton;
    private JCheckBox[] keepCheckboxes;
    private Game game;
    private JLabel rollsLeftLabel;
    private JTable scoreCardTable;
    private DefaultTableModel tableModel;

    public GameGUI() {
        game = new Game();
        initializeUI();
    }

    private void initializeUI() {
        frame = new JFrame("Yacht Dice");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 500);
        frame.setLayout(new BorderLayout());

        JPanel controlPanel = createControlPanel();
        JPanel dicePanel = createDicePanel();
        JPanel scorePanel = createScorePanel();

        rollButton.addActionListener(e -> rollDice());
        submitScoreButton.addActionListener(e -> submitScore());

        frame.add(controlPanel, BorderLayout.SOUTH);
        frame.add(dicePanel, BorderLayout.CENTER);
        frame.add(scorePanel, BorderLayout.EAST);

        updateDiceImages(true);
        frame.setVisible(true);
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        rollButton = new JButton("Roll Dice");
        submitScoreButton = new JButton("Submit Score");
        rollsLeftLabel = new JLabel("Rolls Left: " + game.getRollsLeft());
        panel.add(rollButton);
        panel.add(submitScoreButton);
        panel.add(rollsLeftLabel);
        return panel;
    }

    private JPanel createDicePanel() {
        JPanel panel = new JPanel(new GridLayout(1, 5));
        diceLabels = new JLabel[5];
        keepCheckboxes = new JCheckBox[5];

        for (int i = 0; i < 5; i++) {
            JPanel diceContainer = new JPanel(new BorderLayout());
            diceLabels[i] = new JLabel("", SwingConstants.CENTER);
            keepCheckboxes[i] = new JCheckBox("Keep", false); // 수정된 부분
            diceContainer.add(diceLabels[i], BorderLayout.CENTER);
            diceContainer.add(keepCheckboxes[i], BorderLayout.SOUTH);
            panel.add(diceContainer);
        }

        return panel;
    }

    private JPanel createScorePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Scorecard", SwingConstants.CENTER);

        String[] columns = {"Category", "Score"};
        Object[][] data = {
            {"Aces", ""},
            {"Deuces", ""},
            {"Threes", ""},
            {"Fours", ""},
            {"Fives", ""},
            {"Sixes", ""},
            {"Subtotal", "0/63"},
            {"+35 Bonus", ""},
            {"Choice", ""},
            {"4 of a Kind", ""},
            {"Full House", ""},
            {"S. Straight", ""},
            {"L. Straight", ""},
            {"Yacht", ""},
            {"Total", "0"}
        };

        tableModel = new DefaultTableModel(data, columns);
        scoreCardTable = new JTable(tableModel);

        panel.add(title, BorderLayout.NORTH);
        panel.add(new JScrollPane(scoreCardTable), BorderLayout.CENTER);
        return panel;
    }

    private void updateDiceImages(boolean isInitial) {
        for (int i = 0; i < diceLabels.length; i++) {
            if (isInitial) {
                diceLabels[i].setText("Dice " + (i + 1));
            } else {
                diceLabels[i].setText("Value: " + game.getDice()[i].getValue());
            }
        }
        rollsLeftLabel.setText("Rolls Left: " + game.getRollsLeft());
    }

    private void rollDice() {
        boolean[] keep = new boolean[5];
        for (int i = 0; i < keepCheckboxes.length; i++) {
            keep[i] = keepCheckboxes[i].isSelected();
        }
        game.rollDice(keep);
        updateDiceImages(false);
    }

    private void submitScore() {
        int selectedRow = scoreCardTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, "Select a category to submit.");
            return;
        }

        try {
            game.submitScore(selectedRow);
            updateDiceImages(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, e.getMessage());
        }
    }

    public static void main(String[] args) {
        new GameGUI();
    }
}

