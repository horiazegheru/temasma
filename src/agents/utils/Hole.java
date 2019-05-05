package agents.utils;

import java.io.Serializable;
import java.util.Objects;

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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Hole hole = (Hole) o;
		return depth == hole.depth &&
				Objects.equals(color, hole.color) &&
				Objects.equals(pos, hole.pos);
	}

	@Override
	public int hashCode() {
		return Objects.hash(depth, color, pos);
	}
}
