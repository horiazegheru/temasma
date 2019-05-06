package agents.utils;

import agents.model.Graph;
import jade.core.AID;

import java.io.Serializable;
import java.util.ArrayList;

public class Perception implements Serializable {
    public int operationTime;
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
    public Graph graph;

    public Perception(int operationTime, int width, int height, ArrayList<GridPosition> obstacles,
                      ArrayList<Tile> tiles, ArrayList<Hole> holes, int points, GridPosition pos, Tile currentTile,
                      String error, ArrayList<AID> otherAgentAIDs, Graph graph) {

        this.operationTime = operationTime;
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
        this.graph = graph;
    }

    public String toString() {
        return "PERCEPTION ===> operationTime = " + operationTime + ", width = " + width + ", height = " + height + ", obstacles = " +
                obstacles + ", tiles = " + tiles + ", holes = " + holes + ", points = " + points + ", pos = " + pos +
                ", currentTile = " + currentTile + ", error = " + error + ", other agents = " + otherAgentAIDs;
    }
}
