package B2_SchedulingAlgo;
import java.util.*;

public class SJF_Preemptive {
    static class Process {
        int pid, at, bt, rt, ct, tat, wt;
        Process(int pid, int at, int bt) {
            this.pid = pid; this.at = at; this.bt = bt; this.rt = bt;
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter number of processes: ");
        int n = sc.nextInt();
        List<Process> list = new ArrayList<>();

        for (int i = 1; i <= n; i++) {
            System.out.print("Enter Arrival Time and Burst Time for Process " + i + ": ");
            list.add(new Process(i, sc.nextInt(), sc.nextInt()));
        }

        int time = 0, completed = 0;
        double totalTAT = 0, totalWT = 0;

        System.out.println("\nSJF (Preemptive) Scheduling:");
        System.out.println("PID\tAT\tBT\tCT\tTAT\tWT");

        while (completed < n) {
            Process sj = null;
            int minRT = Integer.MAX_VALUE;

            for (Process p : list)
                if (p.at <= time && p.rt > 0 && p.rt < minRT) { sj = p; minRT = p.rt; }

            if (sj == null) { time++; continue; }

            sj.rt--;
            time++;

            if (sj.rt == 0) {
                sj.ct = time;
                sj.tat = sj.ct - sj.at;
                sj.wt = sj.tat - sj.bt;
                completed++;
                totalTAT += sj.tat;
                totalWT += sj.wt;
            }
        }

        list.sort(Comparator.comparingInt(p -> p.pid));
        for (Process p : list) System.out.printf("P%d\t%d\t%d\t%d\t%d\t%d\n", p.pid, p.at, p.bt, p.ct, p.tat, p.wt);
        System.out.printf("\nAverage Turnaround Time = %.2f", totalTAT / n);
        System.out.printf("\nAverage Waiting Time = %.2f\n", totalWT / n);
        sc.close();
    }
}
//0 300
//0 125
//0 400
//0 150
//0 100
//150 50
