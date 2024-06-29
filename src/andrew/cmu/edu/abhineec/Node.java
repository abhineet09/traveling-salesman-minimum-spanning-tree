package andrew.cmu.edu.abhineec;

/******************************************************************************
 * Node class represents a POJO for each individual node in graph representing
 * vertex - an integer identifying each vertex
 * crime - holds all the data for crime
 * minimum_cost - minimal cost of travelling to the vertex
 * parent - edge from where the vertex can be visited with minimal cost as
 *              identified using Prim's algorithm
 ******************************************************************************/
public class Node {

    int vertex;
    Crime crime;
    Double minimum_cost;
    Integer parent;

    public Node(int v, Crime c, Double mc, Integer p) {
        this.vertex = v;
        this.crime = c;
        this.minimum_cost = mc;
        this.parent = p;
    }
}

