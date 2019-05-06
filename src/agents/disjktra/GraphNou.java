package agents.disjktra;

import java.util.List;

public class GraphNou {
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
