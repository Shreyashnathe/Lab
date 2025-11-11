package B2_SchedulingAlgo;
import java.util.*;

public class PriorityBased {
    static class Process {
        int pid, at, bt, pr, ct, tat, wt;
        boolean done;
        Process(int pid, int at, int bt, int pr) {
            this.pid = pid; this.at = at; this.bt = bt; this.pr = pr;
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter number of processes: ");
        int n = sc.nextInt();
        List<Process> list = new ArrayList<>();

        for (int i = 1; i <= n; i++) {
            System.out.print("Enter Arrival, Burst & Priority for P" + i + ": ");
            list.add(new Process(i, sc.nextInt(), sc.nextInt(), sc.nextInt()));
        }

        int time = 0, completed = 0;
        double totalWT = 0, totalTAT = 0;

        while (completed < n) {
            Process best = null;
            for (Process p : list)
                if (!p.done && p.at <= time && (best == null || p.pr < best.pr))
                    best = p;
            if (best == null) { time++; continue; }

            time += best.bt;
            best.ct = time;
            best.tat = best.ct - best.at;
            best.wt = best.tat - best.bt;
            best.done = true;
            totalWT += best.wt;
            totalTAT += best.tat;
            completed++;
        }

        System.out.println("\nPriority Scheduling (Non-Preemptive):");
        System.out.println("PID\tAT\tBT\tPR\tCT\tTAT\tWT");
        for (Process p : list)
            System.out.printf("P%d\t%d\t%d\t%d\t%d\t%d\t%d\n", p.pid, p.at, p.bt, p.pr, p.ct, p.tat, p.wt);

        System.out.printf("\nAverage Turnaround Time = %.2f", totalTAT / n);
        System.out.printf("\nAverage Waiting Time = %.2f\n", totalWT / n);
        sc.close();
    }
}
//0 5 1
//1 3 2
//2 8 1
//3 6 3