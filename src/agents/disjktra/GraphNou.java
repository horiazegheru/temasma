package agents.disjktra;

import java.io.Serializable;
import java.util.List;

public class GraphNou implements Serializable {
    private final List<Vertex> vertexes;
    private final List<EdgeNou> edges;

    public GraphNou(List<Vertex> vertexes, List<EdgeNou> edges) {
        this.vertexes = vertexes;
        this.edges = edges;
    }

    public List<Vertex> getVertexes() {
        return vertexes;
    }

    public List<EdgeNou> getEdges() {
        return edges;
    }


}
