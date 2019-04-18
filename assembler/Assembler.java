package assembler;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Assembler {

    PrintStream output =
//            new PrintStream(System.out); // Use this to print on System.out
            new PrintStream(new ByteArrayOutputStream()); // Use this to silence debug output

    private final String entireCode;
    private final String[] codeLines;
    Pattern pat_l_a_st = Pattern.compile(
            "(?<instruction>L|ST|A)\\s+(?<register>\\d+),\\s*(?<symbol>[A-Z]+)"
    );
    Pattern pat_pseudo = Pattern.compile("START|USING|END");

    Pattern pat_dc_ds = Pattern.compile(
            "(?<symbol>[A-Z]+)\\s+(?<type>DC|DS)\\s+(?:(?<define>F'\\d+')|(?<alloc>\\d+F))");
    // <symbol> is the name of the symbol being defined
    // <type> means either DS or DC
    // <define> means when data is directly defined
    // eg F'3' or F'49'
    // <alloc> means when memory is allocated
    // eg 3F or 49F

    private Map<String, Integer> symbolLocation = new HashMap<>();
    // Store the location Counter where the symbol is defined


    List<String> pass1LOC = new ArrayList<>();
    List<String> pass2LOC = new ArrayList<>();
    String baseRegister = "-1";

    public Assembler(String input) {
        this.entireCode = input;
        this.codeLines = input.split("\n");

        this.performPass(1);
        this.performPass(2);
    }

    private void performPass(int passno){
        Integer locationCounter = 0;
        for(String oneline: codeLines){
            output.println("LINE:" + oneline);
            Matcher mat_pseudo = pat_pseudo.matcher(oneline);
            Matcher mat_l_a_st = pat_l_a_st.matcher(oneline);
            Matcher mat_dc_ds = pat_dc_ds.matcher(oneline);

            if(mat_pseudo.find()){
                // PSEUDO CODE
                output.println("PSEUDO");
                String pseudoOp = mat_pseudo.group();
                output.println("\t" + pseudoOp);

                if(pseudoOp.equals("USING")){
                    String fifteen =
                            oneline.substring(oneline.indexOf(", ")+2, oneline.length());
                    baseRegister = fifteen;
                }

                // Add blank entry
                switch(passno){
                    case 1: pass1LOC.add("$"); break;
                    case 2: pass2LOC.add("$"); break;
                    default: throw new RuntimeException("invalid passno :" + passno);
                }
            }
            else if(mat_l_a_st.find()){
                //MACHINE OP
                output.println("MACHINE");
                String instruction = mat_l_a_st.group("instruction");
                String register = mat_l_a_st.group("register");
                String symbol = mat_l_a_st.group("symbol");
                output.println("\tINSTRUCTION:" + instruction);
                output.println("\tREGISTER   :" + register);
                output.println("\tSYMBOL     :" + symbol);


                // Add loc entry only for required pass
                switch(passno){
                    case 1:
                        String entry1 = locationCounter + " "
                                + instruction + " "
                                + register + ", "
                                + "_(0, " + baseRegister + ")";
                        pass1LOC.add(entry1);
                        break;
                    case 2:
                        String entry2 = locationCounter + " "
                                + instruction + " "
                                + register + ", "
                                + this.symbolLocation.get(symbol)
                                + "(0, " + baseRegister + ")";
                        pass2LOC.add(entry2);
                        break;
                    default:
                        throw new RuntimeException("invalid pass : " + passno);
                }

                locationCounter = locationCounter + 4;
            }
            else if(mat_dc_ds.find()){
                //DATA INSTRUCTION
                output.println("DATA");
                String symbol = mat_dc_ds.group("symbol");
                String type   = mat_dc_ds.group("type");
                String define = mat_dc_ds.group("define");
                String alloc  = mat_dc_ds.group("alloc");
                output.println("\tSYMBOL:" + symbol);
                output.println("\tTYPE  :" + type);
                output.println("\tDEFINE:" + define);
                output.println("\tALLOC :" + alloc);

                String value = null;
                // this variable is shown in the pass listing
                if(null != define){
                    // when value defined directly
                    // skip F' and '
                    value = define.substring(2, define.length()-1);
                } else if(null != alloc){
                    // when value is allocated
                    value = "-";
                }

                // Insert symbol location
                this.symbolLocation.put(symbol, locationCounter);


                String entry = locationCounter + " "
                        + value;

                switch(passno){
                    case 1: pass1LOC.add(entry); break;
                    case 2: pass2LOC.add(entry); break;
                }
                locationCounter = locationCounter + 4;
            }
            else {
                throw new RuntimeException("Unmatched line! :" + oneline);
            }
        }

        // assert that for all codeLines, we have an entry in `pass1LOC` or `pass2LOC`
        if(1 == passno && pass1LOC.size() != codeLines.length)
            throw new RuntimeException("pass1 doesnt have as many entries as codeLines");
        if(2 == passno && pass2LOC.size() != codeLines.length)
            throw new RuntimeException("pass2 doesnt have as many entries as codeLines");
    }

}
