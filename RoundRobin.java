package B2_SchedulingAlgo;
import java.util.*;

public class RoundRobin {
	static class Process {
	    int pid, at, bt, rt, ct, tat, wt;
	    Process(int pid, int at, int bt) {
	        this.pid = pid;
	        this.at = at;
	        this.bt = bt;
	        this.rt = bt;
	    }
	}
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter number of processes: ");
        int n = sc.nextInt();

        List<Process> list = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            System.out.print("Enter Arrival & Burst Time for P" + i + ": ");
            list.add(new Process(i, sc.nextInt(), sc.nextInt()));
        }

        System.out.print("Enter Time Quantum: ");
        int tq = sc.nextInt(), time = 0, done = 0;
        Queue<Process> q = new LinkedList<>();

        while (done < n) {
            for (Process p : list)
                if (p.at <= time && p.rt > 0 && !q.contains(p)) q.add(p);

            if (q.isEmpty()) { time++; continue; }

            Process p = q.poll();
            int exec = Math.min(tq, p.rt);
            p.rt -= exec; time += exec;

            for (Process np : list)
                if (np.at <= time && np.rt > 0 && !q.contains(np)) q.add(np);

            if (p.rt == 0) {
                p.ct = time;
                p.tat = p.ct - p.at;
                p.wt = p.tat - p.bt;
                done++;
            } else q.add(p);
        }

        double avgTAT = 0, avgWT = 0;
        System.out.println("\n--- Round Robin Scheduling ---");
        System.out.println("PID\tAT\tBT\tCT\tTAT\tWT");

        for (Process p : list) {
            avgTAT += p.tat;
            avgWT += p.wt;
            System.out.printf("P%d\t%d\t%d\t%d\t%d\t%d\n", p.pid, p.at, p.bt, p.ct, p.tat, p.wt);
        }

        System.out.printf("\nAverage TAT = %.2f\nAverage WT = %.2f\n", avgTAT / n, avgWT / n);
        sc.close();
    }
}

//1 150
//2 100
//3 200
//4 50
