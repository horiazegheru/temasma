package agents.model;

import agents.utils.GridPosition;

import java.util.ArrayList;
import java.util.List;

public class Node {

    private GridPosition position;

    private List<Edge> children;
    private Node parent;
    private String nodeType;


    public Node() {
        children = new ArrayList<>();
    }

    public GridPosition getPosition() {
        return position;
    }

    public void setPosition(GridPosition position) {
        this.position = position;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public void addChildren(Node kid, int cost) {
        children.add(new Edge(kid, cost));
    }

    public List<Edge> getChildren() {
        return children;
    }

    public void setChildren(List<Edge> children) {
        this.children = children;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }
}
