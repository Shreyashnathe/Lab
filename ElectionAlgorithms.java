package ds_electionAlgo;
import java.util.*;

public class ElectionAlgorithms {
    static void ringElection(int[] p, int initiator) {
        System.out.println("\n--- Ring Election ---");
        int n = p.length, max = p[initiator];
        System.out.println("Initiator: " + p[initiator]);
        for (int i = 1; i < n; i++) {
            int next = (initiator + i) % n;
            System.out.println("Message passed to Process " + p[next]);
            if (p[next] > max) max = p[next];
        }
        System.out.println("Coordinator (highest ID): " + max);
    }

    static void bullyElection(int[] p, int initiator) {
        System.out.println("\n--- Bully Election ---");
        System.out.println("Initiator: " + p[initiator]);
        for (int i = initiator + 1; i < p.length; i++)
            System.out.println("Election message sent from " + p[initiator] + " to " + p[i]);
        System.out.println("Processes with higher IDs respond...");
        System.out.println("Coordinator (highest ID): " + p[p.length - 1]);
    }

    public static void main(String[] args) {
        int[] processes = {1, 2, 3, 4, 5};
        int initiator = 1; // Process 3 starts the election
        ringElection(processes, initiator);
        bullyElection(processes, initiator);
    }
}
