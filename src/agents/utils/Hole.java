package agents.utils;

public class Hole {
	int depth;
	String color;
	GridPosition pos;

	public Hole(int depth, String color, GridPosition pos) {
		this.depth = depth;
		this.color = color;
		this.pos = pos;
	}

	public String toString() {
		return this.depth + " " + this.color + " " + this.pos;
	}
}
