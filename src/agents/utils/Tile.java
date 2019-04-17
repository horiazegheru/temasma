package agents.utils;

public class Tile {
	int count;
	String color;
	GridPosition pos;

	public Tile(int count, String color, GridPosition pos) {
		this.count = count;
		this.color = color;
		this.pos = pos;
	}

	public String toString() {
		return this.count + " " + this.color + " " + this.pos;
	}
}
