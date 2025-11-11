import java.io.*;
import java.util.*;

public class MacroPass1 {
    Map<String, String> mnt = new LinkedHashMap<>();
    List<String> mdt = new ArrayList<>();
    List<String> pntab = new ArrayList<>();
    Map<String, String> kpdtab = new LinkedHashMap<>();
    List<String> ic = new ArrayList<>();
    Map<String, String> ala = new LinkedHashMap<>();
    int mdtIdx = 0;

    public static void main(String[] args) throws Exception {
        new MacroPass1().run("macro_input.asm");
        System.out.println("Macro Pass-1 done. Check output files.");
    }

    void run(String file) throws Exception {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            boolean inMacro = false;
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                // Tokenize the line based on spaces/commas
                String[] p = line.split("[\\s,]+");

                if (p[0].equalsIgnoreCase("MACRO")) {
                    inMacro = true;
                    // Read the next line, which is the prototype
                    String protoLine = br.readLine().trim();
                    processProto(protoLine);
                } else if (p[0].equalsIgnoreCase("MEND")) {
                    inMacro = false;
                    mdt.add("MEND");
                    mdtIdx++;
                    ala.clear();
                } else if (inMacro) {
                    // This is a macro body line
                    mdt.add(replaceParams(line));
                    mdtIdx++;
                } else {
                    // This is regular code
                    ic.add(line);
                }
            }
        }
        writeFiles();
    }

    void processProto(String line) {
        ala.clear();
        // Split prototype line by spaces or commas
        String[] parts = line.split("[\\s,]+");
        String name = parts[0];
        int pp = 0, kp = 0, pntStart = pntab.size(), kpdStart = kpdtab.size();

        for (int i = 1; i < parts.length; i++) {
            String param = parts[i];
            String pname = param.split("=")[0];
            pntab.add(pname);
            if (param.contains("=")) {
                kp++;
                kpdtab.put(pname, param.split("=").length > 1 ? param.split("=")[1] : "");
            } else {
                pp++;
            }
        }

        mnt.put(name, pp + "," + kp + "," + mdtIdx + "," + kpdStart + "," + pntStart);
        
        // Build the ALA for substitution (e.g., "&O" -> "?1")
        for (int i = 0; i < parts.length - 1; i++) {
            String formalParam = parts[i + 1].split("=")[0];
            ala.put(formalParam, "?" + (i + 1));
        }

        mdt.add(replaceParams(line)); // Add the *processed* prototype line to MDT
        mdtIdx++;
    }

    String replaceParams(String line) {
        // *** THIS IS THE FIX ***
        // Use .replace() for literal replacement.
        // .replaceAll("\\b...") fails because '&' is not a "word" character.
        for (String formalParam : ala.keySet()) {
            line = line.replace(formalParam, ala.get(formalParam));
        }
        return line;
    }

    void writeFiles() throws Exception {
        write("mnt.txt", "--- MNT ---\nName\t#PP\t#KP\tMDT\tKPD\tPNT",
                mnt.entrySet().stream()
                        .map(e -> e.getKey() + "\t" + e.getValue().replace(",", "\t"))
                        .toList());
        write("mdt.txt", "--- MDT ---\nIdx\tDefinition", indexed(mdt));
        write("pntab.txt", "--- PNTAB ---\nIdx\tParam", indexed(pntab));
        write("kpdtab.txt", "--- KPDTAB ---\nIdx\tParam\tDefault", indexMap(kpdtab));
        write("pass1_intermediate.asm", "", ic);
    }

    List<String> indexed(List<String> l) {
        List<String> out = new ArrayList<>();
        for (int i = 0; i < l.size(); i++) out.add(i + "\t" + l.get(i));
        return out;
    }

    List<String> indexMap(Map<String, String> map) {
        List<String> out = new ArrayList<>();
        int i = 0;
        for (var e : map.entrySet()) {
            out.add(i++ + "\t" + e.getKey() + "\t" + e.getValue());
        }
        return out;
    }

    void write(String f, String h, List<String> d) throws Exception {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(f))) {
            if (!h.isEmpty()) w.write(h + "\n");
            for (String s : d) w.write(s + "\n");
        }
    }
}
