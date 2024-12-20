import java.io.*;
import java.util.*;

public class PageRank {
    private static final double DEFAULT_DAMPING_FACTOR = 0.85;
    private static final int MAX_ITERATIONS = 100;
    private static final double CONVERGENCE_THRESHOLD = 1e-6;
    
public static void main(String[] args) throws IOException {
    System.out.println("Welcome to the PageRank Calculator!");
    System.out.println("Choose an option to input the graph:");
    System.out.println("1. Manual Input\n2. Load from File");

    Scanner scanner = new Scanner(System.in);
    int choice = scanner.nextInt();
    scanner.nextLine();

    Map<String, List<String>> graph = new HashMap<>();

    switch (choice) {
        case 1:
            graph = inputGraphManually(scanner);
            break;
        case 2:
            graph = loadGraphFromFile(scanner);
            break;
        default:
            System.out.println("Invalid choice. Exiting.");
            return;
    }

    if (!validateGraph(graph)) {
        System.out.println("Invalid graph input. Please check for issues like self-loops or empty nodes.");
        return;
    }

    long startTime = System.currentTimeMillis();
    Map<String, Double> ranks = computePageRank(graph, DEFAULT_DAMPING_FACTOR);
    long endTime = System.currentTimeMillis();

    System.out.println("\nInfluence Scores (PageRank):");
    for (Map.Entry<String, Double> entry : ranks.entrySet()) {
        System.out.printf("%s: %.6f\n", entry.getKey(), entry.getValue());
    }

    System.out.printf("\nConvergence achieved in %.3f seconds.\n", (endTime - startTime) / 1000.0);

    System.out.println("Would you like to generate a visualization file? (yes/no)");
    String visualize = scanner.nextLine();
    if (visualize.equalsIgnoreCase("yes")) {
        generateVisualizationFile(graph, ranks);
        System.out.println("Visualization file 'pagerank_graph.dot' generated. Use Graphviz to visualize.");
    }
}
    public static Map<String, Double> computePageRank(Map<String, List<String>> graph, double dampingFactor) {
        int numNodes = graph.size();
        Map<String, Double> ranks = new HashMap<>();
        Map<String, Double> newRanks = new HashMap<>();

        //Initialize each node with an equal PageRank score
        for (String node : graph.keySet()) {
            ranks.put(node, 1.0 / numNodes);
        }

        Set<String> danglingNodes = new HashSet<>();
        for (Map.Entry<String, List<String>> entry : graph.entrySet()) {
            if (entry.getValue().isEmpty()) {
                danglingNodes.add(entry.getKey());
            }
        }

        for (int i = 0; i < MAX_ITERATIONS; i++) {
            double danglingContribution = 0.0;
            for (String danglingNode: danglingNodes) {
                danglingContribution += ranks.get(danglingNode)/numNodes;
            }
            for (String node : graph.keySet()) {
                double rankSum = 0.0;

                for (Map.Entry<String, List<String>> entry : graph.entrySet()) {
                    String neighbour = entry.getKey();
                    if (entry.getValue().contains(node)) {
                        rankSum += ranks.get(neighbour) / entry.getValue().size();
                    }
                }

                //Update rank with damping factor
                newRanks.put(node, (1 - dampingFactor) / numNodes + dampingFactor * (rankSum+danglingContribution));
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
                    writer.printf("    %s -> %s\n", entry.getKey(), neighbor);
                }
            }

            for (Map.Entry<String, Double> entry : ranks.entrySet()) {
                writer.printf("    %s [label=\"%s\n%.6f\"];\n", entry.getKey(), entry.getKey(), entry.getValue());
            }

            writer.println("}");
        }
    }
}
