package com.bayviewglen.horseracing;

/* Name: Zane Feder
 * Course: ICS3U-AP
 * Title: Horse-Racing Assignment
 * Description: A multi-player horse racing simulation where the user can play as multiple players, bet on multiple horses, and watch them race in real time.
 * Extra Features: I included three extra features: 1. Players can bet on more than one horse. Horses have two qualities associated with them: a speed index and a variability index.  2. The higher the speed index, the faster the horse will go and the higher the variability
 * index, the less consistent the horses are.  3. After every race, the player data is saved back to the file because only saving on quit has one flaw: If the player bets, then quits without racing, they lose the money that they bet
 * and will not get it back.
 * Betting Style: Winner gets all betting funds collected from every player. 
 * If there are multiple winners they share the funds collected proportionally based on their own bet amounts. 
 * Due Date: 2/9/2015
 * Teacher: Mr. DesLauriers 
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;

public class HorseRacing {

	private static final int TRACK_LENGTH = 60;
	private static int numHorses;
	private static int[][] bets;

	public static void main(String[] args) {

		String horses[][] = getHorses();
		numHorses = horses.length;
		String players[][] = getPlayers();
		int horsesInRace[] = pickHorses(horses);
		bets = new int[players.length][horsesInRace.length];
		mainLoop(players, horsesInRace, horses);

	}

	private static void mainLoop(String[][] players, int[] horsesInRace, String[][] horses) {
		Scanner keyboard = new Scanner(System.in);
		int moneyBetOnWinningHorse;
		int totalBet;
		double payoutRatio;
		int playerNum = 0;
		int horseNum = 0;
		int winningHorse;
		final int CHOICE_BET = 0;
		final int CHOICE_SELECT = 1;
		final int CHOICE_RACE = 2;
		final int CHOICE_QUIT = 3;
		int choice = CHOICE_SELECT;
		do {
			if (choice == CHOICE_BET) {
				displayHorses(horsesInRace, horses);
				horseNum = promptHorse();
				bet(players, playerNum, horseNum, horsesInRace, horses);
			} else if (choice == CHOICE_SELECT) {
				displayPlayers(players);
				playerNum = choosePlayer(players);
			} else if (choice == CHOICE_RACE) {
				winningHorse = race(horses, horsesInRace);
				moneyBetOnWinningHorse = moneyBetOnHorse(winningHorse);
				totalBet = totalBets();
				if (moneyBetOnWinningHorse == 0) {
					payoutRatio = 0;
					System.out.println("Nobody bet on the winning horse.  The house takes all.");
				} else {
					payoutRatio = (double) totalBet / (double) moneyBetOnWinningHorse;
				}

				players = distribute(players, winningHorse, payoutRatio);
				writeToFile(players);
				horsesInRace = pickHorses(horses);
			} else if (choice == CHOICE_QUIT) {
				System.out.println("You have chosen to save and quit.");
			} else {
				System.out.println("invalid choice");
			}

			System.out.println(CHOICE_BET + ": Bet, " + CHOICE_SELECT
					+ ": Select Player, " + CHOICE_RACE + ": Race, "
					+ CHOICE_QUIT + ": Quit.  Enter choice");
			choice = keyboard.nextInt();

		} while (choice != CHOICE_QUIT);
	}

	// writes player data to file
	private static void writeToFile(String[][] players) {

		PrintWriter writer;
		try {
			writer = new PrintWriter("input/playerData.dat", "UTF-8");
			writer.println(players.length);
			for (int i = 0; i < players.length; i++) {
				writer.println(players[i][0] + " " + players[i][1]);
			}
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

	}

	private static String[][] distribute(String[][] players, int winningHorse,
			double payoutRatio) {
		double moneyDouble;
		int moneyInt;
		for (int i = 0; i < players.length; i++) {
			double winnings = bets[i][winningHorse] * payoutRatio;
			// display how much players won
			System.out.println(players[i][0] + " won " + winnings + "$");
			moneyDouble = Double.parseDouble(players[i][1]);
			// update wallets
			moneyDouble += winnings;
			moneyInt = (int) moneyDouble;
			players[i][1] = String.valueOf(moneyInt);
		}
		return players;
	}

	// returns the total bet amount for all players
	private static int totalBets() {
		int total = 0;
		for (int i = 0; i < bets[0].length; i++) {
			total += moneyBetOnHorse(i);
		}
		return total;
	}

	// returns the money bet on a specific horse
	private static int moneyBetOnHorse(int horse) {
		int money = 0;
		for (int i = 0; i < bets.length; i++) {
			money += bets[i][horse];
		}
		return money;

	}

	private static int race(String[][] horses, int[] horsesInRace) {
		int[] position = new int[horsesInRace.length];
		String separator = "-------------------------------------------------------------------------------------";
		for (int i = 0; i < position.length; i++) {
			position[i] = 0;
		}
		boolean isFinished = false;

		do {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for (int i = 0; i < position.length; i++) {
				int horseIndex = horsesInRace[i];
				int speed = Integer.parseInt(horses[horseIndex][1]);
				int variability = Integer.parseInt(horses[horseIndex][2]);
				position[i] = position[i]
						+ (Math.abs((speed - (int) (Math.random() * variability))));

				if (position[i] >= TRACK_LENGTH) {
					isFinished = true;
				}

				System.out.print(separator);
				System.out.println("");
				System.out
						.printf("%-20s %-3s", horses[horsesInRace[i]][0], "|");
				for (int k = 0; k < position[i]; k++) {
					System.out.print(" ");
				}

				System.out.println(i + 1);

			}

			System.out.println("");
		} while (!isFinished);

		int currentPosition = -1;
		int winningHorse = -1;
		int winningHorsePlusOne;
		for (int i = 0; i < position.length; i++) {
			if (position[i] > currentPosition) {
				currentPosition = position[i];
				winningHorse = i;
			}
		}
		winningHorsePlusOne = winningHorse + 1;
		System.out.println("The winning horse is horse number:" + winningHorsePlusOne + ", name:" + horses[horsesInRace[winningHorse]][0]);
		return winningHorse;

	}

	private static void bet(String[][] players, int playerNum, int horseNum,
			int[] horsesInRace, String[][] horses) {
		Scanner keyboard = new Scanner(System.in);
		System.out.println(players[playerNum - 1][0] + ", you have " + players[playerNum - 1][1] + "$.");
		int bet;

		do {
			System.out.println("How much would you like to bet on " + horses[horsesInRace[horseNum - 1]][0] + "?");

			bet = keyboard.nextInt();
		} while (bet > Integer.parseInt(players[playerNum - 1][1]));

		int playersMoney = Integer.parseInt(players[playerNum - 1][1]);
		playersMoney -= bet;
		players[playerNum - 1][1] = String.valueOf(playersMoney);
		bets[playerNum - 1][horseNum - 1] = bet;
	}

	private static int promptHorse() {
		Scanner keyboard = new Scanner(System.in);
		System.out.println("Type the number of the horse you wish to race");
		int horseNum = keyboard.nextInt();
		return horseNum;
	}

	private static int choosePlayer(String[][] players) {
		Scanner keyboard = new Scanner(System.in);
		System.out.println("Type the number of the player you want to be");
		int playerNum = keyboard.nextInt();
		return playerNum;
	}

	private static void displayPlayers(String[][] players) {
		System.out.printf("%-15s %-15s %-15s %n", "Number", "Name",  "Money");

		for (int i = 0; i < players.length; i++) {
			System.out.printf("%-15s %-15s %-15s %n", i + 1, players[i][0], players[i][1]);
		}

	}

	private static String[][] getPlayers() {
		String[][] players = null;
		try {
			Scanner scanner = new Scanner(new File("input/playerData.dat"));
			int numPlayers = Integer.parseInt(scanner.nextLine());
			players = new String[numPlayers][2];

			for (int i = 0; i < numPlayers; i++) {
				String temp = scanner.nextLine();
				String[] str_array = temp.split(" ");
				players[i][0] = str_array[0];
				players[i][1] = str_array[1];
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return players;
	}

	private static int[] pickHorses(String[][] horses) {
		int horsesInRace[] = new int[8];

		for (int i = 0; i < horsesInRace.length; i++) {

			int randomHorse;
			do {
				randomHorse = (int) (Math.random() * numHorses + .499);
			} while (alreadyInRace(randomHorse, horsesInRace));

			horsesInRace[i] = randomHorse;

		}
		return horsesInRace;

	}

	private static void displayHorses(int[] horsesInRace, String[][] horses) {
		System.out.printf("%-15s %-25s %-15s %-15s %n", "Number", "Horse Name", "Speed", "Variability");
		for (int i = 0; i < horsesInRace.length; i++) {
			System.out.printf("%-15s %-25s %-15s %-15s %n", i + 1, horses[horsesInRace[i]][0], horses[horsesInRace[i]][1], horses[horsesInRace[i]][2]);
		}
	}

	private static String[][] getHorses() {
		String[][] horses = null;
		try {
			Scanner scanner = new Scanner(new File("input/horseData.dat"));
			int numHorses = Integer.parseInt(scanner.nextLine());
			horses = new String[numHorses][3];

			for (int i = 0; i < numHorses; i++) {
				String nextLine = scanner.nextLine();

				horses[i][0] = nextLine;
				horses[i][1] = String.valueOf((int) (Math.random() * 10));
				horses[i][2] = String.valueOf((int) (Math.random() * 10));
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return horses;
	}

	public static boolean alreadyInRace(int horse, int[] horsesInRace) {

		for (int i = 0; i < horsesInRace.length; i++) {
			if (horsesInRace[i] == horse) {
				return true;
			}
		}

		return false;
	}
}