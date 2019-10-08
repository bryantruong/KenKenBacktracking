package simple;

import java.util.ArrayList;

public class Cage {
	private int goal;
	private char operation;
	private ArrayList<int[]> locations; 
	
	public Cage(int goal, char operation) {
		this.goal = goal;
		this.operation = operation;
		this.locations = new ArrayList<int[]>();
	}
	public void addLocations(int[] locationsToAdd) {
		locations.add(locationsToAdd);
	}
	
	public ArrayList<int[]> getLocations() {
		return locations;
	}
	
	public char getOp() {
		return operation;
	}
	
	public int getGoal() {
		return goal;
	}
	
}
