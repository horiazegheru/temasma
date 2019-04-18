package agents.utils;

import java.io.Serializable;

public class GridPosition implements Serializable {
	public int x;
	public int y;

	public GridPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public String toString() {
		return this.x + " " + this.y;
	}

	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof GridPosition)) {
			return false;
		}
		GridPosition cc = (GridPosition)o;
		return cc.x == x && cc.y == y;
	}

	public int hashCode() {
		int result = 17;
		result = 31 * result + x;
		return result;
	}

}
