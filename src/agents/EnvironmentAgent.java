package agents;

import FIPA.DateTime;
import agents.behaviors.EnvironmentBehavior;
import agents.disjktra.EdgeNou;
import agents.disjktra.GraphNou;
import agents.disjktra.Vertex;
import agents.model.Edge;
import agents.model.Graph;
import agents.model.Node;
import agents.utils.GridPosition;
import agents.utils.Hole;
import agents.utils.Tile;
import jade.core.AID;
import jade.core.Agent;

import java.util.*;

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

        Graph graph = createGraphForDijkstra();
        Node root = graph.getRoot();
        System.out.println("Graful meu fuTuT");
        Map<Node, Boolean> done = new HashMap<>();
        List<Node> toPrint = new ArrayList<>();
        toPrint.add(root);
        while (!toPrint.isEmpty()) {
            Node currentNode = toPrint.get(0);
            System.out.println(currentNode);
            List<Edge> kidos = currentNode.getChildren();
            kidos.forEach(kid -> {
                if (!Boolean.TRUE.equals(done.get(kid.getNode()))) {
                    done.put(kid.getNode(), Boolean.TRUE);
                    toPrint.add(kid.getNode());
                }
            });
            toPrint.remove(0);
        }


        for (AID aid: aidPositions.keySet())
            aidsScores.put(aid, 0);

        long startTime = new Date().getTime();

        addBehaviour(new EnvironmentBehavior(agentsNr, operationTime, totalTime, width, height, obstacles, tiles, holes,
                aidsScores, aidPositions, startTime, graph));
    }

    private Graph createGraphForDijkstra() {

        List<Vertex> nodes = new ArrayList<>();
        List<EdgeNou> edges = new ArrayList<>();


        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                Vertex vertex  = new Vertex(new GridPosition(i, j));
                nodes.add(vertex);
            }
        }



        GraphNou graphNou = new GraphNou(nodes, edges);


        Graph graph = new Graph();
        List<Node> allNodes = new ArrayList<>();

/*        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {

                GridPosition currentPosition = new GridPosition(i, j);
                if (obstacles.stream().anyMatch(obstacle -> currentPosition.equals(obstacle))) {
                    continue;
                }
                Node node = new Node();
                node.setPosition(currentPosition);
                node.setNodeType("SIMPLE");
                if (!holes.stream().anyMatch(hole -> currentPosition.equals(hole.pos))) {
                    node.setNodeType("HOLE");
                }
                if (!tiles.stream().anyMatch(tile -> currentPosition.equals(tile.pos))) {
                    node.setNodeType("TILE");
                }
                allNodes.add(node);
            }
        }*/

        for (Vertex vertex : nodes) {
            for (Vertex children : nodes) {
                if (isUpperOrLower(vertex, children)) {
                    /*children.setParent(node);*/
                    edges.add(new EdgeNou(vertex, children, getCost(children)));
                }
                if (isLeftOrRight(vertex, children)) {
                    /*children.setParent(node);*/
                    edges.add(new EdgeNou(vertex, children, getCost(children)));
                }
        }
        }

      /*  for (Node node : allNodes) {
            if (node.getPosition().x == 0 && node.getPosition().y == 0) {
                node.setParent(null);
                graph.setRoot(node);
            }
            for (Node children : allNodes) {
                if (isUpperOrLower(node, children)) {
                    *//*children.setParent(node);*//*
                    node.addChildren(children, getCost(children));
                }
                if (isLeftOrRight(node, children)) {
                    *//*children.setParent(node);*//*
                    node.addChildren(children, getCost(children));
                }
            }
        }*/
        return graph;
    }

    private int getCost(Vertex children) {
        return Arrays.asList("TILE", "HOLE").contains(children.getNodeType()) ? Integer.MAX_VALUE : 0;
    }

    @Override
    protected void takeDown() {
        for (AID aid: aidsScores.keySet()) {
            System.out.println("AGENT " + aid.getLocalName() + " finished with " + aidsScores.get(aid) + " points.");
        }

        System.out.println("ENV ENDED");
    }

    private boolean isUpperOrLower(Vertex node, Vertex children) {
        return ((children.getPosition().x == node.getPosition().x - 1 || children.getPosition().x == node.getPosition().x + 1) &&
                children.getPosition().y == node.getPosition().y);
    }

    private boolean isLeftOrRight(Vertex node, Vertex children) {
        return ((children.getPosition().y == node.getPosition().y - 1 || children.getPosition().y == node.getPosition().y + 1) &&
                children.getPosition().x == node.getPosition().x);
    }


}
