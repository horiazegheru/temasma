package agents.model;

import java.io.Serializable;

public class Graph implements Serializable {

    private Node root;

    public Node getRoot() {
        return root;
    }

    public void setRoot(Node root) {
        this.root = root;
    }

    @Override
    public String toString() {
        return root.toString();
    }
}
