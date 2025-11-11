import java.io.*;
import java.util.*;

public class SimplePass1 {
    Map<String, String> optab = Map.ofEntries(
        Map.entry("STOP", "IS,00"), Map.entry("ADD", "IS,01"), Map.entry("SUB", "IS,02"),
        Map.entry("MULT", "IS,03"), Map.entry("MOVER", "IS,04"), Map.entry("MOVEM", "IS,05"),
        Map.entry("COMP", "IS,06"), Map.entry("BC", "IS,07"), Map.entry("DIV", "IS,08"),
        Map.entry("READ", "IS,09"), Map.entry("PRINT", "IS,10"),
        Map.entry("START", "AD,01"), Map.entry("END", "AD,02"), Map.entry("ORIGIN", "AD,03"),
        Map.entry("EQU", "AD,04"), Map.entry("LTORG", "AD,05"),
        Map.entry("DC", "DL,01"), Map.entry("DS", "DL,02")
    );
    Map<String, String> regtab = Map.of("AREG", "1", "BREG", "2", "CREG", "3");
    Map<String, String> condtab = Map.of("LT", "1", "LE", "2", "EQ", "3", "GT", "4", "GE", "5", "ANY", "6");

    List<String> symtab = new ArrayList<>(), littab = new ArrayList<>(), ic = new ArrayList<>();
    List<Integer> symaddr = new ArrayList<>(), litaddr = new ArrayList<>(), pooltab = new ArrayList<>(List.of(0));
    int lc = 0;

    int symIndex(String s) {
        if (!symtab.contains(s)) { symtab.add(s); symaddr.add(-1); }
        return symtab.indexOf(s);
    }

    int litIndex(String l) {
        for (int i = pooltab.get(pooltab.size() - 1); i < littab.size(); i++)
            if (littab.get(i).equals(l)) return i;
        littab.add(l); litaddr.add(-1);
        return littab.size() - 1;
    }

    void processLiterals() {
        for (int i = pooltab.get(pooltab.size() - 1); i < littab.size(); i++) {
            litaddr.set(i, lc);
            String v = littab.get(i).replaceAll("[=' ]", "");
            ic.add(lc++ + "\t(DL,01) (C," + v + ")");
        }
        pooltab.add(littab.size());
    }

    void run(String file) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            for (String line; (line = br.readLine()) != null;) {
                String[] p = line.trim().replaceAll(",", " ").split("\\s+");
                if (p.length == 0) continue;
                int i = 0;
                if (!optab.containsKey(p[0])) { symaddr.set(symIndex(p[0]), lc); i++; }

                String m = p[i++], op1 = (i < p.length ? p[i++] : null), op2 = (i < p.length ? p[i] : null);
                String[] info = optab.get(m).split(","); String type = info[0], code = info[1];

                switch (type) {
                    case "AD" -> {
                        if (m.equals("START")) { lc = Integer.parseInt(op1); ic.add("(AD," + code + ") (C," + op1 + ")"); }
                        else if (m.equals("END")) { processLiterals(); ic.add("(AD," + code + ")"); }
                        else if (m.equals("LTORG")) { ic.add(lc + "\t(AD," + code + ")"); processLiterals(); }
                    }
                    case "DL" -> {
                        ic.add(lc + "\t(DL," + code + ") (C," + op1.replaceAll("['=]", "") + ")");
                        lc += m.equals("DS") ? Integer.parseInt(op1) : 1;
                    }
                    case "IS" -> {
                        String icLine = lc + "\t(IS," + code + ") ";
                        if (m.equals("STOP")) { ic.add(icLine); lc++; continue; }
                        if (m.equals("READ") || m.equals("PRINT")) { ic.add(icLine + "(S," + symIndex(op1) + ")"); lc++; continue; }
                        icLine += m.equals("BC") ? "(" + condtab.get(op1) + ") " : "(" + regtab.get(op1) + ") ";
                        icLine += op2.startsWith("=") ? "(L," + litIndex(op2) + ")" : "(S," + symIndex(op2) + ")";
                        ic.add(icLine); lc++;
                    }
                }
            }
        }
        writeFiles();
    }

    void writeFiles() throws IOException {
        write("ic.txt", ic);
        write("symtab.txt", List.of("--- Symbol Table ---", "Index\tSymbol\tAddress"));
        for (int i = 0; i < symtab.size(); i++)
            append("symtab.txt", i + "\t" + symtab.get(i) + "\t" + symaddr.get(i));
        write("littab.txt", List.of("--- Literal Table ---", "Index\tLiteral\tAddress"));
        for (int i = 0; i < littab.size(); i++)
            append("littab.txt", i + "\t" + littab.get(i) + "\t" + litaddr.get(i));
        write("pooltab.txt", List.of("--- Pool Table ---", "Pool Start Index"));
        for (int i : pooltab) append("pooltab.txt", "" + i);
    }

    void write(String f, List<String> l) throws IOException { try (BufferedWriter w = new BufferedWriter(new FileWriter(f))) { for (String s : l) w.write(s + "\n"); } }
    void append(String f, String s) throws IOException { try (BufferedWriter w = new BufferedWriter(new FileWriter(f, true))) { w.write(s + "\n"); } }

    public static void main(String[] a) {
        try { new SimplePass1().run("input.asm"); 
        System.out.println("Pass-1 done. Check output files."); }
        catch (IOException e) { e.printStackTrace(); }
    }
}
