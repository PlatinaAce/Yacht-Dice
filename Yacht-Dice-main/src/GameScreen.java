import javax.swing.*; // JFrame, JLabel, JTextField 등 Swing 요소
import java.awt.*;    // Color, Font, Layout 관련 요소
import java.awt.event.*; // ActionListener 등 이벤트 관련 요소


class GameScreen {
    public GameScreen(String playerName) {
        JFrame frame = new JFrame("YAHTZEE");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(null);

        // 배경 색상 설정 (연두색)
        frame.getContentPane().setBackground(new Color(144, 238, 144)); // 연두색

        // 플레이어 이름 표시
        JLabel playerLabel = new JLabel("Player: " + playerName, SwingConstants.LEFT);
        playerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        playerLabel.setBounds(30, 20, 200, 30);
        frame.add(playerLabel);
 
        // 주사위 패널
        JPanel dicePanel = new JPanel();
        dicePanel.setBounds(30, 70, 300, 150);
        dicePanel.setLayout(new GridLayout(1, 5, 10, 0));
        dicePanel.setBackground(new Color(144, 238, 144)); // 연두색

        for (int i = 0; i < 5; i++) {
            JLabel diceLabel = new JLabel("⚀", SwingConstants.CENTER); // 기본 주사위 이모지
            diceLabel.setFont(new Font("Arial", Font.BOLD, 50));
            diceLabel.setOpaque(true);
            diceLabel.setBackground(Color.LIGHT_GRAY);
            diceLabel.setForeground(Color.BLACK);
            dicePanel.add(diceLabel);
        }
        frame.add(dicePanel);

        // ROLL 버튼
        JButton rollButton = new JButton("ROLL DICES");
        rollButton.setBounds(100, 250, 150, 40);
        rollButton.setFont(new Font("Arial", Font.BOLD, 14));
        rollButton.setBackground(new Color(100, 149, 237));
        rollButton.setForeground(Color.WHITE);
        rollButton.setFocusPainted(false);
        rollButton.addActionListener(e -> {
            // 주사위 굴리는 동작 (샘플)
            for (Component comp : dicePanel.getComponents()) {
                if (comp instanceof JLabel) {
                    int randomDice = 1 + (int) (Math.random() * 6);
                    ((JLabel) comp).setText(getDiceSymbol(randomDice));
                }
            }
        });
        frame.add(rollButton);

        // 점수판 패널
        JPanel scorePanel = new JPanel();
        scorePanel.setBounds(350, 50, 400, 400);
        scorePanel.setLayout(new GridLayout(15, 2, 5, 5));
        scorePanel.setBackground(new Color(144, 238, 144)); // 연두색

        String[] categories = {"Ones", "Twos", "Threes", "Fours", "Fives", "Sixes", "Bonus", "Sum", "Three of a kind",
                "Four of a kind", "Full House", "Small straight", "Large straight", "Chance", "YAHTZEE"};
        for (String category : categories) {
            JLabel label = new JLabel(category + ":", SwingConstants.LEFT);
            label.setFont(new Font("Arial", Font.BOLD, 14));
            JTextField scoreField = new JTextField("-");
            scoreField.setHorizontalAlignment(JTextField.CENTER);
            scoreField.setEditable(false);

            scorePanel.add(label);
            scorePanel.add(scoreField);
        }
        frame.add(scorePanel);

        frame.setVisible(true);
    }

    private String getDiceSymbol(int value) {
        switch (value) {
            case 1: return "⚀";
            case 2: return "⚁";
            case 3: return "⚂";
            case 4: return "⚃";
            case 5: return "⚄";
            case 6: return "⚅";
        }
        return "⚀";
    }
}
