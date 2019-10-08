package simple;
import simple.Cage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class LocalSearch {
	static int localSearchIterationCount = 0; // Global variable to count iterations when simple solution solver is
												// called

	// runs the program and handles stdin and stdout
	public static void main(String[] args) {
		// First, read the entire input file into a list
		List<String> input = readFileInList(
				"/Users/bryantruong/OneDrive - University of North Carolina at Chapel Hill/Documents/Fall 2019/COMP560/comp560a1/src/simple/testCase.txt");
		int n = Integer.parseInt(input.get(0));
		// This will be the n x n matrix of chars with the cage info
		char[][] cagesMatrix = new char[n][n];
		Hashtable<Character, Cage> cagesHashTable = new Hashtable<Character, Cage>();
		for (int i = 1; i < input.size(); i++) {
			String currentString = input.get(i);
			if (!(currentString.contains(":"))) {
				// input line is part of the KenKen board
				char[] stringToCharArray = currentString.toCharArray();
				cagesMatrix[i - 1] = stringToCharArray;
			} else {
				// input line is an operator rule for a cage
				String[] splitStringArray = currentString.split(":");
				char cageKey = splitStringArray[0].charAt(0);
				if (splitStringArray[1].length() > 1) {
					int goalValue = Integer
							.parseInt(splitStringArray[1].substring(0, splitStringArray[1].length() - 1));
					char operation = splitStringArray[1].charAt(splitStringArray[1].length() - 1);
					cagesHashTable.put((Character) cageKey, new Cage(goalValue, operation));

				} else {
					char operation = '!';
					int goalValue = Integer.parseInt(splitStringArray[1].substring(0));
					cagesHashTable.put((Character) cageKey, new Cage(goalValue, operation));
				}
			}
		}
		// Use cagesMatrix and update the Cage objects' location properties
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				char currentCage = cagesMatrix[i][j];
				Cage matchingCageObject = cagesHashTable.get((Character) currentCage);
				matchingCageObject.addLocations(new int[] { i, j });
			}
		}

		// generate matrix
		int[][] solutionMatrix = generateEmptyMatrix(n);
		// fill with values 1-n for future swap
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				for (int k = 1; k <= n; k++) {
					solutionMatrix[i][j] = k;
				}
			}
		}
		// call localSearch
		boolean wasSuccessful = localSearch(cagesMatrix, cagesHashTable, n, solutionMatrix);
		System.out.println("Local search was successful? " +wasSuccessful);
		print2DArray(solutionMatrix);
		System.out.println("\n");
		System.out.println(localSearchIterationCount);

	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// SOLVING METHOD
	//////////////////////////////////////////////////////////////////////////////////////////

	// public static boolean localSearch(char[][] cagesMatrix, Hashtable<Character,
	// Cage> cagesHashTable, int n,
	// int[][] matrix) {
	// if (localSearchIterationCount > 15000) {
	// return false;
	// }
	// for (int i = 0; i < n; i++) {
	// for (int j = 0; j < n; j++) {
	// // Increase iteration count
	// localSearchIterationCount++;
	// if (checkRowsColumns(matrix, n, i, j, matrix[i][j])
	// && checkCages(matrix, cagesHashTable, cagesMatrix, i, j, matrix[i][j])) {
	// return true;
	// } else {
	// randomSwap(matrix, n);
	// return localSearch(cagesMatrix, cagesHashTable, n, matrix);
	// }
	// }
	// }
	// return true;
	// }

	public static boolean localSearch(char[][] cagesMatrix, Hashtable<Character, Cage> cagesHashTable, int n,
			int[][] matrix) {
		//Init the matrix with random values 1-n
		randomInitMatrix(matrix, n);
		while (localSearchIterationCount < 15000) {
			// Check to see if everything is valid
			checkingMatrixAndSwappingIfNeeded: for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					if (checkRowsColumns(matrix, n, i, j, matrix[i][j])
							&& checkCages(matrix, cagesHashTable, cagesMatrix, i, j, matrix[i][j])) {
						return true;
					} else {
						// If something in the matrix was invalid, randomly swap and restart the
						// checking process
						randomSwap(matrix, n);
						localSearchIterationCount++;
						break checkingMatrixAndSwappingIfNeeded;
					}
				}
			}
		}
		// If we have broken out of the while loop, that means that we have swapped 1500
		// times and still haven't completed the puzzle.
		return false;
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// HELPER METHODS
	//////////////////////////////////////////////////////////////////////////////////////////

	public static int[][] generateEmptyMatrix(int n) {
		// initializing future solution array to be all 0
		int[][] solution = new int[n][n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				solution[i][j] = 0;
			}
		}
		return solution;
	}

	public static boolean checkRowsColumns(int[][] matrix, int n, int row, int col, int num) {
		for (int i = 0; i < n; i++) {
			if (matrix[row][i] == num) {
				return false;
			}
		}
		for (int i = 0; i < n; i++) {
			if (matrix[i][col] == num) {
				return false;
			}
		}
		return true;
	}

	public static boolean checkCages(int[][] matrix, Hashtable<Character, Cage> cagesHashTable, char[][] cagesMatrix,
			int row, int col, int num) {
		int temp = matrix[row][col];
		matrix[row][col] = num;
		Cage current = cagesHashTable.get((Character) cagesMatrix[row][col]);
		ArrayList<int[]> locations = current.getLocations();
		char op = current.getOp();
		int goal = current.getGoal();
		if (op == '+') {
			int sum = 0;
			for (int[] pair : locations) {
				if (matrix[pair[0]][pair[1]] == 0) {
					return true;
				}
				sum += matrix[pair[0]][pair[1]];
			}
			if (sum == goal) {
				return true;
			}
			matrix[row][col] = temp;
			return false;
		} else if (op == '-') {
			int[] pairOne = locations.get(0);
			int[] pairTwo = locations.get(1);
			if (matrix[pairOne[0]][pairOne[1]] == 0 || matrix[pairTwo[0]][pairTwo[1]] == 0) {
				return true;
			}
			if ((matrix[pairOne[0]][pairOne[1]] - matrix[pairTwo[0]][pairTwo[1]] == goal)
					|| (matrix[pairTwo[0]][pairTwo[1]] - matrix[pairOne[0]][pairOne[1]] == goal)) {
				return true;
			}
			matrix[row][col] = temp;
			return false;
		} else if (op == '*') {
			int prod = 1;
			for (int[] pair : locations) {
				if (matrix[pair[0]][pair[1]] == 0) {
					return true;
				}
				prod *= matrix[pair[0]][pair[1]];
			}
			if (prod == goal) {
				return true;
			}
			matrix[row][col] = temp;
			return false;
		} else if (op == '/') {
			int[] pairOne = locations.get(0);
			int[] pairTwo = locations.get(1);
			if (matrix[pairOne[0]][pairOne[1]] == 0 || matrix[pairTwo[0]][pairTwo[1]] == 0) {
				return true;
			}
			if ((matrix[pairOne[0]][pairOne[1]] / matrix[pairTwo[0]][pairTwo[1]] == goal)
					|| (matrix[pairTwo[0]][pairTwo[1]] / matrix[pairOne[0]][pairOne[1]] == goal)) {
				return true;
			}
			matrix[row][col] = temp;
			return false;
		} else if (op == '!') {
			if (num == goal) {
				return true;
			}
			matrix[row][col] = temp;
			return false;
		}
		matrix[row][col] = temp;
		return false;
	}

	// Helper function to read a file into a list, with each element in the list
	// being a line from the .txt file
	static List<String> readFileInList(String fileName) {
		List<String> lines = Collections.emptyList();
		try {
			lines = Files.readAllLines(Paths.get(fileName), StandardCharsets.UTF_8);
		} catch (IOException e) {
			// Just print exception
			e.printStackTrace();
		}
		return lines;
	}

	// Helper function to print the 2D array
	static void print2DArray(int[][] solutionArray) {
		for (int i = 0; i < solutionArray.length; i++) { // this equals to the row in our matrix.
			for (int j = 0; j < solutionArray[i].length; j++) { // this equals to the column in each row.
				System.out.print(solutionArray[i][j] + " ");
			}
			System.out.println(); // change line on console as row comes to end in the matrix.
		}
	}

	/**
	 * Helper function to make a random swap of cells
	 * 
	 * @param matrix The solution matrix
	 * @param n      The dimensions of the array
	 */
	static void randomSwap(int[][] matrix, int n) {
		int random1 = (int) Math.random() * 10;
		int randomRow = (int) Math.random() * 10 % n;
		int randomCol = (int) Math.random() * 10 % n;
		// if odd, swap rows
		if (random1 % 2 == 0) {
			int temp = matrix[randomRow][randomCol];
			// if random row == n - 1, then circle back to 0 for the swap
			if (randomRow + 1 == n) {
				matrix[randomRow][randomCol] = matrix[0][randomCol];
				matrix[0][randomCol] = temp;
			} else {
				matrix[randomRow][randomCol] = matrix[randomRow + 1][randomCol];
				matrix[randomRow + 1][randomCol] = temp;
			}
		} else { // if even, swap columns
			int temp = matrix[randomRow][randomCol];
			// if random col == n - 1, then circle back to 0 for the swap
			if (randomRow + 1 == n) {
				matrix[randomRow][randomCol] = matrix[randomRow][0];
				matrix[randomRow][0] = temp;
			} else {
				matrix[randomRow][randomCol] = matrix[randomRow][randomCol + 1];
				matrix[randomRow][randomCol + 1] = temp;
			}
		}
	}

	/**
	 * Randomly initializes the matrix with cells from 1-9 
	 * @param matrix
	 * @param n
	 */
	static void randomInitMatrix(int[][] matrix, int n) {
		for (int i =0; i < n; i++){
			for (int j = 0; j < n; j++){
				//Generate a new random number to place
				Random r = new Random();
				int low = 1;
				int high = n + 1; //The random is between low (inclusive) and high (exclusive)
				int randomNumber = r.nextInt(high-low) + low;
				matrix[i][j] = randomNumber;
			}
		}
	}

}
