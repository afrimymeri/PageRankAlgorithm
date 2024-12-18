import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class PageRank {
    private static final double DEFAULT_DAMPING_FACTOR = 0.85;
    private static final int MAX_ITERATIONS = 100;
    private static final double CONVERGENCE_THRESHOLD = 1e-6;
    public static Map<String,Double> computePageRank(Map<String, List<String>> graph, double dampingFactor) {
        int numNodes = graph.size();
        Map<String, Double> ranks = new HashMap<>();
        Map<String, Double> newRanks = new HashMap<>();

        //Initialize each node with an equal PageRank score
        for (String node : graph.keySet()) {
            ranks.put(node, 1.0/ numNodes);
        }
        for (int i =0; i< MAX_ITERATIONS; i++) {
            for (String node : graph.keySet()) {
                double rankSum =0.0;

                //Contributions of the incoming nodes
                for (String neighbour : graph.keySet()) {
                    if (graph.get(neighbour).contains(node)){
                        rankSum += ranks.get(neighbour) / graph.get(neighbour).size();
                    }
                }

                //Update rank with damping factor
                newRanks.put(node, (1-dampingFactor)/numNodes + dampingFactor * rankSum);
            }

            //Check for convergence
            boolean converged = true;
            for (String node : graph.keySet()) {
                if (Math.abs(newRanks.get(node) - ranks.get(node)) > CONVERGENCE_THRESHOLD) {
                    converged = false;
                    break;
                }
            }
            if (converged) {
                break;
            }

            //Update ranks for the next iterations
            ranks.putAll(newRanks);
        }
        Map<String,Double> temporarily = new HashMap<>();
        return sortByValue(ranks); // will change when the right method is added
    }

    /**
     * Sorts a map by value in descending order.
     */
    private static Map<String, Double> sortByValue(Map<String, Double> map) {
        List<Map.Entry<String, Double>> entries = new ArrayList<>(map.entrySet());
        entries.sort((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()));

        Map<String, Double> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<String, Double> entry : entries) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }

    /**
     * Validates the input graph for basic issues.
     */
    private static boolean validateGraph(Map<String, List<String>> graph) {
        for (Map.Entry<String, List<String>> entry : graph.entrySet()) {
            if (entry.getValue().contains(entry.getKey())) {
                System.out.printf("Self-loop detected at node %s.\n", entry.getKey());
                return false;
            }
        }
        return !graph.isEmpty();
    }

    /**
     * Loads a graph from a file.
     */
    private static Map<String, List<String>> loadGraphFromFile(Scanner scanner) throws IOException {
        System.out.println("Enter the file path:");
        String filePath = scanner.nextLine();
        Map<String, List<String>> graph = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("->");
                if (parts.length != 2) continue;

                String node = parts[0].trim();
                List<String> neighbors = Arrays.asList(parts[1].trim().split(","));
                graph.put(node, neighbors);
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found.");
        }

        return graph;
    }
}
