package andrew.cmu.edu.abhineec;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/********************************************************************************************************
 * TSP_Tour class includes methods to solve the Travelling Salesman Problem
 * using an Approximation Algorithm where
 * adjMatrix - contains the adjacency matrix of all crime nodes between two nodes
 * nodes - contains (vertex in graph, minimum cost to reach to vertex, parent vertex) information for
 *              every node in graph
 * minimumSpanningTree - contains map of vertex and all its children in a min-heap
 * hamiltonianPath - contains hamiltonian cycle for created MST
 * hamiltonianPathCost - cost of travelling through the hamiltonian cycle
 * optimalPath - contains the minimal costing permutation for MST
 * optimalPathCost - cost of travelling through the optimalPath
 ********************************************************************************************************/
public class TSP_Tour {
    private static final String RESULTS_FILE_PATH = System.getProperty("user.dir") + "/" + "result.txt";
    private String KML_TSP_PATH_PREFIX = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n<kml\n\txmlns=\"http://earth.google.com/kml/2.2\">\n\t<Document>\n\t\t<name>Pittsburgh TSP</name>\n\t\t<description>TSP on Crime</description>\n\t\t<Style id=\"style6\">\n\t\t\t<LineStyle>\n\t\t\t\t<color>73FF0000</color>\n\t\t\t\t<width>5</width>\n\t\t\t</LineStyle>\n\t\t</Style>\n\t\t<Style id=\"style5\">\n\t\t\t<LineStyle>\n\t\t\t\t<color>507800F0</color>\n\t\t\t\t<width>5</width>\n\t\t\t</LineStyle>\n\t\t</Style>\n\t\t<Placemark>\n\t\t\t<name>TSP Path</name>\n\t\t\t<description>TSP Path</description>\n\t\t\t<styleUrl>#style6</styleUrl>\n\t\t\t<LineString>\n\t\t\t\t<tessellate>1</tessellate>\n\t\t\t\t<coordinates>";
    private String KML_OPTIMAL_PATH_PRFIX = "</coordinates>\n\t\t\t</LineString>\n\t\t</Placemark>\n\t\t<Placemark>\n\t\t\t<name>Optimal Path</name>\n\t\t\t<description>Optimal Path</description>\n\t\t\t<styleUrl>#style5</styleUrl>\n\t\t\t<LineString>\n\t\t\t\t<tessellate>1</tessellate>\n\t\t\t\t<coordinates>";
    private String KML_POSTFIX = "</coordinates>\n\t\t\t</LineString>\n\t\t</Placemark>\n\t</Document>\n</kml>";
    private static BufferedWriter resultsBufferedWriter;
    private Map<Integer, Heap> minimumSpanningTree;
    private double[][] adjMatrix;
    private List<Node> nodes;
    private List<Integer> hamiltonianPath;
    private double hamiltonianPathCost;
    private List<Integer> optimalPath;
    private double optimalPathCost;

    private long totalpermutationsvisited;

    private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yy");

    /**
     * Initializes a Graph for all crimes that happened between start and end nodes;
     * Creates a Minimum Spanning Tree(MST) for the initialized graph using Prim's algorithm
     * Finds the hamiltonian path for the evaluated MST
     * @param crimeDataLocation
     *      the file path for the crime data that needs to be loaded
     * @param start_date
     *      start date for crimes date range
     * @param end_date
     *      end date for crimes date range
     * @precondition
     *  The String crimeDataLocation contains the path to
     *  crime file formatted in the exact same way as Project 4's description
     * @postcondition
     *    The Graph is constructed; a
     *    viable MST has been created and
     *    a viable hamiltonian cycle has been found and printed
     **/
    public TSP_Tour(String crimeDataLocation, Date start_date, Date end_date) throws IOException, ParseException {

        nodes = new ArrayList<Node>();

        //extract all nodes between start and end nodes
        File fileObj = new File(crimeDataLocation);
        Scanner myReader = new Scanner(fileObj);
        boolean firstLine = true;
        int nodeCount = 0;
        System.out.println("\nCrime records between " + DATE_FORMAT.format(start_date)+ " and " + DATE_FORMAT.format(end_date));
        while(myReader.hasNextLine()) {
            //ignore the first line as it contains column headers
            if(firstLine) {
                firstLine = false;
                myReader.nextLine();
            }
            else{
                String line = myReader.nextLine();
                String[] data = line.split(",");
                Date crime_date = DATE_FORMAT.parse(data[5]);
                if(crime_date.compareTo(start_date) >=0 && crime_date.compareTo(end_date) <= 0){
                    System.out.println(line);
                    nodes.add(new Node(nodeCount++, new Crime(line), null, null));
                }
            }
        }
        myReader.close();
        //populate adjacency matrix
        populateAdjMatrix();

        //initialize cost of first node as 0
        //this is considered as the start node for
        //the salesman
        nodes.get(0).minimum_cost = 0.0;
        //find a viable MST using an approximation algorithm
        //also find a viable hamiltonian cycle
        buildMinimumSpanningTree(nodes.get(0));

        //evaluate all possible permutation of paths
        //starting and ending at vertex 0
        //store minimal costing path as optimal path
        totalpermutationsvisited = 0;
        System.out.println("\nLooking at every permutation to find the optimal solution");
        optimalPathCost = hamiltonianPathCost;
        optimalPath = hamiltonianPath;
        permute(new ArrayList<>());
        System.out.println("Total Permutations: "+totalpermutationsvisited);
        System.out.println("The best permutation");
        resultsBufferedWriter.write("\nOptimum Path\n");
        for(int v : optimalPath){
            System.out.print(v + " ");
            resultsBufferedWriter.write(v + " ");
        }
        resultsBufferedWriter.write("\nLength\n");
        System.out.println("\nOptimal Cycle length = " + (optimalPathCost * 0.00018939) + " miles");
        resultsBufferedWriter.write(String.valueOf(optimalPathCost * 0.00018939));
        resultsBufferedWriter.flush();

        //create a KML file representing
        //TSP Path and Optimum Path
        //for current input dates
        createKMLFile();
        return;
    }

    /**
     * Builds a Minimum Spanning Tree(MST) for the initialized graph using Prim's algorithm and
     * Finds the hamiltonian path for the evaluated MST
     * @precondition
     *  The graph corresponding crimes that happened btw input dates has been
     *  initialized as adjMatrix (Adjacency Matrix)
     * @postcondition
     *    A viable MST has been created and
     *    a viable hamiltonian cycle has been found and printed
     **/
    private void buildMinimumSpanningTree(Node startNode) throws IOException {
        //initialize minimumSpanningTree map for all vertexes as key
        //and empty heaps representing their children
        minimumSpanningTree = new HashMap<Integer, Heap>();
        for(int i = 0; i<adjMatrix.length; i++)
            minimumSpanningTree.put(i, new Heap());

        //initialize a heap which is utilized
        //to implement the deleteMin() operation in Prim's Algorithm
        Heap heap = new Heap();
        //initialize a boolean array that keeps track of nodes that have already been visited in Prim
        Boolean[] visited = new Boolean[adjMatrix.length];
        //add 0th node (start) in heap
        heap.add(startNode);

        Node nextNode = heap.peek();
        //until all nodes in graph have been visited
        while(!heap.isEmpty()){
            heap.remove();
            visited[nextNode.vertex] = true;

            //check the viability of travelling to all the neighbors
            for(int neighbor =0; neighbor<adjMatrix[nextNode.vertex].length; neighbor++){
                //if neigbor can be vvisited with less cost s=using current vertex
                //update the node's minimum_cost and parent
                if(visited[neighbor]==null && nodes.get(neighbor).minimum_cost == null){
                    nodes.get(neighbor).minimum_cost = adjMatrix[nextNode.vertex][neighbor];
                    nodes.get(neighbor).parent = nextNode.vertex;
                    //add node to heap and MST
                    heap.add(nodes.get(neighbor));
                    minimumSpanningTree.get(nextNode.vertex).add(nodes.get(neighbor));
                }
                else if(visited[neighbor]==null && Double.compare(nodes.get(neighbor).minimum_cost, adjMatrix[nextNode.vertex][neighbor]) > 0){
                    //remove node from its current position in heap and MST
                    heap.remove(nodes.get(neighbor));
                    minimumSpanningTree.get(nodes.get(neighbor).parent).remove(nodes.get(neighbor));
                    nodes.get(neighbor).minimum_cost = adjMatrix[nextNode.vertex][neighbor];
                    nodes.get(neighbor).parent = nextNode.vertex;
                    //add it back to the heap and MST
                    heap.add(nodes.get(neighbor));
                    minimumSpanningTree.get(nextNode.vertex).add(nodes.get(neighbor));
                }
            }
            //get the least costing unvisited edge as the next node to be visited
            nextNode = heap.peek();
        }

        //Evaluating pre-order walk
        List<Integer> preOrderWalk = new ArrayList<Integer>();
        getPreOrderWalk(minimumSpanningTree, 0, preOrderWalk);
//        System.out.print("Pre-order walk: ");
//        for(int v = 0; v<preOrderWalk.size(); v++){
//            if(v==preOrderWalk.size()-1)
//                System.out.println(preOrderWalk.get(v));
//            else
//                System.out.print(preOrderWalk.get(v) + " -> ");
//        }

        //evaluate a viable hamiltonian cycle
        hamiltonianPath = getHamiltonianCycle(preOrderWalk);
        System.out.print("\nHamiltonian Cycle (not necessarily optimum):\n");
        resultsBufferedWriter.write("\nHamiltonian Cycle\n");
        for(int v = 0; v<hamiltonianPath.size(); v++) {
            System.out.print(hamiltonianPath.get(v) + " ");
            resultsBufferedWriter.write(hamiltonianPath.get(v) + " ");
        }
        hamiltonianPathCost = getCostOfPath(hamiltonianPath);
        //remove end node before finding permutations
            //as it is always going to be determined by the start node
        hamiltonianPath.remove(hamiltonianPath.size()-1);
        System.out.println("\nLength Of cycle: "+(hamiltonianPathCost * 0.00018939) + " miles");
        resultsBufferedWriter.write("\nLength\n");
        resultsBufferedWriter.write(String.valueOf(hamiltonianPathCost * 0.00018939));
        resultsBufferedWriter.flush();
    }

    /**
     * Recursive function to find all permutations of an integer array
     * i.e. hamiltonianPath
     * @precondition
     *  hamiltonianPath has been populated with a viable hamiltonian path of MST
     *  except the start and end node in path (i.e. 0th node)
     * @postcondition
     *    All permutations have been evaluated and optimalPath
     *    represents a minimal costing path in graph and optimalCost
     *    representing the cost to travel the optimalPath
     **/
    private void permute(List<Integer> currentPath){
        //if all vertices have been added to current path
        if(currentPath.size() == hamiltonianPath.size()){
            //add the first node of the path as its end node
            List<Integer> possiblePath = new ArrayList<>(currentPath);
            possiblePath.add(possiblePath.get(0));
            //update optimal path and cost if current path's cost
            //is less than current optimal path
            double costOfCurrentPath = getCostOfPath(possiblePath);
//            System.out.println("Path: " + currentPath + " AND cost = "+costOfCurrentPath);
            totalpermutationsvisited++;
            if(Double.compare(optimalPathCost, costOfCurrentPath) > 0){
                optimalPathCost = costOfCurrentPath;
                optimalPath = new ArrayList<>(possiblePath);
            }
            return;
        }

        //for every unvisited vertex add it to currentPath
        //and find all its permutations at current position
        for(Integer v : hamiltonianPath){
            if(!currentPath.contains(v)){
                currentPath.add(v);
                permute(currentPath);
                //remove from current path
                currentPath.remove(v);
            }
        }
    }

    /**
     * Helper function to find cost of travelling a path
     **/
    private double getCostOfPath(List<Integer> path){
        double cost = 0;
        //update cost to include cost to travel form a node's parent to it
        //for every node in path
        for(int i =1; i<path.size(); i++)
            cost += adjMatrix[path.get(i-1)][path.get(i)];
        return cost;
    }

    /**
     * Helper function to find a viable hamiltonian cycle
     **/
    private List<Integer> getHamiltonianCycle(List<Integer> preOrderWalk){
        List<Integer> hamiltonianCycle = new LinkedList<>();
        //for each vertex in pre order walk
        for(int v : preOrderWalk){
            //add to hamiltonian cycle if the vertex hasn't been visited before
            if(!hamiltonianCycle.contains(v)) {
                hamiltonianCycle.add(v);
            }
        }
        //add a path back to the starting node
        hamiltonianCycle.add(preOrderWalk.get(0));
        return hamiltonianCycle;
    }

    /**
     * Helper recursive function to find a viable Pre-order walk
     **/
    private void getPreOrderWalk(Map<Integer, Heap> minimumSpanningTree, int startNode, List<Integer> preOrderWalk) {
        preOrderWalk.add(startNode);
        if(minimumSpanningTree.get(startNode).isEmpty())
            return;
        while(!minimumSpanningTree.get(startNode).isEmpty()){
            Node nextNode = minimumSpanningTree.get(startNode).remove();
            getPreOrderWalk(minimumSpanningTree, nextNode.vertex, preOrderWalk);
            preOrderWalk.add(startNode);
        }
    }

    /**
     * Helper function to populate the adjacency matrix for the graph
     **/
    private void populateAdjMatrix(){
        adjMatrix  = new double[nodes.size()][nodes.size()];
        for(int i = 0; i<nodes.size(); i++){
            Node iNode = nodes.get(i);
            for(int j = 0; j<nodes.size(); j++){
                Node jNode = nodes.get(j);
                if(iNode == jNode){
                    adjMatrix[i][j] = 0.0;
                }
                else{
                    adjMatrix[i][j] = calculateDistance(iNode.crime.x, iNode.crime.y, jNode.crime.x, jNode.crime.y);
                }
            }
        }
    }

    /**
     * Helper function to calculated distance between two points in a 2-d plane
     */
    private double calculateDistance(double x1, double y1, double x2, double y2){
        return Math.sqrt(Math.pow(x1-x2, 2) + Math.pow(y1-y2, 2));
    }

    /**
     * Helper function to create a KML file representing
     * TSP Path and Optimum Path for current input dates
     */
    private void createKMLFile() throws IOException {
        StringBuilder kml = new StringBuilder();
        //ADDING HAMILTONIAN PATH TO KML
        kml.append(KML_TSP_PATH_PREFIX);
        //append start node to kml
        kml.append("\n");
        kml.append(nodes.get(0).crime.longitude);
        kml.append(",");
        kml.append(nodes.get(0).crime.latitude);
        for(int v : hamiltonianPath){
            kml.append(",\n");
            kml.append(nodes.get(v).crime.longitude);
            kml.append(",");
            kml.append(nodes.get(v).crime.latitude);
        }
        //append end node's coordinates to kml
        kml.append(",\n");
        kml.append(nodes.get(0).crime.longitude);
        kml.append(",");
        kml.append(nodes.get(0).crime.latitude);

        //ADDING OPTIMAL PATH TO KML
        kml.append(KML_OPTIMAL_PATH_PRFIX);
        //append start node to kml
        kml.append("\n");
        kml.append(nodes.get(0).crime.longitude);
        kml.append(",");
        kml.append(nodes.get(0).crime.latitude);
        for(int v : optimalPath){
            kml.append(",\n");
            kml.append(nodes.get(v).crime.longitude);
            kml.append(",");
            kml.append(nodes.get(v).crime.latitude);
        }
        //append end node's coordinates to kml
        kml.append(",\n");
        kml.append(nodes.get(0).crime.longitude);
        kml.append(",");
        kml.append(nodes.get(0).crime.latitude);

        kml.append(KML_POSTFIX);

        String FILE_PATH = "PGHCrimes.KML";
        Path path = Paths.get(FILE_PATH);
        Files.writeString(path, kml,  StandardCharsets.UTF_8);
        System.out.println("\nKML Representation of Hamiltonian And Optimal Path:\n");
        System.out.println(kml);

    }

    public static void main(String[] a) throws ParseException, IOException {
        System.out.println("Hello and welcome!\n");
        System.out.println("This assignment was submitted by:\nName: Abhineet Chaudhary\nAndrewId: abhineec\nCourse: 95-771 A Fall 2023\n\n");

        String userDirectory = System.getProperty("user.dir");
        System.out.println("Please ensure your project directory is correct. It should be the absolute path till ...abhineecProject4/Travelling-Salesman-Project \nProject Directory: " + userDirectory + "\n");

        String crimeDataLocation = userDirectory + "/CrimeLatLonXY1990.csv";

        Scanner userScanner = new Scanner(System.in);

        resultsBufferedWriter = new BufferedWriter(new FileWriter(RESULTS_FILE_PATH, false));
        resultsBufferedWriter.write("abhineec\n");
        resultsBufferedWriter.flush();

        boolean USER_WANTS_TO_CONTINUE = true;
        int index = 1;
        while(USER_WANTS_TO_CONTINUE){
            if(index==1)
                resultsBufferedWriter.write("\nTestCase" + index++);
            else
                resultsBufferedWriter.write("\n\nTestCase" + index++);
            System.out.println("Enter start date");
            String start = userScanner.nextLine();
            System.out.println("Enter end date");
            String end = userScanner.nextLine();
            System.out.println();
            TSP_Tour tspTour = new TSP_Tour(crimeDataLocation, DATE_FORMAT.parse(start), DATE_FORMAT.parse(end));

            System.out.println("\nEnter 1 to continue, 0 to exit");
            int ip = Integer.valueOf(userScanner.nextLine());
            if(ip != 1)
                USER_WANTS_TO_CONTINUE = false;
        }
        resultsBufferedWriter.close();
    }

}
