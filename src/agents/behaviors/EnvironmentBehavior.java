package agents.behaviors;

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

public class EnvironmentBehavior extends CyclicBehaviour {
    static final String AGENTPERCEPT_PROTOCOL = "sendpercept";
    static final String AGENTTERMINATE_PROTOCOL = "terminate";

    AtomicInteger recNr = new AtomicInteger();

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
    ArrayList<AID> toStopMessaging = new ArrayList<>();
    ArrayList<AID> otherAgents = new ArrayList<>();

    int currentTime = 0;

    public EnvironmentBehavior(int agentsNr, int operationTime, int totalTime, int width, int height,
                               ArrayList<GridPosition> obstacles, ArrayList<Tile> tiles, ArrayList<Hole> holes,
                               Map<AID, Integer> aidsScores, Map<AID, GridPosition> aidPositions) {

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

        for (AID aid: aidsScores.keySet()) {
            aidErrors.put(aid, null);
            otherAgents.add(aid);
        }

        System.out.println("my name is " + "env");
        System.out.println("agentsNr = " +  agentsNr);
        System.out.println("operationTime = " + operationTime);
        System.out.println("totalTime = " + totalTime);
        System.out.println("width = " + width);
        System.out.println("height = " + height);
        System.out.println("obstacles = " + obstacles);
        System.out.println("tiles = " + tiles);
        System.out.println("holes = " + holes);
        System.out.println("aidsScores = " + aidsScores);
    }

    public void sendPerception() throws IOException {
        if (recNr.get() == 0) {
            for (AID aid : aidsScores.keySet()) {
                if (!toStopMessaging.contains(aid)) {
                    recNr.getAndIncrement();
                    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                    msg.setProtocol(AGENTPERCEPT_PROTOCOL);
                    msg.addReceiver(aid);

                    // SEND PERCEPTION TO ALL AGENTS
                    Perception perception = new Perception(currentTime, width, height, obstacles,
                            tiles, holes, aidsScores.get(aid), aidPositions.get(aid), aidTiles.get(aid),
                            aidErrors.get(aid), otherAgents);
                    msg.setContentObject(perception);

                    if (aidErrors.get(aid) != null) {
                        toStopMessaging.add(aid);
                    }
                    myAgent.send(msg);
                }
            }

            if (toStopMessaging.size() == agentsNr) {
                myAgent.doDelete();
            }
        }
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
                recNr.getAndDecrement();
                Action action = (Action) receivedMsg.getContentObject();
                AID sender = receivedMsg.getSender();
                String errorMessage = executeAction(sender, action);
                aidErrors.put(sender, errorMessage);

                if (recNr.get() == 0) {
                    currentTime += operationTime;
                    // IF TIME IS OUT, INFORM EVERYONE AND EXIT
                    if (currentTime > totalTime) {
                        System.out.println("ENV SAYS TIMEOUT, TERMINATING....");
                        sendTerminate();
                        myAgent.doDelete();
                    }
                }
            }
    }

    public String executePick(AID sender, Action action) {
        Tile toDelete = null;
        boolean tileExists = false;

        for (Tile tile: tiles) {
            if (tile.pos.equals(aidPositions.get(sender))) {
                tileExists = true;
                if (tile.color.equals(action.arg1)) {
                    if (tile.count > 0) {
                        tile.count--;
                        aidTiles.put(sender, new Tile(1, tile.color, tile.pos));
                        if (tile.count == 0)
                            toDelete = tile;
                    } else {
                        return Errors.NO_TILE_AT_COORDS.getMessage();
                    }
                } else {
                    return Errors.NO_TILE_COLOR_AT_COORDS.getMessage();
                }
            }
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
                pos.y++;

                nullOrError = checkPosition(pos);
                return nullOrError;
            }
            if (action.arg1.equals("South")) {
                pos = aidPositions.get(sender);
                pos.y--;

                nullOrError = checkPosition(pos);
                return nullOrError;
            }
            if (action.arg1.equals("East")) {
                pos = aidPositions.get(sender);
                pos.x++;

                nullOrError = checkPosition(pos);
                return nullOrError;
            }
            if (action.arg1.equals("West")) {
                pos = aidPositions.get(sender);
                pos.x--;

                nullOrError = checkPosition(pos);
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

        aidTiles.put(sender, null);
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
                    aidTiles.put(sender, null);

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
        }

        if (!holeExists)
            return Errors.NO_HOLE_AT_COORDS.getMessage();

        return null;
    }

    public String executeUseTile(AID sender, Action action) {
        GridPosition currentPos = aidPositions.get(sender);

        if (action.arg1.equals("North"))
            checkHole(sender, new GridPosition(currentPos.x, currentPos.y + 1));
        if (action.arg1.equals("South"))
            checkHole(sender, new GridPosition(currentPos.x, currentPos.y - 1));
        if (action.arg1.equals("East"))
            checkHole(sender, new GridPosition(currentPos.x + 1, currentPos.y));
        if (action.arg1.equals("West"))
            checkHole(sender, new GridPosition(currentPos.x - 1, currentPos.y));

        return null;
    }

    public String executeAction(AID sender, Action action) throws InterruptedException {
        String errorMessage = null;
        System.out.println(sender.getLocalName() + " " + action);

        if (action.actionName.equals("Pick"))
            errorMessage = executePick(sender, action);
        if (action.actionName.equals("Use_tile"))
            errorMessage = executeUseTile(sender, action);
        if (action.actionName.equals("Drop_tile"))
            errorMessage = executeDrop(sender);
        if (action.actionName.equals("Move"))
            errorMessage = executeMove(sender, action);

        TimeUnit.MILLISECONDS.sleep(operationTime);

        return errorMessage;
    }

    @Override
    public void action() {
        try {
            sendPerception();
        } catch (IOException e) {
            e.printStackTrace();
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
