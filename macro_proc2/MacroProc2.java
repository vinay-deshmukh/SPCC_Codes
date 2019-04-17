package macro_proc2;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static macro_proc2.MacroProc2.SPLIT;

class Macro{
    String name;
    List<String> params = new ArrayList<>();
    List< List<String> >lines = new ArrayList<>();


    Macro(List<String> m_lines){

        //region For each line in m_lines, split into list and add to this.lines
        m_lines.stream()
                .map(s -> Arrays.asList(s.split(SPLIT)))
                .forEach(lines::add);
        //endregion

        // Name is the first word of first line
        List<String> firstLine = lines.get(0);
        this.name = firstLine.get(0);

        // Add all value in first line except first to params
        params.addAll(
            firstLine.subList(1, firstLine.size())

        );

    }

    void updateAndRefreshParams(List<String> new_params, int passno){
        // This method will act as setter to params
        // and also replace all the previous param occurrences in the this.lines

        if(this.params.size() != new_params.size()){
            throw new RuntimeException("newer params List can't be different size");
        }


        for(int li=0; li<this.lines.size(); li++){
            //region Skip first line, if passno ==1
            if(1 == passno && li == 0){
                continue;
            }
            //endregion

            for(int wi=0; wi<this.lines.get(li).size(); wi++){

                String word = this.lines.get(li).get(wi);
                int indexOfWord = this.params.indexOf(word);
                if(-1 != indexOfWord){
                    // if current word is a param
                    this.lines.get(li)
                            .set(wi, new_params.get(indexOfWord));
                }
            }
        }

        // Finally change params to new_params
        this.params = new_params;

    }

    @Override
    public String toString() {
        return "Macro{" +
                "name='" + name + '\'' +
                ", params=" + params +
                ", lines=" + lines +
                '}';
    }
}

public class MacroProc2 {
    static final String SPLIT = "\\s+|,\\s+";
    List<Macro> macroList;
    List< List<String> > macroNameTable = new ArrayList<>();
    List< List<String> > macroDefTable = new ArrayList<>();

    // Macro and the entire line where it's called
    List<Pair<Macro, String>> macroUsage = new ArrayList<>();
    List< String > argListArray = new ArrayList<>();

    MacroProc2(String full){
        String []input = full.split("\n");

        //region Get list of macros
        macroList = this.preprocess(input);
//        System.out.println("===Macro macroList beg====");
//        for(Macro line : macroList){
//            System.out.println(line);
//        }
//        System.out.println("===Macro macroList end====");
        //endregion

        //region Get macro usages / real var names
        for(String line : input){
            for(Macro macro : macroList){
                if(line.startsWith(macro.name) && !line.contains("&")){
                    // & is checked since macro defines use &, so skip those lines
                    Pair p = new Pair<>(macro, line);
                    macroUsage.add(p);
                }
            }
        }
        //endregion

        System.out.println("\nPASS 1 begin");
        this.performPass1();
        this.printTables();
        System.out.println("PASS 1 end\n");

        System.out.println("\nPASS 2 begin");
        this.performPass2();
        this.printTables();
        System.out.println("PASS 2 end\n");
    }

    List<Macro> preprocess(String[] input){
        List<Macro> macroList = new ArrayList<>();
        for(int i=0; i< input.length; i++){

            //region if line isn't empty or doesnt' begin with "MACRO", continue
            if(input[i].length() < 1 || !input[i].startsWith("MACRO")){
                continue;
            }
            //endregion

            i++; // ignore "MACRO" line

            //region Collect all lines till "MEND" into the list `m_lines`
            List<String> m_lines = new ArrayList<>();
            while( !input[i].equals("MEND")){
                m_lines.add(input[i]);
                i++;
            }
            m_lines.add(input[i]); // add MEND too
            //endregion

            Macro macro = new Macro(m_lines);
            macroList.add( macro );
        }
        return macroList;
    }

    void buildDataStructures(int passno){

        //region Clear data structures
        macroNameTable.clear();
        macroDefTable.clear();
        argListArray.clear();
        //endregion

        for(Macro macro : this.macroList){

            // get macro def index
            Integer macroDefIndex = macroDefTable.size();

            // add to macroNameTable
            macroNameTable.add(
                    Arrays.asList( macro.name, macroDefIndex.toString())
            );

            // Add lines of macro to macro def table
            macroDefTable.addAll( macro.lines );

            // Populate argListArray
            //region Pass 1
            if(1 == passno){
                // This int tells how many alt names we need
                int noOfAltNames = macro.params.size();

                // This list will contain the alt names ie #0, #1
                List<String> listAltNames = new ArrayList<>();
                for(int ai=0; ai<noOfAltNames;ai++){
                    String altName = "#" + argListArray.size();
                    listAltNames.add(altName);

                    // Add argument to argListArray
                    argListArray.add(altName);
                }

                // Update macro params with alternate param names
                macro.updateAndRefreshParams(listAltNames, passno);
            }
            //endregion
            //region Pass 2
            else if(2 == passno) {

                //region Update macro param names with real param names from macroUsage
                for(Pair<Macro, String> p : macroUsage){
                    Macro updateMacro = p.getKey();
                    String line = p.getValue();

                    List<String> new_params = Arrays.asList(line.split(SPLIT));

                    // Ignore first element as it's macro name
                    new_params = new_params.subList(1, new_params.size());

                    // Refresh params with actual values from macroUsage
                    updateMacro.updateAndRefreshParams(new_params, passno);
                }
                //endregion

                // Add the updated params to argListArray
                argListArray.addAll(macro.params);
            }
            //endregion
            else{
                throw new RuntimeException("Invalid pass no");
            }
        }
    }

    void performPass1(){
        this.buildDataStructures(1);
    }

    void performPass2(){
        this.buildDataStructures(2);
    }

    void printTables(){

        System.out.println("===Macro Name Table beg==");
        System.out.println("Index | Name | MDT Index");
        for(int i=0; i<macroNameTable.size();i++){
            List<String> row = macroNameTable.get(i);
            System.out.printf("%5d | %4s | %9s\n",
                    i, row.get(0), row.get(1));
        }
        System.out.println("===Macro Name Table end==");

        System.out.println("===Macro Def Table beg===");
        System.out.println("Index | Instruction");
        for(int i=0;i<macroDefTable.size();i++){
            List<String> row = macroDefTable.get(i);
            System.out.printf("%5d | %s\n", i, row);
        }
        System.out.println("===Macro Def Table end===");

        System.out.println("===Arg List Array beg===");
        System.out.println("Index | Arg Name");
        for(int i=0;i<argListArray.size();i++){
            System.out.printf("%5d | %s\n",
                    i, argListArray.get(i));
        }
        System.out.println("===Arg List Array beg===");

//        ONLY FOR DEBUG
//        System.out.println("===Macro Usage Table beg===");
//        for(Pair<Macro, String> p : macroUsage){
//            Macro macro = p.getKey();
//            String line = p.getValue();
//            System.out.println(macro.name + "=" + line);
//        }
//        System.out.println("===Macro Usage Table beg===");
    }

}

/* OUTPUT:
PASS 1 begin
===Macro Name Table beg==
Index | Name | MDT Index
    0 |  ADD |         0
    1 |  SUB |         4
===Macro Name Table end==
===Macro Def Table beg===
Index | Instruction
    0 | [ADD, &ARG1, &ARG2]
    1 | [L, 1, #0]
    2 | [A, 1, #1]
    3 | [MEND]
    4 | [SUB, &ARG3, &ARG4]
    5 | [L, 1, #2]
    6 | [S, 1, #3]
    7 | [MEND]
===Macro Def Table end===
===Arg List Array beg===
Index | Arg Name
    0 | #0
    1 | #1
    2 | #2
    3 | #3
===Arg List Array beg===
PASS 1 end


PASS 2 begin
===Macro Name Table beg==
Index | Name | MDT Index
    0 |  ADD |         0
    1 |  SUB |         4
===Macro Name Table end==
===Macro Def Table beg===
Index | Instruction
    0 | [ADD, &ARG1, &ARG2]
    1 | [L, 1, DATA1]
    2 | [A, 1, DATA2]
    3 | [MEND]
    4 | [SUB, &ARG3, &ARG4]
    5 | [L, 1, DATA1]
    6 | [S, 1, DATA2]
    7 | [MEND]
===Macro Def Table end===
===Arg List Array beg===
Index | Arg Name
    0 | DATA1
    1 | DATA2
    2 | DATA1
    3 | DATA2
===Arg List Array beg===
PASS 2 end
 */

/*
INPUT::

START

MACRO
ADD &ARG1, &ARG2
L 1, &ARG1
A 1, &ARG2
MEND

MACRO
SUB &ARG3, &ARG4
L 1, &ARG3
S 1, &ARG4
MEND

ADD DATA1, DATA2
SUB DATA1, DATA2

DATA1 DC F'9'
DATA2 DC F'5'

END
 */
