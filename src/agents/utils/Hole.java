package agents.utils;

import java.io.Serializable;

public class Hole implements Serializable {
	public int depth;
	public String color;
	public GridPosition pos;

	public Hole(int depth, String color, GridPosition pos) {
		this.depth = depth;
		this.color = color;
		this.pos = pos;
	}

	public String toString() {
		return this.depth + " " + this.color + " " + this.pos;
	}
}
