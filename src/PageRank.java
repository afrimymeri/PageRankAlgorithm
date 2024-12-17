import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        return temporarily; // will change when the right method is added
    }
}
