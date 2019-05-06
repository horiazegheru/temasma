package agents.disjktra;

import agents.utils.GridPosition;

import java.util.Objects;

public class Vertex {
    GridPosition position;
    private String nodeType;

    public Vertex(GridPosition position) {

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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Vertex))
            return false;
        Vertex vertex = (Vertex) o;
        return Objects.equals(position, vertex.position) && Objects
                .equals(nodeType, vertex.nodeType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, nodeType);
    }

    @Override
    public String toString() {
        return "Vertex{" + "position=" + position + ", nodeType='" + nodeType + '\'' + '}';
    }
}
