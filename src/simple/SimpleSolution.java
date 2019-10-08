package simple;

import simple.Cage;

import java.util.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.io.*;

public class SimpleSolution {
	static int simpleSolutionIterationCount = 0; //Global variable to count iterations when simple solution solver is called

	/**
	 * STEPS: 1. Start at the first (top left) cell in the grid, move left to right
	 * (or up/down) NOTE: For our "best" backtracking search solution, instead of
	 * starting at the top left cell, we should choose the Most Constrained Variable
	 * (grid slots that have the fewest remaining possible values, i.e. grids in
	 * 1-square cages) 
	 * 
	 * 2. Place a number from 1 to n in the cell until a valid
	 * number is found or until the number has exceeded n 
	 * 
	 * 3. If the number for the
	 * cell is valid, repeat steps 1 and 2 across the row 
	 * 
	 * 4. If the number has exceeded N and no number from 1 to N is valid, backtrack to the previous cell
	 * and try the next possible number 
	 * 
	 * 5. If there are no more empty cells, the solution has been found The initial
	 * solve() call on the stack will resolve to true
	 * 6. To print the output use the helper method
	 */

	// runs the program and handles stdin and stdout
	public static void main(String[] args) {
		// First, read the entire input file into a list
		List<String> input = readFileInList(
				"/Users/bryantruong/git/comp560a1/src/simple/testCase.txt");
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
					int goalValue = Integer.parseInt(splitStringArray[1].substring(0, splitStringArray[1].length() - 1));
					char operation = splitStringArray[1].charAt(splitStringArray[1].length() - 1);
					cagesHashTable.put((Character) cageKey, new Cage(goalValue, operation));

				}
				else {
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
		
		
		/**
		 * Begin Simple Backtracking Search Solution
		 */
		// generate matrix before calling simpleSolveBackTracking
		int[][] solutionOne = generateEmptyMatrix(n);
		simpleSolveBackTracking(cagesMatrix, cagesHashTable, n, solutionOne);
		print2DArray(solutionOne);
		System.out.println("\n");
		System.out.println(simpleSolutionIterationCount);
		
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// SOLVING METHOD
	//////////////////////////////////////////////////////////////////////////////////////////

	public static boolean simpleSolveBackTracking(char[][] cagesMatrix, Hashtable<Character, Cage> cagesHashTable, int n, int[][] matrix) {
		// starting backtracking process
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				// check to see if the spot hasn't been filled yet
				if (matrix[i][j] == 0) {
					for (int num = 1; num <= n; num++) {
						//Increase iteration count
						simpleSolutionIterationCount++;
						if (checkRowsColumns(matrix, n, i, j, num) && checkCages(matrix, cagesHashTable, cagesMatrix, i, j, num)) {
							matrix[i][j] = num;
							if (simpleSolveBackTracking(cagesMatrix, cagesHashTable, n, matrix)) {
								return true;
							} else {
								matrix[i][j] = 0;
							}
						}
					}
					return false;
				}
			}
		}
		return true;
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

	public static boolean checkCages(int[][] matrix, Hashtable<Character, Cage> cagesHashTable, char[][] cagesMatrix, int row,
			int col, int num) {
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
	
	//Helper function to print the 2D array
	static void print2DArray(int[][] solutionArray) {
		 for (int i = 0; i < solutionArray.length; i++) {         //this equals to the row in our matrix.
	         for (int j = 0; j < solutionArray[i].length; j++) {   //this equals to the column in each row.
	            System.out.print(solutionArray[i][j] + " ");
	         }
	         System.out.println(); //change line on console as row comes to end in the matrix.
	      }
	}

}
