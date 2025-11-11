package B4_PageReplacement;

import java.util.*;

public class FIFO_LRU {
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

    static void lru(int[] pages, int frames) {
        Set<Integer> set = new HashSet<>();
        Map<Integer, Integer> recent = new HashMap<>();
        int faults = 0;

        for (int i = 0; i < pages.length; i++) {
            int p = pages[i];
            if (!set.contains(p)) {
                if (set.size() == frames) {
                    int lru = Collections.min(recent.values());
                    int key = 0;
                    for (Map.Entry<Integer, Integer> entry : recent.entrySet()) {
                        if (entry.getValue() == lru)
                            key = entry.getKey();
                    }
                    set.remove(key);
                    recent.remove(key);
                }
                set.add(p);
                faults++;
            }
            recent.put(p, i);
        }
        System.out.println("LRU Page Faults: " + faults);
    }

    public static void main(String[] args) {
        int[] pages = {7, 0, 1, 2, 0, 3, 0, 4, 2, 3, 0, 3, 2};
        int frames = 3;
        fifo(pages, frames);
        lru(pages, frames);
    }
}
