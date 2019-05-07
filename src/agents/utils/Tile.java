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
		return "cate " +  this.count + " cul " + this.color + " pos " + this.pos;
	}

	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof Tile)) {
			return false;
		}
		Tile cc = (Tile)o;
		return cc.color == color && cc.pos == pos;
	}

	public int hashCode() {
		int result = 17;
		result = 31 * result + color.length() + pos.x + pos.y;
		return result;
	}
}
