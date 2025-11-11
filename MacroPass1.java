import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MacroPass1 {

    // --- Core Data Structures ---
    // MNT: Name -> "ppCount,kpCount,mdtIndex,kpdIndex,pntIndex"
    HashMap<String, String> mnt = new LinkedHashMap<>();
    ArrayList<String> mdt = new ArrayList<>();
    ArrayList<String> pntab = new ArrayList<>();
    HashMap<String, String> kpdtab = new LinkedHashMap<>(); // ParamName -> DefaultValue
    ArrayList<String> ic = new ArrayList<>(); // Intermediate Code
    
    boolean isDefiningMacro = false;
    HashMap<String, String> ala = new HashMap<>(); // Argument List Array
    int mdtIdx = 0;

    public static void main(String[] args) {
        try {
            new MacroPass1().run("macro_input.asm"); 
            System.out.println("Macro Pass-I complete. Check output files.");
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void run(String inputFile) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim().replaceAll(",", " ").replaceAll("\\s+", " ");
                if (line.isEmpty() || line.startsWith(";")) continue;

                String[] parts = line.split(" ");
                String mnemonic = parts[0];

                if (mnemonic.equals("MACRO")) {
                    isDefiningMacro = true;
                    line = br.readLine().trim().replaceAll(",", " ").replaceAll("\\s+", " ");
                    processPrototype(line);
                } else if (mnemonic.equals("MEND")) {
                    isDefiningMacro = false;
                    mdt.add("MEND");
                    mdtIdx++;
                    ala.clear(); 
                } else if (isDefiningMacro) {
                    mdt.add(substituteArgs(line));
                    mdtIdx++;
                } else {
                    ic.add(line);
                }
            }
        }
        writeOutputFiles();
    }

    private void processPrototype(String line) {
        ala.clear();
        String[] parts = line.split(" ");
        String macroName = parts[0];
        
        int ppCount = 0;
        int kpCount = 0;
        int pntabStart = pntab.size(); 
        int kpdtabStart = kpdtab.size();
        List<String> paramNames = new ArrayList<>(); 

        for (int i = 1; i < parts.length; i++) {
            String param = parts[i];
            String paramName = param.split("=")[0];
            
            pntab.add(paramName); 
            paramNames.add(paramName); 

            if (param.contains("=")) {
                kpCount++;
                String defaultValue = (param.split("=").length > 1) ? param.split("=")[1] : ""; 
                kpdtab.put(paramName, defaultValue);
            } else {
                ppCount++;
            }
        }

        // Store MNT row as a comma-separated string
        String mntRow = String.format("%d,%d,%d,%d,%d", ppCount, kpCount, mdtIdx, kpdtabStart, pntabStart);
        mnt.put(macroName, mntRow);

        // Build ALA (e.g., "&O" -> "?1")
        for (int i = 0; i < paramNames.size(); i++) {
            ala.put(paramNames.get(i), "?" + (i + 1));
        }

        mdt.add(substituteArgs(line)); // Add prototype to MDT
        mdtIdx++;
    }

    /** Replaces formal args (e.g., &O) with positional args (e.g., ?1) */
    private String substituteArgs(String line) {
        String substitutedLine = line;
        for (String arg : ala.keySet()) {
            substitutedLine = substitutedLine.replaceAll("\\b" + arg + "\\b", ala.get(arg));
        }
        return substitutedLine;
    }

    /** Writes all tables to their respective output files. */
    private void writeOutputFiles() throws IOException {
        try (BufferedWriter w = new BufferedWriter(new FileWriter("mnt.txt"))) {
            w.write("--- Macro Name Table ---\n");
            w.write("Name\t#PP\t#KP\tMDT_Idx\tKPD_Idx\tPNT_Idx\n");
            for (Map.Entry<String, String> entry : mnt.entrySet()) {
                String[] parts = entry.getValue().split(",");
                w.write(String.format("%s\t%s\t%s\t%s\t%s\t%s\n", entry.getKey(), parts[0], parts[1], parts[2], parts[3], parts[4]));
            }
        }

        try (BufferedWriter w = new BufferedWriter(new FileWriter("mdt.txt"))) {
            w.write("--- Macro Definition Table ---\nIndex\tDefinition\n");
            for (int i = 0; i < mdt.size(); i++) {
                w.write(i + "\t" + mdt.get(i) + "\n");
            }
        }

        try (BufferedWriter w = new BufferedWriter(new FileWriter("pntab.txt"))) {
            w.write("--- Parameter Name Table ---\nIndex\tParam_Name\n");
            for (int i = 0; i < pntab.size(); i++) {
                w.write(i + "\t" + pntab.get(i) + "\n");
            }
        }

        try (BufferedWriter w = new BufferedWriter(new FileWriter("kpdtab.txt"))) {
            w.write("--- Keyword Parameter Default Table ---\nIndex\tParam_Name\tDefault_Value\n");
            int i = 0;
            for (Map.Entry<String, String> entry : kpdtab.entrySet()) {
                w.write(i++ + "\t" + entry.getKey() + "\t" + entry.getValue() + "\n");
            }
        }
        
        try (BufferedWriter w = new BufferedWriter(new FileWriter("pass1_intermediate.asm"))) {
            for (String s : ic) {
                w.write(s + "\n");
            }
        }
    }
}