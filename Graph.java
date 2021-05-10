import java.util.*;

public class Graph implements GraphInterface<Town, Road> {
    private HashSet<Town> vertices;
    private HashSet<Road> edges;
    private ArrayList<String> shortestPath;

    public Graph() {
        vertices = new HashSet<>();
        edges = new HashSet<>();
    }

    @Override public Road getEdge(Town sourceVertex, Town destinationVertex) {
        if (sourceVertex == null || destinationVertex == null) {
            return null;
        }

        for (Road r : edges) {
            if ((r.contains(sourceVertex) && r.contains(destinationVertex))) {
                //new Road(sourceVertex, destinationVertex, r.getWeight(), r.getName());
                return r;
            }
        }
        return null;
    }

    @Override public Road addEdge(Town sourceVertex, Town destinationVertex, int weight,
                                  String description) {

        if (!vertices.contains(sourceVertex) || !vertices.contains(destinationVertex)) {
            throw new IllegalArgumentException(
                    "source or target vertices are not found in the graph");
        }

        if (sourceVertex == null || destinationVertex == null) {
            throw new NullPointerException("source or target vertices are null");
        }

        Road newEdge = new Road(sourceVertex, destinationVertex, weight, description);
        boolean added = edges.add(newEdge);

        for (Town n : vertices) {
            if (n.equals(sourceVertex)) {
                n.getAdjacentTowns().add(destinationVertex);
                sourceVertex.getAdjacentTowns().add(destinationVertex);
            } else if (n.equals(destinationVertex)) {
                n.getAdjacentTowns().add(sourceVertex);
                destinationVertex.getAdjacentTowns().add(sourceVertex);
            }
        }
        if (!added) {
            return null;
        }
        return newEdge;
    }

    @Override public boolean addVertex(Town town) {
        if (town == null) {
            throw new NullPointerException("vertex is null");
        }

        for (Town t : vertices) {
            if (t.equals(town)) {
                return false;
            }
        }

        return vertices.add(town);
    }

    @Override public boolean containsEdge(Town sourceVertex, Town destinationVertex) {
        if (sourceVertex == null || destinationVertex == null) {
            return false;
        }

        for (Road r : edges) {
            if (r.getSource().equals(sourceVertex) && r.getDestination()
                    .equals(destinationVertex)) {
                return true;
            }
        }
        return false;
    }

    @Override public boolean containsVertex(Town town) {
        for (Town t : vertices) {
            if (t.equals(town)) {
                return true;
            }
        }
        return false;
    }

    @Override public Set<Road> edgeSet() {
        return edges;
    }

    @Override public Set<Road> edgesOf(Town vertex) {
        if (vertex == null) {
            throw new NullPointerException("vertex is null");
        }

        if (!vertices.contains(vertex)) {
            throw new NullPointerException("vertex is not found in the graph");
        }

        Set<Road> resultSet = new HashSet<>();
        for (Road r : edges) {
            Town source = r.getSource();
            Town dest = r.getDestination();
            if (source.equals(vertex) || dest.equals(vertex)) {
                resultSet.add(r);
            }
        }

        return resultSet;
    }

    @Override public Road removeEdge(Town sourceVertex, Town destinationVertex, int weight,
                                     String description) {

        Road toRemove = null;
        for (Road r : edges) {
            if (r.getSource().equals(sourceVertex) && r.getDestination()
                    .equals(destinationVertex)) {

                boolean match = false;
                if (weight > 1) {
                    match = r.getWeight() == weight;
                }

                if (description != null) {
                    match = r.getName().equals(description);
                }

                if (match) {
                    toRemove = r;
                }
            }
        }
        if (toRemove != null) {
            edges.remove(toRemove);
        }
        return toRemove;
    }

    @Override public boolean removeVertex(Town town) {
        if (town == null || !vertices.contains(town)) {
            return false;
        }

        vertices.remove(town);
        for (Road r : edges) {
            if (r.getSource().equals(town) || r.getDestination().equals(town)) {
                removeEdge(r.getSource(), r.getDestination(), r.getWeight(), r.getName());
                break;
            }
        }
        return true;
    }

    @Override public Set<Town> vertexSet() {
        return vertices;
    }

    @Override public ArrayList<String> shortestPath(Town sourceVertex, Town destinationVertex) {
        dijkstraShortestPath(sourceVertex);
        ArrayList<String> result = new ArrayList<>();

        //path from the source to the destination
        Stack<String> path = new Stack<>();
        String currentNode = "";
        boolean pathExists = false;

        for (String s : shortestPath) {
            if (s.contains("to " + destinationVertex.getName())) {
                path.push(s);
                currentNode = s.substring(0, s.indexOf(" via"));
                pathExists = true;
                break;
            }
        }

        if (!pathExists) {
            return null;
        }

        while (!currentNode.equals(sourceVertex.getName())) {
            for (String s : shortestPath) {
                if (s.contains("to " + currentNode)) {
                    path.push(s);
                    currentNode = s
                            .substring(0, s.indexOf(" via"));
                    break;
                }
            }
        }

        //Gets the path to the destination
        while (!path.empty()) {
            result.add(path.pop());
        }

        return result;
    }

    private int getInteger(String str) {
        for (int i = str.indexOf("mi") - 2; i > -1; i--) {
            if (str.charAt(i) == ' ')
                return Integer.parseInt(str.substring(i + 1, str.indexOf("mi") - 1));
        }
        return -1;
    }

    private int getTotalWeight(String str, Town sourceVertex) {
        String currentNode = str.substring(0, str.indexOf(" via"));
        Stack<String> path = new Stack<>();
        int weight = 0;
        path.push(str);

        while (!currentNode.equals(sourceVertex.getName())) {
            for (int i = 0; i < shortestPath.size(); i++) {
                if (shortestPath.get(i).contains("to " + currentNode)) {
                    path.push(shortestPath.get(i));
                    currentNode = shortestPath.get(i)
                            .substring(0, shortestPath.get(i).indexOf(" via"));
                    break;
                }
            }
        }
        while (!path.empty())
            weight += getInteger(path.pop());
        return weight;
    }

    @Override public void dijkstraShortestPath(Town sourceVertex) {
        shortestPath = new ArrayList<>();
        HashSet<Town> containedVertices = new HashSet<>();
        HashSet<Road> containedEdges = new HashSet<>();
        HashSet<Road> possibleEdges = new HashSet<>();
        HashSet<Road> currentEdges = new HashSet<>();
        HashSet<Town> toBeRemoved = new HashSet<>();
        Queue<Town> vertexQueue = new PriorityQueue<>();

        shortestPath
                .add(sourceVertex.getName() + " via NONE to " + sourceVertex.getName() + " 0 mi");
        containedVertices.add(sourceVertex);
        vertexQueue.add(sourceVertex);

        /*
         * Inserts the notation for the neighbors of the source node
         */
        currentEdges = (HashSet<Road>) edgesOf(sourceVertex);
        for (Road r : currentEdges) {
            Town destination;
            if (r.getSource().equals(sourceVertex))
                destination = r.getDestination();
            else
                destination = r.getSource();
            shortestPath.add(sourceVertex.getName() + " via " + r.getName() +
                    " to " + destination.getName() + " " + r.getWeight() + " mi");
        }

        //BEGIN ALGORITHM
        while ((containedVertices.size() != vertices.size())) {
            /*
             * Gets the minimum road for each node in vertexQueue
             * and puts them in possibleEdges
             */
            for (Town t : vertexQueue) {
                currentEdges = (HashSet<Road>) edgesOf(t);
                int minWeight = Integer.MAX_VALUE;
                Road minimum = null;

                /*
                 * Gets the minimum road for the node
                 */
                for (Road r : currentEdges) {

                    if ((r.getWeight() < minWeight) && !containedEdges.contains(r)
                            && !(containedVertices.contains(r.getSource())
                            && containedVertices.contains(r.getDestination()))) {
                        minimum = r;
                        minWeight = r.getWeight();
                    }
                }
                if (minimum != null) {
                    if (minimum.getSource().equals(t))
                        possibleEdges.add(minimum);
                    else
                        possibleEdges.add(new Road(minimum.getDestination(), minimum.getSource(),
                                minimum.getWeight(), minimum.getName()));
                } else
                    toBeRemoved.add(t);
            }

            //remove nodes to be removed
            for (Town t : toBeRemoved)
                vertexQueue.remove(t);

            /*
             * Gets the smallest edge, adds the new node to the queue
             * and containedEdges, and does notation on the shortest path
             * to the neighbors of said node
             */
            int min = Integer.MAX_VALUE;
            Road minimum = null;

            for (Road r : possibleEdges) {
                if (r.getWeight() < min) {
                    minimum = r;
                    min = r.getWeight();
                }
            }

            //Notation time
            if (minimum != null) {

                //Gets the neighboring node and adds it to the queue and containedVertices
                containedEdges.add(minimum);
                Town newNode = minimum.getDestination();
                containedVertices.add(newNode);
                vertexQueue.add(newNode);

                //Notation starts here
                currentEdges = (HashSet<Road>) edgesOf(newNode);

                for (Road r : currentEdges) {

                    //Gets name of neighbor
                    String nextNodeName;
                    if (r.getSource().equals(newNode))
                        nextNodeName = r.getDestination().getName();
                    else
                        nextNodeName = r.getSource().getName();

                    //Determines if there is a notation for the neighbor already in shortestPath
                    int notationIndex = -1;
                    for (int i = 0; i < shortestPath.size(); i++) {
                        if (shortestPath.get(i).contains("to " + nextNodeName)) {
                            notationIndex = i;
                            break;
                        }
                    }

                    //If there is no notation present
                    if (notationIndex == -1) {
                        //Add notation of path from source to neighbor through the newly added node
                        shortestPath.add(newNode.getName() + " via " + r.getName() + " to "
                                + nextNodeName + " "
                                + (r.getWeight()) + " mi");
                    } else {

                        //gets the weight of both nodes
                        int neighborWeight = getTotalWeight(shortestPath.get(notationIndex),
                                sourceVertex);
                        int newNodeWeight = -1;
                        for (String s : shortestPath) {
                            if (s.contains("to " + newNode.getName())) {
                                newNodeWeight = getTotalWeight(s, sourceVertex);
                                break;
                            }
                        }

                        //If a new shortest path from the new node to the neighbor has been found,
                        //overwrite that data
                        if (newNodeWeight + r.getWeight() < neighborWeight) {
                            shortestPath.remove(notationIndex);
                            shortestPath.add(newNode.getName() + " via " + r.getName() +
                                    " to " + nextNodeName + " " + (r.getWeight()) + " mi");
                        }
                    }
                }
                //END NOTATION LOOP
            } else
                break;
            possibleEdges.clear();
            toBeRemoved.clear();
        }
    }
}
