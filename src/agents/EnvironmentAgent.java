package agents;

import agents.behaviors.EnvironmentBehavior;
import agents.utils.GridPosition;
import agents.utils.Hole;
import agents.utils.Tile;
import jade.core.AID;
import jade.core.Agent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EnvironmentAgent extends Agent {
    String name;
    int agentsNr;
    int operationTime;
    int totalTime;
    int width;
    int height;
    ArrayList<GridPosition> obstacles = new ArrayList<>();
    ArrayList<Tile> tiles = new ArrayList<>();
    ArrayList<Hole> holes = new ArrayList<>();
    Map<AID, Integer> aidsScores = new HashMap<>();

    @Override
    protected void setup()
    {
        name = getAID().getName();
        agentsNr = (int) getArguments()[0];
        operationTime = (int) getArguments()[1];
        totalTime = (int) getArguments()[2];
        width = (int) getArguments()[3];
        height = (int) getArguments()[4];
        obstacles = (ArrayList<GridPosition>) getArguments()[5];
        tiles = (ArrayList<Tile>) getArguments()[6];
        holes = (ArrayList<Hole>) getArguments()[7];

        HashMap<AID, GridPosition> aidPositions = (HashMap<AID, GridPosition>) getArguments()[8];

        for (AID aid: aidPositions.keySet())
            aidsScores.put(aid, 0);

        addBehaviour(new EnvironmentBehavior(agentsNr, operationTime, totalTime, width, height, obstacles, tiles, holes,
                aidsScores, aidPositions));
    }

    @Override
    protected void takeDown() {
        for (AID aid: aidsScores.keySet()) {
            System.out.println("AGENT " + aid.getLocalName() + " finished with " + aidsScores.get(aid) + " points.");
        }

        System.out.println("ENV ENDED");
    }
}
