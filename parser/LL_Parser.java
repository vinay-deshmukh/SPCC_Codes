package parser;

import first_follow.Grammar;
import first_follow.Production;
import javafx.util.Pair;

import java.util.*;

public class LL_Parser{
    Grammar grammar;
    Map< Pair<String, String>, Production > parseTable = new HashMap<>();
    LL_Parser(Grammar grammar){
        this.grammar = grammar;

        for(Production p : grammar.productionList){
            String lnt = p.head;
            List<String> alpha = p.body; //	α

            //region Find first of alpha where A -> α
            Set<String> firstSet = new HashSet<>();
            int indexFirst = 0;
            boolean doNext = true;
            // this loop is used so as to account for a EPS in First() of first
            // element of production body, so we need to find First() for foll
            // elements and so on
            while( doNext && indexFirst < alpha.size() ) {
                Set<String> tmp_first = grammar.mapFirst.get(alpha.get(indexFirst));
                if (null != tmp_first && tmp_first.contains(Grammar.EPS)) {
                    doNext = true;
                    indexFirst++;
                } else {
                    doNext = false;
                }
                if (null != tmp_first)
                    firstSet.addAll(tmp_first);
            }
            //endregion

            for(String terminal : firstSet){
                //region step1: add entry to M[A, a]
                Pair<String, String> pair = new Pair<>(lnt, terminal);
                if(null == parseTable.get(pair))
                    parseTable.put( pair, p);
                else
                    throw new RuntimeException(pair + " entry already exists!");
                //endregion

                boolean follow_$ = grammar.mapFollow.get(lnt).contains(Grammar.DOLLAR);
                boolean first_eps = firstSet.contains(Grammar.EPS);

                if( first_eps ){
                    for(String b : grammar.mapFollow.get(lnt) ){
                        Pair pair1 = new Pair<>(lnt, b);
                        parseTable.put(pair1, p);
                    }
                }

                if(  first_eps && follow_$ ){
                    Pair pair1 = new Pair<>(lnt, Grammar.DOLLAR);
                    parseTable.put(pair1, p);
                }
                //endregion
            }
        }
    }

    void printParseTable(){
        System.out.println("Parsing table");
        List<String> col_headers = new ArrayList<>(grammar.Terminals);
        col_headers.add(Grammar.DOLLAR);

        String fstr_lnt = " %11s |";
        String fstr_cell = " %12s |";

        System.out.printf(fstr_lnt, "NonTerminal");
        for(String col_head : col_headers){
            System. out.printf(fstr_cell, col_head);
        }
        System.out.println();

        for(String nonterminal : this.grammar.NonTerminals){
            System.out.printf(fstr_lnt, nonterminal);
            for(String col_head : col_headers){
                Pair p = new Pair<>(nonterminal, col_head);
                Production cell = parseTable.get(p);
                if (null == cell) System.out.printf(fstr_cell, "-");
                else System.out.printf(fstr_cell, cell);
            }
            System.out.println();
        }
    }

    void parseString(String w){
        List<String> stack = new ArrayList<>(); // stacktop is size()-1 element
        stack.add(Grammar.DOLLAR);
        stack.add(this.grammar.startSymbol);

        w = w + " " + Grammar.DOLLAR; // have dollar at end of input buffer
        List<String> inp_buffer = Arrays.asList(w.split(" "));
        int ip = 0;
        String a = inp_buffer.get(ip);
        String X = stack.get(stack.size()-1);

        //region Format strs and header row
        String fstr_matched = "%-16s";
        String fstr_stack = "%15s";
        String fstr_inp = "%16s";
        String fstr_action = "%23s";
        String action = "";
        String fstr_full = fstr_matched + "|" + fstr_stack + "|" + fstr_inp + "|" + fstr_action + "\n";

        System.out.printf(fstr_full, "Matched", "Stack", "Input", "Action");
        //endregion

        //region Print first row
        List<String> row1_stack = new ArrayList<>(stack);
        Collections.reverse(row1_stack);
        System.out.printf(fstr_full,
                "",
                String.join(" ", row1_stack),
                String.join(" ", inp_buffer),
                action
                );
        //endregion

        while( !X.equals(Grammar.DOLLAR) ){

            if( X.equals(a) ){
                action = "Match " + a;
                stack.remove(stack.size()-1); // pop stack
                if(ip < inp_buffer.size() - 1)
                    a = inp_buffer.get(++ip); // inc a
            }
            else if( grammar.Terminals.contains(X)){
                throw new RuntimeException("Stack top is terminal");
            }
            else if( null == parseTable.get( new Pair<>(X, a) ) ){
                throw new RuntimeException("Table entry is an error");
            }
            else{ // parseTable has entry
                stack.remove(stack.size()-1); // pop stack
                Production yy = parseTable.get( new Pair<>(X, a) );
                action = "Output " + yy;
                // add in reverse order to stack
                new LinkedList<>(yy.body)
                        .descendingIterator()
                        .forEachRemaining(stack::add);
            }

            // if stack top is epsilon, pop it
            while( stack.get(stack.size()-1).equals(Grammar.EPS)){
                stack.remove(stack.size()-1);
            }

            X = stack.get(stack.size()-1);

            //region print_row
            List<String> reverse_list = new ArrayList<>(stack);
            Collections.reverse(reverse_list);
            System.out.printf(
                    fstr_full,
                    String.join(" ", inp_buffer.subList(0, ip+1)),
                    String.join(" ", reverse_list),
                    String.join( " ", inp_buffer.subList(ip, inp_buffer.size())),
                    action
            );
            //endregion

        }
    }
}
