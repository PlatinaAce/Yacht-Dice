import javax.swing.*;
import java.awt.*;

public class NameInputScreen {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("YAHTZEE");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(400, 300);
            frame.setLayout(null);

            // 배경 색상 설정 (연두색)
            frame.getContentPane().setBackground(new Color(144, 238, 144)); // 연두색

            // 제목 라벨
            JLabel titleLabel = new JLabel("YAHTZEE", SwingConstants.CENTER);
            titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
            titleLabel.setForeground(Color.BLACK);
            titleLabel.setBounds(50, 30, 300, 50);
            frame.add(titleLabel);

            // 이름 입력 안내 라벨
            JLabel nameLabel = new JLabel("Enter a name to display to other player:", SwingConstants.CENTER);
            nameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            nameLabel.setBounds(50, 100, 300, 20);
            frame.add(nameLabel);

            // 이름 입력 필드
            JTextField nameField = new JTextField();
            nameField.setBounds(100, 130, 200, 30);
            nameField.setFont(new Font("Arial", Font.PLAIN, 14));
            frame.add(nameField);

            // 버튼 패널
            JPanel buttonPanel = new JPanel();
            buttonPanel.setBounds(100, 180, 200, 50);
            buttonPanel.setLayout(new GridLayout(1, 2, 10, 0));

            // EXIT 버튼
            JButton exitButton = new JButton("EXIT");
            exitButton.setFont(new Font("Arial", Font.BOLD, 14));
            exitButton.setBackground(new Color(150, 75, 75));
            exitButton.setForeground(Color.WHITE);
            exitButton.addActionListener(e -> System.exit(0));
            buttonPanel.add(exitButton);

            // SUBMIT 버튼
            JButton submitButton = new JButton("SUBMIT");
            submitButton.setFont(new Font("Arial", Font.BOLD, 14));
            submitButton.setBackground(new Color(0, 123, 255));
            submitButton.setForeground(Color.WHITE);
            submitButton.addActionListener(e -> {
                String playerName = nameField.getText().trim();
                if (!playerName.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Welcome, " + playerName + "!");
                    frame.dispose(); // 닫기
                    new GameScreen(playerName); // 게임 화면으로 이동
                } else {
                    JOptionPane.showMessageDialog(frame, "Please enter a valid name.");
                }
            });
            buttonPanel.add(submitButton);

            frame.add(buttonPanel);
            frame.setVisible(true);
        });
    }
}
