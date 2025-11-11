import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class SimplePass1 {

    HashMap<String, String> optab = new HashMap<>();
    HashMap<String, String> regtab = new HashMap<>();
    HashMap<String, String> condtab = new HashMap<>();

    ArrayList<String> symtab_list = new ArrayList<>();
    ArrayList<Integer> symtab_addr = new ArrayList<>();
    
    ArrayList<String> littab_list = new ArrayList<>();
    ArrayList<Integer> littab_addr = new ArrayList<>();
    
    ArrayList<Integer> pooltab = new ArrayList<>();
    ArrayList<String> ic = new ArrayList<>();
    
    int lc = 0;

    public SimplePass1() {
        pooltab.add(0); 

        optab.put("STOP", "IS,00");  optab.put("ADD", "IS,01");   optab.put("SUB", "IS,02");
        optab.put("MULT", "IS,03"); optab.put("MOVER", "IS,04");  optab.put("MOVEM", "IS,05");
        optab.put("COMP", "IS,06");  optab.put("BC", "IS,07");    optab.put("DIV", "IS,08");
        optab.put("READ", "IS,09");  optab.put("PRINT", "IS,10");
        optab.put("START", "AD,01"); optab.put("END", "AD,02");   optab.put("ORIGIN", "AD,03");
        optab.put("EQU", "AD,04");   optab.put("LTORG", "AD,05");
        optab.put("DC", "DL,01");    optab.put("DS", "DL,02");
        regtab.put("AREG", "1");    regtab.put("BREG", "2");    regtab.put("CREG", "3");
        condtab.put("LT", "1");     condtab.put("LE", "2");     condtab.put("EQ", "3");
        condtab.put("GT", "4");     condtab.put("GE", "5");     condtab.put("ANY", "6");
    }

    private int getSymbolIndex(String symbol) {
        if (!symtab_list.contains(symbol)) {
            symtab_list.add(symbol);
            symtab_addr.add(-1); 
        }
        return symtab_list.indexOf(symbol);
    }

    private int getLiteralIndex(String literal) {
        int poolStart = pooltab.get(pooltab.size() - 1);
        for (int i = poolStart; i < littab_list.size(); i++) {
            if (littab_list.get(i).equals(literal)) {
                return i;
            }
        }
        littab_list.add(literal);
        littab_addr.add(-1);
        return littab_list.size() - 1;
    }

    private void processLiteralPool() {
        int poolStart = pooltab.get(pooltab.size() - 1);
        for (int i = poolStart; i < littab_list.size(); i++) {
            littab_addr.set(i, lc); 
            String litValue = littab_list.get(i).replaceAll("[=' ]", "");
            ic.add(lc + "\t(DL, 01) (C, " + litValue + ")");
            lc++;
        }
        pooltab.add(littab_list.size()); 
    }

    public void run(String inputFile) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.trim().replaceAll(",", " ").replaceAll("\\s+", " ").split(" ");
                if (parts.length == 0 || parts[0].isEmpty()) continue;

                String label = null;
                int partIdx = 0;

                if (!optab.containsKey(parts[0])) {
                    label = parts[partIdx++];
                    int symIndex = getSymbolIndex(label);
                    symtab_addr.set(symIndex, lc); 
                }

                String mnemonic = parts[partIdx++];
                String op1 = (partIdx < parts.length) ? parts[partIdx++] : null;
                String op2 = (partIdx < parts.length) ? parts[partIdx++] : null;
                
                String opInfo = optab.get(mnemonic);
                String opType = opInfo.split(",")[0];
                String opCode = opInfo.split(",")[1];
                String icLine = "";

                switch (opType) {
                    case "AD": 
                        if (mnemonic.equals("START")) {
                            lc = Integer.parseInt(op1);
                            ic.add("(AD, " + opCode + ") (C, " + op1 + ")");
                        } else if (mnemonic.equals("END")) {
                            processLiteralPool(); 
                            ic.add("(AD, " + opCode + ")");
                        } else if (mnemonic.equals("LTORG")) {
                            ic.add(lc + "\t(AD, " + opCode + ")");
                            processLiteralPool(); 
                        }
                        break;
                    
                    case "DL": 
                        icLine = lc + "\t(DL, " + opCode + ") ";
                        icLine += "(C, " + op1.replaceAll("['=]", "") + ")";
                        ic.add(icLine);
                        lc += mnemonic.equals("DS") ? Integer.parseInt(op1) : 1;
                        break;

                    case "IS": 
                        icLine = lc + "\t(IS, " + opCode + ") ";
                        
                        if (mnemonic.equals("STOP")) {
                            ic.add(icLine); lc++; continue;
                        }
                        
                        if (mnemonic.equals("READ") || mnemonic.equals("PRINT")) {
                            icLine += "(S, " + getSymbolIndex(op1) + ")";
                            ic.add(icLine); lc++; continue;
                        }

                        if (mnemonic.equals("BC")) {
                            icLine += "(" + condtab.get(op1) + ") ";
                        } else {
                            icLine += "(" + regtab.get(op1) + ") ";
                        }
                        
                        if (op2.startsWith("=")) {
                            icLine += "(L, " + getLiteralIndex(op2) + ")";
                        } else {
                            icLine += "(S, " + getSymbolIndex(op2) + ")";
                        }
                        ic.add(icLine); lc++;
                        break;
                }
            }
        }
        writeTables(); 
    }

    private void writeTables() throws IOException {
        try (BufferedWriter w = new BufferedWriter(new FileWriter("ic.txt"))) {
            for (String s : ic) w.write(s + "\n");
        }
        
        try (BufferedWriter w = new BufferedWriter(new FileWriter("symtab.txt"))) {
            w.write("--- Symbol Table ---\nIndex\tSymbol\tAddress\n");
            for (int i = 0; i < symtab_list.size(); i++) {
                w.write(i + "\t" + symtab_list.get(i) + "\t" + symtab_addr.get(i) + "\n");
            }
        }
        
        try (BufferedWriter w = new BufferedWriter(new FileWriter("littab.txt"))) {
            w.write("--- Literal Table ---\nIndex\tLiteral\tAddress\n");
            for (int i = 0; i < littab_list.size(); i++) {
                w.write(i + "\t" + littab_list.get(i) + "\t" + littab_addr.get(i) + "\n");
            }
        }
        
        try (BufferedWriter w = new BufferedWriter(new FileWriter("pooltab.txt"))) {
            w.write("--- Pool Table ---\nPool Start Index\n");
            for (int i : pooltab) w.write(i + "\n");
        }
    }

    public static void main(String[] args) {
        try {
            new SimplePass1().run("input.asm");
            System.out.println("Pass-1 (Simple) done. Check output files.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
