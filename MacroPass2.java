import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

// Helper class to store MNT data, read from mnt.txt
class MntRowPass2 {
    String name;
    int mdtIndex;
    int ppCount;
    int kpCount;
    int pntabIndex;
    int kpdtabIndex;

    public MntRowPass2(String name, int ppCount, int kpCount, int mdtIndex, int kpdtabIndex, int pntabIndex) {
        this.name = name;
        this.ppCount = ppCount;
        this.kpCount = kpCount;
        this.mdtIndex = mdtIndex;
        this.kpdtabIndex = kpdtabIndex;
        this.pntabIndex = pntabIndex;
    }
}

public class MacroPass2 {

    HashMap<String, MntRowPass2> mnt = new LinkedHashMap<>();
    ArrayList<String> mdt = new ArrayList<>();
    ArrayList<String> pntab = new ArrayList<>();
    HashMap<String, String> kpdtab = new LinkedHashMap<>(); // ParamName -> DefaultValue
    ArrayList<String> expandedCode = new ArrayList<>();

    public static void main(String[] args) {
        try {
            new MacroPass2().run();
            System.out.println("Macro Pass-II complete.");
            System.out.println("Check expanded_code.asm for final output.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() throws IOException {
        loadMnt("mnt.txt");
        loadMdt("mdt.txt");
        loadPntab("pntab.txt");
        loadKpdtab("kpdtab.txt");
        expandMacros("pass1_intermediate.asm");
        writeExpandedCode("expanded_code.asm");
    }

    private void loadMnt(String filename) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            br.readLine(); br.readLine(); // Skip headers
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.trim().split("\t");
                if (parts.length >= 6) {
                    String name = parts[0];
                    int pp = Integer.parseInt(parts[1]);
                    int kp = Integer.parseInt(parts[2]);
                    int mdtIdx = Integer.parseInt(parts[3]);
                    int kpdIdx = Integer.parseInt(parts[4]);
                    int pntIdx = Integer.parseInt(parts[5]);
                    mnt.put(name, new MntRowPass2(name, pp, kp, mdtIdx, kpdIdx, pntIdx));
                }
            }
        }
    }

    private void loadMdt(String filename) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            br.readLine(); br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t", 2); 
                mdt.add((parts.length >= 2) ? parts[1] : "");
            }
        }
    }

    private void loadPntab(String filename) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            br.readLine(); br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length >= 2) pntab.add(parts[1]);
            }
        }
    }

    private void loadKpdtab(String filename) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            br.readLine(); br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length >= 3) {
                    kpdtab.put(parts[1], parts[2]); // ParamName -> DefaultValue
                } else if (parts.length == 2) {
                     kpdtab.put(parts[1], ""); // Handle empty default
                }
            }
        }
    }

    private void expandMacros(String filename) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                String[] parts = line.split(" ");
                String mnemonic = parts[0];
                
                if (mnt.containsKey(mnemonic)) {
                    expandMacroCall(parts);
                } else {
                    expandedCode.add(line);
                }
            }
        }
    }

    private void expandMacroCall(String[] callParts) {
        String macroName = callParts[0];
        MntRowPass2 mntInfo = mnt.get(macroName);
        
        HashMap<String, String> callAla = new HashMap<>(); // Maps ?1 -> "O", ?2 -> "9", ?3 -> "AREG"
        Map<String, String> actualParams = new LinkedHashMap<>(); // Maps &O -> "O", &N -> "9"
        
        // 1. Process actual parameters from the call
        int ppProvided = 0;
        for (int i = 1; i < callParts.length; i++) {
            String part = callParts[i];
            if (part.contains("=")) {
                String[] kp = part.split("=");
                actualParams.put(kp[0], (kp.length > 1) ? kp[1] : "");
            } else {
                ppProvided++;
                // Map positional param to its formal name from PNTAB
                String formalName = pntab.get(mntInfo.pntabIndex + ppProvided - 1);
                actualParams.put(formalName, part);
            }
        }

        // 2. Build the ALA, using defaults for missing params
        int totalParams = mntInfo.ppCount + mntInfo.kpCount;
        for (int i = 0; i < totalParams; i++) {
            String formalName = pntab.get(mntInfo.pntabIndex + i); // e.g., &O, &N, &E
            String positionalName = "?" + (i + 1); // e.g., ?1, ?2, ?3
            
            if (actualParams.containsKey(formalName)) {
                // Value was provided in the call
                callAla.put(positionalName, actualParams.get(formalName));
            } else {
                // Value not provided, use default from KPDTAB
                callAla.put(positionalName, kpdtab.get(formalName)); // Use default
            }
        }

        // 3. Loop through MDT and substitute
        int mdtIndex = mntInfo.mdtIndex + 1; // Skip prototype
        while (!mdt.get(mdtIndex).equals("MEND")) {
            String expandedLine = mdt.get(mdtIndex);
            
            // Substitute all positional placeholders (e.g., ?1, ?2)
            for (String positionalArg : callAla.keySet()) {
                // Use \b (word boundary) to replace only whole args
                expandedLine = expandedLine.replaceAll("\\b" + positionalArg + "\\b", callAla.get(positionalArg));
            }
            
            expandedCode.add(expandedLine);
            mdtIndex++;
        }
    }

    private void writeExpandedCode(String filename) throws IOException {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(filename))) {
            for (String s : expandedCode) {
                w.write(s + "\n");
            }
        }
    }
}