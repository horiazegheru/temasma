package agents.utils;

import jade.core.AID;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;

public class Perception implements Serializable {
    public int currentTime;
    public int width;
    public int height;
    public ArrayList<GridPosition> obstacles = new ArrayList<>();
    public ArrayList<Tile> tiles = new ArrayList<>();
    public ArrayList<Hole> holes = new ArrayList<>();
    public int points;
    public GridPosition pos;
    public Tile currentTile;
    public String error;
    public ArrayList<AID> otherAgentAIDs;

    public Perception(int currentTime, int width, int height, ArrayList<GridPosition> obstacles,
                      ArrayList<Tile> tiles, ArrayList<Hole> holes, int points, GridPosition pos, Tile currentTile,
                      String error, ArrayList<AID> otherAgentAIDs) {

        this.currentTime = currentTime;
        this.width = width;
        this.height = height;
        this.obstacles = obstacles;
        this.tiles = tiles;
        this.holes = holes;
        this.points = points;
        this.pos = pos;
        this.currentTile = currentTile;
        this.error = error;
        this.otherAgentAIDs = otherAgentAIDs;
    }

    public String toString() {
        return "PERCEPTION ===> currentTime = " + currentTime + ", width = " + width + ", height = " + height + ", obstacles = " +
                obstacles + ", tiles = " + tiles + ", holes = " + holes + ", points = " + points + ", pos = " + pos +
                ", currentTile = " + currentTile + ", error = " + error + ", other agents = " + otherAgentAIDs;
    }
}
