package agents.behaviors;

import agents.utils.*;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class EnvironmentBehavior extends CyclicBehaviour {
    static final String AGENTPERCEPT_PROTOCOL = "sendpercept";
    static final String AGENTTERMINATE_PROTOCOL = "terminate";
    AtomicInteger recNr = new AtomicInteger();

    private MessageTemplate msgTemplate			= MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            MessageTemplate.MatchProtocol(AgentBehavior.ENVACTION_PROTOCOL));

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
    Map<AID, GridPosition> aidPositions = new HashMap<>();
    Map<AID, Tile> aidTiles = new HashMap<>();

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
                recNr.getAndIncrement();
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.setProtocol(AGENTPERCEPT_PROTOCOL);
                msg.addReceiver(aid);

                // SEND PERCEPTION TO ALL AGENTS
                Perception perception = new Perception(currentTime, width, height, obstacles,
                        tiles, holes, aidsScores.get(aid), aidPositions.get(aid));
                msg.setContentObject(perception);

                myAgent.send(msg);
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
                executeAction(sender, action);

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

    public void executePick(AID sender, Action action) {
        if (action.actionName.equals("Pick")) {
            Tile toDelete = null;
            boolean tileExists = false;

            for (Tile tile: tiles) {
                if (tile.pos.equals(aidPositions.get(sender))) {
                    tileExists = true;
                    if (tile.color.equals(action.arg1)) {
                        if (tile.count > 0) {
                            tile.count--;
                            aidTiles.put(sender, new Tile(1, tile.color, null));
                            if (tile.count == 0)
                                toDelete = tile;
                        } else {
                            System.out.println("nu mai sunt tile-uri");
                        }
                    } else {
                        System.out.println("vrei sa iei un tile de culoarea gresita");
                    }
                }
            }

            if (!tileExists)
                System.out.println("no tile at coords");

            if (toDelete != null)
                tiles.remove(toDelete);
        }
    }

    public void executeMove(AID sender, Action action) {
        System.out.println(sender.getLocalName() + " " + action);
        if (action.actionName.equals("Move")) {
            // VERIFICARI ZIDURI OBSTACOLE BLABLA
            if (action.arg1.equals("North")) {
                aidPositions.get(sender).y++;
            }
            if (action.arg1.equals("South")) {
                aidPositions.get(sender).y--;
            }
            if (action.arg1.equals("East")) {
                aidPositions.get(sender).x++;
            }
            if (action.arg1.equals("West")) {
                aidPositions.get(sender).x--;
            }
        }
    }

    void executeDrop(AID sender, Action action) {
        if (action.actionName.equals("Drop_tile")) {
            Tile toDrop = aidTiles.get(sender);

            if (toDrop == null) {
                System.out.println("you are not carrying anything");
            }

            GridPosition currentPos = aidPositions.get(sender);

            boolean tileExists = false;
            for (Tile tile: tiles) {
                if (tile.pos == currentPos && tile.color.equals(toDrop.color)) {
                    tile.count++;
                    tileExists = true;
                }
            }

            if (!tileExists) {
                toDrop.pos = currentPos;
                tiles.add(toDrop);
            }

        }
    }

    public void checkHole(AID sender, GridPosition pos) {
        int points = 0;

        boolean holeExists = false;
        for (Hole hole: holes) {
            if (hole.pos == pos) {
                holeExists = true;

                if (hole.depth > 0) {
                    Tile toUse = aidTiles.get(sender);
                    if (toUse.color == hole.color) {
                        points += 10;
                    }

                    hole.depth -= 1;
                    aidTiles.remove(toUse);

                    if (hole.depth == 0) {
                        holes.remove(hole);
                        points += 40;
                    }
                }
            }
        }

        if (!holeExists)
            System.out.println("no hole here");
        else {
            int score = aidsScores.get(sender).intValue() + points;
            aidsScores.put(sender, score);
        }
    }

    public void executeUseTile(AID sender, Action action) {
        if (action.actionName.equals("Use_tile")) {
            GridPosition currentPos = aidPositions.get(sender);

            if (action.arg1.equals("North"))
                checkHole(sender, new GridPosition(currentPos.x, currentPos.y + 1));
            if (action.arg1.equals("South"))
                checkHole(sender, new GridPosition(currentPos.x, currentPos.y - 1));
            if (action.arg1.equals("East"))
                checkHole(sender, new GridPosition(currentPos.x + 1, currentPos.y));
            if (action.arg1.equals("West"))
                checkHole(sender, new GridPosition(currentPos.x - 1, currentPos.y));
        }
    }

    public void executeAction(AID sender, Action action) throws InterruptedException {
        executePick(sender, action);
        executeMove(sender, action);
        executeDrop(sender, action);
        executeUseTile(sender, action);
        TimeUnit.MILLISECONDS.sleep(300);
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
