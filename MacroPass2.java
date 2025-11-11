import java.io.*;
import java.util.*;

class MntRow {
    String name; int pp, kp, mdtIdx, kpdIdx, pntIdx;
    MntRow(String n, int pp, int kp, int mdt, int kpd, int pnt) {
        this.name = n; this.pp = pp; this.kp = kp; this.mdtIdx = mdt;
        this.kpdIdx = kpd; this.pntIdx = pnt;
    }
}

public class MacroPass2 {
    Map<String, MntRow> mnt = new LinkedHashMap<>();
    List<String> mdt = new ArrayList<>(), pntab = new ArrayList<>(), expanded = new ArrayList<>();
    Map<String, String> kpdtab = new LinkedHashMap<>();

    public static void main(String[] args) {
        try {
            new MacroPass2().run();
            System.out.println("Macro Pass-II complete. Check expanded_code.asm");
        } catch (IOException e) { e.printStackTrace(); }
    }

    void run() throws IOException {
        loadMnt("mnt.txt"); loadMdt("mdt.txt"); loadPntab("pntab.txt"); loadKpdtab("kpdtab.txt");
        expand("pass1_intermediate.asm"); write("expanded_code.asm");
    }

    void loadMnt(String f) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            br.readLine(); br.readLine();
            for (String l; (l = br.readLine()) != null;) {
                String[] p = l.trim().split("\t");
                if (p.length >= 6)
                    mnt.put(p[0], new MntRow(p[0], i(p[1]), i(p[2]), i(p[3]), i(p[4]), i(p[5])));
            }
        }
    }

    void loadMdt(String f) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            br.readLine(); br.readLine();
            for (String l; (l = br.readLine()) != null;)
                mdt.add(l.split("\t", 2).length > 1 ? l.split("\t", 2)[1] : "");
        }
    }

    void loadPntab(String f) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            br.readLine(); br.readLine();
            for (String l; (l = br.readLine()) != null;) {
                String[] p = l.split("\t");
                if (p.length > 1) pntab.add(p[1]);
            }
        }
    }

    void loadKpdtab(String f) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            br.readLine(); br.readLine();
            for (String l; (l = br.readLine()) != null;) {
                String[] p = l.split("\t");
                if (p.length >= 2) kpdtab.put(p[1], p.length > 2 ? p[2] : "");
            }
        }
    }

    void expand(String f) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            for (String line; (line = br.readLine()) != null;) {
                line = line.trim(); if (line.isEmpty()) continue;
                String[] p = line.split(" ");
                if (mnt.containsKey(p[0])) expandMacro(p);
                else expanded.add(line);
            }
        }
    }

    void expandMacro(String[] call) {
        MntRow m = mnt.get(call[0]);
        Map<String, String> ala = new HashMap<>(), actual = new LinkedHashMap<>();

        int pos = 0;
        for (int i = 1; i < call.length; i++) {
            String arg = call[i];
            if (arg.contains("=")) {
                String[] kv = arg.split("=");
                actual.put(kv[0], kv.length > 1 ? kv[1] : "");
            } else {
                String formal = pntab.get(m.pntIdx + pos++);
                actual.put(formal, arg);
            }
        }

        int total = m.pp + m.kp;
        for (int i = 0; i < total; i++) {
            String f = pntab.get(m.pntIdx + i), posName = "?" + (i + 1);
            ala.put(posName, actual.getOrDefault(f, kpdtab.get(f)));
        }

        for (int i = m.mdtIdx + 1; !mdt.get(i).equals("MEND"); i++) {
            String line = mdt.get(i);
            for (var a : ala.entrySet())
                line = line.replaceAll("\\b" + a.getKey() + "\\b", a.getValue());
            expanded.add(line);
        }
    }

    void write(String f) throws IOException {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(f))) {
            for (String s : expanded) w.write(s + "\n");
        }
    }

    int i(String s) { return Integer.parseInt(s.trim()); }
}
