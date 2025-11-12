import java.io.*;
import java.util.*;

public class Pass2 {
    public static void main(String[] args) {
        try {
            Map<Integer, Integer> symtab = loadTable("symtab.txt");
            Map<Integer, Integer> littab = loadTable("littab.txt");

            BufferedReader br = new BufferedReader(new FileReader("ic.txt"));
            BufferedWriter bw = new BufferedWriter(new FileWriter("machine_code.txt"));

            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("(AD")) continue; // Skip assembler directives

                // Example: 200 (IS,04) (1) (L,0)
                String[] parts = line.split("\\s+", 2);
                if (parts.length < 2) continue;

                String loc = parts[0].trim();
                String body = parts[1].trim().replaceAll("[()]", "");

                String[] tokens = body.split("\\s+");
                if (tokens.length == 0) continue;

                String[] opInfo = tokens[0].split(",");
                if (opInfo.length < 2) continue;

                String type = opInfo[0];
                String opcode = opInfo[1];
                String reg = "0", addr = "000";

                // ---------------- Declarative ----------------
                if (type.equals("DL")) {
                    if (opcode.equals("01")) { // DC
                        String val = tokens[tokens.length - 1].replaceAll("[^0-9]", "");
                        bw.write(loc + "\t00 0 " + fmt(val) + "\n");
                    } else if (opcode.equals("02")) { // DS
                        bw.write(loc + "\t00 0 000\n");
                    }
                    continue;
                }

                // ---------------- Imperative ----------------
                if (type.equals("IS")) {
                    if (opcode.equals("00")) { // STOP
                        bw.write(loc + "\t00 0 000\n");
                        continue;
                    }

                    // Find register (e.g., (1))
                    for (String t : tokens) {
                        if (t.matches("\\d+")) { // like 1, 2, 3, etc.
                            reg = t;
                            break;
                        }
                    }

                    // Find operand (symbol or literal)
                    for (String t : tokens) {
                        if (t.contains("S,")) {
                            int idx = Integer.parseInt(t.split(",")[1]);
                            addr = fmt(symtab.get(idx));
                        } else if (t.contains("L,")) {
                            int idx = Integer.parseInt(t.split(",")[1]);
                            addr = fmt(littab.get(idx));
                        }
                    }

                    bw.write(loc + "\t" + opcode + " " + reg + " " + addr + "\n");
                }
            }

            br.close();
            bw.close();
            System.out.println(" Pass-2 done. Check machine_code.txt.");

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // -------- Helper: Load Symbol or Literal Table --------
    static Map<Integer, Integer> loadTable(String filename) {
        Map<Integer, Integer> table = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            br.readLine(); // skip header
            br.readLine(); // skip column names
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.trim().split("\\t+");
                if (parts.length >= 3) {
                    try {
                        int index = Integer.parseInt(parts[0].trim());
                        int addr = Integer.parseInt(parts[2].trim());
                        table.put(index, addr);
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (IOException e) {
            System.out.println("Note: " + filename + " not found.");
        }
        return table;
    }

    static String fmt(Object n) {
        if (n == null) return "000";
        String s = n.toString().replaceAll("[^0-9]", "");
        if (s.isEmpty()) s = "0";
        return String.format("%03d", Integer.parseInt(s));
    }
}

