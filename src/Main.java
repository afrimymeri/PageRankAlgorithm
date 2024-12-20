import java.io.*;
import java.util.*;

public class Main{

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
}}
