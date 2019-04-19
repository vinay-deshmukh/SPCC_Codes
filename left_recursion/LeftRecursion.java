package left_recursion;

import java.util.ArrayList;
import java.util.List;

public class LeftRecursion {
    static String EPS = "ϵ";
    static void leftRecursion(String fullProd){
        String []xx = fullProd.split("->");
        String LNT = xx[0];
        String rprod = xx[1];
        String newTemp = "A'";

        String []productions = rprod.split("\\|");
        List<String> nonrecurs = new ArrayList<>();
        // Contains non recursive bodies
        List<String> recurs = new ArrayList<>();
        // Contains recursive bodies

        List<String> solved_norecurs = new ArrayList<>();
        // Contains non recursive bodies after removing left recursion
        List<String> solved_recurs = new ArrayList<>();
        // Contains recursive bodies after removing left recursion

        for(String p: productions){
            if(p.startsWith(LNT)){
                p = p.substring(1); // skip out the LNT
                recurs.add(p); // keep track of p in `recurs`
                solved_recurs.add(p + newTemp); // add solved term
            }
            else{
                nonrecurs.add(p); // keep track of p in `nonrecurs`
                solved_norecurs.add(p + newTemp); // add solved term
            }
        }

        if(nonrecurs.size() == productions.length){
            // no left recursion
            // do nothin and print input as it is
            System.out.println("INPUT:");
            System.out.println(fullProd);
            System.out.println("OUTPUT:");
            System.out.println(fullProd);
            System.out.println("Given production has no left recursion");
            System.out.println("We can proceed with Parsing");
            System.exit(0);
        }


        String strSolvedNoRecurs = String.join("|", solved_norecurs);
        String strSolvedRecurs   = String.join("|", solved_recurs);

        System.out.println("INPUT:" + fullProd);
        System.out.println("OUTPUT:");

        System.out.println(LNT + "->"  + strSolvedNoRecurs);
        System.out.println(newTemp + "->" + strSolvedRecurs + "|" + EPS);
    }
    public static void main(String[] args) {
        leftRecursion("A->Ax|B");
        leftRecursion("A->B");
        leftRecursion("A->Ax|Ay|B|C");
    }
}
/*
INPUT:A->Ax|B
OUTPUT:
A->BA'
A'->xA'|ϵ
INPUT:
A->B
OUTPUT:
A->B
Given production has no left recursion
We can proceed with Parsing

 */
