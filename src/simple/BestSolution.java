package simple;

import simple.Cage;

import java.util.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.io.*;

public class BestSolution {
	static int betterSolutionIterationCount = 0; //Global variable to count iterations when simple solution solver is called

	/**
	 * Improves upon the simple solution by selecting the unfilled box with the
	 * minimum remaining legal values (MRV), as opposed to simply selecting unfilled
	 * boxes from left to right, top down
	 * 
	 */

	public static void main(String[] args) {
		// First, read the entire input file into a list
		List<String> input = readFileInList("/Users/bryantruong/git/comp560a1/src/simple/testCase.txt");
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

		int[][] solution = generateEmptyMatrix(n);
		int[][][] domains = generateDomainMatrix(n);
		bestSolveBackTracking(cagesMatrix, cagesHashTable, n, solution, domains);
		print2DArray(solution);
		System.out.println("\n");
		System.out.println(betterSolutionIterationCount);
		

	} // end main

	//////////////////////////////////////////////////////////////////////////////////////////
	// SOLVING METHOD
	//////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Outline
	 * 
	 * 
	 * 1. Find the most constrained cell. This will be our current cell if
	 * findMostConstrainedCell returns [0,0] we know that we are done 2. Loop over
	 * each possible value in the current cell's domain 3. Place the possible value
	 * into the int matrix 3. Check to see if it violates any constraints 4. If it
	 * does not violate any constraints, recurse. If the recursive call returns
	 * true, return true. 5. If the puzzle does violate a constraint, empty the
	 * cell's value. We should also remove this value from the current cell's domain
	 */

	/**
	 * Recursive solve function. While the puzzle is not complete, finds the most
	 * constrained cell and uses it as the current cell we are examining. Loop over
	 * each possible value in the current cell's domain and place it in the current
	 * cell's domain. We then check to see if the placement violates any of the row,
	 * column, or cage constraints. If it did violate one of the constraints, we
	 * remove it from the cell's domain and try the next value in the current cell's
	 * domain. If it did not violate any of the constraints, mark the cell as placed
	 * in the domain array (which clears the cell's domain), and then call the solve
	 * function again. If we have checked each possible value in the current cell's
	 * domain, and all of them violate a constraint, we return false, beginning the
	 * backtracking process.
	 * 
	 * @param cagesMatrix
	 * @param cagesHashTable
	 * @param n
	 * @param solutionMatrix
	 * @param domains
	 * @return
	 */
	public static boolean bestSolveBackTracking(char[][] cagesMatrix, Hashtable<Character, Cage> cagesHashTable, int n,
			int[][] solutionMatrix, int[][][] domains) {
		

		if (isComplete(solutionMatrix, cagesHashTable, cagesMatrix)){
			return true;
		} else { 
			int[] mostConstrainedCellIndices = findMostConstrainedCell(domains, n, solutionMatrix);
			int mostConstrainedCellRow = mostConstrainedCellIndices[0];
			int mostConstrainedCellCol = mostConstrainedCellIndices[1];
			ArrayList<Integer> possibleValues = getDomain(domains, mostConstrainedCellIndices[0],
					mostConstrainedCellIndices[1], n, solutionMatrix);
			for (Integer possibleValue : possibleValues) {
				betterSolutionIterationCount++;
				// Attempt to place the possible value from the domain
				solutionMatrix[mostConstrainedCellIndices[0]][mostConstrainedCellIndices[1]] = possibleValue;
				if (checkRowsColumns(solutionMatrix, n, mostConstrainedCellIndices[0], mostConstrainedCellIndices[1], possibleValue)
						&& checkCages(solutionMatrix, cagesHashTable, cagesMatrix,
								mostConstrainedCellIndices[0], mostConstrainedCellIndices[1], possibleValue)) {
					// Recurse
					if (bestSolveBackTracking(cagesMatrix, cagesHashTable, n, solutionMatrix, domains)) {
						return true;
					} 
					else {
						for (int k = 0; k < n; k++) {
							domains[mostConstrainedCellIndices[0]][mostConstrainedCellIndices[1]][k] = k + 1;
							for (int l = 0; l < n; l++) {
								domains[l][mostConstrainedCellCol][k] = k + 1;
							}
							
							// reset each row same as current cell to regular domain
							for (int l = 0; l < n; l++) {
								domains[mostConstrainedCellRow][l][k] = k + 1;
							}

						}
						solutionMatrix[mostConstrainedCellIndices[0]][mostConstrainedCellIndices[1]] = 0;
					}
				} else {
					for (int k = 0; k < n; k++) {
						//Reset the failed cell's domain
						domains[mostConstrainedCellRow][mostConstrainedCellCol][k] = k + 1;
						//reset each column same as current cell to regular domain
						for (int l = 0; l < n; l++) {
							domains[l][mostConstrainedCellCol][k] = k + 1;
						}
						
						// reset each row same as current cell to regular domain
						for (int l = 0; l < n; l++) {
							domains[mostConstrainedCellRow][l][k] = k + 1;
						}
					}
					solutionMatrix[mostConstrainedCellIndices[0]][mostConstrainedCellIndices[1]] = 0;
				}
			}
			return false;
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// HELPER METHODS
	//////////////////////////////////////////////////////////////////////////////////////////
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

	public static boolean checkRows(int[][] matrix) {
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix.length; j++) {
				int temp = matrix[i][j]; // The matrix value we are checking against
				for (int k = 0; k < matrix.length; k++) {
					// For each element in temp's row, check to see if its value equals temp
					if (matrix[i][k] == temp && k != j && temp != 0) {
						return false;
					}
				}
			}
		}

		return true;
	}

	public static boolean checkColumns(int[][] matrix) {
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix.length; j++) {
				int temp = matrix[j][i]; // The matrix value we are checking against
				for (int k = 0; k < matrix.length; k++) {
					//For each element in temp's column, check to see if its value equals temp
					if (matrix[k][i] == temp && k != j && temp != 0) {
						return false;
					}
				}
			}
		}

		return true;
	}
	
	//BT copied this over from simple solution for simplicity
	/**
	 * 
	 * @param solutionMatrix The solution matrix
	 * @param n The number of rows/columns in solutionMatrix
	 * @param row The row index to examine
	 * @param col The column index to examine
	 * @param num The newly placed number
	 * @return
	 */
	public static boolean checkRowsColumns(int[][] solutionMatrix, int n, int row, int col, int num) {
		//Check to see that the row is valid
		for (int i = 0; i < n; i++) {
			if (i != col && solutionMatrix[row][i] == num) { //Check that i!= col since we just placed num at row, column
				return false;
			}
		}
		//Check to see if the column is valid
		for (int i = 0; i < n; i++) {
			if (i != row && solutionMatrix[i][col] == num) { //Check that i!= row since we just placed num at row, column
				return false;
			}
		}
		return true;
	}
	

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

	public static int[][][] generateDomainMatrix(int n) {

		// a 2d array of arrays where the 3rd dimension is domain, 1-n
		// if an element is removed from domain, we change this at that index to be 0
		// if domain is all 0, then the value has been placed in regular 2d matrix
		// will make a separate get length function that returns # elements != 0

		int[][][] domains = new int[n][n][n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				for (int k = 0; k < n; k++) {
					domains[i][j][k] = k + 1;
				}
			}
		}
		return domains;
	}

	public static boolean isComplete(int[][] matrix, Hashtable<Character, Cage> cagesHashTable, char[][] cagesMatrix) {
		if (!checkRows(matrix) || !checkColumns(matrix)) {
			return false;
		}
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix.length; j++) {
				if (matrix[i][j] == 0) {
					return false;
				}
				if (!checkCages(matrix, cagesHashTable, cagesMatrix, i, j, matrix[i][j])) {
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * Returns an ArrayList of the domain for the given cell, as specified by the
	 * row/column parameters. This method just looks through and assembles an
	 * ArrayList from the domain matrix, ignoring the 0's. This makes for easier
	 * computations to find the most constrained cell, as ArrayLists are mutable and
	 * have size() methods.
	 * 
	 * @param domainMatrix:
	 *            The actual 2-D Array of Arrays, which holds the array of possible
	 *            values for each cell
	 * @param row:
	 *            The row index of the cell to get domain for
	 * @param column:
	 *            The column index of the cell to get the domain for
	 * @param n:
	 *            The number of rows/columns in the KenKen puzzle
	 * @param solutionMatrix:
	 *            The matrix that will store the solved KenKen puzzle
	 * @return An arrayList of the Domain (all the possible values, 1 through n) for
	 *         that cell
	 */
	public static ArrayList<Integer> getDomain(int[][][] domainMatrix, int row, int column, int n,
			int[][] solutionMatrix) {
		// Logic to adjust a cell's domain for when a number has already been placed
		// into a cell
		if (solutionMatrix[row][column] != 0) {
			for (int k = 0; k < n; k++) {
				domainMatrix[row][column][k] = 0;
			}
		} else {

			// Get the used numbers in that cell's row, which we will prune from this cell's
			// domain
			// I will later get union with the column values into this
			ArrayList<Integer> usedNumbers = new ArrayList<Integer>();
			for (int i = 0; i < n; i++) {
				if (solutionMatrix[row][i] != 0) { // A nonzero value means a value has been placed
					usedNumbers.add(solutionMatrix[row][i]);
				}
			}
			// Get the used numbers in that cell's column, which we will prune from this
			// cell's domain
			ArrayList<Integer> usedNumbersInColumn = new ArrayList<Integer>();
			for (int i = 0; i < n; i++) {
				if (solutionMatrix[i][column] != 0) { // A nonzero value means a value has been placed
					usedNumbersInColumn.add(solutionMatrix[i][column]);
				}
			}
			// Get the union of the two, merged into usedNumbers
			for (Integer intFromColumns : usedNumbersInColumn) {
				if (!usedNumbers.contains(intFromColumns)) // If this number in the column wasn't in the row
					usedNumbers.add(intFromColumns);
			}

			// Now get the set difference between currentDomain and the usedNumbers
			// To do this, loop through the currentDomain (domainMatrix[row][column])
			// If the number in currentDomain is in usedNumbers, set that to zero
			// to indicate that it is pruned
			for (int i = 0; i < n; i++) {
				int numInCurrentDomain = domainMatrix[row][column][i];
				for (int numInUsedNumbers : usedNumbers) {
					if (numInCurrentDomain == numInUsedNumbers) {
						domainMatrix[row][column][i] = 0; // Prune the domain
					}
				}
			}
		}
		// TODO: Here, we would prune each variable's domain even further 
		// depending on the operation for the cage that the cell belongs to
		
		// -For addition cages, prune values from the domain that if added
		//	to the current sum of the cage (and if applicable,
		//	other cage cells' possible values) would exceed the target sum
		
		// -For subtraction cells (which will be 2 cells long, as specified 
		//  during class), prune values whose absolute value when subtracted
		//  with the other cage cells' value/possible values does not
		//  equal the target difference.
		
		// -For multiplication cells, prune values from the domain whose 
		//	result when applying modulus with the target does not result in zero

		// -For division cells, (which will be 2 cells long, as specified 
		//  during class) prune values whose quotient with the other 
		//	cell in the cage's value/possible values does not equal
		//  the target quotient.
		
		ArrayList<Integer> domainList = new ArrayList<Integer>();

		// This for loop simply removes the zeros from the domains and assembles them
		// into an Array List
		for (int k = 0; k < n; k++) {
			if (domainMatrix[row][column][k] != 0) {
				domainList.add((Integer) domainMatrix[row][column][k]);
			}
		}
		return domainList;
	}

	/**
	 * Returns the first index of the most constrained variable (If there are
	 * multiple cells with the same number of constraints, it will return the index
	 * that came first, as we traverse left to right, top down). This will only
	 * return indices that have at least one or more minimum remaining values, as
	 * indices that have zero remaining values should not be considered.
	 * 
	 * @param domainMatrix:
	 *            The matrix that contains the possible values for each cell
	 * @param n:
	 *            The number of rows and columns
	 * @param solutionMatrix:
	 *            The matrix of the integers values, which eventually will be
	 *            completely filled
	 * @return An array of length 2, denoting the indices of the most constrained
	 *         cell.
	 */
	public static int[] findMostConstrainedCell(int[][][] domainMatrix, int n, int[][] solutionMatrix) {
		int[] indexOfMostConstrainedCell = new int[] { 0, 0 };
		int minDomainLength = Integer.MAX_VALUE;
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				int currentDomainLength = getDomain(domainMatrix, i, j, n, solutionMatrix).size();
				if (currentDomainLength > 0 && currentDomainLength < minDomainLength) {
					minDomainLength = currentDomainLength;
					indexOfMostConstrainedCell[0] = i;
					indexOfMostConstrainedCell[1] = j;
				}
			}
		}
		return indexOfMostConstrainedCell;
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
}
