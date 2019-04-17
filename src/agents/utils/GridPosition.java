package agents.utils;

public class GridPosition{
	int x;
	int y;

	public GridPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public String toString() {
		return this.x + " " + this.y;
	}
}
