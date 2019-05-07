package agents.behaviors;

import agents.disjktra.DijkstraAlgorithm;
import agents.disjktra.GraphNou;
import agents.disjktra.Vertex;
import agents.utils.*;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class AgentBehavior extends CyclicBehaviour {
    public static final int BIG_DISTANCE = 100000;
    String color;
    GridPosition pos;
    AID envAID;
    ArrayList<AID> otherAgents;
    boolean gotFirstEnvMessage = false;
    int aliveAgents = 0;

    private MessageTemplate msgTemplate			= MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            MessageTemplate.MatchProtocol(EnvironmentBehavior.AGENTPERCEPT_PROTOCOL));

    private MessageTemplate terminateTemplate   = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            MessageTemplate.MatchProtocol(EnvironmentBehavior.AGENTTERMINATE_PROTOCOL));

    private MessageTemplate negotiateTemplate = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            MessageTemplate.MatchProtocol(AGNEGOTIATE_PROTOCOL));

    static final String ENVACTION_PROTOCOL = "sendaction";
    static final String AGNEGOTIATE_PROTOCOL = "negotiate";

    public AgentBehavior(String color, GridPosition pos, AID envAID, int agentsNr) {
        this.color = color;
        this.pos = pos;
        this.envAID = envAID;

       /* System.out.println("my name is " + color);
        System.out.println(color + " is at pos " + pos);
        System.out.println(color + " knows envAID is " + envAID);*/
        aliveAgents = agentsNr;
    }

    public Action chooseAction(Perception perception) throws InterruptedException {

        // EXAMPLE WITH ACTIONS THAT MAKE GREEN AGENT GRAB A BLUE TILE AND PUT IT IN A BLUE HOLE
        // BLUE WILL DIE BECAUSE IT WILL GET AN ERROR (ACTIONS DO NOT MATCH)

        GridPosition currentPosition = perception.pos;
        List<Tile> tiles = perception.tiles;
        List<Hole> holes = perception.holes;
        ArrayList<GridPosition> obstacles = perception.obstacles;
        Tile currentTile = perception.currentTile;
        GraphNou graph = perception.graph;
        Vertex startingNode = null;
        for (Vertex vertex : graph.getVertexes()) {
            if (vertex.getPosition().equals(currentPosition)) {
                startingNode = vertex;
                break;
            }
        }
        DijkstraAlgorithm djkaAlg = new DijkstraAlgorithm(graph);
        djkaAlg.execute(startingNode);
        Hole holeIWantToFill = null;
        System.out.println("am ion mana " + currentTile);
        if (currentTile == null) {
            for (Hole hole : holes) {
/*
                System.out.println(hole);
*/
                if (/*hole.depth == 1 && */hole.color.equals(color)) {
                    holeIWantToFill = hole;
                }
            }
            if (holeIWantToFill != null) {
                Tile tileIwillBeUsing = null;
                Vertex nodeOfTile = null;
                for (Tile tile : tiles) {
                    Integer minDistanceTale = Integer.MAX_VALUE;
                    if (tile.color.equals(color)) {
                        nodeOfTile = getClosestTile(graph, djkaAlg, nodeOfTile, tile, minDistanceTale);
                        tileIwillBeUsing = tile;
                    }
                }
                if (nodeOfTile == null) {
                    int minDistanceTale = 1000000;
                    for (Tile tile : tiles) {
                        for (Vertex vertex : graph.getVertexes()) {
                            if (vertex.getPosition().equals(tile.pos)) {
                                Integer dist = djkaAlg.getDistance().get(vertex);
                                if (dist < minDistanceTale) {
                                    nodeOfTile = vertex;
                                    tileIwillBeUsing = tile;
                                }
                                break;
                            }
                        }
                    }
                }
                if (nodeOfTile != null) {
                    LinkedList<Vertex> path = djkaAlg.getPath(nodeOfTile);
                    /*System.out.println("am gasit pathul plii plsss <# ");
                    System.out.println(path);*/
                    if (path != null && path.size() > 1) {
                        GridPosition nextPosition = path.get(1).getPosition();
                        /*System.out
                                .println("I am " + color + " and looking to go to " + nextPosition);*/
                        Action x = determineMoveAccordingToNextPosition(currentPosition, nextPosition);
                        if (x != null)
                            return x;
                    } else {
                        return new Action("Pick", tileIwillBeUsing.color);
                    }
                }
            }
        } else {
            String tileColor = currentTile.color;
            if (tileColor.equals(color)) {
                List<Hole> holesToFill = holes.stream().filter(hole -> hole.color.equals(color)).collect(Collectors.toList());
                Integer minDistanceTale = 1000000;
                Vertex nodeOfHole = null;
                nodeOfHole = getClosestHole(graph, djkaAlg, holesToFill, minDistanceTale);
                /*System.out.println(djkaAlg);
                System.out.println("gaura curului " + nodeOfHole + " din " + holeIWantToFill);*/
                if (nodeOfHole == null) {
                    nodeOfHole = getClosestHole(graph, djkaAlg, holes, minDistanceTale);
                }
                if (nodeOfHole != null) {
                    LinkedList<Vertex> path = djkaAlg.getPath(nodeOfHole);

/*
                    System.out.println("am gasit pathul plii spre gaura <# ");
*/
                    System.out.println(path);
                    if (path != null && path.size() > 1 && !"HOLE".equals(path.get(1).getNodeType())) {
                        GridPosition nextPosition = path.get(1).getPosition();
/*
                        System.out.println("I am " + color + " and looking to go to " + nextPosition);
*/
                        Action x = determineMoveAccordingToNextPosition(currentPosition, nextPosition);
                        if (x != null)
                            return x;
                    } else {
                        GridPosition nextPosition = path.get(1).getPosition();
                        Action x = determineMoveAccordingToNextPosition(currentPosition, nextPosition);
                        return new Action("Use_tile", x.arg1);
                    }
                }
            } else {
                Vertex nodeOfHole = getClosestHole(graph, djkaAlg, holes, 1000000);
                if (nodeOfHole != null) {
                    Action x = gotToHoleOrFillHole(currentPosition, djkaAlg, nodeOfHole);
                    if (x != null)
                        return x;
                }
            }

        }
        if (color.equals("green")){
            Thread.sleep(200);
        }
        return currentPosition.y % 2 == 0 ? new Action("Move", "North") : new Action("Move", "South");
    }

    private Action gotToHoleOrFillHole(GridPosition currentPosition, DijkstraAlgorithm djkaAlg,
            Vertex nodeOfHole) {
        LinkedList<Vertex> path = djkaAlg.getPath(nodeOfHole);

        System.out.println("am gasit pathul plii spre gaura <# ");
        System.out.println(path);
        if (path != null && path.size() > 1 && !"HOLE".equals(path.get(1).getNodeType())) {
            GridPosition nextPosition = path.get(1).getPosition();
            System.out.println("I am " + color + " and looking to go to " + nextPosition);
            Action x = determineMoveAccordingToNextPosition(currentPosition, nextPosition);
            if (x != null)
                return x;
        } else {
            GridPosition nextPosition = path.get(1).getPosition();
            Action x = determineMoveAccordingToNextPosition(currentPosition, nextPosition);
            return new Action("Use_tile", x.arg1);
        }
        return null;
    }

    private Vertex getClosestHole(GraphNou graph, DijkstraAlgorithm djkaAlg, List<Hole> holesToFill,
            Integer minDistanceTale) {
        Vertex nodeOfHole = null;
        for (Hole holeToFill : holesToFill) {
            for (Vertex vertex : graph.getVertexes()) {
                if (vertex.getPosition().equals(holeToFill.pos)) {
                    Integer dist = djkaAlg.getDistance().get(vertex);
                    if (dist < minDistanceTale) {
                        nodeOfHole = vertex;
                    }
                    break;
                }
            }
        }
        return nodeOfHole;
    }

    private Vertex getClosestTile(GraphNou graph, DijkstraAlgorithm djkaAlg, Vertex nodeOfTile,
            Tile tile, Integer minDistanceTale) {
        for (Vertex vertex : graph.getVertexes()) {
            if (vertex.getPosition().equals(tile.pos)) {
                Integer dist = djkaAlg.getDistance().get(vertex);
                if (dist < minDistanceTale) {
                    nodeOfTile = vertex;
                }
                break;
            }
        }
        return nodeOfTile;
    }

    private Action determineMoveAccordingToNextPosition(GridPosition currentPosition,
            GridPosition nextPosition) {
        if (nextPosition.x < currentPosition.x) {
            return new Action("Move", "North");
        }
        if (nextPosition.x > currentPosition.x) {
            return new Action("Move", "South");
        }
        if (nextPosition.y < currentPosition.y) {
            return new Action("Move", "East");
        }
        if (nextPosition.y > currentPosition.y) {
            return new Action("Move", "West");
        }
        return null;
    }

    public void sendAction(Perception perception) throws IOException, InterruptedException {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setProtocol(ENVACTION_PROTOCOL);
        msg.addReceiver(envAID);

        // CHOOSE ACTION AND SEND IT
        Action chosenAction = chooseAction(perception);

        msg.setContentObject(chosenAction);
        myAgent.send(msg);
    }

    public void receivePerception() throws UnreadableException, IOException, InterruptedException {
        ACLMessage receivedMsg = myAgent.receive(msgTemplate);
        if (receivedMsg != null) {
            gotFirstEnvMessage = true;
            Perception perception = (Perception) receivedMsg.getContentObject();
           /*
            System.out.println(color + ": " + perception);
            System.out.println(color + " ERROR: " + perception.error);
            */
            pos = perception.pos;
            otherAgents = perception.otherAgentAIDs;
            negotiate();

            if (perception.error != null) {
                System.out.println("AGENT " + color  + " 's action FAILED due to ERROR..." + perception.error);
//                aliveAgents--;
//                myAgent.doDelete();
            }

            // DO SOMETHING WITH PERCEPTION
            sendAction(perception);
            TimeUnit.MILLISECONDS.sleep(perception.operationTime);
        }
    }

    public void receiveTerminate() {
        ACLMessage terminateMsg = myAgent.receive(terminateTemplate);

        if (terminateMsg != null) {
            System.out.println("AGENT " + color  + " TERMINATING...");
            myAgent.doDelete();
        }
    }

    public void negotiate() {
        if (gotFirstEnvMessage) {
            for (int i = 0; i < aliveAgents - 1; ++i) {
                ACLMessage negotiateMsg = myAgent.receive(negotiateTemplate);
//                if (negotiateMsg != null)
//                    System.out.println("Agent " + color  + " received negotiation stuff from " +
//                            negotiateMsg.getSender().getLocalName() + " with content " + negotiateMsg.getContent());
            }

            for (AID aid: otherAgents) {
                if (!aid.getLocalName().equals(myAgent.getAID().getLocalName())) {
                    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                    msg.setProtocol(AGNEGOTIATE_PROTOCOL);
                    msg.addReceiver(aid);

                    msg.setContent("salut" + LocalDateTime.now());
                    myAgent.send(msg);
                }
            }

//            System.out.println("NEGOTIATION PHASE COMPLETED");
        }
    }

    @Override
    public void action() {
        try {
            receivePerception();
        } catch (UnreadableException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        receiveTerminate();
    }
}
