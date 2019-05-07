package agents.behaviors;

import FIPA.DateTime;
import agents.MyAgent;
import agents.disjktra.GraphNou;
import agents.model.Graph;
import agents.utils.*;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class EnvironmentBehavior extends CyclicBehaviour {
    static final String AGENTPERCEPT_PROTOCOL = "sendpercept";
    static final String AGENTTERMINATE_PROTOCOL = "terminate";
    private static final String EMPTY_CELL = "|        |";
    private static final String OBSTACLE = "|////////|";
    private static final int TIMER_DELAY = 1000;

    private MessageTemplate msgTemplate			= MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            MessageTemplate.MatchProtocol(AgentBehavior.ENVACTION_PROTOCOL));

    int agentsNr;
    int operationTime;
    int totalTime;
    int width;
    int height;
    ArrayList<GridPosition> obstacles = new ArrayList<>();
    ArrayList<Tile> tiles = new ArrayList<>();
    ArrayList<Hole> holes = new ArrayList<>();
    Map<AID, Integer> aidsScores = new HashMap<>();
    Map<AID, GridPosition> aidPositions = new HashMap<>();
    Map<AID, Tile> aidTiles = new HashMap<>();
    Map<AID, String> aidErrors = new HashMap<>();
    Map<AID, Perception> aidPerceptions = new HashMap<>();
    ArrayList<AID> otherAgents = new ArrayList<>();
    boolean firstPerceptionRound = false;
    GraphNou graph;

    long startTime;
    long currentTime;
    Timer timer = new Timer();

    public EnvironmentBehavior(int agentsNr, int operationTime, int totalTime, int width, int height,
            ArrayList<GridPosition> obstacles, ArrayList<Tile> tiles, ArrayList<Hole> holes, Map<AID, Integer> aidsScores,
            Map<AID, GridPosition> aidPositions, long startTime, GraphNou graph) {

        this.agentsNr = agentsNr;
        this.operationTime = operationTime;
        this.totalTime = totalTime;
        this.width = width;
        this.height = height;
        this.obstacles = obstacles;
        this.tiles = tiles;
        this.holes = holes;
        this.aidsScores = aidsScores;
        this.aidPositions = aidPositions;
        this.startTime = startTime;
        this.graph = graph;
        for (AID aid: aidsScores.keySet()) {
            aidErrors.put(aid, null);
            otherAgents.add(aid);
        }

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                printGrid();
            }}, 0, TIMER_DELAY);
    }

    public void sendPerception(AID aid) throws IOException {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setProtocol(AGENTPERCEPT_PROTOCOL);
        msg.addReceiver(aid);

        // SEND PERCEPTION TO ALL AGENTS

        if (aidErrors.get(aid) != null) {
            Perception oldPerception = aidPerceptions.get(aid);
            oldPerception.error = aidErrors.get(aid);
            msg.setContentObject(oldPerception);
        } else {
            Perception perception = new Perception(operationTime, width, height, obstacles,
                    tiles, holes, aidsScores.get(aid), aidPositions.get(aid), aidTiles.get(aid),
                    aidErrors.get(aid), otherAgents, graph);

            msg.setContentObject(perception);
            aidPerceptions.put(aid, perception);
        }

        myAgent.send(msg);
    }

    public void sendTerminate() {
        for (AID aid: aidsScores.keySet()) {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.setProtocol(AGENTTERMINATE_PROTOCOL);
            msg.addReceiver(aid);
            msg.setContent("TERMINATE");
            myAgent.send(msg);
        }
    }

    public void receiveAction() throws InterruptedException, UnreadableException {
            ACLMessage receivedMsg = myAgent.receive(msgTemplate);
            if (receivedMsg != null) {
                Action action = (Action) receivedMsg.getContentObject();
                AID sender = receivedMsg.getSender();
                String errorMessage = executeAction(sender, action);
                aidErrors.put(sender, errorMessage);

                currentTime = new Date().getTime() - startTime;
                // IF TIME IS OUT, INFORM EVERYONE AND EXIT

                if (currentTime > totalTime) {
                    System.out.println("ENV SAYS TIMEOUT, TERMINATING....");
                    sendTerminate();
                    myAgent.doDelete();
                    timer.cancel();
                    timer.purge();
                }

                printGridState(currentTime, sender, action);

                try {
                    sendPerception(sender);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
    }

    public String executePick(AID sender, Action action) {
        Tile toDelete = null;
        boolean tileExists = false;
        boolean colorFound = false;
        for (Tile tile: tiles) {
            if (tile.pos.equals(aidPositions.get(sender))) {
                tileExists = true;
                if (tile.color.equals(action.arg1)) {
                    if (tile.count > 0) {
                        colorFound = true;
                        tile.count--;
                        aidTiles.put(sender, new Tile(1, tile.color, tile.pos));
                        if (tile.count == 0)
                            toDelete = tile;
                    } else {
                        return Errors.NO_TILE_AT_COORDS.getMessage();
                    }
                }
            }
        }
        if (tileExists && !colorFound) {
            return  Errors.NO_TILE_COLOR_AT_COORDS.getMessage();
        }
        if (!tileExists)
            return  Errors.NO_TILE_AT_COORDS.getMessage();

        if (toDelete != null) {
            tiles.remove(toDelete);
        }
        return null;
    }

    public String checkPosition(GridPosition pos) {
        for (GridPosition obs: obstacles) {
            if (pos.equals(obs))
                return Errors.OBSTACLE.getMessage();
        }

        for (Hole hole: holes) {
            if (pos.equals(hole.pos) && hole.depth != 0)
                return Errors.NOT_FILLED_HOLE.getMessage();
        }

        if (pos.x < 0 || pos.y < 0 || pos.x > width - 1 || pos.y > height - 1)
            return Errors.OUT_OF_MAP.getMessage();

        return null;
    }

    public String executeMove(AID sender, Action action) {
        if (action.actionName.equals("Move")) {
            GridPosition pos;
            String nullOrError;
            if (action.arg1.equals("North")) {
                pos = aidPositions.get(sender);
                pos.x--;

                nullOrError = checkPosition(pos);
                if (nullOrError != null) {
                    pos.x++;
                }
                return nullOrError;
            }
            if (action.arg1.equals("South")) {

                pos = aidPositions.get(sender);
                pos.x++;

                nullOrError = checkPosition(pos);
                if (nullOrError != null) {
                    pos.x--;
                }
                return nullOrError;
            }
            if (action.arg1.equals("East")) {
                pos = aidPositions.get(sender);
                pos.y--;

                nullOrError = checkPosition(pos);
                if (nullOrError != null) {
                    pos.y++;
                }
                return nullOrError;
            }
            if (action.arg1.equals("West")) {
                pos = aidPositions.get(sender);
                pos.y++;

                nullOrError = checkPosition(pos);
                if (nullOrError != null) {
                    pos.y--;
                }
                return nullOrError;
            }
        }

        return null;
    }

    public String executeDrop(AID sender) {
        Tile toDrop = aidTiles.get(sender);

        if (toDrop == null)
            return Errors.NOT_CARRYING.getMessage();

        GridPosition currentPos = aidPositions.get(sender);

        boolean tileExists = false;
        for (Tile tile: tiles) {
            if (tile.pos.equals(currentPos) && tile.color.equals(toDrop.color)) {
                tile.count++;
                tileExists = true;
            }
        }

        if (!tileExists) {
            GridPosition newPos = new GridPosition(currentPos.x, currentPos.y);
            toDrop.pos = newPos;
            tiles.add(toDrop);
        }
        aidTiles.remove(sender);
        /*aidTiles.put(sender, null);*/
        return null;
    }

    public String checkHole(AID sender, GridPosition pos) {
        int points = 0;
        ArrayList<Hole> toRemoveHoles = new ArrayList<>();

        boolean holeExists = false;
        for (Hole hole: holes) {
            if (hole.pos.equals(pos)) {
                holeExists = true;

                AID pointsReceiver = null;
                for (AID aid: aidsScores.keySet()) {
                    if (aid.getLocalName().equals(hole.color)) {
                        pointsReceiver = aid;
                    }
                }
                if (hole.depth > 0) {
                    Tile toUse = aidTiles.get(sender);
                    if (toUse.color.equals(hole.color)) {
                        points += 10;
                    }

                    hole.depth -= 1;
                    /*aidTiles.put(sender, null);*/
                    aidTiles.remove(sender);

                    if (hole.depth == 0) {
                        toRemoveHoles.add(hole);
                        points += 40;
                        pointsReceiver = sender;
                    }
                }

                int score = aidsScores.get(pointsReceiver).intValue() + points;
                aidsScores.put(pointsReceiver, score);

            }
        }

        for (Hole hole: toRemoveHoles) {
            holes.remove(hole);
            graph.getVertexes().forEach(vertex -> {
                if (vertex.getPosition().equals(hole.pos)) {
                    graph.getEdges().forEach(edgeNou -> {
                        if (edgeNou.getSource().equals(vertex) || edgeNou.getDestination().equals(vertex)) {
                            edgeNou.setWeight(1);
                        }
                    });
                }
            });
        }

        if (!holeExists)
            return Errors.NO_HOLE_AT_COORDS.getMessage();

        return null;
    }

    public String executeUseTile(AID sender, Action action) {
        GridPosition currentPos = aidPositions.get(sender);
        if (action.arg1.equals("North"))
            checkHole(sender, new GridPosition(currentPos.x - 1, currentPos.y));
        if (action.arg1.equals("South"))
            checkHole(sender, new GridPosition(currentPos.x + 1, currentPos.y));
        if (action.arg1.equals("East"))
            checkHole(sender, new GridPosition(currentPos.x, currentPos.y - 1));
        if (action.arg1.equals("West"))
            checkHole(sender, new GridPosition(currentPos.x, currentPos.y + 1));

        return null;
    }

    public String executeAction(AID sender, Action action) throws InterruptedException {
        String errorMessage = null;
        if (action.actionName.equals("Pick"))
            errorMessage = executePick(sender, action);
        if (action.actionName.equals("Use_tile"))
            errorMessage = executeUseTile(sender, action);
        if (action.actionName.equals("Drop_tile"))
            errorMessage = executeDrop(sender);
        if (action.actionName.equals("Move"))
            errorMessage = executeMove(sender, action);

        return errorMessage;
    }

    public void printGridState(long currentTime, AID aidParam, Action action) {

        System.out.println("[" + (float)currentTime / 1000+ "]"  + " [" + aidParam.getLocalName() + "]" + " wants to : " + action.actionName + " " + action.arg1);
    }
    public void printGrid() {

        aidsScores.forEach((agent, score) -> {
            System.out.println("[ENV]" + agent.getName() + ": " + score);
        });

        for (int i = 0; i < (this.height  * 2); i++) {
            int realHeight = i / 2;
            for (int j = 0; j < this.width; j++) {

                boolean printed = false;
                boolean printedAgent = false;
                AID currentAgentInPosition = null;

                for (Map.Entry<AID, GridPosition> entry : aidPositions.entrySet()) {
                    AID aid = entry.getKey();
                    GridPosition postition = entry.getValue();
                    List<Map.Entry<AID, GridPosition>> moreInThisPlace =
                            aidPositions.entrySet().stream()
                                    .filter(otherMember -> !otherMember.equals(entry)
                                            && !otherMember.getKey().getName().equals(aid.getName())
                                            && otherMember.getValue().x == postition.x
                                            && otherMember.getValue().y == postition.y)
                                    .collect(Collectors.toList());
                    if (moreInThisPlace.size() > 0 ) {
                        if (realHeight == postition.x && j == postition.y && i % 2 == 0) {
                            System.out.print("|@ MANY " + (moreInThisPlace.size() + 1) + "|");
                            currentAgentInPosition = aid;
                            printed = true;
                            printedAgent = true;
                            break;
                        }
                    } else {
                        if (realHeight == postition.x && j == postition.y && i % 2 == 0) {
                            System.out.print("| @ " + getColorShort(aid.getName()) + "  |");
                            currentAgentInPosition = aid;
                            printed = true;
                            printedAgent = true;
                        } else if (realHeight == postition.x && j == postition.y) {
                            currentAgentInPosition = aid;
                            printedAgent = true;
                        }
                    }
                }


                printed = printObstacles(realHeight, j, printed);

                printed = printTiles(i, j, printed, printedAgent, currentAgentInPosition);

                printed = printHoles(i, realHeight, j , printed, printedAgent, currentAgentInPosition);
                if (!printed) {
                    System.out.print(EMPTY_CELL);
                }
            }
            System.out.println();
            if (i % 2 == 1) {
                for (int j = 0; j < this.width; j++) {
                    System.out.print("==========");

                }
                System.out.println();
            }

        }
        System.out.println("\n[ENV] current time: " + (float) currentTime / 1000 + "\n");
    }

    private boolean printHoles(int i, int realWidith, int j, boolean printed,
                               boolean printedAgent, AID currentAgentInPosition) {
        for (Hole hole : holes) {
            if (realWidith == hole.pos.x && j == hole.pos.y) {
                if (i % 2 == 0 && !printedAgent) {
                    System.out
                            .print("|# " + getColorShort(hole.color)
                                    + " " + hole.depth + " |");
                } else if (currentAgentInPosition != null && i % 2 == 1) {
                        System.out.print("|# " + getColorShort(hole.color) + " " + hole.depth
                                + " |");
                    } else if (!printedAgent) {
                        System.out.print(EMPTY_CELL);
                    }

                printed = true;
            }
        }
        return printed;
    }

    private boolean printObstacles(int i, int j, boolean printed) {
        for (GridPosition obst : obstacles) {
            if (obst.x == i && obst.y == j) {
                System.out.print(OBSTACLE);
                printed = true;
            }
        }
        return printed;
    }

    private boolean printTiles(int i, int j, boolean printed, boolean printedAgent, AID currentAgentInPosition) {
        String valueToPrint = "";
        String numberOfValues = "";
        boolean [][]printedHere = new boolean[height][width];
        for (int xi = 0; xi < height; xi++)
            for (int yj = 0; yj < width; yj++)
                printedHere[xi][yj] = false;


        for (Tile tle : tiles) {
            if (tle.pos.x == (i / 2) && tle.pos.y == j) {
                if (tiles.stream()
                        .anyMatch(tile -> tile.pos.equals(tle.pos) && !tile.color.equals(tle.color))) {
                    List<Tile> tilesInSamePosition =
                            tiles.stream().filter(tile -> tile.pos.equals(tle.pos))
                                    .collect(Collectors.toList());

                    for (Tile tileInThisPosition : tilesInSamePosition) {
                        valueToPrint = valueToPrint + tileInThisPosition.color.substring(0,1).toUpperCase();
                        numberOfValues += tileInThisPosition.count;
                    }
                }
                if (printedHere[tle.pos.x][tle.pos.y]) {
                    continue;
                }
                if (i % 2 == 0 && !printedAgent) {
                    printCurrentTile(valueToPrint, tle);
                } else if (currentAgentInPosition != null && i % 2 == 1) {
                    printCurrentTile(valueToPrint, tle);
                } else if (!printedAgent){
                    if (valueToPrint.equals("")) {
                        System.out.print(EMPTY_CELL);
                    } else {
                        System.out.print(String.format("| %7.7s|", numberOfValues));
                    }
                }
                if (!valueToPrint.equals("")) {
                    printedHere[tle.pos.x][tle.pos.y] = true;
                }
                valueToPrint = "";
                numberOfValues = "";
                printed = true;
            }
        }
        return printed;
    }

    private void printCurrentTile(String valueToPrint, Tile tle) {
        if (valueToPrint.equals("")) {
            System.out.print("|$ " + tle.count + " " + getColorShort(tle.color) + " |");
        } else {
            System.out.print(String.format("|$%7.7s|", valueToPrint));
        }
    }

    private String getColorShort(String color) {
        return color.substring(0, Math.min(color.length(), 3));
    }


    @Override
    public void action() {
        if (!firstPerceptionRound) {
            for (AID aid: aidsScores.keySet()) {
                try {
                    sendPerception(aid);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            firstPerceptionRound = true;
        }

        try {
            receiveAction();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (UnreadableException e) {
            e.printStackTrace();
        }
    }
}
