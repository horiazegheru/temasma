package agents.disjktra;

import java.io.Serializable;

public class EdgeNou implements Serializable {
    private final Vertex source;
    private final Vertex destination;
    private int weight;

    public EdgeNou(Vertex source, Vertex destination, int weight) {
        this.source = source;
        this.destination = destination;
        this.weight = weight;
    }
    public Vertex getDestination() {
        return destination;
    }

    public Vertex getSource() {
        return source;
    }
    public int getWeight() {
        return weight;
    }
    public void setWeight(int weight) {
        this.weight = weight;
    }



    @Override
    public String toString() {
        return "source : " + source + " destination : " + destination;
    }


}
