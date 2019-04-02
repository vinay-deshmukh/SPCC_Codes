package pass2;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static pass2.AssemberPass2.SPLIT;

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

    void updateAndRefreshParams(List<String> new_params){
        // This method will act as setter to params
        // and also replace all the previoues param occurences in the this.lines

        if(this.params.size() != new_params.size()){
            throw new RuntimeException("newer params List can't be different size");
        }

        for(int pi=0; pi<this.params.size(); pi++){
            for(int li=0; li<this.lines.size(); li++){
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

public class AssemberPass2 {
    static final String SPLIT = "\\s+|,\\s+";
    List<Macro> macroList;
    List< List<String> > macroNameTable = new ArrayList<>();
    List< List<String> > macroDefTable = new ArrayList<>();

    // Macro and the entire line where it's called
    List<Pair<Macro, String>> macroUsage = new ArrayList<>();
    List< String > argListArray = new ArrayList<>();

    AssemberPass2(String full){
        String []input = full.split("\n");

        for(int i=0; i< input.length; i++){
            //System.out.println(i + "=" + input[i]);
        }

        //region Get list of macros
        macroList = this.preprocess(input);
        System.out.println("===Macro macroList beg====");
        for(Macro line : macroList){
            System.out.println(line);
        }
        System.out.println("===Macro macroList end====");
        //endregion

        //region Get macro usages / real var names
        for(String line : input){
            for(Macro macro : macroList){
                if(line.startsWith(macro.name)
                        && !line.contains("&")){
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
            if(input[i].length() < 1
                    || !input[i].startsWith("MACRO")
            ){
                continue;
            }

            i++; // dont add "MACRO"

            List<String> m_lines = new ArrayList<>();
            while( !input[i].equals("MEND")){
                m_lines.add(input[i]);
                i++;
            }
            m_lines.add(input[i]); // add MEND too

            Macro macro = new Macro(m_lines);
            macroList.add( macro );
        }
        return macroList;
    }

    void performPass1(){
        for(Macro macro : this.macroList){

            // get macro def index
            Integer macroDefIndex = macroDefTable.size();

            // add to macroNameTable
            macroNameTable.add(
                    Arrays.asList( macro.name, macroDefIndex.toString())
            );

            // Add lines of macro to macro def table
            //TODO: might change implementation of MDT
            macro.lines.stream()
                    //.map(w -> Arrays.asList(w.split("\\s+|,")))
                    .forEach(macroDefTable::add);

            // Populate argListArray
            macro.params.forEach(argListArray::add);

        }
    }

    void performPass2(){
        for(Pair<Macro, String> p : macroUsage){
            Macro macro = p.getKey();
            String line = p.getValue();

            List<String> new_params =
                    Arrays.asList(line.split(SPLIT));

            // Ignore first element as it's macro name
            new_params = new_params.subList(1, new_params.size());

            macro.updateAndRefreshParams(new_params);
        }


        // Clear the tables
        macroDefTable.clear();
        macroNameTable.clear();
        argListArray.clear();
        // Rebuild data structures
        this.performPass1();

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
            System.out.printf("%5d | %s\n",
                    i, row);
        }
        System.out.println("===Macro Def Table end===");

        System.out.println("===Arg List Array beg===");
        System.out.println("Index | Arg Name");
        for(int i=0;i<argListArray.size();i++){
            System.out.printf("%5d | %s\n",
                    i, argListArray.get(i));
        }
        System.out.println("===Arg List Array beg===");

        System.out.println("===Macro Usage Table beg===");
        for(Pair<Macro, String> p : macroUsage){
            Macro macro = p.getKey();
            String line = p.getValue();
            System.out.println(macro.name + "=" + line);
        }
        System.out.println("===Macro Usage Table beg===");
    }

}

/*
Output:
pass 1 3 tables (with temp nums)
pass 2 3 tables


3 tables ==
macro name table
macro definition table
argument list array

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
