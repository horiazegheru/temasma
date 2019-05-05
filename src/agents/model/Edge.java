package agents.model;

public class Edge {
    private Node node;
    private int cost;

    public Edge(Node node, int cost) {
        this.node = node;
        this.cost = cost;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }
}
