import java.io.*;
import java.util.*;

public class MacroPass1 {
    Map<String, String> mnt = new LinkedHashMap<>();     // Macro Name Table
    List<String> mdt = new ArrayList<>();                // Macro Definition Table
    List<String> pntab = new ArrayList<>();              // Parameter Name Table
    Map<String, String> kpdtab = new LinkedHashMap<>();  // Keyword Param Default Table
    List<String> ic = new ArrayList<>();                 // Intermediate Code
    Map<String, String> ala = new HashMap<>();           // Argument List Array

    boolean inMacro = false;
    int mdtIdx = 0;

    public static void main(String[] args) {
        try {
            new MacroPass1().run("macro_input.asm");
            System.out.println("Macro Pass-1 complete. Check output files.");
        } catch (IOException e) { e.printStackTrace(); }
    }

    void run(String file) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            for (String line; (line = br.readLine()) != null; ) {
                line = line.trim().replaceAll(",", " ").replaceAll("\\s+", " ");
                if (line.isEmpty() || line.startsWith(";")) continue;

                String[] p = line.split(" ");
                if (p[0].equals("MACRO")) {
                    inMacro = true;
                    processPrototype(br.readLine().trim().replaceAll(",", " ").replaceAll("\\s+", " "));
                } else if (p[0].equals("MEND")) {
                    inMacro = false;
                    mdt.add("MEND"); mdtIdx++; ala.clear();
                } else if (inMacro) {
                    mdt.add(substitute(line)); mdtIdx++;
                } else {
                    ic.add(line);
                }
            }
        }
        writeAll();
    }

    void processPrototype(String line) {
        ala.clear();
        String[] parts = line.split(" ");
        String name = parts[0];
        int pp = 0, kp = 0, pntStart = pntab.size(), kpdStart = kpdtab.size();
        List<String> params = new ArrayList<>();

        for (int i = 1; i < parts.length; i++) {
            String param = parts[i];
            String pname = param.split("=")[0];
            pntab.add(pname);
            params.add(pname);
            if (param.contains("=")) {
                kp++;
                String def = param.contains("=") ? param.split("=")[1] : "";
                kpdtab.put(pname, def);
            } else pp++;
        }
        mnt.put(name, pp + "," + kp + "," + mdtIdx + "," + kpdStart + "," + pntStart);
        for (int i = 0; i < params.size(); i++) ala.put(params.get(i), "?" + (i + 1));
        mdt.add(substitute(line)); mdtIdx++;
    }

    String substitute(String line) {
        for (String arg : ala.keySet())
            line = line.replaceAll("\\b" + arg + "\\b", ala.get(arg));
        return line;
    }

    void writeAll() throws IOException {
        write("mnt.txt", "--- MNT ---\nName\t#PP\t#KP\tMDT\tKPD\tPNT", 
            mnt.entrySet().stream().map(e -> {
                String[] p = e.getValue().split(",");
                return e.getKey() + "\t" + String.join("\t", p);
            }).toList());
        write("mdt.txt", "--- MDT ---\nIdx\tDefinition",
            indexList(mdt));
        write("pntab.txt", "--- PNTAB ---\nIdx\tParam", 
            indexList(pntab));
        write("kpdtab.txt", "--- KPDTAB ---\nIdx\tParam\tDefault",
            indexMap(kpdtab));
        write("pass1_intermediate.asm", null, ic);
    }

    List<String> indexList(List<String> list) {
        List<String> out = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) out.add(i + "\t" + list.get(i));
        return out;
    }

    List<String> indexMap(Map<String, String> map) {
        List<String> out = new ArrayList<>();
        int i = 0;
        for (var e : map.entrySet())
            out.add(i++ + "\t" + e.getKey() + "\t" + e.getValue());
        return out;
    }

    void write(String file, String header, List<String> data) throws IOException {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(file))) {
            if (header != null) w.write(header + "\n");
            for (String s : data) w.write(s + "\n");
        }
    }
}
