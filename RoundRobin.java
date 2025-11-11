package B2_SchedulingAlgo;
import java.util.*;

public class RoundRobin {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter number of processes: ");
        int n = sc.nextInt();

        int[] at = new int[n], bt = new int[n], rt = new int[n], ct = new int[n], tat = new int[n], wt = new int[n];
        for (int i = 0; i < n; i++) {
            System.out.print("Enter Arrival & Burst Time for P" + (i + 1) + ": ");
            at[i] = sc.nextInt(); bt[i] = sc.nextInt(); rt[i] = bt[i];
        }

        System.out.print("Enter Time Quantum: ");
        int tq = sc.nextInt(), time = 0, done = 0;
        Queue<Integer> q = new LinkedList<>();

        while (done < n) {
            for (int i = 0; i < n; i++)
                if (at[i] <= time && rt[i] > 0 && !q.contains(i)) q.add(i);

            if (q.isEmpty()) { time++; continue; }

            int i = q.poll(), run = Math.min(tq, rt[i]);
            rt[i] -= run; time += run;

            for (int j = 0; j < n; j++)
                if (at[j] <= time && rt[j] > 0 && !q.contains(j)) q.add(j);

            if (rt[i] == 0) {
                ct[i] = time;
                tat[i] = ct[i] - at[i];
                wt[i] = tat[i] - bt[i];
                done++;
            } else q.add(i);
        }

        double avgTAT = 0, avgWT = 0;
        System.out.println("\nPID\tAT\tBT\tCT\tTAT\tWT");
        for (int i = 0; i < n; i++) {
            avgTAT += tat[i]; avgWT += wt[i];
            System.out.printf("P%d\t%d\t%d\t%d\t%d\t%d\n", i + 1, at[i], bt[i], ct[i], tat[i], wt[i]);
        }
        System.out.printf("\nAverage TAT = %.2f\nAverage WT = %.2f\n", avgTAT / n, avgWT / n);
        sc.close();
    }
}
//1 150
//2 100
//3 200
//4 50