package game;

import java.util.*;

public class Dice {
	private Random random = new Random();
	private int[] dice = new int[5];
	
	public int[] rollDice() {
		for(int i = 0 ; i < 5 ; i++) {
			if(dice[i] == 0) {	// Roll the dice which is not kept
				dice[i] = random.nextInt(6) + 1;
			}
		}
		return dice;
	}
	
	public void keepDice(String input) {
		if(input.isEmpty()) return;
		Arrays.fill(dice, 0);
		for(String s : input.split(",")) {
			int index = Integer.parseInt(s.trim()) - 1;
			dice[index] = 0;
		}
	}
	
	public int[] getCurrentRoll() {
		return dice;
	}

}
