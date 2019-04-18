package agents.utils;

import java.io.Serializable;

public class Tile implements Serializable {
	public int count;
	public String color;
	public GridPosition pos;

	public Tile(int count, String color, GridPosition pos) {
		this.count = count;
		this.color = color;
		this.pos = pos;
	}

	public String toString() {
		return this.count + " " + this.color + " " + this.pos;
	}
}
