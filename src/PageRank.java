import java.io.*;
import java.util.*;

public class PageRank {
    private static final double DEFAULT_DAMPING_FACTOR = 0.85;
    private static final int MAX_ITERATIONS = 100;
    private static final double CONVERGENCE_THRESHOLD = 1e-6;

    public static Map<String, Double> computePageRank(Map<String, List<String>> graph, double dampingFactor) {
        int numNodes = graph.size();
        Map<String, Double> ranks = new HashMap<>();
        Map<String, Double> newRanks = new HashMap<>();

        //Initialize each node with an equal PageRank score
        for (String node : graph.keySet()) {
            ranks.put(node, 1.0 / numNodes);
        }
        for (int i = 0; i < MAX_ITERATIONS; i++) {
            for (String node : graph.keySet()) {
                double rankSum = 0.0;

                for (String neighbour : graph.keySet()) {
                    if (graph.get(neighbour).contains(node)) {
                        rankSum += ranks.get(neighbour) / graph.get(neighbour).size();
                    }
                }

                //Update rank with damping factor
                newRanks.put(node, (1 - dampingFactor) / numNodes + dampingFactor * rankSum);
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

            ranks.putAll(newRanks);
        }
        Map<String, Double> temporarily = new HashMap<>();
        return sortByValue(ranks);
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

    /**
     * Allows the user to input a graph manually via the console.
     */
    private static Map<String, List<String>> inputGraphManually(Scanner scanner) {
        Map<String, List<String>> graph = new HashMap<>();
        Set<String> allNodes = new HashSet<>();

        System.out.println("Enter the graph connections (format: Node -> Neighbor1,Neighbor2,...). Type 'done' to finish:");
        while (true) {
            String line = scanner.nextLine();
            if (line.equalsIgnoreCase("done")) break;

            String[] parts = line.split("->", 2);
            if (parts.length != 2) {
                System.out.println("Invalid format. Try again.");
                continue;
            }

            String node = parts[0].trim();
            List<String> neighbors = parts[1].trim().isEmpty() ? new ArrayList<>()
                    : Arrays.asList(parts[1].trim().split(","));

            graph.put(node, neighbors);
            allNodes.add(node);
            allNodes.addAll(neighbors);
        }

        for (String node : allNodes) {
            graph.putIfAbsent(node, new ArrayList<>());
        }

        return graph;
    }

    /**
     * Generates a Graphviz DOT file for visualization.
     */
    private static void generateVisualizationFile(Map<String, List<String>> graph, Map<String, Double> ranks) throws IOException {
        try (PrintWriter writer = new PrintWriter(new File("pagerank_graph.dot"))) {
            writer.println("digraph PageRank {");
            writer.println("    rankdir=LR;");
            writer.println("    node [shape=circle];");

            for (Map.Entry<String, List<String>> entry : graph.entrySet()) {
                for (String neighbor : entry.getValue()) {
                    writer.printf("    %s -> %s;\n", entry.getKey(), neighbor);
                }
            }

            for (Map.Entry<String, Double> entry : ranks.entrySet()) {
                writer.printf("    %s [label=\"%s\n%.6f\"];\n", entry.getKey(), entry.getKey(), entry.getValue());
            }

            writer.println("}");
        }
    }
}
