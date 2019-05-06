package agents.behaviors;

import agents.model.Edge;
import agents.model.Graph;
import agents.model.Node;
import agents.utils.*;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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

        Hole holeIWantToFill = null;
        if (currentTile == null) {
            for (Hole hole : holes) {
                System.out.println(hole);
                if (hole.depth == 1 && hole.color.equals(color)) {
                    holeIWantToFill = hole;
                    System.out.println("Did I find a gole?");
                }
            }
        }
        if (holeIWantToFill != null) {

            for (Tile tile : tiles) {
                if (tile.color.equals(color)) {

                    findShortestPath(currentPosition, tile.pos, perception.graph);

                    if (tile.pos.x < currentPosition.x) {
                        return new Action("Move", "West");
                    }
                    if (tile.pos.x > currentPosition.x) {
                        return new Action("Move", "East");
                    }
                    if (tile.pos.y < currentPosition.y) {
                        return new Action("Move", "North");
                    }
                    if (tile.pos.y > currentPosition.y) {
                        return new Action("Move", "South");
                    }
                    return new Action("Pick", color);
                }
            }
        }

        if (color.equals("green")){
            Thread.sleep(200);
        }

        return new Action("Move", "North");

        /*
        if (holeIWantToFill.pos.x < currentPosition.x) {
                return new Action("Move", "East");
            }
            if (holeIWantToFill.pos.x > )


        if (perception.operationTime == 0)
            return new Action("Move", "South");

        if (perception.operationTime == 300)
            return new Action("Move", "South");

        if (perception.operationTime == 600)
            return new Action("Pick", "blue");

        if (perception.operationTime == 900)
            return new Action("Move", "North");

        if (perception.operationTime == 1200)
            return new Action("Use_tile", "East");
*/
/*        return new Action("Move", "West");*/
    }

    private void findShortestPath(GridPosition currentPosition, GridPosition pos, Graph graph) {

        Node startingNode = getStartingNode(currentPosition, graph);

        Set<Node> settledNodes = new HashSet<>();
        Set<Node> unsettledNodes = new HashSet<>();
        Map<Node, Integer> distance = new HashMap<>();
        distance.put(startingNode, 0);
        unsettledNodes.add(startingNode);

        while (unsettledNodes.size() > 0) {
            System.out.println("nelinistiti " + unsettledNodes);
            Node evalNode = getMinDistanceNode(unsettledNodes, distance);
            unsettledNodes.remove(evalNode);
            settledNodes.add(evalNode);
            evaluateNeighbours(evalNode, settledNodes, unsettledNodes, distance);
        }
        System.out.println("oare a mers cacatul? " + distance);
    }

    private Node getStartingNode(GridPosition currentPosition, Graph graph) {
        Node root = graph.getRoot();
        Node startingNode = null;

        Map<Node, Boolean> visited = new HashMap<>();
        List<Node> toVisit = new ArrayList<>();
        toVisit.add(root);
        while (!toVisit.isEmpty()) {
            Node currentNode = toVisit.get(0);
            if (currentNode.getPosition().equals(currentPosition)) {
                startingNode = currentNode;
                break;
            }
            List<Edge> kidos = currentNode.getChildren();
            kidos.forEach(kid -> {
                if (!Boolean.TRUE.equals(visited.get(kid.getNode()))) {
                    visited.put(kid.getNode(), Boolean.TRUE);
                    toVisit.add(kid.getNode());
                }
            });
            toVisit.remove(0);
        }
        System.out.println("found the cockfucker " + startingNode);
        return startingNode;
    }

    private Node getMinDistanceNode(Set<Node> unsettledNodes, Map<Node, Integer> distance) {
        int minDistance = Integer.MAX_VALUE;
        Node shortestNode = null;
        for (Node node : unsettledNodes) {
            if (distance.get(node) < minDistance) {
                minDistance = distance.get(node);
                shortestNode = node;
            }
        }
        return shortestNode;
    }

    private void evaluateNeighbours(Node evalNode, Set<Node> settledNodes,
            Set<Node> unsettledNodes, Map<Node, Integer> distance) {
        List<Edge> adjacentNodes = evalNode.getChildren();
        for (Edge destNode : adjacentNodes) {
            if (!settledNodes.contains(destNode.getNode())) {
                Integer dist = distance.get(destNode.getNode());
                if (dist == null) {
                    dist = 0;
                }
                if (destNode.getNode().getNodeType().equals("HOLE")) {
                    dist += BIG_DISTANCE;
                } else {
                    dist += 1;
                }
                if (distance.get(destNode.getNode()) == null || distance.get(destNode.getNode())> dist) {
                    System.out.println("morti matii " + destNode.getNode());
                    distance.put(destNode.getNode(), dist);
                    unsettledNodes.add(destNode.getNode());
                }
            }
        }
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
            System.out.println(perception);
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
