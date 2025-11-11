import java.io.*;
import java.util.*;

public class Pass2 {
    public static void main(String[] args) throws IOException {
        Map<Integer, Integer> symtab = loadTable("symtab.txt");
        Map<Integer, Integer> littab = loadTable("littab.txt");

        try (BufferedReader ic = new BufferedReader(new FileReader("ic.txt"));
             BufferedWriter mc = new BufferedWriter(new FileWriter("machine_code.txt"))) {

            String line;
            while ((line = ic.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("(")) continue;

                String[] parts = line.split("\t");
                if (parts.length < 2) continue;
                String loc = parts[0];

                String[] tokens = parts[1].replaceAll("[()]", "").split("\\s+");
                String[] info = tokens[0].split(",");
                if (info.length < 2) continue;

                String type = info[0], code = info[1];
                if (type.equals("DL") && code.equals("01")) { // DC
                    String val = tokens.length > 1 ? tokens[1].replaceAll("[^0-9]", "") : "0";
                    mc.write(loc + "\t00 0 " + String.format("%03d", Integer.parseInt(val)) + "\n");
                } 
                else if (type.equals("IS")) {
                    String reg = "0", addr = "000";
                    if (tokens.length == 2) { // READ, PRINT
                        String[] op = tokens[1].split(",");
                        addr = fmt(symtab.getOrDefault(Integer.parseInt(op[1]), 0));
                    } else if (tokens.length == 3) { // e.g. MOVER, ADD, BC
                        reg = tokens[1];
                        String[] op2 = tokens[2].split(",");
                        int idx = Integer.parseInt(op2[1]);
                        addr = fmt(op2[0].equals("S") ? symtab.get(idx) : littab.get(idx));
                    }
                    mc.write(loc + "\t" + code + " " + reg + " " + addr + "\n");
                }
            }
        }
        System.out.println("Pass-2 done. Check machine_code.txt.");
    }
    static Map<Integer, Integer> loadTable(String file) {
        Map<Integer, Integer> table = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine(); br.readLine(); // Skip headers
            for (String l; (l = br.readLine()) != null; ) {
                String[] p = l.trim().split("\t");
                if (p.length >= 3)
                    table.put(Integer.parseInt(p[0]), Integer.parseInt(p[2]));
            }
        } catch (Exception e) {
            System.out.println("Note: " + file + " not found.");
        }
        return table;
    }

    static String fmt(Integer n) { return String.format("%03d", n == null ? 0 : n); }
}
