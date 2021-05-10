import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.Set;

public class TownGraphManager implements TownGraphManagerInterface {
    private Graph graph = new Graph();

    /**
     * Adds a road with 2 towns and a road name
     *
     * @param town1    name of town 1 (lastname, firstname)
     * @param town2    name of town 2 (lastname, firstname)
     * @param weight
     * @param roadName name of road
     * @return true if the road was added successfully
     */
    @Override
    public boolean addRoad(String town1, String town2, int weight, String roadName) {
        Town source = new Town(town1);
        Town destination = new Town(town2);
        graph.addVertex(source);
        graph.addVertex(destination);
        Road result = graph.addEdge(source, destination, weight, roadName);
        return result != null;

    }

    /**
     * Returns the name of the road that both towns are connected through
     *
     * @param town1 name of town 1 (lastname, firstname)
     * @param town2 name of town 2 (lastname, firstname)
     * @return name of road if town 1 and town2 are in the same road, returns null if not
     */
    @Override
    public String getRoad(String town1, String town2) {
        Town source = new Town(town1);
        Town dest = new Town(town2);
        Road road = graph.getEdge(source, dest);
        if (road == null) {
            return null;
        }
        return road.getName();
    }

    /**
     * Adds a town to the graph
     *
     * @param v the town's name  (lastname, firstname)
     * @return true if the town was successfully added, false if not
     */
    @Override
    public boolean addTown(String v) {
        Town town = new Town(v);
        return graph.addVertex(town);
    }

    /**
     * Gets a town with a given name
     *
     * @param name the town's name
     * @return the Town specified by the name, or null if town does not exist
     */
    @Override
    public Town getTown(String name) {
        Town town = new Town(name);
        Set<Town> vertices = graph.vertexSet();
        for (Town t : vertices) {
            if (t.equals(town)) {
                return t;
            }
        }
        return null;
    }

    /**
     * Determines if a town is already in the graph
     *
     * @param v the town's name
     * @return true if the town is in the graph, false if not
     */
    @Override
    public boolean containsTown(String v) {
        Town town = new Town(v);
        return graph.containsVertex(town);
    }

    /**
     * Determines if a road is in the graph
     *
     * @param town1 name of town 1 (lastname, firstname)
     * @param town2 name of town 2 (lastname, firstname)
     * @return true if the road is in the graph, false if not
     */
    @Override
    public boolean containsRoadConnection(String town1, String town2) {
        return graph.containsEdge(new Town(town1), new Town(town2));
    }

    /**
     * Creates an arraylist of all road titles in sorted order by road name
     *
     * @return an arraylist of all road titles in sorted order by road name
     */
    @Override
    public ArrayList<String> allRoads() {
        ArrayList<String> roads = new ArrayList<>();
        for (Road r : graph.edgeSet()) {
            roads.add(r.getName());
        }
        Collections.sort(roads);
        return roads;
    }

    /**
     * Deletes a road from the graph
     *
     * @param town1 name of town 1 (lastname, firstname)
     * @param town2 name of town 2 (lastname, firstname)
     * @param road
     * @return true if the road was successfully deleted, false if not
     */
    @Override
    public boolean deleteRoadConnection(String town1, String town2, String road) {
        Town source = new Town(town1);
        Town destination = new Town(town2);
        Road target = graph.getEdge(source, destination);
        if (target == null) {
            return false;
        }
        graph.removeEdge(source, destination, target.getWeight(), road);
        return true;

    }

    /**
     * Deletes a town from the graph
     *
     * @param v name of town (lastname, firstname)
     * @return true if the town was successfully deleted, false if not
     */
    @Override
    public boolean deleteTown(String v) {
        return graph.removeVertex(new Town(v));
    }

    /**
     * Creates an arraylist of all towns in alphabetical order (last name, first name)
     *
     * @return an arraylist of all towns in alphabetical order (last name, first name)
     */
    @Override
    public ArrayList<String> allTowns() {
        Set<Town> towns = graph.vertexSet();
        ArrayList<String> result = new ArrayList<>();
        for (Town t : towns) {
            result.add(t.getName());
        }
        Collections.sort(result);
        return result;

    }

    /**
     * Returns the shortest path from town 1 to town 2
     *
     * @param town1 name of town 1 (lastname, firstname)
     * @param town2 name of town 2 (lastname, firstname)
     * @return an Arraylist of roads connecting the two towns together, null if the
     * towns have no path to connect them.
     */
    @Override
    public ArrayList<String> getPath(String town1, String town2) {
        Town source = new Town(town1);
        Town destination = new Town(town2);
        ArrayList<String> result;
        try {
            if ((graph.containsVertex(source) && graph.containsVertex(destination))
                    && (!graph.edgesOf(source).isEmpty() && !graph.edgesOf(destination)
                    .isEmpty())) {
                result = graph.shortestPath(source, destination);
                if (result == null) {
                    return new ArrayList<>();
                }
                return result;
            }
        } catch (Exception e) {
            return new ArrayList<>();
        }
        return new ArrayList<>();
    }

    public void populateTownGraph(File file) throws IOException {
        try {
            Scanner inFile = new Scanner(file);
            String currentLine;
            String name;
            int weight;
            String source;
            String destination;

            while (inFile.hasNext()) {
                currentLine = inFile.nextLine();

                name = currentLine.substring(0, currentLine.indexOf(','));

                weight = Integer.parseInt(currentLine.substring(currentLine.indexOf(',') + 1,
                        currentLine.indexOf(';')));

                source = currentLine.substring(currentLine.indexOf(';') + 1);
                source = source.substring(0, source.indexOf(';'));

                destination = currentLine.substring(currentLine.indexOf(';') + 1);
                destination = destination.substring(destination.indexOf(';') + 1);

                Town sourceTown = new Town(source);
                Town destinationTown = new Town(destination);
                if (!graph.containsVertex(sourceTown))
                    graph.addVertex(sourceTown);
                if (!graph.containsVertex(destinationTown))
                    graph.addVertex(destinationTown);
                if (!graph.containsEdge(sourceTown, destinationTown))
                    graph.addEdge(sourceTown, destinationTown, weight, name);
            }
        } catch (Exception e) {
            System.out.println("error in readFile");
            e.printStackTrace();
        }

    }
}
