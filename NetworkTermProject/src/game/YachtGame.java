package game;

import java.io.*;
import java.util.*;

public class YachtGame {
	private Dice dice = new Dice();
	private ScoreSheet scoreSheet = new ScoreSheet();
	
	public void startGame(BufferedReader in, PrintWriter out) throws IOException{
		out.println("Game Start. Roll the 5 dice.");
		for(int i = 0 ; i < 3 ; i++) {
			out.println("Roll #" + (i + 1));
			int[] rolls = dice.rollDice();
			out.println("Dice result: " + Arrays.toString(rolls));
			
			// Keep the dice and roll the dice again
			if(i < 2) {
				out.println("Number of dice to roll again / or Enter for next step: ");
				String input = in.readLine();
				dice.keepDice(input);
			}
		}
		
		// Record the score
		scoreSheet.recordScore(dice.getCurrentRoll(), in, out);
	}
}
