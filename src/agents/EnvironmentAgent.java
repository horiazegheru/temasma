package agents;

import agents.utils.GridPosition;
import agents.utils.Hole;
import agents.utils.Tile;
import jade.core.Agent;

import java.util.ArrayList;

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

        System.out.println("my name is " + name);
        System.out.println("agentsNr = " +  agentsNr);
        System.out.println("operationTime = " + operationTime);
        System.out.println("totalTime = " + totalTime);
        System.out.println("width = " + width);
        System.out.println("height = " + height);
        System.out.println("obstacles = " + obstacles);
        System.out.println("tiles = " + tiles);
        System.out.println("holes = " + holes);
    }

    @Override
    protected void takeDown()
    {
    }
}
