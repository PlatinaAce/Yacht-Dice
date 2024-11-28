/*
package game;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

public class YachtGameGUI extends JFrame {
    private int[] diceValues = new int[5];
    private JLabel[] diceLabels = new JLabel[5];
    private JButton rollButton;
    private JTextArea scoreBoard;
    private Random random = new Random();

    public YachtGameGUI() {
        setTitle("Yacht Game");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 주사위 패널
        JPanel dicePanel = new JPanel();
        dicePanel.setLayout(new GridLayout(1, 5));
        for (int i = 0; i < 5; i++) {
            diceLabels[i] = new JLabel("0", SwingConstants.CENTER);
            diceLabels[i].setFont(new Font("Serif", Font.BOLD, 24));
            diceLabels[i].setBorder(BorderFactory.createLineBorder(Color.BLACK));
            dicePanel.add(diceLabels[i]);
        }

        // 버튼 및 점수판
        rollButton = new JButton("Roll Dice");
        rollButton.addActionListener(new RollDiceListener());

        scoreBoard = new JTextArea(10, 20);
        scoreBoard.setEditable(false);

        // 메인 패널 구성
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.add(rollButton, BorderLayout.NORTH);
        bottomPanel.add(new JScrollPane(scoreBoard), BorderLayout.CENTER);

        add(dicePanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    // 주사위를 굴리는 액션 리스너
    private class RollDiceListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            for (int i = 0; i < diceValues.length; i++) {
                diceValues[i] = random.nextInt(6) + 1;
                diceLabels[i].setText(String.valueOf(diceValues[i]));
            }
            scoreBoard.append("Rolled: " + getDiceString() + "\n");
        }

        private String getDiceString() {
            StringBuilder sb = new StringBuilder();
            for (int value : diceValues) {
                sb.append(value).append(" ");
            }
            return sb.toString().trim();
        }
    }

    public static void main(String[] args) {
        new YachtGameGUI();
    }
}
*/