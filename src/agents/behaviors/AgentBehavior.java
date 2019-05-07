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
    public static final int BIG_NUMBER = 1000000;
    public static final String HOLE = "HOLE";
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

        aliveAgents = agentsNr;
    }

    public Action chooseAction(Perception perception) throws InterruptedException {

        // EXAMPLE WITH ACTIONS THAT MAKE GREEN AGENT GRAB A BLUE TILE AND PUT IT IN A BLUE HOLE
        // BLUE WILL DIE BECAUSE IT WILL GET AN ERROR (ACTIONS DO NOT MATCH)

        if (color.equals("green")){
            Thread.sleep(200);
        }

        GridPosition currentPosition = perception.pos;
        List<Tile> tiles = perception.tiles;
        List<Hole> holes = perception.holes;
        ArrayList<GridPosition> obstacles = perception.obstacles;
        Tile currentTile = perception.currentTile;
        GraphNou graph = perception.graph;
        Vertex startingNode = getStartingNode(currentPosition, graph);

        DijkstraAlgorithm djkaAlg = new DijkstraAlgorithm(graph);
        djkaAlg.execute(startingNode);
        Hole holeIWantToFill = null;

        if (currentTile == null) {
            holeIWantToFill = findHoleSameColor(holes, holeIWantToFill);
            holeIWantToFill = findClosestHoleIfDidNotFindSameColor(holes, graph, djkaAlg, holeIWantToFill);
            if (holeIWantToFill != null) {
                return determineActionInOrderToGetTileForHole(currentPosition, tiles, graph,
                                djkaAlg);
            } else {
                // TODO same as no tile, move like a madman, better handling should be done
                return new Action("Move", "East");
            }
        }
        else {
            String tileColor = currentTile.color;
            if (tileColor.equals(color)) {
                return handleTileOfSameColorAsAgent(currentPosition, holes, graph, djkaAlg);
            } else {
                Vertex nodeOfHole = getClosestHole(graph, djkaAlg, holes, BIG_NUMBER);
                if (nodeOfHole != null) {
                    return gotToHoleOrFillHole(currentPosition, djkaAlg, nodeOfHole);
                } else {
                    return new Action("Drop_tile", "const");
                }
            }

        }
    }

    private Action determineActionInOrderToGetTileForHole(GridPosition currentPosition,
            List<Tile> tiles, GraphNou graph, DijkstraAlgorithm djkaAlg) {
        Tile tileIwillBeUsing = null;
        Vertex nodeOfTile = null;
        for (Tile tile : tiles) {
            Integer minDistanceTale = Integer.MAX_VALUE;
            if (tile.color.equals(color) && tile.count > 0) {
                nodeOfTile = getClosestTile(graph, djkaAlg, nodeOfTile, tile, minDistanceTale);
                tileIwillBeUsing = tile;
            }
        }

        if (nodeOfTile == null) {
            int minDistanceTale = BIG_NUMBER;
            for (Tile tile : tiles) {
                for (Vertex vertex : graph.getVertexes()) {
                    if (vertex.getPosition().equals(tile.pos) && tile.count > 0) {
                        Integer dist = djkaAlg.getDistance().get(vertex);
                        if (dist < minDistanceTale) {
                            minDistanceTale = dist;
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
            if (path != null && path.size() > 1) {
                GridPosition nextPosition = path.get(1).getPosition();
                return determinePositioningForPlacingAndMoving(currentPosition, nextPosition);

            } else {
                return new Action("Pick", tileIwillBeUsing.color);
            }
        } else {
            /*
            TODO sometimes there are no more tiles, we should see how we handle that this is a placeholder
             as a bad move is handled by the environment.
            */
            return new Action("Move", "West");
        }
    }

    private Hole findClosestHoleIfDidNotFindSameColor(List<Hole> holes, GraphNou graph,
            DijkstraAlgorithm djkaAlg, Hole holeIWantToFill) {
        if (holeIWantToFill == null) {
            holeIWantToFill = getClosestHoleToFill(holes, graph, djkaAlg, holeIWantToFill);

        }
        return holeIWantToFill;
    }

    private Hole findHoleSameColor(List<Hole> holes, Hole holeIWantToFill) {
        for (Hole hole : holes) {
            if (hole.color.equals(color)) {
                holeIWantToFill = hole;
            }
        }
        return holeIWantToFill;
    }

    private Vertex getStartingNode(GridPosition currentPosition, GraphNou graph) {
        Vertex startingNode = null;
        for (Vertex vertex : graph.getVertexes()) {
            if (vertex.getPosition().equals(currentPosition)) {
                startingNode = vertex;
                break;
            }
        }
        return startingNode;
    }

    private Action handleTileOfSameColorAsAgent(GridPosition currentPosition, List<Hole> holes,
            GraphNou graph, DijkstraAlgorithm djkaAlg) {
        List<Hole> holesToFill = holes.stream().filter(hole -> hole.color.equals(color)).collect(
                Collectors.toList());
        Integer minDistanceTale = 1000000;
        Vertex nodeOfHole = null;
        nodeOfHole = getClosestHole(graph, djkaAlg, holesToFill, minDistanceTale);
        if (nodeOfHole == null) {
            nodeOfHole = getClosestHole(graph, djkaAlg, holes, minDistanceTale);
        }
        if (nodeOfHole != null) {
            LinkedList<Vertex> path = djkaAlg.getPath(nodeOfHole);
            if (path != null && path.size() > 1 && !HOLE.equals(path.get(1).getNodeType())) {
                GridPosition nextPosition = path.get(1).getPosition();
                return determinePositioningForPlacingAndMoving(currentPosition, nextPosition);

            } else {
                GridPosition nextPosition = path.get(1).getPosition();
                Action x = determinePositioningForPlacingAndMoving(currentPosition, nextPosition);
                return new Action("Use_tile", x.arg1);
            }
        } else {
            return new Action("Drop_tile", "const");
        }
    }

    private Hole getClosestHoleToFill(List<Hole> holes, GraphNou graph, DijkstraAlgorithm djkaAlg,
            Hole holeIWantToFill) {
        int minDistanceTale = BIG_NUMBER;
        for (Hole holeToFill : holes) {
            for (Vertex vertex : graph.getVertexes()) {
                if (vertex.getPosition().equals(holeToFill.pos)) {
                    Integer dist = djkaAlg.getDistance().get(vertex);
                    if (dist < minDistanceTale) {
                        holeIWantToFill = holeToFill;
                        minDistanceTale = dist;
                    }
                    break;
                }
            }
        }
        return holeIWantToFill;
    }

    private Action gotToHoleOrFillHole(GridPosition currentPosition, DijkstraAlgorithm djkaAlg,
            Vertex nodeOfHole) {
        LinkedList<Vertex> path = djkaAlg.getPath(nodeOfHole);

        if (path != null && path.size() > 1 && !AgentBehavior.HOLE.equals(path.get(1).getNodeType())) {
            GridPosition nextPosition = path.get(1).getPosition();
            return determinePositioningForPlacingAndMoving(currentPosition, nextPosition);
        } else {
            GridPosition nextPosition = path.get(1).getPosition();
            Action x = determinePositioningForPlacingAndMoving(currentPosition, nextPosition);
            return new Action("Use_tile", x.arg1);
        }
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

    private Action determinePositioningForPlacingAndMoving(GridPosition currentPosition,
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
        throw new RuntimeException("Can't step there");
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
