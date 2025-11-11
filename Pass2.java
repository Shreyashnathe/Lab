//import java.io.BufferedReader;
//import java.io.BufferedWriter;
//import java.io.FileNotFoundException;
//import java.io.FileReader;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.util.HashMap;

//public class Pass2 {

//  HashMap<Integer, Integer> symtab = new HashMap<>();
//  HashMap<Integer, Integer> littab = new HashMap<>();

//  public static void main(String[] args) {
//      try {
//          new Pass2().run();
//      } catch (IOException e) {
//          e.printStackTrace();
//      }
//  }

//  public void run() throws IOException {
//      // --- Load Symbol Table ---
//      try (BufferedReader br = new BufferedReader(new FileReader("symtab.txt"))) {
//          br.readLine(); // Skip header
//          br.readLine(); // Skip column names
//          String line;
//          while ((line = br.readLine()) != null) {
//              String[] parts = line.trim().split("\t");
//              if (parts.length >= 3) {
//                  symtab.put(Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[2].trim()));
//              }
//          }
//      } catch (FileNotFoundException e) {
//          System.out.println("Note: symtab.txt not found.");
//      } catch (Exception e) {
//          e.printStackTrace();
//      }
     
//      // --- Load Literal Table ---
//      try (BufferedReader br = new BufferedReader(new FileReader("littab.txt"))) {
//          br.readLine(); // Skip header
//          br.readLine(); // Skip column names
//          String line;
//          while ((line = br.readLine()) != null) {
//              String[] parts = line.trim().split("\t");
//              if (parts.length >= 3) {
//                  littab.put(Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[2].trim()));
//              }
//          }
//      } catch (FileNotFoundException e) {
//          System.out.println("Note: littab.txt not found.");
//      } catch (Exception e) {
//          e.printStackTrace();
//      }

//      // --- Generate Machine Code (Inlined) ---
//      try (BufferedReader icReader = new BufferedReader(new FileReader("ic.txt"));
//           BufferedWriter mcWriter = new BufferedWriter(new FileWriter("machine_code.txt"))) {

//          String line;
//          while ((line = icReader.readLine()) != null) {
             
//              String trimmedLine = line.trim();
//              if (trimmedLine.isEmpty() || trimmedLine.startsWith("(")) {
//                  continue; // Skip AD lines or blank lines
//              }
             
//              String[] lineParts = trimmedLine.split("\t");
//              if (lineParts.length < 2) {
//                  continue; // Skip malformed lines
//              }

//              String location = lineParts[0];

//              // Robust IC Parsing
//              String[] rawParts = lineParts[1].split("\\) *\\(");
//              String[] parts = new String[rawParts.length];
//              for (int i = 0; i < rawParts.length; i++) {
//                  parts[i] = rawParts[i].replaceAll("[()\\t]", "").trim().replaceAll(", *", ",");
//              }
             
//              if (parts.length == 0 || parts[0].isEmpty()) continue;

//              String[] opInfo = parts[0].split(","); 
//              if (opInfo.length < 2) continue; 
             
//              String type = opInfo[0];
//              String opCode = opInfo[1];
             
//              if (type.equals("DL")) {
//                  if (opCode.equals("01")) { // DC
//                      String constantValue = "0"; 
//                      if (parts.length > 1) {
//                          String[] constantParts = parts[1].split(",");
//                          if (constantParts.length > 1 && !constantParts[1].trim().isEmpty()) {
//                              constantValue = constantParts[1].trim();
//                          }
//                      }
//                      // Clean non-numeric characters
//                      String cleanedConstant = constantValue.replaceAll("[^0-9]", "");
//                      if (cleanedConstant.isEmpty()) cleanedConstant = "0"; 
//                      mcWriter.write(location + "\t00 0 " + String.format("%03d", Integer.parseInt(cleanedConstant)) + "\n");
//                  }
//              } else if (type.equals("IS")) {
//                  String reg = "0";
//                  String address = "000";
                 
//                  if (parts.length == 1) { 
//                      // 0 operands (STOP)
//                  } else if (parts.length == 2) { 
//                      // 1 operand (READ, PRINT)
//                      String[] op1 = parts[1].split(",");
//                      if (op1.length > 1) { 
//                         address = String.format("%03d", symtab.get(Integer.parseInt(op1[1])));
//                      }
//                  } else if (parts.length == 3) { 
//                      // 2 operands (MOVER, ADD, BC, etc.)
//                      reg = parts[1]; 
//                      String[] op2 = parts[2].split(",");
                     
//                      if (op2.length > 1) { 
//                          int index = Integer.parseInt(op2[1].trim());
//                          if (op2[0].equals("S")) {
//                              address = String.format("%03d", symtab.get(index));
//                          } else { // "L"
//                              address = String.format("%03d", littab.get(index));
//                          }
//                      } else {
//                          System.err.println("Malformed IC line (operand error): " + line);
//                      }
//                  }
//                  mcWriter.write(location + "\t" + opCode + " " + reg + " " + address + "\n");
//              }
//          }
//      }
     
//      System.out.println("Pass-2 (Minimal) done. Check machine_code.txt.");
//  }
//}

import java.io.*;
import java.util.HashMap;

public class Pass2 {

 public static void main(String[] args) throws IOException {
     HashMap<Integer, Integer> symtab = new HashMap<>();
     HashMap<Integer, Integer> littab = new HashMap<>();

     // --- Load Symbol Table ---
     try (BufferedReader br = new BufferedReader(new FileReader("symtab.txt"))) {
         br.readLine(); br.readLine(); // Skip headers
         String line;
         while ((line = br.readLine()) != null) {
             String[] parts = line.trim().split("\t");
             if (parts.length >= 3) {
                 symtab.put(Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[2].trim()));
             }
         }
     } catch (FileNotFoundException e) { System.out.println("Note: symtab.txt not found."); }
     
     // --- Load Literal Table ---
     try (BufferedReader br = new BufferedReader(new FileReader("littab.txt"))) {
         br.readLine(); br.readLine(); // Skip headers
         String line;
         while ((line = br.readLine()) != null) {
             String[] parts = line.trim().split("\t");
             if (parts.length >= 3) {
                 littab.put(Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[2].trim()));
             }
         }
     } catch (FileNotFoundException e) { System.out.println("Note: littab.txt not found."); }

     // --- Generate Machine Code ---
     try (BufferedReader r = new BufferedReader(new FileReader("ic.txt"));
          BufferedWriter w = new BufferedWriter(new FileWriter("machine_code.txt"))) {
         String line;
         while ((line = r.readLine()) != null) {
             String l = line.trim();
             if (l.isEmpty() || l.startsWith("(")) continue;
             
             String[] lp = l.split("\t"); // lineParts
             if (lp.length < 2) continue;
             String loc = lp[0];

             String[] rp = lp[1].split("\\) *\\("); // rawParts
             String[] p = new String[rp.length];   // parts
             for (int i = 0; i < rp.length; i++) {
                 p[i] = rp[i].replaceAll("[()\\t]", "").trim().replaceAll(", *", ",");
             }
             
             if (p.length == 0 || p[0].isEmpty()) continue;
             String[] oi = p[0].split(","); // opInfo
             if (oi.length < 2) continue; 
             
             String t = oi[0]; // type
             String oc = oi[1]; // opCode
             
             if (t.equals("DL")) {
                 if (oc.equals("01")) { // DC
                     String cv = "0"; // constantValue
                     if (p.length > 1) {
                         String[] cp = p[1].split(","); // constantParts
                         if (cp.length > 1 && !cp[1].trim().isEmpty()) cv = cp[1].trim();
                     }
                     String cc = cv.replaceAll("[^0-9]", ""); // cleanedConstant
                     if (cc.isEmpty()) cc = "0"; 
                     w.write(loc + "\t00 0 " + String.format("%03d", Integer.parseInt(cc)) + "\n");
                 }
             } else if (t.equals("IS")) {
                 String reg = "0", a = "000"; // address
                 
                 if (p.length == 2) { // 1 operand (READ, PRINT)
                     String[] o1 = p[1].split(",");
                     if (o1.length > 1) a = String.format("%03d", symtab.get(Integer.parseInt(o1[1])));
                 } else if (p.length == 3) { // 2 operands
                     reg = p[1]; 
                     String[] o2 = p[2].split(",");
                     if (o2.length > 1) { 
                         int i = Integer.parseInt(o2[1].trim()); // index
                         a = String.format("%03d", o2[0].equals("S") ? symtab.get(i) : littab.get(i));
                     }
                 }
                 w.write(loc + "\t" + oc + " " + reg + " " + a + "\n");
             }
         }
     }
     System.out.println("Pass-2 (Minimal) done. Check machine_code.txt.");
 }
}