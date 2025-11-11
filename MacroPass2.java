import java.io.*;
import java.util.*;

class MntRow {
    String name; int pp, kp, mdtIdx, kpdIdx, pntIdx;
    MntRow(String n, int pp, int kp, int mdt, int kpd, int pnt) {
        name = n; this.pp = pp; this.kp = kp; this.mdtIdx = mdt;
        this.kpdIdx = kpd; this.pntIdx = pnt;
    }
}

public class MacroPass2 {
    Map<String, MntRow> mnt = new LinkedHashMap<>();
    List<String> mdt = new ArrayList<>(), pntab = new ArrayList<>(), expanded = new ArrayList<>();
    Map<String, String> kpdtab = new LinkedHashMap<>();

    public static void main(String[] args) throws Exception {
        new MacroPass2().run();
        System.out.println("Macro Pass-2 done. Check expanded_code.asm");
    }

    void run() throws Exception {
        loadMnt(); loadMdt(); loadPntab(); loadKpdtab();
        expand("pass1_intermediate.asm");
        try (BufferedWriter w = new BufferedWriter(new FileWriter("expanded_code.asm"))) {
            for (String s : expanded) w.write(s + "\n");
        }
    }

    void loadMnt() throws Exception {
        try (BufferedReader br = new BufferedReader(new FileReader("mnt.txt"))) {
            br.readLine(); br.readLine();
            String l;
            while ((l = br.readLine()) != null) {
                String[] p = l.trim().split("\\s+");
                if (p.length >= 6)
                    mnt.put(p[0], new MntRow(p[0], i(p[1]), i(p[2]), i(p[3]), i(p[4]), i(p[5])));
            }
        }
    }

    void loadMdt() throws Exception {
        try (BufferedReader br = new BufferedReader(new FileReader("mdt.txt"))) {
            br.readLine(); br.readLine();
            String l;
            while ((l = br.readLine()) != null)
                mdt.add(l.split("\\t", 2).length > 1 ? l.split("\\t", 2)[1] : "");
        }
    }

    void loadPntab() throws Exception {
        try (BufferedReader br = new BufferedReader(new FileReader("pntab.txt"))) {
            br.readLine(); br.readLine();
            String l;
            while ((l = br.readLine()) != null)
                pntab.add(l.split("\\t", 2)[1]);
        }
    }

    void loadKpdtab() throws Exception {
        try (BufferedReader br = new BufferedReader(new FileReader("kpdtab.txt"))) {
            br.readLine(); br.readLine();
            String l;
            while ((l = br.readLine()) != null) {
                String[] p = l.split("\\t");
                if (p.length >= 2) kpdtab.put(p[1], p.length > 2 ? p[2] : "");
            }
        }
    }

    void expand(String file) throws Exception {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            for (String line; (line = br.readLine()) != null; ) {
                line = line.trim().replaceAll("\\s+", " ");
                if (line.isEmpty()) continue;

                String[] parts = line.split(" ");
                String name = (parts[0].matches("\\d+") && parts.length > 1) ? parts[1] : parts[0];

                if (mnt.containsKey(name))
                    expandMacro(parts, name);
                else
                    expanded.add(line);
            }
        }
    }

    void expandMacro(String[] call, String name) {
        MntRow m = mnt.get(name);
        Map<String, String> actual = new LinkedHashMap<>();

        int start = (call[0].matches("\\d+") && call.length > 1) ? 2 : 1;
        int pos = 0;
        for (int i = start; i < call.length; i++) {
            String arg = call[i].replace(",", ""); // Clean commas
            if (arg.contains("=")) {
                String[] kv = arg.split("=");
                actual.put(kv[0], kv.length > 1 ? kv[1] : "");
            } else {
                String formal = pntab.get(m.pntIdx + pos++);
                actual.put(formal, arg);
            }
        }

        Map<String, String> ala = new HashMap<>();
        int total = m.pp + m.kp;
        for (int i = 0; i < total; i++) {
            String formal = pntab.get(m.pntIdx + i);
            ala.put("?" + (i + 1), actual.getOrDefault(formal, kpdtab.get(formal)));
        }

        for (int i = m.mdtIdx + 1; !mdt.get(i).equals("MEND"); i++) {
            String l = mdt.get(i);
            for (var e : ala.entrySet()) {
                // Use .replace() for literal replacement of "?1", "?2", etc.
                l = l.replace(e.getKey(), e.getValue());
            }
            expanded.add(l);
        }
    }

    int i(String s) { return Integer.parseInt(s.trim()); }
}
