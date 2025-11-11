package B4_PageReplacement;
import java.util.*;

public class FIFO_Optimal {
    static void fifo(int[] pages, int frames) {
        Set<Integer> set = new HashSet<>();
        Queue<Integer> q = new LinkedList<>();
        int faults = 0;

        for (int p : pages) {
            if (!set.contains(p)) {
                if (set.size() == frames)
                    set.remove(q.poll());
                set.add(p);
                q.add(p);
                faults++;
            }
        }
        System.out.println("FIFO Page Faults: " + faults);
    }

    static void optimal(int[] pages, int frames) {
        Set<Integer> set = new HashSet<>();
        int faults = 0;

        for (int i = 0; i < pages.length; i++) {
            int p = pages[i];
            if (!set.contains(p)) {
                if (set.size() == frames) {
                    int farthest = -1, val = -1;
                    for (int x : set) {
                        int j;
                        for (j = i + 1; j < pages.length; j++) {
                            if (pages[j] == x)
                                break;
                        }
                        if (j > farthest) {
                            farthest = j;
                            val = x;
                        }
                    }
                    set.remove(val);
                }
                set.add(p);
                faults++;
            }
        }
        System.out.println("Optimal Page Faults: " + faults);
    }

    public static void main(String[] args) {
        int[] pages = {7, 0, 1, 2, 0, 3, 0, 4, 2, 3, 0, 3, 2};
        int frames = 3;
        fifo(pages, frames);
        optimal(pages, frames);
    }
}
