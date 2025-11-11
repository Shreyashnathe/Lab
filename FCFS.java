package B2_SchedulingAlgo;
import java.util.*;

public class FCFS {
    static class Process {
        int pid, at, bt, ct, tat, wt;
        Process(int pid, int at, int bt) {
            this.pid = pid;
            this.at = at;
            this.bt = bt;
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

        list.sort(Comparator.comparingInt(p -> p.at));
        int currentTime = 0;
        double totalTAT = 0, totalWT = 0;

        System.out.println("\nFCFS Scheduling Results:");
        System.out.println("PID\tAT\tBT\tCT\tTAT\tWT");

        for (Process p : list) {
            if (currentTime < p.at) currentTime = p.at;
            currentTime += p.bt;
            p.ct = currentTime;
            p.tat = p.ct - p.at;  
            p.wt = p.tat - p.bt;  
            totalTAT += p.tat;
            totalWT += p.wt;
            System.out.printf("P%d\t%d\t%d\t%d\t%d\t%d\n", p.pid, p.at, p.bt, p.ct, p.tat, p.wt);
        }

        System.out.printf("\nAverage Turnaround Time (TAT) = %.2f", totalTAT / n);
        System.out.printf("\nAverage Waiting Time (WT) = %.2f\n", totalWT / n);
        sc.close();
    }
}
//0 5
//1 3
//2 8
//3 6